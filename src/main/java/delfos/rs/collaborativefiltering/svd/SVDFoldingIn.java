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
package delfos.rs.collaborativefiltering.svd;

import delfos.ERROR_CODES;
import delfos.common.Chronometer;
import delfos.common.DateCollapse;
import delfos.common.Global;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.common.exceptions.dataset.CannotLoadRatingsDataset;
import delfos.common.exceptions.dataset.items.ItemNotFound;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.exceptions.ratings.NotEnoughtItemInformation;
import delfos.common.exceptions.ratings.NotEnoughtUserInformation;
import delfos.common.parameters.Parameter;
import delfos.common.parameters.restriction.DoubleParameter;
import delfos.common.parameters.restriction.IntegerParameter;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.TreeMap;

/**
 * Implementa el modelo de recomendación SVD al que añade la
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 08-Julio-2013
 */
public class SVDFoldingIn
        extends TryThisAtHomeSVD {

    private static final long serialVersionUID = 1L;
    /**
     * Parámetro que controla el learning rate, es decir, la velocidad con la que se modifican los valores para
     * minimizar el error de predicción. Este valor se aplica cuando se incrementa el modelo.
     */
    public static final Parameter INCREMENTED_MODEL_LEARNING_RATE = new Parameter("increment_lRate", new DoubleParameter(0.001f, 500f, 0.01f));
    /**
     * Número de iteraciones que se hacen por cada característica para minimizar el error. Este valor se aplica cuando
     * se incrementa el modelo.
     *
     */
    public static final Parameter INCREMENTED_MODEL_NUM_ITER_PER_FEATURE = new Parameter("increment_iterPerFeature", new IntegerParameter(1, 9000000, 10));

    /**
     * Cojnstructor por defecto, que añade los parámetros del sistema de recomendación.
     */
    public SVDFoldingIn() {
        super();

        addParameter(INCREMENTED_MODEL_LEARNING_RATE);
        addParameter(INCREMENTED_MODEL_NUM_ITER_PER_FEATURE);
    }

    /**
     * Constructor que asigna los valores indicados como número de características y número de iteraciones del sistema.
     *
     * @param featuresValue Número de características que se calculan
     * @param iterationsValue Número de iteraciones para cada característica
     */
    public SVDFoldingIn(int featuresValue, int iterationsValue) {
        this();
        setParameterValue(NUM_FEATURES, featuresValue);
        setParameterValue(NUM_ITER_PER_FEATURE, iterationsValue);
    }

    @Override
    public TryThisAtHomeSVDModel buildRecommendationModel(DatasetLoader<? extends Rating> datasetLoader) throws CannotLoadRatingsDataset, CannotLoadRatingsDataset, CannotLoadContentDataset {
        return super.buildRecommendationModel(datasetLoader);
    }

    @Override
    public Collection<Recommendation> recommendToUser(DatasetLoader<? extends Rating> datasetLoader, TryThisAtHomeSVDModel model, Integer idUser, java.util.Set<Integer> candidateItems) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        TryThisAtHomeSVDModel incrementedModel;
        if (model.getUsersIndex().containsKey(idUser)) {
            incrementedModel = model;
        } else {
            incrementedModel = incrementModelWithUserRatings(model, datasetLoader, idUser);
        }

        return super.recommendToUser(datasetLoader, incrementedModel, idUser, candidateItems);
    }

    @Override
    public Number predictRating(DatasetLoader<? extends Rating> datasetLoader, TryThisAtHomeSVDModel model, int idUser, int idItem) throws UserNotFound, ItemNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset, NotEnoughtUserInformation {

        TryThisAtHomeSVDModel incrementedModel;
        if (model.getUsersIndex().containsKey(idUser)) {
            incrementedModel = model;
        } else {
            incrementedModel = incrementModelWithUserRatings(model, datasetLoader, idUser);
        }
        return super.predictRating(datasetLoader, incrementedModel, idUser, idItem); //To change body of generated methods, choose Tools | Templates.
    }

    public TryThisAtHomeSVDModel incrementModelWithUserRatings(TryThisAtHomeSVDModel oldModel, DatasetLoader<? extends Rating> datasetLoader, int idUser) throws CannotLoadRatingsDataset, UserNotFound, UserNotFound, CannotLoadRatingsDataset, CannotLoadContentDataset {

        final boolean normalize = isNormalised();
        final double lrate = ((Number) getParameterValue(INCREMENTED_MODEL_LEARNING_RATE)).doubleValue();
        final int numFeatures = getNumFeatures();
        final int numIterationsPerFeature = (Integer) getParameterValue(INCREMENTED_MODEL_NUM_ITER_PER_FEATURE);
        final RatingsDataset<? extends Rating> ratingsDataset = datasetLoader.getRatingsDataset();

        List<Double> thisUserFeatures = new ArrayList<>(numFeatures);
        int thisUserIndex = -1;
        for (int userIndex : oldModel.getUsersIndex().values()) {
            if (thisUserIndex < userIndex) {
                thisUserIndex = userIndex;
            }
        }
        thisUserIndex++;

        if ((Boolean) getParameterValue(SMART_INITIALISATION)) {
            Random random = new Random(getSeedValue());
            final double maxInitialisation = (double) Math.sqrt(ratingsDataset.getRatingsDomain().max().doubleValue() / numFeatures);
            final double minInitialisation = (double) Math.sqrt(ratingsDataset.getRatingsDomain().min().doubleValue() / numFeatures);

            for (int j = 0; j < numFeatures; j++) {
                long seed = random.nextLong();
                thisUserFeatures.add(getInitialisation(maxInitialisation, minInitialisation, seed));
            }
        } else {
            for (int j = 0; j < numFeatures; j++) {
                thisUserFeatures.add(j, 0.01);
            }
        }

        /**
         * Construyo el nuevo modelo, para posteriormente entrenarlo.
         */
        final TryThisAtHomeSVDModel newModel;
        {
            final TreeMap<Integer, Integer> newUsersIndex = new TreeMap<>(oldModel.getUsersIndex());
            newUsersIndex.put(idUser, thisUserIndex);
            final TreeMap<Integer, Integer> newItemsIndex = new TreeMap<>(oldModel.getItemsIndex());

            /*
             * Copio las características de los productos, tal cual.
             */
            final List<List<Double>> newItemsFeatures = oldModel.getAllItemFeatures();
            for (Entry<Integer, Integer> entry : newItemsIndex.entrySet()) {

                int _idItem = entry.getKey();
                int _idItemIndex = entry.getValue();

                while (newItemsFeatures.size() <= _idItemIndex) {
                    newItemsFeatures.add(null);
                }
                List<Double> loopItemFeatures = oldModel.getAllItemFeatures().get(_idItemIndex);
                newItemsFeatures.set(_idItemIndex, new ArrayList<>(loopItemFeatures));
            }

            /**
             * Copio las características de los usuarios y luego añado el nuevo.
             */
            final List<List<Double>> newUsersFeatures = new ArrayList<>();

            for (Entry<Integer, Integer> entry : oldModel.getUsersIndex().entrySet()) {
                int _idUser = entry.getKey();
                int _idUserIndex = entry.getValue();

                //TODO: Esto está parcheado, hay que hacerlo de una forma más eficiente.
                while (newUsersFeatures.size() <= _idUserIndex) {
                    newUsersFeatures.add(null);
                }
                List<Double> loopUserFeatures = oldModel.getAllUserFeatures().get(_idUserIndex);
                newUsersFeatures.set(_idUserIndex, new ArrayList<>(loopUserFeatures));
            }
            newUsersFeatures.add(thisUserFeatures);
            newModel = new TryThisAtHomeSVDModel(newUsersFeatures, newUsersIndex, newItemsFeatures, newItemsIndex);
        }

        Global.showInfoMessage("ThisUserFeatures: " + thisUserFeatures.toString() + "\n");
        MeanIterative tiempoCiclo = new MeanIterative(20);
        Global.showInfoMessage("Feature\tIteration\tMAE\tThe error has improved" + "\n");

        double maeAnterior = 0;
        for (int indexFeature = 0; indexFeature < numFeatures; indexFeature++) {

            Chronometer c = new Chronometer();
            for (int iteration = 0; iteration < numIterationsPerFeature; iteration++) {

                c.reset();
                MeanIterative meanAbsoluteError = new MeanIterative();

                Map<Integer, ? extends Rating> thisUserRatings = ratingsDataset.getUserRatingsRated(idUser);

                for (int idItem : thisUserRatings.keySet()) {
                    Rating rating = thisUserRatings.get(idItem);
                    Integer indexItem = newModel.getItemsIndex().get(idItem);
                    Double predicted;
                    try {
                        predicted = privatePredictRating(datasetLoader, newModel, idUser, idItem, indexFeature + 1);

                        double error = (rating.getRatingValue().doubleValue() - predicted);
                        meanAbsoluteError.addValue(Math.abs(error));

                        double getUser = thisUserFeatures.get(indexFeature);
                        double getItem = newModel.getAllItemFeatures().get(indexItem).get(indexFeature);
                        double newUserValue = getUser + lrate * (error * getItem - getK() * getUser);

                        if (Double.isInfinite(newUserValue)) {
                            throw new IllegalStateException("Los valores nuevos son erroneos");
                        } else //compruebo que los valores convergen a un valor bajo
                        {
                            if (!(newUserValue > 10E20 || newUserValue < -10E20)) {
                                thisUserFeatures.set(indexFeature, newUserValue);
                            }
                        }
                    } catch (ItemNotFound ex) {
                        ERROR_CODES.ITEM_NOT_FOUND.exit(ex);
                        throw new IllegalStateException(ex);
                    } catch (NotEnoughtItemInformation ex) {
                        //Fallo de cobertura, no habia ratings del producto en la fase de entrenamiento.
                        newModel.warningItemNotInModel(
                                idItem,
                                "SVD recommendation model does not contains the item (" + idItem + ").",
                                ex);
                    } catch (NotEnoughtUserInformation ex) {
                        //Fallo de cobertura, no habia ratings del usuario en la fase de entrenamiento.
                        newModel.warningUserNotInModel(
                                idUser,
                                "SVD recommendation model does not contains the user (" + idUser + ").",
                                ex);
                    }
                }

                long diff = c.getPartialElapsed();

                tiempoCiclo.addValue(diff);

                Global.showInfoMessage(indexFeature + "\t" + iteration + "\t" + String.format("%.8f", meanAbsoluteError.getMean()) + "\t" + String.format("%.8f", (maeAnterior - meanAbsoluteError.getMean())) + " time: " + DateCollapse.collapse(diff) + "\n");
                maeAnterior = meanAbsoluteError.getMean();
                int totalIteraciones = numFeatures * numIterationsPerFeature;
                int iterActual = indexFeature * numIterationsPerFeature + iteration + 1;

                long tiempoRestante = (long) (tiempoCiclo.getMean() * (totalIteraciones - iterActual));
                fireBuildingProgressChangedEvent("training values", ((indexFeature * numIterationsPerFeature + (iteration + 1)) * 90 / (numFeatures * numIterationsPerFeature)) + 10, tiempoRestante);
            }
        }
        Global.showInfoMessage("Incremented model user features: " + newModel.getAllUserFeatures().get(newModel.getUsersIndex().get(idUser)).toString() + "\n");
        return newModel;
    }
}
