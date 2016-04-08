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

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.storage.validationdatasets.PairOfTrainTestRatingsDataset;
import delfos.dataset.storage.validationdatasets.ValidationDatasets;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Clase que implementa el método de partición de datasets Leave-One-Out, que genera un dataset de entrenamiento por
 * cada valoración. No se aconseja utilizar, ya que el cálculo es muy extensivo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (19 Octubre 2011)
 * @version 1.1 21-02-2013 Adecuación a la implementación de {@link SeedHolder}
 * @version 1.1 19-04-2013 Corrección del código para que implemente el algoritmo All-but-one.
 */
public class LeaveOneOut extends ValidationTechnique {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor de la clase que genera los conjuntos de validación cruzada. Por defecto tiene cinco particiones y la
     * semilla utilizada será la fecha actual {@link System#currentTimeMillis()}
     */
    protected LeaveOneOut() {
        super();
    }

    @Override
    public <RatingType extends Rating> PairOfTrainTestRatingsDataset[] shuffle(DatasetLoader<RatingType> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        int numRatings = datasetLoader.getRatingsDataset().getNumRatings();

        PairOfTrainTestRatingsDataset[] ret = new PairOfTrainTestRatingsDataset[numRatings];
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        int split = 0;
        for (Rating rating : ratingsDataset) {

            Map<Integer, Set<Integer>> conjuntoTest = new TreeMap<>();
            conjuntoTest.put(rating.getIdUser(), new TreeSet<>());
            conjuntoTest.get(rating.getIdUser()).add(rating.getIdItem());

            ret[split] = new PairOfTrainTestRatingsDataset(
                    datasetLoader,
                    ValidationDatasets.getInstance().createTrainingDataset(datasetLoader.getRatingsDataset(), conjuntoTest),
                    ValidationDatasets.getInstance().createTestDataset(datasetLoader.getRatingsDataset(), conjuntoTest),
                    "_" + this.getClass().getSimpleName() + "_seed=" + getSeedValue());

            split++;
        }

        return ret;
    }

    @Override
    public int getNumberOfSplits() {
        return -1;
    }
}
