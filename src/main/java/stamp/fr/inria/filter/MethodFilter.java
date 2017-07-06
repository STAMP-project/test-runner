package stamp.fr.inria.filter;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.Collection;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 04/07/17
 */
public class MethodFilter extends Filter {

	private Collection<String> testMethodNames;

	public MethodFilter(Collection<String> testMethodNames) {
		this.testMethodNames = testMethodNames;
	}

	@Override
	public boolean shouldRun(Description description) {
		return (description.isTest() &&
					(description.getMethodName().contains("[") &&
						testMethodNames.stream()
								.anyMatch(testMethodName -> description.getMethodName().startsWith(testMethodName))
					) || testMethodNames.contains(description.getMethodName())
				) ||
				description.getChildren().stream()
						.map(this::shouldRun)
						.reduce(Boolean.FALSE, Boolean::logicalOr);
	}

	@Override
	public String describe() {
		return "stamp.fr.inria.filter with name of test method";
	}
}