package delfos;

import java.util.Arrays;
import java.util.TreeSet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;

/**
 * Tests the command line parameters parser, to ensure the expected behaviour of
 * the parser regarding the parameters and flags present in the command line.
 *
 * @author jcastro
 */
public class ConsoleParametersTest {

    /**
     * Tests the parser with a flag parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testOneFlagWithNoValues() throws Exception {
        String[] args = {
            "--flag"
        };

        try {
            ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(args);

            assertTrue("The parameter '--flag' has not been recognised by the parser.", consoleParameters.isFlagDefined("--flag"));
            assertTrue("The parameter '--flag' was used but the parser did not mark it as used.", consoleParameters.getAllUnusedParameters().isEmpty());
        } catch (CommandLineParametersError ex) {
            fail("The command line is correct but an exception was thrown "
                    + "at testing the command line with '"
                    + Arrays.asList(args) + "' parameters."
            );
        }
    }

    /**
     * Tests the parser with a flag parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testParameterWithOneValue() throws Exception {
        String[] args = {
            "-parameter", "value1"
        };

        try {
            ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(args);
            assertTrue("The parameter '-parameter' is present but the parser do not recognise it.", consoleParameters.isParameterDefined("-parameter"));
            assertTrue("The parameter '-parameter' does not have 'value1' as value", consoleParameters.getValue("-parameter").equals("value1"));
            assertTrue("The parameter '-parameter' was used but the parser did not mark it as used.", consoleParameters.getAllUnusedParameters().isEmpty());
        } catch (CommandLineParametersError ex) {
            fail("The command line is correct but an exception was thrown "
                    + "at testing the command line with '"
                    + Arrays.asList(args) + "' parameters."
            );
        }
    }

    /**
     * Tests the parser with a flag parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testOneFlagWithValues() throws Exception {
        String[] args = {
            "--flag", "value1"
        };

        try {
            ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(args);

            fail("The command line parser should fail, given that a flag cannot take parameters.");
        } catch (CommandLineParametersError ex) {
            assertTrue("The exception generated does not belong to '--flag' parameter.", ex.getParameter().equals("--flag"));
        }
    }

    /**
     * Tests the parser with a flag parameter.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testParameterWithNoValue() throws Exception {
        String[] args = {
            "-parameter"
        };

        try {
            ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(args);
            fail("The command line parser should fail, given that a parameter must have at least one value.");
        } catch (CommandLineParametersError ex) {

            assertTrue("The exception generated does not belong to '-parameter' parameter.", ex.getParameter().equals("-parameter"));
        }
    }

    @Test
    public void testComplexCommandLine() throws CommandLineParametersError {
        String[] args = {
            "--flag", "-parameter1", "value1", "-parameter2", "value2", "--v"

        };
        ConsoleParameters consoleParameters = ConsoleParameters.parseArguments(args);
        assertThat("", consoleParameters.isFlagDefined("--flag"));

        assertEquals("", new TreeSet<>(Arrays.asList("--v", "-parameter1", "-parameter2")), consoleParameters.getAllUnusedParameters());

        assertThat("", consoleParameters.isFlagDefined("--v"));

        assertThat("", consoleParameters.isParameterDefined("-parameter1"));
        assertThat("", consoleParameters.getValue("-parameter1").equals("value1"));
        assertThat("", consoleParameters.getValues("-parameter1").size() == 1);

        assertThat("", consoleParameters.isParameterDefined("-parameter2"));
        assertThat("", consoleParameters.getValue("-parameter2").equals("value2"));
        assertThat("", consoleParameters.getValues("-parameter2").size() == 1);

        assertThat("", consoleParameters.getAllUnusedParameters().isEmpty());
    }

}
