package com.hkxx.drone;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//控制器通信协议解码器，继承cumulative解码器来支持累积数据包解码
public class MavlinkDecoder extends CumulativeProtocolDecoder {

    private static Logger log = LoggerFactory.getLogger(MavlinkDecoder.class);
    private final AttributeKey BUFFER = new AttributeKey(getClass(), "buffer");

    @Override
    public void decode(IoSession session, IoBuffer in, ProtocolDecoderOutput out)
            throws Exception {
        // TODO Auto-generated method stub
        // super.decode(session, in, out);
        boolean usingSessionBuffer = true;
        IoBuffer buf = (IoBuffer) session.getAttribute(BUFFER);
        // If we have a session buffer, append data to that; otherwise
        // use the buffer read from the network directly.
        if (buf != null) {
            boolean appended = false;
            // Make sure that the buffer is auto-expanded.
            if (buf.isAutoExpand()) {
                try {
                    buf.put(in);
                    appended = true;
                } catch (IllegalStateException e) {
                    // A user called derivation method (e.g. slice()),
                    // which disables auto-expansion of the parent buffer.
                } catch (IndexOutOfBoundsException e) {
                    // A user disabled auto-expansion.
                }
            }

            if (appended) {
                buf.flip();
            } else {
                // Reallocate the buffer if append operation failed due to
                // derivation or disabled auto-expansion.
                buf.flip();
                IoBuffer newBuf = IoBuffer.allocate(
                        buf.remaining() + in.remaining()).setAutoExpand(true);
                newBuf.order(buf.order());
                newBuf.put(buf);
                newBuf.put(in);
                newBuf.flip();
                buf = newBuf;

                // Update the session attribute.
                session.setAttribute(BUFFER, buf);
            }
        } else {
            buf = in;
            usingSessionBuffer = false;
        }

        for (; ; ) {
            int oldPos = buf.position();
            boolean decoded = doDecode(session, buf, out);
            if (decoded) {
                if (buf.position() == oldPos) {
                    throw new IllegalStateException(
                            "doDecode() can't return true when buffer is not consumed.");
                }

                if (!buf.hasRemaining()) {
                    break;
                }
            } else {
                break;
            }
        }

        // if there is any data left that cannot be decoded, we store
        // it in a buffer in the session and next time this decoder is
        // invoked the session buffer gets appended to
        if (buf.hasRemaining()) {
            if (usingSessionBuffer && buf.isAutoExpand()) {
                buf.compact();
            } else {
                storeRemainingInSession(buf, session);
            }
        } else {
            if (usingSessionBuffer) {
                removeSessionBuffer(session);
            }
        }
    }

