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
import org.springframework.security.access.AccessDeniedException;
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

/**
 * Task service implementation.
 */
@Service
public class TaskServiceImpl implements TaskService {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final BizDeviceMapper deviceMapper;
    private final BizTestTaskMapper taskMapper;
    private final BizTestResultMapper resultMapper;
    private final SysTestStandardMapper standardMapper;
    private final SysUserMapper userMapper;

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

    @Override
    @Transactional
    public void submit(TaskCreateDTO dto) {
        Long operatorId = parseCurrentUserId();
        validateDeviceExists(dto.getDeviceId());

        BizTestTask task = new BizTestTask();
        fillTaskFromDto(task, dto);
        task.setOperatorId(operatorId);

        taskMapper.insert(task);
        replaceTaskResults(task.getId(), dto.getResultList());
    }

    @Override
    @Transactional
    public void update(Long taskId, TaskCreateDTO dto) {
        BizTestTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task does not exist");
        }
        ensureTaskWritable(task);
        validateDeviceExists(dto.getDeviceId());

        fillTaskFromDto(task, dto);
        taskMapper.updateById(task);

        replaceTaskResults(taskId, dto.getResultList());
    }

    @Override
    @Transactional
    public void delete(Long taskId) {
        BizTestTask task = taskMapper.selectById(taskId);
        if (task == null) {
            throw new IllegalArgumentException("Task does not exist");
        }
        ensureTaskWritable(task);

        resultMapper.deleteByTaskId(taskId);
        taskMapper.deleteById(taskId);
    }

    @Override
    public PageResult<TaskListItemVO> page(TaskPageQuery query) {
        long current = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        long size = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();
        long offset = (current - 1) * size;

        Long operatorId = null;
        if (!isCurrentUserAdmin()) {
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

            SysTestStandard standard = standardMapper.selectById(r.getStandardId());
            if (standard != null) {
                ri.setProjectType(standard.getProjectType());
                ri.setGearLevel(standard.getGearLevel());
                ri.setLoadPercent(standard.getLoadPercent());
            }
            return ri;
        }).collect(Collectors.toList());
        vo.setResultList(resVoList);

        return vo;
    }

    @Override
    @Transactional
    public void correctResult(ResultCorrectRequest request) {
        BizTestResult result = resultMapper.selectById(request.getResultId());
        if (result == null) {
            throw new IllegalArgumentException("Result does not exist");
        }

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

    private void validateDeviceExists(Long deviceId) {
        BizDevice device = deviceMapper.selectById(deviceId);
        if (device == null) {
            throw new IllegalArgumentException("Device does not exist");
        }
    }

    private void fillTaskFromDto(BizTestTask task, TaskCreateDTO dto) {
        task.setDeviceId(dto.getDeviceId());
        task.setMeterPointId(dto.getMeterPointId());
        task.setDeliverDate(LocalDate.parse(dto.getDeliverDate(), DATE_FORMAT));
        task.setTestDate(LocalDateTime.parse(dto.getTestDate(), DATETIME_FORMAT));
        task.setTemperature(dto.getTemperature());
        task.setHumidity(dto.getHumidity());
        task.setTanPhi(dto.getTanPhi());
        task.setRPercent(dto.getRPercent());
    }

    private void ensureTaskWritable(BizTestTask task) {
        if (isCurrentUserAdmin()) {
            return;
        }
        Long currentUserId = parseCurrentUserId();
        if (!currentUserId.equals(task.getOperatorId())) {
            throw new AccessDeniedException("无访问权限");
        }
    }

    private boolean isCurrentUserAdmin() {
        if (SecurityUtils.isAdmin()) {
            return true;
        }
        String userIdStr = SecurityUtils.getCurrentUserId();
        if (userIdStr == null) {
            return false;
        }
        try {
            Long userId = Long.valueOf(userIdStr);
            SysUser currentUser = userMapper.selectById(userId);
            return currentUser != null && Integer.valueOf(0).equals(currentUser.getRole());
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private Long parseCurrentUserId() {
        String userIdStr = SecurityUtils.getCurrentUserId();
        if (userIdStr == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        return Long.valueOf(userIdStr);
    }

    private void replaceTaskResults(Long taskId, List<ResultItemDTO> resultList) {
        if (CollectionUtils.isEmpty(resultList)) {
            throw new IllegalArgumentException("Result list cannot be empty");
        }

        List<BizTestResult> resultEntities = new ArrayList<>();
        for (ResultItemDTO item : resultList) {
            List<SysTestStandard> thresholdItems = standardMapper.matchAll(
                    item.getProjectType(),
                    item.getGearLevel(),
                    item.getLoadPercent()
            );
            if (CollectionUtils.isEmpty(thresholdItems)) {
                throw new IllegalArgumentException("Standard not found: "
                        + item.getProjectType() + "-" + item.getGearLevel() + "-" + item.getLoadPercent());
            }

            BizTestResult result = new BizTestResult();
            result.setTaskId(taskId);
            result.setStandardId(thresholdItems.get(0).getId());
            result.setPhase(item.getPhase());
            result.setValF(item.getValF());
            result.setValDelta(item.getValDelta());
            result.setValDu(item.getValDu());
            result.setValUpt(item.getValUpt());
            result.setValUyb(item.getValUyb());
            result.setIsPass(checkPassAllItems(thresholdItems, item) ? 1 : 0);
            resultEntities.add(result);
        }

        resultMapper.deleteByTaskId(taskId);
        resultMapper.insertBatch(resultEntities);
    }

    private boolean checkPassAllItems(List<SysTestStandard> thresholdItems, ResultItemDTO item) {
        if (CollectionUtils.isEmpty(thresholdItems)) {
            return true;
        }

        String projectType = thresholdItems.get(0).getProjectType();
        boolean isPT = isPtProject(projectType);

        for (SysTestStandard std : thresholdItems) {
            String itemKey = std.getThresholdItem();
            if (itemKey == null) {
                continue;
            }

            String key = itemKey.trim().toLowerCase();
            if (!isPT && isPtExclusiveItem(key)) {
                continue;
            }

            BigDecimal actualValue = getValueFromDto(item, key);
            if (isValueOutOfRange(actualValue, std.getLimitMin(), std.getLimitMax())) {
                return false;
            }
        }
        return true;
    }

    private boolean checkPassAllItemsByResult(List<SysTestStandard> thresholdItems, BizTestResult result) {
        if (CollectionUtils.isEmpty(thresholdItems)) {
            return true;
        }

        String projectType = thresholdItems.get(0).getProjectType();
        boolean isPT = isPtProject(projectType);

        for (SysTestStandard std : thresholdItems) {
            String itemKey = std.getThresholdItem();
            if (itemKey == null) {
                continue;
            }

            String key = itemKey.trim().toLowerCase();
            if (!isPT && isPtExclusiveItem(key)) {
                continue;
            }

            BigDecimal actualValue = getValueFromBizResult(result, key);
            if (isValueOutOfRange(actualValue, std.getLimitMin(), std.getLimitMax())) {
                return false;
            }
        }
        return true;
    }

    private BigDecimal getValueFromDto(ResultItemDTO item, String key) {
        if (item == null) {
            return BigDecimal.ZERO;
        }
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

    private BigDecimal getValueFromBizResult(BizTestResult result, String key) {
        if (result == null) {
            return BigDecimal.ZERO;
        }
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
        if (value == null) {
            return false;
        }
        if (min != null && value.compareTo(min) < 0) {
            return true;
        }
        if (max != null && value.compareTo(max) > 0) {
            return true;
        }
        return false;
    }
}
