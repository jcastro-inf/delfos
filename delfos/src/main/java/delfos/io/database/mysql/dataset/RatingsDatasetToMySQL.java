package delfos.io.database.mysql.dataset;

import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Clase para escribir un dataset de valoraciones a una base de datos MySQL.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 11-Mar-2013
 */
public class RatingsDatasetToMySQL {

    public static final String CORE_RATINGS_TABLE_NAME = "ratings";
    public static final String CORE_ID_USER_COLUMN_NAME = "iduser";
    public static final String CORE_ID_ITEM_COLUMN_NAME = "iditem";
    public static final String CORE_RATING_COLUMN_NAME = "rating";
    public static final String CORE_TIMESTAMP_COLUMN_NAME = "timestamp";

    public String RATINGS_TABLE_NAME;
    public String ID_USER_COLUMN_NAME, ID_ITEM_COLUMN_NAME, RATING_COLUMN_NAME,
            TIMESTAMP_COLUMN_NAME;

    private final MySQLConnection mySQLConnection;

    public RatingsDatasetToMySQL(MySQLConnection mySQLConnection) {
        this.mySQLConnection = mySQLConnection;

        RATINGS_TABLE_NAME = mySQLConnection.getPrefix() + CORE_RATINGS_TABLE_NAME;
        ID_USER_COLUMN_NAME = CORE_ID_USER_COLUMN_NAME;
        ID_ITEM_COLUMN_NAME = CORE_ID_ITEM_COLUMN_NAME;
        RATING_COLUMN_NAME = CORE_RATING_COLUMN_NAME;
        TIMESTAMP_COLUMN_NAME = CORE_TIMESTAMP_COLUMN_NAME;
    }

    public <RatingType extends Rating> void writeDataset(RatingsDataset<RatingType> ratingsDataset) throws SQLException {
        deleteRatingsTableIfExists();
        createRatingsTable();
        insertRatings(ratingsDataset);
        mySQLConnection.commit();
    }

    public RatingsDataset<? extends Rating> readDataset() throws SQLException {

        try (
                Statement statement = mySQLConnection.doConnection().createStatement();
                ResultSet resultSet = statement.executeQuery("SELECT " + ID_USER_COLUMN_NAME + "," + ID_ITEM_COLUMN_NAME + "," + RATING_COLUMN_NAME + " FROM " + RATINGS_TABLE_NAME + ";")) {

            Collection<Rating> ratings = new ArrayList<>();
            while (resultSet.next()) {
                int idUser = resultSet.getInt(1);
                int idItem = resultSet.getInt(2);
                Number rating = resultSet.getInt(3);

                ratings.add(new Rating(idUser, idItem, rating));
            }

            return new BothIndexRatingsDataset(ratings);
        }
    }

    private void createRatingsTable() throws SQLException {

        String createTable = "CREATE TABLE " + RATINGS_TABLE_NAME + " ("
                + "" + ID_USER_COLUMN_NAME + " INT NOT NULL ,"
                + "" + ID_ITEM_COLUMN_NAME + " INT NOT NULL ,"
                + "" + RATING_COLUMN_NAME + " TINYINT NULL ,"
                + "" + TIMESTAMP_COLUMN_NAME + " INT NULL ,"
                + "PRIMARY KEY (" + ID_USER_COLUMN_NAME + ", " + ID_ITEM_COLUMN_NAME + ") );";
        mySQLConnection.execute(createTable);

    }

    private void deleteRatingsTableIfExists() throws SQLException {
        mySQLConnection.execute("DROP TABLE IF EXISTS " + RATINGS_TABLE_NAME + ";");
    }

    private <RatingType extends Rating> void insertRatings(RatingsDataset<RatingType> ratingsDataset) throws SQLException {

        for (Rating rating : ratingsDataset) {
            int idUser = rating.idUser;
            int idItem = rating.idItem;
            Number ratingValue = rating.ratingValue;

            if (rating instanceof RatingWithTimestamp) {
                RatingWithTimestamp ratingWithTimestamp = (RatingWithTimestamp) rating;
                long timestamp = ratingWithTimestamp.getTimestamp();
                mySQLConnection.execute("INSERT INTO " + RATINGS_TABLE_NAME + " (" + ID_USER_COLUMN_NAME + "," + ID_ITEM_COLUMN_NAME + "," + RATING_COLUMN_NAME + "," + TIMESTAMP_COLUMN_NAME + ") "
                        + "VALUES (" + idUser + "," + idItem + "," + ratingValue.intValue() + "," + timestamp + ");");
            } else {
                mySQLConnection.execute("INSERT INTO " + RATINGS_TABLE_NAME + " (" + ID_USER_COLUMN_NAME + "," + ID_ITEM_COLUMN_NAME + "," + RATING_COLUMN_NAME + ") "
                        + "VALUES (" + idUser + "," + idItem + "," + ratingValue.intValue() + ");");

            }
        }
    }
}
