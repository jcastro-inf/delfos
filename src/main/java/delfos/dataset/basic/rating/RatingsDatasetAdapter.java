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

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.rating.domain.Domain;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Clase que almacena un datasets de ratings. (idUser,idItem,Rating)
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 * @version 1.0 (19 Octubre 2011)
 * @param <RatingType>
 */
public abstract class RatingsDatasetAdapter<RatingType extends Rating> implements RatingsDataset<RatingType> {

    /**
     * Buffer para almacenar la valoración media de cada usuario.
     */
    protected final Map<Long, Double> mediaUsers = Collections.synchronizedMap(new TreeMap<Long, Double>());
    /**
     * Buffer para almacenar la valoración media de cada producto.
     */
    protected final Map<Long, Double> mediaItems = Collections.synchronizedMap(new TreeMap<Long, Double>());

    /**
     * Devuelve la valoración que un usuario ha hecho sobre un item determinado
     *
     * @param idUser id del usuario para el que se desea conocer la valoración
     * @param idItem id del item para el que se desea conocer la valoración
     * @return valoración que el usuario ha hecho sobre el item. Si no ha valorado el item, devuelve null
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     */
    @Override
    public abstract RatingType getRating(long idUser, long idItem) throws UserNotFound, ItemNotFound;

    /**
     * Obtiene el conjunto de los id de todos los usuarios que tienen valoraciones en el dataset
     *
     * @return Conjunto de id de usuarios
     */
    @Override
    public abstract Set<Long> allUsers();

    /**
     * Implementación por defecto del método que devuelve todos los items del dataset. En datasets con un gran número de
     * usuarios debería ser implementada más eficientemente, ya que el orden de eficiencia de este método es O(n . m)
     * donde n=numero de usuarios y m = numero medio de items valorados por el usuario.
     *
     * @return Conjunto con los id de los items que han sido valorados
     */
    @Override
    public abstract Set<Long> allRatedItems();

    /**
     * Devuelve las peliculas valoradas por un usuario
     *
     * @param idUser id del usuario para el que se quiere realizar la consulta
     * @return conjunto de id de items que ha valorado el usuario <b>idUser</b>
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     */
    @Override
    public abstract Set<Long> getUserRated(long idUser) throws UserNotFound;

    /**
     * Devuelve los usuarios que han valorado el item
     *
     * @param idItem id del item para el que se quiere consultar los usuarios que lo han valorado
     * @return colección de id de los usuarios que han valorado el item
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     */
    @Override
    public abstract Set<Long> getItemRated(long idItem) throws ItemNotFound;

    /**
     * Devuelve las peliculas valoradas por un usuario
     *
     * @param idUser id del usuario para el que se quiere realizar la consulta
     * @return conjunto de id de items que ha valorado el usuario <b>idUser</b>
     * @throws delfos.common.exceptions.dataset.users.UserNotFound
     */
    @Override
    public abstract Map<Long, RatingType> getUserRatingsRated(long idUser) throws UserNotFound;

    /**
     * Devuelve los usuarios que han valorado un item y su valoracion concreta
     *
     * @param idItem id del item para el que se quiere realizar la consulta
     * @return conjunto de id de items que ha valorado el usuario <b>idUser</b>
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     */
    @Override
    public abstract Map<Long, RatingType> getItemRatingsRated(long idItem) throws ItemNotFound;

    /**
     * Devuelve el ratingValue medio del producto cuyo id se especifica
     *
     * @param idItem producto para el que se desea obtener su valoración de preferencia media
     * @return valoración media del producto
     * @throws delfos.common.exceptions.dataset.items.ItemNotFound
     */
    @Override
    public double getMeanRatingItem(long idItem) throws ItemNotFound {

        synchronized (mediaItems) {
            if (!mediaItems.containsKey(idItem)) {
                Map<Long, RatingType> actualRatings = getItemRatingsRated(idItem);
                double media = 0;
                for (RatingType rating : actualRatings.values()) {
                    media += rating.getRatingValue().doubleValue() / actualRatings.size();
                }
                mediaItems.put(idItem, media);
            }
            Double get = mediaItems.get(idItem);
            if (get == null) {
                throw new ItemNotFound(idItem);
            }
            return get;
        }
    }

