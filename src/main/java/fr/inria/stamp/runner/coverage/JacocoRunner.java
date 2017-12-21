package fr.inria.stamp.runner.coverage;

import fr.inria.stamp.runner.test.TestListener;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.instr.Instrumenter;
import org.jacoco.core.runtime.IRuntime;
import org.jacoco.core.runtime.LoggerRuntime;
import org.jacoco.core.runtime.RuntimeData;
import fr.inria.stamp.runner.test.TestRunner;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
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
        final CoverageListener coverageListener = jacocoRunner.run(classesDirectory,
                testClassesDirectory,
                args[1],
                args.length > 2 ? Arrays.asList(args[2].split(":")) : Collections.emptyList()
        );
        coverageListener.save();
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

    private CoverageListener run(String classesDirectory,
                                 String testClassesDirectory,
                                 String fullQualifiedNameOfTestClass,
                                 List<String> testMethodNames) {
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
            runtime.startup(data);
            final CoverageListener coverageListener = new CoverageListener(data, classesDirectory);
            TestRunner.run(fullQualifiedNameOfTestClass, testMethodNames, coverageListener, this.instrumentedClassLoader);
            if (!coverageListener.getFailingTests().isEmpty()) {
                System.err.println("Some test(s) failed during computation of coverage:\n" +
                        coverageListener.getFailingTests()
                                .stream()
                                .map(TestListener.Failure::toString)
                                .collect(Collectors.joining("\n"))
                );
            }
            runtime.shutdown();
            clearCache(this.instrumentedClassLoader);
            return coverageListener;
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

    private class MemoryClassLoader extends URLClassLoader {

        private final Map<String, byte[]> definitions = new HashMap<>();

        public MemoryClassLoader(URL[] urls) {
            super(urls, ClassLoader.getSystemClassLoader());
        }

        /**
         * Add a in-memory representation of a class.
         *
         * @param name  name of the class
         * @param bytes class definition
         */
        public void addDefinition(final String name, final byte[] bytes) {
            definitions.put(name, bytes);
        }

        @Override
        public Class<?> loadClass(final String name)
                throws ClassNotFoundException {
            final byte[] bytes = definitions.get(name);
            try {
                if (bytes != null) {
                    return defineClass(name, bytes, 0, bytes.length);
                }
            } catch (java.lang.LinkageError error) {
                return super.loadClass(name, false);
            }
            return super.loadClass(name, false);
        }

    }

}
