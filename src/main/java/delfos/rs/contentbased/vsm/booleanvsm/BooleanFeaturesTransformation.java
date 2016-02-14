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
package delfos.rs.contentbased.vsm.booleanvsm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import delfos.rs.contentbased.vsm.booleanvsm.profile.BooleanUserProfile;

/**
 * Objeto que almacena una transformación de características y valores a un
 * vector de ocurrencias.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 10-Octubre-2013
 */
public class BooleanFeaturesTransformation implements Serializable, Iterable<FeatureValue> {

    private static final long serialVersionUID = 1L;
    /**
     * Guarda la correspondencia entre el valor de cada característica y su
     * posición en el perfil booleano.
     */
    private final Map<Feature, Map<Object, Long>> featureValuesIndexes = new TreeMap<Feature, Map<Object, Long>>();
    private final int numFeatures;

    public BooleanFeaturesTransformation(ContentDataset contentDataset) {

        int index = 0;
        for (Feature f : contentDataset.getFeatures()) {
            for (Object value : contentDataset.getAllFeatureValues(f)) {
                addFeatureToIndex(f, value, index++);
            }
        }
        numFeatures = index;
    }

    private long addFeatureToIndex(Feature feature, Object value, long index) {
        if (featureValuesIndexes.containsKey(feature)) {
            if (featureValuesIndexes.get(feature).containsKey(value)) {
                //Todo correcto, ya estaba en su sitio.
            } else {
                featureValuesIndexes.get(feature).put(value, index++);
            }
        } else {
            featureValuesIndexes.put(feature, new TreeMap<Object, Long>());
            featureValuesIndexes.get(feature).put(value, index++);
        }
        return featureValuesIndexes.get(feature).get(value);
    }

    /**
     * Crea un vector para representar el perfil, que sólo permite valores
     * dentro de los definidos en esta transformación booleana.
     *
     * @return
     */
    public MutableSparseVector newProfile() {
        return MutableSparseVector.create(getDomain());
    }

    public Collection<Long> getDomain() {
        List<Long> ret = new ArrayList<Long>(numFeatures);
        for (long i = 0; i < numFeatures; i++) {
            ret.add(i);
        }
        return ret;
    }

    /**
     * Devuelve todos los valores que toma la característica indicada.
     *
     * @param feature Característica para la que se buscan sus posibles valores.
     * @return Valores que toman los productos. Tienen la peculiaridad de que
     * algun ítem tiene para la característica indicada el valor devuelto.
     */
    public Iterable<Object> getAllFeatureValues(Feature feature) {
        ArrayList<Object> ret = new ArrayList<Object>(featureValuesIndexes.get(feature).keySet());
        return ret;
    }

    /**
     * Número de pares (característica, valor) distintos que se dan en el
     * dataset de contenido.
     *
     * @return Número de pares distintos.
     */
    public int sizeOfAllFeatureValues() {
        int size = 0;
        for (Feature feature : featureValuesIndexes.keySet()) {
            size += featureValuesIndexes.get(feature).size();
        }
        return size;
    }

    /**
     * Transforma el vector disperso en un mapa con los valores de las
     * características.
     *
     * @param sparseVector Vector de valores dados en el dominio de esta
     * transformación booleana.
     * @return
     */
    public Map<Feature, Map<Object, Double>> getFeatureValueMap(MutableSparseVector sparseVector) {
        Map<Feature, Map<Object, Double>> ret = new TreeMap<Feature, Map<Object, Double>>();

        for (VectorEntry entry : sparseVector.fast()) {
            long idFeatureValue = entry.getKey();
            double value = entry.getValue();

            FeatureValue featureValuePair = getFeatureValue(idFeatureValue);
            Feature feature = featureValuePair.feature;
            Object featureValue = featureValuePair.value;

            if (!ret.containsKey(feature)) {
                ret.put(feature, new TreeMap<Object, Double>());
            }

            ret.get(feature).put(featureValue, value);

        }

        return ret;
    }

