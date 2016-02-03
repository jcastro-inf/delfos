package delfos.group.casestudy.parallelisation;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import java.util.Collections;
import java.util.Set;

/**
 * Stores the input of the calculation of the recommendations with the stream
 * function {@link SingleGroupRecommendationFunction}
 *
 * @author Jorge Castro Gallardo
 */
public class SingleGroupRecommendationTaskInput {

    private final GroupOfUsers groupOfUsers;
    private final GroupRecommenderSystem groupRecommenderSystem;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Object RecommendationModel;
    private final Set<Integer> candidateItems;

    public SingleGroupRecommendationTaskInput(GroupRecommenderSystem groupRecommenderSystem, DatasetLoader<? extends Rating> datasetLoader,
            Object RecommendationModel, GroupOfUsers group, Set<Integer> candidateItems) {
        this.groupOfUsers = group;
        this.groupRecommenderSystem = groupRecommenderSystem;
        this.datasetLoader = datasetLoader;
        this.RecommendationModel = RecommendationModel;
        this.candidateItems = candidateItems;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("group --------> ").append(groupOfUsers).append("\n");
        str.append("candidateItems ---> ").append(candidateItems).append("\n");
        str.append("grs ----------> ").append(groupRecommenderSystem.getAlias()).append("\n");
        str.append("\t").append(groupRecommenderSystem.getNameWithParameters()).append("\n");

        return str.toString();
    }

    public GroupOfUsers getGroupOfUsers() {
        return groupOfUsers;
    }

    public GroupRecommenderSystem getGroupRecommenderSystem() {
        return groupRecommenderSystem;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public Object getRecommendationModel() {
        return RecommendationModel;
    }

    public Set<Integer> getItemsRequested() {
        return Collections.unmodifiableSet(candidateItems);
    }
}
