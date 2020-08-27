package com.hkxx.drone;

import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFactory;
import org.apache.mina.filter.codec.ProtocolDecoder;
import org.apache.mina.filter.codec.ProtocolEncoder;

//编码器，解码器工厂
public class SimpleCodecFactory implements ProtocolCodecFactory {

	private ProtocolDecoder decoder = null;
	private ProtocolEncoder encoder = null;

	// 编码器，解码器初始化工厂
	public SimpleCodecFactory(ProtocolDecoder decoder, ProtocolEncoder encoder) {
		this.decoder = decoder;
		this.encoder = encoder;
	}

	@Override
	public ProtocolDecoder getDecoder(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		return decoder;
	}

	@Override
	public ProtocolEncoder getEncoder(IoSession arg0) throws Exception {
		// TODO Auto-generated method stub
		return encoder;
	}
}
