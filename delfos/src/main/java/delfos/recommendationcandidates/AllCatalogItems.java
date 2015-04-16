package delfos.recommendationcandidates;

import java.util.Collection;
import java.util.TreeSet;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Devuelve todos los productos del catálogo.
 *
 * @version 03-jun-2014
 * @author Jorge Castro Gallardo
 */
public class AllCatalogItems extends RecommendationCandidatesSelector {

    @Override
    public Collection<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, User user) {
        Collection<Integer> idItemList = new TreeSet<>();

        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            idItemList.addAll(contentDatasetLoader.getContentDataset().getAvailableItems());
        } else {
            idItemList.addAll(datasetLoader.getRatingsDataset().allRatedItems());
        }

        return idItemList;
    }

    @Override
    public Collection<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) {
        return candidateItems(datasetLoader, User.ANONYMOUS_USER);
    }

}
