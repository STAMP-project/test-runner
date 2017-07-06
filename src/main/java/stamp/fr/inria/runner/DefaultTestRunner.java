package stamp.fr.inria.runner;

import stamp.fr.inria.filter.MethodFilter;
import stamp.fr.inria.listener.TestListener;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.util.Collection;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/06/17
 */
public class DefaultTestRunner extends AbstractTestRunner {

	public DefaultTestRunner(String classpath) {
		super(classpath);
	}

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
