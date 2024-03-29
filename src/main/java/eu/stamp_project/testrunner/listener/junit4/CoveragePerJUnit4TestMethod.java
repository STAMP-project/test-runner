package eu.stamp_project.testrunner.listener.junit4;

import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoveragePerTestMethod;
import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.impl.CoverageImpl;
import eu.stamp_project.testrunner.listener.impl.CoveragePerTestMethodImpl;
import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.ICounter;
import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.Description;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/04/18
 * <p>
 * This class represents the instruction coverage per test method.
 */
public class CoveragePerJUnit4TestMethod extends JUnit4TestResult implements CoveragePerTestMethod {

    private static final long serialVersionUID = 8360711686354566769L;

    private CoveragePerTestMethodImpl internalCoverage;

    /**
     * This field is used to support parametrized test
     * In fact, parametrized are reported as follow:
     * - the test method is named "test"
     * - it reports, for each parameter as follow: test[0], test[1].
     * This field stores every coverage, i.e. for each input.
     * Then, we are able to aggregate them to obtain the coverage of the test, for EVERY input.
     */
    private Map<String, List<IClassCoverage>> coveragesPerMethodName;

    public CoveragePerJUnit4TestMethod(RuntimeData data, List<String> classesDirectory, CoverageTransformer coverageTransformer) {
        this.internalCoverage = new CoveragePerTestMethodImpl(data, classesDirectory, coverageTransformer);
        this.coveragesPerMethodName = new HashMap<>();
    }

    @Override
    public synchronized void testStarted(Description description) throws Exception {
        this.internalCoverage.setExecutionData(new ExecutionDataStore());
        this.internalCoverage.setSessionInfos(new SessionInfoStore());
        this.internalCoverage.getData().setSessionId(this.toString.apply(description));
        this.internalCoverage.getData().collect(
                this.internalCoverage.getExecutionData(),
                this.internalCoverage.getSessionInfos(),
                true
        );
    }

    @Override
    public synchronized void testFinished(Description description) throws Exception {
        this.internalCoverage.getData().collect(
                this.internalCoverage.getExecutionData(),
                this.internalCoverage.getSessionInfos(),
                false
        );

        this.internalCoverage.getExecutionDataStoreMap().put(
                this.toString.apply(description),
                ListenerUtils.cloneExecutionDataStore(this.internalCoverage.getExecutionData())
        );

        if (isParametrized.test(ListenerUtils.getMethodName.apply(description))) {
            this.collectForParametrizedTest(this.toStringParametrized.apply(description));
        }
    }

    private void collectForParametrizedTest(String testMethodName) {
        final CoverageBuilder coverageBuilder = new CoverageBuilder();
        final Analyzer analyzer = new Analyzer(this.internalCoverage.getExecutionData(), coverageBuilder);
        try {
            for (String directory : this.internalCoverage.getClassesDirectory()) {
                analyzer.analyzeAll(new File(directory));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (!this.coveragesPerMethodName.containsKey(testMethodName)) {
            this.coveragesPerMethodName.put(testMethodName, new ArrayList<>());
        }
        coverageBuilder.getClasses()
                .forEach(classCoverage ->
                        this.coveragesPerMethodName.get(testMethodName)
                                .add(classCoverage)
                );
    }

    public Map<String, List<IClassCoverage>> getCoveragesPerMethodName() {
        return coveragesPerMethodName;
    }

    @Override
    public Map<String, Coverage> getCoverageResultsMap() {
        return this.internalCoverage.getCoverageResultsMap();
    }

    @Override
    public Coverage getCoverageOf(String testMethodName) {
        return this.internalCoverage.getCoverageOf(testMethodName);
    }

    @Override
    public void save() {
        if (!this.coveragesPerMethodName.isEmpty()) {
            this.aggregateParametrizedTestCoverage();
        }
        this.internalCoverage.save();
    }

    /*
        This method will use the coveragesPerMethodName map to create a coverage for the test method.
        This coverage is the aggregation of the coverages of each input for the same test method.
     */
    private void aggregateParametrizedTestCoverage() {
        this.coveragesPerMethodName.keySet().forEach(testMethodName -> {
                    int covered = 0;
                    int total = 0;
                    final ArrayList<IClassCoverage> classCoverages =
                            new ArrayList<>(this.coveragesPerMethodName.get(testMethodName));
                    while (!classCoverages.isEmpty()) {
                        final IClassCoverage current = classCoverages.get(0);
                        final List<IClassCoverage> subListOnSameClass =
                                getSameClassCoverage(current.getName(), classCoverages);
                        final List<List<Integer>> coveragePerMethods = subListOnSameClass.stream()
                                .map(coverage ->
                                        CoverageImpl.getListOfCountForCounterFunction(coverage, ICounter::getCoveredCount)
                                ).collect(Collectors.toList());
                        final List<Integer> bestCoverage = IntStream.range(0, coveragePerMethods.get(0).size())
                                .boxed()
                                .map(index ->
                                        coveragePerMethods.stream()
                                                .map(integers -> integers.get(index))
                                                .max(Integer::compareTo)
                                                .get()
                                ).collect(Collectors.toList());
                        covered += bestCoverage.stream().mapToInt(Integer::intValue).sum();
                        total += CoverageImpl.getListOfCountForCounterFunction(subListOnSameClass.get(0), ICounter::getTotalCount)
                                .stream()
                                .mapToInt(Integer::intValue)
                                .sum();
                        classCoverages.removeAll(subListOnSameClass);
                    }
                    final JUnit4Coverage value = new JUnit4Coverage(covered, total);
                    value.setExecutionPath(this.internalCoverage.getCoverageResultsMap().keySet()
                            .stream()
                            .map(this.internalCoverage.getCoverageResultsMap()::get)
                            .map(Coverage::getExecutionPath)
                            .collect(Collectors.joining("#"))
                    );
                    this.internalCoverage.getCoverageResultsMap().put(testMethodName, value);
                }
        );
    }

    /*
     * return the sublist of the given list which the coverage of the same class, pointed by the className
     */
    private List<IClassCoverage> getSameClassCoverage(String className, List<IClassCoverage> classCoverages) {
        return classCoverages.stream()
                .filter(iClassCoverage -> className.equals(iClassCoverage.getName()))
                .collect(Collectors.toList());
    }

    public void computeCoverages() {
        internalCoverage.computeCoverages();
    }

}
