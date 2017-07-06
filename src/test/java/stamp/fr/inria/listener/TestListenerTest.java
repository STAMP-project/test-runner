package stamp.fr.inria.listener;

import org.junit.Test;
import stamp.fr.inria.runner.DefaultTestRunner;
import stamp.fr.inria.runner.TestRunner;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
public class TestListenerTest {

    @Test
    public void testAggregate() throws Exception {

        TestRunner runner = new DefaultTestRunner(
                new String[]{
                        "src/test/resources/example-0.0.1-SNAPSHOT.jar",
                        "src/test/resources/example-0.0.1-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("example.TestSuiteExample");
        TestListener results2 = runner.run("example.TestSuiteExample");

        assertEquals(8, results.getRunningTests().size());
        assertEquals(1, results.getFailingTests().size());
        assertEquals(6, results.getPassingTests().size());
        assertEquals(1, results.getAssumptionFailingTests().size());
        assertEquals(1, results.getIgnoredTests().size());

        assertEquals(8, results2.getRunningTests().size());
        assertEquals(1, results2.getFailingTests().size());
        assertEquals(6, results2.getPassingTests().size());
        assertEquals(1, results2.getAssumptionFailingTests().size());
        assertEquals(1, results2.getIgnoredTests().size());

        results.aggregate(results2);

        assertEquals(16, results.getRunningTests().size());
        assertEquals(2, results.getFailingTests().size());
        assertEquals(12, results.getPassingTests().size());
        assertEquals(2, results.getAssumptionFailingTests().size());
        assertEquals(2, results.getIgnoredTests().size());
    }
}