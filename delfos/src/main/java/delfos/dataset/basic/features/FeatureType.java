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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import delfos.common.Global;

/**
 * Tipos de características que puede tener un producto.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 (01-Feb-2013) Cambio de nombre feature --> feature.
 * @version 1.0 (19 Octubre 2011)
 * @version 2.0 18-Septiembre-2013 Generalizado para que las características
 * sean aplicables a los usuarios también. Previamente, esta clase se denominaba
 * ItemFeatureType.
 */
public enum FeatureType {

    /**
     * Tipo numérico, que almacena números reales.
     */
    Numerical("_numerical", true),
    /**
     * Tipo unario, que almacena datos conocidos.
     */
    Unary("_unary", false),
    /**
     * Tipo nominal, que almacena valores categóricos.
     */
    Nominal("_nominal", true),
    /**
     * Tipo nominal que almacena múltiples valores nominales, como puede ser que
     * una película tenga como palabras clave "humor" y "comedia". Estos valores
     * se almacenarán como "humor&comedia" en csv.
     */
    MultiNominal("_nominal*", true);

    /**
     * Comprueba el sufijo del nombre de la característica y devuelve el tipo
     * asociado a dicho sufijo.
     *
     * @param featureNameExtended Nombre de la característica con el sufijo que
     * denota su tipo.
     * @return Tipo de la característica.
     * @throws IllegalArgumentException Si el nombre de la característica no
     * tiene ningún sufijo que coincida con los tipos dados.
     */
    public static FeatureType inferTypeByNameWithSuffix(String featureNameExtended) {

        for (FeatureType type : values()) {
            if (featureNameExtended.endsWith(type.sufix)) {
                return type;
            }
        }

        throw new IllegalArgumentException("Cannot infer type from feature name '" + featureNameExtended + "'");
    }
    /**
     * Sufijo del tipo. Se utiliza en los nombres extendidos de las
     * características.
     *
     * @see
     */
    private final String sufix;
    private final boolean skipNullValues;

    /**
     * Crea una característica de producto y le asigna el sufijo que denota su
     * tipo.
     *
     * @param sufix Sufijo para los nombres extendidos de características.
     */
    private FeatureType(String sufix, boolean isSkipNullValues) {
        this.sufix = sufix;
        this.skipNullValues = isSkipNullValues;
    }

    /**
     * Devuelve el sufijo de este tipo de característica. Se utiliza en los
     * nombres extendidos de las características.
     *
     * @return Sufijo para los nombres extendidos.
     *
     * @see ItemFeatureFactory#searchFeatureByExtendedName(java.lang.String)
     */
    public String getSufix() {
        return sufix;
    }

    @Override
    public String toString() {
        return name();
    }

    /**
     * Devuelve el tipo de la característica de producto cuyo nombre coincide
     * con el nombre especificado.
     *
     * <p>
     * <p>
     * Debe coincidir con algún valor de este enumerado, utilizando el método {@link Enum#name()
     * }.
     *
     * @param itemFeatureType Cadena con el tipo de característica.
     * @return Tipo de característica que se corresponde con la cadena indicada.
     */
    public static FeatureType getFeatureType(String itemFeatureType) {
        for (FeatureType featureType : values()) {
            if (featureType.name().equals(itemFeatureType)) {
                return featureType;
            }
        }
        IllegalArgumentException iae = new IllegalArgumentException("Unknown feature type '" + itemFeatureType + "'");
        Global.showError(iae);
        return null;
    }

