package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.controller.dto.StandardUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.StandardGroupVO;
import com.straykun.svd.svdsys.controller.vo.StandardVO;
import com.straykun.svd.svdsys.domain.SysTestStandard;
import com.straykun.svd.svdsys.mapper.SysTestStandardMapper;
import com.straykun.svd.svdsys.service.StandardService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 标准配置服务实现类。
 */
@Service
public class StandardServiceImpl implements StandardService {

    private final SysTestStandardMapper standardMapper;

    /**
     * 构造函数，初始化 StandardServiceImpl 所需依赖。
     *
     * @param standardMapper 参数 standardMapper。
     */
    public StandardServiceImpl(SysTestStandardMapper standardMapper) {
        this.standardMapper = standardMapper;
    }

    /**
     * 查询 listAll 相关信息。
     *
     * @return 返回结果列表。
     */
    @Override
    public List<StandardVO> listAll() {
        List<SysTestStandard> list = standardMapper.selectAll();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }

    /**
     * 查询 listGroups 相关信息。
     *
     * @return 返回结果列表。
     */
    @Override
    public List<StandardGroupVO> listGroups() {
        List<SysTestStandard> allStandards = standardMapper.selectAll();

        // 按 projectType-gearLevel-loadPercent 分组
        Map<String, List<SysTestStandard>> groupedMap = allStandards.stream()
                .collect(Collectors.groupingBy(
                        s -> s.getProjectType() + "-" + s.getGearLevel() + "-" + s.getLoadPercent(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<StandardGroupVO> result = new ArrayList<>();
        for (Map.Entry<String, List<SysTestStandard>> entry : groupedMap.entrySet()) {
            List<SysTestStandard> items = entry.getValue();
            if (items.isEmpty()) continue;

            SysTestStandard first = items.get(0);
            StandardGroupVO group = new StandardGroupVO();
            group.setProjectType(first.getProjectType());
            group.setGearLevel(first.getGearLevel());
            group.setLoadPercent(first.getLoadPercent());
            group.setGroupKey(entry.getKey());

            // 判断是否是 PT 项目（PT1 或 PT2）
            boolean isPT = first.getProjectType() != null &&
                    (first.getProjectType().equalsIgnoreCase("PT1") ||
                            first.getProjectType().equalsIgnoreCase("PT2"));
            group.setIsPT(isPT);

            // 构建各项阈值映射
            Map<String, StandardGroupVO.LimitRange> thresholds = new HashMap<>();
            for (SysTestStandard std : items) {
                String item = std.getThresholdItem();
                if (item != null) {
                    thresholds.put(item.toLowerCase(),
                            new StandardGroupVO.LimitRange(std.getLimitMin(), std.getLimitMax()));
                }
            }
            group.setThresholds(thresholds);
            result.add(group);
        }
        return result;
    }

    /**
     * 执行 match 业务逻辑。
     *
     * @param projectType 参数 projectType。
     * @param gearLevel 参数 gearLevel。
     * @param loadPercent 参数 loadPercent。
     * @return 返回处理结果。
     */
    @Override
    public StandardLimit match(String projectType, String gearLevel, String loadPercent) {
        SysTestStandard std = standardMapper.match(projectType, gearLevel, loadPercent);
        if (std == null) {
            return null;
        }
        return new StandardLimit(std.getLimitMin(), std.getLimitMax());
    }

    /**
     * 执行 matchAllThresholds 业务逻辑。
     *
     * @param projectType 参数 projectType。
     * @param gearLevel 参数 gearLevel。
     * @param loadPercent 参数 loadPercent。
     * @return 返回处理结果。
     */
    @Override
    public StandardGroupVO matchAllThresholds(String projectType, String gearLevel, String loadPercent) {
        List<SysTestStandard> items = standardMapper.matchAll(projectType, gearLevel, loadPercent);
        if (items == null || items.isEmpty()) {
            return null;
        }

        SysTestStandard first = items.get(0);
        StandardGroupVO group = new StandardGroupVO();
        group.setProjectType(first.getProjectType());
        group.setGearLevel(first.getGearLevel());
        group.setLoadPercent(first.getLoadPercent());
        group.setGroupKey(projectType + "-" + gearLevel + "-" + loadPercent);

        boolean isPT = projectType != null &&
                (projectType.equalsIgnoreCase("PT1") || projectType.equalsIgnoreCase("PT2"));
        group.setIsPT(isPT);

        Map<String, StandardGroupVO.LimitRange> thresholds = new HashMap<>();
        for (SysTestStandard std : items) {
            String item = std.getThresholdItem();
            if (item != null) {
                thresholds.put(item.toLowerCase(),
                        new StandardGroupVO.LimitRange(std.getLimitMin(), std.getLimitMax()));
            }
        }
        group.setThresholds(thresholds);
        return group;
    }

    /**
     * 执行 update 更新处理。
     *
     * @param request 参数 request。
     */
    @Override
    public void update(StandardUpdateRequest request) {
        // 校验上限必须大于等于下限
        if (request.getLimitMax().compareTo(request.getLimitMin()) < 0) {
            throw new IllegalArgumentException("上限值必须大于等于下限值");
        }

        SysTestStandard std = new SysTestStandard();
        std.setId(request.getId());
        std.setLimitMin(request.getLimitMin());
        std.setLimitMax(request.getLimitMax());
        standardMapper.updateById(std);
    }

    private StandardVO toVO(SysTestStandard entity) {
        StandardVO vo = new StandardVO();
        BeanUtils.copyProperties(entity, vo);
        return vo;
    }
}
