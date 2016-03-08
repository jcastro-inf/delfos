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
package delfos.rs.contentbased.interestlms;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.exceptions.dataset.CannotLoadContentDataset;
import delfos.dataset.basic.item.ContentDataset;
import delfos.dataset.basic.item.Item;
import delfos.dataset.basic.features.Feature;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.loader.types.ContentDatasetLoader;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.domain.DecimalDomain;
import delfos.rs.contentbased.vsm.booleanvsm.BooleanFeaturesTransformation;
import delfos.rs.contentbased.vsm.booleanvsm.FeatureValue;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 25-Noviembre-2013
 */
public class InterestLMSPredictorModel implements Serializable {

    private static final long serialVersionUID = 1L;
    private final double learningModerator;
    private BooleanFeaturesTransformation transformation;
    Map<Integer, Map<Feature, Map<Object, Double>>> predictors = new TreeMap<>();
    Map<Integer, Double> userDefaultWeight = new TreeMap<>();
    private final DecimalDomain minusOneToOneDomain = new DecimalDomain(-1, 1);
    private final DecimalDomain ratingDatasetDomain;

    private InterestLMSPredictorModel() {
        this.learningModerator = 0.14;
        ratingDatasetDomain = new DecimalDomain(1, 5);
    }

    protected InterestLMSPredictorModel(DatasetLoader<? extends Rating> datasetLoader, double learningModerator) {
        this.learningModerator = learningModerator;
        this.ratingDatasetDomain = new DecimalDomain(
                datasetLoader.getRatingsDataset().getRatingsDomain().min(),
                datasetLoader.getRatingsDataset().getRatingsDomain().max());

        final ContentDataset contentDataset;
        if (datasetLoader instanceof ContentDatasetLoader) {
            ContentDatasetLoader contentDatasetLoader = (ContentDatasetLoader) datasetLoader;
            contentDataset = contentDatasetLoader.getContentDataset();
        } else {
            throw new CannotLoadContentDataset("The dataset loader is not a ContentDatasetLoader, cannot apply a content-based ");
        }

        transformation = new BooleanFeaturesTransformation(contentDataset);

    }

    private void initUserPredictor(int idUser) {

        double defaultValue = 0;

        userDefaultWeight.put(idUser, defaultValue);

        Map<Feature, Map<Object, Double>> userPredictor = new TreeMap<>();
        for (FeatureValue featureValue : transformation) {
            if (!userPredictor.containsKey(featureValue.feature)) {
                userPredictor.put(featureValue.feature, new TreeMap<>());
            }
            userPredictor.get(featureValue.feature).put(featureValue.value, defaultValue);
        }

        predictors.put(idUser, userPredictor);
    }

    protected double predict(int idUser, Item item) {

        double prediction = predictMinusOneToOne(idUser, item);
        double predictionInDatasetRange = minusOneToOneDomain.convertToDecimalDomain(prediction, ratingDatasetDomain).doubleValue();
        return predictionInDatasetRange;
    }

    private double predictMinusOneToOne(int idUser, Item item) {
        if (!userDefaultWeight.containsKey(idUser)) {
            initUserPredictor(idUser);
        }

        double prediction = 0;

        final double factor = 1.0 / (item.getFeatures().size());

        for (Feature feature : item.getFeatures()) {
            Object featureValue = item.getFeatureValue(feature);

            double weight = predictors.get(idUser).get(feature).get(featureValue);
            prediction += weight * factor;
        }
        double userDefaultWeightValue = userDefaultWeight.get(idUser);

        prediction += userDefaultWeightValue;

        return prediction;

    }

    void enterFeedback(int idUser, Item item, double ratingInMinusOneToOneDomain) {
        final double predictionInMinusOneToOne = predictMinusOneToOne(idUser, item);

        final double factor = 1.0 / (item.getFeatures().size());

        for (Feature feature : item.getFeatures()) {
            Object value = item.getFeatureValue(feature);

            double weight = predictors.get(idUser).get(feature).get(value);

            double newWeight = weight + learningModerator * (ratingInMinusOneToOneDomain - predictionInMinusOneToOne) * factor;

            predictors.get(idUser).get(feature).put(value, newWeight);

        }

        double baseWeight = userDefaultWeight.get(idUser);
        baseWeight += learningModerator * (ratingInMinusOneToOneDomain - predictionInMinusOneToOne);
        userDefaultWeight.put(idUser, baseWeight);

        double predictionAfterTrain = predictMinusOneToOne(idUser, item);
        predictionAfterTrain += 0;
    }
}
