package fr.inria.stamp.runner.coverage;

import fr.inria.stamp.runner.test.Loader;
import fr.inria.stamp.runner.test.TestListener;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.File;
import java.io.IOException;

public class Coverage extends TestListener {

    private int instructionsCovered;

    private int instructionsTotal;

    public int getInstructionsCovered() {
        return instructionsCovered;
    }

    public int getInstructionsTotal() {
        return instructionsTotal;
    }

    @Override
    protected String getSerializeName() {
        return "globalCoverageResult";
    }

    public void collectData(ExecutionDataStore executionData, String classesDirectory) {
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

    public static Coverage load() {
        return new Loader<Coverage>().load(new Coverage().getSerializeName());
    }

}
