package delfos;

import delfos.common.Global;
import delfos.common.parallelwork.Parallelisation;
import delfos.configuration.ConfigurationManager;
import delfos.view.InitialFrame;
import delfos.view.SwingGUI;
import delfos.view.recommendation.RecommendationWindow;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import org.jdom2.output.Format;

/**
 * Proporciona los métodos para lanzar distintas interfaces, según el uso que se
 * quiera dar a la biblioteca de recomendación
 *
 * 15/11/2012 Añadida la funcionalidad para controlar si se muestran los
 * resultados en bruto de las medidas basadas en curvas
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 15/11/2012
 */
public class Constants {

    /**
     * Parameter to specify the location of the directory that contains the
     * configurations xml files.
     */
    public static final String LIBRARY_CONFIGURATION_DIRECTORY = "-config";

    /**
     * Flag para indicar que no se desea obtener los mensajes de warning.
     */
    public static final String HIDE_WARNINGS = "-hideWarnings";
    /**
     * Flag que indica que se deben mostrar mensajes de los procesos que se
     * ejecutan.
     */
    public static final String VERBOSE = "-verbose";
    /**
     * Flag que indica que se deben mostrar mensajes de los procesos que se
     * ejecutan.
     */
    public static final String VERBOSE_SHORT = "-v";
    /**
     * Flag que indica que se deben mostrar mensajes de creación de hebras.
     */
    public static final String THREAD_VERBOSE = "-threadVerbose";
    /**
     * Flag que indica que se deben mostrar mensajes de creación de hebras.
     */
    public static final String THREAD_VERBOSE_SHORT = "-tv";

    /**
     * Flag que indica que se deben mostrar todos los mensajes de los procesos
     * que se ejecutan.
     */
    public static final String VERBOSE_ANNOYING = "-verbose2";
    /**
     * Flag para indicar que se ignoren los errores. No es recomendable usarlo.
     */
    public static final String HIDE_ERRORS = "-hideErrors";
    /**
     * Flag para indicar los mensajes que se impriman se deben hacer por ambas
     * salidas: estándar y de error.
     */
    public static final String DOUBLE_PRINT = "-doublePrint";
    /**
     * Flag para indicar que se escriban los datos en bruto en el fichero de
     * resultados de las ejecuciones.
     */
    static final String RAW_DATA = "-rawData";

    /**
     * Flag para indicar que se escriban los XML detallados de las ejecuciones.
     */
    static final String PRINT_FULL_XML = "-fullXML";
    /**
     * Parámetro de la línea de comandos para limitar el número de cpus
     * adicionales que se pueden utilizar, a parte de la hebra principal.
     *
     */
    static final String MAX_CPUS = "-maxCPU";
    /**
     * Valor de salida que la aplicacion lanza al terminar.
     */
    static int exitValue;

    /**
     * Código de error que se devuelve cuando el sistema de recomendación no
     * implementa {@link RecommenderSystemWithFilePersitence}.
     */
    public static final int ERROR_RECOMMENDER_SYSTEM_DONT_IMPLEMENT_FILE_PERSISTENCE = 10;

    private static boolean printFullXML;

    /**
     * Lanza la interfaz de recomendación, que permite ver qué recomendaciones
     * da un algoritmo a un usuario de un dataset.
     */
    public static void initRecommendationGUI() {
        RecommendationWindow frame = new RecommendationWindow();
        frame.setVisible(true);
    }

    /**
     * Lanza la interfaz de evaluación, que permite realizar experimentaciones
     * sobre un algoritmo de recomendación con un dataset.
     */
    public static void initEvaluationGUI() {
        InitialFrame frame = new InitialFrame();
        frame.setVisible(true);
    }
    /**
     * Característica que almacena si se debe escribir en la salida el resultado
     * en bruto de las medidas que generan curvas (como PRSpace y AUROC). Este
     * resultado en bruto representa los puntos de la curva.
     */
    private static boolean rawResult = false;

    /**
     * Devuelve true si se deben escribir resultados en bruto en los XML de
     * resultados
     *
     * @return true si se deben escribir resultados en bruto en los XML de
     * resultados. false si no se deben incluir
     */
    public static boolean isRawResultDefined() {
        return rawResult;
    }

    /**
     * Establece si se deben escribir en la salida XML los resultados en bruto
     * de las medidas de precisión y recall y auroc
     *
     * @param rawResult true para escribir los resultados, false para omitirlos
     *
     */
    public static void setRawResult(boolean rawResult) {
        Constants.rawResult = rawResult;
    }

