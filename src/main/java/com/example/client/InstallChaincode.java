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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstallProposalRequest;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.TransactionRequest.Type;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class InstallChaincode {

	public static void main(String[] args) throws CryptoException, InvalidArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
			TransactionException, IOException, ProposalException, ChaincodeEndorsementPolicyParseException {

		String path = "../cd-java-cc-1-3";
		String channelName = StaticConfig.CHANNEL_NAME;
		String org = "maple";
		int version = 1;
		String chaincodeName = StaticConfig.CHAIN_CODE_ID;

		String peerName = StaticConfig.DISCOVER_PEER_MAPLE;
		InstallChaincode install = new InstallChaincode();
		User user = new UserFileSystem("Admin", org + ".funds.com");
		install.install(path, peerName, channelName, chaincodeName, version, user);

	}

	protected void install(String path, String discoveryPeer, String channelID, String chaincodeName, int version,
			User user) throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException,
			ProposalException, ChaincodeEndorsementPolicyParseException {

		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(user);
		ChannelUtil util = new ChannelUtil();
		Peer peer = null;
		peer = util.createPeer(client, discoveryPeer);
		Channel channel = util.reconstructChannelServiceDiscovery(channelID, peer, client);

		Collection<Peer> peers = channel.getPeers();

		if (peers.isEmpty()) {
			return;
		}

		ChaincodeID chaincodeID;
		Collection<ProposalResponse> responses;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();

		chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(String.valueOf(version)).build();

		InstallProposalRequest installProposalRequest = client.newInstallProposalRequest();
		installProposalRequest.setChaincodeID(chaincodeID);

		installProposalRequest.setChaincodeSourceLocation(new File(path));

		installProposalRequest.setChaincodeVersion(String.valueOf(version));
		installProposalRequest.setChaincodeLanguage(Type.JAVA);
		installProposalRequest.setChaincodePath(null);
		HashSet<Peer> peersSet = new HashSet<>();
		peersSet.add(peer);

		responses = client.sendInstallProposal(installProposalRequest, peersSet);

		for (ProposalResponse response : responses) {
			if (response.getStatus() == ProposalResponse.Status.SUCCESS) {
				successful.add(response);
			} else {
				failed.add(response);
			}
		}

		if (failed.size() > 0) {
			System.out.println("Error installing chaincode on peers");
		}
		System.out.println("DONE =>>>>>>>>>>>>>>>>>>>>>>>>>>");

	}

}