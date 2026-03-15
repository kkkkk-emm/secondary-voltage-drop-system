package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.common.PageResult;
import com.straykun.svd.svdsys.controller.dto.LogPageQuery;
import com.straykun.svd.svdsys.controller.vo.LogVO;
import com.straykun.svd.svdsys.domain.SysLog;
import com.straykun.svd.svdsys.mapper.SysLogMapper;
import com.straykun.svd.svdsys.service.LogService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class LogServiceImpl implements LogService {

    private final SysLogMapper sysLogMapper;

    public LogServiceImpl(SysLogMapper sysLogMapper) {
        this.sysLogMapper = sysLogMapper;
    }

    @Override
    public PageResult<LogVO> page(LogPageQuery query) {
        long offset = (long) (query.getPage() - 1) * query.getSize();

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        if (query.getStartDate() != null && !query.getStartDate().isEmpty()) {
            startDate = LocalDate.parse(query.getStartDate(), formatter).atStartOfDay();
        }
        if (query.getEndDate() != null && !query.getEndDate().isEmpty()) {
            endDate = LocalDate.parse(query.getEndDate(), formatter).atTime(LocalTime.MAX);
        }

        List<SysLog> logs = sysLogMapper.selectPage(offset, query.getSize(),
                query.getUsername(), startDate, endDate);
        long total = sysLogMapper.count(query.getUsername(), startDate, endDate);

        List<LogVO> voList = logs.stream().map(log -> {
            LogVO vo = new LogVO();
            BeanUtils.copyProperties(log, vo);
            return vo;
        }).collect(Collectors.toList());

        return PageResult.of(total, query.getSize(), query.getPage(), voList);
    }
}

