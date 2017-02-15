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
import delfos.common.parameters.ParameterOwner;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.SeedHolder;
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
import delfos.rs.RecommenderSystem;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Predicate;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 * Clase encargada de hacer la entrada/salida de los resultados de la ejeución de un caso de uso concreto.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 1.0 UnkowDate
 */
public class CaseStudyXML {

    public static String RESULT_EXTENSION = "xml";
    public static final String CASE_ROOT_ELEMENT_NAME = "Case";
    public static final String AGGREGATE_VALUES_ELEMENT_NAME = "Aggregate_values";
    public static final String EXECUTIONS_RESULTS_ELEMENT_NAME = "Executions";

    public static Predicate<File> RESULTS_FILES = (file) -> {
        try {
            CaseStudyResults loadCaseResults = loadCaseResults(file);
            return true;
        } catch (JDOMException ex) {
            return false;
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_READ_FILE.exit(ex);
            return false;
        }
    };

    private static <RecommendationModel extends Object, RatingType extends Rating> Element getResultsElement(
            CaseStudy<RecommendationModel, RatingType> caseStudy) {

        Element executionsElement = new Element(EXECUTIONS_RESULTS_ELEMENT_NAME);
        Element executionElement;
        int numExecutions = caseStudy.getNumExecutions();
        int numSplits = caseStudy.getNumberOfSplits();
        for (int execution = 0; execution < numExecutions; execution++) {
            executionElement = new Element("Execution");
            for (int split = 0; split < numSplits; split++) {
                Element splitElement = new Element("Split");
                for (EvaluationMeasure evaluationMeasure : caseStudy.getEvaluationMeasures()) {

                    MeasureResult measureResult = caseStudy.getMeasureResult(evaluationMeasure, execution, split);
                    splitElement.addContent((Element) measureResult.getXMLElement().clone());

                }
                executionElement.addContent(splitElement);
            }
            executionsElement.addContent(executionElement);
        }
        return executionsElement;
    }

    private static <RecommendationModel extends Object, RatingType extends Rating>
            Element getAggregatedResultsElement(
                    CaseStudy<RecommendationModel, RatingType> caseStudy) {

        Element aggregatedResultsElement = new Element(AGGREGATE_VALUES_ELEMENT_NAME);
        for (EvaluationMeasure evaluationMeasure : caseStudy.getEvaluationMeasures()) {
            Element evaluationMesureElement = caseStudy.getMeasureResult(evaluationMeasure).getXMLElement();
            aggregatedResultsElement.addContent(evaluationMesureElement);
        }

        return aggregatedResultsElement;
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

        FileUtilities.createDirectoriesForFileIfNotExist(tmp);
        CaseStudyXML.caseStudyToXMLFile(caseStudy, tmp);
    }

