package com.example.client;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;

import org.hyperledger.fabric.protos.ledger.rwset.kvrwset.KvRwset;
import org.hyperledger.fabric.sdk.BlockInfo;
import org.hyperledger.fabric.sdk.BlockchainInfo;
import org.hyperledger.fabric.sdk.Channel;
import org.hyperledger.fabric.sdk.HFClient;
import org.hyperledger.fabric.sdk.TxReadWriteSetInfo;
import org.hyperledger.fabric.sdk.exception.CryptoException;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.hyperledger.fabric.sdk.exception.ProposalException;
import org.hyperledger.fabric.sdk.exception.TransactionException;
import org.hyperledger.fabric.sdk.security.CryptoSuite;

import com.example.client.impl.ChannelUtil;
import com.example.client.impl.UserFileSystem;

public class BlockWalk {

	public static void main(String[] args)
			throws UnsupportedEncodingException, FileNotFoundException, IOException, CryptoException,
			InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException,
			NoSuchMethodException, InvocationTargetException, TransactionException, ProposalException {

		String channelName = StaticConfig.CHANNEL_NAME;
		String org = "maple";
		String portClient = "7051";// for fundinc 9051
		String discoveryPeer = "peer0." + org + ".fund.com:" + StaticConfig.GRPC_HOST + ":" + portClient;
		UserFileSystem user = new UserFileSystem("Admin", org + ".fund.com");

		new BlockWalk().walk(channelName, discoveryPeer, user);
	}

	protected void walk(String channelName, String discoveryPeer, UserFileSystem user) throws CryptoException,
			InvalidArgumentException, IllegalAccessException, InstantiationException, ClassNotFoundException,
			NoSuchMethodException, InvocationTargetException, TransactionException, IOException, ProposalException {

		ChannelUtil util = new ChannelUtil();
		HFClient client = HFClient.createNewInstance();
		client.setCryptoSuite(CryptoSuite.Factory.getCryptoSuite());
		client.setUserContext(user);
		Channel channel = util.reconstructChannelServiceDiscovery(channelName, discoveryPeer, client, user);

		BlockchainInfo channelInfo = channel.queryBlockchainInfo();

		System.out.println("Blocks height " + channelInfo.getHeight());

		for (int i = 0; i < channelInfo.getHeight(); i++) {
			BlockInfo returnedBlock = channel.queryBlockByNumber(i);
			long blockNumber = returnedBlock.getBlockNumber();
			System.out.println("Block number : " + blockNumber);
			for (BlockInfo.EnvelopeInfo envelope : returnedBlock.getEnvelopeInfos()) {
				if (envelope.getType() == BlockInfo.EnvelopeType.TRANSACTION_ENVELOPE) {
					BlockInfo.TransactionEnvelopeInfo transactionEnvelopeInfo = (BlockInfo.TransactionEnvelopeInfo) envelope;
					for (BlockInfo.TransactionEnvelopeInfo.TransactionActionInfo trActionInfo : transactionEnvelopeInfo
							.getTransactionActionInfos()) {
						for (int counter = 0; counter < trActionInfo.getChaincodeInputArgsCount(); ++counter) {
							System.out.println("Transaction info >>"
									+ new String(trActionInfo.getChaincodeInputArgs(counter), "UTF-8"));

						}

						TxReadWriteSetInfo readWriteSet = trActionInfo.getTxReadWriteSet();
						if (readWriteSet != null) {
							for (TxReadWriteSetInfo.NsRwsetInfo nsRwsetInfo : readWriteSet.getNsRwsetInfos()) {
								String namespace = nsRwsetInfo.getNamespace();
								KvRwset.KVRWSet rws = nsRwsetInfo.getRwset();

								System.out.println("Read Set");
								for (KvRwset.KVRead readList : rws.getReadsList()) {
									System.out.println("Read Set Key: " + readList.getKey() + "version[] : "
											+ readList.getVersion().getBlockNum() + ":"
											+ readList.getVersion().getTxNum());

								}
								System.out.println("Read Set END");
								System.out.println("Write Set");
								for (KvRwset.KVWrite writeList : rws.getWritesList()) {

									String valAsString = new String(writeList.getValue().toByteArray(), "UTF-8");

									System.out.println("Key: Value " + writeList.getKey() + ":" + valAsString);

								}
								System.out.println("Write Set END");
							}

						}

						System.out
								.println("ProposalResponse: " + new String(trActionInfo.getProposalResponsePayload()));
					}
				}
			}

		}

	}

}
