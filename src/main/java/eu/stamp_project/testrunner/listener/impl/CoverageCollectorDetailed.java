package eu.stamp_project.testrunner.listener.impl;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IClassCoverage;
import org.jacoco.core.analysis.IMethodCoverage;
import org.jacoco.core.data.ExecutionDataStore;
import eu.stamp_project.testrunner.listener.Coverage;
import eu.stamp_project.testrunner.listener.CoverageTransformer;

/**
 * One implementation of {@link CoverageTransformer} that returns a {@link CoverageDetailed}.
 */
public class CoverageCollectorDetailed implements CoverageTransformer {

	private static final long serialVersionUID = 109548359596802378L;

	@Override
	public CoverageDetailed transformJacocoObject(ExecutionDataStore executionData, String classesDirectory) {

	    CoverageInformation covered = new CoverageInformation();
		
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		try {
			//TODO: change the interface to an array of URL
			String[] paths = classesDirectory.split(File.pathSeparator);
			for (String path : paths) {
				analyzer.analyzeAll(new File(path));
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		for (IClassCoverage classCoverage : coverageBuilder.getClasses()) {

			Map<Integer, Integer> covClass = new HashMap<>();

			for (IMethodCoverage methodCoverage : classCoverage.getMethods()) {

				if (!"<clinit>".equals(methodCoverage.getName())) {

					for (int i = methodCoverage.getFirstLine(); i <= methodCoverage.getLastLine() + 1; i++) {
						int coveredI = methodCoverage.getLine(i).getInstructionCounter().getCoveredCount();
						covClass.put(i, coveredI);
					}

				}
			}
			CoverageFromClass l = new CoverageFromClass(classCoverage.getName(), classCoverage.getPackageName(),
					classCoverage.getFirstLine(), classCoverage.getLastLine(), covClass);

			covered.put(classCoverage.getName(), l);

		}
		return new CoverageDetailed(covered);
	}



}
