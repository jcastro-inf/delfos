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
package delfos.rs.nonpersonalised.meanrating.wilsonscoreonterval;

import delfos.common.Global;
import delfos.common.decimalnumbers.NumberRounder;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRating;
import delfos.rs.nonpersonalised.meanrating.arithmeticmean.MeanRatingRSModel;
import delfos.rs.output.RecommendationsOutputFileXML;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.FilePersistence;
import delfos.rs.persistence.database.DAOMeanRatingProfile;
import delfos.rs.recommendation.Recommendation;
import delfos.stats.distributions.NormalDistribution;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.jdom2.JDOMException;

/**
 * Lower bound of Wilson score confidence interval for a Bernoulli parameter.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 07-ene-2014
 */
public class WilsonScoreLowerBound extends NonPersonalisedRecommender<MeanRatingRSModel> {

    public WilsonScoreLowerBound() {
        super();
    }

    @Override
    public final boolean isRatingPredictorRS() {
        return false;
    }

    @Override
    public MeanRatingRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        final double confidence = 0.95;
        final double ratingThreshold = 4;

        final double z = NormalDistribution.z(confidence);
        RelevanceCriteria relevanceCriteria = new RelevanceCriteria(ratingThreshold);

        Collection<Recommendation> recommendationModel1 = new ArrayList<>(ratingsDataset.allRatedItems().size());

        for (Item item : datasetLoader.getContentDataset()) {
            try {
                Map<Long, ? extends Rating> itemRatings = ratingsDataset.getItemRatingsRated(item.getId());

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
                    Global.showln("n=" + n + " pos=" + pRounded + "\t --> \t" + preferenceRounded);
                }

                recommendationModel1.add(new Recommendation(item, preference));
            } catch (ItemNotFound ex) {
                Global.showInfoMessage("No information about item " + item + ", cannot build its model.");
            }

        }

        if (Global.isVerboseAnnoying()) {
            Global.showln("================= Recommendation model for " + this.getName() + "==============");
            Global.showln(recommendationModel1.toString());
            Global.showln("=================");

        }

        return MeanRatingRSModel.create(recommendationModel1);
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader,
            MeanRatingRSModel model,
            Collection<Long> candidateItems) throws ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        Map<Item, MeanRating> meanRatingsByItem = model
                .getSortedMeanRatings().parallelStream()
                .collect(Collectors.toMap(meanRating -> meanRating.getItem(), Function.identity()));

        List<Recommendation> recommendations = candidateItems.parallelStream()
                .map(idItem -> datasetLoader.getContentDataset().getItem(idItem))
                .map(item -> meanRatingsByItem.containsKey(item) ? meanRatingsByItem.get(item) : new MeanRating(item, Double.NaN))
                .map(meanRating -> new Recommendation(meanRating.getItem(), meanRating.getPreference()))
                .collect(Collectors.toList());

        return recommendations;
    }

    public Collection<Recommendation> loadModel(FilePersistence filePersistence) throws FailureInPersistence {

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
    public MeanRatingRSModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Long> users, Collection<Long> items, DatasetLoader<? extends Rating> datasetLoader) throws FailureInPersistence {
        DAOMeanRatingProfile dAOMeanRatingProfile = new DAOMeanRatingProfile();
        return dAOMeanRatingProfile.loadModel(databasePersistence, users, items, datasetLoader);
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, MeanRatingRSModel model) throws FailureInPersistence {
        DAOMeanRatingProfile dAOMeanRatingProfile = new DAOMeanRatingProfile();
        dAOMeanRatingProfile.saveModel(databasePersistence, model);
    }
}
