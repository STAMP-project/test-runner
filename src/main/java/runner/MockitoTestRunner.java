package runner;

import listener.TestListener;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/07/17.
 */
public class MockitoTestRunner extends  AbstractTestRunner {

    public MockitoTestRunner(String classpath) {
        super(classpath);
    }

    public MockitoTestRunner(String[] classpath) {
        super(classpath);
    }

    @Override
    public TestListener run(Map<String, Collection<String>> testMethodNamesForClasses) {
        return null;
    }

    @Override
    public TestListener run(Collection<String> fullQualifiedNames) {
        return null;
    }

    @Override
    public TestListener run(String fullQualifiedName, Collection<String> testMethodNames) {
        return null;
    }

    @Override
    public TestListener run(String fullQualifiedName, String testMethodName) {
        return null;
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
