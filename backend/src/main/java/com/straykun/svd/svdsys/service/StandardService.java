package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.dto.StandardUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.StandardGroupVO;
import com.straykun.svd.svdsys.controller.vo.StandardVO;

import java.math.BigDecimal;
import java.util.List;

public interface StandardService {

    /**
     * 查询所有标准
     *
     * @return 标准列表
     */
    List<StandardVO> listAll();

    /**
     * 获取去重后的标准组合列表（用于下拉选择）
     *
     * @return 标准组合列表
     */
    List<StandardGroupVO> listGroups();

    /**
     * 用于录入时实时获取阈值
     *
     * @param projectType 项目类型
     * @param gearLevel   档位
     * @param loadPercent 负载百分比
     * @return 阈值范围
     */
    StandardLimit match(String projectType, String gearLevel, String loadPercent);

    /**
     * 获取指定组合下的所有阈值项
     *
     * @param projectType 项目类型
     * @param gearLevel   档位
     * @param loadPercent 负载百分比
     * @return 标准组合详情
     */
    StandardGroupVO matchAllThresholds(String projectType, String gearLevel, String loadPercent);

    /**
     * 更新标准
     *
     * @param request 更新请求
     */
    void update(StandardUpdateRequest request);

    class StandardLimit {
        private BigDecimal limitMin;
        private BigDecimal limitMax;

        public StandardLimit(BigDecimal limitMin, BigDecimal limitMax) {
            this.limitMin = limitMin;
            this.limitMax = limitMax;
        }

        public BigDecimal getLimitMin() {
            return limitMin;
        }

        public BigDecimal getLimitMax() {
            return limitMax;
        }
    }
}
