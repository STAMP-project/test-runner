package eu.stamp_project.testrunner.reader;

import eu.stamp_project.testrunner.runner.Failure;
import eu.stamp_project.testrunner.runner.Loader;
import eu.stamp_project.testrunner.listener.junit4.JUnit4TestListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 11/09/18
 */
public class TestListenerReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestListenerReader.class);

    /**
     * @param args should contain only on argument: the path to the testListener.ser to be read
     */
    public static void main(String[] args) {
        if (!new File("target/dspot/JUnit4TestListener.ser").exists()) {
            usage();
        }
        boolean verbose = false;
        if (args.length >= 1 && "--verbose".equals(args[1])) {
            verbose = true;
        }
        JUnit4TestListener JUnit4TestListener = new Loader<JUnit4TestListener>().load("JUnit4TestListener");
        LOGGER.info("Test that has been run:");
        JUnit4TestListener.getRunningTests().forEach(running -> LOGGER.info("\t{}", running));
        LOGGER.info("Passing tests:");
        JUnit4TestListener.getPassingTests().forEach(passing -> LOGGER.info("\t{}", passing));
        LOGGER.info("Failing tests:");
        if (verbose) {
            JUnit4TestListener.getFailingTests().stream()
                    .map(failure -> failure.toString() + ":" + failure.stackTrace)
                    .forEach(failure -> LOGGER.info("\t{}", failure));
        } else {
            JUnit4TestListener.getFailingTests().stream().map(Failure::toString).forEach(failure -> LOGGER.info("\t{}", failure));
        }
        LOGGER.info("Assumption failing tests:");
        JUnit4TestListener.getAssumptionFailingTests().stream().map(Failure::toString).forEach(assumptionFailure -> LOGGER.info("\t{}", assumptionFailure));
        LOGGER.info("Ignored tests:");
        JUnit4TestListener.getIgnoredTests().forEach(ignored -> LOGGER.info("\t{}", ignored));
    }

    private static void usage() {
        LOGGER.error("Usage: java -cp test-runner-<version>-jar-with-dependencies.jar eu.stamp_project.testrunner.reader.TestListenerReader");
        LOGGER.error("The target/dspot/JUnit4TestListener.ser must exist and created by the EntryPoint");
        System.exit(1);
    }

}
