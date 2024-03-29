package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Default implementation of the CoveredTestResultPerTestMethod interface
 *
 * @author andre15silva
 */
public class CoveredTestResultPerTestMethodImpl implements CoveredTestResultPerTestMethod {

	private static final long serialVersionUID = -789740001022671146L;

	protected transient final Map<String, ExecutionDataStore> executionDataStoreMap;

	protected final ConcurrentHashMap<String, Coverage> coverageResultsMap;

	protected final List<String> classesDirectory;

	protected transient RuntimeData data;

	protected transient ExecutionDataStore executionData;

	protected transient SessionInfoStore sessionInfos;

	protected transient CoverageTransformer coverageTransformer;

	private Set<String> runningTests;
	private Set<Failure> failingTests;
	private Set<Failure> assumptionFailingTests;
	private Set<String> ignoredTests;

	public CoveredTestResultPerTestMethodImpl(RuntimeData data, List<String> classesDirectory, CoverageTransformer coverageTransformer) {
		this.data = data;
		this.classesDirectory = classesDirectory;
		this.executionDataStoreMap = new HashMap<>();
		this.coverageResultsMap = new ConcurrentHashMap<>();
		this.coverageTransformer = coverageTransformer;
		this.runningTests = new HashSet<>();
		this.failingTests = new HashSet<>();
		this.assumptionFailingTests = new HashSet<>();
		this.ignoredTests = new HashSet<>();

	}

	public List<String> getClassesDirectory() {
		return classesDirectory;
	}

	public RuntimeData getData() {
		return data;
	}

	public ExecutionDataStore getExecutionData() {
		return executionData;
	}

	public SessionInfoStore getSessionInfos() {
		return sessionInfos;
	}

	public CoverageTransformer getCoverageTransformer() {
		return coverageTransformer;
	}

	public void setData(RuntimeData data) {
		this.data = data;
	}

	public void setExecutionData(ExecutionDataStore executionData) {
		this.executionData = executionData;
	}

	public void setSessionInfos(SessionInfoStore sessionInfos) {
		this.sessionInfos = sessionInfos;
	}

	public Map<String, ExecutionDataStore> getExecutionDataStoreMap() {
		return executionDataStoreMap;
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
		if (that instanceof CoveredTestResultPerTestMethodImpl) {
			final CoveredTestResultPerTestMethodImpl thatListener = (CoveredTestResultPerTestMethodImpl) that;
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

	/**
	 * Writes the serialized object to a memory mapped file.
	 * The location depends on the workspace set for the test runner process.
	 */
	@Override
	public void save() {
		ListenerUtils.saveToMemoryMappedFile(new File(OUTPUT_DIR, SHARED_MEMORY_FILE), this);
	}

	/**
	 * Loads and deserializes the file from a memory mapped file
	 *
	 * @return loaded CoveredTestResultPerTestMethodImpl from the memory mapped file
	 */
	public static CoveredTestResultPerTestMethodImpl load() {
		return ListenerUtils.loadFromMemoryMappedFile(ListenerUtils.computeTargetFilePath(OUTPUT_DIR, SHARED_MEMORY_FILE));
	}

	@Override
	public String toString() {
		return "CoveredTestResultPerTestMethodImpl{" +
				"coverageResultsMap=" + this.coverageResultsMap.keySet()
				.stream()
				.map(test -> "\t" + test + ": " + coverageResultsMap.get(test).toString())
				.collect(Collectors.joining(ConstantsHelper.LINE_SEPARATOR)) +
				", classesDirectory='" + classesDirectory + '\'' +
				", data=" + data +
				", executionData=" + executionData +
				", sessionInfos=" + sessionInfos +
				", coverageTransformer=" + coverageTransformer +
				", runningTests=" + runningTests +
				", failingTests=" + failingTests +
				", assumptionFailingTests=" + assumptionFailingTests +
				", ignoredTests=" + ignoredTests +
				'}';
	}

}
