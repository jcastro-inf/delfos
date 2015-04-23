package delfos.rs.contentbased.vsm.booleanvsm.symeonidis2007;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.rs.collaborativefiltering.knn.RecommendationEntity;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.contentbased.ContentBasedRecommender;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.CosineCoefficient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.grouplens.lenskit.vectors.MutableSparseVector;
import org.grouplens.lenskit.vectors.SparseVector;
import org.grouplens.lenskit.vectors.VectorEntry;

/**
 * Clase que implementa el sistema de recomendación propuesto en el paper:
 * <p>
 * <p>
 * Panagiotis Symeonidis, Alexandros Nanopoulos and Yannis Manolopoulos.
 * "Feature-weighted user model for recommender systems." In User Modeling 2007,
 * pp. 97-106. Springer Berlin Heidelberg, 2007.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1.0 (19 Octubre 2011)
 * @version 2.0 (28 de Febrero de 2013) Refactorización de las clases asociadas
 * a los perfiles de usuario.
 * @version 2.1 9-Octubre-2013 Incorporación del método makeUserModel
 * @verison 6-Noviembre-2013 Implementación correcta según el paper.
 */
public class Symeonidis2007FeatureWeighted extends ContentBasedRecommender<Symeonidis2007Model, Symeonidis2007UserProfile> {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro para almacenar el número de vecinos que se tienen en cuenta
     * para la predicción de la valoración. Si no se modifica, su valor por
     * defecto es 20
     */
    public static final Parameter NEIGHBORHOOD_SIZE = new Parameter("Neighborhood_size", new IntegerParameter(1, 9999, 20));

    /**
     * Constructor por defecto, que añade al sistema de recomendación sus
     * parámetros.
     */
    public Symeonidis2007FeatureWeighted() {
        super();
        addParameter(NEIGHBORHOOD_SIZE);
    }

    /**
     * Construye el sistema con el tamaño de vecindario indicado.
     *
     * @param neighborhoodSize Tamaño del vecindario.
     */
    public Symeonidis2007FeatureWeighted(int neighborhoodSize) {
        this();
        setParameterValue(NEIGHBORHOOD_SIZE, neighborhoodSize);
    }

    @Override
    protected Symeonidis2007UserProfile makeUserProfile(int idUser, DatasetLoader<? extends Rating> datasetLoader, Symeonidis2007Model model) throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound {

        MutableSparseVector userFF = makeFFUserProfile(idUser, datasetLoader, model.getBooleanFeaturesTransformation());

        //Los multiplico por la ponderación iuf.
        SparseVector iuf = model.getAllIUF();
        userFF.multiply(iuf);

        Map<Feature, Map<Object, Double>> userProfileValuesMap = model.getBooleanFeaturesTransformation().getFeatureValueMap(userFF);

        return new Symeonidis2007UserProfile(idUser, userProfileValuesMap);
    }

    private MutableSparseVector makeFFItemProfile(int idItem, DatasetLoader<? extends Rating> datasetLoader, BooleanFeaturesTransformation booleanFeaturesTransformation) throws ItemNotFound {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        try {
            MutableSparseVector itemProfile = booleanFeaturesTransformation.newProfile();

            Item item = contentDataset.get(idItem);
            for (Feature f : item.getFeatures()) {
                Object value = item.getFeatureValue(f);

                long indexFeature = booleanFeaturesTransformation.getFeatureIndex(f, value);
                itemProfile.set(indexFeature, 1);
            }

            return itemProfile;
        } catch (EntityNotFound ex) {
            throw new ItemNotFound(idItem, ex);
        }

    }

