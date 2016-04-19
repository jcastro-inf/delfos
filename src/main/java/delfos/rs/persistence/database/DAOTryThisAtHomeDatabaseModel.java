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
package delfos.rs.persistence.database;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.databaseconnections.DatabaseConection;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Objeto para almacenar y recuperar en una base de datos mysql el modelo de recomendación del
 * sistema{@link TryThisAtHomeSVD}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 1 de Marzo de 2013
 * @version 2.0 28-Mayo-2013 Adecuación a la refactorización de los sistemas de recomendación.
 */
public class DAOTryThisAtHomeDatabaseModel implements RecommendationModelDatabasePersistence<TryThisAtHomeSVDModel> {

    /**
     * Prefijo que se añade a las tablas creadas con este objeto, para denotar que pertenecen al modelo generado por el
     * sistema de recomendación basado en descomposición en valores singulares
     */
    private final String RECOMMENDER_PREFIX = "try_this_";
    /**
     * Nombre de la tabla destinada a almacenar los perfiles de usuario
     */
    private final String USER_PROFILES = "user_profiles";
    /**
     * Nombre de la tabla destinada a almacenar los perfiles de productos
     */
    private final String ITEM_PROFILES = "item_profiles";

    /**
     * Devuelve el nombre final de la tabla que se usa para almacenar/recuperar los perfiles de usuario generados por el
     * sistema de recomendación
     *
     * @return Nombre de la tabla en la base de datos
     */
    private String getUserProfilesTable(String prefix) {
        return prefix + RECOMMENDER_PREFIX + USER_PROFILES;
    }

    /**
     * Devuelve el nombre final de la tabla que se usa para almacenar/recuperar los perfiles de productos generados por
     * el sistema de recomendación
     *
     * @return Nombre de la tabla en la base de datos
     */
    private String getItemProfilesTable(String prefix) {
        return prefix + RECOMMENDER_PREFIX + ITEM_PROFILES;
    }

    private String getTemporalUserProfilesTable(String prefix) {
        return getUserProfilesTable(prefix) + "_temp";
    }

    private String getTemporalItemProfilesTable(String prefix) {
        return getItemProfilesTable(prefix) + "_temp";
    }

    public DAOTryThisAtHomeDatabaseModel() {
    }

