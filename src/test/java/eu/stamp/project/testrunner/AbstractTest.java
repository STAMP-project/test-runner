package eu.stamp.project.testrunner;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class AbstractTest {

    @BeforeClass
    public static void setUp() throws Exception {
        EntryPoint.verbose = true;
        // create folders
        final File target = new File("src/test/resources/test-projects/target");
        if (target.exists()) {
            FileUtils.forceDelete(target);
        }
        target.mkdir();
        final File classes = new File("src/test/resources/test-projects/target/classes");
        classes.mkdir();
        final File testClasses = new File("src/test/resources/test-projects/target/test-classes");
        testClasses.mkdir();
        // compiling
        Runtime.getRuntime().exec("javac -d src/test/resources/test-projects/target/classes " +
                "src/test/resources/test-projects/src/main/java/example/Example.java").waitFor();
        Runtime.getRuntime().exec("javac -d src/test/resources/test-projects/target/test-classes" +
                " -cp src/test/resources/test-projects/target/classes/:" + JUNIT_CP +
                " src/test/resources/test-projects/src/test/java/example/TestSuiteExample.java" + " src/test/resources/test-projects/src/test/java/example/TestSuiteExample2.java").waitFor();
    }

    @AfterClass
    public static void tearDown() throws Exception {
        final File target = new File("src/test/resources/test-projects/target");
        if (target.exists()) {
            FileUtils.forceDelete(target);
        }
    }

    public static final String MAVEN_HOME = Arrays.stream(((URLClassLoader) URLClassLoader.getSystemClassLoader()).getURLs())
            .map(URL::getPath)
            .filter(path -> path.contains("/.m2/repository/"))
            .findFirst()
            .map(s -> s.substring(0, s.indexOf("/.m2/repository/") + "/.m2/repository/".length()))
            .get();

    public static final String TEST_PROJECT_CLASSES = "src/test/resources/test-projects/target/classes:" +
            "src/test/resources/test-projects/target/test-classes";

    public static final String JUNIT_CP = MAVEN_HOME + "junit/junit/4.11/junit-4.11.jar:" + MAVEN_HOME + "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar";

    public static final String PATH_TO_RUNNER_CLASSES = "src/main/resources/runner-classes/";

    public static final String nl = System.getProperty("line.separator");

}
