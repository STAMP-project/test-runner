package eu.stamp_project.testrunner.runner.test;

import eu.stamp_project.testrunner.AbstractTest;
import org.junit.Test;

import java.io.File;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class TestRunnerTest extends AbstractTest {

    @Test
    public void testExecutionTestClassWithBlackList() {

        /*
            Run the whole test class given by the command line.
                    - the listener is loaded using the static method load()
                    - test2 is in the blacklist and should be executed.
         */

        final String blacklistOption = " " + TestRunner.BLACK_LIST_OPTION + " test8:test2";

        Process p;
        try {
            System.out.println(commandLine + blacklistOption);
            p = Runtime.getRuntime().exec(commandLine + blacklistOption);
            p.waitFor();
            assertEquals(0, p.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final TestListener load = TestListener.load();
        assertEquals(4, load.getPassingTests().size());
        assertTrue(load.getFailingTests().isEmpty());
    }

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
            JUNIT_CP + TestRunner.PATH_SEPARATOR + TEST_PROJECT_CLASSES +
            TestRunner.PATH_SEPARATOR + PATH_TO_RUNNER_CLASSES +
            " eu.stamp_project.testrunner.runner.test.TestRunner example.TestSuiteExample";
}
