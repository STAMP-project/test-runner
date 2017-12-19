package stamp.fr.inria.coverage;

import org.junit.Test;
import stamp.fr.inria.AbstractTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class JacocoRunnerTest extends AbstractTest {

    @Test
    public void testExecutionOnTestClass() {
        Process p = null;
        try {
            p = Runtime.getRuntime().exec(commandLine);
            p.waitFor();
            assertEquals(0, p.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try (BufferedReader input =
                     new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            assertEquals("33 / 37" , input.lines().collect(Collectors.joining(nl)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final CoverageResult coverage = CoverageResult.load();
        assertEquals(33, coverage.instructionsCovered);
        assertEquals(37, coverage.instructionsTotal);
    }

    private final String classpath = MAVEN_HOME + "org/jacoco/org.jacoco.core/0.7.9/org.jacoco.core-0.7.9.jar:" +
            MAVEN_HOME + "org/ow2/asm/asm-debug-all/5.2/asm-debug-all-5.2.jar:" +
            MAVEN_HOME + "commons-io/commons-io/2.5/commons-io-2.5.jar:" +
            JUNIT_CP;

    private final String commandLine = "java -cp " +
            classpath + ":" + TEST_PROJECT_CLASSES + ":" + PATH_TO_RUNNER_CLASSES +
            " stamp.fr.inria.coverage.JacocoRunner " +
            TEST_PROJECT_CLASSES +
            " example.TestSuiteExample";
}
