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
