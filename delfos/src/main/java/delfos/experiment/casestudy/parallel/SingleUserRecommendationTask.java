package delfos.experiment.casestudy.parallel;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.RecommenderSystem;
import delfos.rs.recommendation.Recommendation;

/**
 * Almacena todos los datos para realizar una solicitud de recomendación.
 *
* @author Jorge Castro Gallardo
 */
public class SingleUserRecommendationTask extends Task {

    private final Object model;
    private final int idUser;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Collection<Integer> idItemList;
    private List<Recommendation> recommendationList = null;
    private final RecommenderSystem<? extends Object> recommenderSystem;

    public SingleUserRecommendationTask(RecommenderSystem<? extends Object> recommenderSystem, DatasetLoader<? extends Rating> datasetLoader, Object model, int idUser, Collection<Integer> idItemList) {
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

    public Collection<Integer> getIdItemList() {
        return Collections.unmodifiableCollection(idItemList);
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

    public List<Recommendation> getRecommendationList() {
        return Collections.unmodifiableList(recommendationList);
    }

    public void setRecommendationList(List<Recommendation> recommendationList) {
        this.recommendationList = recommendationList;
    }
}
