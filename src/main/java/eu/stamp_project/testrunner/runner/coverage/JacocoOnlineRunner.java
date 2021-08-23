package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Failure;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public abstract class JacocoOnlineRunner {

	protected List<String> classesDirectories;

	protected List<String> testClassesDirectories;

	protected List<String> blackList;

	protected CoverageTransformer coverageTransformer;

	public JacocoOnlineRunner(List<String> classesDirectory, List<String> testClassesDirectory, CoverageTransformer coverageTransformer) {
		this(classesDirectory, testClassesDirectory, Collections.emptyList(), coverageTransformer);
	}

	public JacocoOnlineRunner(List<String> classesDirectory, List<String> testClassesDirectory, List<String> blackList, CoverageTransformer coverageTransformer) {
		this.classesDirectories = classesDirectory;
		this.testClassesDirectories = testClassesDirectory;
		this.blackList = blackList;
		this.coverageTransformer = coverageTransformer;
	}

	protected abstract CoveredTestResultPerTestMethod executeCoveredTestPerTestMethod(List<String> classesDirectory,
																					  String[] testClassNames,
	                                                                                  String[] testMethodNames);

	public CoveredTestResultPerTestMethod runCoveredTestResultPerTestMethod(List<String> classesDirectory,
	                                                                        List<String> testClassesDirectory,
	                                                                        String[] testClassNames,
	                                                                        String[] testMethodNames) {
		final CoveredTestResultPerTestMethod listener = this.executeCoveredTestPerTestMethod(classesDirectory, testClassNames, testMethodNames);
		if (!((TestResult) listener).getFailingTests().isEmpty()) {
			System.err.println("Some test(s) failed during computation of coverage:\n" +
					((TestResult) listener).getFailingTests()
							.stream()
							.map(Failure::toString)
							.collect(Collectors.joining("\n"))
			);
		}
		return listener;
	}

}
