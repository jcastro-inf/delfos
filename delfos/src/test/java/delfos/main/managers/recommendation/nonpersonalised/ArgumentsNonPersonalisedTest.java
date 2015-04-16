package delfos.main.managers.recommendation.nonpersonalised;

import delfos.main.managers.recommendation.nonpersonalised.Recommend;
import delfos.main.managers.recommendation.nonpersonalised.BuildRecommendationModel;
import java.io.File;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import delfos.ConsoleParameters;
import delfos.common.FileUtilities;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.main.Main;
import delfos.main.managers.CaseUseManagerTest;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.nonpersonalised.NonPersonalisedRecommender;
import delfos.rs.nonpersonalised.meanrating.wilsonscoreonterval.WilsonScoreLowerBound;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.persistence.FilePersistence;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class ArgumentsNonPersonalisedTest extends DelfosTest {

    private static final String TEST_DIRECTORY = DelfosTest.getTemporalDirectoryForTest(ArgumentsNonPersonalisedTest.class).getPath() + File.separator;
    private static final File NON_PERSONALISED_RS_CONFIG_XML = new File(TEST_DIRECTORY + "non-personalised-rs-config.xml");

    public ArgumentsNonPersonalisedTest() {
    }

    @Before
    public void beforeTest() {
        FileUtilities.cleanDirectory(new File(TEST_DIRECTORY));
    }

    @Test
    public void test_NonPersonalised_BuildRecommendationModel_manageCaseUse() throws Exception {
        System.out.println("test_NonPersonalised_BuildRecommendationModel_manageCaseUse");

        createConfigurationFile();

        ConsoleParameters consoleParameters = ArgumentsNonPersonalisedTestSuite.buildRecommendationModel(NON_PERSONALISED_RS_CONFIG_XML);

        boolean rightManager = BuildRecommendationModel.getInstance().isRightManager(consoleParameters);
        Assert.assertTrue(BuildRecommendationModel.class + " should have been triggered", rightManager);

        BuildRecommendationModel.getInstance().manageCaseUse(consoleParameters);

    }

    @Test
    public void test_NonPersonalised_Recommend_manageCaseUse() throws Exception {
        System.out.println("test_NonPersonalised_Recommend_manageCaseUse");

        test_NonPersonalised_BuildRecommendationModel_manageCaseUse();

        ConsoleParameters consoleParameters = ArgumentsNonPersonalisedTestSuite.recommendAnonymous(NON_PERSONALISED_RS_CONFIG_XML);

        boolean rightManager = Recommend.getInstance().isRightManager(consoleParameters);
        Assert.assertTrue(BuildRecommendationModel.class + " should have been triggered", rightManager);

        Recommend.getInstance().manageCaseUse(consoleParameters);

    }

    @Test
    public void test_NonPersonalised_BuildRecommendationModel_callFromCommandLine() throws Exception {
        System.out.println("test_NonPersonalised_BuildRecommendationModel_callFromCommandLine");

        createConfigurationFile();

        ConsoleParameters consoleParameters = ArgumentsNonPersonalisedTestSuite.buildRecommendationModel(NON_PERSONALISED_RS_CONFIG_XML);

        CaseUseManagerTest.testCaseUse(BuildRecommendationModel.getInstance(), consoleParameters);
        Main.mainWithExceptions(consoleParameters);
    }

    @Test
    public void test_NonPersonalised_Recommend_callFromCommandLine() throws Exception {
        System.out.println("test_NonPersonalised_Recommend_callFromCommandLine");

        createConfigurationFile();
        test_NonPersonalised_BuildRecommendationModel_callFromCommandLine();

        ConsoleParameters consoleParameters = ArgumentsNonPersonalisedTestSuite.recommendAnonymous(NON_PERSONALISED_RS_CONFIG_XML);

        CaseUseManagerTest.testCaseUse(Recommend.getInstance(), consoleParameters);
        Main.mainWithExceptions(consoleParameters);
    }

    @Test
    public void test_NonPersonalised_RecommendToGivenUser_callFromCommandLine() throws Exception {
        System.out.println("test_NonPersonalised_RecommendToGivenUser_callFromCommandLine");

        int idUser = 23;

        createConfigurationFile();
        test_NonPersonalised_BuildRecommendationModel_callFromCommandLine();

        ConsoleParameters consoleParameters = ArgumentsNonPersonalisedTestSuite.recommendToUser(NON_PERSONALISED_RS_CONFIG_XML, idUser);

        CaseUseManagerTest.testCaseUse(Recommend.getInstance(), consoleParameters);
        Main.mainWithExceptions(consoleParameters);
    }

    private void createConfigurationFile() {

        DatasetLoader<? extends Rating> datasetLoader = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        NonPersonalisedRecommender<? extends Object> nonPersonalisedRecommender = new WilsonScoreLowerBound();
        FilePersistence filePersistence = new FilePersistence("recommendation-model-" + nonPersonalisedRecommender.getAlias().toLowerCase(), "data", new File(TEST_DIRECTORY));

        RecommenderSystemConfigurationFileParser.saveConfigFile(NON_PERSONALISED_RS_CONFIG_XML.getAbsolutePath(),
                nonPersonalisedRecommender,
                datasetLoader,
                filePersistence,
                new OnlyNewItems(),
                new RecommendationsOutputStandardRaw(SortBy.SORT_BY_PREFERENCE));
    }
}
