package delfos.experiment.validation.validationtechnique;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;

/**
 * Clase que implementa el método de partición de datasets Leave-One-Out, que
 * genera un dataset de entrenamiento por cada valoración. No se aconseja
 * utilizar, ya que el cálculo es muy extensivo.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 (19 Octubre 2011)
 * @version 1.1 21-02-2013 Adecuación a la implementación de {@link SeedHolder}
 * @version 1.1 19-04-2013 Corrección del código para que implemente el
 * algoritmo All-but-one.
 */
public class LeaveOneOut extends ValidationTechnique {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada.
     * Por defecto tiene cinco particiones y la semilla utilizada será la fecha
     * actual {@link System#currentTimeMillis()}
     */
    protected LeaveOneOut() {
        super();
    }

    @Override
    public PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        int numRatings = datasetLoader.getRatingsDataset().getNumRatings();

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numRatings];
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        int split = 0;
        for (Rating rating : ratingsDataset) {

            Map<Integer, Set<Integer>> conjuntoTest = new TreeMap<Integer, Set<Integer>>();
            conjuntoTest.put(rating.idUser, new TreeSet<Integer>());
            conjuntoTest.get(rating.idUser).add(rating.idItem);
            try {
                ret[split] = new PairOfTrainTestRatingsDataset(
                        datasetLoader,
                        ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), conjuntoTest),
                        ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), conjuntoTest));
            } catch (UserNotFound ex) {
                /*Por el cálculo que se realiza, si el dataset es estático este error nunca debe suceder.*/
                throw new UnsupportedOperationException();
            } catch (ItemNotFound ex) {
                /*Por el cálculo que se realiza, si el dataset es estático este error nunca debe suceder.*/
                throw new UnsupportedOperationException();
            }
            split++;
        }

        return ret;
    }

    @Override
    public int getNumberOfSplits() {
        return -1;
    }
}
