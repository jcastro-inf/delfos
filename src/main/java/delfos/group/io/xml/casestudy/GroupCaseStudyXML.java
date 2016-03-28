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
package delfos.group.io.xml.casestudy;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.parameters.ParameterOwner;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.SeedHolder;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.io.xml.groupformationtechnique.GroupFormationTechniqueXML;
import delfos.group.io.xml.grs.GroupRecommenderSystemXML;
import delfos.group.io.xml.predictionprotocol.GroupPredictionProtocolXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.io.xml.casestudy.CaseStudyXML;
import static delfos.io.xml.casestudy.CaseStudyXML.CASE_ROOT_ELEMENT_NAME;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.io.xml.dataset.RelevanceCriteriaXML;
import delfos.io.xml.validationtechnique.ValidationTechniqueXML;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución
 * de un caso de uso concreto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (3-Mayo-2013)
 */
public class GroupCaseStudyXML {

    public static String RESULT_EXTENSION = "xml";
    public static final String HASH_ATTRIBUTE_NAME = "hash";
    public static final String HASH_DATA_VALIDATION_ATTRIBUTE_NAME = "hash_DataValidation";
    public static final String HASH_TECHNIQUE_ATTRIBUTE_NAME = "hash_Technique";
    public static final String NUM_EXEC_ATTRIBUTE_NAME = "numExec";
    public static final String FULL_RESULT_SUFFIX = "_FULL";
    public static final String AGGR_RESULT_SUFFIX = "_AGGR";

    public static final String AGGREGATE_VALUES_ELEMENT_NAME = CaseStudyXML.AGGREGATE_VALUES_ELEMENT_NAME;

    private GroupCaseStudyXML() {
    }

    private static Element getResultsElement(GroupCaseStudy c) {

        Element ejecuciones = new Element("Executions");
        Element ejecucion;
        int numExecutions = c.getNumExecutions();
        int numSplits = c.getValidationTechnique().getNumberOfSplits();
        for (int nexecution = 0; nexecution < numExecutions; nexecution++) {
            ejecucion = new Element("Execution");
            ejecucion.setAttribute("execution", Integer.toString(numSplits));
            for (int nSplit = 0; nSplit < numSplits; nSplit++) {
                Element split = new Element("Split");
                split.setAttribute("split", Integer.toString(nSplit));
                for (GroupEvaluationMeasure em : c.getEvaluationMeasures()) {
                    GroupEvaluationMeasureResult mr = c.getMeasureResult(em, nexecution, nSplit);
                    split.addContent((Element) mr.getXMLElement().clone());
                }
                ejecucion.addContent(split);
            }
            ejecuciones.addContent(ejecucion);
        }
        return ejecuciones;
    }

    private static Element getAggregatedResultsElement(GroupCaseStudy c) {
        Element mediaMedidas = new Element("Aggregate_values");
        for (GroupEvaluationMeasure em : c.getEvaluationMeasures()) {
//            MeasureResult aggregateResults = em.agregateResults(allResults.get(em));
            Element element = c.getAggregateMeasureResult(em).getXMLElement();
            mediaMedidas.addContent(element);
        }

        return mediaMedidas;
    }

