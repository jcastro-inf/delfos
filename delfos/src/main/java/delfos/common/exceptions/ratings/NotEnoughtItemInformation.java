package delfos.common.exceptions.ratings;

/**
 * Excepción que se lanza cuando no se tiene información suficiente sobre un
 * producto. Los siguientes ejemplos ilustran los posibles usos de esta
 * excepción:
 *
 * <p>
 * <p>
 * Cuando un producto no tiene valoraciones suficientes para calcular su perfil.
 * <p>
 * <p>
 *
* @author Jorge Castro Gallardo
 *
 * @version 9-abril-2014
 */
public class NotEnoughtItemInformation extends Exception {

    private static final long serialVersionUID = 1L;

    public NotEnoughtItemInformation(String message) {
        super(message);
    }

    public NotEnoughtItemInformation(int idItem, String svd_recommendation_model_does_not_contain) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
