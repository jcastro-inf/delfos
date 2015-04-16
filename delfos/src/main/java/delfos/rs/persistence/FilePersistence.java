package delfos.rs.persistence;

import java.io.File;
import java.io.IOException;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.common.parameters.restriction.DirectoryParameter;
import delfos.common.parameters.restriction.StringParameter;

/**
 * Objeto que genera el archivo en que se almacena el modelo generado por un
 * sistema de recomendación.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 15-Mar-2013 Añadida la posibilidad de que los archivos generados
 * con esta persistencia estén dentro de un directorio.
 * @version 2.0 Ahora implementa la interfaz {@link ParameterOwner}.
 */
public class FilePersistence extends ParameterOwnerAdapter implements PersistenceMethod {

    private static final long serialVersionUID = 1L;
    public static final Parameter fileNameParameter = new Parameter(
            "fileNameParameter",
            new StringParameter("filePersistence"));
    public static final Parameter prefixParameter = new Parameter(
            "prefix",
            new StringParameter(""));
    public static final Parameter sufixParameter = new Parameter(
            "sufix",
            new StringParameter(""));
    public static final Parameter extensionParameter = new Parameter(
            "extension",
            new StringParameter("dat"));
    public static final Parameter directoryParameter = new Parameter(
            "directory",
            new DirectoryParameter(new File("").getAbsoluteFile()));

    public FilePersistence() {
        super();
        addParameter(fileNameParameter);
        addParameter(prefixParameter);
        addParameter(sufixParameter);
        addParameter(extensionParameter);
        addParameter(directoryParameter);
    }

    public FilePersistence(String fileName, String fileType) {
        this();
        setParameterValue(fileNameParameter, fileName);
        setParameterValue(extensionParameter, fileType);
    }

    public FilePersistence(String fileName, String fileType, File directory) {
        this();
        setParameterValue(fileNameParameter, fileName);
        setParameterValue(extensionParameter, fileType);
        setParameterValue(directoryParameter, directory);
    }

    public FilePersistence(String fileName, String fileType, String prefix, String sufix, File directory) {

        this();
        setParameterValue(fileNameParameter, fileName);
        setParameterValue(extensionParameter, fileType);
        setParameterValue(directoryParameter, directory);
        setParameterValue(prefixParameter, prefix);
        setParameterValue(sufixParameter, sufix);
    }

    public String getFileName() {
        return (String) getParameterValue(fileNameParameter);
    }

    public String getPrefix() {
        return (String) getParameterValue(prefixParameter);
    }

    public String getSuffix() {
        return (String) getParameterValue(sufixParameter);
    }

    public String getExtension() {
        return (String) getParameterValue(extensionParameter);
    }

    public File getDirectory() {
        return (File) getParameterValue(directoryParameter);
    }

    /**
     * Devuelve el nombre completo del archivo que se usa para guardar el modelo
     * de recomendación.
     *
     * @return Nombre completo del archivo.
     */
    public String getCompleteFileName() {
        try {
            String completeFileName = getPrefix() + getFileName() + getSuffix() + "." + getExtension();
            completeFileName = getDirectory().getCanonicalPath() + File.separator + completeFileName;
            return completeFileName;
        } catch (IOException ex) {
            Global.showWarning("Failure in converting directory to route");
            Global.showError(ex);
            throw new IllegalArgumentException(ex);
        }
    }

    public File getCompleteFile() {
        return new File(getCompleteFileName());
    }

    /**
     * Realiza una copia del método de persistencia, añadiendo un prefijo al
     * nombre del fichero en que se guardará el modelo persistente del sistema
     * de recomendación. Se utiliza en sistemas de recomendaciones híbridos
     *
     * @param prefix Prefijo de los archivos de persistencia.
     *
     * @return Devuelve una persistencia de tipo archivo que usa el fichero
     * original con el prefijo indicado
     * @see HybridRecommender
     */
    public FilePersistence copyWithPrefix(String prefix) {
        FilePersistence filePersistence = new FilePersistence(
                getFileName(),
                getExtension(),
                prefix + getPrefix(),
                getSuffix(),
                getDirectory());
        return filePersistence;
    }

    /**
     * Realiza una copia del método de persistencia, añadiendo un prefijo al
     * nombre del fichero en que se guardará el modelo persistente del sistema
     * de recomendación. Se utiliza en sistemas de recomendaciones híbridos.
     *
     * @param sufix Sufijo utilizado.
     *
     * @return Devuelve una persistencia de tipo archivo que usa el fichero
     * original con el sufijo indicado
     * @see HybridRecommender
     */
    public FilePersistence copyWithSuffix(String sufix) {
        FilePersistence filePersistence = new FilePersistence(
                getFileName(),
                getExtension(),
                getPrefix(),
                getSuffix() + sufix,
                getDirectory());
        return filePersistence;
    }

    /**
     * Realiza una copia del método de persistencia, añadiendo un prefijo al
     * nombre del fichero en que se guardará el modelo persistente del sistema
     * de recomendación. Se utiliza en sistemas de recomendaciones híbridos
     *
     * @param fileName Nuevo nombre del archivo.
     *
     * @return Devuelve una persistencia de tipo archivo que usa el fichero
     * original con el sufijo indicado
     * @see HybridRecommender
     */
    public FilePersistence copyWithName(String fileName) {
        FilePersistence filePersistence = new FilePersistence(
                fileName,
                getExtension(),
                getPrefix(),
                getSuffix(),
                getDirectory());
        return filePersistence;
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.PERSISTENCE_METHOD;
    }
}
