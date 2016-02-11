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
import static delfos.io.excel.casestudy.CaseStudyExcel.AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME;
import static delfos.io.excel.casestudy.CaseStudyExcel.ALL_EXPERIMENTS_SHEET_NAME;
import static delfos.io.excel.casestudy.CaseStudyExcel.EXPERIMENT_NAME_COLUMN_NAME;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import jxl.Cell;
import jxl.CellType;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

/**
 *
 * @version 24-jun-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class AggregateResultsExcels {

    public AggregateResultsExcels() {
    }

    public Collection<File> extractResultsFiles(File file) {

        Collection<File> returnFiles = new ArrayList<>();

        if (!file.exists()) {
            Global.showWarning("File " + file.getAbsolutePath() + " not exists.");
            return returnFiles;
        }

        if (file.isDirectory()) {
            //Es un directorio, tengo que buscar hijos recursivamente.
            File[] listFiles = file.listFiles((File pathname) -> {
                if (pathname.isDirectory()) {
                    return true;
                } else {
                    return AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME.equals(pathname.getName());
                }
            });
            for (File child : listFiles) {
                Collection<File> extractResultsFiles = extractResultsFiles(child);
                returnFiles.addAll(extractResultsFiles);
            }
        } else {
            if (AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME.equals(file.getName())) {
                returnFiles.add(file);
                return returnFiles;
            } else {
                Global.showWarning("File " + file.getAbsolutePath() + " is not a directory.");
                return returnFiles;
            }
        }
        return returnFiles;
    }

    public void join(Collection<File> files) {
        Map<String, Map<String, Object>> values = new TreeMap<>();

        int i = 1;
        for (File file : files) {
            try {
                System.out.println("(" + (i++) + " of " + files.size() + "): " + "Reading file " + file);
                Map<String, Object> valuesThisFile = extractMapFromFile(file);
                values.put(valuesThisFile.get(EXPERIMENT_NAME_COLUMN_NAME).toString(), valuesThisFile);
            } catch (Throwable ex) {
                System.out.println("ERROR AT --> Reading file " + file);
                ERROR_CODES.CANNOT_READ_CASE_STUDY_EXCEL.exit(ex);
            }
        }
        writeFinalExcel(values);
    }

    private Map<String, Object> extractMapFromFile(File inputFile) {
        try {
            Map<String, Object> valuesByColumnName = new TreeMap<>();
            Workbook workbook = Workbook.getWorkbook(inputFile);

            Sheet aggregateResults = workbook.getSheet(ALL_EXPERIMENTS_SHEET_NAME);
//            GroupCaseStudyExcel.extractConfiguredDatasetLoaderNameFromAggregateResultsSheet(aggregateResults,);

            for (int columnIndex = 0; columnIndex < aggregateResults.getColumns(); columnIndex++) {
                String columnName = aggregateResults.getCell(columnIndex, 0).getContents();

                if (columnName.contains("Precision@")) {
                    int listSize = new Integer(columnName.replace("Precision@", ""));

                    NumberFormat numberFormat = new DecimalFormat("000");
                    String longNumber = numberFormat.format(listSize);
                    columnName = "Precision@" + longNumber;
                }

                CellType type = aggregateResults.getCell(columnIndex, 1).getType();
                Cell cell = aggregateResults.getCell(columnIndex, 1);

                String cellContent = cell.getContents();

                boolean correct = false;

                if (type == CellType.NUMBER) {
                    NumberCell numberRecord = (NumberCell) aggregateResults.getCell(columnIndex, 1);

                    Double measureValue = numberRecord.getValue();
                    valuesByColumnName.put(columnName, measureValue);
                    correct = true;
                }

                if (!correct && type == CellType.LABEL) {
                    valuesByColumnName.put(columnName, cellContent);
                    correct = true;
                }

                if (!correct) {
                    throw new IllegalStateException("Cannot parse cell of type " + type);
                }
            }

            return valuesByColumnName;
        } catch (IOException | BiffException ex) {
            ERROR_CODES.CANNOT_READ_CASE_STUDY_EXCEL.exit(ex);
        }

        throw new IllegalStateException("Never reaches this point");

    }

    private void writeFinalExcel(Map<String, Map<String, Object>> values) {

        AggregateResultsMatrixExcelWriter.writeExcelFromMatrix(values, new File("AggregateExperimentResults.xls"));
    }

}
