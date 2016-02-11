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
import delfos.common.parallelwork.SingleTaskExecute;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.ExperimentListerner_default;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.CaseStudyConfiguration;
import delfos.experiment.casestudy.defaultcase.DefaultCaseStudy;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.io.excel.casestudy.CaseStudyExcel;
import delfos.io.xml.casestudy.CaseStudyXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.RecommenderSystem;
import java.io.File;
import java.util.Collection;

/**
 *
 * @author Jorge Castro Gallardo
 *
 * @version 27-ene-2014
 */
public class CaseStudy_SingleTaskExecute implements SingleTaskExecute<ExecuteCaseStudy_Task> {

    private void executeCaseStudy(
            File experimentsDirectory,
            String caseName,
            CaseStudyConfiguration caseStudyConfiguration,
            DatasetLoader<? extends Rating> datasetLoader,
            RelevanceCriteria relevanceCriteria,
            Collection<EvaluationMeasure> evaluationMeasures,
            int numExecutions,
            long seed)
            throws CannotLoadContentDataset, CannotLoadRatingsDataset {

        RecommenderSystem<Object> recommenderSystem = (RecommenderSystem<Object>) caseStudyConfiguration.getRecommenderSystem();

        PredictionProtocol predictionProtocol = caseStudyConfiguration.getPredictionProtocol();
        ValidationTechnique validationTechnique = caseStudyConfiguration.getValidationTechnique();

        CaseStudy caseStudyRecommendation = new DefaultCaseStudy(
                recommenderSystem,
                datasetLoader,
                validationTechnique,
                predictionProtocol,
                relevanceCriteria,
                evaluationMeasures,
                numExecutions
        );

        String threadName = Thread.currentThread().getName();
        String newThreadName = threadName + "_" + caseStudyRecommendation.getAlias();
        Thread.currentThread().setName(newThreadName);

        caseStudyRecommendation.addExperimentListener(new ExperimentListerner_default(System.out, 10000));
        caseStudyRecommendation.setSeedValue(seed);

        caseStudyRecommendation.execute();

        File fileToSaveResults = new File(caseName);

        fileToSaveResults = FileUtilities.addPrefix(
                fileToSaveResults,
                experimentsDirectory + File.separator + "results" + File.separator);

        File excelFile = FileUtilities.changeExtension(fileToSaveResults, "xls");
        File xmlFile = FileUtilities.changeExtension(fileToSaveResults, "xml");

        CaseStudyXML.saveCaseResults(caseStudyRecommendation, "", xmlFile.getAbsolutePath());
        CaseStudyExcel.saveCaseResults(caseStudyRecommendation, excelFile);

    }

    @Override
    public void executeSingleTask(ExecuteCaseStudy_Task task) {
        try {
            executeCaseStudy(
                    task.getExperimentsDirectory(),
                    task.getCaseName(),
                    task.getCaseStudyConfiguration(),
                    task.getDatasetLoader(),
                    task.getRelevanceCriteria(),
                    task.getEvaluationMeasures(),
                    task.getNumExecutions(),
                    task.getSeed());
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        }
    }

}
