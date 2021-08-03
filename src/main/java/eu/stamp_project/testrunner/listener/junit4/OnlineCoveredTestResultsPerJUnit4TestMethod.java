package eu.stamp_project.testrunner.listener.junit4;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.listener.impl.OnlineCoveredTestResultPerTestMethodImpl;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.runner.Description;

import java.util.List;
import java.util.Map;

public class OnlineCoveredTestResultsPerJUnit4TestMethod extends JUnit4TestResult implements CoveredTestResultPerTestMethod {

	private static final long serialVersionUID = -8098503960956110904L;

	private final OnlineCoveredTestResultPerTestMethodImpl internalCoveredTestResult;

	public OnlineCoveredTestResultsPerJUnit4TestMethod(List<String> classesDirectory, CoverageTransformer coverageTransformer) {
		this.internalCoveredTestResult = new OnlineCoveredTestResultPerTestMethodImpl(classesDirectory, coverageTransformer);
	}

	@Override
	public synchronized void testStarted(Description description) throws Exception {
		this.internalCoveredTestResult.setSessionId(this.toString.apply(description));
		this.internalCoveredTestResult.reset();
	}

	@Override
	public synchronized void testFinished(Description description) throws Exception {
		this.internalCoveredTestResult.getRunningTests().add(this.toString.apply(description));
		this.internalCoveredTestResult.collect();

		Coverage jUnit4Coverage =
				internalCoveredTestResult.getCoverageTransformer().transformJacocoObject(
						this.internalCoveredTestResult.getExecutionData(),
						this.internalCoveredTestResult.getClassesDirectory()
				);

		this.internalCoveredTestResult.getCoverageResultsMap().put(this.toString.apply(description), jUnit4Coverage);
	}

	@Override
	public void testFailure(org.junit.runner.notification.Failure failure) throws Exception {
		this.internalCoveredTestResult.getFailingTests().add(
				new Failure(
						this.toString.apply(failure.getDescription()),
						failure.getDescription().getClassName(),
						failure.getException()
				)
		);
	}

	@Override
	public void testAssumptionFailure(org.junit.runner.notification.Failure failure) {
		this.internalCoveredTestResult.getAssumptionFailingTests().add(
				new Failure(
						this.toString.apply(failure.getDescription()),
						failure.getDescription().getClassName(),
						failure.getException()
				)
		);
	}

	@Override
	public void testIgnored(Description description) throws Exception {
		this.internalCoveredTestResult.getIgnoredTests().add(this.toString.apply(description));
	}

	@Override
	public Map<String, Coverage> getCoverageResultsMap() {
		return this.internalCoveredTestResult.getCoverageResultsMap();
	}

	@Override
	public Coverage getCoverageOf(String testMethodName) {
		return this.internalCoveredTestResult.getCoverageOf(testMethodName);
	}

	@Override
	public List<String> getRunningTests() {
		return this.internalCoveredTestResult.getRunningTests();
	}

	@Override
	public List<String> getPassingTests() {
		return this.internalCoveredTestResult.getPassingTests();
	}

	@Override
	public TestResult aggregate(TestResult that) {
		if (that instanceof OnlineCoveredTestResultsPerJUnit4TestMethod) {
			return this.internalCoveredTestResult.aggregate(((OnlineCoveredTestResultsPerJUnit4TestMethod) that).internalCoveredTestResult);
		} else {
			return this;
		}
	}

	@Override
	public List<Failure> getFailingTests() {
		return this.internalCoveredTestResult.getFailingTests();
	}

	@Override
	public List<Failure> getAssumptionFailingTests() {
		return this.internalCoveredTestResult.getAssumptionFailingTests();
	}

	@Override
	public List<String> getIgnoredTests() {
		return this.internalCoveredTestResult.getIgnoredTests();
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
		this.internalCoveredTestResult.save();
	}

}
