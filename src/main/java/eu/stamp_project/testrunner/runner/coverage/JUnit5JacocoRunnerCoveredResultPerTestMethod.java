package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.junit5.CoveredTestResultsPerJUnit5TestMethod;
import eu.stamp_project.testrunner.listener.junit5.JUnit5TestResult;
import eu.stamp_project.testrunner.runner.JUnit5Runner;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.jacoco.core.runtime.RuntimeData;

import java.util.Collections;
import java.util.List;

/**
 * JUnit5 implementation of {@link JacocoRunnerCoveredResultPerTestMethod}.
 *
 * @author andre15silva
 */
public class JUnit5JacocoRunnerCoveredResultPerTestMethod extends JacocoRunnerCoveredResultPerTestMethod {

	public JUnit5JacocoRunnerCoveredResultPerTestMethod(List<String> classesDirectory,
														List<String> testClassesDirectory,
														CoverageTransformer coverageTransformer) {
		super(classesDirectory, testClassesDirectory, coverageTransformer);
	}

	public JUnit5JacocoRunnerCoveredResultPerTestMethod(List<String> classesDirectory,
														List<String> testClassesDirectory,
														List<String> blackList,
														CoverageTransformer coverageTransformer) {
		super(classesDirectory, testClassesDirectory, blackList, coverageTransformer);
	}

	public JUnit5JacocoRunnerCoveredResultPerTestMethod(List<String> classesDirectory,
														List<String> testClassesDirectory,
														List<String> blackList,
														int nbFailingLoadClass,
														CoverageTransformer coverageTransformer) {
		super(classesDirectory, testClassesDirectory, blackList, nbFailingLoadClass, coverageTransformer);
	}

	@Override
	protected CoveredTestResultPerTestMethod executeCoveredTestPerTestMethod(RuntimeData data, List<String> classesDirectory, String[] testClassNames, String[] testMethodNames) {
		final CoveredTestResultsPerJUnit5TestMethod listener = new CoveredTestResultsPerJUnit5TestMethod(data, classesDirectory, coverageTransformer);
		JUnit5Runner.run(
				testClassNames,
				testMethodNames,
				Collections.emptyList(),
				this.nbFailingLoadClass,
				(JUnit5TestResult) listener,
				this.instrumentedClassLoader
		);
		listener.computeCoverages();
		return listener;
	}

	public static void main(String[] args) {
		final ParserOptions options = ParserOptions.parse(args);
		new JUnit5JacocoRunnerCoveredResultPerTestMethod(
				options.getPathToCompiledClassesOfTheProject(),
				options.getPathToCompiledTestClassesOfTheProject(),
				options.getBlackList(),
				options.getNbFailingLoadClass(),
				options.getCoverageTransformer()
		).runCoveredTestResultPerTestMethod(
				options.getPathToCompiledClassesOfTheProject(),
				options.getPathToCompiledTestClassesOfTheProject(),
				options.getFullQualifiedNameOfTestClassesToRun(),
				options.getTestMethodNamesToRun()
		).save();
		System.exit(0);
	}

}