    public synchronized static void caseStudyToXMLFile_fullResults(GroupCaseStudy caseStudyGroup, File file) {
        if (!caseStudyGroup.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        Document doc = new Document();
        Element casoDeUso = new Element("Case");

        casoDeUso.setAttribute(SeedHolder.SEED.getName(), Long.toString(caseStudyGroup.getSeedValue()));
        casoDeUso.setAttribute(NUM_EXEC_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.getNumExecutions()));
        casoDeUso.setAttribute(ParameterOwner.ALIAS.getName(), caseStudyGroup.getAlias());

        casoDeUso.addContent(GroupRecommenderSystemXML.getElement(caseStudyGroup.getGroupRecommenderSystem()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudyGroup.getDatasetLoader()));

        casoDeUso.addContent(GroupFormationTechniqueXML.getElement(caseStudyGroup.getGroupFormationTechnique()));
        casoDeUso.addContent(ValidationTechniqueXML.getElement(caseStudyGroup.getValidationTechnique()));
        casoDeUso.addContent(GroupPredictionProtocolXML.getElement(caseStudyGroup.getGroupPredictionProtocol()));

        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudyGroup.getRelevanceCriteria()));

        casoDeUso.addContent(getResultsElement(caseStudyGroup));
        casoDeUso.addContent(getAggregatedResultsElement(caseStudyGroup));
        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        FileUtilities.createDirectoriesForFile(file);
        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    public static String getCaseStudyFileNameTimestamped(GroupCaseStudy caseStudyGroup) {
        Date date = new Date();
        String dateBasedName = "aux";
        try {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH.mm.ss", new Locale("es", "ES"));
            dateBasedName = sdf.format(date);
        } catch (Exception ex) {
            Global.showError(ex);
            Global.showWarning("Cannot get timestamp" + ex.getMessage() + "\n");
        }

        dateBasedName = dateBasedName + "_" + GroupCaseStudy.SEED.getName() + "=" + caseStudyGroup.getSeedValue() + "." + CaseStudyXML.RESULT_EXTENSION;

        File f = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + dateBasedName);
        return f.getAbsolutePath();
    }

    public static String getCaseStudyFileName(GroupCaseStudy caseStudyGroup) {
        return caseStudyGroup.getAlias()
                + "_" + SeedHolder.SEED.getName() + "=" + caseStudyGroup.getSeedValue()
                + "_" + GroupCaseStudy.NUM_EXECUTIONS.getName() + "=" + caseStudyGroup.getNumExecutions();
    }

    /**
     * Saves the xml with the description of the case study in de file
     * specified.
     *
     * @param caseStudyGroup Group case study whose description is saved.
     * @param directory File in which the description is saved.
     */
    public static void saveCaseDescription(GroupCaseStudy caseStudyGroup, File directory) {
        File file = new File(directory.getPath() + File.separator + caseStudyGroup.getAlias() + ".xml");
        GroupCaseStudyXML.caseStudyToXMLFile_onlyDescription(caseStudyGroup, file);
    }

    public static void saveCaseResults(GroupCaseStudy caseStudyGroup, File directory) {
        if (!directory.isDirectory()) {
            throw new IllegalStateException("GroupCaseStudy save to XML: Not a directory (" + directory.toString() + ")");
        }
        FileUtilities.createDirectoryPathIfNotExists(directory);

        String fileName = getCaseStudyFileName(caseStudyGroup);

        if (Constants.isPrintFullXML()) {
            File caseStudyFullXML = new File(directory.getPath() + File.separator + fileName + FULL_RESULT_SUFFIX + ".xml");
            GroupCaseStudyXML.caseStudyToXMLFile_fullResults(caseStudyGroup, caseStudyFullXML);
        }

        File caseStudyAggrXML = new File(directory.getPath() + File.separator + fileName + AGGR_RESULT_SUFFIX + ".xml");
        GroupCaseStudyXML.caseStudyToXMLFile_aggregateResults(caseStudyGroup, caseStudyAggrXML);
    }

    /**
     * Saves the xml with the description of the case study in de file
     * specified.
     *
     * @param caseStudyGroup Group case study whose description is saved.
     * @param file File in which the description is saved.
     */
    public static void caseStudyToXMLFile_onlyDescription(GroupCaseStudy caseStudyGroup, File file) {

        if (caseStudyGroup.isFinished()) {
            throw new IllegalArgumentException("Ya se ha ejecutado el caso de estudio!");
        }

        Document doc = new Document();
        Element casoDeUso = new Element(CASE_ROOT_ELEMENT_NAME);

        casoDeUso.setAttribute(SeedHolder.SEED.getName(), Long.toString(caseStudyGroup.getSeedValue()));
        casoDeUso.setAttribute(NUM_EXEC_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.getNumExecutions()));
        casoDeUso.setAttribute(ParameterOwner.ALIAS.getName(), caseStudyGroup.getAlias());

        casoDeUso.setAttribute(HASH_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.hashCode()));
        casoDeUso.setAttribute(HASH_DATA_VALIDATION_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.hashCode()));

        casoDeUso.addContent(GroupRecommenderSystemXML.getElement(caseStudyGroup.getGroupRecommenderSystem()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudyGroup.getDatasetLoader()));

        casoDeUso.addContent(GroupFormationTechniqueXML.getElement(caseStudyGroup.getGroupFormationTechnique()));
        casoDeUso.addContent(ValidationTechniqueXML.getElement(caseStudyGroup.getValidationTechnique()));
        casoDeUso.addContent(GroupPredictionProtocolXML.getElement(caseStudyGroup.getGroupPredictionProtocol()));
        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudyGroup.getRelevanceCriteria()));
        doc.addContent(casoDeUso);

        FileUtilities.createDirectoriesForFile(file);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
            fileWriter.close();
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    private static void caseStudyToXMLFile_aggregateResults(GroupCaseStudy caseStudyGroup, File file) {
        if (!caseStudyGroup.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        Document doc = new Document();
        Element casoDeUso = new Element(CASE_ROOT_ELEMENT_NAME);

        casoDeUso.setAttribute(SeedHolder.SEED.getName(), Long.toString(caseStudyGroup.getSeedValue()));
        casoDeUso.setAttribute(NUM_EXEC_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.getNumExecutions()));
        casoDeUso.setAttribute(ParameterOwner.ALIAS.getName(), caseStudyGroup.getAlias());

        casoDeUso.setAttribute(HASH_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.hashCode()));
        casoDeUso.setAttribute(HASH_DATA_VALIDATION_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.hashDataValidation()));
        casoDeUso.setAttribute(HASH_TECHNIQUE_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.hashTechnique()));

        casoDeUso.addContent(GroupRecommenderSystemXML.getElement(caseStudyGroup.getGroupRecommenderSystem()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudyGroup.getDatasetLoader()));

        casoDeUso.addContent(GroupFormationTechniqueXML.getElement(caseStudyGroup.getGroupFormationTechnique()));
        casoDeUso.addContent(ValidationTechniqueXML.getElement(caseStudyGroup.getValidationTechnique()));
        casoDeUso.addContent(GroupPredictionProtocolXML.getElement(caseStudyGroup.getGroupPredictionProtocol()));

        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudyGroup.getRelevanceCriteria()));

        casoDeUso.addContent(getAggregatedResultsElement(caseStudyGroup));
        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        FileUtilities.createDirectoriesForFile(file);

        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    /**
     * Carga la descripción de un caso de estudio para sistemas de recomendación
     * para grupos.
     *
     * @param file Archivo donde se encuentra almacenado el caso de estudio.
     * @return Caso de estudio recuperado del archivo.
     * @throws org.jdom2.JDOMException Cuando se intenta cargar un xml que no
     * tiene la estructura esperada. Chequear si esta desfasada la versión.
     * @throws IOException Cuando no se puede leer el archivo indicado o existe
     * algun fallo de conversión de datos al leer el contenido del mismo.
     */
    public static GroupCaseStudyConfiguration loadGroupCaseDescription(File file) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();

        Document doc = builder.build(file);
        Element caseStudy = doc.getRootElement();
        if (!caseStudy.getName().equals(CASE_ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study.");
        }
        GroupRecommenderSystem<Object, Object> groupRecommenderSystem = GroupRecommenderSystemXML.getGroupRecommenderSystem(caseStudy.getChild(GroupRecommenderSystemXML.ELEMENT_NAME));

        GroupFormationTechnique groupFormationTechnique = GroupFormationTechniqueXML.getGroupFormationTechnique(caseStudy.getChild(GroupFormationTechniqueXML.ELEMENT_NAME));
        ValidationTechnique validationTechnique = ValidationTechniqueXML.getValidationTechnique(caseStudy.getChild(ValidationTechniqueXML.ELEMENT_NAME));
        GroupPredictionProtocol groupPredictionProtocol = GroupPredictionProtocolXML.getGroupPredictionProtocol(caseStudy.getChild(GroupPredictionProtocolXML.ELEMENT_NAME));
        RelevanceCriteria relevanceCriteria = RelevanceCriteriaXML.getRelevanceCriteria(caseStudy.getChild(RelevanceCriteriaXML.ELEMENT_NAME));

        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(caseStudy.getChild(DatasetLoaderXML.ELEMENT_NAME));

        long seed = Long.parseLong(caseStudy.getAttributeValue(SeedHolder.SEED.getName()));
        int numExecutions = Integer.parseInt(caseStudy.getAttributeValue(NUM_EXEC_ATTRIBUTE_NAME));
        String caseStudyAlias = caseStudy.getAttributeValue(ParameterOwner.ALIAS.getName());

        return new GroupCaseStudyConfiguration(
                groupRecommenderSystem, datasetLoader,
                groupFormationTechnique,
                validationTechnique,
                groupPredictionProtocol,
                relevanceCriteria,
                caseStudyAlias,
                numExecutions,
                seed,
                null);
    }

    /**
     * Carga la descripción de un caso de estudio para sistemas de recomendación
     * para grupos.
     *
     * @param file Archivo donde se encuentra almacenado el caso de estudio.
     * @return Caso de estudio recuperado del archivo.
     * @throws org.jdom2.JDOMException Cuando se intenta cargar un xml que no
     * tiene la estructura esperada. Chequear si esta desfasada la versión.
     * @throws IOException Cuando no se puede leer el archivo indicado o existe
     * algun fallo de conversión de datos al leer el contenido del mismo.
     */
    public static GroupCaseStudyConfiguration loadGroupCaseWithResults(File file) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();

        Document doc = builder.build(file);
        Element caseStudy = doc.getRootElement();
        if (!caseStudy.getName().equals(CASE_ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study.");
        }
        GroupRecommenderSystem<Object, Object> groupRecommenderSystem = GroupRecommenderSystemXML.getGroupRecommenderSystem(caseStudy.getChild(GroupRecommenderSystemXML.ELEMENT_NAME));

        GroupFormationTechnique groupFormationTechnique = GroupFormationTechniqueXML.getGroupFormationTechnique(caseStudy.getChild(GroupFormationTechniqueXML.ELEMENT_NAME));
        ValidationTechnique validationTechnique = ValidationTechniqueXML.getValidationTechnique(caseStudy.getChild(ValidationTechniqueXML.ELEMENT_NAME));
        GroupPredictionProtocol groupPredictionProtocol = GroupPredictionProtocolXML.getGroupPredictionProtocol(caseStudy.getChild(GroupPredictionProtocolXML.ELEMENT_NAME));
        RelevanceCriteria relevanceCriteria = RelevanceCriteriaXML.getRelevanceCriteria(caseStudy.getChild(RelevanceCriteriaXML.ELEMENT_NAME));

        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(caseStudy.getChild(DatasetLoaderXML.ELEMENT_NAME));

        Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults = getEvaluationMeasures(caseStudy);

        long seed = Long.parseLong(caseStudy.getAttributeValue(SeedHolder.SEED.getName()));
        int numExecutions = Integer.parseInt(caseStudy.getAttributeValue(NUM_EXEC_ATTRIBUTE_NAME));
        String caseStudyAlias = caseStudy.getAttributeValue(ParameterOwner.ALIAS.getName());

        return new GroupCaseStudyConfiguration(
                groupRecommenderSystem, datasetLoader,
                groupFormationTechnique,
                validationTechnique,
                groupPredictionProtocol,
                relevanceCriteria,
                caseStudyAlias,
                numExecutions,
                seed,
                groupEvaluationMeasuresResults);
    }

    public static int extractResultNumExec(File groupCaseStudyXML) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();

        Document doc = builder.build(groupCaseStudyXML);
        Element caseStudy = doc.getRootElement();
        if (!caseStudy.getName().equals(CASE_ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study.");
        }

        Integer numExec = new Integer(caseStudy.getAttributeValue(NUM_EXEC_ATTRIBUTE_NAME));

        return numExec;
    }

    private static Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> getEvaluationMeasures(Element caseStudy) {

        Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasures = new TreeMap<>();

        Element aggregateValues = caseStudy.getChild(AGGREGATE_VALUES_ELEMENT_NAME);

        if (aggregateValues == null) {
            throw new IllegalStateException("Unable to load a case study description only, the XML must have results details.");
        }

        for (Element groupEvaluationMeasureResultElement : aggregateValues.getChildren()) {
            String name = groupEvaluationMeasureResultElement.getName();

            GroupEvaluationMeasure groupEvaluationMeasure = GroupEvaluationMeasuresFactory.getInstance().getClassByName(name);
            if (groupEvaluationMeasure == null) {
                throw new IllegalStateException("The group evaluation measure '" + name + "' does not exists in delfos' factory");
            } else {
                groupEvaluationMeasures.put(groupEvaluationMeasure, groupEvaluationMeasure.getGroupEvaluationMeasureResultFromXML(groupEvaluationMeasureResultElement));
            }
        }

        return groupEvaluationMeasures;
    }

}
