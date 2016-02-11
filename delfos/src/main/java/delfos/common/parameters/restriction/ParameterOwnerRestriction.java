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

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import org.jdom2.Element;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.factories.ParameterOwnerFactory;
import delfos.io.xml.parameterowner.parameter.ParameterOwnerParameterXML;

/**
 * Encapsula el comportamiento de una restricción de valores de parámetro que
 * sólo permite seleccionar sistemas de recomendación que concuerden con los
 * tipos pasados por parámetro en el constructor. De esta manera, se pueden
 * asignar sistemas de recomendación del mismo tipo o que hereden del mismo.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class ParameterOwnerRestriction extends ParameterRestriction {

    private static final long serialVersionUID = 1L;

    private final Class<? extends ParameterOwner> tipoPermitido;

    /**
     * Constructor de una restricción de valores del parámetro para que sólo
     * permita objetos de tipo {@link ParameterOwner}. Si se tiene alguna
     * restricción más concreta sobre el tipo que deben implementar los valores,
     * se debe especificar mediante el parámetro <code>tiposPermitidos</code>.
     *
     * @param tipoPermitido Tipo de {@link ParameterOwner} que el parámetro
     * puede tomar como valor.
     * @param defaultParameterOwner valor por defecto que se asigna al
     * parámetro. Debe ser de alguno de los tipos indicados en el parámetro.
     * <code>tiposPermitidos</code>.
     */
    public ParameterOwnerRestriction(Class<? extends ParameterOwner> tipoPermitido, ParameterOwner defaultParameterOwner) {
        super(defaultParameterOwner);
        this.tipoPermitido = tipoPermitido;

        defaultParameterOwner.getParameterOwnerType().loadFactory();
        if (!isCorrect(defaultParameterOwner)) {
            throw new IllegalArgumentException("The default value isn't correct");
        }
    }

    /**
     * {@inheritDoc }
     *
     * Comprueba si el objeto indicado por parámetro es de alguno de los tipos
     * permitidos. Esto lo hace usando reflectividad, comprobando si las clases
     * que se indicaron como permitidas pueden almacenar el objeto, es decir,
     * comprueba si alguno de los tipos permitidos es compatible con el objeto.
     *
     * @param newValue Nuevo valor para este parámetro.
     * @return Devuelve true si el nuevo valor es compatible con alguno de los
     * tipos permitidos.
     */
    @Override
    public final boolean isCorrect(Object newValue) {
        return (tipoPermitido.isAssignableFrom(newValue.getClass()));
    }

    @Override
    public Object parseString(String parameterValue) {
        ParameterOwner defaultValueParameterOwner = (ParameterOwner) defaultValue;
        ParameterOwner parameterOwnerParsedValue = defaultValueParameterOwner
                .getParameterOwnerType()
                .createObjectFromClassName(parameterValue);

        if (parameterOwnerParsedValue == null) {
            parameterOwnerParsedValue = defaultValueParameterOwner
                    .getParameterOwnerType()
                    .createObjectFromClassName(parameterValue);
        }
        return parameterOwnerParsedValue;
    }

    /**
     * Devuelve todos los {@link ParameterOwner} que pueden ser asignados
     * atendiendo a los tipos que se indicaron como permitidos.
     *
     * @return {@link ParameterOwner} que el parámetro puede tomar como valor
     * @deprecated La función no se debe utilizar, sino que se deben recuperar
     * todos los sistemas de recomendación y comprobar cuáles de ellos son
     * válidos para esta restricción.
     */
    public Object[] getAllowed() {
        Collection<ParameterOwner> ret = new LinkedList<>();

        ret.addAll(ParameterOwnerFactory.getInstance().getAllClasses());

        for (Iterator<ParameterOwner> it = ret.iterator(); it.hasNext();) {
            ParameterOwner po = it.next();
            if (!isCorrect(po)) {
                it.remove();
            }
        }

        if (ret.isEmpty()) {
            throw new IllegalStateException("The restriction does not have any allowed value.");
        }

        return ret.toArray();
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return ParameterOwnerParameterXML.getParameterOwnerParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return ParameterOwnerParameterXML.getParameterOwnerElement(parameterOwner, parameter);
    }
}
