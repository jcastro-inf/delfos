package delfos.group.results.groupevaluationmeasures.printers;

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationWithNeighbors;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

/**
 * Evaluation measure that prints the ratings used to produce the
 * recommendations.
 *
 * @author Jorge Castro Gallardo
 */
public class PrintGroupRatingsToPlainText extends GroupEvaluationMeasureInformationPrinter {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        File output = new File(PRINTER_DIRECTORY.getPath() + File.separator
                + groupRecommenderSystemResult.getGroupCaseStudyAlias() + File.separator
                + "exec=" + groupRecommenderSystemResult.getThisExecution()
                + "-split=" + groupRecommenderSystemResult.getThisSplit()
                + "-group-ratings.txt");
        StringBuilder str = new StringBuilder();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendation = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            List<Neighbor> neighbors;

            if (!groupRecommendation.isEmpty() && groupRecommendation.iterator().next() instanceof RecommendationWithNeighbors) {
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
                                ERROR_CODES.USER_NOT_FOUND.exit(ex);
                            }
                            datasetToShow.put(idMember, thisMemberRatings);
                        });

                String printCompactRatingTable = DatasetPrinter.printCompactRatingTable(datasetToShow);
                str.append("==============================================================").append("\n");
                str.append(printCompactRatingTable).append("\n");
                str.append("==============================================================").append("\n");
            }
        }

        FileUtilities.createDirectoriesForFile(output);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output))) {
            bufferedWriter.write(str.toString());
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }

        return new GroupEvaluationMeasureResult(this, 1.0);
    }

}
