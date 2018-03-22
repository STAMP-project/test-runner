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
