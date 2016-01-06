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
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import org.junit.Test;

public class RandomGRS_testOutOfMemoryError extends DelfosTest {

    public RandomGRS_testOutOfMemoryError() {
    }

    public static final long SEED_VALUE = 123456L;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "HesitantGRS.experiment4outofmemory" + File.separator
            + RandomGRS_testOutOfMemoryError.class.getSimpleName() + File.separator);

    private List<Integer> getGroupSizes() {
        return Arrays.asList(1, 2);
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
        return Arrays.asList(new ConfiguredDatasetLoader("ml-100k"));
    }

    private List<GroupRecommenderSystem> getGRSs() {
        return Arrays.asList(new RandomGroupRecommender());
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
        FileUtilities.deleteDirectoryRecursive(Constants.getTempDirectory());

        createCaseStudyXML();
        Global.show("This case study has " + new TuringPreparator()
                .sizeOfAllExperimentsInDirectory(experimentDirectory)
                + " experiments");

        new TuringPreparator().executeAllExperimentsInDirectory_withSeed(experimentDirectory, 1, 123456);
    }
}
