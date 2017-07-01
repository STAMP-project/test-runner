package runner;

import listener.TestListener;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public interface TestRunner {

    TestListener run(Map<String, Collection<String>> testMethodNamesForClasses);

    TestListener run(Collection<String> fullQualifiedNames);

    TestListener run(String fullQualifiedName, Collection<String> testMethodNames);

    TestListener run(String fullQualifiedName, String testMethodName);

    TestListener run(String fullQualifiedName);

}
