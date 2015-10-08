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
import java.util.Collection;
import java.util.Set;

/**
 * Interface of a single-user recommender system, which recommends items to
 * users. This interface provides the recommendation method.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
    public Recommendations recommendToUser(
            DatasetLoader<? extends Rating> dataset,
            RecommendationModel model,
            User user,
            Set<Item> candidateItems);
}
