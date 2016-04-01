/**
 * 项目名称:	baidu
 * 创建时间:	2015年7月3日
 * (C) Copyright KeDu Corporation 2015
 * All Rights Reserved.
 * 注意：本内容仅限于杭州科度科技有限公司内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.ketech.map.util;

import java.io.File;
import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.gdal.gdal.Band;
import org.gdal.gdal.Dataset;
import org.gdal.gdal.Driver;
import org.gdal.gdal.gdal;
import org.gdal.gdalconst.gdalconst;

import com.ketech.map.download.FileDownLoadInfo;
import com.ketech.map.download.MultiFileDownLoad;
import com.ketech.map.enums.MapTypeEnums;

/**
 * 包名：com.ketech.edap.util <br/>
 * 类名：TilesUtil.java <br/>
 * 版本：version 1.0 <br/>
 * 作者：ZhangHeng <br/>
 * 描述：瓦片工具类
 */
public class TilesUtil {
	/**
	 * 瓦片边像素
	 */
	private static int TILE_SIDE_PIXEL = 256;

	/**
	 * 瓦片下载格式
	 */
	public static final String TILE_SUFFIX = "jpg";

	public static void download(Double leftBottomLng, Double leftBottomLat, Double rightTopLng, Double rightTopBottomLat, int zoomLevel, MapTypeEnums type, String tilesRootDir) {
		File file = new File(tilesRootDir);
		if (!file.exists()) {
			file.mkdirs();
		}
		double[] min = BaiDuMapUtil.calBoundRowCol(leftBottomLng, leftBottomLat, zoomLevel);
		double[] max = BaiDuMapUtil.calBoundRowCol(rightTopLng, rightTopBottomLat, zoomLevel);
		int colMin = Math.min((int) min[0], (int) max[0]); // 最小列
		int rowMin = Math.min((int) min[1], (int) max[1]); // 最小行
		int colMax = Math.max((int) min[0], (int) max[0]); // 最大列
		int rowMax = Math.max((int) min[1], (int) max[1]);

		boolean beginDownload = false; // 是否调用算法API
		int filesAmount = (rowMax - rowMin + 1) * (colMax - colMin + 1); // 图片数量
		System.out.println("总计瓦片数量： " + filesAmount);

		int threadNum, batchNum;

		if (filesAmount < 100) {
			threadNum = 4;
			batchNum = 25;
		} else if (filesAmount > 500 && filesAmount < 2000) {
			threadNum = 10;
			batchNum = 500;
		} else {
			threadNum = 20;
			batchNum = 2000;
		}

		int current = 0, index = 0;
		List<FileDownLoadInfo> fileDownLoadInfos = new LinkedList<FileDownLoadInfo>();

		if (MapTypeEnums.HYBRID.getTypeValue() != type.getTypeValue()) {
			for (int i = rowMin; i <= rowMax; i++) {
				for (int j = colMin; j <= colMax; j++) {
					current++;
					File localeDir = new File(tilesRootDir + File.separator + type.getTypeName(), zoomLevel + File.separator + i);
					if (!localeDir.exists()) {
						localeDir.mkdirs();
					}
					FileDownLoadInfo fileDownLoadInfo = new FileDownLoadInfo();
					fileDownLoadInfo.setLocalPath(localeDir.getAbsolutePath() + File.separator + j + "." + TILE_SUFFIX);
					fileDownLoadInfo.setRemotePath(MessageFormat.format(type.getUrl(), String.valueOf(j), String.valueOf(i), String.valueOf(zoomLevel)));
					fileDownLoadInfos.add(fileDownLoadInfo);

					if (current == filesAmount) { // 说明为最后一个图片
						beginDownload = true;
					} else {
						if (index < batchNum - 1) {
							index++;
						} else if (index == batchNum - 1) {
							index = 0;
							beginDownload = true;
						}
					}

					if (beginDownload) {
						try {
							beginDownLoad(threadNum, fileDownLoadInfos);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						} finally {
							fileDownLoadInfos.clear();
							beginDownload = false;
						}
						System.out.println("当前已下载图片数量：" + current + "\t总计需下载图片数量：" + filesAmount);
					}
				}
			}
		} else {
			for (int i = rowMin; i <= rowMax; i++) {
				for (int j = colMin; j <= colMax; j++) {
					current++;
					File localeDir = new File(tilesRootDir + File.separator + type.getTypeName(), zoomLevel + File.separator + i);
					if (!localeDir.exists()) {
						localeDir.mkdirs();
					}

					FileDownLoadInfo fileDownLoadInfo = new FileDownLoadInfo();
					File satelliteDir = new File(tilesRootDir + File.separator + MapTypeEnums.SATELLITE.getTypeName(), zoomLevel + File.separator + i);
					fileDownLoadInfo.setLocalPath(localeDir.getAbsolutePath() + File.separator + j + "." + TILE_SUFFIX + "*" + satelliteDir.getAbsolutePath() + File.separator + j + "." + TILE_SUFFIX);
					fileDownLoadInfo.setRemotePath(MessageFormat.format(MapTypeEnums.HYBRID.getUrl(), String.valueOf(j), String.valueOf(i), String.valueOf(zoomLevel)) + "*" + MessageFormat.format(MapTypeEnums.SATELLITE.getUrl(), String.valueOf(j), String.valueOf(i), String.valueOf(zoomLevel)));
					fileDownLoadInfos.add(fileDownLoadInfo);

					if (current == filesAmount) { // 说明为最后一个图片
						beginDownload = true;
					} else {
						if (index < batchNum - 1) {
							index++;
						} else if (index == batchNum - 1) {
							index = 0;
							beginDownload = true;
						}
					}

					if (beginDownload) {
						try {
							beginDownLoad(threadNum, fileDownLoadInfos);
						} catch (InterruptedException e) {
							e.printStackTrace();
						} catch (ExecutionException e) {
							e.printStackTrace();
						} finally {
							fileDownLoadInfos.clear();
							beginDownload = false;
						}
					}
				}
			}
		}
	}

