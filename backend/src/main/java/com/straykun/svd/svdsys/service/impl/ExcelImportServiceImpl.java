package com.straykun.svd.svdsys.service.impl;

import com.straykun.svd.svdsys.controller.vo.ExcelImportResultVO;
import com.straykun.svd.svdsys.controller.vo.ExcelPreviewVO;
import com.straykun.svd.svdsys.domain.BizDevice;
import com.straykun.svd.svdsys.domain.BizTestResult;
import com.straykun.svd.svdsys.domain.BizTestTask;
import com.straykun.svd.svdsys.domain.SysTestStandard;
import com.straykun.svd.svdsys.mapper.BizDeviceMapper;
import com.straykun.svd.svdsys.mapper.BizTestResultMapper;
import com.straykun.svd.svdsys.mapper.BizTestTaskMapper;
import com.straykun.svd.svdsys.mapper.SysTestStandardMapper;
import com.straykun.svd.svdsys.security.SecurityUtils;
import com.straykun.svd.svdsys.service.ExcelImportService;
import lombok.Data;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.CollectionUtils;

import java.io.InputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;

/**
 * Excel 导入服务实现
 */
@Service
public class ExcelImportServiceImpl implements ExcelImportService {

    // ==========================================
    // 1. 静态常量 (Constants)
    // ==========================================
    public static final String EXCEL_NO_DATA_ROW = "Excel 文件没有数据行";
    public static final String EXCEL_FAIL_PARSE = "Excel 解析失败: ";

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // 预编译正则与集合
    private static final Pattern PRODUCT_NO_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]+$");
    private static final Set<String> VALID_PROJECT_TYPES = Set.of("PT1", "PT2", "CT1", "CT2");
    private static final Set<String> VALID_PHASES = Set.of("ao", "bo", "co", "AO", "BO", "CO");
    private static final List<DateTimeFormatter> DATE_FORMATTERS = Arrays.asList(
            DateTimeFormatter.ofPattern("yyyy-MM-dd"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    // ==========================================
    // 2. 依赖注入 (Dependencies)
    // ==========================================
    private final BizDeviceMapper deviceMapper;
    private final BizTestTaskMapper taskMapper;
    private final BizTestResultMapper resultMapper;
    private final SysTestStandardMapper standardMapper;

    public ExcelImportServiceImpl(BizDeviceMapper deviceMapper,
                                  BizTestTaskMapper taskMapper,
                                  BizTestResultMapper resultMapper,
                                  SysTestStandardMapper standardMapper) {
        this.deviceMapper = deviceMapper;
        this.taskMapper = taskMapper;
        this.resultMapper = resultMapper;
        this.standardMapper = standardMapper;
    }

    // ==========================================
    // 3. 公有业务接口 (Public API)
    // ==========================================

    @Override
    public ExcelPreviewVO previewTasks(MultipartFile file) {
        return executePreview(file, true);
    }

    @Override
    public ExcelPreviewVO previewDevices(MultipartFile file) {
        return executePreview(file, false);
    }

    @Override
    @Transactional
    public ExcelImportResultVO importTasks(MultipartFile file) {
        ExcelImportResultVO result = new ExcelImportResultVO();

        // 基础校验
        String validationError = validateExcelFile(file);
        if (validationError != null) {
            result.addError(0, validationError);
            return result;
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            result.setTotalRows(lastRowNum);

            if (lastRowNum < 1) {
                result.addError(0, EXCEL_NO_DATA_ROW);
                return result;
            }

            // 表头校验
            Row headerRow = sheet.getRow(0);
            String headerError = validateTaskHeader(headerRow);
            if (headerError != null) {
                result.addError(1, headerError);
                return result;
            }

            // 权限校验
            String userIdStr = SecurityUtils.getCurrentUserId();
            if (userIdStr == null) {
                result.addError(0, "未认证用户，无法导入");
                return result;
            }
            Long operatorId = Long.valueOf(userIdStr);

            // 逐行处理
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                try {
                    importTaskRow(row, i + 1, operatorId, result);
                } catch (Exception e) {
                    result.addError(i + 1, "导入失败: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            result.addError(0, EXCEL_FAIL_PARSE + e.getMessage());
        }

        return result;
    }

    @Override
    @Transactional
    public ExcelImportResultVO importDevices(MultipartFile file) {
        ExcelImportResultVO result = new ExcelImportResultVO();

        String validationError = validateExcelFile(file);
        if (validationError != null) {
            result.addError(0, validationError);
            return result;
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            result.setTotalRows(lastRowNum);

            if (lastRowNum < 1) {
                result.addError(0, EXCEL_NO_DATA_ROW);
                return result;
            }

            // 表头校验
            Row headerRow = sheet.getRow(0);
            String headerError = validateDeviceHeader(headerRow);
            if (headerError != null) {
                result.addError(1, headerError);
                return result;
            }

            // 逐行处理
            for (int i = 1; i <= lastRowNum; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;
                try {
                    importDeviceRow(row, i + 1, result);
                } catch (Exception e) {
                    result.addError(i + 1, "导入失败: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            result.addError(0, EXCEL_FAIL_PARSE + e.getMessage());
        }

        return result;
    }

    // ==========================================
    // 4. 私有业务逻辑：预览相关 (Preview Logic)
    // ==========================================

    /**
     * 统一预览执行逻辑
     */
    private ExcelPreviewVO executePreview(MultipartFile file, boolean isTask) {
        ExcelPreviewVO preview = new ExcelPreviewVO();
        String validationError = validateExcelFile(file);
        if (validationError != null) {
            preview.addValidationError(0, "", validationError);
            preview.calculateValid();
            return preview;
        }

        try (InputStream is = file.getInputStream();
             Workbook workbook = new XSSFWorkbook(is)) {

            Sheet sheet = workbook.getSheetAt(0);
            int lastRowNum = sheet.getLastRowNum();
            preview.setTotalRows(lastRowNum);

            if (lastRowNum < 1) {
                preview.addValidationError(0, "", EXCEL_NO_DATA_ROW);
                preview.calculateValid();
                return preview;
            }

            // 验证并读取表头
            Row headerRow = sheet.getRow(0);
            String headerError = isTask ? validateTaskHeader(headerRow) : validateDeviceHeader(headerRow);
            if (headerError != null) {
                preview.addValidationError(1, "", headerError);
                preview.calculateValid();
                return preview;
            }

            // 设置预期表头
            String[] expectedHeaders = isTask ?
                    new String[]{"设备编号", "计量点编号", "送检日期", "测试日期", "温度", "湿度", "tanφ", "r%", "相别", "项目类型", "档位", "负载%", "f(%)", "δ(分)", "dU(%)", "Upt", "Uyb"} :
                    new String[]{"产品编号", "产品名称", "型号", "制造商", "产地", "生产日期"};
            preview.setHeaders(Arrays.asList(expectedHeaders));

            // 解析数据行（最多预览前 100 行）
            int previewLimit = Math.min(lastRowNum, 100);
            for (int i = 1; i <= previewLimit; i++) {
                Row row = sheet.getRow(i);
                if (row == null || isRowEmpty(row)) continue;

                Map<String, Object> rowData = new LinkedHashMap<>();
                for (int j = 0; j < expectedHeaders.length; j++) {
                    rowData.put(expectedHeaders[j], getCellStringValue(row.getCell(j)));
                }
                preview.getRows().add(rowData);

                if (isTask) {
                    validateTaskRowForPreview(row, i + 1, preview);
                } else {
                    validateDeviceRowForPreview(row, i + 1, preview);
                }
            }

        } catch (Exception e) {
            preview.addValidationError(0, "", EXCEL_FAIL_PARSE + e.getMessage());
        }

        preview.calculateValid();
        return preview;
    }

    private void validateTaskRowForPreview(Row row, int rowNum, ExcelPreviewVO preview) {
        String deviceProductNo = getCellStringValue(row.getCell(0));
        String phase = getCellStringValue(row.getCell(8));
        String projectType = getCellStringValue(row.getCell(9));

        if (!StringUtils.hasText(deviceProductNo)) {
            preview.addValidationError(rowNum, "设备编号", "设备编号不能为空");
        } else {
            if (deviceMapper.selectByProductNo(deviceProductNo.trim()) == null) {
                preview.addValidationError(rowNum, "设备编号", "设备编号 \"" + deviceProductNo + "\" 不存在");
            }
        }

        if (!StringUtils.hasText(phase)) {
            preview.addValidationError(rowNum, "相别", "相别不能为空");
        } else if (!VALID_PHASES.contains(phase.trim())) {
            preview.addValidationError(rowNum, "相别", "相别只能是 ao/bo/co");
        }

        if (!StringUtils.hasText(projectType)) {
            preview.addValidationError(rowNum, "项目类型", "项目类型不能为空");
        } else if (!VALID_PROJECT_TYPES.contains(projectType.trim().toUpperCase())) {
            preview.addValidationError(rowNum, "项目类型", "项目类型只能是 PT1/PT2/CT1/CT2");
        }
    }

    private void validateDeviceRowForPreview(Row row, int rowNum, ExcelPreviewVO preview) {
        String productNo = getCellStringValue(row.getCell(0));
        String productName = getCellStringValue(row.getCell(1));
        String productionDateStr = getCellStringValue(row.getCell(5));

        if (!StringUtils.hasText(productNo)) {
            preview.addValidationError(rowNum, "产品编号", "产品编号不能为空");
        } else {
            if (deviceMapper.selectByProductNo(productNo.trim()) != null) {
                preview.addValidationError(rowNum, "产品编号", "产品编号 \"" + productNo + "\" 已存在");
            }
            if (productNo.length() > 50) {
                preview.addValidationError(rowNum, "产品编号", "产品编号长度不能超过 50 个字符");
            }
            if (!PRODUCT_NO_PATTERN.matcher(productNo.trim()).matches()) {
                preview.addValidationError(rowNum, "产品编号", "产品编号只能包含字母、数字、横线和下划线");
            }
        }

        if (!StringUtils.hasText(productName)) {
            preview.addValidationError(rowNum, "产品名称", "产品名称不能为空");
        }

        if (StringUtils.hasText(productionDateStr) && parseDate(productionDateStr.trim()) == null) {
            preview.addValidationError(rowNum, "生产日期", "生产日期格式错误，应为 yyyy-MM-dd 或 yyyy/MM/dd");
        }
    }

    // ==========================================
    // 5. 私有业务逻辑：任务导入 (Task Import)
    // ==========================================

    private void importTaskRow(Row row, int rowNum, Long operatorId, ExcelImportResultVO result) {
        TaskRowContext ctx = new TaskRowContext(row, rowNum);
        List<String> errors = new ArrayList<>();

        validateBasicFields(ctx, errors);
        validateDevice(ctx, errors);
        validateDates(ctx, errors);
        validateLogic(ctx, errors);
        validateStandard(ctx, errors);

        if (!errors.isEmpty()) {
            result.addError(rowNum, String.join("; ", errors));
            return;
        }

        saveTaskAndResult(ctx, operatorId);
        result.incrementSuccess();
    }

    private void validateBasicFields(TaskRowContext ctx, List<String> errors) {
        if (!StringUtils.hasText(ctx.getDeviceProductNo())) errors.add("设备编号不能为空");
        if (!StringUtils.hasText(ctx.getMeterPointId())) errors.add("计量点编号不能为空");
        if (!StringUtils.hasText(ctx.getDeliverDateStr())) errors.add("送检日期不能为空");
        if (!StringUtils.hasText(ctx.getTestDateStr())) errors.add("测试日期不能为空");
        if (ctx.getTemperature() == null) errors.add("温度不能为空");
        if (ctx.getHumidity() == null) errors.add("湿度不能为空");
        if (ctx.getTanPhi() == null) errors.add("tanφ不能为空");
        if (ctx.getRPercent() == null) errors.add("r%不能为空");
        if (!StringUtils.hasText(ctx.getPhase())) errors.add("相别不能为空");
        if (!StringUtils.hasText(ctx.getProjectType())) errors.add("项目类型不能为空");
        if (!StringUtils.hasText(ctx.getGearLevel())) errors.add("档位不能为空");
        if (!StringUtils.hasText(ctx.getLoadPercent())) errors.add("负载%不能为空");
    }

    private void validateDevice(TaskRowContext ctx, List<String> errors) {
        if (StringUtils.hasText(ctx.getDeviceProductNo())) {
            BizDevice device = deviceMapper.selectByProductNo(ctx.getDeviceProductNo());
            if (device == null) {
                errors.add("设备编号 \"" + ctx.getDeviceProductNo() + "\" 不存在");
            } else {
                ctx.setDevice(device);
            }
        }
    }

    private void validateDates(TaskRowContext ctx, List<String> errors) {
        if (StringUtils.hasText(ctx.getDeliverDateStr())) {
            try {
                ctx.setDeliverDate(LocalDate.parse(ctx.getDeliverDateStr().trim(), DATE_FORMAT));
            } catch (DateTimeParseException e) {
                errors.add("送检日期格式错误，应为 yyyy-MM-dd");
            }
        }
        if (StringUtils.hasText(ctx.getTestDateStr())) {
            try {
                String ts = ctx.getTestDateStr().trim();
                if (ts.length() <= 10) {
                    ctx.setTestDate(LocalDate.parse(ts, DATE_FORMAT).atStartOfDay());
                } else {
                    ctx.setTestDate(LocalDateTime.parse(ts, DATETIME_FORMAT));
                }
            } catch (DateTimeParseException e) {
                errors.add("测试日期格式错误，应为 yyyy-MM-dd 或 yyyy-MM-dd HH:mm:ss");
            }
        }
    }

    private void validateLogic(TaskRowContext ctx, List<String> errors) {
        if (StringUtils.hasText(ctx.getPhase()) && !VALID_PHASES.contains(ctx.getPhase().trim())) {
            errors.add("相别必须是 ao/bo/co 之一");
        }
        if (StringUtils.hasText(ctx.getProjectType()) && !VALID_PROJECT_TYPES.contains(ctx.getProjectType().trim().toUpperCase())) {
            errors.add("项目类型必须是 PT1/PT2/CT1/CT2 之一");
        }
        if (ctx.getTemperature() != null && (ctx.getTemperature().compareTo(new BigDecimal("-50")) < 0 || ctx.getTemperature().compareTo(new BigDecimal("100")) > 0)) {
            errors.add("温度范围应在 -50 到 100 之间");
        }
        if (ctx.getHumidity() != null && (ctx.getHumidity().compareTo(BigDecimal.ZERO) < 0 || ctx.getHumidity().compareTo(new BigDecimal("100")) > 0)) {
            errors.add("湿度范围应在 0 到 100 之间");
        }
    }

    private void validateStandard(TaskRowContext ctx, List<String> errors) {
        if (StringUtils.hasText(ctx.getProjectType()) && StringUtils.hasText(ctx.getGearLevel()) && StringUtils.hasText(ctx.getLoadPercent())) {
            List<SysTestStandard> items = standardMapper.matchAll(
                    ctx.getProjectType().trim().toUpperCase(),
                    ctx.getGearLevel().trim(),
                    ctx.getLoadPercent().trim()
            );
            if (CollectionUtils.isEmpty(items)) {
                errors.add("未找到匹配的检定标准: " + ctx.getProjectType() + "-" + ctx.getGearLevel() + "-" + ctx.getLoadPercent());
            } else {
                ctx.setThresholdItems(items);
            }
        }
    }

    private void saveTaskAndResult(TaskRowContext ctx, Long operatorId) {
        BizTestTask task = new BizTestTask();
        task.setDeviceId(ctx.getDevice().getId());
        task.setOperatorId(operatorId);
        task.setMeterPointId(ctx.getMeterPointId().trim());
        task.setDeliverDate(ctx.getDeliverDate());
        task.setTestDate(ctx.getTestDate());
        task.setTemperature(ctx.getTemperature());
        task.setHumidity(ctx.getHumidity());
        task.setTanPhi(ctx.getTanPhi());
        task.setRPercent(ctx.getRPercent());
        taskMapper.insert(task);

        BizTestResult resultEntity = new BizTestResult();
        resultEntity.setTaskId(task.getId());
        resultEntity.setStandardId(ctx.getThresholdItems().get(0).getId());
        resultEntity.setPhase(ctx.getPhase().trim().toLowerCase());
        resultEntity.setValF(ctx.getValF());
        resultEntity.setValDelta(ctx.getValDelta());
        resultEntity.setValDu(ctx.getValDu());
        resultEntity.setValUpt(ctx.getValUpt());
        resultEntity.setValUyb(ctx.getValUyb());

        boolean isPass = checkPassAllItems(
                ctx.getThresholdItems(),
                ctx.getProjectType(),
                ctx.getValF(),
                ctx.getValDelta(),
                ctx.getValDu(),
                ctx.getValUpt(),
                ctx.getValUyb()
        );
        resultEntity.setIsPass(isPass ? 1 : 0);

        resultMapper.insert(resultEntity);
    }

    // ==========================================
    // 6. 私有业务逻辑：设备导入 (Device Import)
    // ==========================================

    private void importDeviceRow(Row row, int rowNum, ExcelImportResultVO result) {
        DeviceImportContext ctx = new DeviceImportContext(row);
        List<String> errors = new ArrayList<>();

        validateFormat(ctx, errors);
        validateBusiness(ctx, errors);

        if (!errors.isEmpty()) {
            result.addError(rowNum, String.join("; ", errors));
            return;
        }

        saveDevice(ctx);
        result.incrementSuccess();
    }

    private void validateFormat(DeviceImportContext ctx, List<String> errors) {
        if (!StringUtils.hasText(ctx.getProductNo())) errors.add("产品编号不能为空");
        if (!StringUtils.hasText(ctx.getProductName())) errors.add("产品名称不能为空");

        if (StringUtils.hasText(ctx.getProductNo())) {
            String no = ctx.getProductNo().trim();
            if (no.length() > 50) {
                errors.add("产品编号长度不能超过 50 个字符");
            }
            if (!PRODUCT_NO_PATTERN.matcher(no).matches()) {
                errors.add("产品编号只能包含字母、数字、横线和下划线");
            }
        }

        if (StringUtils.hasText(ctx.getProductionDateStr())) {
            LocalDate date = parseDate(ctx.getProductionDateStr().trim());
            if (date == null) {
                errors.add("生产日期格式错误，应为 yyyy-MM-dd 或 yyyy/MM/dd");
            } else if (date.isAfter(LocalDate.now())) {
                errors.add("生产日期不能是未来日期");
            } else {
                ctx.setProductionDate(date);
            }
        }
    }

    private void validateBusiness(DeviceImportContext ctx, List<String> errors) {
        if (StringUtils.hasText(ctx.getProductNo())) {
            BizDevice existing = deviceMapper.selectByProductNo(ctx.getProductNo().trim());
            if (existing != null) {
                errors.add("产品编号 \"" + ctx.getProductNo() + "\" 已存在");
            }
        }
    }

    private void saveDevice(DeviceImportContext ctx) {
        BizDevice device = new BizDevice();
        device.setProductNo(ctx.getProductNo().trim());
        device.setProductName(ctx.getProductName().trim());
        device.setModel(StringUtils.hasText(ctx.getModel()) ? ctx.getModel().trim() : null);
        device.setManufacturer(StringUtils.hasText(ctx.getManufacturer()) ? ctx.getManufacturer().trim() : null);
        device.setPlaceOrigin(StringUtils.hasText(ctx.getPlaceOrigin()) ? ctx.getPlaceOrigin().trim() : null);
        device.setProductionDate(ctx.getProductionDate());
        deviceMapper.insert(device);
    }

    // ==========================================
    // 7. 通用验证与文件工具方法 (Helpers)
    // ==========================================

    private String validateExcelFile(MultipartFile file) {
        if (file == null || file.isEmpty()) return "文件不能为空";
        String filename = file.getOriginalFilename();
        if (filename == null || !filename.toLowerCase().endsWith(".xlsx")) return "仅支持 .xlsx 格式的 Excel 文件";
        if (file.getSize() > 10 * 1024 * 1024) return "文件大小不能超过 10MB";
        return null;
    }

    private String validateTaskHeader(Row header) {
        if (header == null) return "表头行不存在";
        String[] expectedHeaders = {
                "设备编号", "计量点编号", "送检日期", "测试日期", "温度", "湿度", "tanφ", "r%",
                "相别", "项目类型", "档位", "负载%", "f(%)", "δ(分)", "dU(%)", "Upt", "Uyb"
        };
        return checkHeaders(header, expectedHeaders);
    }

    private String validateDeviceHeader(Row header) {
        if (header == null) return "表头行不存在";
        String[] expectedHeaders = {"产品编号", "产品名称", "型号", "制造商", "产地", "生产日期"};
        return checkHeaders(header, expectedHeaders);
    }

    private String checkHeaders(Row header, String[] expected) {
        for (int i = 0; i < expected.length; i++) {
            String cellValue = getCellStringValue(header.getCell(i));
            if (!expected[i].equals(cellValue.trim())) {
                return "表头第 " + (i + 1) + " 列应为 \"" + expected[i] + "\"，实际为 \"" + cellValue + "\"";
            }
        }
        return null;
    }

    private boolean checkPassAllItems(List<SysTestStandard> thresholdItems, String projectType,
                                      BigDecimal valF, BigDecimal valDelta, BigDecimal valDu,
                                      BigDecimal valUpt, BigDecimal valUyb) {
        boolean isPT = isPtProject(projectType);
        for (SysTestStandard std : thresholdItems) {
            String itemKey = std.getThresholdItem();
            if (itemKey == null) continue;
            BigDecimal actualValue = getActualValueByKey(itemKey.trim().toLowerCase(), valF, valDelta, valDu, valUpt, valUyb);
            if (!isPT && isPtExclusiveItem(itemKey)) continue;
            if (isValueOutOfRange(actualValue, std.getLimitMin(), std.getLimitMax())) return false;
        }
        return true;
    }

    private BigDecimal getActualValueByKey(String key, BigDecimal valF, BigDecimal valDelta,
                                           BigDecimal valDu, BigDecimal valUpt, BigDecimal valUyb) {
        BigDecimal target = switch (key) {
            case "f" -> valF;
            case "delta" -> valDelta;
            case "du" -> valDu;
            case "upt" -> valUpt;
            case "uyb" -> valUyb;
            default -> null;
        };
        return target != null ? target : BigDecimal.ZERO;
    }

    private boolean isPtProject(String projectType) {
        return projectType != null && (projectType.equalsIgnoreCase("PT1") || projectType.equalsIgnoreCase("PT2"));
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

    private LocalDate parseDate(String dateStr) {
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                return LocalDate.parse(dateStr, formatter);
            } catch (DateTimeParseException ignored) { }
        }
        return null;
    }

    private boolean isRowEmpty(Row row) {
        if (row == null) return true;
        for (int i = 0; i < row.getLastCellNum(); i++) {
            Cell cell = row.getCell(i);
            if (cell != null && cell.getCellType() != CellType.BLANK && StringUtils.hasText(getCellStringValue(cell))) {
                return false;
            }
        }
        return true;
    }

    // 静态工具方法（必须为 static，因为被静态内部类调用）
    private static String getCellStringValue(Cell cell) {
        if (cell == null) return "";
        try {
            return switch (cell.getCellType()) {
                case STRING -> cell.getStringCellValue();
                case NUMERIC -> {
                    if (DateUtil.isCellDateFormatted(cell)) {
                        yield new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(cell.getDateCellValue());
                    }
                    yield new java.math.BigDecimal(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
                }
                case BOOLEAN -> String.valueOf(cell.getBooleanCellValue());
                case FORMULA -> cell.getStringCellValue();
                default -> "";
            };
        } catch (Exception e) {
            return "";
        }
    }

    private static BigDecimal getCellDecimalValue(Cell cell) {
        if (cell == null) return null;
        try {
            return switch (cell.getCellType()) {
                case NUMERIC -> BigDecimal.valueOf(cell.getNumericCellValue());
                case STRING -> {
                    String str = cell.getStringCellValue().trim();
                    yield StringUtils.hasText(str) ? new BigDecimal(str) : null;
                }
                default -> null;
            };
        } catch (Exception e) {
            return null;
        }
    }

    // ==========================================
    // 8. 静态内部类 (Inner Contexts)
    // ==========================================

    @Data
    private static class DeviceImportContext {
        private String productNo;
        private String productName;
        private String model;
        private String manufacturer;
        private String placeOrigin;
        private String productionDateStr;
        private LocalDate productionDate;

        public DeviceImportContext(Row row) {
            this.productNo = getCellStringValue(row.getCell(0));
            this.productName = getCellStringValue(row.getCell(1));
            this.model = getCellStringValue(row.getCell(2));
            this.manufacturer = getCellStringValue(row.getCell(3));
            this.placeOrigin = getCellStringValue(row.getCell(4));
            this.productionDateStr = getCellStringValue(row.getCell(5));
        }
    }

    @Data
    private static class TaskRowContext {
        private Row row;
        private int rowNum;

        // 解析出的字段
        private String deviceProductNo;
        private String meterPointId;
        private String deliverDateStr;
        private String testDateStr;
        private BigDecimal temperature;
        private BigDecimal humidity;
        private BigDecimal tanPhi;
        private BigDecimal rPercent;
        private String phase;
        private String projectType;
        private String gearLevel;
        private String loadPercent;
        private BigDecimal valF;
        private BigDecimal valDelta;
        private BigDecimal valDu;
        private BigDecimal valUpt;
        private BigDecimal valUyb;

        // 校验过程中的中间数据
        private BizDevice device;
        private LocalDate deliverDate;
        private LocalDateTime testDate;
        private List<SysTestStandard> thresholdItems;

        public TaskRowContext(Row row, int rowNum) {
            this.row = row;
            this.rowNum = rowNum;
            this.deviceProductNo = getCellStringValue(row.getCell(0));
            this.meterPointId = getCellStringValue(row.getCell(1));
            this.deliverDateStr = getCellStringValue(row.getCell(2));
            this.testDateStr = getCellStringValue(row.getCell(3));
            this.temperature = getCellDecimalValue(row.getCell(4));
            this.humidity = getCellDecimalValue(row.getCell(5));
            this.tanPhi = getCellDecimalValue(row.getCell(6));
            this.rPercent = getCellDecimalValue(row.getCell(7));
            this.phase = getCellStringValue(row.getCell(8));
            this.projectType = getCellStringValue(row.getCell(9));
            this.gearLevel = getCellStringValue(row.getCell(10));
            this.loadPercent = getCellStringValue(row.getCell(11));
            this.valF = getCellDecimalValue(row.getCell(12));
            this.valDelta = getCellDecimalValue(row.getCell(13));
            this.valDu = getCellDecimalValue(row.getCell(14));
            this.valUpt = getCellDecimalValue(row.getCell(15));
            this.valUyb = getCellDecimalValue(row.getCell(16));
        }
    }
}