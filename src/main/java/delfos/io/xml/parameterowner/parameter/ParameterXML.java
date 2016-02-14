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
package delfos.io.xml.parameterowner.parameter;

import org.jdom2.Element;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.restriction.BooleanParameter;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.FileParameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.parameters.restriction.LongParameter;
import delfos.common.parameters.restriction.ObjectParameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
import delfos.common.parameters.restriction.PasswordParameter;
import delfos.common.parameters.restriction.RecommenderSystemParameterRestriction;
import delfos.common.parameters.restriction.StringParameter;

/**
 * Clase que realiza el almacenamiento/recuperación de los parámetros de un
 * {@link ParameterOwner} en un elemento XML
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 06/12/2012
 */
public class ParameterXML {

    /**
     * Nombre del elemento XML que contiene información de un parámetro
     */
    public static final String PARAMETER_ELEMENT_NAME = "Parameter";
    /**
     * Característica en el que se almacena el nombre del parámetro
     */
    public static final String PARAMETER_NAME = "parameterName";
    public static final String PARAMETER_TYPE = "parameterType";
    public static String PARAMETER_VALUE = "value";

    /**
     * Obtiene el valor del parámetro descrito en el elemento de XML y lo asigna
     * al objeto indicado por parámetro
     *
     * @param parameterOwner Objeto al que se asignan los parámetros.
     * @param elementParameter Elemento que contiene los valores de los
     * parámetros.
     * @return Valor del parámetro
     */
    public static Object getParameterValue(ParameterOwner parameterOwner, Element elementParameter) {
        Object value = null;

        String parameterType = elementParameter.getAttributeValue(PARAMETER_TYPE);

        if (IntegerParameter.class.getSimpleName().equals(parameterType)) {
            value = IntegerParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (LongParameter.class.getSimpleName().equals(parameterType)) {
            value = LongParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (FloatParameter.class.getSimpleName().equals(parameterType)) {
            value = FloatParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (BooleanParameter.class.getSimpleName().equals(parameterType)) {
            value = BooleanParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (RecommenderSystemParameterRestriction.class.getSimpleName().equals(parameterType)) {
            value = RecommenderSystemParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (FileParameter.class.getSimpleName().equals(parameterType)) {
            value = FileParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (ObjectParameter.class.getSimpleName().equals(parameterType)) {
            value = ObjectParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (PasswordParameter.class.getSimpleName().equals(parameterType)) {
            value = PasswordParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (StringParameter.class.getSimpleName().equals(parameterType)) {
            value = StringParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (DirectoryParameter.class.getSimpleName().equals(parameterType)) {
            value = DirectoryParameterXML.getParameterValue(parameterOwner, elementParameter);
        }

        if (ParameterOwnerRestriction.class.getSimpleName().equals(parameterType)) {
            value = ParameterOwnerParameterXML.getParameterOwnerParameterValue(parameterOwner, elementParameter);
        }



        if (value == null) {
            Global.showWarning("Not Implemented for " + parameterType + "\n");
            Global.showError(new IllegalStateException("The restriction is not known"));

            throw new UnsupportedOperationException("Not Implemented for " + parameterType + "\n");
        }

        return value;
    }

    //TODO: Documentación
    public static Element getElement(ParameterOwner parameterOwner, Parameter p) {
        Element ret = null;

        if (p.getRestriction() instanceof IntegerParameter) {
            ret = IntegerParameterXML.getIntegerParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof FloatParameter) {
            ret = FloatParameterXML.getFloatParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof BooleanParameter) {
            ret = BooleanParameterXML.getBooleanParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof RecommenderSystemParameterRestriction) {
            ret = RecommenderSystemParameterXML.getRecommenderSystemParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof FileParameter) {
            ret = FileParameterXML.getFileParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof ObjectParameter) {
            ret = ObjectParameterXML.getObjectParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof PasswordParameter) {
            ret = PasswordParameterXML.getPasswordParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof StringParameter) {

            ret = StringParameterXML.getStringParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof LongParameter) {
            ret = LongParameterXML.getLongParameterElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof ParameterOwnerRestriction) {
            ret = ParameterOwnerParameterXML.getParameterOwnerElement(parameterOwner, p);
        }

        if (p.getRestriction() instanceof DirectoryParameter) {
            ret = DirectoryParameterXML.getDirectoryParameterElement(parameterOwner, p);
        }

        if (ret == null) {
            Global.showWarning("Not Implemented for " + p.getRestriction().getName() + "\n");
            Global.showError(new IllegalStateException("The restriction is not known"));
            throw new UnsupportedOperationException("Not Implemented for " + p.getRestriction().getName() + "\n");
        }

        return ret;
    }

    public static Parameter getParameter(ParameterOwner parameterOwner, Element parameterElement) {
        String parameterName = parameterElement.getAttributeValue(PARAMETER_NAME);
        return parameterOwner.getParameterByName(parameterName);
    }
}
