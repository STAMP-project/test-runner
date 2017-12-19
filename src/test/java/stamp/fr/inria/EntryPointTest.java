package stamp.fr.inria;

import org.junit.Test;
import stamp.fr.inria.test.TestListener;

import java.util.Arrays;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPointTest extends AbstractTest {

    @Test
    public void testRunTestTestClass() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the TestListener with the result of the execution of the test class.
         */

        final TestListener testListener = EntryPoint.runTest(JUNIT_CP + ":"
                        + TEST_PROJECT_CLASSES + ":"
                        + PATH_TO_RUNNER_CLASSES,
                "example.TestSuiteExample",
                Collections.emptyList()
        );
        assertEquals(6, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }

    @Test
    public void testRunTestTestMethods() throws Exception {

        /*
            Test the method runTest() of EntryPoint.
                It should return the TestListener with the result of the execution of the test class.
         */

        final TestListener testListener = EntryPoint.runTest(JUNIT_CP + ":"
                        + TEST_PROJECT_CLASSES + ":"
                        + PATH_TO_RUNNER_CLASSES,
                "example.TestSuiteExample",
                Arrays.asList(new String[]{"test4", "test9"})
        );
        assertEquals(2, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
    }
}
