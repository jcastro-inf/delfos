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
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
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

    protected float predict(int idUser, Item item) {

        float prediction = predictMinusOneToOne(idUser, item);
        float predictionInDatasetRange = minusOneToOneDomain.convertToDomain(prediction, ratingDatasetDomain).floatValue();
        return predictionInDatasetRange;
    }

    private float predictMinusOneToOne(int idUser, Item item) {
        if (!userDefaultWeight.containsKey(idUser)) {
            initUserPredictor(idUser);
        }

        float prediction = 0;

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

    void enterFeedback(int idUser, Item item, float ratingInMinusOneToOneDomain) {
        final float predictionInMinusOneToOne = predictMinusOneToOne(idUser, item);

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

        float predictionAfterTrain = predictMinusOneToOne(idUser, item);
        predictionAfterTrain += 0;
    }
}
