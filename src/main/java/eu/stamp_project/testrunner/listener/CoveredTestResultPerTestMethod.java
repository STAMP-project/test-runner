package eu.stamp_project.testrunner.listener;

import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.io.Serializable;
import java.util.Map;

/**
 * Stores both the individual test unit (i.e. method) results as well as the individual coverage
 * information for each of them.
 *
 * @author andre15silva
 */
public interface CoveredTestResultPerTestMethod extends TestResult, Serializable {

	public static final String SERIALIZE_NAME = "CoveredTestResultPerTest";

	public static final String OUTPUT_DIR = "target" + ConstantsHelper.FILE_SEPARATOR;

	public static final String EXTENSION = ".ser";

	public Map<String, Coverage> getCoverageResultsMap();

	public Coverage getCoverageOf(String testMethodName);

	public void save();

	public void computeCoverages();

}
