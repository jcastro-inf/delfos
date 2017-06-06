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
package delfos.group.results.groupevaluationmeasures.printers;

import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
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
 * Evaluation measure that prints the ratings used to produce the recommendations.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PrintGroupRatingsToPlainText extends GroupEvaluationMeasureInformationPrinter {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
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
            Collection<Recommendation> groupRecommendation = groupRecommenderSystemResult
                    .getGroupOutput(groupOfUsers)
                    .getRecommendations().getRecommendations();

            List<Neighbor> neighbors;

            if (!groupRecommendation.isEmpty() && groupRecommendation.iterator().next() instanceof RecommendationWithNeighbors) {
                RecommendationWithNeighbors recommendationWithNeighbors = (RecommendationWithNeighbors) groupRecommendation.iterator().next();
                neighbors = recommendationWithNeighbors.getNeighbors().stream().collect(Collectors.toList());
                Collections.sort(neighbors, (Neighbor o1, Neighbor o2) -> Long.compare(o1.getIdNeighbor(), o2.getIdNeighbor()));
            }

            if (groupRecommendation.isEmpty()) {
                str.append("No recommendations for group ").append(groupOfUsers).append("\n");
            } else {
                Set<Long> items = Recommendation.getSetOfItems(groupRecommendation);
                Map<Long, Map<Long, Number>> datasetToShow = new TreeMap<>();
                datasetToShow.put(8888l, Recommendation.convertToMapOfNumbers_onlyRankPreference(groupRecommendation));
                groupOfUsers
                        .getIdMembers().stream().forEach((idMember) -> {
                            Map<Long, Number> thisMemberRatings = new TreeMap<>();

                            try {
                                Map<Long, ? extends Rating> memberRatings = testDatasetLoader.getRatingsDataset().getUserRatingsRated(idMember);

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

        FileUtilities.createDirectoriesForFileIfNotExist(output);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output))) {
            bufferedWriter.write(str.toString());
        } catch (IOException ex) {
            ERROR_CODES.CANNOT_WRITE_FILE.exit(ex);
        }

        return new GroupEvaluationMeasureResult(this, 1.0);
    }

}
