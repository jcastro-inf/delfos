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
import java.io.IOException;
import org.jdom2.Element;
import delfos.io.xml.parameterowner.parameter.DirectoryParameterXML;
import delfos.common.Global;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.ParameterOwner;

/**
 * Restricción de parámetro de tipo directorio. Esta restricción comprueba que
 * los valores del parámetro son directorios.
 *
 * <p><p>NOTA: Si el directorio especificado no existe, a veces no es posible
 * determinar si es un archivo o un fichero, por lo que se supone que es un
 * directorio.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 25-Julio-2013
 */
public class DirectoryParameter extends ParameterRestriction {

    private final static long serialVersionUID = 1L;

    /**
     * Constructor por defecto de esta restricción, que establece el directorio
     * por defecto.
     *
     * <p><p>NOTA: No se comprueba fichero indicado como valor por defecto
     * existe.
     *
     * @param defaultFile Valor por defecto del parámetro.
     */
    public DirectoryParameter(File defaultFile) {
        super(defaultFile);

        if (defaultFile == null) {
            throw new IllegalArgumentException("The default file cant be null.");
        }

        if (!isCorrect(defaultValue)) {
            throw new IllegalArgumentException("The default value isn't correct");
        }

    }

    @Override
    public final boolean isCorrect(Object o) {
        if (o instanceof File) {
            File file = (File) o;
            File absoluteFile;
            try {
                absoluteFile = file.getCanonicalFile();
            } catch (IOException ex) {
                absoluteFile = file.getAbsoluteFile();
            }

            boolean ret = absoluteFile.isDirectory();
            if (ret == false) {

                boolean existe = absoluteFile.exists();
                boolean isAbsolute = absoluteFile.isAbsolute();
                boolean isFile = absoluteFile.isFile();
                boolean idDirectory = absoluteFile.isDirectory();

                if (!existe) {
                    return true;
                } else {
                    Global.showError(new IllegalStateException(file + " not a directory (Absolute file path: '" + absoluteFile.toString() + "'"));
                }
            }
            return ret;
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
        return DirectoryParameterXML.getParameterValue(parameterOwner, elementParameter);
    }

    @Override
    public Element getXMLElement(ParameterOwner parameterOwner, Parameter parameter) {
        return DirectoryParameterXML.getDirectoryParameterElement(parameterOwner, parameter);
    }
}
