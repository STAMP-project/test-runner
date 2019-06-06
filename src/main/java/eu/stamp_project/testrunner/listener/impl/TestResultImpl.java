package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.runner.Loader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/11/18
 */
public class TestResultImpl implements TestResult, Serializable {

    private static final long serialVersionUID = 6898135595908384570L;

    private List<String> runningTests;
    private List<Failure> failingTests;
    private List<Failure> assumptionFailingTests;
    private List<String> ignoredTests;

    public TestResultImpl() {
        this.runningTests = new ArrayList<>();
        this.failingTests = new ArrayList<>();
        this.assumptionFailingTests = new ArrayList<>();
        this.ignoredTests = new ArrayList<>();
    }

    @Override
    public List<String> getRunningTests() {
        return runningTests;
    }

    @Override
    public List<String> getPassingTests() {
        final List<String> failing = this.failingTests.stream()
                .map(failure -> failure.testCaseName)
                .collect(Collectors.toList());
        final List<String> assumptionFailing = this.assumptionFailingTests.stream()
                .map(failure -> failure.testCaseName)
                .collect(Collectors.toList());
        return this.runningTests.stream()
                .filter(description -> !assumptionFailing.contains(description))
                .filter(description -> !failing.contains(description))
                .collect(Collectors.toList());
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
    public List<Failure> getFailingTests() {
        return failingTests;
    }

    @Override
    public List<Failure> getAssumptionFailingTests() {
        return assumptionFailingTests;
    }

    @Override
    public List<String> getIgnoredTests() {
        return ignoredTests;
    }

    @Override
    public Failure getFailureOf(String testMethodName) {
        return this.getFailingTests().stream()
                .filter(failure -> failure.testCaseName.equals(testMethodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find %s in failing test", testMethodName)));
    }

    @Override
    public synchronized void save() {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Error while creating output dir");
            }
        }
        File f = new File(outputDir, SERIALIZE_NAME + EXTENSION);
        try (FileOutputStream fout = new FileOutputStream(f)) {
            try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
                oos.writeObject(this);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } catch (Exception e) {
            System.err.println("Error while writing serialized file.");
            throw new RuntimeException(e);
        }
        System.out.println("File saved to the following path: " + f.getAbsolutePath());
    }

    public static TestResult load() {
        return new Loader<TestResult>().load(SERIALIZE_NAME);
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
