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
package delfos.rs.contentbased.vsm.multivalued.entropydependence;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.aggregationoperators.AggregationOperator;
import delfos.common.aggregationoperators.Mean;
import delfos.common.datastructures.MultiSet;
import delfos.common.exceptions.CouldNotComputeSimilarity;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.entity.EntityNotFound;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.ParameterOwnerRestriction;
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
import delfos.rs.persistence.DatabasePersistence;
import delfos.rs.persistence.FailureInPersistence;
import delfos.rs.recommendation.Recommendation;
import delfos.similaritymeasures.PearsonCorrelationCoefficient;
import delfos.similaritymeasures.WeightedSimilarityMeasure;
import delfos.stats.associationmeasures.CannotComputeAssociation;
import delfos.stats.associationmeasures.CramerV;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Sistema de recomendación basado en contenido con ponderación de
 * características multivaluados. Su funcionamiento está descrito en los papers:
 *
 * <ul>
 *
 * <li>EVALUACIÓN DE UN MÉTODO DE PONDERACIÓN DE ATRIBUTOS MULTIVALUADOS EN
 * SISTEMAS DE RECOMENDACIÓN BASADOS EN CONTENIDO, Manuel J. Barranco , Jorge
 * Castro , Luis Martínez</li>
 *
 * </ul>
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.1 21-Jan-2013
 * @version 2.1 9-Octubre-2013 Incorporación del método makeUserModel
 *
 */
