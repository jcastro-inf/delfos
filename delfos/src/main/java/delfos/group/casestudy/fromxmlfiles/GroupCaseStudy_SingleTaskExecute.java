package delfos.group.casestudy.fromxmlfiles;

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.ExecutionProgressListener_default;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.GroupCaseStudyConfiguration;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.io.excel.casestudy.GroupCaseStudyExcel;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.jdom2.JDOMException;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 27-ene-2014
 */
public class GroupCaseStudy_SingleTaskExecute implements SingleTaskExecute<ExecuteGroupCaseStudy_Task> {

    private void executeCaseStudy(
            File experimentsDirectory,
            String caseName,
            GroupCaseStudyConfiguration caseStudyConfiguration,
            DatasetLoader<? extends Rating> datasetLoader,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            int numExecutions,
            long seed)
            throws CannotLoadContentDataset, JDOMException, IOException, CannotLoadRatingsDataset {

        GroupRecommenderSystem<Object, Object> groupRecommenderSystem = caseStudyConfiguration.getGroupRecommenderSystem();

        GroupFormationTechnique groupFormationTechnique = caseStudyConfiguration.getGroupFormationTechnique();
        GroupPredictionProtocol groupPredictionProtocol = caseStudyConfiguration.getGroupPredictionProtocol();
        GroupValidationTechnique groupValidationTechnique = caseStudyConfiguration.getGroupValidationTechnique();

        RelevanceCriteria relevanceCriteria = caseStudyConfiguration.getRelevanceCriteria();

        GroupCaseStudy caseStudyGroupRecommendation = new DefaultGroupCaseStudy(
                datasetLoader,
                groupRecommenderSystem,
                groupFormationTechnique,
                groupValidationTechnique, groupPredictionProtocol,
                groupEvaluationMeasures,
                relevanceCriteria,
                numExecutions);

        String threadName = Thread.currentThread().getName();
        Thread.currentThread().setName(threadName + "_" + caseStudyGroupRecommendation.getAlias());

        caseStudyGroupRecommendation.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
        caseStudyGroupRecommendation.addExecutionProgressListener(new ExecutionProgressListener_default(System.out, 10000));
        caseStudyGroupRecommendation.setSeedValue(seed);
        try {
            Global.showln("Executing case " + caseName);
            caseStudyGroupRecommendation.execute();
        } catch (UserNotFound | ItemNotFound ex) {
            throw new IllegalStateException(ex);
        }

        File fileToSaveResults = new File(
                experimentsDirectory.getAbsolutePath() + File.separator
                + "results" + File.separator
                + caseName);

        File excelFile = FileUtilities.changeExtension(fileToSaveResults, "xls");
        File xmlFile = FileUtilities.changeExtension(fileToSaveResults, "xml");

        GroupCaseStudyXML.saveCaseResults(caseStudyGroupRecommendation, "", xmlFile.getAbsolutePath());
        GroupCaseStudyExcel.saveCaseResults(caseStudyGroupRecommendation, excelFile);

    }

    @Override
    public void executeSingleTask(ExecuteGroupCaseStudy_Task task) {
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