    protected FeatureValue getFeatureValue(long index) {
        for (Feature f : featureValuesIndexes.keySet()) {
            if (featureValuesIndexes.get(f).containsValue(index)) {
                for (Map.Entry<Object, Long> entry : featureValuesIndexes.get(f).entrySet()) {
                    if (entry.getValue().equals(index)) {
                        return new FeatureValue(f, entry.getKey());
                    }
                }
            }
        }

        throw new IndexOutOfBoundsException("The index " + index + " is not defined");
    }

    public long getFeatureIndex(Feature f, Object value) {
        if (f.getType() == FeatureType.Unary) {
            value = "1";
        }

        if (featureValuesIndexes.containsKey(f)) {
            if (featureValuesIndexes.get(f).containsKey(value)) {
                return featureValuesIndexes.get(f).get(value);
            } else {
                throw new IllegalArgumentException("The feature '" + f + "' value '" + value + "' is not defined.");
            }
        } else {
            throw new IllegalArgumentException("The feature '" + f + "' is not defined");
        }

    }

    public List<Float> getFloatVector(SparseVector sparseVector) {

        List<Float> ret = new ArrayList<Float>(numFeatures);
        for (int i = 0; i < numFeatures; i++) {
            ret.add(0f);
        }
        for (VectorEntry entry : sparseVector.fast()) {
            ret.set((int) entry.getKey(), (float) entry.getValue());
        }
        return ret;
    }

    public List<Float> getFloatValuesVector(BooleanUserProfile booleanUserProfile) {
        List<Float> ret = new ArrayList<Float>(numFeatures);
        for (int i = 0; i < numFeatures; i++) {
            ret.add(0f);
        }

        for (Feature feature : booleanUserProfile.getFeatures()) {
            for (Object featureValue : booleanUserProfile.getValuedFeatureValues(feature)) {
                long idFeatureValue = getFeatureIndex(feature, featureValue);
                double featureValueValue = booleanUserProfile.getFeatureValueValue(feature, featureValue);

                ret.set((int) idFeatureValue, (float) featureValueValue);
            }
        }

        return ret;
    }

    public SparseVector getFloatValuesSparseVector(BooleanUserProfile booleanUserProfile) {
        MutableSparseVector userProfile = newProfile();

        for (Feature feature : booleanUserProfile.getFeatures()) {
            for (Object featureValue : booleanUserProfile.getValuedFeatureValues(feature)) {
                long idFeatureValue = getFeatureIndex(feature, featureValue);
                double featureValueValue = booleanUserProfile.getFeatureValueValue(feature, featureValue);

                userProfile.set(idFeatureValue, featureValueValue);
            }
        }

        return userProfile;
    }

    public SparseVector getFloatWeightsSparseVector(BooleanUserProfile booleanUserProfile) {
        MutableSparseVector userProfile = newProfile();

        for (Feature feature : booleanUserProfile.getFeatures()) {
            for (Object featureValue : booleanUserProfile.getValuedFeatureValues(feature)) {
                long idFeatureValue = getFeatureIndex(feature, featureValue);
                double featureValueWeight = booleanUserProfile.getFeatureValueWeight(feature, featureValue);

                userProfile.set(idFeatureValue, featureValueWeight);
            }
        }

        return userProfile;
    }

    public List<Float> getFloatWeightsVector(BooleanUserProfile booleanUserProfile) {
        List<Float> ret = new ArrayList<Float>(numFeatures);
        for (int i = 0; i < numFeatures; i++) {
            ret.add(0f);
        }

        for (Feature feature : booleanUserProfile.getFeatures()) {
            for (Object featureValue : booleanUserProfile.getValuedFeatureValues(feature)) {
                long idFeatureValue = getFeatureIndex(feature, featureValue);
                double featureValueValue = booleanUserProfile.getFeatureValueWeight(feature, featureValue);

                ret.set((int) idFeatureValue, (float) featureValueValue);
            }
        }

        return ret;
    }

    @Override
    public Iterator<FeatureValue> iterator() {
        Collection<FeatureValue> list = new ArrayList<FeatureValue>();

        for (Feature feature : featureValuesIndexes.keySet()) {
            for (Object value : featureValuesIndexes.get(feature).keySet()) {
                list.add(new FeatureValue(feature, value));
            }
        }
        list = Collections.unmodifiableCollection(list);

        return list.iterator();
    }
}
