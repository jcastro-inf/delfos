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

import delfos.common.Global;
import delfos.common.datastructures.histograms.HistogramNumbersSmart;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.dataset.util.DatasetUtilities;
import delfos.results.MeasureResult;
import delfos.results.RecommendationResults;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jdom2.Element;

/**
 * Calcula el histograma del error.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1-julio-2014
 */
public class PredictionErrorHistogram extends EvaluationMeasure {

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
    public PredictionErrorHistogram() {
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

            Map<Integer, Double> recommendations = DatasetUtilities
                    .convertToMapOfRecommendations(new RecommendationsToUser(new User(idUser), recommendationList))
                    .entrySet()
                    .parallelStream()
                    .filter(entry -> !Double.isNaN(entry.getValue().getPreference().doubleValue()))
                    .filter(entry -> !Double.isInfinite(entry.getValue().getPreference().doubleValue()))
                    .collect(Collectors.toMap(
                            entry -> entry.getKey().getId(),
                            entry -> entry.getValue().getPreference().doubleValue())
                    );

            Map<Integer, Double> testRatings = testDataset
                    .getUserRatingsRated(idUser)
                    .values()
                    .parallelStream()
                    .filter(rating -> !Double.isNaN(rating.getRatingValue().doubleValue()))
                    .filter(rating -> !Double.isInfinite(rating.getRatingValue().doubleValue()))
                    .collect(Collectors.toMap(
                            rating -> rating.getItem().getId(),
                            rating -> rating.getRatingValue().doubleValue())
                    );

            Set<Integer> commonItems = recommendations.keySet();
            commonItems.retainAll(testRatings.keySet());

            commonItems.stream().forEach(idItem -> {
                Double predictedRating = recommendations.get(idItem);
                Double trueRating = testRatings.get(idItem);

                double error = Math.abs(trueRating - predictedRating);
                histogram.addValue(error);
            });

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
