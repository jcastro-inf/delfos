package delfos.common.exceptions;

import java.io.File;

/**
 * Excepción que se lanza cuando no se encuentra el archivo que almacena el
 * modelo generado por un sistema de recomendación
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2103
 */
public class ModelFileNotFound extends Exception {

    private final static long serialVersionUID = 1L;

    /**
     * Excepción que se lanza cuando no se encuentra el archivo que contiene el
     * modelo de recomendación.
     *
     * @param msg Mensaje que describe el error.
     */
    public ModelFileNotFound(String msg) {
        super(msg);
    }

    /**
     * Excepción que se lanza cuando no se encuentra el archivo que contiene el
     * modelo de recomendación.
     *
     * @param modelFile Fichero no encontrado.
     */
    public ModelFileNotFound(File modelFile) {
        super("Model file '" + modelFile.getAbsolutePath() + "' not found.");
    }
}
