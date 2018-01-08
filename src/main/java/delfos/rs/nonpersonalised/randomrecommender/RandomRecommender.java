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
package delfos.rs.nonpersonalised.randomrecommender;

import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.experiment.SeedHolder;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Set;

/**
 * Sistema de recomendación que realiza la recomendación de manera aleatoria. No
 * se debe utilizar en un sistema real, sino que se utiliza para comprobar una
 * cota mínima de las métricas de evaluación que revisan la eficacia de los
 * sistemas de recomendación, como por ejemplo, calcular el peor mae que un
 * sistema de recomendación puede tener en un dataset dado.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 (28 de Febrero de 2013)
 */
public class RandomRecommender extends CollaborativeRecommender<RandomRecommendationModel<Long>> implements SeedHolder {

    private static final long serialVersionUID = 1L;

    private long oldSeed;

    /**
     * Constructor por defecto que se limita a llamar al método super();
     */
    public RandomRecommender() {
        super();
        addParameter(SEED);
        init();
    }

    @Override
    public RandomRecommendationModel<Long> buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        Global.showln("Dataset Alias: " + datasetLoader.getAlias());
        Global.showln("#Ratings: \t" + datasetLoader.getRatingsDataset().getNumRatings());
        Global.showln("#Users:   \t" + datasetLoader.getRatingsDataset().allUsers().size());
        Global.showln("#Items:   \t" + datasetLoader.getRatingsDataset().allRatedItems().size());

        HistogramNumbersSmart histogramSmart = new HistogramNumbersSmart(0.5);
        for (Rating rating : datasetLoader.getRatingsDataset()) {
            histogramSmart.addValue(rating.getRatingValue().doubleValue());
        }

        histogramSmart.printHistogram(System.out);
        return new RandomRecommendationModel<>(
                getSeedValue(),
                datasetLoader.getRatingsDataset().getRatingsDomain().min(),
                datasetLoader.getRatingsDataset().getRatingsDomain().max());
    }

    @Override
    public Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> datasetLoader,
            RandomRecommendationModel<Long> model,
            long idUser,
            Set<Long> candidateItems)
            throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        LinkedList<Recommendation> recom = new LinkedList<>();

        int i = 0;
        for (long idItem : candidateItems) {
            recom.add(new Recommendation(idItem, model.predict(idUser, idItem)));
            i++;
        }
        return recom;
    }

    /**
     * Realiza las inicializaciones de este sistema de recomendación, añadiendo
     * un listener para cambios en el valor de la semilla y llamando a resetRandomValues.
     */
    private void init() {
        oldSeed = (Long) getParameterValue(SEED);

        addParammeterListener(() -> {
            long newSeed = (Long) getParameterValue(SEED);
            if (oldSeed != newSeed) {
                Global.showWarning("Reset " + getName() + " to seed = " + newSeed + "\n");
                oldSeed = newSeed;
                setSeedValue(newSeed);
            }
        });
    }

    @Override
    public void setSeedValue(long seedValue) {
        setParameterValue(SEED, seedValue);
    }

    @Override
    public long getSeedValue() {
        return (Long) getParameterValue(SEED);
    }
}
