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
package delfos.results.evaluationmeasures.prediction;

import delfos.ERROR_CODES;
import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import org.jdom2.Element;

/**
 * Calcula el histograma del error.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
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
            Collection<Recommendation> recommendationList = recommendationResults.getRecommendationsForUser(idUser);
            try {
                Map<Integer, ? extends Rating> userRated = testDataset.getUserRatingsRated(idUser);
                for (Recommendation lista : recommendationList) {
                    Number rating = userRated.get(lista.getIdItem()).getRatingValue();
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

        return new MeasureResult(this, 0.0f);
    }
}
