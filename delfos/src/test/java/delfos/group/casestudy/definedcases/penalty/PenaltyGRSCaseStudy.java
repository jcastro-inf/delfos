package delfos.group.casestudy.definedcases.penalty;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;
import delfos.common.FileUtilities;
import delfos.common.aggregationoperators.MaximumValue;
import delfos.common.aggregationoperators.Mean;
import delfos.common.aggregationoperators.Median;
import delfos.common.aggregationoperators.MinimumValue;
import delfos.common.aggregationoperators.RMSMean;
import delfos.common.aggregationoperators.penalty.functions.NoPenalty;
import delfos.common.aggregationoperators.penalty.functions.PenaltyFunction;
import delfos.common.aggregationoperators.penalty.functions.PenaltyWholeMatrix;
import delfos.configureddatasets.ConfiguredDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.group.casestudy.GroupCaseStudy;
import delfos.group.casestudy.defaultcase.DefaultGroupCaseStudy;
import delfos.group.experiment.validation.groupformation.FixedGroupSize_OnlyNGroups;
import delfos.group.experiment.validation.groupformation.GroupFormationTechnique;
import delfos.group.experiment.validation.predictionvalidation.GroupPredictionProtocol;
import delfos.group.experiment.validation.predictionvalidation.NoPredictionProtocol;
import delfos.group.experiment.validation.validationtechniques.GroupValidationTechnique;
import delfos.group.experiment.validation.validationtechniques.HoldOutGroupRatedItems;
import delfos.group.factories.GroupEvaluationMeasuresFactory;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.group.grs.penalty.PenaltyGRS_Ratings;
import delfos.group.grs.penalty.grouper.Grouper;
import delfos.group.grs.penalty.grouper.GrouperByDataClustering;
import delfos.group.grs.penalty.grouper.GrouperByIdItem;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.rs.RecommenderSystem;
import delfos.rs.bufferedrecommenders.RecommenderSystem_fixedFilePersistence;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.collaborativefiltering.knn.modelbased.nwr.KnnModelBased_NWR;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.persistence.FilePersistence;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import delfos.similaritymeasures.CosineCoefficient;

public class PenaltyGRSCaseStudy {

    public File cleanCaseStudyDirectories() {
        String directoryName = "experiments" + File.separator
                + this.getClass().getSimpleName() + File.separator;
        File directory = new File(directoryName);
        if (directory.exists()) {
            FileUtilities.deleteDirectoryRecursive(directory);
        }
        FileUtilities.createDirectoryPath(directory);
        return directory;
    }

    @Test
    public void generateCaseXML() {

        File directory = cleanCaseStudyDirectories();

        List<GroupCaseStudy> groupCaseStudies = createPenaltyGroupCaseStudies();

        TuringPreparator turingPreparator = new TuringPreparator();

        turingPreparator.prepareGroupExperiment(
                directory,
                groupCaseStudies,
                new ConfiguredDatasetLoader("ml-100k")
        );

        //TuringPreparator.executeAllExperimentsInDirectory(directory);
    }

    public List<GroupCaseStudy> createPenaltyGroupCaseStudies() {
        int numGroups = 100;
        final int[] groupSizeArray = {9};
        final DatasetLoader<? extends Rating> datasetLoaderDummy = new RandomDatasetLoader();
        final int numEjecuciones = 1;
        final Collection<GroupEvaluationMeasure> evaluationMeasures = GroupEvaluationMeasuresFactory.getInstance().getAllClasses();
        final RelevanceCriteria criteria = new RelevanceCriteria(4);
        final GroupPredictionProtocol groupPredictionProtocol = new NoPredictionProtocol();
        final GroupValidationTechnique groupValidationTechniqueValue = new HoldOutGroupRatedItems();
        List<RecommenderSystem> coreRSs = getCoreRSs();
        List<GroupCaseStudy> groupCaseStudies = new ArrayList<>();
        for (int groupSize : groupSizeArray) {
            final GroupFormationTechnique groupFormationTechnique = new FixedGroupSize_OnlyNGroups(numGroups, groupSize);

            for (RecommenderSystem coreRS : coreRSs) {
                for (GroupRecommenderSystem groupRecommenderSystem : getGRS(coreRS)) {
                    GroupCaseStudy groupCaseStudy = new DefaultGroupCaseStudy(
                            datasetLoaderDummy,
                            groupRecommenderSystem,
                            groupFormationTechnique,
                            groupValidationTechniqueValue, groupPredictionProtocol,
                            evaluationMeasures,
                            criteria,
                            numEjecuciones);
                    groupCaseStudy.setAlias(groupRecommenderSystem.getAlias()
                            + "_" + coreRS.getAlias()
                            + "_groupSize-" + groupSize);

                    groupCaseStudies.add(groupCaseStudy);
                }
            }
        }
        return groupCaseStudies;
    }

