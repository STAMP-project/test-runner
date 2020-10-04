package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.Loader;
import org.jacoco.core.analysis.*;
import java.io.*;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/11/18
 */
public class CoverageImpl implements Coverage, Serializable {

    private static final long serialVersionUID = 109548359596802378L;

    protected int instructionsCovered;

    protected int instructionsTotal;

    protected String executionPath;

    public CoverageImpl() {
        // empty
    }

    public CoverageImpl(int covered, int total) {
        this.instructionsCovered = covered;
        this.instructionsTotal = total;
        this.executionPath = "";
    }

	public CoverageImpl(int covered, int total, String executionPath) {
		this.instructionsCovered = covered;
		this.instructionsTotal = total;
		this.executionPath = executionPath;
	}

    @Override
    public void setExecutionPath(String executionPath) {
        this.executionPath = executionPath;
    }

    @Override
    public int getInstructionsCovered() {
        return instructionsCovered;
    }

    @Override
    public int getInstructionsTotal() {
        return instructionsTotal;
    }

    @Override
    public String getExecutionPath() {
        return executionPath;
    }

    public static List<Integer> getListOfCountForCounterFunction(IClassCoverage coverage,
                                                                 Function<ICounter, Integer> counterGetter) {
        return coverage.getMethods()
                .stream()
                .filter(iMethodCoverage -> !"<clinit>".equals(iMethodCoverage.getName()))
                .flatMap(iMethodCoverage ->
                        IntStream.range(iMethodCoverage.getFirstLine(), iMethodCoverage.getLastLine() + 1)
                                .mapToObj(iMethodCoverage::getLine)
                                .map(ILine::getInstructionCounter)
                                .map(counterGetter)
                ).collect(Collectors.toList());
    }

    @Override
    public boolean isBetterThan(Coverage that) {
        if (that == null) {
            return true;
        }
        double percCoverageThis = ((double) this.instructionsCovered / (double) this.instructionsTotal);
        double percCoverageThat = ((double) that.getInstructionsCovered() / (double) that.getInstructionsTotal());
        return (!this.executionPath.equals(that.getExecutionPath())) && percCoverageThis >= percCoverageThat;
    }

    @Override
    public String toString() {
        return this.instructionsCovered + " / " + this.instructionsTotal;
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

}
