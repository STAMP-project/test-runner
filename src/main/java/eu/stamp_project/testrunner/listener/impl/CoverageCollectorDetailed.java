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
import eu.stamp_project.testrunner.listener.CoverageCollector;

/**
 * 
 */
public class CoverageCollectorDetailed implements CoverageCollector {

	private static final long serialVersionUID = 109548359596802378L;


	public CoverageCollectorDetailed() {

	}


	@Override
	public Coverage collectData(ExecutionDataStore executionData, String classesDirectory) {

	    CoverageInformation covered = new CoverageInformation();
		
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionData, coverageBuilder);
		try {
			analyzer.analyzeAll(new File(classesDirectory));
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
		Coverage coverage = new CoverageDetailed(covered);
		return coverage;
	}



}
