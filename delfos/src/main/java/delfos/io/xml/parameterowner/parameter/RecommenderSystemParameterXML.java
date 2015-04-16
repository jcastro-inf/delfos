package delfos.io.xml.parameterowner.parameter;

import org.jdom2.Element;
import delfos.rs.GenericRecommenderSystem;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 *
* @author Jorge Castro Gallardo
 * @version 1.0 15-May-2013
 */
public class RecommenderSystemParameterXML {

    public static GenericRecommenderSystem<Object> getParameterValue(ParameterOwner parameterOwner, Element parameterElement) {
        return (GenericRecommenderSystem) ParameterOwnerParameterXML.getParameterOwnerParameterValue(parameterOwner, parameterElement);
    }

    public static Element getRecommenderSystemParameterElement(ParameterOwner parameterOwner, Parameter p) {
        return ParameterOwnerParameterXML.getParameterOwnerElement(parameterOwner, p);
    }
}
