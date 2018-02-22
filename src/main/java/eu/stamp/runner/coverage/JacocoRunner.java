package eu.stamp.runner.coverage;

import eu.stamp.runner.test.Failure;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import eu.stamp.runner.test.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 18/12/17
 */
public class JacocoRunner {

    public static void main(String[] args) {
        // inputs: classes:test-classes, fullqualifiednameoftest, method1:method2....
        final String classesDirectory = args[0].split(":")[0];
        final String testClassesDirectory = args[0].split(":")[1];
        final JacocoRunner jacocoRunner = new JacocoRunner(classesDirectory, testClassesDirectory);
        if (args[1].contains(":")) {
            jacocoRunner.run(classesDirectory,
                    testClassesDirectory,
                    args[1].split(":")
            ).save();
        } else {
            jacocoRunner.run(classesDirectory,
                    testClassesDirectory,
                    args[1],
                    args.length > 2 ? args[2].split(":") : new String[]{}
            ).save();
        }
    }

    private MemoryClassLoader instrumentedClassLoader;

    private Instrumenter instrumenter;

    private IRuntime runtime;

    public JacocoRunner(String classesDirectory, String testClassesDirectory) {
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
        final String resource = fullQualifiedNameOfTestClass.replace('.', '/') + ".class";
        try {
            this.instrumentedClassLoader.addDefinition(
                    fullQualifiedNameOfTestClass,
                    IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
            );
            runtime.startup(data);
            final Coverage listener = new Coverage();
            TestRunner.run(fullQualifiedNameOfTestClass, Arrays.asList(testMethodNames), listener, this.instrumentedClassLoader);
            if (!listener.getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        listener.getFailingTests()
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
            final String resource = fullQualifiedNameOfTestClass.replace('.', '/') + ".class";
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
            final Coverage listener = new Coverage();
            TestRunner.run(Arrays.asList(fullQualifiedNameOfTestClasses), listener, this.instrumentedClassLoader);
            if (!listener.getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        listener.getFailingTests()
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
            final String fileName = next.getPath().substring(classesDirectory.length() + (classesDirectory.endsWith("/") ? 0 : 1));
            final String fullQualifiedName = fileName.replaceAll("/", ".").substring(0, fileName.length() - ".class".length());
            try {
                instrumentedClassLoader.addDefinition(fullQualifiedName,
                        instrumenter.instrument(instrumentedClassLoader.getResourceAsStream(fileName), fullQualifiedName));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        clearCache(instrumentedClassLoader);
    }


}
