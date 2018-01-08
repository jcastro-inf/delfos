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
package delfos.io.xml.recommendations;

import delfos.Constants;
import delfos.rs.recommendation.DetailField;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.jdom2.Attribute;
import org.jdom2.Element;

/**
 * Clase que se utiliza para generar un XML que almacene los productos que un sistema de recomendación devuelve como
 * recomendaciones de manera ordenada, proporcionando el id del producto y su valor de preferencia asignado por el
 * sistema de recomendación.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 *
 * @version 1.0 Unknown date
 * @version 1.1 20-Mar-2013
 */
public class RecommendationsToXML {

    public static final String RECOMMENDATIONS_ELEMENT_NAME = "recommendations";
    public static final String RECOMMENDATION_ELEMENT_NAME = "recommendation";
    public static final String TIME_TAKEN_ATTRIBUTE_NAME = "time";
    public static final String ID_TARGET_ATTRIBUTE_NAME = "idTarget";
    public static final String ID_ITEM_ATTRIBUTE_NAME = "idItem";
    public static final String PREFERENCE_ATTRIBUTE_NAME = "preference";
    public static final String RANK_ATTRIBUTE_NAME = "rank";

    /**
     * Convierte las recomendaciones de un usuario a un elemento XML.
     *
     * @param recommendations Objeto a convertir.
     * @return Elemento XML.
     */
    public static Element getRecommendationsElement(Recommendations recommendations) {

        Element element = new Element(RECOMMENDATIONS_ELEMENT_NAME);

        element.setAttribute(ID_TARGET_ATTRIBUTE_NAME, recommendations.getTargetIdentifier());

        for (DetailField detailField : recommendations.detailFieldSet()) {
            element.setAttribute(
                    detailField.name().toLowerCase(),
                    recommendations.getDetails(detailField).toString());
        }

        List<Recommendation> recommendationsSortedById = new ArrayList<>(recommendations.getRecommendations());
        Collections.sort(recommendationsSortedById, Recommendation.BY_ID);

        Map<Recommendation, Integer> recommendationsRank = new TreeMap();

        ArrayList<Recommendation> groupRecommendationSortedByPreference = new ArrayList<>(recommendations.getRecommendations());
        Collections.sort(groupRecommendationSortedByPreference, Recommendation.BY_PREFERENCE_DESC);
        groupRecommendationSortedByPreference.stream().forEachOrdered((recommendation) -> {
            recommendationsRank.put(recommendation, recommendationsRank.size() + 1);
        });

        for (Recommendation r : recommendationsSortedById) {
            Element recommendation = new Element(RECOMMENDATION_ELEMENT_NAME);
            recommendation.setAttribute(ID_ITEM_ATTRIBUTE_NAME, Long.toString(r.getIdItem()));
            recommendation.setAttribute(PREFERENCE_ATTRIBUTE_NAME, r.getPreference().toString());
            recommendation.setAttribute(RANK_ATTRIBUTE_NAME, r.getPreference().toString());
            element.addContent(recommendation);
        }

        if (Constants.isRawResultDefined()) {
            Element rawDataElement = new Element(RECOMMENDATIONS_ELEMENT_NAME + "_RAW");
            rawDataElement.setAttribute(ID_TARGET_ATTRIBUTE_NAME, recommendations.getTargetIdentifier());
            StringBuilder rawData = new StringBuilder();

            recommendationsSortedById.stream().forEachOrdered(recommendation -> {
                rawData.append(
                        recommendation.getIdItem()).append("\t")
                        .append(recommendation.getPreference().doubleValue()).append("\t")
                        .append(recommendationsRank.get(recommendation)).append("\n");
            });
            rawDataElement.addContent(rawData.toString());
            element.addContent(rawDataElement);
        }

        return element;
    }

    /**
     * Convierte el elemento XML indicado en un objeto que representa las recomendaciones al usuario.
     *
     * @param element Elemento XML a convertir.
     * @return Recomendaciones.
     *
     * @throws IllegalArgumentException Si el elemento no contiene la información necesaria para recuperar un objeto
     * {@link Recommendations}.
     */
    public static Recommendations getRecommendations(Element element) {

        if (!element.getName().equals(RECOMMENDATIONS_ELEMENT_NAME)) {
            throw new IllegalArgumentException("Element name doesn't match this reader: found '" + element.getName() + "' expected '" + RECOMMENDATIONS_ELEMENT_NAME + "'");
        }

        String idTarget = element.getAttributeValue(ID_TARGET_ATTRIBUTE_NAME);

        Map<DetailField, Object> details = new TreeMap<>();
        for (Attribute attribute : element.getAttributes()) {

            if (ID_TARGET_ATTRIBUTE_NAME.equals(attribute.getName())) {
                continue;
            }

            DetailField detailField = DetailField.valueOfNoCase(attribute.getName());

            String detailFieldValueString = attribute.getValue();
            Object detailFieldValue = detailField.parseValue(detailFieldValueString);

            details.put(detailField, detailFieldValue);

        }

        RecommendationComputationDetails recommendationComputationDetails = new RecommendationComputationDetails(details);
        List<Recommendation> recommendations = new LinkedList<>();
        for (Object r : element.getChildren(RECOMMENDATION_ELEMENT_NAME)) {
            Element recommendationElement = (Element) r;

            if (!recommendationElement.getName().equals(RECOMMENDATION_ELEMENT_NAME)) {
                throw new IllegalArgumentException("Element name doesn't match this reader: found '" + recommendationElement.getName() + "' expected '" + RECOMMENDATION_ELEMENT_NAME + "'");
            }
            long idItem = new Long(recommendationElement.getAttributeValue(ID_ITEM_ATTRIBUTE_NAME));
            double preference = Double.parseDouble(recommendationElement.getAttributeValue(PREFERENCE_ATTRIBUTE_NAME));
            recommendations.add(new Recommendation(idItem, preference));
        }

        return RecommendationsFactory.createRecommendations(idTarget, recommendations, recommendationComputationDetails);
    }
}
