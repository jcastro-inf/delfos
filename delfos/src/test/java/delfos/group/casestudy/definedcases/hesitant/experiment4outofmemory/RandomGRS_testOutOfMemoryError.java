package delfos.group.casestudy.definedcases.hesitant.experiment4outofmemory;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.DissimilarMembers;
import delfos.group.experiment.validation.groupformation.FixedGroupSize;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.groupformation.SimilarMembers;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.CrossFoldValidation_groupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.RandomGroupRecommender;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import delfos.utils.hesitant.similarity.HesitantPearson;
import delfos.utils.hesitant.similarity.factory.HesitantSimilarityFactory;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class RandomGRS_testOutOfMemoryError extends DelfosTest {

    public RandomGRS_testOutOfMemoryError() {
    }

    public static final long SEED_VALUE = 123456L;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "HesitantGRS.experiment4outofmemory" + File.separator
            + RandomGRS_testOutOfMemoryError.class.getSimpleName() + File.separator);

    private List<Integer> getGroupSizes() {
        return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100, 200, 500);
    }

    private Collection<GroupFormationTechnique> getGroupFormationTechnique() {
        final List<Integer> groupSizes = getGroupSizes();

        List<GroupFormationTechnique> ret = new ArrayList<>();

        groupSizes.stream().forEach((groupSize) -> {
            ret.add(new SimilarMembers(groupSize));
            ret.add(new DissimilarMembers(groupSize));
            ret.add(new FixedGroupSize(groupSize));
        });

        return ret;
    }

    private Collection<ConfiguredDatasetLoader> getDatasetLoader() {
        return Arrays.asList(
                new ConfiguredDatasetLoader("ml-100k"),
                new ConfiguredDatasetLoader("ml-1m")
        );
    }

    private List<GroupRecommenderSystem> getGRSs() {
        int neighborhoodSize = 100;

        List<GroupRecommenderSystem> ret = new ArrayList<>();

        ret.addAll(HesitantSimilarityFactory.getAll()
                .stream()
                .map((hesitantSimilarity) -> {
                    HesitantKnnGroupUser grs = new HesitantKnnGroupUser();
                    grs.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
                    grs.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
                    return grs;

                }).collect(Collectors.toList()));

        {

            HesitantPearson hesitantSimilarity = new HesitantPearson();
            NumberFormat format = new DecimalFormat("000");
            HesitantKnnGroupUser hesitantGRS = new HesitantKnnGroupUser();

            hesitantGRS.setAlias(hesitantSimilarity.getName() + "_deleteRepeated" + "_neighborhoodSize=" + format.format(neighborhoodSize));
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
            hesitantGRS.setParameterValue(HesitantKnnGroupUser.DELETE_REPEATED, true);

            ret.add(hesitantGRS);
        }

        {

            RandomGroupRecommender hesitantGRS = new RandomGroupRecommender();
            ret.add(hesitantGRS);
        }

        return ret;
    }

    public void createCaseStudyXML() {

        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupFormationTechnique groupFormationTechnique : getGroupFormationTechnique()) {
            for (GroupRecommenderSystem groupRecommenderSystem : getGRSs()) {
                GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                        null,
                        groupRecommenderSystem,
                        groupFormationTechnique,
                        new CrossFoldValidation_groupRatedItems(),
                        new NoPredictionProtocol(),
                        GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                        new RelevanceCriteria(4),
                        1,
                        SEED_VALUE
                );

                groupCaseStudys.add(groupCaseStudy);
            }
        }

        turingPreparator.renameCaseStudyWithTheMinimumDistinctAlias(groupCaseStudys);

        turingPreparator.prepareGroupExperiment(
                experimentDirectory,
                groupCaseStudys,
                getDatasetLoader().toArray(new DatasetLoader[0]));
    }

    @Test
    public void testExecute() throws Exception {
        FileUtilities.deleteDirectoryRecursive(experimentDirectory);
        createCaseStudyXML();
        Global.show("This case study has " + new TuringPreparator()
                .sizeOfAllExperimentsInDirectory(experimentDirectory)
                + " experiments");
    }
}
