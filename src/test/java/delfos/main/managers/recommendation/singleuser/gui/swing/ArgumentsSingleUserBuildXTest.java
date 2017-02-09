package delfos.main.managers.recommendation.singleuser.gui.swing;

import delfos.ConsoleParameters;
import delfos.common.FileUtilities;
import delfos.configfile.rs.single.RecommenderSystemConfigurationFileParser;
import delfos.configureddatasets.ConfiguredDatasetsFactory;
import delfos.constants.DelfosTest;
import delfos.dataset.basic.loader.types.DatasetLoader;
import delfos.dataset.basic.rating.Rating;
import delfos.main.managers.CaseUseManagerTest;
import delfos.recommendationcandidates.OnlyNewItems;
import delfos.rs.RecommenderSystem;
import delfos.rs.collaborativefiltering.knn.memorybased.KnnMemoryBasedCFRS;
import delfos.rs.output.RecommendationsOutputStandardRaw;
import delfos.rs.output.sort.SortBy;
import delfos.rs.persistence.FilePersistence;
import java.io.File;
import org.junit.Before;
import org.junit.Test;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class ArgumentsSingleUserBuildXTest extends DelfosTest {

    public ArgumentsSingleUserBuildXTest() {
    }

    /**
     * Directorio en el que se almacenan los ficheros relacionados con los tests del manejo de un dataset.
     */
    private final static String TEST_DIRECTORY = DelfosTest.getTemporalDirectoryForTest(ArgumentsSingleUserBuildXTest.class).getPath() + File.separator;
    /**
     * Nombre del fichero que almacena la configuración del dataset manejado por la biblioteca.
     */
    private final static String SINGLE_USER_RS_CONFIG_XML = TEST_DIRECTORY + "single-user-rs-config.xml";

    @Before
    public void beforeTest() {
        FileUtilities.cleanDirectory(new File(TEST_DIRECTORY));
    }

    @Test
    public void test_BuildX_isRightManager() throws Exception {

        createConfigurationFile();
        String[] consoleArguments = {
            "-rs-config",
            SINGLE_USER_RS_CONFIG_XML,
            "--single-user-build-x"
        };

        BuildConfigurationFileGUI.getInstance().isRightManager(ConsoleParameters.parseArguments(consoleArguments));
    }

    @Test
    public void test_SingleUser_BuildX_callFromCommandLine() throws Exception {

        createConfigurationFile();
        ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(
                "-rs-config",
                SINGLE_USER_RS_CONFIG_XML,
                "--single-user-build-x"
        );
        CaseUseManagerTest.testCaseUse(BuildConfigurationFileGUI.getInstance(), consoleParameters);
    }

    private void createConfigurationFile() {

        File directory = new File(TEST_DIRECTORY);

        RecommenderSystem<? extends Object> singleUserRecommender
                = new KnnMemoryBasedCFRS();

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
