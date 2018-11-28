package eu.stamp_project.testrunner.runner;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.junit5.JUnit5TestListener;
import org.junit.platform.launcher.Launcher;
import org.junit.platform.launcher.LauncherDiscoveryRequest;
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;

import java.util.Arrays;
import java.util.List;

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
        final ParserOptions options = ParserOptions.parse(args);
        JUnit5Runner.run(
                options.getFullQualifiedNameOfTestClassesToRun(),
                options.getTestMethodNamesToRun(),
                options.getBlackList(),
                jUnit5TestListener,
                JUnit5Runner.class.getClassLoader()
        );
        jUnit5TestListener.save();
    }

    public static void run(String[] testClassNames,
                           String[] testMethodNames,
                           List<String> blackList, // TODO the blacklist is not yet implemented
                           JUnit5TestListener listener,
                           ClassLoader customClassLoader) {
        final LauncherDiscoveryRequestBuilder requestBuilder = LauncherDiscoveryRequestBuilder.request();
        if (testMethodNames.length == 0) {
            if (testClassNames.length > 0) {
                Arrays.asList(testClassNames).forEach(testClassName -> {
                            try {
                                requestBuilder.selectors(
                                        selectClass(customClassLoader.loadClass(testClassName))
                                );
                            } catch (ClassNotFoundException e) {
                                throw new RuntimeException(e);
                            }
                        }
                );
            } else {
                try {
                    requestBuilder.selectors(selectClass(customClassLoader.loadClass(testClassNames[0])));
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } else {
            Arrays.asList(testMethodNames).forEach(testMethodName -> {
                        try {
                            requestBuilder.selectors(selectMethod(customClassLoader.loadClass(testClassNames[0]), testMethodName));
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    }
            );
        }
        final LauncherDiscoveryRequest request = requestBuilder.build();
        final Launcher launcher = LauncherFactory.create();
        launcher.registerTestExecutionListeners(listener);
        launcher.execute(request);
    }
}