	private static void beginDownLoad(int threadNum, List<FileDownLoadInfo> fileDownLoadInfos) throws InterruptedException, ExecutionException {
		MultiFileDownLoad multiFileDownLoad = new MultiFileDownLoad(threadNum, fileDownLoadInfos);
		multiFileDownLoad.downLoad();
	}

	/**
	 * 
	 * 描述： 合并百度地图瓦片 <br/>
	 * 作者： ZhangHeng
	 * 
	 * @param leftBottomLng
	 *            左下角经度
	 * @param leftBottomLat
	 *            左下角纬度
	 * @param rightTopLng
	 *            右上角经度
	 * @param rightTopBottomLat
	 *            右上角纬度
	 * @param zoomLevel
	 *            地图缩放等级
	 * @param tilesRootDir
	 *            瓦片存放根目录
	 * @param mergeStorePath
	 *            网片存放保存路径 void <br/>
	 */
	public static void mergeBaiDuTiles(Double leftBottomLng, Double leftBottomLat, Double rightTopLng, Double rightTopBottomLat, int zoomLevel, MapTypeEnums type, String tilesRootDir, String outDirPath, String mergeFileName) {
		double[] min = BaiDuMapUtil.calBoundRowCol(leftBottomLng, leftBottomLat, zoomLevel);
		double[] max = BaiDuMapUtil.calBoundRowCol(rightTopLng, rightTopBottomLat, zoomLevel);
		int colMin = Math.min((int) min[0], (int) max[0]); // 最小列
		int rowMin = Math.min((int) min[1], (int) max[1]); // 最小行
		int colMax = Math.max((int) min[0], (int) max[0]); // 最大列
		int rowMax = Math.max((int) min[1], (int) max[1]); // 最小列

		int width = (colMax - colMin + 1) * TILE_SIDE_PIXEL;
		int height = (rowMax - rowMin + 1) * TILE_SIDE_PIXEL;

		gdal.AllRegister();
		Driver driver = gdal.GetDriverByName("GTIFF");
		File outDirFile = new File(outDirPath);
		if (!outDirFile.exists()) {
			outDirFile.mkdirs();
		}
		File outputImage = new File(outDirFile, mergeFileName);
		Dataset destDataset = driver.Create(outputImage.getAbsolutePath(), width, height, 3, gdalconst.GDT_Byte);
		int colIndex = height / TILE_SIDE_PIXEL - 1;
		for (int i = rowMin; i < rowMax + 1; i++) {
			int rowIndex = 0;
			for (int j = colMin; j < colMax + 1; j++) {
				File tilesDir = new File(tilesRootDir + File.separator + type.getTypeName(), zoomLevel + File.separator + i);
				String tilePath = tilesDir.getAbsolutePath() + File.separator + j + "." + TILE_SUFFIX;
				Dataset sourceDataset = gdal.Open(tilePath);
				if (null != sourceDataset) {
					saveBitmapBuffered(sourceDataset, destDataset, rowIndex * TILE_SIDE_PIXEL, colIndex * TILE_SIDE_PIXEL);
					sourceDataset.delete();
				}
				rowIndex++;
			}
			colIndex--;
		}
		destDataset.delete();
		gdal.GDALDestroyDriverManager();
	}

