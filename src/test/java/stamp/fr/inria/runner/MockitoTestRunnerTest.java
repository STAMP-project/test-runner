package stamp.fr.inria.runner;

import stamp.fr.inria.listener.TestListener;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/07/17.
 */
public class MockitoTestRunnerTest {

    @Test
    public void testRunTestClass() throws Exception {
        TestRunner runner = new MockitoTestRunner(
                new String[]{
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("info.sanaulla.dal.BookDALTest");
        assertEquals(5, results.getRunningTests().size());
        assertEquals(1, results.getFailingTests().size());
        assertEquals(4, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testRunTestClasses() throws Exception {
        TestRunner runner = new MockitoTestRunner(
                new String[]{
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run(Arrays.asList(new String[]{
                "info.sanaulla.dal.BookDALTest",
                "info.sanaulla.dal.BookDALTest"
        }));
        assertEquals(10, results.getRunningTests().size());
        assertEquals(2, results.getFailingTests().size());
        assertEquals(8, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testRunTestMethod() throws Exception {
        TestRunner runner = new MockitoTestRunner(
                new String[]{
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("info.sanaulla.dal.BookDALTest", "testGetAllBooks");
        assertEquals(1, results.getRunningTests().size());
        assertEquals(0, results.getFailingTests().size());
        assertEquals(1, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testRunTestMethods() throws Exception {
        TestRunner runner = new MockitoTestRunner(
                new String[]{
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("info.sanaulla.dal.BookDALTest",
                Arrays.asList(new String[]{"testGetAllBooks", "testGetAllBooksFailing"}));
        assertEquals(2, results.getRunningTests().size());
        assertEquals(1, results.getFailingTests().size());
        assertEquals(1, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }

    @Test
    public void testRunTestClassesMethods() throws Exception {
        TestRunner runner = new MockitoTestRunner(
                new String[]{
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });

        Map<String, Collection<String>> testMethodsNamesForClasses = new HashMap<>();
        testMethodsNamesForClasses.put("info.sanaulla.dal.BookDALTest", Collections.singletonList("testGetAllBooks"));

        TestListener results = runner.run(testMethodsNamesForClasses);
        assertEquals(1, results.getRunningTests().size());
        assertEquals(0, results.getFailingTests().size());
        assertEquals(1, results.getPassingTests().size());
        assertEquals(0, results.getAssumptionFailingTests().size());
        assertEquals(0, results.getIgnoredTests().size());
    }
}
