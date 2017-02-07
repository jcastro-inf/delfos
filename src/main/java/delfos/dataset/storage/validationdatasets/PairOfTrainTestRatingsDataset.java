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
package delfos.dataset.storage.validationdatasets;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.CompleteDatasetLoaderAbstract_withTrust;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.loaders.given.DatasetLoaderGivenRatingsDataset;

/**
 * Par de conjuntos entrenamiento y evaluación. La unión de los dos conjuntos resulta en el conjunto de datos original.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
    private final String generationExplanation;

    /**
     *
     * @param originalDatasetLoader
     * @param train
     * @param test
     * @param generationExplanation String to explain how the train and test sets have been generated.
     * @throws CannotLoadRatingsDataset
     * @throws CannotLoadContentDataset
     */
    public PairOfTrainTestRatingsDataset(
            DatasetLoader<RatingType> originalDatasetLoader,
            RatingsDataset<RatingType> train, RatingsDataset<RatingType> test,
            String generationExplanation
    ) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        this.originalDatasetLoader = originalDatasetLoader;

        this.train = train;
        this.test = test;
        this.generationExplanation = generationExplanation;
    }

    public DatasetLoader<RatingType> getTrainingDatasetLoader() {
        return new DatasetLoaderGivenRatingsDataset(originalDatasetLoader, train, generationExplanation + "_train");
    }

    public DatasetLoader<RatingType> getTestDatasetLoader() {
        return new DatasetLoaderGivenRatingsDataset(originalDatasetLoader, test, generationExplanation + "_test");
    }
}
