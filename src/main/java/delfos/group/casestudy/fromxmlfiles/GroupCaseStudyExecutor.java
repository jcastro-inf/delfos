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

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.ExecutionProgressListener_default;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.function.Consumer;
import org.jdom2.JDOMException;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 27-ene-2014
 */
public class GroupCaseStudyExecutor implements Consumer<ExecuteGroupCaseStudy_Task> {

    private void executeCaseStudy(
            File experimentsDirectory,
            String caseName,
            GroupCaseStudyConfiguration caseStudyConfiguration,
            DatasetLoader<? extends Rating> datasetLoader,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            int numExecutions,
            long seed)
            throws CannotLoadContentDataset, JDOMException, IOException, CannotLoadRatingsDataset {

        GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem = caseStudyConfiguration.getGroupRecommenderSystem();

        GroupFormationTechnique groupFormationTechnique = caseStudyConfiguration.getGroupFormationTechnique();
        GroupPredictionProtocol groupPredictionProtocol = caseStudyConfiguration.getGroupPredictionProtocol();
        ValidationTechnique validationTechnique = caseStudyConfiguration.getValidationTechnique();

        RelevanceCriteria relevanceCriteria = caseStudyConfiguration.getRelevanceCriteria();

        GroupCaseStudy caseStudyGroupRecommendation = new GroupCaseStudy(
                datasetLoader,
                groupRecommenderSystem,
                groupFormationTechnique,
                validationTechnique, groupPredictionProtocol,
                groupEvaluationMeasures,
                relevanceCriteria,
                numExecutions);

        caseStudyGroupRecommendation.setAlias("[" + datasetLoader.getAlias() + "]" + caseStudyConfiguration.getCaseStudyAlias());

        String threadName = Thread.currentThread().getName();
        Thread.currentThread().setName(threadName + "_" + caseStudyGroupRecommendation.getAlias());

        caseStudyGroupRecommendation.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
        caseStudyGroupRecommendation.addExecutionProgressListener(new ExecutionProgressListener_default(System.out, 10000));
        caseStudyGroupRecommendation.setSeedValue(seed);

        File resultsDirectory = new File(
                experimentsDirectory.getAbsolutePath() + File.separator
                + "results" + File.separator
        );
        caseStudyGroupRecommendation.setResultsDirectory(resultsDirectory);
        try {
            Global.showln("Executing case " + caseName);
            caseStudyGroupRecommendation.execute();
        } catch (UserNotFound | ItemNotFound ex) {
            throw new IllegalStateException(ex);
        }

        Global.showMessageTimestamped("Saving results of " + resultsDirectory);
        GroupCaseStudyXML.saveCaseResults(caseStudyGroupRecommendation, resultsDirectory);
        Global.showMessageTimestamped("Saved XML results of " + resultsDirectory);

        GroupCaseStudyExcel.saveCaseResults(caseStudyGroupRecommendation, resultsDirectory);
        Global.showMessageTimestamped("Saved XLS results of " + resultsDirectory);

    }

    @Override
    public void accept(ExecuteGroupCaseStudy_Task task) {
        try {
            executeCaseStudy(
                    task.getExperimentsDirectory(),
                    task.getCaseName(),
                    task.getCaseStudyConfiguration(),
                    task.getDatasetLoader(),
                    task.getGroupEvaluationMeasures(),
                    task.getNumExecutions(),
                    task.getSeed());
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (JDOMException ex) {
            ERROR_CODES.CANNOT_LOAD_CONFIG_FILE.exit(ex);
        } catch (IOException ex) {
            ERROR_CODES.UNDEFINED_ERROR.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }

}
