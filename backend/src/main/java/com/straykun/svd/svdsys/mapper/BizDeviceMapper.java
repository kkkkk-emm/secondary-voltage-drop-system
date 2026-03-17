package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.BizDevice;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务设备数据访问接口，定义持久化操作。
 */
@Mapper
public interface BizDeviceMapper {

    /**
     * 查询 selectPage 相关信息。
     *
     * @param offset 参数 offset。
     * @param size 参数 size。
     * @param productNo 参数 productNo。
     * @param productName 参数 productName。
     * @param manufacturer 参数 manufacturer。
     * @return 返回结果列表。
     */
    List<BizDevice> selectPage(@Param("offset") long offset,
                               @Param("size") long size,
                               @Param("productNo") String productNo,
                               @Param("productName") String productName,
                               @Param("manufacturer") String manufacturer);

    /**
     * 查询 count 相关信息。
     *
     * @param productNo 参数 productNo。
     * @param productName 参数 productName。
     * @param manufacturer 参数 manufacturer。
     * @return 返回统计结果。
     */
    long count(@Param("productNo") String productNo,
               @Param("productName") String productName,
               @Param("manufacturer") String manufacturer);

    /**
     * 查询 selectById 相关信息。
     *
     * @param id 参数 id。
     * @return 返回处理结果。
     */
    BizDevice selectById(@Param("id") Long id);

    /**
     * 查询 selectByProductNo 相关信息。
     *
     * @param productNo 参数 productNo。
     * @return 返回处理结果。
     */
    BizDevice selectByProductNo(@Param("productNo") String productNo);

    /**
     * 执行 insert 新增处理。
     *
     * @param device 参数 device。
     * @return 返回处理结果数量。
     */
    int insert(BizDevice device);

    /**
     * 执行 update 更新处理。
     *
     * @param device 参数 device。
     * @return 返回处理结果数量。
     */
    int update(BizDevice device);

    /**
     * 执行 deleteById 删除处理。
     *
     * @param id 参数 id。
     * @return 返回处理结果数量。
     */
    int deleteById(@Param("id") Long id);

    /**
     * 查询 selectAll 相关信息。
     *
     * @return 返回结果列表。
     */
    List<BizDevice> selectAll();
}
