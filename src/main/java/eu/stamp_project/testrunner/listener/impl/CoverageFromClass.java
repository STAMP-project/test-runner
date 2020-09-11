package eu.stamp_project.testrunner.listener.impl;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the coverage per line from a class
 * @author Matias Martinez
 *
 */
public class CoverageFromClass {

	private String classname;
	private String packageName;
	private int init;
	private int end;

	private	Map<Integer, Integer> covered = new HashMap<>();

	public CoverageFromClass(String classname, String packageName, int init, int end, Map<Integer, Integer> cov) {
		super();
		this.classname = classname;
		this.packageName = packageName;
		this.init = init;
		this.end = end;
		this.covered = cov;
	}

	public int getInit() {
		return init;
	}

	public void setInit(int init) {
		this.init = init;
	}

	public int getEnd() {
		return end;
	}

	public void setEnd(int end) {
		this.end = end;
	}

	public Map<Integer, Integer> getCov() {
		return covered;
	}

	public void setCov(Map<Integer, Integer> cov) {
		this.covered = cov;
	}

	@Override
	public String toString() {
		return "Lines covered [init=" + init + ", end=" + end + ", cov=" + covered + "]";
	}

	public String getClassname() {
		return classname;
	}

	public void setClassname(String classname) {
		this.classname = classname;
	}

	public String getPackageName() {
		return packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Map<Integer, Integer> getCovered() {
		return covered;
	}

	public void setCovered(Map<Integer, Integer> covered) {
		this.covered = covered;
	}

}
