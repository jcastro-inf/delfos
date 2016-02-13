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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
