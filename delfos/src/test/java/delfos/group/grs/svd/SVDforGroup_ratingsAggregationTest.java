package delfos.group.grs.svd;

import delfos.common.Global;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.dataset.util.DatasetPrinter;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationComputationDetails;
import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class SVDforGroup_ratingsAggregationTest {

    public SVDforGroup_ratingsAggregationTest() {
    }

    @Test
    public void testWithSomeUsers() throws Exception {

        Global.setVerbose();
        RandomDatasetLoader randomDataset = new RandomDatasetLoader(50, 100, 0.5);

        SVDforGroup_ratingsAggregation grs = new SVDforGroup_ratingsAggregation();
        grs.setParameterValue(TryThisAtHomeSVD.LEARNING_RATE, 0.02f);
        grs.setParameterValue(TryThisAtHomeSVD.K, 0.02f);
        TryThisAtHomeSVDModel recommenderSystemModel = grs.build(randomDataset);

        GroupOfUsers group = new GroupOfUsers(1, 2, 3);
        GroupSVDModel groupModel = grs.buildGroupModel(randomDataset, recommenderSystemModel, group);

        Set<Integer> idItemList = new TreeSet<>();
        for (int idUser : group) {
            idItemList.addAll(randomDataset.getRatingsDataset().getUserRated(idUser));
        }
        Collection<Recommendation> recommendOnly = grs.recommendOnly(randomDataset, recommenderSystemModel, groupModel, group, idItemList);

        Global.showMessage(DatasetPrinter.printCompactRatingTable(randomDataset.getRatingsDataset(), group.getGroupMembers(), idItemList));

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(new GroupRecommendations(group, recommendOnly, RecommendationComputationDetails.EMPTY_DETAILS));
    }
}
