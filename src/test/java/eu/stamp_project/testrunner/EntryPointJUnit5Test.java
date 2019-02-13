package eu.stamp_project.testrunner;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.*;
import static org.junit.Assert.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/11/18
 */
public class EntryPointJUnit5Test extends AbstractTest {

    @Before
    public void setUp() throws Exception {
        EntryPoint.persistence = true;
        EntryPoint.outPrintStream = null;
        EntryPoint.errPrintStream = null;
        EntryPoint.jUnit5Mode = true;
    }

    @After
    public void tearDown() throws Exception {
        EntryPoint.blackList.clear();
        EntryPoint.jUnit5Mode = false;
    }

    @Ignore
    @Test
    public void testWithBlackList() throws Exception {
        /*
            EntryPoint should not execute blacklisted test method
         */

        EntryPoint.blackList.add("testFailing");

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"failing.FailingTestClass"},
                new String[0]
        );

        assertEquals(2, testResult.getRunningTests().size());
        assertEquals(1, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
        assertEquals(1, testResult.getAssumptionFailingTests().size());
        assertEquals(1, testResult.getIgnoredTests().size());
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

        final TestResult testResult = EntryPoint.runTests(
                JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                        JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"junit5.TestSuiteExample"},
                new String[0]
        );

        final String GCdetail = outStream.toString();
        assertTrue(errStream.toString().isEmpty()); // no error occurs
//        assertTrue(GCdetail + " should contain GC detail, e.g. the word \"Heap\".", GCdetail.contains("Heap")); // it print the GC Details TODO FIXME

        assertEquals(6, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
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

        TestResult testResult = EntryPoint.runTests(
                JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                        JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample"
        );

        assertNotNull(EntryPoint.JVMArgs);
        assertEquals(6, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());

        EntryPoint.persistence = false;
        EntryPoint.JVMArgs = "-XX:+PrintGCDetails";
        assertNotNull(EntryPoint.JVMArgs);

        testResult = EntryPoint.runTests(
                JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                        JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample"
        );

        assertNull(EntryPoint.JVMArgs);
        assertEquals(6, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Test
    public void testOnFailingTest() throws Exception {
        /*
            EntryPoint should return a proper testResult.
            This testResult contains:
                - three running test
                - one failing test
                - one passing test
                - one assumption failing test
                - one ignored test
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                        JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "junit5.FailingTestClass"
        );

        assertEquals(2, testResult.getRunningTests().size());
        assertEquals(1, testResult.getPassingTests().size());
        assertEquals(1, testResult.getFailingTests().size());
        //assertEquals(1, testResult.getAssumptionFailingTests().size()); TODO
        assertEquals(1, testResult.getIgnoredTests().size());

        final Failure testFailing = testResult.getFailureOf("testFailing");
        assertEquals("testFailing", testFailing.testCaseName);

        try {
            testResult.getFailureOf("testPassing");
            fail("Should have throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // expected
        }
    }

    @Test
    public void testTimeOut() {
        EntryPoint.timeoutInMs = 1;
        try {
            EntryPoint.runTests(
                    JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                            JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                    new String[]{"junit5.TestSuiteExample", "junit5.TestSuiteExample2"}
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
                It should return the testResult with the result of the execution of the list of test classes.
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                        JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"junit5.TestSuiteExample", "junit5.TestSuiteExample2"}
        );
        assertEquals(12, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Test
    public void testRunTestTestClass() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the testResult with the result of the execution of the test class.
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                        JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample"
        );
        assertEquals(6, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Ignore
    @Test //TODO FIXME FLAKY
    public void testRunTestTestMethods() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the testResult with the result of the execution of the test class.
         */

        EntryPoint.verbose = true;

        final TestResult testResult = EntryPoint.runTests(
                JUNIT5_CP + ConstantsHelper.PATH_SEPARATOR +
                        JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample",
                new String[]{"test4", "test9"}
        );
        assertEquals(2, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());

        EntryPoint.verbose = false;
    }

    @Test
    public void testRunCoverage() throws Exception {

        /*
            Test the runCoverage() of EntryPoint.
                It should return the CoverageResult with the instruction jUnit4Coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + ConstantsHelper.PATH_SEPARATOR +
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                JUNIT5_CP;

        final Coverage coverage = EntryPoint.runCoverage(
                classpath + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample",
                new String[]{"test8", "test3"}
        );
        assertEquals(23, coverage.getInstructionsCovered());
        assertEquals(107, coverage.getInstructionsTotal());
    }

    @Test
    public void testRunGlobalCoverage() throws Exception {

        /*
            Test the runCoverage() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + ConstantsHelper.PATH_SEPARATOR +
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                JUNIT5_CP;

        final Coverage coverage = EntryPoint.runCoverage(
                classpath + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample"
        );

        assertEquals(30, coverage.getInstructionsCovered());
        assertEquals(107, coverage.getInstructionsTotal());
    }

    @Ignore
    @Test
    public void testRunGlobalCoverageWithBlackList() throws Exception {

        /*
            Test the runCoverage() of EntryPoint with blacklisted test methods.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + ConstantsHelper.PATH_SEPARATOR +
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                JUNIT5_CP;

        EntryPoint.blackList.add("test8");
        EntryPoint.blackList.add("test6");
        EntryPoint.blackList.add("test3");
        EntryPoint.blackList.add("test4");
        EntryPoint.blackList.add("test7");
        EntryPoint.blackList.add("test9");

        final Coverage coverage = EntryPoint.runCoverage(
                classpath + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample"
        );

        assertEquals(26, coverage.getInstructionsCovered());
        assertEquals(118, coverage.getInstructionsTotal());
    }

    @Test
    public void testRunCoveragePerTestMethods() throws Exception {

        /*
            Test the runCoveragePerTestMethods() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + ConstantsHelper.PATH_SEPARATOR +
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveragePerTestMethod coveragePerTestMethod = EntryPoint.runCoveragePerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "junit5.TestSuiteExample",
                new String[]{"test8", "test3"}
        );

        assertEquals(23, coveragePerTestMethod.getCoverageOf("test3").getInstructionsCovered()); // TODO something may be wrong here. The instruction coverage of test 3 is 26
        assertEquals(107, coveragePerTestMethod.getCoverageOf("test3").getInstructionsTotal());
        assertEquals(23, coveragePerTestMethod.getCoverageOf("test8").getInstructionsCovered());// TODO something may be wrong here. The instruction coverage of test 8 is 23
        assertEquals(107, coveragePerTestMethod.getCoverageOf("test8").getInstructionsTotal());
    }

    @Ignore
    @Test
    public void testRunCoveragePerTestMethodsOnParametrizedTest() throws Exception {

        /*
            Test the runCoveragePerTestMethods() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar" + ConstantsHelper.PATH_SEPARATOR +
                MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar" + ConstantsHelper.PATH_SEPARATOR +
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveragePerTestMethod coveragePerTestMethod = EntryPoint.runCoveragePerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "junit5.ParametrizedTest",
                new String[]{"test"}
        );

        System.out.println(coveragePerTestMethod);

        assertEquals(23, coveragePerTestMethod.getCoverageOf("test3").getInstructionsCovered()); // TODO something may be wrong here. The instruction coverage of test 3 is 26
        assertEquals(107, coveragePerTestMethod.getCoverageOf("test3").getInstructionsTotal());
        assertEquals(23, coveragePerTestMethod.getCoverageOf("test8").getInstructionsCovered());// TODO something may be wrong here. The instruction coverage of test 8 is 23
        assertEquals(107, coveragePerTestMethod.getCoverageOf("test8").getInstructionsTotal());
    }
}
