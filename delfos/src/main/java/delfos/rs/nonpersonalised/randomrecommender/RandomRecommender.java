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

/**
 * Sistema de recomendación que realiza la recomendación de manera aleatoria. No
 * se debe utilizar en un sistema real, sino que se utiliza para comprobar una
 * cota mínima de las métricas de evaluación que revisan la eficacia de los
 * sistemas de recomendación, como por ejemplo, calcular el peor mae que un
 * sistema de recomendación puede tener en un dataset dado.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknown date
 * @version 1.1 (28 de Febrero de 2013)
 */
public class RandomRecommender extends CollaborativeRecommender<RandomRecommendationModel<Integer>> implements SeedHolder {

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
    public RandomRecommendationModel<Integer> build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset {

        System.out.println("Dataset Alias: " + datasetLoader.getAlias());
        System.out.println("#Ratings: \t" + datasetLoader.getRatingsDataset().getNumRatings());
        System.out.println("#Users:   \t" + datasetLoader.getRatingsDataset().allUsers().size());
        System.out.println("#Items:   \t" + datasetLoader.getRatingsDataset().allRatedItems().size());

        HistogramNumbersSmart histogramSmart = new HistogramNumbersSmart(0.5);
        for (Rating rating : datasetLoader.getRatingsDataset()) {
            histogramSmart.addValue(rating.ratingValue.doubleValue());
        }

        histogramSmart.printHistogram(System.out);
        return new RandomRecommendationModel<>(
                getSeedValue(),
                datasetLoader.getRatingsDataset().getRatingsDomain().min(),
                datasetLoader.getRatingsDataset().getRatingsDomain().max());
    }

    @Override
    public Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RandomRecommendationModel<Integer> model, Integer idUser, java.util.Set<Integer> idItemList) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        LinkedList<Recommendation> recom = new LinkedList<>();

        int i = 0;
        for (int idItem : idItemList) {
            recom.add(new Recommendation(idItem, model.predict(idUser, idItem)));
            i++;
        }
        return recom;
    }

    /**
     * Realiza las inicializaciones de este sistema de recomendación, añadiendo
     * un listener para cambios en el valor de la semilla y llamando a
     * {@link RandomRecommender#resetRandomValues()}.
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
