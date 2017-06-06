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
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.features.CollectionOfEntitiesWithFeatures;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.user.UsersDatasetAdapter;
import delfos.dataset.changeable.ChangeableUsersDataset;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Implementa un dataset de usuarios con persistencia en base de datos mysql.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 04-Diciembre-2013
 */
public class ChangeableMySQLUsersDataset implements ChangeableUsersDataset, CollectionOfEntitiesWithFeatures<User> {

    private UsersDatasetAdapter usersDataset;
    private final FeatureGenerator featureGenerator = new FeatureGenerator();
    private final MySQLConnection mySQLConnection;
    private final String userFeaturesTable_name;
    private final String userFeaturesTable_FeatureIdField;
    private final String userFeaturesTable_FeatureNameField;
    private final String userFeaturesTable_FeatureTypeField;
    private final String usersTable_name;
    private final String usersTable_UserIDField;
    private final String usersTable_NameField;

    ChangeableMySQLUsersDataset(
            MySQLConnection connection,
            String contentDefinitionTable_name,
            String contentDefinitionTable_FeatureIdField,
            String contentDefinitionTable_FeatureNameField,
            String contentDefinitionTable_FeatureTypeField,
            String usersTable_name,
            String usersTable_UserIDField,
            String usersTable_NameField) {

        this.mySQLConnection = connection;
        this.userFeaturesTable_name = contentDefinitionTable_name;
        this.userFeaturesTable_FeatureIdField = contentDefinitionTable_FeatureIdField;
        this.userFeaturesTable_FeatureNameField = contentDefinitionTable_FeatureNameField;
        this.userFeaturesTable_FeatureTypeField = contentDefinitionTable_FeatureTypeField;
        this.usersTable_name = usersTable_name;
        this.usersTable_UserIDField = usersTable_UserIDField;
        this.usersTable_NameField = usersTable_NameField;

        if (MySQLConnection.existsTable(connection, contentDefinitionTable_name) && MySQLConnection.existsTable(connection, usersTable_name)) {
            try {
                readUsersDataset();
            } catch (SQLException ex) {
                ERROR_CODES.DATABASE_NOT_READY.exit(ex);
            }
        } else {
            usersDataset = new UsersDatasetAdapter();
        }
    }

    @Override
    public final void addUser(User user) {
        if (usersDataset.allIDs().contains(user.getId())) {
            //El dataset ya tenía el usuario, haciento cambio.

            Map<Long, User> users = new TreeMap<>();

            for (User user2 : usersDataset) {
                users.put(user2.getId(), user2);
            }

            users.remove(user.getId());
            users.put(user.getId(), user);

            usersDataset = new UsersDatasetAdapter(users.values().stream().collect(Collectors.toSet()));

        }

        boolean necesarioRegenerarTabla = false;

        for (Feature feature : user.getFeatures()) {
            if (featureGenerator.searchFeature(feature.getName()) == null) {
                //El usuario tiene nuevas características, se necesita añadir una columna.
                necesarioRegenerarTabla = true;
                break;
            }
        }

        try {
            if (necesarioRegenerarTabla) {
                createUsersTable();
            } else {
                insertUserInTable(user);
            }
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }

        // TODO hay algo por hacer...
    }

    @Override
    public User get(long idUser) throws EntityNotFound {
        return usersDataset.get(idUser);
    }

