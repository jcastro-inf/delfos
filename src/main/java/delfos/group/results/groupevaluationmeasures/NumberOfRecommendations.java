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
package delfos.group.results.groupevaluationmeasures;

import delfos.common.Global;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import org.jdom2.Element;

/**
 * Medida de evaluación para calcular el número de predicciones que se
 * calcularon.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 (26-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE_ForGroups
 */
public class NumberOfRecommendations extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Element ret = ParameterOwnerXML.getElement(this);
        long recomendadas = 0;

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            Element groupRecommendationsElement = new Element("GroupRecommendations");
            groupRecommendationsElement.setAttribute("group", groupOfUsers.toString());

            if (groupRecommendations == null) {
                Global.showWarning("the group " + groupOfUsers + " has no recommendations (null)");
                groupRecommendationsElement.addContent("[]");
            } else {
                long recommendedThisGroup = groupRecommendations.stream()
                        .filter(recommendation -> recommendation != null)
                        .filter(recommendation -> !Double.isNaN(recommendation.getPreference().doubleValue()))
                        .count();
                recomendadas += recommendedThisGroup;
                groupRecommendationsElement.addContent(groupRecommendations.toString());
            }

            ret.addContent(groupRecommendationsElement);
        }
        ret.setAttribute("value", Long.toString(recomendadas));
        return new GroupEvaluationMeasureResult(this, recomendadas);
    }

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }
}