	public static void toJPEG(String gtiffPath, String descFilePath) {
		gdal.AllRegister();
		Dataset hDataset = gdal.Open(gtiffPath, gdalconst.GA_ReadOnly);
		Driver drijpg = gdal.GetDriverByName("JPEG");
		drijpg.CreateCopy(descFilePath, hDataset);
		hDataset.delete();
		gdal.GDALDestroyDriverManager();
	}

	private static void saveBitmapBuffered(Dataset src, Dataset dataset, int x, int y) {
		if (src.getRasterCount() < 3) {
			return;
		}

		Band redBand = src.GetRasterBand(1);
		Band greenBand = src.GetRasterBand(2);
		Band blueBand = src.GetRasterBand(3);
		int tileSizePixel = TILE_SIDE_PIXEL * TILE_SIDE_PIXEL;
		byte[] red = new byte[tileSizePixel];
		byte[] green = new byte[tileSizePixel];
		byte[] blue = new byte[tileSizePixel];

		redBand.ReadRaster(0, 0, TILE_SIDE_PIXEL, TILE_SIDE_PIXEL, red);
		greenBand.ReadRaster(0, 0, TILE_SIDE_PIXEL, TILE_SIDE_PIXEL, green);
		blueBand.ReadRaster(0, 0, TILE_SIDE_PIXEL, TILE_SIDE_PIXEL, blue);

		Band red1 = dataset.GetRasterBand(1);
		Band green1 = dataset.GetRasterBand(2);
		Band blue1 = dataset.GetRasterBand(3);
		red1.WriteRaster(x, y, TILE_SIDE_PIXEL, TILE_SIDE_PIXEL, red);
		green1.WriteRaster(x, y, TILE_SIDE_PIXEL, TILE_SIDE_PIXEL, green);
		blue1.WriteRaster(x, y, TILE_SIDE_PIXEL, TILE_SIDE_PIXEL, blue);
	}

	public static int[] calMercator(Double leftBottomLng, Double leftBottomLat, Double rightTopLng, Double rightTopBottomLat, int zoomLevel) {
		double[] min = BaiDuMapUtil.calBoundRowCol(leftBottomLng, leftBottomLat, zoomLevel);
		double[] max = BaiDuMapUtil.calBoundRowCol(rightTopLng, rightTopBottomLat, zoomLevel);
		double colMin = Math.min(min[0], max[0]); // 最小列
		double rowMin = Math.min(min[1], max[1]); // 最小行
		double colMax = Math.max(min[0], max[0]); // 最大列
		double rowMax = Math.max(min[1], max[1]); // 最小列

		Map<String, Double> map = BaiDuMapUtil.convertMC2LL(colMin, rowMin);
		System.out.println("lng:" + map.get("lng"));
		System.out.println("lat:" + map.get("lat"));

		int x = (int) (colMax - colMin + 1) * TILE_SIDE_PIXEL;
		int y = (int) (rowMax - rowMin + 1) * TILE_SIDE_PIXEL;
		return new int[] { x, y };
	}

	public static void main(String[] args) {
//		TilesUtil.download(120.024656d, 30.285833d, 120.931214d, 30.490222d, 19, MapTypeEnums.NORMAL, "D:\\");
		TilesUtil.mergeBaiDuTiles(120.024656d, 30.285833d, 120.231214d, 30.390222d, 19, MapTypeEnums.NORMAL, "D:\\", "D:\\", "1.tif");
		TilesUtil.toJPEG("D:/1.tif", "D:/2.jpg");

	}
}
