package eu.stamp_project.testrunner.runner;

import eu.stamp_project.testrunner.listener.CoverageTransformer;
import eu.stamp_project.testrunner.listener.impl.CoverageCollectorDetailed;
import eu.stamp_project.testrunner.listener.impl.CoverageCollectorDetailedCompressed;
import eu.stamp_project.testrunner.listener.impl.CoverageCollectorMethodDetailed;
import eu.stamp_project.testrunner.listener.impl.CoverageCollectorSummarization;
import eu.stamp_project.testrunner.utils.ConstantsHelper;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

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
                    parserOptions.pathToCompiledClassesOfTheProject = Arrays.stream(args[++i].split(ConstantsHelper.PATH_SEPARATOR)).collect(Collectors.toList());
                    break;
                case FLAG_pathToCompiledTestClassesOfTheProject:
                    parserOptions.pathToCompiledTestClassesOfTheProject = Arrays.stream(args[++i].split(ConstantsHelper.PATH_SEPARATOR)).collect(Collectors.toList());
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
                case FLAG_coverage_detail:
                    parserOptions.coverageTransformerDetail = CoverageTransformerDetail.valueOf(args[++i]);
                    break;
                case FLAG_nbFailingLoadClass:
                    parserOptions.nbFailingLoadClass = Integer.parseInt(args[++i]);
                    break;
                case FLAG_pathToOptionsFile:
                    final String pathToOptionsFile = args[i+1];
                    try (final BufferedReader reader = new BufferedReader(new FileReader(pathToOptionsFile))) {
                        return parse(reader.readLine().split(ConstantsHelper.WHITE_SPACE));
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                case " ":
                case "":
                    break;
                default:
                    System.err.println(String.format("[ERROR]: %s is not a supported command line options", args[i]));
                    usage();
            }
        }
        return parserOptions;
    }

    private static void usage() {
        final StringBuilder usage = new StringBuilder();
        usage.append("Usage:").append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_pathToCompiledClassesOfTheProject).append(ConstantsHelper.WHITE_SPACE)
                .append(FLAG_HELP_pathToCompiledClassesOfTheProject).append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_pathToCompiledTestClassesOfTheProject).append(ConstantsHelper.WHITE_SPACE)
                .append(FLAG_HELP_pathToCompiledTestClassesOfTheProject).append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_fullQualifiedNameOfTestClassToRun).append(ConstantsHelper.WHITE_SPACE)
                .append(FLAG_HELP_fullQualifiedNameOfTestClassToRun).append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_testMethodNamesToRun).append(ConstantsHelper.WHITE_SPACE)
                .append(FLAG_HELP_testMethodNamesToRun).append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_blackList).append(ConstantsHelper.WHITE_SPACE)
                .append(FLAG_HELP_blackList).append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_coverage_detail).append(ConstantsHelper.WHITE_SPACE)
             .append(FLAG_HELP_coverage_detail).append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_nbFailingLoadClass).append(ConstantsHelper.WHITE_SPACE)
                .append(FLAG_HELP_nbFailingLoadClass).append(ConstantsHelper.LINE_SEPARATOR);

        usage.append(FLAG_pathToOptionsFile).append(ConstantsHelper.WHITE_SPACE)
                .append(FLAG_HELP_pathToOptionsFile).append(ConstantsHelper.LINE_SEPARATOR);

        System.out.println(usage.toString());
    }

    /**
     * Options for the locations of source and test binaries
     */
    private List<String> pathToCompiledClassesOfTheProject;

    public static final String FLAG_pathToCompiledClassesOfTheProject = "--sourceBinaries";

    private static final String FLAG_HELP_pathToCompiledClassesOfTheProject = "This flag must be followed by the paths of source binaries. Paths must be separated by the system path separator, e.g. ':' on Linux";

    private List<String> pathToCompiledTestClassesOfTheProject;

    public static final String FLAG_pathToCompiledTestClassesOfTheProject = "--testBinaries";

    private static final String FLAG_HELP_pathToCompiledTestClassesOfTheProject = "This flag must be followed by the paths of test binaries. Paths must be separated by the system path separator, e.g. ':' on Linux";

    /**
     * This list the full qualified names of the test classes to run.
     * For example, eu.stamp_project.my.project.MyClassTest:eu.stamp_project.my.project.MySecondClassTest
     */
    private String[] fullQualifiedNameOfTestClassesToRun;

    public static final String FLAG_fullQualifiedNameOfTestClassToRun = "--class";

    public static final String FLAG_HELP_fullQualifiedNameOfTestClassToRun = "This flag must be followed by the full qualified names of test classes to be run. Names must be separated by the system path separator, e.g. ':' on Linux";

    /**
     * This list is the simple names of test method to run.
     * Simples names must be separated by the system path separator, e.g. ':' on Linux.
     */
    private String[] testMethodNamesToRun;

    public static final String FLAG_testMethodNamesToRun = "--tests";

    public static final String FLAG_HELP_testMethodNamesToRun = "This flag must be followed by the list of simple names of test methods to be run. Names must be separated by the system path separator, e.g. ':' on Linux";

    /**
     * This list is the simple names of test method to NOT run
     * Simples names must be separated by the system path separator, e.g. ':' on Linux.
     */
    private List<String> blackList;

    public static final String FLAG_blackList = "--blacklist";

    public static final String FLAG_HELP_blackList = "This flag must be followed by the list of simple names of test methods to NOT be run. Names must be separated by the system path separator, e.g. ':' on Linux";



    public enum CoverageTransformerDetail {
        SUMMARIZED,
        DETAIL,
        METHOD_DETAIL,
        DETAIL_COMPRESSED
    }
    /**
     * This value represents at which level of detail coverage information should be provided
     */
    private CoverageTransformerDetail coverageTransformerDetail;

    public static final String FLAG_coverage_detail = "--coverage-detail";

    public static final String FLAG_HELP_coverage_detail = "The value following this flag defines the level of detail" +
                                                           " provided in the coverage information. Valid values:" +
                                                           "'SUMMARIZED' (default), 'DETAIL' or 'METHOD_DETAIL'.";

    /**
     * This option specifies the number of "ClassNotFoundException" throws when running the tests.
     * This option allows to skip some missing compiled test classes
     */
    private int nbFailingLoadClass = 0;

    public static final String FLAG_nbFailingLoadClass = "--nb-failing-load-class";

    public static final String FLAG_HELP_nbFailingLoadClass = "This option specifies the number of \"ClassNotFoundException\"" +
            " throws when running the tests." +
            "This option allows to skip some missing compiled test classes";

    public static final String FLAG_pathToOptionsFile = "--path-options-file";

    public static final String FLAG_HELP_pathToOptionsFile = "This option specifies a path to a file that contains options" +
            " for test-runner." +
            " This can be used to pass too large command line options such a very long list of test method names.";

    private ParserOptions() {
        this.pathToCompiledClassesOfTheProject = Collections.singletonList("");
        this.pathToCompiledTestClassesOfTheProject = Collections.singletonList("");
        this.fullQualifiedNameOfTestClassesToRun = new String[]{};
        this.testMethodNamesToRun = new String[]{};
        this.blackList = new ArrayList<>();
        this.coverageTransformerDetail = CoverageTransformerDetail.SUMMARIZED;
    }

    public List<String> getPathToCompiledClassesOfTheProject() {
        return pathToCompiledClassesOfTheProject;
    }

    public List<String> getPathToCompiledTestClassesOfTheProject() {
        return pathToCompiledTestClassesOfTheProject;
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

    public int getNbFailingLoadClass() {
        return nbFailingLoadClass;
    }

    public CoverageTransformer getCoverageTransformer() {
        switch (coverageTransformerDetail) {
            case DETAIL:
                return new CoverageCollectorDetailed();
            case METHOD_DETAIL:
                return new CoverageCollectorMethodDetailed();
            case DETAIL_COMPRESSED:
                return new CoverageCollectorDetailedCompressed();
            case SUMMARIZED:
            default:
                return new CoverageCollectorSummarization();
        }

    }

}
