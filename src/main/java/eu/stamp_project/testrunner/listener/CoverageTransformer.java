package eu.stamp_project.testrunner.listener;

import org.jacoco.core.data.ExecutionDataStore;

/**
 * Something that is able to receive a ExecutionDataStore from Jacoco
 * and transform it into a {@link Coverage} object.
 */
public interface CoverageTransformer {
    public Coverage transformJacocoObject(ExecutionDataStore executionData, String classesDirectory);
    
}