    private void createStructures(DatabaseConection databaseConection) throws FailureInPersistence {
        try (
                Statement st = databaseConection.doConnection().createStatement()) {

            String prefix = databaseConection.getPrefix();

            st.execute("DROP TABLE IF EXISTS " + getTemporalUserProfilesTable(prefix) + ";");
            st.execute("DROP TABLE IF EXISTS " + getTemporalItemProfilesTable(prefix) + ";");

            st.execute("CREATE TABLE  " + getTemporalUserProfilesTable(prefix) + " ("
                    + "idUser int(10) NOT NULL,"
                    + "idFeature int(10) unsigned NOT NULL,"
                    + "value double NOT NULL,"
                    + "PRIMARY KEY (idUser,idFeature)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");

            st.execute("CREATE TABLE  " + getTemporalItemProfilesTable(prefix) + " ("
                    + "idItem int(10) unsigned NOT NULL,"
                    + "idFeature int(10) unsigned NOT NULL,"
                    + "value double NOT NULL,"
                    + "PRIMARY KEY (idItem,idFeature)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");

            st.close();
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    private void makePermanent(DatabaseConection databaseConection) throws FailureInPersistence {
        try (
                Statement st = databaseConection.doConnection().createStatement()) {
            String prefix = databaseConection.getPrefix();

            st.execute("COMMIT");
            st.execute("DROP TABLE IF EXISTS " + getUserProfilesTable(prefix) + ";");
            st.execute("ALTER TABLE " + getTemporalUserProfilesTable(prefix) + " RENAME TO " + getUserProfilesTable(prefix) + ";");

            st.execute("DROP TABLE IF EXISTS " + getItemProfilesTable(prefix) + ";");
            st.execute("ALTER TABLE " + getTemporalItemProfilesTable(prefix) + " RENAME TO " + getItemProfilesTable(prefix) + ";");

            st.execute("COMMIT");
            st.close();
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public void saveModel(DatabasePersistence databasePersistence, TryThisAtHomeSVDModel model) throws FailureInPersistence {

        try {
            createStructures(databasePersistence.getConection());
        } catch (SQLException | ClassNotFoundException ex) {
            Logger.getLogger(DAOTryThisAtHomeDatabaseModel.class.getName()).log(Level.SEVERE, null, ex);
        }
        final String prefix = databasePersistence.getPrefix();

        try (Statement st = databasePersistence.getConection().doConnection().createStatement()) {
            {
                //Guardo los usuarios
                List<List<Double>> userProfiles = model.getAllUserFeatures();
                Map<Integer, Integer> usersIndex = model.getUsersIndex();

                for (int idUser : usersIndex.keySet()) {
                    int userIndex = usersIndex.get(idUser);

                    List<Double> features = userProfiles.get(userIndex);

                    StringBuilder sentence = new StringBuilder();
                    sentence.append("insert into ");
                    sentence.append(getTemporalUserProfilesTable(prefix));
                    sentence.append(" (idUser,idFeature,value) values ");

                    for (int idFeature = 0; idFeature < features.size(); idFeature++) {
                        sentence.append("(");
                        sentence.append(idUser);
                        sentence.append(",");
                        sentence.append(idFeature);
                        sentence.append(",");
                        sentence.append(features.get(idFeature));
                        sentence.append("),");

                    }
                    sentence.setCharAt(sentence.length() - 1, ';');
                    Global.showInfoMessage("================================================\n");
                    Global.showInfoMessage(sentence.toString() + "\n");
                    st.executeUpdate(sentence.toString());
                }
            }

            {
                //Guardo los productos
                List<List<Double>> itemProfiles = model.getAllItemFeatures();
                Map<Integer, Integer> itemsIndex = model.getItemsIndex();

                for (int idItem : itemsIndex.keySet()) {
                    int itemIndex = itemsIndex.get(idItem);
                    List<Double> features = itemProfiles.get(itemIndex);
                    StringBuilder sentence = new StringBuilder();
                    sentence.append("insert into ");
                    sentence.append(getTemporalItemProfilesTable(prefix));
                    sentence.append(" (idItem,idFeature,value) values ");
                    for (int idFeature = 0; idFeature < features.size(); idFeature++) {
                        sentence.append("(");
                        sentence.append(idItem);
                        sentence.append(",");
                        sentence.append(idFeature);
                        sentence.append(",");
                        sentence.append(features.get(idFeature));
                        sentence.append("),");
                    }
                    sentence.setCharAt(sentence.length() - 1, ';');
                    Global.showInfoMessage("================================================\n");
                    Global.showInfoMessage(sentence.toString() + "\n");
                    st.executeUpdate(sentence.toString());
                }
            }

            makePermanent(databasePersistence.getConection());
        } catch (ClassNotFoundException ex) {
            throw new FailureInPersistence(ex);
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public TryThisAtHomeSVDModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items, DatasetLoader<? extends Rating> datasetLoader) throws FailureInPersistence {

        try {
            final String prefix = databasePersistence.getPrefix();

            final int numFeatures;
            final int numUsers;
            final int numItems;

            try (
                    Statement statement = databasePersistence.getConection().doConnection().createStatement()) {

                ResultSet executeQuery;

                //Calculo el número de características para los usuarios.
                executeQuery = statement.executeQuery(
                        "select count(distinct idFeature) "
                        + "from " + getUserProfilesTable(prefix) + ";");
                executeQuery.next();
                int numUserFeatures = executeQuery.getInt(1);
                executeQuery.close();

                //NumUsers
                executeQuery = statement.executeQuery(
                        "select count(distinct idUser) "
                        + "from " + getUserProfilesTable(prefix) + ";");
                executeQuery.next();
                numUsers = executeQuery.getInt(1);
                executeQuery.close();

                //Calculo el número de características para los productos.
                executeQuery = statement.executeQuery(
                        "select count(distinct idFeature) "
                        + "from " + getItemProfilesTable(prefix) + ";");
                executeQuery.next();
                int numItemFeatures = executeQuery.getInt(1);
                executeQuery.close();

                //NumItems
                executeQuery = statement.executeQuery(
                        "select count(distinct idItem) "
                        + "from " + getItemProfilesTable(prefix) + ";");
                executeQuery.next();
                numItems = executeQuery.getInt(1);
                executeQuery.close();

                if (numUserFeatures == numItemFeatures) {
                    numFeatures = numUserFeatures;
                } else {
                    throw new FailureInPersistence("The number of features for users and items is different (" + numUserFeatures + "," + numItemFeatures + ")");
                }
            } catch (SQLException ex) {
                throw new FailureInPersistence(ex);
            }

            List<List<Double>> usersFeatures = new ArrayList<>(numUsers);
            TreeMap<Integer, Integer> usersIndex = new TreeMap<>();

            {
                StringBuilder sentence = new StringBuilder();

                sentence.append("select idUser,idFeature,value from ");
                sentence.append(getUserProfilesTable(prefix));
                sentence.append(";");

                try (
                        Statement statement = databasePersistence.getConection().doConnection().createStatement();
                        ResultSet rstUsers = statement.executeQuery(sentence.toString())) {

                    while (rstUsers.next()) {
                        int idUser = rstUsers.getInt("idUser");
                        int idFeature = rstUsers.getInt("idFeature");
                        double featureValue = rstUsers.getDouble("value");

                        if (!usersIndex.containsKey(idUser)) {
                            usersIndex.put(idUser, usersIndex.size());
                            List<Double> arrayList = new ArrayList<>(numFeatures);
                            for (int i = 0; i < numFeatures; i++) {
                                arrayList.add(null);
                            }
                            usersFeatures.add(arrayList);
                        }
                        usersFeatures.get(usersIndex.get(idUser)).set(idFeature, featureValue);
                    }
                    rstUsers.close();
                } catch (SQLException ex) {
                    throw new FailureInPersistence(ex);
                }
            }

            List<List<Double>> itemsFeatures = new ArrayList<>(numItems);
            TreeMap<Integer, Integer> itemsIndex = new TreeMap<>();

            {
                StringBuilder sentence = new StringBuilder();
                sentence.append("select idItem,idFeature,value from ");
                sentence.append(getItemProfilesTable(prefix));
                sentence.append(";");

                try (
                        Statement statement = databasePersistence.getConection().doConnection().createStatement();
                        ResultSet rstItems = statement.executeQuery(sentence.toString());) {

                    while (rstItems.next()) {
                        int idItem = rstItems.getInt("idItem");
                        int idFeature = rstItems.getInt("idFeature");
                        double featureValue = rstItems.getDouble("value");

                        if (!itemsIndex.containsKey(idItem)) {
                            itemsIndex.put(idItem, itemsIndex.size());
                            List<Double> arrayList = new ArrayList<>(numFeatures);
                            for (int i = 0; i < numFeatures; i++) {
                                arrayList.add(null);
                            }
                            itemsFeatures.add(arrayList);
                        }

                        itemsFeatures.get(itemsIndex.get(idItem)).set(idFeature, featureValue);
                    }
                    rstItems.close();
                    statement.close();
                } catch (SQLException ex) {
                    throw new FailureInPersistence(ex);
                }
            }

            return new TryThisAtHomeSVDModel(usersFeatures, usersIndex, itemsFeatures, itemsIndex);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        }
    }
}
