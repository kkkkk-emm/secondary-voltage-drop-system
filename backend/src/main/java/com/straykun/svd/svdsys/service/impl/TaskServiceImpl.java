package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.ResultCorrectRequest;
import com.straykun.svd.svdsys.controller.dto.ResultItemDTO;
import com.straykun.svd.svdsys.controller.dto.TaskCreateDTO;
import com.straykun.svd.svdsys.controller.dto.TaskPageQuery;
import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;
import com.straykun.svd.svdsys.controller.vo.TaskListItemVO;
import com.straykun.svd.svdsys.domain.BizDevice;
import com.straykun.svd.svdsys.domain.BizTestResult;
import com.straykun.svd.svdsys.domain.BizTestTask;
import com.straykun.svd.svdsys.domain.SysTestStandard;
import com.straykun.svd.svdsys.domain.SysUser;
import com.straykun.svd.svdsys.mapper.BizDeviceMapper;
import com.straykun.svd.svdsys.mapper.BizTestResultMapper;
import com.straykun.svd.svdsys.mapper.BizTestTaskMapper;
import com.straykun.svd.svdsys.mapper.SysTestStandardMapper;
import com.straykun.svd.svdsys.mapper.SysUserMapper;
import com.straykun.svd.svdsys.security.SecurityUtils;
import com.straykun.svd.svdsys.service.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskServiceImpl implements TaskService {

    // ==========================================
    // 1. 静态常量 & 成员变量
    // ==========================================
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BizDeviceMapper deviceMapper;
    private final BizTestTaskMapper taskMapper;
    private final BizTestResultMapper resultMapper;
    private final SysTestStandardMapper standardMapper;
    private final SysUserMapper userMapper;

    // ==========================================
    // 2. 构造函数
    // ==========================================
    public TaskServiceImpl(BizDeviceMapper deviceMapper,
                           BizTestTaskMapper taskMapper,
                           BizTestResultMapper resultMapper,
                           SysTestStandardMapper standardMapper,
                           SysUserMapper userMapper) {
        this.deviceMapper = deviceMapper;
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.standardMapper = standardMapper;
        this.userMapper = userMapper;
    }

    // ==========================================
    // 3. 公有业务方法 (Public API)
    // ==========================================

    /**
     * 提交检定任务（含实时自动校验）
     */
    @Override
    @Transactional
    public void submit(TaskCreateDTO dto) {
        // include: 选择被检设备 -> 校验设备是否存在
        BizDevice device = deviceMapper.selectById(dto.getDeviceId());
        if (device == null) {
            throw new IllegalArgumentException("设备不存在，无法创建检定任务");
        }

        String userIdStr = SecurityUtils.getCurrentUserId();
        if (userIdStr == null) {
            throw new IllegalStateException("未认证");
        }
        Long operatorId = Long.valueOf(userIdStr);

        BizTestTask task = new BizTestTask();
        task.setDeviceId(dto.getDeviceId());
        task.setOperatorId(operatorId);
        task.setMeterPointId(dto.getMeterPointId());
        task.setDeliverDate(LocalDate.parse(dto.getDeliverDate(), DATE_FORMAT));
        task.setTestDate(LocalDateTime.parse(dto.getTestDate(), DATETIME_FORMAT));
        task.setTemperature(dto.getTemperature());
        task.setHumidity(dto.getHumidity());
        task.setTanPhi(dto.getTanPhi());
        task.setRPercent(dto.getRPercent());

        taskMapper.insert(task);

        // 录入实时数据 & 实时自动校验
        if (CollectionUtils.isEmpty(dto.getResultList())) {
            throw new IllegalArgumentException("明细结果不能为空");
        }
        List<BizTestResult> resultEntities = new ArrayList<>();
        for (ResultItemDTO item : dto.getResultList()) {
            // 获取该标准组合下的所有阈值项
            List<SysTestStandard> thresholdItems = standardMapper.matchAll(
                    item.getProjectType(),
                    item.getGearLevel(),
                    item.getLoadPercent()
            );
            if (CollectionUtils.isEmpty(thresholdItems)) {
                throw new IllegalArgumentException("标准配置不存在: " +
                        item.getProjectType() + "-" + item.getGearLevel() + "-" + item.getLoadPercent());
            }

            BizTestResult r = new BizTestResult();
            r.setTaskId(task.getId());
            // 存储第一个匹配的标准ID（用于后续关联）
            r.setStandardId(thresholdItems.get(0).getId());
            r.setPhase(item.getPhase());
            r.setValF(item.getValF());
            r.setValDelta(item.getValDelta());
            r.setValDu(item.getValDu());
            r.setValUpt(item.getValUpt());
            r.setValUyb(item.getValUyb());

            // 实时自动校验（使用多项阈值校验）
            r.setIsPass(checkPassAllItems(thresholdItems, item) ? 1 : 0);
            resultEntities.add(r);
        }
        resultMapper.insertBatch(resultEntities);
    }

    /**
     * 分页查询检定记录
     */
    @Override
    public PageResult<TaskListItemVO> page(TaskPageQuery query) {
        long current = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        long size = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();
        long offset = (current - 1) * size;

        Long operatorId = null;
        // 非管理员默认只能看自己的记录
        if (!SecurityUtils.isAdmin()) {
            String userIdStr = SecurityUtils.getCurrentUserId();
            if (userIdStr != null) {
                operatorId = Long.valueOf(userIdStr);
            }
        }

        LocalDate start = query.getStartDate() != null ? LocalDate.parse(query.getStartDate(), DATE_FORMAT) : null;
        LocalDate end = query.getEndDate() != null ? LocalDate.parse(query.getEndDate(), DATE_FORMAT) : null;

        List<BizTestTaskMapper.TaskRecord> records = taskMapper.selectPage(
                offset,
                size,
                query.getTaskId(),
                query.getDeviceProductNo(),
                query.getOperatorName(),
                operatorId,
                start,
                end,
                query.getIsPass()
        );
        long total = taskMapper.count(
                query.getTaskId(),
                query.getDeviceProductNo(),
                query.getOperatorName(),
                operatorId,
                start,
                end,
                query.getIsPass()
        );

        List<TaskListItemVO> list = records.stream().map(r -> {
            TaskListItemVO vo = new TaskListItemVO();
            vo.setId(r.id);
            vo.setDeviceId(r.deviceId);
            vo.setDeviceProductNo(r.deviceProductNo);
            vo.setDeviceProductName(r.deviceProductName);
            vo.setOperatorId(r.operatorId);
            vo.setOperatorName(r.operatorName);
            vo.setMeterPointId(r.meterPointId);
            vo.setTestDate(r.testDate != null ? r.testDate.format(DATETIME_FORMAT) : null);
            vo.setResult(r.result);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(total, size, current, list);
    }

    /**
     * 获取检定任务详情
     */
    @Override
    public TaskDetailVO detail(Long id) {
        BizTestTask task = taskMapper.selectById(id);
        if (task == null) {
            return null;
        }
        BizDevice device = deviceMapper.selectById(task.getDeviceId());
        SysUser operator = userMapper.selectById(task.getOperatorId());
        List<BizTestResult> resultList = resultMapper.selectByTaskId(id);

        TaskDetailVO vo = new TaskDetailVO();
        TaskDetailVO.TaskInfo taskInfo = new TaskDetailVO.TaskInfo();
        taskInfo.setId(task.getId());
        taskInfo.setDeviceId(task.getDeviceId());
        taskInfo.setOperatorId(task.getOperatorId());
        taskInfo.setMeterPointId(task.getMeterPointId());
        taskInfo.setDeliverDate(task.getDeliverDate() != null ? task.getDeliverDate().format(DATE_FORMAT) : null);
        taskInfo.setTestDate(task.getTestDate() != null ? task.getTestDate().format(DATETIME_FORMAT) : null);
        taskInfo.setTemperature(task.getTemperature());
        taskInfo.setHumidity(task.getHumidity());
        taskInfo.setTanPhi(task.getTanPhi());
        taskInfo.setRPercent(task.getRPercent());
        vo.setTaskInfo(taskInfo);

        if (device != null) {
            TaskDetailVO.DeviceInfo deviceInfo = new TaskDetailVO.DeviceInfo();
            deviceInfo.setProductNo(device.getProductNo());
            deviceInfo.setProductName(device.getProductName());
            deviceInfo.setModel(device.getModel());
            deviceInfo.setManufacturer(device.getManufacturer());
            vo.setDeviceInfo(deviceInfo);
        }

        vo.setOperatorName(operator != null ? operator.getRealName() : null);

        List<TaskDetailVO.ResultInfo> resVoList = resultList.stream().map(r -> {
            TaskDetailVO.ResultInfo ri = new TaskDetailVO.ResultInfo();
            ri.setId(r.getId());
            ri.setPhase(r.getPhase());
            ri.setValF(r.getValF());
            ri.setValDelta(r.getValDelta());
            ri.setValDu(r.getValDu());
            ri.setValUpt(r.getValUpt());
            ri.setValUyb(r.getValUyb());
            ri.setIsPass(r.getIsPass());
            return ri;
        }).collect(Collectors.toList());
        vo.setResultList(resVoList);

        return vo;
    }

    /**
     * 数据修正（管理员专用）
     */
    @Override
    @Transactional
    public void correctResult(ResultCorrectRequest request) {
        BizTestResult result = resultMapper.selectById(request.getResultId());
        if (result == null) {
            throw new IllegalArgumentException("检测结果不存在");
        }

        // 更新数据
        if (request.getValF() != null) {
            result.setValF(request.getValF());
        }
        if (request.getValDelta() != null) {
            result.setValDelta(request.getValDelta());
        }
        if (request.getValDu() != null) {
            result.setValDu(request.getValDu());
        }
        if (request.getValUpt() != null) {
            result.setValUpt(request.getValUpt());
        }
        if (request.getValUyb() != null) {
            result.setValUyb(request.getValUyb());
        }

        // 重新校验是否合格 - 获取该标准对应的所有阈值项
        SysTestStandard std = standardMapper.selectById(result.getStandardId());
        if (std != null) {
            List<SysTestStandard> thresholdItems = standardMapper.matchAll(
                    std.getProjectType(),
                    std.getGearLevel(),
                    std.getLoadPercent()
            );
            result.setIsPass(checkPassAllItemsByResult(thresholdItems, result) ? 1 : 0);
        }

        resultMapper.updateById(result);
    }

    // ==========================================
    // 4. 私有业务拆分方法 (Private Business Methods)
    // ==========================================

    /**
     * 多项阈值校验逻辑 (针对 DTO)
     */
    private boolean checkPassAllItems(List<SysTestStandard> thresholdItems, ResultItemDTO item) {
        // 1. 防御性判断
        if (CollectionUtils.isEmpty(thresholdItems)) {
            return true;
        }

        // 2. 确定项目类型 (直接取第一个标准里的类型)
        String projectType = thresholdItems.get(0).getProjectType();
        boolean isPT = isPtProject(projectType);

        // 3. 核心循环：遍历标准，去 DTO 里找对应的值
        for (SysTestStandard std : thresholdItems) {
            String itemKey = std.getThresholdItem();
            if (itemKey == null) continue;

            String key = itemKey.trim().toLowerCase();

            // 4. 业务规则过滤：非 PT 项目跳过 PT 独有指标
            if (!isPT && isPtExclusiveItem(key)) {
                continue;
            }

            // 5. 从 DTO 对象中提取值
            BigDecimal actualValue = getValueFromDto(item, key);

            // 6. 数值比对
            if (isValueOutOfRange(actualValue, std.getLimitMin(), std.getLimitMax())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 多项阈值校验逻辑（针对 Entity，用于数据修正后的重新校验）
     */
    private boolean checkPassAllItemsByResult(List<SysTestStandard> thresholdItems, BizTestResult result) {
        // 1. 防御性判断
        if (CollectionUtils.isEmpty(thresholdItems)) {
            return true;
        }

        // 2. 确定项目类型
        String projectType = thresholdItems.get(0).getProjectType();
        boolean isPT = isPtProject(projectType);

        // 3. 核心循环
        for (SysTestStandard std : thresholdItems) {
            String itemKey = std.getThresholdItem();
            if (itemKey == null) continue;

            String key = itemKey.trim().toLowerCase();

            // 4. 业务规则过滤
            if (!isPT && isPtExclusiveItem(key)) {
                continue;
            }

            // 5. 从 Result 对象中提取值
            BigDecimal actualValue = getValueFromBizResult(result, key);

            // 6. 数值比对
            if (isValueOutOfRange(actualValue, std.getLimitMin(), std.getLimitMax())) {
                return false;
            }
        }
        return true;
    }

    // ==========================================
    // 5. 纯辅助/工具方法 (Private Helpers)
    // ==========================================

    /**
     * 根据 Key 从 ResultItemDTO 对象中获取值
     */
    private BigDecimal getValueFromDto(ResultItemDTO item, String key) {
        if (item == null) return BigDecimal.ZERO;
        BigDecimal target = switch (key) {
            case "f" -> item.getValF();
            case "delta" -> item.getValDelta();
            case "du" -> item.getValDu();
            case "upt" -> item.getValUpt();
            case "uyb" -> item.getValUyb();
            default -> null;
        };
        return target != null ? target : BigDecimal.ZERO;
    }

    /**
     * 根据 Key 从 BizTestResult 获取对应的数值
     */
    private BigDecimal getValueFromBizResult(BizTestResult result, String key) {
        if (result == null) return BigDecimal.ZERO;
        BigDecimal target = switch (key) {
            case "f" -> result.getValF();
            case "delta" -> result.getValDelta();
            case "du" -> result.getValDu();
            case "upt" -> result.getValUpt();
            case "uyb" -> result.getValUyb();
            default -> null;
        };
        return target != null ? target : BigDecimal.ZERO;
    }

    private boolean isPtProject(String projectType) {
        return projectType != null &&
                (projectType.equalsIgnoreCase("PT1") || projectType.equalsIgnoreCase("PT2"));
    }

    private boolean isPtExclusiveItem(String key) {
        return !"f".equalsIgnoreCase(key) && !"delta".equalsIgnoreCase(key);
    }

    private boolean isValueOutOfRange(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value == null) return false;
        if (min != null && value.compareTo(min) < 0) return true;
        if (max != null && value.compareTo(max) > 0) return true;
        return false;
    }
}