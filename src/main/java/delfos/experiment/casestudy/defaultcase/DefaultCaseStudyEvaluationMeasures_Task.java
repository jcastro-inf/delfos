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
package delfos.experiment.casestudy.defaultcase;

import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import delfos.common.parallelwork.Task;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;
import delfos.results.evaluationmeasures.EvaluationMeasure;

/**
 *
 * @version 29-may-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class DefaultCaseStudyEvaluationMeasures_Task extends Task {

    protected final int ejecucion;
    protected final int particion;

    protected RatingsDataset<? extends Rating> testSet;
    protected Collection<EvaluationMeasure> evaluationMeasures;
    protected RelevanceCriteria relevanceCriteria;
    protected RecommendationResults recommendationResults;

    /**
     * Campo de salida.
     */
    protected Map<EvaluationMeasure, MeasureResult> executionsResult = new TreeMap<>();

    public DefaultCaseStudyEvaluationMeasures_Task(
            int ejecucion,
            int particion,
            RecommendationResults esr,
            RatingsDataset<? extends Rating> testSet,
            Collection<EvaluationMeasure> evaluationMeasures,
            RelevanceCriteria relevanceCriteria) {
        this.particion = particion;
        this.recommendationResults = esr;
        this.testSet = testSet;
        this.ejecucion = ejecucion;
        this.evaluationMeasures = evaluationMeasures;
        this.relevanceCriteria = relevanceCriteria;
    }

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append("Exec: ").append(ejecucion).append("\tSplit: ").append(particion);

        return stringBuilder.toString();
    }

}
