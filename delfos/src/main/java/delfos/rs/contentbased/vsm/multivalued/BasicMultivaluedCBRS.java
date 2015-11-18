package delfos.rs.contentbased.vsm.multivalued;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.features.FeatureType;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.rs.contentbased.ContentBasedRecommender;
import static delfos.rs.contentbased.vsm.ContentBasedVSMRS.SIMILARITY_MEASURE;
import static delfos.rs.contentbased.vsm.multivalued.entropydependence.EntropyDependenceCBRS.AGGREGATION_OPERATOR;
import delfos.rs.contentbased.vsm.multivalued.profile.BasicMultivaluedUserProfile;
import delfos.rs.contentbased.vsm.multivalued.profile.MultivaluedUserProfile;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.BasicSimilarityMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Sistema de recomendación similar al {@link EntropyDependenceCBRS} pero sin
 * ponderación de características.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 Unknow date
 * @version 1.1 21-Jan-2013
 * @version 1.2 21-Mar-2013 Implementación de las interfaces para la
 * persistencia.
 * @version 2.0 28-Mayo-2013 Refactorización para paralelizar la ejecución de
 * los sistemas de recomendación.
 * @version 2.1 9-Octubre-2013 Incorporación del método makeUserModel
 */
public class BasicMultivaluedCBRS extends ContentBasedRecommender<MultivaluedUserProfilesModel, MultivaluedUserProfile> {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Constructor por defecto, que añade al sistema de recomendación sus
     * parámetros.
     */
    public BasicMultivaluedCBRS() {
        super();

        addParameter(SIMILARITY_MEASURE);
        addParameter(AGGREGATION_OPERATOR);
    }

    @Override
    public MultivaluedUserProfilesModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }
        final RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        Collection<Integer> allUsers = ratingsDataset.allUsers();

        fireBuildingProgressChangedEvent("Profile creation", 0, -1);

        Map<Integer, MultivaluedUserProfile> userProfiles = new TreeMap<>();

        //Creando los perfiles
        AggregationOperator condensationFormula_ = (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);
        int i = 0;
        for (int idUser : allUsers) {
            try {
                BasicMultivaluedUserProfile profile = new BasicMultivaluedUserProfile(idUser);
                Map<Integer, ? extends Rating> userRated = ratingsDataset.getUserRatingsRated(idUser);
                Set<Item> items = new LinkedHashSet<>();
                //Calculo del perfil
                for (int idItem : userRated.keySet()) {
                    try {
                        if (relevanceCriteria.isRelevant(userRated.get(idItem).getRatingValue())) {
                            Item item = contentDataset.get(idItem);

                            items.add(item);
                        }

                    } catch (ItemNotFound ex) {
                        ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                        throw new IllegalStateException(ex);
                    } catch (EntityNotFound ex) {
                        throw new IllegalStateException(ex);
                    }
                }
                profile.addItems(items, condensationFormula_);
                userProfiles.put(idUser, profile);

                fireBuildingProgressChangedEvent("Profile creation", (int) ((float) i * 100 / allUsers.size()), -1);
                i++;
            } catch (UserNotFound ex) {
                Global.showError(ex);
            }
        }
        fireBuildingProgressChangedEvent("Profile creation", 100, -1);

        return new MultivaluedUserProfilesModel(userProfiles);
    }

    @Override
    public MultivaluedUserProfile makeUserProfile(int idUser, DatasetLoader<? extends Rating> datasetLoader, MultivaluedUserProfilesModel model) throws CannotLoadContentDataset, CannotLoadContentDataset, UserNotFound {

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }
        RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        AggregationOperator condensationFormula_ = (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);

        BasicMultivaluedUserProfile profile = new BasicMultivaluedUserProfile(idUser);

        Map<Integer, ? extends Rating> userRated = ratingsDataset.getUserRatingsRated(idUser);
        Set<Item> items = new LinkedHashSet<>();
        //Calculo del perfil
        for (int idItem : userRated.keySet()) {
            try {
                if (relevanceCriteria.isRelevant(userRated.get(idItem).getRatingValue())) {
                    Item item = contentDataset.get(idItem);

                    items.add(item);
                }

            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                throw new IllegalStateException(ex);
            } catch (EntityNotFound ex) {
                throw new IllegalStateException(ex);
            }
        }
        profile.addItems(items, condensationFormula_);
        return profile;
    }

    @Override
    protected Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, MultivaluedUserProfilesModel model, MultivaluedUserProfile userProfile, Collection<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }
        BasicSimilarityMeasure similarity = (BasicSimilarityMeasure) getParameterValue(SIMILARITY_MEASURE);

        Collection<Recommendation> recomendaciones = new ArrayList<>();

        for (int idItem : candidateItems) {
            try {
                Item item = contentDataset.get(idItem);
                //Extraer v1 y v2 del perfil del usuario y del perfil del item
                ArrayList<Float> arrayUser = new ArrayList<>();
                ArrayList<Float> arrayItem = new ArrayList<>();
                for (Feature f : item.getFeatures()) {
                    Object value = item.getFeatureValue(f);
                    if (userProfile.contains(f, value)) {
                        if (f.getType() == FeatureType.Nominal) {
                            arrayItem.add(1.0f);
                            arrayUser.add(userProfile.getFeatureValueValue(f, value));
                        } else {
                            arrayItem.add((float) ((((Number) item.getFeatureValue(f)).doubleValue() - contentDataset.getMinValue(f)) / (contentDataset.getMaxValue(f) - contentDataset.getMinValue(f))));
                            arrayUser.add((float) (((userProfile.getFeatureValueValue(f, value)) - contentDataset.getMinValue(f)) / (contentDataset.getMaxValue(f) - contentDataset.getMinValue(f))));
                        }
                    }

                }
                float[] vUser = new float[arrayUser.size()];
                float[] vItem = new float[arrayUser.size()];
                for (int j = 0; j < arrayUser.size(); j++) {
                    vUser[j] = arrayUser.get(j);
                    vItem[j] = arrayItem.get(j);
                }

                Recommendation r;
                try {

                    r = new Recommendation(idItem, similarity.similarity(vItem, vUser));
                    recomendaciones.add(r);
                } catch (CouldNotComputeSimilarity ex) {
                }
            } catch (EntityNotFound ex) {
                throw new ItemNotFound(ex.getIdEntity());
            }
        }

        return recomendaciones;

    }
}
