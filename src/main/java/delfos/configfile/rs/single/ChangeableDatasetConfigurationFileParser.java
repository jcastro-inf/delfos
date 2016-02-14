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
package delfos.configfile.rs.single;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.changeable.ChangeableDatasetLoader;
import delfos.io.xml.dataset.DatasetLoaderXML;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * Realiza la lectura/escritura del archivo de configuración que describe un
 * dataset modificable
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 17-Septiembre-2013
 */
public class ChangeableDatasetConfigurationFileParser {

    /**
     * Extensión que tienen los archivos de configuración.
     */
    public static final String CONFIGURATION_EXTENSION = "xml";

    private ChangeableDatasetConfigurationFileParser() {
    }

    /**
     * Almacena la configuración completa del dataset en el fichero indicado.
     *
     * @param configFile Nombre del fichero en que se almacena la configuración.
     * @param datasetLoader Objeto para recuperar los datos de entrada.
     */
    public static void saveConfigFile(File configFile, ChangeableDatasetLoader datasetLoader) throws IOException {

        Document doc = new Document();
        Element root = new Element("config");

        //Creo el objeto Jdom del datasetLoader
        root.addContent(DatasetLoaderXML.getElement(datasetLoader));

        doc.addContent(root);
        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        if (!configFile.getAbsolutePath().endsWith("." + CONFIGURATION_EXTENSION)) {
            configFile = new File(configFile.getAbsolutePath() + "." + CONFIGURATION_EXTENSION);
        }

        try (FileWriter fileWriter = new FileWriter(configFile)) {
            outputter.output(doc, fileWriter);
        }
    }

    /**
     * Método para recuperar los objetos que se deben usar según los parámetros
     * que dicta el fichero de configuración indicado. Como se devuelven
     * múltiples valores y java no permite la devolución de múltiples valores en
     * una función, se ha creado un objeto para almacenarlos.
     *
     * @param configFile Ruta del fichero de configuración.
     * @return Devuelve un objeto que contiene los parámetros necesarios para
     * definir completamente un sistema de recomendación.
     * @throws JDOMException Si el archivo de configuración no tiene la
     * estructura de objetos JDOM esperada, es decir, no se reconoce el formato
     * del archivo.
     * @throws CannotLoadContentDataset Si no se puede cargar el dataset de
     * contenido.
     * @throws CannotLoadRatingsDataset Si no se puede cargar el dataset de
     * valoraciones.
     * @throws FileNotFoundException Si el archivo indicado no existe.
     */
    public static ChangeableDatasetConfiguration loadConfigFile(File configFile) throws JDOMException, CannotLoadContentDataset, CannotLoadRatingsDataset, FileNotFoundException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;
        try {
            doc = builder.build(configFile);
        } catch (IOException ex) {
            Global.showError(ex);
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
            throw new IllegalStateException(ex);
        }

        Element config = doc.getRootElement();
        Element datasetLoaderElement = config.getChild(DatasetLoaderXML.ELEMENT_NAME);
        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(datasetLoaderElement);

        return new ChangeableDatasetConfiguration((ChangeableDatasetLoader) datasetLoader);
    }
}
