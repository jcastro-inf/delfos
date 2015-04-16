package delfos.common.parameters.restriction;

import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.io.xml.parameterowner.parameter.PasswordParameterXML;
import org.jdom2.Element;

/**
 * Restricción específica para almacenar valores que representan una contraseña
 * en los objetos {@link ParameterOwner}.
 *
 *
 * <p>
 * <p>
 * Los valores que puede tomar son {@link String} distintas de null.
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 Unknown date
 * @version 1.1 (18-02-2013) Javadoc detallado.
 */
public class PasswordParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;

    /**
     * Constructor por defecto, que especifica el valor por defecto para este
     * parámetro.
     *
     * @param defaultValue Valor por defecto.
     */
    public PasswordParameter(Object defaultValue) {
        super(defaultValue);

        if (!isCorrect(defaultValue)) {
            throw new UnsupportedOperationException("Default value must be a string");
        }
    }

    /**
     * Un valor para este parámetro es correcto si es de tipo {@link String} y
     * su valor es distinto de null.
     *
     * {@inheritDoc }
     */
    @Override
    public final boolean isCorrect(Object o) {
        return o instanceof String;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public Object parseString(String parameterValue) {
        return parameterValue;
    }

    /**
     * Obtiene el valor a partir del objeto al cual pertenece el parámetro y del
     * elemento XML que lo especifica. Para obtener el valor, utiliza el método {@link PasswordParameterXML#getParameterValue(delfos.common.Parameters.ParameterOwner, org.jdom2.Element)
     * } de la clase {@link PasswordParameterXML}
     *
     * {@inheritDoc }
     */
    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return PasswordParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    /**
     * Obtiene el elemento XML que especifica el valor de este parametro. Para
     * obtenerlo, utiliza el método {@link PasswordParameterXML#getPasswordParameterElement(delfos.common.Parameters.ParameterOwner, delfos.common.Parameters.Parameter)
     * }
     * de la clase {@link PasswordParameterXML}
     *
     * {@inheritDoc }
     */
    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return PasswordParameterXML.getPasswordParameterElement(parameterOwner, parameter);
    }
}
