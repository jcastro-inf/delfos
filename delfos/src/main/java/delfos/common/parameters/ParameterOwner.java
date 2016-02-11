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

import delfos.common.StringsOrderings;
import delfos.common.parameters.restriction.StringParameter;
import delfos.factories.Factory;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 08-Mar-2013
 */
public interface ParameterOwner extends Serializable, Comparable<Object> {

    public static final Comparator<Object> SAME_CLASS_COMPARATOR_OBJECT = (Object o1, Object o2) -> {
        if (o1 instanceof ParameterOwner && o2 instanceof ParameterOwner) {
            ParameterOwner p1 = (ParameterOwner) o1;
            ParameterOwner p2 = (ParameterOwner) o2;

            return ParameterOwner.SAME_CLASS_COMPARATOR.compare(p1, p2);
        } else {
            return StringsOrderings.getNaturalComparatorIgnoreCaseAscii().compare(o1.toString(), o2.toString());
        }
    };

    public static final Comparator<ParameterOwner> SAME_CLASS_COMPARATOR = (ParameterOwner o1, ParameterOwner o2) -> {
        if (o1 instanceof ParameterOwner && o2 instanceof ParameterOwner) {

            ParameterOwner parameterOwner1 = (ParameterOwner) o1;
            ParameterOwner parameterOwner2 = (ParameterOwner) o2;

            if (parameterOwner1.isSameClass(parameterOwner2)) {
                return 0;
            } else {
                return parameterOwner1.compareTo(parameterOwner2);
            }
        } else {
            return StringsOrderings.compareNatural(o1.toString(), o2.toString());
        }
    };

    public static final Comparator<ParameterOwner> PARAMETERS_DETAILED = new Comparator<ParameterOwner>() {

        @Override
        public int compare(ParameterOwner o1, ParameterOwner o2) {
            if (!o1.getClass().equals(o2.getClass())) {
                return o1.getNameWithParameters().compareTo(o2.getNameWithParameters());
            }

            Set<Parameter> parameters = new TreeSet<>(o1.getParameters());

            parameters.remove(ParameterOwner.ALIAS);

            for (Parameter parameter : parameters) {
                Object o1Value = o1.getParameterValue(parameter);
                Object o2Value = o2.getParameterValue(parameter);

                if (o1Value instanceof ParameterOwner || o2Value instanceof ParameterOwner) {
                    ParameterOwner o1ValueParameterOwner = (ParameterOwner) o1Value;
                    ParameterOwner o2ValueParameterOwner = (ParameterOwner) o2Value;

                    int compare = compare(o1ValueParameterOwner, o2ValueParameterOwner);

                    if (compare != 0) {
                        return compare;
                    }
                } else if (o1Value instanceof Number) {

                    Number o1ValueNumber = (Number) o1Value;
                    Number o2ValueNumber = (Number) o2Value;

                    int compare = Double.compare(o1ValueNumber.doubleValue(), o2ValueNumber.doubleValue());

                    if (compare != 0) {
                        return compare;
                    }
                } else {
                    int compare = StringsOrderings.getNaturalComparatorIgnoreCaseAscii().compare(o1Value.toString(), o2Value.toString());
                    if (compare != 0) {
                        return compare;
                    }
                }
            }

            return 0;
        }
    };

    public static final Parameter ALIAS = new Parameter("alias", new StringParameter(""));

    /**
     * Añade un listener para que sea notificado de todos los cambios en los
     * parámetros del {@link ParameterOwner}, tanto de parámetros añadidos como
     * de cambios del valor asignado a los mismos.
     *
     * @param listener objeto a notificar de los cambios
     */
    public void addParammeterListener(ParameterListener listener);

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
    public String getName();

    /**
     * Devuelve el nombre usando sólo 4 caracteres.
     *
     * @return Nombre corto, de 4 caracteres de longitud.
     */
    public String getShortName();

    /**
     * Devuelve el objeto convertido a cadena de manera que se refleje el valor
     * actual de cada uno de sus parámetros.
     *
     * @return Cadena que refleja la configuración del {@link ParameterOwner}
     */
    public String getNameWithParameters();

    /**
     * Devuelve el ALIAS del ParameterOwner.
     *
     * @return
     */
    public String getAlias();

    /**
     * Establece el nuevo alias del objeto. Si se quiere eliminar el alias, se
     * debe indicar null.
     *
     * @param alias Alias del objeto.
     */
    public void setAlias(String alias);

    /**
     * Devuelve el parámetro especificado.
     *
     * @param parameterName Parámetro del que se desea conocer su valor
     * @return valor del parámetro que se consulta
     */
    public Parameter getParameterByName(String parameterName);

    /**
     * Devuelve el valor asignado al parámetro especificado.
     *
     * @param p Parámetro del que se desea conocer su valor
     * @return valor del parámetro que se consulta
     */
    public Object getParameterValue(Parameter p);

    /**
     * Devuelve true si el {@link ParameterOwner} tiene algún parámetro. False
     * si no tiene ningún parámetro
     *
     * @return True si el {@link ParameterOwner} tiene algún parámetro. False si
     * no tiene ningún parámetro
     */
    public boolean hasParameters();

    /**
     * Devuelve true si el {@link ParameterOwner} tiene definido el párametro
     * <code>parameter</code>
     *
     * @param parameter
     * @return Devuelve true si el {@link ParameterOwner} tiene definido el
     * parámetro <code>parameter</code>
     */
    public boolean haveParameter(Parameter parameter);

    /**
     * Elimina un listener para que no sea notificado nunca más de los cambios
     * en los parámetros del {@link ParameterOwner}.
     *
     * @param listener objeto que no desea ser notificado más de los cambios
     */
    public void removeParammeterListener(ParameterListener listener);

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
    public Object setParameterValue(Parameter p, Object value);

    /**
     * Devuelve una colección con todos los {@link Parameter} que posee el
     * {@link ParameterOwner}
     *
     * @return colección con los parámetros del objeto
     */
    public Collection<Parameter> getParameters();

    /**
     * Tipo del {@link ParameterOwner}. Se utiliza posteriormente para saber que
     * {@link Factory} se encarga de crear el objeto.
     *
     * @return Tipo de este objeto.
     */
    public ParameterOwnerType getParameterOwnerType();

    public default boolean isSameClass(ParameterOwner parameterOwner) {
        if (parameterOwner == null) {
            return true;
        }

        return this.getClass().equals(parameterOwner.getClass());
    }
}
