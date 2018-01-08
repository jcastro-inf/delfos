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

import delfos.common.exceptions.dataset.entity.EntityNotFound;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Interfaz que implementan los objetos que contengan
 * {@link EntityWithFeatures}. Proporciona métodos útiles sobre las entidades,
 * sus características y los valores de las mismas.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 4-Octubre-2013
 * @param <Entity>
 */
public interface CollectionOfEntitiesWithFeatures<Entity extends EntityWithFeatures> extends Iterable<Entity>, Collection<Entity> {

    public Entity get(long idEntity) throws EntityNotFound;

    /**
     * Devuelve todas las posibles características que un objeto del dataset
     * puede tener
     *
     * @return vector de características
     */
    public Feature[] getFeatures();

    /**
     * Devuelve los valores conocidos para una característica determinada
     *
     * @param feature característica para la que se desea conocer todos los
     * valores posibles
     * @return conjunto de valores de la característica
     */
    public Set<Object> getAllFeatureValues(Feature feature);

    /**
     * Devuelve el valor mínimo de una característica numérico de los productos
     * en el dataset de contenido
     *
     * @param feature Característica para el que se desea conocer el mínimo
     * @return valor mínimo de la característica
     */
    public double getMinValue(Feature feature);

    /**
     * Devuelve el valor máximo de una característica numérico de los productos
     * en el dataset de contenido
     *
     * @param feature Característica para el que se desea conocer el mínimo
     * @return valor máximo de la característica
     */
    public double getMaxValue(Feature feature);

    /**
     * Obtiene una característica a partir de su nombre y tipo. Si no existe, se
     * crea y se añade a la lista de características.
     *
     * @param featureName Nombre de la característica.
     * @return Característica que coincide con los parámetros indicados.
     *
     * @throws IllegalArgumentException Si ya existe una característica con el
     * mismo nombre pero distinto tipo.
     */
    public Feature searchFeature(String featureName);

    /**
     * Busca una característica a partir de su nombre extendido. El nombre
     * extendido de la característica contiene un sufijo que indica su tipo.
     *
     * @param extendedName Nombre extendido de la característica, que contiene
     * información sobre su tipo.
     * @return Característica a la que se refiere el nombre extendido.
     */
    public Feature searchFeatureByExtendedName(String extendedName);

    public Map<Feature, Object> parseEntityFeatures(Map<String, String> features);

    public Collection<Long> allIDs();

    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(long idEntity, Map<String, String> features) throws EntityNotFound;

    @Override
    public default boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public default boolean remove(Object o) {
        throw new UnsupportedOperationException("Not allowed to delete entities.");
    }

    @Override
    public default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public default void clear() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
