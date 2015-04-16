package delfos.io.xml;

import org.jdom2.Element;

/**
 * Excepción que se lanza para informar que el objeto {@link Element} que se 
 * deseaba convertir a un objeto concreto no tiene el formato correcto.
* @author Jorge Castro Gallardo
 */
public class UnrecognizedElementException extends Exception{

    private static final long serialVersionUID = 1L;
    public UnrecognizedElementException(String message) {
        super(message);
    }
    
}
