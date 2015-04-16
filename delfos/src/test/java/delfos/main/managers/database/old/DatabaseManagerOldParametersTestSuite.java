package delfos.main.managers.database.old;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import delfos.main.Main;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Granada, sci2s)
 * <jorgecastrog@correo.ugr.es>
 */
public class DatabaseManagerOldParametersTestSuite {

    public static void initDatabase(File manageDatasetConfigFile) throws Exception {

        String[] commandLineArguments = {
            "-manageRatingDatabase", manageDatasetConfigFile.getPath(),
            "-initDatabase"};
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addUser(File manageDatasetConfigFile, int idUser) throws Exception {
        String[] commandLineArguments = {
            "-manageRatingDatabase", manageDatasetConfigFile.getPath(),
            "-addUser", Integer.toString(idUser)
        };
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addUserFeatures(File manageDatasetConfigFile, int idUser, String... features) throws Exception {

        ArrayList<String> commandLineArguments = new ArrayList<>();

        commandLineArguments.add("-manageRatingDatabase");
        commandLineArguments.add(manageDatasetConfigFile.getPath());

        commandLineArguments.add("-addUserFeatures");
        commandLineArguments.add(Integer.toString(idUser));

        commandLineArguments.add("-features");
        commandLineArguments.addAll(Arrays.asList(features));

        Main.mainWithExceptions(commandLineArguments.toArray(new String[0]));
    }

    public static void addItem(File manageDatasetConfigFile, int idItem) throws Exception {

        String[] commandLineArguments = {
            "-manageRatingDatabase", manageDatasetConfigFile.getPath(),
            "-addItem", Integer.toString(idItem)};
        Main.mainWithExceptions(commandLineArguments);
    }

    public static void addItemFeatures(File manageDatasetConfigFile, int idItem, String... features) throws Exception {

        ArrayList<String> commandLineArguments = new ArrayList<>();

        commandLineArguments.add("-manageRatingDatabase");
        commandLineArguments.add(manageDatasetConfigFile.getPath());

        commandLineArguments.add("-addItemFeatures");
        commandLineArguments.add(Integer.toString(idItem));

        commandLineArguments.add("-features");
        commandLineArguments.addAll(Arrays.asList(features));

        Main.mainWithExceptions(commandLineArguments.toArray(new String[0]));
    }

    public static void addRating(File manageDatasetConfigFile, int idUser, int idItem, double ratingValue) throws Exception {

        String[] commandLineArguments = {
            "-manageRatingDatabase",
            manageDatasetConfigFile.getPath(),
            "-addRating",
            "-idUser", Integer.toString(idUser),
            "-idItem", Integer.toString(idItem),
            "-ratingValue", Double.toString(ratingValue)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printUsers(File manageDatasetConfigFile) {
        String[] commandLineArguments = {
            "-manageRatingDatabase",
            manageDatasetConfigFile.getPath(),
            "-userSet"};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printItems(File manageDatasetConfigFile) {

        String[] commandLineArguments = {
            "-manageRatingDatabase",
            manageDatasetConfigFile.getPath(),
            "-itemSet"};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printUserRatings(File manageDatasetConfigFile, int idUser) {

        String[] commandLineArguments = {
            "-manageRatingDatabase",
            manageDatasetConfigFile.getPath(),
            "-userRatings", Integer.toString(idUser)};

        Main.mainWithExceptions(commandLineArguments);
    }

    static void printItemRatings(File manageDatasetConfigFile, int idItem) {

        String[] commandLineArguments = {
            "-manageRatingDatabase",
            manageDatasetConfigFile.getPath(),
            "-itemRatings", Integer.toString(idItem)};

        Main.mainWithExceptions(commandLineArguments);
    }
}
