package delfos.main.managers;

import delfos.main.managers.CaseUseManager;
import java.util.List;
import org.junit.Assert;
import delfos.ConsoleParameters;
import delfos.main.Main;

/**
 *
 * @author jcastro
 */
public class CaseUseManagerTest {

    public static void testCaseUse(CaseUseManager caseUseManager, String[] consoleArguments) {
        testCaseUse(caseUseManager, new ConsoleParameters(consoleArguments));
    }

    public static void testCaseUse(CaseUseManager caseUseManager, ConsoleParameters consoleParameters) {

        boolean rightManager = caseUseManager.isRightManager(consoleParameters);
        Assert.assertTrue(caseUseManager + " should have been triggered for command line '" + consoleParameters.printOriginalParameters() + "'", rightManager);
        List<CaseUseManager> caseUseManagers = Main.getAllCaseUseManagers();
        Assert.assertTrue(caseUseManager.getClass() + " not in Main.getAllCaseUseManagers() method", caseUseManagers.contains(caseUseManager));
        List<CaseUseManager> triggeredCaseUseManagers = Main.getSuitableCaseUseManagers(caseUseManagers, consoleParameters);
        Assert.assertFalse("No case use activated for command line " + consoleParameters.printOriginalParameters(), triggeredCaseUseManagers.isEmpty());
        Assert.assertTrue(
                "More than one case use manager activated for command line "
                + consoleParameters.printOriginalParameters() + "\n"
                + "Cases activated: " + triggeredCaseUseManagers.toString(),
                triggeredCaseUseManagers.size() == 1
        );

        Assert.assertEquals("Expected case use '" + caseUseManager + "', while the triggered was '" + triggeredCaseUseManagers.get(0) + "'", caseUseManager, triggeredCaseUseManagers.get(0));
    }

    public CaseUseManagerTest() {
    }

}
