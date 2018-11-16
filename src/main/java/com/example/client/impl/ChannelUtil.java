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

package com.example.client.impl;

import static org.hyperledger.fabric.sdk.Channel.PeerOptions.createPeerOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EnumSet;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.IOUtils;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.Channel.SDPeerAddition;
import org.hyperledger.fabric.sdk.Channel.SDPeerAdditionInfo;
import org.hyperledger.fabric.sdk.ChannelConfiguration;
import org.hyperledger.fabric.sdk.EventHub;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.Orderer;
import org.hyperledger.fabric.sdk.Peer;
import org.hyperledger.fabric.sdk.Peer.PeerRole;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ServiceDiscoveryException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.springframework.util.StringUtils;

import com.example.client.StaticConfig;

public class ChannelUtil {

	public Channel reconstructChannelServiceDiscovery(String channelID, String discoveryPeer, HFClient client,
			UserFileSystem user)
			throws FileNotFoundException, InvalidArgumentException, TransactionException, IOException {
		Peer peer = null;
		if (discoveryPeer != null) {
			peer = createPeer(client, discoveryPeer);
		}

		return reconstructChannelServiceDiscovery(channelID, peer, client, user);
	}

	public Channel reconstructChannelServiceDiscovery(String channelID, Peer discoveryPeer, HFClient client,
			UserFileSystem user)
			throws FileNotFoundException, IOException, InvalidArgumentException, TransactionException {
		Channel channel;
		try {
			channel = client.newChannel(channelID);
			SDPeerAddition peerAddition = new SDPeerAdditionImpl( );
			channel.setSDPeerAddition(peerAddition);
		} catch (InvalidArgumentException e) {
			throw new RuntimeException("Could not create a new channel", e);
		} // create channel that will be discovered.
		if (discoveryPeer != null) {

			try {
				channel.addPeer(discoveryPeer,
						Channel.PeerOptions.createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.SERVICE_DISCOVERY,
								PeerRole.LEDGER_QUERY, PeerRole.EVENT_SOURCE, PeerRole.CHAINCODE_QUERY)));
				channel.setServiceDiscoveryProperties(getServiceDiscoveryProps(
						client.getUserContext().getName() + "@" + user.getOrgName(), user.getOrgName()));
			} catch (InvalidArgumentException e) {
				throw new RuntimeException("Could not add peer to channel", e);
			}
		}

		channel.initialize();

		return channel;
	}

	/**
	 * @return
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	private Properties getServiceDiscoveryProps(String userName, String orgName)
			throws FileNotFoundException, IOException {
		Properties sdprops = new Properties();
		sdprops.put("org.hyperledger.fabric.sdk.discovery.default.clientCertBytes",
				IOUtils.toByteArray(new FileInputStream(new File("./store/crypto-config/peerOrganizations/" + orgName
						+ "/users/" + userName + "/tls/client.crt"))));

		sdprops.put("org.hyperledger.fabric.sdk.discovery.default.clientKeyBytes",
				IOUtils.toByteArray(new FileInputStream(new File("./store/crypto-config/peerOrganizations/" + orgName
						+ "/users/" + userName + "/tls/client.key"))));

		return sdprops;

	}

	protected Properties getOrdererProps(String name) {
		return getProps("orderer", name);
	}

	protected Properties getPeerProps(String name) {
		return getProps("peer", name);
	}

	protected Properties getProps(String type, String name) {
		String orgName = getOrgName(name);
		File cert = null;
		if ("peer".equals(type)) {
			cert = new File(
					"./store/crypto-config/peerOrganizations/" + orgName + "/peers/" + name + "/tls/server.crt");
		} else {
			cert = new File(
					"./store/crypto-config/ordererOrganizations/" + orgName + "/orderers/" + name + "/tls/server.crt");
		}

		if (!cert.exists()) {
			throw new RuntimeException("Missing certificate file ");
		}

		Properties props = new Properties();
		props.setProperty("pemFile", cert.getAbsolutePath());
		// ret.setProperty("trustServerCertificate", "true"); //testing environment only
		// NOT FOR PRODUCTION!
		props.setProperty("hostnameOverride", name);
		props.setProperty("sslProvider", "openSSL");
		props.setProperty("negotiationType", "TLS");
		if ("orderer".equals(type)) {
			props.put("ordererWaitTimeMilliSecs", "10000");
		}
		props.put("grpc.NettyChannelBuilderOption.keepAliveTime", new Object[] { 5L, TimeUnit.MINUTES });
		props.put("grpc.NettyChannelBuilderOption.keepAliveTimeout", new Object[] { 8L, TimeUnit.SECONDS });
		props.put("grpc.NettyChannelBuilderOption.keepAliveWithoutCalls", new Object[] { true });
		return props;
	}

	private String getOrgName(String name) {
		int index = name.indexOf(".");
		return name.substring(index + 1);
	}

	public EventHub createHub(HFClient client, String value) throws InvalidArgumentException {
		String[] split = split(value);
		return client.newEventHub(split[0], split[1], getPeerProps(split[0]));
	}

	public Peer createPeer(HFClient client, String value) throws InvalidArgumentException {
		String[] split = split(value);
		return client.newPeer(split[0], split[1], getPeerProps(split[0]));
	}

	public Orderer createOrderer(HFClient client, String value) throws InvalidArgumentException {
		String[] split = split(value);
		return client.newOrderer(split[0], split[1], getOrdererProps(split[0]));
	}

	protected String[] split(String str) {
		return StringUtils.split(str, ":");
	}

	public Channel createNewChannel(String ordererPath, String pathToConfigTX, String channelName, String org,
			HFClient client) throws IOException, InvalidArgumentException, TransactionException {

		Orderer orderer = createOrderer(client, ordererPath);

		Channel channel = createNewChannel(pathToConfigTX, channelName, client, orderer, client.getUserContext());
		return channel;
	}

	protected Channel createNewChannel(String pathToConfigTX, String channelName, HFClient client, Orderer orderer,
			User user) throws TransactionException, InvalidArgumentException, IOException {

		ChannelConfiguration channelConfiguration = new ChannelConfiguration(new File(pathToConfigTX));
		Channel newChannel = client.newChannel(channelName, orderer, channelConfiguration,
				client.getChannelConfigurationSignature(channelConfiguration, user));
		return newChannel;
	}
	
	class SDPeerAdditionImpl implements SDPeerAddition {

		@Override
		public Peer addPeer(SDPeerAdditionInfo sdPeerAddition) throws InvalidArgumentException, ServiceDiscoveryException {
			Properties properties = new Properties();
			final String endpoint = sdPeerAddition.getEndpoint();
			final String mspid = sdPeerAddition.getMspId();

			
			Peer peer = sdPeerAddition.getEndpointMap().get(endpoint); // maybe there already.
			if (null != peer) {
				return peer;
			}


			byte[] pemBytes = sdPeerAddition.getAllTLSCerts();
			if (pemBytes.length > 0) {
				properties.put("pemBytes", pemBytes);
			}
			
			System.out.println("Adding discovered peer: " + endpoint);
			
			String[] hostPort = StringUtils.split(endpoint, ":");
			
			String host = StaticConfig.GRPC_HOST;
			String port = StaticConfig.findPort(hostPort[0]);
			
			properties.put("hostnameOverride", hostPort[0]);
			

			peer = sdPeerAddition.getClient().newPeer(endpoint, host + ":" + port, properties);
			
			System.out.println("Adding new peer: " + peer);

			sdPeerAddition.getChannel().addPeer(peer, createPeerOptions().setPeerRoles(EnumSet.of(PeerRole.ENDORSING_PEER,
					PeerRole.EVENT_SOURCE, PeerRole.LEDGER_QUERY, PeerRole.CHAINCODE_QUERY))); // application can decide on
																								// roles.

			return peer;
		}
	}

}