    /**
     * Carga un caso de estudio desde un archivo XML
     *
     * @param file Archivo XML del que se carga la configuración del caso de estudio.
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

    public static void saveCaseResults(CaseStudy caseStudy, File file) {

        if (Constants.isPrintFullXML()) {
            caseStudyToXMLFile(caseStudy, "", FileUtilities.addSufix(file, "_FULL"));
        }

        File aggregateFileName = FileUtilities.addSufix(file, "_AGGR");
        caseStudyToXMLFile_onlyAggregate(caseStudy, aggregateFileName);
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

        doc.addContent(casoDeUso);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        FileUtilities.createDirectoriesForFile(file);
        try (FileWriter fileWriter = new FileWriter(file)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    private static void caseStudyToXMLFile_onlyAggregate(CaseStudy caseStudy, File file) {
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

    public static <RecommendationModel extends Object, RatingType extends Rating>
            CaseStudyResults<RecommendationModel, RatingType> loadCaseResults(
                    File file,
                    Class<RecommendationModel> recommendationModelClass,
                    Class<RatingType> ratingTypeClass
            ) throws JDOMException, IOException {

        return (CaseStudyResults<RecommendationModel, RatingType>) loadCaseResults(file);
    }

    /**
     * Carga un caso de estudio desde un archivo XML
     *
     * @param <RecommendationModel>
     * @param <RatingType>
     * @param file Archivo XML del que se carga la configuración del caso de estudio.
     * @return
     * @throws org.jdom2.JDOMException
     * @throws java.io.IOException
     */
    public static <RecommendationModel extends Object, RatingType extends Rating> CaseStudyResults<RecommendationModel, RatingType> loadCaseResults(File file) throws JDOMException, IOException {

        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(file);

        Element caseStudyElement = doc.getRootElement();
        if (!caseStudyElement.getName().equals(CASE_ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study.");
        }
        RecommenderSystem<? extends Object> recommenderSystem = (RecommenderSystem<? extends Object>) RecommenderSystemXML
                .getRecommenderSystem(caseStudyElement.getChild(RecommenderSystemXML.ELEMENT_NAME));

        DatasetLoader<? extends Rating> datasetLoader = DatasetLoaderXML
                .getDatasetLoader(caseStudyElement.getChild(DatasetLoaderXML.ELEMENT_NAME));

        ValidationTechnique validationTechnique = ValidationTechniqueXML
                .getValidationTechnique(caseStudyElement.getChild(ValidationTechniqueXML.ELEMENT_NAME));

        PredictionProtocol predictionProtocol = PredictionProtocolXML
                .getPredictionProtocol(caseStudyElement.getChild(PredictionProtocolXML.ELEMENT_NAME));

        RelevanceCriteria relevanceCriteria = RelevanceCriteriaXML
                .getRelevanceCriteria(caseStudyElement.getChild(RelevanceCriteriaXML.ELEMENT_NAME));

        Map<EvaluationMeasure, MeasureResult> evaluationMeasuresResults = getEvaluationMeasures(caseStudyElement);

        long seed = Long.parseLong(caseStudyElement.getAttributeValue(SeedHolder.SEED.getName()));
        int numExecutions = Integer.parseInt(caseStudyElement.getAttributeValue(NUM_EXEC_ATTRIBUTE_NAME));
        String caseStudyAlias = caseStudyElement.getAttributeValue(ParameterOwner.ALIAS.getName());

        CaseStudy caseStudy = new CaseStudy(
                recommenderSystem,
                datasetLoader, validationTechnique,
                predictionProtocol,
                relevanceCriteria,
                evaluationMeasuresResults.keySet(),
                numExecutions);

        caseStudy.setAlias(caseStudyAlias);
        caseStudy.setSeedValue(seed);
        caseStudy.setAggregateResults(evaluationMeasuresResults);

        return new CaseStudyResults(caseStudy);
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
                IllegalStateException ex = new IllegalStateException("Evaluation measure '" + evaluationMeasureName + "' not in factory.");
                Global.showError(ex);
            } else {
                ret.put(evaluationMeasure, measureValue);
            }
        }

        return new EvaluationMeasuresResults(ret, buildTime, recommendationTime);
    }

    private static Map<EvaluationMeasure, MeasureResult> getEvaluationMeasures(Element caseStudy) {

        Map<EvaluationMeasure, MeasureResult> evaluationMeasuresResults = new TreeMap<>();

        Element aggregateValues = caseStudy.getChild(AGGREGATE_VALUES_ELEMENT_NAME);

        if (aggregateValues == null) {
            throw new IllegalStateException("Unable to load a case study description only, the XML must have results details.");
        }

        for (Element evaluationMeasureResultElement : aggregateValues.getChildren()) {
            String name = evaluationMeasureResultElement.getName();

            EvaluationMeasure evaluationMeasure = EvaluationMeasuresFactory.getInstance().getClassByName(name);
            if (evaluationMeasure == null) {
                throw new IllegalStateException("The group evaluation measure '" + name + "' does not exists in delfos' factory");
            } else {
                evaluationMeasuresResults.put(evaluationMeasure, evaluationMeasure.getEvaluationMeasureResultFromXML(evaluationMeasureResultElement));
            }
        }

        return evaluationMeasuresResults;
    }

}
