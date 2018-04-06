package eu.stamp.project.testrunner.runner.coverage;

import eu.stamp.project.testrunner.runner.test.Loader;
import eu.stamp.project.testrunner.runner.test.TestListener;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.Description;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/04/18
 *
 * This class represents the instruction coverage per test method.
 */
public class CoveragePerTestMethod extends TestListener {

    private final Map<String, Coverage> coverageResultsMap;

    private final String classesDirectory;

    private transient RuntimeData data;

    private transient ExecutionDataStore executionData;

    private transient SessionInfoStore sessionInfos;

    private CoveragePerTestMethod() {
        coverageResultsMap = null;
        classesDirectory = null;
    }

    public CoveragePerTestMethod(RuntimeData data, String classesDirectory) {
        this.data = data;
        this.classesDirectory = classesDirectory;
        this.coverageResultsMap = new HashMap<>();
    }

    public Map<String, Coverage> getCoverageResultsMap() {
        return coverageResultsMap;
    }

    public Coverage getCoverageOf(String testMethodName) {
        return this.getCoverageResultsMap().get(testMethodName);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        this.executionData = new ExecutionDataStore();
        this.sessionInfos = new SessionInfoStore();
        data.setSessionId(description.getMethodName());
        data.collect(executionData, sessionInfos, true);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        data.collect(executionData, sessionInfos, false);
        final Coverage coverage = new Coverage();
        coverage.collectData(executionData, classesDirectory);
        coverageResultsMap.put(description.getMethodName(), coverage);
    }

    @Override
    protected String getSerializeName() {
        return "perTestCoverageResult";
    }

    /**
     * Load from serialized object
     * @return an Instance of CoveragePerTestMethod loaded from a serialized file. The name of the file is returned by {@link #getSerializeName()}
     */
    public static CoveragePerTestMethod load() {
        return new Loader<CoveragePerTestMethod>().load(new CoveragePerTestMethod().getSerializeName());
    }
}
