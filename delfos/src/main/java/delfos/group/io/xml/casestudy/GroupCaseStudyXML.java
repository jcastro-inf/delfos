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
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.io.xml.groupformationtechnique.GroupFormationTechniqueXML;
import delfos.group.io.xml.grs.GroupRecommenderSystemXML;
import delfos.group.io.xml.predictionprotocol.GroupPredictionProtocolXML;
import delfos.group.io.xml.validationtechnique.GroupValidationTechniqueXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import delfos.io.xml.casestudy.CaseStudyXML;
import static delfos.io.xml.casestudy.CaseStudyXML.CASE_ROOT_ELEMENT_NAME;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.io.xml.dataset.RelevanceCriteriaXML;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución
 * de un caso de uso concreto.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 Unknown date
 * @version 1.1 (3-Mayo-2013)
 */
public class GroupCaseStudyXML {

    private static int meanBuildTime;
    private static int meanRecommendationTime;
    public static String RESULT_EXTENSION = "xml";
    public static final String HASH_ATTRIBUTE_NAME = "hash";
    public static final String HASH_DATA_VALIDATION_ATTRIBUTE_NAME = "hash_DataValidation";
    public static final String HASH_TECHNIQUE_ATTRIBUTE_NAME = "hash_Technique";
    public static final String NUM_EXEC_ATTRIBUTE_NAME = "numExec";
    public static final String FULL_RESULT_SUFFIX = "_FULL";
    public static final String AGGR_RESULT_SUFFIX = "_AGGR";

    private GroupCaseStudyXML() {
    }

    private static Element getResultsElement(GroupCaseStudy c) {
        meanBuildTime = 0;
        meanRecommendationTime = 0;

        Element ejecuciones = new Element("Executions");
        Element ejecucion;
        int numExecutions = c.getNumExecutions();
        int numSplits = c.getGroupValidationTechnique().getNumberOfSplits();
        for (int nexecution = 0; nexecution < numExecutions; nexecution++) {
            ejecucion = new Element("Execution");
            for (int nSplit = 0; nSplit < numSplits; nSplit++) {
                Element split = new Element("Split");
                for (GroupEvaluationMeasure em : c.getEvaluationMeasures()) {

                    GroupMeasureResult mr = c.getMeasureResult(em, nexecution, nSplit);
                    split.addContent((Element) mr.getXMLElement().clone());
                }

                {
                    Element resultado = new Element("Build time".replaceAll(" ", "_"));
                    resultado.setAttribute("value", Long.toString(c.getBuildTime(nexecution, nSplit)));
                    float valueBuild = c.getBuildTime(nexecution, nSplit);
                    meanBuildTime += valueBuild / (c.getNumExecutions() * c.getGroupValidationTechnique().getNumberOfSplits());
                    split.addContent(resultado);
                }

                {
                    Element resultado = new Element("Group build time".replaceAll(" ", "_"));
                    resultado.setAttribute("value", Long.toString(c.getGroupBuildTime(nexecution, nSplit)));
                    float valueBuild = c.getGroupBuildTime(nexecution, nSplit);
                    meanBuildTime += valueBuild / (c.getNumExecutions() * c.getGroupValidationTechnique().getNumberOfSplits());
                    split.addContent(resultado);
                }

                {
                    Element resultado = new Element("Recommendation time".replaceAll(" ", "_"));
                    resultado.setAttribute("value", Long.toString(c.getRecommendationTime(nexecution, nSplit)));
                    float valueRecom = c.getRecommendationTime(nexecution, nSplit);
                    meanRecommendationTime += valueRecom / (c.getNumExecutions() * c.getGroupValidationTechnique().getNumberOfSplits());
                    split.addContent(resultado);
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
        Element meanBuildTime1 = new Element("Build_time");
        meanBuildTime1.addContent(Float.toString(meanBuildTime));
        mediaMedidas.addContent(meanBuildTime1);
        Element meanRecommendationTime1 = new Element("Recommendation_time");
        meanRecommendationTime1.addContent(Float.toString(meanRecommendationTime));
        mediaMedidas.addContent(meanRecommendationTime1);

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
        casoDeUso.addContent(GroupValidationTechniqueXML.getElement(caseStudyGroup.getGroupValidationTechnique()));
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

        dateBasedName = dateBasedName + "_seed=" + caseStudyGroup.getSeedValue() + "." + CaseStudyXML.RESULT_EXTENSION;

        File f = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + dateBasedName);
        return f.getAbsolutePath();
    }

    public static String getCaseStudyFileName(GroupCaseStudy caseStudyGroup) {
        return caseStudyGroup.getAlias() + "_seed=" + caseStudyGroup.getSeedValue() + "_numExec=" + caseStudyGroup.getNumExecutions();
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
        casoDeUso.addContent(GroupValidationTechniqueXML.getElement(caseStudyGroup.getGroupValidationTechnique()));
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
        casoDeUso.setAttribute(HASH_DATA_VALIDATION_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.hashCodeWithoutGroupRecommenderSystem()));
        casoDeUso.setAttribute(HASH_TECHNIQUE_ATTRIBUTE_NAME, Integer.toString(caseStudyGroup.hashCodeOfTheRecommenderSystem()));

        casoDeUso.addContent(GroupRecommenderSystemXML.getElement(caseStudyGroup.getGroupRecommenderSystem()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudyGroup.getDatasetLoader()));

        casoDeUso.addContent(GroupFormationTechniqueXML.getElement(caseStudyGroup.getGroupFormationTechnique()));
        casoDeUso.addContent(GroupValidationTechniqueXML.getElement(caseStudyGroup.getGroupValidationTechnique()));
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
        GroupValidationTechnique groupValidationTechnique = GroupValidationTechniqueXML.getGroupValidationTechnique(caseStudy.getChild(GroupValidationTechniqueXML.ELEMENT_NAME));
        GroupPredictionProtocol groupPredictionProtocol = GroupPredictionProtocolXML.getGroupPredictionProtocol(caseStudy.getChild(GroupPredictionProtocolXML.ELEMENT_NAME));
        RelevanceCriteria relevanceCriteria = RelevanceCriteriaXML.getRelevanceCriteria(caseStudy.getChild(RelevanceCriteriaXML.ELEMENT_NAME));

        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(caseStudy.getChild(DatasetLoaderXML.ELEMENT_NAME));

        String caseStudyAlias = caseStudy.getAttributeValue(ParameterOwner.ALIAS.getName());
        return new GroupCaseStudyConfiguration(groupRecommenderSystem, datasetLoader, groupFormationTechnique, groupValidationTechnique, groupPredictionProtocol, relevanceCriteria, caseStudyAlias);
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
}