    @Override
    public void commitChangesInPersistence() {
        // No need for commit changes, they are already in the database
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            statement.execute("COMMIT;");
            statement.close();
        } catch (SQLException ex) {
            ERROR_CODES.DATABASE_NOT_READY.exit(ex);
        }
    }

    protected void createTables() throws SQLException {
        createUserFeaturesTable();
        createUsersTable();
    }

    public void createUserFeaturesTable() throws SQLException {
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            String dropTable = "drop table if exists " + userFeaturesTable_nameWithPrefix() + ";";
            statement.execute(dropTable);

            String createTable = "CREATE TABLE IF NOT EXISTS `" + userFeaturesTable_nameWithPrefix() + "` (\n"
                    + "`" + userFeaturesTable_FeatureIdField + "` int(11) NOT NULL,\n"
                    + "`" + userFeaturesTable_FeatureNameField + "` varchar(255) NOT NULL,\n"
                    + "`" + userFeaturesTable_FeatureTypeField + "` varchar(255) NOT NULL,\n"
                    + "  PRIMARY KEY (`" + userFeaturesTable_FeatureNameField + "`)\n"
                    + ") ENGINE=MyISAM DEFAULT CHARSET=latin1;";
            statement.execute(createTable);
        }
    }

    protected void createUsersTable() throws SQLException {
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            Feature[] features = usersDataset.getFeatures();

            String clearFeaturesTable = "delete from " + userFeaturesTable_nameWithPrefix() + ";";
            int clearFeaturesTableResult = statement.executeUpdate(clearFeaturesTable);
            if (clearFeaturesTableResult != 0) {
                Global.showInfoMessage("Features table '" + userFeaturesTable_nameWithPrefix() + "' cleared due to new user features (" + clearFeaturesTableResult + " rows deleted).\n");
            }

            for (int i = 0; i < features.length; i++) {

                Feature feature = features[i];
                String insert = "insert into " + userFeaturesTable_nameWithPrefix() + "(" + userFeaturesTable_FeatureIdField + "," + userFeaturesTable_FeatureNameField + "," + userFeaturesTable_FeatureTypeField + ")"
                        + " values (" + i + ",'" + feature.getName() + "','" + feature.getType().name() + "');";
                statement.executeUpdate(insert);
            }

            String dropTable = "drop table if exists " + getProductsTable_nameWithPrefix() + ";";
            statement.execute(dropTable);

            StringBuilder createUsersTable = new StringBuilder();
            createUsersTable.append("CREATE TABLE IF NOT EXISTS `").append(getProductsTable_nameWithPrefix()).append("` (\n");
            createUsersTable.append("`").append(usersTable_UserIDField).append("` int(11) NOT NULL,\n");
            createUsersTable.append("`").append(usersTable_NameField).append("` varchar(255) NOT NULL,\n");

            for (Feature feature : features) {
                String fieldType = feature.getType().getMySQLfieldType();
                createUsersTable.append("`").append(feature.getName()).append("` ").append(fieldType).append(",\n");
            }

            createUsersTable.append("  PRIMARY KEY (`").append(usersTable_UserIDField).append("`)\n");
            createUsersTable.append(") ENGINE=MyISAM DEFAULT CHARSET=latin1;");

            statement.execute(createUsersTable.toString());

            for (User user : usersDataset) {
                insertUserInTable(user);
            }

        }
    }

    @Override
    public Feature[] getFeatures() {
        return usersDataset.getFeatures();
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        return usersDataset.getAllFeatureValues(feature);
    }

    @Override
    public double getMinValue(Feature feature) {
        return usersDataset.getMinValue(feature);
    }

    @Override
    public double getMaxValue(Feature feature) {
        return usersDataset.getMaxValue(feature);
    }

    @Override
    public Feature searchFeature(String featureName) {
        return usersDataset.searchFeature(featureName);
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
        return usersDataset.searchFeatureByExtendedName(extendedName);
    }

    @Override
    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features) {
        return usersDataset.parseEntityFeatures(features);
    }

    @Override
    public Collection<Long> allIDs() {
        return usersDataset.allIDs();
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(long idEntity, Map<String, String> features) throws EntityNotFound {
        return usersDataset.parseEntityFeaturesAndAddToExisting(idEntity, features);
    }

    @Override
    public Iterator<User> iterator() {
        return new LockedIterator<>(usersDataset.iterator());
    }

    @Override
    public boolean add(User entity) {
        addUser(entity);
        return true;
    }

    private void insertUserInTable(User user) throws SQLException {
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {

            //Borro el usuario.
            String deleteUser = "delete from " + getProductsTable_nameWithPrefix() + " where " + usersTable_UserIDField + " = " + user.getId() + ";";
            int deleteUserResult = statement.executeUpdate(deleteUser);
            if (deleteUserResult != 0) {
                Global.showInfoMessage("User " + user.getName() + " (id=" + user.getId() + ") deleted from table " + getProductsTable_nameWithPrefix() + "\n");
            }

            //Insert de cada user.
            StringBuilder insertUser = new StringBuilder();

            insertUser.append("insert into ").append(getProductsTable_nameWithPrefix()).append(" (").append(usersTable_UserIDField).append(",").append(usersTable_NameField);

            user.getFeatures().stream().forEach((feature) -> {
                insertUser.append(",`").append(feature.getName()).append("`");
            });

            insertUser.append(") values (").append(user.getId()).append(",'").append(user.getName()).append("'");

            for (Feature feature : user.getFeatures()) {
                Object featureValue = user.getFeatureValue(feature);
                insertUser.append(",");
                if (feature.getType() == FeatureType.Numerical) {
                    insertUser.append(featureValue);
                } else {
                    insertUser.append("'").append(user.getFeatureValue(feature)).append("'");
                }
            }

            insertUser.append(");");
            int insertUserResult = statement.executeUpdate(insertUser.toString());
            if (insertUserResult != 0) {
                Global.showInfoMessage("User " + user.getName() + " (id=" + user.getId() + ") inserted into table " + getProductsTable_nameWithPrefix() + "\n");
            }
        }
    }

    private void readUsersDataset() throws SQLException {
        Set<User> users = new TreeSet<>();

        //Leo las características
        try (Statement statement = mySQLConnection.doConnection().createStatement()) {
            String selectFeatures = "Select " + userFeaturesTable_FeatureIdField + "," + userFeaturesTable_FeatureNameField + "," + userFeaturesTable_FeatureTypeField
                    + " from " + userFeaturesTable_nameWithPrefix() + ";";
            try (ResultSet result = statement.executeQuery(selectFeatures)) {
                while (result.next()) {

                    String featureName = result.getString(userFeaturesTable_FeatureNameField);
                    String featureType = result.getString(userFeaturesTable_FeatureTypeField);

                    if (!featureGenerator.containsFeature(featureName)) {
                        featureGenerator.createFeature(featureName, FeatureType.getFeatureType(featureType));
                    }
                }
            }

            StringBuilder selectUsers = new StringBuilder();
            selectUsers.append("Select ");
            selectUsers.append(usersTable_UserIDField);
            selectUsers.append(",").append(usersTable_NameField);

            for (Feature feature : featureGenerator.getSortedFeatures()) {
                selectUsers.append(",`").append(feature.getName()).append("`");
            }

            selectUsers.append("\nFrom ").append(getProductsTable_nameWithPrefix());
            ResultSet selectUserResult = statement.executeQuery(selectUsers.toString());

            while (selectUserResult.next()) {
                int idUser = selectUserResult.getInt(usersTable_UserIDField);
                String name = selectUserResult.getString(usersTable_NameField);
                Map<Feature, Object> userFeatures = new TreeMap<>();

                for (Feature feature : featureGenerator.getSortedFeatures()) {
                    final String column = feature.getName();
                    Object featureValue;

                    if (feature.getType() == FeatureType.Numerical) {
                        featureValue = selectUserResult.getDouble(column);
                    } else {
                        featureValue = selectUserResult.getString(column);
                    }

                    if (!selectUserResult.wasNull()) {
                        userFeatures.put(feature, featureValue);
                    }
                }
                User user = new User(idUser, name, userFeatures);

                users.add(user);

            }
            usersDataset = new UsersDatasetAdapter(users);
        }
    }

    public String userFeaturesTable_nameWithPrefix() {
        return mySQLConnection.getPrefix() + userFeaturesTable_name;
    }

    public String getProductsTable_nameWithPrefix() {
        return mySQLConnection.getPrefix() + usersTable_name;
    }

    @Override
    public User getUser(long idUser) throws UserNotFound {
        try {
            return usersDataset.get(idUser);
        } catch (EntityNotFound ex) {
            throw new UserNotFound(idUser, ex);
        }
    }

    @Override
    public String toString() {
        Set<String> _entitiesById = new TreeSet<>();
        for (User user : this) {
            _entitiesById.add(user.getName() + " (User " + user.getId() + ")");
        }
        return _entitiesById.toString();
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException("Not allowed to delete entities.");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean isEmpty() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean contains(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Collection<User> getAllItems() {
        return allIDs().stream().map((idUser) -> get(idUser)).collect(Collectors.toList());
    }

    @Override
    public Object[] toArray() {
        return getAllItems().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return getAllItems().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(((element) -> this.contains(element)));
    }

    @Override
    public boolean addAll(Collection<? extends User> entitys) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        return allIDs().size();
    }

}
