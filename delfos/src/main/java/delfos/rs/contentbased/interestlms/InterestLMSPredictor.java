package delfos.rs.contentbased.interestlms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
    public InterestLMSPredictorModel build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
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
                int idUser = rating.idUser;
                int idItem = rating.idItem;
                Item item = contentDataset.get(idItem);

                Number ratingValue = rating.ratingValue;
                Number minusOneToOneValue = domain.convertToDomain(ratingValue, minusOneToOne);

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
    public List<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, InterestLMSPredictorModel model, Integer idUser, Collection<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        List<Recommendation> ret = new ArrayList<>(idItemList.size());
        for (int idItem : idItemList) {
            try {
                Item item = contentDataset.get(idItem);
                float prediction = model.predict(idUser, item);

                ret.add(new Recommendation(idItem, prediction));
            } catch (EntityNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        Collections.sort(ret);

        return ret;
    }

}
