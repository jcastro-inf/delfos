package delfos.rs.nonpersonalised.mostpopular;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 25-Noviembre-2013
 */
public class MostPopularRS extends RecommenderSystemAdapter<Collection<Recommendation>> {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isRatingPredictorRS() {
        return false;
    }

    @Override
    public Collection<Recommendation> build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        final float numUsers = datasetLoader.getRatingsDataset().allUsers().size();
        Collection<Recommendation> model = new ArrayList<>(datasetLoader.getRatingsDataset().allRatedItems().size());
        RatingsDataset<? extends Rating> ratingDataset = datasetLoader.getRatingsDataset();
        for (int idItem : ratingDataset.allRatedItems()) {
            try {
                int numRatings = datasetLoader.getRatingsDataset().sizeOfItemRatings(idItem);

                model.add(new Recommendation(idItem, numRatings / numUsers));
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        return model;
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, Collection<Recommendation> model, Integer idUser, java.util.Set<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        Collection<Recommendation> ret = new ArrayList<>(idItemList.size());

        Set<Integer> added = new TreeSet<>();
        for (Recommendation recommendation : model) {
            if (idItemList.contains(recommendation.getIdItem())) {
                ret.add(new Recommendation(recommendation.getIdItem(), recommendation.getPreference()));
                added.add(recommendation.getIdItem());
            }
        }

        //Para que la cobertura sea 1 en todos los casos.
        Set<Integer> toAdd = new TreeSet<>(idItemList);
        toAdd.removeAll(added);
        for (int idItem : toAdd) {
            ret.add(new Recommendation(idItem, 0));
        }

        return ret;
    }
}
