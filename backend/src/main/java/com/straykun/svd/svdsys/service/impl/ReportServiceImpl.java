package com.straykun.svd.svdsys.service.impl;

import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.HorizontalAlignment;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.straykun.svd.svdsys.controller.vo.TaskDetailVO;
import com.straykun.svd.svdsys.service.ReportService;
import org.springframework.stereotype.Service;

import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * 报告服务实现类。
 */
@Service
public class ReportServiceImpl implements ReportService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    // 定义表头常量，避免硬编码
    private static final String[] TABLE_HEADERS = {"相别", "f(%)", "δ(分)", "dU(%)", "Upt", "Uyb", "结果"};
    private static final DeviceRgb COLOR_GREEN = new DeviceRgb(0, 128, 0);
    private static final DeviceRgb COLOR_RED = new DeviceRgb(255, 0, 0);
    private static final DeviceRgb BG_GRAY = new DeviceRgb(240, 240, 240);

    /**
     * 执行 generatePdf 业务逻辑。
     *
     * @param detail 参数 detail。
     * @param outputStream 参数 outputStream。
     */
    @Override
    public void generatePdf(TaskDetailVO detail, OutputStream outputStream) {
        try {
            PdfWriter writer = new PdfWriter(outputStream);
            PdfDocument pdfDoc = new PdfDocument(writer);
            Document document = new Document(pdfDoc);

            // 使用系统字体支持中文
            PdfFont chineseFont = createChineseFont();

            // 1. 标题
            addTitle(document, chineseFont);

            // 2. 任务基本信息
            addTaskInfo(document, detail, chineseFont);

            // 3. 设备信息
            addDeviceInfo(document, detail, chineseFont);

            // 4. 检测明细数据
            addResultTable(document, detail, chineseFont);

            // 5. 结论
            addConclusion(document, detail, chineseFont);

            // 6. 签章区域
            addSignature(document, chineseFont);

            document.close();
        } catch (Exception e) {
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    private PdfFont createChineseFont() {
        try {
            // 尝试使用 Windows 系统中文字体（按优先级）
            String[][] fontConfigs = {
                    {"C:\\Windows\\Fonts\\simhei.ttf", null},     // 黑体（单字体文件，最简单）
                    {"C:\\Windows\\Fonts\\simsun.ttc", "0"},      // 宋体（ttc 集合文件）
                    {"C:\\Windows\\Fonts\\simfang.ttf", null},    // 仿宋
                    {"C:\\Windows\\Fonts\\msyh.ttc", "0"},        // 微软雅黑
            };

            for (String[] config : fontConfigs) {
                String fontPath = config[0];
                String ttcIndex = config[1];
                try {
                    java.io.File fontFile = new java.io.File(fontPath);
                    if (fontFile.exists()) {
                        String fontSource = ttcIndex != null ? fontPath + "," + ttcIndex : fontPath;
                        return PdfFontFactory.createFont(fontSource, PdfEncodings.IDENTITY_H,
                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    }
                } catch (Exception e) {
                    // 继续尝试下一个字体
                }
            }
            // 如果所有字体都不可用，抛出明确错误
            throw new RuntimeException("系统中未找到可用的中文字体");
        } catch (Exception e) {
            throw new RuntimeException("无法加载中文字体: " + e.getMessage(), e);
        }
    }

    private void addTitle(Document document, PdfFont font) {
        Paragraph title = new Paragraph("互感器二次压降检测仪检定证书")
                .setFont(font)
                .setFontSize(22)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20);
        document.add(title);
    }

    private void addTaskInfo(Document document, TaskDetailVO detail, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("一、检定任务信息")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        TaskDetailVO.TaskInfo taskInfo = detail.getTaskInfo();
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        addTableRow(table, font, "任务编号", String.valueOf(taskInfo.getId()));
        addTableRow(table, font, "计量点编号", taskInfo.getMeterPointId() != null ? taskInfo.getMeterPointId() : "-");
        addTableRow(table, font, "送检日期", taskInfo.getDeliverDate() != null ? taskInfo.getDeliverDate() : "-");
        addTableRow(table, font, "检定日期", taskInfo.getTestDate() != null ? taskInfo.getTestDate() : "-");
        addTableRow(table, font, "检定员", detail.getOperatorName() != null ? detail.getOperatorName() : "-");
        addTableRow(table, font, "", "");
        addTableRow(table, font, "环境温度", taskInfo.getTemperature() != null ? taskInfo.getTemperature() + " ℃" : "-");
        addTableRow(table, font, "环境湿度", taskInfo.getHumidity() != null ? taskInfo.getHumidity() + " %" : "-");
        addTableRow(table, font, "tanφ", taskInfo.getTanPhi() != null ? String.valueOf(taskInfo.getTanPhi()) : "-");
        addTableRow(table, font, "r%", taskInfo.getRPercent() != null ? String.valueOf(taskInfo.getRPercent()) : "-");

        document.add(table);
    }

    private void addDeviceInfo(Document document, TaskDetailVO detail, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("二、被检设备信息")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        TaskDetailVO.DeviceInfo deviceInfo = detail.getDeviceInfo();
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 2, 1, 2}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        if (deviceInfo != null) {
            addTableRow(table, font, "产品编号", deviceInfo.getProductNo() != null ? deviceInfo.getProductNo() : "-");
            addTableRow(table, font, "产品名称", deviceInfo.getProductName() != null ? deviceInfo.getProductName() : "-");
            addTableRow(table, font, "型号规格", deviceInfo.getModel() != null ? deviceInfo.getModel() : "-");
            addTableRow(table, font, "制造厂商", deviceInfo.getManufacturer() != null ? deviceInfo.getManufacturer() : "-");
        } else {
            addTableRow(table, font, "产品编号", "-");
            addTableRow(table, font, "产品名称", "-");
        }

        document.add(table);
    }

    /**
     * 执行 addResultTable 业务逻辑。
     *
     * @param document 参数 document。
     * @param detail 参数 detail。
     * @param font 参数 font。
     */
    public void addResultTable(Document document, TaskDetailVO detail, PdfFont font) {
        // 1. 添加标题
        addSectionTitle(document, font, "三、检定数据明细");

        // 2. 初始化表格结构
        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1.5f, 1.5f, 1.5f, 1.5f, 1.5f, 1}));
        table.setWidth(UnitValue.createPercentValue(100));
        table.setMarginBottom(20);

        // 3. 添加表头
        for (String header : TABLE_HEADERS) {
            addHeaderCell(table, font, header, BG_GRAY);
        }

        // 4. 填充数据
        fillTableData(table, detail.getResultList(), font);

        document.add(table);
    }

