package delfos.main.managers.database;

import delfos.ConsoleParameters;
import delfos.main.Main;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Granada, sci2s)
 * <jorgecastrog@correo.ugr.es>
 */
public class DatabaseManagerTestSuite {

    public static void initDatabase(File manageDatasetConfigFile) throws Exception {

        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "--init-database"};
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addUser(File manageDatasetConfigFile, int idUser) throws Exception {
        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "-add-user", Integer.toString(idUser)
        };
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addUserFeatures(File manageDatasetConfigFile, int idUser, String... features) throws Exception {

        String[] args = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "-add-user-features", Integer.toString(idUser),
            "-features"
        };

        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        arguments.addAll(Arrays.asList(features));

        ConsoleParameters consoleParameters
                = ConsoleParameters.parseArguments(arguments.toArray(new String[0]));

        Main.mainWithExceptions(consoleParameters);
    }

    public static void addItem(File manageDatasetConfigFile, int idItem) throws Exception {

        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "-add-item", Integer.toString(idItem)};
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addItemFeatures(File manageDatasetConfigFile, int idItem, String... features) throws Exception {

        String[] args = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "-add-item-features", Integer.toString(idItem),
            "-features"
        };

        List<String> arguments = new ArrayList<>(Arrays.asList(args));
        arguments.addAll(Arrays.asList(features));

        ConsoleParameters consoleParameters
                = ConsoleParameters.parseArguments(arguments.toArray(new String[0]));

        Main.mainWithExceptions(consoleParameters);
    }

    public static void addRating(File manageDatasetConfigFile, int idUser, int idItem, double ratingValue) throws Exception {

        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "--add-rating",
            "-user", Integer.toString(idUser),
            "-item", Integer.toString(idItem),
            "-value", Double.toString(ratingValue)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printUsers(File manageDatasetConfigFile) {
        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "--user-set"};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printItems(File manageDatasetConfigFile) {

        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "--item-set"};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printUserRatings(File manageDatasetConfigFile, int idUser) {

        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "-user-ratings", Integer.toString(idUser)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printItemRatings(File manageDatasetConfigFile, int idItem) {

        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "-item-ratings", Integer.toString(idItem)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printRatingsTable(File manageDatasetConfigFile) {
        String[] commandLineArguments = {
            "--manage-database",
            "-database-config", manageDatasetConfigFile.getPath(),
            "--ratings-table"};

        Main.mainWithExceptions(commandLineArguments);

    }
}
