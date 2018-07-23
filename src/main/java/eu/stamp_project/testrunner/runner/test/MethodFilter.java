package eu.stamp_project.testrunner.runner.test;

import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

import java.util.Collection;
import java.util.Collections;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

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

    public static String getMethodName(Description description) {
        try {
            return description.getMethodName();
        } catch (NoSuchMethodError error) {
            return description.getDisplayName().split("\\(")[0];
        }
    }

    @Override
    public boolean shouldRun(Description description) {
        return !this.blackList.contains(getMethodName(description)) &&
                (
                        (description.isTest() &&
                        this.anyTestMethodNamesMatch.test(description) ||
                        testMethodNames.isEmpty()) ||
                        this.anyChildrenMatch.test(description)
                );
    }

    private final Predicate<Description> anyChildrenMatch = description ->
            description.getChildren().stream()
                    .map(this::shouldRun)
                    .reduce(Boolean.FALSE, Boolean::logicalOr);

    private final Predicate<Description> anyTestMethodNamesMatch = description ->
            this.testMethodNames.stream().anyMatch(testMethodName ->
                    Pattern.compile(testMethodName + "\\[\\d:(.*?)\\]")
                            .matcher(description.getMethodName())
                            .find()
            ) || this.testMethodNames.contains(getMethodName(description));

    @Override
    public String describe() {
        return "Filter test methods according their simple name: " + this.testMethodNames.toString();
    }
}
