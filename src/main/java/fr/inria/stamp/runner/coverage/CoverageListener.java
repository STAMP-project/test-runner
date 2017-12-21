package fr.inria.stamp.runner.coverage;

import fr.inria.stamp.runner.test.TestListener;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.Description;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class CoverageListener extends TestListener {

    private final Map<String, List<Coverage>> instructionsCoveragePerLinePerTestCasesName;

    private transient final String classesDirectory;

    private transient RuntimeData data;

    private transient ExecutionDataStore executionData;

    private transient SessionInfoStore sessionInfos;

    public CoverageListener(RuntimeData data, String classesDirectory) {
        this.data = data;
        this.classesDirectory = classesDirectory;
        this.instructionsCoveragePerLinePerTestCasesName = new HashMap<>();
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
        data.setSessionId(description.getMethodName());
        data.collect(executionData, sessionInfos, false);
        instructionsCoveragePerLinePerTestCasesName.put(description.getMethodName(), convert(executionData));
    }

    private List<Coverage> convert(ExecutionDataStore executionData) {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
        try {
            analyzer.analyzeAll(new File(classesDirectory));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return coverageBuilder.getClasses().stream()
                .flatMap(iClassCoverage ->
                        IntStream.range(iClassCoverage.getFirstLine(), iClassCoverage.getLastLine())
                                .boxed()
                                .map(integer ->
                                        new Coverage(iClassCoverage.getName(),
                                                integer,
                                                iClassCoverage.getLine(integer)
                                                        .getInstructionCounter()
                                                        .getCoveredCount(),
                                                iClassCoverage.getLine(integer)
                                                        .getInstructionCounter()
                                                        .getTotalCount())
                                ).collect(Collectors.toList()).stream()
                ).collect(Collectors.toList());
    }

    @Override
    protected String getSerializeName() {
        return "coverageResult";
    }

    public static CoverageListener load() {
        try (FileInputStream fin = new FileInputStream("target/dspot/coverageResult.ser")) {
            try (ObjectInputStream ois = new ObjectInputStream(fin)) {
                return (CoverageListener) ois.readObject();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public Map<String, List<Coverage>> getInstructionsCoveragePerLinePerTestCasesName() {
        return instructionsCoveragePerLinePerTestCasesName;
    }

    public static boolean isIncreasingNumberOfInstructionExecuted(List<Coverage> objectToCompareTo, List<Coverage> that) {
        final List<String> executedClassesName = objectToCompareTo.stream()
                .map(coverage -> coverage.className)
                .collect(Collectors.toList());
        final List<Coverage> filteredThat = that.stream()
                .filter(coverage -> executedClassesName.contains(coverage.className))
                .collect(Collectors.toList());
        final List<String> executedClassesNameByThat = objectToCompareTo.stream()
                .map(coverage -> coverage.className)
                .collect(Collectors.toList());
        final List<Coverage> filteredObjectToCompare = objectToCompareTo.stream()
                .filter(coverage -> executedClassesNameByThat.contains(coverage.className))
                .collect(Collectors.toList());
        // should we verify that the sizes match?
        final Iterator<Coverage> iterator = filteredObjectToCompare.iterator();
        final Iterator<Coverage> iterator1 = filteredThat.iterator();
        while (iterator.hasNext()) {
            final Coverage next = iterator.next();
            final Coverage next1 = iterator1.next();
            if (next.instructionCovered > next1.instructionCovered) {
                return true;
            }
        }
        return false;
    }

    public class Coverage implements Serializable {

        public final String className;

        public final int lineNumber;

        public final int instructionCovered;

        public final int instructionTotal;

        public Coverage(String className, int lineNumber, int instructionCovered, int instructionTotal) {
            this.className = className;
            this.lineNumber = lineNumber;
            this.instructionCovered = instructionCovered;
            this.instructionTotal = instructionTotal;
        }
    }

}
