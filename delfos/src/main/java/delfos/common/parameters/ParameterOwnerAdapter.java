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
package delfos.common.parameters;

import delfos.common.Global;
import delfos.common.StringsOrderings;
import delfos.common.parameters.restriction.CannotParseParameterValue;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Clase que define el comportamiento de cualquier objeto al que se le puedan
 * asignar parámetros para determinar su comportamiento
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 22 de Febrero de 2013
 */
public abstract class ParameterOwnerAdapter implements ParameterOwner {

    private static final long serialVersionUID = 120L;

    public static final int CHARACTER_LENGTH_ALIAS_WARNING = 60;

    private String oldAlias = null;

    public ParameterOwnerAdapter() {
        addParameter(ALIAS);
        setParameterValue(ALIAS, getName());

        oldAlias = getAlias();
    }

    @Override
    public final String getAlias() {
        return (String) getParameterValue(ALIAS);
    }

    @Override
    public void setAlias(String alias) {
        setParameterValue(ALIAS, alias);
    }

    @Override
    public String getShortName() {
        String name = getName();
        return name.substring(0, Math.min(name.length(), 4));
    }

    public static String getShortName(ParameterOwner po) {
        String name = po.getName();
        return name.substring(0, Math.min(name.length(), 4));
    }
    /**
     * Almacena para cada parámetro el valor que posee.
     */
    private final Map<Parameter, Object> parameterValues = new TreeMap<>();
    /**
     * Almacena los objetos que desean ser notificados de cambios en los
     * parámetros de este objeto.
     */
    private final Collection<ParameterListener> parammeterListeners = new LinkedList<>();

    /**
     * Añade un nuevo parámetro a la lista de parametros del
     * {@link ParameterOwner}
     *
     * @param parameter nuevo parámetro del {@link ParameterOwner}
     */
    protected final void addParameter(Parameter parameter) {
        if (parameterValues.containsKey(parameter)) {
            Global.showWarning("Parameter " + parameter.getName() + " already added in " + this.getName());
        }
        parameterValues.put(parameter, parameter.getDefaultValue());
        fireParammeterChangedEvent();
    }

    /**
     * Añade un listener para que sea notificado de todos los cambios en los
     * parámetros del {@link ParameterOwner}, tanto de parámetros añadidos como
     * de cambios del valor asignado a los mismos.
     *
     * @param listener objeto a notificar de los cambios
     */
    @Override
    public final void addParammeterListener(ParameterListener listener) {
        this.parammeterListeners.add(listener);
        listener.parameterChanged();
    }

    /**
     * Elimina un listener para que no sea notificado nunca más de los cambios
     * en los parámetros del {@link ParameterOwner}.
     *
     * @param listener objeto que no desea ser notificado más de los cambios
     */
    @Override
    public final void removeParammeterListener(ParameterListener listener) {
        this.parammeterListeners.remove(listener);
    }

    /**
     * Método que se invoca cuando ocurre algún cambio en los parámetros de este
     * objeto. Notifica a todos los observadores registrados.
     */
    private void fireParammeterChangedEvent() {
        for (ParameterListener listener : parammeterListeners) {
            listener.parameterChanged();
        }
    }

    /**
     * Devuelve el valor asignado al parámetro especificado.
     *
     * @param p Parámetro del que se desea conocer su valor
     * @return valor del parámetro que se consulta
     */
    @Override
    public final Object getParameterValue(Parameter p) {
        if (parameterValues.containsKey(p)) {
            Object ret = parameterValues.get(p);

            if (!p.isCorrect(ret)) {
                throw new IllegalArgumentException("Error!");
            }
            return ret;
        } else {
            throw new IllegalArgumentException(this.getClass().getName() + " hasn't the parammeter " + p.getName() + "[" + p + "]");
        }
    }

    /**
     * Asigna al parámetro p el valor value. Si el parámetro no es correcto (por
     * que no coincida el tipo o la restricción sobre el mismo) lanza una
     * excepción
     *
     * @param p Parámetro al que se desea asignar un nuevo valor
     * @param value Nuevo valor para el parámetro
     * @return devuelve el valor del objeto si se ha conseguido asignar. Null si
     * viola las restricciones del parámetro o el parámetro no existe
     */
    @Override
    public final Object setParameterValue(Parameter p, Object value) {
        if (parameterValues.containsKey(p)) {

            Object valueMejorado = value;
            if (value instanceof String) {
                try {
                    valueMejorado = p.parseString(value.toString());
                } catch (CannotParseParameterValue ex) {
                    throw new IllegalArgumentException("Parameter error: " + p.getName() + " not compatible with " + value);
                }
            }

            if (p.isCorrect(valueMejorado)) {
                parameterValues.put(p, valueMejorado);
            } else {
                p.isCorrect(valueMejorado);
                throw new IllegalArgumentException("Parameter error: " + p.getName() + " not compatible with " + value);
            }

            fireParammeterChangedEvent();
            return value;
        } else {
            throw new IllegalArgumentException(this.getClass().getName() + " hasn't the parameter " + p.getName());
        }
    }

