package delfos.common;

import delfos.Constants;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Clase que encapsula los métodos para mostrar información en la consola (o en
 * la salida por defecto) de la aplicación
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (03/01/2013)
 */
public class Global {

    private static boolean threadVerbose = false;

    public static boolean isPrintDatasets() {
        return false;
    }

    /**
     * Hace una pregunta cerrarda al usuario por linea de comandos cuya
     * respuesta es sí o no. Si el usuario da una respuesta distinta, vuelve a
     * formularle la pregunta indicando que solo puede responder sí o no.
     *
     * @param textOfQuestion Texto informativo que se muestra al usuario para
     * informar de la decisión que debe tomar.
     * @return true si el usuario responde sí, false si responde no.
     */
    public static boolean askUser(String textOfQuestion) {
        if (Global.isDefaultAnswerYes()) {
            return true;
        }

        while (true) {
            System.out.print(textOfQuestion);
            System.out.println("");

            //  open up standard input
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

            String answer;

            try {
                answer = br.readLine();

                if (answer != null) {

                    if (answer.toLowerCase().equals("yes") || answer.toLowerCase().equals("y") || answer.toLowerCase().equals("sí") || answer.toLowerCase().equals("si") || answer.toLowerCase().equals("s")) {
                        return true;
                    }

                    if (answer.toLowerCase().equals("no") || answer.toLowerCase().equals("n")) {
                        return false;
                    }
                } else {
                    System.out.println("Answer was null. Assuming answer is yes");
                    return true;
                }

            } catch (IOException ioe) {
            }

            //Ha respondido algo distinto, insistir
            System.out.println("The answer should be yes or no (y/n).");
        }
    }
    private static boolean defaultAnswerYes = false;

    public static void setDefaultAnswerYes(boolean defaultAnswerYes) {
        Global.defaultAnswerYes = defaultAnswerYes;
    }

    public static boolean isDefaultAnswerYes() {
        return defaultAnswerYes;
    }

    /**
     * Se controla en linea de comandos con el flag
     * {@link Constants#THREAD_VERBOSE}
     *
     * @param threadVerbose
     */
    public static void setThreadVerbose(boolean threadVerbose) {
        Global.threadVerbose = threadVerbose;
        if (!Global.isVerbose()) {
            System.out.println("Verbose mode not set, setting to true.");
            Global.setVerbose();

        }
    }

    /**
     * Muestra la cadena indicada como parámetro en la salida de error.
     *
     * @param warningMessage Mensaje a mostrar
     */
    public static void showThreadMessage(String warningMessage) {
        if (Global.threadVerbose) {
            System.err.println("\tTHREAD: " + warningMessage);

            if (doublePrint) {
                System.out.println("\tTHREAD: " + warningMessage);
            }
        }
    }

    /**
     * Muestra la cadena indicada como parámetro en la salida de error.
     *
     * @param message Mensaje a mostrar
     */
    public static void showThreadMessageTimestamped(String message) {
        if (Global.threadVerbose) {
            String timestampedMessage = addTimestampToMessage(message);
            showThreadMessage(timestampedMessage);
        }
    }

    public static String addTimestampToMessage(String message) {
        String timestampedMessage;
        if (message.endsWith("\n")) {
            timestampedMessage = message.substring(0, message.length() - 1) + " (" + new Date().toLocaleString() + ")\n";
        } else {
            timestampedMessage = message + " (" + new Date().toLocaleString() + ")\n";
        }

        return timestampedMessage;
    }

    public static void showMessageTimestamped(String message) {
        Global.showMessage(addTimestampToMessage(message));
    }

    public static void showThreadMessageAnnoying(String message) {
        if (Global.isVerboseAnnoying()) {
            Global.showThreadMessage(message);
        }
    }

    public static void showThreadMessageAnnoyingTimestamped(String message) {
        if (Global.isVerboseAnnoying()) {
            Global.showThreadMessageTimestamped(message);
        }
    }

    enum VerboseLevel {

