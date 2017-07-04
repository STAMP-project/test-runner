package runner;

import filter.MethodFilter;
import listener.TestListener;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
public class DefaultTestRunner extends AbstractTestRunner {

	public DefaultTestRunner(String[] classpath) {
		super(classpath);
	}

	@Override
	public TestListener run(String fullQualifiedName, Collection<String> testMethodNames) {
		TestListener listener = new TestListener();
		Request request = Request.aClass(this.loadClass(fullQualifiedName));
		request = request.filterWith(new MethodFilter(testMethodNames));
		Runner runner = request.getRunner();
		RunNotifier runNotifier = new RunNotifier();
		runNotifier.addFirstListener(listener);
		runner.run(runNotifier);
		return listener;
	}

	@Override
	public TestListener run(String fullQualifiedName, String testMethodName) {
		TestListener listener = new TestListener();
		Request request = Request.method(this.loadClass(fullQualifiedName), testMethodName);
		Runner runner = request.getRunner();
		RunNotifier runNotifier = new RunNotifier();
		runNotifier.addFirstListener(listener);
		runner.run(runNotifier);
		return listener;
	}

	@Override
	public TestListener run(String fullQualifiedName) {
		TestListener listener = new TestListener();
		Request request = Request.classes(this.loadClass(fullQualifiedName));
		Runner runner = request.getRunner();
		RunNotifier runNotifier = new RunNotifier();
		runNotifier.addFirstListener(listener);
		runner.run(runNotifier);
		return listener;
	}
}
