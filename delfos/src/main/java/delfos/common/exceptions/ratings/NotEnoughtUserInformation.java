package delfos.common.exceptions.ratings;

/**
 * Excepción que se lanza cuando no se tiene información suficiente por parte
 * del usuario. Los siguientes ejemplos ilustran los posibles usos de esta
 * excepción:
 *
 * <p>
 * <p>
 * Cuando un usuario no tienen valoraciones suficientes para calcular su
 * similitud con otros usuarios.
 * <p>
 * <p>
 * Cuando un usuario no tiene
 *
* @author Jorge Castro Gallardo
 *
 * @version 15-oct-2013
 */
public class NotEnoughtUserInformation extends Exception {

    private static final long serialVersionUID = 1L;

    public NotEnoughtUserInformation(String message) {
        super(message);
    }
}
