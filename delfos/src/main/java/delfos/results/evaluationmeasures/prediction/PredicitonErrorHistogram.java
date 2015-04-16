package delfos.results.evaluationmeasures.prediction;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.jdom2.Element;
import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;

/**
 * Calcula el histograma del error.
 *
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 *
 * @version 1-julio-2014
 */
public class PredicitonErrorHistogram extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;
    private static final String HISTOGRAM_ELEMENT_NAME = "Histogram";
    private static final String HISTOGRAM_BIN_WIDTH_ATTRIBUTE_NAME = "binWidth";

    private static final String BIN_ELEMENT_NAME = "Bin";
    private static final String BIN_MIN_VALUE_ATTRIBUTE_NAME = "min";
    private static final String BIN_MAX_VALUE_ATTRIBUTE_NAME = "max";
    private static final String BIN_BIN_VALUE_ATTRIBUTE_NAME = "binValue";

    private static final double binWidth = 0.01;

    /**
     * Constructor por defecto de la medida de evaluación.
     */
    public PredicitonErrorHistogram() {
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        HistogramNumbersSmart histogram = new HistogramNumbersSmart(binWidth);

        for (int idUser : testDataset.allUsers()) {
            List<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            try {
                Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);
                for (Recommendation lista : recommendationList) {
                    Number rating = userRated.get(lista.getIdItem()).ratingValue;
                    Number prediction = lista.getPreference();

                    if (rating != null
                            && !Double.isNaN(rating.doubleValue())
                            && !Double.isInfinite(rating.doubleValue())
                            && prediction != null
                            && !Double.isNaN(prediction.doubleValue())
                            && !Double.isInfinite(prediction.doubleValue())) {
                        float error = prediction.floatValue() - rating.floatValue();
                        histogram.addValue(error);
                    }
                }
            } catch (UserNotFound ex) {
                ERROR_CODES.USER_NOT_FOUND.exit(ex);
            }

        }
        if (histogram.getNumValues() == 0) {
            Global.showWarning("Cannot compute 'MAE' since the RS did not predicted any recommendation!!");
        }

        Element histogramElement = new Element(HISTOGRAM_ELEMENT_NAME);
        histogramElement.setAttribute(HISTOGRAM_BIN_WIDTH_ATTRIBUTE_NAME, Double.toString(binWidth));

        for (int indexBin = 0; indexBin < histogram.getNumBins(); indexBin++) {
            Element binElement = new Element(BIN_ELEMENT_NAME);

            binElement.setAttribute(BIN_MIN_VALUE_ATTRIBUTE_NAME, Double.toString(histogram.getBin_minBound(indexBin)));
            binElement.setAttribute(BIN_MAX_VALUE_ATTRIBUTE_NAME, Double.toString(histogram.getBin_maxBound(indexBin)));
            binElement.setAttribute(BIN_BIN_VALUE_ATTRIBUTE_NAME, Integer.toString(histogram.getBin_numValues(indexBin)));

            histogramElement.addContent(binElement);
        }

        if (Global.isVerboseAnnoying()) {
            histogram.printHistogram(System.out);
        }

        return new MeasureResult(this, 0.0f, histogramElement);
    }

    @Override
    public MeasureResult agregateResults(Collection<MeasureResult> results) {

        HistogramNumbersSmart aggregateHistogram = new HistogramNumbersSmart(binWidth);

        for (MeasureResult measureResult : results) {
            Element histogramElement = measureResult.getXMLElement();

            double _binWidth = Double.parseDouble(histogramElement.getAttributeValue(HISTOGRAM_BIN_WIDTH_ATTRIBUTE_NAME));
            if (_binWidth != binWidth) {
                throw new IllegalStateException("Bin width does not match.");
            }

            for (Element binElement : histogramElement.getChildren(BIN_ELEMENT_NAME)) {
                double min = Double.parseDouble(binElement.getAttributeValue(BIN_MIN_VALUE_ATTRIBUTE_NAME));
                double max = Double.parseDouble(binElement.getAttributeValue(BIN_MAX_VALUE_ATTRIBUTE_NAME));
                int numValues = Integer.parseInt(binElement.getAttributeValue(BIN_BIN_VALUE_ATTRIBUTE_NAME));

                double representativeValueForAggregation = (max + min) / 2;
                for (int i = 0; i < numValues; i++) {
                    aggregateHistogram.addValue(representativeValueForAggregation);
                }
            }

        }

        if (aggregateHistogram.getNumValues() == 0) {
            Global.showWarning("Cannot compute '" + this.getClass().getSimpleName() + "' since the RS did not predicted any recommendation!!");
        }

        Element histogramElement = new Element(HISTOGRAM_ELEMENT_NAME);
        histogramElement.setAttribute(HISTOGRAM_BIN_WIDTH_ATTRIBUTE_NAME, Double.toString(binWidth));

        for (int indexBin = 0; indexBin < aggregateHistogram.getNumBins(); indexBin++) {
            Element binElement = new Element(BIN_ELEMENT_NAME);

            binElement.setAttribute(BIN_MIN_VALUE_ATTRIBUTE_NAME, Double.toString(aggregateHistogram.getBin_minBound(indexBin)));
            binElement.setAttribute(BIN_MAX_VALUE_ATTRIBUTE_NAME, Double.toString(aggregateHistogram.getBin_maxBound(indexBin)));
            binElement.setAttribute(BIN_BIN_VALUE_ATTRIBUTE_NAME, Integer.toString(aggregateHistogram.getBin_numValues(indexBin)));

            histogramElement.addContent(binElement);
        }

        return new MeasureResult(this, 0.0f, histogramElement);
    }
}
