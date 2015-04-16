package delfos.group.experiment.validation.groupformation;

/**
 * Interfaz que define el método que se invoca cuando el progreso de cálculo de
 * los grupos de usuarios mediante el método {@link GroupFormationTechnique#shuffle() }
* @author Jorge Castro Gallardo
 */
public interface GroupFormationTechniqueProgressListener {
    
    /**
     * Método para notificar a las clases listener de un cambio en el progreso
     * de formación de los grupos.
     * @param message Mensaje que informa de la fase en la que se encuentra
     * @param progress Porcentaje completado
     */
    public void progressChanged(String message,int progress);
}
