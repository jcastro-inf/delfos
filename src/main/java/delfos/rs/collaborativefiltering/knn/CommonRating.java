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
package delfos.rs.collaborativefiltering.knn;

import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import java.util.Collection;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

/**
 * Entidad que encapsula un rating en común. rating1 y rating2 son valoraciones que tienen en común una entidad
 * <b>commonEntity</b> con id <b> idCommon</b>.
 *
 *
 * <p>
 * <p>
 * Se utiliza, por ejemplo, para almacenar que el usuario u54 y el usuario u98 valoraron el producto i46 con una
 * valoración de 3 y 5 respectivamente. En este caso, {@link CommonRating#getCommonEntity() } indicara que es un
 * producto y {@link CommonRating#getRatingEntity() } indicará que es un usuario. El método {@link CommonRating#getIdR1()
 * } devuelve 54 y el método {@link CommonRating#getIdR2()} devuelve 98.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date.
 * @version 1.1 20-Mar-2013
 * @version 1.2 19-Abril-2013 Completada la documentación de la clase.
 */
public class CommonRating {

    public static final Comparator<CommonRating> BY_ID_COMMON_ASC
            = (CommonRating o1, CommonRating o2) -> Long.compare(o1.idCommon, o2.idCommon);

    public static final Comparator<CommonRating> BY_ID_COMMON_DESC
            = BY_ID_COMMON_ASC.reversed();

    /**
     * Returns the intersection of the items rated by both users. This method is symmetric.
     *
     * @param datasetLoader Dataset from which the information is loaded.
     * @param user1 User 1.
     * @param user2 User 2.
     * @return The collection of ratings over the items rated by both users.
     */
    public static Collection<CommonRating> intersection(DatasetLoader<? extends Rating> datasetLoader, User user1, User user2) {

        final Map<Long, ? extends Rating> itemsRatedUser1 = datasetLoader.getRatingsDataset()
                .getUserRatingsRated(user1.getId());

        final Map<Long, ? extends Rating> itemsRatedUser2 = datasetLoader.getRatingsDataset()
                .getUserRatingsRated(user2.getId());

        Set<Long> intersection = new TreeSet<>();
        intersection.addAll(itemsRatedUser1.keySet());
        intersection.retainAll(itemsRatedUser2.keySet());

        final ContentDataset contentDataset = datasetLoader.getContentDataset();

        Set<Item> itemsIntersection = intersection.parallelStream()
                .map(idItem -> {
                    return contentDataset.get(idItem);
                })
                .collect(Collectors.toSet());

        Collection<CommonRating> commonRatings = itemsIntersection.parallelStream().map(item -> {

            double ratingUser1 = itemsRatedUser1
                    .get(item.getId())
                    .getRatingValue().doubleValue();

            double ratingUser2 = itemsRatedUser2
                    .get(item.getId())
                    .getRatingValue().doubleValue();

            return new CommonRating(
                    RecommendationEntity.ITEM,
                    item.getId(),
                    RecommendationEntity.USER,
                    user1.getId(),
                    user2.getId(),
                    ratingUser1, ratingUser2);
        }).collect(Collectors.toList());

        return commonRatings;
    }

    public static Collection<CommonRating> intersection(DatasetLoader<? extends Rating> datasetLoader, Item item1, Item item2) {
        final Map<Long, ? extends Rating> ratingsOverItem1 = datasetLoader.getRatingsDataset()
                .getItemRatingsRated(item1.getId());

        final Map<Long, ? extends Rating> ratingsOverItem2 = datasetLoader.getRatingsDataset()
                .getItemRatingsRated(item2.getId());

        Set<User> intersection
                = ratingsOverItem1.keySet().parallelStream()
                .map(idUser -> datasetLoader.getUsersDataset().get(idUser))
                .filter(user -> ratingsOverItem2.containsKey(user.getId()))
                .collect(Collectors.toSet());

        Collection<CommonRating> commonRatings = intersection.parallelStream().map(user -> {

            double ratingUser1 = ratingsOverItem1
                    .get(user.getId())
                    .getRatingValue().doubleValue();

            double ratingUser2 = ratingsOverItem2
                    .get(user.getId())
                    .getRatingValue().doubleValue();

            return new CommonRating(
                    RecommendationEntity.USER,
                    user.getId(),
                    RecommendationEntity.ITEM,
                    item1.getId(),
                    item2.getId(),
                    ratingUser1, ratingUser2);
        }).collect(Collectors.toList());

        return commonRatings;
    }

    /**
     * Tipo de la entidad común, es decir, tipo al que todos los ratings se refieren.
     */
    private final RecommendationEntity commonEntity;
    private final RecommendationEntity ratingEntity;
    private final long idCommon;
    private final double rating1;
    private final double rating2;
    private final long idR1;
    private final long idR2;
    private Double weight = null;

