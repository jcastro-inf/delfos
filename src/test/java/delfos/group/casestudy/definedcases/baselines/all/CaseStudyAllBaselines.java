package delfos.group.casestudy.definedcases.baselines.all;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.GeometricMean;
import delfos.common.aggregationoperators.HarmonicMean;
import delfos.common.aggregationoperators.MaximumValue;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.Median;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.aggregationoperators.RMSMean;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.validation.validationtechnique.HoldOut_Ratings;
import delfos.group.casestudy.defaultcase.GroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.aggregation.AggregationOfIndividualRatings;
import delfos.group.grs.aggregation.AggregationOfIndividualRecommendations;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.knn.modelbased.nwr.KnnModelBased_NWR;
import delfos.rs.collaborativefiltering.predictiontechniques.WeightedSum;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.persistence.FilePersistence;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;

/**
 * Caso de estudio que ejecuta y recopila todas las técnicas consideradas
 * baseline para grupos.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 28-enero-2015
 */
public class CaseStudyAllBaselines {

    public CaseStudyAllBaselines() {
    }

    public static final String EXPERIMENT_DIRECTORY
            = Constants.getTempDirectory().getAbsolutePath() + File.separator
            + "experiments" + File.separator
            + CaseStudyAllBaselines.class.getSimpleName() + File.separator;

    public static long SEED_VALUE = 123456;

    public static final Collection<Integer> GROUPS_SIZES = Arrays.asList(5, 10, 15);
    public static final int NUM_GROUPS = 943 / 15;

    public static final int NUMBER_OF_EXECUTIONS = 20;

    @Before
    public void before() {
        final File experimentDirectory = new File(EXPERIMENT_DIRECTORY);

        FileUtilities.deleteDirectoryRecursive(experimentDirectory);
        experimentDirectory.mkdirs();
    }

    @Test
    public void createCaseStudyExperiments() throws Exception {
        List<GroupCaseStudy> allGroupCaseStudy = getAllGroupCaseStudy();

        new TuringPreparator().prepareGroupExperiment(new File(EXPERIMENT_DIRECTORY),
                allGroupCaseStudy,
                new ConfiguredDatasetLoader("ml-100k"));
    }

    private List<GroupCaseStudy> getAllGroupCaseStudy() {
        List<GroupCaseStudy> groupCaseStudys = new ArrayList<>();

        for (GroupFormationTechnique groupFormationTechnique : getGroupFormationTechnique()) {
            for (RecommenderSystem coreRS : getCoreRecommenderSystems()) {
                for (GroupRecommenderSystem groupRecommenderSystem : getGroupRecommenderSystems(coreRS)) {
                    groupCaseStudys.add(
                            getGroupCaseStudy_HoldOut_Ratings(groupRecommenderSystem, groupFormationTechnique)
                    );

                    groupCaseStudys.add(
                            getGroupCaseStudy_HoldOutGroupMemberRatings(groupRecommenderSystem, groupFormationTechnique)
                    );
                }
            }
        }

        return groupCaseStudys;
    }

