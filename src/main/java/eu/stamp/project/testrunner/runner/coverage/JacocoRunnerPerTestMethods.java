package eu.stamp.project.testrunner.runner.coverage;

import eu.stamp.project.testrunner.runner.test.Failure;
import eu.stamp.project.testrunner.runner.test.TestRunner;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/04/18
 */
public class JacocoRunnerPerTestMethods extends JacocoRunner {

    /**
     * The entry method to compute the instruction coverage per test method.
     * This method is not meant to be used directly, but rather using {@link eu.stamp.project.testrunner.EntryPoint}
     * @param args this array should be build by {@link eu.stamp.project.testrunner.EntryPoint}
     *             the first argument is the path to classes and test classes separated by ":". <i>e.g. target/classes:target/test-classes</i> for a typical maven project.
     *             the second argument is the full qualified name of the test class
     *             the third argument is optionally the list of the test method name separated by ":".
     * @throws ClassNotFoundException in case of the supplied classpath is wrong
     */
    public static void main(String[] args) throws ClassNotFoundException {
        // inputs: classes:test-classes, fullqualifiednameoftest, method1:method2....
        final String classesDirectory = args[0].split(":")[0];
        final String testClassesDirectory = args[0].split(":")[1];
        final JacocoRunnerPerTestMethods jacocoRunner = new JacocoRunnerPerTestMethods(classesDirectory, testClassesDirectory);
        jacocoRunner.run(classesDirectory,
                    testClassesDirectory,
                    args[1],
                    args.length > 2 ? args[2].split(":") : new String[]{}
        ).save();
    }

    private CoveragePerTestMethod run(String classesDirectory,
                         String testClassesDirectory,
                         String fullQualifiedNameOfTestClass,
                         String... testMethodNames) {
        final RuntimeData data = new RuntimeData();
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]
                    {new File(testClassesDirectory).toURI().toURL()}, this.instrumentedClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        final String resource = fullQualifiedNameOfTestClass.replace('.', '/') + ".class";
        try {
            this.instrumentedClassLoader.addDefinition(
                    fullQualifiedNameOfTestClass,
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            this.runtime.startup(data);
            final CoveragePerTestMethod listener = new CoveragePerTestMethod(data, classesDirectory);
            TestRunner.run(fullQualifiedNameOfTestClass, Arrays.asList(testMethodNames), listener, this.instrumentedClassLoader);
            if (!listener.getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        listener.getFailingTests()
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

    private JacocoRunnerPerTestMethods(String classesDirectory, String testClassesDirectory) {
        super(classesDirectory, testClassesDirectory);
    }

}
