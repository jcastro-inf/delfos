package delfos.group.results.groupevaluationmeasures;

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.grouprecomendationresults.GroupRecommendationResult;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationWithNeighbors;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Evaluation measure that prints the neighbors used to produce the
 * recommendations.
 *
 * @author Jorge Castro Gallardo
 */
public class PrintNeighborsToXML extends GroupEvaluationMeasure {

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    public static final File TEST_SET_DIRECTORY = new File(
            Constants.getTempDirectory().getAbsoluteFile() + File.separator
            + PrintNeighborsToXML.class.getSimpleName() + File.separator
            + "test-set" + File.separator);

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        FileUtilities.createDirectoryPath(TEST_SET_DIRECTORY);

        String fileName = TEST_SET_DIRECTORY.getPath() + File.separator + recommendationResults.getCaseAlias() + "-testSet.xml";

        StringBuilder str = new StringBuilder();

        for (Map.Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {

            GroupOfUsers groupOfUsers = entry.getKey();
            List<Recommendation> groupRecommendation = entry.getValue();
            List<Neighbor> neighbors;

            if ((groupRecommendation.iterator().next() instanceof RecommendationWithNeighbors)) {
                RecommendationWithNeighbors recommendationWithNeighbors = (RecommendationWithNeighbors) groupRecommendation.iterator().next();
                neighbors = recommendationWithNeighbors.getNeighbors().stream().collect(Collectors.toList());

                Collections.sort(neighbors, (Neighbor o1, Neighbor o2) -> Integer.compare(o1.getIdNeighbor(), o2.getIdNeighbor()));
            }

            if (groupRecommendation.isEmpty()) {
                str.append("No recommendations for group ").append(groupOfUsers).append("\n");
            } else {
                Set<Integer> items = Recommendation.getSetOfItems(groupRecommendation);

                Map<Integer, Map<Integer, Number>> datasetToShow = new TreeMap<>();

                datasetToShow.put(8888, Recommendation.convertToMapOfNumbers_onlyRankPreference(groupRecommendation));

                groupOfUsers
                        .getGroupMembers().stream().forEach((idMember) -> {
                            Map<Integer, Number> thisMemberRatings = new TreeMap<>();

                            try {
                                Map<Integer, ? extends Rating> memberRatings = testDataset.getUserRatingsRated(idMember);

                                memberRatings.values().stream()
                                .filter((rating) -> (items.contains(rating.getIdItem())))
                                .forEach((rating) -> {
                                    thisMemberRatings.put(rating.getIdItem(), rating.getRatingValue());
                                });
                            } catch (UserNotFound ex) {
                                Logger.getLogger(PrintNeighborsToXML.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            datasetToShow.put(idMember, thisMemberRatings);
                        });

                String printCompactRatingTable = DatasetPrinter.printCompactRatingTable(datasetToShow);
                str.append("==============================================================").append("\n");
                str.append(printCompactRatingTable).append("\n");
                str.append("==============================================================").append("\n");
            }
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(fileName)))) {
            bufferedWriter.write(str.toString());
        } catch (IOException ex) {
            Logger.getLogger(PrintNeighborsToXML.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new GroupMeasureResult(this, 1.0);
    }

}
