package eu.stamp_project.testrunner.listener.impl;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 15/11/18
 */
public class CoveragePerTestMethodImpl implements CoveragePerTestMethod {

    private static final long serialVersionUID = 606642107403361456L;

    protected transient final Map<String, ExecutionDataStore> executionDataStoreMap;

    protected final ConcurrentHashMap<String, Coverage> coverageResultsMap;

    protected final List<String> classesDirectory;

    protected transient RuntimeData data;

    protected transient ExecutionDataStore executionData;

    protected transient SessionInfoStore sessionInfos;

    protected transient CoverageTransformer coverageTransformer;

    public CoveragePerTestMethodImpl(RuntimeData data, List<String> classesDirectory, CoverageTransformer coverageTransformer) {
        this.data = data;
        this.classesDirectory = classesDirectory;
        this.executionDataStoreMap = new HashMap<>();
        this.coverageResultsMap = new ConcurrentHashMap<>();
        this.coverageTransformer = coverageTransformer;
    }

    public List<String> getClassesDirectory() {
        return classesDirectory;
    }

    public RuntimeData getData() {
        return data;
    }

    public ExecutionDataStore getExecutionData() {
        return executionData;
    }

    public SessionInfoStore getSessionInfos() {
        return sessionInfos;
    }

    public CoverageTransformer getCoverageTransformer() {
        return coverageTransformer;
    }

    public void setData(RuntimeData data) {
        this.data = data;
    }

    public void setExecutionData(ExecutionDataStore executionData) {
        this.executionData = executionData;
    }

    public void setSessionInfos(SessionInfoStore sessionInfos) {
        this.sessionInfos = sessionInfos;
    }

    public Map<String, ExecutionDataStore> getExecutionDataStoreMap() {
        return executionDataStoreMap;
    }

    @Override
    public void computeCoverages() {
        executionDataStoreMap.entrySet().parallelStream()
                .forEach(x -> {
                    Coverage jUnit4Coverage = coverageTransformer.transformJacocoObject(
                            x.getValue(),
                            classesDirectory
                    );
                    coverageResultsMap.put(x.getKey(), jUnit4Coverage);
                });
    }


    @Override
    public Map<String, Coverage> getCoverageResultsMap() {
        return coverageResultsMap;
    }

    @Override
    public Coverage getCoverageOf(String testMethodName) {
        return this.getCoverageResultsMap().get(testMethodName);
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
     * @return loaded CoveragePerTestMethodImpl from the memory mapped file
     */
    public static CoveragePerTestMethodImpl load() {
        return ListenerUtils.loadFromMemoryMappedFile(ListenerUtils.computeTargetFilePath(OUTPUT_DIR, SHARED_MEMORY_FILE));
    }

    @Override
    public String toString() {
        return this.coverageResultsMap.keySet()
                        .stream()
                        .map(test -> "\t" + test + ": " + coverageResultsMap.get(test).toString())
                        .collect(Collectors.joining(ConstantsHelper.LINE_SEPARATOR));
    }
}
