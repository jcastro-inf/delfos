package delfos.dataset.loaders.netflix;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.databaseconnections.MySQLConnection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;

/**
 * Objeto que sirve de interfaz a la librería para acceder al dataset
 * proporcionado por netflix en el premio finalizado 21 de Septiembre de 2009
 * NOTA: no almacena ningún valor recuperado en memoria, por lo que los accesos
 * al dataset serán lentos. Es recomendable usar este dataset solo para casos
 * muy concretos de recomendación online, nunca en evaluación de algoritmos
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 */
public class NetflixDatabaseRatingsDataset extends RatingsDatasetAdapter {

    private MySQLConnection conexion;

    public NetflixDatabaseRatingsDataset(MySQLConnection conexion) {
        super();
        this.conexion = conexion;
    }

    @Override
    public Rating getRating(int idUser, int idItem) {
        Integer ret = null;
        try {

            String query = "SELECT userID,puntuacion,itemID FROM netflix_ratings where userID = " + idUser + " and itemID = " + idItem + ";";
            try (
                    Connection connection = conexion.doConnection();
                    Statement statement = connection.createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    ret = rst.getInt("puntuacion");
                }
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
    public Collection<Integer> allUsers() {
        ArrayList<Integer> ret = new ArrayList<Integer>();
        try {

            String query = "select distinct userID from netflix_ratings;";
            try (
                    Connection connection = conexion.doConnection();
                    Statement statement = connection.createStatement();
                    ResultSet rst = statement.executeQuery(query)) {

                int i = 0;
                while (rst.next()) {
                    int idUser = rst.getInt("userID");
                    ret.add(idUser);
                    i++;
                }
            }
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }

        return ret;
    }

    @Override
    public Set<Integer> allRatedItems() {
        Set<Integer> items = new TreeSet<Integer>();
        try {

            String query = "SELECT distinct itemID FROM netflix_ratings;";
            try (
                    Connection connection = conexion.doConnection();
                    Statement statement = connection.createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    int idUser = rst.getInt("itemID");
                    items.add(idUser);
                }
            }
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return items;
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) {
        Set<Integer> valuedItems = new TreeSet<Integer>();
        try {

            String query = "SELECT itemID FROM netflix_ratings WHERE userID = " + idUser + ";";
            try (
                    Connection connection = conexion.doConnection();
                    Statement statement = connection.createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    int idItem = rst.getInt("itemID");
                    valuedItems.add(idItem);
                }
            }
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return valuedItems;
    }

    @Override
    public Map<Integer, Rating> getUserRatingsRated(Integer idUser) {

        Map<Integer, Rating> valuedItems = new TreeMap<Integer, Rating>();
        try {

            String query = "SELECT itemID,puntuacion FROM netflix_ratings WHERE userID = " + idUser + ";";
            try (
                    Connection connection = conexion.doConnection();
                    Statement statement = connection.createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    int idItem = rst.getInt("itemID");
                    byte rating = rst.getByte("puntuacion");
                    valuedItems.put(idItem, new Rating(idUser, idItem, rating));
                }
            }
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return valuedItems;
    }

    @Override
    public Collection<Integer> getItemRated(Integer idItem) {
        Set<Integer> users = new TreeSet<Integer>();
        try {

            String query = "SELECT userID FROM netflix_ratings WHERE itemID = " + idItem + ";";
            try (
                    Connection connection = conexion.doConnection();
                    Statement statement = connection.createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    int idUser = rst.getInt("userID");
                    users.add(idUser);
                }
            }
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return users;
    }

    @Override
    public Map<Integer, Rating> getItemRatingsRated(Integer idItem) {

        Map<Integer, Rating> usersRatings = new TreeMap<Integer, Rating>();
        try {

            String query = "SELECT userID,puntuacion FROM netflix_ratings WHERE itemID = " + idItem + ";";
            try (
                    Connection connection = conexion.doConnection();
                    Statement statement = connection.createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    int idUser = rst.getInt("userID");
                    byte rating = rst.getByte("puntuacion");
                    usersRatings.put(idUser, new Rating(idUser, idItem, rating));
                }
            }
        } catch (SQLException ex) {
            connectionError(ex);
            return null;
        }
        return usersRatings;
    }
    public static final DecimalDomain ratingDomain = new DecimalDomain(1, 5);

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
