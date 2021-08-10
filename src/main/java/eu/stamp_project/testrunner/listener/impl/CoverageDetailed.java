package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;

import java.io.File;
import java.io.Serializable;

/**
 * created by Benjamin DANGLOT benjamin.danglot@inria.fr on 14/11/18
 */
public class CoverageDetailed implements Coverage, Serializable {

	private static final long serialVersionUID = 109548359596802378L;
	/**
	 * contains the information of the coverage
	 */
	public CoverageInformation covered = new CoverageInformation();

	public CoverageDetailed() {
		// empty
	}

	public CoverageDetailed(CoverageInformation covered) {
		this.covered = covered;
	}

	@Override
	public int getInstructionsCovered() {
		return -1;
	}

	@Override
	public int getInstructionsTotal() {
		return -1;
	}

	@Override
	public String getExecutionPath() {
		return null;
	}

	@Override
	public boolean isBetterThan(Coverage that) {
		return false;
	}

	@Override
	public String toString() {
		return covered.toString();
	}

	@Override
	public void save() {
		ListenerUtils.saveToMemoryMappedFile(new File(OUTPUT_DIR, SHARED_MEMORY_FILE), this);
	}

	/**
	 * Loads and deserializes the file from a memory mapped file
	 *
	 * @return loaded CoverageDetailed from the memory mapped file
	 */
	public static Coverage load() {
		return ListenerUtils.loadFromMemoryMappedFile(ListenerUtils.computeTargetFilePath(OUTPUT_DIR, SHARED_MEMORY_FILE));
	}

	@Override
	public void setExecutionPath(String executionPath) {

	}

	public CoverageInformation getDetailedCoverage() {

		return covered;
	}

}
