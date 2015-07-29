package delfos.dataset.loaders.database;

import delfos.databaseconnections.DatabaseConection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 */
public class DatabaseRatingsDataset extends RatingsDatasetAdapter {

    private final DatabaseConection conexion;
    public static final DecimalDomain ratingDomain = new DecimalDomain(1, 5);

    public DatabaseRatingsDataset(DatabaseConection conexion) {
        super();
        this.conexion = conexion;
    }

    @Override
    public Rating getRating(int idUser, int idItem) {

        Integer ret = null;
        String query = "SELECT idUser,idItem,rating FROM ratings where idUser = " + idUser + " and idItem = " + idItem + ";";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            while (rst.next()) {
                ret = rst.getInt("rating");
            }
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        if (ret == null) {
            return null;
        } else {
            return new Rating(idUser, idItem, ret);
        }
    }

    @Override
    public Set<Integer> allUsers() {
        Set<Integer> ret = new TreeSet<>();
        String query = "select distinct idUser from ratings;";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            int i = 0;
            while (rst.next()) {
                int idUser = rst.getInt("idUser");
                ret.add(idUser);
                i++;
            }
            rst.close();
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }

        return ret;
    }

    @Override
    public Set<Integer> allRatedItems() {
        Set<Integer> items = new TreeSet<>();
        String query = "SELECT distinct idItem FROM ratings;";

        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {
            while (rst.next()) {
                int idUser = rst.getInt("idItem");
                items.add(idUser);
            }
            rst.close();
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return items;
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) {
        Set<Integer> valuedItems = new TreeSet<>();
        String query = "SELECT idItem FROM ratings WHERE idUser = " + idUser + ";";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            while (rst.next()) {
                int idItem = rst.getInt("idItem");
                valuedItems.add(idItem);
            }
            rst.close();
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return valuedItems;
    }

    @Override
    public Map<Integer, Rating> getUserRatingsRated(Integer idUser) {

        Map<Integer, Rating> valuedItems = new TreeMap<>();

        String query = "SELECT idItem,rating FROM ratings WHERE idUser = " + idUser + ";";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            while (rst.next()) {
                int idItem = rst.getInt("idItem");
                float rating = rst.getFloat("rating");
                valuedItems.put(idItem, new Rating(idUser, idItem, rating));
            }
            rst.close();
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return valuedItems;
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) {
        Set<Integer> users = new TreeSet<>();

        String query = "SELECT idUser FROM ratings WHERE idItem = " + idItem + ";";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            while (rst.next()) {
                int idUser = rst.getInt("idUser");
                users.add(idUser);
            }
            rst.close();
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return users;
    }

    @Override
    public Map<Integer, Rating> getItemRatingsRated(Integer idItem) {

        Map<Integer, Rating> usersRatings = new TreeMap<>();

        String query = "SELECT idUser,rating FROM ratings WHERE idItem = " + idItem + ";";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            while (rst.next()) {
                int idUser = rst.getInt("idUser");
                float rating = rst.getFloat("rating");
                usersRatings.put(idUser, new Rating(idUser, idItem, rating));
            }
            rst.close();
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return usersRatings;
    }

    @Override
    public Domain getRatingsDomain() {
        return ratingDomain;
    }

    /**
     * Método provisional para manejar el error que se produce cuando hay un
     * error SQL. Este error está casi siempre provocado por una pérdida de
     * conexión con la base de datos.
     *
     * @param ex Excepción del error.
     */
    protected void connectionError(SQLException ex) {
        throw new IllegalArgumentException(ex);
    }
}
