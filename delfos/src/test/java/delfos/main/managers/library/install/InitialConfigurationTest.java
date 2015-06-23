package delfos.main.managers.library.install;

import delfos.ConsoleParameters;
import delfos.constants.DelfosTest;
import delfos.main.managers.CaseUseManagerTest;
import java.io.File;
import org.junit.Test;

/**
 * Tests the case use for the initial configuration of the library.
 *
 * @author jcastro
 */
public class InitialConfigurationTest extends DelfosTest {

    public InitialConfigurationTest() {
    }

    @Test
    public void testInitialConfigurationCaseUse() throws Exception {
        String delfosConfigurationDirectory = getTemporalDirectoryForTest().getAbsolutePath() + File.separator + ".config" + File.separator + "delfos";

        ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(
                "--initial-config",
                "-datasets-dir", "./delfos-install/datasets",
                "-config", delfosConfigurationDirectory
        );

        CaseUseManagerTest.testCaseUse(
                InitialConfiguration.getInstance(),
                consoleParameters
        );

        InitialConfiguration.getInstance()
                .manageCaseUse(consoleParameters);
    }

}
