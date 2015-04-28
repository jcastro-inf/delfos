package delfos.group.casestudy.fromxmlfiles;

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
import delfos.casestudy.fromxmlfiles.ExecuteCaseStudy_Task;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.io.xml.casestudy.GroupCaseStudyConfigurationXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;

/**
 *
* @author Jorge Castro Gallardo
 *
 * @version 27-ene-2014
 */
public class ExecuteGroupCaseStudy_Task extends Task {

    private final File experimentsDirectory;
    private final String caseName;
    private final GroupCaseStudyConfiguration groupCaseStudyConfiguration;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Collection<GroupEvaluationMeasure> groupEvaluationMeasures;
    private final int numExecutions;
    private final long seed;

    public ExecuteGroupCaseStudy_Task(
            File experimentsDirectory,
            String caseName,
            GroupCaseStudyConfiguration groupCaseStudyConfiguration,
            DatasetLoader<? extends Rating> datasetLoader,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            int numExecutions,
            long seed) {

        this.experimentsDirectory = experimentsDirectory;
        this.caseName = caseName;
        this.groupCaseStudyConfiguration = groupCaseStudyConfiguration;
        this.datasetLoader = datasetLoader;
        this.groupEvaluationMeasures = groupEvaluationMeasures;
        this.numExecutions = numExecutions;
        this.seed = seed;
    }

    @Override
    public String toString() {
        Element caseStudyElement = GroupCaseStudyConfigurationXML.caseStudyConfigurationToXMLElement(groupCaseStudyConfiguration);

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

    public GroupCaseStudyConfiguration getCaseStudyConfiguration() {
        return groupCaseStudyConfiguration;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public File getExperimentsDirectory() {
        return experimentsDirectory;
    }

    public Collection<GroupEvaluationMeasure> getGroupEvaluationMeasures() {
        return Collections.unmodifiableCollection(groupEvaluationMeasures);
    }

    public int getNumExecutions() {
        return numExecutions;
    }

    public long getSeed() {
        return seed;
    }
}
