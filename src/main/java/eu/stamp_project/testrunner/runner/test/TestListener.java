package eu.stamp_project.testrunner.runner.test;

import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 * <p>
 * This object is the output of the execution of the tests.
 */
public class TestListener extends RunListener implements Serializable {

    private List<String> runningTests = new ArrayList<>();
    private List<Failure> failingTests = new ArrayList<>();
    private List<Failure> assumptionFailingTests = new ArrayList<>();
    private List<String> ignoredTests = new ArrayList<>();
    public static final String OUTPUT_DIR = "target/dspot/";
    public static final String EXTENSION = ".ser";

    @Override
    public void testFinished(Description description) throws Exception {
        this.runningTests.add(MethodFilter.getMethodName(description));
    }

    @Override
    public void testFailure(org.junit.runner.notification.Failure failure) throws Exception {
        this.failingTests.add(
                new Failure(
                        MethodFilter.getMethodName(failure.getDescription()),
                        failure.getDescription().getClassName(),
                        failure.getException()
                )
        );
    }

    @Override
    public void testAssumptionFailure(org.junit.runner.notification.Failure failure) {
        this.assumptionFailingTests.add(
                new Failure(
                        MethodFilter.getMethodName(failure.getDescription()),
                        failure.getDescription().getClassName(),
                        failure.getException()
                )
        );
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        this.ignoredTests.add(MethodFilter.getMethodName(description));
    }

    public List<String> getRunningTests() {
        return runningTests;
    }

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

    public TestListener aggregate(TestListener that) {
        this.runningTests.addAll(that.runningTests);
        this.failingTests.addAll(that.failingTests);
        this.assumptionFailingTests.addAll(that.assumptionFailingTests);
        this.ignoredTests.addAll(that.ignoredTests);
        return this;
    }

    public List<Failure> getFailingTests() {
        return failingTests;
    }

    public List<Failure> getAssumptionFailingTests() {
        return assumptionFailingTests;
    }

    public List<String> getIgnoredTests() {
        return ignoredTests;
    }

    public Failure getFailureOf(String testMethodName) {
        return this.getFailingTests().stream()
                .filter(failure -> failure.testCaseName.equals(testMethodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find %s in failing test", testMethodName)));
    }

    protected String getSerializeName() {
        return "TestListener";
    }

    public void save() {
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            if (!outputDir.mkdirs()) {
                System.err.println("Error while creating output dir");
            }
        }
        File f = new File(outputDir, this.getSerializeName() + EXTENSION);
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
        System.out.println("File saved to the following path: "+f.getAbsolutePath());
    }

    public static TestListener load() {
        return new Loader<TestListener>().load(new TestListener().getSerializeName());
    }

    @Override
    public String toString() {
        return "TestListener{" +
                "runningTests=" + runningTests +
                ", failingTests=" + failingTests +
                ", assumptionFailingTests=" + assumptionFailingTests +
                ", ignoredTests=" + ignoredTests +
                '}';
    }
}
