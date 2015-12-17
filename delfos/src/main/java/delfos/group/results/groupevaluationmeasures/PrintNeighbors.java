package delfos.group.results.groupevaluationmeasures;

import delfos.Constants;
import delfos.ERROR_CODES;
import delfos.common.FileUtilities;
import delfos.dataset.basic.rating.Rating;
import delfos.dataset.basic.rating.RatingsDataset;
import delfos.dataset.basic.rating.RelevanceCriteria;
import delfos.group.groupsofusers.GroupOfUsers;
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
 * @author Jorge Castro Gallardo
 */
public class PrintNeighbors extends GroupEvaluationMeasure {

    @Override
    public boolean usesRatingPrediction() {
        return false;
    }

    public static final File TEST_SET_DIRECTORY = new File(
            Constants.getTempDirectory().getAbsoluteFile() + File.separator
            + "GroupCaseStudy-Print" + File.separator);

    @Override
    public GroupEvaluationMeasureResult getMeasureResult(GroupRecommenderSystemResult groupRecommenderSystemResult, RatingsDataset<? extends Rating> testDataset, RelevanceCriteria relevanceCriteria) {

        FileUtilities.createDirectoryPath(TEST_SET_DIRECTORY);

        String fileName = TEST_SET_DIRECTORY.getPath() + File.separator
                + groupRecommenderSystemResult.getGroupCaseStudyAlias() + "__"
                + "-exec=" + groupRecommenderSystemResult.getThisExecution()
                + "-split=" + groupRecommenderSystemResult.getThisSplit()
                + "-neighbors.xml";

        File file = new File(fileName);
        boolean writeFile = false;

        Element neighborsDetails = new Element("NeighborsDetails");

        for (GroupOfUsers groupOfUsers : groupRecommenderSystemResult) {
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

            FileUtilities.createDirectoriesForFile(file);
            try (FileWriter fileWriter = new FileWriter(file)) {
                outputter.output(neighborsDetails, fileWriter);
            } catch (IOException ex) {
                ERROR_CODES.CANNOT_WRITE_RESULTS_FILE.exit(ex);
            }
        }

        return new GroupEvaluationMeasureResult(this, 1.0);
    }

}
