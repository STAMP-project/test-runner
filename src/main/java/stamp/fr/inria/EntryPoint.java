package stamp.fr.inria;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import stamp.fr.inria.coverage.CoverageListener;
import stamp.fr.inria.test.TestListener;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPoint {

    public static TestListener runTest(String classpath,
                                       String fullQualifiedNameOfTestClass,
                                       List<String> simpleNameOfTestMethods) {
        final String commandLine = Arrays.stream(new String[]{
                        JAVA_COMMAND,
                        classpath + PATH_SEPARATOR + PATH_TO_RUNNER_CLASSES,
                        TEST_RUNNER_QUALIFIED_NAME,
                        fullQualifiedNameOfTestClass,
                        simpleNameOfTestMethods.stream()
                                .collect(Collectors.joining(PATH_SEPARATOR))
                }
        ).collect(Collectors.joining(WHITE_SPACE));
        runGivenCommandLine(commandLine);
        final TestListener load = TestListener.load();
        LOGGER.info("Test has been run: {}",
                Stream.concat(load.getPassingTests().stream().map(Object::toString),
                        load.getFailingTests().stream().map(Object::toString)
                ).collect(Collectors.joining(",")));
        return load;
    }

    public static CoverageListener runCoverage(String classpath,
                                               String targetProjectClasses,
                                               String fullQualifiedNameOfTestClass) {
        final String commandLine = Arrays.stream(new String[]{
                JAVA_COMMAND,
                classpath + PATH_SEPARATOR + PATH_TO_RUNNER_CLASSES,
                JACOCO_RUNNER_QUALIFIED_NAME,
                targetProjectClasses,
                fullQualifiedNameOfTestClass
        }).collect(Collectors.joining(WHITE_SPACE));
        runGivenCommandLine(commandLine);
        final CoverageListener load = CoverageListener.load();
        LOGGER.info("Coverage has been computed {}", load.toString());
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

    private static void runGivenCommandLine(String commandLine) {
        LOGGER.info("Run: {}", commandLine);
        try {
            Process p = Runtime.getRuntime().exec(commandLine);
            p.waitFor();
            printStream(p.getInputStream(), System.out);
            printStream(p.getErrorStream(), System.err);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

    public static final String WHITE_SPACE = " ";

    public static final String JAVA_COMMAND = "java -cp";

    public static final String TEST_RUNNER_QUALIFIED_NAME = "stamp.fr.inria.test.TestRunner";

    public static final String JACOCO_RUNNER_QUALIFIED_NAME = "stamp.fr.inria.coverage.JacocoRunner";

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String LINE_SEPARATOR = System.getProperty("line.separator");

    public static final String PATH_TO_RUNNER_CLASSES = "src/test/resources/";

}