    // 返回true，则表示已经成功解析了一个数据包（帧），进入handler处理器详细处理；
    // 返回false，表示本次接收的数据不能完整构成一个数据包，本次解析结束，等待客户端下次发送的数据进行累积，合并数据包后再进行解析
    @Override
    protected boolean doDecode(IoSession session, IoBuffer in,
                               ProtocolDecoderOutput out) throws Exception {
        // TODO Auto-generated method stub

        boolean result = false;

        byte stx = 0; // 数据包的起始字节，即包头stx
        byte current = 0; // 当前字节内容
        int length = 0; // 单个数据包的长度
        boolean isBegin = false; // 是否找到包头

        byte stx_mavlink_v1 = (byte) (0xfe & 0xff); // mavlink第一版的包头，0xfe默认是int型，需要处理，转为byte
        byte stx_mavlink_v2 = (byte) (0xfd & 0xff); // mavlink第二版的包头
        int mavlink_version = 0; // mavlink版本号，1或者2
        byte incFlags = 0;

        int pos = 0;
        int limit = 0;

        try {
            // 扫描缓冲区，根据通信协议，解析出数据包单元（本协议的数据包单元为IoBuffer，处理更灵活）
            while (in.hasRemaining()) {
                isBegin = false; // 未找到包头
                pos = 0;
                limit = 0;
                // 根据协议解析数据包
                current = in.get(); // 读取当前字节
                if (current == stx_mavlink_v1) {
                    isBegin = true; // 找到包头，mavlink第一版
                    mavlink_version = 1;
                } else if (current == stx_mavlink_v2) {
                    isBegin = true; // 找到包头，mavlink第二版
                    mavlink_version = 2;
                }
                if (isBegin) {
                    // 找到包头了，根据协议版本，分开进行解码
                    if (mavlink_version == 1) {
                        // 第一版
                        if (in.hasRemaining()) {
                            // 取长度字节
                            length = in.get();
                            length = length & 0xff; //length必须为无符号数
                            // 与当前pos位置比较计算数据包剩余字节
                            length += 6;

                            // 判断数组剩余长度是否包含完整数据包
                            if (in.remaining() >= length) {
                                // 有完整数据包，提取包，本次解析结束，提交给处理器
                                // 保存当前的position，limit
                                pos = in.position();
                                limit = in.limit();
                                try {
                                    // 提取满足条件的数据包
                                    in.position(pos - 2);
                                    in.limit(pos + length);
                                    IoBuffer rt = in.slice();
                                    out.write(rt);
                                } catch (Exception e) {
                                    // TODO: handle exception
                                    log.error(e.getMessage());
                                } finally {
                                    // 将剩余的缓冲区做为下次调用
                                    in.position(pos + length);
                                    in.limit(limit);
                                }
                                result = true;
                                break;
                                // return true;
                            } else {
                                // 数据包不够，则累积至下次处理，pos指针回退2个字节
                                pos = in.position();
                                in.position(pos - 2);
                                result = false;
                                break;
                            }
                        } else {
                            // 数据包不够，则累积至下次处理
                            pos = in.position();
                            // 回退一个字节，把pos指针放到包头
                            in.position(pos - 1);
                            result = false;
                            break;
                            // return false;
                        }
                    } else if (mavlink_version == 2) {
                        // 第二版
                        if (in.hasRemaining()) {
                            // 取长度字节
                            length = in.get();
                            length = length & 0xff; //length必须为无符号数
                            // 判断是否包含signature数据
                            if (in.remaining() > 0) {
                                // 读取inc flags字段，根据该字段，与当前pos位置比较计算数据包剩余字节
                                incFlags = in.get();
                                if (incFlags == 0x01) {
                                    // 包含singnature数据
                                    length += 22;
                                } else {
                                    // 不含singnature
                                    length += 9;
                                }

                                // 判断数组剩余长度是否包含完整数据包
                                if (in.remaining() >= length) {
                                    // 有完整数据包，提取包，本次解析结束，提交给处理器
                                    // 保存当前的position，limit
                                    pos = in.position();
                                    limit = in.limit();
                                    try {
                                        // 提取满足条件的数据包
                                        in.position(pos - 3);
                                        in.limit(pos + length);
                                        IoBuffer rt = in.slice();
                                        out.write(rt);
                                    } catch (Exception e) {
                                        // TODO: handle exception
                                        log.error(e.getMessage());
                                    } finally {
                                        // 将剩余的缓冲区做为下次调用
                                        in.position(pos + length);
                                        in.limit(limit);
                                    }
                                    result = true;
                                    break;
                                    // return true;
                                } else {
                                    // 数据包不够，则累积至下次处理，pos指针回退3个字节
                                    pos = in.position();
                                    in.position(pos - 3);
                                    result = false;
                                    break;
                                }
                            } else {
                                // 数据包不够，则累积至下次处理，pos指针回退2个字节
                                pos = in.position();
                                in.position(pos - 2);
                                result = false;
                                break;
                            }
                        } else {
                            // 数据包不够，则累积至下次处理
                            pos = in.position();
                            // 回退一个字节，把pos指针放到包头
                            in.position(pos - 1);
                            result = false;
                            break;
                            // return false;
                        }

                    }
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }

        return result;
    }

    private void removeSessionBuffer(IoSession session) {
        session.removeAttribute(BUFFER);
    }

    private void storeRemainingInSession(IoBuffer buf, IoSession session) {
        final IoBuffer remainingBuf = IoBuffer.allocate(buf.capacity())
                .setAutoExpand(true);

        remainingBuf.order(buf.order());
        remainingBuf.put(buf);

        session.setAttribute(BUFFER, remainingBuf);
    }
}
