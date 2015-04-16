package delfos;

/**
 * Excepción que se lanza cuando se solicitan valores de un parámetro de la
 * linea de comandos y no está definido ({@link ConsoleParameterParser#isDefined(java.lang.String)
 * } devuelve falso)
 *
 * @author Jorge Castro Gallardo
 *
 */
public class UndefinedParameterException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private final String parameterMissing;

    /**
     * Crea la excepción con el mensaje indicado.
     *
     * @param message Mensaje en el que se puede informar de las causas del
     * error.
     * @param parameterMissing Parámetro que se estaba buscando y no se ha
     * encontrado.
     */
    public UndefinedParameterException(String message, String parameterMissing) {
        super(message + " call ConsoleParameterParser.isDefined(parameter) to check before usage]");
        this.parameterMissing = parameterMissing;
    }

    public String getParameterMissing() {
        return parameterMissing;
    }
}
