package com.yanhe.zkclient;

import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.serialize.SerializableSerializer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class LeaderSelectorZkClient {
	// 启动的服务个数
	private static final int CLIENT_QTY = 10;
	
	// zookeeper服务器的地址
	private static final String ZOOKEEPER_SERVER = "10.211.55.7:2181";

	public static void main(String[] args) throws Exception {
		// 保存所有 zkClient 的列表
		List<ZkClient> clients = new ArrayList<ZkClient>();
		
		// 保存所有服务的列表
		List<IServer> workServers = new ArrayList<IServer>();

		try {
			for (int i = 0; i < CLIENT_QTY; ++i) {
				// 创建 zkClient
				ZkClient client = new ZkClient(ZOOKEEPER_SERVER, 5000, 5000, new SerializableSerializer());
				clients.add(client);
				
				// 创建 serverData
				RunningData runningData = new RunningData();
				runningData.setCid(Long.valueOf(i));
				runningData.setName("Client #" + i);
				
				// 创建服务
				// IServer workServer = new ViolenceWorkServer(runningData);
				IServer workServer = new WorkServer(runningData);
				workServer.setZkClient(client);

				workServers.add(workServer);
				workServer.start();
			}

			System.out.println("敲回车键退出！\n");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
		} finally {
			System.out.println("Shutting down...");

			for (IServer workServer : workServers) {
				try {
					workServer.stop();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			
			for (ZkClient client : clients) {
				try {
					client.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
