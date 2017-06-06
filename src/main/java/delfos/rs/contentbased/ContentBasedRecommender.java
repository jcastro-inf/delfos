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
package delfos.rs.contentbased;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.rs.RecommenderSystemAdapter;
import delfos.rs.recommendation.Recommendation;

/**
 * Clase abstracta que proporciona los métodos para establecer el dataset de
 * contenido y la medida de similitud
 * <p>
 * <b>Nota:</b> Los sistemas de recomendación basados en contenido deben heredar
 * de esta clase.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (18 Octubre 2011)
 * @version 2.1 9-Octubre-2013 Incorporación del método makeUserModel
 * @param <RecommendationModel> Clase que almacena el modelo de recomendación
 * del sistema.
 * @param <UserProfile> Clase que almacena el perfil de usuario.
 *
 */
public abstract class ContentBasedRecommender<RecommendationModel, UserProfile> extends RecommenderSystemAdapter<RecommendationModel> {

    /**
     * Constructor por defecto de la clase. No realiza ninguna instrucción
     * especial, simplemente llama al constructor de la clase de la que extiende
     * {@link RecommenderSystemAdapter}
     */
    public ContentBasedRecommender() {
        super();
    }

    /**
     * {@inheritDoc }
     *
     * Los sistemas de recomendación basados en contenido, por lo general, no
     * predicen recomendaciones, por lo que el método devuelve falso.
     *
     * @return Falso, ya que los sistemas de recomendación basados en contenido
     * por lo general no utilizan predicción de recomendaciones.
     */
    @Override
    public final boolean isRatingPredictorRS() {
        return false;
    }

    /**
     * Construye el perfil de usuario que será usado dentro del método de
     * recomendación.
     *
     * @param idUser Identificador del usuario para el que se genera el perfil.
     * @param datasetLoader Dataset de entrada.
     * @param model Modelo de los productos.
     * @return
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     * @throws
     * delfos.common.exceptions.ratings.NotEnoughtUserInformation
     */
    protected abstract UserProfile makeUserProfile(long idUser, DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model) throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, NotEnoughtUserInformation;

    @Override
    public final Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> datasetLoader,
            RecommendationModel model,
            long idUser,
            Set<Long> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        UserProfile makeUserProfile;

        makeUserProfile = makeUserProfile(idUser, datasetLoader, model);
        return recommendOnly(datasetLoader, model, makeUserProfile, candidateItems);
    }

    protected abstract Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, UserProfile userProfile, Collection<Long> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset;
}
