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

import java.io.Serializable;
import org.jdom2.Element;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Objeto encargado de comprobar si los valores de los parámetros son correctos.
 * Tiene implementaciones para las restricciones más comunes: valor entero
 * {@link IntegerParameter}, valor real {@link FloatParameter}, valor booleano
 * {@link BooleanParameter} y objetos concretos {@link ObjectParameter}
 *
 * <p>
 * <p>
 * Version 1.1: Se establecen los métodos de entrada/salida a XML.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public abstract class ParameterRestriction implements Serializable {

    private static final long serialVersionUID = 116L;

    /**
     * Constructor por defecto de una restricción de parámetro.
     *
     * @param defaultValue Valor por defecto del parámetro que se crea.
     */
    protected ParameterRestriction(Object defaultValue) {
        this.defaultValue = defaultValue;
    }
    /**
     * Valor por defecto del parámetro.
     */
    protected Object defaultValue;

    /**
     * Método que comprueba si el nuevo valor del parámetro es correcto según la
     * restricción del mismo
     *
     * @param o nuevo valor del parámetro
     * @return Devuelve <code>true</code> si el parámetro es un parámetro válido
     * y <code>false</code> si no lo es.
     */
    public abstract boolean isCorrect(Object o);

    /**
     * Devuelve el valor por defecto del parámetro al que pertenece esta
     * restricción
     *
     * @return valor válido asignado por defecto al parámetro
     */
    public final Object getDefaultValue() {
        return defaultValue;
    }

    /**
     * Convierte la cadena en un valor válido de acuerdo con la restricción del
     * parámetro
     *
     * @param parameterValue
     * @return objeto que corresponde a la cadena según la restricción del
     * parámetro. Devuelve null si no hay correspondencia correcta entre la
     * cadena y un valor correcto de la característica
     * @throws
     * delfos.common.parameters.restriction.CannotParseParameterValue
     */
    public abstract Object parseString(String parameterValue) throws CannotParseParameterValue;

    /**
     * Devuelve el nombre de la clase que implementa esta restricción.
     *
     * @return Nombre de la clase que implementa la restricción.
     */
    public String getName() {
        return this.getClass().getSimpleName();
    }

    /**
     * Método para obtener el valor de un parámetro a partir del elemento XML
     * que lo describe.
     *
     * @param parameterOwner Objeto que contiene el parámetro.
     * @param elementParameter Elemento que describe el valor del parámetro.
     * @return Valor del parámetro.
     */
    public abstract Object getValue(ParameterOwner parameterOwner, Element elementParameter);

    /**
     * Método para obtener el elemento XML que describe el valor del parámetro.
     *
     * @param parameterOwner Objeto que contiene el parámetro.
     * @param parameter Parámetro que se desea obtener.
     * @return Elemento XML que describe el valor del mismo.
     */
    public abstract Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter);
}
