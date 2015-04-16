package delfos.group.grs.itemweighted.measures;

import java.util.Map;
import java.util.TreeMap;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.ParameterOwnerAdapter;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;

/**
 * Debe devolver los pesos normalizados.
 *
* @author Jorge Castro Gallardo
 */
public abstract class GroupItemWeight extends ParameterOwnerAdapter {

    private static final long serialVersionUID = 1L;

    public abstract Map<Integer, ? extends Number> getItemWeights(DatasetLoader<? extends Rating> datasetLoader, GroupOfUsers groupOfUsers) throws CannotLoadRatingsDataset, UserNotFound;

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.GROUP_ITEM_WEIGHT;
    }

    public static Map<Integer, Number> normaliseWeights(Map<Integer, ? extends Number> weights) {
        double sumOfValues = 0;
        sumOfValues = weights
                .values()
                .stream()
                .map((value) -> value.doubleValue())
                .reduce(sumOfValues, (accumulator, _item) -> accumulator + _item);

        Map<Integer, Number> normalisedWeights = new TreeMap<>();

        for (int key : weights.keySet()) {
            double weight = weights.get(key).doubleValue();
            double normalisedWeight = weight / sumOfValues;
            normalisedWeights.put(key, normalisedWeight);
        }

        return normalisedWeights;
    }

}
