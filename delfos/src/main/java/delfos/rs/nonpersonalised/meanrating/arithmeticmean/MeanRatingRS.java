package delfos.rs.nonpersonalised.meanrating.arithmeticmean;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.persistence.database.DAOMeanRatingProfile;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Sistema de recomendación que realiza la recomendación basándose en el rating
 * medio de los productos. No se recomienda utilizar este sistema de
 * recomendación en un sistema real como sistema de recomendación principal. Se
 * puede usar para tareas complementarias, como calcular recomendaciones en caso
 * de que el usuario no obtenga recomendaciones con otros sistemas o para
 * deshacer empates entre productos.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 *
 * @version 1.0 (03‎ de Jjulio‎ de ‎2012)
 * @version 1.1 (28 de Febrero de 2013)
 * @version 1.2 21-Mar-2013 Implementada la persistencia en base de datos.
 */
public class MeanRatingRS extends CollaborativeRecommender<MeanRatingRSModel> {

    private static final long serialVersionUID = 1L;

    /**
     * Constructor por defecto que sólo llama al constructor de la superclase.
     */
    public MeanRatingRS() {
        super();
    }

    @Override
    public MeanRatingRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadRatingsDataset {
        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        Collection<Integer> allItems = ratingsDataset.allRatedItems();

        List<MeanRating> rangedMeanRatings = new ArrayList<MeanRating>(allItems.size());

        float i = 0;
        for (int idItem : allItems) {
            MeanIterative mean = new MeanIterative();

            try {
                Map<Integer, ? extends Rating> map = ratingsDataset.getItemRatingsRated(idItem);
                for (Rating rating : map.values()) {
                    mean.addValue(rating.ratingValue.floatValue());
                }
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }

            if (mean.getNumValues() > 0) {
                rangedMeanRatings.add(new MeanRating(idItem, mean.getMean()));
            }
            i++;
            float percent = i / allItems.size();
            fireBuildingProgressChangedEvent("Calculating means", (int) (percent * 100), -1);
        }
        Collections.sort(rangedMeanRatings);
        return new MeanRatingRSModel(rangedMeanRatings);
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, MeanRatingRSModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        Collection<Recommendation> recom = new LinkedList<>();
        Iterator<MeanRating> iterator = model.getRangedMeanRatings().listIterator();

        int i = 0;
        while (iterator.hasNext()) {
            MeanRating next = iterator.next();
            if (candidateItems.contains(next.getIdItem())) {
                recom.add(new Recommendation(next.getIdItem(), next.getPreference()));
            }
            i++;
        }

        return recom;
    }

    @Override
    public MeanRatingRSModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Integer> users, Collection<Integer> items) throws FailureInPersistence {
        DAOMeanRatingProfile dAOMeanRatingProfile = new DAOMeanRatingProfile();
        return dAOMeanRatingProfile.loadModel(databasePersistence, users, items);
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, MeanRatingRSModel model) throws FailureInPersistence {
        DAOMeanRatingProfile dAOMeanRatingProfile = new DAOMeanRatingProfile();
        dAOMeanRatingProfile.saveModel(databasePersistence, model);
    }
}
