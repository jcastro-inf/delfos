package delfos.dataset.basic.features;

import java.io.Serializable;

/**
 * Objeto que almacena la información referente a una característica del
 * contenido de una entidad cualquiera dentro de un sistema de recomendación.
 *
* @author Jorge Castro Gallardo
 *
 * @version 2.0 18-Septiembre-2013 Generalizado para que las características
 * sean aplicables a los usuarios también. Previamente, esta clase se denominaba
 * ItemFeature.
 */
public class Feature implements Comparable<Feature>, Serializable {

    /**
     * Valor que se utiliza para denotar valores perdidos de las
     * características.
     */
    public static final String NULL_VALUE = "";
    private static final long serialVersionUID = 101L;

    /**
     * Comprueba si la cadena es identificativa de valor nulo.
     *
     * @param recordValue
     * @return
     */
    public static boolean isNullValue(String recordValue) {
        if (recordValue.equals(Feature.NULL_VALUE)) {
            return true;
        }
        if (recordValue.equals("NULL")) {
            return true;
        }
        if (recordValue.equals("<null>")) {
            return true;
        }
        if (recordValue.equals("?")) {
            return true;
        }
        return false;
    }
    /**
     * Nombre de esta característica.
     */
    private final String name;
    /**
     * Tipo de esta característica.
     */
    private final FeatureType type;
    /**
     * Indice de la característica en {@link ItemFeatureFactory}, que indica en
     * qué orden fue creado.
     */
    private final int index;

    /**
     * Tipo de esta característica, es decir, si es nominal, numérica, etc.
     *
     * @return Tipo de la característica.
     */
    public FeatureType getType() {
        return type;
    }

    /**
     * Devuelve el nombre de esta característica.
     *
     * @return Nombre de esta característica.
     */
    public String getName() {
        return name;
    }

    /**
     * Devuelve el nombre extendido de la característica, que añade el sufijo
     * que denota su tipo.
     *
     * @return Nombre extendido de la característica.
     */
    public String getExtendedName() {
        return getName() + type.getSufix();
    }

    /**
     * Se implementa el constructor por defecto para que el objeto sea
     * serializable.
     */
    protected Feature() {
        this.name = null;
        this.type = null;
        this.index = -1;
    }

    /**
     * Constructor de una característica de contenido de productos.
     *
     * @param name Nombre de la caracerística.
     * @param type Tipo de la característica.
     * @param index Índice que indica en qué orden fue creada.
     */
    protected Feature(String name, FeatureType type, int index) {
        this.name = name;
        this.type = type;
        this.index = index;
    }

    /**
     * Compara los productos utilizando su orden de creación.
     *
     * {@inheritDoc }
     */
    @Override
    public int compareTo(Feature o) {
        return this.index - o.index;
    }

    /**
     * Comprueba si el objeto comparado es de la misma clase y tiene los mismos
     * atributos. Si es así, considera que los objetos son iguales.
     *
     * {@inheritDoc }
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Feature) {
            Feature itemFeature = (Feature) obj;
            if (name.equals(itemFeature.name)
                    && type.equals(itemFeature.type)
                    && index == itemFeature.index) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Genera el código hash a partir del orden de creación de la
     * característica.
     *
     * {@inheritDoc }
     */
    @Override
    public int hashCode() {
        int hash = 3;
        hash = 53 * hash + this.index;
        return hash;
    }

    /**
     * Devuelve el nombre de la característica.
     *
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return name;
    }

    protected int getIndex() {
        return index;
    }
}
