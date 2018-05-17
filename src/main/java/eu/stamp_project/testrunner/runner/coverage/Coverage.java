package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.runner.test.Loader;
import eu.stamp_project.testrunner.runner.test.TestListener;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.analysis.ILine;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.File;
import java.io.IOException;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * This class represents the instruction coverage of source.
 */
public class Coverage extends TestListener {

    private int instructionsCovered;

    private int instructionsTotal;

    private String executionPath;

    public int getInstructionsCovered() {
        return instructionsCovered;
    }

    public int getInstructionsTotal() {
        return instructionsTotal;
    }

    private static final transient Function<CoverageBuilder, String> computePathExecuted = coverageBuilder ->
            coverageBuilder.getClasses()
                    .stream()
                    .map(iClassCoverage ->
                            iClassCoverage.getName().replaceAll("/", ".") + ":" +
                                    IntStream.range(iClassCoverage.getFirstLine(), iClassCoverage.getLastLine())
                                            .mapToObj(iClassCoverage::getLine)
                                            .map(ILine::getInstructionCounter)
                                            .map(ICounter::getCoveredCount)
                                            .map(Object::toString)
                                            .collect(Collectors.joining(","))
                    ).collect(Collectors.joining(";"));

    public String getExecutionPath() {
        return executionPath;
    }

    public boolean isBetterThan(Coverage that) {
        if (that == null) {
            return true;
        }
        double percCoverageThis = ((double) this.instructionsCovered / (double) this.instructionsTotal);
        double percCoverageThat = ((double) that.instructionsCovered / (double) that.instructionsTotal);
        return (!this.executionPath.equals(that.executionPath)) && percCoverageThis >= percCoverageThat;
    }

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

        this.executionPath = Coverage.computePathExecuted.apply(coverageBuilder);
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
