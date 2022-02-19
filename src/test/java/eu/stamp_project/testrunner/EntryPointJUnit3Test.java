package eu.stamp_project.testrunner;

import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.runner.ParserOptions;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import spoon.Launcher;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import static org.junit.Assert.*;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@davidson.fr
 * on 19/02/2022
 */
public class EntryPointJUnit3Test {

    public static String MAVEN_HOME;

    public static String JUNIT_3_CP;

    {
        List<String> classPath = Arrays.stream(((URLClassLoader) URLClassLoader.getSystemClassLoader()).getURLs())
                .map(URL::getPath).collect(Collectors.toList());

        System.out.println(classPath);
        if (classPath.size() == 1) {
            // we only have the surefire booter
            // so we take a value by default
            MAVEN_HOME = System.getProperty("user.home") + "/.m2/repository/";
        } else {
            // we infer it from the classpath
            MAVEN_HOME = classPath.stream()
                    .filter(path -> path.contains("/.m2/repository/"))
                    .findFirst()
                    .map(s -> s.substring(0, s.indexOf("/.m2/repository/") + "/.m2/repository/".length()))
                    .get();
        }

        JUNIT_3_CP = MAVEN_HOME + "junit/junit/3.8.2/junit-3.8.2.jar";
    }

    @Test
    public void testOnJUnit3() throws TimeoutException, IOException {
        // make sure you run this from Java 8 otherwise the classloader is not the expected one
        EntryPoint.verbose = true;
        // create folders
        final String ROOT_TEST_PROJECTS_JUNIT3 = "src/test/resources/test-projects-junit3/";
        final File target = new File(ROOT_TEST_PROJECTS_JUNIT3 + "target");
        if (target.exists()) {
            FileUtils.forceDelete(target);
        }
        target.mkdir();
        final File classes = new File(ROOT_TEST_PROJECTS_JUNIT3 + "target/classes");
        classes.mkdir();
        final File testClasses = new File(ROOT_TEST_PROJECTS_JUNIT3 + "target/test-classes");
        testClasses.mkdir();
        String command;

        // compiling with spoon
        Launcher l = new Launcher();
        l.setBinaryOutputDirectory(ROOT_TEST_PROJECTS_JUNIT3 + "target/classes/");
        l.addInputResource(ROOT_TEST_PROJECTS_JUNIT3 + "src/main/java/example/Example.java");
        l.getEnvironment().setShouldCompile(true);
        l.run(); // compile code;

        // now we compile the test code
        l = new Launcher();
        l.setBinaryOutputDirectory(ROOT_TEST_PROJECTS_JUNIT3 + "target/test-classes/");
        l.getEnvironment().setShouldCompile(true);
        List<String> classpath = new ArrayList<>();
        classpath.add(ROOT_TEST_PROJECTS_JUNIT3 + "target/classes/");
        classpath.addAll(Arrays.asList(JUNIT_3_CP.split(ConstantsHelper.PATH_SEPARATOR)));
        l.getEnvironment().setSourceClasspath(classpath.toArray(new String[0]));
        l.addInputResource(ROOT_TEST_PROJECTS_JUNIT3 + "src/test/java/example/TestSuiteExample.java");
        l.run(); // compile tests

        final TestResult testResult = EntryPoint.runTests(
                JUNIT_3_CP + ConstantsHelper.PATH_SEPARATOR +
                        ROOT_TEST_PROJECTS_JUNIT3 + "target/classes" + ConstantsHelper.PATH_SEPARATOR +
                        ROOT_TEST_PROJECTS_JUNIT3 + "target/test-classes",
                "example.TestSuiteExample"
        );
        assertEquals(6, testResult.getRunningTests().size());
        assertEquals(6, testResult.getPassingTests().size());
        assertEquals(0, testResult.getFailingTests().size());
    }

    // depends on the compiler and compiler version that is used on the test project
    public static final int NUMBER_OF_INSTRUCTIONS = 104;

    @Before
    public void setUp() {
        EntryPoint.persistence = true;
        EntryPoint.outPrintStream = null;
        EntryPoint.errPrintStream = null;
        EntryPoint.verbose = true;
        EntryPoint.setMutationEngine(ConstantsHelper.MutationEngine.DESCARTES);
        EntryPoint.coverageDetail = ParserOptions.CoverageTransformerDetail.SUMMARIZED;
    }

    @After
    public void tearDown() {
        EntryPoint.blackList.clear();
    }

}
