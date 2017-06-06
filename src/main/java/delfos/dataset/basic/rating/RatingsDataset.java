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
package delfos.dataset.basic.rating;

import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Map;
import java.util.Set;

/**
 * Almacena un datasets de ratings. (idUser,idItem,Rating)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 Octubre 2011)
 * @param <RatingType>
 */
public interface RatingsDataset<RatingType extends Rating> extends Iterable<RatingType> {

    /**
     * Devuelve la valoración que un usuario ha hecho sobre un item determinado
     *
     * @param idUser id del usuario para el que se desea conocer la valoración
     * @param idItem id del item para el que se desea conocer la valoración
     * @return valoración que el usuario ha hecho sobre el item. Si no ha valorado el item, devuelve null.
     *
     * @throws UserNotFound Si el usuario no existe.
     * @throws ItemNotFound Si el producto no existe.
     */
    public RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound;

    /**
     * Obtiene el conjunto de los id de todos los usuarios que tienen valoraciones en el dataset
     *
     * @return Conjunto de id de usuarios
     */
    public Set<Long> allUsers();

    /**
     * Implementación por defecto del método que devuelve todos los items del dataset. En datasets con un gran número de
     * usuarios debería ser implementada más eficientemente, ya que el orden de eficiencia de este método es O(n . m)
     * donde n=numero de usuarios y m = numero medio de items valorados por el usuario.
     *
     * <p>
     * <p>
     * Para obtener todos los productos se debe usar el método ContentDataset.allItems() y considerar la
     * posibilidad de que un producto no tenga valoraciones.
     *
     * @return Conjunto con los id de los items que han sido valorados
     */
    public Set<Long> allRatedItems();

    /**
     * Devuelve las peliculas valoradas por un usuario
     *
     * @param idUser id del usuario para el que se quiere realizar la consulta
     * @return conjunto de id de items que ha valorado el usuario <n>idUser</n>
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public Set<Long> getUserRated(long idUser) throws UserNotFound;

    /**
     * Devuelve los usuarios que han valorado el item
     *
     * @param idItem id del item para el que se quiere consultar los usuarios que lo han valorado
     * @return colección de id de los usuarios que han valorado el item
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public Set<Long> getItemRated(long idItem) throws ItemNotFound;

    /**
     * Devuelve las valoraciones de un usuario, indexadas por id de producto.
     *
     * @param idUser id del usuario para el que se quiere realizar la consulta
     * @return conjunto de id de items que ha valorado el usuario <n>idUser</n>.
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound;

    /**
     * Devuelve las valoraciones sobre un producto, indexadas por id de usuario.
     *
     * @param idItem id del item para el que se quiere realizar la consulta
     * @return Conjunto de valoraciones sobre el producto.
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound;

    /**
     * Devuelve el rating medio del producto cuyo id se especifica.
     *
     * @param idItem producto para el que se desea obtener su valoración de preferencia media.
     * @return valoración media del producto.
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public double getMeanRatingItem(long idItem) throws ItemNotFound;

    /**
     * Devuelve el la valoración media que un usuario ha dado a los productos.
     *
     * @param idUser usuario para el que se desea obtener la media de las valoraciones que ha proporcionado
     * @return valoración media del usuario.
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public double getMeanRatingUser(long idUser) throws UserNotFound;

    /**
     * Devuelve el dominio de valoración de este dataset.
     *
     * @return Dominio de valoración del dataset.
     */
    public Domain getRatingsDomain();

    /**
     * Devuelve el número de valoraciones totales que tiene almacenado el dataset <br> NOTA: Por defecto se calcula
     * sumando el método {@link RatingsDataset#sizeOfUserRatings(long)} por lo que puede ser necesario sobreescribir el
     * método para una implementación más eficiente.
     *
     * @return Número de valoraciones que todos los usuarios han hecho sobre los productos.
     */
    public long getNumRatings();

    /**
     * Devuelve el número de valoraciones que un usuario ha hecho
     *
     * @param idUser usuario que se desea consultar
     * @return número de valoraciones que otorgó.
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public long sizeOfUserRatings(long idUser) throws UserNotFound;

    /**
     * Devuelve el número de valoraciones que un producto tiemne
     *
     * @param idItem Producto que se desea consultar
     * @return número de valoraciones que otorgó.
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public long sizeOfItemRatings(long idItem) throws ItemNotFound;

    /**
     * Comprueba si un usuario tiene valoraciones.
     *
     * @param idUser Usuario a comprobar
     * @return True si tiene valoraciones.
     * @throws UserNotFound Si no se encuentra el usuario especificado.
     */
    public boolean isRatedUser(long idUser) throws UserNotFound;

    /**
     * Comprueba si un producto tiene valoraciones.
     *
     * @param idItem Producto a comprobar
     * @return True si tiene valoraciones.
     * @throws ItemNotFound Si no se encuentra el producto especificado.
     */
    public boolean isRatedItem(long idItem) throws ItemNotFound;

    /**
     * Devuelve el valor medio de todas las valoraciones del conjunto.
     *
     * @return Valoración media.
     */
    public double getMeanRating();

}
