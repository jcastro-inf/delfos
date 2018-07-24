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
package delfos.dataset.loaders.database.mysql.changeable;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.LockedIterator;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingWithTimestamp;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDataset;
import delfos.dataset.changeable.ChangeableRatingsDataset;
import delfos.dataset.storage.memory.BothIndexRatingsDataset;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementa un dataset de valoraciones modificable sobre fichero CSV.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
            String ratingsTable_TimestampField,
            UsersDataset usersDataset,
            ContentDataset contentDataset) {
        this.mySQLConnection = connection;
        this.ratingsTable_name = ratingsTable_name;
        this.ratingsTable_UserIDField = ratingsTable_UserIDField;
        this.ratingsTable_ItemIDField = ratingsTable_ItemIDField;
        this.ratingsTable_RatingField = ratingsTable_RatingField;
        this.ratingsTable_TimestampField = ratingsTable_TimestampField;

        if (MySQLConnection.existsTable(connection, ratingsTable_name)) {
            try {
                loadRatingsFromExistingTable(usersDataset, contentDataset);
            } catch (SQLException ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
            }
        } else {
            ratingsDataset = new BothIndexRatingsDataset();
        }
    }

    @Override
    public Rating getRating(long idUser, long idItem) throws ItemNotFound, UserNotFound {
        return ratingsDataset.getRating(idUser, idItem);
    }

    @Override
    public Set<Long> allUsers() {
        return ratingsDataset.allUsers();
    }

    @Override
    public Set<Long> allRatedItems() {
        return ratingsDataset.allRatedItems();
    }

    @Override
    public Set<Long> getUserRated(long idUser) throws UserNotFound {
        return ratingsDataset.getUserRated(idUser);
    }

    @Override
    public Map<Long, Rating> getUserRatingsRated(long idUser) throws UserNotFound {
        return ratingsDataset.getUserRatingsRated(idUser);
    }

    @Override
    public Set<Long> getItemRated(long idItem) throws ItemNotFound {
        return ratingsDataset.getItemRated(idItem);
    }

    @Override
    public Map<Long, Rating> getItemRatingsRated(long idItem) throws ItemNotFound {
        return ratingsDataset.getItemRatingsRated(idItem);
    }

    @Override
    public Domain getRatingsDomain() {
        return ratingsDataset.getRatingsDomain();
    }

    public static boolean mostradoWarning = false;

    @Override
    public void addRating(long idUser, long idItem, Rating ratingValue) {

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
                + ratingValue.getRatingValue().doubleValue() + ","
                + "'" + date.toString() + "');";
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            statement.execute(insert);
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }

        List<Rating> ratings = new LinkedList<Rating>();
        for (Rating r : ratingsDataset) {
            ratings.add(r);
        }
        ratings.add(new RatingWithTimestamp(idUser, idItem, ratingValue.getRatingValue().doubleValue(), System.currentTimeMillis()));

        ratingsDataset = new BothIndexRatingsDataset(ratings);
    }

    @Override
    public void removeRating(long idUser, long idItem) {
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
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

        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            statement.execute("COMMIT;");
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }
    }

    protected void createTables() throws SQLException {

        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            String dropTable = "drop table if exists " + getRatingsTable_nameWithPrefix() + ";";
            statement.execute(dropTable);

            String createTable = "CREATE TABLE IF NOT EXISTS `" + getRatingsTable_nameWithPrefix() + "` (\n"
                    + "`" + ratingsTable_UserIDField + "` int(11) NOT NULL,\n"
                    + "`" + ratingsTable_ItemIDField + "` int(11) NOT NULL,\n"
                    + "`" + ratingsTable_RatingField + "` double NOT NULL,\n"
                    + "`" + ratingsTable_TimestampField + "` timestamp,\n"
                    + "  PRIMARY KEY (`" + ratingsTable_UserIDField + "`,`" + ratingsTable_ItemIDField + "`)\n"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;";
            statement.execute(createTable);
        }
    }

    private void loadRatingsFromExistingTable(UsersDataset usersDataset, ContentDataset contentDataset) throws SQLException {

        try (Statement statement = mySQLConnection.doConnection().createStatement()) {

            String select = "select " + ratingsTable_UserIDField + "," + ratingsTable_ItemIDField + "," + ratingsTable_RatingField + "," + ratingsTable_TimestampField
                    + " from " + getRatingsTable_nameWithPrefix();
            ResultSet result = statement.executeQuery(select);

            Map<Long,User> usersNotFound = new TreeMap<>();
            Map<Long,Item> itemsNotFound = new TreeMap<>();
            
            List<Rating> ratings = new LinkedList<>();

            while (result.next()) {
                int idUser = result.getInt(1);
                int idItem = result.getInt(2);
                double ratingValue = result.getDouble(3);
                Date timestamp = result.getDate(4);

                long timestamp_ms = timestamp.getTime();
                User user;
                try {
                    user = usersDataset.get(idUser);
                }catch(EntityNotFound ex){
                    if(!usersNotFound.containsKey((long) idUser)){
                        usersNotFound.put((long) idUser, new User(idUser, "User_"+idUser));
                    }
                    user = usersNotFound.get( (long) idUser);
                }
                Item item ;
                try{
                    item = contentDataset.get(idItem);
                } catch(EntityNotFound ex){
                    if(!itemsNotFound.containsKey((long) idItem)){
                        itemsNotFound.put((long) idItem, new Item(idItem, "Item_"+idItem));
                    }
                    item = itemsNotFound.get((long) idItem);
                }
                
                RatingWithTimestamp rating = new RatingWithTimestamp(user,item, ratingValue, timestamp_ms);

                ratings.add(rating);
            }
            
            if(!itemsNotFound.isEmpty()) Global.showWarning("There were missing items from the ratings table: "+itemsNotFound.keySet().stream().map(l1 -> l1.toString()+", ").collect(Collectors.joining(",")));
            if(!usersNotFound.isEmpty()) Global.showWarning("There were missing users from the ratings table: "+usersNotFound.keySet().stream().map(l1 -> l1.toString()+", ").collect(Collectors.joining(",")));

            ratingsDataset = new BothIndexRatingsDataset(ratings);
        }
    }

    public String getRatingsTable_nameWithPrefix() {
        return mySQLConnection.getPrefix() + ratingsTable_name;
    }

    @Override
    public double getMeanRatingItem(long idItem) throws ItemNotFound {
        return ratingsDataset.getMeanRatingItem(idItem);
    }

    @Override
    public double getMeanRatingUser(long idUser) throws UserNotFound {
        return ratingsDataset.getMeanRatingUser(idUser);
    }

    @Override
    public long getNumRatings() {
        return ratingsDataset.getNumRatings();
    }

    @Override
    public long sizeOfUserRatings(long idUser) throws UserNotFound {
        return ratingsDataset.sizeOfUserRatings(idUser);
    }

    @Override
    public long sizeOfItemRatings(long idItem) throws ItemNotFound {
        return ratingsDataset.sizeOfItemRatings(idItem);
    }

    @Override
    public boolean isRatedUser(long idUser) throws UserNotFound {
        return ratingsDataset.isRatedUser(idUser);
    }

    @Override
    public boolean isRatedItem(long idItem) throws ItemNotFound {
        return ratingsDataset.isRatedItem(idItem);
    }

    @Override
    public double getMeanRating() {
        return ratingsDataset.getMeanRating();
    }

    @Override
    public Iterator<Rating> iterator() {
        return new LockedIterator<>(ratingsDataset.iterator());
    }
}
