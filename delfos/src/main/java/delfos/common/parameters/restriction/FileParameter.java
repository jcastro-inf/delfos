package delfos.common.parameters.restriction;

import java.io.File;
import java.io.FileFilter;
import org.jdom2.Element;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;
import delfos.io.xml.parameterowner.parameter.FileParameterXML;

/**
 * Restricción de parámetro de tipo fichero. Esta restricción comprueba que los
 * valores del parámetro satisfacen las restricciones. En este caso, la
 * restricción es que la extensión del archivo sea una de las extensiones
 * permitidas.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (Unknow date)
 * @version 1.1 18-Jan-2013
 */
public class FileParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;
    /**
     * Filtro que define los archivos que puede tomar este parámetro como valor.
     */
    private final FileFilter fileFilter;

    /**
     * Constructor por defecto de esta restricción, que establece el archivo por
     * defecto y las extensiones de los archivos que acepta.
     *
     * <p>
     * <p>
     * NOTA: No se comprueba fichero indicado como valor por defecto existe.
     *
     * @param defaultFile Valor por defecto del parámetro.
     * @param fileFilter Filtro que define los archivos que puede tomar este
     * parámetro como valor.
     */
    public FileParameter(File defaultFile, FileFilter fileFilter) {
        super(defaultFile);
        this.fileFilter = fileFilter;

        if (defaultFile == null) {
            throw new IllegalArgumentException("The default file cant be null.");
        }

        if (fileFilter == null) {
            throw new IllegalArgumentException("The file filter cannot be null.");
        }

        if (!isCorrect(defaultValue)) {
            throw new IllegalArgumentException("The default value '" + defaultValue + "'isn't correct");
        }

    }

    @Override
    public final boolean isCorrect(Object o) {
        if (o instanceof File) {
            File f = (File) o;
            return fileFilter.accept(f);
        } else {
            return false;
        }
    }

    @Override
    public Object parseString(String parameterValue) {
        return new File(parameterValue);
    }

    @Override
    public Object getValue(ParameterOwner parameterOwner, Element elementParameter) {
        return FileParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return FileParameterXML.getFileParameterElement(parameterOwner, parameter);
    }

    /**
     * Devuelve el filtro de ficheros de esta restricción.
     *
     * @return
     */
    public FileFilter getFileFilter() {
        return fileFilter;
    }
}
