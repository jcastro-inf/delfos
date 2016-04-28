/*
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package delfos.common;

import delfos.ConsoleParameters;
import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Clase que encapsula los métodos para mostrar información en la consola (o en la salida por defecto) de la aplicación
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (03/01/2013)
 */
public class Global {

    private static final List<PrintStream> standardOutputWriters = new ArrayList<>();
    private static final List<PrintStream> errorOutputWriters = new ArrayList<>();

    private static final List<FileWriter> standardOutputLoggers = new ArrayList<>();
    private static final List<FileWriter> errorOutputLoggers = new ArrayList<>();

    static {
        standardOutputWriters.add(System.out);
        errorOutputWriters.add(System.err);
    }

    public static void addStandardOutputLogger(FileWriter fileWriter) {
        standardOutputLoggers.add(fileWriter);
    }

    public static void addErrorOutputLogger(FileWriter fileWriter) {
        errorOutputLoggers.add(fileWriter);
    }

    public static void removeStandardOutputLogger(FileWriter fileWriter) {
        standardOutputLoggers.remove(fileWriter);
    }

    public static void removeErrorOutputLogger(FileWriter fileWriter) {
        errorOutputLoggers.remove(fileWriter);
    }

    public static boolean isPrintDatasets() {
        return false;
    }

    /**
     * Hace una pregunta cerrarda al usuario por linea de comandos cuya respuesta es sí o no. Si el usuario da una
     * respuesta distinta, vuelve a formularle la pregunta indicando que solo puede responder sí o no.
     *
     * @param textOfQuestion Texto informativo que se muestra al usuario para informar de la decisión que debe tomar.
     * @return true si el usuario responde sí, false si responde no.
     */
    public static boolean askUser(String textOfQuestion) {
        if (Global.isDefaultAnswerYes()) {
            return true;
        }

        while (true) {
            printStandard(textOfQuestion + "\n");

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
                    printStandard("Answer was null. Assuming answer is yes\n");
                    return true;
                }

            } catch (IOException ioe) {
            }

            //Ha respondido algo distinto, insistir
            printStandard("The answer should be yes or no (y/n).\n");
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
            printError("\tTHREAD: " + warningMessage + "\n");
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

    public static void show(String message) {
        printStandard(message);
    }

    public static void showln(String message) {
        printStandard(message + "\n");
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
                return messageLevelPrinted;
            }
        }
    }
    /**
     * Indica si se deben mostrar mensajes o no. Los algoritmos indican en qué punto están en cada momento. Por defecto
     * no se muestran.
     */
    private static MessageLevel messageLevelPrinted = MessageLevel.MESSAGE;

    /**
     * Muestra la cadena indicada como parámetro en la salida por defecto.
     *
     * @param message Mensaje a mostrar
     */
    public static void showInfoMessage(String message) {
        if (messageLevelPrinted.isPrinted(MessageLevel.INFO)) {
            printStandard(message);
        }
    }

    /**
     * Muestra la cadena indicada como parámetro en la salida por defecto.
     *
     * @param message Mensaje a mostrar
     */
    public static void showMessage(String message) {
        if (messageLevelPrinted.isPrinted(MessageLevel.MESSAGE)) {
            printStandard(message);
        }
    }

    /**
     * Muestra el estado de la pila en el momento en que se creó la excepción indicada por parámetro.
     *
     * @param ex Excepción con la información del error.
     */
    public static void showError(Throwable ex) {
        if (messageLevelPrinted.isPrinted(MessageLevel.ERROR)) {
            ex.printStackTrace(System.err);
        }
    }

    /**
     * Muestra la cadena indicada como parámetro en la salida de error.
     *
     * @param warningMessage Mensaje a mostrar
     */
    public static void showWarning(String warningMessage) {
        if (messageLevelPrinted.isPrinted(MessageLevel.WARNING)) {
            printError("WARNING: " + warningMessage + "\n");
        }
    }

    /**
     * Muestra el estado de la pila en el momento en que se creó la excepción indicada por parámetro.
     *
     * @param ex Excepción con la información de la advertencia.
     */
    public static void showWarning(Throwable ex) {
        if (messageLevelPrinted.isPrinted(MessageLevel.WARNING)) {
            ex.printStackTrace(System.err);
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

    public static boolean isDebugPrinted() {
        return messageLevelPrinted.isPrinted(MessageLevel.DEBUG);
    }

    public static boolean isVerboseAnnoying() {
        return messageLevelPrinted.isPrinted(MessageLevel.DEBUG);
    }

    public static void setMessageLevel(MessageLevel messageLevel) {
        messageLevelPrinted = messageLevel;
    }

    public static void printStandard(String message) {
        standardOutputLoggers.stream().forEach(logger -> {
            try {
                logger.append(message);
            } catch (IOException ex) {
                Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        standardOutputWriters.stream().forEach(logger -> {
            logger.append(message);
        });
    }

    public static void printError(String message) {
        errorOutputLoggers.stream().forEach(logger -> {
            try {
                logger.append(message);
            } catch (IOException ex) {
                Logger.getLogger(Global.class.getName()).log(Level.SEVERE, null, ex);
            }
        });

        errorOutputWriters.stream().forEach(logger -> {
            logger.append(message);
        });
    }
}
