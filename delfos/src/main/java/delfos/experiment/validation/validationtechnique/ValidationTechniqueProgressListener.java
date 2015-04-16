package delfos.experiment.validation.validationtechnique;

/**
 * Interfaz que deben implementar los objetos que deseen registrar el progreso
 * de ejecución de una validación
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 * @see ValidationTechnique#shuffle()
 */
public interface ValidationTechniqueProgressListener {

    /**
     * Metodo que se invoca cuando la técnica de validación observada cambia.
     *
     * @param message Tarea que está realizando.
     * @param percent Porcentaje completado de la tarea.
     */
    public void progressChanged(String message, int percent);
}
