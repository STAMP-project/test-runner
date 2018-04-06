package eu.stamp.project.testrunner;

import eu.stamp.project.testrunner.runner.coverage.Coverage;
import eu.stamp.project.testrunner.runner.coverage.CoveragePerTestMethod;
import eu.stamp.project.testrunner.runner.test.TestListener;
import org.apache.commons.io.FileUtils;
import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 * <p>
 * This class is the EntryPoint of the project. This is the only class that the user should use.
 * <p>
 * Main methods are:
 * <ul>
 * <li>{@link eu.stamp.project.testrunner.EntryPoint#runTestClasses(String, String...)} to run all the given test classes.</li>
 * <li>{@link eu.stamp.project.testrunner.EntryPoint#runTests(String, String, String...)} to run all the test methods of the given test class</li>
 * <li>{@link eu.stamp.project.testrunner.EntryPoint#runCoverageOnTestClasses(String, String, String...)} to compute the coverage of the given test classes</li>
 * <li>{@link eu.stamp.project.testrunner.EntryPoint#runTests(String, String, String...)} to compute the coverage of the test methods of the given test classes</li>
 * </ul>
 * </p>
 * <p>
 * <p>
 * This class relies on {@link eu.stamp.project.testrunner.runner.test.TestRunner} and {@link eu.stamp.project.testrunner.runner.coverage.JacocoRunner}
 * to respectively execute tests and compute the coverage.
 * This class creates a new JVM by calling the command "java" with proper arguments.
 * </p>
 * <p>
 * <p>
 * This class has two options accessible from the outside:
 * <ul>
 * <li>verbose: boolean to enable traces to track the progress</li>
 * <li>defaultTimeoutInMs: integer timeout time in milliseconds for the whole requested process.</li>
 * </ul>
 * </p>
 */
public class EntryPoint {

    /**
     * enable traces to track the progress
     */
    public static boolean verbose = false;

    /**
     * timeout time in milliseconds for the whole requested process
     */
    public static int defaultTimeoutInMs = 10000;

    /**
     * Execution of various test classes.
     * <p>
     * Run all the test classes given as a full qualified name. For instance, my.package.MyClassTest.
     * This methods will run all the test methods within the given test classes.
     * </p>
     *
     * @param classpath                      the classpath required to run the given test classes.
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @return an instance of TestListener {@link eu.stamp.project.testrunner.runner.test.TestListener} containing result of the exeuction of test methods.
     * @throws TimeoutException when the execution takes longer than defaultTimeoutInMs
     */
    public static TestListener runTestClasses(String classpath,
                                              String... fullQualifiedNameOfTestClasses) throws TimeoutException {
        return runTests(Arrays.stream(new String[]{
                        JAVA_COMMAND,
                        classpath + PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES,
                        TEST_RUNNER_QUALIFIED_NAME,
                        Arrays.stream(fullQualifiedNameOfTestClasses)
                                .collect(Collectors.joining(":"))
                }).collect(Collectors.joining(WHITE_SPACE))
        );
    }

    /**
     * Execution of various test methods inside a given test class.
     * <p>
     * Run all the test methods given inside the given test class. The test class must be given as a full qualified name.
     * For instance, my.package.MyClassTest.
     * This methods will run all the test methods given.
     * </p>
     *
     * @param classpath                    the classpath required to run the given test.
     * @param fullQualifiedNameOfTestClass test class to be run.
     * @param testMethods                  test methods to be run.
     * @return an instance of TestListener {@link eu.stamp.project.testrunner.runner.test.TestListener} containing result of the execution of test methods.
     * @throws TimeoutException when the execution takes longer than defaultTimeoutInMs
     */
    public static TestListener runTests(String classpath,
                                        String fullQualifiedNameOfTestClass,
                                        String... testMethods) throws TimeoutException {
        return runTests(Arrays.stream(new String[]{
                        JAVA_COMMAND,
                        classpath + PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES,
                        TEST_RUNNER_QUALIFIED_NAME,
                        fullQualifiedNameOfTestClass,
                        Arrays.stream(testMethods)
                                .collect(Collectors.joining(":"))
                }).collect(Collectors.joining(WHITE_SPACE))
        );
    }

    private static TestListener runTests(String commandLine) throws TimeoutException {
        try {
            runGivenCommandLine(commandLine);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout when running {}", commandLine);
            throw e;
        }
        final TestListener load = TestListener.load();
        if (EntryPoint.verbose) {
            LOGGER.info("Test has been run: {}",
                    Stream.concat(load.getPassingTests().stream().map(Object::toString),
                            load.getFailingTests().stream().map(Object::toString)
                    ).collect(Collectors.joining(",")));
        }
        return load;
    }

    /**
     * Compute of the instruction coverage using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> for various test classes.
     * <p>
     * This method compute the instruction coverage, using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> obtained by executing the given test classes.
     * This method require the path to the binaries, i.e. .class, of the source code on which the instruction must be computed.
     * This method computes the "global" coverage, <i>i.e.</i> the coverage obtained if all the test are run.
     * For a test method per test method result, see {{@link {@link eu.stamp.project.testrunner.EntryPoint#runCoveragePerTestMethods(String, String, String, String...)}
     * </p>
     *
     * @param classpath                      the classpath required to run the given tests classes.
     * @param targetProjectClasses           path to the folder that contains binaries, i.e. .class, on which Jacoco computes the coverage.
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @return an instance of Coverage {@link eu.stamp.project.testrunner.runner.coverage.Coverage} containing result of the execution of test classes.
     * @throws TimeoutException when the execution takes longer than defaultTimeoutInMs
     */
    public static Coverage runCoverageOnTestClasses(String classpath,
                                                    String targetProjectClasses,
                                                    String... fullQualifiedNameOfTestClasses) throws TimeoutException {
        return EntryPoint.runCoverage(Arrays.stream(new String[]{
                        JAVA_COMMAND,
                        classpath +
                                PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES +
                                PATH_SEPARATOR + ABSOLUTE_PATH_TO_JACOCO_DEPENDENCIES,
                        JACOCO_RUNNER_QUALIFIED_NAME,
                        targetProjectClasses,
                        Arrays.stream(fullQualifiedNameOfTestClasses)
                                .collect(Collectors.joining(":"))
                }).collect(Collectors.joining(WHITE_SPACE))
        );
    }

    /**
     * Compute of the instruction coverage using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> for various test methods inside the given test class.
     * <p>
     * This method compute the instruction coverage, using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> obtained by executing the given test methods inside the given test classes.
     * This method require the path to the binaries, i.e. .class, of the source code on which the instruction must be computed.
     * This method computes the "global" coverage, <i>i.e.</i> the coverage obtained if all the test are run.
     * For a test method per test method result, see {{@link {@link eu.stamp.project.testrunner.EntryPoint#runCoveragePerTestMethods(String, String, String, String...)}
     * </p>
     *
     * @param classpath                    the classpath required to run the given tests classes.
     * @param targetProjectClasses         path to the folder that contains binaries, i.e. .class, on which Jacoco computes the coverage.
     * @param fullQualifiedNameOfTestClass test classes to be run.
     * @param methodNames                  test methods to be run.
     * @return an instance of Coverage {@link eu.stamp.project.testrunner.runner.coverage.Coverage} containing result of the exeuction of test classes.
     * @throws TimeoutException when the execution takes longer than defaultTimeoutInMs
     */
    public static Coverage runCoverageOnTests(String classpath,
                                              String targetProjectClasses,
                                              String fullQualifiedNameOfTestClass,
                                              String... methodNames) throws TimeoutException {
        return EntryPoint.runCoverage(Arrays.stream(new String[]{
                        JAVA_COMMAND,
                        classpath +
                                PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES +
                                PATH_SEPARATOR + ABSOLUTE_PATH_TO_JACOCO_DEPENDENCIES,
                        JACOCO_RUNNER_QUALIFIED_NAME,
                        targetProjectClasses,
                        fullQualifiedNameOfTestClass,
                        Arrays.stream(methodNames)
                                .collect(Collectors.joining(":"))
                }).collect(Collectors.joining(WHITE_SPACE))
        );
    }

    /**
     * Compute of the instruction coverage using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> for various test methods inside the given test class.
     * <p>
     * This method computes the instruction coverage, using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> obtained by executing the given test methods inside the given test classes.
     * This method requires the path to the binaries, i.e. .class, of the source code on which the instruction must be computed.
     * This method computes the per test method coverage, <i>i.e.</i> the coverage obtained by each test methods, separately.
     * It does not run one by one test methods, but rather use a specific implementation of {@link org.junit.runner.notification.RunListener}.
     * </p>
     *
     * @param classpath                    the classpath required to run the given tests classes.
     * @param targetProjectClasses         path to the folder that contains binaries, i.e. .class, on which Jacoco computes the coverage.
     * @param fullQualifiedNameOfTestClass test classes to be run.
     * @param methodNames                  test methods to be run.
     * @return a Map that associate each test method name to its instruction coverage, as an instance of Coverage {@link eu.stamp.project.testrunner.runner.coverage.Coverage} of test classes.
     * @throws TimeoutException when the execution takes longer than defaultTimeoutInMs
     */
    public static CoveragePerTestMethod runCoveragePerTestMethods(String classpath,
                                                                  String targetProjectClasses,
                                                                  String fullQualifiedNameOfTestClass,
                                                                  String... methodNames) throws TimeoutException {
        final String commandLine = Arrays.stream(new String[]{
                JAVA_COMMAND,
                classpath +
                        PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES +
                        PATH_SEPARATOR + ABSOLUTE_PATH_TO_JACOCO_DEPENDENCIES,
                JACOCO_RUNNER_PER_TEST_QUALIFIED_NAME,
                targetProjectClasses,
                fullQualifiedNameOfTestClass,
                Arrays.stream(methodNames)
                        .collect(Collectors.joining(":"))
        }).collect(Collectors.joining(WHITE_SPACE));
        try {
            EntryPoint.runGivenCommandLine(commandLine);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout when running {}", commandLine);
            throw e;
        }
        final CoveragePerTestMethod load = CoveragePerTestMethod.load();
        if (EntryPoint.verbose) {
            LOGGER.info("Global Coverage has been computed {}", load.toString());
        }
        return load;
    }

    private static Coverage runCoverage(String commandLine) throws TimeoutException {
        try {
            runGivenCommandLine(commandLine);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout when running {}", commandLine);
            throw e;
        }
        final Coverage load = Coverage.load();
        if (EntryPoint.verbose) {
            LOGGER.info("Global Coverage has been computed {}", load.toString());
        }
        return load;
    }

    private static void runGivenCommandLine(String commandLine) throws TimeoutException {
        if (EntryPoint.verbose) {
            LOGGER.info("Run: {}", commandLine);
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        final Future<?> submit = executor.submit(() -> {
            try {
                Process p = Runtime.getRuntime().exec(commandLine);
                p.waitFor();
                if (EntryPoint.verbose) {
                    new ThreadToReadInputStream(System.out, p.getInputStream()).start();
                    new ThreadToReadInputStream(System.err, p.getErrorStream()).start();
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
        try {
            submit.get(defaultTimeoutInMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            submit.cancel(true);
            executor.shutdownNow();
        }
    }

    private static class ThreadToReadInputStream extends Thread {

        private final PrintStream output;
        private final InputStream input;

        ThreadToReadInputStream(PrintStream output, InputStream input) {
            this.output = output;
            this.input = input;
        }

        @Override
        public synchronized void start() {
            int read;
            try {
                while ((read = this.input.read()) != -1) {
                    this.output.print((char) read);
                }
            } catch (Exception ignored) {
                //ignored
            } finally {
                this.interrupt();
            }
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

    static final String WHITE_SPACE = " ";

    static final String JAVA_COMMAND = "java -cp";

    static final String TEST_RUNNER_QUALIFIED_NAME = "eu.stamp.project.testrunner.runner.test.TestRunner";

    static final String JACOCO_RUNNER_QUALIFIED_NAME = "eu.stamp.project.testrunner.runner.coverage.JacocoRunner";

    static final String JACOCO_RUNNER_PER_TEST_QUALIFIED_NAME = "eu.stamp.project.testrunner.runner.coverage.JacocoRunnerPerTestMethods";

    static final String PATH_SEPARATOR = System.getProperty("path.separator");

    static final String LINE_SEPARATOR = System.getProperty("line.separator");

    static final String ABSOLUTE_PATH_TO_RUNNER_CLASSES = initAbsolutePathToRunnerClasses();

    private static final Function<List<String>, String> LIST_OF_DEPENDENCIES_TO_ABS_PATH = list ->
            Arrays.stream(((URLClassLoader) ClassLoader.getSystemClassLoader())
                    .getURLs())
                    .filter(url -> list.stream().anyMatch(s -> url.getPath().contains(s)))
                    .map(URL::getPath)
                    .collect(Collectors.joining(PATH_SEPARATOR));

    private static final Function<List<Class<?>>, String> CLASSES_TO_PATH_OF_DEPENDENCIES = classes ->
            classes.stream()
                    .map(clazz -> clazz.getResource("/" + clazz.getName().replaceAll("\\.", "/") + ".class"))
                    .map(URL::getPath)
                    .map(path -> path.substring("file:".length()))
                    .map(path -> path.split("!")[0])
                    .collect(Collectors.joining(PATH_SEPARATOR));

    private static final List<Class<?>> JACOCO_DEPENDENCIES = Arrays.asList(
            IRuntime.class,
            Type.class,
            FileUtils.class
    );

    private static final String ABSOLUTE_PATH_TO_JACOCO_DEPENDENCIES = CLASSES_TO_PATH_OF_DEPENDENCIES.apply(JACOCO_DEPENDENCIES);

    private static String initAbsolutePathToRunnerClasses() {
        final String path = ClassLoader.getSystemClassLoader().getResource("runner-classes/").getPath();
        if (path.contains("!") && path.startsWith("file:")) {
            return path.substring("file:".length()).split("!")[0];
        } else {
            return path;
        }
    }

}
