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
import delfos.io.xml.parameterowner.parameter.DoubleParameterXML;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Restricción de valores de un parámetro que permite que tome valores reales
 * entre un valor mínimo y un valor máximo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class DoubleParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;
    /**
     * Valor mínimo del parámetro que tiene esta restricción.
     */
    private final double minValue;
    /**
     * Valor máximo del parámetro que tiene esta restricción.
     */
    private final double maxValue;

    /**
     * Crea la restricción para aceptar valores entre cero y uno, con valor por
     * defecto uno.
     */
    public DoubleParameter() {
        this(0, 1, 1);
    }

    /**
     * Crea la restricción con los valores mínimo y máximo indicados. También se
     * especifica el valor por defecto.
     *
     * @param minValue Valor minimo.
     * @param maxValue Valor máximo.
     * @param defaultValue Valor por defecto.
     */
    public DoubleParameter(double minValue, double maxValue, double defaultValue) {
        super(defaultValue);

        this.minValue = minValue;
        this.maxValue = maxValue;

        if (!isCorrect(defaultValue)) {
            throw new UnsupportedOperationException("Invalid default value");
        }
    }

    @Override
    public final boolean isCorrect(Object o) {
        if (o instanceof Number) {
            Number d = (Number) o;
            if (d.doubleValue() >= minValue && d.doubleValue() <= maxValue) {
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
    public double getMin() {
        return minValue;
    }

    /**
     * Devuelve el valor máximo para el parámetro que tiene esta restricción.
     *
     * @return Valor máximo.
     */
    public double getMax() {
        return maxValue;
    }

    @Override
    public Object parseString(String parameterValue) {
        Double d = Double.parseDouble(parameterValue);
        return d;
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return DoubleParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return DoubleParameterXML.getDoubleParameterElement(parameterOwner, parameter);
    }
}
