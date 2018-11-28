package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.TestListener;
import eu.stamp_project.testrunner.listener.junit4.CoveragePerJUnit4TestMethod;
import eu.stamp_project.testrunner.listener.junit5.CoveragePerJUnit5TestMethod;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.runner.JUnit4Runner;
import eu.stamp_project.testrunner.runner.JUnit5Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/11/18
 */
public class JacocoRunnerPerTestMethod extends JacocoRunner {


    /**
     * The entry method to compute the instruction coverage per test method.
     * This method is not meant to be used directly, but rather using {@link EntryPoint}
     *
     * @param args this array should be build by {@link EntryPoint}
     *             the first argument is the path to classes and test classes separated by the system path separator. <i>e.g. target/classes:target/test-classes</i> for a typical maven project.
     *             the second argument is the full qualified name of the test class
     *             the third argument is optionally the list of the test method name separated by the system path separator.
     */
    public static void main(String[] args) {
        final ParserOptions options = ParserOptions.parse(args);
        final String[] splittedArgs0 = options.getPathToCompiledClassesOfTheProject().split(JUnit4Runner.PATH_SEPARATOR);
        final String classesDirectory = splittedArgs0[0];
        final String testClassesDirectory = splittedArgs0[1];
        final boolean isJUnit5 = options.isJUnit5();
        final JacocoRunnerPerTestMethod jacocoRunner =
                new JacocoRunnerPerTestMethod(isJUnit5,
                        classesDirectory,
                        testClassesDirectory,
                        options.getBlackList()
                );
        jacocoRunner.run(classesDirectory,
                testClassesDirectory,
                options.getFullQualifiedNameOfTestClassesToRun()[0],
                options.getTestMethodNamesToRun()
        ).save();
    }

    private CoveragePerTestMethod run(String classesDirectory,
                                      String testClassesDirectory,
                                      String fullQualifiedNameOfTestClass,
                                      String[] testMethodNames) {
        final RuntimeData data = new RuntimeData();
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]
                    {new File(testClassesDirectory).toURI().toURL()}, this.instrumentedClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final String resource = JUnit4Runner.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
        try {
            this.instrumentedClassLoader.addDefinition(
                    fullQualifiedNameOfTestClass,
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            this.runtime.startup(data);
            final CoveragePerTestMethod listener;
            if (this.isJUnit5) {
                listener = new CoveragePerJUnit5TestMethod(data, classesDirectory);
                JUnit5Runner.run(new String[]{fullQualifiedNameOfTestClass}, testMethodNames, Collections.emptyList(), (CoveragePerJUnit5TestMethod) listener, this.instrumentedClassLoader);
            } else {
                listener = new CoveragePerJUnit4TestMethod(data, classesDirectory);
                JUnit4Runner.run(new String[]{fullQualifiedNameOfTestClass}, testMethodNames, Collections.emptyList(), (CoveragePerJUnit4TestMethod) listener, this.instrumentedClassLoader);
            }
            if (!((TestListener) listener).getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        ((TestListener) listener).getFailingTests()
                                .stream()
                                .map(Failure::toString)
                                .collect(Collectors.joining("\n"))
                );
            }
            this.runtime.shutdown();
            clearCache(this.instrumentedClassLoader);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public JacocoRunnerPerTestMethod(boolean isJUnit5, String classesDirectory, String testClassesDirectory) {
        super(isJUnit5, classesDirectory, testClassesDirectory);
    }

    public JacocoRunnerPerTestMethod(boolean isJUnit5, String classesDirectory, String testClassesDirectory, List<String> blackList) {
        super(isJUnit5, classesDirectory, testClassesDirectory, blackList);
    }
}
