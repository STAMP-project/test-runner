package eu.stamp_project.testrunner.runner;

import eu.stamp_project.testrunner.EntryPoint;
import eu.stamp_project.testrunner.listener.junit4.JUnit4TestListener;
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
public class JUnit4Runner {

    public static final String BLACK_LIST_OPTION = "--blacklist";

    public static final String PATH_SEPARATOR = System.getProperty("path.separator");

    public static final String FILE_SEPARATOR = System.getProperty("file.separator");

    public static final transient Function<String, String> pathToFullQualifiedName = string ->
    {
        if (JUnit4Runner.FILE_SEPARATOR.equals("\\")) {
            return string.replace("\\", ".");
        } else {
            return string.replace(JUnit4Runner.FILE_SEPARATOR, ".");
        }
    };

    public static final transient Function<String, String> fullQualifiedNameToPath = (String string) ->
    {
        if (JUnit4Runner.FILE_SEPARATOR.equals("\\")) {
            return string.replace(".", "\\\\");
        } else {
            return string.replace(".", JUnit4Runner.FILE_SEPARATOR);
        }
    };

    public static void main(String[] args) {
        final JUnit4TestListener jUnit4TestListener = new JUnit4TestListener();
        final ParserOptions options = ParserOptions.parse(args);
        JUnit4Runner.run(
                options.getFullQualifiedNameOfTestClassesToRun(),
                options.getTestMethodNamesToRun(),
                options.getBlackList(),
                jUnit4TestListener,
                JUnit4Runner.class.getClassLoader()
        );
        jUnit4TestListener.save();
    }

    public static void run(String[] testClassNames,
                           String[] testMethodNames,
                           List<String> blackList,
                           JUnit4TestListener listener,
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
