package delfos.io.excel.joiner;

import delfos.ERROR_CODES;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.io.excel.casestudy.CaseStudyExcel;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 *
 * @version 24-jun-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class AggregateResultsMatrixExcelWriter {

    private static WritableCellFormat titleFormat;
    private static WritableCellFormat defaultFormat;
    private static WritableCellFormat decimalFormat;
    private static WritableCellFormat integerFormat;
    private static final int titleCellWidth = 3 - 1;

    public static void writeExcelFromMatrix(File outputFile, Map<String, Map<String, Object>> valuesByExperimentAndColumnName) {

        Map<String, Integer> indexColumn = new TreeMap<>();
        indexColumn.put(CaseStudyExcel.EXPERIMENT_NAME_COLUMN_NAME, 0);

        for (String key : valuesByExperimentAndColumnName.keySet()) {
            for (String columnName : valuesByExperimentAndColumnName.get(key).keySet()) {

                if (!indexColumn.containsKey(columnName)) {
                    indexColumn.put(columnName, indexColumn.size());
                }
            }
        }

        //Escribo resultado.
        try {
            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook = Workbook.createWorkbook(outputFile, wbSettings);

            if (workbook == null) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(new FileNotFoundException("Cannot access file " + outputFile.getAbsolutePath() + "."));
                return;
            }

            WritableSheet allExperiments = workbook.createSheet(CaseStudyExcel.ALL_EXPERIMENTS_SHEET_NAME, 0);
            createLabel(allExperiments);

            //Seet the content.
            int row = 0;

            //Titulos.
            {
                addTitleText(allExperiments, row, 0, CaseStudyExcel.EXPERIMENT_NAME_COLUMN_NAME);
                for (String metrica : indexColumn.keySet()) {
                    int column = indexColumn.get(metrica);
                    column = column + 1;
                    addTitleText(allExperiments, column, row, metrica);
                }
                row++;
            }

            {
                for (String experimentName : valuesByExperimentAndColumnName.keySet()) {

                    addText(allExperiments, 0, row, experimentName);

                    Map<String, Object> experimentResults = valuesByExperimentAndColumnName.get(experimentName);
                    for (String metricName : experimentResults.keySet()) {

                        Object value = valuesByExperimentAndColumnName.get(experimentName).get(metricName);

                        if (value instanceof Number) {
                            Number number = (Number) value;
                            double metricValue = number.doubleValue();
                            int column = indexColumn.get(metricName) + 1;
                            addNumber(allExperiments, column, row, metricValue);
                        } else {
                            int column = indexColumn.get(metricName) + 1;
                            if (value != null) {
                                addText(allExperiments, column, row, value.toString());
                            }
                        }
                    }
                    row++;
                }
            }

            autoSizeColumns(allExperiments);
            workbook.write();
            workbook.close();

        } catch (WriteException | IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
    }

    private static void createLabel(WritableSheet sheet)
            throws WriteException {

        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        // Define the cell format
        defaultFormat = new WritableCellFormat(times10pt);

        //Column width control
        defaultFormat.setWrap(false);

        decimalFormat = new WritableCellFormat(times10pt, new NumberFormat("0.00000"));
        integerFormat = new WritableCellFormat(times10pt, new NumberFormat("0"));

        // create create a bold font with unterlines
        WritableFont times14ptBoldUnderline = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD, false,
                UnderlineStyle.SINGLE);
        titleFormat = new WritableCellFormat(times14ptBoldUnderline);
        titleFormat.setAlignment(Alignment.CENTRE);

        //Column width control
        titleFormat.setWrap(false);

    }

    public static void autoSizeColumns(WritableSheet sheet) {
        for (int x = 0; x < 40; x++) {
            CellView cell = sheet.getColumnView(x);
            cell.setAutosize(true);
            sheet.setColumnView(x, cell);
        }
    }

    private static void createContent(WritableSheet sheet) throws WriteException,
            RowsExceededException {
        // Write a few number
        for (int i = 1; i < 10; i++) {
            // First column
            addNumber(sheet, 0, i, i + 10);
            // Second column
            addNumber(sheet, 1, i, i * i);
        }
        // Lets calculate the sum of it
        StringBuffer buf = new StringBuffer();
        buf.append("SUM(A2:A10)");
        Formula f = new Formula(0, 10, buf.toString());
        sheet.addCell(f);
        buf = new StringBuffer();
        buf.append("SUM(B2:B10)");
        f = new Formula(1, 10, buf.toString());
        sheet.addCell(f);

        // now a bit of text
        for (int i = 12; i < 20; i++) {
            // First column
            addText(sheet, 0, i, "Boring text " + i);
            // Second column
            addText(sheet, 1, i, "Another text");
        }
    }

    private static void addTitleText(WritableSheet sheet, int column, int row, String s)
            throws RowsExceededException, WriteException {
        Label label;
        label = new Label(column, row, s, titleFormat);
        sheet.addCell(label);
    }

    private static void addNumber(WritableSheet sheet, int column, int row,
            double value) throws WriteException, RowsExceededException {
        double rounded = NumberRounder.round(value, 8);

        jxl.write.Number number = new jxl.write.Number(column, row, rounded, decimalFormat);
        sheet.addCell(number);
    }

    private static void addNumber(WritableSheet sheet, int column, int row,
            long integer) throws WriteException, RowsExceededException {
        jxl.write.Number number = new jxl.write.Number(column, row, integer, integerFormat);
        sheet.addCell(number);
    }

    private static void addText(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, defaultFormat);
        sheet.addCell(label);
    }
}
