package delfos.casestudy.fromxmlfiles;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;
import delfos.Constants;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import delfos.io.xml.casestudy.CaseStudyConfigurationXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 27-ene-2014
 */
public class ExecuteCaseStudy_Task extends Task {

    private final File experimentsDirectory;
    private final String caseName;
    private final CaseStudyConfiguration caseStudyConfiguration;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Collection<EvaluationMeasure> evaluationMeasures;
    private final int numExecutions;
    private final RelevanceCriteria relevanceCriteria;
    private final long seed;

    public ExecuteCaseStudy_Task(
            File experimentsDirectory,
            String caseName,
            CaseStudyConfiguration caseStudyConfiguration,
            DatasetLoader<? extends Rating> datasetLoader,
            RelevanceCriteria relevanceCriteria, Collection<EvaluationMeasure> EvaluationMeasures, int numExecutions, long seed) {
        this.experimentsDirectory = experimentsDirectory;
        this.caseName = caseName;
        this.caseStudyConfiguration = caseStudyConfiguration;
        this.datasetLoader = datasetLoader;
        this.evaluationMeasures = EvaluationMeasures;
        this.numExecutions = numExecutions;
        this.relevanceCriteria = relevanceCriteria;
        this.seed = seed;
    }

    @Override
    public String toString() {
        Element caseStudyElement = CaseStudyConfigurationXML.caseStudyConfigurationToXMLElement(caseStudyConfiguration);

        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());
        StringWriter str = new StringWriter();
        try {
            outputter.output(caseStudyElement, str);
        } catch (IOException ex) {
            Logger.getLogger(ExecuteCaseStudy_Task.class.getName()).log(Level.SEVERE, null, ex);
        }
        return str.toString();
    }

    public String getCaseName() {
        return caseName;
    }

    public CaseStudyConfiguration getCaseStudyConfiguration() {
        return caseStudyConfiguration;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public File getExperimentsDirectory() {
        return experimentsDirectory;
    }

    public Collection<EvaluationMeasure> getEvaluationMeasures() {
        return Collections.unmodifiableCollection(evaluationMeasures);
    }

    public int getNumExecutions() {
        return numExecutions;
    }

    RelevanceCriteria getRelevanceCriteria() {
        return relevanceCriteria;
    }

    long getSeed() {
        return seed;
    }

}
