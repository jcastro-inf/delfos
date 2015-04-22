package delfos.experiment.validation.predictionprotocol;

import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Esta técnica aplica la validación cruzada para la predicción de valoraciones.
 *
 * @author Jorge Castro Gallardo
 */
public class CrossFoldPredictionProtocol extends PredictionProtocol {

    private static final long serialVersionUID = 1L;
    public static final Parameter numFolds = new Parameter("c", new IntegerParameter(2, Integer.MAX_VALUE, 5));

    public CrossFoldPredictionProtocol() {
        super();

        addParameter(numFolds);
    }

    public CrossFoldPredictionProtocol(int c) {
        this();
        setParameterValue(numFolds, c);
    }

    @Override
    public Collection<Set<Integer>> getRecommendationRequests(RatingsDataset<? extends Rating> testRatingsDataset, int idUser) throws UserNotFound {
        Random random = new Random(getSeedValue());
        ArrayList<Set<Integer>> ret = new ArrayList<>();
        Set<Integer> items = new TreeSet<>(testRatingsDataset.getUserRated(idUser));
        for (int i = 0; i < getNumPartitions(); i++) {
            ret.add(new TreeSet<>());
        }
        int n = 0;
        while (!items.isEmpty()) {
            int idItem = items.toArray(new Integer[0])[random.nextInt(items.size())];
            items.remove(idItem);
            int partition = n % getNumPartitions();
            ret.get(partition).add(idItem);
            n++;
        }
        return ret;
    }

    protected int getNumPartitions() {
        return (Integer) getParameterValue(numFolds);
    }
}