    /**
     * Devuelve el la valoración media que un usuario ha dado a los productos.
     *
     * @param idUser usuario para el que se desea obtener la media de las valoraciones que ha proporcionado
     * @return valoración media del usuario
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    @Override
    public double getMeanRatingUser(long idUser) throws UserNotFound {
        if (!mediaUsers.containsKey(idUser)) {
            Map<Long, RatingType> actualRatings = getUserRatingsRated(idUser);
            double media = 0;
            for (RatingType rating : actualRatings.values()) {
                media += rating.getRatingValue().doubleValue() / actualRatings.size();
            }
            mediaUsers.put(idUser, media);
        }
        Double get = mediaUsers.get(idUser);
        if (get == null) {
            throw new UserNotFound(idUser);
        }
        return get;
    }

    @Override
    public abstract Domain getRatingsDomain();

    /**
     * Devuelve el número de valoraciones totales que tiene almacenado el dataset <br> NOTA: Por defecto se calcula
     * sumando el método {@link RatingsDataset#sizeOfUserRatings(long)} por lo que puede ser necesario sobreescribir el
     * método para una implementación más eficiente.
     *
     * @return Número de valoraciones que todos los usuarios han hecho sobre los productos.
     */
    @Override
    public long getNumRatings() {
        long size = 0;
        for (long idUser : allUsers()) {
            try {
                size += sizeOfUserRatings(idUser);
            } catch (UserNotFound ex) {
                Global.showError(ex);
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }
        return size;
    }

    /**
     * Devuelve el número de valoraciones que un usuario ha hecho
     *
     * @param idUser usuario que se desea consultar
     * @return número de valoraciones que otorgó
     *
     * @throws UserNotFound Si el usuario no existe.
     */
    @Override
    public long sizeOfUserRatings(long idUser) throws UserNotFound {
        return getUserRated(idUser).size();
    }

    /**
     * Devuelve el número de valoraciones que un producto tiemne
     *
     * @param idItem Producto que se desea consultar
     * @return número de valoraciones que otorgó.
     *
     * @throws ItemNotFound Si el producto no existe.
     */
    @Override
    public long sizeOfItemRatings(long idItem) throws ItemNotFound {
        return getItemRated(idItem).size();
    }

    /**
     * Comprueba si un usuario tiene valoraciones.
     *
     * @param idUser Usuario a comprobar
     * @return True si tiene valoraciones.
     * @throws UserNotFound Si no se encuentra el usuario especificado.
     */
    @Override
    public boolean isRatedUser(long idUser) throws UserNotFound {
        return sizeOfUserRatings(idUser) != 0;
    }

    /**
     * Comprueba si un producto tiene valoraciones.
     *
     * @param idItem Producto a comprobar
     * @return True si tiene valoraciones.
     * @throws ItemNotFound Si no se encuentra el producto especificado.
     */
    @Override
    public boolean isRatedItem(long idItem) throws ItemNotFound {
        return sizeOfItemRatings(idItem) != 0;
    }

    @Override
    public final Iterator<RatingType> iterator() {
        return new IteratorRatingsDataset<>(this);
    }
    private double meanRatingValue = Double.NaN;

    @Override
    public double getMeanRating() {

        if (Double.isNaN(meanRatingValue)) {
            synchronized (this) {
                MeanIterative meanRating = new MeanIterative();
                for (Rating r : this) {
                    meanRating.addValue(r.getRatingValue().doubleValue());
                }
                meanRatingValue = (double) meanRating.getMean();
            }

        }
        return meanRatingValue;
    }

    /**
     * Devuelve el ratingValue medio del dataset indicado por parámetro.
     *
     * @param <RatingType>
     * @param ratingsDataset
     * @return
     */
    public static <RatingType extends Rating> double getMeanRating(RatingsDataset<RatingType> ratingsDataset) {
        MeanIterative meanRating = new MeanIterative();
        for (Rating r : ratingsDataset) {
            meanRating.addValue(r.getRatingValue().doubleValue());
        }

        return (double) meanRating.getMean();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof RatingsDataset) {
            RatingsDataset<? extends Rating> otherRatingsDataset = (RatingsDataset) obj;

            Set<Long> thisAllUsers = new TreeSet<>(this.allUsers());
            Set<Long> otherAllUsers = new TreeSet<>(otherRatingsDataset.allUsers());

            if (!thisAllUsers.equals(otherAllUsers)) {
                return false;
            }

            for (long idUser : thisAllUsers) {
                try {
                    Map<Long, ? extends Rating> thisUserRatingsRated = this.getUserRatingsRated(idUser);
                    Map<Long, ? extends Rating> otherUserRatingsRated = otherRatingsDataset.getUserRatingsRated(idUser);

                    Set<Long> thisDatasetItemsRated = new TreeSet<>(thisUserRatingsRated.keySet());
                    Set<Long> otherDatasetItemsRated = new TreeSet<>(otherUserRatingsRated.keySet());

                    if (!thisDatasetItemsRated.equals(otherDatasetItemsRated)) {
                        return false;
                    }

                    for (long item : thisDatasetItemsRated) {
                        Rating thisRating = thisUserRatingsRated.get(item);
                        Rating otherRating = otherUserRatingsRated.get(item);

                        if (!thisRating.equals(otherRating)) {
                            return false;
                        }
                    }

                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            }
            return true;
        }
        return super.equals(obj); //To change body of generated methods, choose Tools | Templates.
    }

    private Integer hashCodeBuffer = null;

    @Override
    public synchronized int hashCode() {
        if (hashCodeBuffer == null) {
            hashCodeBuffer = hashCode(this);
        }
        return hashCodeBuffer;
    }

    public static <RatingType extends Rating> int hashCode(RatingsDataset<RatingType> ratingsDataset) {
        HashCodeBuilder hashCodeBuilder = new HashCodeBuilder(37, 11);

        List<Long> usersSorted = ratingsDataset.allUsers().stream().collect(Collectors.toList());
        usersSorted.sort((i1, i2) -> Long.compare(i1, i2));

        List<Long> itemsSorted = ratingsDataset.allRatedItems().stream().collect(Collectors.toList());
        itemsSorted.sort((i1, i2) -> Long.compare(i1, i2));

        for (long idUser : usersSorted) {
            hashCodeBuilder.append(idUser);
            try {
                Map<Long, RatingType> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);

                List<Long> thisUserItemsSorted = userRatingsRated.keySet().stream().sorted((i1, i2) -> Long.compare(i1, i2)).collect(Collectors.toList());
                for (Long idItem : thisUserItemsSorted) {
                    RatingType rating = userRatingsRated.get(idItem);
                    double ratingValue = rating.getRatingValue().doubleValue();
                    hashCodeBuilder.append(idItem);
                    hashCodeBuilder.append(ratingValue);
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        return hashCodeBuilder.hashCode();
    }

}
