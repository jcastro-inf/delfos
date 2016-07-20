package delfos.group.casestudy.definedcases.cww;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.cww.CentralityWeightedAggregationGRS;
import delfos.group.io.xml.casestudy.GroupCaseStudyXML;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import java.io.File;
import java.util.Collection;
import java.util.LinkedList;
import org.junit.Test;

/**
 * Crea los experimentos del congreso FLINS 2014.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 22-ene-2014
 */
public class CaseComputingWithWordsCentralityAggregation {

    @Test
    public void generateCaseXML() {

        String directoryName
                = Constants.getTempDirectory().getAbsolutePath() + File.separator
                + "experiments" + File.separator
                + "grs-cww-centrality" + File.separator;

        File datasetDirectory = new File(directoryName + File.separator + "dataset");
        File directory = new File(directoryName);

        FileUtilities.deleteDirectoryRecursive(directory);
        FileUtilities.createDirectoryPath(datasetDirectory);

        final int numGroups = 9;
        final int[] groupSizeArray = {5, 10, 15, 20, 30, 40, 50, 100};

        final DatasetLoader<? extends Rating> datasetLoader = new RandomDatasetLoader();
        final int numEjecuciones = 1;
        final Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final GroupPredictionProtocol groupPredictionProtocol = new NoPredictionProtocol();
        final ValidationTechnique validationTechniqueValue = new HoldOut_Ratings();

        for (int groupSize : groupSizeArray) {

            final GroupFormationTechnique groupFormationTechnique = new FixedGroupSize_OnlyNGroups(numGroups, groupSize);

            for (GroupRecommenderSystem<? extends Object, ? extends Object> groupRecommenderSystem : getGRS()) {
                GroupCaseStudy groupCaseStudy = new GroupCaseStudy(datasetLoader, groupRecommenderSystem, groupFormationTechnique, validationTechniqueValue, groupPredictionProtocol, evaluationMeasures, criteria, numEjecuciones);
                String fileName = groupRecommenderSystem.getAlias() + "_group-" + groupSize + ".xml";
                File file = new File(directory + File.separator + fileName);
                GroupCaseStudyXML.caseStudyToXMLFile_onlyDescription(groupCaseStudy, file);
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
