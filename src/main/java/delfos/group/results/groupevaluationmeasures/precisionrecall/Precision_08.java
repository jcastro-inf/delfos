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
package delfos.group.results.groupevaluationmeasures.precisionrecall;

import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import java.util.Map;
import java.util.TreeMap;

/**
 *
 * @author jcastro
 */
public class Precision_08 extends PRSpaceGroups {

    public static final int LIST_SIZE = 8;

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommenderSystemResult groupRecommenderSystemResult, DatasetLoader<? extends Rating> originalDatasetLoader, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria, DatasetLoader<? extends Rating> trainingDatasetLoader, DatasetLoader<? extends Rating> testDatasetLoader) {

        ConfusionMatricesCurve agregada = getDetailedResult(
                groupRecommenderSystemResult,
                originalDatasetLoader,
                testDataset,
                relevanceCriteria,
                trainingDatasetLoader,
                testDatasetLoader);

        Map<String, Double> detailedResult = new TreeMap<>();
        for (int i = 0; i < agregada.size(); i++) {
            double precisionAt = agregada.getPrecisionAt(i);
            detailedResult.put("Precision@" + i, precisionAt);
        }

        double value;
        if (detailedResult.containsKey("Precision@" + LIST_SIZE)) {
            value = agregada.getPrecisionAt(LIST_SIZE);
        } else {
            value = Double.NaN;
        }

        return new GroupEvaluationMeasureResult(this, value);
    }

}