    /**
     * Crea una entidad de rating común, que almacena dos ratings dados sobre una misma entidad.
     *
     * @param commonEntity Tipo de la entidad a la que se refieren las dos valoraciones.
     * @param idCommon Id de la entidad en común.
     * @param ratingEntity Tipo de la entidad que es distinta.
     * @param idR1 Id de la entidad distinta 1.
     * @param idR2 Id de la entidad distinta 2.
     * @param rating1 Valoración 1.
     * @param rating2 Valoración 2.
     * @param weight Ponderación de la valoración.
     */
    public CommonRating(RecommendationEntity commonEntity, int idCommon, RecommendationEntity ratingEntity, int idR1, int idR2, double rating1, double rating2, double weight) {
        this.commonEntity = commonEntity;
        this.ratingEntity = ratingEntity;
        this.idCommon = idCommon;
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.idR1 = idR1;
        this.idR2 = idR2;
        this.weight = weight;
    }

    /**
     * Crea una entidad de rating común, que almacena dos ratings dados sobre una misma entidad.
     *
     * @param commonEntity Tipo de la entidad a la que se refieren las dos valoraciones.
     * @param idCommon Id de la entidad en común.
     * @param ratingEntity Tipo de la entidad que es distinta.
     * @param idR1 Id de la entidad distinta 1.
     * @param idR2 Id de la entidad distinta 2.
     * @param rating1 Valoración 1.
     * @param rating2 Valoración 2.
     */
    public CommonRating(RecommendationEntity commonEntity,
                        long idCommon, RecommendationEntity ratingEntity,
                        long idR1,
                        long idR2, double rating1, double rating2) {
        this.commonEntity = commonEntity;
        this.ratingEntity = ratingEntity;
        this.idCommon = idCommon;
        this.rating1 = rating1;
        this.rating2 = rating2;
        this.idR1 = idR1;
        this.idR2 = idR2;
    }

    /**
     * Devuelve el tipo de la entidad común, es decir, tipo al que todos los ratings se refieren.
     *
     * @return Tipo de la entidad común, es decir, tipo al que todos los ratings se refieren.
     */
    public RecommendationEntity getCommonEntity() {
        return commonEntity;
    }

    /**
     * Devuelve el ID de la entidad en común. La entidad de recomendación será un usuario o un producto, dependiendo del
     * valor del método {@link CommonRating#commonEntity}.
     *
     * @return ID de la entidad en común.
     */
    public long getIdCommon() {
        return idCommon;
    }

    /**
     * Devuelve el ID de la entidad de valoración 1, que será un usuario o un producto dependiendo del valor del método
     * {@link CommonRating#ratingEntity}.
     *
     * @return ID de la entidad de valoración 1.
     */
    public long getIdR1() {
        return idR1;
    }

    /**
     * Devuelve el ID de la entidad de valoración 2, que será un usuario o un producto dependiendo del valor del método
     * {@link CommonRating#ratingEntity}.
     *
     * @return ID de la entidad de valoración 2.
     */
    public long getIdR2() {
        return idR2;
    }

    /**
     * Devuelve la valoración del objeto el común y la entidad de valoración 1.
     *
     * @return Valoración de la entidad de valoración 1.
     */
    public double getRating1() {
        return rating1;
    }

    /**
     * Devuelve la valoración del objeto el común y la entidad de valoración 2.
     *
     * @return Valoración de la entidad de valoración 2.
     */
    public double getRating2() {
        return rating2;
    }

    /**
     * Devuelve el tipo de la entidad de valoración, es decir, las entidades diferentes.
     *
     * @return Tipo de la entidad de valoración.
     */
    public RecommendationEntity getRatingEntity() {
        return ratingEntity;
    }

    /**
     * Devuelve la ponderación del rating en común.
     *
     * @return Ponderación del rating en común.
     */
    public double getWeight() {
        return weight;
    }

    /**
     * Devuelve true si el rating en común tiene una ponderación definida.
     *
     * @return true si el rating en común tiene una ponderación definida.
     */
    public boolean isWeighted() {
        return weight != null;
    }

    /**
     * Establece la ponderación de esta valoración en común.
     *
     * @param weight Ponderación de la valoración en común.
     */
    public void setWeight(double weight) {
        this.weight = weight;
    }

    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();

        switch (ratingEntity) {
            case USER:
                str.append("Users");
                break;
            case ITEM:
                str.append("Items");
                break;
            default:
                throw new IllegalStateException("Unknown ratingEntity: " + ratingEntity);
        }

        str.append("(").append(idR1).append(",").append(idR2).append(")");

        str.append(" rated ");
        switch (commonEntity) {
            case USER:
                str.append("User");
                break;
            case ITEM:
                str.append("Item");
                break;
            default:
                throw new IllegalStateException("Unknown ratingEntity: " + ratingEntity);
        }
        str.append("(").append(idCommon).append(")");

        str.append(" --> ").append("(").append(rating1).append(",").append(rating2).append(")");

        return str.toString();
    }

}
