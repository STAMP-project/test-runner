package eu.stamp_project.testrunner.runner;

import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 27/11/18
 * <p>
 * This class aims at parsing options for
 * - {@link JUnit4Runner}, {@link JUnit5Runner}, {@link eu.stamp_project.testrunner.runner.coverage.JacocoRunner}
 */
public class ParserOptions {

    private static final Function<String, List<String>> convertArrayToList =
            value -> Arrays.asList(value.split(ConstantsHelper.PATH_SEPARATOR));

    public static ParserOptions parse(String[] args) {
        System.out.println(String.format("Parsing %s", String.join(" ", args)));
        final ParserOptions parserOptions = new ParserOptions();
        for (int i = 0; i < args.length; i++) {
            switch (args[i]) {
                case FLAG_pathToCompiledClassesOfTheProject:
                    parserOptions.pathToCompiledClassesOfTheProject = args[++i];
                    break;
                case FLAG_fullQualifiedNameOfTestClassToRun:
                    parserOptions.fullQualifiedNameOfTestClassesToRun = args[++i].split(ConstantsHelper.PATH_SEPARATOR);
                    break;
                case FLAG_testMethodNamesToRun:
                    parserOptions.testMethodNamesToRun = args[++i].split(ConstantsHelper.PATH_SEPARATOR);
                    break;
                case FLAG_blackList:
                    parserOptions.blackList = convertArrayToList.apply(args[++i]);
                    break;
                case FLAG_isJUnit5:
                    parserOptions.isJUnit5 = true;
                    break;
                default:
                    System.err.println(String.format("[ERROR]: %s is not a supported command line options", args[i]));
                    //TODO implement usage()
            }
        }
        return parserOptions;
    }

    /**
     * This string represents the path to the compiles classes, sources and tests, of the project.
     * These two paths should be separated by the system path separator, e.g. ':' on Linux.
     */
    private String pathToCompiledClassesOfTheProject;

    public static final String FLAG_pathToCompiledClassesOfTheProject = "--binaries";

    /**
     * This list the full qualified names of the test classes to run.
     * For example, eu.stamp_project.my.project.MyClassTest:eu.stamp_project.my.project.MySecondClassTest
     */
    private String[] fullQualifiedNameOfTestClassesToRun;

    public static final String FLAG_fullQualifiedNameOfTestClassToRun = "--class";

    /**
     * This list is the simple names of test method to run.
     * Simples names must be separated by the system path separator, e.g. ':' on Linux.
     */
    private String[] testMethodNamesToRun;

    public static final String FLAG_testMethodNamesToRun = "--tests";

    /**
     * This list is the simple names of test method to NOT run
     * Simples names must be separated by the system path separator, e.g. ':' on Linux.
     */
    private List<String> blackList;

    public static final String FLAG_blackList = "--blacklist";

    /**
     * If this boolean is true, it enables the JUnit5 mode of the test runner, otherwise, it executes JUnit4.
     */
    private boolean isJUnit5;

    public static final String FLAG_isJUnit5 = "--junit5";

    private ParserOptions() {
        this.pathToCompiledClassesOfTheProject = "";
        this.fullQualifiedNameOfTestClassesToRun = new String[]{};
        this.testMethodNamesToRun = new String[]{};
        this.blackList = new ArrayList<>();
        this.isJUnit5 = false;
    }

    public String getPathToCompiledClassesOfTheProject() {
        return pathToCompiledClassesOfTheProject;
    }

    public String[] getFullQualifiedNameOfTestClassesToRun() {
        return fullQualifiedNameOfTestClassesToRun;
    }

    public String[] getTestMethodNamesToRun() {
        return testMethodNamesToRun;
    }

    public List<String> getBlackList() {
        return blackList;
    }

    public boolean isJUnit5() {
        return isJUnit5;
    }
}
