package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.BizDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BizDeviceMapper {

    /**
     * 分页查询设备列表
     *
     * @param offset       偏移量
     * @param size         每页大小
     * @param productNo    产品编号
     * @param productName  产品名称
     * @param manufacturer 生产厂家
     * @return 设备列表
     */
    List<BizDevice> selectPage(@Param("offset") long offset,
                               @Param("size") long size,
                               @Param("productNo") String productNo,
                               @Param("productName") String productName,
                               @Param("manufacturer") String manufacturer);

    /**
     * 统计设备数量
     *
     * @param productNo    产品编号
     * @param productName  产品名称
     * @param manufacturer 生产厂家
     * @return 设备数量
     */
    long count(@Param("productNo") String productNo,
               @Param("productName") String productName,
               @Param("manufacturer") String manufacturer);

    /**
     * 根据ID查询设备
     *
     * @param id 设备ID
     * @return 设备信息
     */
    BizDevice selectById(@Param("id") Long id);

    /**
     * 根据产品编号查询设备
     *
     * @param productNo 产品编号
     * @return 设备信息
     */
    BizDevice selectByProductNo(@Param("productNo") String productNo);

    /**
     * 新增设备
     *
     * @param device 设备信息
     * @return 影响行数
     */
    int insert(BizDevice device);

    /**
     * 更新设备
     *
     * @param device 设备信息
     * @return 影响行数
     */
    int update(BizDevice device);

    /**
     * 根据ID删除设备
     *
     * @param id 设备ID
     * @return 影响行数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 获取所有设备列表（用于下拉选择）
     *
     * @return 所有设备列表
     */
    List<BizDevice> selectAll();
}