// ================= 辅助方法 (降低复杂度) =================

    private void fillTableData(Table table, List<TaskDetailVO.ResultInfo> resultList, PdfFont font) {
        if (resultList == null || resultList.isEmpty()) {
            // 无数据：填充一行空占位符
            for (int i = 0; i < TABLE_HEADERS.length; i++) {
                addDataCell(table, font, "-");
            }
            return;
        }

        // 有数据：遍历填充
        for (TaskDetailVO.ResultInfo result : resultList) {
            addDataCell(table, font, formatPhase(result.getPhase()));
            addDataCell(table, font, formatValue(result.getValF()));
            addDataCell(table, font, formatValue(result.getValDelta()));
            addDataCell(table, font, formatValue(result.getValDu()));
            addDataCell(table, font, formatValue(result.getValUpt()));
            addDataCell(table, font, formatValue(result.getValUyb()));

            // 特殊处理最后一列（合格/不合格颜色）
            table.addCell(createStatusCell(result.getIsPass(), font));
        }
    }

    private Cell createStatusCell(Integer isPass, PdfFont font) {
        boolean pass = isPass != null && isPass == 1;
        String text = pass ? "合格" : "不合格";
        DeviceRgb color = pass ? COLOR_GREEN : COLOR_RED;

        return new Cell()
                .add(new Paragraph(text)
                        .setFont(font)
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.CENTER))
                .setFontColor(color);
    }

    private String formatValue(Object val) {
        return val == null ? "-" : val.toString();
    }

    private String formatPhase(String phase) {
        return phase == null ? "-" : phase.toUpperCase();
    }

    private void addSectionTitle(Document document, PdfFont font, String titleText) {
        Paragraph title = new Paragraph(titleText)
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(title);
    }

    private void addConclusion(Document document, TaskDetailVO detail, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("四、检定结论")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        // 计算总体结论
        boolean allPass = true;
        if (detail.getResultList() != null) {
            for (TaskDetailVO.ResultInfo result : detail.getResultList()) {
                if (result.getIsPass() == null || result.getIsPass() != 1) {
                    allPass = false;
                    break;
                }
            }
        }

        String conclusionText = allPass ?
                "经检定，该被检设备各项指标均符合检定规程要求，判定为【合格】。" :
                "经检定，该被检设备部分指标不符合检定规程要求，判定为【不合格】。";

        Paragraph conclusion = new Paragraph(conclusionText)
                .setFont(font)
                .setFontSize(12)
                .setFontColor(allPass ? new DeviceRgb(0, 128, 0) : new DeviceRgb(255, 0, 0))
                .setMarginBottom(30);
        document.add(conclusion);
    }

    private void addSignature(Document document, PdfFont font) {
        Paragraph sectionTitle = new Paragraph("五、签章")
                .setFont(font)
                .setFontSize(14)
                .setBold()
                .setMarginBottom(10);
        document.add(sectionTitle);

        Table table = new Table(UnitValue.createPercentArray(new float[]{1, 1, 1}));
        table.setWidth(UnitValue.createPercentValue(100));

        Cell cell1 = new Cell()
                .add(new Paragraph("检定员签字：_______________").setFont(font).setFontSize(11))
                .setBorder(null)
                .setPaddingTop(20);
        Cell cell2 = new Cell()
                .add(new Paragraph("核验员签字：_______________").setFont(font).setFontSize(11))
                .setBorder(null)
                .setPaddingTop(20);
        Cell cell3 = new Cell()
                .add(new Paragraph("日期：" + LocalDateTime.now().format(DATE_FORMATTER)).setFont(font).setFontSize(11))
                .setBorder(null)
                .setPaddingTop(20);

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);

        document.add(table);

        // 底部单位盖章区
        Paragraph stampArea = new Paragraph("检定机构（盖章）：")
                .setFont(font)
                .setFontSize(11)
                .setTextAlignment(TextAlignment.RIGHT)
                .setMarginTop(50);
        document.add(stampArea);
    }

    private void addTableRow(Table table, PdfFont font, String label, String value) {
        Cell labelCell = new Cell()
                .add(new Paragraph(label).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(new DeviceRgb(248, 248, 248))
                .setTextAlignment(TextAlignment.RIGHT)
                .setPadding(5);
        Cell valueCell = new Cell()
                .add(new Paragraph(value).setFont(font).setFontSize(10))
                .setPadding(5);
        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    private void addHeaderCell(Table table, PdfFont font, String text, DeviceRgb bgColor) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10).setBold())
                .setBackgroundColor(bgColor)
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(cell);
    }

    private void addDataCell(Table table, PdfFont font, String text) {
        Cell cell = new Cell()
                .add(new Paragraph(text).setFont(font).setFontSize(10))
                .setTextAlignment(TextAlignment.CENTER)
                .setPadding(5);
        table.addCell(cell);
    }
}
