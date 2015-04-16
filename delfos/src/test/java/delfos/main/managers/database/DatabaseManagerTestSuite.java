package delfos.main.managers.database;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import delfos.main.Main;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Granada, sci2s)
 * <jorgecastrog@correo.ugr.es>
 */
public class DatabaseManagerTestSuite {

    public static void initDatabase(File manageDatasetConfigFile) throws Exception {

        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "--init"};
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addUser(File manageDatasetConfigFile, int idUser) throws Exception {
        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "-add-user", Integer.toString(idUser)
        };
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addUserFeatures(File manageDatasetConfigFile, int idUser, String... features) throws Exception {

        ArrayList<String> commandLineArguments = new ArrayList<>();

        commandLineArguments.add("-manage-database");
        commandLineArguments.add(manageDatasetConfigFile.getPath());

        commandLineArguments.add("-add-user-features");
        commandLineArguments.add(Integer.toString(idUser));

        commandLineArguments.add("-features");
        commandLineArguments.addAll(Arrays.asList(features));

        Main.mainWithExceptions(commandLineArguments.toArray(new String[0]));
    }

    public static void addItem(File manageDatasetConfigFile, int idItem) throws Exception {

        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "-add-item", Integer.toString(idItem)};
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addItemFeatures(File manageDatasetConfigFile, int idItem, String... features) throws Exception {

        ArrayList<String> commandLineArguments = new ArrayList<>();

        commandLineArguments.add("-manage-database");
        commandLineArguments.add(manageDatasetConfigFile.getPath());

        commandLineArguments.add("-add-item-features");
        commandLineArguments.add(Integer.toString(idItem));

        commandLineArguments.add("-features");
        commandLineArguments.addAll(Arrays.asList(features));

        Main.mainWithExceptions(commandLineArguments.toArray(new String[0]));
    }

    public static void addRating(File manageDatasetConfigFile, int idUser, int idItem, double ratingValue) throws Exception {

        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "--add-rating",
            "-idUser", Integer.toString(idUser),
            "-idItem", Integer.toString(idItem),
            "-ratingValue", Double.toString(ratingValue)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printUsers(File manageDatasetConfigFile) {
        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "--user-set"};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printItems(File manageDatasetConfigFile) {

        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "--item-set"};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printUserRatings(File manageDatasetConfigFile, int idUser) {

        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "-user-ratings", Integer.toString(idUser)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printItemRatings(File manageDatasetConfigFile, int idItem) {

        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "-item-ratings", Integer.toString(idItem)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printRatingsTable(File manageDatasetConfigFile) {
        String[] commandLineArguments = {
            "-manage-database", manageDatasetConfigFile.getPath(),
            "--ratings-table"};

        Main.mainWithExceptions(commandLineArguments);

    }
}
