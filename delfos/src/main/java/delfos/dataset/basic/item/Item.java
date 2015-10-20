package delfos.dataset.basic.item;

import delfos.dataset.basic.features.EntityWithFeatures;
import static delfos.dataset.basic.features.EntityWithFeaturesDefault.checkFeatureAndFeatureValuesArrays;
import delfos.dataset.basic.features.Feature;
import delfos.rs.contentbased.ContentBasedRecommender;
import java.io.Serializable;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Clase que almacena el contenido de un producto. Se utiliza en
 * <code>{@link ContentBasedRecommender}</code> como perfil de los productos.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 */
public class Item implements Comparable<Item>, EntityWithFeatures, Serializable {

    private static final long serialVersionUID = 3034;

    private final int idItem;
    private final String name;
    private final Map<Feature, Object> featuresValues = new TreeMap<>();

    /**
     * Crea un producto sin características. Se crea con nombre "Item [idItem]".
     *
     * @param idItem Id del producto que se crea.
     */
    public Item(int idItem) {
        this(idItem, "Item " + idItem, new Feature[0], new Object[0]);
    }

    /**
     * Crea un producto sin características. Se crea con nombre "Item [idItem]".
     *
     * @param idItem Id del producto que se crea.
     * @param name
     */
    public Item(int idItem, String name) {
        this.idItem = idItem;
        this.name = name;
    }

    /**
     * Crea un producto a partir de sus características.
     *
     * @param idItem
     * @param name
     * @param featureValues Mapa de (característica,valor). El tipo de la
     * característica se infiere según si el valor
     */
    public Item(int idItem, String name, Map<Feature, Object> featureValues) {
        this(idItem, name);

        this.featuresValues.putAll(featureValues);
    }

    /**
     * Constructor de un producto de la base de datos para almacenarlo en
     * memoria
     *
     * @param idItem identificador del producto que se almacena.
     * @param name Nombre del producto-
     * @param features características relevantes del producto
     * @param values vector de valores correspondientes a las características en
     * el vector <code>features</code>
     */
    public Item(int idItem, String name, Feature[] features, Object[] values) {
        this.idItem = idItem;
        this.name = name;

        checkFeatureAndFeatureValuesArrays(features, values);

        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
            Object featureValue = values[i];

            if (featureValue == null && feature.getType().isSkipNullValues()) {
                continue;
            }

            if (!feature.getType().isValueCorrect(featureValue)) {
                throw new IllegalArgumentException("The feature value '" + featureValue + "' for feature '" + feature + "' does not match");
            }

            this.featuresValues.put(features[i], values[i]);
        }
    }

    /**
     * Crea un producto georreferenciado.
     *
     * @param idItem identificador del producto que se almacena.
     * @param name Nombre del producto-
     * @param features características relevantes del producto
     * @param values vector de valores correspondientes a las características en
     * el vector <code>features</code>
     * @param latitude Latitud del producto.
     * @param longitude Longitud del producto.
     */
    public Item(int idItem, String name, Feature[] features, Object[] values, float latitude, float longitude) {
        this.idItem = idItem;
        this.name = name;

        checkFeatureAndFeatureValuesArrays(features, values);

        for (int i = 0; i < features.length; i++) {
            Feature feature = features[i];
//            if (values[i] != null) {
//                continue;
//            }
            Object featureValue = values[i];
//            if (!feature.getType().isValueCorrect(featureValue)) {
//                throw new IllegalArgumentException("The feature value '" + featureValue + "' for feature '" + featureValue + "' does not match");
//            }
            this.featuresValues.put(feature, featureValue);

        }
    }

    /**
     * Devuelve el valor que el producto tiene para una característica dado
     *
     * @param feature característica que se desea consultar
     * @return devuelve un objeto con el valor de la característica. Si la
     * característica es nominal, es de tipo <code>{@link String}</code>; si es
     * numérico, devuelve un <code>{@link Float}</code>
     */
    @Override
    public Object getFeatureValue(Feature feature) {
        if (featuresValues.containsKey(feature)) {
            return featuresValues.get(feature);
        } else {
            //No está definido el valor de la característica feature para el producto idItem
            return null;
        }
    }

    /**
     * Devuelve las características que están definidas para este producto.
     *
     * @return conjunto de características del producto
     */
    @Override
    public Set<Feature> getFeatures() {
        return featuresValues.keySet();
    }

    /**
     * Devuelve el identificador del producto al que pertenece el contenido
     * almacenado en este objeto.
     *
     * @return identificador del producto
     */
    @Override
    public Integer getId() {
        return idItem;
    }

    /**
     * Devuelve el nombre del producto.
     *
     * @return Nombre del producto.
     */
    @Override
    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Item o) {
        int diff = this.getName().compareTo(o.getName());

        if (diff == 0) {
            return this.getId().compareTo(o.getId());
        } else {
            return diff;
        }
    }
}
