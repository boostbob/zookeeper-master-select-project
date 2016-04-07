package com.yanhe.zkclient;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;
import org.I0Itec.zkclient.exception.ZkException;
import org.I0Itec.zkclient.exception.ZkInterruptedException;
import org.I0Itec.zkclient.exception.ZkNoNodeException;
import org.I0Itec.zkclient.exception.ZkNodeExistsException;
import org.apache.zookeeper.CreateMode;

public class WorkServer extends AbstractServer {
	public WorkServer(RunningData rd) {
		super(rd);
	
		this.dataListener = new IZkDataListener() {
			public void handleDataDeleted(String dataPath) throws Exception {
				// 应对网络抖动，上一轮 master 优先
				if (masterData != null && masterData.getName().equals(serverData.getName())) {
					takeMaster();
				} else {
					delayExector.schedule(new Runnable() {
						public void run() {
							takeMaster();
						}
					}, 5, TimeUnit.SECONDS);
				}
			}

			public void handleDataChange(String dataPath, Object data) throws Exception {
				// 
			}
		};
	}
}
