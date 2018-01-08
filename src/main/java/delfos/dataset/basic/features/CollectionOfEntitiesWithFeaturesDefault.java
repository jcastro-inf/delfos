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
package delfos.dataset.basic.features;

import delfos.common.LockedIterator;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.user.User;

import java.util.*;

/**
 * Clase que define el comportamiento común de una colección de
 * {@link EntityWithFeatures}. Proporciona métodos como obtener todas las
 * características u obtener los valores distintos valores que los objetos que
 * contiene tienen en una característica dada.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 18-sep-2013
 * @param <Entity>
 */
public abstract class CollectionOfEntitiesWithFeaturesDefault<Entity extends EntityWithFeatures> implements CollectionOfEntitiesWithFeatures<Entity> {

    /**
     * Generador de caracteristicas que almacena las características de las
     * entidades de esta colección.
     */
    protected final FeatureGenerator featureGenerator = new FeatureGenerator();
    /**
     * Almacena las entidades de esta colección, indexadas por identificador.
     */
    protected final Map<Long, Entity> entitiesById = new HashMap<>();
    /*
     * Valores distintos de todas las características (incluidas las numéricas).
     */
    protected final Map<Feature, Set<Object>> featureValues = new TreeMap<>();
    /**
     * Valores mínimos de los atributos numéricos.
     */
    protected Map<Feature, Double> minNumerical = new TreeMap<>();
    /**
     * Valores máximos de los atributos numéricos.
     */
    protected Map<Feature, Double> maxNumerical = new TreeMap<>();

    @Override
    public final boolean add(Entity entity) {

        Map<Feature, Object> featureValues_thisEntity = new TreeMap<>();
        for (Feature feature : entity.getFeatures()) {

            Object featureValue = entity.getFeatureValue(feature);
            FeatureType featureType = feature.getType();

            if (!featureGenerator.containsFeature(feature.getName())) {
                featureGenerator.createFeature(feature.getName(), feature.getType());
            }

            Feature featureInThisDataset = featureGenerator.searchFeature(feature.getName());

            if (!featureValues.containsKey(featureInThisDataset)) {
                featureValues.put(featureInThisDataset, new TreeSet<>());
            }

            Object featureValueReady;
            if (featureType.isValueCorrect(featureValue)) {
                featureValueReady = featureValue;
            } else {
                featureValueReady = featureType.parseFeatureValue(featureValue);
            }

            if (featureValueReady instanceof List) {
                List listOfValues = (List) featureValueReady;
                featureValues.get(featureInThisDataset).addAll(listOfValues);
            } else {
                featureValues.get(featureInThisDataset).add(featureValueReady);
            }

            featureValues_thisEntity.put(featureInThisDataset, featureValueReady);
        }

        Entity newEntity = null;
        if (entity instanceof Item) {
            Item item = (Item) entity;
            newEntity = (Entity) new Item(item.getId(), item.getName(), featureValues_thisEntity);
        }

        if (entity instanceof User) {
            User user = (User) entity;
            newEntity = (Entity) new User(user.getId(), user.getName(), featureValues_thisEntity);
        }

        if (newEntity == null) {
            throw new IllegalStateException("Unknown type " + entity.getClass());
        }

        entitiesById.put(entity.getId(), newEntity);
        return true;
    }

    @Override
    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(long idEntity, Map<String, String> features) throws EntityNotFound {
        Entity entity = get(idEntity);

        Map<Feature, Object> ret = new TreeMap<>();
        for (Feature feature : entity.getFeatures()) {
            ret.put(feature, entity.getFeatureValue(feature));
        }
        ret.putAll(parseEntityFeatures(features));

        return ret;
    }

