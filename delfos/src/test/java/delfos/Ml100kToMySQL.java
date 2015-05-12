package delfos;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.dataset.loaders.movilens.ml100k.validation.MovieLens100kValidation;
import delfos.io.database.mysql.dataset.DatasetToMySQL;
import delfos.io.database.mysql.dataset.RatingsDatasetToMySQL;
import java.io.File;
import java.sql.SQLException;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class Ml100kToMySQL extends DelfosTest {

    //@Test
    public void convertDataset() throws ClassNotFoundException, SQLException {

        DatasetLoader<? extends Rating> ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        MySQLConnection mySQLConnection = new MySQLConnection(
                "testuser",
                "testuser",
                "ml-100k-mysql",
                "localhost",
                8306,
                "");

        DatasetToMySQL datasetToMySQL = new DatasetToMySQL(mySQLConnection);

        datasetToMySQL.saveContentDataset(((ContentDatasetLoader) ml100k).getContentDataset());
        datasetToMySQL.saveUsersDataset(((UsersDatasetLoader) ml100k).getUsersDataset());
        datasetToMySQL.saveRatingsDataset(ml100k.getRatingsDataset());

        mySQLConnection.close();
    }

    @Test
    public void convertValidationDatasets() throws ClassNotFoundException, SQLException {
        savePartition("1");
        savePartition("2");
        savePartition("3");
        savePartition("4");
        savePartition("5");
        savePartition("a");
        savePartition("b");
    }

    private void savePartition(String partition) throws ClassNotFoundException, SQLException {
        File ml100kdirectory = (File) ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k").getParameterValue(MovieLens100k.DirectoryOfDataset);
        MovieLens100kValidation ml100k = new MovieLens100kValidation(ml100kdirectory, partition);

        MySQLConnection mySQLConnection = new MySQLConnection(
                "testuser",
                "testuser",
                "ml-100k-mysql",
                "localhost",
                8306,
                "");

        RatingsDatasetToMySQL ratingsDatasetToMySQL = new RatingsDatasetToMySQL(mySQLConnection);

        ratingsDatasetToMySQL.RATINGS_TABLE_NAME = "ratings_" + partition + "_training";
        ratingsDatasetToMySQL.writeDataset(ml100k.getRatingsDataset());
        ratingsDatasetToMySQL.RATINGS_TABLE_NAME = "ratings_" + partition + "_test";
        ratingsDatasetToMySQL.writeDataset(ml100k.getRatingsDataset_test());

        mySQLConnection.close();
    }

}
