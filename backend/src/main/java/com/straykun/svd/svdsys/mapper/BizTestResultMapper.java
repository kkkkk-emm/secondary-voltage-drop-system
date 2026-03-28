package com.straykun.svd.svdsys.mapper;

import com.straykun.svd.svdsys.domain.BizTestResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 业务检定结果数据访问接口，定义持久化操作。
 */
@Mapper
public interface BizTestResultMapper {

    /**
     * 执行 insert 新增处理。
     *
     * @param result 参数 result。
     * @return 返回处理结果数量。
     */
    int insert(BizTestResult result);

    /**
     * 执行 insertBatch 新增处理。
     *
     * @param list 参数 list。
     * @return 返回处理结果数量。
     */
    int insertBatch(@Param("list") List<BizTestResult> list);

    /**
     * 查询 selectByTaskId 相关信息。
     *
     * @param taskId 参数 taskId。
     * @return 返回结果列表。
     */
    List<BizTestResult> selectByTaskId(@Param("taskId") Long taskId);

    /**
     * 查询 selectById 相关信息。
     *
     * @param id 参数 id。
     * @return 返回处理结果。
     */
    BizTestResult selectById(@Param("id") Long id);

    /**
     * 执行 updateById 更新处理。
     *
     * @param result 参数 result。
     * @return 返回处理结果数量。
     */
    int updateById(BizTestResult result);

    /**
     * 鎵ц deleteByTaskId 鍒犻櫎澶勭悊銆?     *
     * @param taskId 鍙傛暟 taskId銆?     * @return 杩斿洖澶勭悊缁撴灉鏁伴噺銆?     */
    int deleteByTaskId(@Param("taskId") Long taskId);
}
