package eu.stamp_project.testrunner.maven;

import org.junit.Test;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class EntryPointTest {


    @Test
    public void test() {

        /*
            Test the EntryPoint using maven
         */

        EntryPoint.runTestClasses("/home/bdanglot/workspace/testrunner/src/test/resources/test-projects/pom.xml");
        EntryPoint.runTestClasses("/home/bdanglot/workspace/testrunner/src/test/resources/test-projects/pom.xml",
                "example.TestSuiteExample", "example.TestSuiteExample"
        );
        EntryPoint.runTests("/home/bdanglot/workspace/testrunner/src/test/resources/test-projects/pom.xml",
                "example.TestSuiteExample", "test2", "test3", "test7"
        );

        

    }
}
