/*
 * Copyright (C) 2017 jcastro
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
package delfos.main.managers.experiment.join.xml;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.StringsOrderings;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import org.jdom2.JDOMException;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class GroupCaseStudyXMLJoin {

    public static void writeJoinIntoSpreadsheet(List<File> relevantFiles, File outputSpreadsheetFile, Set<String> filterMeasures) {
        List<GroupCaseStudy> groupCaseStudies = relevantFiles.parallelStream().map((File file) -> {
            try {
                return GroupCaseStudyXML.loadGroupCaseWithResults(file);
            } catch (JDOMException | IOException ex) {
                Logger.getLogger(XMLJoin.class.getName()).log(Level.SEVERE, null, ex);
                return null;
            }
        }).filter((GroupCaseStudyConfiguration groupCaseStudyConfiguration) -> groupCaseStudyConfiguration != null).map((GroupCaseStudyConfiguration groupCaseStudyConfiguration) -> groupCaseStudyConfiguration.createGroupCaseStudy()).collect(Collectors.toList());
        List<GroupCaseStudyResult> groupCaseStudyResults = groupCaseStudies.parallelStream().map((GroupCaseStudy groupCaseStudy) -> new GroupCaseStudyResult(groupCaseStudy)).collect(Collectors.toList());
        List<String> dataValidationParametersOrder = obtainDataValidationParametersOrder(groupCaseStudyResults);
        List<String> techniqueParametersOrder = obtainTechniqueParametersOrder(groupCaseStudyResults);
        List<String> evaluationMeasuresOrder = obtainEvaluationMeasuresOrder(groupCaseStudyResults);
        checkEvaluationMeasuresFilterArePresent(evaluationMeasuresOrder, filterMeasures);
        evaluationMeasuresOrder = filterMeasures.isEmpty() ? evaluationMeasuresOrder : evaluationMeasuresOrder.stream().filter((String measure) -> filterMeasures.contains(measure)).collect(Collectors.toList());
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
            GroupCaseStudyExcel.writeGeneralSheet(groupCaseStudyResults, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasuresOrder, workbook);
        } catch (WriteException | IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
        try {
            GroupCaseStudyExcel.writeNumExecutionsSheet(groupCaseStudyResults, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasuresOrder, workbook);
        } catch (WriteException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
        evaluationMeasuresOrder.parallelStream().forEach((String evaluationMeasure) -> {
            try {
                GroupCaseStudyExcel.writeEvaluationMeasureSpecificSheet(groupCaseStudyResults, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasure, workbook);
            } catch (WriteException | IOException ex) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
            }
        });
        if (GroupCaseStudyExcel.isOnlyOneColumn(groupCaseStudyResults)) {
            evaluationMeasuresOrder.parallelStream().forEach((String evaluationMeasure) -> {
                try {
                    GroupCaseStudyExcel.writeEvaluationMeasureParameterCombinationsSheets(groupCaseStudyResults, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasure, workbook);
                } catch (WriteException ex) {
                    ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
                }
            });
        }
        sortSheets(workbook);
        try {
            workbook.write();
            workbook.close();
        } catch (IOException | WriteException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        } catch (Exception ex) {
            ex.printStackTrace(System.err);
            System.out.println("Some strange thing happened now...");
        }
    }

    private static List<String> obtainDataValidationParametersOrder(List<GroupCaseStudyResult> groupCaseStudyResults) {
        List<String> commonParametersOrder
                = groupCaseStudyResults.parallelStream()
                .map((groupCaseStudyResult) -> (groupCaseStudyResult.getDefinedDataValidationParameters()))
                .flatMap(dataValidationParameters -> dataValidationParameters.parallelStream())
                .collect(Collectors.toSet())
                .parallelStream()
                .sorted()
                .collect(Collectors.toList());

        return commonParametersOrder;
    }

    private static List<String> obtainTechniqueParametersOrder(List<GroupCaseStudyResult> groupCaseStudyResults) {

        List<String> commonParametersOrder
                = groupCaseStudyResults.parallelStream()
                .map((groupCaseStudyResult) -> (groupCaseStudyResult.getDefinedTechniqueParameters()))
                .flatMap(dataValidationParameters -> dataValidationParameters.parallelStream())
                .collect(Collectors.toSet())
                .parallelStream()
                .sorted()
                .collect(Collectors.toList());

        return commonParametersOrder;
    }

    private static List<String> obtainEvaluationMeasuresOrder(List<GroupCaseStudyResult> groupCaseStudyResults) {
        Set<String> commonEvaluationMeasures = groupCaseStudyResults.parallelStream()
                .flatMap(groupCaseStudyResult -> groupCaseStudyResult.getDefinedEvaluationMeasures().parallelStream())
                .collect(Collectors.toSet());

        List<String> evaluationMeasuresOrder = commonEvaluationMeasures.stream().sorted().collect(Collectors.toList());
        return evaluationMeasuresOrder;
    }

    private static void checkEvaluationMeasuresFilterArePresent(List<String> evaluationMeasuresOrder, Set<String> filterMeasures) {
        Set<String> measuresPresent = evaluationMeasuresOrder.stream().collect(Collectors.toSet());

        filterMeasures.stream()
                .filter(filteredMeasure -> !measuresPresent.contains(filteredMeasure))
                .forEach(filteredMeasureNotPresent -> {
                    Global.showWarning("Evaluation measure " + filteredMeasureNotPresent + " is not present in the case studies.");
                });
    }

    private static void sortSheets(WritableWorkbook workbook) {

        List<String> sheetsNames = IntStream.range(0, workbook.getNumberOfSheets()).boxed().map(sheetNumber -> {
            final WritableSheet sheet = workbook.getSheet(sheetNumber);
            return sheet.getName();
        }).collect(Collectors.toList());

        sheetsNames.remove("AllCasesAggregateResults");
        sheetsNames.remove("numExecutions");

        List<String> sortedSheets = sheetsNames.stream().sorted(StringsOrderings.getNaturalComparator()).collect(Collectors.toList());

        sortedSheets.add(0, "AllCasesAggregateResults");
        sortedSheets.add(1, "numExecutions");

        for (int i = 0; i < sortedSheets.size(); i++) {
            String sheetName = sortedSheets.get(i);

            int fromIndex = IntStream.range(0, workbook.getNumberOfSheets()).boxed().filter(sheetNumber -> {
                WritableSheet sheet = workbook.getSheet(sheetNumber);
                return sheet.getName().equals(sheetName);
            }).findAny().orElse(-1);

            int toIndex = i;

            if (fromIndex == -1 || toIndex == -1) {
                throw new IllegalStateException("The sheets names in the original and sorted vector do not match.");
            }

            workbook.moveSheet(fromIndex, toIndex);
        }
    }

    public static void joinDirectory(File directory, Set<String> filterMeasures) {
        File outputFile = new File(directory.getPath() + File.separator + "joined-results");
        XMLJoin.mergeResultsIntoOutput(Arrays.asList(directory.getPath()), outputFile, filterMeasures);
    }
}
