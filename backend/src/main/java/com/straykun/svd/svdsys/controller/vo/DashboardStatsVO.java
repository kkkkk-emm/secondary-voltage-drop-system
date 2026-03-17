package com.straykun.svd.svdsys.controller.vo;

import java.util.List;

/**
 * 看板统计响应视图对象。
 */
public class DashboardStatsVO {

    /**
     * deviceCount 字段。
     */
    private Long deviceCount;

    /**
     * taskCount 字段。
     */
    private Long taskCount;

    /**
     * userCount 字段。
     */
    private Long userCount;

    /**
     * passRate 字段。
     */
    private Double passRate;

    /**
     * taskTrend 字段。
     */
    private List<DayStats> taskTrend;

    /**
     * passFailDistribution 字段。
     */
    private PassFailDistribution passFailDistribution;

    /**
     * recentTasks 字段。
     */
    private List<RecentTask> recentTasks;

    // 内部类：每日统计
    /**
     * Day统计响应视图对象。
     */
    public static class DayStats {
        /**
         * date 字段。
         */
        private String date;
        /**
         * count 字段。
         */
        private Long count;

        /**
         * 构造函数，初始化 DayStats 所需依赖。
         */
        public DayStats() {}

        /**
         * 构造函数，初始化 DayStats 所需依赖。
         *
         * @param date 参数 date。
         * @param count 参数 count。
         */
        public DayStats(String date, Long count) {
            this.date = date;
            this.count = count;
        }

        /**
         * 查询 getDate 相关信息。
         *
         * @return 返回字符串结果。
         */
        public String getDate() { return date; }
        /**
         * 执行 setDate 业务逻辑。
         *
         * @param date 参数 date。
         */
        public void setDate(String date) { this.date = date; }
        /**
         * 查询 getCount 相关信息。
         *
         * @return 返回处理结果数量。
         */
        public Long getCount() { return count; }
        /**
         * 执行 setCount 业务逻辑。
         *
         * @param count 参数 count。
         */
        public void setCount(Long count) { this.count = count; }
    }

    // 内部类：合格/不合格分布
    /**
     * PassFailDistribution响应视图对象。
     */
    public static class PassFailDistribution {
        /**
         * passCount 字段。
         */
        private Long passCount;
        /**
         * failCount 字段。
         */
        private Long failCount;

        /**
         * 构造函数，初始化 PassFailDistribution 所需依赖。
         */
        public PassFailDistribution() {}

        /**
         * 构造函数，初始化 PassFailDistribution 所需依赖。
         *
         * @param passCount 参数 passCount。
         * @param failCount 参数 failCount。
         */
        public PassFailDistribution(Long passCount, Long failCount) {
            this.passCount = passCount;
            this.failCount = failCount;
        }

        /**
         * 查询 getPassCount 相关信息。
         *
         * @return 返回处理结果数量。
         */
        public Long getPassCount() { return passCount; }
        /**
         * 执行 setPassCount 业务逻辑。
         *
         * @param passCount 参数 passCount。
         */
        public void setPassCount(Long passCount) { this.passCount = passCount; }
        /**
         * 查询 getFailCount 相关信息。
         *
         * @return 返回处理结果数量。
         */
        public Long getFailCount() { return failCount; }
        /**
         * 执行 setFailCount 业务逻辑。
         *
         * @param failCount 参数 failCount。
         */
        public void setFailCount(Long failCount) { this.failCount = failCount; }
    }

    // 内部类：最近任务
    /**
     * Recent检定任务响应视图对象。
     */
    public static class RecentTask {
        /**
         * 主键 ID。
         */
        private Long id;
        /**
         * deviceProductNo 字段。
         */
        private String deviceProductNo;
        /**
         * 操作员姓名。
         */
        private String operatorName;
        /**
         * 测试日期，格式为 yyyy-MM-dd HH:mm:ss。
         */
        private String testDate;
        /**
         * result 字段。
         */
        private Integer result; // 1:合格 0:不合格

