package delfos.group.casestudy.parallelisation;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.GroupRecommenderSystem;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 30-May-2013
 */
public class SingleGroupRecommendationTask extends Task {

    private final GroupOfUsers group;
    private GroupRecommenderSystem groupRecommenderSystem;
    private DatasetLoader<? extends Rating> datasetLoader;
    private Object recommenderSystemModel;
    private final Set<Integer> idItemList;

    private Collection<Recommendation> recommendations;
    private long buildGroupModelTime;
    private long recommendationTime;

    public SingleGroupRecommendationTask(GroupRecommenderSystem groupRecommenderSystem, DatasetLoader<? extends Rating> datasetLoader,
            Object recommenderSystemModel, GroupOfUsers group, Set<Integer> idItemList) {
        this.group = group;
        this.groupRecommenderSystem = groupRecommenderSystem;
        this.datasetLoader = datasetLoader;
        this.recommenderSystemModel = recommenderSystemModel;
        this.idItemList = idItemList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("group --------> ").append(group).append("\n");
        str.append("idItemList ---> ").append(idItemList).append("\n");
        str.append("grs ----------> ").append(groupRecommenderSystem.getAlias()).append("\n");
        str.append("\t").append(groupRecommenderSystem.getNameWithParameters()).append("\n");

        return str.toString();
    }

    public GroupOfUsers getGroup() {
        return group;
    }

    public long getBuildGroupModelTime() {
        return buildGroupModelTime;
    }

    public long getRecommendationTime() {
        return recommendationTime;
    }

    public GroupRecommenderSystem getGroupRecommenderSystem() {
        return groupRecommenderSystem;
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public Object getRecommenderSystemModel() {
        return recommenderSystemModel;
    }

    public Set<Integer> getIdItemList() {
        return Collections.unmodifiableSet(idItemList);
    }

    protected void setResults(Collection<Recommendation> recommendations, long buildGroupModelTime, long recommendationTime) {
        this.recommendations = recommendations;
        this.buildGroupModelTime = buildGroupModelTime;
        this.recommendationTime = recommendationTime;
        groupRecommenderSystem = null;
        datasetLoader = null;
        recommenderSystemModel = null;
    }

    public Collection<Recommendation> getRecommendations() {
        return Collections.unmodifiableCollection(recommendations);
    }
}
