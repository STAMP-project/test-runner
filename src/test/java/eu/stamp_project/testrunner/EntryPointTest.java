package eu.stamp_project.testrunner;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.listener.impl.CoverageDetailed;
import eu.stamp_project.testrunner.listener.pit.AbstractPitResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.junit.*;

import java.io.*;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPointTest extends AbstractTest {

    // depends on the compiler and compiler version that is used on the test project
    public static final int NUMBER_OF_INSTRUCTIONS = 103;

    @Before
    public void setUp() {
        EntryPoint.persistence = true;
        EntryPoint.outPrintStream = null;
        EntryPoint.errPrintStream = null;
        EntryPoint.verbose = true;
        EntryPoint.setMutationEngine(ConstantsHelper.MutationEngine.DESCARTES);
        EntryPoint.coverageDetail = ParserOptions.CoverageTransformerDetail.SUMMARIZED;
        EntryPoint.useOptionsFile = false;
    }

    @After
    public void tearDown() {
        EntryPoint.blackList.clear();
    }

    @Test
    public void testUseOptionsFile() throws TimeoutException {
        /*
            Test option useOptionsFile of Entrypoint.
                It should output a file containing the command line options to be parsed by the runner.
                This file can be found at the path designed by EntryPoint.ABSOLUTE_PATH_TO_OPTIONS_FILE
         */

        assertTrue(new File(EntryPoint.ABSOLUTE_PATH_TO_OPTIONS_FILE).exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(EntryPoint.ABSOLUTE_PATH_TO_OPTIONS_FILE))) {
            assertNull("the options file should be empty when created", reader.readLine());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        EntryPoint.useOptionsFile = true;
        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"}
        );

        assertEquals(13, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
        assertTrue(new File(EntryPoint.ABSOLUTE_PATH_TO_OPTIONS_FILE).exists());
        try (BufferedReader reader = new BufferedReader(new FileReader(EntryPoint.ABSOLUTE_PATH_TO_OPTIONS_FILE))) {
            assertEquals(
                    "--class example.TestSuiteExample:example.TestSuiteExample2   --nb-failing-load-class 0",
                    reader.readLine()
            );
            assertNull("the options file should have only one line", reader.readLine());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /*
        TODO : Both test using pit seems to not pass using maven
     */

    @Test
    @Ignore
    public void testPitRun() throws TimeoutException {

        /*
            Run pit with default option on test projects.
            Default options are using descartes mutation engine and default mutators of descartes.
            This result with a list of pit result.
            NOTE: it seems that running pit in debug mode (in IDEA) does not work, be careful.
         */

        final List<? extends AbstractPitResult> pitResults = EntryPoint.runPit(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "src/test/resources/test-projects/",
                "example.*",
                "example.TestSuiteExample"
        );
        assertEquals(2, pitResults.size());
        assertEquals(2, pitResults.stream().filter(result -> result.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
    }

    @Test
    @Ignore
    public void testPitRunWithGregor() {

        /*
            Run pit with gregor mutation engine and ALL mutators.
            We observe much more mutants
         */

        EntryPoint.setMutationEngine(ConstantsHelper.MutationEngine.GREGOR);

        final List<? extends AbstractPitResult> pitResults = EntryPoint.runPit(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "src/test/resources/test-projects/",
                "example.*",
                "example.TestSuiteExample(.)*"
        );
        assertEquals(95, pitResults.size());
        assertEquals(45, pitResults.stream().filter(result -> result.getStateOfMutant() == AbstractPitResult.State.KILLED).count());
    }

    @Test
    public void testWithBlackList() throws Exception {
        /*
            EntryPoint should not execute blacklisted test method
         */

        EntryPoint.blackList.add("testFailing");

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "failing.FailingTestClass"
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
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        final String GCdetail = outStream.toString();
        assertTrue(errStream.toString().isEmpty()); // no error occurs
//        assertTrue(GCdetail + " should contain GC detail, e.g. the word \"Heap\".", GCdetail.contains("Heap")); // it print the GC Details TODO FIXME

        assertEquals(7, testResult.getPassingTests().size());
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
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertNotNull(EntryPoint.JVMArgs);
        assertEquals(7, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());

        EntryPoint.persistence = false;
        EntryPoint.JVMArgs = "-XX:+PrintGCDetails";
        assertNotNull(EntryPoint.JVMArgs);

        testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertNull(EntryPoint.JVMArgs);
        assertEquals(7, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Test
    public void testOnEasyMockTests() throws Exception {
        Assume.assumeTrue(getJavaVersion() <= 11);

        /*
            Test to run test class that use easymock framework
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + EASYMOCK_CP +
                        ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "easymock.LoginControllerIntegrationTest"
        );
        assertEquals(7, testResult.getRunningTests().size());
        assertEquals(7, testResult.getPassingTests().size());
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
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "failing.FailingTestClass"
        );

        assertEquals(3, testResult.getRunningTests().size());
        assertEquals(1, testResult.getPassingTests().size());
        assertEquals(1, testResult.getFailingTests().size());
        assertEquals(1, testResult.getAssumptionFailingTests().size());
        assertEquals(1, testResult.getIgnoredTests().size());

        final Failure testFailing = testResult.getFailureOf("failing.FailingTestClass#testFailing");
        assertEquals("failing.FailingTestClass#testFailing", testFailing.testCaseName);

        try {
            testResult.getFailureOf("failing.FailingTestClass#testPassing");
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
                    JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                            SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                    new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"}
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
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"}
        );
        assertEquals(13, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Test
    public void testRunTestTestClass() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the testResult with the result of the execution of the test class.
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );
        assertEquals(7, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Test
    public void testRunTestTestClassSameNamedMethods() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the testResult with the result of the execution of the test classes.
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"},
                new String[]{"example.TestSuiteExample#test3", "example.TestSuiteExample2#test3"}
        );
        assertEquals(2, testResult.getPassingTests().size());
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
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
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
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final Coverage coverage = EntryPoint.runCoverage(
                classpath + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                new String[]{"test8", "test3"}
        );
        assertEquals(23, coverage.getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coverage.getInstructionsTotal());
    }

    @Test
    public void testRunCoverageSameNamedMethods() throws Exception {

        /*
            Test the runCoverage() of EntryPoint.
                It should return the CoverageResult with the instruction jUnit4Coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final Coverage coverage = EntryPoint.runCoverage(
                classpath + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"},
                new String[]{"example.TestSuiteExample#test3", "example.TestSuiteExample2#test3"}
        );
        assertEquals(30, coverage.getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coverage.getInstructionsTotal());
    }

    @Test
    public void testRunGlobalCoverage() throws Exception {

        /*
            Test the runCoverage() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final Coverage coverage = EntryPoint.runCoverage(
                classpath + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertEquals(30, coverage.getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coverage.getInstructionsTotal());
    }

    @Test
    public void testRunGlobalCoverageWithBlackList() throws Exception {

        /*
            Test the runCoverage() of EntryPoint with blacklisted test methods.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        EntryPoint.blackList.add("test8");
        EntryPoint.blackList.add("test6");
        EntryPoint.blackList.add("test3");
        EntryPoint.blackList.add("test4");
        EntryPoint.blackList.add("test7");
        EntryPoint.blackList.add("test9");

        final Coverage coverage = EntryPoint.runCoverage(
                classpath + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );

        assertEquals(23, coverage.getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coverage.getInstructionsTotal());
    }

    @Test
    public void testRunCoveragePerTestMethods() throws Exception {

        /*
            Test the runCoveragePerTestMethods() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveragePerTestMethod coveragePerTestMethod = EntryPoint.runCoveragePerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                new String[]{"test8", "test3"}
        );

        assertEquals(23, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsTotal());
        assertEquals(23, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample#test8").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample#test8").getInstructionsTotal());
    }

    @Test
    public void testRunCoveragePerTestMethodsSameNamedMethods() throws Exception {

        /*
            Test the runCoveragePerTestMethods() of EntryPoint.
                It should return the CoverageResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveragePerTestMethod coveragePerTestMethod = EntryPoint.runCoveragePerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"},
                new String[]{"example.TestSuiteExample#test3", "example.TestSuiteExample2#test3"}
        );

        System.out.println(coveragePerTestMethod);
        assertEquals(23, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsTotal());
        assertEquals(23, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample2#test3").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveragePerTestMethod.getCoverageOf("example.TestSuiteExample2#test3").getInstructionsTotal());
    }

    @Test
    public void testOnParametrized() throws TimeoutException {

        /*
            Test the execution of Parametrized test class
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.ParametrizedTestSuiteExample"
        );
        System.out.println(testResult);
        assertEquals(10, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Test
    public void testOnParametrizedForOneTestMethod() throws TimeoutException {

        /*
            Test the execution of Parametrized test
         */

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.ParametrizedTestSuiteExample",
                "test3"
        );
        assertEquals(2, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    @Test
    public void testOnParametrizedForOneTestMethodCoveragePerTestMethod() throws TimeoutException {

        /*
            Test the execution of Parametrized test
         */

        EntryPoint.runCoveragePerTestMethods(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR +
                        SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES
                        + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.ParametrizedTestSuiteExample",
                "test3:test4:test7"
        );
    }

    @Test
    public void testRunCoveredTestResultPerTestMethods() throws Exception {

        /*
            Test the runCoveredTestResultPerTestMethods() of EntryPoint.
                It should return the CoveredTestResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runCoveredTestResultPerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                new String[]{"test8", "test3"}
        );

        // Assert test results
        assertEquals(2, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(2, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert coverage
        assertEquals(2, coveredTestResultPerTestMethod.getCoverageResultsMap().size());
        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsTotal());
        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8").getInstructionsTotal());
    }

    @Test
    public void testRunOnlineCoveredTestResultPerTestMethods() throws Exception {
        EntryPoint.jacocoAgentIncludes = "example.*:tobemocked.*";
        EntryPoint.jacocoAgentExcludes = "example.TestSuiteExample";

        /*
            Test the runOnlineCoveredTestResultPerTestMethods() of EntryPoint.
                It should return the CoveredTestResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runOnlineCoveredTestResultPerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                new String[]{"test8", "test3"}
        );

        // Assert test results
        assertEquals(2, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(2, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert coverage
        assertEquals(2, coveredTestResultPerTestMethod.getCoverageResultsMap().size());
        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3").getInstructionsTotal());
        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8").getInstructionsTotal());
    }

    @Test
    public void testRunOnlineCoveredTestResultPerTestMethodsDetailedCompressedCoverage() throws Exception {
        EntryPoint.coverageDetail = ParserOptions.CoverageTransformerDetail.DETAIL_COMPRESSED;
        EntryPoint.jacocoAgentIncludes = "example.*:tobemocked.*";
        EntryPoint.jacocoAgentExcludes = "example.TestSuiteExample";

        /*
            Test the runOnlineCoveredTestResultPerTestMethods() of EntryPoint.
                It should return the CoveredTestResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runOnlineCoveredTestResultPerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                new String[]{"test8", "test3"}
        );

        // Assert test results
        assertEquals(2, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(2, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert detailed coverage
        assertEquals(2, coveredTestResultPerTestMethod.getCoverageResultsMap().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3") instanceof CoverageDetailed);
        CoverageDetailed coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(1, coverageDetailed.getDetailedCoverage().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8") instanceof CoverageDetailed);
        coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(1, coverageDetailed.getDetailedCoverage().size());
    }

    @Ignore
    @Test
    public void testOnParametrizedForOneTestMethodCoveredTestResultPerTestMethod() throws TimeoutException {

        /*
            Test the execution of Parametrized test
         */

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runCoveredTestResultPerTestMethods(
                JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES
                        + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.ParametrizedTestSuiteExample",
                "test3:test4:test7"
        );

        // Assert test results
        // FIXME: Assertion failing because there the keys for the original test method names are missing
        assertEquals(9, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(9, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert coverage
        assertEquals(9, coveredTestResultPerTestMethod.getCoverageResultsMap().size());
        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("test3[0]").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("test3[0]").getInstructionsTotal());
        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("test3[1]").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("test3[1]").getInstructionsTotal());

        assertEquals(26, coveredTestResultPerTestMethod.getCoverageOf("test4[0]").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("test4[0]").getInstructionsTotal());
        assertEquals(26, coveredTestResultPerTestMethod.getCoverageOf("test4[1]").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("test4[1]").getInstructionsTotal());


        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("test7[0]").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("test7[0]").getInstructionsTotal());
        assertEquals(23, coveredTestResultPerTestMethod.getCoverageOf("test7[1]").getInstructionsCovered());
        assertEquals(NUMBER_OF_INSTRUCTIONS, coveredTestResultPerTestMethod.getCoverageOf("test7[1]").getInstructionsTotal());
    }

    // Tests when running same named test methods from different classes
    @Test
    public void testRunCoveredTestResultPerTestMethodsSameNamedMethodsDetailedCoverage() throws Exception {
        EntryPoint.coverageDetail = ParserOptions.CoverageTransformerDetail.DETAIL;

        /*
            Test the runCoveredTestResultPerTestMethods() of EntryPoint.
                It should return the CoveredTestResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runCoveredTestResultPerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"},
                new String[]{"example.TestSuiteExample#test3", "example.TestSuiteExample2#test3"}
        );

        // Assert test results
        assertEquals(2, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(2, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert detailed coverage
        assertEquals(2, coveredTestResultPerTestMethod.getCoverageResultsMap().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3") instanceof CoverageDetailed);
        CoverageDetailed coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(5, coverageDetailed.getDetailedCoverage().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample2#test3") instanceof CoverageDetailed);
        coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample2#test3");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(5, coverageDetailed.getDetailedCoverage().size());
    }

    @Test
    public void testRunCoveredTestResultPerTestMethodsDetailedCoverage() throws Exception {
        EntryPoint.coverageDetail = ParserOptions.CoverageTransformerDetail.DETAIL;

        /*
            Test the runCoveredTestResultPerTestMethods() of EntryPoint.
                It should return the CoveredTestResultPerTestMethod with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runCoveredTestResultPerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                new String[]{"test8", "test3"}
        );

        // Assert test results
        assertEquals(2, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(2, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert detailed coverage
        assertEquals(2, coveredTestResultPerTestMethod.getCoverageResultsMap().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3") instanceof CoverageDetailed);
        CoverageDetailed coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(5, coverageDetailed.getDetailedCoverage().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8") instanceof CoverageDetailed);
        coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(5, coverageDetailed.getDetailedCoverage().size());
    }

    // Tests when running same named test methods from different classes
    @Test
    public void testRunCoveredTestResultPerTestMethodsSameNamedMethodsDetailedCompressedCoverage() throws Exception {
        EntryPoint.coverageDetail = ParserOptions.CoverageTransformerDetail.DETAIL_COMPRESSED;

        /*
            Test the runCoveredTestResultPerTestMethods() of EntryPoint.
                It should return the CoveredTestResult with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runCoveredTestResultPerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                new String[]{"example.TestSuiteExample", "example.TestSuiteExample2"},
                new String[]{"example.TestSuiteExample#test3", "example.TestSuiteExample2#test3"}
        );

        // Assert test results
        assertEquals(2, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(2, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert detailed coverage
        assertEquals(2, coveredTestResultPerTestMethod.getCoverageResultsMap().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3") instanceof CoverageDetailed);
        CoverageDetailed coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(1, coverageDetailed.getDetailedCoverage().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample2#test3") instanceof CoverageDetailed);
        coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample2#test3");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(1, coverageDetailed.getDetailedCoverage().size());
    }

    @Test
    public void testRunCoveredTestResultPerTestMethodsDetailedCompressedCoverage() throws Exception {
        EntryPoint.coverageDetail = ParserOptions.CoverageTransformerDetail.DETAIL_COMPRESSED;

        /*
            Test the runCoveredTestResultPerTestMethods() of EntryPoint.
                It should return the CoveredTestResultPerTestMethod with the instruction coverage computed by Jacoco.
         */
        final String classpath = JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;

        final CoveredTestResultPerTestMethod coveredTestResultPerTestMethod = EntryPoint.runCoveredTestResultPerTestMethods(
                classpath + ConstantsHelper.PATH_SEPARATOR + SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                SOURCE_PROJECT_CLASSES + ConstantsHelper.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                new String[]{"test8", "test3"}
        );

        // Assert test results
        assertEquals(2, coveredTestResultPerTestMethod.getRunningTests().size());
        assertEquals(2, coveredTestResultPerTestMethod.getPassingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getFailingTests().size());
        assertEquals(0, coveredTestResultPerTestMethod.getIgnoredTests().size());

        // Assert detailed coverage
        assertEquals(2, coveredTestResultPerTestMethod.getCoverageResultsMap().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3") instanceof CoverageDetailed);
        CoverageDetailed coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test3");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(1, coverageDetailed.getDetailedCoverage().size());

        assertTrue(coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8") instanceof CoverageDetailed);
        coverageDetailed = (CoverageDetailed) coveredTestResultPerTestMethod.getCoverageOf("example.TestSuiteExample#test8");
        assertNotNull(coverageDetailed.getDetailedCoverage());
        assertEquals(1, coverageDetailed.getDetailedCoverage().size());
    }

}
