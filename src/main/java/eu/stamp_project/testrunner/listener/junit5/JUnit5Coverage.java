package eu.stamp_project.testrunner.listener.junit5;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.TestCoveredResult;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;


import java.io.Serializable;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/11/18
 */
public class JUnit5Coverage extends JUnit5TestResult implements Coverage, Serializable, TestCoveredResult {

    private static final long serialVersionUID = -2873920196510405923L;

    private Coverage internalCoverage;

    public JUnit5Coverage() {
        this.internalCoverage = new CoverageImpl();
    }

    public JUnit5Coverage(int covered, int total) {
        this.internalCoverage = new CoverageImpl(covered, total);
    }
    
    public JUnit5Coverage(Coverage internalCoverage) {
        this.internalCoverage = internalCoverage;
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
    public boolean isBetterThan(Coverage that) {
        return this.internalCoverage.isBetterThan(that);
    }

    @Override
    public synchronized void save() {
        this.internalCoverage.save();
    }

    @Override
    public String toString() {
        return this.internalCoverage.toString();
    }
    
	
	@Override
	public void setCoverageInformation(Coverage coverage) {
		this.internalCoverage = coverage;		
	}

	@Override
	public Coverage getCoverageInformation() {
		return this.internalCoverage;
	}
	
}
