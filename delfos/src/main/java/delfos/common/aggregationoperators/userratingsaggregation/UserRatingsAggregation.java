package delfos.common.aggregationoperators.userratingsaggregation;

import java.util.Collection;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;

/**
 * Agrega las valoraciones de un grupo de usuarios sobre un producto indicado.
 * Utiliza como ponderación el número de ratings de cada usuario, ya que cuanto
 * más activo es, más influyente es y tendrá un mayor peso en la agregación.
 *
 * <p>
 * <p>
 * Shlomo Berkovsky, Jill Freyne: Group-based recipe recommendations: analysis
 * of data aggregation strategies. RecSys '10 Proceedings of the fourth ACM
 * conference on Recommender systems Pages 111-118 ACM New York, NY, USA.
 *
* @author Jorge Castro Gallardo
 * @version 1.0 04-Julio-2013
 */
public interface UserRatingsAggregation {

    /**
     * Agrega las valoraciones de los usuarios indicados sobre el producto
     * indicado.
     *
     * @param ratingsDataset Dataset de valoraciones.
     * @param users Usuarios.
     * @param idItem Producto para el que se agregan las valoraciones de los
     * usuarios.
     * @return Valoración agregada sobre el producto.
     * @throws UserNotFound Si no existe alguno de los usuarios indicados.
     * @throws ItemNotFound Si no existe el producto indicado.
     */
    public Number aggregateRatings(
            RatingsDataset<? extends Rating> ratingsDataset,
            Collection<Integer> users,
            int idItem)
            throws UserNotFound, ItemNotFound;
}
