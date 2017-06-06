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
package delfos.rs.contentbased.vsm.booleanvsm.tfidf;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.rs.contentbased.ContentBasedRecommender;
import static delfos.rs.contentbased.vsm.ContentBasedVSMRS.SIMILARITY_MEASURE;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.contentbased.vsm.booleanvsm.SparseVector;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.WeightedSimilarityMeasure;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.commons.math4.util.Pair;

/**
 * Clase que implementa el sistema de pesado Tf Idf (procedente del área de la recuperación de información) aplicado a
 * la recomendación basada en contenido. Su funcionamiento es similar al de {@link BasicBooleanCBRS}, pero añade
 * ponderación de características.
 *
 * <p>
 * <p>
 * Este sistema de recomendación esta explicado en detalle en: <ul><li> A new weighted model for Content-based
 * Recommender Systems with Contingency and Entropy Measures, Ph.D. candidate: Jorge Castro Gallardo, Advisor: Luis
 * Martínez López, Co-advisor: Manuel José Barranco. September 2012.
 * http://sinbad2.ujaen.es/cod/archivosPublicos/dea/TTII_JorgeCastro.pdf García.</li></ul>
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (19 Octubre 2011)
 * @version 2.0 (28 de Febrero de 2013) Refactorización de las clases asociadas a los perfiles de usuario.
 * @version 2.1 9-Octubre-2013 Incorporación del método makeUserModel
 */
public class TfIdfCBRS extends ContentBasedRecommender<TfIdfCBRSModel, TfIdfCBRSUserProfile> {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Constructor por defecto, que añade los parámetros del sistema de recomendación.
     */
    public TfIdfCBRS() {
        super();
        addParameter(SIMILARITY_MEASURE);
    }

    public TfIdfCBRS(WeightedSimilarityMeasure similarityMeasure) {
        this();
        setParameterValue(SIMILARITY_MEASURE, similarityMeasure);
    }

