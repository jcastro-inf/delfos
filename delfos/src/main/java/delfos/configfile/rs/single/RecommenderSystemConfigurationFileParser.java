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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;
import delfos.ERROR_CODES;
import delfos.Constants;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.io.xml.dataset.RelevanceCriteriaXML;
import delfos.io.xml.persistencemethod.PersistenceMethodXML;
import delfos.io.xml.recommendationcandidatesselector.RecommendationCandidatesSelectorXML;
import delfos.io.xml.recommendations.RecommdendationsOutputMethodXML;
import delfos.io.xml.rs.RecommenderSystemXML;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.GenericRecommenderSystem;
import delfos.rs.output.RecommendationsOutputMethod;
import delfos.rs.output.RecommendationsOutputStandardXML;
import delfos.rs.persistence.PersistenceMethod;

/**
 * Realiza la lectura/escritura del archivo de configuración que describe un
 * sistema de recomendación completo, indicando el algoritmo y dataset.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 28-Febrero-2013
 * @version 1.2 28-Octubre-2013 Añadidos los {@link RecommendationsOutputMethod}
 * al archivo de configuración.
 */
public class RecommenderSystemConfigurationFileParser {

    /**
     * Extensión que tienen los archivos de configuración.
     */
    public static final String CONFIGURATION_EXTENSION = "xml";

    private RecommenderSystemConfigurationFileParser() {
    }

    /**
     * Almacena la configuración completa del sistema en el fichero indicado.
     *
     * @param fileName
     * @param recommenderSystem Sistema de recomendación que utiliza.
     * @param datasetLoader Objeto para recuperar los datos de entrada.
     * @param persistenceMethod
     * @param recommendationCandidatesSelector
     * @param recommdendationsOutputMethod
     */
    public static void saveConfigFile(String fileName, GenericRecommenderSystem<? extends Object> recommenderSystem, DatasetLoader<? extends Rating> datasetLoader, PersistenceMethod persistenceMethod, RecommendationCandidatesSelector recommendationCandidatesSelector, RecommendationsOutputMethod recommdendationsOutputMethod) {

        saveConfigFile(
                fileName,
                recommenderSystem,
                datasetLoader,
                datasetLoader.getDefaultRelevanceCriteria(),
                persistenceMethod,
                recommendationCandidatesSelector,
                recommdendationsOutputMethod);
    }

    public static void saveConfigFile(
            String fileName,
            RecommenderSystemConfiguration recommenderSystemConfiguration) {

        saveConfigFile(
                fileName,
                recommenderSystemConfiguration.recommenderSystem,
                recommenderSystemConfiguration.datasetLoader,
                recommenderSystemConfiguration.relevanceCriteria,
                recommenderSystemConfiguration.persistenceMethod,
                recommenderSystemConfiguration.recommendationCandidatesSelector,
                recommenderSystemConfiguration.recommdendationsOutputMethod);
    }

    /**
     * Almacena la configuración completa del sistema en el fichero indicado.
     *
     * @param fileName Fichero en el que se almacena la configuración.
     * @param recommenderSystem Sistema de recomendación que utiliza.
     * @param datasetLoader Objeto para recuperar los datos de entrada.
     * @param relevanceCriteria Criterio de relevancia utilizado.
     * @param persistenceMethod
     * @param recommendationCandidatesSelector
     * @param recommdendationsOutputMethod
     */
    public static void saveConfigFile(
            String fileName, GenericRecommenderSystem recommenderSystem, DatasetLoader<? extends Rating> datasetLoader, RelevanceCriteria relevanceCriteria, PersistenceMethod persistenceMethod, RecommendationCandidatesSelector recommendationCandidatesSelector, RecommendationsOutputMethod recommdendationsOutputMethod) {

        Document doc = new Document();
        Element root = new Element("config");

        //Creo el objeto Jdom del sistema de recomendación
        root.addContent(RecommenderSystemXML.getElement(recommenderSystem));

        //Creo el objeto Jdom del datasetLoader
        root.addContent(DatasetLoaderXML.getElement(datasetLoader));

        //Umbral de relevancia
        root.addContent(RelevanceCriteriaXML.getElement(relevanceCriteria));

        //Persistence Method
        root.addContent(PersistenceMethodXML.getElement(persistenceMethod));

        root.addContent(RecommendationCandidatesSelectorXML.getElement(recommendationCandidatesSelector));

        root.addContent(RecommdendationsOutputMethodXML.getElement(recommdendationsOutputMethod));

        doc.addContent(root);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        try {
            if (!fileName.endsWith("." + CONFIGURATION_EXTENSION)) {
                fileName += "." + CONFIGURATION_EXTENSION;
            }
            try (FileWriter fileWriter = new FileWriter(fileName)) {
                outputter.output(doc, fileWriter);
            }
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }
    }