    private GroupCaseStudy getGroupCaseStudy_HoldOut_Ratings(GroupRecommenderSystem groupRecommenderSystem, GroupFormationTechnique groupFormationTechnique) {
        GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                getDatasetLoader(),
                groupRecommenderSystem,
                groupFormationTechnique,
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                new RelevanceCriteria(4),
                1);
        groupCaseStudy.setAlias(HoldOut_Ratings.class.getSimpleName() + "_" + groupFormationTechnique.getAlias() + "_" + groupRecommenderSystem.getAlias());
        groupCaseStudy.setSeedValue(SEED_VALUE);
        return groupCaseStudy;
    }

    private GroupCaseStudy getGroupCaseStudy_HoldOutGroupMemberRatings(GroupRecommenderSystem groupRecommenderSystem, GroupFormationTechnique groupFormationTechnique) {
        GroupCaseStudy groupCaseStudy = new GroupCaseStudy(
                getDatasetLoader(),
                groupRecommenderSystem,
                groupFormationTechnique,
                new HoldOut_Ratings(),
                new NoPredictionProtocol(),
                GroupEvaluationMeasuresFactory.getInstance().getAllClasses(),
                new RelevanceCriteria(4),
                1);
        groupCaseStudy.setAlias(HoldOut_Ratings.class.getSimpleName() + "_" + groupFormationTechnique.getAlias() + "_" + groupRecommenderSystem.getAlias());
        groupCaseStudy.setSeedValue(SEED_VALUE);
        return groupCaseStudy;
    }

    private Collection<GroupFormationTechnique> getGroupFormationTechnique() {
        Collection<GroupFormationTechnique> groupFormationTechniques = new ArrayList<>();

        for (int sizeOfGroups : GROUPS_SIZES) {
            GroupFormationTechnique groupFormationTechnique = new FixedGroupSize_OnlyNGroups(NUM_GROUPS, sizeOfGroups);
            groupFormationTechnique.setSeedValue(SEED_VALUE);
            if (Integer.toString(sizeOfGroups).length() < 2) {
                groupFormationTechnique.setAlias("GroupSize=0" + sizeOfGroups);
            } else {
                groupFormationTechnique.setAlias("GroupSize=" + sizeOfGroups);
            }

            groupFormationTechniques.add(groupFormationTechnique);
        }

        return groupFormationTechniques;
    }

    private DatasetLoader<? extends Rating> getDatasetLoader() {
        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");
        return datasetLoader;
    }

    private List<GroupRecommenderSystem> getGroupRecommenderSystems(RecommenderSystem coreRS) {
        LinkedList<GroupRecommenderSystem> GRSs = new LinkedList<>();

        GRSs.addAll(getAgregationOfIndividualRecommendationsGRSs(coreRS));
        GRSs.addAll(getAgregationOfIndividualRatingsGRSs(coreRS));

        return GRSs;
    }

    private Collection<GroupRecommenderSystem> getAgregationOfIndividualRecommendationsGRSs(RecommenderSystem coreRS) {
        Collection<GroupRecommenderSystem> groupRecommenderSystems = new ArrayList<>();

        getAggregationOperators().stream().forEach((aggregationOperator) -> {
            GroupRecommenderSystem grs = new AggregationOfIndividualRecommendations(coreRS, aggregationOperator);
            grs.setAlias(grs.getAlias() + "_" + coreRS.getAlias());
            groupRecommenderSystems.add(grs);
        });

        return groupRecommenderSystems;
    }

    private Collection<GroupRecommenderSystem> getAgregationOfIndividualRatingsGRSs(RecommenderSystem coreRS) {
        Collection<GroupRecommenderSystem> groupRecommenderSystems = new ArrayList<>();

        getAggregationOperators().stream().forEach((aggregationOperator) -> {
            GroupRecommenderSystem grs = new AggregationOfIndividualRatings(coreRS, aggregationOperator);
            grs.setAlias(grs.getAlias() + "_" + coreRS.getAlias());
            groupRecommenderSystems.add(grs);
        });

        return groupRecommenderSystems;
    }

    private static Collection<AggregationOperator> getAggregationOperators() {

        return Arrays.asList(
                //Bound aggregations.
                new MaximumValue(),
                new MinimumValue(),
                //Averaging aggregations
                new Mean(),
                new HarmonicMean(),
                new GeometricMean(),
                new RMSMean(),
                //Other
                new Median()
        //      new Mode(),
        //      new EnsureDegreeOfFairness()
        );

    }

    private Iterable<RecommenderSystem> getCoreRecommenderSystems() {
        return Arrays.asList(
                //getSVDRecommender(),
                //getKnnItemRecommender(),
                getKnnUserRecommender()
        );
    }

    private RecommenderSystem getSVDRecommender() {

        final int numFeatures = 20;
        final int numIterPerFeature = 10;
        SVDFoldingIn svdFoldingIn = new SVDFoldingIn(numFeatures, numIterPerFeature);

        File directory = new File(
                Constants.getTempDirectory().getAbsoluteFile() + File.separator
                + "svd-folding-in-recommendation-model" + File.separator);

        RecommenderSystem_fixedFilePersistence rs
                = new RecommenderSystem_fixedFilePersistence(
                        svdFoldingIn,
                        new FilePersistence(
                                "svd-folding-in-recommendation-model",
                                "dat",
                                directory
                        )
                );
        rs.setAlias("SVD");
        return rs;
    }

    private RecommenderSystem getKnnUserRecommender() {

        KnnMemoryBasedNWR rs = new KnnMemoryBasedNWR();

        rs.setSIMILARITY_MEASURE(new PearsonCorrelationCoefficient());
        rs.setRELEVANCE_FACTOR_VALUE(30);
        rs.setDEFAULT_RATING_VALUE(null);
        rs.setINVERSE_FREQUENCY(false);
        rs.setCASE_AMPLIFICATION(1);
        rs.setNeighborhoodSize(50);
        rs.setPREDICTION_TECHNIQUE(new WeightedSum());

        rs.setAlias("KnnUser");
        return rs;
    }

    private RecommenderSystem getKnnItemRecommender() {
        KnnModelBased_NWR knnItem = new KnnModelBased_NWR();

        knnItem.setSIMILARITY_MEASURE(new PearsonCorrelationCoefficient());
        knnItem.setRELEVANCE_FACTOR_VALUE(30);
        knnItem.setNeighborhoodSize(60);
        knnItem.setPREDICTION_TECHNIQUE(new WeightedSum());

        File directory = new File(
                Constants.getTempDirectory().getAbsoluteFile() + File.separator
                + "knn-item-item-recommendation-model" + File.separator);

        RecommenderSystem_fixedFilePersistence rs
                = new RecommenderSystem_fixedFilePersistence(
                        knnItem,
                        new FilePersistence(
                                "knn-item-item-recommendation-model",
                                "dat",
                                directory)
                );

        rs.setAlias("KnnItem");
        return rs;
    }

}
