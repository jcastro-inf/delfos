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
package delfos.io.xml.persistencemethod;

import java.io.File;
import org.jdom2.Element;
import delfos.common.Global;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.persistence.PersistenceMethod;

/**
 * Clase para efectuar la entrada/salida de los distintos métodos de
 * persistencia
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (06/12/2012)
 */
public class PersistenceMethodXML {

    /**
     * Nombre del elemento XML que almacena la información del método de
     * persistencia
     */
    public static final String PERSISTENCE_METHOD_ELEMENT = "PersistenceMethod";
    /**
     * Nombre de la característica que define el tipo de persistencia que se
     * almacena en el elemento XML
     */
    public static final String PERSISTENCE_METHOD_TYPE_ATTRIBUTE = "persistenceType";
    public static final String FILE_PERSISTENCE_DIRECTORY = "directory";
    public static final String FILE_PERSISTENCE_PREFIX = "prefix";
    public static final String FILE_PERSISTENCE_SUFIX = "sufix";
    public static final String FILE_PERSISTENCE_FILE_NAME = "fileName";
    public static final String FILE_PERSISTENCE_FILE_TYPE = "fileType";
    public static final String DATABASE_PERSISTENCE_SERVER_NAME = "serverName";
    public static final String DATABASE_PERSISTENCE_PORT = "port";
    public static final String DATABASE_PERSISTENCE_DATABASE_NAME = "databaseName";
    public static final String DATABASE_PERSISTENCE_PREFIX = "prefix";
    public static final String DATABASE_PERSISTENCE_USER = "user";
    public static final String DATABASE_PERSISTENCE_PASSWORD = "pass";

    /**
     * Construye el elemento XML que define el método de persistencia pasado por
     * parámetro
     *
     * @param persistenceMethod Método de persistencia a almacenar
     * @return Elemento XML que describe el método de persistencia
     */
    public static Element getElement(PersistenceMethod persistenceMethod) {

        if (persistenceMethod instanceof DatabasePersistence) {
            return getDatabasePersistenceElement((DatabasePersistence) persistenceMethod);
        }

        if (persistenceMethod instanceof FilePersistence) {
            return getFilePersistenceElement((FilePersistence) persistenceMethod);
        }

        throw new UnsupportedOperationException("Unknow PersistenceMethod: '" + persistenceMethod.getClass().getSimpleName() + "'");
    }

    /**
     * Construye el elemento XML que define la persistencia en base de datos
     * indicada por parámetro
     *
     * @param databasePersistence Persistencia en base de datos a almacenar
     * @return Elemento XML que describe la persistencia en base de datos
     */
    public static Element getDatabasePersistenceElement(DatabasePersistence databasePersistence) {
        Element ret = new Element(PERSISTENCE_METHOD_ELEMENT);
        ret.setAttribute(PERSISTENCE_METHOD_TYPE_ATTRIBUTE, databasePersistence.getClass().getSimpleName());

        ret.setAttribute(DATABASE_PERSISTENCE_SERVER_NAME, databasePersistence.getServer());
        ret.setAttribute(DATABASE_PERSISTENCE_PORT, Integer.toString(databasePersistence.getPort()));
        ret.setAttribute(DATABASE_PERSISTENCE_DATABASE_NAME, databasePersistence.getDatabase());
        ret.setAttribute(DATABASE_PERSISTENCE_PREFIX, databasePersistence.getPrefix());
        ret.setAttribute(DATABASE_PERSISTENCE_USER, databasePersistence.getUser());
        ret.setAttribute(DATABASE_PERSISTENCE_PASSWORD, databasePersistence.getPass());
        return ret;
    }

    /**
     * Construye el elemento XML que define la persistencia en fichero pasada
     * por parámetro
     *
     * @param filePersistence Persistencia en fichero a almacenar
     * @return Elemento XML que describe la persistencia en fichero
     */
    private static Element getFilePersistenceElement(FilePersistence filePersistence) {
        Element ret = new Element(PERSISTENCE_METHOD_ELEMENT);
        ret.setAttribute(PERSISTENCE_METHOD_TYPE_ATTRIBUTE, filePersistence.getClass().getSimpleName());

        ret.setAttribute(FILE_PERSISTENCE_PREFIX, filePersistence.getPrefix());
        ret.setAttribute(FILE_PERSISTENCE_SUFIX, filePersistence.getSuffix());
        ret.setAttribute(FILE_PERSISTENCE_FILE_NAME, filePersistence.getFileName());
        ret.setAttribute(FILE_PERSISTENCE_FILE_TYPE, filePersistence.getExtension());
        ret.setAttribute(FILE_PERSISTENCE_DIRECTORY, filePersistence.getDirectory().getPath());

        return ret;
    }

