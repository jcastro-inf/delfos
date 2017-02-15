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
import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.chain.CaseStudyResultMatrix;
import delfos.common.parameters.chain.ParameterChain;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.CaseStudyResults;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel.Combination;
import static delfos.group.io.excel.casestudy.GroupCaseStudyExcel.obtainDifferentParameterInCollumn;
import delfos.io.excel.parameterowner.ParameterOwnerExcel;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución de un caso de uso concreto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    private static final int TITLE_CELL_WIDTH = 3 - 1;

    public static final int EXPERIMENT_NAME_COLUMN = 0;
    public static final int DATASET_LOADER_ALIAS_COLUMN = 1;
    public static final int _EVALUATION_MEASURES_OFFSET = 2;

    public static final String NAN_CELL_STRING = "NaN";

    static {
        try {
            initTitleFormat();
            initIntegerFormat();
            initDecimalFormat();
            initDefaultFormat();
        } catch (WriteException ex) {
            Logger.getLogger(CaseStudyExcel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private static void initTitleFormat() throws WriteException {
        if (titleFormat == null) {
            // create create a bold font with unterlines
            WritableFont times14ptBoldUnderline = new WritableFont(WritableFont.TIMES, 14, WritableFont.BOLD, false,
                    UnderlineStyle.SINGLE);

            titleFormat = new WritableCellFormat(times14ptBoldUnderline);
            titleFormat.setAlignment(Alignment.CENTRE);

            //Column width control
            titleFormat.setWrap(false);
        }
    }

    private static void initIntegerFormat() {

        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        integerFormat = new WritableCellFormat(times10pt, new NumberFormat("0"));
    }

    private static void initDecimalFormat() {

        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        decimalFormat = new WritableCellFormat(times10pt, new NumberFormat("0.00000"));
    }

    private static void initDefaultFormat() throws WriteException {

        // Lets create a times font
        WritableFont times10pt = new WritableFont(WritableFont.TIMES, 10);
        // Define the cell format
        defaultFormat = new WritableCellFormat(times10pt);

        //Column width control
        defaultFormat.setWrap(false);

    }

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
                    } else if (type == CellType.EMPTY) {
                        valoresDeMetricas.put(measureName, 0.0);
                    } else {
                        throw new IllegalStateException("Cannot recognize cell type");
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
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
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
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
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
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
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
            PredictionProtocol PredictionProtocol = caseStudy.getPredictionProtocol();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
            addTitleText(sheet, column, row, " Prediction Protocol");
            row++;
            addText(sheet, column, row, PredictionProtocol.getName());
            row++;
            for (Parameter parameter : PredictionProtocol.getParameters()) {
                Object parameterValue = PredictionProtocol.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for RelevanceCriteria
        {
            RelevanceCriteria relevanceCriteria = caseStudy.getRelevanceCriteria();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
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
        } else if (parameterValue instanceof java.lang.Number) {

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

    final static int MAX_LIST_SIZE = 20;

    private static <RecommendationModel extends Object, RatingType extends Rating> void createExecutionsSheet(
            CaseStudy<RecommendationModel, RatingType> caseStudy,
            WritableSheet sheet)
            throws WriteException {

        int row = 0;

        final int numExecutions = caseStudy.getNumExecutions();
        final int numSplits = caseStudy.getValidationTechnique().getNumberOfSplits();

        final int vueltaColumn = 0;
        final int executionColumn = 1;
        final int splitColumn = 2;

        //Escribo los titulos de las columnas.
        addTitleText(sheet, vueltaColumn, row, "#");
        addTitleText(sheet, executionColumn, row, "Execution");
        addTitleText(sheet, splitColumn, row, "Split");

        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, EvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = splitColumn + 1;
            for (EvaluationMeasure evaluationMeasure : caseStudy.getEvaluationMeasures()) {
                indexOfMeasures.put(evaluationMeasure.getName(), i++);

                metricsByName.put(evaluationMeasure.getName(), evaluationMeasure);

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

                    //Es una medida cualquiera.
                    if (metricsByName.containsKey(name)) {
                        EvaluationMeasure EvaluationMeasure = metricsByName.get(name);
                        value = caseStudy.getMeasureResult(EvaluationMeasure, thisExecution, thisSplit).getValue();
                    } else {
                        value = Double.NaN;
                    }

                    if (Double.isNaN(value)) {
                        addText(sheet, column, row, "");
                    } else {
                        double decimalTrimmedValue = NumberRounder.round(value);
                        addNumber(sheet, column, row, decimalTrimmedValue);
                    }
                }

                vuelta++;
                row++;

            }
        }

    }

    private static <RecommendationModel extends Object, RatingType extends Rating> void
            createAggregateResultsSheet(CaseStudy<RecommendationModel, RatingType> caseStudy, WritableSheet sheet)
            throws WriteException {
        int row = 0;

        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, EvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = 0;
            for (EvaluationMeasure EvaluationMeasure : caseStudy.getEvaluationMeasures()) {
                indexOfMeasures.put(EvaluationMeasure.getName(), i++);

                metricsByName.put(EvaluationMeasure.getName(), EvaluationMeasure);
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

            //Es una medida cualquiera.
            EvaluationMeasure EvaluationMeasure = metricsByName.get(name);

            if (EvaluationMeasure == null) {
                continue;
            }

            value = caseStudy.getAggregateMeasureResult(EvaluationMeasure).getValue();

            if (!Double.isNaN(value)) {
                double decimalTrimmedValue = NumberRounder.round(value);
                addNumber(sheet, column, row, decimalTrimmedValue);
            } else {
                addText(sheet, column, row, "");
            }
            double decimalTrimmedValue = NumberRounder.round(value);

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
        double rounded = NumberRounder.round(value);

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

    public static <RecommendationModel extends Object, RatingType extends Rating>
            void writeGeneralSheet(List<CaseStudyResults<RecommendationModel, RatingType>> caseStudyResultss,
                    List<String> dataValidationParametersOrder,
                    List<String> techniqueParametersOrder,
                    List<String> evaluationMeasuresOrder,
                    WritableWorkbook workbook)
            throws WriteException, IOException {

        WritableSheet allCasesAggregateResults = workbook.createSheet("AllCasesAggregateResults", 0);

        {

            int column = 0;
            final int titlesRow = 0;

            //ExperimentNamesColumn
            addTitleText(allCasesAggregateResults, column, titlesRow, EXPERIMENT_NAME_COLUMN_NAME);
            for (int index = 0; index < caseStudyResultss.size(); index++) {
                int row = index + 1;
                setCellText(allCasesAggregateResults, column, row, caseStudyResultss.get(index).getCaseStudyAlias());
            }
            column++;

            //General hash
            addTitleText(allCasesAggregateResults, column, titlesRow, "hash");
            for (int index = 0; index < caseStudyResultss.size(); index++) {
                int row = index + 1;
                setCellIntegerNumber(allCasesAggregateResults, column, row, caseStudyResultss.get(index).getCaseStudy().hashCode());
            }
            column++;

            //dataValidation hash
            addTitleText(allCasesAggregateResults, column, titlesRow, "hashDataValidation");
            for (int index = 0; index < caseStudyResultss.size(); index++) {
                int row = index + 1;
                setCellIntegerNumber(allCasesAggregateResults, column, row, caseStudyResultss.get(index).getCaseStudy().hashDataValidation());
            }
            column++;

            for (String dataValidationParameter : dataValidationParametersOrder) {

                addTitleText(allCasesAggregateResults, column, titlesRow, dataValidationParameter);
                for (int index = 0; index < caseStudyResultss.size(); index++) {
                    int row = index + 1;

                    CaseStudyResults caseStudyResults = caseStudyResultss.get(index);
                    if (caseStudyResults.getDefinedDataValidationParameters().contains(dataValidationParameter)) {

                        Object dataValidationParameterValue = caseStudyResults.getDataValidationParameterValue(dataValidationParameter);
                        setCellContent(allCasesAggregateResults, column, row, dataValidationParameterValue);
                    }
                }
                column++;
            }

            //technique hash
            addTitleText(allCasesAggregateResults, column, titlesRow, "hashTechnique");
            for (int index = 0; index < caseStudyResultss.size(); index++) {
                int row = index + 1;
                setCellIntegerNumber(allCasesAggregateResults, column, row, caseStudyResultss.get(index).getCaseStudy().hashTechnique());
            }
            column++;

            for (String techniqueParameter : techniqueParametersOrder) {

                addTitleText(allCasesAggregateResults, column, titlesRow, techniqueParameter);
                for (int index = 0; index < caseStudyResultss.size(); index++) {
                    int row = index + 1;
                    CaseStudyResults caseStudyResults = caseStudyResultss.get(index);
                    if (caseStudyResults.getDefinedTechniqueParameters().contains(techniqueParameter)) {
                        Object techniqueParameterValue = caseStudyResults.getTechniqueParameterValue(techniqueParameter);
                        setCellContent(allCasesAggregateResults, column, row, techniqueParameterValue);
                    }
                }
                column++;
            }

            for (String evaluationMeasure : evaluationMeasuresOrder) {

                addTitleText(allCasesAggregateResults, column, titlesRow, evaluationMeasure);
                for (int index = 0; index < caseStudyResultss.size(); index++) {
                    int row = index + 1;
                    CaseStudyResults caseStudyResults = caseStudyResultss.get(index);
                    if (caseStudyResults.getDefinedEvaluationMeasures().contains(evaluationMeasure)) {
                        Object evaluationMeasureValue = caseStudyResults.getEvaluationMeasureValue(evaluationMeasure);
                        setCellContent(allCasesAggregateResults, column, row, evaluationMeasureValue);
                    }
                }
                column++;
            }
        }

        autoSizeColumns(allCasesAggregateResults);

    }

    private static void setCellText(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, defaultFormat);
        sheet.addCell(label);
    }

    /**
     * Converts the parameter structure of the case study definition (dataset,Formation and validations) into a plain
     * key-> value map.
     *
     * @param caseStudy
     * @return
     */
    public static <RecommendationModel extends Object, RatingType extends Rating> Map<String, Object> extractDataValidationParameters(CaseStudy caseStudy) {

        Map<String, Object> caseStudyParameters = new TreeMap<>();

        Map<String, Object> datasetLoaderParameters = ParameterOwnerExcel
                .extractParameterValues(caseStudy.getDatasetLoader());
        caseStudyParameters.putAll(datasetLoaderParameters);

        Map<String, Object> validationTechniqueParameters = ParameterOwnerExcel
                .extractParameterValues(caseStudy.getValidationTechnique());
        caseStudyParameters.putAll(validationTechniqueParameters);

        Map<String, Object> PredictionProtocolParameters = ParameterOwnerExcel
                .extractParameterValues(caseStudy.getPredictionProtocol());
        caseStudyParameters.putAll(PredictionProtocolParameters);

        return caseStudyParameters;
    }

    private static void setCellDoubleNumber(WritableSheet sheet, int column, int row,
            double value) throws WriteException, RowsExceededException {
        double rounded = NumberRounder.round(value);

        Number number = new Number(column, row, rounded, decimalFormat);
        sheet.addCell(number);
    }

    private static void setCellIntegerNumber(WritableSheet sheet, int column, int row,
            long integer) throws WriteException, RowsExceededException {
        Number number = new Number(column, row, integer, integerFormat);
        sheet.addCell(number);
    }

    private static void setCellContent(WritableSheet sheet, int column, int row,
            Object content) throws WriteException, RowsExceededException {

        if (content instanceof Long) {
            setCellIntegerNumber(sheet, column, row, (Long) content);
        } else if (content instanceof Integer) {
            setCellIntegerNumber(sheet, column, row, (Integer) content);
        } else if (content instanceof Double) {
            setCellDoubleNumber(sheet, column, row, (Double) content);
        } else if (content instanceof Double) {
            setCellDoubleNumber(sheet, column, row, (Double) content);
        } else {
            setCellText(sheet, column, row, content.toString());
        }
    }

    /**
     * Converts the parameter structure of the case study technique (RecommenderSystem) into a plain key-> value map.
     *
     * @param caseStudy
     * @return
     */
    public static <RecommendationModel extends Object, RatingType extends Rating> Map<String, Object> extractTechniqueParameters(CaseStudy caseStudy) {
        Map<String, Object> techniqueParameters = new TreeMap<>();

        Map<String, Object> RecommenderSystemParameters = ParameterOwnerExcel
                .extractParameterValues(caseStudy.getRecommenderSystem());
        techniqueParameters.putAll(RecommenderSystemParameters);

        return techniqueParameters;
    }

    public static <RecommendationModel extends Object, RatingType extends Rating>
            Map<String, java.lang.Number> extractEvaluationMeasuresValues(
                    CaseStudy<RecommendationModel, RatingType> caseStudy) {

        Map<String, java.lang.Number> evaluationMeasuresValues = new TreeMap<>();

        for (EvaluationMeasure evaluationMeasure : caseStudy.getEvaluationMeasures()) {

            MeasureResult measureResult = caseStudy.getAggregateMeasureResult(evaluationMeasure);
            double measureValue = measureResult.getValue();
            evaluationMeasuresValues.put(evaluationMeasure.getName(), measureValue);

        }

        return evaluationMeasuresValues;
    }

    public static <RecommendationModel extends Object, RatingType extends Rating>
            void writeNumExecutionsSheet(
                    List<CaseStudyResults<RecommendationModel, RatingType>> caseStudyResultses,
                    List<String> dataValidationParametersOrder,
                    List<String> techniqueParametersOrder,
                    List<String> evaluationMeasuresOrder,
                    WritableWorkbook workbook)
            throws WriteException {

        List<CaseStudy> caseStudys = caseStudyResultses.stream().map(caseStudyResults -> caseStudyResults.getCaseStudy()).collect(Collectors.toList());

        Set<CaseStudyResults> dataValidationAliases = new TreeSet<>(
                CaseStudyResults.dataValidationComparator);
        Set<CaseStudyResults> techniqueAliases = new TreeSet<>(
                CaseStudyResults.techniqueComparator);

        dataValidationAliases.addAll(caseStudyResultses);
        techniqueAliases.addAll(caseStudyResultses);

        Map<String, List<CaseStudy>> byCaseStudy = caseStudys.stream().collect(Collectors
                .groupingBy(CaseStudy -> CaseStudy.getAlias()));

        caseStudys = byCaseStudy.values().parallelStream().map(sameCaseStudyWithDifferentNumExecutions -> {
            Map<Integer, List<CaseStudy>> byNumExecutions = sameCaseStudyWithDifferentNumExecutions
                    .parallelStream()
                    .collect(Collectors.groupingBy(CaseStudy -> CaseStudy.getNumExecutions()));

            int maxExec = byNumExecutions.keySet().stream().mapToInt(numExec -> numExec)
                    .max().orElse(-1);

            if (byNumExecutions.get(maxExec).size() > 1) {
                byNumExecutions.get(maxExec).forEach(CaseStudy -> Global.showWarning(CaseStudy.getAlias() + "\""));
                throw new IllegalStateException("More than one execution with the maximum! (" + byNumExecutions.get(maxExec).size() + ")");
            }

            return byNumExecutions.get(maxExec).get(0);
        }).collect(Collectors.toList());

        Global.showInfoMessage("Processing " + caseStudys.size() + " different results files.\n");

        List<ParameterChain> differentChainsWithAliasesAndSeed = ParameterChain.obtainDifferentChains(caseStudys);

        List<ParameterChain> differentChains = differentChainsWithAliasesAndSeed.stream()
                .filter(chain -> !chain.isAlias())
                .filter(chain -> !chain.isSeed())
                .collect(Collectors.toList());

        List<ParameterChain> dataValidationDifferentChains = differentChains.stream()
                .filter(chain -> chain.isDataValidationParameter())
                .filter(chain -> !chain.isNumExecutions())
                .collect(Collectors.toList());

        List<ParameterChain> techniqueDifferentChains = differentChains.stream()
                .filter(chain -> chain.isTechniqueParameter()).collect(Collectors.toList());

        if (techniqueDifferentChains.isEmpty()) {
            ParameterChain grsAliasChain = new ParameterChain(caseStudys.get(0))
                    .createWithNode(CaseStudy.RECOMMENDER_SYSTEM, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);
            techniqueDifferentChains.add(grsAliasChain);
        }
        if (dataValidationDifferentChains.isEmpty()) {
            ParameterChain datasetLoaderAliasChain = new ParameterChain(caseStudys.get(0))
                    .createWithNode(CaseStudy.DATASET_LOADER, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            dataValidationDifferentChains.add(datasetLoaderAliasChain);
        }

        CaseStudyResultMatrix matrix = getNumExecutionsMatrix(techniqueDifferentChains, dataValidationDifferentChains, caseStudyResultses);

        writeMatrixInSheet(workbook, CaseStudy.NUM_EXECUTIONS.getName(), matrix, CaseStudy.NUM_EXECUTIONS.getName());
    }

    public static <RecommendationModel extends Object, RatingType extends Rating> CaseStudyResultMatrix getNumExecutionsMatrix(
            List<ParameterChain> techniqueDifferentChains,
            List<ParameterChain> dataValidationDifferentChains,
            List<CaseStudyResults<RecommendationModel, RatingType>> caseStudyResultses) {
        CaseStudyResultMatrix matrix = new CaseStudyResultMatrix(techniqueDifferentChains, dataValidationDifferentChains, CaseStudy.NUM_EXECUTIONS.getName());
        caseStudyResultses.stream().forEach(caseStudyResults -> {
            int numExecutions = caseStudyResults.getNumExecutions();
            java.lang.Number existingNumExecutions = matrix.getValue(caseStudyResults.getCaseStudy());

            if (existingNumExecutions == null) {
                matrix.addValue(caseStudyResults.getCaseStudy(), numExecutions);
            } else if (numExecutions > existingNumExecutions.intValue()) {
                matrix.addValue(caseStudyResults.getCaseStudy(), numExecutions);
            }
        });
        return matrix;
    }

    public static <RecommendationModel extends Object, RatingType extends Rating>
            void writeEvaluationMeasureSpecificSheet(
                    List<CaseStudyResults<RecommendationModel, RatingType>> caseStudyResultses,
                    List<String> dataValidationParametersOrder,
                    List<String> techniqueParametersOrder,
                    String evaluationMeasure,
                    WritableWorkbook workbook)
            throws WriteException, IOException {

        List<CaseStudy<RecommendationModel, RatingType>> caseStudys = caseStudyResultses.stream()
                .map(caseStudyResult -> caseStudyResult.getCaseStudy())
                .collect(Collectors.toList());

        Set<CaseStudyResults<RecommendationModel, RatingType>> dataValidationAliases = new TreeSet<>(
                CaseStudyResults.dataValidationComparator);
        Set<CaseStudyResults<RecommendationModel, RatingType>> techniqueAliases = new TreeSet<>(
                CaseStudyResults.techniqueComparator);

        dataValidationAliases.addAll(caseStudyResultses);
        techniqueAliases.addAll(caseStudyResultses);

        List<ParameterChain> differentChainsWithAliases = ParameterChain.obtainDifferentChains(caseStudys);

        List<ParameterChain> differentChains = differentChainsWithAliases.stream()
                .filter(chain -> !chain.isAlias())
                .filter(chain -> !chain.isSeed())
                .collect(Collectors.toList());

        List<ParameterChain> dataValidationDifferentChains = differentChains.stream()
                .filter(chain -> chain.isDataValidationParameter())
                .filter(chain -> !chain.isNumExecutions())
                .collect(Collectors.toList());
        List<ParameterChain> techniqueDifferentChains = differentChains.stream()
                .filter(chain -> chain.isTechniqueParameter())
                .collect(Collectors.toList());

        writeRowAndColumnCombination(techniqueDifferentChains, caseStudys, dataValidationDifferentChains, evaluationMeasure, caseStudyResultses, workbook);
    }

    public static <RecommendationModel extends Object, RatingType extends Rating>
            void writeRowAndColumnCombination(
                    List<ParameterChain> rowChains,
                    List<CaseStudy<RecommendationModel, RatingType>> CaseStudys,
                    List<ParameterChain> columnChains,
                    String evaluationMeasure,
                    List<CaseStudyResults<RecommendationModel, RatingType>> CaseStudyResults,
                    WritableWorkbook workbook) throws WriteException {

        writeRowAndColumnCombination(rowChains, CaseStudys, columnChains, evaluationMeasure, CaseStudyResults, workbook, evaluationMeasure);
    }

    public static <RecommendationModel extends Object, RatingType extends Rating>
            void writeRowAndColumnCombination(
                    List<ParameterChain> rowChains,
                    List<CaseStudy<RecommendationModel, RatingType>> CaseStudys,
                    List<ParameterChain> columnChains,
                    String evaluationMeasure,
                    List<CaseStudyResults<RecommendationModel, RatingType>> CaseStudyResults,
                    WritableWorkbook workbook,
                    String sheetName) throws WriteException {
        if (rowChains.isEmpty()) {
            ParameterChain grsAliasChain = new ParameterChain(CaseStudys.get(0))
                    .createWithNode(CaseStudy.RECOMMENDER_SYSTEM, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);
            rowChains.add(grsAliasChain);
        }
        if (columnChains.isEmpty()) {
            ParameterChain datasetLoaderAliasChain = new ParameterChain(CaseStudys.get(0))
                    .createWithNode(CaseStudy.DATASET_LOADER, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            columnChains.add(datasetLoaderAliasChain);
        }

        CaseStudyResultMatrix matrix = prepareExcelMatrix(
                rowChains,
                columnChains,
                evaluationMeasure,
                CaseStudyResults);

        writeMatrixInSheet(workbook, evaluationMeasure, matrix, sheetName);
    }

    public static <RecommendationModel extends Object, RatingType extends Rating> CaseStudyResultMatrix prepareExcelMatrix(
            List<ParameterChain> rowChains,
            List<ParameterChain> columnChains,
            String evaluationMeasure,
            List<CaseStudyResults<RecommendationModel, RatingType>> caseStudyResultses) {

        CaseStudyResultMatrix matrix = new CaseStudyResultMatrix(rowChains, columnChains, evaluationMeasure);
        matrix.prepareColumnAndRowNames(caseStudyResultses);
        List<CaseStudyResults> CaseStudyResultsMaxNumExecutions = new ArrayList<>();
        for (String rowName : matrix.getRowNames()) {
            for (String columnName : matrix.getColumnNames()) {

                List<CaseStudyResults> thisCellCaseStudys = caseStudyResultses.stream()
                        .filter(CaseStudyResult -> matrix.getRow((ParameterOwner) CaseStudyResult.getCaseStudy()).equals(rowName))
                        .filter(CaseStudyResult -> matrix.getColumn((ParameterOwner) CaseStudyResult.getCaseStudy()).equals(columnName))
                        .sorted(((CaseStudyResult1, CaseStudyResult2) -> Integer.compare(CaseStudyResult1.getNumExecutions(), CaseStudyResult2.getNumExecutions())))
                        .collect(Collectors.toList());
                Collections.reverse(thisCellCaseStudys);

                if (thisCellCaseStudys.isEmpty()) {
                    continue;
                }

                if (thisCellCaseStudys.size() == 1) {
                    CaseStudyResultsMaxNumExecutions.addAll(thisCellCaseStudys);
                } else {

                    if (Global.isVerboseAnnoying()) {
                        String row = matrix.getRow(thisCellCaseStudys.get(0).getCaseStudy());
                        String column = matrix.getColumn(thisCellCaseStudys.get(0).getCaseStudy());
                        Global.show("Executions for cell (" + row + "," + column + ")\n");
                        thisCellCaseStudys.stream().forEach((CaseStudyResultsMaxNumExecution) -> {
                            Global.show(CaseStudyResultsMaxNumExecution.getNumExecutions() + "\n");
                        });
                    }

                    CaseStudyResultsMaxNumExecutions.add(thisCellCaseStudys.get(0));
                }
            }
        }
        CaseStudyResultsMaxNumExecutions.stream().forEach(CaseStudyResult -> {
            java.lang.Number evaluationMeasureValue = CaseStudyResult.getEvaluationMeasureValue(evaluationMeasure);
            matrix.addValue(CaseStudyResult.getCaseStudy(), evaluationMeasureValue);
        });
        return matrix;
    }

    public static void writeMatrixInSheet(
            WritableWorkbook workbook,
            String evaluationMeasure,
            CaseStudyResultMatrix matrix,
            String sheetName) throws WriteException {
        final int sheetNameMaxLenght = 30;

        if (sheetName.length() > sheetNameMaxLenght) {

            String shortenName = sheetName.substring(0, Math.min(sheetNameMaxLenght, sheetName.length()));
            Global.showInfoMessage("Sheet name too long! " + sheetName + " --> " + shortenName);
            sheetName = shortenName;
        }

        WritableSheet sheet = workbook.createSheet(sheetName, workbook.getNumberOfSheets());

        {

            int column = 0;
            int row = 0;

            //Titles ROW
            addTitleText(sheet, column, row, evaluationMeasure);
            column++;

            for (String columnName : matrix.getColumnNames()) {
                setCellContent(sheet, column, row, columnName);
                column++;
            }

            row++;

            //Titles row
            for (String rowName : matrix.getRowNames()) {

                column = 0;
                setCellContent(sheet, column, row, rowName);
                column++;
                for (String columnName : matrix.getColumnNames()) {

                    if (matrix.containsValue(rowName, columnName)) {
                        Object value = matrix.getValue(rowName, columnName);
                        setCellContent(sheet, column, row, value);
                    }
                    column++;
                }
                row++;
            }
        }

        autoSizeColumns(sheet);
    }

    public static <RecommendationModel extends Object, RatingType extends Rating>
            boolean isOnlyOneColumn(List<CaseStudyResults<RecommendationModel, RatingType>> caseStudyResultses) {
        List<CaseStudy<RecommendationModel, RatingType>> caseStudys = caseStudyResultses.stream()
                .map(caseStudyResult -> caseStudyResult.getCaseStudy())
                .collect(Collectors.toList());

        List<ParameterChain> differentChainsWithAliases = ParameterChain.obtainDifferentChains(caseStudys);

        List<ParameterChain> differentChains = differentChainsWithAliases.stream()
                .filter(chain -> !chain.isAlias())
                .filter(chain -> !chain.isSeed())
                .collect(Collectors.toList());

        List<ParameterChain> dataValidationDifferentChains = differentChains.stream()
                .filter(chain -> chain.isDataValidationParameter())
                .filter(chain -> !chain.isNumExecutions())
                .collect(Collectors.toList());

        return dataValidationDifferentChains.isEmpty() || dataValidationDifferentChains.size() == 1;
    }

    public static <RecommendationModel extends Object, RatingType extends Rating>
            void writeEvaluationMeasureParameterCombinationsSheets(
                    List<CaseStudyResults<RecommendationModel, RatingType>> caseStudyResultses,
                    List<String> dataValidationParametersOrder,
                    List<String> techniqueParametersOrder,
                    String evaluationMeasure,
                    WritableWorkbook workbook
            ) throws WriteException {

        List<CaseStudy<RecommendationModel, RatingType>> caseStudys = caseStudyResultses.stream().map(caseStudyResult -> caseStudyResult.getCaseStudy()).collect(Collectors.toList());

        Set<CaseStudyResults<RecommendationModel, RatingType>> dataValidationAliases = new TreeSet<>(
                CaseStudyResults.dataValidationComparator);
        Set<CaseStudyResults<RecommendationModel, RatingType>> techniqueAliases = new TreeSet<>(
                CaseStudyResults.techniqueComparator);

        dataValidationAliases.addAll(caseStudyResultses);
        techniqueAliases.addAll(caseStudyResultses);

        List<ParameterChain> differentChainsWithAliases = ParameterChain
                .obtainDifferentChains(caseStudys);

        List<ParameterChain> differentChains = differentChainsWithAliases
                .stream()
                .filter(chain -> !chain.isAlias())
                .filter(chain -> !chain.isNumExecutions())
                .filter(chain -> !chain.isSeed())
                .collect(Collectors.toList());

        Set<Combination> combinationsOfColumnRowParameters = obtainDifferentParameterInCollumn(differentChains)
                .stream()
                .filter(combination -> combination.row.size() + combination.column.size() == differentChains.size())
                .collect(Collectors.toCollection(TreeSet::new));

        List<ParameterChain> columnChains = differentChains.stream()
                .filter(chain -> chain.isDataValidationParameter())
                .filter(chain -> !chain.isNumExecutions())
                .collect(Collectors.toList());

        List<ParameterChain> rowChains = differentChains.stream()
                .filter(chain -> chain.isTechniqueParameter())
                .collect(Collectors.toList());
        combinationsOfColumnRowParameters.add(new Combination(rowChains, columnChains));

        combinationsOfColumnRowParameters.parallelStream().forEach(combination -> {
            try {

                String sheetName = evaluationMeasure;
                for (ParameterChain chain : combination.column) {
                    sheetName = sheetName + "_" + chain.getParameterName();
                }

                List<ParameterChain> rowList = combination.row.stream().sorted().collect(Collectors.toList());
                List<ParameterChain> columnList = combination.column.stream().sorted().collect(Collectors.toList());
                writeRowAndColumnCombination(
                        rowList,
                        caseStudys,
                        columnList,
                        evaluationMeasure,
                        caseStudyResultses,
                        workbook,
                        sheetName
                );
            } catch (WriteException ex) {
                Logger.getLogger(CaseStudyExcel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }
}
