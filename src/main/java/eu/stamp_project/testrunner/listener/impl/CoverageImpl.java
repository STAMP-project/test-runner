package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;

import java.io.File;
import java.io.Serializable;
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

    /**
     * Writes the serialized object to a memory mapped file.
     * The location depends on the workspace set for the test runner process.
     */
    @Override
    public void save() {
        ListenerUtils.saveToMemoryMappedFile(new File(OUTPUT_DIR, SHARED_MEMORY_FILE), this);
    }

    /**
     * Loads and deserializes the file from a memory mapped file
     *
     * @return loaded Coverage from the memory mapped file
     */
    public static Coverage load() {
        return ListenerUtils.loadFromMemoryMappedFile(ListenerUtils.computeTargetFilePath(OUTPUT_DIR, SHARED_MEMORY_FILE));
    }

}
