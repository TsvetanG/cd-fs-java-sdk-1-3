package com.example.client;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class ListDiscoveredPeers {

	public static void main(String[] args) throws Exception {

		String channelName = StaticConfig.CHANNEL_NAME;
		String org = "maple";
		String portClient = "7051";// for fundinc 9051
		String discoveryPeer = "peer0." + org + ".fund.com:" + StaticConfig.GRPC_HOST + ":" + portClient;
		UserFileSystem user = new UserFileSystem("Admin", org + ".fund.com");

		new ListDiscoveredPeers().list(channelName, discoveryPeer, user);
	}

	protected void list(String channelName, String discoveryPeer, UserFileSystem user) throws Exception {

		ChannelUtil util = new ChannelUtil();
		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(user);
		Channel channel = util.reconstructChannelServiceDiscovery(channelName, discoveryPeer, client, user);
		
		for (Peer peer : channel.getPeers()) {
			System.out.println("Peer: " + peer.toString());
		}
		
		for (Orderer orderer : channel.getOrderers()) {
			System.out.println("Orderer: " + orderer.toString());
		}


	}

}
