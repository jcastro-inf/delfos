package delfos.recommendationcandidates;

import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Set;
import java.util.TreeSet;

/**
 * Devuelve todos los productos del catálogo.
 *
 * @version 03-jun-2014
 * @author Jorge Castro Gallardo
 */
public class AllCatalogItems extends RecommendationCandidatesSelector {

    @Override
    public Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, User user) {
        Set<Integer> candidateItems = new TreeSet<>();

        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            candidateItems.addAll(contentDatasetLoader.getContentDataset().getAvailableItems());
        } else {
            candidateItems.addAll(datasetLoader.getRatingsDataset().allRatedItems());
        }

        return candidateItems;
    }

    @Override
    public Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) {
        return candidateItems(datasetLoader, User.ANONYMOUS_USER);
    }

}
