package eu.stamp.project.testrunner.runner.test;

import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/12/17
 */
public class TestRunner {

    /**
     * The entry method to execute junit tests.
     * This method is not meant to be used directly, but rather using {@link eu.stamp.project.testrunner.EntryPoint}
     * @param args this array should be build by {@link eu.stamp.project.testrunner.EntryPoint}.
     *             the first argument is the full qualified name of the test class
     *             the second argument is optionally the list of the test method name separated by ":".
     * @throws ClassNotFoundException in case of the supplied classpath is wrong
     */
    public static void main(String[] args) throws ClassNotFoundException {
        final TestListener testListener = new TestListener();
        if (args[0].contains(":")) {
            TestRunner.run(Arrays.asList(args[0].split(":")), testListener);
        } else {
            TestRunner.run(args[0],
                    args.length > 1 ? Arrays.asList(args[1].split(":")) : Collections.emptyList(),
                    testListener
            );
        }
        testListener.save();
    }

    public static void run(List<String> testClassNames, TestListener listener) {
        TestRunner.run(testClassNames, listener, TestRunner.class.getClassLoader());
    }

    public static void run(List<String> testClassNames, TestListener listener, ClassLoader customClassLoader) {
        Request request;
        request = Request.classes(testClassNames.stream().map(testClassName -> {
            try {
                return customClassLoader.loadClass(testClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toArray(Class[]::new));
        final Runner runner = request.getRunner();
        final RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(listener);
        runner.run(runNotifier);
    }

    public static void run(String testClassName, TestListener listener) {
        TestRunner.run(testClassName, Collections.emptyList(), listener, TestRunner.class.getClassLoader());
    }

    public static void run(String testClassName, List<String> testMethodNames, TestListener listener) {
        TestRunner.run(testClassName, testMethodNames, listener, TestRunner.class.getClassLoader());
    }

    public static void run(String testClassName,
                           TestListener listener,
                           ClassLoader customClassLoader) {
        TestRunner.run(testClassName, Collections.emptyList(), listener, customClassLoader);
    }

    public static void run(String testClassName,
                           List<String> testMethodNames,
                           TestListener listener,
                           ClassLoader customClassLoader) {
        Request request;
        try {
            request = Request.aClass(customClassLoader.loadClass(testClassName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        request = request.filterWith(new MethodFilter(testMethodNames));
        final Runner runner = request.getRunner();
        final RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(listener);
        runner.run(runNotifier);
    }

}
