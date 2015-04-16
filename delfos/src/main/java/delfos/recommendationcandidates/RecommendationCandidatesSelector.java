package delfos.recommendationcandidates;

import java.util.Collection;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

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

    public abstract Collection<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, User user) throws UserNotFound;

    public abstract Collection<Integer> candidateItems(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws UserNotFound;

    @Override
    public ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RECOMMENDATION_CANDIDATES_SELECTOR;
    }
}
