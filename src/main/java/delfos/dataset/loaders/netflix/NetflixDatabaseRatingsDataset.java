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
package delfos.dataset.loaders.netflix;

import delfos.databaseconnections.MySQLConnection;
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
 * Objeto que sirve de interfaz a la librería para acceder al dataset
 * proporcionado por netflix en el premio finalizado 21 de Septiembre de 2009
 * NOTA: no almacena ningún valor recuperado en memoria, por lo que los accesos
 * al dataset serán lentos. Es recomendable usar este dataset solo para casos
 * muy concretos de recomendación online, nunca en evaluación de algoritmos
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 */
public class NetflixDatabaseRatingsDataset extends RatingsDatasetAdapter {

    private final MySQLConnection conexion;

    public NetflixDatabaseRatingsDataset(MySQLConnection conexion) {
        super();
        this.conexion = conexion;
    }

    @Override
    public Rating getRating(long idUser, long idItem) {
        Double ret = null;
        try {

            String query = "SELECT userID,puntuacion,itemID FROM netflix_ratings where userID = " + idUser + " and itemID = " + idItem + ";";
            try (
                    Statement statement = conexion.doConnection().createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    ret = rst.getDouble("puntuacion");
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
    public Set<Long> allUsers() {
        Set<Long> ret = new TreeSet<>();
        try {

            String query = "select distinct userID from netflix_ratings;";
            try (
                    Statement statement = conexion.doConnection().createStatement();
                    ResultSet rst = statement.executeQuery(query)) {

                int i = 0;
                while (rst.next()) {
                    long idUser = rst.getLong("userID");
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
    public Set<Long> allRatedItems() {
        Set<Long> items = new TreeSet<>();
        try {

            String query = "SELECT distinct itemID FROM netflix_ratings;";
            try (
                    Statement statement = conexion.doConnection().createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    long idUser = rst.getLong("itemID");
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
    public Set<Long> getUserRated(long idUser) {
        Set<Long> valuedItems = new TreeSet<>();
        try {

            String query = "SELECT itemID FROM netflix_ratings WHERE userID = " + idUser + ";";
            try (
                    Statement statement = conexion.doConnection().createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    long idItem = rst.getLong("itemID");
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
    public Map<Long, Rating> getUserRatingsRated(long idUser) {

        Map<Long, Rating> valuedItems = new TreeMap<>();
        try {

            String query = "SELECT itemID,puntuacion FROM netflix_ratings WHERE userID = " + idUser + ";";
            try (
                    Statement statement = conexion.doConnection().createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    long idItem = rst.getLong("itemID");
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
    public Set<Long> getItemRated(long idItem) {
        Set<Long> users = new TreeSet<>();
        try {

            String query = "SELECT userID FROM netflix_ratings WHERE itemID = " + idItem + ";";
            try (
                    Statement statement = conexion.doConnection().createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    long idUser = rst.getLong("userID");
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
    public Map<Long, Rating> getItemRatingsRated(long idItem) {

        Map<Long, Rating> usersRatings = new TreeMap<>();
        try {

            String query = "SELECT userID,puntuacion FROM netflix_ratings WHERE itemID = " + idItem + ";";
            try (
                    Statement statement = conexion.doConnection().createStatement();
                    ResultSet rst = statement.executeQuery(query)) {
                while (rst.next()) {
                    long idUser = rst.getLong("userID");
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
