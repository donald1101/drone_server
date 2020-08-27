package com.hkxx.drone;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolEncoderAdapter;
import org.apache.mina.filter.codec.ProtocolEncoderOutput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//控制器通信协议编码器
public class MavlinkEncoder extends ProtocolEncoderAdapter {

	private static Logger log = LoggerFactory.getLogger(MavlinkEncoder.class);

	// 具体的编码器实现，传递的参数为IoBuffer对象，相当于字节数组，不做解析，便于IoHandler应用逻辑扩展灵活处理
	@Override
	public void encode(IoSession session, Object message,
			ProtocolEncoderOutput out) throws Exception {
		// TODO Auto-generated method stub
		try {
			IoBuffer buf = (IoBuffer) message;
			out.write(buf);
		} catch (Exception e) {
			// TODO: handle exception
			log.error(e.getMessage());
		}

	}

}
