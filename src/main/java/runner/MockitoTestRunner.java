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
import java.util.Collections;
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
    public TestListener run(String fullQualifiedName, Collection<String> testMethodNames) {
        try {
            TestListener listener = new TestListener();
            MockitoJUnitRunner runner = new MockitoJUnitRunner(this.loadClass(fullQualifiedName));
            runner.filter(new MethodFilter(testMethodNames));
            RunNotifier runNotifier = new RunNotifier();
            runNotifier.addFirstListener(listener);
            runner.run(runNotifier);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public TestListener run(String fullQualifiedName, String testMethodName) {
        try {
            TestListener listener = new TestListener();
            MockitoJUnitRunner runner = new MockitoJUnitRunner(this.loadClass(fullQualifiedName));
            runner.filter(new MethodFilter(Collections.singletonList(testMethodName)));
            RunNotifier runNotifier = new RunNotifier();
            runNotifier.addFirstListener(listener);
            runner.run(runNotifier);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public TestListener run(String fullQualifiedName) {
        try {
            TestListener listener = new TestListener();
            MockitoJUnitRunner runner = new MockitoJUnitRunner(this.loadClass(fullQualifiedName));
            RunNotifier runNotifier = new RunNotifier();
            runNotifier.addFirstListener(listener);
            runner.run(runNotifier);
            return listener;
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
