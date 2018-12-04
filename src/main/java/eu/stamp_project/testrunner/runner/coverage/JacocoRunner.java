package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.TestListener;
import eu.stamp_project.testrunner.listener.junit4.JUnit4Coverage;
import eu.stamp_project.testrunner.listener.junit5.JUnit5Coverage;
import eu.stamp_project.testrunner.runner.JUnit4Runner;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.runner.JUnit5Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/12/17
 */
public class JacocoRunner {

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
        final boolean isJUnit5 = options.isJUnit5();
        final JacocoRunner jacocoRunner =
                new JacocoRunner(isJUnit5,
                        classesDirectory,
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

    protected MemoryClassLoader instrumentedClassLoader;

    protected Instrumenter instrumenter;

    protected IRuntime runtime;

    protected List<String> blackList;

    protected boolean isJUnit5;

    /**
     * @param isJUnit5             tell if the given tests are JUnit5 or not
     * @param classesDirectory     the path to the directory that contains the .class file of sources
     * @param testClassesDirectory the path to the directory that contains the .class file of test sources
     */
    public JacocoRunner(boolean isJUnit5, String classesDirectory, String testClassesDirectory) {
        this(isJUnit5, classesDirectory, testClassesDirectory, Collections.emptyList());
    }

    /**
     * @param isJUnit5             tell if the given tests are JUnit5 or not
     * @param classesDirectory     the path to the directory that contains the .class file of sources
     * @param testClassesDirectory the path to the directory that contains the .class file of test sources
     * @param blackList            the names of the test methods to NOT be run.
     */
    public JacocoRunner(boolean isJUnit5, String classesDirectory, String testClassesDirectory, List<String> blackList) {
        this.isJUnit5 = isJUnit5;
        try {
            this.instrumentedClassLoader = new MemoryClassLoader(
                    new URL[]{
                            new File(classesDirectory).toURI().toURL(),
                            new File(testClassesDirectory).toURI().toURL()
                    }
            );
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        this.blackList = blackList;
        this.runtime = new LoggerRuntime();
        this.instrumenter = new Instrumenter(this.runtime);
        // instrument source code
        instrumentAll(classesDirectory);
    }

    /**
     * Compute the instruction coverage of the given tests
     * Using directly this method is discouraged, since it won't avoid class loading conflict. Use {@link EntryPoint#runCoverage(String, String, String[], String[])} instead.
     *
     * @param classesDirectory             the path to the directory that contains the .class file of sources
     * @param testClassesDirectory         the path to the directory that contains the .class file of test sources
     * @param fullQualifiedNameOfTestClass the full qualified name of the test class to execute
     * @param testMethodNames              the simple names of the test methods to exeecute
     * @return a {@link Coverage} instance that contains the instruction coverage of the given tests.
     */
    public Coverage run(String classesDirectory,
                        String testClassesDirectory,
                        String fullQualifiedNameOfTestClass,
                        String... testMethodNames) {
        final RuntimeData data = new RuntimeData();
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
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
            runtime.startup(data);
            final Coverage listener;
            if (this.isJUnit5) {
                listener = new JUnit5Coverage();
                JUnit5Runner.run(new String[]{fullQualifiedNameOfTestClass}, testMethodNames, Collections.emptyList(), (JUnit5Coverage) listener, this.instrumentedClassLoader);
            } else {
                listener = new JUnit4Coverage();
                JUnit4Runner.run(new String[]{fullQualifiedNameOfTestClass}, testMethodNames, Collections.emptyList(), (JUnit4Coverage) listener, this.instrumentedClassLoader);
            }
            if (!((TestListener) listener).getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        ((TestListener) listener).getFailingTests()
                                .stream()
                                .map(Failure::toString)
                                .collect(Collectors.joining("\n"))
                );
            }
            data.collect(executionData, sessionInfos, false);
            runtime.shutdown();
            clearCache(this.instrumentedClassLoader);
            listener.collectData(executionData, classesDirectory);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Compute the instruction coverage of the given tests
     * Using directly this method is discouraged, since it won't avoid class loading conflict. Use {@link EntryPoint#runCoverage(String, String, String[], String[])} instead.
     *
     * @param classesDirectory               the path to the directory that contains the .class file of sources
     * @param testClassesDirectory           the path to the directory that contains the .class file of test sources
     * @param fullQualifiedNameOfTestClasses the full qualified names of the test classes to execute
     * @return a {@link Coverage} instance that contains the instruction coverage of the given tests.
     */
    public Coverage run(String classesDirectory,
                        String testClassesDirectory,
                        String... fullQualifiedNameOfTestClasses) {
        final RuntimeData data = new RuntimeData();
        final ExecutionDataStore executionData = new ExecutionDataStore();
        final SessionInfoStore sessionInfos = new SessionInfoStore();
        URLClassLoader classLoader;
        try {
            classLoader = new URLClassLoader(new URL[]
                    {new File(testClassesDirectory).toURI().toURL()}, this.instrumentedClassLoader);
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        Arrays.stream(fullQualifiedNameOfTestClasses).forEach(fullQualifiedNameOfTestClass -> {
            final String resource = ConstantsHelper.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
            try {
                this.instrumentedClassLoader.addDefinition(
                        fullQualifiedNameOfTestClass,
                        IOUtils.toByteArray(classLoader.getResourceAsStream(resource)));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            runtime.startup(data);
            final Coverage listener;
            if (this.isJUnit5) {
                listener = new JUnit5Coverage();
                JUnit5Runner.run(fullQualifiedNameOfTestClasses, new String[0], this.blackList, (JUnit5Coverage) listener, this.instrumentedClassLoader);
            } else {
                listener = new JUnit4Coverage();
                JUnit4Runner.run(fullQualifiedNameOfTestClasses, new String[0], this.blackList, (JUnit4Coverage) listener, this.instrumentedClassLoader);
            }
            if (!((TestListener) listener).getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        ((TestListener) listener).getFailingTests()
                                .stream()
                                .map(Failure::toString)
                                .collect(Collectors.joining("\n"))
                );
            }
            data.collect(executionData, sessionInfos, false);
            runtime.shutdown();
            clearCache(this.instrumentedClassLoader);
            listener.collectData(executionData, classesDirectory);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void instrumentAll(String classesDirectory) {
        final Iterator<File> iterator = FileUtils.iterateFiles(new File(classesDirectory), new String[]{"class"}, true);
        while (iterator.hasNext()) {
            final File next = iterator.next();
            final String fileName = next.getPath().substring(classesDirectory.length() + (classesDirectory.endsWith(ConstantsHelper.FILE_SEPARATOR) ? 0 : 1));
            final String fullQualifiedName = ConstantsHelper.pathToFullQualifiedName.apply(fileName).substring(0, fileName.length() - ".class".length());
            try {
                instrumentedClassLoader.addDefinition(fullQualifiedName,
                        instrumenter.instrument(instrumentedClassLoader.getResourceAsStream(fileName), fullQualifiedName));
            } catch (IOException e) {
                throw new RuntimeException(fileName + "," + new File(fileName).getAbsolutePath() +
                        "," + fullQualifiedName, e);
            }
        }
        clearCache(instrumentedClassLoader);
    }


}
