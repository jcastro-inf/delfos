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
package delfos.casestudy.parallelisation;

import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.RecommenderSystem;
import java.util.Collections;
import java.util.Set;

/**
 * Stores the input of the calculation of the recommendations with the stream function {@link RecommendationFunction}
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @param <RecommendationModel>
 * @param <RatingType>
 */
public class RecommendationTaskInput<RecommendationModel extends Object, RatingType extends Rating> {

    private final User user;
    private final RecommenderSystem<RecommendationModel> recommenderSystem;
    private final DatasetLoader<RatingType> datasetLoader;
    private final RecommendationModel recommendationModel;
    private final Set<Item> candidateItems;

    public RecommendationTaskInput(
            User user,
            RecommenderSystem<RecommendationModel> recommenderSystem,
            DatasetLoader<RatingType> datasetLoader,
            RecommendationModel recommendationModel,
            Set<Item> candidateItems) {
        this.user = user;
        this.recommenderSystem = recommenderSystem;
        this.datasetLoader = datasetLoader;
        this.recommendationModel = recommendationModel;
        this.candidateItems = Collections.unmodifiableSet(candidateItems);
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        str.append("user --------> ").append(user).append("\n");
        str.append("candidateItems ---> ").append(candidateItems).append("\n");
        str.append("rs ----------> ").append(recommenderSystem.getAlias()).append("\n");
        str.append("\t").append(recommenderSystem.getNameWithParameters()).append("\n");

        return str.toString();
    }

    public User getUser() {
        return user;
    }

    public RecommenderSystem<RecommendationModel> getRecommenderSystem() {
        return recommenderSystem;
    }

    public Set<Item> getCandidateItems() {
        return candidateItems;
    }

    public DatasetLoader<RatingType> getDatasetLoader() {
        return datasetLoader;
    }

    public RecommendationModel getRecommendationModel() {
        return recommendationModel;
    }

    public Set<Item> getItemsRequested() {
        return candidateItems;
    }
}
