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
        assertEquals(33, load.getInstructionsCovered());
        assertEquals(118, load.getInstructionsTotal());
        System.out.println(load.getExecutionPath());
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
        assertEquals(26, load.getInstructionsCovered());
        assertEquals(118, load.getInstructionsTotal());
    }

    private final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar:" +
            MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar:" +
            MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar:" +
            JUNIT_CP + ConstantsHelper.PATH_SEPARATOR + JUNIT5_CP;
}
