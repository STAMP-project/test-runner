package eu.stamp_project.testrunner.listener;

import eu.stamp_project.testrunner.utils.ConstantsHelper;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/11/18
 */
public interface Coverage {

    public static final String SERIALIZE_NAME = "Coverage";

    public static final String OUTPUT_DIR = "target" + ConstantsHelper.FILE_SEPARATOR;

    public static final String EXTENSION = ".ser";

    public void setExecutionPath(String executionPath);

    public int getInstructionsCovered();

    public int getInstructionsTotal();

    public String getExecutionPath();

    public boolean isBetterThan(Coverage that);

    public void save();

}
