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
package delfos.dataset.loaders.epinions;

import delfos.common.Chronometer;
import delfos.common.Global;
import delfos.common.datastructures.DoubleMapping;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.ContentDatasetDefault;
import delfos.dataset.basic.item.Item;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class EPinionsContentDataset implements ContentDataset {

    private final DoubleMapping<Long, Integer> productsIndex = new DoubleMapping<>();
    private final DoubleMapping<Long, Integer> authorsIndex = new DoubleMapping<>();
    private final DoubleMapping<Long, Integer> subjectsIndex = new DoubleMapping<>();

    private final ContentDatasetDefault contentDataset;

    private final FeatureGenerator featureGenerator = new FeatureGenerator();

    public EPinionsContentDataset(File contentFile) throws FileNotFoundException, IOException {

        featureGenerator.createFeature("author", FeatureType.Nominal);
        featureGenerator.createFeature("subject", FeatureType.Nominal);

        BufferedReader br = new BufferedReader(new FileReader(contentFile));
        String linea = br.readLine();

        int i = 1;

        Chronometer c = new Chronometer();

        TreeSet<Item> items = new TreeSet<>();

        while (linea != null) {

            String[] columns = linea.split("\\|");

            final long CONTENT_ID;
            final String AUTHOR_ID_STRING;
            final String SUBJECT_ID_STRING;

            CONTENT_ID = new Long(columns[0]);
            if (!productsIndex.containsType1Value(CONTENT_ID)) {
                productsIndex.add(CONTENT_ID, productsIndex.size() + 1);
            }

            AUTHOR_ID_STRING = columns[1];
            long AUTHOR_ID = new Long(AUTHOR_ID_STRING);
            if (!authorsIndex.containsType1Value(AUTHOR_ID)) {
                authorsIndex.add(AUTHOR_ID, authorsIndex.size() + 1);
            }

            Feature[] features;
            Object[] values;
            if (columns.length == 3) {
                SUBJECT_ID_STRING = columns[2];
                long SUBJECT_ID = new Long(SUBJECT_ID_STRING);

                if (!subjectsIndex.containsType1Value(SUBJECT_ID)) {
                    subjectsIndex.add(SUBJECT_ID, subjectsIndex.size() + 1);
                }

                features = new Feature[2];
                features[0] = featureGenerator.searchFeature("author");
                features[1] = featureGenerator.searchFeature("subject");

                values = new Object[2];
                values[0] = AUTHOR_ID_STRING;
                values[1] = SUBJECT_ID_STRING;
            } else {

                features = new Feature[1];
                features[0] = featureGenerator.searchFeature("author");

                values = new Object[1];
                values[0] = AUTHOR_ID_STRING;
            }

            int idItem = productsIndex.typeOneToTypeTwo(CONTENT_ID);

            Item item = new Item(idItem, "Item_" + idItem, features, values);
            items.add(item);

            linea = br.readLine();

            if (i % 100000 == 0) {
                Global.showInfoMessage("Loading EPinions content --> " + i + " items " + c.printPartialElapsed() + " / " + c.printTotalElapsed() + "\n");
                c.setPartialEllapsedCheckpoint();
            }

            i++;
        }

        contentDataset = new ContentDatasetDefault(items);

    }

    public DoubleMapping<Long, Integer> getProductsIndex() {
        return productsIndex;
    }

    @Override
    public Item get(int idItem) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Integer> allIDs() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Collection<Integer> getAvailableItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setItemAvailable(int idItem, boolean available) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Object o) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean add(Item entity) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature[] getFeatures() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
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
    public Feature searchFeature(String featureName) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
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
    public Iterator<Item> iterator() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public DoubleMapping<Long, Integer> getAuthorsIndex() {
        return authorsIndex;
    }

    public DoubleMapping<Long, Integer> getSubjectsIndex() {
        return subjectsIndex;
    }

    @Override
    public Item getItem(int idItem) throws ItemNotFound {
        try {
            return get(idItem);
        } catch (EntityNotFound ex) {
            ex.isA(Item.class);
            throw new ItemNotFound(idItem, ex);
        }
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
