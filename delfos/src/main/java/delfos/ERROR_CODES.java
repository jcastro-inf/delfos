package delfos;

import delfos.common.parameters.ParameterOwner;
import delfos.dataset.changeable.ChangeableDatasetLoaderAbstract;
import java.util.Map;
import java.util.TreeMap;

/**
 * Enumerado con todos los códigos de error de salida de la biblioteca de
 * recomendación.
 *
 * @version 1.0 30-Abril-2013
 *
 * @author Jorge Castro Gallardo
 */
public enum ERROR_CODES {

    /**
     * Código de error que se devuelve cuando no se conoce el motivo de fallo de
     * la biblioteca. No está recomendado el uso de este código de error, ya que
     * no describe el error que ha ocurrido.
     */
    /**
     * Código de error que se devuelve cuando no se conoce el motivo de fallo de
     * la biblioteca. No está recomendado el uso de este código de error, ya que
     * no describe el error que ha ocurrido.
     */
    UNDEFINED_ERROR(9999),
    /**
     * Codigo de error que se devuelve cuando no se encuentra el usuario
     * especificado.
     */
    USER_NOT_FOUND(1011),
    /**
     * Código de error que se devuelve cuando se solicita una recomendación pero
     * no se ha indicado el id del usuario por línea de comandos.
     */
    USER_NOT_DEFINED(1012),
    USER_NOT_ENOUGHT_INFORMATION(1021),
    /**
     * Código de error que se devuelve cuando no se encuentra el producto
     * especificado.
     */
    ITEM_NOT_FOUND(1031),
    ITEM_ALREADY_EXISTS(1032),
    ITEM_NOT_DEFINED(1033),
    /**
     * Código de error que se devuelve cuando no se encuentra el archivo de
     * configuración especificado.
     */
    CONFIG_FILE_NOT_EXISTS(2001),
    /**
     * Código de error que se devuelve cuando no se encuentra el dataset de
     * valoraciones.
     */
    RATINGS_DATASET_NOT_FOUND(2002),
    /**
     * Código de error que se devuelve cuando no se encuentra el dataset de
     * contenido.
     */
    CONTENT_DATASET_NOT_FOUND(2003),
    /**
     * Código de error que se devuelve cuando no se encuentra el fichero de
     * donde cargar el modelo del sistema de recomendación.
     */
    MODEL_FILE_NOT_FOUND(2004),
    /**
     * Código de error que se devuelve cuando no se encuentra el fichero de
     * configuración de base de datos.
     */
    DATABASE_CONFIG_FILE_NOT_FOUND(2005),
    /**
     * Código de error que se devuelve cuando no se encuentra el
     * {@link DatasetLoader} especificado.
     */
    DATASET_LOADER_NOT_FOUND(3001),
    /**
     * Código de error que se devuelve cuando no se ha especificado el usuario
     * al que recomendar.
     */
    RECOMMEND_MODE_USER_NOT_DEFINED(4001),
    USER_ALREADY_EXISTS(4003),
    /**
     * Código de error que se devuelve cuando el sistema de recomendación no
     * implementa {@link RecommenderSystemWithFilePersitence}.
     */
    RECOMMENDER_SYSTEM_DONT_IMPLEMENT_FILE_PERSISTENCE(3002),
    /**
     * Código de error que se devuelve cuando el sistema de recomendación no
     * implementa {@link RecommenderSystemWithDatabasePersistence}.
     */
    RECOMMENDER_SYSTEM_DONT_IMPLEMENT_DATABASE_PERSISTENCE(3003),
    /**
     * Código de eror que la biblioteca devuelve cuando no se puede cargar el
     * dataset de valoraciones.
     */
    CANNOT_LOAD_RATINGS_DATASET(5001),
    /**
     * Código de eror que la biblioteca devuelve cuando no se puede cargar el
     * dataset de contenido.
     */
    CANNOT_LOAD_CONTENT_DATASET(5002),
    /**
     * Código de eror que la biblioteca devuelve cuando no se puede cargar el
     * dataset de contenido.
     */
    CANNOT_LOAD_USERS_DATASET(5003),
    /**
     * No se puede cargar el archivo de configuración XML que describe el
     * sistema de recomendacion a utilizar.
     */
    CANNOT_LOAD_CONFIG_FILE(6001),
    /**
     * No se puede almacenar/recuperar el modelo del sistema de recomendación.
     */
    FAILURE_IN_PERSISTENCE(7001),
    /**
     * No se ha definido el fichero de configuración.
     */
    CONFIG_FILE_NOT_DEFINED(4002),
    /**
     * No se puede convertir la cadena a un id de usuario.
     */
    USER_ID_NOT_RECOGNISED(8001),
    /**
     * No se puede convertir la cadena a un id de producto.
     */
    ITEM_ID_NOT_RECOGNISED(8002),
    /**
     * No se puede escribir en el archivo.
     */
    CANNOT_WRITE_FILE(19),
    /**
     * No se ha definido el archivo de configuración PHP
     */
    COMMAND_LINE_PARAMETER_IS_NOT_DEFINED(20),
    /**
     * No se puede establecer la conexión con la base de datos.
     */
    DATABASE_NOT_READY(21),
    /**
     * Se ha intentado acceder a clases de otra librería y no se han encontrado.
     */
    DEPENDENCY_NOT_FOUND(22),
    /**
     * No se encuentra el archivo de indice de aceites-productos. Parche para
     * corregir el tratamiento de errores.
     */
    IAP_FILE_NOT_FOUND(26),
    /**
     * No se reconoce el formato del archivo de indice de aceites-productos.
     * Parche para corregir el tratamiento de errores.
     */
    IAP_FILE_FORMAT_NOT_RECOGNISED(23),
    /**
     * No se pueden leer las recomendaciones.
     */
    CANNOT_READ_RECOMMENDATIONS(24),
    /**
     * No se pueden escribir las recomendaciones en la persistencia.
     */
    CANNOT_WRITE_RECOMMENDATIONS(28),
    /**
     * No se puede leer el archivo de configuración de la biblioteca.
     */
    CANNOT_READ_LIBRARY_CONFIG_FILE(25),
    /**
     * No se puede escribir el archivo de configuración de la biblioteca.
     */
    CANNOT_WRITE_LIBRARY_CONFIG_FILE(27),
    /**
     * No se puede escribir el dataset de valoraciones.
     */
    CANNOT_WRITE_RATINGS_DATASET(7101),
    /**
     * No se puede escribir el dataset de contenido.
     */
    CANNOT_WRITE_CONTENT_DATASET(7102),
    /**
     * No se puede escribir el dataset de usuarios.
     */
    CANNOT_WRITE_USERS_DATASET(7103),
    /**
     * No se puede escribir el fichero XML con los resultados del caso de
     * estudio.
     */
    CANNOT_WRITE_RESULTS_FILE(7104),
    /**
     * No se puede escribir el archivo de configuración que describe el sistema
     * de recomendación.
     */
    CANNOT_SAVE_CONFIG_FILE(29),
    /**
     * No se reconoce el elemento de XML.
     */
    UNRECOGNIZED_XML_ELEMENT(30),
    /**
     * El valor de los parámetros de un {@link ParameterOwner} son
     * incompatibles.
     */
    PARAMETER_VIOLATION(31),
    /**
     * Se han solicitado valores a una factoría pero no conoce ninguna instancia
     * que satisfaga las condiciones para ser devuelta.
     */
    NO_INSTANCES_IN_FACTORY(32),
    /**
     * No se ha espeficicado fichero de base de datos de ratings en el modo de
     * gestión de base de datos de ratings o no se encuentra el archivo
     * especificado.
     */
    MANAGE_RATING_DATABASE_CONFIGURATION_FILE_NOT_DEFINED(5001),
    /**
     * Cuando se intenta utilizar un dataset que no implementa
     * {@link ChangeableDatasetLoaderAbstract} como un dataset modificable.
     */
    MANAGE_RATING_DATABASE_DATASET_NOT_CHANGEABLE(5002),
    /**
     * Si el usuario que se desea añadir ya existía.
     */
    MANAGE_RATING_DATABASE_USER_ALREADY_EXISTS(5011),
    /**
     * Código de error que se devuelve cuando no se ha especificado el
     * identificador del usuario a añadir.
     */
    MANAGE_RATING_DATABASE_USER_NOT_DEFINED(5012),
    /**
     * Si el producto que se desea añadir ya existía.
     */
    MANAGE_RATING_DATABASE_ITEM_ALREADY_EXISTS(5021),
    /**
     * Código de error que se devuelve cuando no se ha especificado el
     * identificador del producto a añadir.
     */
    MANAGE_RATING_DATABASE_ITEM_NOT_DEFINED(5022),
    /**
     * Código de error que se devuelve cuando se está intentando añadir una
     * valoración (rating) al dataset pero no se ha especificado el valor del
     * mismo en la línea de comandos.
     */
    MANAGE_RATING_DATABASE_RATINGS_VALUE_NOT_DEFINED(5032),
    /**
     * Si la sintaxis de alguna de las características a añadir no es correcta.
     */
    MANAGE_RATING_DATABASE_WRONG_FEATURES_SYNTAX(5101),
    /**
     * Si no se han espedificado las caracterínkm 6ft3sticas en el modo añadir
     * caraacterísticas.
     */
    MANAGE_RATING_DATABASE_FEATURES_NOT_DEFINED(5102),
    /**
     * Persistencia no soportada.
     */
    UNSUPPORTED_PERSISTENCE_METHOD(991),
    RECOMMENDER_SYSTEM_DONT_IMPLEMENT_UPDATE_ONE_USER_PROFILE(3004),
    /**
     * Cuando el usuario decide que no desea terminar continuar la ejecución de
     * la biblioteca.
     */
    OPERATION_ABORTED_BY_USER(4000),
    CANNOT_READ_CASE_STUDY_XML(26),
    CANNOT_WRITE_CASE_STUDY_XML(27),
    CANNOT_READ_CASE_STUDY_EXCEL(28),
    CANNOT_WRITE_CASE_STUDY_EXCEL(29),
    CANNOT_WRITE_CONFIGURED_DATASETS_FILE(30),
    CANNOT_READ_CONFIGURED_DATASETS_FILE(31),
    NOT_A_GROUP_RECOMMENDER_SYSTEM(9999991),
    NOT_A_RECOMMENDER_SYSTEM(9999992),
    TASK_EXECUTION_FAILED(9999993),
    THREAD_INTERRUMPTED(9999994),
    PARAMETER_OWNER_ILLEGAL_PARAMETER_VALUE(9999995),
    PARAMETER_OWNER_NOT_HAVE_PARAMETER(9999996),
    GROUP_NOT_DEFINED(239847789),
    COMMAND_LINE_PARAMETERS_ERROR(123214);

