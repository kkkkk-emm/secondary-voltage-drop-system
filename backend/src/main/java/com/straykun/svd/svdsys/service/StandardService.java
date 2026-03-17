package com.straykun.svd.svdsys.service;

import com.straykun.svd.svdsys.controller.dto.StandardUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.StandardGroupVO;
import com.straykun.svd.svdsys.controller.vo.StandardVO;

import java.math.BigDecimal;
import java.util.List;

/**
 * 标准服务接口，定义业务能力。
 */
public interface StandardService {

    /**
     * 查询 listAll 相关信息。
     *
     * @return 返回结果列表。
     */
    List<StandardVO> listAll();

    /**
     * 查询 listGroups 相关信息。
     *
     * @return 返回结果列表。
     */
    List<StandardGroupVO> listGroups();

    /**
     * 执行 match 业务逻辑。
     *
     * @param projectType 参数 projectType。
     * @param gearLevel 参数 gearLevel。
     * @param loadPercent 参数 loadPercent。
     * @return 返回处理结果。
     */
    StandardLimit match(String projectType, String gearLevel, String loadPercent);

    /**
     * 执行 matchAllThresholds 业务逻辑。
     *
     * @param projectType 参数 projectType。
     * @param gearLevel 参数 gearLevel。
     * @param loadPercent 参数 loadPercent。
     * @return 返回处理结果。
     */
    StandardGroupVO matchAllThresholds(String projectType, String gearLevel, String loadPercent);

    /**
     * 执行 update 更新处理。
     *
     * @param request 参数 request。
     */
    void update(StandardUpdateRequest request);

    /**
     * 标准限制服务组件。
     */
    class StandardLimit {
        private BigDecimal limitMin;
        private BigDecimal limitMax;

        /**
         * 构造函数，初始化 StandardLimit 所需依赖。
         *
         * @param limitMin 参数 limitMin。
         * @param limitMax 参数 limitMax。
         */
        public StandardLimit(BigDecimal limitMin, BigDecimal limitMax) {
            this.limitMin = limitMin;
            this.limitMax = limitMax;
        }

        /**
         * 查询 getLimitMin 相关信息。
         *
         * @return 返回处理结果。
         */
        public BigDecimal getLimitMin() {
            return limitMin;
        }

        /**
         * 查询 getLimitMax 相关信息。
         *
         * @return 返回处理结果。
         */
        public BigDecimal getLimitMax() {
            return limitMax;
        }
    }
}
