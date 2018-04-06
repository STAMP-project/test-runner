package eu.stamp.project.testrunner.runner.coverage;

import eu.stamp.project.testrunner.runner.test.Loader;
import eu.stamp.project.testrunner.runner.test.TestListener;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.File;
import java.io.IOException;

/**
 * This class represents the instruction coverage of source.
 */
public class Coverage extends TestListener {

    private int instructionsCovered;

    private int instructionsTotal;

    public int getInstructionsCovered() {
        return instructionsCovered;
    }

    public int getInstructionsTotal() {
        return instructionsTotal;
    }

    /*public Coverage(int instructionsCovered, int instructionsTotal) {
        this.instructionsCovered = instructionsCovered;
        this.instructionsTotal = instructionsTotal;
    }*/

    @Override
    protected String getSerializeName() {
        return "globalCoverageResult";
    }

    void collectData(ExecutionDataStore executionData, String classesDirectory) {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        try {
            analyzer.analyzeAll(new File(classesDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    @Override
    public String toString() {
        return this.instructionsCovered + " / " + this.instructionsTotal;
    }

    /**
     * Load from serialized object
     * @return an Instance of Coverage loaded from a serialized file. The name of the file is returned by {@link #getSerializeName()}
     */
    public static Coverage load() {
        return new Loader<Coverage>().load(new Coverage().getSerializeName());
    }

}
