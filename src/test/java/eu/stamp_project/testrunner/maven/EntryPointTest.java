package eu.stamp_project.testrunner.maven;

import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.testrunner.runner.test.TestListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPointTest {

    @Before
    public void setUp() throws Exception {
        EntryPoint.runMavenGoal("src/test/resources/test-projects", "clean");
    }

    @After
    public void tearDown() throws Exception {
        EntryPoint.runMavenGoal("src/test/resources/test-projects", "clean");
    }

    @Test
    public void testOnFailing() {

        /*
            Test the EntryPoint using maven on failing test

                When running with maven, assumption failure are consider as ignored.
         */

        final TestListener testListener =  EntryPoint.runTestClasses("src/test/resources/test-projects", "failing.FailingTestClass");
        assertEquals(2, testListener.getRunningTests().size());
        assertEquals(1, testListener.getPassingTests().size());
        assertEquals(1, testListener.getFailingTests().size());
        assertEquals(0, testListener.getAssumptionFailingTests().size());
        assertEquals(2, testListener.getIgnoredTests().size());

        final Failure testFailing = testListener.getFailureOf("testFailing");
        assertEquals("testFailing", testFailing.testCaseName);

        try {
            testListener.getFailureOf("testPassing");
            fail("Should have throw IllegalArgumentException");
        } catch (IllegalArgumentException e) {
            assertTrue(true); // expected
        }
    }

    @Test
    public void testOnSpecificTestMethod() {

        /*
            Test the EntryPoint using maven on failing test

                When running with maven, assumption failure are consider as ignored.
         */

        final TestListener testListener =  EntryPoint.runTests("src/test/resources/test-projects",
                "example.TestSuiteExample", "test2");
        assertEquals(1, testListener.getRunningTests().size());
        assertEquals(1, testListener.getPassingTests().size());
        assertEquals(0, testListener.getFailingTests().size());
        assertEquals(0, testListener.getAssumptionFailingTests().size());
        assertEquals(0, testListener.getIgnoredTests().size());
    }

}
