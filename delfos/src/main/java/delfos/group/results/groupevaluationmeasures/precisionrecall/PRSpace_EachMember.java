package delfos.group.results.groupevaluationmeasures.precisionrecall;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import org.jdom2.Element;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.ERROR_CODES;
import delfos.io.xml.evaluationmeasures.confusionmatricescurve.ConfusionMatricesCurveXML;
import delfos.io.xml.UnrecognizedElementException;
import delfos.rs.recommendation.Recommendation;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatricesCurve;
import delfos.results.evaluationmeasures.confusionmatrix.ConfusionMatrix;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasure;
import delfos.group.results.groupevaluationmeasures.GroupMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;

/**
 * Medida de evaluación para sistemas de recomendación a grupos que calcula la
 * precisión y recall a lo largo de todos los tamaño de recomendación al grupo.
 *
* @author Jorge Castro Gallardo
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
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {
        Map<GroupOfUsers, ConfusionMatricesCurve> groupsCurves = new TreeMap<GroupOfUsers, ConfusionMatricesCurve>();

        Element measureElement = new Element(getName());

        for (Map.Entry<GroupOfUsers, List<Recommendation>> next : recommendationResults) {

            GroupOfUsers group = next.getKey();
            List<Recommendation> groupRecommendations = next.getValue();

            Set<Integer> recommendedItems = new TreeSet<Integer>();
            for (Recommendation r : groupRecommendations) {
                recommendedItems.add(r.getIdItem());
            }

            Element groupElement = new Element(GROUP_OF_USERS_ELEMENT);
            groupElement.setAttribute(USERS_ATTRIBUTE, group.getGroupMembers().toString());

            List<ConfusionMatrix> matrices = new ArrayList<ConfusionMatrix>();

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
            List<Set<Boolean>> relevanteParaUser = new ArrayList<Set<Boolean>>(group.size());
            for (int i = 0; i < groupRecommendations.size(); i++) {
                relevanteParaUser.add(new TreeSet<Boolean>());
            }

            Map<Integer, ConfusionMatricesCurve> matricesParaCadaMiembro = new TreeMap<Integer, ConfusionMatricesCurve>();

            for (int idUser : group.getGroupMembers()) {
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
                    if (recommendedItems.contains(idItem) && relevanceCriteria.isRelevant(userRatings.get(idItem).ratingValue)) {
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

                    if (relevanceCriteria.isRelevant(userRatings.get(idItem).ratingValue)) {
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

            List<ConfusionMatrix> groupMatrices = new ArrayList<ConfusionMatrix>();

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

                float precision;
                float recall;
                if (((float) truePositive_group + (float) falsePositive_group) == 0) {
                    precision = 0;
                } else {
                    precision = (float) truePositive_group / ((float) truePositive_group + (float) falsePositive_group);
                }

                if (((float) truePositive_group + (float) falseNegative_group) == 0) {
                    recall = 0;
                } else {
                    recall = (float) truePositive_group / ((float) truePositive_group + (float) falseNegative_group);
                }

                groupMatrices.add(new ConfusionMatrix(falsePositive_group, falseNegative_group, truePositive_group, trueNegative_group));
            }

            groupsCurves.put(group, new ConfusionMatricesCurve(groupMatrices.toArray(new ConfusionMatrix[1])));
            groupElement.addContent(ConfusionMatricesCurveXML.getElement(groupsCurves.get(group)));
            measureElement.addContent(groupElement);
        }

        ConfusionMatricesCurve curvaTotal = ConfusionMatricesCurve.mergeCurves(groupsCurves.values());

        measureElement.addContent(ConfusionMatricesCurveXML.getElement(curvaTotal));
        return new GroupMeasureResult(this, curvaTotal.getAreaUnderROC(), measureElement);
    }

    @Override
    public GroupMeasureResult agregateResults(Collection<GroupMeasureResult> results) {
        ArrayList<ConfusionMatricesCurve> curves = new ArrayList<ConfusionMatricesCurve>();

        for (GroupMeasureResult r : results) {
            Element e = r.getXMLElement();
            e.getChild(ConfusionMatricesCurveXML.CURVE_ELEMENT);
            try {
                curves.add(ConfusionMatricesCurveXML.getConfusionMatricesCurve(r.getXMLElement().getChild(ConfusionMatricesCurveXML.CURVE_ELEMENT)));
            } catch (UnrecognizedElementException ex) {
                ERROR_CODES.UNRECOGNIZED_XML_ELEMENT.exit(ex);
            }
        }

        ConfusionMatricesCurve mergeCurves = ConfusionMatricesCurve.mergeCurves(curves);
        return new GroupMeasureResult(this, mergeCurves.getAreaUnderROC(), ConfusionMatricesCurveXML.getElement(mergeCurves));
    }
}
