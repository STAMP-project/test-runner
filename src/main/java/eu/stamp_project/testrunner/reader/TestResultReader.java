package eu.stamp_project.testrunner.reader;

import eu.stamp_project.testrunner.listener.junit4.JUnit4TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.runner.Loader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/09/18
 */
public class TestResultReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestResultReader.class);

    /**
     * @param args should contain only on argument: the path to the testResult.ser to be read
     */
    public static void main(String[] args) {
        if (!new File("target/dspot/JUnit4TestResult.ser").exists()) {
            usage();
        }
        boolean verbose = false;
        if (args.length >= 1 && "--verbose".equals(args[1])) {
            verbose = true;
        }
        JUnit4TestResult JUnit4TestResult = new Loader<JUnit4TestResult>().load("JUnit4TestResult");
        LOGGER.info("Test that has been run:");
        JUnit4TestResult.getRunningTests().forEach(running -> LOGGER.info("\t{}", running));
        LOGGER.info("Passing tests:");
        JUnit4TestResult.getPassingTests().forEach(passing -> LOGGER.info("\t{}", passing));
        LOGGER.info("Failing tests:");
        if (verbose) {
            JUnit4TestResult.getFailingTests().stream()
                    .map(failure -> failure.toString() + ":" + failure.stackTrace)
                    .forEach(failure -> LOGGER.info("\t{}", failure));
        } else {
            JUnit4TestResult.getFailingTests().stream().map(Failure::toString).forEach(failure -> LOGGER.info("\t{}", failure));
        }
        LOGGER.info("Assumption failing tests:");
        JUnit4TestResult.getAssumptionFailingTests().stream().map(Failure::toString).forEach(assumptionFailure -> LOGGER.info("\t{}", assumptionFailure));
        LOGGER.info("Ignored tests:");
        JUnit4TestResult.getIgnoredTests().forEach(ignored -> LOGGER.info("\t{}", ignored));
    }

    private static void usage() {
        LOGGER.error("Usage: java -cp test-runner-<version>-jar-with-dependencies.jar eu.stamp_project.testrunner.reader.TestResultReader");
        LOGGER.error("The target/dspot/JUnit4TestResult.ser must exist and created by the EntryPoint");
        System.exit(1);
    }

}