    @Override
    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features) {

        Map<Feature, Object> _features = new TreeMap<>();

        for (String featureNameExtended : features.keySet()) {
            String featureValueString = features.get(featureNameExtended);

            Feature feature = featureGenerator.searchFeatureByExtendedName(featureNameExtended);
            if (feature == null) {
                feature = featureGenerator.createFeatureByExtendedName(featureNameExtended);
            }

            Object featureValue = feature.getType().parseFeatureValue(featureValueString);

            _features.put(feature, featureValue);
        }

        return _features;
    }

    @Override
    public Iterator<Entity> iterator() {
        return new LockedIterator<>(entitiesById.values().iterator());
    }

    @Override
    public int size() {
        return entitiesById.size();
    }

    @Override
    public Feature[] getFeatures() {
        return featureGenerator.getSortedFeatures().toArray(new Feature[0]);
    }

    @Override
    public Set<Object> getAllFeatureValues(Feature feature) {
        Set<Object> allFeatureValues = new TreeSet<>();
        allFeatureValues.addAll(feature.getType().getDefaultValues());
        allFeatureValues.addAll(featureValues.get(feature));
        return allFeatureValues;
    }

    /**
     * Devuelve el valor mínimo de una característica numérico de los productos
     * en el dataset de contenido
     *
     * @param feature Característica para el que se desea conocer el mínimo
     * @return valor mínimo de la característica
     */
    @Override
    public double getMinValue(final Feature feature) {
        if (!feature.getType().isNumerical()) {
            throw new IllegalArgumentException("The feature must be numerical (name='" + feature + "' type='" + feature.getType() + "'");
        }
        if (!minNumerical.containsKey(feature)) {
            Set<Object> values = getAllFeatureValues(feature);
            List<Object> a = new ArrayList<>(values);
            Collections.sort(a, (Object o1, Object o2) -> {
                if (o1 instanceof Number) {
                    if (o2 instanceof Number) {
                        return (int) (((Number) o1).doubleValue() - ((Number) o2).doubleValue());
                    } else {
                        throw new IllegalArgumentException("There is a not numerical value (featureName='" + feature + "' type='" + feature.getType() + "' value='" + o2 + "'");
                    }
                } else {
                    throw new IllegalArgumentException("There is a not numerical value (featureName='" + feature + "' type='" + feature.getType() + "' value='" + o1 + "'");
                }
            });
            minNumerical.put(feature, ((Number) a.get(0)).doubleValue());
        }
        return minNumerical.get(feature);
    }

    /**
     * Devuelve el valor máximo de una característica numérico de los productos
     * en el dataset de contenido
     *
     * @param feature Característica para el que se desea conocer el mínimo
     * @return valor máximo de la característica
     */
    @Override
    public double getMaxValue(final Feature feature) {
        if (!feature.getType().isNumerical()) {
            throw new IllegalArgumentException("The feature must be numerical (name='" + feature + "' type='" + feature.getType() + "'");
        }
        if (!maxNumerical.containsKey(feature)) {
            Set<Object> values = getAllFeatureValues(feature);
            List<Object> a = new ArrayList<>(values);
            Collections.sort(a, (Object o1, Object o2) -> {
                if (o1 instanceof Number) {
                    if (o2 instanceof Number) {
                        return (int) (((Number) o1).doubleValue() - ((Number) o2).doubleValue());
                    } else {
                        throw new IllegalArgumentException("There is a not numerical value (featureName='" + feature + "' type='" + feature.getType() + "' value='" + o2 + "'");
                    }
                } else {
                    throw new IllegalArgumentException("There is a not numerical value (featureName='" + feature + "' type='" + feature.getType() + "' value='" + o1 + "'");
                }
            });
            maxNumerical.put(feature, ((Number) a.get(a.size() - 1)).doubleValue());
        }
        return maxNumerical.get(feature);
    }

    @Override
    public Feature searchFeature(String featureName) {
        return featureGenerator.searchFeature(featureName);
    }

    @Override
    public Feature searchFeatureByExtendedName(String extendedName) {
        return featureGenerator.searchFeatureByExtendedName(extendedName);
    }

    @Override
    public Collection<Long> allIDs() {
        return new TreeSet<>(entitiesById.keySet());
    }

    @Override
    public Entity get(long idItem) throws EntityNotFound {
        if (entitiesById.containsKey(idItem)) {
            return entitiesById.get(idItem);
        } else {
            throw new EntityNotFound(Item.class, idItem);
        }
    }

    @Override
    public boolean contains(Object o) {
        if (o instanceof EntityWithFeatures) {
            EntityWithFeatures name = (EntityWithFeatures) o;
            return entitiesById.containsKey(name.getId()) && entitiesById.get(name.getId()) != null;
        } else {
            return false;
        }
    }

    @Override
    public Object[] toArray() {
        return this.entitiesById.values().toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return this.entitiesById.values().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return c.stream().allMatch(((element) -> this.contains(element)));
    }

    @Override
    public boolean addAll(Collection<? extends Entity> entitys) {
        return entitys.stream().anyMatch((entity) -> add(entity));
    }
}
