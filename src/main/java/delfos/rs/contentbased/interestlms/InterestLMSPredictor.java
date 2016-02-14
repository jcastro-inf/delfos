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
package delfos.rs.contentbased.interestlms;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.FloatParameter;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 25-Noviembre-2013
 */
public class InterestLMSPredictor extends RecommenderSystemAdapter<InterestLMSPredictorModel> {

    private static final long serialVersionUID = 1L;

    public double getLearningModerator() {
        return ((Number) getParameterValue(LEARNING_MODERATOR)).doubleValue();
    }

    public double getCertaintyModerator() {
        return ((Number) getParameterValue(CERTAINTY_MODERATOR)).doubleValue();
    }

    /**
     * Tuning attribute: The rate of updating the value of interest in subjects
     * in the user profile.
     */
    public static final Parameter LEARNING_MODERATOR = new Parameter("LEARNING_MODERATOR", new FloatParameter(0, 50, 0.14f));
    /**
     * Tuning attribute: The rate of updating the certainty of interest in
     * subjects in the user profile.
     */
    public static final Parameter CERTAINTY_MODERATOR = new Parameter("CERTAINTY_MODERATOR", new FloatParameter(0, 50, 0.2f));

    public InterestLMSPredictor() {
        addParameter(LEARNING_MODERATOR);
        addParameter(CERTAINTY_MODERATOR);
    }

    @Override

    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public InterestLMSPredictorModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        //Construyo el predictorModel
        InterestLMSPredictorModel predictorModel = new InterestLMSPredictorModel(datasetLoader, getLearningModerator());

        DecimalDomain minusOneToOne = new DecimalDomain(-1, 1);

        Domain domain = datasetLoader.getRatingsDataset().getRatingsDomain();

        int numRatings = datasetLoader.getRatingsDataset().getNumRatings();
        int i = 0;
        RatingsDataset<? extends Rating> ratingDataset = datasetLoader.getRatingsDataset();
        for (Rating rating : ratingDataset) {
            try {
                int idUser = rating.getIdUser();
                int idItem = rating.getIdItem();
                Item item = contentDataset.get(idItem);

                Number ratingValue = rating.getRatingValue();
                Number minusOneToOneValue = domain.convertToDecimalDomain(ratingValue, minusOneToOne);

                predictorModel.enterFeedback(idUser, item, minusOneToOneValue.floatValue());
            } catch (EntityNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
            i++;

            fireBuildingProgressChangedEvent("Training InterestLMSPredictor", i * 100 / numRatings, -1);
        }

        return predictorModel;
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, InterestLMSPredictorModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        Collection<Recommendation> ret = new ArrayList<>(candidateItems.size());
        for (int idItem : candidateItems) {
            try {
                Item item = contentDataset.get(idItem);
                float prediction = model.predict(idUser, item);

                ret.add(new Recommendation(idItem, prediction));
            } catch (EntityNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        return ret;
    }

}
