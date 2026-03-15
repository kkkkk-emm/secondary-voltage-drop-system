package com.straykun.svd.svdsys.controller.vo;

import java.util.List;

/**
 * 数据可视化统计数据VO
 */
public class DashboardStatsVO {

    /** 设备总数 */
    private Long deviceCount;

    /** 检定任务总数 */
    private Long taskCount;

    /** 用户总数 */
    private Long userCount;

    /** 检定合格率 (百分比) */
    private Double passRate;

    /** 最近7天任务趋势 */
    private List<DayStats> taskTrend;

    /** 合格/不合格分布 */
    private PassFailDistribution passFailDistribution;

    /** 最近检定任务 */
    private List<RecentTask> recentTasks;

    // 内部类：每日统计
    public static class DayStats {
        private String date;
        private Long count;

        public DayStats() {}
        
        public DayStats(String date, Long count) {
            this.date = date;
            this.count = count;
        }

        public String getDate() { return date; }
        public void setDate(String date) { this.date = date; }
        public Long getCount() { return count; }
        public void setCount(Long count) { this.count = count; }
    }

    // 内部类：合格/不合格分布
    public static class PassFailDistribution {
        private Long passCount;
        private Long failCount;

        public PassFailDistribution() {}
        
        public PassFailDistribution(Long passCount, Long failCount) {
            this.passCount = passCount;
            this.failCount = failCount;
        }

        public Long getPassCount() { return passCount; }
        public void setPassCount(Long passCount) { this.passCount = passCount; }
        public Long getFailCount() { return failCount; }
        public void setFailCount(Long failCount) { this.failCount = failCount; }
    }

    // 内部类：最近任务
    public static class RecentTask {
        private Long id;
        private String deviceProductNo;
        private String operatorName;
        private String testDate;
        private Integer result; // 1:合格 0:不合格

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getDeviceProductNo() { return deviceProductNo; }
        public void setDeviceProductNo(String deviceProductNo) { this.deviceProductNo = deviceProductNo; }
        public String getOperatorName() { return operatorName; }
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        public String getTestDate() { return testDate; }
        public void setTestDate(String testDate) { this.testDate = testDate; }
        public Integer getResult() { return result; }
        public void setResult(Integer result) { this.result = result; }
    }

    // Getters and Setters
    public Long getDeviceCount() { return deviceCount; }
    public void setDeviceCount(Long deviceCount) { this.deviceCount = deviceCount; }
    public Long getTaskCount() { return taskCount; }
    public void setTaskCount(Long taskCount) { this.taskCount = taskCount; }
    public Long getUserCount() { return userCount; }
    public void setUserCount(Long userCount) { this.userCount = userCount; }
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    public List<DayStats> getTaskTrend() { return taskTrend; }
    public void setTaskTrend(List<DayStats> taskTrend) { this.taskTrend = taskTrend; }
    public PassFailDistribution getPassFailDistribution() { return passFailDistribution; }
    public void setPassFailDistribution(PassFailDistribution passFailDistribution) { this.passFailDistribution = passFailDistribution; }
    public List<RecentTask> getRecentTasks() { return recentTasks; }
    public void setRecentTasks(List<RecentTask> recentTasks) { this.recentTasks = recentTasks; }
}
