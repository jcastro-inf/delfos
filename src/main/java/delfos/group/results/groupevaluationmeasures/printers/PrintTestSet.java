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

import delfos.Constants;
import delfos.common.FileUtilities;
import delfos.common.exceptions.dataset.users.UserNotFound;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.recommendation.Recommendation;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Writes extended information of the recommendation in an XML file. This information is the test ratings of each group
 * and its recommendations.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PrintTestSet extends GroupEvaluationMeasureInformationPrinter {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        File output = new File(PRINTER_DIRECTORY.getPath() + File.separator
                + groupRecommenderSystemResult.getGroupCaseStudyAlias() + "__"
                + "exec=" + groupRecommenderSystemResult.getThisExecution()
                + "-split=" + groupRecommenderSystemResult.getThisSplit()
                + "-test-set.txt");

        StringBuilder str = new StringBuilder();

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult
                    .getGroupOutput(groupOfUsers)
                    .getRecommendations().getRecommendations();

            if (groupRecommendations.isEmpty()) {
                str.append("No recommendations for group ").append(groupOfUsers).append("\n");
            } else {
                Set<Long> items = Recommendation.getSetOfItems(groupRecommendations);

                Map<Long, Map<Long, Number>> membersRatings = new TreeMap<>();

                membersRatings.put(8888l, Recommendation.convertToMapOfNumbers_onlyRankPreference(groupRecommendations));
                membersRatings.put(9999l, Recommendation.convertToMapOfNumbers(groupRecommendations));

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
                                Logger.getLogger(PrintTestSet.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            membersRatings.put(idMember, thisMemberRatings);
                        });

                String printCompactRatingTable = DatasetPrinter.printCompactRatingTable(membersRatings);
                str.append("==============================================================").append("\n");
                str.append(printCompactRatingTable).append("\n");
                str.append("==============================================================").append("\n");

                if (Constants.isRawResultDefined()) {
                    str.append(printRawOutput(groupOfUsers,
                            groupRecommendations.stream().sorted(Recommendation.BY_PREFERENCE_DESC).collect(Collectors.toList()),
                            membersRatings));
                }
            }
        }

        FileUtilities.createDirectoriesForFile(output);

        try (BufferedWriter bufferedWriter = new BufferedWriter(new FileWriter(output))) {
            bufferedWriter.write(str.toString());
        } catch (IOException ex) {
            Logger.getLogger(PrintTestSet.class.getName()).log(Level.SEVERE, null, ex);
        }

        return new GroupEvaluationMeasureResult(this, 1.0);
    }

    private StringBuilder printRawOutput(GroupOfUsers groupOfUsers, List<Recommendation> recommendations, Map<Long, Map<Long, Number>> membersRatings) {

        StringBuilder rawData = new StringBuilder();

        rawData.append("idItem\tprediction\trank\t");
        for (Long member : groupOfUsers) {
            rawData.append(member).append("\t");
        }
        rawData.setCharAt(rawData.length() - 1, '\n');

        List<Recommendation> recommendationsSortedById = new ArrayList<>(recommendations);
        Collections.sort(recommendationsSortedById, Recommendation.BY_ID);

        Map<Recommendation, Long> recommendationsRank = new TreeMap();

        ArrayList<Recommendation> groupRecommendationSortedByPreference = new ArrayList<>(recommendations);
        Collections.sort(groupRecommendationSortedByPreference, Recommendation.BY_PREFERENCE_DESC);
        groupRecommendationSortedByPreference.stream().forEachOrdered((recommendation) -> {
            recommendationsRank.put(recommendation, (long) (recommendationsRank.size() + 1));
        });
        recommendationsSortedById.stream().forEachOrdered(recommendation -> {
            rawData.append(
                    recommendation.getIdItem()).append("\t")
                    .append(recommendation.getPreference().doubleValue()).append("\t")
                    .append(recommendationsRank.get(recommendation)).append("\t");
            for (Long member : groupOfUsers) {
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
