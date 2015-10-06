package delfos.group.casestudy.definedcases.hesitant;

import delfos.Constants;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.hesitant.HesitantKnnGroupUser;
import es.jcastro.hesitant.similarity.factory.HesitantSimilarityFactory;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;

public class HesitantGRS_CaseStudy extends DelfosTest {

    public HesitantGRS_CaseStudy() {
    }

    public static final long SEED_VALUE = 77352653L;
    public static final int NUM_GROUPS = 90;

    File experimentDirectory = new File(Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + "HesitantGRS" + File.separator);

    private Collection<GroupFormationTechnique> getGroupFormationTechnique() {
        return Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .stream()
                .map((groupSize) -> {
                    GroupFormationTechnique gft = new FixedGroupSize_OnlyNGroups(NUM_GROUPS, groupSize);
                    gft.setSeedValue(SEED_VALUE);
                    return gft;
                }).collect(Collectors.toList());

    }

    private Collection<ConfiguredDatasetLoader> getDatasetLoader() {
        if (1 == 1) {
            return Arrays.asList(
                    new ConfiguredDatasetLoader("ml-100k"),
                    new ConfiguredDatasetLoader("ml-1m"));
        }

        return ConfiguredDatasetsFactory.getInstance()
                .keySet()
                .stream()
                .map((datasetName) -> {
                    return new ConfiguredDatasetLoader(datasetName);
                })
                .collect(Collectors.toList());

    }

    private List<GroupRecommenderSystem> getGRSs() {
        return HesitantSimilarityFactory.getAll()
                .stream()
                .map((hesitantSimilarity) -> {
                    HesitantKnnGroupUser grs = new HesitantKnnGroupUser();
                    grs.setAlias(hesitantSimilarity.getName());
                    grs.setParameterValue(HesitantKnnGroupUser.HESITANT_SIMILARITY_MEASURE, hesitantSimilarity);
                    return grs;
                })
                .collect(Collectors.toList());
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
                        new HoldOutGroupRatedItems(SEED_VALUE),
                        new NoPredictionProtocol(SEED_VALUE),
                        GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                        new RelevanceCriteria(4), 1);

                groupCaseStudy.setAlias(groupRecommenderSystem.getAlias());
                groupCaseStudy.setSeedValue(SEED_VALUE);
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
        createCaseStudyXML();

        executeAllExperimentsInDirectory(experimentDirectory);
    }

    private void executeAllExperimentsInDirectory(File directory) {
        new TuringPreparator().executeAllExperimentsInDirectory(directory, 1);
    }
}
