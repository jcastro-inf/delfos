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
package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.Global;
import delfos.databaseconnections.DatabaseConection;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

/**
 * Clase encargada de almacenar el modelo de recomendación generado por el
 * sistem {@link KnnModelBasedCFRS}.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 2.0 28-Mayo-2013 Se ha cambiado la persistencia, ahora la realiza el
 * sistema de recomendación, que delega en esta clase.
 */
public class DAOKnnModelBasedDatabaseModel {

    private final String profilesTableName = "item_profiles";

    /**
     * Devuelve el nombre final de la tabla que se usa para almacenar/recuperar
     * el modelo en la base de datos.
     *
     * @return
     */
    private String getProfilesTable(DatabaseConection databaseConection) {
        return databaseConection.getPrefix() + profilesTableName;
    }

    private String getTemporalProfilesTable(DatabaseConection databaseConection) {
        return getProfilesTable(databaseConection) + "_temp";
    }

    public DAOKnnModelBasedDatabaseModel() {
    }

    private void createStructures(DatabaseConection databaseConection) throws FailureInPersistence {
        try (
                Statement st = databaseConection.doConnection().createStatement()) {
            String dropTable = "DROP TABLE IF EXISTS " + getTemporalProfilesTable(databaseConection) + ";";
            st.execute(dropTable);

            st.execute("CREATE TABLE  " + getTemporalProfilesTable(databaseConection) + " ("
                    + "idItem int(10) unsigned NOT NULL,"
                    + "idNeighbor int(10) unsigned NOT NULL,"
                    + "similarity double unsigned NOT NULL,"
                    + "PRIMARY KEY (idItem,idNeighbor)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    private void makePermanent(DatabaseConection databaseConection) throws FailureInPersistence {
        try (
                Statement st = databaseConection.doConnection().createStatement()) {
            st.execute("DROP TABLE IF EXISTS " + getProfilesTable(databaseConection) + ";");
            st.execute("ALTER TABLE " + getTemporalProfilesTable(databaseConection) + " RENAME TO " + getProfilesTable(databaseConection) + ";");

        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    public void saveModel(DatabasePersistence databasePersistence, KnnModelBasedCFRSModel model) throws FailureInPersistence {
        DatabaseConection databaseConection;
        try {
            databaseConection = databasePersistence.getConection();
        } catch (Throwable ex) {
            throw new FailureInPersistence(ex);
        }

        createStructures(databaseConection);

        String consulta = "";
        try (
                Statement st = databaseConection.doConnection().createStatement()) {

            int i = 1;
            for (KnnModelItemProfile profile : model) {

                if (profile.getAllNeighbors().isEmpty()) {
                    Global.showWarning("No neighbors found for item " + profile.getIdItem());
                    continue;
                }

                StringBuilder c = new StringBuilder();
                c.append("Insert into ").append(getTemporalProfilesTable(databaseConection)).append(" (idItem,idNeighbor,similarity) ");
                c.append("Values ");

                for (Neighbor neighbor : profile.getAllNeighbors()) {
                    Double sim = neighbor.getSimilarity();

                    if (sim > 1) {
                        Global.showWarning("Similarity of items " + profile.getIdItem() + " and " + neighbor.getIdNeighbor() + " was " + sim.toString() + " set to 1");
                        sim = 1.0;
                    }
                    if (sim < 0) {
                        Global.showWarning("Similarity of items " + profile.getIdItem() + " and " + neighbor.getIdNeighbor() + " was " + sim.toString() + " set to 0");
                        sim = 0.0;
                    }

                    c.append("(").append(profile.getIdItem()).append(",");
                    c.append(neighbor.getIdNeighbor()).append(",").append(sim).append("),");

                    //insert into itemBasedProfile values (idItem, idNeighbor,similarity
                }

                c.setCharAt(c.length() - 1, ';');

                consulta = c.toString();

                st.executeUpdate(consulta);

                Global.showInfoMessage("Saving profile --> " + (i * 100) / model.getNumProfiles() + "%\n");
                i++;
            }
            st.close();
        } catch (SQLException ex) {
            Global.showWarning(consulta);
            throw new FailureInPersistence(ex);
        }

        makePermanent(databaseConection);
    }

    public KnnModelBasedCFRSModel loadModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        Map<Integer, KnnModelItemProfile> itemProfiles = new TreeMap<>();

        DatabaseConection databaseConection;
        try {
            databaseConection = databasePersistence.getConection();

            Statement st = databaseConection.doConnection().createStatement();

            final String query = "select idItem,idNeighbor,similarity from " + getProfilesTable(databaseConection) + " where 1;";
            try (ResultSet rst = st.executeQuery(query)) {
                while (rst.next()) {

                    int idItem = rst.getInt("idItem");
                    int idNeighbor = rst.getInt("idNeighbor");
                    double similarity = rst.getDouble("similarity");

                    if (!itemProfiles.containsKey(idItem)) {
                        itemProfiles.put(idItem, new KnnModelItemProfile(idItem));
                    }

                    itemProfiles.get(idItem).addItem(idNeighbor, similarity);
                }

            }
        } catch (SQLException | ClassNotFoundException ex) {
            throw new FailureInPersistence(ex);
        }

        return new KnnModelBasedCFRSModel(itemProfiles);
    }
}
