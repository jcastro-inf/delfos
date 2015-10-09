package delfos.recommendationcandidates;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import java.util.Set;
import java.util.stream.Collectors;

/**
 *
 * @version 03-jun-2014
 * @author Jorge Castro Gallardo
 */
public abstract class RecommendationCandidatesSelector extends ParameterOwnerAdapter {

    static final long serialVersionUID = 1l;
    public static RecommendationCandidatesSelector defaultValue = new OnlyNewItems();

    public RecommendationCandidatesSelector() {
        super();
    }

    @Deprecated
    public abstract Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound;

    @Deprecated
    public abstract Set<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws UserNotFound;

    public Set<Item> candidateItemsNew(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound {
        Set<Integer> candidateItems = candidateItems(datasetLoader, user);
        return candidateItems.parallelStream().map((idItem) -> ((ContentDatasetLoader) datasetLoader).getContentDataset().get(idItem)).collect(Collectors.toSet());
    }

    public Set<Item> candidateItemsNew(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws UserNotFound {
        Set<Integer> candidateItems = candidateItems(datasetLoader, groupOfUsers);
        return candidateItems.parallelStream().map((idItem) -> ((ContentDatasetLoader) datasetLoader).getContentDataset().get(idItem)).collect(Collectors.toSet());
    }

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RECOMMENDATION_CANDIDATES_SELECTOR;
    }
}
