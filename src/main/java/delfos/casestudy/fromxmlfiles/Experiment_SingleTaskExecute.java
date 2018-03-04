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

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.Experiment;
import delfos.experiment.ExperimentListener_default;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.xml.experiment.ExperimentXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;

import java.io.File;
import java.util.Collection;
import java.util.function.Consumer;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 27-ene-2014
 */
public class Experiment_SingleTaskExecute implements Consumer<Experiment> {

    @Override
    public void accept(Experiment experiment) {
        try {
            executeExperiment(experiment);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }

    private void executeExperiment(Experiment experiment) {

        String threadName = Thread.currentThread().getName();
        String newThreadName = threadName + "_" + experiment.getAlias();
        Thread.currentThread().setName(newThreadName);

        experiment.addExperimentListener(new ExperimentListener_default(System.out, 10000));
        experiment.execute();

        File fileToSaveResults = new File(experiment.getResultsDirectory().getPath() + File.separator + experiment.getAlias());

        File excelFile = FileUtilities.addSufix(fileToSaveResults, ".xls");
        File xmlFile = FileUtilities.addSufix(fileToSaveResults, ".xml");

        ExperimentXML.saveExperiment(experiment, xmlFile.getAbsoluteFile());
        if(experiment instanceof CaseStudy) {
            CaseStudy caseStudy = (CaseStudy) experiment;
            CaseStudyExcel.saveCaseResults(caseStudy, excelFile);
        } else if (experiment instanceof GroupCaseStudy){
            GroupCaseStudy groupCaseStudy = (GroupCaseStudy) experiment;
            GroupCaseStudyExcel.saveCaseResults(groupCaseStudy,excelFile);
        }
    }

}
