
/**
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *    http://www.apache.org/licenses/LICENSE-2.0
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *  
 *  DO NOT USE IN PROJECTS , NOT for use in production
 */

package com.example.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Channel.PeerOptions;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class JoinChannel {

	public static void main(String[] args) throws CryptoException, InvalidArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
			TransactionException, IOException, ProposalException {

		/**
		 * For the correct ports check docker-compose-base.yaml
		 */
		String channelName = StaticConfig.CHANNEL_NAME;
		String org = "maple";//fundinc
		String portClient = "7051";// for fundinc 9051

		String peer = "peer0." + org + ".fund.com:" + StaticConfig.GRPC_HOST + ":" + portClient;
		JoinChannel join = new JoinChannel();
		User user = new UserFileSystem("Admin", org + ".fund.com");
		join.join(channelName, StaticConfig.ORDERER, peer, user);

	}

	protected void join(String channelID, String ordererName, String peerName, User user) throws CryptoException,
			InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException,
			NoSuchMethodException, InvocationTargetException, TransactionException, IOException, ProposalException {
		ChannelUtil util = new ChannelUtil();
		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(user);

		Channel channel = client.newChannel(channelID);
		Peer peer = util.createPeer(client, peerName);
		Orderer orderer = util.createOrderer(client, ordererName);
		channel.addOrderer(orderer);

		channel = channel.joinPeer(orderer, peer, PeerOptions.createPeerOptions());

		System.out.println("DONE>>>>>>>");

	}

}