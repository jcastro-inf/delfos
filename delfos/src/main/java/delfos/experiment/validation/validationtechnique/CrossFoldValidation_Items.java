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

/**
 * Clase que implementa el método validación cross fold validation que se aplica
 * en productos (las particiones las hace por productos, no por ratings o por
 * usuarios)
 *
* @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 */
public class CrossFoldValidation_Items extends ValidationTechnique {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para especificar el número de particiones que se realizan.
     */
    public static final Parameter numFolds = new Parameter("numFolds", new IntegerParameter(2, Integer.MAX_VALUE, 5));

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada.
     * Por defecto tiene cinco particiones.
     */
    public CrossFoldValidation_Items() {
        addParameter(numFolds);
    }

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadContentDataset, CannotLoadRatingsDataset {
        Random random = new Random(getSeedValue());

        int numSplits = getNumberOfFolds();
        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numSplits];

        List<Integer> setItems = new ArrayList<>(datasetLoader.getRatingsDataset().allRatedItems());
        Collection<Integer>[] itemsTest = (Collection<Integer>[]) new Collection[numSplits];
        for (int i = 0; i < numSplits; i++) {
            itemsTest[i] = new TreeSet<>();
        }
        while (!setItems.isEmpty()) {
            int index = 0;
            while (!setItems.isEmpty() && index < numSplits) {
                int idUser = setItems.remove(random.nextInt(setItems.size()));
                itemsTest[index].add(idUser);
                index++;
            }

        }
        for (Iterator<Integer> it = setItems.listIterator(); it.hasNext();) {
            Integer integer = it.next();
            itemsTest[random.nextInt(numSplits)].add(integer);
        }

        Set<Integer> allUsers = new TreeSet<>(datasetLoader.getRatingsDataset().allUsers());

        Global.showInfoMessage("Original dataset #users " + datasetLoader.getRatingsDataset().allUsers().size() + "\n");
        for (int i = 0; i < numSplits; i++) {
            Set<Integer> productosEnTraining = new TreeSet<>(datasetLoader.getRatingsDataset().allRatedItems());
            productosEnTraining.removeAll(itemsTest[i]);

            SelectionDataset training = new SelectionDataset(datasetLoader.getRatingsDataset());
            training.setProductosPermitidos(productosEnTraining);
            training.setUsuariosPermitidos(allUsers);

            SelectionDataset test = new SelectionDataset(datasetLoader.getRatingsDataset());
            test.setProductosPermitidos(itemsTest[i]);
            test.setUsuariosPermitidos(allUsers);
            ret[i] = new PairOfTrainTestRatingsDataset(datasetLoader, training, test);
            Global.showInfoMessage("------------------  " + i + "  ------------------\n");
            Global.showInfoMessage("Training dataset #users " + training.allUsers().size() + "\n");
            Global.showInfoMessage("Test dataset #users     " + test.allUsers().size() + "\n");
        }
        progressChanged("Validation.shuffle() finished", 100);
        return ret;
    }

    public int getNumberOfFolds() {
        return ((Number) getParameterValue(numFolds)).intValue();
    }

    @Override
    public int getNumberOfSplits() {
        return getNumberOfFolds();
    }
}
