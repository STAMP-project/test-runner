package eu.stamp.project.testrunner.runner.coverage;

import eu.stamp.project.testrunner.AbstractTest;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class JacocoRunnerTest extends AbstractTest {

    @Test
    public void testWithoutNewJvmOnTestClass() throws Exception {

        /*
            Using the api to compute the coverage on a test class
         */

        JacocoRunner.main(new String[]{
                        TEST_PROJECT_CLASSES, "example.TestSuiteExample"
                }
        );
        final Coverage load = Coverage.load();
        assertEquals(33, load.getInstructionsCovered());
        assertEquals(118, load.getInstructionsTotal());
    }

    @Test
    public void testWithoutNewJvmOnTestCases() throws Exception {

        /*
            Using the api to compute the coverage on test cases
         */

        JacocoRunner.main(new String[]{
                        TEST_PROJECT_CLASSES,
                        "example.TestSuiteExample",
                        "test8:test2"
                }
        );
        final Coverage load = Coverage.load();
        assertEquals(26, load.getInstructionsCovered());
        assertEquals(118, load.getInstructionsTotal());
    }

    @Test
    public void testExecutionOnTestClass() {

        /*
            Launch a new process to compute the coverage on the test class
         */

        Process p;
        try {
            p = Runtime.getRuntime().exec(commandLine);
            p.waitFor();
            assertEquals(0, p.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final Coverage load = Coverage.load();
        assertEquals(33, load.getInstructionsCovered());
        assertEquals(118, load.getInstructionsTotal());
    }

    @Test
    public void testExecutionTestCases() throws Exception {

         /*
            Launch a new process to compute the coverage on the test class
         */

        Process p;
        try {
            p = Runtime.getRuntime().exec(commandLine + " test8:test3");
            p.waitFor();
            assertEquals(0, p.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final Coverage load = Coverage.load();
        assertEquals(26, load.getInstructionsCovered());
        assertEquals(118, load.getInstructionsTotal());
    }

    private final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar:" +
            MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar:" +
            MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar:" +
            JUNIT_CP;

    private final String commandLine = "java -cp " +
            classpath + ":" + TEST_PROJECT_CLASSES + ":" + PATH_TO_RUNNER_CLASSES +
            " eu.stamp.project.testrunner.runner.coverage.JacocoRunner " +
            TEST_PROJECT_CLASSES +
            " example.TestSuiteExample";
}
