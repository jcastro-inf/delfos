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
package delfos.rs;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtItemInformation;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Collection;
import java.util.Set;

/**
 * Interface of a single-user recommender system, which recommends items to
 * users. This interface provides the recommendation method.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 08-Mar-2013
 * @version 2.0 26-Mayo-2013 Ahora los datasets se pasan por parámetro en cada
 * método.
 * @param <RecommendationModel>
 */
public interface RecommenderSystem<RecommendationModel>
        extends GenericRecommenderSystem<RecommendationModel> {

    /**
     * Método para la realización de una recomendación al usuario <i>idUser</i>
     * en una ejecución de evaluación del sistema de recomendación. La lista de
     * películas que se pueden recomendar viene determinada por el parámetro
     * <i>candidateItems</i>, que generalmente vendrá determinado por un
     * conjunto de test.
     *
     * <p>
     * <p>
     * Este método no comprueba si un usuario (o item) existe o no, por lo que
     * ya no lanza las excepciones {@link UserNotFound}, {@link ItemNotFound},
     * en su lugar lanza las excepciones {@link NotEnoughtUserInformation} y
     * {@link NotEnoughtItemInformation}, para indicar que no pudo generar su
     * correspondiente perfil.
     *
     * @param dataset Establece el dataset que se usará en la recomendación.
     * @param model Modelo de recomendación que se usará en la recomendación.
     * @param idUser id del usuario para el que se realiza la recomendación
     * @param candidateItems Lista de productos que pueden ser recomendados al
     * usuario.
     * @return Lista de recomendaciones ordenada por la métrica que utiliza el
     * sistema de recomendación (similarity o predicted rating).
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     *
     * @throws NotEnoughtUserInformation
     */
    @Deprecated
    public Collection<Recommendation> recommendToUser(
            DatasetLoader<? extends Rating> dataset,
            RecommendationModel model,
            Integer idUser,
            Set<Integer> candidateItems)
            throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation;

    /**
     * Método para la realización de una recomendación al usuario <i>idUser</i>
     * en una ejecución de evaluación del sistema de recomendación. La lista de
     * películas que se pueden recomendar viene determinada por el parámetro
     * <i>candidateItems</i>, que generalmente vendrá determinado por un
     * conjunto de test.
     *
     * <p>
     * <p>
     * Este método no comprueba si un usuario (o item) existe o no, por lo que
     * ya no lanza las excepciones {@link UserNotFound}, {@link ItemNotFound},
     * en su lugar lanza las excepciones {@link NotEnoughtUserInformation} y
     * {@link NotEnoughtItemInformation}, para indicar que no pudo generar su
     * correspondiente perfil.
     *
     * @param dataset Establece el dataset que se usará en la recomendación.
     * @param model Modelo de recomendación que se usará en la recomendación.
     * @param user usuario al que van dirigidas las recomendaciones
     * @param candidateItems Lista de productos que pueden ser recomendados al
     * usuario.
     * @return Objeto con los resultados de la recomendación.
     */
    public RecommendationsToUser recommendToUser(
            DatasetLoader<? extends Rating> dataset, RecommendationModel model, User user, Set<Item> candidateItems);
}
