package delfos.main.managers.experiment.join.xml;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.jdom2.JDOMException;

/**
 *
 * Case use to joinAndWrite many excel of many experiment in a single one.
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class XMLJoin extends CaseUseMode {

    public static final String MODE_PARAMETER = "--xml-join";
    public static final String RESULTS_PATH_PARAMETER = "-results";

    public static final String OUTPUT_FILE_PARAMETER = "-o";

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    private static class Holder {

        private static final XMLJoin INSTANCE = new XMLJoin();
    }

    public static XMLJoin getInstance() {
        return Holder.INSTANCE;
    }

    private XMLJoin() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        try {
            List<String> resultsPaths = consoleParameters.getValues(RESULTS_PATH_PARAMETER);

            File outputFile;
            if (consoleParameters.isParameterDefined(OUTPUT_FILE_PARAMETER)) {
                outputFile = new File(consoleParameters.getValue(OUTPUT_FILE_PARAMETER));
            } else {
                String firstPath = resultsPaths.get(0);

                if (firstPath.endsWith(File.separator)) {
                    firstPath = firstPath.substring(0, firstPath.length() - 1);
                }

                String firstPathCleaned = firstPath
                        .replace(File.separatorChar, '.');

                outputFile = new File(firstPathCleaned + ".xls");
            }

            consoleParameters.printUnusedParameters(System.err);
            mergeResultsIntoOutput(resultsPaths, outputFile);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);

            consoleParameters.printUnusedParameters(System.err);
        }
    }

    public static void mergeResultsIntoOutput(List<String> resultsPaths, File outputFile) {

        validateResultsPaths(resultsPaths);

        AggregateResultsXML aggregateResultsXML = new AggregateResultsXML();

        List<File> allFiles = new LinkedList<>();

        resultsPaths.stream()
                .map((path) -> new File(path))
                .forEach((directory) -> {
                    if (!directory.exists()) {
                        FileNotFoundException notFound = new FileNotFoundException("Directory '" + directory + "' does not exists [" + directory.getAbsolutePath() + "]");
                        ERROR_CODES.CANNOT_READ_FILE.exit(notFound);
                    }
                    allFiles.addAll(FileUtilities.findInDirectory(directory));
                });

        List<File> relevantFiles = aggregateResultsXML.filterResultsFiles(allFiles);

        if (relevantFiles.isEmpty()) {
            return;
        }

        if (Global.isInfoPrinted()) {
            for (int i = 0; i < relevantFiles.size(); i++) {
                File relevantFile = relevantFiles.get(i);
                Global.showMessageTimestamped(("(" + i + " of " + relevantFiles.size()) + "): " + relevantFile.getAbsolutePath() + "\n");
            }

        }

        writeJoinIntoSpreadsheet(relevantFiles, outputFile);

        System.out.println("Finished parsing " + relevantFiles.size() + " results files.");

    }

    public static void validateResultsPaths(List<String> resultsPaths) throws IllegalArgumentException {
        for (String resultPath : resultsPaths) {
            File resultDirectory = new File(resultPath);

            if (!resultDirectory.exists()) {
                throw new IllegalArgumentException("The directory '" + resultPath + "' does not exist [" + resultDirectory.getAbsolutePath() + "]");
            }
            if (!resultDirectory.isDirectory()) {
                throw new IllegalArgumentException("The path '" + resultPath + "' is not a directory[" + resultDirectory.getAbsolutePath() + "]");
            }
        }
    }

    public static void writeJoinIntoSpreadsheet(List<File> relevantFiles, File outputSpreadsheetFile) {
        List<GroupCaseStudy> groupCaseStudies = relevantFiles.stream().map(file -> {
            try {
                return GroupCaseStudyXML.loadGroupCaseWithResults(file);
            } catch (JDOMException | IOException ex) {
                Logger.getLogger(XMLJoin.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        })
                .filter(groupCaseStudyConfiguration -> groupCaseStudyConfiguration != null)
                .map(groupCaseStudyConfiguration -> groupCaseStudyConfiguration.createGroupCaseStudy())
                .collect(Collectors.toList());

        List<GroupCaseStudyResult> groupCaseStudyResults = groupCaseStudies.stream().map(groupCaseStudy -> new GroupCaseStudyResult(groupCaseStudy)).collect(Collectors.toList());

        List<String> dataValidationParametersOrder = obtainDataValidationParametersOrder(groupCaseStudyResults);
        List<String> techniqueParametersOrder = obtainTechniqueParametersOrder(groupCaseStudyResults);
        List<String> evaluationMeasuresOrder = obtainEvaluationMeasuresOrder(groupCaseStudyResults);

        WorkbookSettings wbSettings = new WorkbookSettings();

        wbSettings.setLocale(new Locale("en", "EN"));

        WritableWorkbook workbook;

        if (outputSpreadsheetFile.exists()) {
            outputSpreadsheetFile.delete();
        }

        try {
            workbook = Workbook.createWorkbook(outputSpreadsheetFile, wbSettings);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(new FileNotFoundException("Cannot access file " + outputSpreadsheetFile.getAbsolutePath() + "."));
            return;
        }

        try {
            GroupCaseStudyExcel.writeGeneralSheet(
                    groupCaseStudyResults,
                    dataValidationParametersOrder,
                    techniqueParametersOrder,
                    evaluationMeasuresOrder,
                    workbook);
        } catch (WriteException | IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }

        for (String evaluationMeasure : evaluationMeasuresOrder) {
            try {
                GroupCaseStudyExcel.writeEvaluationMeasureSpecificFile(
                        groupCaseStudyResults,
                        dataValidationParametersOrder,
                        techniqueParametersOrder,
                        evaluationMeasure,
                        workbook);
            } catch (WriteException | IOException ex) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
            }
        }

        try {
            workbook.write();
            workbook.close();
        } catch (IOException | WriteException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
    }

    private static List<String> obtainDataValidationParametersOrder(List<GroupCaseStudyResult> groupCaseStudyResults) {
        Set<String> commonParameters = new TreeSet<>();

        for (GroupCaseStudyResult groupCaseStudyResult : groupCaseStudyResults) {
            commonParameters.addAll(groupCaseStudyResult.getDefinedDataValidationParameters());
        }

        List<String> commonParametersOrder = commonParameters.stream().sorted().collect(Collectors.toList());
        return commonParametersOrder;
    }

    private static List<String> obtainTechniqueParametersOrder(List<GroupCaseStudyResult> groupCaseStudyResults) {
        Set<String> commonParameters = new TreeSet<>();

        for (GroupCaseStudyResult groupCaseStudyResult : groupCaseStudyResults) {
            commonParameters.addAll(groupCaseStudyResult.getDefinedTechniqueParameters());
        }

        List<String> commonParametersOrder = commonParameters.stream().sorted().collect(Collectors.toList());
        return commonParametersOrder;
    }

    private static List<String> obtainEvaluationMeasuresOrder(List<GroupCaseStudyResult> groupCaseStudyResults) {
        Set<String> commonEvaluationMeasures = new TreeSet<>();

        for (GroupCaseStudyResult groupCaseStudyResult : groupCaseStudyResults) {
            commonEvaluationMeasures.addAll(groupCaseStudyResult.getDefinedEvaluationMeasures());
        }

        List<String> evaluationMeasuresOrder = commonEvaluationMeasures.stream().sorted().collect(Collectors.toList());
        return evaluationMeasuresOrder;
    }

    public static void joinDirectory(File directory) {
        File outputFile = new File(directory.getPath() + File.separator + "joined-results");
        XMLJoin.mergeResultsIntoOutput(Arrays.asList(directory.getPath()), outputFile);
    }
}
