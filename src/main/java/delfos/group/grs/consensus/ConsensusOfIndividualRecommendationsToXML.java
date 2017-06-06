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
package delfos.group.grs.consensus;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.user.User;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsToUser;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.XMLOutputter;

/**
 *
 * @version 22-sep-2014
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ConsensusOfIndividualRecommendationsToXML {

    public static final String CONSENSUS_ROOT_ELEMENT_NAME = "Recommendations";
    public static final String MEMBER_ELEMENT_NAME = "Member";
    public static final String MEMBER_ELEMENT_NAME_ID_ATTRIBUTE_NAME = "id";
    public static final String GROUP_ELEMENT_NAME = "Group";
    public static final String GROUP_ELEMENT_MEMBERS_ATTRIBUTE_NAME = "members";
    public static final String RECOMMENDATION_ELEMENT_NAME = "Recommendation";
    public static final String RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME = "idItem";
    public static final String RECOMMENDATION_ELEMENT_PREFERENCE_ATTRIBUTE_NAME = "preference";
    public static final String RECOMMENDATION_ELEMENT_RANK_ATTRIBUTE_NAME = "rank";

    public static void writeConsensusInputXML(DatasetLoader datasetLoader,
            GroupRecommendations groupRecommendations,
            Collection<RecommendationsToUser> membersRecommendations,
            File outputFile) {

        Element root = new Element(CONSENSUS_ROOT_ELEMENT_NAME);

        for (RecommendationsToUser recommendationsToMember : membersRecommendations) {
            User member = recommendationsToMember.getUser();
            Element thisMemberElement = new Element(MEMBER_ELEMENT_NAME);
            thisMemberElement.setAttribute(MEMBER_ELEMENT_NAME_ID_ATTRIBUTE_NAME,
                    Long.toString(member.getId()));

            List<Recommendation> sortedRecommendations = recommendationsToMember
                    .getRecommendations().stream()
                    .sorted(Recommendation.BY_PREFERENCE_DESC)
                    .collect(Collectors.toList());

            int rank = 1;
            for (Recommendation r : sortedRecommendations) {
                Element recommendation = new Element(RECOMMENDATION_ELEMENT_NAME);
                recommendation.setAttribute(RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME,
                        Long.toString(r.getItem().getId()));

                double preferenceInDomain = datasetLoader.getRatingsDataset().getRatingsDomain().trimValueToDomain(r.getPreference()).doubleValue();
                recommendation.setAttribute(RECOMMENDATION_ELEMENT_PREFERENCE_ATTRIBUTE_NAME, Double.toString(preferenceInDomain));
                recommendation.setAttribute(RECOMMENDATION_ELEMENT_RANK_ATTRIBUTE_NAME, Integer.toString(rank));
                thisMemberElement.addContent(recommendation);
                rank++;

            }
            root.addContent(thisMemberElement);
        }

        Element groupElement = new Element(GROUP_ELEMENT_NAME);
        String members = groupRecommendations.getGroupOfUsers().toString();
        groupElement.setAttribute(GROUP_ELEMENT_MEMBERS_ATTRIBUTE_NAME, members);
        int rank = 1;

        List<Recommendation> sortedGroupRecommendations = groupRecommendations
                .getRecommendations().stream()
                .sorted(Recommendation.BY_PREFERENCE_DESC)
                .collect(Collectors.toList());
        for (Recommendation r : sortedGroupRecommendations) {

            Element recommendation = new Element(RECOMMENDATION_ELEMENT_NAME);
            recommendation.setAttribute(RECOMMENDATION_ELEMENT_ID_ITEM_ATTRIBUTE_NAME, Long.toString(r.getIdItem()));
            recommendation.setAttribute(RECOMMENDATION_ELEMENT_PREFERENCE_ATTRIBUTE_NAME, Double.toString(r.getPreference().doubleValue()));
            recommendation.setAttribute(RECOMMENDATION_ELEMENT_RANK_ATTRIBUTE_NAME, Integer.toString(rank));
            groupElement.addContent(recommendation);

            rank++;
        }
        root.addContent(groupElement);

        Document doc = new Document();
        doc.addContent(root);
        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        try (FileWriter fileWriter = new FileWriter(outputFile)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    public static final String RECOMMENDATION_INPUT_ROOT_ELEMENT_NAME = "RecommendationInputData";
    public static final String RECOMMENDATION_INPUT_MEMBERS_RATINGS_ELEMENT_NAME = "MembersRatings";
    public static final String RECOMMENDATION_INPUT_MEMBER_RATINGS_ELEMENT_NAME = "MemberRatings";
    public static final String RECOMMENDATION_INPUT_MEMBER_RATINGS_RATING_ELEMENT_NAME = "Rating";
    public static final String RECOMMENDATION_INPUT_MEMBER_RATINGS_ID_USER_ATTRIBUTE_NAME = "idUser";
    public static final String RECOMMENDATION_INPUT_MEMBER_RATINGS_ID_ITEM_ATTRIBUTE_NAME = "idItem";
    public static final String RECOMMENDATION_INPUT_MEMBER_RATINGS_RATING_VALUE_ATTRIBUTE_NAME = "ratingValue";

    public static final String RECOMMENDATION_INPUT_ID_ITEM_LIST_ELEMENT_NAME = "CandidateItems";
    public static final String RECOMMENDATION_INPUT_ITEM_REQUEST_ELEMENT_NAME = "ItemRequested";
    public static final String RECOMMENDATION_INPUT_ITEM_REQUEST_ID_ITEM_ATTRIBUTE_NAME = "idItem";

    public static <RatingType extends Rating> void writeRecommendationMembersRatingsXML(
            Map<Long, Map<Long, RatingType>> membersRatings,
            Collection<Long> candidateItems,
            File groupPredictionRequestsFile) {

        Element root = new Element(RECOMMENDATION_INPUT_ROOT_ELEMENT_NAME);

        Element membersRatingsElement = new Element(RECOMMENDATION_INPUT_MEMBERS_RATINGS_ELEMENT_NAME);
        for (long idMember : membersRatings.keySet()) {
            Element thisMemberRatingsElement = new Element(RECOMMENDATION_INPUT_MEMBER_RATINGS_ELEMENT_NAME);
            thisMemberRatingsElement.setAttribute(RECOMMENDATION_INPUT_MEMBER_RATINGS_ID_USER_ATTRIBUTE_NAME, Long.toString(idMember));

            Map<Long, RatingType> memberRatings = membersRatings.get(idMember);

            for (RatingType memberRating : memberRatings.values()) {
                Element ratingElement = new Element(RECOMMENDATION_INPUT_MEMBER_RATINGS_RATING_ELEMENT_NAME);
                ratingElement.setAttribute(RECOMMENDATION_INPUT_MEMBER_RATINGS_ID_USER_ATTRIBUTE_NAME, Long.toString(memberRating.getIdUser()));
                ratingElement.setAttribute(RECOMMENDATION_INPUT_MEMBER_RATINGS_ID_ITEM_ATTRIBUTE_NAME, Long.toString(memberRating.getIdItem()));
                ratingElement.setAttribute(RECOMMENDATION_INPUT_MEMBER_RATINGS_RATING_VALUE_ATTRIBUTE_NAME, Double.toString(memberRating.getRatingValue().doubleValue()));
                thisMemberRatingsElement.addContent(ratingElement);
            }
            membersRatingsElement.addContent(thisMemberRatingsElement);
        }
        root.addContent(membersRatingsElement);

        Element candidateItemsElement = new Element(RECOMMENDATION_INPUT_ID_ITEM_LIST_ELEMENT_NAME);
        for (long idItemRequested : candidateItems) {
            Element itemRequestedElement = new Element(RECOMMENDATION_INPUT_ITEM_REQUEST_ELEMENT_NAME);
            itemRequestedElement.setAttribute(RECOMMENDATION_INPUT_ITEM_REQUEST_ID_ITEM_ATTRIBUTE_NAME, Long.toString(idItemRequested));
            candidateItemsElement.addContent(itemRequestedElement);
        }
        root.addContent(candidateItemsElement);

        Document doc = new Document();
        doc.addContent(root);
        XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

        try (FileWriter fileWriter = new FileWriter(groupPredictionRequestsFile)) {
            outputter.output(doc, fileWriter);
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
        }
    }

    public static final String CONSENSUS_OUTPUT_ROOT_NAME = "Consensus";
    public static final String CONSENSUS_OUTPUT_CONSENSUS_ELEMENT_NAME = "GroupConsensus";
    public static final String CONSENSUS_OUTPUT_CONSENSUS_ATTRIBUTE_ROUND = "round";
    public static final String CONSENSUS_OUTPUT_CONSENSUS_ATTRIBUTE_CONSENSUS_DEGREE = "consensusDegree";

    public static final String CONSENSUS_OUTPUT_ALTERNATVE_ELEMENT_NAME = "Alternative";
    public static final String CONSENSUS_OUTPUT_ALTERNATVE_ATTRIBUTE_ID_ITEM = "idItem";
    public static final String CONSENSUS_OUTPUT_ALTERNATVE_ATTRIBUTE_RANK = "rank";

    public static ConsensusOutputModel readConsensusOutputXML(File consensusIntputXML) throws JDOMException, IOException {
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(consensusIntputXML);

        Element root = doc.getRootElement();
        if (!root.getName().equals(CONSENSUS_OUTPUT_ROOT_NAME)) {
            throw new IllegalArgumentException("The XML does not contains a Case Study (" + consensusIntputXML.getAbsolutePath() + ")");
        }

        Element consensus = root.getChild(CONSENSUS_OUTPUT_CONSENSUS_ELEMENT_NAME);

        int round = Integer.parseInt(consensus.getAttributeValue(CONSENSUS_OUTPUT_CONSENSUS_ATTRIBUTE_ROUND));
        double consensusDegree = Double.parseDouble(consensus.getAttributeValue(CONSENSUS_OUTPUT_CONSENSUS_ATTRIBUTE_CONSENSUS_DEGREE));

        Collection<Recommendation> consensusRecommendations = new ArrayList<>();

        for (Element alternative : consensus.getChildren(CONSENSUS_OUTPUT_ALTERNATVE_ELEMENT_NAME)) {
            long idItem = new Long(alternative.getAttributeValue(CONSENSUS_OUTPUT_ALTERNATVE_ATTRIBUTE_ID_ITEM));
            double rank = Double.parseDouble(alternative.getAttributeValue(CONSENSUS_OUTPUT_ALTERNATVE_ATTRIBUTE_RANK));

            double preference = 1 / rank;
            consensusRecommendations.add(new Recommendation(idItem, preference));
        }

        return new ConsensusOutputModel(consensusDegree, round, consensusRecommendations);
    }
}
