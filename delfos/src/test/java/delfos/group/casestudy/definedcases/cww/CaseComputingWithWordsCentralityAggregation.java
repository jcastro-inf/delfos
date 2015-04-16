package delfos.group.casestudy.definedcases.cww;

import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import org.junit.Test;
import delfos.common.FileUtilities;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.cww.CentralityWeightedAggregationGRS;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;

/**
 * Crea los experimentos del congreso FLINS 2014.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 22-ene-2014
 */
public class CaseComputingWithWordsCentralityAggregation {

    @Test
    public void generateCaseXML() {

        String folderName = "experiments" + File.separator + "grs-cww-centrality" + File.separator;
        File datasetFolder = new File(folderName + File.separator + "dataset");
        File folder = new File(folderName);

        FileUtilities.deleteDirectoryRecursive(folder);
        FileUtilities.createDirectoryPath(datasetFolder);

        final int numGroups = 9;
        final int[] groupSizeArray = {5, 10, 15, 20, 30, 40, 50, 100};

        final DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader();
        final int numEjecuciones = 1;
        final Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final GroupPredictionProtocol groupPredictionProtocol = new NoPredictionProtocol();
        final GroupValidationTechnique groupValidationTechniqueValue = new HoldOutGroupRatedItems();

        for (int groupSize : groupSizeArray) {

            final GroupFormationTechnique groupFormationTechnique = new FixedGroupSize_OnlyNGroups(numGroups, groupSize);

            for (GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem : getGRS()) {
                GroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(datasetLoader, groupRecommenderSystem, groupFormationTechnique, groupValidationTechniqueValue, groupPredictionProtocol, evaluationMeasures, criteria, numEjecuciones);
                String fileName = groupRecommenderSystem.getAlias() + "_group-" + groupSize + ".xml";
                File file = new File(folder + File.separator + fileName);
                GroupCaseStudyXML.saveCaseDescription(groupCaseStudy, file.getAbsolutePath());
            }
        }
    }

    private static Iterable<GroupRecommenderSystem<? extends Object, ? extends Object>> getGRS() {
        LinkedList<GroupRecommenderSystem<? extends Object, ? extends Object>> grsList = new LinkedList<>();

        final boolean[] normaliseArray = {true, false};
        final double[] strongDefinitionArray = {0, 0.2, 0.4, 0.6, 0.8, 1};

        int i = 0;

        grsList.add(new AggregationOfIndividualRatings(new KnnMemoryBasedNWR()));
        grsList.getLast().setAlias("0" + i++ + "_" + "MeanAggregationGRS");

        for (boolean normalise : normaliseArray) {
            for (int index = 0; index < strongDefinitionArray.length - 1; index++) {
                double aStrong = strongDefinitionArray[index];
                double bStrong = strongDefinitionArray[index + 1];

                grsList.add(new CentralityWeightedAggregationGRS(new KnnMemoryBasedNWR(), true, aStrong, bStrong, normalise));
                grsList.getLast().setAlias("0" + i++ + "_CentralityAggrGRS_norm=(" + normalise + "_ST(" + aStrong + "," + bStrong + ")");

            }
        }

        return grsList;
    }
}
