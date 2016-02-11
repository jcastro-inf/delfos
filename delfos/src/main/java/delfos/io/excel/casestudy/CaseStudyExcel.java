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
package delfos.io.excel.casestudy;

import delfos.ERROR_CODES;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.results.evaluationmeasures.PRSpace;
import delfos.rs.RecommenderSystem;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import jxl.Cell;
import jxl.CellType;
import jxl.CellView;
import jxl.NumberCell;
import jxl.Sheet;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.UnderlineStyle;
import jxl.read.biff.BiffException;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.Number;
import jxl.write.NumberFormat;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import jxl.write.biff.RowsExceededException;

/**
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución
 * de un caso de uso concreto.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (3-Mayo-2013)
 */
public class CaseStudyExcel {

    public static String RESULT_EXTENSION = "xml";

    public static final String ALL_EXPERIMENTS_SHEET_NAME = "AllExperiments";

    public static final String AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME = "aggregateResults.xls";
    public static final String EXPERIMENT_NAME_COLUMN_NAME = "ExperimentName";
    public static final String DATASET_LOADER_COLUMN_NAME = "DatasetLoader";

    public static final String AGGREGATE_RESULTS_SHEET_NAME = "AggregateResults";

    private static WritableCellFormat titleFormat;
    private static WritableCellFormat defaultFormat;
    private static WritableCellFormat decimalFormat;
    private static WritableCellFormat integerFormat;
    private static final int titleCellWidth = 3 - 1;

    public static void aggregateExcels(File[] inputFiles, File outputFile) throws WriteException {

        Map<String, Integer> indexColumn = new TreeMap<>();

        Map<String, Map<String, Double>> valores = new TreeMap<>();

        for (File inputFile : inputFiles) {
            try {
                TreeMap<String, Double> valoresDeMetricas = new TreeMap<>();
                Workbook workbook = Workbook.getWorkbook(inputFile);

                Sheet aggregateResults = workbook.getSheet(AGGREGATE_RESULTS_SHEET_NAME);

                for (int columnIndex = 0; columnIndex < aggregateResults.getColumns(); columnIndex++) {
                    String measureName = aggregateResults.getCell(columnIndex, 0).getContents();

                    if (!indexColumn.containsKey(measureName)) {
                        int index = indexColumn.size();
                        indexColumn.put(measureName, index);
                    }
                    CellType type = aggregateResults.getCell(columnIndex, 1).getType();
                    Cell cell = aggregateResults.getCell(columnIndex, 1);

                    String measureValueString = cell.getContents();
                    if (type == CellType.NUMBER) {
                        NumberCell numberRecord = (NumberCell) aggregateResults.getCell(columnIndex, 1);

                        Double measureValue = numberRecord.getValue();
                        valoresDeMetricas.put(measureName, measureValue);
                    } else {
                        throw new IllegalStateException("CAnnot recognize cell type");
                    }
                }

                valores.put(inputFile.getName(), valoresDeMetricas);
            } catch (IOException | BiffException ex) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
            }
        }

