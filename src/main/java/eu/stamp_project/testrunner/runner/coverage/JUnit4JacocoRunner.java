package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.TestListener;
import eu.stamp_project.testrunner.listener.junit4.JUnit4Coverage;
import eu.stamp_project.testrunner.runner.JUnit4Runner;
import eu.stamp_project.testrunner.runner.Failure;
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
public class JUnit4JacocoRunner {

    /**
     * The entry method to compute the instruction coverage.
     * This method is not meant to be used directly, but rather using {@link EntryPoint}
     *
     * @param args this array should be build by {@link EntryPoint}
     *             the first argument is the path to classes and test classes separated by the system path separator, <i>e.g. target/classes:target/test-classes</i> for a typical maven project on Linux (the system path separator is ':').
     *             the second argument is the full qualified name of the test class
     *             the third argument is optionally the list of the test method name separated by the system path separator.
     *             You can pass the --blacklist flag, following by a list of test method name to be blacklisted.
     *             You can pass the --junit5 to enable the junit5 mode, otherwise it will run in JUnit4
     *             Each method name is separated with the system path separator.
     */
    public static void main(String[] args) throws ClassNotFoundException {
        // inputs: classes:test-classes, fullqualifiednameoftest, method1:method2....
        final String[] splittedArgs0 = args[0].split(JUnit4Runner.PATH_SEPARATOR);
        final String classesDirectory = splittedArgs0[0];
        final String testClassesDirectory = splittedArgs0[1];
        final JUnit4JacocoRunner jacocoRunner;
        if (args.length > 3 && args[2].equals(JUnit4Runner.BLACK_LIST_OPTION)) {
            jacocoRunner = new JUnit4JacocoRunner(classesDirectory, testClassesDirectory, Arrays.asList(args[3].split(JUnit4Runner.PATH_SEPARATOR)));
        } else {
            jacocoRunner = new JUnit4JacocoRunner(classesDirectory, testClassesDirectory);
        }
        if (args[1].contains(JUnit4Runner.PATH_SEPARATOR)) { // multiple test classes
            jacocoRunner.run(classesDirectory,
                    testClassesDirectory,
                    args[1].split(JUnit4Runner.PATH_SEPARATOR)
            ).save();
        } else {
            if (args.length > 2 && args[2].equals(JUnit4Runner.BLACK_LIST_OPTION)) {
                jacocoRunner.run(classesDirectory,
                        testClassesDirectory,
                        new String[]{args[1]}
                ).save();
            } else {
                jacocoRunner.run(classesDirectory,
                        testClassesDirectory,
                        args[1],
                        args.length > 2 ?
                                args[2].split(JUnit4Runner.PATH_SEPARATOR) : new String[]{}
                ).save();
            }
        }
    }

    protected MemoryClassLoader instrumentedClassLoader;

    protected Instrumenter instrumenter;

    protected IRuntime runtime;

    protected List<String> blackList;

    public JUnit4JacocoRunner(String classesDirectory, String testClassesDirectory) {
        this(classesDirectory, testClassesDirectory, Collections.emptyList());
    }

    public JUnit4JacocoRunner(String classesDirectory, String testClassesDirectory, List<String> blackList) {
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

    private Coverage run(String classesDirectory,
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
        final String resource = JUnit4Runner.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
        System.out.println(resource);
        try {
            this.instrumentedClassLoader.addDefinition(
                    fullQualifiedNameOfTestClass,
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            runtime.startup(data);
            final JUnit4Coverage listener = new JUnit4Coverage();
            JUnit4Runner.run(fullQualifiedNameOfTestClass, Arrays.asList(testMethodNames), listener, this.instrumentedClassLoader);
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

    private Coverage run(String classesDirectory,
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
            final String resource = JUnit4Runner.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
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
            final JUnit4Coverage listener = new JUnit4Coverage();
            JUnit4Runner.run(Arrays.asList(fullQualifiedNameOfTestClasses), this.blackList, listener, this.instrumentedClassLoader);
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
            final String fileName = next.getPath().substring(classesDirectory.length() + (classesDirectory.endsWith(JUnit4Runner.FILE_SEPARATOR) ? 0 : 1));
            final String fullQualifiedName = JUnit4Runner.pathToFullQualifiedName.apply(fileName).substring(0, fileName.length() - ".class".length());
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
