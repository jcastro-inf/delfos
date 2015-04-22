package delfos.recommendationcandidates;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;

/**
 * Devuelve los productos del catálogo no valorados por el usuario indicado.
 *
 * @version 03-jun-2014
 * @author Jorge Castro Gallardo
 */
public class OnlyNewItems extends RecommendationCandidatesSelector {

    @Override
    public Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound {
        Set<Integer> candidateItems = new TreeSet<>();

        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            candidateItems.addAll(contentDatasetLoader.getContentDataset().getAvailableItems());
        } else {
            candidateItems.addAll(datasetLoader.getRatingsDataset().allRatedItems());
        }

        if (user == User.ANONYMOUS_USER) {
            return candidateItems;
        } else {
            Collection<Integer> userRated = datasetLoader.getRatingsDataset().getUserRated(user.getId());
            candidateItems.removeAll(userRated);
            return candidateItems;
        }
    }

    @Override
    public Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws UserNotFound {

        Set<Integer> candidateItems = new TreeSet<>();

        Iterator<Integer> it = groupOfUsers.getGroupMembers().iterator();

        candidateItems.addAll(candidateItems(datasetLoader, new User(it.next())));

        for (; it.hasNext();) {
            int idUser = it.next();
            candidateItems.retainAll(candidateItems(datasetLoader, new User(idUser)));
        }

        return candidateItems;
    }
}
