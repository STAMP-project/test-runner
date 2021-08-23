package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import eu.stamp_project.testrunner.runner.Failure;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecFileLoader;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class OnlineCoveredTestResultPerTestMethodImpl implements CoveredTestResultPerTestMethod {

    private static final long serialVersionUID = 3860229575340959882L;

    protected transient final Map<String, ExecutionDataStore> executionDataStoreMap;

    protected final ConcurrentHashMap<String, Coverage> coverageResultsMap;

    protected final List<String> classesDirectory;

    protected transient ExecFileLoader execFileLoader;

    protected transient CoverageTransformer coverageTransformer;

    private final Set<String> runningTests;
    private final Set<Failure> failingTests;
    private final Set<Failure> assumptionFailingTests;
    private final Set<String> ignoredTests;

    public OnlineCoveredTestResultPerTestMethodImpl(List<String> classesDirectory, CoverageTransformer coverageTransformer) {
        this.classesDirectory = classesDirectory;
        this.executionDataStoreMap = new HashMap<>();
        this.coverageResultsMap = new ConcurrentHashMap<>();
        this.coverageTransformer = coverageTransformer;
        this.runningTests = new HashSet<>();
        this.failingTests = new HashSet<>();
        this.assumptionFailingTests = new HashSet<>();
        this.ignoredTests = new HashSet<>();
    }

    public static void setSessionId(String id) {
        RT.getAgent().setSessionId(id);
    }

    public Map<String, ExecutionDataStore> getExecutionDataStoreMap() {
        return executionDataStoreMap;
    }

    public void collect() {
        try {
            execFileLoader = new ExecFileLoader();
            execFileLoader.load(new ByteArrayInputStream(RT.getAgent().getExecutionData(false)));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void reset() {
        RT.getAgent().reset();
        RT.getAgent().getExecutionData(true);
    }

    public List<String> getClassesDirectory() {
        return classesDirectory;
    }

    public ExecutionDataStore getExecutionData() {
        return execFileLoader.getExecutionDataStore();
    }

    public SessionInfoStore getSessionInfos() {
        return execFileLoader.getSessionInfoStore();
    }

    public CoverageTransformer getCoverageTransformer() {
        return coverageTransformer;
    }

    @Override
    public Map<String, Coverage> getCoverageResultsMap() {
        return coverageResultsMap;
    }

    @Override
    public Coverage getCoverageOf(String testMethodName) {
        return this.getCoverageResultsMap().get(testMethodName);
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
        if (that instanceof OnlineCoveredTestResultPerTestMethodImpl) {
            final OnlineCoveredTestResultPerTestMethodImpl thatListener = (OnlineCoveredTestResultPerTestMethodImpl) that;
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

    @Override
    public void save() {
        ListenerUtils.saveToMemoryMappedFile(new File(OUTPUT_DIR, SHARED_MEMORY_FILE), this);
    }

    @Override
    public void computeCoverages() {
        executionDataStoreMap.entrySet().parallelStream()
                .forEach(x -> {
                    Coverage jUnit4Coverage = coverageTransformer.transformJacocoObject(
                            x.getValue(),
                            classesDirectory
                    );
                    coverageResultsMap.put(x.getKey(), jUnit4Coverage);
                });
    }

    /**
     * Load from serialized object
     *
     * @return an Instance of OnlineCoveredTestResultPerTestMethodImpl loaded from a serialized file.
     */
    public static OnlineCoveredTestResultPerTestMethodImpl load() {
        return ListenerUtils.loadFromMemoryMappedFile(ListenerUtils.computeTargetFilePath(OUTPUT_DIR, SHARED_MEMORY_FILE));
    }

    @Override
    public String toString() {
        return "OnlineCoveredTestResultPerTestMethodImpl{" +
                "coverageResultsMap=" + coverageResultsMap +
                ", classesDirectory=" + classesDirectory +
                ", coverageTransformer=" + coverageTransformer +
                ", runningTests=" + runningTests +
                ", failingTests=" + failingTests +
                ", assumptionFailingTests=" + assumptionFailingTests +
                ", ignoredTests=" + ignoredTests +
                '}';
    }

}
