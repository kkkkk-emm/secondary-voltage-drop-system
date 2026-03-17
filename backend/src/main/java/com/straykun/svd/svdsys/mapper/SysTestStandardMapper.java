package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.SysTestStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 系统检定标准数据访问接口，定义持久化操作。
 */
@Mapper
public interface SysTestStandardMapper {

    /**
     * 查询 selectAll 相关信息。
     *
     * @return 返回结果列表。
     */
    List<SysTestStandard> selectAll();

    /**
     * 执行 match 业务逻辑。
     *
     * @param projectType 参数 projectType。
     * @param gearLevel 参数 gearLevel。
     * @param loadPercent 参数 loadPercent。
     * @return 返回处理结果。
     */
    SysTestStandard match(@Param("projectType") String projectType,
            @Param("gearLevel") String gearLevel,
            @Param("loadPercent") String loadPercent);

    /**
     * 执行 matchAll 业务逻辑。
     *
     * @param projectType 参数 projectType。
     * @param gearLevel 参数 gearLevel。
     * @param loadPercent 参数 loadPercent。
     * @return 返回结果列表。
     */
    List<SysTestStandard> matchAll(@Param("projectType") String projectType,
            @Param("gearLevel") String gearLevel,
            @Param("loadPercent") String loadPercent);

    /**
     * 查询 selectById 相关信息。
     *
     * @param id 参数 id。
     * @return 返回处理结果。
     */
    SysTestStandard selectById(@Param("id") Long id);

    /**
     * 执行 updateById 更新处理。
     *
     * @param standard 参数 standard。
     * @return 返回处理结果数量。
     */
    int updateById(SysTestStandard standard);
}
