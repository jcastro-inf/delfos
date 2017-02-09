package delfos.main.managers.experiment.join.xml;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.aggregationoperators.MaximumValue;
import delfos.common.aggregationoperators.Mean;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class XMLJoinTest {

    private final static File experimentDirectory = new File(
            Constants.getTempDirectory().getAbsolutePath() + File.separator
            + XMLJoinTest.class.getSimpleName() + File.separator
            + "GroupCaseStudy" + File.separator);

    public void createCaseStudyXML() {

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        List<GroupFormationTechnique> groupFormationTechniques
                = Arrays.asList(1, 2, 3).stream()
                .map((groupSize -> new FixedGroupSize_OnlyNGroups(10, groupSize)))
                .collect(Collectors.toList());

        DatasetLoader ml100k = new ConfiguredDatasetLoader("ml-100k");

        List<GroupRecommenderSystem> groupRecommenderSystems = Arrays.asList(
                new AggregationOfIndividualRatings(new KnnMemoryBasedCFRS(), new Mean()),
                new AggregationOfIndividualRatings(new KnnMemoryBasedCFRS(), new MaximumValue()),
                new AggregationOfIndividualRecommendations(new KnnMemoryBasedCFRS(), new MaximumValue()),
                new AggregationOfIndividualRecommendations(new KnnMemoryBasedCFRS(), new Mean())
        );

        for (GroupFormationTechnique groupFormationTechnique : groupFormationTechniques) {
            for (GroupRecommenderSystem groupRecommenderSystem : groupRecommenderSystems) {
                GroupCaseStudy groupCaseStudy = new GroupCaseStudy()
                        .setGroupRecommenderSystem(groupRecommenderSystem)
                        .setGroupFormationTechnique(groupFormationTechnique)
                        .setGroupPredictionProtocol(new NoPredictionProtocol())
                        .setValidationTechnique(new HoldOut_Ratings())
                        .setNumExecutions(1);

                groupCaseStudy.setSeedValue(123456L);

                groupCaseStudy.setAlias(
                        "_dataValidation=" + groupCaseStudy.hashDataValidation()
                        + "_technique=" + groupCaseStudy.hashTechnique()
                        + "_" + groupRecommenderSystem.getAlias()
                        + "_allHash=" + groupCaseStudy.hashCode()
                );
                groupCaseStudys.add(groupCaseStudy);
            }
        }

        new TuringPreparator(true).prepareGroupExperiment(experimentDirectory,
                groupCaseStudys,
                Arrays.asList(ml100k).toArray(new DatasetLoader[0]));
    }

    @Test
    public void testXMLJoinerWithASimpleExperiment_groupRecommendation() {

        //Data preparation
        FileUtilities.deleteDirectoryRecursive(experimentDirectory);
        if (!experimentDirectory.exists()) {
            createCaseStudyXML();
            new TuringPreparator(true).executeAllExperimentsInDirectory(experimentDirectory, 1);
        }

        Set<String> filterMeasures = Collections.EMPTY_SET;

        //Execution of the joiner
        File outputFile = new File(experimentDirectory.getPath() + File.separator + "xml-join-test.xls");
        XMLJoin.mergeResultsIntoOutput(Arrays.asList(experimentDirectory.getPath()), outputFile, filterMeasures);

        //Check the results correctness
        Assert.assertTrue("The output file does not exists", outputFile.exists());
    }
}
