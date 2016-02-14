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
package delfos.results.evaluationmeasures;

import org.jdom2.Element;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.results.RecommendationResults;
import delfos.results.MeasureResult;

/**
 * Cuenta el número de recomendaciones que se calcularon. También es el
 * numerador en la medida de evaluación de cobertura.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @see Coverage
 */
public class NumberOfRecommendations extends EvaluationMeasure {

    private static final long serialVersionUID = 1L;

    @Override
    public MeasureResult getMeasureResult(RecommendationResults recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        float numberOfRecommendations = 0;
        Element element = new Element(this.getName());
        for (int idUser : testDataset.allUsers()) {
            numberOfRecommendations += recommendationResults.getRecommendationsForUser(idUser).size();
        }
        element.setAttribute("value", Float.toString(numberOfRecommendations));
        return new MeasureResult(this, numberOfRecommendations);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
