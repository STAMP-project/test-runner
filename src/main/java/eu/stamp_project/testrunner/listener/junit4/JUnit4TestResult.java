package eu.stamp_project.testrunner.listener.junit4;

import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.listener.impl.TestResultImpl;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.runner.Description;
import org.junit.runner.notification.RunListener;

import java.io.Serializable;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 * <p>
 * This object is the output of the execution of the tests.
 */
public class JUnit4TestResult extends RunListener implements TestResult, Serializable {

    private final static long serialVersionUID = 2295395800748319976L;

    private TestResultImpl internalTestResult;

    protected transient final Function<Description, String> toString = description ->
            description.getClassName() + "#" + description.getMethodName();

    protected transient final Function<Description, String> toStringParametrized = description ->
            description.getClassName() + "#" + fromParametrizedToSimpleName.apply(description.getMethodName());

    protected static final Predicate<String> isParametrized = testMethodName ->
            Pattern.compile(".+\\[\\d+\\]").matcher(testMethodName).matches();

    protected static final Function<String, String> fromParametrizedToSimpleName = parametrizedName ->
            parametrizedName.contains("[") ? parametrizedName.split("\\[")[0] : parametrizedName;


    public JUnit4TestResult() {
        this.internalTestResult = new TestResultImpl();
    }

    @Override
    public void testFinished(Description description) throws Exception {
        this.internalTestResult.getRunningTests().add(this.toString.apply(description));
    }

    @Override
    public void testFailure(org.junit.runner.notification.Failure failure) throws Exception {
        this.internalTestResult.getFailingTests().add(
                new Failure(
                        this.toString.apply(failure.getDescription()),
                        failure.getDescription().getClassName(),
                        failure.getException()
                )
        );
    }

    @Override
    public void testAssumptionFailure(org.junit.runner.notification.Failure failure) {
        this.internalTestResult.getAssumptionFailingTests().add(
                new Failure(
                        this.toString.apply(failure.getDescription()),
                        failure.getDescription().getClassName(),
                        failure.getException()
                )
        );
    }

    @Override
    public void testIgnored(Description description) throws Exception {
        this.internalTestResult.getIgnoredTests().add(this.toString.apply(description));
    }

    @Override
    public Set<String> getRunningTests() {
        return this.internalTestResult.getRunningTests();
    }

    @Override
    public Set<String> getPassingTests() {
        return this.internalTestResult.getPassingTests();
    }

    @Override
    public TestResult aggregate(TestResult that) {
        if (that instanceof JUnit4TestResult) {
            return this.internalTestResult.aggregate(((JUnit4TestResult) that).internalTestResult);
        } else {
            return this;
        }
    }

    @Override
    public Set<Failure> getFailingTests() {
        return this.internalTestResult.getFailingTests();
    }

    @Override
    public Set<Failure> getAssumptionFailingTests() {
        return this.internalTestResult.getAssumptionFailingTests();
    }

    @Override
    public Set<String> getIgnoredTests() {
        return this.internalTestResult.getIgnoredTests();
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
        this.internalTestResult.save();
    }

    public String toString() {
        return "JUnit4TestResult{" +
                "runningTests=" + this.internalTestResult.getRunningTests() +
                ", failingTests=" + this.internalTestResult.getFailingTests() +
                ", assumptionFailingTests=" + this.internalTestResult.getAssumptionFailingTests() +
                ", ignoredTests=" + this.internalTestResult.getIgnoredTests() +
                '}';
    }
}