    /**
     * Devuelve el método de persistencia que hay descrito en el elemento XML
     * que se pasa por parámetro
     *
     * @param persistenceElement Elemento XML con la información para recuperar
     * el método de persistencia
     * @return Método de persistencia que se encuentra descrito en el elemento
     */
    public static PersistenceMethod getPersistenceMethod(Element persistenceElement) {

        String persistenceType = persistenceElement.getAttributeValue(PERSISTENCE_METHOD_TYPE_ATTRIBUTE);

        if (DatabasePersistence.class.getSimpleName().equals(persistenceType)) {
            return getDatabasePersistence(persistenceElement);
        }

        if (FilePersistence.class.getSimpleName().equals(persistenceType)) {
            return getFilePersistence(persistenceElement);
        }

        throw new IllegalArgumentException("Unknow persistence method '" + persistenceType + "'");
    }

    /**
     * Devuelve la persistencia en base de datos que hay descrita en el elemento
     * XML que se pasa por parámetro
     *
     * @param databasePersistenceElement Elemento XML con la información para
     * recuperar la persistencia en base de datos.
     * @return Persistencia en base de datosque se encuentra descrito en el
     * elemento
     */
    public static DatabasePersistence getDatabasePersistence(Element databasePersistenceElement) {
        String persistenceType = databasePersistenceElement.getAttributeValue(PERSISTENCE_METHOD_TYPE_ATTRIBUTE);
        if (!persistenceType.equals(DatabasePersistence.class.getSimpleName())) {
            throw new IllegalArgumentException("Wrong element, not a '" + DatabasePersistence.class.getSimpleName() + "'");
        }

        String serverName = databasePersistenceElement.getAttribute(DATABASE_PERSISTENCE_SERVER_NAME).getValue();
        int port = Integer.parseInt(databasePersistenceElement.getAttribute(DATABASE_PERSISTENCE_PORT).getValue());
        String databaseName = databasePersistenceElement.getAttribute(DATABASE_PERSISTENCE_DATABASE_NAME).getValue();
        String prefix = databasePersistenceElement.getAttribute(DATABASE_PERSISTENCE_PREFIX).getValue();
        String user = databasePersistenceElement.getAttribute(DATABASE_PERSISTENCE_USER).getValue();
        String pass = databasePersistenceElement.getAttribute(DATABASE_PERSISTENCE_PASSWORD).getValue();

        return new DatabasePersistence(user, pass, databaseName, serverName, port, prefix);
    }

    /**
     * Devuelve la persistencia de fichero que hay descrita en el elemento XML
     * que se pasa por parámetro
     *
     * @param filePersistenceElement Elemento XML con la información para
     * recuperar la persistencia de fichero
     * @return Persistencia de fichero que se encuentra descrito en el elemento
     */
    public static FilePersistence getFilePersistence(Element filePersistenceElement) {
        String prefix = filePersistenceElement.getAttribute(FILE_PERSISTENCE_PREFIX).getValue().toString();
        String sufix = filePersistenceElement.getAttribute(FILE_PERSISTENCE_SUFIX).getValue().toString();
        String fileName = filePersistenceElement.getAttribute(FILE_PERSISTENCE_FILE_NAME).getValue().toString();
        String fileType = filePersistenceElement.getAttribute(FILE_PERSISTENCE_FILE_TYPE).getValue().toString();
        String directory = filePersistenceElement.getAttribute(FILE_PERSISTENCE_DIRECTORY).getValue().toString();

        if (directory.equals("")) {
            directory = "." + File.separator;
            Global.showWarning("The directory was empty, set to current work directory.");
        }

        return new FilePersistence(fileName, fileType, prefix, sufix, new File(directory));

    }
}
