package eu.stamp_project.testrunner.maven;

import eu.stamp_project.testrunner.runner.test.TestListener;
import org.apache.maven.shared.invoker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/09/18
 * <p>
 * This class is an extension of {@link eu.stamp_project.testrunner.EntryPoint} to run the tests using Maven goals
 */
public class EntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

    /**
     * Execution of various test classes.
     * <p>
     * Run all the test classes given as a full qualified name. For instance, my.package.MyClassTest.
     * This methods will run all the test methods within the given test classes.
     * </p>
     *
     * @param absolutePathToRootProject      path to the root of the targeted project
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @return an instance of TestListener {@link TestListener} containing result of the exeuction of test methods.
     */
    public static TestListener runTestClasses(String absolutePathToRootProject,
                                              String... fullQualifiedNameOfTestClasses) {
        if (fullQualifiedNameOfTestClasses.length > 0) {
            EntryPoint.runMavenGoal(absolutePathToRootProject, GOAL_TEST, GOAL_SPECIFY +
                    String.join(TEST_CLASS_SEPARATOR, fullQualifiedNameOfTestClasses)
            );
        } else {
            EntryPoint.runMavenGoal(absolutePathToRootProject, GOAL_TEST);
        }
        return new SurefireReportsReader().readAll(absolutePathToRootProject + "/" + PATH_TO_SUREFIRE);
    }

    /**
     * Execution of various test methods inside a given test class.
     * <p>
     * Run all the test methods given inside the given test class. The test class must be given as a full qualified name.
     * For instance, my.package.MyClassTest.
     * This methods will run all the test methods given.
     * </p>
     *
     * @param absolutePathToRootProject    path to the root of the targeted project
     * @param fullQualifiedNameOfTestClass test class to be run.
     * @param testMethods                  test methods to be run.
     * @return an instance of TestListener {@link TestListener} containing result of the execution of test methods.
     */
    public static TestListener runTests(String absolutePathToRootProject,
                                        String fullQualifiedNameOfTestClass,
                                        String... testMethods) {
        if (testMethods.length > 0) {
            EntryPoint.runMavenGoal(absolutePathToRootProject, GOAL_TEST, GOAL_SPECIFY +
                    fullQualifiedNameOfTestClass + TEST_CLASS_METHOD_SEPARATOR +
                    String.join(TEST_METHOD_SEPARATOR, testMethods)
            );
        } else {
            EntryPoint.runMavenGoal(absolutePathToRootProject, GOAL_TEST);
        }
        return new SurefireReportsReader().readAll(absolutePathToRootProject + "/" + PATH_TO_SUREFIRE);
    }

    static int runMavenGoal(String absolutePathToPomFile, String... goals) {
        LOGGER.info("run mvn {}", String.join(eu.stamp_project.testrunner.EntryPoint.WHITE_SPACE, goals));
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Arrays.asList(goals));
        request.setPomFile(new File(absolutePathToPomFile + "/" + POM_FILE));
        request.setJavaHome(new File(System.getProperty("java.home")));
        request.setProperties(properties);
        Invoker invoker = new DefaultInvoker();
        if (mavenHome == null) {
            setMavenHome();
        }
        invoker.setMavenHome(new File(mavenHome));
        invoker.setOutputHandler(LOGGER::info);
        invoker.setErrorHandler(LOGGER::error);
        try {
            return invoker.execute(request).getExitCode();
        } catch (MavenInvocationException e) {
            throw new RuntimeException(e);
        }
    }

    private static final Properties properties = new Properties();

    static {
        properties.setProperty("enforcer.skip", "true");
        properties.setProperty("checkstyle.skip", "true");
        properties.setProperty("cobertura.skip", "true");
        properties.setProperty("skipITs", "true");
        properties.setProperty("rat.skip", "true");
        properties.setProperty("license.skip", "true");
        properties.setProperty("findbugs.skip", "true");

        properties.setProperty("gpg.skip", "true");
        properties.setProperty("jacoco.skip", "true");
    }

    private static final String POM_FILE = "pom.xml";

    private static final String GOAL_TEST = "test";

    private static final String GOAL_SPECIFY = "-Dtest=";

    private static final String TEST_CLASS_SEPARATOR = ",";

    private static final String TEST_METHOD_SEPARATOR = "+";

    private static final String TEST_CLASS_METHOD_SEPARATOR = "#";

    private static final String PATH_TO_SUREFIRE = "target/surefire-reports/";

    public static String mavenHome;

    private static void setMavenHome() {
        LOGGER.warn("Trying to lookup for maven home.");
        LOGGER.warn("This can fail, and thus lead to a crash of the application.");
        LOGGER.warn("You can set this value using the field mavenHome or defining the following property: MAVEN_HOME or M2_HOME");
        mavenHome = getMavenHome(envVariable -> System.getenv().get(envVariable) != null,
                envVariable -> System.getenv().get(envVariable),
                "MAVEN_HOME", "M2_HOME");
        if (mavenHome == null) {
            mavenHome = getMavenHome(path -> new File(path).exists(),
                    Function.identity(),
                    "/usr/share/maven/", "/usr/local/maven-3.3.9/", "/usr/share/maven3/");
            if (mavenHome == null) {
                throw new RuntimeException("Maven home not found, please set properly MAVEN_HOME or M2_HOME.");
            }
        }
    }

    private static String getMavenHome(Predicate<String> conditional,
                                       Function<String, String> getFunction,
                                       String... possibleValues) {
        String mavenHome = null;
        final Optional<String> potentialMavenHome = Arrays.stream(possibleValues).filter(conditional).findFirst();
        if (potentialMavenHome.isPresent()) {
            mavenHome = getFunction.apply(potentialMavenHome.get());
        }
        return mavenHome;
    }


}
