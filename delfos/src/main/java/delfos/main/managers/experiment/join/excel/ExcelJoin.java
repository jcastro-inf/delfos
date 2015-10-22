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
 * @author Jorge Castro Gallardo
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
