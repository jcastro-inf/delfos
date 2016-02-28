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

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.entity.EntityAlreadyExists;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.databaseconnections.DatabaseConection;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Constructor de los datasets en memoria (de contenido y de ratings) a partir
 * de los datos en la base de datos mysql del conocido conjunto Movilens
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknow date
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 */
public class DatabaseContentDataset implements ContentDataset {

    private final Feature[] features;
    private final DatabaseConection conexion;
    private final FeatureGenerator featureGenerator;

    public DatabaseContentDataset(DatabaseConection conexion) throws ClassNotFoundException, SQLException, FileNotFoundException, IOException {

        featureGenerator = new FeatureGenerator();
        featureGenerator.createFeatureByExtendedName("genero_nominal");
        featureGenerator.createFeatureByExtendedName("director_nominal");
        featureGenerator.createFeatureByExtendedName("pais_nominal");
        featureGenerator.createFeatureByExtendedName("anyo_numerical");

        features = new Feature[4];
        features[0] = featureGenerator.searchFeatureByExtendedName("genero_nominal");
        features[0] = featureGenerator.searchFeatureByExtendedName("director_nominal");
        features[0] = featureGenerator.searchFeatureByExtendedName("pais_nominal");
        features[0] = featureGenerator.searchFeatureByExtendedName("anyo_numerical");

        this.conexion = conexion;
    }

    @Override
    public Item get(int idItem) throws ItemNotFound {
        Object[] values = new Object[4];
        Item i = null;

        //Selecionamos todas las películas de la BD:
        String query = "SELECT * FROM movies WHERE idItem = " + idItem + ";";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            //Mientras haya películas las insertamos en la lista:
            while (rst.next()) {
                values[0] = rst.getString("genero_nominal");
                values[1] = rst.getString("director_nominal");
                values[2] = rst.getString("pais_nominal");
                values[3] = rst.getDouble("anyo_numerical");
                String name = rst.getString("name");

                i = new Item(idItem, name, features, values);
            }
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        }
        if (i == null) {
            throw new ItemNotFound(idItem);
        }

        return i;
    }

    @Override
    public Feature[] getFeatures() {
        return Arrays.copyOf(features, features.length);
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        Set<Object> ret = new TreeSet<>();

        FeatureType featureType = feature.getType();

        String query = "SELECT distinct " + feature.getName() + " FROM movies;";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            //Mientras haya películas las insertamos en la lista:
            while (rst.next()) {
                String stringValue = rst.getString(feature.getName());
                Object featureValue = featureType.parseFeatureValue(stringValue);
                ret.add(featureValue);
            }
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        }
        return ret;
    }

    @Override
    public int size() {
        int numItems = 0;
        String query = "SELECT count(*) n FROM movies;";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {
            //Mientras haya películas las insertamos en la lista:
            while (rst.next()) {
                numItems = rst.getInt("n");
            }
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        }

        return numItems;
    }

    @Override
    public Collection<Integer> allIDs() {
        Set<Integer> items = new TreeSet<>();

        String query = "SELECT idItem FROM movies;";
        try (
                Statement statement = conexion.doConnection().createStatement();
                ResultSet rst = statement.executeQuery(query)) {

            while (rst.next()) {
                int idItem = rst.getInt("idItem");
                items.add(idItem);
            }
        } catch (SQLException ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        }
        return items;
    }

    @Override
    public final Feature searchFeature(String featureName) {
        return featureGenerator.searchFeature(featureName);
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
        return featureGenerator.searchFeatureByExtendedName(extendedName);
    }

    @Override
    public Collection<Integer> getAvailableItems() {
        return allIDs();
    }

    @Override
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ContentDataset) {
            ContentDataset contentDataset = (ContentDataset) o;
            return ContentDatasetDefault.compareTo(this, contentDataset);
        } else {
            throw new IllegalArgumentException("Type not comparable.");
        }
    }

    @Override
    public Iterator<Item> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(Item entity) throws EntityAlreadyExists {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMinValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public double getMaxValue(Feature feature) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(int idEntity, Map<String, String> features) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Item getItem(int idItem) throws ItemNotFound {
        return get(idItem);
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

    public Collection<Item> getAllItems() {
        return allIDs().stream().map((idItem) -> get(idItem)).collect(Collectors.toList());
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
    public boolean addAll(Collection<? extends Item> entitys) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
