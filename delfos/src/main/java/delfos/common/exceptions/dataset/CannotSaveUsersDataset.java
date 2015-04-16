package delfos.common.exceptions.dataset;

/**
 * Excepción lanzada cuando se intenta guardar un dataset pero se produce algún
 * fallo que provoca que no sea posible. Ejemplos de fallos son que el archivo
 * en el que se intenta escribir está protegido o que no se encuentra la base de
 * datos en la que se almacena.
 *
* @author Jorge Castro Gallardo
 *
 * @version 18-sep-2013
 * @version 30-Octubre-2013 Ahora es una excepción Unchecked.
 */
public class CannotSaveUsersDataset extends RuntimeException {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param cause Excepción con el error detallado.
     */
    public CannotSaveUsersDataset(Throwable cause) {
        super(cause);
    }

    /**
     * Crea la excepción a partir de otra excepción que describe el error en
     * detalle.
     *
     * @param message Mensaje a mostrar.
     */
    public CannotSaveUsersDataset(String message) {
        super(message);
    }
}
