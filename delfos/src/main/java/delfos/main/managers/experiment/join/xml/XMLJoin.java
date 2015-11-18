package delfos.main.managers.experiment.join.xml;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.FileUtilities;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
            manageCaseUse(resultsPaths, outputFile);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);

            consoleParameters.printUnusedParameters(System.err);
        }
    }

    public static void manageCaseUse(List<String> resultsPaths, File outputFile) {

        AggregateResultsXML aggregateResultsXML = new AggregateResultsXML();

        List<File> allFiles = new LinkedList<>();

        resultsPaths.stream()
                .map((path) -> new File(path))
                .forEach((pathFile) -> {
                    allFiles.addAll(FileUtilities.findInDirectory(pathFile));
                });

        List<File> relevantFiles = aggregateResultsXML.filterResultsFiles(allFiles);

        System.out.println("Detected " + relevantFiles.size() + " results files");

        if (relevantFiles.isEmpty()) {
            return;
        }

        aggregateResultsXML.joinAndWrite(relevantFiles, outputFile);

        System.out.println("Finished parsing " + relevantFiles.size() + " results files.");

        newXMLJoined(relevantFiles, outputFile);

    }

    public static void newXMLJoined(List<File> relevantFiles, File outputFile) {
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

        File newOutput = FileUtilities.addSufix(outputFile, "-completeTable");
        GroupCaseStudyExcel.writeGeneralFile(groupCaseStudyResults, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasuresOrder, newOutput);

        for (String evaluationMeasure : evaluationMeasuresOrder) {
            File measureOutput = FileUtilities.addSufix(outputFile, "-" + evaluationMeasure);
            GroupCaseStudyExcel.writeEvaluationMeasureSpecificFile(groupCaseStudyResults, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasure, measureOutput);
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
}