        writeExcelFromMatrix(outputFile, indexColumn, valores);
    }

    public static void writeExcelFromMatrix(File outputFile, Map<String, Integer> indexColumn, Map<String, Map<String, Double>> valores) {
        //Escribo resultado.
        try {
            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook = Workbook.createWorkbook(outputFile, wbSettings);

            if (workbook == null) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(new FileNotFoundException("Cannot access file " + outputFile.getAbsolutePath() + "."));
                return;
            }

            WritableSheet allExperiments = workbook.createSheet(ALL_EXPERIMENTS_SHEET_NAME, 0);
            createLabel(allExperiments);

            //Seet the content.
            int row = 0;

            //Titulos.
            {
                addTitleText(allExperiments, row, 0, EXPERIMENT_NAME_COLUMN_NAME);
                for (String metrica : indexColumn.keySet()) {
                    int column = indexColumn.get(metrica);
                    column = column + 1;
                    addTitleText(allExperiments, column, row, metrica);
                }
                row++;
            }

            {
                for (String experimentName : valores.keySet()) {

                    addText(allExperiments, 0, row, experimentName);

                    Map<String, Double> experimentResults = valores.get(experimentName);
                    for (String metricName : experimentResults.keySet()) {
                        double metricValue = valores.get(experimentName).get(metricName);
                        int column = indexColumn.get(metricName) + 1;
                        addNumber(allExperiments, column, row, metricValue);
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

    private CaseStudyExcel() {
    }

    public synchronized static void saveCaseResults(CaseStudy caseStudy, File file) {

        if (!caseStudy.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        try {
            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook = null;

            try {
                workbook = Workbook.createWorkbook(file, wbSettings);
            } catch (IOException ex) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(new FileNotFoundException("Cannot access file " + file.getAbsolutePath() + "."));
                return;
            }

            WritableSheet caseDefinitionSheet = workbook.createSheet(CASE_DEFINITION_SHEET_NAME, 0);
            createLabel(caseDefinitionSheet);
            createCaseDefinitionSheet(caseStudy, caseDefinitionSheet);
            autoSizeColumns(caseDefinitionSheet);

            WritableSheet executionsSheet = workbook.createSheet("Executions", 1);
            createLabel(executionsSheet);
            createExecutionsSheet(caseStudy, executionsSheet);
            autoSizeColumns(executionsSheet);

            WritableSheet aggregateResultsSheet = workbook.createSheet(AGGREGATE_RESULTS_SHEET_NAME, 2);
            createLabel(aggregateResultsSheet);
            createAggregateResultsSheet(caseStudy, aggregateResultsSheet);
            autoSizeColumns(aggregateResultsSheet);

            workbook.write();
            workbook.close();

        } catch (WriteException | IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
    }
    public static final String CASE_DEFINITION_SHEET_NAME = "CaseDefinition";

    private static void createCaseDefinitionSheet(CaseStudy caseStudy, WritableSheet sheet) throws WriteException {

        int column = 0;
        int row = 0;

        //Create table for GRS
        {
            RecommenderSystem recommenderSystem = caseStudy.getRecommenderSystem();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, " Recommender System");
            row++;
            addText(sheet, column, row, recommenderSystem.getName());
            row++;
            for (Parameter parameter : recommenderSystem.getParameters()) {
                Object parameterValue = recommenderSystem.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for DatasetLoader
        {
            DatasetLoader<? extends Rating> datasetLoader = caseStudy.getDatasetLoader();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, "Dataset Loader");
            row++;
            addText(sheet, column, row, datasetLoader.getName());
            row++;
            for (Parameter parameter : datasetLoader.getParameters()) {
                Object parameterValue = datasetLoader.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for ValidationTechnique
        {
            ValidationTechnique validationTechnique = caseStudy.getValidationTechnique();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, " Validation Technique");
            row++;
            addText(sheet, column, row, validationTechnique.getName());
            row++;
            for (Parameter parameter : validationTechnique.getParameters()) {
                Object parameterValue = validationTechnique.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for PredictionProtocol
        {
            PredictionProtocol groupPredictionProtocol = caseStudy.getPredictionProtocol();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, " Prediction Protocol");
            row++;
            addText(sheet, column, row, groupPredictionProtocol.getName());
            row++;
            for (Parameter parameter : groupPredictionProtocol.getParameters()) {
                Object parameterValue = groupPredictionProtocol.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for RelevanceCriteria
        {
            RelevanceCriteria relevanceCriteria = caseStudy.getRelevanceCriteria();
            sheet.mergeCells(column + 0, row, column + titleCellWidth, row);
            addTitleText(sheet, column, row, "Relevance Criteria threshold >= " + relevanceCriteria.getThreshold().doubleValue());
            row++;
        }
    }

    final static int parameterNameOffset = 1;
    final static int parameterTypeOffset = 2;
    final static int parameterValueOffset = 3;

    /**
     *
     * @param parameter
     * @param parameterValue
     * @param sheet
     * @param column
     * @param row
     * @return Devuelve la fila por la que se debe seguir escribiendo.
     * @throws WriteException
     */
    private static int writeParameterAndValue(Parameter parameter, Object parameterValue, WritableSheet sheet, int column, int row) throws WriteException {

        //First write the parameter line
        addText(sheet, column, row, "Parameter");
        addText(sheet, column + parameterNameOffset, row, parameter.getName());
        addText(sheet, column + parameterTypeOffset, row, parameter.getRestriction().getName());

        if (parameterValue instanceof ParameterOwner) {
            ParameterOwner parameterOwner = (ParameterOwner) parameterValue;
            addText(sheet, column + parameterValueOffset, row, parameterOwner.getName());
        } else {
            if (parameterValue instanceof java.lang.Number) {

                if ((parameterValue instanceof java.lang.Integer) || (parameterValue instanceof java.lang.Long)) {
                    java.lang.Long number = ((java.lang.Number) parameterValue).longValue();
                    addNumber(sheet, column + parameterValueOffset, row, number);
                } else {
                    java.lang.Number number = (java.lang.Number) parameterValue;
                    addNumber(sheet, column + parameterValueOffset, row, number.doubleValue());
                }
            } else {
                addText(sheet, column + parameterValueOffset, row, parameterValue.toString());
            }
        }

        //Then, if it is a parameter owner, write its children parameters.
        if (parameterValue instanceof ParameterOwner) {
            column++;
            ParameterOwner parameterOwner = (ParameterOwner) parameterValue;
            for (Parameter innerParameter : parameterOwner.getParameters()) {
                row++;
                Object innerParameterValue = parameterOwner.getParameterValue(innerParameter);
                row = writeParameterAndValue(innerParameter, innerParameterValue, sheet, column, row);
            }
            column--;
        }

        return row;
    }

    final static int maxListSize = 20;

    private static void createExecutionsSheet(CaseStudy caseStudy, WritableSheet sheet) throws WriteException {

        int row = 0;

        final int numExecutions = caseStudy.getNumExecutions();
        final int numSplits = caseStudy.getValidationTechnique().getNumberOfSplits();

        final int vueltaColumn = 0;
        final int executionColumn = 1;
        final int splitColumn = 2;
        /**
         * Numero de recomendaciones que se consideran para la precisión.
         */

        //Escribo los titulos de las columnas.
        addTitleText(sheet, vueltaColumn, row, "#");
        addTitleText(sheet, executionColumn, row, "Execution");
        addTitleText(sheet, splitColumn, row, "Split");

        PRSpace pRSpace = null;
        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, EvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = splitColumn + 1;
            for (EvaluationMeasure evaluationMeasure : caseStudy.getEvaluationMeasures()) {
                indexOfMeasures.put(evaluationMeasure.getName(), i++);

                metricsByName.put(evaluationMeasure.getName(), evaluationMeasure);

                if (evaluationMeasure instanceof PRSpace) {
                    pRSpace = (PRSpace) evaluationMeasure;
                    for (int listSize = 1; listSize <= maxListSize; listSize++) {
                        indexOfMeasures.put("Precision@" + listSize, i++);
                    }
                }
            }
            indexOfMeasures.put("BuildTime", i++);
            indexOfMeasures.put("RecommendationTime", i++);
        }

        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();
            addTitleText(sheet, column, row, name);
        }

        row++;

        int vuelta = 1;
        for (int thisExecution = 0; thisExecution < numExecutions; thisExecution++) {
            for (int thisSplit = 0; thisSplit < numSplits; thisSplit++) {

                //Escribo la linea de esta ejecución concreta
                addNumber(sheet, vueltaColumn, row, vuelta);
                addNumber(sheet, executionColumn, row, thisExecution + 1);
                addNumber(sheet, splitColumn, row, thisSplit + 1);

                //Ahora los valores de cada metrica.
                for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
                    String name = entry.getKey();
                    int column = entry.getValue();

                    final double value;
                    if (name.equals("BuildTime")) {
                        value = caseStudy.getBuildTime(thisExecution, thisSplit);
                    } else {
                        if (name.equals("RecommendationTime")) {
                            value = caseStudy.getRecommendationTime(thisExecution, thisSplit);
                        } else {
                            if (name.startsWith("Precision@")) {
                                MeasureResult measureResult = caseStudy.getMeasureResult(pRSpace, thisExecution, thisSplit);
                                Map<String, Double> detailedResult = (Map<String, Double>) measureResult.getDetailedResult();

                                Double get = detailedResult.get(name);

                                if (get == null) {
                                    //No se llegan a recomendar tantos productos.
                                    value = Double.NaN;
                                } else {
                                    value = get;
                                }
                            } else {
                                //Es una medida cualquiera.
                                EvaluationMeasure groupEvaluationMeasure = metricsByName.get(name);
                                value = caseStudy.getMeasureResult(groupEvaluationMeasure, thisExecution, thisSplit).getValue();
                            }
                        }
                    }

                    if (!Double.isNaN(value)) {
                        double decimalTrimmedValue = NumberRounder.round(value, 5);
                        addNumber(sheet, column, row, decimalTrimmedValue);
                    } else {
                        addText(sheet, column, row, "");
                    }
                }

                vuelta++;
                row++;

            }
        }

    }

    private static void createAggregateResultsSheet(CaseStudy caseStudy, WritableSheet sheet) throws WriteException {
        int row = 0;

        PRSpace pRSpaces = null;
        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, EvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = 0;
            for (EvaluationMeasure groupEvaluationMeasure : caseStudy.getEvaluationMeasures()) {
                indexOfMeasures.put(groupEvaluationMeasure.getName(), i++);

                metricsByName.put(groupEvaluationMeasure.getName(), groupEvaluationMeasure);

                if (groupEvaluationMeasure instanceof PRSpace) {
                    pRSpaces = (PRSpace) groupEvaluationMeasure;
                    for (int listSize = 1; listSize <= maxListSize; listSize++) {
                        indexOfMeasures.put("Precision@" + listSize, i++);
                    }
                }
            }
            indexOfMeasures.put("BuildTime", i++);
            indexOfMeasures.put("RecommendationTime", i++);
        }

        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();
            addTitleText(sheet, column, row, name);
        }

        row++;

        //Ahora los valores agregados de cada metrica.
        for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
            String name = entry.getKey();
            int column = entry.getValue();

            final double value;

            if (name.equals("BuildTime")) {
                value = caseStudy.getAggregateBuildTime();
            } else {
                if (name.equals("RecommendationTime")) {
                    value = caseStudy.getAggregateRecommendationTime();
                } else {
                    if (name.startsWith("Precision@")) {
                        MeasureResult measureResult = caseStudy.getAggregateMeasureResult(pRSpaces);
                        Map<String, Double> detailedResult = (Map<String, Double>) measureResult.getDetailedResult();

                        Double get = detailedResult.get(name);

                        if (get == null) {
                            //No se llegan a recomendar tantos productos.
                            value = Double.NaN;
                        } else {
                            value = get;
                        }
                    } else {
                        //Es una medida cualquiera.
                        EvaluationMeasure groupEvaluationMeasure = metricsByName.get(name);
                        value = caseStudy.getAggregateMeasureResult(groupEvaluationMeasure).getValue();
                    }
                }

            }

            if (!Double.isNaN(value)) {
                double decimalTrimmedValue = NumberRounder.round(value, 5);
                addNumber(sheet, column, row, decimalTrimmedValue);
            } else {
                addText(sheet, column, row, "");
            }
            double decimalTrimmedValue = NumberRounder.round(value, 5);

            addNumber(sheet, column, row, decimalTrimmedValue);
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

        Number number = new Number(column, row, rounded, decimalFormat);
        sheet.addCell(number);
    }

    private static void addNumber(WritableSheet sheet, int column, int row,
            long integer) throws WriteException, RowsExceededException {
        Number number = new Number(column, row, integer, integerFormat);
        sheet.addCell(number);
    }

    private static void addText(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, defaultFormat);
        sheet.addCell(label);
    }
}
