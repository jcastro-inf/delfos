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
package delfos.experiment.validation.validationtechnique;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Clase que implementa el método de partición de datasets Hold Out por ratings.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (19 Octubre 2011)
 * @version 1.0 (19-Abril-2013) Documentación de la clase.
 */
public class HoldOut_Ratings extends ValidationTechnique {

    private static final long serialVersionUID = 1L;
    /**
     * Porcentaje de valoraciones que contiene el conjunto de entrenamiento.
     */
    public static final Parameter TRAIN_PERCENT = new Parameter(
            "Training_percent",
            new DoubleParameter(0, 1, 0.8f),
            "Porcentaje de valoraciones que contiene el conjunto de entrenamiento.");

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada. Por defecto tiene cinco particiones y la
     * semilla utilizada será la fecha actual {@link System#currentTimeMillis()}
     */
    public HoldOut_Ratings() {
        super();
        addParameter(TRAIN_PERCENT);
    }

    @Override
    public <RatingType extends Rating> PairOfTrainTestRatingsDataset<RatingType>[] shuffle(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        Random random = new Random(getSeedValue());
        PairOfTrainTestRatingsDataset<RatingType>[] ret = new PairOfTrainTestRatingsDataset[1];

        //HoldOut initialization
        Map<Integer, Set<Integer>> testSet = new TreeMap<>();
        double testPercentValue = 1 - getTrainPercent();

        //composicion de los conjuntos de training y test
        int numUsers = datasetLoader.getRatingsDataset().allUsers().size();
        int usuActual = 1;
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        for (int idUser : ratingsDataset.allUsers()) {
            try {
                //creo una lista con todos los idItem para ir quitando cuando se elige la partición en la que estará
                Set<Integer> allItemsThisUser = new TreeSet<>(datasetLoader.getRatingsDataset().getUserRated(idUser));
                Set<Integer> testItemsThisUser = new TreeSet<>();

                long ratingsToRemove = Math.round(testPercentValue * datasetLoader.getRatingsDataset().getUserRated(idUser).size());
                if (ratingsToRemove == 0) {
                    ratingsToRemove = 1;
                }
                //Realizo la elección de la particion a la que pertenece cada item
                //sacando uno aleatoriamente y lo meto en la lista que toca
                while (testItemsThisUser.size() < ratingsToRemove) {
                    int index = random.nextInt(allItemsThisUser.size());
                    Integer idItem = (Integer) allItemsThisUser.toArray()[index];
                    allItemsThisUser.remove(idItem);
                    testItemsThisUser.add(idItem);
                }

                //compongo los conjuntos de validación completos de cada ejecución
                if (!testItemsThisUser.isEmpty()) {
                    testSet.put(idUser, testItemsThisUser);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
            progressChanged("Shuffle", (int) ((usuActual++ * 100.0) / numUsers));
        }

        try {
            ret[0] = new PairOfTrainTestRatingsDataset<>(
                    datasetLoader,
                    ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), testSet),
                    ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), testSet),
                    "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue());
        } catch (UserNotFound ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        }
        return ret;
    }

    public double getTrainPercent() {
        return ((Number) getParameterValue(TRAIN_PERCENT)).doubleValue();
    }

    @Override
    public int getNumberOfSplits() {
        return 1;
    }
}
