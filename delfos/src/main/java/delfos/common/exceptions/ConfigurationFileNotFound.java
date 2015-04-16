package delfos.common.exceptions;

/**
 * Clase que representa la ausencia del archivo de la configuración del sistema
 * de recomendación en la ruta especificada
 *
* @author Jorge Castro Gallardo
 */
public class ConfigurationFileNotFound extends Exception {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Constructor de la excepción que asigna un mensaje de error
     *
     * @param msg Mensaje de la excepción
     */
    public ConfigurationFileNotFound(String msg) {
        super(msg);
    }
}