    /**
     * Devuelve una colección con todos los {@link Parameter} que posee el
     * {@link ParameterOwner}
     *
     * @return colección con los parámetros del objeto
     */
    @Override
    public final Collection<Parameter> getParameters() {
        Collection<Parameter> list = new LinkedList<>(parameterValues.keySet());
        return list;
    }

    /**
     * Devuelve true si el {@link ParameterOwner} tiene algún parámetro. False
     * si no tiene ningún parámetro
     *
     * @return True si el {@link ParameterOwner} tiene algún parámetro. False si
     * no tiene ningún parámetro
     */
    @Override
    public final boolean hasParameters() {
        return !parameterValues.isEmpty();
    }

    /**
     * Devuelve el nombre de este poseedor de parámetros.
     *
     * <p>
     * <p>
     * NOTA: Actualmente devuelve el nombre de la clase que lo implementa, ya
     * que un {@link ParameterOwner} representa un algoritmo configurable.
     *
     * @return Nombre de la clase.
     */
    @Override
    public final String getName() {
        return this.getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }

    /**
     * Devuelve el parámetro especificado.
     *
     * @param parameterName Parámetro del que se desea conocer su valor
     * @return valor del parámetro que se consulta
     */
    @Override
    public final Parameter getParameterByName(String parameterName) {
        for (Parameter p : parameterValues.keySet()) {
            if (p.getName().equals(parameterName)) {
                return p;
            }
        }
        throw new IllegalArgumentException(this.getName() + " hasn't the parammeter " + parameterName);
    }

    /**
     * Devuelve true si el {@link ParameterOwner} tiene definido el párametro
     * <code>parameter</code>
     *
     * @param parameter
     * @return Devuelve true si el {@link ParameterOwner} tiene definido el
     * parámetro <code>parameter</code>
     */
    @Override
    public final boolean haveParameter(Parameter parameter) {
        return parameterValues.containsKey(parameter);
    }

    /**
     * Devuelve el objeto convertido a cadena de manera que se refleje el valor
     * actual de cada uno de sus parámetros.
     *
     * @return Cadena que refleja la configuración del {@link ParameterOwner}
     */
    @Override
    public String getNameWithParameters() {
        StringBuilder str = new StringBuilder();

        str.append(getName());
        if (getParameters().isEmpty()) {
            return str.toString();
        }
        str.append(" ");
        for (Parameter p : getParameters()) {

            Object value = getParameterValue(p);

            str.append(p.getName()).append("=");
            if (value instanceof ParameterOwner) {
                ParameterOwner po = (ParameterOwner) value;
                str.append("[");
                str.append(po.getNameWithParameters());
                str.append("]");
            } else {
                str.append(value.toString());
            }

            str.append(" ");

        }
        str = str.delete(str.length() - 1, str.length());
        return str.toString();
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof ParameterOwner) {
            ParameterOwner parameterOwner = (ParameterOwner) o;
            return PARAMETERS_DETAILED.compare(this, parameterOwner);
        } else {
            return StringsOrderings.getNaturalComparatorIgnoreCaseAscii().compare(this.toString(), o.toString());
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ParameterOwner)) {
            return false;
        } else {
            ParameterOwner parameterOwner = (ParameterOwner) obj;

            return equalsIgnoreAlias(parameterOwner);
        }
    }

    private boolean equalsIgnoreAlias(ParameterOwner parameterOwner) {
        String regex = ALIAS.getName() + "=([^\\s]+)";

        String myNameWithParameters = this.getNameWithParameters().replaceAll(regex, "");
        String otherNameWithParameters = parameterOwner.getNameWithParameters().replaceAll(regex, "");

        boolean equals = myNameWithParameters.equals(otherNameWithParameters);
        return equals;
    }

    @Override
    public int hashCode() {
        int hash = 7;

        hash = 97 * hash + Objects.hashCode(this.getClass().hashCode());

        for (Parameter parameter : parameterValues.keySet()) {
            hash = 97 * hash + parameter.getName().hashCode();
            hash = 97 * hash + getParameterValue(parameter).hashCode();
        }

        return hash;
    }

    public static int compare(ParameterOwner parameterOwner1, ParameterOwner parameterOwner2) {
        return ParameterOwner.PARAMETERS_DETAILED.compare(parameterOwner1, parameterOwner2);
    }
}
