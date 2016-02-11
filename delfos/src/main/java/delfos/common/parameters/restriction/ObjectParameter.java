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

import java.util.Arrays;
import java.util.Collection;
import org.jdom2.Element;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class ObjectParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;
    /**
     * Valores que puede tomar el parámtero.
     */
    private final Object[] _values;

    /**
     * Crea un parámetro que permite un número fijo de valores, que se indican
     * en el constructor.
     *
     * @param values Valores que permite el parámetro.El valor por defecto es el
     * primer valor de la colección.
     * @param defaultValue
     */
    public ObjectParameter(Object[] values, Object defaultValue) {
        this(Arrays.asList(values), defaultValue);
    }

    /**
     * Constructor de la restricción que asigna los distintos valores que puede
     * tomar. Actualmente se comprueba que un valor nuevo sea
     *
     * @param values Valores que permite el parámetro. El valor por defecto es
     * el primer valor de la colección.
     * @param defaultValue
     */
    public ObjectParameter(Collection<? extends Object> values, Object defaultValue) {
        super(defaultValue);

        if (values == null) {
            throw new IllegalArgumentException("The list of values cannot be null.");
        }

        if (values.isEmpty()) {
            throw new IllegalArgumentException("The list of values is empty.");
        }

        if (defaultValue instanceof ParameterOwner) {
            IllegalStateException ise = new IllegalStateException(defaultValue.toString() + " is a " + ParameterOwner.class.getSimpleName() + " therefore it should be used with a " + ParameterOwnerRestriction.class.getSimpleName() + " for this parameter.");
            throw ise;
        }

        for (Object value : values) {
            if (value instanceof ParameterOwner) {
                IllegalStateException ise = new IllegalStateException(value.toString() + " is a " + ParameterOwner.class.getSimpleName() + " therefore it should be used with a " + ParameterOwnerRestriction.class.getSimpleName() + " for this parameter.");
                throw ise;
            }
        }

        this._values = new Object[values.size()];

        int i = 0;
        for (Object value : values) {
            _values[i] = value;
            i++;
        }

        if (!isCorrect(this.defaultValue)) {
            isCorrect(defaultValue);
            throw new IllegalArgumentException("The default value isn't correct");
        }
    }

    @Override
    public final boolean isCorrect(Object newValue) {
        if (_values != null) {
            for (Object value : _values) {
                if (newValue.equals(value)) {
                    return true;
                }
            }
        }
        Global.showError(new IllegalArgumentException("WARNING: " + newValue + " isnt allowed\n"));
        Global.showWarning(newValue.getClass() + " isnt allowed\n");
        return false;
    }

    /**
     * Deuvelve todos los valores permitidos de esta restricción.
     *
     * <p>
     * <p>
     * NOTA: Esta función es útil para dar a elegir al usuario entre los valores
     * del parámetro que tenga esta restricción.
     *
     * @return Valores permitidos de esta restricción.
     */
    public Object[] getAllowed() {
        Object[] ret = new Object[_values.length];
        System.arraycopy(_values, 0, ret, 0, _values.length);
        return ret;
    }

    @Override
    public Object parseString(String parameterValue) throws CannotParseParameterValue {
        Object ret = null;
        for (Object o : _values) {
            if (o instanceof Enum && ((Enum) o).name().equals(parameterValue)) {
                ret = o;
            } else if (o instanceof String && o.equals(parameterValue)) {
                ret = o;
            } else if (o.getClass().getSimpleName().equals(parameterValue)) {
                ret = o;
            }
        }

        if (ret == null) {
            throw new CannotParseParameterValue("The value '" + parameterValue + "' is not valid for this restriction configuration.");
        }

        return ret;
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        //TODO: Implementar método.
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        //TODO: Implementar método.
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