    protected MutableSparseVector makeFFUserProfile(int idUser, DatasetLoader<? extends Rating> datasetLoader, BooleanFeaturesTransformation booleanFeaturesTransformation) throws CannotLoadRatingsDataset, CannotLoadContentDataset, UserNotFound {

        RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        MutableSparseVector userProfileValues = booleanFeaturesTransformation.newProfile();

        RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        //Calculo del perfil, FF(u)
        for (Map.Entry<Integer, ? extends Rating> entry : ratingsDataset.getUserRatingsRated(idUser).entrySet()) {
            try {
                int idItem = entry.getKey();
                Rating rating = entry.getValue();

                if (relevanceCriteria.isRelevant(rating.ratingValue)) {

                    SparseVector itemProfile = makeFFItemProfile(idItem, datasetLoader, booleanFeaturesTransformation);
                    for (VectorEntry entryItemProfile : itemProfile.fast()) {
                        long idFeature = entryItemProfile.getKey();
                        double featureValue = entryItemProfile.getValue();

                        if (featureValue > 0) {
                            if (userProfileValues.containsKey(idFeature)) {
                                userProfileValues.add(idFeature, featureValue);
                            } else {
                                userProfileValues.set(idFeature, featureValue);
                            }
                        }

                    }

                }
            } catch (ItemNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        return userProfileValues;
    }

    @Override
    public Symeonidis2007Model build(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {

        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }
        BooleanFeaturesTransformation booleanFeaturesTransformation = new BooleanFeaturesTransformation(contentDataset);

        Symeonidis2007Model model = new Symeonidis2007Model(booleanFeaturesTransformation);
        fireBuildingProgressChangedEvent("Model creation", 0, -1);
        {
            int i = 1;
            for (Item item : contentDataset) {
                try {
                    MutableSparseVector itemProfile = makeFFItemProfile(item.getId(), datasetLoader, booleanFeaturesTransformation);
                    model.putItemProfile(item.getId(), itemProfile);

                    fireBuildingProgressChangedEvent("Profile creation", (int) ((float) i++ * 100 / contentDataset.size()), -1);
                } catch (ItemNotFound ex) {
                    ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                }
            }
        }

        fireBuildingProgressChangedEvent("Profile creation", 100, -1);

        RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        Map<Integer, SparseVector> ff_userProfiles = new TreeMap<Integer, SparseVector>();

        //Calculo los perfiles de usuario, la parte FF(u)
        for (int idUser : ratingsDataset.allUsers()) {
            try {
                ff_userProfiles.put(idUser, makeFFUserProfile(idUser, datasetLoader, booleanFeaturesTransformation));
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }
        }

        //Calculo la IUF.
        MutableSparseVector iuf = booleanFeaturesTransformation.newProfile();
        {
            int i = 0;
            final float numUsers = ratingsDataset.allUsers().size();
            fireBuildingProgressChangedEvent("IUF calculation", 0, -1);
            for (Feature feature : contentDataset.getFeatures()) {
                for (Object featureValue : booleanFeaturesTransformation.getAllFeatureValues(feature)) {
                    long idFeatureValue = booleanFeaturesTransformation.getFeatureIndex(feature, featureValue);

                    double count = 0;
                    for (int idUser : ratingsDataset.allUsers()) {

                        try {
                            Map<Integer, ? extends Rating> userRatingsRated = ratingsDataset.getUserRatingsRated(idUser);
                            for (Map.Entry<Integer, ? extends Rating> entry : userRatingsRated.entrySet()) {

                                int idItemRatedByUser = entry.getKey();
                                Number rating = entry.getValue().ratingValue.doubleValue();

                                //Si el rating es negativo, este producto no cuenta.
                                if (relevanceCriteria.isRelevant(rating)) {

                                    SparseVector itemProfile = model.getItemProfile(idItemRatedByUser);
                                    if (itemProfile.containsKey(idFeatureValue) && itemProfile.get(idFeatureValue) > 0) {
                                        count++;

                                        //Como este usuario tiene algún producto valorado con la característica, paro el cálculo ya que no me interesa si tiene más de uno.
                                        break;
                                    }
                                }
                            }
                        } catch (UserNotFound ex) {
                            ERROR_CODES.USER_NOT_FOUND.exit(ex);
                            throw new IllegalArgumentException(ex);
                        }
                    }

                    double u_div_uf = numUsers / count;

                    double iufThisFeatureValue = Math.log10(u_div_uf);

                    if (Global.isVerboseAnnoying()) {
                        Global.showMessage("Feature " + feature + " and value " + featureValue + " has an IUF of " + iufThisFeatureValue + "\n");
                    }
                    iuf.set(idFeatureValue, iufThisFeatureValue);
                    fireBuildingProgressChangedEvent("IUF calculation", (int) ((float) i++ * 100 / booleanFeaturesTransformation.sizeOfAllFeatureValues()), -1);
                }
            }
        }

        model.setAllIuf(iuf);

        //Ahora calculo los perfiles de los usuarios, para luego hacer vecindario...
        {
            for (int idUser : ratingsDataset.allUsers()) {
                SparseVector userFF = ff_userProfiles.get(idUser);

                MutableSparseVector userProfileFinalVector = userFF.mutableCopy();
                userProfileFinalVector.multiply(iuf);

                Map<Feature, Map<Object, Double>> userProfileValues = booleanFeaturesTransformation.getFeatureValueMap(userProfileFinalVector);

                model.putUserProfile(idUser, new Symeonidis2007UserProfile(idUser, userProfileValues));
            }
        }

        return model;
    }

    @Override
    protected Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, Symeonidis2007Model model, Symeonidis2007UserProfile userProfile, Collection<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

//Step1: Busco los vecinos más cercanos.
        int neighborhoodSize = getNeighborhoodSize();
        List<Neighbor> neighbors = getUserNeighbors(model, userProfile);

//Step 2: We get the items in the neighborhood ( and perform intersection with candidate items).
        Set<Integer> itemsNeighborhood = new TreeSet<Integer>();
        for (Neighbor neighbor : neighbors.subList(0, Math.min(neighbors.size(), neighborhoodSize))) {
            Collection<Integer> neighborRated = ratingsDataset.getUserRated(neighbor.getIdNeighbor());
            itemsNeighborhood.addAll(neighborRated);
        }
        itemsNeighborhood.retainAll(candidateItems);
//Step 3: We get the features of each item: I1: {F2}, I3: {F2, F3}, I5: {F1, F2, F3}
//Step 4: We ﬁnd their frequency in the neighborhood:fr(F1)=1, fr(F2)=3, fr(F3)=2
        MutableSparseVector featureFrequency = model.getBooleanFeaturesTransformation().newProfile();
        featureFrequency.fill(0);
        for (int idItem : itemsNeighborhood) {
            try {
                Item item = contentDataset.get(idItem);
                for (Feature feature : item.getFeatures()) {
                    Object featureValue = item.getFeatureValue(feature);
                    long idFeature = model.getBooleanFeaturesTransformation().getFeatureIndex(feature, featureValue);
                    featureFrequency.add(idFeature, 1.0);
                }
            } catch (EntityNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }
//Step 5: For each item, we add its features frequency ﬁnding its weight in the neighborhood: w(I1) = 3, w(I3) = 5, w(I5) = 6.
        Collection<Recommendation> recommendations = new ArrayList<>();

        for (int idItem : candidateItems) {
            try {
                double itemScore = 0;
                Item item = contentDataset.get(idItem);

                for (Feature feature : item.getFeatures()) {
                    Object featureValue = item.getFeatureValue(feature);
                    long idFeature = model.getBooleanFeaturesTransformation().getFeatureIndex(feature, featureValue);
                    itemScore += featureFrequency.get(idFeature);
                }

                recommendations.add(new Recommendation(idItem, itemScore));
            } catch (EntityNotFound ex) {
                ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
            }
        }

        return recommendations;
    }

    protected List<Neighbor> getUserNeighbors(Symeonidis2007Model model, Symeonidis2007UserProfile userProfile) {

        CosineCoefficient cosineCoefficient = new CosineCoefficient();

        List<Neighbor> neighbors = new ArrayList<>();
        for (Symeonidis2007UserProfile neighborProfile : model.userProfiles()) {
            if (neighborProfile.getId() != userProfile.getId()) {
                List<Float> v1 = new LinkedList<>();
                List<Float> v2 = new LinkedList<>();

                for (Feature feature : userProfile.getFeatures()) {
                    for (Object value : userProfile.getValuedFeatureValues(feature)) {
                        if (neighborProfile.contains(feature, value)) {
                            float userValue = (float) userProfile.getFeatureValueValue(feature, value);
                            float neighborValue = (float) neighborProfile.getFeatureValueValue(feature, value);

                            v1.add(userValue);
                            v2.add(neighborValue);
                        }
                    }
                }

                try {
                    float sim = cosineCoefficient.similarity(v1, v2);
                    neighbors.add(new Neighbor(RecommendationEntity.USER, neighborProfile.getId(), sim));
                } catch (CouldNotComputeSimilarity ex) {
                }
            }
        }
        Collections.sort(neighbors);
        return neighbors;
    }

    /**
     * Devuelve el numero de usuarios vecinos que se consideran en el cálculo de
     * las recomendaciones.
     *
     * @return Número de vecinos.
     */
    private int getNeighborhoodSize() {
        return (Integer) getParameterValue(NEIGHBORHOOD_SIZE);
    }
}
