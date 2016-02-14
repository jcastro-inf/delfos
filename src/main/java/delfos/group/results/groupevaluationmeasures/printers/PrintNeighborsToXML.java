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
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.results.groupevaluationmeasures.GroupEvaluationMeasureResult;
import delfos.group.results.groupevaluationmeasures.detailed.RecommendationsWithNeighborToXML;
import delfos.group.results.grouprecomendationresults.GroupRecommenderSystemResult;
import delfos.rs.collaborativefiltering.profile.Neighbor;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationWithNeighbors;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jdom2.Element;
import org.jdom2.output.XMLOutputter;

/**
 * Evaluation measure that prints the neighbors used to produce the
 * recommendations.
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class PrintNeighborsToXML extends GroupEvaluationMeasureInformationPrinter {

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(
            GroupRecommenderSystemResult groupRecommenderSystemResult,
            DatasetLoader<? extends Rating> originalDatasetLoader,
            RatingsDataset<? extends Rating> testDataset,
            RelevanceCriteria relevanceCriteria,
            DatasetLoader<? extends Rating> trainingDatasetLoader,
            DatasetLoader<? extends Rating> testDatasetLoader) {

        File output = new File(PRINTER_DIRECTORY.getPath() + File.separator
                + groupRecommenderSystemResult.getGroupCaseStudyAlias() + "__"
                + "exec=" + groupRecommenderSystemResult.getThisExecution()
                + "-split=" + groupRecommenderSystemResult.getThisSplit()
                + "-neighbors.xml");

        boolean writeFile = false;

        Element neighborsDetails = new Element("NeighborsDetails");

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult.getGroupsOfUsers()) {
            Collection<Recommendation> groupRecommendations = groupRecommenderSystemResult.getGroupOutput(groupOfUsers).getRecommendations();

            List<Neighbor> neighbors;

            if (groupRecommendations.isEmpty()) {
            } else {
                if ((groupRecommendations.iterator().next() instanceof RecommendationWithNeighbors)) {

                    writeFile = true;
                    RecommendationWithNeighbors recommendationWithNeighbors = (RecommendationWithNeighbors) groupRecommendations.iterator().next();
                    neighbors = recommendationWithNeighbors.getNeighbors().stream().collect(Collectors.toList());

                    Collections.sort(neighbors, Neighbor.BY_ID);
                    neighbors = Collections.unmodifiableList(neighbors);

                    Element groupResults = new Element("GroupResults");
                    groupResults.addContent(RecommendationsWithNeighborToXML.getNeighborsElement(
                            groupOfUsers.getTargetId(),
                            neighbors
                    ));

                    neighborsDetails.addContent(groupResults);
                } else {
                    return new GroupEvaluationMeasureResult(this, 0);
                }

            }
        }

        if (writeFile) {
            XMLOutputter outputter = new XMLOutputter(Constants.getXMLFormat());

            FileUtilities.createDirectoriesForFile(output);
            try (FileWriter fileWriter = new FileWriter(output)) {
                outputter.output(neighborsDetails, fileWriter);
            } catch (IOException ex) {
                ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
            }
        }

        return new GroupEvaluationMeasureResult(this, 1.0);
    }

}
