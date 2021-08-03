package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.runner.Loader;
import org.jacoco.agent.rt.RT;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.tools.ExecFileLoader;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OnlineCoveredTestResultPerTestMethodImpl implements CoveredTestResultPerTestMethod {

	private static final long serialVersionUID = 3860229575340959882L;

	protected final Map<String, Coverage> coverageResultsMap;

	protected final List<String> classesDirectory;

	protected transient ExecFileLoader execFileLoader;

	protected transient CoverageTransformer coverageTransformer;

	private final List<String> runningTests;
	private final List<Failure> failingTests;
	private final List<Failure> assumptionFailingTests;
	private final List<String> ignoredTests;

	private static final String SERVICE_URL = "service:jmx:rmi:///jndi/rmi://localhost:9999/jmxrmi";

	public OnlineCoveredTestResultPerTestMethodImpl(List<String> classesDirectory, CoverageTransformer coverageTransformer) {
		this.classesDirectory = classesDirectory;
		this.coverageResultsMap = new HashMap<>();
		this.coverageTransformer = coverageTransformer;
		this.runningTests = new ArrayList<>();
		this.failingTests = new ArrayList<>();
		this.assumptionFailingTests = new ArrayList<>();
		this.ignoredTests = new ArrayList<>();
	}

	public static void setSessionId(String id) {
		RT.getAgent().setSessionId(id);
		/*
		try {
			// Open connection to the coverage agent:
			final JMXServiceURL url = new JMXServiceURL(SERVICE_URL);
			final JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
			final MBeanServerConnection connection = jmxc
					.getMBeanServerConnection();

			final IProxy proxy = (IProxy) MBeanServerInvocationHandler
					.newProxyInstance(connection,
							new ObjectName("org.jacoco:type=Runtime"), IProxy.class,
							false);

			// Set new session id
			proxy.setSessionId(id);

			// Close connection:
			jmxc.close();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		 */
	}

	public void collect() {
		try {
			execFileLoader = new ExecFileLoader();
			execFileLoader.load(new ByteArrayInputStream(RT.getAgent().getExecutionData(false)));
			/*
			// Open connection to the coverage agent:
			final JMXServiceURL url = new JMXServiceURL(SERVICE_URL);
			final JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
			final MBeanServerConnection connection = jmxc
					.getMBeanServerConnection();

			final IProxy proxy = (IProxy) MBeanServerInvocationHandler
					.newProxyInstance(connection,
							new ObjectName("org.jacoco:type=Runtime"), IProxy.class,
							false);

			// Collect
			execFileLoader = new ExecFileLoader();
			execFileLoader.load(new ByteArrayInputStream(proxy.getExecutionData(false)));

			// Close connection:
			jmxc.close();
			 */
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void reset() {
		RT.getAgent().reset();
		RT.getAgent().getExecutionData(true);
		/*
		try {
			// Open connection to the coverage agent:
			final JMXServiceURL url = new JMXServiceURL(SERVICE_URL);
			final JMXConnector jmxc = JMXConnectorFactory.connect(url, null);
			final MBeanServerConnection connection = jmxc
					.getMBeanServerConnection();

			final IProxy proxy = (IProxy) MBeanServerInvocationHandler
					.newProxyInstance(connection,
							new ObjectName("org.jacoco:type=Runtime"), IProxy.class,
							false);

			// Collect
			proxy.reset();

			// Close connection:
			jmxc.close();
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException(e);
		}
		 */
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
	public List<String> getRunningTests() {
		return runningTests;
	}

	@Override
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
	public List<Failure> getFailingTests() {
		return failingTests;
	}

	@Override
	public List<Failure> getAssumptionFailingTests() {
		return assumptionFailingTests;
	}

	@Override
	public List<String> getIgnoredTests() {
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
		File outputDir = new File(TestResult.OUTPUT_DIR);
		if (!outputDir.exists()) {
			if (!outputDir.mkdirs()) {
				System.err.println("Error while creating output dir");
			}
		}
		File f = new File(outputDir, SERIALIZE_NAME + EXTENSION);
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
		System.out.println("File saved to the following path: " + f.getAbsolutePath());
	}

	/**
	 * Load from serialized object
	 *
	 * @return an Instance of OnlineCoveredTestResultPerTestMethodImpl loaded from a serialized file.
	 */
	public static OnlineCoveredTestResultPerTestMethodImpl load() {
		return new Loader<OnlineCoveredTestResultPerTestMethodImpl>().load(SERIALIZE_NAME);
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

	/**
	 * Interface for JMX proxy
	 */
	public interface IProxy {
		String getVersion();

		String getSessionId();

		void setSessionId(String id);

		byte[] getExecutionData(boolean reset);

		void dump(boolean reset);

		void reset();
	}
}
