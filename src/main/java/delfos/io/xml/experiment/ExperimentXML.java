package delfos.io.xml.experiment;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.experiment.Experiment;
import delfos.experiment.casestudy.CaseStudy;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class ExperimentXML {
    public static final String ELEMENT_NAME = "Experiment";

    public static final String EXPERIMENT_TYPE_ATTRIBUTE = "experimentType";

    public static Element getElement(Experiment experiment){
        Element element = ParameterOwnerXML.getElement(experiment);

        element.setName(ELEMENT_NAME);
        element.setAttribute(EXPERIMENT_TYPE_ATTRIBUTE,experiment.getClass().getSimpleName());

        return element;
    }

    public static Experiment getExperiment(Element element){
        String experimentType = element.getAttributeValue(EXPERIMENT_TYPE_ATTRIBUTE);

        if(CaseStudy.class.getSimpleName().equals(experimentType)) {
            return CaseStudyXML.loadCaseResults(element).getCaseStudy();
        } else if(GroupCaseStudy.class.getSimpleName().equals(experimentType)) {
            return GroupCaseStudyXML.loadGroupCaseDescription(element).createGroupCaseStudy();
        } else {
            return (Experiment) ParameterOwnerXML.getParameterOwner(element);
        }
    }

    public static void saveExperiment(Experiment experiment, File file) {
        Element experimentElement = getElement(experiment);
        Document doc = new Document(experimentElement);
        FileUtilities.createDirectoriesForFile(file);

        try (FileWriter fileWriter = new FileWriter(file)) {
            new XMLOutputter(Constants.getXMLFormat()).output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    public static Experiment loadExperiment(File file) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();

        Document doc = builder.build(file);
        Element experimentElement = doc.getRootElement();

        Experiment experiment = getExperiment(experimentElement);
        return experiment;
    }
}
