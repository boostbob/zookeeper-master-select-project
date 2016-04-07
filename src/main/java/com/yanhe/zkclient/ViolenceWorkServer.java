package com.yanhe.zkclient;

import org.I0Itec.zkclient.IZkDataListener;

public class ViolenceWorkServer extends AbstractServer {
	public ViolenceWorkServer(RunningData rd) {
		super(rd);

		this.dataListener = new IZkDataListener() {
			public void handleDataDeleted(String dataPath) throws Exception {
				// 直接竞争导致 master 随机，增加服务器的切换成本
				takeMaster();
			}

			public void handleDataChange(String dataPath, Object data) throws Exception {
				// 
			}
		};
	}
}
