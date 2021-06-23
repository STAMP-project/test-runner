package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.apache.commons.io.IOUtils;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.ResourceBundle.clearCache;

public abstract class JacocoRunnerCoveredResultPerTestMethod extends JacocoRunnerPerTestMethod {


	public JacocoRunnerCoveredResultPerTestMethod(String classesDirectory, String testClassesDirectory, CoverageTransformer coverageTransformer) {
		super(classesDirectory, testClassesDirectory, coverageTransformer);
	}

	public JacocoRunnerCoveredResultPerTestMethod(String classesDirectory, String testClassesDirectory, List<String> blackList, CoverageTransformer coverageTransformer) {
		super(classesDirectory, testClassesDirectory, blackList, coverageTransformer);
	}

	public CoveredTestResultPerTestMethod runCoveredTestResultPerTestMethod(String classesDirectory,
	                                                                        String testClassesDirectory,
	                                                                        String fullQualifiedNameOfTestClass,
	                                                                        String[] testMethodNames) {
		final RuntimeData data = new RuntimeData();
		URLClassLoader classLoader;
		try {
			classLoader = new URLClassLoader(new URL[]
					{new File(testClassesDirectory).toURI().toURL()}, this.instrumentedClassLoader);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
		final String resource = ConstantsHelper.fullQualifiedNameToPath.apply(fullQualifiedNameOfTestClass) + ".class";
		try {
			this.instrumentedClassLoader.addDefinition(
					fullQualifiedNameOfTestClass,
					IOUtils.toByteArray(classLoader.getResourceAsStream(resource))
			);
			this.runtime.startup(data);
			final CoveredTestResultPerTestMethod listener = this.executeCoveredTestPerTestMethod(data, classesDirectory, new String[]{fullQualifiedNameOfTestClass}, testMethodNames);
			if (!((TestResult) listener).getFailingTests().isEmpty()) {
				System.err.println("Some test(s) failed during computation of coverage:\n" +
						((TestResult) listener).getFailingTests()
								.stream()
								.map(Failure::toString)
								.collect(Collectors.joining("\n"))
				);
			}
			this.runtime.shutdown();
			clearCache(this.instrumentedClassLoader);
			return listener;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected CoveragePerTestMethod executeTestPerTestMethod(RuntimeData data, String classesDirectory, String[] testClassNames, String[] testMethodNames) {
		throw new UnsupportedOperationException();
	}

	protected abstract CoveredTestResultPerTestMethod executeCoveredTestPerTestMethod(RuntimeData data,
	                                                                                  String classesDirectory,
	                                                                                  String[] testClassNames,
	                                                                                  String[] testMethodNames);

}