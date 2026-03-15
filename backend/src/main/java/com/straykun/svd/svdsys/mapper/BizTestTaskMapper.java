package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.BizTestTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

@Mapper
public interface BizTestTaskMapper {

    /**
     * 新增检测任务
     *
     * @param task 检测任务
     * @return 影响行数
     */
    int insert(BizTestTask task);

    /**
     * 根据ID查询检测任务
     *
     * @param id 任务ID
     * @return 检测任务
     */
    BizTestTask selectById(@Param("id") Long id);

    /**
     * 分页查询任务列表，联表查询设备和操作员基础信息
     *
     * @param offset          偏移量
     * @param size            每页大小
     * @param taskId          任务ID
     * @param deviceProductNo 设备产品编号
     * @param operatorName    操作员姓名
     * @param operatorId      操作员ID
     * @param startDate       开始日期
     * @param endDate         结束日期
     * @param isPass          是否合格
     * @return 任务记录列表
     */
    List<TaskRecord> selectPage(@Param("offset") long offset,
                                @Param("size") long size,
                                @Param("taskId") Long taskId,
                                @Param("deviceProductNo") String deviceProductNo,
                                @Param("operatorName") String operatorName,
                                @Param("operatorId") Long operatorId,
                                @Param("startDate") LocalDate startDate,
                                @Param("endDate") LocalDate endDate,
                                @Param("isPass") Integer isPass);

    /**
     * 统计任务数量
     *
     * @param taskId          任务ID
     * @param deviceProductNo 设备产品编号
     * @param operatorName    操作员姓名
     * @param operatorId      操作员ID
     * @param startDate       开始日期
     * @param endDate         结束日期
     * @param isPass          是否合格
     * @return 任务数量
     */
    long count(@Param("taskId") Long taskId,
               @Param("deviceProductNo") String deviceProductNo,
               @Param("operatorName") String operatorName,
               @Param("operatorId") Long operatorId,
               @Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate,
               @Param("isPass") Integer isPass);

    /**
     * 任务记录DTO
     */
    class TaskRecord {
        /**
         * 任务ID
         */
        public Long id;
        /**
         * 设备ID
         */
        public Long deviceId;
        /**
         * 设备产品编号
         */
        public String deviceProductNo;
        /**
         * 设备产品名称
         */
        public String deviceProductName;
        /**
         * 操作员ID
         */
        public Long operatorId;
        /**
         * 操作员姓名
         */
        public String operatorName;
        /**
         * 计量点编号
         */
        public String meterPointId;
        /**
         * 检测时间
         */
        public java.time.LocalDateTime testDate;
        /**
         * 结果 1:合格 0:不合格
         */
        public Integer result; // 1:合格 0:不合格
    }
}
