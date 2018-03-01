/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 08-ene-2014
 */
public class PositiveRatingPercent extends NonPersonalisedRecommender<Collection<Recommendation>> {

    @Override
    public final boolean isRatingPredictorRS() {
        return false;
    }

    @Override
    public <RatingType extends Rating> Collection<Recommendation> buildRecommendationModel(
            DatasetLoader<RatingType> datasetLoader)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        final RatingsDataset<RatingType> ratingsDataset = datasetLoader.getRatingsDataset();

        final double ratingThreshold = 4;
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(ratingThreshold);

        Collection<Recommendation> recommendationModel1 = new ArrayList<>(ratingsDataset.allRatedItems().size());

        for (long idItem : ratingsDataset.allRatedItems()) {
            try {
                Map<Long, RatingType> itemRatings = ratingsDataset.getItemRatingsRated(idItem);

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
    public <RatingType extends Rating> Collection<Recommendation> recommendOnly(
            DatasetLoader<RatingType> datasetLoader,
            Collection<Recommendation> model,
            Collection<Long> candidateItems
    ) throws ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        Collection<Recommendation> recommendations = new ArrayList<>();
        model.stream()
                .filter((recommendation) -> (candidateItems.contains(recommendation.getIdItem())))
                .forEach((recommendation) -> {
                    recommendations.add(new Recommendation(recommendation.getIdItem(), recommendation.getPreference()));
                });

        return recommendations;
    }

}
