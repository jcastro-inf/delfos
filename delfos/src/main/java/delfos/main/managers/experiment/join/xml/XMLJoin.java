package delfos.main.managers.experiment.join.xml;

import delfos.ConsoleParameters;
import delfos.ERROR_CODES;
import delfos.UndefinedParameterException;
import delfos.common.FileUtilities;
import delfos.main.managers.CaseUseMode;
import java.io.File;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * Case use to join many excel of many experiment in a single one.
 *
 * @version 21-oct-2014
 * @author Jorge Castro Gallardo
 */
public class XMLJoin extends CaseUseMode {

    public static final String MODE_PARAMETER = "--xml-join";
    public static final String RESULTS_PATH_PARAMETER = "-results";

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
            consoleParameters.printUnusedParameters(System.err);
            manageCaseUse(resultsPaths);
        } catch (UndefinedParameterException ex) {
            ERROR_CODES.COMMAND_LINE_PARAMETER_IS_NOT_DEFINED.exit(ex);

            consoleParameters.printUnusedParameters(System.err);
        }
    }

    public static void manageCaseUse(List<String> resultsPaths) {

        AggregateResultsXML aggregateResultsXML = new AggregateResultsXML();

        List<File> allFiles = new LinkedList<>();

        resultsPaths.stream()
                .map((path) -> new File(path))
                .forEach((pathFile) -> {
                    allFiles.addAll(FileUtilities.findInDirectory(pathFile));
                });

        List<File> relevantFiles = aggregateResultsXML.filterResultsFiles(allFiles);

        System.out.println("Detected " + relevantFiles.size() + " results files");

        aggregateResultsXML.join(relevantFiles);

        System.out.println("Finished parsing " + relevantFiles.size() + " results files.");
    }
}
