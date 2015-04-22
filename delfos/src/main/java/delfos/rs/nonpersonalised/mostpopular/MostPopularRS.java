package delfos.rs.nonpersonalised.mostpopular;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.ERROR_CODES;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.RecommenderSystemAdapter;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 25-Noviembre-2013
 */
public class MostPopularRS extends RecommenderSystemAdapter<List<Recommendation>> {

    private static final long serialVersionUID = 1L;

    @Override
    public boolean isRatingPredictorRS() {
        return false;
    }

    @Override
    public List<Recommendation> build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset {

        final float numUsers = datasetLoader.getRatingsDataset().allUsers().size();
        List<Recommendation> model = new ArrayList<Recommendation>(datasetLoader.getRatingsDataset().allRatedItems().size());
        RatingsDataset<? extends Rating> ratingDataset = datasetLoader.getRatingsDataset();
        for (int idItem : ratingDataset.allRatedItems()) {
            try {
                int numRatings = datasetLoader.getRatingsDataset().sizeOfItemRatings(idItem);

                model.add(new Recommendation(idItem, numRatings / numUsers));
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        Collections.sort(model);
        return model;
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, List<Recommendation> model, Integer idUser, java.util.Set<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        List<Recommendation> ret = new ArrayList<Recommendation>(idItemList.size());

        Set<Integer> added = new TreeSet<Integer>();
        for (Recommendation recommendation : model) {
            if (idItemList.contains(recommendation.getIdItem())) {
                ret.add(new Recommendation(recommendation.getIdItem(), recommendation.getPreference()));
                added.add(recommendation.getIdItem());
            }
        }

        //Para que la cobertura sea 1 en todos los casos.
        Set<Integer> toAdd = new TreeSet<Integer>(idItemList);
        toAdd.removeAll(added);
        for (int idItem : toAdd) {
            ret.add(new Recommendation(idItem, 0));
        }

        Collections.sort(ret);
        return ret;
    }
}
