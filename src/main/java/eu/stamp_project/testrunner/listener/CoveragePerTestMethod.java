package eu.stamp_project.testrunner.listener;

import eu.stamp_project.testrunner.runner.JUnit4Runner;

import java.io.Serializable;
import java.util.Map;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/11/18
 */
public interface CoveragePerTestMethod extends Serializable {

    public static final String SERIALIZE_NAME = "CoveragePerTest";

    public static final String OUTPUT_DIR = "target" + JUnit4Runner.FILE_SEPARATOR + "dspot" + JUnit4Runner.FILE_SEPARATOR;

    public static final String EXTENSION = ".ser";

    public Map<String, Coverage> getCoverageResultsMap();

    public Coverage getCoverageOf(String testMethodName);

    public void save();

}
