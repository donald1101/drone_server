package com.hkxx.drone;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.AttributeKey;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.CumulativeProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolDecoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// 管理控制端通信协议解码器，继承cumulative解码器来支持累积数据包解码
public class CommandDecoder extends CumulativeProtocolDecoder {

    private static Logger log = LoggerFactory.getLogger(CommandDecoder.class);
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

    // private void analyzeData(String data) {
    // // 处理datas，进行协议解析，需要进行分包处理
    // byte[] datas = CHexConver.hexStr2Bytes(data);
    // String test = Convert.bytesToHexString(datas, true);
    //
    // // 默认普通模式
    // boolean beginAA = false;
    // boolean beginBB = false;
    // int idxStart = 0;
    // int idxEnd = 0;
    // int len = 0;
    //
    // // 方法二：合并上次缓存的数据
    // byte[] totalBuf = datas;
    // int totalLen = datas.length;
    // if (lastDataBuf != null) {
    // totalBuf = new byte[lastDataBuf.length + datas.length];
    // System.arraycopy(lastDataBuf, 0, totalBuf, 0, lastDataBuf.length);
    // System.arraycopy(datas, 0, totalBuf, lastDataBuf.length,
    // datas.length);
    // totalLen = totalBuf.length; // 更新处理数据的长度
    // // 清空上次缓存
    // lastDataBuf = null;
    // }
    //
    // // 处理完整的数据包
    // for (int i = 0; i < totalLen; i++) {
    // if (totalBuf[i] == (byte) 0xaa) {
    // beginAA = true;
    // idxStart = i; // 保存起始索引
    // } else if (totalBuf[i] == (byte) 0xbb) {
    // if (beginAA) {
    // // 取长度字节，提取一个指令数据包
    // if (i + 1 < totalLen) {
    // // 剩余的数据包中存在长度信息字节
    // len = totalBuf[i + 1];
    // if (i + 1 + len < totalLen) {
    // // 缓冲区剩下数据够一个数据包，提取并处理
    // idxEnd = idxStart + len + 3;
    // byte[] pData = new byte[len + 3];
    // System.arraycopy(totalBuf, idxStart, pData, 0,
    // len + 3);
    // processData(pData);
    // i = idxEnd - 1;
    // idxStart = 0;
    // idxEnd = 0;
    // beginAA = false;
    // beginBB = false;
    // continue;
    // } else {
    // // 缓冲区剩下数据为 aa bb 帧长...，但不够一个数据包
    // lastDataBuf = new byte[totalLen - idxStart];
    // System.arraycopy(totalBuf, idxStart, lastDataBuf,
    // 0, lastDataBuf.length);
    // idxStart = 0;
    // idxEnd = 0;
    // beginAA = false;
    // beginBB = false;
    // return;
    // }
    //
    // } else {
    // // 缓冲区剩下数据为 aa bb
    // lastDataBuf = new byte[totalLen - idxStart];
    // System.arraycopy(totalBuf, idxStart, lastDataBuf, 0,
    // lastDataBuf.length);
    // idxStart = 0;
    // idxEnd = 0;
    // beginAA = false;
    // beginBB = false;
    // return;
    // }
    // }
    // } else {
    // beginAA = false;
    // }
    // }
    //
    // // 扫描结束后，继续判断是否存在未处理的数据包，若存在，则缓存数据，等待下次合并处理
    // if (beginAA) {
    // // 缓冲区只剩下 aa
    // lastDataBuf = new byte[totalLen - idxStart];
    // System.arraycopy(totalBuf, idxStart, lastDataBuf, 0,
    // lastDataBuf.length);
    // idxStart = 0;
    // idxEnd = 0;
    // beginAA = false;
    // beginBB = false;
    // }
    //
    // }

    // 返回true，则表示需要重复调用该方法，继续解析生成数据包；返回false，表示本次接收的数据不能完整
    // 构成一个数据包，本次解析结束，等待客户端下次发送的数据进行累积，合并数据包后再进行解析
    @Override
    protected boolean doDecode(IoSession session, IoBuffer in,
                               ProtocolDecoderOutput out) throws Exception {
        // TODO Auto-generated method stub

        boolean result = false;
        int start = in.position(); // 记录缓冲区的起点
        // int end = in.limit(); // 记录缓冲区的终点
        byte prev = 0; // 前一个字节内容
        byte current = 0; // 当前字节内容

        try {
            // 扫描缓冲区，根据通信协议，解析出数据包单元（本协议的数据包单元为IoBuffer，处理更灵活）
            while (in.hasRemaining()) {
                // 根据\r\n解析数据包
                current = in.get(); // 读取当前字节
                if (prev == '\r' && current == '\n') {
                    // 满足条件，提取数据包
                    // 保存当前的position，limit
                    int pos = in.position();
                    int limit = in.limit();
                    try {
                        // 提取满足条件的数据包
                        in.position(start);
                        in.limit(pos);
                        IoBuffer rt = in.slice();
                        out.write(rt);
                    } catch (Exception e) {
                        // TODO: handle exception
                        log.error(e.getMessage());
                    } finally {
                        // 将剩余的缓冲区做为下次调用
                        in.position(pos);
                        in.limit(limit);
                    }
                    return true;
                }
                prev = current;
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error(e.getMessage());
        }
        // 扫描结束，没有可以解析成数据单元，恢复position，等待下次数据包到来时，合并解析
        in.position(start);
        return false;
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
