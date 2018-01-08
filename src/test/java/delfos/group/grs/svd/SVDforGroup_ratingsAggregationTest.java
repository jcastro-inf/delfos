package delfos.group.grs.svd;

import delfos.dataset.basic.item.Item;
import delfos.dataset.generated.random.RandomDatasetLoader;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVD;
import delfos.rs.collaborativefiltering.svd.TryThisAtHomeSVDModel;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class SVDforGroup_ratingsAggregationTest {

    public SVDforGroup_ratingsAggregationTest() {
    }

    @Test
    public void testWithSomeUsers() throws Exception {

        RandomDatasetLoader randomDataset = new RandomDatasetLoader(50, 100, 0.5);

        SVDforGroup_ratingsAggregation grs = new SVDforGroup_ratingsAggregation();
        grs.setParameterValue(TryThisAtHomeSVD.LEARNING_RATE, 0.02f);
        grs.setParameterValue(TryThisAtHomeSVD.K, 0.02f);
        TryThisAtHomeSVDModel recommendationModel = grs.buildRecommendationModel(randomDataset);

        GroupOfUsers group = new GroupOfUsers(1l, 2l, 3l);
        GroupSVDModel groupModel = grs.buildGroupModel(randomDataset, recommendationModel, group);

        Set<Item> candidateItems = candidateItems = group.getMembers().stream()
                .map(member -> randomDataset.getRatingsDataset().getUserRated(member.getId()))
                .flatMap(ratings -> ratings.stream())
                .map(idItem -> randomDataset.getContentDataset().get(idItem))
                .collect(Collectors.toSet());

        GroupRecommendations recommendOnly = grs.recommendOnly(randomDataset, recommendationModel, groupModel, group, candidateItems);

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();
        output.writeRecommendations(recommendOnly);
    }
}
