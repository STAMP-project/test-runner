package eu.stamp_project.testrunner.listener;

/**
 * Represents the results of a test that has been instrumented for computing the coverage
 */
public interface CoveredTestResult extends TestResult, Coverage {

  public void setCoverageInformation(Coverage coverage);
  
  public Coverage getCoverageInformation();

}
