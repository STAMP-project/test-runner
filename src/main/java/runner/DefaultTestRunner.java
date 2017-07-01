package runner;

import listener.TestListener;
import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;

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
    public TestListener run(Map<String, Collection<String>> testMethodNamesForClasses) {
        return testMethodNamesForClasses.keySet().stream()
                .map(fullQualifiedName -> this.run(fullQualifiedName, testMethodNamesForClasses.get(fullQualifiedName)))
                .reduce(new TestListener(), TestListener::aggregate);
    }

    @Override
    public TestListener run(Collection<String> fullQualifiedNames) {
        return fullQualifiedNames.stream()
                .map(this::run)
                .reduce(new TestListener(), TestListener::aggregate);
    }

    @Override
    public TestListener run(String fullQualifiedName, Collection<String> testMethodNames) {
        TestListener listener = new TestListener();
        Request request = Request.aClass(this.loadClass(fullQualifiedName));
        request = request.filterWith(new Filter() {
            @Override
            public boolean shouldRun(Description description) {
                return (description.isTest() &&
                        testMethodNames.contains(description.getMethodName())) ||
                        description.getChildren().stream()
                                .map(this::shouldRun)
                                .reduce(Boolean.FALSE, Boolean::logicalOr);
            }
            @Override
            public String describe() {
                return "filter with name of test method";
            }
        });
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
