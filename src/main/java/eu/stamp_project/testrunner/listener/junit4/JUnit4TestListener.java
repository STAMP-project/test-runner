package eu.stamp_project.testrunner.listener.junit4;

import eu.stamp_project.testrunner.listener.TestListener;
import eu.stamp_project.testrunner.listener.impl.TestListenerImpl;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 * <p>
 * This object is the output of the execution of the tests.
 */
public class JUnit4TestListener extends RunListener implements TestListener, Serializable {

    private final static long serialVersionUID = 2295395800748319976L;

    private TestListenerImpl internalTestListener;

    public JUnit4TestListener() {
        this.internalTestListener = new TestListenerImpl();
    }

    @Override
    public void testFinished(Description description) throws Exception {
        this.internalTestListener.getRunningTests().add(description.getMethodName());
    }

    @Override
    public void testFailure(org.junit.runner.notification.Failure failure) throws Exception {
        this.internalTestListener.getFailingTests().add(
                new Failure(
                        failure.getDescription().getMethodName(),
                        failure.getDescription().getClassName(),
                        failure.getException()
                )
        );
    }

    @Override
    public void testAssumptionFailure(org.junit.runner.notification.Failure failure) {
        this.internalTestListener.getAssumptionFailingTests().add(
                new Failure(
                        failure.getDescription().getMethodName(),
                        failure.getDescription().getClassName(),
                        failure.getException()
                )
        );
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        this.internalTestListener.getIgnoredTests().add(description.getMethodName());
    }

    @Override
    public List<String> getRunningTests() {
        return this.internalTestListener.getRunningTests();
    }

    @Override
    public List<String> getPassingTests() {
        return this.internalTestListener.getPassingTests();
    }

    @Override
    public TestListener aggregate(TestListener that) {
        if (that instanceof JUnit4TestListener) {
            return this.internalTestListener.aggregate(((JUnit4TestListener) that).internalTestListener);
        } else {
            return this;
        }
    }

    @Override
    public List<Failure> getFailingTests() {
        return this.internalTestListener.getFailingTests();
    }

    @Override
    public List<Failure> getAssumptionFailingTests() {
        return this.internalTestListener.getAssumptionFailingTests();
    }

    @Override
    public List<String> getIgnoredTests() {
        return this.internalTestListener.getIgnoredTests();
    }

    @Override
    public Failure getFailureOf(String testMethodName) {
        return this.getFailingTests().stream()
                .filter(failure -> failure.testCaseName.equals(testMethodName))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException(String.format("Could not find %s in failing test", testMethodName)));
    }

    @Override
    public void save() {
        this.internalTestListener.save();
    }

    public String toString() {
        return "JUnit4TestListener{" +
                "runningTests=" + this.internalTestListener.getRunningTests() +
                ", failingTests=" + this.internalTestListener.getFailingTests() +
                ", assumptionFailingTests=" + this.internalTestListener.getAssumptionFailingTests() +
                ", ignoredTests=" + this.internalTestListener.getIgnoredTests() +
                '}';
    }
}
