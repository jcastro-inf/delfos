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
import delfos.rs.recommendation.Recommendation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Writes extended information of the recommendation in an XML file. This
 * information is the test ratings of each group and its recommendations.
 *
 * @version 05-oct-2014
 * @author Jorge Castro Gallardo
 */
public class GroupRecommendationMemberRatingsComparison extends GroupEvaluationMeasure {

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    public static final File TEST_SET_DIRECTORY = new File(
            Constants.getTempDirectory().getAbsoluteFile() + File.separator
            + GroupRecommendationMemberRatingsComparison.class.getSimpleName() + File.separator
            + "test-set" + File.separator);

    @Override
    public GroupMeasureResult getMeasureResult(GroupRecommendationResult recommendationResults, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        FileUtilities.createDirectoryPath(TEST_SET_DIRECTORY);

        String fileName = TEST_SET_DIRECTORY.getPath() + File.separator + recommendationResults.getCaseAlias() + "-testSet.xml";

        StringBuilder str = new StringBuilder();

        for (Map.Entry<GroupOfUsers, List<Recommendation>> entry : recommendationResults) {

            GroupOfUsers groupOfUsers = entry.getKey();
            List<Recommendation> recommendations = entry.getValue();

            if (recommendations.isEmpty()) {
                str.append("No recommendations for group ").append(groupOfUsers).append("\n");
            } else {
                Set<Integer> items = Recommendation.getSetOfItems(recommendations);

                Map<Integer, Map<Integer, Number>> membersRatings = new TreeMap<>();

                membersRatings.put(8888, Recommendation.convertToMapOfNumbers_onlyRankPreference(recommendations));
                membersRatings.put(9999, Recommendation.convertToMapOfNumbers(recommendations));

                groupOfUsers
                        .getIdMembers().stream().forEach((idMember) -> {
                            Map<Integer, Number> thisMemberRatings = new TreeMap<>();

                            try {
                                Map<Integer, ? extends Rating> memberRatings = testDataset.getUserRatingsRated(idMember);

                                memberRatings.values().stream()
                                .filter((rating) -> (items.contains(rating.getIdItem())))
                                .forEach((rating) -> {
                                    thisMemberRatings.put(rating.getIdItem(), rating.getRatingValue());
                                });
                            } catch (UserNotFound ex) {
                                Logger.getLogger(GroupRecommendationMemberRatingsComparison.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            membersRatings.put(idMember, thisMemberRatings);
                        });

                String printCompactRatingTable = DatasetPrinter.printCompactRatingTable(membersRatings);
                str.append("==============================================================").append("\n");
                str.append(printCompactRatingTable).append("\n");
                str.append("==============================================================").append("\n");

                if (Constants.isRawResultDefined()) {
                    str.append(printRawOutput(groupOfUsers, recommendations, membersRatings));
                }
            }
        }

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(new File(fileName)))) {
            bufferedWriter.write(str.toString());
        } catch (IOException ex) {
            Logger.getLogger(GroupRecommendationMemberRatingsComparison.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new GroupMeasureResult(this, 1.0);
    }

    private StringBuilder printRawOutput(GroupOfUsers groupOfUsers, List<Recommendation> recommendations, Map<Integer, Map<Integer, Number>> membersRatings) {

        StringBuilder rawData = new StringBuilder();

        rawData.append("idItem\tprediction\trank\t");
        for (Integer member : groupOfUsers) {
            rawData.append(member).append("\t");
        }
        rawData.setCharAt(rawData.length() - 1, '\n');

        List<Recommendation> recommendationsSortedById = new ArrayList<>(recommendations);
        Collections.sort(recommendationsSortedById, Recommendation.BY_ID);

        Map<Recommendation, Integer> recommendationsRank = new TreeMap();

        ArrayList<Recommendation> groupRecommendationSortedByPreference = new ArrayList<>(recommendations);
        Collections.sort(groupRecommendationSortedByPreference, Recommendation.BY_PREFERENCE_DESC);
        groupRecommendationSortedByPreference.stream().forEachOrdered((recommendation) -> {
            recommendationsRank.put(recommendation, recommendationsRank.size() + 1);
        });
        recommendationsSortedById.stream().forEachOrdered(recommendation -> {
            rawData.append(
                    recommendation.getIdItem()).append("\t")
                    .append(recommendation.getPreference().doubleValue()).append("\t")
                    .append(recommendationsRank.get(recommendation)).append("\t");
            for (Integer member : groupOfUsers) {
                String ratingStr;
                if (membersRatings.get(member).containsKey(recommendation.getIdItem())) {
                    ratingStr = membersRatings.get(member).get(recommendation.getIdItem()).toString();
                } else {
                    ratingStr = "-";
                }

                rawData.append(ratingStr).append("\t");
            }
            rawData.deleteCharAt(rawData.length() - 1);
            rawData.append("\n");
        });

        rawData.append("Recommendations_raw\n");
        return rawData;

    }

}
