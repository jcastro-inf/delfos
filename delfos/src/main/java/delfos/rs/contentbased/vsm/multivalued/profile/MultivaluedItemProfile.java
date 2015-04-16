package delfos.rs.contentbased.vsm.multivalued.profile;

import java.io.Serializable;
import delfos.dataset.basic.features.Feature;
import delfos.rs.ItemProfile;

/**
 * Define los métodos de un perfil de producto multivaluado para sistemas de
 * recomendación basados en contenido.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 14-Octubre-2013
 */
public interface MultivaluedItemProfile extends Serializable, ItemProfile {

    /**
     * Devuelve el Id del producto al que se refiere este perfil.
     *
     * @return Id del producto.
     */
    @Override
    public int getId();

    /**
     * Devuelve las características definidas para este perfil de producto.
     *
     * @return Características.
     */
    public Iterable<Feature> getFeatures();

    /**
     * Devuelve el valor para la característica indicada.
     *
     * @param itemFeature Característica que se busca.
     * @return Valor de la característica indicada.
     */
    public Object getFeatureValue(Feature itemFeature);
}
