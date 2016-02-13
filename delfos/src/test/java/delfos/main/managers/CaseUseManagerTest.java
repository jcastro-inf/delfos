package delfos.main.managers;

import delfos.ConsoleParameters;
import delfos.main.Main;
import java.util.List;
import org.junit.Assert;

/**
 *
 * @author jcastro-inf ( https://github.com/jcastro-inf )
 */
public class CaseUseManagerTest {

    public CaseUseManagerTest() {
    }

    public static void testCaseUse(CaseUseMode caseUseManager, ConsoleParameters consoleParameters) {

        boolean rightManager = caseUseManager.isRightManager(consoleParameters);
        Assert.assertTrue(caseUseManager + " should have been triggered for command line '" + consoleParameters.printOriginalParameters() + "'", rightManager);
        List<CaseUseMode> caseUse = Main.getAllCaseUse();
        Assert.assertTrue(caseUseManager.getClass() + " not in Main.getAllCaseUse() method", caseUse.contains(caseUseManager));
        List<CaseUseMode> triggeredCaseUse = Main.getSuitableCaseUse(caseUse, consoleParameters);
        Assert.assertFalse("No case use activated for command line " + consoleParameters.printOriginalParameters(), triggeredCaseUse.isEmpty());
        Assert.assertTrue(
                "More than one case use manager activated for command line "
                + consoleParameters.printOriginalParameters() + "\n"
                + "Cases activated: " + triggeredCaseUse.toString(),
                triggeredCaseUse.size() == 1
        );

        Assert.assertEquals("Expected case use '" + caseUseManager + "', while the triggered was '" + triggeredCaseUse.get(0) + "'", caseUseManager, triggeredCaseUse.get(0));
    }

    public static void testCaseUseSubManager(CaseUseSubManager caseUseSubManager, ConsoleParameters consoleParameters) {

        boolean rightManager = caseUseSubManager.isRightManager(consoleParameters);
        Assert.assertTrue(caseUseSubManager + " should have been triggered for command line '" + consoleParameters.printOriginalParameters() + "'", rightManager);

        CaseUseSubManager triggeredCaseUse = caseUseSubManager.getParent().getSuitableCaseUseSubManager(consoleParameters);

        Assert.assertEquals("Expected case use '" + caseUseSubManager + "', while the triggered was '" + triggeredCaseUse + "'", caseUseSubManager, triggeredCaseUse);
    }

}
