package stamp.fr.inria.test;

import org.junit.Test;
import stamp.fr.inria.AbstractTest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class TestRunnerTest extends AbstractTest {

    @Test
    public void testExecutionTestClass() {

        /*
            Run the whole test class given by the command line.
                We have two outputs:
                    - on stdout, the process prints the list of passing tests
                    - the listener is loaded using the static method load()
         */

        System.out.println(commandLine);

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
            assertEquals("[test2, test3, test4, test7, test8, test9]",
                    input.lines().collect(Collectors.joining(nl)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final TestListener load = TestListener.load();
        assertEquals(6, load.getPassingTests().size());
        assertTrue(load.getFailingTests().isEmpty());
    }

    @Test
    public void testExecutionTestCases() {

        /*
            Run the test cases of the test class given by the command line.
                We have two outputs:
                    - on stdout, the process prints the list of passing tests
                    - the listener is loaded using the static method load()
         */

        Process p = null;
        try {
            p = Runtime.getRuntime().exec(commandLine + " test8:test2");
            p.waitFor();
            assertEquals(0, p.exitValue());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        try (BufferedReader input =
                     new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            assertEquals("[test2, test8]",
                    input.lines().collect(Collectors.joining(System.getProperty("line.separator"))));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        final TestListener load = TestListener.load();
        assertEquals(2, load.getPassingTests().size());
        assertTrue(load.getFailingTests().isEmpty());
    }


    private final String classpath = MAVEN_HOME + "junit/junit/4.11/junit-4.11.jar:" + MAVEN_HOME + "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar";
    private final String compiledClasses = "src/test/resources/test-projects/target/classes:src/test/resources/test-projects/target/test-classes";
    private final String classpathOfTestRunnerClasses = "src/main/resources/";
    private final String commandLine = "java -cp " +
            classpath + ":" + compiledClasses + ":" + classpathOfTestRunnerClasses +
            " stamp.fr.inria.test.TestRunner example.TestSuiteExample";
}
