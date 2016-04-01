/**
 * 项目名称:	baidu
 * 创建时间:	2015年7月4日
 * (C) Copyright KeDu Corporation 2015
 * All Rights Reserved.
 * 注意：本内容仅限于杭州科度科技有限公司内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.ketech.map.download;

import java.awt.AlphaComposite;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Callable;

import javax.imageio.ImageIO;

import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.JPEGEncodeParam;
import com.sun.media.jai.codecimpl.JPEGCodec;

/**
 * 包名：com.ketech.map.download <br/>
 * 类名：FileDownloadRunable.java <br/>
 * 版本：version 1.0 <br/>
 * 作者：ZhangHeng <br/>
 * 描述：文件下载
 */
public class FileDownloadRunable implements Callable<List<FileDownLoadInfo>> {
	/**
	 * 瓦片边像素
	 */
	public static int TILE_SIDE_PIXEL = 256;
	
	/**
	 * 下载文件队列
	 */
	private ArrayBlockingQueue<FileDownLoadInfo> filePathQueue;
	
	/**
	 * 构造方法.
	 * 
	 * @param filePathQueue
	 * @param ftpClient
	 */
	public FileDownloadRunable(ArrayBlockingQueue<FileDownLoadInfo> filePathQueue) {
		super();
		this.filePathQueue = filePathQueue;
	}
	
	@Override
	public List<FileDownLoadInfo> call() throws Exception {
		List<FileDownLoadInfo> fileDownLoadInfos = new ArrayList<FileDownLoadInfo>();
		while (null != filePathQueue.peek()) {
			FileDownLoadInfo fileDownLoadInfo = filePathQueue.poll();
			String remotePath = fileDownLoadInfo.getRemotePath();
			String localPath = fileDownLoadInfo.getLocalPath();
			if(-1 != remotePath.indexOf("*")) {
				String[] remotePathArr = remotePath.split("*");
				String[] localPathArr = localPath.split("*");
				File hybridFile = new File(localPathArr[0]);
				String satellitePath = localPathArr[1];
				String hybridURL = remotePathArr[0];
				String satelliteURL = remotePathArr[1];
				createURLToSateJPG(hybridFile, satellitePath, satelliteURL, hybridURL);
			} else {
				File tileFile = new File(localPath);
				createURLToJPG(tileFile, remotePath);
			}
			fileDownLoadInfos.add(fileDownLoadInfo);
		}
		return fileDownLoadInfos;
	}
	
	private void createURLToSateJPG(File targetFile, String satellitePath, String satelliteURL, String hybridURL) {
		OutputStream os = null;
		InputStream hybridIn = null;

		try {
			os = new FileOutputStream(targetFile);
			File satelliteFile = new File(satellitePath);
			if (!satelliteFile.exists() || satelliteFile.length() == 0) {
				createURLToJPG(satelliteFile, satelliteURL);
			}
			BufferedImage bi1 = ImageIO.read(satelliteFile);
			hybridIn = new URL(hybridURL).openStream();
			BufferedImage bi2 = ImageIO.read(hybridIn);
			BufferedImage image = new BufferedImage(TILE_SIDE_PIXEL, TILE_SIDE_PIXEL, BufferedImage.TYPE_INT_RGB);
			Graphics2D g = image.createGraphics();
			g.drawImage(bi1, 0, 0, null);
			if (null != bi2 && bi2.getColorModel().hasAlpha()) {
				g.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 1f));
				g.drawImage(bi2, 0, 0, null);
			}
			g.dispose();

			JPEGEncodeParam param = new JPEGEncodeParam();
			param.setQuality(3);
			ImageEncoder encoder = JPEGCodec.createImageEncoder("JPEG", os, param);
			encoder.encode(image);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(hybridIn);
			closeQuietly(os);
		}
	}
	
	/**
	 * 描述：URL转JPG
	 * 
	 * @param targetPath
	 * @param filePath
	 */
	private void createURLToJPG(File targetFile, String filePath) {
		OutputStream os = null;
		try {
			File dir = targetFile.getParentFile();
			if (!dir.exists()) {
				dir.mkdirs();
			}
			targetFile.createNewFile();
			os = new FileOutputStream(targetFile);
			JPEGEncodeParam param = new JPEGEncodeParam();
			param.setQuality(3);
			ImageEncoder encoder = JPEGCodec.createImageEncoder("JPEG", os, param);
			RenderedImage src = ImageIO.read(new URL(filePath));
			encoder.encode(src);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			closeQuietly(os);
		}
	}

	private void closeQuietly(InputStream input) {
        try {
            if (input != null) {
                input.close();
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
	
	private void closeQuietly(OutputStream output) {
        try {
            if (output != null) {
                output.close();
            }
        } catch (IOException e) {
        	e.printStackTrace();
        }
    }
}
