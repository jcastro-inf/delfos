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
package delfos.main.managers.experiment.join.xml;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 *
 * Case use to joinAndWrite many excel of many experiment in a single one.
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class XMLJoin extends CaseUseMode {

    public static final String MODE_PARAMETER = "--xml-join";
    public static final String RESULTS_PATH_PARAMETER = "-results";

    public static final String OUTPUT_FILE_PARAMETER = "-o";

    public static final String MEASURES_PARAMETER = "-measures";

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

        List<String> resultsPaths = getInputs(consoleParameters);

        File outputFile = getOutputFile(consoleParameters, resultsPaths);

        Set<String> filterMeasures = getFilterMeasures(consoleParameters);

        consoleParameters.printUnusedParameters(System.err);

        Global.showMessage("Writing file:  " + outputFile.getAbsolutePath() + "\n");
        mergeResultsIntoOutput(resultsPaths, outputFile, filterMeasures);

        Global.showMessage("\t\tFinished file: " + outputFile.getAbsolutePath() + "\n");

    }

    public List<String> getInputs(ConsoleParameters consoleParameters) {
        List<String> resultsPaths = consoleParameters.getValues(RESULTS_PATH_PARAMETER);
        return resultsPaths;
    }

    public File getOutputFile(ConsoleParameters consoleParameters, List<String> resultsPaths) {
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
        return outputFile;
    }

    public static Predicate<File> RESULTS_FILES = (file -> {
        if (AggregateResultsXML.RESULTS_FILES.test(file)) {
            return true;
        } else if (CaseStudyXML.RESULTS_FILES.test(file)) {
            return true;
        } else if (GroupCaseStudyXML.RESULTS_FILES.test(file)) {
            return true;
        } else {
            return false;
        }
    });

    public static void mergeResultsIntoOutput(List<String> resultsPaths, File outputFile, Set<String> filterMeasures) {

        validateResultsPaths(resultsPaths);

        List<File> allFiles
                = resultsPaths.parallelStream()
                .map((path) -> new File(path))
                .map((directory) -> {
                    if (!directory.exists()) {
                        FileNotFoundException notFound = new FileNotFoundException("Directory '" + directory + "' does not exists [" + directory.getAbsolutePath() + "]");
                        ERROR_CODES.CANNOT_READ_FILE.exit(notFound);
                    }
                    return FileUtilities.findInDirectory(directory);
                })
                .flatMap(directories -> directories.parallelStream())
                .collect(Collectors.toList());

        List<File> relevantFiles = allFiles.stream()
                .filter(RESULTS_FILES)
                .collect(Collectors.toList());

        Global.showInfoMessage("Parsing " + relevantFiles.size() + " results files.\n");

        if (relevantFiles.isEmpty()) {
            return;
        }

        if (Global.isInfoPrinted()) {
            for (int i = 0; i < relevantFiles.size(); i++) {
                File relevantFile = relevantFiles.get(i);
                Global.showMessageTimestamped(("(" + i + " of " + relevantFiles.size()) + "): " + relevantFile.getAbsolutePath() + "\n");
            }
        }

        if (relevantFiles.stream().allMatch(CaseStudyXML.RESULTS_FILES)) {
            throw new IllegalStateException("Merge and write of individual RS case studies is not implemented yet!");
        } else if (relevantFiles.stream().allMatch(GroupCaseStudyXML.RESULTS_FILES)) {
            GroupCaseStudyXMLJoin.writeJoinIntoSpreadsheet(relevantFiles, outputFile, filterMeasures);
        } else {
            File caseStudyResultsXML = relevantFiles.stream().filter(CaseStudyXML.RESULTS_FILES).findAny().get();
            File groupCaseStudyResultsXML = relevantFiles.stream().filter(GroupCaseStudyXML.RESULTS_FILES).findAny().get();

            throw new IllegalStateException(
                    "Mix of individual and group case studies: "
                    + "'" + caseStudyResultsXML.getPath() + "'"
                    + " and "
                    + "'" + groupCaseStudyResultsXML.getPath() + "'"
            );
        }

        Global.showInfoMessage("Finished parsing " + relevantFiles.size() + " results files.\n");

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

    private Set<String> getFilterMeasures(ConsoleParameters consoleParameters) {
        if (consoleParameters.isParameterDefined(MEASURES_PARAMETER)) {
            return consoleParameters.getValues(MEASURES_PARAMETER).stream().collect(Collectors.toSet());
        } else {
            return Collections.EMPTY_SET;
        }
    }

}
