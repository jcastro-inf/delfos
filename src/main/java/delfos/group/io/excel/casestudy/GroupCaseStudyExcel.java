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
package delfos.group.io.excel.casestudy;

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
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.excel.parameterowner.ParameterOwnerExcel;
import delfos.main.managers.experiment.join.xml.GroupCaseStudyResult;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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
public class GroupCaseStudyExcel {

    public static String RESULT_EXTENSION = "xml";

    public static final String AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME = CaseStudyExcel.AGGREGATE_RESULTS_EXCEL_DEFAULT_FILE_NAME;
    public static final String ALL_EXPERIMENTS_SHEET_NAME = CaseStudyExcel.ALL_EXPERIMENTS_SHEET_NAME;
    public static final String CASE_DEFINITION_SHEET_NAME = CaseStudyExcel.CASE_DEFINITION_SHEET_NAME;

    public static final String EXPERIMENT_NAME_COLUMN_NAME = CaseStudyExcel.EXPERIMENT_NAME_COLUMN_NAME;
    public static final String DATASET_LOADER_COLUMN_NAME = CaseStudyExcel.DATASET_LOADER_COLUMN_NAME;

    public static final int EXPERIMENT_NAME_COLUMN = 0;
    public static final int DATASET_LOADER_ALIAS_COLUMN = 1;
    public static final int GROUP_EVALUATION_MEASURES_OFFSET = 2;

    public static final String NAN_CELL_STRING = "NaN";

    private static WritableCellFormat titleFormat = null;
    private static WritableCellFormat defaultFormat = null;
    private static WritableCellFormat decimalFormat;
    private static WritableCellFormat integerFormat;
    private static final int TITLE_CELL_WIDTH = 3 - 1;

