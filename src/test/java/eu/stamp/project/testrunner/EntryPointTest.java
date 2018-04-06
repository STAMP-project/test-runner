package eu.stamp.project.testrunner;

import eu.stamp.project.testrunner.runner.coverage.Coverage;
import eu.stamp.project.testrunner.runner.coverage.CoveragePerTestMethod;
import eu.stamp.project.testrunner.runner.test.Failure;
import eu.stamp.project.testrunner.runner.test.TestListener;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPointTest extends AbstractTest {

    @Test
    public void testOnEasyMockTests() throws Exception {

        /*
            Test to run test class that use easymock framework
         */

        final TestListener testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + EASYMOCK_CP +
                        EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "easymock.LoginControllerIntegrationTest"
        );
        assertEquals(7, testListener.getRunningTests().size());
        assertEquals(7, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }

    @Test
    public void testOnFailingTest() throws Exception {
        /*
            EntryPoint should return a proper TestListener.
            This TestListener contains:
                - three running test
                - one failing test
                - one passing test
                - one assumption failing test
                - one ignored test
         */

        final TestListener testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "failing.FailingTestClass"
        );

        assertEquals(3, testListener.getRunningTests().size());
        assertEquals(1, testListener.getPassingTests().size());
        assertEquals(1, testListener.getFailingTests().size());
        assertEquals(1, testListener.getAssumptionFailingTests().size());
        assertEquals(1, testListener.getIgnoredTests().size());

        final Failure testFailing = testListener.getFailureOf("testFailing");
        assertEquals("testFailing", testFailing.testCaseName);

        try {
            testListener.getFailureOf("testPassing");
            fail("Should have throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // expected
        }
    }

    @Test
    public void testTimeOut() {
        EntryPoint.defaultTimeoutInMs = 1;
        try {
            EntryPoint.runTestClasses(
                    JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                    "example.TestSuiteExample", "example.TestSuiteExample2"
            );
            fail("Should have thrown a Time out Exception");
        } catch (Exception e) {
            assertTrue(true); // success!
        }
        EntryPoint.defaultTimeoutInMs = 10000;
    }

    @Test
    public void testRunTestClasses() throws Exception {

        /*
            Test the method runTestClasses() of EntryPoint.
                It should return the TestListener with the result of the execution of the list of test classes.
         */

        final TestListener testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample", "example.TestSuiteExample2"
        );
        assertEquals(12, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }

    @Test
    public void testRunTestTestClass() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the TestListener with the result of the execution of the test class.
         */

        final TestListener testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );
        assertEquals(6, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }

    @Test //FLAKY
    public void testRunTestTestMethods() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the TestListener with the result of the execution of the test class.
         */

        EntryPoint.verbose = true;

        final TestListener testListener = EntryPoint.runTests(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                "test4", "test9"
        );
        assertEquals(2, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());

        EntryPoint.verbose = false;
    }

    @Test
    public void testRunCoverage() throws Exception {

        /*
            Test the runCoverage() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar:" +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar:" +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar:" +
                JUNIT_CP;

        final Coverage coverage = EntryPoint.runCoverageOnTests(
                classpath + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                "test8", "test3"
        );
        assertEquals(26, coverage.getInstructionsCovered());
        assertEquals(118, coverage.getInstructionsTotal());
    }

    @Test
    public void testRunGlobalCoverage() throws Exception {

        /*
            Test the runCoverage() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar:" +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar:" +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar:" +
                JUNIT_CP;

        final Coverage globalCoverage = EntryPoint.runCoverageOnTestClasses(
                classpath + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertEquals(33, globalCoverage.getInstructionsCovered());
        assertEquals(118, globalCoverage.getInstructionsTotal());
    }

    @Test
    public void testRunCoveragePerTestMethods() throws Exception {

        /*
            Test the runCoveragePerTestMethods() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar:" +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar:" +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar:" +
                JUNIT_CP;

        final CoveragePerTestMethod coveragePerTestMethod = EntryPoint.runCoveragePerTestMethods(
                classpath + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                "test8", "test3"
        );

        assertEquals(26, coveragePerTestMethod.getCoverageOf("test3").getInstructionsCovered());
        assertEquals(118, coveragePerTestMethod.getCoverageOf("test3").getInstructionsTotal());
        assertEquals(23, coveragePerTestMethod.getCoverageOf("test8").getInstructionsCovered());
        assertEquals(118, coveragePerTestMethod.getCoverageOf("test8").getInstructionsTotal());
    }
}
