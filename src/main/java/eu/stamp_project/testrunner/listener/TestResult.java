package eu.stamp_project.testrunner.listener;

import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.io.Serializable;
import java.util.List;

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

    public List<Failure> getFailingTests();

    public List<Failure> getAssumptionFailingTests();

    public List<String> getIgnoredTests();

    public Failure getFailureOf(String testMethodName);

    public List<String> getPassingTests();

    public List<String> getRunningTests();

    public void save();

}
