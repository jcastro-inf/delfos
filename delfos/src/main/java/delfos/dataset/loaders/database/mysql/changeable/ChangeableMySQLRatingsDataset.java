package delfos.dataset.loaders.database.mysql.changeable;

import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.LockedIterator;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.changeable.ChangeableRatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;

/**
 * Implementa un dataset de valoraciones modificable sobre fichero CSV.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 16-sep-2013
 */
public final class ChangeableMySQLRatingsDataset implements RatingsDataset<Rating>, ChangeableRatingsDataset<Rating> {

    private BothIndexRatingsDataset<Rating> ratingsDataset = new BothIndexRatingsDataset<>();
    private final MySQLConnection mySQLConnection;
    private final String ratingsTable_name;
    private final String ratingsTable_UserIDField;
    private final String ratingsTable_ItemIDField;
    private final String ratingsTable_RatingField;
    private final String ratingsTable_TimestampField;

    public ChangeableMySQLRatingsDataset(MySQLConnection connection,
            String ratingsTable_name,
            String ratingsTable_UserIDField,
            String ratingsTable_ItemIDField,
            String ratingsTable_RatingField,
            String ratingsTable_TimestampField) {
        this.mySQLConnection = connection;
        this.ratingsTable_name = ratingsTable_name;
        this.ratingsTable_UserIDField = ratingsTable_UserIDField;
        this.ratingsTable_ItemIDField = ratingsTable_ItemIDField;
        this.ratingsTable_RatingField = ratingsTable_RatingField;
        this.ratingsTable_TimestampField = ratingsTable_TimestampField;

        if (MySQLConnection.existsTable(connection, ratingsTable_name)) {
            try {
                loadRatingsFromExistingTable();
            } catch (SQLException ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
            }
        } else {
            ratingsDataset = new BothIndexRatingsDataset();
        }
    }

    @Override
    public Rating getRating(int idUser, int idItem) throws ItemNotFound, UserNotFound {
        return ratingsDataset.getRating(idUser, idItem);
    }

    @Override
    public Collection<Integer> allUsers() {
        return ratingsDataset.allUsers();
    }

    @Override
    public Collection<Integer> allRatedItems() {
        return ratingsDataset.allRatedItems();
    }

    @Override
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound {
        return ratingsDataset.getUserRated(idUser);
    }

    @Override
    public Map<Integer, Rating> getUserRatingsRated(Integer idUser) throws UserNotFound {
        return ratingsDataset.getUserRatingsRated(idUser);
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        return ratingsDataset.getItemRated(idItem);
    }

