package delfos.dataset.basic.features;

import java.util.Set;

/**
 * Determina los métodos que una entidad con características, como un usuario o
 * un producto, debe implementar. Generaliza el anterior comportamiento de los
 * productos y el manejo del contenido de los mismos.
 *
* @author Jorge Castro Gallardo
 *
 * @version 18-sep-2013
 */
public interface EntityWithFeatures {

    /**
     * Devuelve el valor que la entidad tiene para una característica dado
     *
     * @param feature característica que se desea consultar
     * @return devuelve un objeto con el valor de la característica. Si la
     * característica es nominal, es de tipo <code>{@link String}</code>; si es
     * numérico, devuelve un <code>{@link Float}</code>
     */
    public Object getFeatureValue(Feature feature);

    /**
     * Devuelve las características que están definidas para este producto.
     *
     * @return conjunto de características de la entidad
     */
    public Set<Feature> getFeatures();

    /**
     * Devuelve el identificador de la entidad al que pertenece el contenido
     * almacenado en este objeto.
     *
     * @return identificador de la entidad
     */
    public Integer getId();

    /**
     * Devuelve el nombre de la entidad.
     *
     * @return Nombre de la entidad.
     */
    public String getName();

    @Override
    public String toString();
}
