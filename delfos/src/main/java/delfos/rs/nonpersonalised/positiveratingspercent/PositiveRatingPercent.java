package delfos.rs.nonpersonalised.positiveratingspercent;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * Algoritmo de recomendación no personalizado que recomienda los productos con
 * mayor porcentaje de ratings positivos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 08-ene-2014
 */
public class PositiveRatingPercent extends NonPersonalisedRecommender<Collection<Recommendation>> {

    @Override
    public final boolean isRatingPredictorRS() {
        return false;
    }

    @Override
    public Collection<Recommendation> buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        final double ratingThreshold = 4;
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(ratingThreshold);

        Collection<Recommendation> recommendationModel1 = new ArrayList<>(ratingsDataset.allRatedItems().size());

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

                final double preference = positiveRatings / numRatings;

                recommendationModel1.add(new Recommendation(idItem, preference));
            } catch (ItemNotFound ex) {

            }

        }

        if (Global.isVerboseAnnoying()) {
            Global.showln("================= Recommendation model for " + this.getName() + "==============");
            Global.showln(recommendationModel1.toString());
            Global.showln("=================");

        }
        return recommendationModel1;
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, Collection<Recommendation> model, Collection<Integer> candidateItems)
            throws ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        Collection<Recommendation> recommendations = new ArrayList<>();
        model.stream()
                .filter((recommendation) -> (candidateItems.contains(recommendation.getIdItem())))
                .forEach((recommendation) -> {
                    recommendations.add(new Recommendation(recommendation.getIdItem(), recommendation.getPreference()));
                });

        return recommendations;
    }

}