public class EntropyDependenceCBRS
        extends ContentBasedRecommender<EntropyDependenceCBRSModel, EntropyDependenceCBRSUserProfile> {

    private static final long serialVersionUID = -3387516993124229948L;

    /**
     * Parámetro para almacenar la fórmula que se utiliza para agregar los
     * valores de las características numéricas.
     */
    public static final Parameter AGGREGATION_OPERATOR = new Parameter(
            "Aggregation_operator",
            new ParameterOwnerRestriction(AggregationOperator.class, new Mean()),
            "Parámetro para almacenar la fórmula que se utiliza para agregar "
            + "los valores de las características numéricas."
    );

    /**
     * Constructor por defecto que añade los parámetros del sistema de
     * recomendación.
     */
    public EntropyDependenceCBRS() {
        super();
        addParameter(SIMILARITY_MEASURE);
        addParameter(AGGREGATION_OPERATOR);
    }

    public EntropyDependenceCBRS(WeightedSimilarityMeasure similarityMeasure, AggregationOperator aggregationOperator) {
        this();
        setParameterValue(SIMILARITY_MEASURE, similarityMeasure);
        setParameterValue(AGGREGATION_OPERATOR, aggregationOperator);
    }

    @Override
    public <RatingType extends Rating> EntropyDependenceCBRSModel buildRecommendationModel(
            DatasetLoader<RatingType> datasetLoader
    ) throws CannotLoadContentDataset {

        Global.showInfoMessage(new Date().toString() + "\tBuilding model");

        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        Map<Long, EntropyDependenceCBRSItemProfile> itemProfiles = new TreeMap<>();

        //construyo la eedd de soporte para calcular la entropia
        Map<Feature, MultiSet<Object>> frecuencias = new TreeMap<>();
        for (Feature c : contentDataset.getFeatures()) {
            frecuencias.put(c, new MultiSet<>());
        }

        //Calculo la frecuencia de cada <caracteristica,valor>.
        for (Item item : contentDataset) {
            for (Feature feature : item.getFeatures()) {
                Object featureValue = item.getFeatureValue(feature);
                frecuencias.get(feature).add(featureValue);
            }
        }

        /*Calculo la entropía de cada característica. se puede hacer directamente porque
         no se tienen en cuenta las valoraciones, sino que se hace con la bbdd entera*/
        fireBuildingProgressChangedEvent("Entropy calculation", 0, -1);
        Map<Feature, Number> entropias = new TreeMap<>();
        double normaEntropias = 0;
        for (Feature feature : contentDataset.getFeatures()) {
            MultiSet<Object> multiset = frecuencias.get(feature);
            double entropia = -0;
            for (Object clave : multiset.keySet()) {
                double freq = (double) multiset.getFreq(clave) / (double) multiset.getN();
                entropia += freq * (Math.log(freq) / Math.log(2));
            }
            entropia = -entropia;
            Global.showInfoMessage("entropia de " + feature + " = " + entropia + "\n");
            entropias.put(feature, entropia);
            normaEntropias += entropia;
        }

        //normalizo las entropias
        for (Feature c : contentDataset.getFeatures()) {
            double entropia = entropias.get(c).doubleValue();
            entropia = entropia / normaEntropias;
            Global.showInfoMessage("entropia normalizada de " + c + " = " + entropia + "\n");
            entropias.put(c, entropia);
        }

        //Creando los perfiles
        fireBuildingProgressChangedEvent("Item profiles creation", 0, -1);
        {
            double i = 0;
            for (Item item : contentDataset) {
                Map<Feature, Object> featureValue = new TreeMap<>();
                for (Feature feature : item.getFeatures()) {
                    featureValue.put(feature, item.getFeatureValue(feature));
                }

                itemProfiles.put(item.getId(), new EntropyDependenceCBRSItemProfile(item.getId(), featureValue));

                fireBuildingProgressChangedEvent("Item profiles creation", (int) ((i * 100) / contentDataset.size()), -1);

                i++;
            }

            fireBuildingProgressChangedEvent("Item profiles creation", (int) ((i * 100) / contentDataset.size()), -1);
        }

        Global.showInfoMessage(new Date().toString() + "\tModel built");
        return new EntropyDependenceCBRSModel(itemProfiles, entropias);
    }

    @Override
    public <RatingType extends Rating> EntropyDependenceCBRSUserProfile makeUserProfile(
            long idUser,
            DatasetLoader<RatingType> datasetLoader,
            EntropyDependenceCBRSModel model) throws CannotLoadContentDataset, UserNotFound, CannotLoadRatingsDataset, NotEnoughtUserInformation {

        final RatingsDataset<RatingType> ratingsDataset = datasetLoader.getRatingsDataset();
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }
        final RelevanceCriteria relevanceCriteria = datasetLoader.getDefaultRelevanceCriteria();

        final Map<Feature, Map<Object, Number>> _nominalValues = new TreeMap<>();
        final Map<Feature, Number> _numericalValues = new TreeMap<>();
        final Map<Feature, Number> _weights = new TreeMap<>();

        AggregationOperator condensationFormula_ = (AggregationOperator) getParameterValue(AGGREGATION_OPERATOR);

        Map<Long, RatingType> userRated = ratingsDataset.getUserRatingsRated(idUser);
        if (userRated.isEmpty()) {
            throw new NotEnoughtUserInformation("User " + idUser + " has no rated items.");
        }

        Set<Item> positiveItems = new TreeSet<>();
        {
            final Map<Feature, Collection<Number>> _numericalValuesMeans = new TreeMap<>();

            //Calculo del perfil
            for (long idItem : userRated.keySet()) {
                if (relevanceCriteria.isRelevant(userRated.get(idItem))) {
                    try {
                        Item item = contentDataset.get(idItem);
                        positiveItems.add(item);
                        for (Feature feature : item.getFeatures()) {
                            switch (feature.getType()) {
                                case Nominal:
                                    Object featureValue = item.getFeatureValue(feature);
                                    if (_nominalValues.containsKey(feature)) {
                                        Map<Object, Number> treeMap = _nominalValues.get(feature);
                                        if (treeMap.containsKey(featureValue)) {
                                            treeMap.put(featureValue, treeMap.get(featureValue).doubleValue() + 1);
                                        } else {
                                            treeMap.put(featureValue, 1.0f);
                                        }
                                    } else {
                                        Map<Object, Number> treeMap = new TreeMap<>();
                                        treeMap.put(featureValue, 1.0f);
                                        _nominalValues.put(feature, treeMap);
                                    }
                                    break;
                                case Numerical:
                                    if (_numericalValuesMeans.containsKey(feature)) {
                                        _numericalValuesMeans.get(feature).add((Number) item.getFeatureValue(feature));
                                    } else {
                                        Set<Number> lista = new TreeSet<>();
                                        lista.add((Number) item.getFeatureValue(feature));
                                        _numericalValuesMeans.put(feature, lista);
                                    }
                                    break;
                                default:
                                    throw new UnsupportedOperationException("The item feature type " + feature.getType() + " isn't supported");
                            }
                        }
                    } catch (EntityNotFound ex) {
                        throw new IllegalStateException(ex);
                    }
                }
            }

            for (Map.Entry<Feature, Collection<Number>> entry : _numericalValuesMeans.entrySet()) {
                Feature feature = entry.getKey();
                Collection<Number> values = entry.getValue();
                double aggregateValue = condensationFormula_.aggregateValues(values);

                _numericalValues.put(feature, aggregateValue);
            }
        }

        if (positiveItems.isEmpty()) {
            throw new NotEnoughtUserInformation("User " + idUser + " has no positively rated items.");
        }

        //Ahora creo los perfiles intra usuario.
        //Primero valores nominales
        {
            CramerV cramerV = new CramerV();
            for (Feature feature : _nominalValues.keySet()) {
                List<Object> ratings = new ArrayList<>(positiveItems.size());
                List<Object> featureValues = new ArrayList<>(positiveItems.size());
                for (Item item : positiveItems) {
                    featureValues.add(item.getFeatureValue(feature));
                    ratings.add(userRated.get(item.getId()));
                }
                double intraUserWeight;
                try {
                    intraUserWeight = cramerV.association(ratings, featureValues);
                } catch (CannotComputeAssociation ex) {
                    intraUserWeight = 1;
                }
                //Aplico las entropías.
                Number entropy = model.getEntropy(feature);
                _weights.put(feature, entropy.doubleValue() * intraUserWeight);
            }
        }

        //Ahora los valores numéricos
        {
            PearsonCorrelationCoefficient pearsonCorrelationCoefficient = new PearsonCorrelationCoefficient();
            for (Feature feature : _numericalValues.keySet()) {
                List<Number> ratings = new ArrayList<>(positiveItems.size());
                List<Number> featureValues = new ArrayList<>(positiveItems.size());
                for (Item item : positiveItems) {
                    featureValues.add((Number) item.getFeatureValue(feature));
                    ratings.add(userRated.get(item.getId()).getRatingValue());
                }

                double intraUserWeight;
                try {
                    intraUserWeight = (double) pearsonCorrelationCoefficient.pearsonCorrelationCoefficient(ratings, featureValues);
                } catch (CouldNotComputeSimilarity ex) {
                    intraUserWeight = 1;
                }

                //Aplico las entropías.
                Number entropy = model.getEntropy(feature);
                _weights.put(feature, entropy.doubleValue() * intraUserWeight);
            }
        }

        //Normalizo los pesos dividiendo por la suma.
        {
            double norma = 0;

            for (Number weights : _weights.values()) {
                norma += weights.doubleValue();
            }

            for (Map.Entry<Feature, Number> entry : _weights.entrySet()) {
                double weight = entry.getValue().doubleValue();
                entry.setValue(weight / norma);
            }
        }

        return new EntropyDependenceCBRSUserProfile(idUser, _nominalValues, _numericalValues, _weights);
    }

    @Override
    protected <RatingType extends Rating> Collection<Recommendation> recommendOnly(DatasetLoader<RatingType> datasetLoader, EntropyDependenceCBRSModel model, EntropyDependenceCBRSUserProfile userProfile, Collection<Long> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {
        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        WeightedSimilarityMeasure similarity = (WeightedSimilarityMeasure) getParameterValue(SIMILARITY_MEASURE);
        Collection<Recommendation> recomendaciones = new ArrayList<>();

        for (long idItem : candidateItems) {
            Item item;
            try {
                item = contentDataset.get(idItem);
            } catch (EntityNotFound ex) {
                throw new ItemNotFound(idItem, ex);
            }

            EntropyDependenceCBRSItemProfile itemProfile = model.get(item.getId());

            //Extraer v1 y v2 del perfil del usuario y del perfil del item
            ArrayList<Double> arrayUser = new ArrayList<>();
            ArrayList<Double> arrayItem = new ArrayList<>();
            ArrayList<Double> weight = new ArrayList<>();
            for (Feature feature : itemProfile.getFeatures()) {
                Object value = itemProfile.getFeatureValue(feature);

                weight.add(userProfile.getFeatureValueWeight(feature));
                if (userProfile.contains(feature, value)) {
                    if (feature.getType() == FeatureType.Nominal) {
                        arrayItem.add(1.0);
                        arrayUser.add(userProfile.getFeatureValueValue(feature, value));
                    } else {
                        arrayItem.add((double) ((((Number) itemProfile.getFeatureValue(feature)).doubleValue() - contentDataset.getMinValue(feature)) / (contentDataset.getMaxValue(feature) - contentDataset.getMinValue(feature))));
                        arrayUser.add((double) (((userProfile.getFeatureValueValue(feature, value)) - contentDataset.getMinValue(feature)) / (contentDataset.getMaxValue(feature) - contentDataset.getMinValue(feature))));
                    }
                } else {
                    if (userProfile.contains(feature) && feature.getType() == FeatureType.Nominal) {
                        arrayItem.add(1.0);
                        arrayUser.add(0.0);
                    } else {
                        Global.showInfoMessage("la caracteristica " + feature + " no está en el perfil del usuario.\n");
                        throw new IllegalArgumentException("The feature " + feature + " with type " + feature.getType() + "is not defined in the user " + userProfile.getId() + " profile");
                    }
                }
            }
            double[] vUser = new double[arrayUser.size()];
            double[] vItem = new double[arrayUser.size()];
            double[] weights = new double[arrayUser.size()];
            for (int j = 0; j < arrayUser.size(); j++) {
                vUser[j] = arrayUser.get(j);
                vItem[j] = arrayItem.get(j);
                weights[j] = weight.get(j);
            }

            Recommendation r;
            try {
                r = new Recommendation(item.getId(), similarity.weightedSimilarity(vItem, vUser, weights));
                recomendaciones.add(r);
            } catch (CouldNotComputeSimilarity ex) {
                Global.showWarning(ex);
            }

        }

        if (recomendaciones.size() != candidateItems.size()) {
            Global.showWarning("Se están devolviendo " + recomendaciones.size() + " de " + candidateItems.size());

            return recommendOnly(datasetLoader, model, userProfile, candidateItems);
        }

        return recomendaciones;
    }

    @Override
    public void saveRecommendationModel(DatabasePersistence databasePersistence, EntropyDependenceCBRSModel model) throws FailureInPersistence {
        DAOEntropyDependenceCBRSModel dao = new DAOEntropyDependenceCBRSModel();
        try {
            dao.saveModel(databasePersistence, model);
        } catch (ClassNotFoundException ex) {
            ERROR_CODES.DEPENDENCY_NOT_FOUND.exit(ex);
            throw new IllegalArgumentException(ex);
        } catch (SQLException ex) {
            throw new FailureInPersistence(ex);
        }
    }

    @Override
    public <RatingType extends Rating> EntropyDependenceCBRSModel loadRecommendationModel(DatabasePersistence databasePersistence, Collection<Long> users, Collection<Long> items, DatasetLoader<RatingType> datasetLoader) throws FailureInPersistence {
        DAOEntropyDependenceCBRSModel dao = new DAOEntropyDependenceCBRSModel();
        return dao.loadModel(databasePersistence, users, items);
    }
}
