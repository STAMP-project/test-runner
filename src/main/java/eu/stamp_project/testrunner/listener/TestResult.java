package eu.stamp_project.testrunner.listener;

import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

/**
 * High level abstraction representing a test execution result.
 */
public interface TestResult extends Serializable {

    public static final String SERIALIZE_NAME = "TestResult";

    public static final String OUTPUT_DIR = "target" + ConstantsHelper.FILE_SEPARATOR;

    public static final String EXTENSION = ".ser";

    /**
     * Aggregate result of this instance to the given instance
     * @param that the other instance of TestResult of which we need to add the values to this instance
     * @return this
     */
    public TestResult aggregate(TestResult that);

    public Set<Failure> getFailingTests();

    public Set<Failure> getAssumptionFailingTests();

    public Set<String> getIgnoredTests();

    public Failure getFailureOf(String testMethodName);

    public Set<String> getPassingTests();

    public Set<String> getRunningTests();

    public void save();

}
