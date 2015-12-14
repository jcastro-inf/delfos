package delfos.common;

import delfos.ConsoleParameters;
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
     * Muestra la cadena indicada como parámetro en la salida de error.
     *
     * @param warningMessage Mensaje a mostrar
     */
    public static void showThreadMessage(String warningMessage) {
        if (messageLevelPrinted.isPrinted(MessageLevel.THREAD)) {
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
        if (messageLevelPrinted.isPrinted(MessageLevel.THREAD)) {
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
        Global.showInfoMessage(addTimestampToMessage(message));
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

    public static void show(String message) {
        System.out.print(message);
    }

    public static void showln(String message) {
        System.out.println(message);
    }

    public enum MessageLevel {

        /**
         * Prints errors only.
         */
        /**
         * Prints errors only.
         */
        ERROR(-2, "--verbose-errors", "--ve"),
        /**
         * Prints errors and warnings.
         */
        WARNING(-1, "--verbose-warnings", "--vw"),
        /**
         * Prints errors, warnings and messages.
         */
        MESSAGE(0, "--verbose-normal"),
        /**
         * Imprime solo mensajes generales e informativos.
         */
        INFO(1, "--verbose"),
        /**
         * Imprime todos los mensajes.
         */
        DEBUG(2, "--debug"),
        /**
         * Imprime todos los mensajes y los de hebra.
         */
        THREAD(3, "--debug-thread");

        int level;
        String[] commandLineFlags;

        private MessageLevel(int verboseLevel, String... commandLineFlags) {
            this.level = verboseLevel;

            this.commandLineFlags = commandLineFlags;
        }

        @Override
        public String toString() {
            return name() + "(" + level + ")";
        }

        public boolean isPrinted(MessageLevel messageLevel) {
            return this.level >= messageLevel.level;
        }

        public String[] getCommandLineFlags() {
            return commandLineFlags;
        }

        public boolean isFlagPresent(ConsoleParameters consoleParameters) {
            for (String commandLineFlag : commandLineFlags) {
                if (consoleParameters.isFlagDefined(commandLineFlag)) {
                    return true;
                }
            }
            return false;
        }

        public static MessageLevel getPrintMessageLevel(ConsoleParameters consoleParameters) {
            if (THREAD.isFlagPresent(consoleParameters)) {
                return THREAD;
            } else if (DEBUG.isFlagPresent(consoleParameters)) {
                return DEBUG;
            } else if (INFO.isFlagPresent(consoleParameters)) {
                return INFO;
            } else if (MESSAGE.isFlagPresent(consoleParameters)) {
                return MESSAGE;
            } else if (WARNING.isFlagPresent(consoleParameters)) {
                return WARNING;
            } else if (ERROR.isFlagPresent(consoleParameters)) {
                return ERROR;
            } else {
                return MESSAGE;
            }
        }
    }
    /**
     * Indica si se deben mostrar mensajes o no. Los algoritmos indican en qué
     * punto están en cada momento. Por defecto no se muestran.
     */
    private static MessageLevel messageLevelPrinted = MessageLevel.MESSAGE;

    private static boolean doublePrint = false;

    /**
     * Muestra la cadena indicada como parámetro en la salida por defecto.
     *
     * @param message Mensaje a mostrar
     */
    public static void showInfoMessage(String message) {
        if (messageLevelPrinted.isPrinted(MessageLevel.MESSAGE)) {
            System.out.print(message);
            if (doublePrint) {
                System.err.print(message);
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
        if (messageLevelPrinted.isPrinted(MessageLevel.ERROR)) {
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
        if (messageLevelPrinted.isPrinted(MessageLevel.WARNING)) {
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
        if (messageLevelPrinted.isPrinted(MessageLevel.WARNING)) {
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
    public static boolean isInfoPrinted() {
        return messageLevelPrinted.isPrinted(MessageLevel.INFO);
    }

    public static boolean isDoublePrint() {
        return doublePrint;
    }

    public static boolean isDebugPrinted() {
        return messageLevelPrinted.isPrinted(MessageLevel.DEBUG);
    }

    public static boolean isVerboseAnnoying() {
        return messageLevelPrinted.isPrinted(MessageLevel.DEBUG);
    }

    public static void setMessageLevel(MessageLevel messageLevel) {
        messageLevelPrinted = messageLevel;
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
