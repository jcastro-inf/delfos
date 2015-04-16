package delfos.group.grs.penalty;

import delfos.group.grs.penalty.PenaltyGRS_Ratings;
import java.util.List;
import org.junit.Test;
import delfos.common.aggregationoperators.penalty.functions.PenaltyWholeMatrix;
import delfos.constants.DelfosTest;
import delfos.dataset.loaders.csv.CSVfileDatasetLoader_table;
import delfos.group.groupsofusers.GroupOfUsers;
import delfos.group.grs.SingleRecommenderSystemModel;
import delfos.group.grs.aggregation.GroupModelPseudoUser;
import delfos.group.grs.penalty.grouper.Grouper;
import delfos.group.grs.penalty.grouper.GrouperByDataClustering;
import delfos.rs.collaborativefiltering.svd.SVDFoldingIn;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.recommendation.Recommendation;
import delfos.rs.recommendation.RecommendationsFactory;
import delfos.similaritymeasures.CosineCoefficient;

/**
 *
 * @version 14-oct-2014
 * @author Jorge Castro Gallardo (Universidad de Jaén, Sinbad2)
 */
public class PenaltyExcelExample extends DelfosTest {

    @Test
    public void test() throws Exception {

        CSVfileDatasetLoader_table datasetLoader = new CSVfileDatasetLoader_table("/home/jcastro/Temp/dataset_penaltis");

        GroupOfUsers groupOfUsers = new GroupOfUsers(8, 9, 10);
        Grouper itemGrouper = new GrouperByDataClustering(3, 3, 10, 2, new CosineCoefficient());

        PenaltyGRS_Ratings penaltyGRS_Ratings = new PenaltyGRS_Ratings(new SVDFoldingIn(10, 1000), new PenaltyWholeMatrix(1, 3), itemGrouper, null);

        SingleRecommenderSystemModel recommendationModel = penaltyGRS_Ratings.build(datasetLoader);

        GroupModelPseudoUser groupModel = penaltyGRS_Ratings.buildGroupModel(datasetLoader, recommendationModel, groupOfUsers);

        List<Recommendation> recommendOnly = penaltyGRS_Ratings.recommendOnly(datasetLoader, recommendationModel, groupModel, groupOfUsers, datasetLoader.getRatingsDataset().allRatedItems());

        RecommendationsOutputStandardRaw output = new RecommendationsOutputStandardRaw();

        output.writeRecommendations(RecommendationsFactory.createRecommendations(groupOfUsers, recommendOnly));
    }
}
