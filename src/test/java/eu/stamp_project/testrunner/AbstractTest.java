package eu.stamp_project.testrunner;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 19/12/17
 */
public class AbstractTest {

    @BeforeClass
    public static void beforeClass() throws Exception {
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
        String command = "javac -d src/test/resources/test-projects/target/classes " +
                "src/test/resources/test-projects/src/main/java/example/Example.java" +
                " src/test/resources/test-projects/src/main/java/tobemocked/LoginController.java" +
                " src/test/resources/test-projects/src/main/java/tobemocked/LoginDao.java" +
                " src/test/resources/test-projects/src/main/java/tobemocked/LoginService.java" +
                " src/test/resources/test-projects/src/main/java/tobemocked/UserForm.java";
        System.out.println(command);
        if (Runtime.getRuntime().exec(command).waitFor() != 0) {
            throw new RuntimeException("Problem when compiling sources.");
        }
        command = "javac -d src/test/resources/test-projects/target/test-classes" +
                " -cp src/test/resources/test-projects/target/classes/" + EntryPoint.PATH_SEPARATOR +
                JUNIT_CP + EntryPoint.PATH_SEPARATOR +
                EASYMOCK_CP + EntryPoint.PATH_SEPARATOR +
                JUNIT5_CP +
                " src/test/resources/test-projects/src/test/java/example/TestSuiteExample.java" +
                " src/test/resources/test-projects/src/test/java/example/ParametrizedTestSuiteExample.java" +
                " src/test/resources/test-projects/src/test/java/failing/FailingTestClass.java" +
                " src/test/resources/test-projects/src/test/java/example/TestSuiteExample2.java" +
                " src/test/resources/test-projects/src/test/java/easymock/LoginControllerIntegrationTest.java" +
                " src/test/resources/test-projects/src/test/java/junit5/TestSuiteExample.java" +
                " src/test/resources/test-projects/src/test/java/junit5/FailingTestClass.java" +
                " src/test/resources/test-projects/src/test/java/junit5/TestSuiteExample2.java";
        System.out.println(command);
        if (Runtime.getRuntime().exec(command).waitFor() != 0) {
            throw new RuntimeException("Problem when compiling test sources.");
        }
    }

    @AfterClass
    public static void afterClass() throws Exception {
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

    public static final String TEST_PROJECT_CLASSES = "src/test/resources/test-projects/target/classes/" + EntryPoint.PATH_SEPARATOR +
            "src/test/resources/test-projects/target/test-classes/";

    public static final String JUNIT_CP = MAVEN_HOME + "junit/junit/4.12/junit-4.12.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar";

    public static final String EASYMOCK_CP = MAVEN_HOME + "org/easymock/easymock/3.4/easymock-3.4.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/objenesis/objenesis/2.2/objenesis-2.2.jar";

    public static final String JUNIT5_CP = MAVEN_HOME + "org/junit/jupiter/junit-jupiter-api/5.1.0/junit-jupiter-api-5.1.0.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/apiguardian/apiguardian-api/1.0.0/apiguardian-api-1.0.0.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/opentest4j/opentest4j/1.0.0/opentest4j-1.0.0.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/junit/platform/junit-platform-commons/1.1.0/junit-platform-commons-1.1.0.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/junit/jupiter/junit-jupiter-engine/5.1.0/junit-jupiter-engine-5.1.0.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/junit/platform/junit-platform-engine/1.1.0/junit-platform-engine-1.1.0.jar" + EntryPoint.PATH_SEPARATOR
            + MAVEN_HOME + "org/junit/platform/junit-platform-launcher/1.2.0/junit-platform-launcher-1.2.0.jar";

    public static final String PATH_TO_RUNNER_CLASSES = "src/main/resources/runner-classes/";

    public static final String nl = System.getProperty("line.separator");


}
