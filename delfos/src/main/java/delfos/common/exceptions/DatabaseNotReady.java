package delfos.common.exceptions;

import java.sql.SQLException;

/**
 * Excepción que se lanza cuando no se pudo realizar la conexión a la base de
 * datos (usuario o pass erróneo, host no encontrado, puerto erróneo, etc)
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 */
public class DatabaseNotReady extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param msg Mensaje a mostrar.
     */
    public DatabaseNotReady(String msg) {
        super(msg);
    }

    public DatabaseNotReady(SQLException ex) {
        super(ex);
    }
}
