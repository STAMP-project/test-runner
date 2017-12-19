package stamp.fr.inria.test;

import org.junit.runner.Description;
import org.junit.runner.Request;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.notification.RunNotifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 14/12/17
 */
public class TestRunner {

    public static void main(String[] args) throws ClassNotFoundException {
        final ClassLoader classLoader = TestRunner.class.getClassLoader();
        final TestListener testListener = new TestListener();
        TestRunner.run(args[0],
                args.length > 1 ? Arrays.asList(args[1].split(":")) : null,
                testListener
        );
        testListener.save();
        System.out.println(testListener.getPassingTests());
    }

    public static void run(String testClassName, List<String> testMethodNames, TestListener listener) {
        final ClassLoader classLoader = TestRunner.class.getClassLoader();
        Request request = null;
        try {
            request = Request.aClass(classLoader.loadClass(testClassName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (testMethodNames != null) {
            request = request.filterWith(new MethodFilter(testMethodNames));
        }
        final Runner runner = request.getRunner();
        final RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(listener);
        runner.run(runNotifier);
    }

    public static void run(String testClassName,
                           List<String> testMethodNames,
                           TestListener listener,
                           ClassLoader customClassLoader) {
        Request request = null;
        try {
            request = Request.aClass(customClassLoader.loadClass(testClassName));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
        if (testMethodNames != null) {
            request = request.filterWith(new MethodFilter(testMethodNames));
        }
        final Runner runner = request.getRunner();
        final RunNotifier runNotifier = new RunNotifier();
        runNotifier.addFirstListener(listener);
        runner.run(runNotifier);
    }

    private static class MethodFilter extends Filter {

        private Collection<String> testMethodNames;

        public MethodFilter(Collection<String> testMethodNames) {
            this.testMethodNames = testMethodNames;
        }

        @Override
        public boolean shouldRun(Description description) {
            return (description.isTest() &&
                    testMethodNames.stream().anyMatch(testMethodName ->
                            Pattern.compile(testMethodName + "\\[\\d:(.*?)\\]").matcher(description.getMethodName()).find()
                    ) || testMethodNames.contains(description.getMethodName())
            ) ||
                    description.getChildren().stream()
                            .map(this::shouldRun)
                            .reduce(Boolean.FALSE, Boolean::logicalOr);
        }

        @Override
        public String describe() {
            return "stamp.fr.inria.filter with name of test method";
        }
    }

}
