package delfos.rs.collaborativefiltering.knn.modelbased;

import delfos.common.FileUtilities;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.casestudy.cluster.TuringPreparator;
import delfos.experiment.casestudy.CaseStudy;
import delfos.experiment.validation.predictionprotocol.NoPredictionProtocol;
import delfos.experiment.validation.predictionprotocol.PredictionProtocol;
import delfos.experiment.validation.validationtechnique.CrossFoldValidation_Ratings;
import delfos.experiment.validation.validationtechnique.GivenTrainingTestInCSV;
import delfos.experiment.validation.validationtechnique.ValidationTechnique;
import delfos.factories.EvaluationMeasuresFactory;
import delfos.recommendationcandidates.AllCatalogItems;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class KnnModelBasedCFRSTest {

    private final RandomDatasetLoader datasetLoader;

    public KnnModelBasedCFRSTest() {
        datasetLoader = new RandomDatasetLoader(25, 25, 0.5);
        datasetLoader.setSeedValue(123456);
    }

    /**
     * Test of buildRecommendationModel method, of class KnnModelBasedCFRS.
     */
    @Test
    public void testBuildRecommendationModel() {
        KnnModelBasedCFRS instance = new KnnModelBasedCFRS();
        KnnModelBasedCFRSModel model = instance.buildRecommendationModel(datasetLoader);
    }

    /**
     * Test of recommendToUser method, of class KnnModelBasedCFRS.
     */
    @Test
    public void testRecommendToUser() {
        KnnModelBasedCFRS instance = new KnnModelBasedCFRS();
        KnnModelBasedCFRSModel model = instance.buildRecommendationModel(datasetLoader);

        User user = datasetLoader.getUsersDataset().get(1);
        Set<Item> candidateItems = new AllCatalogItems().candidateItems(datasetLoader, user);

        Collection<Recommendation> result = instance
                .recommendToUser(datasetLoader, model, user.getId(),
                        candidateItems.stream().map(item -> item.getId()).collect(Collectors.toSet())
                );
    }

    @Test
    public void testGetNeighborsMethodReturnsANeighborForEachItemButHimself() {

        ContentDataset contentDataset = datasetLoader.getContentDataset();

        KnnModelBasedCFRS instance = new KnnModelBasedCFRS();

        Integer relevanceFactor = instance.isRelevanceFactorApplied() ? instance.getRelevanceFactorValue() : null;

        boolean requirementViolated = contentDataset.stream().anyMatch(item -> {

            List<Neighbor> neighbors = KnnModelBasedCFRS.getNeighbors(
                    datasetLoader,
                    item, instance.getSimilarityMeasure(),
                    relevanceFactor
            );

            Set<Integer> allItems = contentDataset.parallelStream()
                    .map(itemInner -> itemInner.getId())
                    .filter(innerItem -> !innerItem.equals(item.getId()))
                    .collect(Collectors.toCollection(TreeSet::new));

            Set<Integer> itemsSimilares = neighbors.parallelStream()
                    .map(neighbor -> neighbor.getIdNeighbor())
                    .collect(Collectors.toCollection(TreeSet::new));

            return !allItems.equals(itemsSimilares);
        });

        Assert.assertFalse(
                "The method does not fulfills its requirements, "
                + "a neighbor item should be returned for each item.",
                requirementViolated);
    }

    @Test
    public void createExperimentsCourseraDataset() {
        File experimentBaseDirectory = new File("./temp/knn-item-item-rs-coursera/");
        FileUtilities.createDirectoryPathIfNotExists(experimentBaseDirectory);

        DatasetLoader<? extends Rating> rsCourseraDataset = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("rs-coursera");
        ValidationTechnique validationTechnique = new GivenTrainingTestInCSV();
        PredictionProtocol predictionProtocol = new NoPredictionProtocol();

        int[] sizes = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100};
        List<CaseStudy> caseStudys = IntStream.of(sizes).boxed()
                .flatMap(neighbourhoodSize -> {
                    return IntStream.of(sizes).boxed()
                            .filter(neighbourhoodSizeStore -> {
                                return (neighbourhoodSize <= neighbourhoodSizeStore);
                            })
                            .map(neighbourhoodSizeStore -> {

                                KnnModelBasedCFRS rs = new KnnModelBasedCFRS();

                                rs.setNeighborhoodSize(neighbourhoodSize);
                                rs.setNeighborhoodSizeStore(neighbourhoodSizeStore);

                                CaseStudy caseStudy = new CaseStudy(
                                        rs,
                                        rsCourseraDataset,
                                        validationTechnique,
                                        predictionProtocol,
                                        rsCourseraDataset.getDefaultRelevanceCriteria(),
                                        EvaluationMeasuresFactory.getInstance().getAllClasses(),
                                        1);

                                caseStudy.setAlias("caseStudy"
                                        + "-nei=" + neighbourhoodSize
                                        + "-neiStore=" + neighbourhoodSizeStore);
                                rs.setAlias("knn-item-item"
                                        + "-nei=" + neighbourhoodSize
                                        + "-neiStore=" + neighbourhoodSizeStore);

                                return caseStudy;

                            });
                })
                .collect(Collectors.toList());

        new TuringPreparator().prepareExperiment(experimentBaseDirectory, caseStudys, rsCourseraDataset);

    }

    @Test
    public void createExperimentsML100kDataset() {
        File experimentBaseDirectory = new File("./temp/knn-item-item-ml100k/");
        FileUtilities.createDirectoryPathIfNotExists(experimentBaseDirectory);

        DatasetLoader<? extends Rating> ml100k = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("rs-coursera");

        ValidationTechnique validationTechnique = new CrossFoldValidation_Ratings();
        validationTechnique.setSeedValue(0);

        PredictionProtocol predictionProtocol = new NoPredictionProtocol();

        int[] sizes = {10, 20, 30, 40, 50, 60, 70, 80, 90, 100, 250, 500, 750};
        List<CaseStudy> caseStudys = IntStream.of(sizes).boxed()
                .flatMap(neighbourhoodSize -> {
                    return IntStream.of(sizes).boxed()
                            .filter(neighbourhoodSizeStore -> {
                                return (neighbourhoodSize <= neighbourhoodSizeStore);
                            })
                            .map(neighbourhoodSizeStore -> {

                                KnnModelBasedCFRS rs = new KnnModelBasedCFRS();

                                rs.setNeighborhoodSize(neighbourhoodSize);
                                rs.setNeighborhoodSizeStore(neighbourhoodSizeStore);

                                CaseStudy caseStudy = new CaseStudy(
                                        rs,
                                        ml100k,
                                        validationTechnique,
                                        predictionProtocol,
                                        ml100k.getDefaultRelevanceCriteria(),
                                        EvaluationMeasuresFactory.getInstance().getAllClasses(),
                                        1);

                                caseStudy.setAlias("caseStudy"
                                        + "-nei=" + neighbourhoodSize
                                        + "-neiStore=" + neighbourhoodSizeStore);
                                rs.setAlias("knn-item-item"
                                        + "-nei=" + neighbourhoodSize
                                        + "-neiStore=" + neighbourhoodSizeStore);

                                return caseStudy;

                            });
                })
                .collect(Collectors.toList());

        new TuringPreparator().prepareExperiment(experimentBaseDirectory, caseStudys, ml100k);

    }
}
