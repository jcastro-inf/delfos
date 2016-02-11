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
package delfos.group.casestudy.defaultcase;

import delfos.common.parallelwork.Task;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

public class DefaultGroupCaseStudyGroupEvaluationMeasures_Task extends Task {

    //Atributos de entrada.
    int ejecucion;
    int particion;
    RatingsDataset<? extends Rating> testSet;
    GroupRecommenderSystemResult groupRecommendationResult;
    Collection<GroupEvaluationMeasure> groupEvaluationMeasures;
    RelevanceCriteria relevanceCriteria;

    DatasetLoader<? extends Rating> originalDatasetLoader;
    DatasetLoader<? extends Rating> trainingDatasetLoader;
    DatasetLoader<? extends Rating> testDatasetLoader;

    // Atributos para el resultado.
    Map<GroupEvaluationMeasure, GroupEvaluationMeasureResult> groupEvaluationMeasuresResults;

    public DefaultGroupCaseStudyGroupEvaluationMeasures_Task(
            int ejecucion,
            int particion,
            GroupRecommenderSystemResult groupRecommendationResult,
            RatingsDataset<? extends Rating> testSet,
            Collection<GroupEvaluationMeasure> groupEvaluationMeasures,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        this.particion = particion;
        this.groupRecommendationResult = groupRecommendationResult;
        this.testSet = testSet;
        this.ejecucion = ejecucion;
        this.originalDatasetLoader = originalDatasetLoader;
        this.trainingDatasetLoader = trainingDatasetLoader;
        this.testDatasetLoader = testDatasetLoader;

        groupEvaluationMeasuresResults = new TreeMap<>();
        this.groupEvaluationMeasures = groupEvaluationMeasures;
        this.relevanceCriteria = relevanceCriteria;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Exec: ").append(ejecucion).append("\tSplit: ").append(particion);

        return stringBuilder.toString();
    }

    public void clear() {
        this.testSet = null;
        this.groupEvaluationMeasures = null;
    }
}
