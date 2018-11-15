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

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.sdk.ChaincodeEndorsementPolicy;
import org.hyperledger.fabric.sdk.ChaincodeID;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.InstantiateProposalRequest;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.ProposalResponse;
import org.hyperledger.fabric.sdk.UpgradeProposalRequest;
import org.hyperledger.fabric.sdk.exception.ChaincodeEndorsementPolicyParseException;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class InstantiateChaincode {

	public static void main(String[] args)
			throws CryptoException, InvalidArgumentException, IllegalAccessException, InstantiationException,
			ClassNotFoundException, NoSuchMethodException, InvocationTargetException, TransactionException, IOException,
			ProposalException, ChaincodeEndorsementPolicyParseException, InterruptedException, ExecutionException {

		String chaincodeName = StaticConfig.CHAIN_CODE_ID;
		String channelName = StaticConfig.CHANNEL_NAME;
		int version = 1;
		String org = "maple";
		InstantiateChaincode instantiate = new InstantiateChaincode();
		UserFileSystem user = new UserFileSystem("Admin", org + ".fund.com");
		String[] params = new String[] { "Alice", "500", "Bob", "500" };
		Collection<String> peers = new HashSet<>();
		peers.add(StaticConfig.DISCOVER_PEER_MAPLE);
		instantiate.instantiate(chaincodeName, channelName, peers, StaticConfig.ORDERER, version, user, params);

	}

	protected void instantiate(String chaincodeName, String channelID, Collection<String> peers, String orderer,
			int version, UserFileSystem user, String[] params)
			throws InvalidArgumentException, TransactionException, IOException, CryptoException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
			ChaincodeEndorsementPolicyParseException, ProposalException, InterruptedException, ExecutionException {
		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(user);
		ChannelUtil util = new ChannelUtil();
		Channel channel = util.reconstructChannelServiceDiscovery(channelID, (String) peers.toArray()[0], client, user);
		Orderer ordering = util.createOrderer(client, orderer);

		ChaincodeID chaincodeID;
		Collection<ProposalResponse> responses;
		Collection<ProposalResponse> successful = new LinkedList<>();
		Collection<ProposalResponse> failed = new LinkedList<>();

		Collection<String> chainCodes = channel.getDiscoveredChaincodeNames();

		boolean isUpgrade = chainCodes.contains(chaincodeName);

		Collection<Peer> peersToSend = new HashSet<>();
		for (Peer discPeer : channel.getPeers()) {
			if (!discPeer.getUrl().contains("fund.com")) {
				peersToSend.add(discPeer);
			}
		}

		chaincodeID = ChaincodeID.newBuilder().setName(chaincodeName).setVersion(String.valueOf(version)).build();
		if (isUpgrade) {
			UpgradeProposalRequest upgrade = client.newUpgradeProposalRequest();
			upgrade.setChaincodeID(chaincodeID);
			upgrade.setProposalWaitTime(60000);
			upgrade.setFcn("init");
			upgrade.setChaincodeName(chaincodeName);
			upgrade.setChaincodeVersion(String.valueOf(version));
			Map<String, byte[]> tm = new HashMap<>();
			tm.put("HyperLedgerFabric", "UpgradeProposalRequest:JavaSDK".getBytes(UTF_8));
			tm.put("method", "UpgradeProposalRequest".getBytes(UTF_8));
			upgrade.setTransientMap(tm);
			upgrade.setArgs(params);

			ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
			chaincodeEndorsementPolicy.fromYamlFile(new File("./store/endorsement/chaincodeendorsementpolicy.yaml"));
			upgrade.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);
			responses = channel.sendUpgradeProposal(upgrade, peersToSend);
		} else {

			InstantiateProposalRequest instantiateProposalRequest = client.newInstantiationProposalRequest();
			instantiateProposalRequest.setProposalWaitTime(60000);
			instantiateProposalRequest.setChaincodeID(chaincodeID);
			instantiateProposalRequest.setFcn("init");
			instantiateProposalRequest.setChaincodeVersion(String.valueOf(version));
			instantiateProposalRequest.setChaincodeName(chaincodeName);

			instantiateProposalRequest.setArgs(params);
			Map<String, byte[]> tm = new HashMap<>();
			tm.put("HyperLedgerFabric", "InstantiateProposalRequest:JavaSDK".getBytes(UTF_8));
			tm.put("method", "InstantiateProposalRequest".getBytes(UTF_8));
			instantiateProposalRequest.setTransientMap(tm);

			ChaincodeEndorsementPolicy chaincodeEndorsementPolicy = new ChaincodeEndorsementPolicy();
			chaincodeEndorsementPolicy.fromYamlFile(new File("./store/endorsement/chaincodeendorsementpolicy.yaml"));
			instantiateProposalRequest.setChaincodeEndorsementPolicy(chaincodeEndorsementPolicy);

			responses = channel.sendInstantiationProposal(instantiateProposalRequest, peersToSend);
		}

		for (ProposalResponse response : responses) {
			if (response.isVerified() && response.getStatus() == ProposalResponse.Status.SUCCESS) {
				successful.add(response);
				System.out.println("SUCCESS-------------------------------------->");
			} else {
				failed.add(response);
				System.out.println("ERROR-------------------------------------->");
			}
		}

		if (!successful.isEmpty()) {
			Collection<Orderer> orderers = new HashSet<>();
			orderers.add(ordering);
			channel.addOrderer(ordering);
			channel.sendTransaction(successful, orderers);
		}
			
		System.out.println("DONE=>>>>>>>>>>>>>>>>>>>>>>>>>");
	}

}