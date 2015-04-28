package delfos.common.parameters;

import delfos.common.parameters.restriction.CannotParseParameterValue;
import delfos.common.parameters.restriction.ParameterRestriction;
import java.io.Serializable;

/**
 * Parámetro de uno de los algoritmos de la biblioteca. Se utiliza por la clase
 * {@link ParameterOwner} para definir los distintos parámetros y asignarles una
 * restricción que compruebe que su valor es válido (satisface la restricción)
 * en todo momento.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (21-02-2013)
 */
public class Parameter implements Comparable<Parameter>, Serializable {

    private static final long serialVersionUID = 117L;

    /**
     * Restricción que se aplica a los valores del parámetro.
     */
    private final ParameterRestriction restriction;
    /**
     * Nombre del parámetro.
     */
    private final String name;
    /**
     * Descripción de cómo se debe utilizar el parámetro.
     */
    private final Object description;

    /**
     * Constructor por defecto de un parámetro, al cual se le asigna el
     *
     * @param name Nombre del parámetro.
     * @param restriction Restricción que se aplica a los valores del parámetro.
     */
    public Parameter(String name, ParameterRestriction restriction) {

        if (name.contains(" ") || name.contains(".")) {
            throw new UnsupportedOperationException("The parameter name mustn't contain whitespaces (must be a valid XML label)");
        } else {
            this.name = name;
            this.restriction = restriction;
            this.description = null;
        }
    }

    /**
     * Constructor por defecto de un parámetro, al cual se le asigna el
     *
     * @param name Nombre del parámetro.
     * @param restriction Restricción que se aplica a los valores del parámetro.
     * @param description Descripción del parámetro, para ser usada en la
     * interfaz.
     */
    public Parameter(String name, ParameterRestriction restriction, Object description) {
        if (name.contains(" ") || name.contains(".")) {
            throw new UnsupportedOperationException("The parameter name mustn't contain whitespaces (must be a valid XML label)");
        } else {
            this.name = name;
            this.restriction = restriction;
            this.description = description;
        }
    }

    /**
     * Devuelve el nombre del parámetro.
     *
     * @return Nombre del parámetro.
     */
    public String getName() {
        return name;
    }

    /**
     * Comprueba si el nuevo valor del parámetro es correcto, invocando el
     * método {@link ParameterRestriction#isCorrect(java.lang.Object) } de la
     * restricción impuesta a este parámetro.
     *
     * @param value Valor a comprobar.
     *
     * @return true si el valor satisface la restricción, false en otro caso.
     */
    public boolean isCorrect(Object value) {
        return restriction.isCorrect(value);
    }

    /**
     * Devuelve el valor por defecto del parámetro.
     *
     * @return Valor por defecto del parámetro.
     */
    public Object getDefaultValue() {
        return restriction.getDefaultValue();
    }

    /**
     * Compara los parámetros utilizando el nombre, por lo que los parámetros se
     * ordenan por nombre en orden ascendente.
     *
     * {@inheritDoc }
     */
    @Override
    public int compareTo(Parameter o) {
        return this.name.compareTo(o.name);
    }

    /**
     * Devuelve la restricción que se aplica al parámetro.
     *
     * @return Restricción del parámetro.
     */
    public ParameterRestriction getRestriction() {
        return restriction;
    }

    /**
     * Función para obtener el valor del parámetro partir de una cadena de
     * caracteres, utilizando para ello la restricción que se aplica al mismo.
     *
     * <p>
     * <p>
     * NOTA: No se garantiza que el valor devuelto satisfaga la restricción, por
     * lo que se debe comprobar utilizando la función {@link Parameter#isCorrect(java.lang.Object)
     * }.
     *
     * @param stringParameterValue Cadena de caracteres que se intenta
     * transformar a un valor del parámetro.
     *
     * @return Objeto asociado a la cadena, null si no se puede transformar a un
     * valor acorde con la restricción.
     * @throws delfos.common.parameters.restriction.CannotParseParameterValue
     */
    public Object parseString(String stringParameterValue) throws CannotParseParameterValue {
        try {
            return restriction.parseString(stringParameterValue);
        } catch (CannotParseParameterValue ex) {
            throw new CannotParseParameterValue(
                    "Parameter '" + this.name + "' not compatible "
                    + "with value'" + stringParameterValue + "'", ex);
        }
    }

    /**
     * Devuelve una cadena que representa, a grandes rasgos, el atributo y su
     * restricción.
     *
     * {@inheritDoc }
     */
    @Override
    public String toString() {
        return getName() + "[" + restriction.getName() + "]";
    }

    /**
     * Devuelve si el parámetro tiene una descripción.
     *
     * @return
     */
    public boolean hasDescription() {
        return description != null;
    }

    /**
     * Devuelve la descripción del parámetro.
     *
     * @return Descripción del parámetro.
     * @throws IllegalStateException Si el parámetro no tiene definida una
     * descipción.
     */
    public Object getDescription() {
        if (!hasDescription()) {
            throw new IllegalStateException();
        } else {
            return description;
        }
    }

}
