package eu.stamp_project.testrunner;

import eu.stamp_project.testrunner.runner.coverage.Coverage;
import eu.stamp_project.testrunner.runner.coverage.CoveragePerTestMethod;
import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.testrunner.runner.test.TestListener;
import eu.stamp_project.testrunner.runner.test.TestRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPointTest extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        EntryPoint.persistence = true;
        EntryPoint.outPrintStream = null;
        EntryPoint.errPrintStream = null;
    }

    @After
    public void tearDown() throws Exception {
        EntryPoint.blackList.clear();
    }

    @Test
    public void testWithBlackList() throws Exception {
        /*
            EntryPoint should not execute blacklisted test method
         */

        EntryPoint.blackList.add("testFailing");

        final TestListener testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "failing.FailingTestClass"
        );

        assertEquals(2, testListener.getRunningTests().size());
        assertEquals(1, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
        assertEquals(1, testListener.getAssumptionFailingTests().size());
        assertEquals(1, testListener.getIgnoredTests().size());
    }


    // TODO FIXME: This test seems to be flaky. Something happens with the stream of outputs
    @Test
    public void testJVMArgsAndCustomPrintStream() throws Exception {

        /*
            Test the method runTest() of EntryPoint using JVMArgs.
                This test verifies the non-persistence of the JVMArgs.
                This test verifies the usage of JVMArgs.
         */

        /* setup the out stream */
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        PrintStream outPrint = new PrintStream(outStream);
        EntryPoint.outPrintStream = outPrint;
        /* setup the err stream */
        ByteArrayOutputStream errStream = new ByteArrayOutputStream();
        PrintStream errPrint = new PrintStream(errStream);
        EntryPoint.errPrintStream = errPrint;

        /* persistence disabled, in order to clean after the execution */
        EntryPoint.persistence = false;

        EntryPoint.JVMArgs = "-XX:+PrintGCDetails";
        assertNotNull(EntryPoint.JVMArgs);

        final TestListener testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        final String GCdetail = outStream.toString();
        assertTrue(errStream.toString().isEmpty()); // no error occurs
//        assertTrue(GCdetail + " should contain GC detail, e.g. the word \"Heap\".", GCdetail.contains("Heap")); // it print the GC Details TODO FIXME

        assertEquals(6, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }

    @Test
    public void testPersistence() throws Exception {

        /*
            test the persistence boolean.
            First run, (default behavior), the persistence is enabled
            Second run, the persistence is set to false
         */

        EntryPoint.JVMArgs = "-XX:+PrintGCDetails";
        assertNotNull(EntryPoint.JVMArgs);

        TestListener testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertNotNull(EntryPoint.JVMArgs);
        assertEquals(6, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());

        EntryPoint.persistence = false;
        EntryPoint.JVMArgs = "-XX:+PrintGCDetails";
        assertNotNull(EntryPoint.JVMArgs);

        testListener = EntryPoint.runTestClasses(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertNull(EntryPoint.JVMArgs);
        assertEquals(6, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }

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
        EntryPoint.timeoutInMs = 1;
        try {
            EntryPoint.runTestClasses(
                    JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                    "example.TestSuiteExample", "example.TestSuiteExample2"
            );
            fail("Should have thrown a Time out Exception");
        } catch (Exception e) {
            assertTrue(true); // success!
        }
        EntryPoint.timeoutInMs = 10000;
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

    @Test //TODO FIXME FLAKY
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
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + TestRunner.PATH_SEPARATOR +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + TestRunner.PATH_SEPARATOR +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + TestRunner.PATH_SEPARATOR +
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
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + TestRunner.PATH_SEPARATOR +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + TestRunner.PATH_SEPARATOR +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + TestRunner.PATH_SEPARATOR +
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
    public void testRunGlobalCoverageWithBlackList() throws Exception {

        /*
            Test the runCoverage() of EntryPoint with blacklisted test methods.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + TestRunner.PATH_SEPARATOR+
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + TestRunner.PATH_SEPARATOR+
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + TestRunner.PATH_SEPARATOR+
                JUNIT_CP;

        EntryPoint.blackList.add("test8");
        EntryPoint.blackList.add("test6");
        EntryPoint.blackList.add("test3");
        EntryPoint.blackList.add("test4");
        EntryPoint.blackList.add("test7");
        EntryPoint.blackList.add("test9");

        final Coverage globalCoverage = EntryPoint.runCoverageOnTestClasses(
                classpath + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertEquals(26, globalCoverage.getInstructionsCovered());
        assertEquals(118, globalCoverage.getInstructionsTotal());
    }

    @Test
    public void testRunCoveragePerTestMethods() throws Exception {

        /*
            Test the runCoveragePerTestMethods() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + TestRunner.PATH_SEPARATOR+
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + TestRunner.PATH_SEPARATOR+
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + TestRunner.PATH_SEPARATOR+
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
