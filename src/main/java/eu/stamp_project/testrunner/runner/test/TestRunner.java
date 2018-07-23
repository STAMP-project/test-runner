package eu.stamp_project.testrunner.runner.test;

import eu.stamp_project.testrunner.EntryPoint;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/12/17
 */
public class TestRunner {

    public static final String BLACK_LIST_OPTION = "--blacklist";

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final transient Function<String, String> pathToFullQualifiedName = string ->
    {
        if (TestRunner.FILE_SEPARATOR.equals("\\")) {
            return string.replace("\\\\", ".");
        } else {
            return string.replace(TestRunner.FILE_SEPARATOR, ".");
        }
    };

    /**
     * The entry method to execute junit tests.
     * This method is not meant to be used directly, but rather using {@link EntryPoint}
     *
     * @param args this array should be build by {@link EntryPoint}.
     *             the first argument is the full qualified name of the test class
     *             the second argument is optionally the list of the test method name separated by the path separator of the system, <i>e.g.</i> ':' on Linux.
     *             You can pass the --blacklist flag, following by a list of test method name to be blacklisted.
     *             Each method name is separated with the path separator of the system, <i>e.g.</i> ':' on Linux.
     * @throws ClassNotFoundException in case of the supplied classpath is wrong
     */
    public static void main(String[] args) throws ClassNotFoundException {
        final TestListener testListener = new TestListener();
        if (args[0].contains(PATH_SEPARATOR )) {
            TestRunner.run(Arrays.asList(args[0].split(PATH_SEPARATOR)), Collections.emptyList(), testListener);
        } else {
            if (args.length > 1) {
                if (args[1].startsWith(BLACK_LIST_OPTION)) {
                    TestRunner.run(Collections.singletonList(args[0]), Arrays.asList(args[2].split(PATH_SEPARATOR )), testListener);
                } else {
                    TestRunner.run(args[0], Arrays.asList(args[1].split(PATH_SEPARATOR )), testListener);
                }
            } else {
                TestRunner.run(Collections.singletonList(args[0]), Collections.emptyList(), testListener);
            }
        }
        testListener.save();
    }

    /**
     * Run all test methods of testClassNames
     *
     * @param testClassNames list of full qualified name of test classes
     * @param blackList list of test methods name to not execute
     * @param listener test listener to gather test result
     */
    public static void run(List<String> testClassNames, List<String> blackList, TestListener listener) {
        TestRunner.run(testClassNames, blackList, listener, TestRunner.class.getClassLoader());
    }

    /**
     * Run all test methods of testClassNames using custom class loader
     *
     * @param testClassNames list of full qualified name of test classes
     * @param blackList list of test methods name to not execute
     * @param listener test listener to gather test result
     * @param customClassLoader this class loader should contains the .class to be executed and all the dependencies
     */
    public static void run(List<String> testClassNames, List<String> blackList, TestListener listener, ClassLoader customClassLoader) {
        Request request;
        request = Request.classes(testClassNames.stream().map(testClassName -> {
            try {
                return customClassLoader.loadClass(testClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toArray(Class[]::new));
        final MethodFilter filter = new MethodFilter(Collections.emptyList(), blackList);
        request = request.filterWith(filter);
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
        final MethodFilter filter = new MethodFilter(testMethodNames);
        request = request.filterWith(filter);
        final Runner runner = request.getRunner();
        final RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(listener);
        runner.run(runNotifier);
    }

}
