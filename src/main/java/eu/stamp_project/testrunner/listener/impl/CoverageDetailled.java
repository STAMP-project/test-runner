package eu.stamp_project.testrunner.listener.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Loader;

/**
 * created by Benjamin DANGLOT benjamin.danglot@inria.fr on 14/11/18
 */
public class CoverageDetailled implements Coverage, Serializable {

	private static final long serialVersionUID = 109548359596802378L;
	/**
	 * contains the information of the coverage
	 */
	public CoverageInformation covered = new CoverageInformation();

	public CoverageDetailled() {
		// empty
	}

	public CoverageDetailled(CoverageInformation covered) {
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
		return null;// return this.instructionsCovered + " / " + this.instructionsTotal;
	}

	@Override
	public void save() {
		File outputDir = new File(TestResult.OUTPUT_DIR);
		if (!outputDir.exists()) {
			if (!outputDir.mkdirs()) {
				System.err.println("Error while creating output dir");
			}
		}
		File f = new File(outputDir, SERIALIZE_NAME + EXTENSION);
		try (FileOutputStream fout = new FileOutputStream(f)) {
			try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
				oos.writeObject(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			System.err.println("Error while writing serialized file.");
			throw new RuntimeException(e);
		}
		System.out.println("File saved to the following path: " + f.getAbsolutePath());
	}

	/**
	 * Load from serialized object
	 *
	 * @return an Instance of JUnit4Coverage loaded from a serialized file.
	 */
	public static Coverage load() {
		return new Loader<Coverage>().load(SERIALIZE_NAME);
	}

	@Override
	public void setExecutionPath(String executionPath) {

	}

	public CoverageInformation getDetailedCoverage() {

		return covered;
	}

}
