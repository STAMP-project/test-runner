package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResult;
import eu.stamp_project.testrunner.listener.junit5.JUnit5Coverage;
import eu.stamp_project.testrunner.runner.JUnit5Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.util.List;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/04/19
 */
public class JUnit5JacocoRunner extends JacocoRunner {

	
    public JUnit5JacocoRunner(List<String> classesDirectory,
                              List<String> testClassesDirectory,
                              CoverageTransformer coverageTransformer) {
        super(classesDirectory, testClassesDirectory, coverageTransformer);
    }
    
    public JUnit5JacocoRunner(List<String> classesDirectory,
                              List<String> testClassesDirectory,
                              List<String> blackList,
                              CoverageTransformer coverageTransformer) {
        super(classesDirectory, testClassesDirectory, blackList, coverageTransformer);;
    }

    public JUnit5JacocoRunner(List<String> classesDirectory,
                              List<String> testClassesDirectory,
                              List<String> blackList,
                              int nbFailingLoadClass,
                              CoverageTransformer coverageTransformer) {
        super(classesDirectory, testClassesDirectory, blackList, nbFailingLoadClass, coverageTransformer);;
    }

    /**
     * The entry method to execute junit tests.
     * This method is not meant to be used directly, but rather using {@link EntryPoint}
     * For the expected arguments, see {@link ParserOptions}
     */
    public static void main(String[] args) {
        final ParserOptions options = ParserOptions.parse(args);
        final JacocoRunner jacocoRunner =
                new JUnit5JacocoRunner(
                        options.getPathToCompiledClassesOfTheProject(),
                        options.getPathToCompiledTestClassesOfTheProject(),
                        options.getBlackList(),
                        options.getNbFailingLoadClass(),
                        options.getCoverageTransformer()
                );
        final String[] testClassesToRun = options.getFullQualifiedNameOfTestClassesToRun();
        if (testClassesToRun.length > 1) {
            jacocoRunner.run(
                    options.getPathToCompiledClassesOfTheProject(),
                    options.getPathToCompiledTestClassesOfTheProject(),
                    testClassesToRun
            ).save();
        } else {
            if (options.getTestMethodNamesToRun().length == 0) {
                jacocoRunner.run(
                        options.getPathToCompiledClassesOfTheProject(),
                        options.getPathToCompiledTestClassesOfTheProject(),
                        testClassesToRun
                ).save();
            } else {
                jacocoRunner.run(
                        options.getPathToCompiledClassesOfTheProject(),
                        options.getPathToCompiledTestClassesOfTheProject(),
                        testClassesToRun[0],
                        options.getTestMethodNamesToRun()
                ).save();
            }
        }
        System.exit(0);
    }

    @Override
    protected CoveredTestResult executeTest(String[] testClassNames,
                                            String[] testMethodNames,
                                            List<String> blackList,
                                            int nbFailingLoadClass) {
        final JUnit5Coverage listener = new JUnit5Coverage();
        JUnit5Runner.run(
                testClassNames,
                testMethodNames,
                blackList,
                nbFailingLoadClass,
                listener,
                this.instrumentedClassLoader
        );
        return listener;
    }
    
}
