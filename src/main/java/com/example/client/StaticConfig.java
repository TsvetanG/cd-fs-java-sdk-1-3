package com.example.client;

public class StaticConfig {
	/**
	 * Setup your specific host, channel name , chain code id, etc.
	 */
//	public static final String HOST = "enter host name OR host IP address";ec2-18-221-58-213.us-east-2.compute.amazonaws.com
	public static final String HOST = "ec2-18-221-58-213.us-east-2.compute.amazonaws.com";
	public static final String GRPC_HOST = "grpcs://" + HOST;
	public static final String ORDERER = "orderer.fund.com:" + GRPC_HOST + ":7050";
	public static final String DISCOVER_PEER_MAPLE = "peer0.maple.fund.com:" + GRPC_HOST + ":7051";
	public static final String DISCOVER_PEER_FUNDINC = "peer0.fundinc.fund.com:" + GRPC_HOST + ":9051";
	public static final String CHANNEL_NAME = "transfer";
	public static final String CHAIN_CODE_ID = "transfercc";

}
