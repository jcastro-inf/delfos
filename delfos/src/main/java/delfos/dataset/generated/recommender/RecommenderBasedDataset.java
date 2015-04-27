package delfos.dataset.generated.recommender;

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDatasetAdapter;
import delfos.dataset.basic.rating.domain.Domain;
import delfos.rs.collaborativefiltering.CollaborativeRecommender;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Dataset que se basa en las predicciones hechas por un sistema de
 * recomendación colaborativo para generar las valoraciones que el usuario daría
 * a un producto concreto.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.1 (21-01-2013) Ahora implementa de {@link RatingsDatasetAdapter}
 * @version 1.0 Unknow date
 */
public class RecommenderBasedDataset extends RatingsDatasetAdapter<Rating> {

    private final CollaborativeRecommender recommenderSystem;
    private final DatasetLoader<? extends Rating> datasetLoader;
    private final Object model;

    public RecommenderBasedDataset(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        this(datasetLoader, new TryThisAtHomeSVD(4, 5));
    }

    public RecommenderBasedDataset(DatasetLoader<? extends Rating> datasetLoader, CollaborativeRecommender recommenderSystem) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        super();
        this.datasetLoader = datasetLoader;
        this.recommenderSystem = recommenderSystem;
        this.model = recommenderSystem.buildRecommendationModel(datasetLoader);
    }

    @Override
    public Rating getRating(int idUser, int idItem) throws UserNotFound, ItemNotFound {
        try {
            Number ret = recommenderSystem.predictRating(datasetLoader, model, idUser, idItem);
            if (ret != null) {
                return new Rating(idUser, idItem, ret);
            }
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (NotEnoughtUserInformation ex) {
            ERROR_CODES.USER_NOT_FOUND.exit(ex);
        }
        return null;
    }

    @Override
    public Set<Integer> allUsers() {
        return datasetLoader.getRatingsDataset().allUsers();
    }

    @Override
    public Set<Integer> allRatedItems() {
        return datasetLoader.getRatingsDataset().allRatedItems();
    }

    @Override
    public Set<Integer> getUserRated(Integer idUser) throws UserNotFound {
        return getUserRatingsRated(idUser).keySet();
    }

    @Override
    public Set<Integer> getItemRated(Integer idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<Integer, Rating> getUserRatingsRated(Integer idUser) throws UserNotFound {
        try {
            Map<Integer, Rating> ret = new TreeMap<>();
            Collection<Recommendation> recommendations = recommenderSystem.recommendToUser(datasetLoader, model, idUser, allRatedItems());

            for (Recommendation r : recommendations) {
                ret.put(r.getIdItem(), new Rating(idUser, r.getIdItem(), r.getPreference()));
            }
            return ret;
        } catch (ItemNotFound ex) {
            ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
        } catch (CannotLoadContentDataset ex) {
            ERROR_CODES.CANNOT_LOAD_CONTENT_DATASET.exit(ex);
        } catch (NotEnoughtUserInformation ex) {

        }
        return Collections.EMPTY_MAP;
    }

    @Override
    public Map<Integer, Rating> getItemRatingsRated(Integer idItem) throws ItemNotFound {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Domain getRatingsDomain() {
        try {
            return datasetLoader.getRatingsDataset().getRatingsDomain();
        } catch (CannotLoadRatingsDataset ex) {
            ERROR_CODES.CANNOT_LOAD_RATINGS_DATASET.exit(ex);
            return null;
        }
    }
}
