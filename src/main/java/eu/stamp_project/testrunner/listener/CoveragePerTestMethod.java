package eu.stamp_project.testrunner.listener;

import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.io.Serializable;
import java.util.Map;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/11/18
 */
public interface CoveragePerTestMethod extends Serializable {

    public static final String SHARED_MEMORY_FILE = "CoveragePerTestMethod.dat";

    public static final String OUTPUT_DIR = "target" + ConstantsHelper.FILE_SEPARATOR;

    public Map<String, Coverage> getCoverageResultsMap();

    public Coverage getCoverageOf(String testMethodName);

    public void save();

    public void computeCoverages();

}
