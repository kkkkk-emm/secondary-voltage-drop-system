package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.controller.vo.DashboardStatsVO;
import com.straykun.svd.svdsys.mapper.BizDeviceMapper;
import com.straykun.svd.svdsys.mapper.BizTestTaskMapper;
import com.straykun.svd.svdsys.mapper.SysUserMapper;
import com.straykun.svd.svdsys.service.DashboardService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据可视化统计服务实现
 */
@Service
public class DashboardServiceImpl implements DashboardService {

    private final BizDeviceMapper deviceMapper;
    private final BizTestTaskMapper taskMapper;
    private final SysUserMapper userMapper;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public DashboardServiceImpl(BizDeviceMapper deviceMapper,
                                 BizTestTaskMapper taskMapper,
                                 SysUserMapper userMapper) {
        this.deviceMapper = deviceMapper;
        this.taskMapper = taskMapper;
        this.userMapper = userMapper;
    }

    @Override
    public DashboardStatsVO getStats() {
        DashboardStatsVO stats = new DashboardStatsVO();

        // 1. 设备总数
        long deviceCount = deviceMapper.count(null, null, null);
        stats.setDeviceCount(deviceCount);

        // 2. 任务总数
        long taskCount = taskMapper.count(null, null, null, null, null, null, null);
        stats.setTaskCount(taskCount);

        // 3. 用户总数
        long userCount = userMapper.count(null, null, null);
        stats.setUserCount(userCount);

        // 4. 合格率
        long passCount = taskMapper.count(null, null, null, null, null, null, 1);
        long failCount = taskMapper.count(null, null, null, null, null, null, 0);
        long totalTasks = passCount + failCount;
        double passRate = totalTasks > 0 ? Math.round(passCount * 1000.0 / totalTasks) / 10.0 : 0.0;
        stats.setPassRate(passRate);

        // 5. 合格/不合格分布
        stats.setPassFailDistribution(new DashboardStatsVO.PassFailDistribution(passCount, failCount));

        // 6. 最近7天任务趋势
        List<DashboardStatsVO.DayStats> taskTrend = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) {
            LocalDate date = today.minusDays(i);
            long count = taskMapper.count(null, null, null, null, date, date, null);
            taskTrend.add(new DashboardStatsVO.DayStats(date.format(DATE_FORMATTER), count));
        }
        stats.setTaskTrend(taskTrend);

        // 7. 最近检定任务（最新5条）
        List<BizTestTaskMapper.TaskRecord> recentRecords = taskMapper.selectPage(0, 5, null, null, null, null, null, null, null);
        List<DashboardStatsVO.RecentTask> recentTasks = new ArrayList<>();
        for (BizTestTaskMapper.TaskRecord record : recentRecords) {
            DashboardStatsVO.RecentTask task = new DashboardStatsVO.RecentTask();
            task.setId(record.id);
            task.setDeviceProductNo(record.deviceProductNo);
            task.setOperatorName(record.operatorName);
            task.setTestDate(record.testDate != null ? record.testDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null);
            task.setResult(record.result);
            recentTasks.add(task);
        }
        stats.setRecentTasks(recentTasks);

        return stats;
    }
}
