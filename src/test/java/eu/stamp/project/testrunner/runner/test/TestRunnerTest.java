package eu.stamp.project.testrunner.runner.test;

import eu.stamp.project.testrunner.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class TestRunnerTest extends AbstractTest {

    @Test
    public void testExecutionTestClass() {

        /*
            Run the whole test class given by the command line.
                    - the listener is loaded using the static method load()
         */

        Process p;
        try {
            p = Runtime.getRuntime().exec(commandLine);
            p.waitFor();
            assertEquals(0, p.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final TestListener load = TestListener.load();
        assertEquals(6, load.getPassingTests().size());
        assertTrue(load.getFailingTests().isEmpty());
    }

    @Test
    public void testExecutionTestCases() {

        /*
            Run the test cases of the test class given by the command line.
                    - the listener is loaded using the static method load()
         */

        Process p;
        try {
            p = Runtime.getRuntime().exec(commandLine + " test8:test2");
            p.waitFor();
            assertEquals(0, p.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final TestListener load = TestListener.load();
        assertEquals(2, load.getPassingTests().size());
        assertTrue(load.getFailingTests().isEmpty());
    }


    private final String commandLine = "java -cp " +
            JUNIT_CP + ":" + TEST_PROJECT_CLASSES + ":" + PATH_TO_RUNNER_CLASSES +
            " eu.stamp.project.testrunner.runner.test.TestRunner example.TestSuiteExample";
}