        /**
         * 查询 getId 相关信息。
         *
         * @return 返回处理结果数量。
         */
        public Long getId() { return id; }
        /**
         * 执行 setId 业务逻辑。
         *
         * @param id 参数 id。
         */
        public void setId(Long id) { this.id = id; }
        /**
         * 查询 getDeviceProductNo 相关信息。
         *
         * @return 返回字符串结果。
         */
        public String getDeviceProductNo() { return deviceProductNo; }
        /**
         * 执行 setDeviceProductNo 业务逻辑。
         *
         * @param deviceProductNo 参数 deviceProductNo。
         */
        public void setDeviceProductNo(String deviceProductNo) { this.deviceProductNo = deviceProductNo; }
        /**
         * 查询 getOperatorName 相关信息。
         *
         * @return 返回字符串结果。
         */
        public String getOperatorName() { return operatorName; }
        /**
         * 执行 setOperatorName 业务逻辑。
         *
         * @param operatorName 参数 operatorName。
         */
        public void setOperatorName(String operatorName) { this.operatorName = operatorName; }
        /**
         * 查询 getTestDate 相关信息。
         *
         * @return 返回字符串结果。
         */
        public String getTestDate() { return testDate; }
        /**
         * 执行 setTestDate 业务逻辑。
         *
         * @param testDate 参数 testDate。
         */
        public void setTestDate(String testDate) { this.testDate = testDate; }
        /**
         * 查询 getResult 相关信息。
         *
         * @return 返回处理结果数量。
         */
        public Integer getResult() { return result; }
        /**
         * 执行 setResult 业务逻辑。
         *
         * @param result 参数 result。
         */
        public void setResult(Integer result) { this.result = result; }
    }

    // Getters and Setters
    /**
     * 查询 getDeviceCount 相关信息。
     *
     * @return 返回处理结果数量。
     */
    public Long getDeviceCount() { return deviceCount; }
    /**
     * 执行 setDeviceCount 业务逻辑。
     *
     * @param deviceCount 参数 deviceCount。
     */
    public void setDeviceCount(Long deviceCount) { this.deviceCount = deviceCount; }
    /**
     * 查询 getTaskCount 相关信息。
     *
     * @return 返回处理结果数量。
     */
    public Long getTaskCount() { return taskCount; }
    /**
     * 执行 setTaskCount 业务逻辑。
     *
     * @param taskCount 参数 taskCount。
     */
    public void setTaskCount(Long taskCount) { this.taskCount = taskCount; }
    /**
     * 查询 getUserCount 相关信息。
     *
     * @return 返回处理结果数量。
     */
    public Long getUserCount() { return userCount; }
    /**
     * 执行 setUserCount 业务逻辑。
     *
     * @param userCount 参数 userCount。
     */
    public void setUserCount(Long userCount) { this.userCount = userCount; }
    /**
     * 查询 getPassRate 相关信息。
     *
     * @return 返回处理结果。
     */
    public Double getPassRate() { return passRate; }
    /**
     * 执行 setPassRate 业务逻辑。
     *
     * @param passRate 参数 passRate。
     */
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    /**
     * 查询 getTaskTrend 相关信息。
     *
     * @return 返回结果列表。
     */
    public List<DayStats> getTaskTrend() { return taskTrend; }
    /**
     * 执行 setTaskTrend 业务逻辑。
     *
     * @param taskTrend 参数 taskTrend。
     */
    public void setTaskTrend(List<DayStats> taskTrend) { this.taskTrend = taskTrend; }
    /**
     * 查询 getPassFailDistribution 相关信息。
     *
     * @return 返回处理结果。
     */
    public PassFailDistribution getPassFailDistribution() { return passFailDistribution; }
    /**
     * 执行 setPassFailDistribution 业务逻辑。
     *
     * @param passFailDistribution 参数 passFailDistribution。
     */
    public void setPassFailDistribution(PassFailDistribution passFailDistribution) { this.passFailDistribution = passFailDistribution; }
    /**
     * 查询 getRecentTasks 相关信息。
     *
     * @return 返回结果列表。
     */
    public List<RecentTask> getRecentTasks() { return recentTasks; }
    /**
     * 执行 setRecentTasks 业务逻辑。
     *
     * @param recentTasks 参数 recentTasks。
     */
    public void setRecentTasks(List<RecentTask> recentTasks) { this.recentTasks = recentTasks; }
}
