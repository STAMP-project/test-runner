package eu.stamp_project.testrunner.runner.coverage;

import eu.stamp_project.testrunner.TestListener;
import eu.stamp_project.testrunner.runner.test.JUnit4TestListener;
import eu.stamp_project.testrunner.runner.test.Loader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.Description;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/04/18
 *
 * This class represents the instruction coverage per test method.
 */
public class CoveragePerJUnit4TestMethod extends JUnit4TestListener {

    private final Map<String, Coverage> coverageResultsMap;

    private final String classesDirectory;

    private transient RuntimeData data;

    private transient ExecutionDataStore executionData;

    private transient SessionInfoStore sessionInfos;

    private CoveragePerJUnit4TestMethod() {
        coverageResultsMap = null;
        classesDirectory = null;
    }

    public CoveragePerJUnit4TestMethod(RuntimeData data, String classesDirectory) {
        this.data = data;
        this.classesDirectory = classesDirectory;
        this.coverageResultsMap = new HashMap<>();
    }

    public Map<String, Coverage> getCoverageResultsMap() {
        return coverageResultsMap;
    }

    public Coverage getCoverageOf(String testMethodName) {
        return this.getCoverageResultsMap().get(testMethodName);
    }

    @Override
    public void testStarted(Description description) throws Exception {
        this.executionData = new ExecutionDataStore();
        this.sessionInfos = new SessionInfoStore();
        data.setSessionId(description.getMethodName());
        data.collect(executionData, sessionInfos, true);
    }

    @Override
    public void testFinished(Description description) throws Exception {
        data.collect(executionData, sessionInfos, false);
        final Coverage coverage = new Coverage();
        coverage.collectData(executionData, classesDirectory);
        coverageResultsMap.put(description.getMethodName(), coverage);
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
     * @return an Instance of CoveragePerJUnit4TestMethod loaded from a serialized file.
     */
    public static CoveragePerJUnit4TestMethod load() {
        return new Loader<CoveragePerJUnit4TestMethod>().load(SERIALIZE_NAME);
    }
}