    /**
     * Recibe una cadena y la convierte a un objeto según el tipo de
     * caracteristica al que pertenezca.
     *
     * @param featureValue
     * @return
     */
    public Object parseFeatureValue(Object featureValue) {
        if (featureValue == null) {
            return null;
        }
        try {
            switch (this) {
                case Nominal:
                    return featureValue;
                case Numerical:
                    return new Double(featureValue.toString());
                case Unary:
                    return "1";
                case MultiNominal:
                    List<String> values = new ArrayList<>();
                    if (featureValue instanceof String) {
                        String featureValueString = (String) featureValue;
                        String[] valuesVector = featureValueString.split("&&");
                        for (String value : valuesVector) {
                            value = value.trim();
                            values.add(value);
                        }

                    } else {
                        if (featureValue instanceof List) {
                            return true;
                        } else {
                            throw new IllegalArgumentException("The feature value '" + featureValue + "' is not a string separated by '&'");
                        }
                    }
                    return values;
                default:
                    throw new UnsupportedOperationException("Type not supported yet.");
            }
        } catch (Throwable ex) {
            throw new IllegalStateException("Cannot parse feature type " + this.name() + " value '" + featureValue + "'", ex);
        }
    }

    /**
     * Convierte el valor de la característica en una representación en forma de
     * cadena de texto. La cadena es recuperable mediante el método {@link FeatureType#parseFeatureValue(java.lang.Object)
     * }.
     *
     * @param value
     * @return
     */
    public String featureValueToString(Object value) {
        if (value == null) {
            return Feature.NULL_VALUE;
        }

        switch (this) {
            case Numerical:
                if (value instanceof Number) {
                    return value.toString();
                } else {
                    throw new IllegalArgumentException("The feature value '" + value + "' is not valid for feature type " + Numerical);
                }
            case Nominal:
                if (value instanceof String) {
                    return value.toString();
                } else {
                    throw new IllegalArgumentException("The feature value '" + value + "' is not valid for feature type " + Nominal);
                }
            case Unary:
                if (value != null) {
                    return "1";
                } else {
                    return "";
                }
            case MultiNominal:
                if (value instanceof List) {
                    List<String> list = (List<String>) value;

                    StringBuilder str = new StringBuilder();

                    if (list.isEmpty()) {
                        return "";
                    } else {
                        Iterator<String> iterator = list.iterator();
                        str.append(iterator.next());
                        for (; iterator.hasNext();) {
                            String string = iterator.next();
                            str.append("&&").append(string);
                        }
                    }
                    return str.toString();
                } else {
                    throw new IllegalArgumentException("The feature value '" + value + "' is not valid for feature type " + Nominal);
                }
            default:
                throw new UnsupportedOperationException("Feature type '" + this + "' not supported yet.");
        }
    }

    public String getFeatureRealName(String extendedName) {
        return extendedName.substring(0, extendedName.lastIndexOf(sufix));
    }

    public boolean isValueCorrect(Object featureValue) {
        if (featureValue == null) {
            return true;
        }

        switch (this) {
            case Numerical:
                if (featureValue instanceof Number) {
                    return true;
                } else {
                    try {
                        Double.parseDouble(featureValue.toString());
                        return true;
                    } catch (NumberFormatException ex) {
                        return false;
                    }
                }
            case Nominal:
                return true;
            case Unary:
                return true;
            case MultiNominal:
                return featureValue instanceof List;
            default:
                throw new UnsupportedOperationException("Type not supported yet.");
        }
    }

    boolean isNumerical() {
        return this == Numerical;
    }

    public String getMySQLfieldType() {
        switch (this) {
            case Nominal:
                return "varchar(255)";
            case Numerical:
                return "float";
            case Unary:
                return "varchar(255)";
            default:
                throw new UnsupportedOperationException("Type not supported yet.");

        }
    }

    Collection<Object> getDefaultValues() {

        Collection<Object> ret = new LinkedList<>();
        switch (this) {
            case Nominal:
                return ret;
            case Numerical:
                return ret;
            case Unary:
                ret.add("1");
                return ret;
            case MultiNominal:
                return ret;
            default:
                throw new UnsupportedOperationException("Type not supported yet.");

        }
    }

    public boolean isSkipNullValues() {
        return skipNullValues;
    }

}
