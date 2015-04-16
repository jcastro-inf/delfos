package delfos.rs.nonpersonalised.meanrating.arithmeticmean;

import java.io.Serializable;

/**
 * Clase para almacenar el modelo del sistema de recomendación basado en
 * valoración media de los productos. Almacena un producto y su valoración
 * media.
 *
 * @see MeanRatingRS
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 21-Feb-2013
 */
public class MeanRating implements Serializable, Comparable<MeanRating> {
    
    private static final long serialVersionUID = 103L;

    /**
     * Id del producto.
     */
    private int _idItem;
    /**
     * Valor de preferencia medio del producto.
     */
    private Number _preference;

    
    /**
     * Se implementa el constructor por defecto para que el objeto sea
     * serializable.
     */
    protected MeanRating() {
    }
    

    /**
     * Constructor que asigna los valores del objeto.
     *
     * @param idItem Producto sobre el que se realiza la recomendación
     * @param preference Valor de preferencia medio del producto.
     */
    public MeanRating(Integer idItem, Number preference) {
        this._idItem = idItem;
        this._preference = preference;
    }

    /**
     * ID del producto al que se refiere este objeto.
     *
     * @return ID del producto al que se refiere este objeto.
     */
    public int getIdItem() {
        return _idItem;
    }

    /**
     * Valor de preferencia medio del producto. Cuanto mayor es, más probable es
     * que el producto sea relevante para un usuario.
     *
     * @return Valoración media del producto.
     */
    public Number getPreference() {
        return _preference;
    }

    @Override
    public String toString() {
        return "item:" + _idItem + "-->" + _preference;
    }

    /**
     * Compara este objeto con otro del mismo tipo, teniendo en cuenta el valor
     * de preferencia medio de cada uno. Los ordena en orden descendente.
     *
     * {@inheritDoc }
     */
    @Override
    public int compareTo(MeanRating o) {
        float diff = _preference.floatValue() - o._preference.floatValue();
        if (diff == 0) {
            return 0;
        } else if (diff > 0) {
            return -1;
        } else {
            return 1;
        }
    }
}
