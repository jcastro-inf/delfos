package delfos.dataset.basic.features;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import delfos.common.exceptions.dataset.entity.EntityNotFound;

/**
 * Interfaz que implementan los objetos que contengan
 * {@link EntityWithFeatures}. Proporciona métodos útiles sobre las entidades,
 * sus características y los valores de las mismas.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 4-Octubre-2013
 * @param <Entity>
 */
public interface CollectionOfEntitiesWithFeatures<Entity extends EntityWithFeatures> extends Iterable<Entity> {

    public void add(Entity entity) throws EntityNotFound;

    public Entity get(int idEntity) throws EntityNotFound;

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

    public Collection<Integer> getAllID();

    public Map<Feature, Object> parseEntityFeaturesAndAddToExisting(int idEntity, Map<String, String> features) throws EntityNotFound;
}
