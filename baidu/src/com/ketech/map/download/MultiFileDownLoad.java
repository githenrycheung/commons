/**
 * 项目名称:	baidu
 * 创建时间:	2015年7月4日
 * (C) Copyright KeDu Corporation 2015
 * All Rights Reserved.
 * 注意：本内容仅限于杭州科度科技有限公司内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.ketech.map.download;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * 包名：com.ketech.map.download <br/>
 * 类名：MultiFileDownLoad.java <br/>
 * 版本：version 1.0 <br/>
 * 作者：ZhangHeng <br/>
 * 描述：多线程文件下载
 */
public class MultiFileDownLoad {
	/**
	 * 线程数量
	 */
	private int numThreads;
	
	/**
	 * 下载文件队列
	 */
	private ArrayBlockingQueue<FileDownLoadInfo> filePathQueue;
	
	/**
	 * 线程池
	 */
	private ExecutorService executorService;
	
	/**
	 * 构造方法.
	 * 
	 * @param numThreads
	 * @param executorService
	 * @param filePathQueue
	 */
	public MultiFileDownLoad(int numThreads, List<FileDownLoadInfo> fileDownLoadInfos) {
		super();
		this.numThreads = numThreads;
		this.executorService = Executors.newFixedThreadPool(this.numThreads);
		this.filePathQueue = new ArrayBlockingQueue<FileDownLoadInfo>(fileDownLoadInfos.size(), true, fileDownLoadInfos);
	}
	
	public List<FileDownLoadInfo> downLoad() throws InterruptedException, ExecutionException{ 
		List<Future<List<FileDownLoadInfo>>> futures = new ArrayList<Future<List<FileDownLoadInfo>>>();
		for (int i = 0; i < this.numThreads; i++) {
			futures.add(executorService.submit(new FileDownloadRunable(filePathQueue)));
		}
		executorService.shutdown();
		while(!executorService.isTerminated()){
			
		}
		List<FileDownLoadInfo> complete = new ArrayList<FileDownLoadInfo>();
		for (Future<List<FileDownLoadInfo>> future : futures) {
			if (future.isDone()) {
				complete.addAll(future.get());
			}
		}
		return complete;
	}
}
