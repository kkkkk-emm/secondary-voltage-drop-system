package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.DevicePageQuery;
import com.straykun.svd.svdsys.controller.dto.DeviceSaveRequest;
import com.straykun.svd.svdsys.controller.dto.DeviceUpdateRequest;
import com.straykun.svd.svdsys.controller.vo.DeviceVO;
import com.straykun.svd.svdsys.domain.BizDevice;
import com.straykun.svd.svdsys.mapper.BizDeviceMapper;
import com.straykun.svd.svdsys.service.DeviceService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 设备服务实现类。
 */
@Service
public class DeviceServiceImpl implements DeviceService {

    private final BizDeviceMapper bizDeviceMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * 构造函数，初始化 DeviceServiceImpl 所需依赖。
     *
     * @param bizDeviceMapper 参数 bizDeviceMapper。
     */
    public DeviceServiceImpl(BizDeviceMapper bizDeviceMapper) {
        this.bizDeviceMapper = bizDeviceMapper;
    }

    /**
     * 查询 page 相关信息。
     *
     * @param query 参数 query。
     * @return 返回分页结果。
     */
    @Override
    public PageResult<DeviceVO> page(DevicePageQuery query) {
        long current = query.getPage() == null || query.getPage() <= 0 ? 1 : query.getPage();
        long size = query.getSize() == null || query.getSize() <= 0 ? 10 : query.getSize();
        long offset = (current - 1) * size;

        List<BizDevice> list = bizDeviceMapper.selectPage(offset, size, query.getProductNo(), query.getProductName(), query.getManufacturer());
        long total = bizDeviceMapper.count(query.getProductNo(), query.getProductName(), query.getManufacturer());

        List<DeviceVO> records = list.stream().map(this::toVO).collect(Collectors.toList());
        return PageResult.of(total, size, current, records);
    }

    /**
     * 执行 add 业务逻辑。
     *
     * @param request 参数 request。
     */
    @Override
    public void add(DeviceSaveRequest request) {
        // 唯一性校验：产品编号不能重复
        BizDevice exists = bizDeviceMapper.selectByProductNo(request.getProductNo());
        if (exists != null) {
            throw new IllegalArgumentException("产品编号已存在");
        }
        BizDevice device = new BizDevice();
        device.setProductNo(request.getProductNo());
        device.setProductName(request.getProductName());
        device.setModel(request.getModel());
        device.setManufacturer(request.getManufacturer());
        device.setPlaceOrigin(request.getPlaceOrigin());
        if (StringUtils.hasText(request.getProductionDate())) {
            device.setProductionDate(LocalDate.parse(request.getProductionDate(), DATE_FORMATTER));
        }
        bizDeviceMapper.insert(device);
    }

    /**
     * 执行 update 更新处理。
     *
     * @param request 参数 request。
     */
    @Override
    public void update(DeviceUpdateRequest request) {
        BizDevice device = bizDeviceMapper.selectById(request.getId());
        if (device == null) {
            throw new IllegalArgumentException("设备不存在");
        }
        // 如果修改了产品编号，需要校验唯一
        if (!device.getProductNo().equals(request.getProductNo())) {
            BizDevice exists = bizDeviceMapper.selectByProductNo(request.getProductNo());
            if (exists != null && !exists.getId().equals(request.getId())) {
                throw new IllegalArgumentException("产品编号已存在");
            }
        }
        device.setProductNo(request.getProductNo());
        device.setProductName(request.getProductName());
        device.setModel(request.getModel());
        device.setManufacturer(request.getManufacturer());
        device.setPlaceOrigin(request.getPlaceOrigin());
        if (StringUtils.hasText(request.getProductionDate())) {
            device.setProductionDate(LocalDate.parse(request.getProductionDate(), DATE_FORMATTER));
        } else {
            device.setProductionDate(null);
        }
        bizDeviceMapper.update(device);
    }

    /**
     * 执行 delete 删除处理。
     *
     * @param id 参数 id。
     */
    @Override
    public void delete(Long id) {
        bizDeviceMapper.deleteById(id);
    }

    /**
     * 查询 listAll 相关信息。
     *
     * @return 返回结果列表。
     */
    @Override
    public List<DeviceVO> listAll() {
        List<BizDevice> list = bizDeviceMapper.selectAll();
        return list.stream().map(this::toVO).collect(Collectors.toList());
    }
    private DeviceVO toVO(BizDevice entity) {
        DeviceVO vo = new DeviceVO();
        BeanUtils.copyProperties(entity, vo);
        if (entity.getProductionDate() != null) {
            vo.setProductionDate(entity.getProductionDate().format(DATE_FORMATTER));
        }
        if (entity.getCreateTime() != null) {
            vo.setCreateTime(entity.getCreateTime().toString());
        }
        return vo;
    }
}
