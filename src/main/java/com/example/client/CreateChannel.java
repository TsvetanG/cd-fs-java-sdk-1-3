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
 *  
 */

package com.example.client;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.User;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class CreateChannel {

	public static void main(String[] args) throws CryptoException, InvalidArgumentException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
			TransactionException, IOException, ProposalException {

		/**
		 * Channel creation transaction takes as input a "*.tx" file. This file can be
		 * generated by using configtxgen tool distributed by hyperledger fabric
		 * project. It is part of fabric sample project on github
		 * 
		 * cd /home/ubuntu/fabric-samples/first-network export
		 * CHANNEL_NAME=transferfunds ../bin/configtxgen -profile TwoOrgsChannel
		 * -outputCreateChannelTx ./channel-artifacts/transferfunds.tx -channelID
		 * $CHANNEL_NAME
		 * 
		 * In the above case the transferfunds.tx should be provided to the client. You
		 * can copy it to the project folder: store/channels
		 */
		String channelName = StaticConfig.CHANNEL_NAME;
		String org = "maple";
		String orderer = StaticConfig.ORDERER;
		String pathToConfigTX = "./store/channels/" + channelName + ".tx";
		CreateChannel create = new CreateChannel();
		User user = new UserFileSystem("Admin",  org + ".fund.com");
		create.create(orderer, channelName, pathToConfigTX, org, user);

	}

	protected void create(String orderer, String channelName, String pathToConfigTX, String org, User user)
			throws InvalidArgumentException, TransactionException, IOException, CryptoException, IllegalAccessException,
			InstantiationException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException {
		ChannelUtil util = new ChannelUtil();
		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(user);

		Channel channel = util.createNewChannel(orderer, pathToConfigTX, channelName, org, client);

		System.out.println("DONE>>>>>>>");
	}

}