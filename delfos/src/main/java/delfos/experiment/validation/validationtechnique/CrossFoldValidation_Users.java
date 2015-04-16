package delfos.experiment.validation.validationtechnique;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.generated.modifieddatasets.SelectionDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;

/**
 * Clase que implementa el método validación cross fold validation que se aplica
 * en usuarios (las particiones las hace por usuarios, no por ratings o por
 * items) con la predicción todos menos 1 rating. {@link KnnMemoryBasedCFRS}
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 */
public class CrossFoldValidation_Users extends ValidationTechnique {

    private static final long serialVersionUID = 1L;

    /**
     * Parámetro para almacenar el número de particiones que se realizan sobre
     * el dataset original.
     */
    public static final Parameter NUM_PARTITIONS = new Parameter("NUM_PARTITIONS", new IntegerParameter(2, Integer.MAX_VALUE, 5), "Número de particiones que se realizan sobre el dataset original.");

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada.
     * Por defecto tiene cinco particiones y la semilla utilizada será la fecha
     * actual {@link System#currentTimeMillis()}
     */
    public CrossFoldValidation_Users() {
        addParameter(NUM_PARTITIONS);
    }

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        Random randomGenerator = new Random(getSeedValue());

        int numSplits = getNumberOfPartitions();

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numSplits];

        List<Integer> setUsers = new ArrayList<>(datasetLoader.getRatingsDataset().allUsers());
        Collection<Integer>[] usersTest = (Collection<Integer>[]) new Collection[numSplits];
        for (int i = 0; i < numSplits; i++) {
            usersTest[i] = new TreeSet<>();
        }
        while (!setUsers.isEmpty()) {
            int index = 0;
            while (!setUsers.isEmpty() && index < numSplits) {
                int idUser = setUsers.remove(randomGenerator.nextInt(setUsers.size()));
                usersTest[index].add(idUser);
                index++;
            }

        }
        for (Iterator<Integer> it = setUsers.listIterator(); it.hasNext();) {
            Integer integer = it.next();
            usersTest[randomGenerator.nextInt(numSplits)].add(integer);
        }

        Set<Integer> allItems = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());

        Global.showMessage("Original dataset #users " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");
        for (int i = 0; i < numSplits; i++) {
            Set<Integer> usuariosEnTraining = new TreeSet<>(datasetLoader.getRatingsDataset().allUsers());
            usuariosEnTraining.removeAll(usersTest[i]);

            SelectionDataset training = new SelectionDataset(datasetLoader.getRatingsDataset());
            training.setProductosPermitidos(allItems);
            training.setUsuariosPermitidos(usuariosEnTraining);

            SelectionDataset test = new SelectionDataset(datasetLoader.getRatingsDataset());
            test.setProductosPermitidos(allItems);
            test.setUsuariosPermitidos(usersTest[i]);

            ret[i] = new PairOfTrainTestRatingsDataset(datasetLoader, training, test);

            Global.showMessage("------------------  " + i + "  ------------------\n");
            Global.showMessage("Training dataset #users " + training.allUsers().size() + "\n");
            Global.showMessage("Test dataset #users     " + test.allUsers().size() + "\n");
        }
        progressChanged("Validation.shuffle() finished", 100);
        return ret;
    }

    public int getNumberOfPartitions() {
        return ((Number) getParameterValue(NUM_PARTITIONS)).intValue();
    }

    @Override
    public int getNumberOfSplits() {
        return getNumberOfPartitions();
    }
}
