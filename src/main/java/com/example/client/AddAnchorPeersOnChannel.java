package com.example.client;

import java.util.Arrays;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class AddAnchorPeersOnChannel {
	
	public static void main(String[] args) throws Exception {
		/**
		 * For the correct ports check docker-compose-base.yaml
		 */
		String channelName = StaticConfig.CHANNEL_NAME;
		String org = "maple";//fundinc
		String portClient = "7051";// for fundinc 9051
		
		String anchorPeer = "peer0." + org + ".fund.com:7051";

		String discoveryPeer = "peer0." + org + ".fund.com:" + StaticConfig.GRPC_HOST + ":" + portClient;
		AddAnchorPeersOnChannel updateChannel = new AddAnchorPeersOnChannel();
		UserFileSystem user = new UserFileSystem("Admin", org + ".fund.com");
		updateChannel.update(channelName, StaticConfig.ORDERER, anchorPeer,discoveryPeer, user);

	}

	protected void update(String channelID, String ordererName, String anchorPeer , String discoveryPeer, UserFileSystem user) throws Exception {
		// TODO Auto-generated method stub
		ChannelUtil util = new ChannelUtil();
		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(user);
		
		Channel channel = util.reconstructChannelServiceDiscovery(channelID, discoveryPeer, client, user);
		
		Peer peer = null;
		for(Peer dpeer : channel.getPeers() ) {
			System.out.println("discovered peer: " + dpeer.getName() + " url: " + dpeer.getUrl());
			if (anchorPeer.equals(dpeer.getName()) && discoveryPeer.contains(dpeer.getUrl())) {
				peer = dpeer;
				break;
			}
		}
		
		if (peer == null) {
			System.out.println("Peer not found: " + anchorPeer);
			return;
		}
		System.out.println("Adding anchor peer: " + peer.getName());
		Channel.AnchorPeersConfigUpdateResult configUpdateAnchorPeers = channel.getConfigUpdateAnchorPeers(peer, user,
                Arrays.asList(anchorPeer), null);
		
		Orderer orderer = util.createOrderer(client, ordererName);
		channel.addOrderer(orderer);
		
		channel.updateChannelConfiguration(configUpdateAnchorPeers.getUpdateChannelConfiguration(), orderer,
                   client.getUpdateChannelConfigurationSignature(configUpdateAnchorPeers.getUpdateChannelConfiguration(), user));
         
		
		System.out.println("DONE");

	}

}