        NONE(),
        /**
         * Imprime solo mensajes generales.
         */
        GENERAL(),
        /**
         * Imprime todos los mensajes.
         */
        ANNOYING();
    }
    /**
     * Indica si se deben mostrar mensajes o no. Los algoritmos indican en qué
     * punto están en cada momento. Por defecto no se muestran.
     */
    private static VerboseLevel verbose = VerboseLevel.NONE;
    /**
     * Indica si se deben mostrar los mensajes de error. Por defecto están
     * activos.
     */
    private static boolean showErrors = true;
    /**
     * Indica si se deben mostrar los mensajes de advertencia. Por defecto están
     * activos.
     */
    private static boolean showWarnings = true;
    private static boolean doublePrint = false;

    /**
     * Muestra la cadena indicada como parámetro en la salida por defecto.
     *
     * @param message Mensaje a mostrar
     */
    public static void showMessage(String message) {
        if (verbose != VerboseLevel.NONE) {
            System.out.print(message);
            if (doublePrint) {
                System.err.println(message);
            }
        }
    }

    /**
     * Muestra el estado de la pila en el momento en que se creó la excepción
     * indicada por parámetro.
     *
     * @param ex Excepción con la información del error.
     */
    public static void showError(Throwable ex) {
        if (showErrors) {
            ex.printStackTrace(System.err);

            if (doublePrint) {
                ex.printStackTrace(System.out);
            }
        }
    }

    /**
     * Muestra la cadena indicada como parámetro en la salida de error.
     *
     * @param warningMessage Mensaje a mostrar
     */
    public static void showWarning(String warningMessage) {
        if (showWarnings) {
            System.err.println("WARNING: " + warningMessage);

            if (doublePrint) {
                System.out.println("WARNING: " + warningMessage);
            }
        }
    }

    /**
     * Muestra el estado de la pila en el momento en que se creó la excepción
     * indicada por parámetro.
     *
     * @param ex Excepción con la información de la advertencia.
     */
    public static void showWarning(Throwable ex) {
        if (showWarnings) {
            ex.printStackTrace(System.err);

            if (doublePrint) {
                ex.printStackTrace(System.out);
            }
        }
    }

    /**
     * Devuelve si se deben imprimir mensajes o no.
     *
     * @return true si se deben imprimir mensajes.
     */
    public static boolean isVerbose() {
        return verbose != VerboseLevel.NONE;
    }

    public static boolean isVerboseAnnoying() {
        return verbose == VerboseLevel.ANNOYING;
    }

    /**
     * Establece si se deben mostrar los mensajes comunes.
     */
    public static void setVerbose() {
        verbose = VerboseLevel.GENERAL;
        System.out.println("Verbose mode ON");
    }

    /**
     * Establece si se deben mostrar los mensajes de advertencia.
     *
     * @param showWarnings Nuevo valor. True para mostrarlos, false para
     * ocultarlos.
     */
    public static void setShowWarnings(boolean showWarnings) {
        Global.showWarnings = showWarnings;
    }

    /**
     * Establece si se deben mostrar los mensajes de error.
     *
     * @param showErrors Nuevo valor. True para mostrarlos, false para
     * ocultarlos.
     */
    public static void setShowErrors(boolean showErrors) {
        Global.showErrors = showErrors;
    }

    public static void setVerboseAnnoying() {
        System.out.println("Verbose level: Annoying");
        System.err.println("Verbose level: Annoying");
        verbose = VerboseLevel.ANNOYING;
    }

    public static void setNoVerbose() {
        verbose = VerboseLevel.NONE;
    }

    /**
     * Establece si se debe escribir los mensajes de error y warning en ambas
     * salidas, la salida por defecto y la de error.
     *
     * <p>
     * <p>
     * NOTA: Si la salida por defecto y la salida de error es la misma, se
     * pueden producir colisiones en la escritura.
     *
     * @param doublePrint
     */
    public static void setDoublePrint(boolean doublePrint) {
        Global.doublePrint = doublePrint;
    }
}