    /**
     * Método para recuperar los objetos que se deben usar según los parámetros
     * que dicta el fichero de configuración indicado. Como se devuelven
     * múltiples valores y java no permite la devolución de múltiples valores en
     * una función, se ha creado un objeto para almacenarlos.
     *
     * @param configFilePath Ruta del fichero de configuración.
     * @return Devuelve un objeto que contiene los parámetros necesarios para
     * definir completamente un sistema de recomendación.
     */
    public static RecommenderSystemConfiguration loadConfigFile(String configFilePath) {
        try {
            RecommenderSystemConfiguration rsc = loadConfigFileWithExceptions(configFilePath);
            if (rsc == null) {
                IllegalStateException ex = new IllegalStateException("The recommenderSystemConfiguration cannot be loaded.");
                ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
                throw ex;
            } else {
                return rsc;
            }
        } catch (JDOMException ex) {
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (FileNotFoundException ex) {
            ERROR_CODES.CONFIG_FILE_NOT_EXISTS.exit(ex);
        }
        throw new IllegalStateException("Error found, cannot progress.");
    }

    private static RecommenderSystemConfiguration loadConfigFileWithExceptions(String configFilePath) throws JDOMException, FileNotFoundException {

        Global.showMessageTimestamped("Loading config file " + configFilePath);
        SAXBuilder builder = new SAXBuilder();
        Document doc = null;

        File configFile = new File(configFilePath);
        try {
            doc = builder.build(configFile);
        } catch (IOException ex) {
            Global.showError(ex);
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
            throw new IllegalStateException(ex);
        }

        Element config = doc.getRootElement();

        Element rs = config.getChild(RecommenderSystemXML.ELEMENT_NAME);
        GenericRecommenderSystem<Object> recommender = RecommenderSystemXML.getRecommenderSystem(rs);

        Element datasetLoaderElement = config.getChild(DatasetLoaderXML.ELEMENT_NAME);
        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(datasetLoaderElement);

        Element relevanceCriteriaElement = config.getChild(RelevanceCriteriaXML.ELEMENT_NAME);
        RelevanceCriteria relevanceCriteria = RelevanceCriteriaXML.getRelevanceCriteria(relevanceCriteriaElement);

        //Persistence Method
        Element persistenceMethodElement = config.getChild(PersistenceMethodXML.PERSISTENCE_METHOD_ELEMENT);
        PersistenceMethod persistenceMethod = PersistenceMethodXML.getPersistenceMethod(persistenceMethodElement);

        //Obtiene el método para devolver las recomendaciones.
        RecommendationsOutputMethod recommdendationsOutputMethod;
        {
            Element recommdendationsOutputMethodElement = config.getChild(RecommdendationsOutputMethodXML.RECOMMENDATIONS_OUTPUT_METHOD_ELEMENT_NAME);
            if (recommdendationsOutputMethodElement == null) {
                Global.showWarning("This configuration file is old, update to the new version which includes recommendations output management.");
                Global.showWarning("Using default RecommendationsOutputMethod --> " + RecommendationsOutputStandardXML.class.getName());
                recommdendationsOutputMethod = new RecommendationsOutputStandardXML();
            } else {
                recommdendationsOutputMethod = RecommdendationsOutputMethodXML.getRecommdendationsOutputMethod(recommdendationsOutputMethodElement);
            }
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Recommendation output method loaded: " + recommdendationsOutputMethod.getNameWithParameters() + "\n");
            }
        }

        //Obtiene el método para calcular los items candidatos a recomendación.
        RecommendationCandidatesSelector recommendationCandidatesSelector;
        {
            Element recommendationCandidatesSelectorElement = config.getChild(RecommendationCandidatesSelectorXML.RECOMMENDATION_CANDIDATE_SELECTOR_ELEMENT_NAME);
            if (recommendationCandidatesSelectorElement == null) {
                Global.showWarning("This configuration file is old, update to the new version which includes recommendation candidates selector.");
                Global.showWarning("Using default RecommendationCandidatesSelector --> " + RecommendationCandidatesSelector.defaultValue.getClass().getName());
                recommendationCandidatesSelector = RecommendationCandidatesSelector.defaultValue;
            } else {
                recommendationCandidatesSelector = RecommendationCandidatesSelectorXML.getRecommendationsCandidatesSelector(recommendationCandidatesSelectorElement);
            }
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Recommendation output method loaded: " + recommdendationsOutputMethod.getNameWithParameters() + "\n");
            }
        }

        if (config.getChild("NUMBER_OF_RECOMMENDATIONS") != null) {
            Global.showWarning("Deprecated RecommenderSystem Configuration element: _Number of recommendations. Use RecommendationOutputMethod parameter NUMBER_OF_RECOMMENDATIONS instead.");
        }

        RecommenderSystemConfiguration ret = new RecommenderSystemConfiguration(recommender, datasetLoader, persistenceMethod, recommendationCandidatesSelector, recommdendationsOutputMethod, relevanceCriteria);
        Global.showMessageTimestamped("Loaded config file " + configFilePath);

        return ret;

    }
}
