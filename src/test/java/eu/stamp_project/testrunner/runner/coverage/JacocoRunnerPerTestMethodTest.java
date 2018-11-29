package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.AbstractTest;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import eu.stamp_project.testrunner.listener.impl.CoveragePerTestMethodImpl;
import eu.stamp_project.testrunner.runner.ParserOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class JacocoRunnerPerTestMethodTest extends AbstractTest {

    @Test
    public void testWithoutNewJvmOnTestClass() throws Exception {

        /*
            Using the api to compute the coverage on a test class
         */

        JacocoRunnerPerTestMethod.main(new String[]{
                        ParserOptions.FLAG_pathToCompiledClassesOfTheProject, TEST_PROJECT_CLASSES,
                        ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, "example.ParametrizedTestSuiteExample",
                        ParserOptions.FLAG_testMethodNamesToRun, "test8:test2"
                }
        );
        final CoveragePerTestMethodImpl load = CoveragePerTestMethodImpl.load();
        System.out.println(load);
        //assertEquals(26, load.getCoverageResultsMap().get("test2").getInstructionsCovered());
        assertEquals(26, load.getCoverageResultsMap().get("test2[0]").getInstructionsCovered());
        assertEquals(26, load.getCoverageResultsMap().get("test2[1]").getInstructionsCovered());
        //assertEquals(23, load.getCoverageResultsMap().get("test8").getInstructionsCovered());
        assertEquals(23, load.getCoverageResultsMap().get("test8[0]").getInstructionsCovered());
        assertEquals(23, load.getCoverageResultsMap().get("test8[0]").getInstructionsCovered());
    }

    @Test
    public void testWithoutNewJvmOnTestCases() throws Exception {

        /*
            Using the api to compute the coverage on test cases
         */

        JacocoRunnerPerTestMethod.main(new String[]{
                        ParserOptions.FLAG_pathToCompiledClassesOfTheProject, TEST_PROJECT_CLASSES,
                        ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, "example.TestSuiteExample",
                        ParserOptions.FLAG_testMethodNamesToRun, "test8:test2:copyOftest2"
                }
        );
        final CoveragePerTestMethodImpl load = CoveragePerTestMethodImpl.load();
        System.out.println(load);
        assertEquals(26, load.getCoverageResultsMap().get("test2").getInstructionsCovered());
        assertEquals(23, load.getCoverageResultsMap().get("test8").getInstructionsCovered());
        assertEquals(26, load.getCoverageResultsMap().get("copyOftest2").getInstructionsCovered());
    }
}