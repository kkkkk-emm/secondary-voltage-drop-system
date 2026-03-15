package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.SysTestStandard;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface SysTestStandardMapper {

    /**
     * 查询所有标准
     *
     * @return 标准列表
     */
    List<SysTestStandard> selectAll();

    /**
     * 匹配标准
     *
     * @param projectType 项目类型
     * @param gearLevel   档位
     * @param loadPercent 负载百分比
     * @return 匹配的标准
     */
    SysTestStandard match(@Param("projectType") String projectType,
            @Param("gearLevel") String gearLevel,
            @Param("loadPercent") String loadPercent);

    /**
     * 获取指定组合下的所有阈值项
     *
     * @param projectType 项目类型
     * @param gearLevel   档位
     * @param loadPercent 负载百分比
     * @return 标准列表
     */
    List<SysTestStandard> matchAll(@Param("projectType") String projectType,
            @Param("gearLevel") String gearLevel,
            @Param("loadPercent") String loadPercent);

    /**
     * 根据ID查询标准
     *
     * @param id 标准ID
     * @return 标准信息
     */
    SysTestStandard selectById(@Param("id") Long id);

    /**
     * 更新标准
     *
     * @param standard 标准信息
     * @return 影响行数
     */
    int updateById(SysTestStandard standard);
}
