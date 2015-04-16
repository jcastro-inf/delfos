package delfos.io.xml.dataset;

import org.jdom2.DataConversionException;
import org.jdom2.Element;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.common.Global;

/**
 * Clase para realizar la entrada/salida a XML para el criterio de relevancia
 *
 * @version 1.0 (06/12/2012)
 *
* @author Jorge Castro Gallardo
 */
public class RelevanceCriteriaXML {

    /**
     * Nombre del elemento que tiene el criterio de relevancia
     */
    public static final String ELEMENT_NAME = "RelevanceCriteria";
    /**
     * Nombre de la característica en que se guarda el valor de umbral del
     * criterio de relevancia
     */
    private static final String TRESHOLD_ATTRIBUTE = "threshold";

    /**
     * Genera el elemento XML que describe el criterio de relevancia
     *
     * @param relevanceCriteria Criterio de relevancia a almacenar
     * @return Elemento con la información necesaria para reconstruir el
     * criterio de relevancia
     */
    public static Element getElement(RelevanceCriteria relevanceCriteria) {
        Element relevanceCriteriaElement = new Element(ELEMENT_NAME);
        relevanceCriteriaElement.setAttribute(TRESHOLD_ATTRIBUTE, relevanceCriteria.getThreshold().toString());
        return relevanceCriteriaElement;
    }

    /**
     * Recupera el criterio de relevancia a partir del elemento XML que contiene
     * la información del mismo.
     *
     * @param relevanceCriteriaElement Elemento que describe el criterio de
     * relevancia
     *
     * @return Criterio de relevancia descrito por el elemento XML
     */
    public static RelevanceCriteria getRelevanceCriteria(Element relevanceCriteriaElement) {
        Number threshold = 4;
        try {
            threshold = relevanceCriteriaElement.getAttribute(TRESHOLD_ATTRIBUTE).getDoubleValue();
        } catch (DataConversionException ex) {
            Global.showError(ex);
        }
        return new RelevanceCriteria(threshold);
    }
}
