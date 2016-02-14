package delfos.main.managers.recommendation.nonpersonalised;

import delfos.ConsoleParameters;
import java.io.File;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * <jorgecastrog@correo.ugr.es>
 */
public class ArgumentsNonPersonalisedTestSuite {

    public static ConsoleParameters buildRecommendationModel(File manageDatasetConfigFile) throws Exception {

        String[] consoleArguments = {
            "--non-personalised",
            "--build",
            "-rs-config", manageDatasetConfigFile.getAbsolutePath()
        };

        return ConsoleParameters.parseArguments(consoleArguments);
    }

    public static ConsoleParameters recommendAnonymous(File manageDatasetConfigFile) throws Exception {
        String[] consoleArguments = {
            "--non-personalised",
            "--recommend",
            "-rs-config", manageDatasetConfigFile.getAbsolutePath()
        };

        return ConsoleParameters.parseArguments(consoleArguments);
    }

    public static ConsoleParameters recommendToUser(File manageDatasetConfigFile, int idUser) throws Exception {
        String[] consoleArguments = {
            "--non-personalised",
            "--recommend",
            "-u", Integer.toString(idUser),
            "-rs-config", manageDatasetConfigFile.getAbsolutePath()
        };

        return ConsoleParameters.parseArguments(consoleArguments);
    }

}
