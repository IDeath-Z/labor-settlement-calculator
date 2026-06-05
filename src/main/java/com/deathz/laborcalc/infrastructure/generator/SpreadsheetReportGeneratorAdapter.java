package com.deathz.laborcalc.infrastructure.generator;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.deathz.laborcalc.domain.enums.ReportFormat;
import com.deathz.laborcalc.domain.model.MonthlyCompetenceDetail;
import com.deathz.laborcalc.domain.model.SettlementResult;
import com.deathz.laborcalc.domain.ports.ReportGeneratorPort;

@Component
public class SpreadsheetReportGeneratorAdapter implements ReportGeneratorPort {

    @Value("${bacen.api.sgs.wage.url}") 
    private String minimumWageUrl;

    @Value("${bacen.api.sgs.selic.url}")
    private String selicUrl;

    // Configs
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final String OFFICIAL_FONT_NAME = "Arial";
    private static final short HEADER_FONT_SIZE = 18;
    private static final short OFFICIAL_FONT_SIZE = 11;
    private static final short FOOTER_FONT_SIZE = 9;
    private static final String SHEET_NAME = "Cálculo Trabalhista";
    private static final String BRASIL_CURRENCY_FORMAT = "R$ #,##0.00";
    private static final String PERCENTAGE_FORMAT = "0.00%";
    private static final String ERROR_GENERATING_REPORT = "Error generating Excel report";

    private static final String NULL_PLACE_HOLDER = "PANDEMIA / RODÍZIO";
    private static final String[] TABLE_HEADERS = {
        "Inicio\nPeríodo", "Fim\nPeríodo", "Dias\nTrabalhados", "Sal. Min.\nVigente (R$)", "% Adicional", "Adicional\nIntegral (R$)", 
        "Adicional\nProporcional (R$)","13º Sal.\nRef. (R$)", "Férias\n+1/3 (R$)", "FGTS\n8% (R$)", "Total\nCompetência (R$)", "% Selic\nAcumulada", "Juros\nSelic (R$)", "Total\nAtualizado (R$)"
    };

    // Headers templates
    private static final String MAIN_HEADER_TEMPLATE = "CÁLCULO DO ADICIONAL DE INSALUBRIDADE - (%d%%)";
    private static final String SUB_HEADER_TEMPLATE = "Período: %s a %s | Base Legal: Art. 192 da CLT | Adicional %d%% do Salário Mínimo Vigente";

    // Anual summary
    private static final String SUMMARY_TITLE = "RESUMO ANUAL";
    private static final String SUMMARY_COL_YEAR = "Ano";
    private static final String SUMMARY_COL_PRINCIPAL = "Total Principal";
    private static final String SUMMARY_COL_ADJUSTED = "Total Atualizado";
    private static final String SUMMARY_GENERAL_TOTAL = "TOTAL DOS PERÍODOS DOS ADICIONAIS PROPORCIONAIS";

    // Footer
    private static final String FOOTER_TITLE = "Dados obtidos de:";
    private static final String FOOTER_WAGE_LABEL = "- Salário Mínimo Vigente: ";
    private static final String FOOTER_SELIC_LABEL = "- Taxa Selic Acumulada: ";

    @Override
    public boolean supports(ReportFormat format) {
        return format == ReportFormat.SPREADSHEET;
    }

    @Override
    public byte[] generate(List<SettlementResult> results) {
        try (Workbook workbook = new XSSFWorkbook(); 
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet(SHEET_NAME);

            CellStyle headerStyle = createHeaderStyle(workbook);
            CellStyle subHeaderStyle = createSubHeaderStyle(workbook);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle centerStyle = createCenterStyle(workbook);
            CellStyle percentStyle = createPercentStyle(workbook);
            CellStyle footerStyle = createFooterStyle(workbook);

            int percentage = extractPercentage(results);
            LocalDate startDate = extractStartDate(results);
            LocalDate endDate = extractEndDate(results);

            buildMainHeader(sheet, percentage, headerStyle);
            buildSubHeader(sheet, startDate, endDate, percentage, subHeaderStyle);
            buildTable(sheet, results, headerStyle, subHeaderStyle, currencyStyle, centerStyle, percentStyle);

            int lastRow = sheet.getLastRowNum();
            
            buildFooter(sheet, lastRow, footerStyle);

            autoSizeColumns(sheet);

            workbook.write(out);
            return out.toByteArray();

        } catch (IOException e) {
            throw new RuntimeException(ERROR_GENERATING_REPORT, e);
        }
    }

