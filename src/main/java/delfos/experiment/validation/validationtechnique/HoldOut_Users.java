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

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.generated.modifieddatasets.SelectionDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Clase que implementa el método de partición en datasets de entrenamiento-evaluación, de manera que se divide por
 * usuarios, es decir, la partición de training tendra un determinado porcentaje de usuarios y la de test tendrá el
 * resto de usuarios, sin importar el número de valoraciones que tengan. Por lo tanto, esta validación toma como dato de
 * entrada el usuario.
 *
 * <p>
 * Esta validación se suele aplicar a sistemas de recomendación colaborativos Item-Item.
 *
 * <p>
 * Versión 1.1 Optimizado el código para igualdad de velocidad con distintos valores de training_percent (mejora del
 * muestreo aleatorio).
 *
 *
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (19 Octubre 2011)
 * @version 1.1 (18-Jan-2013)
 */
public class HoldOut_Users extends ValidationTechnique {

    private static final long serialVersionUID = 1L;
    /**
     * Porcentaje de valoraciones que contiene el conjunto de entrenamiento.
     */
    public static final Parameter TRAIN_PERCENT = new Parameter("Training_percent", new IntegerParameter(0, 100, 80), "Porcentaje de usuarios que contiene el conjunto de entrenamiento.");

    /**
     * Constructor de la clase que genera los conjuntos de entrenamiento y test con un esquema hold-out. Por defecto, el
     * conjunto de entrenamiento es el 80% de valores de entrada y el de test el 20% restante.
     */
    public HoldOut_Users() {
        super();

        addParameter(TRAIN_PERCENT);
    }

    @Override
    public <RatingType extends Rating> PairOfTrainTestRatingsDataset<RatingType>[] shuffle(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        Random random = new Random(getSeedValue());
        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[1];

        List<Integer> users = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());

        int numUserInTest = (users.size() * (100 - getTrainPercentValue())) / 100;
        Set<Integer> usersInTest = new TreeSet<>();

        while (usersInTest.size() < numUserInTest && !users.isEmpty()) {
            int index = random.nextInt(users.size());
            int idUser = users.remove(index);
            usersInTest.add(idUser);
        }

        Set<Integer> allItems = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());

        Global.showInfoMessage("Original dataset #users " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");

        Set<Integer> usuariosEnTraining = new TreeSet<>(datasetLoader.getRatingsDataset().allUsers());
        usuariosEnTraining.removeAll(usersInTest);

        SelectionDataset training = new SelectionDataset(datasetLoader.getRatingsDataset());
        training.setAllowedItems(allItems);
        training.setAllowedUsers(usuariosEnTraining);

        SelectionDataset test = new SelectionDataset(datasetLoader.getRatingsDataset());
        test.setAllowedItems(allItems);
        test.setAllowedUsers(usersInTest);

        ret[0] = new PairOfTrainTestRatingsDataset<>(datasetLoader, training, test,
                "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue());

        Global.showInfoMessage("Training dataset #users " + training.allUsers().size() + "\n");
        Global.showInfoMessage("Test dataset #users     " + test.allUsers().size() + "\n");

        progressChanged("Validation.shuffle() finished", 100);
        return ret;
    }

    /**
     * Devuelve el valor del parámetro {@link HoldOut_Users#TRAIN_PERCENT}.
     *
     * @return Porcentaje de usuarios en entrenamiento.
     */
    public int getTrainPercentValue() {
        return (Integer) getParameterValue(TRAIN_PERCENT);
    }

    @Override
    public int getNumberOfSplits() {
        return 1;
    }
}
