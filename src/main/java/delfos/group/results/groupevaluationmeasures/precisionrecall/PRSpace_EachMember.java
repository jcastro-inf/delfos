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

import delfos.ERROR_CODES;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;
import delfos.rs.recommendation.Recommendation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jdom2.Element;

/**
 * Medida de evaluación para sistemas de recomendación a grupos que calcula la
 * precisión y recall a lo largo de todos los tamaño de recomendación al grupo.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PRSpace_EachMember extends GroupEvaluationMeasure {

    /**
     * Nombre del elemento que almacena la información de un grupo de usuarios
     */
    public static final String GROUP_OF_USERS_ELEMENT = "GroupOfUsers";
    /**
     * Nombre del atributo que almacena la lista de usuarios que forman el grupo
     */
    public static final String USERS_ATTRIBUTE = "users";

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        Map<GroupOfUsers, ConfusionMatricesCurve> groupsCurves = new TreeMap<>();

        Element measureElement = new Element(getName());

        for (GroupOfUsers group : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(group).getRecommendations();

            Set<Integer> recommendedItems = new TreeSet<>();
            for (Recommendation r : groupRecommendations) {
                recommendedItems.add(r.getIdItem());
            }

            Element groupElement = new Element(GROUP_OF_USERS_ELEMENT);
            groupElement.setAttribute(USERS_ATTRIBUTE, group.getIdMembers().toString());

            List<ConfusionMatrix> matrices = new ArrayList<>();

            /**
             * Estas variables se utilizan en la generación de la curva para el
             * grupo, pero se pueden inicializar en el cálculo de la curva de
             * cada usuario
             */
            int truePositive_group = 0;
            int falseNegative_group = 0;
            int falsePositive_group = 0;
            int trueNegative_group = 0;

            /**
             * Almacena una lista de conjuntos. Cada elemento de la lista
             * representa cada un conjunto con un booleano si al usuario está
             * satisfecho con esa recomendación.
             */
            List<Set<Boolean>> relevanteParaUser = new ArrayList<>(group.size());
            for (int i = 0; i < groupRecommendations.size(); i++) {
                relevanteParaUser.add(new TreeSet<>());
            }

            Map<Integer, ConfusionMatricesCurve> matricesParaCadaMiembro = new TreeMap<>();

            for (int idUser : group.getIdMembers()) {
                Element userElement = new Element("User");
                userElement.setAttribute("idUser", Integer.toString(idUser));

                int truePositive = 0;
                int falseNegative = 0;
                int falsePositive = 0;
                int trueNegative = 0;

                Map<Integer, ? extends Rating> userRatings = null;
                try {
                    userRatings = testDataset.getUserRatingsRated(idUser);
                } catch (UserNotFound ex) {
                    ERROR_CODES.USER_NOT_FOUND.exit(ex);
                    throw new IllegalArgumentException(ex);
                }

                for (int idItem : userRatings.keySet()) {
                    if (recommendedItems.contains(idItem) && relevanceCriteria.isRelevant(userRatings.get(idItem).getRatingValue())) {
                        falseNegative++;
                    } else {
                        trueNegative++;
                    }
                }

                falseNegative_group += falseNegative;
                trueNegative_group += trueNegative;

                int i = 0;
                for (Recommendation r : groupRecommendations) {

                    int idItem = r.getIdItem();
                    if (!userRatings.containsKey(idItem)) {
                        continue;
                    }

                    if (relevanceCriteria.isRelevant(userRatings.get(idItem).getRatingValue())) {
                        relevanteParaUser.get(i).add(true);
                        truePositive++;
                        falseNegative--;
                    } else {
                        relevanteParaUser.get(i).add(false);
                        falsePositive++;
                        trueNegative--;
                    }
                    matrices.add(new ConfusionMatrix(falsePositive, falseNegative, truePositive, trueNegative));
                    i++;
                }

                matricesParaCadaMiembro.put(idUser, new ConfusionMatricesCurve(matrices.toArray(new ConfusionMatrix[1])));

                //Añado la curva del usuario a los resultados del grupo
                userElement.addContent(
                        ConfusionMatricesCurveXML.getElement(
                                matricesParaCadaMiembro.get(idUser)));

                groupElement.addContent(userElement);
            }

            List<ConfusionMatrix> groupMatrices = new ArrayList<>();

            //Ahora se calcula la curva para el grupo
            for (int i = 0; i < relevanteParaUser.size(); i++) {
                Set<Boolean> set = relevanteParaUser.get(i);
                for (boolean b : set) {
                    if (b) {
                        relevanteParaUser.get(i).add(true);
                        truePositive_group++;
                        falseNegative_group--;
                    } else {
                        relevanteParaUser.get(i).add(false);
                        falsePositive_group++;
                        trueNegative_group--;
                    }
                }

                double precision;
                double recall;
                if (((double) truePositive_group + (double) falsePositive_group) == 0) {
                    precision = 0;
                } else {
                    precision = (double) truePositive_group / ((double) truePositive_group + (double) falsePositive_group);
                }

                if (((double) truePositive_group + (double) falseNegative_group) == 0) {
                    recall = 0;
                } else {
                    recall = (double) truePositive_group / ((double) truePositive_group + (double) falseNegative_group);
                }

                groupMatrices.add(new ConfusionMatrix(falsePositive_group, falseNegative_group, truePositive_group, trueNegative_group));
            }

            groupsCurves.put(group, new ConfusionMatricesCurve(groupMatrices.toArray(new ConfusionMatrix[1])));
            groupElement.addContent(ConfusionMatricesCurveXML.getElement(groupsCurves.get(group)));
            measureElement.addContent(groupElement);
        }

        ConfusionMatricesCurve curvaTotal = ConfusionMatricesCurve.mergeCurves(groupsCurves.values());

        measureElement.addContent(ConfusionMatricesCurveXML.getElement(curvaTotal));
        return new GroupEvaluationMeasureResult(this, curvaTotal.getAreaUnderROC());
    }
}
