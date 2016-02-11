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
package delfos.io.xml.casestudy;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import delfos.experiment.casestudy.CaseStudyResults;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.io.xml.dataset.DatasetLoaderXML;
import delfos.io.xml.dataset.RelevanceCriteriaXML;
import delfos.io.xml.predictionprotocol.PredictionProtocolXML;
import delfos.io.xml.rs.RecommenderSystemXML;
import delfos.io.xml.validationtechnique.ValidationTechniqueXML;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.GenericRecommenderSystem;
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
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 UnkowDate
 */
public class CaseStudyXML {

    private static int meanBuildTime;
    private static int meanRecommendationTime;
    public static String RESULT_EXTENSION = "xml";
    public static final String CASE_ROOT_ELEMENT_NAME = "Case";
    public static final String AGGREGATE_VALUES_ELEMENT_NAME = "Aggregate_values";
    public static final String EXECUTIONS_RESULTS_ELEMENT_NAME = "Executions";

    private static Element getResultsElement(CaseStudy c) {
        meanBuildTime = 0;
        meanRecommendationTime = 0;

        Element ejecuciones = new Element(EXECUTIONS_RESULTS_ELEMENT_NAME);
        Element ejecucion;
        int numExecutions = c.getNumExecutions();
        int numSplits = c.getNumberOfSplits();
        for (int ex = 0; ex < numExecutions; ex++) {
            ejecucion = new Element("Execution");
            for (int nSplit = 0; nSplit < numSplits; nSplit++) {
                Element split = new Element("Split");
                for (EvaluationMeasure em : c.getEvaluationMeasures()) {

                    MeasureResult mr = c.getMeasureResult(em, ex, nSplit);
                    split.addContent((Element) mr.getXMLElement().clone());
                }
                Element resultado = new Element("Build time".replaceAll(" ", "_"));
                resultado.setAttribute("value", Long.toString(c.getBuildTime(ex, nSplit)));
                float valueBuild = c.getBuildTime(ex, nSplit);
                meanBuildTime += valueBuild / (c.getNumExecutions() * c.getNumberOfSplits());
                split.addContent(resultado);

                resultado = new Element("Recommendation time".replaceAll(" ", "_"));
                resultado.setAttribute("value", Long.toString(c.getRecommendationTime(ex, nSplit)));
                float valueRecom = c.getRecommendationTime(ex, nSplit);
                meanRecommendationTime += valueRecom / (c.getNumExecutions() * c.getNumberOfSplits());
                split.addContent(resultado);
                ejecucion.addContent(split);
            }
            ejecuciones.addContent(ejecucion);
        }
        return ejecuciones;
    }

    private static Element getAggregatedResultsElement(CaseStudy c) {
        Element mediaMedidas = new Element(AGGREGATE_VALUES_ELEMENT_NAME);
        for (EvaluationMeasure em : c.getEvaluationMeasures()) {
            Element element = c.getMeasureResult(em).getXMLElement();
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

    public synchronized static void caseStudyToXMLFile(CaseStudy caseStudy, File file) {
        if (!caseStudy.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        Document doc = new Document();
        Element casoDeUso = new Element(CASE_ROOT_ELEMENT_NAME);

        casoDeUso.setAttribute(SEED_ATTRIBUTE_NAME, Long.toString(caseStudy.getSeedValue()));
        casoDeUso.setAttribute(NUM_EXEC_ATTRIBUTE_NAME, Integer.toString(caseStudy.getNumExecutions()));

        casoDeUso.addContent(RecommenderSystemXML.getElement(caseStudy.getRecommenderSystem()));
        casoDeUso.addContent(ValidationTechniqueXML.getElement(caseStudy.getValidationTechnique()));
        casoDeUso.addContent(PredictionProtocolXML.getElement(caseStudy.getPredictionProtocol()));

        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudy.getRelevanceCriteria()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudy.getDatasetLoader()));

        casoDeUso.addContent(getResultsElement(caseStudy));
        casoDeUso.addContent(getAggregatedResultsElement(caseStudy));
        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);

        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }
    public static final String SEED_ATTRIBUTE_NAME = "seed";
    public static final String NUM_EXEC_ATTRIBUTE_NAME = "numExec";

    public static void saveCaseResults(CaseStudy caseStudy) {
        Date date = new Date();
        String dateBasedName = "aux";
        try {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH.mm.ss", new Locale("es", "ES"));
            dateBasedName = sdf.format(date);
        } catch (Throwable ex) {
            Global.showWarning("Cannot get timestamp" + ex.getMessage() + "\n");
            Global.showError(ex);
        }

        dateBasedName = dateBasedName
                + " seed=" + caseStudy.getSeedValue()
                + "." + CaseStudyXML.RESULT_EXTENSION;

        File tmp = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + dateBasedName);

