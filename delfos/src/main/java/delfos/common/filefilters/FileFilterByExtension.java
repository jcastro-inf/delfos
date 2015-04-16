package delfos.common.filefilters;

import java.io.File;
import java.io.FileFilter;

/**
 * Filtro de ficheros que acepta sólo los ficheros que son directorios.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 16-May-2013
 * @version 10-Enero-2014 Ahora se permite especificar si se listan directorios
 * o no.
 */
public class FileFilterByExtension implements FileFilter {

    private final boolean listFolders;

    private final String[] extensionsAllowed;

    /**
     * Constructor del filtro que permite también archivos de tipo directorio.
     *
     * @param _extension
     */
    public FileFilterByExtension(String... _extension) {
        this(true, _extension);
    }

    public FileFilterByExtension(boolean listFolders, String... _extension) {
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
        this.listFolders = listFolders;
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

        if (f.isDirectory() && listFolders) {
            return true;
        }
        for (String extensionAllowed : extensionsAllowed) {
            if (extension.equals(extensionAllowed)) {
                return true;
            }
        }
        return false;
    }
}
