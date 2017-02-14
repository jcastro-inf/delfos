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
import delfos.experiment.casestudy.CaseStudyResults;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.xml.casestudy.CaseStudyXML;
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
public class CaseStudyXMLJoin {

    public static void writeJoinIntoSpreadsheet(
            List<File> relevantFiles,
            File outputSpreadsheetFile,
            Set<String> filterMeasures) {

        List<CaseStudyResults> caseStudyResultss = relevantFiles.parallelStream()
                .map((File file) -> {
                    try {
                        return CaseStudyXML.loadCaseResults(file);
                    } catch (JDOMException | IOException ex) {
                        Logger.getLogger(XMLJoin.class.getName()).log(Level.SEVERE, null, ex);
                        return null;
                    }
                })
                .filter(caseStudyResults -> caseStudyResults != null)
                .collect(Collectors.toList());

        List<String> dataValidationParametersOrder = obtainDataValidationParametersOrder(caseStudyResultss);
        List<String> techniqueParametersOrder = obtainTechniqueParametersOrder(caseStudyResultss);
        List<String> evaluationMeasuresOrder = obtainEvaluationMeasuresOrder(caseStudyResultss);
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
            CaseStudyExcel.writeGeneralSheet(caseStudyResultss, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasuresOrder, workbook);
        } catch (WriteException ex) {
            Logger.getLogger(CaseStudyXMLJoin.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CaseStudyXMLJoin.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            CaseStudyExcel.writeNumExecutionsSheet(caseStudyResultss, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasuresOrder, workbook);
        } catch (WriteException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
        evaluationMeasuresOrder.parallelStream().forEach((String evaluationMeasure) -> {
            try {
                CaseStudyExcel.writeEvaluationMeasureSpecificSheet(caseStudyResultss, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasure, workbook);
            } catch (WriteException | IOException ex) {
                ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
            }
        });
        if (CaseStudyExcel.isOnlyOneColumn(caseStudyResultss)) {
            evaluationMeasuresOrder.parallelStream().forEach((String evaluationMeasure) -> {
                try {
                    CaseStudyExcel.writeEvaluationMeasureParameterCombinationsSheets(caseStudyResultss, dataValidationParametersOrder, techniqueParametersOrder, evaluationMeasure, workbook);
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

    private static List<String> obtainDataValidationParametersOrder(List<CaseStudyResults> caseStudyResultses) {
        List<String> commonParametersOrder
                = caseStudyResultses.parallelStream()
                .map((CaseStudyResult) -> (CaseStudyResult.getDefinedDataValidationParameters()))
                .flatMap(dataValidationParameters -> dataValidationParameters.parallelStream())
                .collect(Collectors.toSet())
                .parallelStream()
                .sorted()
                .collect(Collectors.toList());

        return commonParametersOrder;
    }

    private static List<String> obtainTechniqueParametersOrder(List<CaseStudyResults> caseStudyResultses) {

        List<String> commonParametersOrder
                = caseStudyResultses.parallelStream()
                .map((CaseStudyResult) -> (CaseStudyResult.getDefinedTechniqueParameters()))
                .flatMap(dataValidationParameters -> dataValidationParameters.parallelStream())
                .collect(Collectors.toSet())
                .parallelStream()
                .sorted()
                .collect(Collectors.toList());

        return commonParametersOrder;
    }

    private static List<String> obtainEvaluationMeasuresOrder(List<CaseStudyResults> caseStudyResultses) {
        Set<String> commonEvaluationMeasures = caseStudyResultses.parallelStream()
                .flatMap(CaseStudyResult -> CaseStudyResult.getDefinedEvaluationMeasures().parallelStream())
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