    private void buildMainHeader(Sheet sheet, int percentage, CellStyle style) {
        String title = String.format(MAIN_HEADER_TEMPLATE, percentage);
        Row row = sheet.createRow(0);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, TABLE_HEADERS.length - 1));
    }

    private void buildSubHeader(Sheet sheet, LocalDate start, LocalDate end, int percentage, CellStyle style) {
        String title = String.format(SUB_HEADER_TEMPLATE, 
            start.format(DATE_FORMAT), 
            end.format(DATE_FORMAT), 
            percentage);
        Row row = sheet.createRow(1);
        Cell cell = row.createCell(0);
        cell.setCellValue(title);
        cell.setCellStyle(style);
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, TABLE_HEADERS.length - 1));
    }

    private void buildTable(Sheet sheet, List<SettlementResult> results, CellStyle headerStyle, CellStyle subHeaderStyle, CellStyle currencyStyle, CellStyle centerStyle, CellStyle percentStyle) {
        Row columnsRow = sheet.createRow(3);
        columnsRow.setHeightInPoints(45);
        
        for (int i = 0; i < TABLE_HEADERS.length; i++) {
            Cell cell = columnsRow.createCell(i);
            cell.setCellValue(TABLE_HEADERS[i]);
            cell.setCellStyle(subHeaderStyle);
        }

        int rowNum = 4;

        for (SettlementResult yearResult : results) {
            for (MonthlyCompetenceDetail month : yearResult.monthlyDetails()) {
                Row row = sheet.createRow(rowNum++);
                
                Cell startCell = row.createCell(0);
                startCell.setCellValue(month.periodStart().format(DATE_FORMAT));
                startCell.setCellStyle(centerStyle);

                Cell endCell = row.createCell(1);
                endCell.setCellValue(month.periodEnd().format(DATE_FORMAT));
                endCell.setCellStyle(centerStyle);

                if (month.daysWorked() == null) {
                    for (int i = 2; i < TABLE_HEADERS.length; i++) {
                        Cell exceptionCell = row.createCell(i);
                        exceptionCell.setCellStyle(centerStyle);
                        if (i == 2)
                            exceptionCell.setCellValue(NULL_PLACE_HOLDER);
                    }
                    sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 2, TABLE_HEADERS.length - 1));
                    continue; 
                }

                createNumberCell(row, 2, month.daysWorked(), centerStyle);
                createCurrencyCell(row, 3, month.currentMinimumWage().doubleValue(), currencyStyle);
                createNumberCell(row, 4, month.additionalPercentage().doubleValue() / 100, percentStyle);
                createCurrencyCell(row, 5, month.fullAdditionalAmount().doubleValue(), currencyStyle);
                createCurrencyCell(row, 6, month.proportionalAdditionalAmount().doubleValue(), currencyStyle);
                createCurrencyCell(row, 7, month.thirteenthSalaryProportion().doubleValue(), currencyStyle);
                createCurrencyCell(row, 8, month.vacationPlusOneThirdProportion().doubleValue(), currencyStyle);
                createCurrencyCell(row, 9, month.fgtsAmount().doubleValue(), currencyStyle);
                createCurrencyCell(row, 10, month.periodTotal().doubleValue(), currencyStyle);
                createNumberCell(row, 11, month.accumulatedSelicForMonth().doubleValue() / 100, percentStyle);
                createCurrencyCell(row, 12, month.selicAmount().doubleValue(), currencyStyle);
                createCurrencyCell(row, 13, month.totalWithSelic().doubleValue(), currencyStyle);
            }
        }

        buildAnnualSummary(sheet, results, rowNum, headerStyle, subHeaderStyle, centerStyle, currencyStyle);
    }

    private int buildAnnualSummary(Sheet sheet, List<SettlementResult> results, int startRow, CellStyle headerStyle, CellStyle subHeaderStyle, CellStyle centerStyle, CellStyle currencyStyle) {
        int rowNum = startRow + 2;

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(SUMMARY_TITLE);
        titleCell.setCellStyle(headerStyle);
        sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, 4));

        Row headerRow = sheet.createRow(rowNum++);
        
        Cell cellAno = headerRow.createCell(0);
        cellAno.setCellValue(SUMMARY_COL_YEAR);
        cellAno.setCellStyle(subHeaderStyle);

        Cell cellPrin = headerRow.createCell(1);
        cellPrin.setCellValue(SUMMARY_COL_PRINCIPAL);
        cellPrin.setCellStyle(subHeaderStyle);
        headerRow.createCell(2).setCellStyle(subHeaderStyle); 
        sheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum(), headerRow.getRowNum(), 1, 2));

        Cell cellAtu = headerRow.createCell(3);
        cellAtu.setCellValue(SUMMARY_COL_ADJUSTED);
        cellAtu.setCellStyle(subHeaderStyle);
        headerRow.createCell(4).setCellStyle(subHeaderStyle); 
        sheet.addMergedRegion(new CellRangeAddress(headerRow.getRowNum(), headerRow.getRowNum(), 3, 4));

        for (SettlementResult yearResult : results) {
            Row row = sheet.createRow(rowNum++);
            createNumberCell(row, 0, yearResult.year().getValue(), centerStyle);

            createCurrencyCell(row, 1, yearResult.totalPrincipal().doubleValue(), currencyStyle);
            row.createCell(2).setCellStyle(currencyStyle); 
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 1, 2));

            createCurrencyCell(row, 3, yearResult.totalAdjustedValue().doubleValue(), currencyStyle);
            row.createCell(4).setCellStyle(currencyStyle); 
            sheet.addMergedRegion(new CellRangeAddress(row.getRowNum(), row.getRowNum(), 3, 4));
        }
        
        double totalGeralAtualizado = results.stream()
            .mapToDouble(r -> r.totalAdjustedValue().doubleValue())
            .sum();

        rowNum++;

        Row totalRow = sheet.createRow(rowNum++);
        Cell totalLabel = totalRow.createCell(0);
        totalLabel.setCellValue(SUMMARY_GENERAL_TOTAL);
        totalLabel.setCellStyle(subHeaderStyle); 
        sheet.addMergedRegion(new CellRangeAddress(totalRow.getRowNum(), totalRow.getRowNum(), 0, 2));

        createCurrencyCell(totalRow, 3, totalGeralAtualizado, currencyStyle);
        totalRow.createCell(4).setCellStyle(currencyStyle);
        sheet.addMergedRegion(new CellRangeAddress(totalRow.getRowNum(), totalRow.getRowNum(), 3, 4)); 
        
        return rowNum;
    }

    private void buildFooter(Sheet sheet, int startRow, CellStyle footerStyle) {
        int rowNum = startRow + 2; 
        int lastCol = TABLE_HEADERS.length - 1; 

        Row titleRow = sheet.createRow(rowNum++);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue(FOOTER_TITLE);
        titleCell.setCellStyle(footerStyle);
        sheet.addMergedRegion(new CellRangeAddress(titleRow.getRowNum(), titleRow.getRowNum(), 0, lastCol));
        
        Row wageRow = sheet.createRow(rowNum++);
        Cell wageCell = wageRow.createCell(0);
        wageCell.setCellValue(FOOTER_WAGE_LABEL + minimumWageUrl);
        wageCell.setCellStyle(footerStyle);
        sheet.addMergedRegion(new CellRangeAddress(wageRow.getRowNum(), wageRow.getRowNum(), 0, lastCol));

        Row selicRow = sheet.createRow(rowNum++);
        Cell selicCell = selicRow.createCell(0);
        selicCell.setCellValue(FOOTER_SELIC_LABEL + selicUrl);
        selicCell.setCellStyle(footerStyle);
        sheet.addMergedRegion(new CellRangeAddress(selicRow.getRowNum(), selicRow.getRowNum(), 0, lastCol));
    }

    private void autoSizeColumns(Sheet sheet) {
        for (int i = 0; i < TABLE_HEADERS.length; i++) {
            sheet.autoSizeColumn(i);
            int currentWidth = sheet.getColumnWidth(i);
            sheet.setColumnWidth(i, currentWidth + 1024); 
        }
    }

    private int extractPercentage(List<SettlementResult> results) {
        return results.stream()
            .flatMap(year -> year.monthlyDetails().stream())
            .filter(month -> month.additionalPercentage() != null)
            .findFirst()
            .map(month -> month.additionalPercentage().intValue())
            .orElse(0);
    }

    private LocalDate extractStartDate(List<SettlementResult> results) {
        return results.get(0).monthlyDetails().get(0).periodStart();
    }

    private LocalDate extractEndDate(List<SettlementResult> results) {
        SettlementResult lastYear = results.get(results.size() - 1);
        return lastYear.monthlyDetails().get(lastYear.monthlyDetails().size() - 1).periodEnd();
    }

    private void createCurrencyCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }
    
    private void createNumberCell(Row row, int column, double value, CellStyle style) {
        Cell cell = row.createCell(column);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private CellStyle createBaseStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        return style;
    }

    private Font createFont(Workbook workbook, short size, boolean bold, IndexedColors color) {
        Font font = workbook.createFont();
        font.setFontName(OFFICIAL_FONT_NAME);
        font.setFontHeightInPoints(size);
        font.setBold(bold);
        if (color != null) {
            font.setColor(color.getIndex());
        }
        return font;
    }

    private CellStyle createHeaderStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setFillForegroundColor(IndexedColors.DARK_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(createFont(workbook, HEADER_FONT_SIZE, true, IndexedColors.WHITE));
        return style;
    }

    private CellStyle createSubHeaderStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setWrapText(true);
        style.setFillForegroundColor(IndexedColors.LIGHT_BLUE.getIndex());
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setFont(createFont(workbook, OFFICIAL_FONT_SIZE, true, IndexedColors.WHITE));
        return style;
    }

    private CellStyle createCurrencyStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat(BRASIL_CURRENCY_FORMAT));
        style.setFont(createFont(workbook, OFFICIAL_FONT_SIZE, false, null));
        return style;
    }
    
    private CellStyle createPercentStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setDataFormat(workbook.createDataFormat().getFormat(PERCENTAGE_FORMAT));
        style.setFont(createFont(workbook, OFFICIAL_FONT_SIZE, false, null));
        return style;
    }
    
    private CellStyle createCenterStyle(Workbook workbook) {
        CellStyle style = createBaseStyle(workbook);
        style.setFont(createFont(workbook, OFFICIAL_FONT_SIZE, false, null));
        return style;
    }

    private CellStyle createFooterStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setAlignment(HorizontalAlignment.LEFT);
        
        Font font = workbook.createFont();
        font.setFontName(OFFICIAL_FONT_NAME);
        font.setFontHeightInPoints(FOOTER_FONT_SIZE);
        font.setItalic(true);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        
        return style;
    }
}