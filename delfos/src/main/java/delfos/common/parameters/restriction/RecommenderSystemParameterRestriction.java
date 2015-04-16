package delfos.common.parameters.restriction;

import java.util.Iterator;
import java.util.List;
import org.jdom2.Element;
import delfos.factories.RecommenderSystemsFactory;
import delfos.io.xml.rs.RecommenderSystemXML;
import delfos.rs.GenericRecommenderSystem;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Encapsula el comportamiento de una restricción de valores de parámetro que
 * sólo permite seleccionar sistemas de recomendación que concuerden con los
 * tipos pasados por parámetro en el constructor. De esta manera, se pueden
 * asignar sistemas de recomendación del mismo tipo o que hereden del mismo.
 *
 * <p><p>Version 1.1: Se establecen los métodos de entrada/salida a XML.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class RecommenderSystemParameterRestriction extends ParameterRestriction {

    private final static long serialVersionUID = 1L;
    private final Class<? extends GenericRecommenderSystem> tiposPermitidos[];

    /**
     * Constructor de una restricción de valores del parámetro para que sólo
     * permita sistemas de recomendación. El tipo de los sistemas de
     * recomendación que admite como valor se puede restringir utilizando el
     * parámetro
     * <code>tiposPermitidos</code>.
     *
     * @param tiposPermitidos Vector que contiene el tipo de sistemas de
     * recomendación que el parámetro puede tomar como valor.
     * @param defaultRS Sistema de recomendación por defecto que se asigna al
     * parámetro. Debe ser de alguno de los tipos indicados en el parámetro
     * <code>tiposPermitidos</code>.
     */
    public RecommenderSystemParameterRestriction(GenericRecommenderSystem defaultRS, Class<? extends GenericRecommenderSystem>... tiposPermitidos) {
        super(defaultRS);
        this.tiposPermitidos = tiposPermitidos;


        if (!isCorrect(defaultRS)) {
            throw new IllegalArgumentException("The default value isn't correct");
        }
    }

    @Override
    public final boolean isCorrect(Object newValue) {
        for (Class<? extends GenericRecommenderSystem> tipoPermitido : tiposPermitidos) {
            if (tipoPermitido.isAssignableFrom(newValue.getClass())) {
                return true;
            }
        }
        Global.showWarning("WARNING: class " + newValue.getClass() + " isn't allowed\n");
        Global.showError(new IllegalArgumentException("WARNING: " + newValue.getClass() + " isn't allowed\n"));
        return false;
    }

    /**
     * Comprueba si el objeto indicado por parámetro es de alguno de los tipos
     * permitidos. Esto lo hace usando reflectividad, comprobando si las clases
     * que se indicaron como permitidas pueden almacenar el objeto, es decir,
     * comprueba si alguno de los tipos permitidos es compatible con el objeto.
     *
     * @param newValue Nuevo valor para este parámetro.
     * @return Devuelve true si el nuevo valor es compatible con alguno de los
     * tipos permitidos.
     */
    public boolean isInstanceOfAny(Object newValue) {
        for (Class<? extends GenericRecommenderSystem> tipoPermitido : tiposPermitidos) {
            if (tipoPermitido.isAssignableFrom(newValue.getClass())) {
                return true;
            }
        }

        return false;
    }

    @Override
    public Object parseString(String parameterValue) {
        return RecommenderSystemsFactory.getInstance().getClassByName(parameterValue);
    }

    /**
     * Devuelve todos los sistemas de recomendación que pueden ser asignados
     * atendiendo a los tipos que se indicaron como permitidos.
     *
     * @return Sistemas de recomendación que el parámetro puede tomar como valor
     * @deprecated La función no se debe utilizar, sino que se deben recuperar
     * todos los sistemas de recomendación y comprobar cuáles de ellos son
     * válidos para esta restricción.
     */
    public Object[] getAllowed() {
        List<GenericRecommenderSystem> allGenericRecommenderSystems = RecommenderSystemsFactory.getInstance().getAllClasses();
        for (Iterator<GenericRecommenderSystem> it = allGenericRecommenderSystems.iterator(); it.hasNext();) {
            GenericRecommenderSystem rs = it.next();
            if (!isInstanceOfAny(rs)) {
                it.remove();
            }
        }
        return allGenericRecommenderSystems.toArray();
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return RecommenderSystemXML.getRecommenderSystem(elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return RecommenderSystemXML.getElement((GenericRecommenderSystem) parameterOwner.getParameterValue(parameter));
    }
}
