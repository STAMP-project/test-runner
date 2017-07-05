package stamp.fr.inria.runner;

import stamp.fr.inria.listener.TestListener;

import java.util.Collection;
import java.util.Map;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/06/17
 */
public interface TestRunner {

    TestListener run(String fullQualifiedName, Collection<String> testMethodNames);

    TestListener run(String fullQualifiedName, String testMethodName);

    TestListener run(String fullQualifiedName);

}
