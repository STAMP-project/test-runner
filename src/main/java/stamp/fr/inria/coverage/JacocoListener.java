package stamp.fr.inria.coverage;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;
import org.jacoco.core.runtime.RuntimeData;
import org.junit.runner.Description;
import stamp.fr.inria.test.TestListener;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 31/07/17
 */
public class JacocoListener extends TestListener {

	private final Map<String, CoverageResult> coverageResultsMap;

	private final String classesDirectory;

	private RuntimeData data;

	private ExecutionDataStore executionData;

	private SessionInfoStore sessionInfos;

	public JacocoListener(RuntimeData data, String classesDirectory) {
		this.data = data;
		this.classesDirectory = classesDirectory;
		this.coverageResultsMap = new HashMap<>();
	}

	public Map<String, CoverageResult> getCoverageResultsMap() {
		return coverageResultsMap;
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
		final CoverageResult v = coverageResults(executionData);
		coverageResultsMap.put(description.getMethodName(), v);
	}

	private CoverageResult coverageResults(ExecutionDataStore executionData) {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);

		try {
			analyzer.analyzeAll(new File(classesDirectory));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		return new CoverageResult(coverageBuilder);
	}

}
