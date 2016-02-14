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

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeMap;

/**
 * Almacena los características de un item que están disponibles (que han sido
 * creados) y los pone a disposición del content dataset al que pertenece.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unkown date
 * @version 1.1 1-Feb-2013
 * @version 2.0 15-Mar-2013 Eliminación del patrón singleton en esta clase, para
 * que cada content dataset tenga distintas características.
 *
 * @version 2.0 18-Septiembre-2013 Generalizado para que las características
 * sean aplicables a los usuarios también. Previamente, esta clase se denominaba
 * ItemFeatureGenerator.
 */
public class FeatureGenerator {

    /**
     * Características definidas para los productos, indexadas por nombre.
     */
    private final TreeMap<String, Feature> itemFeatures;
    /**
     * Índice de creación de la siguiente característica. Cada vez que se usa se
     * debe incrementar.
     */
    private int index = 0;

    /**
     * Constructor por defecto, que crea la lista de características definidas
     * vacía.
     */
    public FeatureGenerator() {
        itemFeatures = new TreeMap<String, Feature>();
    }

    public boolean containsFeature(String featureName) {
        return itemFeatures.containsKey(featureName);
    }

    /**
     * Crea una característica a partir de su nombre y tipo. Si existe, lanza
     * una excepción, por lo que se aconseja comprobar si esta existe con {@link  FeatureGenerator#exists(java.lang.String, delfos.Dataset.Basic.Features.FeatureType)
     * }.
     *
     * @param featureName Nombre de la característica.
     * @param type Tipo de la característica.
     * @return Característica creada.
     *
     * @throws IllegalArgumentException Si ya existe una característica con el
     * mismo nombre pero distinto tipo.
     */
    public Feature createFeature(String featureName, FeatureType type) {
        if (containsFeature(featureName)) {
            throw new IllegalArgumentException("ERROR: Feature '" + featureName + "' with type: '" + type.name() + "' already exists");
        } else {
            Feature f = new Feature(featureName, type, getIndex());
            itemFeatures.put(featureName, f);
            return f;
        }
    }

    /**
     * Obtiene una característica a partir de su nombre. Si no existe devuelve
     * null.
     *
     * @param featureName Nombre de la característica.
     * @return Característica que coincide con los parámetros indicados. Si
     * ninguna coincide, devuelve null.
     */
    public Feature searchFeature(String featureName) {
        if (itemFeatures.containsKey(featureName)) {
            Feature f = itemFeatures.get(featureName);
            return f;
        } else {
            return null;
        }
    }

    /**
     * Devuelve todas las características conocidas, ordenadas por su orden de
     * creación.
     *
     * @return Lista ordenada de características.
     */
    public List<Feature> getSortedFeatures() {
        List<Feature> ret = new LinkedList<>();
        ret.addAll(itemFeatures.values());

        Collections.sort(ret, (Feature o1, Feature o2) -> o1.getIndex() - o2.getIndex());

        return ret;
    }

    /**
     * Busca una característica a partir de su nombre extendido. El nombre
     * extendido de la característica contiene un sufijo que indica su tipo. Si
     * no existía, la crea.
     *
     * @param extendedName Nombre extendido de la característica, que contiene
     * información sobre su tipo.
     * @return Característica a la que se refiere el nombre extendido.
     */
    public Feature searchFeatureByExtendedName(String extendedName) {
        String realName;

        FeatureType featureType = FeatureType.inferTypeByNameWithSuffix(extendedName);
        realName = featureType.getFeatureRealName(extendedName);

        if (!containsFeature(realName)) {
            createFeature(realName, featureType);
        }
        Feature featureCreated = itemFeatures.get(realName);

        if (featureCreated.getType() != featureType) {
            throw new IllegalArgumentException("The type does not match. (expected '" + featureType + "', actual type '" + featureCreated.getType() + "'");
        }

        return featureCreated;
    }

    /**
     * Devuelve el valor del índice para la siguiente característica que se cree
     * y se incrementa el valor del mismo.
     *
     * @return Indice de la característica.
     */
    private int getIndex() {
        return index++;
    }

    public Feature createFeatureByExtendedName(String featureNameExtended) {
        FeatureType featureType = FeatureType.inferTypeByNameWithSuffix(featureNameExtended);
        String realName = featureType.getFeatureRealName(featureNameExtended);
        if (!containsFeature(realName)) {
            createFeature(realName, featureType);
        }

        Feature featureCreated = itemFeatures.get(realName);
        if (featureCreated.getType() != featureType) {
            throw new IllegalArgumentException("The type does not match. (expected '" + featureType + "', actual type '" + featureCreated.getType() + "'");
        }

        return featureCreated;
    }
}
