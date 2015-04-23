package delfos.rs.bias;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;

/**
 * Sistema de recomendación que siempre devuelve una predicción usando el bias
 * general, bias del user y/o bias del item, si existen.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 5-marzo-2015
 */
public class PredictUserItemBias extends CollaborativeRecommender<Object> {

    public PredictUserItemBias() {
        super();
    }

    @Override
    public Object buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {
        return datasetLoader.getRatingsDataset().getMeanRating();
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, Object model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        Collection<Recommendation> recommendations = new ArrayList<>(candidateItems.size());

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        double generalBias = ((Number) model).doubleValue();
        double userBias = getUserBias(ratingsDataset, idUser);

        for (int idItem : candidateItems) {
            double itemBias = getItemBias(ratingsDataset, idUser);
            recommendations.add(new Recommendation(idItem, generalBias + userBias + itemBias));
        }

        return recommendations;
    }

    private double getUserBias(RatingsDataset ratingsDataset, Integer idUser) throws CannotLoadRatingsDataset {
        double userBias;
        try {
            userBias = ratingsDataset.getMeanRating() - ratingsDataset.getMeanRatingUser(idUser);
        } catch (UserNotFound ex) {
            userBias = 0;
        }

        return userBias;
    }

    private double getItemBias(RatingsDataset ratingsDataset, Integer idItem) throws CannotLoadRatingsDataset {
        double itemBias;
        try {
            itemBias = ratingsDataset.getMeanRating() - ratingsDataset.getMeanRatingItem(idItem);
        } catch (ItemNotFound ex) {
            itemBias = 0;
        }

        return itemBias;
    }

}
