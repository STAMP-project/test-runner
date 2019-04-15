package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.junit4.JUnit4Coverage;
import eu.stamp_project.testrunner.listener.junit4.JUnit4TestResult;
import eu.stamp_project.testrunner.runner.JUnit4Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/04/19
 */
public class JUnit4JacocoRunner extends JacocoRunner {


    public JUnit4JacocoRunner(String classesDirectory, String testClassesDirectory) {
        super(classesDirectory, testClassesDirectory);
    }

    public JUnit4JacocoRunner(String classesDirectory, String testClassesDirectory, List<String> blackList) {
        super(classesDirectory, testClassesDirectory, blackList);
    }

    /**
     * The entry method to execute junit tests.
     * This method is not meant to be used directly, but rather using {@link EntryPoint}
     * For the expected arguments, see {@link ParserOptions}
     */
    public static void main(String[] args) {
        final ParserOptions options = ParserOptions.parse(args);
        final String[] splittedArgs0 = options.getPathToCompiledClassesOfTheProject().split(ConstantsHelper.PATH_SEPARATOR);
        final String classesDirectory = splittedArgs0[0];
        final String testClassesDirectory = splittedArgs0[1];
        final JacocoRunner jacocoRunner =
                new JUnit4JacocoRunner(classesDirectory,
                        testClassesDirectory,
                        options.getBlackList()
                );
        final String[] testClassesToRun = options.getFullQualifiedNameOfTestClassesToRun();
        if (testClassesToRun.length > 1) {
            jacocoRunner.run(classesDirectory,
                    testClassesDirectory,
                    testClassesToRun
            ).save();
        } else {
            if (options.getTestMethodNamesToRun().length == 0) {
                jacocoRunner.run(classesDirectory,
                        testClassesDirectory,
                        testClassesToRun
                ).save();
            } else {
                jacocoRunner.run(classesDirectory,
                        testClassesDirectory,
                        testClassesToRun[0],
                        options.getTestMethodNamesToRun()
                ).save();
            }
        }
    }

    @Override
    protected Coverage executeTest(String[] testClassNames,
                               String[] testMethodNames,
                               List<String> blackList) {
        final Coverage listener = new JUnit4Coverage();
        JUnit4Runner.run(
                testClassNames,
                testMethodNames,
                blackList,
                (JUnit4TestResult) listener,
                this.instrumentedClassLoader
        );
        return listener;
    }

}
