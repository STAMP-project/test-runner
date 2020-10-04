package eu.stamp_project.testrunner.listener.impl;

import java.util.HashMap;

/**
 * Utility class for {@link CoverageDetailed}
 * @author Matias Martinez
 */
public class CoverageInformation extends HashMap<String, CoverageFromClass> {

	private static final long serialVersionUID = 1L;

	public void putCoverageOfClass(String classname, CoverageFromClass cov) {
		this.put(classname, cov);
	}

	public CoverageFromClass getCoverageOfClass(String classname) {
		return this.get(classname);
	}
}
