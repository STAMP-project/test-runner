package eu.stamp_project.testrunner.runner.pit;

import eu.stamp_project.testrunner.utils.ConstantsHelper;
import org.pitest.mutationtest.config.PluginServices;
import org.pitest.mutationtest.config.ReportOptions;
import org.pitest.mutationtest.config.SettingsFactory;
import org.pitest.mutationtest.tooling.EntryPoint;
import org.pitest.testapi.TestGroupConfig;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 03/04/19
 */
public class PitRunner {

    private static final String JUNIT4_TEST_PLUGIN = "junit";

    private static final String JUNIT5_TEST_PLUGIN = "junit5";

    public final static String REPORT_PITS = "target/report-pits/";

    /**
     * Main method to execute pit
     *
     * @param args an array that contains the following arguments, in the same order:
     *             classpath the full classpath of the application, plus the required class to run pit
     *             pathRootProject the root folder of the project
     *             filterTargetClasses the regex to match source classes (application) to mutate, e.g. example.*
     *             targetTest the regex to match test class(es) to run
     *             outputFormat the format of the output, either XML or CSV.
     *             mutationEngine the mutation engine to be used: descartes or gregor
     *             mutators the list of the mutators to be used. It must be correspond to the specified mutationEngine.
     */
    public static void main(String[] args) {
        final String classpath = args[0];
        final String pathRootProject = args[1];
        final String filterTargetClasses = args[2];
        final String targetTest = args[3];
        final String outputFormat = args[4];
        final String mutationEngine = args[5];
        final List<String> mutators;
        if (args.length == 7) {
            mutators = Arrays.asList(args[6].split(ConstantsHelper.PATH_SEPARATOR));
        } else {
            mutators = Collections.singletonList("");
        }
        final ReportOptions options = createReportOptions(classpath,
                pathRootProject,
                filterTargetClasses,
                targetTest,
                mutators,
                mutationEngine,
                outputFormat
        );
        final SettingsFactory settingsFactory = createSettingFactory(classpath, options);
        final EntryPoint e = new EntryPoint();
        e.execute(new File(pathRootProject), options, settingsFactory, Collections.emptyMap());
    }

    private static ReportOptions createReportOptions(final String classpath,
                                                     final String pathRootProject,
                                                     final String filterTargetClasses,
                                                     final String targetTest,
                                                     final List<String> mutators,
                                                     final String mutationEngine,
                                                     final String outputFormat) {
        final ReportOptions data = new ReportOptions();
        final List<String> classpathList = Arrays.asList(classpath.split(":"));
        data.setClassPathElements(classpathList);
        data.setDependencyAnalysisMaxDistance(-1);
        data.setTargetClasses(Collections.singletonList(filterTargetClasses));
        data.setTargetTests(Collections.singletonList(toBeMatched -> Pattern.compile(targetTest).matcher(toBeMatched).matches()));
        data.setReportDir(pathRootProject + REPORT_PITS);
        data.setVerbose(true);
        data.setMutators(mutators);
        data.setSourceDirs(Collections.singletonList(new File(pathRootProject)));
        data.addOutputFormats(Collections.singletonList(outputFormat));
        final TestGroupConfig testGroupConfig = new TestGroupConfig(Collections.emptyList(), Collections.emptyList());
        data.setGroupConfig(testGroupConfig);
        data.setExportLineCoverage(true);
        data.setMutationEngine(mutationEngine);
        data.setTestPlugin(
                eu.stamp_project.testrunner.EntryPoint.jUnit5Mode ?
                        JUNIT5_TEST_PLUGIN :
                        JUNIT4_TEST_PLUGIN
        );
        return data;
    }

    private static SettingsFactory createSettingFactory(final String classpath,
                                                        final ReportOptions options) {
        final URL[] urls = Arrays.stream(classpath.split(":"))
                .map(File::new)
                .map(file -> {
                    try {
                        return file.toURI().toURL();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }).toArray(URL[]::new);
        ClassLoader classLoader = new URLClassLoader(urls);
        return new SettingsFactory(options, new PluginServices(classLoader));
    }


}
