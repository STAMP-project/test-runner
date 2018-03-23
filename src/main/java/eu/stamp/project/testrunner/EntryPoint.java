package eu.stamp.project.testrunner;

import eu.stamp.project.testrunner.runner.coverage.Coverage;
import eu.stamp.project.testrunner.runner.test.TestListener;
import org.apache.commons.io.FileUtils;
import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.Type;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPoint {

    public static boolean verbose = false;

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
     */
    public static TestListener runTestClasses(String classpath,
                                              String... fullQualifiedNameOfTestClasses) {
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
     * @return an instance of TestListener {@link eu.stamp.project.testrunner.runner.test.TestListener} containing result of the exeuction of test methods.
     */
    public static TestListener runTests(String classpath,
                                        String fullQualifiedNameOfTestClass,
                                        String... testMethods) {
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

    private static TestListener runTests(String commandLine) {
        runGivenCommandLine(commandLine);
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
     * </p>
     *
     * @param classpath                      the classpath required to run the given tests classes.
     * @param targetProjectClasses           path to the folder that contains binaries, i.e. .class, on which Jacoco computes the coverage.
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @return an instance of Coverage {@link eu.stamp.project.testrunner.runner.coverage.Coverage} containing result of the exeuction of test classes.
     */
    public static Coverage runCoverageOnTestClasses(String classpath,
                                                    String targetProjectClasses,
                                                    String... fullQualifiedNameOfTestClasses) {
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
     * </p>
     *
     * @param classpath                    the classpath required to run the given tests classes.
     * @param targetProjectClasses         path to the folder that contains binaries, i.e. .class, on which Jacoco computes the coverage.
     * @param fullQualifiedNameOfTestClass test classes to be run.
     * @param methodNames                  test methods to be run.
     * @return an instance of Coverage {@link eu.stamp.project.testrunner.runner.coverage.Coverage} containing result of the exeuction of test classes.
     */
    public static Coverage runCoverageOnTests(String classpath,
                                              String targetProjectClasses,
                                              String fullQualifiedNameOfTestClass,
                                              String... methodNames) {
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

    private static Coverage runCoverage(String commandLine) {
        runGivenCommandLine(commandLine);
        final Coverage load = Coverage.load();
        if (EntryPoint.verbose) {
            LOGGER.info("Global Coverage has been computed {}", load.toString());
        }
        return load;
    }

    private static void printStream(InputStream stream, PrintStream print) {
        try (BufferedReader input =
                     new BufferedReader(new InputStreamReader(stream))) {
            final String toBePrinted = input.lines().collect(Collectors.joining(System.getProperty("line.separator")));
            if (!toBePrinted.isEmpty()) {
                print.println(toBePrinted);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void runGivenCommandLine(String commandLine) {
        if (EntryPoint.verbose) {
            LOGGER.info("Run: {}", commandLine);
        }
        try {
            Process p = Runtime.getRuntime().exec(commandLine);
            p.waitFor();
            if (EntryPoint.verbose) {
                printStream(p.getInputStream(), System.out);
                printStream(p.getErrorStream(), System.err);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

    static final String WHITE_SPACE = " ";

    static final String JAVA_COMMAND = "java -cp";

    static final String TEST_RUNNER_QUALIFIED_NAME = "eu.stamp.project.testrunner.runner.test.TestRunner";

    static final String JACOCO_RUNNER_QUALIFIED_NAME = "eu.stamp.project.testrunner.runner.coverage.JacocoRunner";

    static final String CLOVER_RUNNER_QUALIFIED_NAME = "eu.stamp.project.testrunner.runner.clover.CloverRunner";

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
