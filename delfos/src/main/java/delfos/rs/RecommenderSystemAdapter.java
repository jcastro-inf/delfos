package delfos.rs;

import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Interfaz que establece los métodos necesarios para implementar un sistema de
 * recomendación genérico
 * <p>
 * <b>Nota:</b> Los sistemas de recomendación basados en contenido deben heredar
 * de la clase <code>{@link ContentBasedRecommender}</code>.
 *
 *
 * @param <RecommendationModel> Clase que almacena el modelo de recomendación
 * del sistema.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 * @version 1.0 (19 Octubre 2011)
 * @version 2.0 26-Mayo-2013 Ahora los datasets se pasan por parámetro en cada
 * método.
 */
public abstract class RecommenderSystemAdapter<RecommendationModel>
        extends GenericRecommenderSystemAdapter<RecommendationModel>
        implements RecommenderSystem<RecommendationModel> {

    /**
     * Constructor por defecto de un sistema de recomendación a usuarios
     * individuales
     */
    protected RecommenderSystemAdapter() {
        super();
    }

    @Override
    public final ParameterOwnerType getParameterOwnerType() {
        return ParameterOwnerType.RECOMMENDER_SYSTEM;
    }

    @Override
    public Recommendations recommendToUser(
            DatasetLoader<? extends Rating> dataset,
            RecommendationModel recommendationModel,
            User user,
            Set<Item> candidateItems) {

        try {
            return new Recommendations(user, recommendToUser(
                    dataset,
                    recommendationModel,
                    user.getId(),
                    candidateItems.parallelStream()
                    .map((item) -> item.getId())
                    .collect(Collectors.toSet())));
        } catch (UserNotFound | CannotLoadRatingsDataset ex) {
            throw new IllegalArgumentException(ex);
        } catch (ItemNotFound | CannotLoadContentDataset ex) {
            throw new IllegalStateException(ex);
        } catch (NotEnoughtUserInformation ex) {
            return new Recommendations(
                    dataset,
                    candidateItems.parallelStream()
                    .map((item) -> new Recommendation(item, Double.NaN))
                    .collect(Collectors.toList()));
        }
    }

}
