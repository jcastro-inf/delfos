package delfos.dataset.mockdatasets;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.entity.EntityAlreadyExists;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureGenerator;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 *
 * @author Jorge
 * @version 10-Octubre-2013
 */
public final class MockContentDataset implements ContentDataset {

    private final Map<Long, Item> items = new TreeMap<>();
    private final FeatureGenerator featureGenerator = new FeatureGenerator();
    private final Feature featurePriceNumerical;
    private final Feature featureClassNominal;

    /**
     * Comprueba que el producto no existe en el conjunto. Si existe lanza una
     * excepción {@link IllegalArgumentException}.
     *
     * @param idItem
     */
    private void checkItemNotExists(long idItem) {
        if (items.containsKey(idItem)) {
            throw new IllegalArgumentException("The item " + idItem + " already exists.");
        }
    }

    private void checkItem(long idItem) throws ItemNotFound {
        if (!items.containsKey(idItem)) {
            throw new ItemNotFound(idItem);
        }
    }

    public MockContentDataset() {

        featureGenerator.createFeature("class", FeatureType.Nominal);
        featureGenerator.createFeature("price", FeatureType.Numerical);

        featureClassNominal = featureGenerator.searchFeature("class");
        featurePriceNumerical = featureGenerator.searchFeature("price");

        Object[] featureValues;
        int idItem;
        Item item;

        idItem = 1;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);
        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 2;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);
        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 3;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 4;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 5;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 6;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 7;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 8;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 9;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 10;
        featureValues = new Object[2];
        featureValues[0] = "A";
        featureValues[1] = 1.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 11;
        featureValues = new Object[2];
        featureValues[0] = "B";
        featureValues[1] = 2.5;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 12;
        featureValues = new Object[2];
        featureValues[0] = "C";
        featureValues[1] = 3.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

        idItem = 13;
        featureValues = new Object[2];
        featureValues[0] = "D";
        featureValues[1] = 4.0;
        item = new Item(idItem, "Item " + idItem, featureGenerator.getSortedFeatures().toArray(new Feature[0]), featureValues);

        try {
            this.add(item);
        } catch (EntityAlreadyExists ex) {
            ERROR_CODES.ITEM_ALREADY_EXISTS.exit(ex);
        }

    }

    @Override
    public Item get(long idItem) throws EntityNotFound {
        try {
            checkItem(idItem);
        } catch (ItemNotFound ex) {
            throw new EntityNotFound(Item.class, idItem, ex);
        }
        return items.get(idItem);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    public Collection<Long> allIDs() {
        return new ArrayList<>(items.keySet());
    }

    @Override
    public Collection<Long> getAvailableItems() {
        return allIDs();
    }

    @Override
    public void setItemAvailable(long idItem, boolean available) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public int compareTo(Object o) {
        return Integer.compare(this.hashCode(),o.hashCode());
    }

    @Override
    public boolean add(Item entity) throws EntityAlreadyExists {
        checkItemNotExists(entity.getId());

        items.put(entity.getId(), entity);
        return true;
    }

    @Override
    public Feature[] getFeatures() {
        return featureGenerator.getSortedFeatures().toArray(new Feature[0]);
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        if (feature.equals(featureClassNominal)) {
            Set<Object> ret = new TreeSet<>();
            ret.add("A");
            ret.add("B");
            ret.add("C");
            ret.add("D");
            return ret;
        }

        if (feature.equals(featurePriceNumerical)) {
            Set<Object> ret = new TreeSet<>();
            ret.add(1.0);
            ret.add(2.5);
            ret.add(3.0);
            ret.add(4.0);
            return ret;
        }

        throw new IllegalArgumentException("Feature '" + feature + "'not known ");
    }

    @Override
    public double getMinValue(Feature feature) {
        if (feature.equals(featureClassNominal)) {
            throw new IllegalArgumentException("Not a numerical feature '" + feature + "'");
        }

        if (feature.equals(featurePriceNumerical)) {
            return 1;
        }

        throw new IllegalArgumentException("Feature '" + feature + "'not known ");
    }

    @Override
    public double getMaxValue(Feature feature) {
        if (feature.equals(featureClassNominal)) {
            throw new IllegalArgumentException("Not a numerical feature '" + feature + "'");
        }

        if (feature.equals(featurePriceNumerical)) {
            return 4;
        }

        throw new IllegalArgumentException("Feature '" + feature + "'not known ");
    }

    @Override
    public Feature searchFeature(String featureName) {
        if (featureName.equals(featureClassNominal.getName())) {
            return featureClassNominal;
        }

        if (featureName.equals(featurePriceNumerical.getName())) {
            return featurePriceNumerical;
        }
        return null;
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
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(long idEntity, Map<String, String> features) throws EntityNotFound {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public Iterator<Item> iterator() {
        return new ArrayList<>(items.values()).iterator();
    }

    @Override
    public Item getItem(long idItem) throws ItemNotFound {
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
    public boolean addAll(Collection<? extends Item> entitys) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
