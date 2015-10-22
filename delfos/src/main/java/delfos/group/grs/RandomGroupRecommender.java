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
 * @author Jorge Castro Gallardo
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

        if (RecommendationModel.getRandomFloat(groupOfUsers) > 0.999) {
            return Collections.EMPTY_LIST;
        } else {
            final int numRecomendaciones = (int) (RecommendationModel.getRandomInt(groupOfUsers, candidateItems.size()));
            final double min = datasetLoader.getRatingsDataset().getRatingsDomain().min().doubleValue();
            final double rango = datasetLoader.getRatingsDataset().getRatingsDomain().width().doubleValue();

            Collection<Recommendation> recommendationList = new ArrayList<>(numRecomendaciones);
            ArrayList<Integer> toPredict = new ArrayList<>(candidateItems);
            for (int i = 0; i < numRecomendaciones; i++) {
                int idItem = toPredict.remove(RecommendationModel.getRandomInt(groupOfUsers, toPredict.size()));

                double ratingAleatorio = RecommendationModel.getRandomFloat(groupOfUsers);
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