    static {
        try {
            initTitleFormat();
            initIntegerFormat();
            initDecimalFormat();
            initDefaultFormat();
        } catch (WriteException ex) {
            Logger.getLogger(GroupCaseStudyExcel.class.getName()).log(Level.SEVERE, null, ex);
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

        Map<String, Map<String, Double>> metricValues_byCase = new TreeMap<>();
        Map<String, Map<String, String>> otherValues_byCase = new TreeMap<>();

        for (File inputFile : inputFiles) {
            try {
                TreeMap<String, Double> valoresDeMetricas = new TreeMap<>();
                Workbook workbook = Workbook.getWorkbook(inputFile);

                Sheet aggregateResults = workbook.getSheet(AGGREGATE_RESULTS);

                if (aggregateResults != null) {

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
                            throw new IllegalStateException("Cannot recognize cell type");
                        }
                    }

                    metricValues_byCase.put(inputFile.getName(), valoresDeMetricas);

                    otherValues_byCase.put(inputFile.getName(), new TreeMap<>());

                    String datasetLoaderName = getConfiguredDatasetLoaderName(workbook.getSheet(CASE_DEFINITION_SHEET_NAME));
                    otherValues_byCase.get(inputFile.getName()).put(DATASET_LOADER_COLUMN_NAME, datasetLoaderName);
                } else {
                    Global.showWarning("The file '" + inputFile.getAbsolutePath() + "' does not have a proper caseStudyResult");
                }
            } catch (IOException | BiffException ex) {
                ERROR_CODES.CANNOT_READ_CASE_STUDY_EXCEL.exit(ex);
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

            WritableSheet allExperiments = workbook.createSheet(ALL_EXPERIMENTS_SHEET_NAME, 0);

            //Seet the content.
            int row = 0;

            //Titulos.
            {
                addTitleText(allExperiments, EXPERIMENT_NAME_COLUMN, row, EXPERIMENT_NAME_COLUMN_NAME);
                addTitleText(allExperiments, DATASET_LOADER_ALIAS_COLUMN, row, DATASET_LOADER_COLUMN_NAME);
                for (String metrica : indexColumn.keySet()) {
                    int column = indexColumn.get(metrica);
                    column = column + GROUP_EVALUATION_MEASURES_OFFSET;
                    addTitleText(allExperiments, column, row, metrica);
                }
                row++;
            }

            {
                for (String experimentName : metricValues_byCase.keySet()) {

                    String datasetLoaderName = otherValues_byCase.get(experimentName).get(DATASET_LOADER_COLUMN_NAME);

                    setCellText(allExperiments, EXPERIMENT_NAME_COLUMN, row, experimentName);

                    setCellText(allExperiments, DATASET_LOADER_ALIAS_COLUMN, row, datasetLoaderName);

                    Map<String, Double> experimentResults = metricValues_byCase.get(experimentName);
                    for (String metricName : experimentResults.keySet()) {
                        double metricValue = metricValues_byCase.get(experimentName).get(metricName);
                        int column = indexColumn.get(metricName) + GROUP_EVALUATION_MEASURES_OFFSET;
                        setCellDoubleNumber(allExperiments, column, row, metricValue);
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

    private static String getConfiguredDatasetLoaderName(Sheet sheet) {
        String datasetLoaderAlias;

        Cell datasetLoaderCell = sheet.findCell(DATASET_LOADER_CELL_CONTENT);
        if (datasetLoaderCell == null) {
            throw new IllegalArgumentException("Cannot datasetLoader cell.");
        }

        Cell datasetLoaderAliasCell = sheet.findCell(
                ParameterOwner.ALIAS.getName(),
                datasetLoaderCell.getColumn(), datasetLoaderCell.getRow(),
                sheet.getColumns(), sheet.getRows(), false);
        if (datasetLoaderAliasCell == null) {
            throw new IllegalArgumentException("Cannot datasetLoader alias cell.");
        }

        Cell dataetLoaderAliasValueCell = sheet.getCell(datasetLoaderAliasCell.getColumn() + 2, datasetLoaderAliasCell.getRow());
        datasetLoaderAlias = dataetLoaderAliasValueCell.getContents();

        if (datasetLoaderAlias == null || datasetLoaderAlias.equals("")) {
            throw new IllegalArgumentException("Cannot find the datasetLoader alias");
        }
        return datasetLoaderAlias;
    }

    public synchronized static void saveCaseResults(GroupCaseStudy caseStudyGroup, File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalStateException("GroupCaseStudy save to spreadsheet: Not a directory (" + directory.toString() + ")");
        }

        String caseStudyFileName = GroupCaseStudyXML.getCaseStudyFileName(caseStudyGroup);

        File file = new File(directory.getPath() + File.separator + caseStudyFileName + ".xls");
        caseStudyToSpreadsheetFile_results(caseStudyGroup, file);
    }

    public synchronized static void caseStudyToSpreadsheetFile_results(GroupCaseStudy caseStudyGroup, File file) {

        if (file.isDirectory()) {
            throw new IllegalStateException("GroupCaseStudy save to spreadsheet: Not a file (" + file.toString() + ")");
        }

        if (!caseStudyGroup.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        try {
            WorkbookSettings wbSettings = new WorkbookSettings();

            wbSettings.setLocale(new Locale("en", "EN"));

            WritableWorkbook workbook;

            if (file.exists()) {
                file.delete();
            }

            workbook = Workbook.createWorkbook(file, wbSettings);

            WritableSheet caseDefinitionSheet = workbook.createSheet("CaseDefinition", 0);
            createCaseDefinitionSheet(caseStudyGroup, caseDefinitionSheet);
            autoSizeColumns(caseDefinitionSheet);

            WritableSheet executionsSheet = workbook.createSheet("Executions", 1);
            createExecutionsSheet(caseStudyGroup, executionsSheet);
            autoSizeColumns(executionsSheet);

            WritableSheet aggregateResultsSheet = workbook.createSheet(AGGREGATE_RESULTS, 2);
            createAggregateResultsSheet(caseStudyGroup, aggregateResultsSheet);
            autoSizeColumns(aggregateResultsSheet);

            workbook.write();
            workbook.close();

        } catch (WriteException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(new FileNotFoundException("Cannot access file " + file.getAbsolutePath() + "."));
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
    }
    public static final String AGGREGATE_RESULTS = "AggregateResults";

    private static void createCaseDefinitionSheet(GroupCaseStudy caseStudyGroup, WritableSheet sheet) throws WriteException {

        int column = 0;
        int row = 0;

        //Create table for GRS
        {
            GroupRecommenderSystem<Object, Object> groupRecommenderSystem = caseStudyGroup.getGroupRecommenderSystem();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
            addTitleText(sheet, column, row, "Group Recommender System");
            row++;
            setCellText(sheet, column, row, groupRecommenderSystem.getName());
            row++;
            for (Parameter parameter : groupRecommenderSystem.getParameters()) {
                Object parameterValue = groupRecommenderSystem.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for DatasetLoader
        {
            DatasetLoader<? extends Rating> datasetLoader = caseStudyGroup.getDatasetLoader();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
            addTitleText(sheet, column, row, DATASET_LOADER_CELL_CONTENT);
            row++;
            setCellText(sheet, column, row, datasetLoader.getName());
            row++;
            for (Parameter parameter : datasetLoader.getParameters()) {
                Object parameterValue = datasetLoader.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for GroupFormationTechnique
        {
            GroupFormationTechnique groupFormationTechnique = caseStudyGroup.getGroupFormationTechnique();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
            addTitleText(sheet, column, row, "Group Formation Technique");
            row++;
            setCellText(sheet, column, row, groupFormationTechnique.getName());
            row++;
            for (Parameter parameter : groupFormationTechnique.getParameters()) {
                Object parameterValue = groupFormationTechnique.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for ValidationTechnique
        {
            ValidationTechnique validationTechnique = caseStudyGroup.getValidationTechnique();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
            addTitleText(sheet, column, row, "Group Validation Technique");
            row++;
            setCellText(sheet, column, row, validationTechnique.getName());
            row++;
            for (Parameter parameter : validationTechnique.getParameters()) {
                Object parameterValue = validationTechnique.getParameterValue(parameter);
                row = writeParameterAndValue(parameter, parameterValue, sheet, column, row);
                row++;
            }
        }
        row += 2;

        //Create table for GroupPredictionProtocol
        {
            GroupPredictionProtocol groupPredictionProtocol = caseStudyGroup.getGroupPredictionProtocol();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
            addTitleText(sheet, column, row, "Group Prediction Protocol");
            row++;
            setCellText(sheet, column, row, groupPredictionProtocol.getName());
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
            RelevanceCriteria relevanceCriteria = caseStudyGroup.getRelevanceCriteria();
            sheet.mergeCells(column + 0, row, column + TITLE_CELL_WIDTH, row);
            addTitleText(sheet, column, row, "Relevance Criteria threshold >= " + relevanceCriteria.getThreshold().doubleValue());
            row++;
        }
    }
    public static final String DATASET_LOADER_CELL_CONTENT = "Dataset Loader";

    final static int PARAMETER_NAME_OFFSET = 1;
    final static int PARAMETER_TYPE_OFFSET = 2;
    final static int PARAMETER_VALUE_OFFSET = 3;

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
        setCellText(sheet, column, row, "Parameter");
        setCellText(sheet, column + PARAMETER_NAME_OFFSET, row, parameter.getName());
        setCellText(sheet, column + PARAMETER_TYPE_OFFSET, row, parameter.getRestriction().getName());

        if (parameterValue instanceof ParameterOwner) {
            ParameterOwner parameterOwner = (ParameterOwner) parameterValue;
            setCellText(sheet, column + PARAMETER_VALUE_OFFSET, row, parameterOwner.getName());
        } else if (parameterValue instanceof java.lang.Number) {

            if ((parameterValue instanceof java.lang.Integer) || (parameterValue instanceof java.lang.Long)) {
                java.lang.Long number = ((java.lang.Number) parameterValue).longValue();
                setCellIntegerNumber(sheet, column + PARAMETER_VALUE_OFFSET, row, number);
            } else {
                java.lang.Number number = (java.lang.Number) parameterValue;
                setCellDoubleNumber(sheet, column + PARAMETER_VALUE_OFFSET, row, number.doubleValue());
            }
        } else {
            setCellText(sheet, column + PARAMETER_VALUE_OFFSET, row, parameterValue.toString());
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

    private static void createExecutionsSheet(GroupCaseStudy caseStudyGroup, WritableSheet sheet) throws WriteException {

        int row = 0;

        final int numExecutions = caseStudyGroup.getNumExecutions();
        final int numSplits = caseStudyGroup.getValidationTechnique().getNumberOfSplits();

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

        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, GroupEvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = splitColumn + 1;
            for (GroupEvaluationMeasure groupEvaluationMeasure : caseStudyGroup.getEvaluationMeasures()) {
                indexOfMeasures.put(groupEvaluationMeasure.getName(), i++);
                metricsByName.put(groupEvaluationMeasure.getName(), groupEvaluationMeasure);
            }
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
                setCellIntegerNumber(sheet, vueltaColumn, row, vuelta);
                setCellIntegerNumber(sheet, executionColumn, row, thisExecution + 1);
                setCellIntegerNumber(sheet, splitColumn, row, thisSplit + 1);

                //Ahora los valores de cada metrica.
                for (Map.Entry<String, Integer> entry : indexOfMeasures.entrySet()) {
                    String name = entry.getKey();
                    int column = entry.getValue();

                    final double value;

                    //Es una medida cualquiera.
                    GroupEvaluationMeasure groupEvaluationMeasure = metricsByName.get(name);
                    value = caseStudyGroup.getMeasureResult(groupEvaluationMeasure, thisExecution, thisSplit).getValue();

                    if (!Double.isNaN(value)) {
                        double decimalTrimmedValue = NumberRounder.round(value);
                        setCellDoubleNumber(sheet, column, row, decimalTrimmedValue);
                    } else {
                        setCellText(sheet, column, row, NAN_CELL_STRING);
                    }
                }

                vuelta++;
                row++;

            }
        }

    }

    private static void createAggregateResultsSheet(GroupCaseStudy caseStudyGroup, WritableSheet sheet) throws WriteException {
        int row = 0;

        Map<String, Integer> indexOfMeasures = new TreeMap<>();
        Map<String, GroupEvaluationMeasure> metricsByName = new TreeMap<>();
        {
            int i = 0;
            for (GroupEvaluationMeasure groupEvaluationMeasure : caseStudyGroup.getEvaluationMeasures()) {
                indexOfMeasures.put(groupEvaluationMeasure.getName(), i++);

                metricsByName.put(groupEvaluationMeasure.getName(), groupEvaluationMeasure);
            }
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
            GroupEvaluationMeasure groupEvaluationMeasure = metricsByName.get(name);
            value = caseStudyGroup.getAggregateMeasureResult(groupEvaluationMeasure).getValue();

            if (!Double.isNaN(value)) {
                double decimalTrimmedValue = NumberRounder.round(value);
                setCellDoubleNumber(sheet, column, row, decimalTrimmedValue);
            } else {
                setCellText(sheet, column, row, NAN_CELL_STRING);
            }
            double decimalTrimmedValue = NumberRounder.round(value);

            setCellDoubleNumber(sheet, column, row, decimalTrimmedValue);
        }

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

    private static void setCellText(WritableSheet sheet, int column, int row, String s)
            throws WriteException, RowsExceededException {
        Label label;
        label = new Label(column, row, s, defaultFormat);
        sheet.addCell(label);
    }

    /**
     * Converts the parameter structure of the case study definition (dataset,groupFormation and validations) into a
     * plain key-> value map.
     *
     * @param groupCaseStudy
     * @return
     */
    public static Map<String, Object> extractDataValidationParameters(GroupCaseStudy groupCaseStudy) {

        Map<String, Object> caseStudyParameters = new TreeMap<>();

        Map<String, Object> datasetLoaderParameters = ParameterOwnerExcel
                .extractParameterValues(groupCaseStudy.getDatasetLoader());
        caseStudyParameters.putAll(datasetLoaderParameters);

        Map<String, Object> groupFormationTechniqueParameters = ParameterOwnerExcel
                .extractParameterValues(groupCaseStudy.getGroupFormationTechnique());
        caseStudyParameters.putAll(groupFormationTechniqueParameters);

        Map<String, Object> validationTechniqueParameters = ParameterOwnerExcel
                .extractParameterValues(groupCaseStudy.getValidationTechnique());
        caseStudyParameters.putAll(validationTechniqueParameters);

        Map<String, Object> groupPredictionProtocolParameters = ParameterOwnerExcel
                .extractParameterValues(groupCaseStudy.getGroupPredictionProtocol());
        caseStudyParameters.putAll(groupPredictionProtocolParameters);

        return caseStudyParameters;
    }

    /**
     * Converts the parameter structure of the case study technique (groupRecommenderSystem) into a plain key-> value
     * map.
     *
     * @param groupCaseStudy
     * @return
     */
    public static Map<String, Object> extractTechniqueParameters(GroupCaseStudy groupCaseStudy) {
        Map<String, Object> techniqueParameters = new TreeMap<>();

        Map<String, Object> groupRecommenderSystemParameters = ParameterOwnerExcel
                .extractParameterValues(groupCaseStudy.getGroupRecommenderSystem());
        techniqueParameters.putAll(groupRecommenderSystemParameters);

        return techniqueParameters;
    }

    public static Map<String, java.lang.Number> extractEvaluationMeasuresValues(GroupCaseStudy groupCaseStudy) {

        Map<String, java.lang.Number> evaluationMeasuresValues = new TreeMap<>();

        for (GroupEvaluationMeasure evaluationMeasure : groupCaseStudy.getEvaluationMeasures()) {

            GroupEvaluationMeasureResult measureResult = groupCaseStudy.getAggregateMeasureResult(evaluationMeasure);
            double measureValue = measureResult.getValue();
            evaluationMeasuresValues.put(evaluationMeasure.getName(), measureValue);

            Map<String, java.lang.Number> extendedPerformances = evaluationMeasure.agregateResultsExtendedPerformance(measureResult);

            extendedPerformances.keySet().stream().forEach((extendedPerformance) -> {
                java.lang.Number extendedPerformanceValue = extendedPerformances.get(extendedPerformance);

                evaluationMeasuresValues.put(evaluationMeasure.getName() + "." + extendedPerformance, extendedPerformanceValue);
            });
        }

        return evaluationMeasuresValues;
    }

    public static void writeGeneralSheet(List<GroupCaseStudyResult> groupCaseStudyResults, List<String> dataValidationParametersOrder, List<String> techniqueParametersOrder, List<String> evaluationMeasuresOrder, WritableWorkbook workbook) throws WriteException, IOException {

        WritableSheet allCasesAggregateResults = workbook.createSheet("AllCasesAggregateResults", 0);

        {

            int column = 0;
            final int titlesRow = 0;

            //ExperimentNamesColumn
            addTitleText(allCasesAggregateResults, column, titlesRow, EXPERIMENT_NAME_COLUMN_NAME);
            for (int index = 0; index < groupCaseStudyResults.size(); index++) {
                int row = index + 1;
                setCellText(allCasesAggregateResults, column, row, groupCaseStudyResults.get(index).getGroupCaseStudyAlias());
            }
            column++;

            //General hash
            addTitleText(allCasesAggregateResults, column, titlesRow, "hash");
            for (int index = 0; index < groupCaseStudyResults.size(); index++) {
                int row = index + 1;
                setCellIntegerNumber(allCasesAggregateResults, column, row, groupCaseStudyResults.get(index).getGroupCaseStudy().hashCode());
            }
            column++;

            //dataValidation hash
            addTitleText(allCasesAggregateResults, column, titlesRow, "hashDataValidation");
            for (int index = 0; index < groupCaseStudyResults.size(); index++) {
                int row = index + 1;
                setCellIntegerNumber(allCasesAggregateResults, column, row, groupCaseStudyResults.get(index).getGroupCaseStudy().hashDataValidation());
            }
            column++;

            for (String dataValidationParameter : dataValidationParametersOrder) {

                addTitleText(allCasesAggregateResults, column, titlesRow, dataValidationParameter);
                for (int index = 0; index < groupCaseStudyResults.size(); index++) {
                    int row = index + 1;

                    GroupCaseStudyResult groupCaseStudyResult = groupCaseStudyResults.get(index);
                    if (groupCaseStudyResult.getDefinedDataValidationParameters().contains(dataValidationParameter)) {

                        Object dataValidationParameterValue = groupCaseStudyResult.getDataValidationParameterValue(dataValidationParameter);
                        setCellContent(allCasesAggregateResults, column, row, dataValidationParameterValue);
                    }
                }
                column++;
            }

            //technique hash
            addTitleText(allCasesAggregateResults, column, titlesRow, "hashTechnique");
            for (int index = 0; index < groupCaseStudyResults.size(); index++) {
                int row = index + 1;
                setCellIntegerNumber(allCasesAggregateResults, column, row, groupCaseStudyResults.get(index).getGroupCaseStudy().hashTechnique());
            }
            column++;

            for (String techniqueParameter : techniqueParametersOrder) {

                addTitleText(allCasesAggregateResults, column, titlesRow, techniqueParameter);
                for (int index = 0; index < groupCaseStudyResults.size(); index++) {
                    int row = index + 1;
                    GroupCaseStudyResult groupCaseStudyResult = groupCaseStudyResults.get(index);
                    if (groupCaseStudyResult.getDefinedTechniqueParameters().contains(techniqueParameter)) {
                        Object techniqueParameterValue = groupCaseStudyResult.getTechniqueParameterValue(techniqueParameter);
                        setCellContent(allCasesAggregateResults, column, row, techniqueParameterValue);
                    }
                }
                column++;
            }

            for (String evaluationMeasure : evaluationMeasuresOrder) {

                addTitleText(allCasesAggregateResults, column, titlesRow, evaluationMeasure);
                for (int index = 0; index < groupCaseStudyResults.size(); index++) {
                    int row = index + 1;
                    GroupCaseStudyResult groupCaseStudyResult = groupCaseStudyResults.get(index);
                    if (groupCaseStudyResult.getDefinedEvaluationMeasures().contains(evaluationMeasure)) {
                        Object evaluationMeasureValue = groupCaseStudyResult.getEvaluationMeasureValue(evaluationMeasure);
                        setCellContent(allCasesAggregateResults, column, row, evaluationMeasureValue);
                    }
                }
                column++;
            }
        }

        autoSizeColumns(allCasesAggregateResults);

    }

    public static void writeEvaluationMeasureSpecificSheet(List<GroupCaseStudyResult> groupCaseStudyResults, List<String> dataValidationParametersOrder, List<String> techniqueParametersOrder, String evaluationMeasure, WritableWorkbook workbook) throws WriteException, IOException {

        List<GroupCaseStudy> groupCaseStudys = groupCaseStudyResults.stream().map(groupCaseStudyResult -> groupCaseStudyResult.getGroupCaseStudy()).collect(Collectors.toList());

        Set<GroupCaseStudyResult> dataValidationAliases = new TreeSet<>(
                GroupCaseStudyResult.dataValidationComparator);
        Set<GroupCaseStudyResult> techniqueAliases = new TreeSet<>(
                GroupCaseStudyResult.techniqueComparator);

        dataValidationAliases.addAll(groupCaseStudyResults);
        techniqueAliases.addAll(groupCaseStudyResults);

        List<ParameterChain> differentChainsWithAliases = ParameterChain.obtainDifferentChains(groupCaseStudys);

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

        writeRowAndColumnCombination(techniqueDifferentChains, groupCaseStudys, dataValidationDifferentChains, evaluationMeasure, groupCaseStudyResults, workbook);
    }

    public static boolean isOnlyOneColumn(List<GroupCaseStudyResult> groupCaseStudyResults) {
        List<GroupCaseStudy> groupCaseStudys = groupCaseStudyResults.stream().map(groupCaseStudyResult -> groupCaseStudyResult.getGroupCaseStudy()).collect(Collectors.toList());

        List<ParameterChain> differentChainsWithAliases = ParameterChain.obtainDifferentChains(groupCaseStudys);

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

    public static void writeEvaluationMeasureSpecificFileParameter(List<GroupCaseStudyResult> groupCaseStudyResults, List<String> dataValidationParametersOrder, List<String> techniqueParametersOrder, String evaluationMeasure, WritableWorkbook workbook) throws WriteException, IOException {

        List<GroupCaseStudy> groupCaseStudys = groupCaseStudyResults.stream().map(groupCaseStudyResult -> groupCaseStudyResult.getGroupCaseStudy()).collect(Collectors.toList());

        Set<GroupCaseStudyResult> dataValidationAliases = new TreeSet<>(
                GroupCaseStudyResult.dataValidationComparator);
        Set<GroupCaseStudyResult> techniqueAliases = new TreeSet<>(
                GroupCaseStudyResult.techniqueComparator);

        dataValidationAliases.addAll(groupCaseStudyResults);
        techniqueAliases.addAll(groupCaseStudyResults);

        List<ParameterChain> differentChainsWithAliases = ParameterChain.obtainDifferentChains(groupCaseStudys);

        List<ParameterChain> differentChains = differentChainsWithAliases.stream()
                .filter(chain -> !chain.isAlias())
                .filter(chain -> !chain.isSeed())
                .collect(Collectors.toList());

        List<ParameterChain> dataValidationDifferentChains = differentChains.stream()
                .filter(chain -> chain.isDataValidationParameter())
                .filter(chain -> !chain.isNumExecutions())
                .collect(Collectors.toList());
        List<ParameterChain> techniqueDifferentChains = differentChains.stream().filter(chain -> chain.isTechniqueParameter()).collect(Collectors.toList());

        writeRowAndColumnCombination(techniqueDifferentChains, groupCaseStudys, dataValidationDifferentChains, evaluationMeasure, groupCaseStudyResults, workbook);
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

    public static CaseStudyResultMatrix prepareExcelMatrix(List<ParameterChain> rowChains, List<ParameterChain> columnChains, String evaluationMeasure, List<GroupCaseStudyResult> groupCaseStudyResults) {
        CaseStudyResultMatrix matrix = new CaseStudyResultMatrix(rowChains, columnChains, evaluationMeasure);
        matrix.prepareColumnAndRowNames(groupCaseStudyResults);
        List<GroupCaseStudyResult> groupCaseStudyResultsMaxNumExecutions = new ArrayList<>();
        for (String rowName : matrix.getRowNames()) {
            for (String columnName : matrix.getColumnNames()) {

                List<GroupCaseStudyResult> thisCellGroupCaseStudys = groupCaseStudyResults.stream()
                        .filter(groupCaseStudyResult -> matrix.getRow((ParameterOwner) groupCaseStudyResult.getGroupCaseStudy()).equals(rowName))
                        .filter(groupCaseStudyResult -> matrix.getColumn((ParameterOwner) groupCaseStudyResult.getGroupCaseStudy()).equals(columnName))
                        .sorted(((groupCaseStudyResult1, groupCaseStudyResult2) -> Integer.compare(groupCaseStudyResult1.getNumExecutions(), groupCaseStudyResult2.getNumExecutions())))
                        .collect(Collectors.toList());
                Collections.reverse(thisCellGroupCaseStudys);

                if (thisCellGroupCaseStudys.isEmpty()) {
                    continue;
                }

                if (thisCellGroupCaseStudys.size() == 1) {
                    groupCaseStudyResultsMaxNumExecutions.addAll(thisCellGroupCaseStudys);
                } else {

                    if (Global.isVerboseAnnoying()) {
                        String row = matrix.getRow(thisCellGroupCaseStudys.get(0).getGroupCaseStudy());
                        String column = matrix.getColumn(thisCellGroupCaseStudys.get(0).getGroupCaseStudy());
                        Global.show("Executions for cell (" + row + "," + column + ")\n");
                        thisCellGroupCaseStudys.stream().forEach((groupCaseStudyResultsMaxNumExecution) -> {
                            Global.show(groupCaseStudyResultsMaxNumExecution.getNumExecutions() + "\n");
                        });
                    }

                    groupCaseStudyResultsMaxNumExecutions.add(thisCellGroupCaseStudys.get(0));
                }
            }
        }
        groupCaseStudyResultsMaxNumExecutions.stream().forEach(groupCaseStudyResult -> {
            java.lang.Number evaluationMeasureValue = groupCaseStudyResult.getEvaluationMeasureValue(evaluationMeasure);
            matrix.addValue(groupCaseStudyResult.getGroupCaseStudy(), evaluationMeasureValue);
        });
        return matrix;
    }

    public static void writeNumExecutionsSheet(List<GroupCaseStudyResult> groupCaseStudyResults, List<String> dataValidationParametersOrder, List<String> techniqueParametersOrder, List<String> evaluationMeasuresOrder, WritableWorkbook workbook) throws WriteException {

        List<GroupCaseStudy> groupCaseStudys = groupCaseStudyResults.stream().map(groupCaseStudyResult -> groupCaseStudyResult.getGroupCaseStudy()).collect(Collectors.toList());

        Set<GroupCaseStudyResult> dataValidationAliases = new TreeSet<>(
                GroupCaseStudyResult.dataValidationComparator);
        Set<GroupCaseStudyResult> techniqueAliases = new TreeSet<>(
                GroupCaseStudyResult.techniqueComparator);

        dataValidationAliases.addAll(groupCaseStudyResults);
        techniqueAliases.addAll(groupCaseStudyResults);

        Map<String, List<GroupCaseStudy>> byCaseStudy = groupCaseStudys.stream().collect(Collectors
                .groupingBy(groupCaseStudy -> groupCaseStudy.getAlias()));

        groupCaseStudys = byCaseStudy.values().parallelStream().map(sameCaseStudyWithDifferentNumExecutions -> {
            Map<Integer, List<GroupCaseStudy>> byNumExecutions = sameCaseStudyWithDifferentNumExecutions
                    .parallelStream()
                    .collect(Collectors.groupingBy(groupCaseStudy -> groupCaseStudy.getNumExecutions()));

            int maxExec = byNumExecutions.keySet().stream().mapToInt(numExec -> numExec)
                    .max().orElse(-1);

            if (byNumExecutions.get(maxExec).size() > 1) {
                byNumExecutions.get(maxExec).forEach(groupCaseStudy -> Global.showWarning(groupCaseStudy.getAlias() + "\""));
                throw new IllegalStateException("More than one execution with the maximum! (" + byNumExecutions.get(maxExec).size() + ")");
            }

            return byNumExecutions.get(maxExec).get(0);
        }).collect(Collectors.toList());

        Global.showInfoMessage("Processing " + groupCaseStudys.size() + " different results files.\n");

        List<ParameterChain> differentChainsWithAliasesAndSeed = ParameterChain.obtainDifferentChains(groupCaseStudys);

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
            ParameterChain grsAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);
            techniqueDifferentChains.add(grsAliasChain);
        }
        if (dataValidationDifferentChains.isEmpty()) {
            ParameterChain datasetLoaderAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.DATASET_LOADER, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            ParameterChain groupFormationTechniqueAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.GROUP_FORMATION_TECHNIQUE, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            dataValidationDifferentChains.add(datasetLoaderAliasChain);
            dataValidationDifferentChains.add(groupFormationTechniqueAliasChain);
        }

        CaseStudyResultMatrix matrix = getNumExecutionsMatrix(techniqueDifferentChains, dataValidationDifferentChains, groupCaseStudyResults);

        writeMatrixInSheet(workbook, GroupCaseStudy.NUM_EXECUTIONS.getName(), matrix, GroupCaseStudy.NUM_EXECUTIONS.getName());
    }

    public static CaseStudyResultMatrix getNumExecutionsMatrix(List<ParameterChain> techniqueDifferentChains, List<ParameterChain> dataValidationDifferentChains, List<GroupCaseStudyResult> groupCaseStudyResults) {
        CaseStudyResultMatrix matrix = new CaseStudyResultMatrix(techniqueDifferentChains, dataValidationDifferentChains, GroupCaseStudy.NUM_EXECUTIONS.getName());
        groupCaseStudyResults.stream().forEach(groupCaseStudyResult -> {
            int numExecutions = groupCaseStudyResult.getNumExecutions();
            java.lang.Number existingNumExecutions = matrix.getValue(groupCaseStudyResult.getGroupCaseStudy());

            if (existingNumExecutions == null) {
                matrix.addValue(groupCaseStudyResult.getGroupCaseStudy(), numExecutions);
            } else if (numExecutions > existingNumExecutions.intValue()) {
                matrix.addValue(groupCaseStudyResult.getGroupCaseStudy(), numExecutions);
            }
        });
        return matrix;
    }

    public static void writeEvaluationMeasureParameterCombinationsSheets(List<GroupCaseStudyResult> groupCaseStudyResults, List<String> dataValidationParametersOrder, List<String> techniqueParametersOrder, String evaluationMeasure, WritableWorkbook workbook) throws WriteException {
        List<GroupCaseStudy> groupCaseStudys = groupCaseStudyResults.stream().map(groupCaseStudyResult -> groupCaseStudyResult.getGroupCaseStudy()).collect(Collectors.toList());

        Set<GroupCaseStudyResult> dataValidationAliases = new TreeSet<>(
                GroupCaseStudyResult.dataValidationComparator);
        Set<GroupCaseStudyResult> techniqueAliases = new TreeSet<>(
                GroupCaseStudyResult.techniqueComparator);

        dataValidationAliases.addAll(groupCaseStudyResults);
        techniqueAliases.addAll(groupCaseStudyResults);

        List<ParameterChain> differentChainsWithAliases = ParameterChain
                .obtainDifferentChains(groupCaseStudys);

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
                        groupCaseStudys,
                        columnList,
                        evaluationMeasure,
                        groupCaseStudyResults,
                        workbook,
                        sheetName
                );
            } catch (WriteException ex) {
                Logger.getLogger(GroupCaseStudyExcel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

    }

    public static void writeRowAndColumnCombination(List<ParameterChain> rowChains, List<GroupCaseStudy> groupCaseStudys, List<ParameterChain> columnChains, String evaluationMeasure, List<GroupCaseStudyResult> groupCaseStudyResults, WritableWorkbook workbook) throws WriteException {
        writeRowAndColumnCombination(rowChains, groupCaseStudys, columnChains, evaluationMeasure, groupCaseStudyResults, workbook, evaluationMeasure);
    }

    public static void writeRowAndColumnCombination(
            List<ParameterChain> rowChains,
            List<GroupCaseStudy> groupCaseStudys,
            List<ParameterChain> columnChains,
            String evaluationMeasure,
            List<GroupCaseStudyResult> groupCaseStudyResults,
            WritableWorkbook workbook,
            String sheetName) throws WriteException {
        if (rowChains.isEmpty()) {
            ParameterChain grsAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.GROUP_RECOMMENDER_SYSTEM, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);
            rowChains.add(grsAliasChain);
        }
        if (columnChains.isEmpty()) {
            ParameterChain datasetLoaderAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.DATASET_LOADER, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            ParameterChain groupFormationTechniqueAliasChain = new ParameterChain(groupCaseStudys.get(0))
                    .createWithNode(GroupCaseStudy.GROUP_FORMATION_TECHNIQUE, null)
                    .createWithLeaf(ParameterOwner.ALIAS, null);

            columnChains.add(datasetLoaderAliasChain);
            columnChains.add(groupFormationTechniqueAliasChain);
        }

        CaseStudyResultMatrix matrix = prepareExcelMatrix(
                rowChains,
                columnChains,
                evaluationMeasure,
                groupCaseStudyResults);

        writeMatrixInSheet(workbook, evaluationMeasure, matrix, sheetName);
    }

    public static final Comparator<Set<ParameterChain>> COMPARATOR_OF_SET_OF_CHAINS = (s1, s2) -> {

        List<ParameterChain> l1 = new ArrayList<>(s1);
        List<ParameterChain> l2 = new ArrayList<>(s2);

        for (int index = 0; index < Math.min(l1.size(), l2.size()); index++) {

            ParameterChain e1 = l1.get(index);
            ParameterChain e2 = l2.get(index);

            int compare = e1.compareTo(e2);

            if (compare != 0) {
                return compare;
            }
        }
        return Integer.compare(l1.size(), l2.size());
    };

    public static Set<Combination> obtainDifferentParameterInCollumn(List<ParameterChain> chains) {

        Set<Combination> allCombinations = chains.parallelStream().map(chain -> {
            List<ParameterChain> chainsWithoutMe = new ArrayList<>(chains);
            chainsWithoutMe.remove(chain);

            return new Combination(chainsWithoutMe, Arrays.asList(chain));
        }).collect(Collectors.toSet());

        return allCombinations;

    }

    private static Set<Combination> obtainAllTwoPartitions(List<ParameterChain> chains) {
        List<ParameterChain> sortedChains = chains.parallelStream().sorted().collect(Collectors.toList());

        if (sortedChains.isEmpty() || sortedChains.size() == 1) {
            return Collections.EMPTY_SET;
        } else if (sortedChains.size() == 2) {

            ParameterChain element1 = chains.get(0);
            ParameterChain element2 = chains.get(1);

            Combination combination = new Combination(
                    Arrays.asList(element1),
                    Arrays.asList(element2));

            return Arrays.asList(combination).stream().collect(Collectors.toSet());
        } else {
            Set<Combination> allCombinations = chains.parallelStream().flatMap(chain -> {
                List<ParameterChain> chainsWithoutMe = new ArrayList<>(chains);
                chainsWithoutMe.remove(chain);

                Set<Combination> obtainCombinationsWithoutMe = obtainAllTwoPartitions(chainsWithoutMe);
                Set<Combination> combinationsThisChain = obtainCombinationsWithoutMe.parallelStream()
                        .flatMap(combinationWithoutMe -> {
                            Set<Combination> combinationsThisCombination = new HashSet<>();

                            Set<ParameterChain> rowWithMe = combinationWithoutMe.row.stream()
                                    .collect(Collectors.toCollection(TreeSet::new));
                            rowWithMe.add(chain);

                            Set<ParameterChain> columnWithMe = combinationWithoutMe.column.stream()
                                    .collect(Collectors.toCollection(TreeSet::new));
                            columnWithMe.add(chain);

                            combinationsThisCombination.add(new Combination(combinationWithoutMe.row, columnWithMe));
                            combinationsThisCombination.add(new Combination(rowWithMe, combinationWithoutMe.column));

                            return combinationsThisCombination.parallelStream();
                        })
                        .collect(Collectors.toCollection(TreeSet::new));

                combinationsThisChain.add(new Combination(chainsWithoutMe, Arrays.asList(chain)));
                return combinationsThisChain.parallelStream();
            })
                    .collect(Collectors.toCollection(TreeSet::new));
            return allCombinations;
        }
    }

    public static class Combination implements Comparable<Combination> {

        Set<ParameterChain> row;
        Set<ParameterChain> column;

        public Combination(List<ParameterChain> row, List<ParameterChain> column) {
            this.row = row.parallelStream().sorted().collect(Collectors.toSet());
            this.column = column.parallelStream().sorted().collect(Collectors.toSet());

            if (this.row.size() < this.column.size()) {
                Set<ParameterChain> aux = this.row;
                this.row = this.column;
                this.column = aux;
            }
        }

        public Combination(Set<ParameterChain> row, Set<ParameterChain> column) {
            this.row = row.parallelStream().sorted().collect(Collectors.toSet());
            this.column = column.parallelStream().sorted().collect(Collectors.toSet());

            if (this.row.size() < this.column.size()) {
                Set<ParameterChain> aux = this.row;
                this.row = this.column;
                this.column = aux;
            }
        }

        @Override
        public String toString() {
            List<String> rowIDs = row.stream().map(chain -> chain.getParameterName()).sorted().collect(Collectors.toList());
            List<String> columnIDs = column.stream().map(chain -> chain.getParameterName()).sorted().collect(Collectors.toList());
            return "[row={" + rowIDs.toString()
                    + "}, column={" + columnIDs.toString() + "}]";
        }

        public boolean isEmpty() {
            return row.isEmpty() || column.isEmpty();
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof Combination) {
                Combination combination = (Combination) obj;

                if (COMPARATOR_OF_SET_OF_CHAINS.compare(this.row, combination.row) == 0) {
                    return true;
                } else if (COMPARATOR_OF_SET_OF_CHAINS.compare(this.row, combination.column) == 0) {
                    return true;
                } else if (COMPARATOR_OF_SET_OF_CHAINS.compare(this.column, combination.row) == 0) {
                    return true;
                } else {
                    return false;
                }
            } else {
                throw new IllegalStateException("arg");
            }
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 13 * hash + Objects.hashCode(this.row.hashCode());
            hash = 13 * hash + Objects.hashCode(this.column.hashCode());
            return hash;
        }

        @Override
        public int compareTo(Combination o) {
            int compareRow = COMPARATOR_OF_SET_OF_CHAINS.compare(this.row, o.row);
            int compareColumn = COMPARATOR_OF_SET_OF_CHAINS.compare(this.column, o.column);

            if (compareRow == 0 && compareColumn == 0) {
                return 0;
            } else if (compareRow == 0) {
                return compareColumn;
            } else {
                return compareRow;
            }
        }

    }

}
