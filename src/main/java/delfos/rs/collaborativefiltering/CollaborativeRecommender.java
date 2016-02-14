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
package delfos.rs.collaborativefiltering;

import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.CannotLoadUsersDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.TreeSet;

/**
 * Interface of a collaborative recommender. Collaborative recommenders are, by
 * definition, rating predictors. Therefore the preference of the
 * recommendations must be given on the ratingsDomain of the dataset.
 *
 * <p>
 * <p>
 * Classes that inhterit {@link CollaborativeRecommender} must call this class
 * default constructor (super) on every constructor that they implement.
 * Otherwise, the correct behaviour is not guaranteed.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 2.0 Unknown date
 * @version 2.1 (18-Feb-2013)
 * @param <RecommendationModel> Modelo de recomendación del sistema.
 */
public abstract class CollaborativeRecommender<RecommendationModel>
        extends RecommenderSystemAdapter<RecommendationModel> {

    /**
     * Constructor por defecto de un sistema de recomendación colaborativo.
     */
    public CollaborativeRecommender() {
        super();
    }

    /**
     * Los sistemas de recomendación colaborativos funcionan prediciendo la
     * valoración para los productos no valorados, por lo que siempre devuelve
     * true;
     *
     * {@inheritDoc }
     *
     * @return
     */
    @Override
    public final boolean isRatingPredictorRS() {
        return true;
    }

    @Override
    public abstract RecommendationModel buildRecommendationModel(
            DatasetLoader<? extends Rating> datasetLoader)
            throws CannotLoadRatingsDataset, CannotLoadContentDataset, CannotLoadUsersDataset;

    /**
     * Transforma una predicción del sistema de recomendación en una predicción
     * dentro del rango de valoraciones. De esta manera se mejora el error de
     * predicción.
     *
     * @param datasetLoader Conjunto de datos.
     * @param rating Predicción en bruto.
     * @return Predicción dentro del rango de valoración.
     */
    public Number toRatingRange(DatasetLoader<? extends Rating> datasetLoader, Number rating) {
        try {
            if (rating.floatValue() < datasetLoader.getRatingsDataset().getRatingsDomain().min().doubleValue()) {
                return datasetLoader.getRatingsDataset().getRatingsDomain().min();
            }
            if (rating.floatValue() > datasetLoader.getRatingsDataset().getRatingsDomain().max().doubleValue()) {
                return datasetLoader.getRatingsDataset().getRatingsDomain().max();
            }
            return rating;
        } catch (CannotLoadRatingsDataset ex) {
            throw new IllegalStateException(ex);
        }
    }

    /**
     * Predice la valoración que un usuario daría a un producto, utilizando el
     * sistema de recomendación.
     *
     * @param datasetLoader Conjunto de datos.
     * @param model Modelo de recomendación.
     * @param idUser Usuario para el que se realiza la predicción.
     * @param idItem Producto para el que se realiza la predicción.
     * @return Valoración predicha del usuario sobre el producto. Si no se puede
     * calcular, devuelve null (indica un fallo de cobertura)
     * @throws UserNotFound Si no se encuentra el usuario en los datasets.
     * @throws ItemNotFound Si no se encuenta el producto en los datasets.
     * @throws delfos.common.exceptions.ratings.NotEnoughtUserInformation
     */
    public Number predictRating(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, int idUser, int idItem)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        if (Global.isVerboseAnnoying()) {
            Global.showInfoMessage("Predicting rating of user " + idUser + " over item " + idItem + "\n");
            try {
                Global.showInfoMessage("Valoración real " + datasetLoader.getRatingsDataset().getRating(idUser, idItem) + "\n");
                Global.showInfoMessage("Media user " + datasetLoader.getRatingsDataset().getMeanRatingUser(idUser) + "\n");
                Global.showInfoMessage("Media item " + datasetLoader.getRatingsDataset().getMeanRatingItem(idItem) + "\n");
            } catch (CannotLoadRatingsDataset ex) {
                throw new IllegalStateException(ex);
            }
        }

        TreeSet<Integer> items = new TreeSet<>();
        items.add(idItem);

        Collection<Recommendation> recommendOnly = recommendToUser(datasetLoader, model, idUser, items);
        if (recommendOnly.isEmpty()) {
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Prediction of rating of user " + idUser + " over item " + idItem + " can't be predicted\n");
            }
            return null;
        } else {
            double prediction = recommendOnly.iterator().next().getPreference().doubleValue();
            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("Prediction of rating of user " + idUser + " over item " + idItem + " ---> " + prediction + "\n");
            }
            return prediction;
        }
    }

}
