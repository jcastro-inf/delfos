/* 
 * Copyright (C) 2016 jcastro
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class SingleUserRecommendationTask extends Task {

    private final Object model;
    private final int idUser;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Set<Integer> candidateItems;
    private Collection<Recommendation> recommendationList = null;
    private final RecommenderSystem<? extends Object> recommenderSystem;

    public SingleUserRecommendationTask(RecommenderSystem<? extends Object> recommenderSystem, DatasetLoader<? extends Rating> datasetLoader, Object model, int idUser, Set<Integer> candidateItems) {
        this.model = model;
        this.idUser = idUser;
        this.recommenderSystem = recommenderSystem;
        this.datasetLoader = datasetLoader;
        this.candidateItems = candidateItems;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("recommendTo --------> ").append(idUser).append("\n");
        str.append("candidateItems ---------> ").append(candidateItems).append("\n");
        str.append("recommenderSystem --> ").append(recommenderSystem.getAlias()).append("\n");
        str.append("\t").append(recommenderSystem.getNameWithParameters()).append("\n");

        return str.toString();
    }

    public Set<Integer> getCandidateItems() {
        return Collections.unmodifiableSet(candidateItems);
    }

    public DatasetLoader<? extends Rating> getDatasetLoader() {
        return datasetLoader;
    }

    public RecommenderSystem<? extends Object> getRecommenderSystem() {
        return recommenderSystem;
    }

    public void clearResources() {
    }

    public Object getRecommendationModel() {
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
