package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.AbstractTest;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import eu.stamp_project.testrunner.runner.JUnit4Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
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
                        ParserOptions.FLAG_pathToCompiledClassesOfTheProject, TEST_PROJECT_CLASSES,
                        ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, "example.TestSuiteExample"
                }
        );
        final Coverage load = CoverageImpl.load();
        assertEquals(30, load.getInstructionsCovered());
        assertEquals(107, load.getInstructionsTotal());
        System.out.println(String.join(ConstantsHelper.LINE_SEPARATOR, load.getExecutionPath().split(";")));
        System.out.println(load.getInstructionsCovered());
    }

    @Test
    public void testWithoutNewJvmOnTestCases() throws Exception {

        /*
            Using the api to compute the coverage on test cases
         */

        JacocoRunner.main(new String[]{
                        ParserOptions.FLAG_pathToCompiledClassesOfTheProject, TEST_PROJECT_CLASSES,
                        ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, "example.TestSuiteExample",
                        ParserOptions.FLAG_testMethodNamesToRun, "test8:test2"
                }
        );
        final Coverage load = CoverageImpl.load();
        assertEquals(23, load.getInstructionsCovered());
        assertEquals(107, load.getInstructionsTotal());
        assertEquals(115, load.getInstructionsTotal());
        System.out.println(load.getExecutionPath());
    }
}
