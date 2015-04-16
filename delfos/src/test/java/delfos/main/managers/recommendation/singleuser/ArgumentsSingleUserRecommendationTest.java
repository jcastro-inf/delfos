package delfos.main.managers.recommendation.singleuser;

import delfos.main.managers.recommendation.singleuser.Recommend;
import delfos.main.managers.recommendation.singleuser.BuildRecommendationModel;
import java.io.File;
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
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.nwr.KnnMemoryBasedNWR;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.persistence.FilePersistence;

/**
 *
 * @author Jorge Castro Gallardo
 */
public class ArgumentsSingleUserRecommendationTest extends DelfosTest {

    public ArgumentsSingleUserRecommendationTest() {
    }

    /**
     * Directorio en el que se almacenan los ficheros relacionados con los tests
     * del manejo de un dataset.
     */
    private final static String TEST_DIRECTORY = DelfosTest.getTemporalDirectoryForTest(ArgumentsSingleUserRecommendationTest.class).getPath() + File.separator;
    /**
     * Nombre del fichero que almacena la configuración del dataset manejado por
     * la biblioteca.
     */
    private final static String SINGLE_USER_RS_CONFIG_XML = TEST_DIRECTORY + "csvDatasetConfiguration.xml";

    @Before
    public void beforeTest() {
        FileUtilities.cleanDirectory(new File(TEST_DIRECTORY));
    }

    @Test
    public void test_SingleUser_BuildRecommendationModel_manageCaseUse() throws Exception {
        System.out.println("test_SingleUser_BuildRecommendationModel_manageCaseUse");

        createConfigurationFile();
        String[] consoleArguments = {
            "--single-user",
            "--build",
            "-config-file", SINGLE_USER_RS_CONFIG_XML
        };

        BuildRecommendationModel.getInstance().manageCaseUse(new ConsoleParameters(consoleArguments));
    }

    @Test
    public void test_SingleUser_Recommend_manageCaseUse() throws Exception {
        System.out.println("test_SingleUser_Recommend_manageCaseUse");

        createConfigurationFile();

        test_SingleUser_BuildRecommendationModel_manageCaseUse();

        String[] consoleArguments = {
            "--single-user",
            "--recommend",
            "-u", "1",
            "-config-file", SINGLE_USER_RS_CONFIG_XML

        };

        CaseUseManagerTest.testCaseUse(Recommend.getInstance(), new ConsoleParameters(consoleArguments));

        Recommend.getInstance()
                .manageCaseUse(new ConsoleParameters(consoleArguments));
    }

    @Test
    public void test_SingleUser_BuildRecommendationModel_callFromCommandLine() throws Exception {
        System.out.println("test_SingleUser_BuildRecommendationModel_manageCaseUse");

        createConfigurationFile();
        String[] consoleArguments = {
            "--single-user",
            "--build",
            "-config-file", SINGLE_USER_RS_CONFIG_XML
        };

        CaseUseManagerTest.testCaseUse(BuildRecommendationModel.getInstance(), consoleArguments);
        Main.mainWithExceptions(consoleArguments);
    }

    @Test
    public void test_SingleUser_Recommend_callFromCommandLine() throws Exception {
        System.out.println("test_SingleUser_Recommend_callFromCommandLine");

        createConfigurationFile();

        test_SingleUser_BuildRecommendationModel_manageCaseUse();

        String[] consoleArguments = {
            "--single-user",
            "--recommend",
            "-u", "1",
            "-config-file", SINGLE_USER_RS_CONFIG_XML

        };

        CaseUseManagerTest.testCaseUse(Recommend.getInstance(), consoleArguments);
        Main.mainWithExceptions(consoleArguments);
    }

    private void createConfigurationFile() {

        File directory = new File(TEST_DIRECTORY);

        RecommenderSystem<? extends Object> singleUserRecommender
                = new KnnMemoryBasedNWR();

        DatasetLoader<? extends Rating> datasetLoader
                = ConfiguredDatasetsFactory.getInstance().getDatasetLoader("ml-100k");

        FilePersistence filePersistence = new FilePersistence(
                "recommendation-model-" + singleUserRecommender.getAlias().toLowerCase(),
                "data",
                directory);

        RecommenderSystemConfigurationFileParser.saveConfigFile(
                SINGLE_USER_RS_CONFIG_XML,
                singleUserRecommender,
                datasetLoader,
                filePersistence,
                new OnlyNewItems(),
                new RecommendationsOutputStandardRaw(SortBy.SORT_BY_PREFERENCE));
    }
}
