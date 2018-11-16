package com.example.client;

public class StaticConfig {
	/**
	 * Setup your specific host, channel name , chain code id, etc.
	 */
//	public static final String HOST = "ec2-3-16-24-60.us-east-2.compute.amazonaws.com";
	public static final String HOST = "enter your instance host name here";
	public static final String GRPC_HOST = "grpcs://" + HOST;
	public static final String ORDERER = "orderer.fund.com:" + GRPC_HOST + ":7050";
	public static final String DISCOVER_PEER_MAPLE = "peer0.maple.fund.com:" + GRPC_HOST + ":7051";
	public static final String DISCOVER_PEER_FUNDINC = "peer0.fundinc.fund.com:" + GRPC_HOST + ":9051";
	public static final String CHANNEL_NAME = "transfer";
	public static final String CHAIN_CODE_ID = "transfercc";
	
	public static String findPort(String string) {
		switch (string) {
		case "peer0.maple.fund.com":
			return "7051";
		case "peer0.fundinc.fund.com":
			return "9051";
		case "peer1.maple.fund.com":
			return "8051";
		case "peer1.fundinc.fund.com":
			return "10051";
		case "orderer.fund.com":
			return "7050";
		default:
			break;
		}
		return null;
	}

}
