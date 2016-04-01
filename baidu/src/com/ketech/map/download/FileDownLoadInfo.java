/**
 * 项目名称:	baidu
 * 创建时间:	2015年7月4日
 * (C) Copyright KeDu Corporation 2015
 * All Rights Reserved.
 * 注意：本内容仅限于杭州科度科技有限公司内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.ketech.map.download;

/**
 * 包名：com.ketech.map.download <br/>
 * 类名：FileDownLoadInfo.java <br/>
 * 版本：version 1.0 <br/>
 * 作者：ZhangHeng <br/>
 * 描述：文件下载属性
 */
public class FileDownLoadInfo {
	/**
	 * 本地已下载路径
	 */
	private String localPath;

	/**
	 * 远程下载路径
	 */
	private String remotePath;
	
	/**
	 * 是否下载成功
	 */
	private boolean success;

	public String getLocalPath() {
		return localPath;
	}

	public void setLocalPath(String localPath) {
		this.localPath = localPath;
	}

	public String getRemotePath() {
		return remotePath;
	}

	public void setRemotePath(String remotePath) {
		this.remotePath = remotePath;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}
}
