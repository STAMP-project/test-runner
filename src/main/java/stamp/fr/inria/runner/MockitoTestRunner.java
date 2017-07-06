package stamp.fr.inria.runner;

import stamp.fr.inria.filter.MethodFilter;
import stamp.fr.inria.listener.TestListener;
import org.junit.runner.notification.RunNotifier;
import org.mockito.runners.MockitoJUnitRunner;

import java.lang.reflect.InvocationTargetException;
import java.util.Collection;

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
