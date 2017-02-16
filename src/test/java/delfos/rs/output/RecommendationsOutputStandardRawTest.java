package delfos.rs.output;

import delfos.constants.DelfosTest;
import delfos.dataset.basic.user.User;
import delfos.rs.output.sort.SortBy;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.Recommendations;
import delfos.rs.recommendation.RecommendationsFactory;
import delfos.rs.recommendation.RecommendationsToUser;
import java.util.LinkedList;
import java.util.List;
import org.junit.Test;

public class RecommendationsOutputStandardRawTest extends DelfosTest {

    public RecommendationsOutputStandardRawTest() {
    }

    @Test
    public void testWriteRecommendations_defaultConstructor() {
        RecommendationsOutputStandardRaw instance = new RecommendationsOutputStandardRaw();
        instance.writeRecommendations(getRecommendations());
    }

    @Test
    public void testWriteRecommendations_sortByIdItem() {
        RecommendationsOutputStandardRaw instance = new RecommendationsOutputStandardRaw(SortBy.SORT_BY_ID_ITEM);
        instance.writeRecommendations(getRecommendations());
    }

    @Test
    public void testWriteRecommendations_sortByPreference() {
        RecommendationsOutputStandardRaw instance = new RecommendationsOutputStandardRaw(SortBy.SORT_BY_PREFERENCE);
        instance.writeRecommendations(getRecommendations());
    }

    @Test
    public void testWriteRecommendations_topFive() {

        RecommendationsOutputStandardRaw instance = new RecommendationsOutputStandardRaw(5);
        instance.writeRecommendations(getRecommendations());
    }

    private Recommendations getRecommendations() {
        int idUser = 1;
        List<Recommendation> recommendations = new LinkedList<>();
        recommendations.add(new Recommendation(1, 4.98));
        recommendations.add(new Recommendation(3, 4.53));
        recommendations.add(new Recommendation(2, 4.34));
        recommendations.add(new Recommendation(10, 3.98));
        recommendations.add(new Recommendation(31, 3.53));
        recommendations.add(new Recommendation(12, 2.34));
        recommendations.add(new Recommendation(15, 1.98));
        recommendations.add(new Recommendation(37, 2.53));
        recommendations.add(new Recommendation(28, 1.34));
        recommendations.add(new Recommendation(19, 2.98));

        RecommendationsToUser recommendationsToUser = RecommendationsFactory.createRecommendations(new User(idUser), recommendations);
        return recommendationsToUser;
    }

}