    /**
     * Returns the current time in milliseconds. Note that while the unit of
     * time of the return value is a millisecond, the granularity of the value
     * depends on the underlying operating system and may be larger. For
     * example, many operating systems measure time in units of tens of
     * milliseconds. See the description of the class Date for a discussion of
     * slight discrepancies that may arise between "computer time" and
     * coordinated universal time (UTC).
     *
     * @return The difference, measured in milliseconds, between the current
     * time and midnight, January 1, 1970 UTC.
     */
    public static long getCurrentTimeMillis() {
        return System.currentTimeMillis();
    }

    /**
     * Inicializa los parámetros básicos de la interacción con la biblioteca,
     * comprobando los argumentos de la linea de comandos.
     *
     * <p>
     * <p>
     * Actualmente comprueba si existen los flags para mostrar mensajes u
     * ocultar warnings o errores.
     *
     * @param consoleParameters
     */
    public static void initLibraryGeneralParameters(ConsoleParameters consoleParameters) {

        if (consoleParameters.isDefined(LIBRARY_CONFIGURATION_DIRECTORY)) {
            String configDirectory = consoleParameters.getValue(LIBRARY_CONFIGURATION_DIRECTORY);
            ConfigurationManager.CONFIGURATION_DIRECTORY = new File(configDirectory + File.separator);
        }

        if (!ConfigurationManager.CONFIGURATION_DIRECTORY.exists()) {
            boolean mkdir = ConfigurationManager.CONFIGURATION_DIRECTORY.mkdirs();
            if (!mkdir) {
                IOException ex = new IOException("Cannot create '" + ConfigurationManager.CONFIGURATION_DIRECTORY.getAbsolutePath() + "' directory");
                ERROR_CODES.CANNOT_WRITE_LIBRARY_CONFIG_FILE.exit(ex);
            }
        } else {
            Global.showMessage("Configuration directory exists. (" + ConfigurationManager.CONFIGURATION_DIRECTORY.getAbsolutePath() + ")\n");
        }

        Locale.setDefault(Locale.ENGLISH);

        if (consoleParameters.isDefined(THREAD_VERBOSE) || consoleParameters.isDefined(THREAD_VERBOSE_SHORT)) {
            Global.setThreadVerbose(true);
        }

        if (consoleParameters.isDefined(VERBOSE) || consoleParameters.isDefined(VERBOSE_SHORT)) {
            Global.setVerbose();
        }

        if (consoleParameters.isDefined(VERBOSE_ANNOYING)) {
            Global.setVerboseAnnoying();
        }

        if (consoleParameters.isDefined(HIDE_WARNINGS)) {
            Global.setShowWarnings(false);
        }
        if (consoleParameters.isDefined(HIDE_ERRORS)) {
            Global.setShowErrors(false);
        }

        if (consoleParameters.isDefined(DOUBLE_PRINT)) {
            Global.setDoublePrint(true);
        }

        if (consoleParameters.isDefined(RAW_DATA)) {
            Constants.setRawResult(true);
        }

        if (consoleParameters.isDefined(PRINT_FULL_XML)) {
            Constants.setPrintFullXML(true);
        }

        if (consoleParameters.isDefined(MAX_CPUS)) {
            String value = "0";
            try {
                value = consoleParameters.getValue(MAX_CPUS);
            } catch (UndefinedParameterException ex) {
                //Este error no se puede producir nunca.
                ERROR_CODES.UNDEFINED_ERROR.exit(ex);
            }

            int numCPU = Integer.parseInt(value);
            Parallelisation.setMaxCPU(numCPU);
        }
    }

    /**
     * Devuelve el formato que se usa para guardar los archivos XML.
     *
     * @return
     */
    public static Format getXMLFormat() {
        return Format.getPrettyFormat().setEncoding("ISO-8859-1");
    }

    public static void initBuildGUI() {
        SwingGUI.initRSBuilderGUI("recommenderSystemConfiguration.xml");
    }

    /**
     * Establece si se debe terminar la ejecución del programa al más mínimo
     * error que se detecte o se debe continuar y lanzar excepción.
     *
     * @param b
     */
    public static void setExitOnFail(boolean b) {
        ERROR_CODES.setExitOnFail(b);
    }

    /**
     * Realiza la configuración para que la biblioteca funcione en modo
     * depuración. Se debe activar antes de los test Junit.
     */
    public static void setJUnitTestMode() {
        Global.setVerbose();
        Global.setDoublePrint(false);
        setExitOnFail(false);
        Global.setShowErrors(true);
        Global.setShowWarnings(true);
        Global.setDefaultAnswerYes(true);
    }

    private static void setPrintFullXML(boolean printFullXML) {
        Constants.printFullXML = printFullXML;
    }

    public static boolean isPrintFullXML() {
        return printFullXML;
    }

}
