package delfos.group.grs;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterListener;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.experiment.SeedHolder;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.rs.nonpersonalised.randomrecommender.RandomRecommenderModel;
import delfos.rs.recommendation.Recommendation;

/**
 * Recomendador aleatorio para grupos de usuarios. No usar en un sistema real
 *
 * @author Jorge Castro Gallardo
 */
public class RandomGroupRecommender
        extends GroupRecommenderSystemAdapter<RandomRecommenderModel<GroupOfUsers>, GroupOfUsers>
        implements SeedHolder {

    private static final long serialVersionUID = 1L;

    public RandomGroupRecommender() {
        super();

        init();
    }

    @Override
    public RandomRecommenderModel<GroupOfUsers> build(DatasetLoader<? extends Rating> datasetLoader)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        return new RandomRecommenderModel(
                (int) getSeedValue(),
                datasetLoader.getRatingsDataset().getRatingsDomain().min(),
                datasetLoader.getRatingsDataset().getRatingsDomain().max());
    }

    @Override
    public GroupOfUsers buildGroupModel(DatasetLoader<? extends Rating> datasetLoader, RandomRecommenderModel<GroupOfUsers> recommenderSystemModel, GroupOfUsers groupOfUsers) throws UserNotFound {
        return new GroupOfUsers(groupOfUsers.getGroupMembers());
    }

    @Override
    public List<Recommendation> recommendOnly(
            DatasetLoader<? extends Rating> datasetLoader,
            RandomRecommenderModel<GroupOfUsers> recommenderSystemModel,
            GroupOfUsers groupModel,
            GroupOfUsers groupOfUsers,
            Collection<Integer> idItemList)
            throws UserNotFound, CannotLoadRatingsDataset {

        if (recommenderSystemModel.getRandomFloat(groupOfUsers) > 0.999) {
            return Collections.EMPTY_LIST;
        } else {
            final int numRecomendaciones = (int) (recommenderSystemModel.getRandomInt(groupOfUsers, idItemList.size()));
            final double min = datasetLoader.getRatingsDataset().getRatingsDomain().min().doubleValue();
            final double rango = datasetLoader.getRatingsDataset().getRatingsDomain().width().doubleValue();

            List<Recommendation> recommendationList = new ArrayList<>(numRecomendaciones);
            ArrayList<Integer> toPredict = new ArrayList<>(idItemList);
            for (int i = 0; i < numRecomendaciones; i++) {
                int idItem = toPredict.remove(recommenderSystemModel.getRandomInt(groupOfUsers, toPredict.size()));

                double ratingAleatorio = recommenderSystemModel.getRandomFloat(groupOfUsers);
                ratingAleatorio = ratingAleatorio * rango + min;
                recommendationList.add(new Recommendation(idItem, ratingAleatorio));
            }
            Collections.sort(recommendationList);
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
