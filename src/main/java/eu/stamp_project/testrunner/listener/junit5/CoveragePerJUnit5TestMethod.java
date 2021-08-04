package eu.stamp_project.testrunner.listener.junit5;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.impl.CoveragePerTestMethodImpl;
import eu.stamp_project.testrunner.runner.Failure;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.launcher.TestIdentifier;

import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/04/18
 *
 * This class represents the instruction coverage per test method.
 */
public class CoveragePerJUnit5TestMethod extends JUnit5TestResult implements CoveragePerTestMethod {

    private static final long serialVersionUID = 8360711686354566769L;

    private CoveragePerTestMethodImpl internalCoverage;


    public CoveragePerJUnit5TestMethod(RuntimeData data, List<String> classesDirectory, CoverageTransformer coverageTransformer) {
        this.internalCoverage = new CoveragePerTestMethodImpl(data, classesDirectory, coverageTransformer);
    }

    @Override
    public synchronized void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            this.internalCoverage.setExecutionData(new ExecutionDataStore());
            this.internalCoverage.setSessionInfos(new SessionInfoStore());
            this.internalCoverage.getData().setSessionId(this.toString.apply(testIdentifier));
            this.internalCoverage.getData().collect(
                    this.internalCoverage.getExecutionData(),
                    this.internalCoverage.getSessionInfos(),
                    true
            );
            this.getRunningTests().add(this.toString.apply(testIdentifier));
        }
    }

    @Override
    public synchronized void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            this.internalCoverage.getData().collect(
                    this.internalCoverage.getExecutionData(),
                    this.internalCoverage.getSessionInfos(),
                    false
            );

            // We need to clone each result so it doesn't get changed by the runtime afterwards
            ExecutionDataStore executionDataStore = new ExecutionDataStore();
            this.internalCoverage.getExecutionData().getContents().stream().forEach(x -> {
                ExecutionData executionData = new ExecutionData(x.getId(), x.getName(), x.getProbes().clone());
                synchronized (executionDataStore) {
                    executionDataStore.put(executionData);
                }
            });
            this.internalCoverage.getExecutionDataStoreMap().put(
                    this.toString.apply(testIdentifier),
                    executionDataStore
            );

            switch (testExecutionResult.getStatus()) {
                case FAILED:
                    this.getFailingTests().add(
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
        return this.internalCoverage.getCoverageResultsMap();
    }

    @Override
    public Coverage getCoverageOf(String testMethodName) {
        return this.internalCoverage.getCoverageOf(testMethodName);
    }

    @Override
    public void save() {
        this.internalCoverage.save();
    }

    public void computeCoverages() {
        this.internalCoverage.computeCoverages();
    }

}
