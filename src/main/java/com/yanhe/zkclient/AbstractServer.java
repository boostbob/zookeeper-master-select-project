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

public abstract class AbstractServer implements IServer {
	private volatile boolean running = false;
	private ZkClient zkClient;
	private static final String MASTER_PATH = "/master";

	// 节点内容监听器
	protected IZkDataListener dataListener;
	protected RunningData serverData;
	protected RunningData masterData;

	protected ScheduledExecutorService delayExector = Executors.newScheduledThreadPool(1);

	public AbstractServer(RunningData rd) {
		this.serverData = rd;
	}

	public ZkClient getZkClient() {
		return zkClient;
	}

	public void setZkClient(ZkClient zkClient) {
		this.zkClient = zkClient;
	}

	public void start() throws Exception {
		if (running) {
			throw new Exception("server has startup...");
		}

		running = true;
		zkClient.subscribeDataChanges(MASTER_PATH, dataListener);
		takeMaster();
	}

	public void stop() throws Exception {
		if (!running) {
			throw new Exception("server has stoped");
		}

		running = false;
		delayExector.shutdown();

		zkClient.unsubscribeDataChanges(MASTER_PATH, dataListener);
		releaseMaster();
	}

	protected void takeMaster() {
		if (!running)
			return;

		try {
			zkClient.create(MASTER_PATH, serverData, CreateMode.EPHEMERAL);
			masterData = serverData;
			System.out.println(serverData.getName() + " is master");

			// 演示抖动
			delayExector.schedule(new Runnable() {
				public void run() {
					if (checkMaster()) {
						releaseMaster();
					}
				}
			}, 5, TimeUnit.SECONDS);

		} catch (ZkNodeExistsException e) {
			// 节点已经存在
			RunningData runningData = zkClient.readData(MASTER_PATH, true);

			if (runningData == null) {
				takeMaster();
			} else {
				masterData = runningData;
			}
		} catch (Exception e) {
			// ignore
		}
	}

	private void releaseMaster() {
		if (checkMaster()) {
			// 如果本服务器是master
			zkClient.delete(MASTER_PATH);
		}
	}

	private boolean checkMaster() {
		try {
			masterData = zkClient.readData(MASTER_PATH);
			return masterData.getName().equals(serverData.getName());
		} catch (ZkNoNodeException e) {
			return false;
		} catch (ZkInterruptedException e) {
			// 被中断则重新检查
			return checkMaster();
		} catch (ZkException e) {
			return false;
		}
	}
}