    private static boolean isExitOnFail = true;

    /**
     * Establece si se debe llamar a {@link System#exit(int) } cuando ocurra un
     * error (true) o sólo lanzar una excepción {@link IllegalStateException}
     * (false). Esta excepción JAMÁS debe ser capturada en la lógica del
     * programa.
     *
     * @param isExitOnFail
     */
    public static void setExitOnFail(boolean isExitOnFail) {
        ERROR_CODES.isExitOnFail = isExitOnFail;
    }
    /**
     * Código de salida del error.
     */
    private final int exitValue;

    private static Map<Integer, String> errorCodes;

    private ERROR_CODES(int exitValue) {
        this.exitValue = exitValue;
        addThisErrorCode();
    }

    private void addThisErrorCode() {

        if (errorCodes == null) {
            errorCodes = new TreeMap<>();
        }

        if (errorCodes.containsKey(exitValue)) {
            errorCodeClash(exitValue);
        } else {
            errorCodes.put(exitValue, this.name());
        }
    }

    private void errorCodeClash(int exitValue) {
        String error = errorCodes.get(exitValue);
        IllegalArgumentException ex = new IllegalArgumentException(this.name() + ": Error code '" + exitValue + "' already used by '" + error + "' error");
    }

    public void exit(Throwable ex) throws RuntimeException {
        System.out.flush();
        System.err.flush();

        ex.printStackTrace(System.out);
        System.out.flush();

        ex.printStackTrace(System.err);
        System.err.flush();

        System.err.print("Error exit code " + this.name() + ":" + exitValue + "\n");
        System.err.flush();
        System.out.print("Error exit code " + this.name() + ":" + exitValue + "\n");
        System.out.flush();

        if (isExitOnFail) {
            System.exit(exitValue);
        } else {
            if (ex instanceof RuntimeException) {
                RuntimeException runtimeException = (RuntimeException) ex;
                throw runtimeException;
            } else {
                throw new IllegalStateException(ex);
            }
        }
    }
}
