package delfos.experiment.casestudy;

/**
 * Interfaz que deben implementar todos los listener que necesiten ser
 * notificados de las modificaciones de las propiedades de un {@link CaseStudy}.
* @author Jorge Castro Gallardo
 */
public interface CaseStudyParameterChangedListener {

    /**
     * Método que se invoca cuando el caso de estudio sufre un cambio en alguno 
     * de sus parámetros. La clase que implemente este método será la encargada
     * de chequear qué parámetro ha sido modificado.
     * @param cs Caso de estudio que ha sido modificado
     */
    public void caseStudyParameterChanged(CaseStudy cs);
}
