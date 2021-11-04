package eu.stamp_project.testrunner.runner;

import eu.stamp_project.testrunner.listener.utils.ListenerUtils;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.Pattern;

class MethodFilter extends Filter {

    private Collection<String> testMethodNames;
    private Collection<String> blackList;

    public MethodFilter(Collection<String> testMethodNames) {
        this.testMethodNames = testMethodNames;
        this.blackList = Collections.emptyList();
    }

    public MethodFilter(Collection<String> testMethodNames, Collection<String> blackListMethodNames) {
        this.testMethodNames = testMethodNames;
        this.blackList = blackListMethodNames;
    }

    @Override
    public boolean shouldRun(Description description) {
        if (description.isSuite()) {
            return this.anyChildrenMatch.test(description);
        } else {
            final String methodName = ListenerUtils.getMethodName.apply(description);
            if (this.blackList.contains(methodName)) {
                return false;
            } else {
                return description.isTest() && (this.anyTestMethodNamesMatch.test(description) || testMethodNames.isEmpty());
            }
        }
    }

    private final Predicate<Description> anyChildrenMatch = description ->
            description.getChildren().stream()
                    .map(this::shouldRun)
                    .reduce(Boolean.FALSE, Boolean::logicalOr);

    private final Predicate<Description> anyTestMethodNamesMatch = description ->
            this.testMethodNames.stream()
                    .anyMatch(testMethodName ->
                            Pattern.compile("(" + ListenerUtils.getClassName.apply(description) + ")?" + testMethodName + "\\[(\\d+)\\]")
                                    .matcher(ListenerUtils.getMethodName.apply(description))
                                    .find()
                    ) ||
                    this.testMethodNames.contains(ListenerUtils.getMethodName.apply(description)) ||
                    this.testMethodNames.contains(ListenerUtils.getClassName.apply(description) + "#" + ListenerUtils.getMethodName.apply(description));

    @Override
    public String describe() {
        return "Filter test methods according their simple name: " + this.testMethodNames.toString();
    }
}
