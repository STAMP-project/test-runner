package eu.stamp_project.testrunner.runner;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.junit5.JUnit5TestResult;
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
     * For the expected arguments, see {@link ParserOptions}
     */
    public static void main(String args[]) {
        final JUnit5TestResult jUnit5TestResult = new JUnit5TestResult();
        final ParserOptions options = ParserOptions.parse(args);
        JUnit5Runner.run(
                options.getFullQualifiedNameOfTestClassesToRun(),
                options.getTestMethodNamesToRun(),
                options.getBlackList(),
                jUnit5TestResult,
                JUnit5Runner.class.getClassLoader()
        );
        jUnit5TestResult.save();
    }

    /**
     * Execute the test
     * @param testClassNames full qualified names of the test classes to be run
     * @param testMethodNames simple names of the test methods to be run
     * @param blackList simple names of the test methods to NOT be run
     * @param listener JUnit5 listener to record the result of the execution
     * @param customClassLoader the classloader that contains the classes to execute
     */
    public static void run(String[] testClassNames,
                           String[] testMethodNames,
                           List<String> blackList, // TODO the blacklist is not yet implemented
                           JUnit5TestResult listener,
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
                            // If there is no class name in the method name, we try the first class
                            if (!testMethodName.contains("#")) {
                                requestBuilder.selectors(selectMethod(customClassLoader.loadClass(testClassNames[0]), testMethodName));
                            } else {
                                // Else we load the fully qualified method name
                                requestBuilder.selectors(selectMethod(testMethodName));
                            }
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
