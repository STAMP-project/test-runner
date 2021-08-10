package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import eu.stamp_project.testrunner.runner.Failure;

import java.io.File;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/11/18
 */
public class TestResultImpl implements TestResult, Serializable {

    private static final long serialVersionUID = 6898135595908384570L;

    private Set<String> runningTests;
    private Set<Failure> failingTests;
    private Set<Failure> assumptionFailingTests;
    private Set<String> ignoredTests;

    public TestResultImpl() {
        this.runningTests = new HashSet<>();
        this.failingTests = new HashSet<>();
        this.assumptionFailingTests = new HashSet<>();
        this.ignoredTests = new HashSet<>();
    }

    @Override
    public Set<String> getRunningTests() {
        return runningTests;
    }

    @Override
    public Set<String> getPassingTests() {
        Set<String> passingTests = new HashSet<>(runningTests);
        passingTests.removeAll(failingTests.stream().map(x -> x.testCaseName).collect(Collectors.toSet()));
        passingTests.removeAll(assumptionFailingTests.stream().map(x -> x.testCaseName).collect(Collectors.toSet()));
        return passingTests;
    }

    @Override
    public TestResult aggregate(TestResult that) {
        if (that instanceof TestResultImpl) {
            final TestResultImpl thatListener = (TestResultImpl) that;
            this.runningTests.addAll(thatListener.runningTests);
            this.failingTests.addAll(thatListener.failingTests);
            this.assumptionFailingTests.addAll(thatListener.assumptionFailingTests);
            this.ignoredTests.addAll(thatListener.ignoredTests);
        }
        return this;
    }

    @Override
    public Set<Failure> getFailingTests() {
        return failingTests;
    }

    @Override
    public Set<Failure> getAssumptionFailingTests() {
        return assumptionFailingTests;
    }

    @Override
    public Set<String> getIgnoredTests() {
        return ignoredTests;
    }

    @Override
    public Failure getFailureOf(String testMethodName) {
        return this.getFailingTests().stream()
                .filter(failure -> failure.testCaseName.equals(testMethodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find %s in failing test", testMethodName)));
    }

    /**
     * Writes the serialized object to a memory mapped file.
     * The location depends on the workspace set for the test runner process.
     */
    @Override
    public synchronized void save() {
        ListenerUtils.saveToMemoryMappedFile(new File(OUTPUT_DIR, SHARED_MEMORY_FILE), this);
    }

    /**
     * Loads and deserializes the file from a memory mapped file
     *
     * @param workingDirectory working directory of the forked process
     * @return loaded TestResult from the memory mapped file
     */
    public static TestResult load(File workingDirectory) {
        return ListenerUtils.loadFromMemoryMappedFile(ListenerUtils.computeTargetFilePath(workingDirectory, OUTPUT_DIR, SHARED_MEMORY_FILE));
    }

    public String toString() {
        return "TestResultImpl{" +
                "runningTests=" + this.getRunningTests() +
                ", failingTests=" + this.getFailingTests() +
                ", assumptionFailingTests=" + this.getAssumptionFailingTests() +
                ", ignoredTests=" + this.getIgnoredTests() +
                '}';
    }
}
