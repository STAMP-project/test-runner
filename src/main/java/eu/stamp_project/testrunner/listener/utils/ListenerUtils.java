package eu.stamp_project.testrunner.listener.utils;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataStore;

public class ListenerUtils {

	/**
	 * Clones each result so it doesn't get changed by the runtime afterwards
	 * @param original the original store
	 * @return a clone of the original store
	 */
	public static ExecutionDataStore cloneExecutionDataStore(ExecutionDataStore original) {
		ExecutionDataStore cloned = new ExecutionDataStore();
		original.getContents().stream().forEach(x -> {
			ExecutionData executionData = new ExecutionData(x.getId(), x.getName(), x.getProbes().clone());
			synchronized (cloned) {
				cloned.put(executionData);
			}
		});
		return cloned;
	}

}