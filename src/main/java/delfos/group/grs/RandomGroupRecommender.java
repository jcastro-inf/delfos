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
package delfos.group.grs;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.SeedHolder;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommendationModel;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Recomendador aleatorio para grupos de usuarios. No usar en un sistema real
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class RandomGroupRecommender
        extends GroupRecommenderSystemAdapter<RandomRecommendationModel<GroupOfUsers>, GroupOfUsers>
        implements SeedHolder {

    private static final long serialVersionUID = 1L;

    public RandomGroupRecommender() {
        super();

        init();
    }

    @Override
    public RandomRecommendationModel<GroupOfUsers> buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return new RandomRecommendationModel(
                (int) getSeedValue(),
                datasetLoader.getRatingsDataset().getRatingsDomain().min(),
                datasetLoader.getRatingsDataset().getRatingsDomain().max());
    }

    @Override
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, RandomRecommendationModel<GroupOfUsers> RecommendationModel, GroupOfUsers groupOfUsers) throws UserNotFound {
        return new GroupOfUsers(groupOfUsers.getIdMembers());
    }

    @Override
    public Collection<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader, RandomRecommendationModel<GroupOfUsers> RecommendationModel, GroupOfUsers groupModel, GroupOfUsers groupOfUsers, java.util.Set<Integer> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset {

        if (RecommendationModel.getRandomDouble(groupOfUsers) > 0.999) {
            return Collections.EMPTY_LIST;
        } else {
            final int numRecomendaciones = (int) (RecommendationModel.getRandomInt(groupOfUsers, candidateItems.size()));
            final double min = datasetLoader.getRatingsDataset().getRatingsDomain().min().doubleValue();
            final double rango = datasetLoader.getRatingsDataset().getRatingsDomain().width().doubleValue();

            Collection<Recommendation> recommendationList = new ArrayList<>(numRecomendaciones);
            ArrayList<Integer> toPredict = new ArrayList<>(candidateItems);
            for (int i = 0; i < numRecomendaciones; i++) {
                int idItem = toPredict.remove(RecommendationModel.getRandomInt(groupOfUsers, toPredict.size()));

                double ratingAleatorio = RecommendationModel.getRandomDouble(groupOfUsers);
                ratingAleatorio = ratingAleatorio * rango + min;
                recommendationList.add(new Recommendation(idItem, ratingAleatorio));
            }
            return recommendationList;
        }
    }

    @Override
    public boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return ((Number) getParameterValue(SEED)).longValue();
    }

    private void init() {
        addParameter(SEED);

        addParammeterListener(new ParameterListener() {
            private long valorAnterior = (Long) getParameterValue(SEED);

            @Override
            public void parameterChanged() {
                long newValue = (Long) getParameterValue(SEED);
                if (valorAnterior != newValue) {
                    if (Global.isVerboseAnnoying()) {
                        Global.showWarning("Reset " + getName() + " to seed = " + newValue + "\n");
                    }
                    valorAnterior = newValue;
                    setSeedValue(newValue);
                }
            }
        });
    }
}
