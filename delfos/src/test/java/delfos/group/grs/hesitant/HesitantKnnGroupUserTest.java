package delfos.group.grs.hesitant;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.recommendation.Recommendation;
import es.jcastro.hesitant.HesitantValuation;
import java.util.Collection;
import org.junit.Test;

/**
 *
 * @author jcastro
 */
public class HesitantKnnGroupUserTest {

    public HesitantKnnGroupUserTest() {
    }

    @Test
    public void testRecommendationWholeProcess() throws Exception {
        MovieLens100k datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        GroupOfUsers groupOfUsers = new GroupOfUsers(4, 8, 15, 16, 23, 42);
        RecommendationCandidatesSelector selector = new OnlyNewItems();

        HesitantKnnGroupUser grs = new HesitantKnnGroupUser();
        Object recommendationModel = grs.buildRecommendationModel(datasetLoader);
        HesitantValuation groupModel = grs.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);

        Collection<Recommendation> groupRecommendations = grs.recommendOnly(
                datasetLoader,
                recommendationModel,
                groupModel,
                groupOfUsers,
                selector.candidateItems(datasetLoader, groupOfUsers)
        );

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw(SortBy.SORT_BY_PREFERENCE);
        output.writeRecommendations(new GroupRecommendations(groupOfUsers, groupRecommendations));
    }

}
