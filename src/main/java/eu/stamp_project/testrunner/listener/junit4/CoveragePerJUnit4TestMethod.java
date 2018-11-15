package eu.stamp_project.testrunner.listener.junit4;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.impl.CoveragePerTestMethodImpl;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.Description;

import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/04/18
 *
 * This class represents the instruction coverage per test method.
 */
public class CoveragePerJUnit4TestMethod extends JUnit4TestListener implements CoveragePerTestMethod {

    private static final long serialVersionUID = 8360711686354566769L;

    private CoveragePerTestMethodImpl internalCoverage;

    private CoveragePerJUnit4TestMethod() {
        this.internalCoverage = new CoveragePerTestMethodImpl();
    }

    public CoveragePerJUnit4TestMethod(RuntimeData data, String classesDirectory) {
        this.internalCoverage = new CoveragePerTestMethodImpl(data, classesDirectory);
    }


    @Override
    public void testStarted(Description description) throws Exception {
        this.internalCoverage.setExecutionData(new ExecutionDataStore());
        this.internalCoverage.setSessionInfos(new SessionInfoStore());
        this.internalCoverage.getData().setSessionId(description.getMethodName());
        this.internalCoverage.getData().collect(
                this.internalCoverage.getExecutionData(),
                this.internalCoverage.getSessionInfos(),
                true
        );
    }

    @Override
    public void testFinished(Description description) throws Exception {
        this.internalCoverage.getData().collect(
                this.internalCoverage.getExecutionData(),
                this.internalCoverage.getSessionInfos(),
                false
        );
        final JUnit4Coverage jUnit4Coverage = new JUnit4Coverage();
        jUnit4Coverage.collectData(this.internalCoverage.getExecutionData(), this.internalCoverage.getClassesDirectory());
        this.internalCoverage.getCoverageResultsMap().put(description.getMethodName(), jUnit4Coverage);
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

}
