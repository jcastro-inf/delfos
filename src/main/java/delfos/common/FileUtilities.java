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

import delfos.ERROR_CODES;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 17-May-2013
 */
public class FileUtilities {

    /**
     * Used to execute the create/delete files methods synchronized.
     */
    public static final Object EXMUT = 1.0;

    public static synchronized File addPrefix(File originalFile, String prefix) {
        String file = originalFile.getAbsolutePath();
        String newFileName = file.substring(0, file.lastIndexOf(File.separator)) + File.separator + prefix + file.substring(file.lastIndexOf(File.separator) + 1, file.length());
        return new File(newFileName);
    }

    public static File addSufix(File originalFile, String sufix) {

        String file = originalFile.getAbsolutePath();
        if (file.substring(file.lastIndexOf(File.separator)).lastIndexOf('.') == -1) {
            //El archivo no tiene extensión, añadir directamente al final del nombre.
            String newFileName = file + sufix;
            return new File(newFileName);
        } else {
            String newFileName = file.substring(0, file.lastIndexOf('.')) + sufix + "." + file.substring(file.lastIndexOf('.') + 1, file.length());
            return new File(newFileName);
        }
    }

    public static File changeExtension(File originalFile, String extension) {
        String file = originalFile.getAbsolutePath();
        if (file.lastIndexOf('.') == -1) {
            throw new IllegalArgumentException("The file do not have a extension.");
        }

        String newFileName = file.substring(0, file.lastIndexOf('.')) + "." + extension;
        return new File(newFileName);
    }

    /**
     * Borra el directorio indicado y todos los archivos y directorios que cuelgan de él.
     *
     * @param directory Directorio a borrar.
     */
    public static void deleteDirectoryRecursive(File directory) {
        synchronized (EXMUT) {
            if (!directory.exists()) {
                return;
            }
            if (!directory.isDirectory()) {
                throw new IllegalArgumentException("The passed file is not a directory (" + directory + ").");
            }
            if (!directory.exists()) {
                throw new IllegalArgumentException("The directory must exist (" + directory + ")");
            }
            File[] listFiles = directory.listFiles();
            for (File f : listFiles) {
                if (f.isDirectory()) {
                    deleteDirectoryRecursive(f);
                } else {
                    f.delete();
                }
            }
            directory.delete();
        }
    }

    public static String getFileName(File file) {
        String fileName = file.getAbsolutePath();
        return fileName.substring(fileName.lastIndexOf(File.separator) + File.separator.length());
    }

    public static void createDirectoriesForFile(File file) {
        synchronized (EXMUT) {
            if (file == null) {
                throw new IllegalArgumentException("File for path creation is null");
            }

            File directory = file.getAbsoluteFile().getParentFile();

            if (directory == null) {
                throw new IllegalArgumentException("Directory for creation is null");
            }

            if (!directory.exists() || !directory.isDirectory()) {
                createDirectoryPath(directory);
            }
        }
    }

    public static void createDirectoryPath(File directory) {
        synchronized (EXMUT) {
            if (directory == null) {
                throw new IllegalStateException("Directory for creation is null.");
            }

            if (!directory.exists()) {
                createDirectoryPathIfNotExists(directory);
            }
        }
    }

    /**
     * Creates all the not existing parents of the specified file.
     *
     * @param file
     * @return True if one or more directories have been created.
     */
    public static boolean createDirectoriesForFileIfNotExist(File file) {
        synchronized (EXMUT) {
            if (file == null) {
                throw new IllegalArgumentException("File for path creation is null");
            }

            File directory = file.getParentFile();

            if (directory == null) {
                throw new IllegalArgumentException("Directory for creation is null");
            }

            return createDirectoryPathIfNotExists(directory);
        }
    }

    public static boolean createDirectoryPathIfNotExists(File directory) {
        synchronized (EXMUT) {
            if (directory == null) {
                throw new IllegalStateException("Directory for creation is null.");
            }

            if (!directory.exists()) {
                boolean mkdirs = directory.mkdirs();
                if (!mkdirs) {
                    Global.showWarning("Could not create directory '" + directory.getAbsolutePath() + "'");
                    Global.showWarning("Check for permissions.");

                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(FileUtilities.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (directory.exists()) {
                        return true;
                    }

                    FileNotFoundException ex = new FileNotFoundException("Could not create directory '" + directory.getAbsolutePath() + "'");
                    ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
                }
                return true;
            } else {
                return false;
            }
        }
    }

    public static void cleanDirectory(File directory) {
        synchronized (EXMUT) {
            if (directory.exists()) {
                FileUtilities.deleteDirectoryRecursive(directory);
            }
            directory.mkdirs();
        }
    }

    /**
     * Returns all files and directories under the specified one.
     *
     * @param directory
     * @return
     */
    public static List<File> findInDirectory(File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("The argument must be a directory! '" + directory + "'");
        }

        List<File> childs = new LinkedList<>();
        for (File child : directory.listFiles()) {
            childs.add(child);

            if (child.isDirectory()) {
                childs.addAll(findInDirectory(child));
            }
        }

        return childs;
    }
}
