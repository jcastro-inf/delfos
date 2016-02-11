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
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.ParameterOwnerType;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.loader.types.UsersDatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
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
            TreeSet<Integer> itemSet = candidateItems.parallelStream()
                    .map((item) -> item.getId())
                    .collect(Collectors.toCollection(TreeSet::new));

            return new Recommendations(user, recommendToUser(
                    dataset,
                    recommendationModel,
                    user.getId(),
                    itemSet));
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

    @Deprecated
    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> dataset, RecommendationModel model, Integer idUser, Set<Integer> idItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        Set<Item> candidateItems = idItems.stream()
                .filter((idItem) -> idItems.contains(idItem))
                .map((idItem) -> ((ContentDatasetLoader) dataset).getContentDataset().get(idItem))
                .collect(Collectors.toSet());

        User user;
        if (dataset instanceof UsersDatasetLoader) {
            UsersDatasetLoader usersDatasetLoader = (UsersDatasetLoader) dataset;
            user = usersDatasetLoader.getUsersDataset().get(idUser);
        } else {
            user = new User(idUser);
        }

        return recommendToUser(dataset, model, user, candidateItems).getRecommendations();
    }

}
