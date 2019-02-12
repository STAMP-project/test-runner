package eu.stamp_project.testrunner;

import eu.stamp_project.testrunner.listener.*;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import eu.stamp_project.testrunner.listener.impl.CoveragePerTestMethodImpl;
import eu.stamp_project.testrunner.listener.impl.TestListenerImpl;
import eu.stamp_project.testrunner.listener.junit4.JUnit4Coverage;
import eu.stamp_project.testrunner.runner.JUnit4Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.apache.commons.io.FileUtils;
import org.jacoco.core.runtime.IRuntime;
import org.objectweb.asm.xml.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 * <p>
 * This class is the EntryPoint of the project. This is the only class that the user should use.
 * </p>
 * <p>
 * Main methods are:
 * </p>
 * <ul>
 * <li>{@link EntryPoint#runTests(String, String[], String[])} to run all the given test classes.</li>
 * <li>{@link EntryPoint#runCoverage(String, String, String[], String[])} to compute the coverage of the given test classes</li>
 * <li>{@link EntryPoint#runCoveragePerTestMethods(String, String, String[], String[])} to compute the coverage of the test methods of the given test classes</li>
 * </ul>
 * <p>
 * This class relies on {@link JUnit4Runner} and {@link eu.stamp_project.testrunner.runner.coverage.JacocoRunner}
 * to respectively execute tests and compute the coverage.
 * This class creates a new JVM by calling the command "java" with proper arguments.
 * <p>
 * This class has options accessible from the outside:
 * </p>
 * <ul>
 * <li>verbose: boolean to enable traces to track the progress</li>
 * <li>timeoutInMs: integer timeout time in milliseconds for the whole requested process.</li>
 * <li>workingDirectory: change the directory where the java command will be launch</li>
 * <li>JVMArgs: pass Java Virtual Machine arguments to the java command</li>
 * <li>outPrintStream: to redirect the standard output to a custom print stream</li>
 * <li>errPrintStream: to redirect the standard error output to a custom print stream</li>
 * <li>persistence: if enable, keeps the configuration between runs, else reset it</li>
 * </ul>
 */
public class EntryPoint {

    /**
     * enable the JUnit 5 test runner
     */
    public static boolean jUnit5Mode = false;

    /**
     * enable traces to track the progress
     */
    public static boolean verbose = false;

    /**
     * timeout time in milliseconds for the whole requested process
     */
    public static int timeoutInMs = 10000;

    /**
     * working directory of the java sub-process.
     * By default, it is set to null to inherit from this java process.
     */
    public static File workingDirectory = null;

    /**
     * {@link EntryPoint} uses the command "java". This field allows users to specify Java Virtual Machine(JVM) arguments, <i>e.g.</i> -Xms4G.
     * If this value is <code>null</code>, {@link EntryPoint} won't pass any JVMArgs.
     * The value of this field should be properly formatted for command line usage, <i>e.g.</i> -Xms4G -Xmx8G -XX:-UseGCOverheadLimit.
     * The args should be separated with white spaces.
     */
    public static String JVMArgs = null;

    /**
     * Allows to pass a customized PrintStream on which the java process called will printout.
     * If this field is equal to null, {@link EntryPoint} with use the stdout.
     */
    public static PrintStream outPrintStream = null;

    /**
     * Allows to pass a customized PrintStream on which the java process called will printerr.
     * If this field is equal to null, {@link EntryPoint} with use the stderr.
     */
    public static PrintStream errPrintStream = null;

    /**
     * Enable this boolean to keep the values after each run.
     * The list of concerned values are:
     * JVMArgs,
     * outPrintStream,
     * errPrintStream,
     * workingDirectory,
     * timeoutInMs,
     */
    public static boolean persistence = true;

    /**
     * Allows to blacklist specific test methods of a test class.
     * This blackList is used only in {@link EntryPoint#runTests} and {@link EntryPoint#runCoverage(String, String, String[], String[])} since
     * other methods allow to specify which test method to execute.
     */
    public static List<String> blackList = new ArrayList<>();


    /* EXECUTION OF TEST API */


    public static TestListener runTests(String classpath,
                                        String fullQualifiedNameOfTestClass) throws TimeoutException {
        return EntryPoint.runTests(classpath, new String[]{fullQualifiedNameOfTestClass}, new String[0]);
    }

    public static TestListener runTests(String classpath,
                                        String[] fullQualifiedNameOfTestClasses) throws TimeoutException {
        return EntryPoint.runTests(classpath, fullQualifiedNameOfTestClasses, new String[0]);
    }

    public static TestListener runTests(String classpath,
                                        String fullQualifiedNameOfTestClass,
                                        String methodName) throws TimeoutException {
        return EntryPoint.runTests(classpath, new String[]{fullQualifiedNameOfTestClass}, new String[]{methodName});
    }

    public static TestListener runTests(String classpath,
                                        String fullQualifiedNameOfTestClass,
                                        String[] methodNames) throws TimeoutException {
        return EntryPoint.runTests(classpath, new String[]{fullQualifiedNameOfTestClass}, methodNames);
    }

    /**
     * Execution of various test methods inside a given test class.
     * <p>
     * Run all the test methods given inside the given test class. The test class must be given as a full qualified name.
     * For instance, my.package.MyClassTest.
     * This methods will run all the test methods given.
     * </p>
     *
     * @param classpath                      the classpath required to run the given test.
     * @param fullQualifiedNameOfTestClasses test class to be run.
     * @param methodNames                    test methods to be run.
     * @return an instance of TestListener {@link TestListener} containing result of the execution of test methods.
     * @throws TimeoutException when the execution takes longer than timeoutInMs
     */
    public static TestListener runTests(String classpath,
                                        String[] fullQualifiedNameOfTestClasses,
                                        String[] methodNames) throws TimeoutException {
        final String javaCommand = String.join(ConstantsHelper.WHITE_SPACE, new String[]{
                        getJavaCommand(),
                        classpath +
                                ConstantsHelper.PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES,
                        EntryPoint.jUnit5Mode ? EntryPoint.JUNIT5_TEST_RUNNER_QUALIFIED_NAME : EntryPoint.JUNIT4_TEST_RUNNER_QUALIFIED_NAME,
                        ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, String.join(ConstantsHelper.PATH_SEPARATOR, fullQualifiedNameOfTestClasses),
                        methodNames.length == 0 ?  "" :
                                ParserOptions.FLAG_testMethodNamesToRun + ConstantsHelper.WHITE_SPACE + String.join(ConstantsHelper.PATH_SEPARATOR, methodNames),
                        EntryPoint.blackList.isEmpty() ? "" :
                                (ParserOptions.FLAG_blackList + ConstantsHelper.WHITE_SPACE + String.join(ConstantsHelper.PATH_SEPARATOR, EntryPoint.blackList))
                }
        );
        return EntryPoint.runTests(javaCommand);
    }

    private static TestListener runTests(String commandLine) throws TimeoutException {
        try {
            runGivenCommandLine(commandLine);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout when running {}", commandLine);
            throw e;
        }
        final TestListener load = TestListenerImpl.load();
        if (EntryPoint.verbose) {
            LOGGER.info("Test has been run: {}",
                    Stream.concat(load.getPassingTests().stream().map(Object::toString),
                            load.getFailingTests().stream().map(Object::toString)
                    ).collect(Collectors.joining(",")));
        }
        return load;
    }

    /* COMPUTE COVERAGE API */

    public static Coverage runCoverage(String classpath,
                                       String targetProjectClasses,
                                       String fullQualifiedNameOfTestClass) throws TimeoutException {
        return EntryPoint.runCoverage(classpath, targetProjectClasses, new String[]{fullQualifiedNameOfTestClass}, new String[0]);
    }

    public static Coverage runCoverage(String classpath,
                                       String targetProjectClasses,
                                       String[] fullQualifiedNameOfTestClasses) throws TimeoutException {
        return EntryPoint.runCoverage(classpath, targetProjectClasses, fullQualifiedNameOfTestClasses, new String[0]);
    }

    public static Coverage runCoverage(String classpath,
                                       String targetProjectClasses,
                                       String fullQualifiedNameOfTestClass,
                                       String[] methodNames) throws TimeoutException {
        return EntryPoint.runCoverage(classpath, targetProjectClasses, new String[]{fullQualifiedNameOfTestClass}, methodNames);
    }

    /**
     * Compute of the instruction coverage using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> for various test classes.
     * <p>
     * This method compute the instruction coverage, using <a href=http://www.eclemma.org/jacoco/>JaCoCo</a> obtained by executing the given test classes.
     * This method require the path to the binaries, i.e. .class, of the source code on which the instruction must be computed.
     * This method computes the "global" coverage, <i>i.e.</i> the coverage obtained if all the test are run.
     * For a test method per test method result, see {@link EntryPoint#runCoveragePerTestMethods(String, String, String[], String[])}
     * </p>
     *
     * @param classpath                      the classpath required to run the given tests classes.
     * @param targetProjectClasses           path to the folder that contains binaries, i.e. .class, on which Jacoco computes the coverage.
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @param methodNames                    test methods to be run. Can be empty
     * @return an instance of Coverage {@link Coverage} containing result of the execution of test classes.
     * @throws TimeoutException when the execution takes longer than timeoutInMs
     */
    public static Coverage runCoverage(String classpath,
                                       String targetProjectClasses,
                                       String[] fullQualifiedNameOfTestClasses,
                                       String[] methodNames) throws TimeoutException {
        final String javaCommand = String.join(ConstantsHelper.WHITE_SPACE, new String[]{
                        getJavaCommand(),
                        classpath +
                                ConstantsHelper.PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES +
                                ConstantsHelper.PATH_SEPARATOR + ABSOLUTE_PATH_TO_JACOCO_DEPENDENCIES,
                        EntryPoint.JACOCO_RUNNER_QUALIFIED_NAME,
                        ParserOptions.FLAG_pathToCompiledClassesOfTheProject, targetProjectClasses,
                        ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, String.join(ConstantsHelper.PATH_SEPARATOR, fullQualifiedNameOfTestClasses),
                        methodNames.length == 0 ? "" :
                                ParserOptions.FLAG_testMethodNamesToRun, String.join(ConstantsHelper.PATH_SEPARATOR, methodNames),
                        EntryPoint.blackList.isEmpty() ? "" :
                                (ParserOptions.FLAG_blackList + ConstantsHelper.WHITE_SPACE + String.join(ConstantsHelper.PATH_SEPARATOR, EntryPoint.blackList)),
                        EntryPoint.jUnit5Mode ? ParserOptions.FLAG_isJUnit5 : ""
                }
        );
        return EntryPoint.runCoverage(javaCommand);
    }

    /* COMPUTE COVERAGE PER TEST METHOD API */

    public static CoveragePerTestMethod runCoveragePerTestMethods(String classpath,
                                                                  String targetProjectClasses,
                                                                  String fullQualifiedNameOfTestClass) throws TimeoutException {
        return EntryPoint.runCoveragePerTestMethods(classpath, targetProjectClasses, new String[]{fullQualifiedNameOfTestClass}, new String[0]);
    }

    public static CoveragePerTestMethod runCoveragePerTestMethods(String classpath,
                                                                  String targetProjectClasses,
                                                                  String fullQualifiedNameOfTestClass,
                                                                  String testMethodName) throws TimeoutException {
        return EntryPoint.runCoveragePerTestMethods(classpath, targetProjectClasses, new String[]{fullQualifiedNameOfTestClass}, new String[]{testMethodName});
    }

    public static CoveragePerTestMethod runCoveragePerTestMethods(String classpath,
                                                                  String targetProjectClasses,
                                                                  String fullQualifiedNameOfTestClass,
                                                                  String[] testMethodNames) throws TimeoutException {
        return EntryPoint.runCoveragePerTestMethods(classpath, targetProjectClasses, new String[]{fullQualifiedNameOfTestClass}, testMethodNames);
    }

    public static CoveragePerTestMethod runCoveragePerTestMethods(String classpath,
                                                                  String targetProjectClasses,
                                                                  String[] fullQualifiedNameOfTestClasses) throws TimeoutException {
        return EntryPoint.runCoveragePerTestMethods(classpath, targetProjectClasses, fullQualifiedNameOfTestClasses, new String[0]);
    }

    private static Coverage runCoverage(String commandLine) throws TimeoutException {
        try {
            runGivenCommandLine(commandLine);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout when running {}", commandLine);
            throw e;
        }
        final Coverage load = CoverageImpl.load();
        if (EntryPoint.verbose) {
            LOGGER.info("Global coverage has been computed {}", load.toString());
        }
        return load;
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
     * @param classpath                      the classpath required to run the given tests classes.
     * @param targetProjectClasses           path to the folder that contains binaries, i.e. .class, on which Jacoco computes the coverage.
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @param methodNames                    test methods to be run.
     * @return a Map that associate each test method name to its instruction coverage, as an instance of JUnit4Coverage {@link JUnit4Coverage} of test classes.
     * @throws TimeoutException when the execution takes longer than timeoutInMs
     */
    public static CoveragePerTestMethod runCoveragePerTestMethods(String classpath,
                                                                  String targetProjectClasses,
                                                                  String[] fullQualifiedNameOfTestClasses,
                                                                  String[] methodNames) throws TimeoutException {
        final String javaCommand = String.join(ConstantsHelper.WHITE_SPACE, new String[]{
                        getJavaCommand(),
                        classpath +
                                ConstantsHelper.PATH_SEPARATOR + ABSOLUTE_PATH_TO_RUNNER_CLASSES +
                                ConstantsHelper.PATH_SEPARATOR + ABSOLUTE_PATH_TO_JACOCO_DEPENDENCIES,
                        EntryPoint.JACOCO_RUNNER_PER_TEST_QUALIFIED_NAME,
                        ParserOptions.FLAG_pathToCompiledClassesOfTheProject, targetProjectClasses,
                        ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, String.join(ConstantsHelper.PATH_SEPARATOR, fullQualifiedNameOfTestClasses),
                        methodNames.length == 0 ? "" :
                                ParserOptions.FLAG_testMethodNamesToRun, String.join(ConstantsHelper.PATH_SEPARATOR, methodNames),
                        EntryPoint.blackList.isEmpty() ? "" :
                                (ParserOptions.FLAG_blackList + ConstantsHelper.WHITE_SPACE + String.join(ConstantsHelper.PATH_SEPARATOR, EntryPoint.blackList)),
                        EntryPoint.jUnit5Mode ? ParserOptions.FLAG_isJUnit5 : ""
                }
        );
        try {
            EntryPoint.runGivenCommandLine(javaCommand);
        } catch (TimeoutException e) {
            LOGGER.warn("Timeout when running {}", javaCommand);
            throw e;
        }
        final CoveragePerTestMethod load = CoveragePerTestMethodImpl.load();
        if (EntryPoint.verbose) {
            LOGGER.info("Coverage per test methods has been computed {}{}", ConstantsHelper.LINE_SEPARATOR, load.toString());
        }
        return load;
    }

    /*

        INTERNAL CLASS AND METHOD

     */
    private static class RunnableProcess implements Runnable {

        private Process process;

        private String commandLine;

        public RunnableProcess(Process process, String commandLine) {
            this.process = process;
            this.commandLine = commandLine;
        }

        @Override
        public void run() {
            try {

                if (EntryPoint.verbose) {
                    new ThreadToReadInputStream(
                            EntryPoint.outPrintStream != null ? EntryPoint.outPrintStream : System.out,
                            process.getInputStream()
                    ).start();
                    new ThreadToReadInputStream(EntryPoint.errPrintStream != null ? EntryPoint.errPrintStream : System.err
                            , process.getErrorStream()
                    ).start();
                }
                process.waitFor();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    private static void runGivenCommandLine(String commandLine) throws TimeoutException {
        if (EntryPoint.verbose) {
            LOGGER.info("Run: {}", commandLine);
        }
        if (workingDirectory != null && !workingDirectory.exists()) {
            LOGGER.warn("The specified working directory does not exist: {}." +
                            "{} Inherit from this process: {}." +
                            " Reset workingDirectory variable.",
                    workingDirectory.getAbsolutePath(),
                    new File(".").getAbsolutePath()
            );
            workingDirectory = null;
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Process process = null;
        try {
            process = Runtime.getRuntime().exec(commandLine, null, workingDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        final Future<?> submit = executor.submit(new RunnableProcess(process, commandLine));
        try {
            submit.get(timeoutInMs, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (process != null) {
                process.destroyForcibly();
            }
            submit.cancel(true);
            executor.shutdownNow();
            if (!persistence) {
                reset();
            }
        }
    }

    private static void reset() {
        EntryPoint.JVMArgs = null;
        EntryPoint.workingDirectory = null;
        EntryPoint.timeoutInMs = EntryPoint.DEFAULT_TIMEOUT;
        EntryPoint.outPrintStream = null;
        EntryPoint.errPrintStream = null;
        EntryPoint.blackList.clear();
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

    /*
        FIELDS
     */

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

    private static final String JAVA_COMMAND = "java";

    private static final String CLASSPATH_OPT = "-classpath";

    private static final String JUNIT4_TEST_RUNNER_QUALIFIED_NAME = "eu.stamp_project.testrunner.runner.JUnit4Runner";

    private static final String JUNIT5_TEST_RUNNER_QUALIFIED_NAME = "eu.stamp_project.testrunner.runner.JUnit5Runner";

    private static final String JACOCO_RUNNER_QUALIFIED_NAME = "eu.stamp_project.testrunner.runner.coverage.JacocoRunner";

    private static final String JACOCO_RUNNER_PER_TEST_QUALIFIED_NAME = "eu.stamp_project.testrunner.runner.coverage.JacocoRunnerPerTestMethod";

    private static final String ABSOLUTE_PATH_TO_RUNNER_CLASSES = initAbsolutePathToRunnerClasses();

    private static final int DEFAULT_TIMEOUT = 10000;

    static String getJavaCommand() {
        if (EntryPoint.JVMArgs != null) {
            return JAVA_COMMAND + ConstantsHelper.WHITE_SPACE + EntryPoint.JVMArgs + ConstantsHelper.WHITE_SPACE + CLASSPATH_OPT;
        } else {
            return JAVA_COMMAND + ConstantsHelper.WHITE_SPACE + CLASSPATH_OPT;
        }
    }

    private static String RemoveWinFileSeparator(String string) {
        if (!"/".equals(ConstantsHelper.FILE_SEPARATOR) && string.startsWith(ConstantsHelper.FILE_SEPARATOR)) {
            return string.substring(1);
        } else {
            return string;
        }
    }

    private static final Function<List<Class<?>>, String> CLASSES_TO_PATH_OF_DEPENDENCIES = classes ->
            classes.stream()
                    .map(clazz -> clazz.getProtectionDomain().getCodeSource().getLocation())
                    .map(URL::getPath)
                    .map(path -> path.startsWith("file:") ? path.substring("file:".length()) : path)
                    .map(path -> path.split("!")[0])
                    .map(path -> path.replace("/", ConstantsHelper.FILE_SEPARATOR))
                    .map(EntryPoint::RemoveWinFileSeparator)
                    .map(path -> {
                        LOGGER.info("{}", path);
                        return path;
                    })
                    .collect(Collectors.joining(ConstantsHelper.PATH_SEPARATOR));

    private static final List<Class<?>> JACOCO_DEPENDENCIES = Arrays.asList(
            IRuntime.class,
            Processor.class,
            FileUtils.class
    );

    private static final String ABSOLUTE_PATH_TO_JACOCO_DEPENDENCIES = CLASSES_TO_PATH_OF_DEPENDENCIES.apply(JACOCO_DEPENDENCIES);

    private static String initAbsolutePathToRunnerClasses() {
        URL resource = ClassLoader.getSystemClassLoader().getResource("runner-classes/");

        //this is the way to make it work from a jenkins plugin
        if (resource == null) {
            resource = EntryPoint.class.getClassLoader().getResource("runner-classes/");
        }
        // if the resource is null, this is because of the usage of a custom class loader.
        // For example, if we use the test-runner within a maven plugin, the resource must be find using
        // ClassRealm#findResource(String)
        // to not add every dependencies to each case, we use here reflection
        if (resource == null) {
            // for now, we use specifically ClassRealm.
            // If we encounter new problems of the same type, i.e. class loading problem
            // we will extends this support other cases, but for now, we implement the way for maven plugin.
            try {
                final Class<? extends ClassLoader> aClass = EntryPoint.class.getClassLoader().getClass();
                final Method findResources = aClass.getMethod("findResource", String.class);
                resource = (URL) findResources.invoke(EntryPoint.class.getClassLoader(), "runner-classes/");
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
        String path = resource.getPath();
        if (path.contains("!") && path.startsWith("file:")) {
            path = path.substring("file:".length()).split("!")[0];
        }
        path = RemoveWinFileSeparator(path.replace("/", ConstantsHelper.FILE_SEPARATOR));
        LOGGER.info("Path to runner Classes: {}", path);
        return path;
    }

}