    @Override
    public TfIdfCBRSModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadContentDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }
        BooleanFeaturesTransformation booleanFeaturesTransformation = new BooleanFeaturesTransformation(contentDataset);

        TfIdfCBRSModel model = new TfIdfCBRSModel(booleanFeaturesTransformation);
        fireBuildingProgressChangedEvent("Model creation", 0, -1);
        int i = 1;
        for (Item item : contentDataset) {

            SparseVector<Long> itemProfile = booleanFeaturesTransformation.newProfile();

            for (Feature f : item.getFeatures()) {
                Object value = item.getFeatureValue(f);

                long indexFeature = booleanFeaturesTransformation.getFeatureIndex(f, value);
                if (value == null) {
                    itemProfile.set(indexFeature, 0);
                } else {
                    itemProfile.set(indexFeature, 1);
                }
            }

            model.put(item.getId(), itemProfile);

            fireBuildingProgressChangedEvent("Profile creation", (int) ((double) i++ * 100 / contentDataset.size()), -1);
        }

        fireBuildingProgressChangedEvent("Profile creation", 100, -1);

        RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        //Calculo la IUF.
        SparseVector<Long> iuf = booleanFeaturesTransformation.newProfile();

        RatingsDataset<? extends Rating> ratingDataset = datasetLoader.getRatingsDataset();

        i = 0;
        fireBuildingProgressChangedEvent("IUF calculation", 0, -1);
        for (Feature feature : contentDataset.getFeatures()) {
            for (Object featureValue : booleanFeaturesTransformation.getAllFeatureValues(feature)) {
                long idFeatureValue = booleanFeaturesTransformation.getFeatureIndex(feature, featureValue);

                double count = 0;
                for (long idUser : ratingDataset.allUsers()) {

                    try {
                        Map<Long, ? extends Rating> userRatingsRated = datasetLoader.getRatingsDataset().getUserRatingsRated(idUser);
                        for (Map.Entry<Long, ? extends Rating> entry : userRatingsRated.entrySet()) {

                            long idItemRatedByUser = entry.getKey();
                            Rating rating = entry.getValue();

                            //Si el rating es negativo, este producto no cuenta.
                            if (relevanceCriteria.isRelevant(rating)) {

                                SparseVector itemProfile = model.get(idItemRatedByUser);
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

                double iufThisFeatureValue = Math.log(model.size() / count);
                if (count == 0) {
                    iufThisFeatureValue = 0;
                }

                iuf.set(idFeatureValue, iufThisFeatureValue);
                fireBuildingProgressChangedEvent("IUF calculation", (int) ((double) i++ * 100 / booleanFeaturesTransformation.sizeOfAllFeatureValues()), -1);

            }
        }

        //Normalizo la iuf. Como son pesos, hago que sumen uno.
        {
            double norma = 0;

            //Calculo la norma.
            for (Pair<Long, Double> entry : iuf.fast()) {
                norma += entry.getValue();
            }

            //La aplico
            for (Pair<Long, Double> entry : iuf.fast()) {
                iuf.set(entry.getKey(), entry.getValue() / norma);
            }
        }

        model.setAllIuf(iuf);

        return model;
    }

    @Override
    public TfIdfCBRSUserProfile makeUserProfile(long idUser, DatasetLoader<? extends Rating> datasetLoader, TfIdfCBRSModel model) throws CannotLoadContentDataset, CannotLoadContentDataset, UserNotFound {
        RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        SparseVector<Long> userProfileValues = model.getBooleanFeaturesTransformation().newProfile();
        SparseVector<Long> userProfileWeights;

        int numItemsPositivelyRated = 0;

        RatingsDataset<? extends Rating> ratingDataset = datasetLoader.getRatingsDataset();
        //Calculo del perfil
        userProfileValues.fill(0);
        for (Map.Entry<Long, ? extends Rating> entry : ratingDataset.getUserRatingsRated(idUser).entrySet()) {
            long idItem = entry.getKey();
            Rating rating = entry.getValue();

            if (relevanceCriteria.isRelevant(rating.getRatingValue())) {
                SparseVector itemProfile = model.get(idItem);
                userProfileValues.sum(itemProfile);
                numItemsPositivelyRated++;
            }
        }

        {
            //Normalisation of user profile, using the number of positive items to normalise.
            double norm = numItemsPositivelyRated;
            for (Pair<Long, Double> entry : userProfileValues.fast()) {
                long idFeature = entry.getKey();
                double value = entry.getValue();
                userProfileValues.set(idFeature, value / norm);
            }
        }

        {
            //Creo los pesos para el usuario a partir del TF normalizado.
            userProfileWeights = userProfileValues.clone();

            //Los multiplico por la ponderación iuf.
            SparseVector iuf = model.getAllIUF();
            userProfileWeights.multiply(iuf);

            //Normalizo los pesos para que sumen uno.
            double norm = userProfileWeights.norm();
            for (Pair<Long, Double> entry : userProfileWeights.fast()) {
                long key = entry.getKey();
                double weight = entry.getValue();
                double newWeight = weight / norm;
                userProfileWeights.set(key, newWeight);

            }
        }
        Map<Feature, Map<Object, Double>> userProfileValuesMap = model.getBooleanFeaturesTransformation().getFeatureValueMap(userProfileValues);
        Map<Feature, Map<Object, Double>> userProfileWeightsMap = model.getBooleanFeaturesTransformation().getFeatureValueMap(userProfileWeights);

        int i = 1;
        for (Feature feature : userProfileValuesMap.keySet()) {
            for (Object featureValue : userProfileValuesMap.get(feature).keySet()) {
                double featureValueValue = userProfileValuesMap.get(feature).get(featureValue);
                Global.showInfoMessage(i + "\t" + feature + "\t" + featureValue + "\t" + featureValueValue + "\n");
                i++;
            }
        }

        return new TfIdfCBRSUserProfile(idUser, userProfileValuesMap, userProfileWeightsMap);
    }

    @Override
    protected Collection<Recommendation> recommendOnly(DatasetLoader<? extends Rating> datasetLoader, TfIdfCBRSModel model, TfIdfCBRSUserProfile userProfile, Collection<Long> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        Collection<Recommendation> ret = new ArrayList<>(candidateItems.size());

        WeightedSimilarityMeasure weightedSimilarity = getSimilarityMeasure();

        for (long idItem : candidateItems) {
            SparseVector itemProfile = model.get(idItem);
            List<Double> itemVector = model.getBooleanFeaturesTransformation().getDoubleVector(itemProfile);

            List<Double> userVector = model.getBooleanFeaturesTransformation().getDoubleValuesVector(userProfile);
            List<Double> userWeights = model.getBooleanFeaturesTransformation().getDoubleWeightsVector(userProfile);

            if (Global.isVerboseAnnoying()) {
                Global.showInfoMessage("========================================\n"
                        + itemVector + "\n"
                        + userVector + "\n"
                        + userWeights + "\n");
            }
            try {
                double similarity = weightedSimilarity.weightedSimilarity(itemVector, userVector, userWeights);
                ret.add(new Recommendation(idItem, similarity));
            } catch (CouldNotComputeSimilarity ex) {
                //No big deal, cannot compute similarity. But it is a problem in TF idf method, since all value must be set, so I throw an unchecked exception to track if it happens.
                throw new IllegalArgumentException(ex);
            }
        }

        return ret;
    }

    /**
     * Devuelve la medida de similitud ponderada que utiliza.
     *
     * @return Similitud entre vectores para las predicciones.
     */
    public WeightedSimilarityMeasure getSimilarityMeasure() {
        return (WeightedSimilarityMeasure) getParameterValue(SIMILARITY_MEASURE);
    }
}
