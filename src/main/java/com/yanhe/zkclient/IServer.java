package com.yanhe.zkclient;

import org.I0Itec.zkclient.ZkClient;

public interface IServer {
	public void setZkClient(ZkClient zkClient);
	public void start() throws Exception;
	public void stop() throws Exception;
}
