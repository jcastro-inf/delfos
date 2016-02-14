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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
