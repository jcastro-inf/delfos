package delfos.dataset.storage.validationdatasets;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGiven;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract_withTrust;
import delfos.dataset.basic.loader.types.DatasetLoader;

/**
 * Par de conjuntos entrenamiento y evaluación. La unión de los dos conjuntos
 * resulta en el conjunto de datos original.
 *
* @author Jorge Castro Gallardo
 *
 * @version 1.0 4-Junio-2013
 * @version 1.1 26-Noviembre-2013
 * @param <RatingType>
 */
public class PairOfTrainTestRatingsDataset<RatingType extends Rating> extends CompleteDatasetLoaderAbstract_withTrust<RatingType> {

    private static final long serialVersionUID = 1L;
    /**
     * Dataset de entrenamiento.
     */
    public final RatingsDataset<RatingType> train;
    /**
     * Dataset de evaluación.
     */
    public final RatingsDataset<RatingType> test;
    private final DatasetLoader<RatingType> originalDatasetLoader;

    /**
     *
     * @param originalDatasetLoader
     * @param train
     * @param test
     * @throws CannotLoadRatingsDataset
     * @throws CannotLoadContentDataset
     */
    public PairOfTrainTestRatingsDataset(DatasetLoader<RatingType> originalDatasetLoader, RatingsDataset<RatingType> train, RatingsDataset<RatingType> test) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        this.originalDatasetLoader = originalDatasetLoader;

        this.train = train;
        this.test = test;
    }

    public DatasetLoader<? extends Rating> getTrainingDatasetLoader() {
        return new DatasetLoaderGiven(originalDatasetLoader, train);
    }

    public DatasetLoader<? extends Rating> getTestDatasetLoader() {
        return new DatasetLoaderGiven(originalDatasetLoader, test);
    }
}
