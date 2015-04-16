package delfos.rs.collaborativefiltering.knn.modelbased;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.Global;
import delfos.databaseconnections.DatabaseConection;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;

/**
 * Clase encargada de almacenar el modelo de recomendación generado por el
 * sistem {@link KnnModelBasedCFRS}.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
                Connection connection = databaseConection.doConnection();
                Statement st = connection.createStatement()) {
            String dropTable = "DROP TABLE IF EXISTS " + getTemporalProfilesTable(databaseConection) + ";";
            st.execute(dropTable);

            st.execute("CREATE TABLE  " + getTemporalProfilesTable(databaseConection) + " ("
                    + "idItem int(10) unsigned NOT NULL,"
                    + "idNeighbor int(10) unsigned NOT NULL,"
                    + "similarity float unsigned NOT NULL,"
                    + "PRIMARY KEY (idItem,idNeighbor)"
                    + ") ENGINE=InnoDB DEFAULT CHARSET=latin1;");
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    private void makePermanent(DatabaseConection databaseConection) throws FailureInPersistence {
        try (
                Connection connection = databaseConection.doConnection();
                Statement st = connection.createStatement()) {
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
                Connection connection = databaseConection.doConnection();
                Statement st = connection.createStatement()) {

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
                    Float sim = neighbor.getSimilarity();

                    if (sim > 1) {
                        Global.showWarning("Similarity of items " + profile.getIdItem() + " and " + neighbor.getIdNeighbor() + " was " + sim.toString() + " set to 1");
                        sim = 1.0f;
                    }
                    if (sim < 0) {
                        Global.showWarning("Similarity of items " + profile.getIdItem() + " and " + neighbor.getIdNeighbor() + " was " + sim.toString() + " set to 0");
                        sim = 0.0f;
                    }

                    c.append("(").append(profile.getIdItem()).append(",");
                    c.append(neighbor.getIdNeighbor()).append(",").append(sim).append("),");

                    //insert into itemBasedProfile values (idItem, idNeighbor,similarity
                }

                c.setCharAt(c.length() - 1, ';');

                consulta = c.toString();

                st.executeUpdate(consulta);

                Global.showMessage("Saving profile --> " + (i * 100) / model.getNumProfiles() + "%\n");
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
        Map<Integer, KnnModelItemProfile> itemProfiles = new TreeMap<Integer, KnnModelItemProfile>();

        DatabaseConection databaseConection;
        try {
            databaseConection = databasePersistence.getConection();

            Connection connection = databaseConection.doConnection();
            Statement st = connection.createStatement();

            final String query = "select idItem,idNeighbor,similarity from " + getProfilesTable(databaseConection) + " where 1;";
            try (ResultSet rst = st.executeQuery(query)) {
                while (rst.next()) {

                    int idItem = rst.getInt("idItem");
                    int idNeighbor = rst.getInt("idNeighbor");
                    float similarity = rst.getFloat("similarity");

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
