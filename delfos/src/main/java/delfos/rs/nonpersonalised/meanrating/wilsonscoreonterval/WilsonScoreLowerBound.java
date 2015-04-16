package delfos.rs.nonpersonalised.meanrating.wilsonscoreonterval;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.jdom2.JDOMException;
import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.output.RecommendationsOutputFileXML;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.SingleUserRecommendations;
import delfos.stats.distributions.NormalDistribution;

/**
 * Lower bound of Wilson score confidence interval for a Bernoulli parameter.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 07-ene-2014
 */
public class WilsonScoreLowerBound extends NonPersonalisedRecommender<List<Recommendation>> {

    public WilsonScoreLowerBound() {
        super();
    }

    @Override
    public final boolean isRatingPredictorRS() {
        return false;
    }

    @Override
    public List<Recommendation> build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        final double confidence = 0.95;
        final double ratingThreshold = 4;

        final double z = NormalDistribution.z(confidence);
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(ratingThreshold);

        List<Recommendation> recommendationModel1 = new ArrayList<>(ratingsDataset.allRatedItems().size());

        for (int idItem : ratingsDataset.allRatedItems()) {
            try {
                Map<Integer, ? extends Rating> itemRatings = ratingsDataset.getItemRatingsRated(idItem);

                double numRatings = 0;
                double positiveRatings = 0;
                for (Rating rating : itemRatings.values()) {
                    numRatings++;
                    if (relevanceCriteria.isRelevant(rating)) {
                        positiveRatings++;
                    }
                }

                final double p = positiveRatings / numRatings;
                final double n = numRatings;

                double base = p + z * z / (2 * n);
                double offset = z * Math.sqrt((p * (1 - p) + z * z / (4 * n)) / n);

                double denominator = (1 + (z * z) / n);
                double preference = (base - offset) / denominator;

                if (Global.isVerboseAnnoying()) {
                    String pRounded = NumberRounder.round_str(p);
                    String preferenceRounded = NumberRounder.round_str(preference);
                    System.out.println("n=" + n + " pos=" + pRounded + "\t --> \t" + preferenceRounded);
                }

                recommendationModel1.add(new Recommendation(idItem, preference));
            } catch (ItemNotFound ex) {
                Global.showMessage("No information about item " + idItem + ", cannot build its model.");
            }

        }
        Collections.sort(recommendationModel1);

        if (Global.isVerboseAnnoying()) {
            System.out.println("================= Recommendation model for " + this.getName() + "==============");
            System.out.println(recommendationModel1);
            System.out.println("=================");

        }
        return recommendationModel1;
    }

    @Override
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, List<Recommendation> recommendationModel, Collection<Integer> idItemList) throws ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        List<Recommendation> recommendations = new ArrayList<>();

        Set<Integer> unpredicted = new TreeSet<>(idItemList);
        recommendationModel.stream()
                .filter((recommendation) -> (idItemList.contains(recommendation.getIdItem()))).map((recommendation) -> {
                    recommendations.add(new Recommendation(recommendation.getIdItem(), recommendation.getPreference()));
                    return recommendation;
                }).forEach((recommendation) -> {
                    unpredicted.remove(recommendation.getIdItem());
                });

        unpredicted.stream().forEach((idItem) -> {
            recommendations.add(new Recommendation(idItem, 0));
        });

        Collections.sort(recommendations);
        return recommendations;
    }

    public List<Recommendation> loadModel(FilePersistence filePersistence) throws FailureInPersistence {

        try {
            RecommendationsOutputFileXML recommendationsOutputMethod = new RecommendationsOutputFileXML(filePersistence.getCompleteFileName());
            return recommendationsOutputMethod.readRecommendations(User.ANONYMOUS_USER.getTargetId()).getRecommendations();
        } catch (JDOMException ex) {
            throw new FailureInPersistence("Malformed recommendationModel XML '"
                    + filePersistence.getCompleteFile().getAbsolutePath() + "'",
                    ex);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public List<Recommendation> loadModel(FilePersistence filePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        return loadModel(filePersistence);
    }

    @Override
    public void saveModel(FilePersistence filePersistence, List<Recommendation> model) throws FailureInPersistence {
        RecommendationsOutputFileXML recommendationsOutputMethod = new RecommendationsOutputFileXML(filePersistence.getCompleteFileName());
        recommendationsOutputMethod.writeRecommendations(new SingleUserRecommendations(User.ANONYMOUS_USER, model, RecommendationComputationDetails.EMPTY_DETAILS));
    }
}
