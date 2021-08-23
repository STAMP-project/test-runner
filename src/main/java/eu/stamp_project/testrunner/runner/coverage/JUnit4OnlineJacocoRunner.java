package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.junit4.JUnit4TestResult;
import eu.stamp_project.testrunner.listener.junit4.OnlineCoveredTestResultsPerJUnit4TestMethod;
import eu.stamp_project.testrunner.runner.JUnit4Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;

import java.util.Collections;
import java.util.List;

public class JUnit4OnlineJacocoRunner extends JacocoOnlineRunner {

	public JUnit4OnlineJacocoRunner(List<String> classesDirectory, List<String> testClassesDirectory, CoverageTransformer coverageTransformer) {
		this(classesDirectory, testClassesDirectory, Collections.emptyList(), coverageTransformer);
	}

	public JUnit4OnlineJacocoRunner(List<String> classesDirectory, List<String> testClassesDirectory, List<String> blackList, CoverageTransformer coverageTransformer) {
		super(classesDirectory, testClassesDirectory, blackList, coverageTransformer);
	}

	@Override
	protected CoveredTestResultPerTestMethod executeCoveredTestPerTestMethod(List<String> classesDirectory, String[] testClassNames, String[] testMethodNames) {
		final OnlineCoveredTestResultsPerJUnit4TestMethod listener = new OnlineCoveredTestResultsPerJUnit4TestMethod(classesDirectory, coverageTransformer);
		JUnit4Runner.run(
				testClassNames,
				testMethodNames,
				Collections.emptyList(),
				(JUnit4TestResult) listener,
				JUnit4OnlineJacocoRunner.class.getClassLoader()
		);
		listener.computeCoverages();
		return listener;
	}

	public static void main(String[] args) {
		final ParserOptions options = ParserOptions.parse(args);
		new JUnit4OnlineJacocoRunner(
				options.getPathToCompiledClassesOfTheProject(),
				options.getPathToCompiledTestClassesOfTheProject(),
				options.getBlackList(),
				options.getCoverageTransformer()
		).runCoveredTestResultPerTestMethod(
				options.getPathToCompiledClassesOfTheProject(),
				options.getPathToCompiledTestClassesOfTheProject(),
				options.getFullQualifiedNameOfTestClassesToRun(),
				options.getTestMethodNamesToRun()
		).save();
	}
}
