package delfos.group.results.groupevaluationmeasures;

import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.detailed.RecommendationsWithNeighborToXML;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.io.xml.recommendations.RecommendationsToXML;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationWithNeighbors;
import delfos.rs.recommendation.Recommendations;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jdom2.Element;

/**
 * Evaluation measure that prints the neighbors used to produce the
 * recommendations.
 *
 * @author Jorge Castro Gallardo
 */
public class PrintNeighbors extends GroupEvaluationMeasure {

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommenderSystemResult groupRecommenderSystemResult, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        Element neighborsDetails = new Element("NeighborsDetails");

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            List<Neighbor> neighbors;

            if (groupRecommendations.isEmpty()) {
            } else {
                if ((groupRecommendations.iterator().next() instanceof RecommendationWithNeighbors)) {
                    RecommendationWithNeighbors recommendationWithNeighbors = (RecommendationWithNeighbors) groupRecommendations.iterator().next();
                    neighbors = recommendationWithNeighbors.getNeighbors().stream().collect(Collectors.toList());

                    Collections.sort(neighbors, Neighbor.BY_ID);
                    neighbors = Collections.unmodifiableList(neighbors);

                    Element groupResults = new Element("GroupResults");
                    groupResults.addContent(RecommendationsWithNeighborToXML.getNeighborsElement(
                            groupOfUsers.getTargetId(),
                            neighbors
                    ));

                    ArrayList<Recommendation> recommendationsById = new ArrayList<>(groupRecommendations);

                    Collection<Integer> requests = groupRecommenderSystemResult.getGroupInput(groupOfUsers).getItemsRequested();

                    requests.removeAll(recommendationsById.stream().map((recommendation) -> recommendation.getIdItem()).collect(Collectors.toList()));
                    recommendationsById.addAll(requests.stream().map((idItem) -> new Recommendation(idItem, Float.NaN)).collect(Collectors.toList()));

                    Collections.sort(recommendationsById, Recommendation.BY_ID);

                    groupResults.addContent(RecommendationsToXML.getRecommendationsElement(new Recommendations(
                            groupOfUsers.toString(),
                            recommendationsById)
                    ));

                    neighborsDetails.addContent(groupResults);
                } else {
                    return new GroupEvaluationMeasureResult(this, 0);
                }

            }
        }

        return new GroupEvaluationMeasureResult(this, 1.0, neighborsDetails);
    }

}
