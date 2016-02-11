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
package delfos.common.parameters.restriction;

import org.jdom2.Element;
import delfos.io.xml.parameterowner.parameter.IntegerParameterXML;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Restricción de valores de un parámetro que permite que tome valores enteros
 * (representable con un {@link Integer}) entre un valor mínimo y un valor
 * máximo dados.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class IntegerParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;
    /**
     * Valor mínimo del parámetro que tiene esta restricción.
     */
    private final int minValue;
    /**
     * Valor máximo del parámetro que tiene esta restricción.
     */
    private final int maxValue;

    /**
     * Crea una restricción de parámetros enteros con un valor mínimo y máximo
     * especificados.
     *
     * @param minValue Valor mínimo.
     * @param maxValue Valor máximo.
     * @param defaultValue Valor por defecto.
     *
     * @throws IllegalArgumentException Si el valor por defecto no satisface la
     * restricción.
     */
    public IntegerParameter(int minValue, int maxValue, int defaultValue) {
        super(defaultValue);

        this.minValue = minValue;
        this.maxValue = maxValue;

        if (!isCorrect(defaultValue)) {
            throw new UnsupportedOperationException("Invalid default value");
        }
    }

    @Override
    public final boolean isCorrect(Object o) {
        if (o instanceof Integer) {
            Integer d = (Integer) o;
            if (d >= minValue && d <= maxValue) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    /**
     * Devuelve el valor mínimo para el parámetro que tiene esta restricción.
     *
     * @return Valor mínimo.
     */
    public int getMin() {
        return minValue;
    }

    /**
     * Devuelve el valor máximo para el parámetro que tiene esta restricción.
     *
     * @return Valor máximo.
     */
    public int getMax() {
        return maxValue;
    }

    @Override
    public Object parseString(String parameterValue) {
        Integer ret = Integer.parseInt(parameterValue);
        return ret;
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return IntegerParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return IntegerParameterXML.getIntegerParameterElement(parameterOwner, parameter);
    }
}
