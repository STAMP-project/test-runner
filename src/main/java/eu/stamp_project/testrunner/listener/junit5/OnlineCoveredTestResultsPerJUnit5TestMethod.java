package eu.stamp_project.testrunner.listener.junit5;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.impl.OnlineCoveredTestResultPerTestMethodImpl;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

import java.util.List;
import java.util.Map;

public class OnlineCoveredTestResultsPerJUnit5TestMethod extends JUnit5TestResult implements CoveredTestResultPerTestMethod {

    private static final long serialVersionUID = -6377245801235679892L;

    private final OnlineCoveredTestResultPerTestMethodImpl internalCoveredTestResult;

    public OnlineCoveredTestResultsPerJUnit5TestMethod(List<String> classesDirectory, CoverageTransformer coverageTransformer) {
        this.internalCoveredTestResult = new OnlineCoveredTestResultPerTestMethodImpl(classesDirectory, coverageTransformer);
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (testIdentifier.isTest()) {
            this.internalCoveredTestResult.getIgnoredTests().add(this.toString.apply(testIdentifier));
        }
    }

    @Override
    public synchronized void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            this.internalCoveredTestResult.setSessionId(this.toString.apply(testIdentifier));
            this.internalCoveredTestResult.reset();
            this.internalCoveredTestResult.getRunningTests().add(this.toString.apply(testIdentifier));
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            this.internalCoveredTestResult.collect();

            this.internalCoveredTestResult.getExecutionDataStoreMap().put(
                    this.toString.apply(testIdentifier),
                    ListenerUtils.cloneExecutionDataStore(this.internalCoveredTestResult.getExecutionData())
            );

            switch (testExecutionResult.getStatus()) {
                case FAILED:
                    this.internalCoveredTestResult.getFailingTests().add(
                            new Failure(
                                    this.toString.apply(testIdentifier),
                                    this.toClassName.apply(testIdentifier),
                                    testExecutionResult.getThrowable().get()
                            )
                    );
                    break;
                default:
                    break;
            }
        }
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
    public synchronized void save() {
        this.internalCoveredTestResult.save();
    }

    @Override
    public void computeCoverages() {
        this.internalCoveredTestResult.computeCoverages();
    }
}
