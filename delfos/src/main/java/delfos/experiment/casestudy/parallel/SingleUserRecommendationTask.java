package delfos.experiment.casestudy.parallel;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.RecommenderSystem;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * Almacena todos los datos para realizar una solicitud de recomendación.
 *
 * @author Jorge Castro Gallardo
 */
public class SingleUserRecommendationTask extends Task {

    private final Object model;
    private final int idUser;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Set<Integer> idItemList;
    private Collection<Recommendation> recommendationList = null;
    private final RecommenderSystem<? extends Object> recommenderSystem;

    public SingleUserRecommendationTask(RecommenderSystem<? extends Object> recommenderSystem, DatasetLoader<? extends Rating> datasetLoader, Object model, int idUser, Set<Integer> idItemList) {
        this.model = model;
        this.idUser = idUser;
        this.recommenderSystem = recommenderSystem;
        this.datasetLoader = datasetLoader;
        this.idItemList = idItemList;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("recommendTo --------> ").append(idUser).append("\n");
        str.append("idItemList ---------> ").append(idItemList).append("\n");
        str.append("recommenderSystem --> ").append(recommenderSystem.getAlias()).append("\n");
        str.append("\t").append(recommenderSystem.getNameWithParameters()).append("\n");

        return str.toString();
    }

    public Set<Integer> getIdItemList() {
        return Collections.unmodifiableSet(idItemList);
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public RecommenderSystem<? extends Object> getRecommenderSystem() {
        return recommenderSystem;
    }

    public void clearResources() {
    }

    public Object getRecommenderSystemModel() {
        return model;
    }

    public int getIdUser() {
        return idUser;
    }

    public Collection<Recommendation> getRecommendationList() {
        return Collections.unmodifiableCollection(recommendationList);
    }

    public void setRecommendationList(Collection<Recommendation> recommendationList) {
        this.recommendationList = recommendationList;
    }
}
