package delfos.main.managers.experiment.join.xml;

import delfos.common.Global;
import delfos.group.results.groupevaluationmeasures.AreaUnderRoc;
import delfos.io.excel.joiner.AggregateResultsMatrixExcelWriter;
import delfos.io.xml.UnrecognizedElementException;
import delfos.io.xml.casestudy.CaseStudyXML;
import static delfos.io.xml.casestudy.CaseStudyXML.AGGREGATE_VALUES_ELEMENT_NAME;
import static delfos.io.xml.casestudy.CaseStudyXML.CASE_ROOT_ELEMENT_NAME;
import static delfos.io.xml.casestudy.CaseStudyXML.EXECUTIONS_RESULTS_ELEMENT_NAME;
import delfos.io.xml.dataset.RelevanceCriteriaXML;
import delfos.io.xml.evaluationmeasures.NDCGXML;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.NDCG;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.roccurve.AreaUnderROC;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import org.apache.commons.io.FileUtils;
import org.jdom2.Attribute;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;

/**
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class AggregateResultsXML {

    public static final String AGGREGATE_XML_SUFFIX = "_AGGR.xml";

    public AggregateResultsXML() {
    }

    public List<File> filterResultsFiles(List<File> files) {

        List<File> returnFiles = files.stream()
                .filter(file -> file.getName().endsWith(AGGREGATE_XML_SUFFIX))
                .collect(Collectors.toList());

        return returnFiles;
    }

    public void joinAndWrite(Collection<File> files, File outputFile) {
        Map<String, Map<String, Object>> values = join(files);
        writeFinalExcel(values, outputFile);
    }

    public Map<String, Map<String, Object>> join(Collection<File> files) {
        Map<String, Map<String, Object>> values = new TreeMap<>();
        int i = 1;
        for (File file : files) {
            try {
                System.out.println("(" + (i++) + " of " + files.size() + "): " + "Reading file " + file.getName());
                Map<String, Object> valuesThisFile = extractMapFromFile(file);

                String experimentName = file.getName().replaceAll(AGGREGATE_XML_SUFFIX, "");

                if (values.containsKey(experimentName)) {
                    throw new IllegalStateException("Repeated experiment names: " + experimentName + "\nFile: " + file.getAbsolutePath());
                }

                values.put(experimentName, valuesThisFile);

            } catch (Exception ex) {
                Global.show("ERROR AT --> Reading file " + file);
                Global.showWarning("ERROR AT --> Reading file " + file);
                Global.showError(ex);
            }
        }
        return values;
    }

    public Map<String, Object> extractMapFromFile(File inputFile) throws JDOMException, IOException {

        Element caseStudy;

        long fileSize = FileUtils.sizeOf(inputFile);
        if (fileSize > Math.pow(2, 20)) {
            File smallerFile = new File(inputFile.getPath() + "_smaller");

            BufferedWriter writer;
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                writer = new BufferedWriter(new FileWriter(smallerFile));
                String line = reader.readLine();
                while (line != null) {
                    if (!line.contains("ndcg_value=")) {
                        writer.write(line);
                    }
                    line = reader.readLine();
                }
            }
            writer.close();

            SAXBuilder builder = new SAXBuilder();

            Document doc = builder.build(smallerFile);
            caseStudy = doc.getRootElement();
            smallerFile.delete();
        } else {
            SAXBuilder builder = new SAXBuilder();

            Document doc = builder.build(inputFile);
            caseStudy = doc.getRootElement();
        }

        Map<String, Object> valuesByColumnName = new TreeMap<>();

        if (!caseStudy.getName().equals(CASE_ROOT_ELEMENT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study (" + inputFile.getAbsolutePath() + ")");
        }

        valuesByColumnName.putAll(extractCaseParametersMapFromElement(caseStudy));
        valuesByColumnName.putAll(extractCaseResultsMapFromElement(caseStudy));

        return valuesByColumnName;

    }

    public Map<String, Object> extractCaseParametersMapFromElement(Element element) {
        Map<String, Object> valuesByColumnName = new TreeMap<>();

        String elementName = element.getName();
        if (element.getAttribute("name") != null) {
            elementName = elementName + "." + element.getAttributeValue("name");
        }

        if (elementName.equals(CASE_ROOT_ELEMENT_NAME)) {
            for (Attribute attribute : element.getAttributes()) {
                String name = CaseStudyXML.CASE_ROOT_ELEMENT_NAME + "." + attribute.getName();
                String value = attribute.getValue();
                valuesByColumnName.put(name, value);
            }
        }

        if (elementName.equals(RelevanceCriteriaXML.ELEMENT_NAME)) {

            double threshold = RelevanceCriteriaXML.getRelevanceCriteria(element).getThreshold().doubleValue();
            valuesByColumnName.put(RelevanceCriteriaXML.ELEMENT_NAME, threshold);

        } else if (element.getChildren().isEmpty()) {
            String columnName;
            String value;

            if (!element.hasAttributes()) {
                throw new IllegalArgumentException("arg");
            }

            if (element.getAttribute("name") != null) {
                columnName = elementName;
                value = element.getAttributeValue("name");
            } else if (element.getAttribute("parameterName") != null) {
                columnName = element.getAttributeValue("parameterName");
                value = element.getAttributeValue("parameterValue");
            } else {
                throw new IllegalStateException("arg");
            }

            valuesByColumnName.put(columnName, value);

        } else {
            for (Element child : element.getChildren()) {

                if (child.getName().equals(AGGREGATE_VALUES_ELEMENT_NAME)) {
                    continue;
                }

                if (child.getName().equals(EXECUTIONS_RESULTS_ELEMENT_NAME)) {
                    throw new IllegalArgumentException("The file is a full results file!");
                }

                Map<String, Object> extractCaseParametersMapFromElement = extractCaseParametersMapFromElement(child);

                for (Map.Entry<String, Object> entry : extractCaseParametersMapFromElement.entrySet()) {
                    String columnNameWithPrefix = elementName + "." + entry.getKey();

                    Object value = entry.getValue();

                    valuesByColumnName.put(columnNameWithPrefix, value);
                }

            }
        }
        return valuesByColumnName;

    }

    private Map<String, Object> extractCaseResultsMapFromElement(Element caseStudy) {

        Map<String, Object> ret = new TreeMap<>();

        Element aggregateValues = caseStudy.getChild(AGGREGATE_VALUES_ELEMENT_NAME);

        for (Element measureResult : aggregateValues.getChildren()) {

            if (measureResult.getName().equals(AreaUnderROC.class.getSimpleName())
                    || measureResult.getName().equals(AreaUnderRoc.class.getSimpleName())) {

                Element curveElement = measureResult.getChild(ConfusionMatricesCurveXML.CONFUSION_MATRIX_CURVE_ELEMENT_NAME);

                try {
                    ConfusionMatricesCurve curve = ConfusionMatricesCurveXML.getConfusionMatricesCurve(curveElement);

                    for (int index = 1; index < curve.size(); index++) {
                        float precisionAt = curve.getPrecisionAt(index);

                        DecimalFormat format = new DecimalFormat("000");

                        ret.put("Precision@" + format.format(index), precisionAt);

                        if (index == 20) {
                            break;
                        }
                    }

                } catch (UnrecognizedElementException ex) {
                    Logger.getLogger(AggregateResultsXML.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            if (measureResult.getName().equals(NDCG.class.getSimpleName())) {
                ret.put(NDCGXML.NDCG_ELEMENT_NAME + "." + NDCGXML.NDCG_MIN_ATTRIBUTE_NAME, measureResult.getAttributeValue(NDCGXML.NDCG_MIN_ATTRIBUTE_NAME));
                ret.put(NDCGXML.NDCG_ELEMENT_NAME + "." + NDCGXML.NDCG_PERCENTILE_25_ATTRIBUTE_NAME, measureResult.getAttributeValue(NDCGXML.NDCG_PERCENTILE_25_ATTRIBUTE_NAME));
                ret.put(NDCGXML.NDCG_ELEMENT_NAME + "." + NDCGXML.NDCG_MEAN_ATTRIBUTE_NAME, measureResult.getAttributeValue(NDCGXML.NDCG_MEAN_ATTRIBUTE_NAME));
                ret.put(NDCGXML.NDCG_ELEMENT_NAME + "." + NDCGXML.NDCG_PERCENTILE_75_ATTRIBUTE_NAME, measureResult.getAttributeValue(NDCGXML.NDCG_PERCENTILE_75_ATTRIBUTE_NAME));
                ret.put(NDCGXML.NDCG_ELEMENT_NAME + "." + NDCGXML.NDCG_MAX_ATTRIBUTE_NAME, measureResult.getAttributeValue(NDCGXML.NDCG_MAX_ATTRIBUTE_NAME));
            }

            if (measureResult.getName().equals(ParameterOwnerXML.PARAMETER_OWNER_ELEMENT_NAME)) {
                String measureName = measureResult.getAttributeValue("name");
                String measureValue = measureResult.getAttributeValue("value");
                ret.put(measureName, measureValue);
            } else {
                String measureName = measureResult.getName();
                String measureValue = measureResult.getAttributeValue("value");
                ret.put(measureName, measureValue);
            }
        }

        return ret;
    }

    private void writeFinalExcel(Map<String, Map<String, Object>> values, File outputFile) {

        AggregateResultsMatrixExcelWriter.writeExcelFromMatrix(values, outputFile);
    }

}
