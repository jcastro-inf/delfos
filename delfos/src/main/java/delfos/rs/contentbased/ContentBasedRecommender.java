package delfos.rs.contentbased;

import java.util.Collection;
import java.util.List;
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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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
    protected abstract UserProfile makeUserProfile(int idUser, DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model) throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound, NotEnoughtUserInformation;

    @Override
    public final Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, Integer idUser, java.util.Set<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {
        UserProfile makeUserProfile;

        makeUserProfile = makeUserProfile(idUser, datasetLoader, model);
        return recommendOnly(datasetLoader, model, makeUserProfile, idItemList);
    }

    protected abstract Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, RecommendationModel model, UserProfile userProfile, Collection<Integer> idItemList) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset;
}
