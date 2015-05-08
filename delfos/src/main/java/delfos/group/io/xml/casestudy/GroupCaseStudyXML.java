package delfos.group.io.xml.casestudy;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
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

    public synchronized static void caseStudyToXMLFile(GroupCaseStudy caseStudyGroup, String descriptiveName, File f) {
        if (!caseStudyGroup.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        Document doc = new Document();
        Element casoDeUso = new Element("Case");

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

        FileUtilities.createDirectoriesForFile(f);
        try (FileWriter fileWriter = new FileWriter(f)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    public static String getDefaultFileName(GroupCaseStudy caseStudyGroup) {
        Date date = new Date();
        String dateBasedName = "aux";
        try {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH.mm.ss", new Locale("es", "ES"));
            dateBasedName = sdf.format(date);
        } catch (Exception ex) {
            Global.showError(ex);
            Global.showWarning("Cannot get timestamp" + ex.getMessage() + "\n");
        }

        dateBasedName = dateBasedName + " seed=" + caseStudyGroup.getSeedValue() + "." + CaseStudyXML.RESULT_EXTENSION;

        File f = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + dateBasedName);
        return f.getAbsolutePath();
    }

    public static void saveCaseDescription(GroupCaseStudy caseStudyGroup, String file) {
        GroupCaseStudyXML.caseStudyToXMLFile_onlyDescription(caseStudyGroup, new File(file));
    }

    public static void saveCaseResults(GroupCaseStudy caseStudyGroup, String descriptivePrefix, String file) {
        File fileFile = FileUtilities.addPrefix(new File(file), descriptivePrefix);
        if (Constants.isPrintFullXML()) {

            File fullFile = FileUtilities.addSufix(fileFile, "_FULL");
            GroupCaseStudyXML.caseStudyToXMLFile(caseStudyGroup, "", fullFile);
        }

        File aggrFile = FileUtilities.addSufix(fileFile, "_AGGR");
        GroupCaseStudyXML.caseStudyToXMLFile_onlyAggregate(caseStudyGroup, descriptivePrefix, aggrFile);
    }

    private static void caseStudyToXMLFile_onlyDescription(GroupCaseStudy caseStudyGroup, File file) {

        if (caseStudyGroup.isFinished()) {
            throw new IllegalArgumentException("Ya se ha ejecutado el caso de estudio!");
        }

        Document doc = new Document();
        Element casoDeUso = new Element("Case");

        casoDeUso.addContent(GroupRecommenderSystemXML.getElement(caseStudyGroup.getGroupRecommenderSystem()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudyGroup.getDatasetLoader()));

        casoDeUso.addContent(GroupFormationTechniqueXML.getElement(caseStudyGroup.getGroupFormationTechnique()));
        casoDeUso.addContent(GroupValidationTechniqueXML.getElement(caseStudyGroup.getGroupValidationTechnique()));
        casoDeUso.addContent(GroupPredictionProtocolXML.getElement(caseStudyGroup.getGroupPredictionProtocol()));
        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudyGroup.getRelevanceCriteria()));
        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
            fileWriter.close();
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    private static void caseStudyToXMLFile_onlyAggregate(GroupCaseStudy caseStudyGroup, String descriptivePrefix, File file) {
        if (!caseStudyGroup.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        Document doc = new Document();
        Element casoDeUso = new Element("Case");

        casoDeUso.addContent(GroupRecommenderSystemXML.getElement(caseStudyGroup.getGroupRecommenderSystem()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudyGroup.getDatasetLoader()));

        casoDeUso.addContent(GroupFormationTechniqueXML.getElement(caseStudyGroup.getGroupFormationTechnique()));
        casoDeUso.addContent(GroupValidationTechniqueXML.getElement(caseStudyGroup.getGroupValidationTechnique()));
        casoDeUso.addContent(GroupPredictionProtocolXML.getElement(caseStudyGroup.getGroupPredictionProtocol()));

        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudyGroup.getRelevanceCriteria()));

        casoDeUso.addContent(getAggregatedResultsElement(caseStudyGroup));
        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }

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

        return new GroupCaseStudyConfiguration(groupRecommenderSystem, datasetLoader, groupFormationTechnique, groupValidationTechnique, groupPredictionProtocol, relevanceCriteria);
    }
}
