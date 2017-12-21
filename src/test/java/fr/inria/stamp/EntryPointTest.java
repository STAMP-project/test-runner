package fr.inria.stamp;

import org.junit.Test;
import fr.inria.stamp.runner.coverage.CoverageListener;
import fr.inria.stamp.runner.test.TestListener;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPointTest extends AbstractTest {

    @Test
    public void testRunTestTestClass() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the TestListener with the result of the execution of the test class.
         */

        final TestListener testListener = EntryPoint.runTest(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                Collections.emptyList()
        );
        assertEquals(6, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }

    @Test
    public void testRunTestTestMethods() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the TestListener with the result of the execution of the test class.
         */

        final TestListener testListener = EntryPoint.runTest(
                JUNIT_CP + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                "example.TestSuiteExample",
                Arrays.asList("test4", "test9")
        );
        assertEquals(2, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
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

        final CoverageListener coverageListener = EntryPoint.runCoverage(
                classpath + EntryPoint.PATH_SEPARATOR + TEST_PROJECT_CLASSES,
                TEST_PROJECT_CLASSES,
                "example.TestSuiteExample"
        );
        assertEquals(141, coverageListener.getInstructionsCoveragePerLinePerTestCasesName().keySet()
                .stream()
                .flatMap(methodName -> coverageListener.getInstructionsCoveragePerLinePerTestCasesName().get(methodName).stream())
                .map(coverage -> coverage.instructionCovered)
                .reduce(0, (integer, instructionCovered) -> integer + instructionCovered).intValue());
        assertEquals(204, coverageListener.getInstructionsCoveragePerLinePerTestCasesName().keySet()
                .stream()
                .flatMap(methodName -> coverageListener.getInstructionsCoveragePerLinePerTestCasesName().get(methodName).stream())
                .map(coverage -> coverage.instructionTotal)
                .reduce(0, (integer, instructionTotal) -> integer + instructionTotal).intValue());
    }
}
