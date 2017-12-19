package stamp.fr.inria.coverage;


import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import stamp.fr.inria.test.TestListener;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/07/17
 */
public class CoverageResult implements Serializable {

	public final int instructionsCovered;

	public final int instructionsTotal;

	//private final CoverageBuilder coverageBuilder;

	public CoverageResult(int instructionsCovered, int instructionsTotal) {
		this.instructionsCovered = instructionsCovered;
		this.instructionsTotal = instructionsTotal;
		//this.coverageBuilder = null;
	}

	public CoverageResult(CoverageBuilder coverageBuilder) {
		//this.coverageBuilder = coverageBuilder;
		final int[] counter = new int[2];
		coverageBuilder.getClasses().stream()
				.map(IClassCoverage::getInstructionCounter)
				.forEach(iCounter -> {
					counter[0] += iCounter.getCoveredCount();
					counter[1] += iCounter.getTotalCount();
				});
		this.instructionsCovered = counter[0];
		this.instructionsTotal = counter[1];
	}

	//public CoverageBuilder getCoverageBuilder() {
    //	return this.coverageBuilder;
    //}

	public boolean isBetterThan(CoverageResult that) {
		if (that == null) {
			return true;
		}
		double percCoverageThis = ((double) this.instructionsCovered / (double) this.instructionsTotal);
		double percCoverageThat = ((double) that.instructionsCovered / (double) that.instructionsTotal);
		return percCoverageThis >= percCoverageThat;
	}

	public void save() {
		if (!new File("target/dspot").exists()) {
			try {
				Files.createDirectory(Paths.get("target/dspot/"));
			} catch (IOException ignored) {
				// it is not a big deal if there is an exeception
			}
		}
		try (FileOutputStream fout = new FileOutputStream("target/dspot/coverageResult.ser")) {
			try (ObjectOutputStream oos = new ObjectOutputStream(fout)) {
				oos.writeObject(this);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}


	public static CoverageResult load() {
		try (FileInputStream fin = new FileInputStream("target/dspot/coverageResult.ser");) {
			try (ObjectInputStream ois = new ObjectInputStream(fin);) {
				return (CoverageResult) ois.readObject();
			} catch (Exception ex) {
				throw new RuntimeException(ex);
			}
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}


}
