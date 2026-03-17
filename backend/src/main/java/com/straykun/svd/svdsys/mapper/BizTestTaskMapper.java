package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.BizTestTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * 业务检定检定任务数据访问接口，定义持久化操作。
 */
@Mapper
public interface BizTestTaskMapper {

    /**
     * 执行 insert 新增处理。
     *
     * @param task 参数 task。
     * @return 返回处理结果数量。
     */
    int insert(BizTestTask task);

    /**
     * 查询 selectById 相关信息。
     *
     * @param id 参数 id。
     * @return 返回处理结果。
     */
    BizTestTask selectById(@Param("id") Long id);

    /**
     * 查询 selectPage 相关信息。
     *
     * @param offset 参数 offset。
     * @param size 参数 size。
     * @param taskId 参数 taskId。
     * @param deviceProductNo 参数 deviceProductNo。
     * @param operatorName 参数 operatorName。
     * @param operatorId 参数 operatorId。
     * @param startDate 参数 startDate。
     * @param endDate 参数 endDate。
     * @param isPass 参数 isPass。
     * @return 返回结果列表。
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
     * 查询 count 相关信息。
     *
     * @param taskId 参数 taskId。
     * @param deviceProductNo 参数 deviceProductNo。
     * @param operatorName 参数 operatorName。
     * @param operatorId 参数 operatorId。
     * @param startDate 参数 startDate。
     * @param endDate 参数 endDate。
     * @param isPass 参数 isPass。
     * @return 返回统计结果。
     */
    long count(@Param("taskId") Long taskId,
               @Param("deviceProductNo") String deviceProductNo,
               @Param("operatorName") String operatorName,
               @Param("operatorId") Long operatorId,
               @Param("startDate") LocalDate startDate,
               @Param("endDate") LocalDate endDate,
               @Param("isPass") Integer isPass);

    /**
     * 检定任务Record数据访问组件。
     */
    class TaskRecord {
        public Long id;
        public Long deviceId;
        public String deviceProductNo;
        public String deviceProductName;
        public Long operatorId;
        public String operatorName;
        public String meterPointId;
        public java.time.LocalDateTime testDate;
        public Integer result; // 1:合格 0:不合格
    }
}
