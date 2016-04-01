/**
 * 项目名称:	edap
 * 创建时间:	2015年5月22日
 * (C) Copyright KeDu Corporation 2015
 * All Rights Reserved.
 * 注意：本内容仅限于杭州科度科技有限公司内部传阅，禁止外泄以及用于其他的商业目的。
 */
package com.ketech.map.enums;

/**
 * 包名：com.ketech.edap.enums <br/>
 * 类名：MapTypeEnums.java <br/>
 * 版本：version 1.0 <br/>
 * 作者：ZhangHeng <br/>
 * 描述：地图类型枚举类
 */
public enum MapTypeEnums {
	/**
     * 普通地图
     */
	NORMAL(1, "normal", "http://online3.map.bdimg.com/tile/?qt=tile&x={0}&y={1}&z={2}&styles=pl&udt=20140415"),
    
    /**
     * 卫星地图
     */
	SATELLITE(2, "satellite", "http://shangetu4.map.bdimg.com/it/u=x={0};y={1};z={2};v=009;type=sate&fm=46"),
    
    /**
     * 混合地图
     */
	HYBRID(3, "hybrid", "http://online3.map.bdimg.com/tile/?qt=tile&x={0}&y={1}&z={2}&styles=sl&udt=20131220");
    
    /**
     * 地图类型值
     */
    private Integer typeValue;

    /**
     * 地图类型名
     */
    private String typeName;
    
    /**
     * 下载地址
     */
    private String url;

    /**
     * 构造方法.
     *
     * @param typeValue
     * @param typeName
     */
    private MapTypeEnums(Integer typeValue, String typeName, String url) {
        this.typeValue = typeValue;
        this.typeName = typeName;
        this.url = url;
    }

	public Integer getTypeValue() {
		return typeValue;
	}

	public void setTypeValue(Integer typeValue) {
		this.typeValue = typeValue;
	}

	public String getTypeName() {
		return typeName;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
