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
import delfos.io.xml.parameterowner.parameter.BooleanParameterXML;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Clase que representa una restricción sobre parámetros en la que el valor del
 * parámetro debe ser un valor booleano:
 * <code>false</code>,
 * <code>true</code> {@link Boolean#FALSE} ó {@link Boolean#TRUE}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class BooleanParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;

    /**
     * Constructor de la restricción que asigna un valor por defecto al
     * característica
     *
     * @param defaultValue Valor por defecto del parámetro al que se asigne la
     * restricción
     */
    public BooleanParameter(Boolean defaultValue) {
        super(defaultValue);

        if (!isCorrect(defaultValue)) {
            throw new IllegalArgumentException("Argument isn't correct");
        }
    }

    @Override
    public final boolean isCorrect(Object o) {
        boolean ret = o instanceof Boolean;
        return ret;
    }

    @Override
    public Object parseString(String parameterValue) {
        return Boolean.parseBoolean(parameterValue);
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return BooleanParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return BooleanParameterXML.getBooleanParameterElement(parameterOwner, parameter);
    }
}
