package delfos.group.grs.hesitant;

import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.dataset.basic.user.User;
import delfos.dataset.loaders.movilens.ml100k.MovieLens100k;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.recommendations.GroupRecommendations;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.recommendationcandidates.RecommendationCandidatesSelector;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.recommendation.Recommendation;
import delfos.utils.hesitant.HesitantValuation;
import java.util.Arrays;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class HesitantKnnGroupUserTest {

    public HesitantKnnGroupUserTest() {
    }

    @Test
    public void testRecommendationWholeProcess() throws Exception {
        MovieLens100k datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        datasetLoader.getRatingsDataset();

        Set<User> members = Arrays.asList(4, 8, 15, 16, 23, 42).stream()
                .map(idUser -> datasetLoader.getUsersDataset().get(idUser))
                .collect(Collectors.toSet());

        GroupOfUsers groupOfUsers = new GroupOfUsers(members);
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

    @Test
    public void testRecommendationWholeProcessDeleteRepeated() throws Exception {
        MovieLens100k datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k", MovieLens100k.class);
        datasetLoader.getRatingsDataset();

        Set<User> members = Arrays.asList(4, 8, 15, 16, 23, 42).stream()
                .map(idUser -> datasetLoader.getUsersDataset().get(idUser))
                .collect(Collectors.toSet());

        GroupOfUsers groupOfUsers = new GroupOfUsers(members);
        RecommendationCandidatesSelector selector = new OnlyNewItems();

        HesitantKnnGroupUser grs = new HesitantKnnGroupUser();
        grs.setParameterValue(HesitantKnnGroupUser.DELETE_REPEATED, true);

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
