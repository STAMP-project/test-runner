package eu.stamp_project.testrunner.runner;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.junit4.JUnit4TestResult;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;

import java.util.Arrays;
import java.util.List;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/12/17
 */
public class JUnit4Runner {

    /**
     * The entry method to execute junit tests.
     * This method is not meant to be used directly, but rather using {@link EntryPoint}
     * For the expected arguments, see {@link ParserOptions}
     */
    public static void main(String[] args) {
        final JUnit4TestResult jUnit4TestResult = new JUnit4TestResult();
        final ParserOptions options = ParserOptions.parse(args);
        JUnit4Runner.run(
                options.getFullQualifiedNameOfTestClassesToRun(),
                options.getTestMethodNamesToRun(),
                options.getBlackList(),
                jUnit4TestResult,
                JUnit4Runner.class.getClassLoader()
        );
        jUnit4TestResult.save();
        ConstantsHelper.exit();
    }

    /**
     * Execute the test
     * @param testClassNames full qualified names of the test classes to be run
     * @param testMethodNames simple names of the test methods to be run
     * @param blackList simple names of the test methods to NOT be run
     * @param listener the listener to record the result of the execution
     * @param customClassLoader the classloader that contains the classes to execute
     */
    public static void run(String[] testClassNames,
                           String[] testMethodNames,
                           List<String> blackList,
                           JUnit4TestResult listener,
                           ClassLoader customClassLoader) {
        Request request;
        request = Request.classes(Arrays.stream(testClassNames).map(testClassName -> {
            try {
                return customClassLoader.loadClass(testClassName);
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }).toArray(Class[]::new));
        final MethodFilter filter = new MethodFilter(Arrays.asList(testMethodNames), blackList);
        request = request.filterWith(filter);
        final Runner runner = request.getRunner();
        final RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(listener);
        runner.run(runNotifier);
    }
}
