package eu.stamp_project.testrunner.listener.junit4;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import org.jacoco.core.data.ExecutionDataStore;

import java.io.Serializable;

/**
 * This class represents the instruction coverage of source.
 */
public class JUnit4Coverage extends JUnit4TestResult implements Coverage, Serializable {

    private static final long serialVersionUID = 109548359596802378L;

    private Coverage internalCoverage;

    public JUnit4Coverage() {
        this.internalCoverage = new CoverageImpl();
    }

    public JUnit4Coverage(int covered, int total) {
        this.internalCoverage = new CoverageImpl(covered, total);
    }

    @Override
    public void setExecutionPath(String executionPath) {
        this.internalCoverage.setExecutionPath(executionPath);
    }

    @Override
    public int getInstructionsCovered() {
        return this.internalCoverage.getInstructionsCovered();
    }

    @Override
    public int getInstructionsTotal() {
        return this.internalCoverage.getInstructionsTotal();
    }

    @Override
    public String getExecutionPath() {
        return this.internalCoverage.getExecutionPath();
    }

    @Override
    public void collectData(ExecutionDataStore executionData, String classesDirectory) {
        this.internalCoverage.collectData(executionData, classesDirectory);
    }

    @Override
    public boolean isBetterThan(Coverage that) {
        return this.internalCoverage.isBetterThan(that);
    }

    @Override
    public void save() {
        this.internalCoverage.save();
    }

    @Override
    public String toString() {
        return this.internalCoverage.toString();
    }

     @Override
    public boolean equals(Object that) {
        return that instanceof JUnit4Coverage &&
                ((JUnit4Coverage) that).getInstructionsCovered() == this.getInstructionsCovered() &&
                ((JUnit4Coverage) that).getInstructionsTotal() == this.getInstructionsTotal();
     }
}
