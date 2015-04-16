package delfos.group.experiment.validation.validationtechniques;

/**
 * Interfaz que deben implementar los objetos que deseen registrar el progreso
 * de ejecución de una validación
 *
* @author Jorge Castro Gallardo
 * @see GroupValidationTechnique#shuffle() 
 *
 * @version 1.0 (12/12/2012)
 */
public interface GroupValidationTechniqueProgressListener {

    /**
     * Método para informar que el progreso de ejecución ha cambiado.
     *
     * @param message Tarea actual
     * @param percent Porcentaje completado
     */
    public void progressChanged(String message, int percent);
}
