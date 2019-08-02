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

    public static final String SERIALIZE_NAME = "CoveragePerTest";

    public static final String OUTPUT_DIR = "target" + ConstantsHelper.FILE_SEPARATOR;

    public static final String EXTENSION = ".ser";

    public Map<String, Coverage> getCoverageResultsMap();

    public Coverage getCoverageOf(String testMethodName);

    public void save();

}