        CaseStudyXML.caseStudyToXMLFile(caseStudy, tmp);
    }

    /**
     * Carga un caso de estudio desde un archivo XML
     *
     * @param file Archivo XML del que se carga la configuración del caso de
     * estudio.
     * @return
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    public static CaseStudyConfiguration loadCase(File file) throws JDOMException, IOException {

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(file);

        Element caseStudy = doc.getRootElement();
        if (!caseStudy.getName().equals(CASE_ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study (" + file.getAbsolutePath() + ")");
        }
        GenericRecommenderSystem<Object> recommenderSystem = RecommenderSystemXML.getRecommenderSystem(caseStudy.getChild(RecommenderSystemXML.ELEMENT_NAME));
        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(caseStudy.getChild(DatasetLoaderXML.ELEMENT_NAME));
        ValidationTechnique validationTechnique = ValidationTechniqueXML.getValidationTechnique(caseStudy.getChild(ValidationTechniqueXML.ELEMENT_NAME));
        PredictionProtocol predictionProtocol = PredictionProtocolXML.getPredictionProtocol(caseStudy.getChild(PredictionProtocolXML.ELEMENT_NAME));
        RelevanceCriteria relevanceCriteria = RelevanceCriteriaXML.getRelevanceCriteria(caseStudy.getChild(RelevanceCriteriaXML.ELEMENT_NAME));

        return new CaseStudyConfiguration(
                recommenderSystem, datasetLoader,
                validationTechnique,
                predictionProtocol,
                relevanceCriteria);
    }

    public static String getDefaultFileName(CaseStudy caseStudy) {
        Date date = new Date();
        String dateBasedName = "aux";
        try {
            SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH.mm.ss", new Locale("es", "ES"));
            dateBasedName = sdf.format(date);
        } catch (Exception ex) {
            Global.showWarning("Cannot get timestamp" + ex.getMessage() + "\n");
            Global.showError(ex);
        }

        dateBasedName = dateBasedName
                + " seed=" + caseStudy.getSeedValue()
                + "." + CaseStudyXML.RESULT_EXTENSION;

        File f = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator + dateBasedName);
        return f.getAbsolutePath();
    }

    public synchronized static void caseStudyToXMLFile(CaseStudy caseStudy, String descriptiveName, File f) {
        if (!caseStudy.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        Document doc = new Document();
        Element casoDeUso = new Element("Case");

        casoDeUso.addContent(RecommenderSystemXML.getElement(caseStudy.getRecommenderSystem()));
        casoDeUso.addContent(ValidationTechniqueXML.getElement(caseStudy.getValidationTechnique()));

        casoDeUso.addContent(PredictionProtocolXML.getElement(caseStudy.getPredictionProtocol()));

        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudy.getRelevanceCriteria()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudy.getDatasetLoader()));

        casoDeUso.addContent(getResultsElement(caseStudy));
        casoDeUso.addContent(getAggregatedResultsElement(caseStudy));
        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        try (FileWriter fileWriter = new FileWriter(f)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    public static void saveCaseDescription(CaseStudy caseStudy, String file) {
        CaseStudyXML.caseStudyToXMLFile_onlyDescription(caseStudy, new File(file));
    }

    public static void saveCaseResults(CaseStudy caseStudy, String descriptivePrefix, String file) {
        File fileFile = FileUtilities.addPrefix(new File(file), descriptivePrefix);
        if (Constants.isPrintFullXML()) {
            caseStudyToXMLFile(caseStudy, "", FileUtilities.addSufix(fileFile, "_FULL"));
        }

        File aggregateFileName = FileUtilities.addSufix(fileFile, "_AGGR");
        aggregateFileName = FileUtilities.changeExtension(aggregateFileName, ".aggr.XML");
        caseStudyToXMLFile_onlyAggregate(caseStudy, descriptivePrefix, aggregateFileName);
    }

    private static void caseStudyToXMLFile_onlyDescription(CaseStudy caseStudy, File file) {

        if (caseStudy.isFinished()) {
            throw new IllegalArgumentException("Ya se ha ejecutado el caso de estudio!");
        }

        Document doc = new Document();
        Element casoDeUso = new Element(CASE_ROOT_ELEMENT_NAME);

        casoDeUso.addContent(RecommenderSystemXML.getElement(caseStudy.getRecommenderSystem()));
        casoDeUso.addContent(ValidationTechniqueXML.getElement(caseStudy.getValidationTechnique()));

        casoDeUso.addContent(PredictionProtocolXML.getElement(caseStudy.getPredictionProtocol()));

        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudy.getRelevanceCriteria()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudy.getDatasetLoader()));

        //casoDeUso.addContent(getResultsElement(caseStudy));
        //casoDeUso.addContent(getAggregatedResultsElement(caseStudy));
        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    private static void caseStudyToXMLFile_onlyAggregate(CaseStudy caseStudy, String descriptivePrefix, File file) {
        if (!caseStudy.isFinished()) {
            throw new UnsupportedOperationException("No se ha ejecutado el caso de uso todavía");
        }

        Document doc = new Document();
        Element casoDeUso = new Element("Case");

        casoDeUso.setAttribute("seed", Long.toString(caseStudy.getSeedValue()));
        casoDeUso.setAttribute("numExec", Integer.toString(caseStudy.getNumExecutions()));

        casoDeUso.addContent(RecommenderSystemXML.getElement(caseStudy.getRecommenderSystem()));
        casoDeUso.addContent(ValidationTechniqueXML.getElement(caseStudy.getValidationTechnique()));

        casoDeUso.addContent(PredictionProtocolXML.getElement(caseStudy.getPredictionProtocol()));

        casoDeUso.addContent(RelevanceCriteriaXML.getElement(caseStudy.getRelevanceCriteria()));
        casoDeUso.addContent(DatasetLoaderXML.getElement(caseStudy.getDatasetLoader()));

        //casoDeUso.addContent(getResultsElement(caseStudy));
        casoDeUso.addContent(getAggregatedResultsElement(caseStudy));

        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    /**
     * Carga un caso de estudio desde un archivo XML
     *
     * @param file Archivo XML del que se carga la configuración del caso de
     * estudio.
     * @return
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    public static CaseStudyResults loadCaseResults(File file) throws JDOMException, IOException {

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(file);

        Element caseStudy = doc.getRootElement();
        if (!caseStudy.getName().equals(CASE_ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study.");
        }
        GenericRecommenderSystem<Object> recommenderSystem = RecommenderSystemXML.getRecommenderSystem(caseStudy.getChild(RecommenderSystemXML.ELEMENT_NAME));
        ValidationTechnique validationTechnique = ValidationTechniqueXML.getValidationTechnique(caseStudy.getChild(ValidationTechniqueXML.ELEMENT_NAME));
        PredictionProtocol predictionProtocol = PredictionProtocolXML.getPredictionProtocol(caseStudy.getChild(PredictionProtocolXML.ELEMENT_NAME));
        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML.getDatasetLoader(caseStudy.getChild(DatasetLoaderXML.ELEMENT_NAME));

        EvaluationMeasuresResults aggregatedElement = getAggregateEvaluationMeasures(caseStudy.getChild("Aggregate_values"));

        return new CaseStudyResults(
                recommenderSystem,
                datasetLoader,
                validationTechnique,
                predictionProtocol,
                aggregatedElement.evaluationMeasuresResults,
                aggregatedElement.buildTime,
                aggregatedElement.recommendationTime);

    }

    static class EvaluationMeasuresResults {

        Map<EvaluationMeasure, Double> evaluationMeasuresResults;
        long buildTime;
        long recommendationTime;

        public EvaluationMeasuresResults(Map<EvaluationMeasure, Double> evaluationMeasuresResults, long buildTime, long recommendationTime) {
            this.evaluationMeasuresResults = evaluationMeasuresResults;
            this.buildTime = buildTime;
            this.recommendationTime = recommendationTime;
        }
    }

    static EvaluationMeasuresResults getAggregateEvaluationMeasures(Element element) {
        if (!element.getName().equals("Aggregate_values")) {
            throw new IllegalArgumentException("The XML element is not an aggregate values element.");
        }

        Map<EvaluationMeasure, Double> ret = new TreeMap<>();
        long buildTime = -1;
        long recommendationTime = -1;

        for (Element child : element.getChildren()) {
            String evaluationMeasureName = child.getName();

            EvaluationMeasure evaluationMeasure = EvaluationMeasuresFactory.getInstance().getClassByName(evaluationMeasureName);
            String valueString = child.getAttributeValue("value");
            if (valueString == null) {
                continue;
            }
            double measureValue = new Double(valueString);

            if (evaluationMeasure == null) {

                if (evaluationMeasureName.equals("Build_time")) {
                    buildTime = (long) measureValue;
                }
                if (evaluationMeasureName.equals("Recommendation_time")) {
                    recommendationTime = (long) measureValue;
                }

            }

            ret.put(evaluationMeasure, measureValue);
        }

        return new EvaluationMeasuresResults(ret, buildTime, recommendationTime);
    }
}
