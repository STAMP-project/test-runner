package eu.stamp_project.testrunner.maven;

import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.apache.maven.shared.invoker.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Arrays;
import java.util.Optional;
import java.util.Properties;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/09/18
 * <p>
 * This class is an extension of {@link eu.stamp_project.testrunner.EntryPoint} to run the tests using Maven goals
 */
@Deprecated
public class EntryPoint {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntryPoint.class);

    public static String preGoals = "";

    /**
     * Execution of various test classes.
     * <p>
     * Run all the test classes given as a full qualified name. For instance, my.package.MyClassTest.
     * This methods will run all the test methods within the given test classes.
     * </p>
     *
     * @param absolutePathToRootProject      path to the root of the targeted project
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @return an instance of TestResult {@link TestResult} containing result of the exeuction of test methods.
     */
    public static TestResult runTestClasses(String absolutePathToRootProject,
                                            String... fullQualifiedNameOfTestClasses) {
        return runTestClassesSpecificPom(absolutePathToRootProject, POM_FILE, fullQualifiedNameOfTestClasses);
    }

    /**
     * Execution of various test classes.
     * <p>
     * Run all the test classes given as a full qualified name. For instance, my.package.MyClassTest.
     * This methods will run all the test methods within the given test classes.
     * </p>
     *
     * @param absolutePathToRootProject      path to the root of the targeted project
     * @param fullQualifiedNameOfTestClasses test classes to be run.
     * @param pomFileName                    the filename of the pom to be used
     * @return an instance of TestResult {@link TestResult} containing result of the exeuction of test methods.
     */
    public static TestResult runTestClassesSpecificPom(String absolutePathToRootProject,
                                                       String pomFileName,
                                                       String... fullQualifiedNameOfTestClasses) {
        if (fullQualifiedNameOfTestClasses.length > 0) {
            EntryPoint.runMavenGoal(absolutePathToRootProject, GOAL_TEST, GOAL_SPECIFY +
                    String.join(TEST_CLASS_SEPARATOR, fullQualifiedNameOfTestClasses)
            );
        } else {
            EntryPoint.runMavenGoal(absolutePathToRootProject, pomFileName, GOAL_TEST);
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
     * @return an instance of TestResult {@link TestResult} containing result of the execution of test methods.
     */
    public static TestResult runTests(String absolutePathToRootProject,
                                      String fullQualifiedNameOfTestClass,
                                      String... testMethods) {
        return runTestsSpecificPom(absolutePathToRootProject, fullQualifiedNameOfTestClass, POM_FILE, testMethods);
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
     * @param pomFileName                  the filename of the pom to be used
     * @param testMethods                  test methods to be run.
     * @return an instance of TestResult {@link TestResult} containing result of the execution of test methods.
     */
    public static TestResult runTestsSpecificPom(String absolutePathToRootProject,
                                                 String fullQualifiedNameOfTestClass,
                                                 String pomFileName,
                                                 String... testMethods) {
        if (testMethods.length > 0) {
            EntryPoint.runMavenGoal(absolutePathToRootProject + "/" + pomFileName, GOAL_TEST, GOAL_SPECIFY +
                    fullQualifiedNameOfTestClass + TEST_CLASS_METHOD_SEPARATOR +
                    String.join(TEST_METHOD_SEPARATOR, testMethods)
            );
        } else {
            EntryPoint.runMavenGoal(absolutePathToRootProject + "/" + pomFileName, GOAL_TEST);
        }
        return new SurefireReportsReader().readAll(absolutePathToRootProject + "/" + PATH_TO_SUREFIRE);
    }


    static int runMavenGoal(String absolutePathToPomFile, String... goals) {
        final String[] splittedPreGoals = preGoals.isEmpty() ? new String[0] : preGoals.split(",");
        if (!eu.stamp_project.testrunner.EntryPoint.persistence) {
            preGoals = "";
        }
        final String[] finalGoals = new String[splittedPreGoals.length + goals.length];
        System.arraycopy(splittedPreGoals, 0, finalGoals, 0, splittedPreGoals.length);
        System.arraycopy(goals, 0, finalGoals, splittedPreGoals.length, goals.length);
        LOGGER.debug("run mvn {}", String.join(ConstantsHelper.WHITE_SPACE, finalGoals));
        InvocationRequest request = new DefaultInvocationRequest();
        request.setGoals(Arrays.asList(goals));
        request.setPomFile(new File(absolutePathToPomFile));
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
                    "/usr/share/maven/", "/usr/local/maven-3.3.9/", "/usr/share/maven3/", "/usr/share/apache-maven-3.8.1");
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
