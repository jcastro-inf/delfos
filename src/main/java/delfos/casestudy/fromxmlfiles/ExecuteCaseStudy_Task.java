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
package delfos.casestudy.fromxmlfiles;

import delfos.Constants;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import delfos.io.xml.casestudy.CaseStudyConfigurationXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