    private List<RecommenderSystem> getCoreRSs() {
        List<RecommenderSystem> coreRSs = new ArrayList<>();

        coreRSs.add(getKnnMemoryBasedNWR());

        return coreRSs;
    }

    private KnnMemoryBasedNWR getKnnMemoryBasedNWR() {
        KnnMemoryBasedNWR knnMemoryBasedNWR = new KnnMemoryBasedNWR();
        knnMemoryBasedNWR.setAlias("KnnUser");
        return knnMemoryBasedNWR;
    }

    private RecommenderSystem getKnnModelNWR_fixedRecommendationModel() {
        KnnModelBased_NWR knnModelBased_NWR = new KnnModelBased_NWR();
        File recommendationModelDirectory = new File("knn-model-recommendation-model" + File.separator);
        FilePersistence filePersistence = new FilePersistence("knn-model-recommendation-model", "dat", recommendationModelDirectory);
        RecommenderSystem rsFixedModel = new RecommenderSystem_fixedFilePersistence(knnModelBased_NWR, filePersistence);
        rsFixedModel.setAlias("KnnItem");
        return rsFixedModel;
    }

    private RecommenderSystem getSVDFoldingIn_fixedRecommendationModel() {
        SVDFoldingIn sVDFoldingIn = new SVDFoldingIn();
        sVDFoldingIn.setSeedValue(987654321);
        File recommendationModelDirectory = new File("test-temp" + File.separator + "svd-folding-in-model" + File.separator);
        FilePersistence filePersistence = new FilePersistence("svd-folding-in-model", "dat", recommendationModelDirectory);
        RecommenderSystem rsFixedModel = new RecommenderSystem_fixedFilePersistence(sVDFoldingIn, filePersistence);
        rsFixedModel.setAlias("SVD");
        return rsFixedModel;
    }

    private static Iterable<GroupRecommenderSystem<? extends Object, ? extends Object>> getGRS(RecommenderSystem coreRS) {
        LinkedList<GroupRecommenderSystem<? extends Object, ? extends Object>> grsList = new LinkedList<>();
        List<PenaltyFunction> penaltyFunctions = getPenaltyFunctions();

        Number[] completeThresholds = {null, 0, 1, 2, 3, 4, 5};

        for (BasicSimilarityMeasure similarityForClustering : getSimilaritiesForClustering()) {
            for (Grouper grouper : getGroupers(similarityForClustering)) {
                for (PenaltyFunction penaltyFunction : penaltyFunctions) {
                    for (Number completeThreshold : completeThresholds) {
                        PenaltyGRS_Ratings penaltyGRS_Ratings = new PenaltyGRS_Ratings(coreRS, penaltyFunction, grouper, completeThreshold);
                        grsList.add(penaltyGRS_Ratings);
                    }
                }

            }
        }

        return grsList;
    }

    private static List<PenaltyFunction> getPenaltyFunctions() {
        List<PenaltyFunction> penaltyFunctions = new ArrayList<>();
        penaltyFunctions.add(new NoPenalty(new Median()));
        penaltyFunctions.add(new NoPenalty(new Mean()));
        penaltyFunctions.add(new NoPenalty(new RMSMean()));
        penaltyFunctions.add(new NoPenalty(new MaximumValue()));
        penaltyFunctions.add(new NoPenalty(new MinimumValue()));

        penaltyFunctions.add(new PenaltyWholeMatrix(1, 3));

        return penaltyFunctions;
    }

    private static LinkedList<BasicSimilarityMeasure> getSimilaritiesForClustering() {
        LinkedList<BasicSimilarityMeasure> similaritiesForClustering = new LinkedList<>();
        similaritiesForClustering.add(new CosineCoefficient());
        return similaritiesForClustering;
    }

    private static Iterable<Grouper> getGroupers(BasicSimilarityMeasure similarityForClustering) {

        ArrayList<Grouper> groupers = new ArrayList<>();

        int[] numsItems = {7};
        int[] numsClusters = {50};
        int[] numsRounds = {10};

        for (int numItems : numsItems) {

            groupers.add(new GrouperByIdItem(numItems));

            for (int numClusters : numsClusters) {
                for (int numRounds : numsRounds) {
                    groupers.add(new GrouperByDataClustering(
                            numItems,
                            numClusters,
                            numRounds,
                            2, similarityForClustering
                    ));
                }
            }
        }

        return groupers;
    }

}
