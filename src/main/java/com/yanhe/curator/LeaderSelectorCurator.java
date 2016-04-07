package com.yanhe.curator;

import com.google.common.collect.Lists;

import org.apache.curator.utils.CloseableUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;

public class LeaderSelectorCurator {
	private static final int CLIENT_QTY = 10;
	private static final String PATH = "/leader";

	public static void main(String[] args) throws Exception {
		List<CuratorFramework> clients = Lists.newArrayList();
		List<WorkServer> workServers = Lists.newArrayList();

		try {
			for (int i = 0; i < CLIENT_QTY; ++i) {
				CuratorFramework client = CuratorFrameworkFactory.newClient("192.168.1.105:2181",
						new ExponentialBackoffRetry(1000, 3));
				clients.add(client);

				WorkServer workServer = new WorkServer(client, PATH, "Client #" + i);
				workServer.setListener(new RunningListener() {
					public void processStop(Object context) {
						System.out.println(context.toString() + "processStop...");
					}

					public void processStart(Object context) {
						System.out.println(context.toString() + "processStart...");
					}

					public void processActiveExit(Object context) {
						System.out.println(context.toString() + "processActiveExit...");
					}

					public void processActiveEnter(Object context) {
						System.out.println(context.toString() + "processActiveEnter...");
					}
				});

				workServers.add(workServer);

				client.start();
				workServer.start();
			}

			System.out.println("Press enter/return to quit\n");
			new BufferedReader(new InputStreamReader(System.in)).readLine();
		} finally {
			System.out.println("Shutting down...");

			for (WorkServer workServer : workServers) {
				CloseableUtils.closeQuietly(workServer);
			}
			
			for (CuratorFramework client : clients) {
				CloseableUtils.closeQuietly(client);
			}
		}
	}
}
