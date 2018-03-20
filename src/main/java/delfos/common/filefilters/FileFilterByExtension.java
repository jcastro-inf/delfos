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
package delfos.common.filefilters;

import java.io.File;
import java.io.FileFilter;
import java.util.function.Predicate;

/**
 * Filtro de ficheros que acepta sólo los ficheros que son directorios.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 16-May-2013
 * @version 10-Enero-2014 Ahora se permite especificar si se listan directorios
 * o no.
 */
public class FileFilterByExtension implements FileFilter,Predicate<File> {

    private final boolean listDirectorys;

    private final String[] extensionsAllowed;

    /**
     * Constructor del filtro que permite también archivos de tipo directorio.
     *
     * @param _extension
     */
    public FileFilterByExtension(String... _extension) {
        this(true, _extension);
    }

    public FileFilterByExtension(boolean listDirectorys, String... _extension) {
        if (_extension == null) {
            throw new IllegalArgumentException("The extension vector cannot be null");
        }
        if (_extension.length == 0) {
            throw new IllegalArgumentException("The extensions vector cannot be empty.");
        }
        for (int i = 0; i < _extension.length; i++) {
            if (_extension[i] == null) {
                throw new IllegalArgumentException("The extension is null (index=" + i + ").");
            }
        }
        this.listDirectorys = listDirectorys;
        this.extensionsAllowed = _extension;
    }

    @Override
    public boolean accept(File f) {
        String extension;

        int ultimoSeparador = f.getAbsolutePath().lastIndexOf(File.separator);
        int ultimoPunto = f.getAbsolutePath().lastIndexOf('.');
        if (ultimoPunto >= 0 && ultimoPunto > ultimoSeparador) {
            extension = f.getAbsolutePath().substring(ultimoPunto + 1);
        } else {
            extension = "";
        }

        if (f.isDirectory() && listDirectorys) {
            return true;
        }
        for (String extensionAllowed : extensionsAllowed) {
            if (extension.equals(extensionAllowed)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean test(File file) {
        return accept(file);
    }
}
