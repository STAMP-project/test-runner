package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.TestListener;
import eu.stamp_project.testrunner.runner.Loader;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.jacoco.core.analysis.*;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.*;
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

    protected static final transient Function<CoverageBuilder, String> computePathExecuted = coverageBuilder ->
            coverageBuilder.getClasses()
                    .stream()
                    .map(iClassCoverage ->
                            ConstantsHelper.pathToFullQualifiedName.apply(iClassCoverage.getName()) + ConstantsHelper.PATH_SEPARATOR  +
                                    IntStream.range(iClassCoverage.getFirstLine(), iClassCoverage.getLastLine())
                                            .mapToObj(iClassCoverage::getLine)
                                            .map(ILine::getInstructionCounter)
                                            .map(ICounter::getCoveredCount)
                                            .map(Object::toString)
                                            .collect(Collectors.joining(","))
                    ).collect(Collectors.joining(";"));

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

        this.executionPath = computePathExecuted.apply(coverageBuilder);
        this.instructionsCovered = counter[0];
        this.instructionsTotal = counter[1];
    }

    @Override
    public String toString() {
        return this.instructionsCovered + " / " + this.instructionsTotal;
    }

    @Override
    public void save() {
        File outputDir = new File(TestListener.OUTPUT_DIR);
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
     * @return an Instance of JUnit4Coverage loaded from a serialized file.
     */
    public static Coverage load() {
        return new Loader<Coverage>().load(SERIALIZE_NAME);
    }

}
