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

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.common.statisticalfuncions.MeanIterative;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.basic.user.User;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.parameterowner.ParameterOwnerXML;
import delfos.results.evaluationmeasures.EvaluationMeasure;
import delfos.rs.recommendation.Recommendation;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Element;

/**
 * Medida de evaluación para calcular el error absoluto medio del sistema de
 * recomendación evaluado. Calcula la diferencia entre la valoración hecha para
 * el grupo y la valoración individual que cada usuario dió para el producto, si
 * lo ha valorado.
 *
 * <p>
 * Es una extensión de la medida de evaluación
 * {@link delfos.Results.EvaluationMeasures.RatingPrediction.MAE} para
 * recomendaciones individuales.
 *
 * @author Jorge Castro Gallardo
 *
 * @version 1.0 (10-01-2013)
 * @see delfos.Results.EvaluationMeasures.RatingPrediction.MAE
 */
public class MAE extends GroupEvaluationMeasure {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Element elementMae = new Element(this.getClass().getSimpleName());
        elementMae = ParameterOwnerXML.getElement(this);

        MeanIterative maeGeneral = new MeanIterative();
        TreeMap<GroupOfUsers, MeanIterative> maeGroups = new TreeMap<>();
        TreeMap<Integer, MeanIterative> maeAllMembers = new TreeMap<>();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            MeanIterative maeGroup = new MeanIterative();
            Map<Integer, MeanIterative> maeMembers = new TreeMap<>();
            for (User member : groupOfUsers.getMembers()) {
                maeMembers.put(member.getId(), new MeanIterative());
            }

            Map<Integer, Map<Integer, ? extends Rating>> groupTrueRatings = new TreeMap<>();
            groupOfUsers.getIdMembers().stream().forEach((idUser) -> {
                try {
                    groupTrueRatings.put(idUser, testDataset.getUserRatingsRated(idUser));
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                }
            });

            for (Recommendation recommendation : groupRecommendations) {
                int idItem = recommendation.getIdItem();
                for (int idUser : groupOfUsers.getIdMembers()) {
                    if (groupTrueRatings.get(idUser).containsKey(idItem)) {
                        double trueRating = groupTrueRatings.get(idUser).get(idItem).getRatingValue().doubleValue();
                        double predicted = recommendation.getPreference().doubleValue();
                        double absoluteError = Math.abs(predicted - trueRating);

                        maeGeneral.addValue(absoluteError);
                        maeGroup.addValue(absoluteError);
                        maeMembers.get(idUser).addValue(absoluteError);
                    }
                }
            }

            maeGroups.put(groupOfUsers, maeGroup);
            maeAllMembers.putAll(maeMembers);

            Element groupElement = getGroupElement(groupOfUsers, maeGroup, maeMembers);
            elementMae.addContent(groupElement);

        }

        elementMae.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(maeGeneral.getMean()));
        elementMae.setAttribute("numPredicted",
                Long.toString(maeGeneral.getNumValues()));

        elementMae.addContent(getRawGroupMAEsElement(maeGroups));
        elementMae.addContent(getRawAllMembersMAEsElement(maeAllMembers));

        if (maeGeneral.isEmpty()) {
            return new GroupEvaluationMeasureResult(this, Double.NaN, elementMae);
        } else {
            double mae = maeGeneral.getMean();
            return new GroupEvaluationMeasureResult(this, mae, elementMae);
        }
    }

    @Override
    public boolean usesRatingPrediction() {
        return true;
    }

    private Element getGroupElement(GroupOfUsers groupOfUsers, MeanIterative maeGroup, Map<Integer, MeanIterative> maeMembers) {
        Element groupElement = new Element(this.getClass().getSimpleName() + "_group");

        groupElement.setAttribute("groupMembers", groupOfUsers.toString());
        groupElement.setAttribute(EvaluationMeasure.VALUE_ATTRIBUTE_NAME, Double.toString(maeGroup.getMean()));
        groupElement.setAttribute("numPredicted",
                Long.toString(maeGroup.getNumValues()));

        for (User member : groupOfUsers.getMembers()) {
            Element memberElement = new Element(this.getClass().getSimpleName() + "_member");

            memberElement.setAttribute(
                    "idMember",
                    Integer.toString(member.getId()));

            memberElement.setAttribute(
                    EvaluationMeasure.VALUE_ATTRIBUTE_NAME,
                    Double.toString(maeMembers.get(member.getId()).getMean()));

            memberElement.setAttribute("numPredicted",
                    Long.toString(maeMembers.get(member.getId()).getNumValues()));

            groupElement.addContent(memberElement);

        }

        return groupElement;
    }

    private Element getRawGroupMAEsElement(TreeMap<GroupOfUsers, MeanIterative> maeGroups) {
        Element element = new Element("MAE_group_raw");

        StringBuilder str = new StringBuilder();

        str.append("\n");
        str.append("group\tmae\tnumValues\n");

        for (GroupOfUsers groupOfUsers : maeGroups.keySet()) {
            str.append(groupOfUsers.getIdMembers().toString()).append("\t");
            str.append(maeGroups.get(groupOfUsers).getMean()).append("\t");
            str.append(maeGroups.get(groupOfUsers).getNumValues()).append("\n");
        }

        element.addContent(str.toString());

        return element;
    }

    private Element getRawAllMembersMAEsElement(TreeMap<Integer, MeanIterative> maeAllMembers) {
        Element element = new Element("MAE_allMembers_raw");

        StringBuilder str = new StringBuilder();

        str.append("\n");
        str.append("idMember\tmae\tnumValues\n");

        for (Integer idMember : maeAllMembers.keySet()) {
            str.append(idMember).append("\t");
            str.append(maeAllMembers.get(idMember).getMean()).append("\t");
            str.append(maeAllMembers.get(idMember).getNumValues()).append("\n");
        }

        element.addContent(str.toString());

        return element;
    }
}
