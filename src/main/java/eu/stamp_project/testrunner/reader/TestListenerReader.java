package eu.stamp_project.testrunner.reader;

import eu.stamp_project.testrunner.runner.test.Failure;
import eu.stamp_project.testrunner.runner.test.Loader;
import eu.stamp_project.testrunner.runner.test.TestListener;
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
        if (!new File("target/dspot/TestListener.ser").exists()) {
            usage();
        }
        boolean verbose = false;
        if (args.length >= 1 && "--verbose".equals(args[1])) {
            verbose = true;
        }
        TestListener testListener = new Loader<TestListener>().load("TestListener");
        LOGGER.info("Test that has been run:");
        testListener.getRunningTests().forEach(running -> LOGGER.info("\t{}", running));
        LOGGER.info("Passing tests:");
        testListener.getPassingTests().forEach(passing -> LOGGER.info("\t{}", passing));
        LOGGER.info("Failing tests:");
        if (verbose) {
            testListener.getFailingTests().stream()
                    .map(failure -> failure.toString() + ":" + failure.stackTrace)
                    .forEach(failure -> LOGGER.info("\t{}", failure));
        } else {
            testListener.getFailingTests().stream().map(Failure::toString).forEach(failure -> LOGGER.info("\t{}", failure));
        }
        LOGGER.info("Assumption failing tests:");
        testListener.getAssumptionFailingTests().stream().map(Failure::toString).forEach(assumptionFailure -> LOGGER.info("\t{}", assumptionFailure));
        LOGGER.info("Ignored tests:");
        testListener.getIgnoredTests().forEach(ignored -> LOGGER.info("\t{}", ignored));
    }

    private static void usage() {
        LOGGER.error("Usage: java -cp test-runner-<version>-jar-with-dependencies.jar eu.stamp_project.testrunner.reader.TestListenerReader");
        LOGGER.error("The target/dspot/TestListener.ser must exist and created by the EntryPoint");
        System.exit(1);
    }

}
