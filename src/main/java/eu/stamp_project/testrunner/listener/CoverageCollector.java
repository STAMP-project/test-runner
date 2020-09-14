package eu.stamp_project.testrunner.listener;

import org.jacoco.core.data.ExecutionDataStore;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/11/18
 */
public interface CoverageCollector {

   
    public Coverage collectData(ExecutionDataStore executionData, String classesDirectory);
    
}
