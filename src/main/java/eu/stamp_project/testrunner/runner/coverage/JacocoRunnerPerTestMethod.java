package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.TestCoveredResult;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/11/18
 */
public abstract class JacocoRunnerPerTestMethod extends JacocoRunner {

    /**
     * Compute the instruction coverage of the given tests per test methods
     * Using directly this method is discouraged, since it won't avoid class loading conflict. Use {@link EntryPoint#runCoverage(String, String, String[], String[])} instead.
     *
     * @param classesDirectory             the path to the directory that contains the .class file of sources
     * @param testClassesDirectory         the path to the directory that contains the .class file of test sources
     * @param fullQualifiedNameOfTestClass the full qualified name of the test class to execute
     * @param testMethodNames              the simple names of the test methods to exeecute
     * @return a {@link CoveragePerTestMethod} instance that contains the instruction coverage of the given tests.
     */
    public CoveragePerTestMethod runCoveragePerTestMethod(String classesDirectory,
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
        final String resource = ConstantsHelper.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
        try {
            this.instrumentedClassLoader.addDefinition(
                    fullQualifiedNameOfTestClass,
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            this.runtime.startup(data);
            final CoveragePerTestMethod listener = this.executeTestPerTestMethod(data, classesDirectory, new String[]{fullQualifiedNameOfTestClass}, testMethodNames);
            if (!((TestResult) listener).getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        ((TestResult) listener).getFailingTests()
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

    @Override
    protected TestCoveredResult executeTest(String[] testClassNames, String[] testMethodNames, List<String> blackList) {
        throw new UnsupportedOperationException();
    }

    protected abstract CoveragePerTestMethod executeTestPerTestMethod(RuntimeData data,
                                                                      String classesDirectory,
                                                                      String[] testClassNames,
                                                                      String[] testMethodNames);

    /**
     * @param classesDirectory     the path to the directory that contains the .class file of sources
     * @param testClassesDirectory the path to the directory that contains the .class file of test sources
     */
    public JacocoRunnerPerTestMethod(String classesDirectory, String testClassesDirectory) {
        super(classesDirectory, testClassesDirectory);
    }

    /**
     * @param classesDirectory     the path to the directory that contains the .class file of sources
     * @param testClassesDirectory the path to the directory that contains the .class file of test sources
     * @param blackList            the names of the test methods to NOT be run.
     */
    public JacocoRunnerPerTestMethod(String classesDirectory, String testClassesDirectory, List<String> blackList) {
        super(classesDirectory, testClassesDirectory, blackList);
    }
}
