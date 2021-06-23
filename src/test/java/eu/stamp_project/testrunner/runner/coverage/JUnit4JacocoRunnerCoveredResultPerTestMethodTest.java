package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.AbstractTest;
import eu.stamp_project.testrunner.listener.CoveredTestResultPerTestMethod;
import eu.stamp_project.testrunner.listener.impl.CoveredTestResultPerTestMethodImpl;
import eu.stamp_project.testrunner.runner.ParserOptions;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JUnit4JacocoRunnerCoveredResultPerTestMethodTest extends AbstractTest {

	@Test
	public void testWithoutNewJvmOnTestClassParametrized() throws Exception {

        /*
            Using the api to compute the coverage on a test class
         */

		JUnit4JacocoRunnerCoveredResultPerTestMethod.main(new String[]{
						ParserOptions.FLAG_pathToCompiledClassesOfTheProject, TEST_PROJECT_CLASSES,
						ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, "example.ParametrizedTest",
						ParserOptions.FLAG_testMethodNamesToRun, "test"
				}
		);
		final CoveredTestResultPerTestMethod load = CoveredTestResultPerTestMethodImpl.load();
		System.out.println(load);
		assertEquals(34, load.getCoverageResultsMap().get("test").getInstructionsCovered());
		System.out.println(load.getCoverageResultsMap().get("test").getExecutionPath());
	}

	@Test
	public void testWithoutNewJvmOnTestCases() throws Exception {

        /*
            Using the api to compute the coverage on test cases
         */

		JUnit4JacocoRunnerCoveredResultPerTestMethod.main(new String[]{
						ParserOptions.FLAG_pathToCompiledClassesOfTheProject, TEST_PROJECT_CLASSES,
						ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, "example.TestSuiteExample",
						ParserOptions.FLAG_testMethodNamesToRun, "test3:test2:copyOftest2"
				}
		);
		final CoveredTestResultPerTestMethod load = CoveredTestResultPerTestMethodImpl.load();
		System.out.println(load);
		assertEquals(23, load.getCoverageResultsMap().get("test2").getInstructionsCovered());
		assertEquals(23, load.getCoverageResultsMap().get("test3").getInstructionsCovered());
		assertEquals(23, load.getCoverageResultsMap().get("copyOftest2").getInstructionsCovered());
		System.out.println(load.getCoverageResultsMap().get("test2").getExecutionPath());
		System.out.println(load.getCoverageResultsMap().get("copyOftest2").getExecutionPath());
		System.out.println(load.getCoverageResultsMap().get("test3").getExecutionPath());
	}

	@Test
	public void testWithoutNewJvmOnTestClassAll() throws Exception {

        /*
            Using the api to compute the coverage on test cases
         */

		JUnit4JacocoRunnerCoveredResultPerTestMethod.main(new String[]{
						ParserOptions.FLAG_pathToCompiledClassesOfTheProject, TEST_PROJECT_CLASSES,
						ParserOptions.FLAG_fullQualifiedNameOfTestClassToRun, "example.TestSuiteExample",
				}
		);
		final CoveredTestResultPerTestMethod load = CoveredTestResultPerTestMethodImpl.load();
		System.out.println(load);
		load.getCoverageResultsMap()
				.keySet()
				.stream()
				.map(s -> load.getCoverageResultsMap().get(s).getExecutionPath())
				.forEach(System.out::println);
	}

}