    @Override
    public Map<Integer, Rating> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        return ratingsDataset.getItemRatingsRated(idItem);
    }

    @Override
    public Domain getRatingsDomain() {
        return ratingsDataset.getRatingsDomain();
    }

    public static boolean mostradoWarning = false;

    @Override
    public void addRating(int idUser, int idItem, Rating ratingValue) {

        if (ratingValue instanceof RatingWithTimestamp) {
            if (!mostradoWarning) {
                Global.showWarning("Implement rating with timestamps.");
                mostradoWarning = true;
            }
        }

        java.sql.Date date = new Date(System.currentTimeMillis());

        String insert = "INSERT INTO `" + getRatingsTable_nameWithPrefix() + "` (`" + ratingsTable_UserIDField + "`, `" + ratingsTable_ItemIDField + "`, `" + ratingsTable_RatingField + "`, `" + ratingsTable_TimestampField + "`) "
                + "VALUES ("
                + idUser + ","
                + idItem + ","
                + ratingValue.ratingValue.floatValue() + ","
                + "'" + date.toString() + "');";
        try (Connection connection = mySQLConnection.doConnection(); Statement statement = connection.createStatement()) {
            statement.execute(insert);
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }

        List<Rating> ratings = new LinkedList<Rating>();
        for (Rating r : ratingsDataset) {
            ratings.add(r);
        }
        ratings.add(new RatingWithTimestamp(idUser, idItem, ratingValue.ratingValue.floatValue(), System.currentTimeMillis()));

        ratingsDataset = new BothIndexRatingsDataset(ratings);
    }

    @Override
    public void removeRating(int idUser, int idItem) {
        try (Connection connection = mySQLConnection.doConnection(); Statement statement = connection.createStatement()) {
            String delete = "delete from `" + getRatingsTable_nameWithPrefix()
                    + " where " + ratingsTable_UserIDField + " = " + idUser + " and "
                    + ratingsTable_ItemIDField + " = " + idItem + ";";

            statement.execute(delete);
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }
    }

    @Override
    public void commitChangesInPersistence() {
        //No se hace nada, la tabla ya esta actualizada.

        try (Connection connection = mySQLConnection.doConnection(); Statement statement = connection.createStatement()) {
            statement.execute("COMMIT;");
            statement.close();
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }
    }

    protected void createTables() throws SQLException {

        try (Connection connection = mySQLConnection.doConnection(); Statement statement = connection.createStatement()) {
            String dropTable = "drop table if exists " + getRatingsTable_nameWithPrefix() + ";";
            statement.execute(dropTable);

            String createTable = "CREATE TABLE IF NOT EXISTS `" + getRatingsTable_nameWithPrefix() + "` (\n"
                    + "`" + ratingsTable_UserIDField + "` int(11) NOT NULL,\n"
                    + "`" + ratingsTable_ItemIDField + "` int(11) NOT NULL,\n"
                    + "`" + ratingsTable_RatingField + "` float NOT NULL,\n"
                    + "`" + ratingsTable_TimestampField + "` timestamp,\n"
                    + "  PRIMARY KEY (`" + ratingsTable_UserIDField + "`,`" + ratingsTable_ItemIDField + "`)\n"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;";
            statement.execute(createTable);
        }
    }

    private void loadRatingsFromExistingTable() throws SQLException {

        try (Connection connection = mySQLConnection.doConnection(); Statement statement = connection.createStatement()) {

            String select = "select " + ratingsTable_UserIDField + "," + ratingsTable_ItemIDField + "," + ratingsTable_RatingField + "," + ratingsTable_TimestampField
                    + " from " + getRatingsTable_nameWithPrefix();
            ResultSet result = statement.executeQuery(select);

            List<Rating> ratings = new LinkedList<Rating>();

            while (result.next()) {
                int idUser = result.getInt(1);
                int idItem = result.getInt(2);
                float ratingValue = result.getFloat(3);
                Date timestamp = result.getDate(4);

                long timestamp_ms = timestamp.getTime();

                RatingWithTimestamp rating = new RatingWithTimestamp(idUser, idItem, ratingValue, timestamp_ms);

                ratings.add(rating);
            }

            ratingsDataset = new BothIndexRatingsDataset(ratings);
        }
    }

    public String getRatingsTable_nameWithPrefix() {
        return mySQLConnection.getPrefix() + ratingsTable_name;
    }

    @Override
    public float getMeanRatingItem(int idItem) throws ItemNotFound {
        return ratingsDataset.getMeanRatingItem(idItem);
    }

    @Override
    public float getMeanRatingUser(int idUser) throws UserNotFound {
        return ratingsDataset.getMeanRatingUser(idUser);
    }

    @Override
    public int getNumRatings() {
        return ratingsDataset.getNumRatings();
    }

    @Override
    public int sizeOfUserRatings(int idUser) throws UserNotFound {
        return ratingsDataset.sizeOfUserRatings(idUser);
    }

    @Override
    public int sizeOfItemRatings(int idItem) throws ItemNotFound {
        return ratingsDataset.sizeOfItemRatings(idItem);
    }

    @Override
    public boolean isRatedUser(int idUser) throws UserNotFound {
        return ratingsDataset.isRatedUser(idUser);
    }

    @Override
    public boolean isRatedItem(int idItem) throws ItemNotFound {
        return ratingsDataset.isRatedItem(idItem);
    }

    @Override
    public float getMeanRating() {
        return ratingsDataset.getMeanRating();
    }

    @Override
    public Iterator<Rating> iterator() {
        return new LockedIterator<>(ratingsDataset.iterator());
    }
}
