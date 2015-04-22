package delfos.dataset.basic.rating;

import java.util.Collection;
import java.util.Map;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Set;

/**
 * Almacena un datasets de ratings. (idUser,idItem,Rating)
 *
 * @author Jorge Castro Gallardo
 * @version 1.0 (19 Octubre 2011)
 * @param <RatingType>
 */
public interface RatingsDataset<RatingType extends Rating> extends Iterable<RatingType> {

    /**
     * Devuelve la valoración que un usuario ha hecho sobre un item determinado
     *
     * @param idUser id del usuario para el que se desea conocer la valoración
     * @param idItem id del item para el que se desea conocer la valoración
     * @return valoración que el usuario ha hecho sobre el item. Si no ha
     * valorado el item, devuelve null.
     *
     * @throws UserNotFound Si el usuario no existe.
     * @throws ItemNotFound Si el producto no existe.
     */
    public RatingType getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound;

    /**
     * Obtiene el conjunto de los id de todos los usuarios que tienen
     * valoraciones en el dataset
     *
     * @return Conjunto de id de usuarios
     */
    public Set<Integer> allUsers();

    /**
     * Implementación por defecto del método que devuelve todos los items del
     * dataset. En datasets con un gran número de usuarios debería ser
     * implementada más eficientemente, ya que el orden de eficiencia de este
     * método es O(n . m) donde n=numero de usuarios y m = numero medio de items
     * valorados por el usuario.
     *
     * <p>
     * <p>
     * Para obtener todos los productos se debe usar el método
     * {@link ContentDataset#allItems()} y considerar la posibilidad de que un
     * producto no tenga valoraciones.
     *
     * @return Conjunto con los id de los items que han sido valorados
     */
    public Set<Integer> allRatedItems();

    /**
     * Devuelve las peliculas valoradas por un usuario
     *
     * @param idUser id del usuario para el que se quiere realizar la consulta
     * @return conjunto de id de items que ha valorado el usuario <n>idUser</n>
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public Collection<Integer> getUserRated(Integer idUser) throws UserNotFound;

    /**
     * Devuelve los usuarios que han valorado el item
     *
     * @param idItem id del item para el que se quiere consultar los usuarios
     * que lo han valorado
     * @return colección de id de los usuarios que han valorado el item
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public Collection<Integer> getItemRated(Integer idItem) throws ItemNotFound;

    /**
     * Devuelve las valoraciones de un usuario, indexadas por id de producto.
     *
     * @param idUser id del usuario para el que se quiere realizar la consulta
     * @return conjunto de id de items que ha valorado el usuario <n>idUser</n>.
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public Map<Integer, RatingType> getUserRatingsRated(Integer idUser) throws UserNotFound;

    /**
     * Devuelve las valoraciones sobre un producto, indexadas por id de usuario.
     *
     * @param idItem id del item para el que se quiere realizar la consulta
     * @return Conjunto de valoraciones sobre el producto.
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public Map<Integer, RatingType> getItemRatingsRated(Integer idItem) throws ItemNotFound;

    /**
     * Devuelve el rating medio del producto cuyo id se especifica.
     *
     * @param idItem producto para el que se desea obtener su valoración de
     * preferencia media.
     * @return valoración media del producto.
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public float getMeanRatingItem(int idItem) throws ItemNotFound;

    /**
     * Devuelve el la valoración media que un usuario ha dado a los productos.
     *
     * @param idUser usuario para el que se desea obtener la media de las
     * valoraciones que ha proporcionado
     * @return valoración media del usuario.
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public float getMeanRatingUser(int idUser) throws UserNotFound;

    /**
     * Devuelve el dominio de valoración de este dataset.
     *
     * @return Dominio de valoración del dataset.
     */
    public Domain getRatingsDomain();

    /**
     * Devuelve el número de valoraciones totales que tiene almacenado el
     * dataset <br> NOTA: Por defecto se calcula sumando el método
     * {@link RatingsDataset#sizeOfUserRatings(int)} por lo que puede ser
     * necesario sobreescribir el método para una implementación más eficiente.
     *
     * @return Número de valoraciones que todos los usuarios han hecho sobre los
     * productos.
     */
    public int getNumRatings();

    /**
     * Devuelve el número de valoraciones que un usuario ha hecho
     *
     * @param idUser usuario que se desea consultar
     * @return número de valoraciones que otorgó.
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    public int sizeOfUserRatings(int idUser) throws UserNotFound;

    /**
     * Devuelve el número de valoraciones que un producto tiemne
     *
     * @param idItem Producto que se desea consultar
     * @return número de valoraciones que otorgó.
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    public int sizeOfItemRatings(int idItem) throws ItemNotFound;

    /**
     * Comprueba si un usuario tiene valoraciones.
     *
     * @param idUser Usuario a comprobar
     * @return True si tiene valoraciones.
     * @throws UserNotFound Si no se encuentra el usuario especificado.
     */
    public boolean isRatedUser(int idUser) throws UserNotFound;

    /**
     * Comprueba si un producto tiene valoraciones.
     *
     * @param idItem Producto a comprobar
     * @return True si tiene valoraciones.
     * @throws ItemNotFound Si no se encuentra el producto especificado.
     */
    public boolean isRatedItem(int idItem) throws ItemNotFound;

    /**
     * Devuelve el valor medio de todas las valoraciones del conjunto.
     *
     * @return Valoración media.
     */
    public float getMeanRating();
}
