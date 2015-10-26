package delfos.group.casestudy.definedcases.hesitant.experiment1;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.Global;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import es.jcastro.hesitant.similarity.HesitantPearson;
import es.jcastro.hesitant.similarity.HesitantSimilarity;
import es.jcastro.hesitant.similarity.factory.HesitantSimilarityFactory;
import java.io.File;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class HesitantGRS_3_CaseStudy_FixedGroupSize extends DelfosTest {

    public static final long SEED_VALUE = 123456L;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + "3-HesitantGRS-allGroups" + File.separator);

    private Collection<GroupFormationTechnique> getGroupFormationTechnique() {
        return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 15, 20, 25, 50, 100, 200, 500).stream()
                .map((groupSize) -> {
                    return new FixedGroupSize(groupSize);
                }).collect(Collectors.toList());
    }

    private Collection<ConfiguredDatasetLoader> getDatasetLoader() {
        return Arrays.asList(new ConfiguredDatasetLoader("ml-100k"));
    }

    private List<GroupRecommenderSystem> getGRSs() {
        final List<Integer> neighborsTried = Arrays.asList(10, 20, 30, 40, 50, 100, 150, 200, 500);

        List<GroupRecommenderSystem> ret = new ArrayList<>();

        List<List<HesitantKnnGroupUser>> lists = HesitantSimilarityFactory.getAll()
                .stream()
                .map((hesitantSimilarity) -> {
                    return neighborsTried.stream()
                    .map((neighborhoodSize)
                            -> {
                        DecimalFormat format = new DecimalFormat("000");
                        HesitantKnnGroupUser grs = new HesitantKnnGroupUser();
                        grs.setAlias(hesitantSimilarity.getName() + "_neighborhoodSize=" + format.format(neighborhoodSize));
                        grs.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
                        grs.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
                        return grs;
                    })
                    .collect(Collectors.toList());
                }).collect(Collectors.toList());

        lists.stream().forEach((list) -> {
            ret.addAll(list);
        });
        {
            HesitantSimilarity hesitantSimilarity = new HesitantPearson();
            List<HesitantKnnGroupUser> collect = neighborsTried.stream()
                    .map((neighborhoodSize)
                            -> {
                        NumberFormat format = new DecimalFormat("000");
                        HesitantKnnGroupUser grs = new HesitantKnnGroupUser();

                        grs.setAlias(hesitantSimilarity.getName() + "_deleteRepeated" + "_neighborhoodSize=" + format.format(neighborhoodSize));
                        grs.setParameterValue(HesitantKnnGroupUser.NEIGHBORHOOD_SIZE, neighborhoodSize);
                        grs.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
                        grs.setParameterValue(HesitantKnnGroupUser.DELETE_REPEATED, true);
                        return grs;
                    }).collect(Collectors.toList());
            ret.addAll(collect);

        }
        return ret;
    }

    public void createCaseStudyXML() {

        TuringPreparator turingPreparator = new TuringPreparator();

        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupFormationTechnique groupFormationTechnique : getGroupFormationTechnique()) {
            for (GroupRecommenderSystem groupRecommenderSystem : getGRSs()) {
                DefaultGroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(
                        null,
                        groupRecommenderSystem,
                        groupFormationTechnique,
                        new HoldOutGroupRatedItems(),
                        new NoPredictionProtocol(),
                        GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                        new RelevanceCriteria(4),
                        1,
                        SEED_VALUE
                );

                groupCaseStudy.setAlias(
                        "methodHash=" + groupCaseStudy.hashCodeWithoutGroupRecommenderSystem()
                        + "_" + groupRecommenderSystem.getAlias()
                        + "_allHash=" + groupCaseStudy.hashCode()
                );

                groupCaseStudys.add(groupCaseStudy);
            }
        }

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
