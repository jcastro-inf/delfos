package delfos.common.parameters;

import java.io.Serializable;
import java.util.Collection;
import delfos.common.parameters.restriction.StringParameter;
import delfos.factories.Factory;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 08-Mar-2013
 */
public interface ParameterOwner extends Serializable, Comparable<Object> {

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

}
