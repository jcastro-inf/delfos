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
package delfos.group.casestudy.fromxmlfiles;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 27-ene-2014
 */
public class ExecuteGroupCaseStudy_Task extends Task {

    private final File experimentsDirectory;
    private final String caseName;
    private final GroupCaseStudyConfiguration groupCaseStudyConfiguration;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Collection<GroupEvaluationMeasure> groupEvaluationMeasures;
    private final Optional<Integer> numExecutions;
    private final Optional<Long> seed;

    public ExecuteGroupCaseStudy_Task(
            File experimentsDirectory,
            String caseName,
            GroupCaseStudyConfiguration groupCaseStudyConfiguration,
            DatasetLoader<? extends Rating> datasetLoader,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            Optional<Integer> numExecutions,
            Optional<Long> seed) {

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

    public Optional<Integer> getNumExecutions() {
        return numExecutions;
    }

    public Optional<Long> getSeed() {
        return seed;
    }
}
