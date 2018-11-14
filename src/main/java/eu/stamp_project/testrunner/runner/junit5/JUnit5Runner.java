package eu.stamp_project.testrunner.runner.junit5;

import eu.stamp_project.testrunner.EntryPoint;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static eu.stamp_project.testrunner.runner.test.TestRunner.PATH_SEPARATOR;
import static eu.stamp_project.testrunner.runner.test.TestRunner.BLACK_LIST_OPTION;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectClass;
import static org.junit.platform.engine.discovery.DiscoverySelectors.selectMethod;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/11/18
 */
public class JUnit5Runner {

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
    public static void main(String args[]) throws ClassNotFoundException {
        final JUnit5TestListener jUnit5TestListener = new JUnit5TestListener();
        if (args[0].contains(PATH_SEPARATOR)) {
            JUnit5Runner.run(Arrays.asList(args[0].split(PATH_SEPARATOR)), Collections.emptyList(), jUnit5TestListener);
        } else {
            if (args.length > 1) {
                if (args[1].startsWith(BLACK_LIST_OPTION)) {
                    JUnit5Runner.run(Collections.singletonList(args[0]), Arrays.asList(args[2].split(PATH_SEPARATOR)), jUnit5TestListener);
                } else {
                    JUnit5Runner.run(args[0], Arrays.asList(args[1].split(PATH_SEPARATOR)), jUnit5TestListener);
                }
            } else {
                JUnit5Runner.run(Collections.singletonList(args[0]), Collections.emptyList(), jUnit5TestListener);
            }
        }
        jUnit5TestListener.save();
    }

    /**
     * Run all test methods of testClassNames
     *
     * @param testClassNames list of full qualified name of test classes
     * @param blackList      list of test methods name to not execute
     * @param listener       test listener to gather test result
     */
    public static void run(List<String> testClassNames, List<String> blackList, JUnit5TestListener listener) {
        run(testClassNames, blackList, listener, JUnit5Runner.class.getClassLoader());
    }

    /**
     * Run all test methods of testClassNames using custom class loader
     *
     * @param testClassNames    list of full qualified name of test classes
     * @param blackList         list of test methods name to not execute
     * @param listener          test listener to gather test result
     * @param customClassLoader this class loader should contains the .class to be executed and all the dependencies
     */
    // TODO the black list is not implemented
    public static void run(List<String> testClassNames, List<String> blackList, JUnit5TestListener listener, ClassLoader customClassLoader) {
        final LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request();
        testClassNames.forEach(testClassName -> {
                    try {
                        requestBuilder.selectors(
                                selectClass(customClassLoader.loadClass(testClassName))
                        );
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        final LauncherDiscoveryRequest request = requestBuilder.build();
        final Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
    }

    public static void run(String testClassName, JUnit5TestListener listener) {
        JUnit5Runner.run(testClassName, Collections.emptyList(), listener, eu.stamp_project.testrunner.runner.test.TestRunner.class.getClassLoader());
    }

    public static void run(String testClassName, List<String> testMethodNames, JUnit5TestListener listener) {
        JUnit5Runner.run(testClassName, testMethodNames, listener, eu.stamp_project.testrunner.runner.test.TestRunner.class.getClassLoader());
    }

    public static void run(String testClassName,
                           JUnit5TestListener listener,
                           ClassLoader customClassLoader) {
        JUnit5Runner.run(testClassName, Collections.emptyList(), listener, customClassLoader);
    }

    public static void run(String testClassName,
                           List<String> testMethodNames,
                           JUnit5TestListener listener,
                           ClassLoader customClassLoader) {
        final LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request();
        testMethodNames.forEach(testMethodName -> {
                    try {
                        requestBuilder.selectors(selectMethod(customClassLoader.loadClass(testClassName), testMethodName));
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }
        );
        final LauncherDiscoveryRequest request = requestBuilder.build();
        final Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
    }
}
