/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.io.excel.joiner;

import delfos.ERROR_CODES;
import delfos.common.Global;
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

    public static void writeExcelFromMatrix(Map<String, Map<String, Object>> valuesByExperimentAndColumnName, File outputFile) {

        if (!outputFile.getName().endsWith(".xls")) {
            outputFile = new File(outputFile.getPath() + ".xls");
            Global.showWarning("Spreadsheet file renamed to include XLS extension [" + outputFile.getPath() + "]");
        }

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

            Global.showMessageTimestamped("Results saved in " + outputFile.getAbsolutePath());
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

        try {
            //Smartly check if it should be a number instead of text.
            long number = Long.parseLong(s);
            addNumber(sheet, column, row, number);
        } catch (NumberFormatException ex) {

            try {
                double numberD = Double.parseDouble(s);
                addNumber(sheet, column, row, numberD);
            } catch (NumberFormatException ex2) {

                Label label = new Label(column, row, s, defaultFormat);
                sheet.addCell(label);
            }
        }
    }
}
