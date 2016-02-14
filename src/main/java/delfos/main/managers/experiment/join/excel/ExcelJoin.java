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
package delfos.main.managers.experiment.join.excel;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.io.excel.joiner.AggregateResultsExcels;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 *
 * Case use to join many excel of many experiment in a single one.
 *
 * @version 21-oct-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ExcelJoin extends CaseUseMode {

    public static final String MODE_PARAMETER = "--excel-join";
    public static final String RESULTS_PATH_PARAMETER = "-results";

    @Override
    public String getModeParameter() {
        return MODE_PARAMETER;
    }

    private static class Holder {

        private static final ExcelJoin INSTANCE = new ExcelJoin();
    }

    public static ExcelJoin getInstance() {
        return Holder.INSTANCE;
    }

    private ExcelJoin() {
    }

    @Override
    public void manageCaseUse(ConsoleParameters consoleParameters) {
        try {
            List<String> resultsPaths = consoleParameters.getValues(RESULTS_PATH_PARAMETER);
            consoleParameters.printUnusedParameters(System.err);
            manageCaseUse(resultsPaths);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);

            consoleParameters.printUnusedParameters(System.err);
        }
    }

    public static void manageCaseUse(List<String> resultsPaths) {
        List<File> files = new ArrayList<>();
        AggregateResultsExcels aggregateResultsExcels = new AggregateResultsExcels();

        for (String resultPath : resultsPaths) {
            File file = new File(resultPath);
            System.out.println("Parsing '" + file.getAbsolutePath() + "' file");
            Collection<File> extractResultsFiles = aggregateResultsExcels.extractResultsFiles(file);
            files.addAll(extractResultsFiles);
        }

        System.out.println("Detected " + files.size() + " results files");

        aggregateResultsExcels.join(files);

        System.out.println("Finished parsing " + files.size() + " results files.");
    }
}
