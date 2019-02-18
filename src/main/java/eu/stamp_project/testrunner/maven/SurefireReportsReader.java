package eu.stamp_project.testrunner.maven;

import eu.stamp_project.testrunner.listener.TestResult;
import eu.stamp_project.testrunner.listener.junit4.JUnit4TestResult;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 12/09/18
 */
public class SurefireReportsReader {

    private static final Logger LOGGER = LoggerFactory.getLogger(SurefireReportsReader.class);

    public TestResult readAll(String pathToRootContainingReports) {
        final File rootDirectory = new File(pathToRootContainingReports);
        if (!rootDirectory.exists()) {
            LOGGER.error("{} does not exists! Could not read the surefire reports.", pathToRootContainingReports);
        }
        return Arrays.stream(Objects.requireNonNull(rootDirectory.listFiles()))
                .filter(file -> file.getName().startsWith("TEST-"))
                .map(File::getAbsolutePath)
                .map(this::read)
                .reduce(TestResult::aggregate)
                .get();
    }

    public TestResult read(String pathToSurefireReports) {
        try {
            final JUnit4TestResult listener = new JUnit4TestResult();
            File fXmlFile = new File(pathToSurefireReports);
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(fXmlFile);
            //optional, but recommended
            //read this - http://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
            doc.getDocumentElement().normalize();
            final NodeList testCases = doc.getElementsByTagName("testcase");
            IntStream.range(0, testCases.getLength()).boxed()
                    .map(testCases::item)
                    .map(this::readTestCase)
                    .forEach(listener::aggregate);
            return listener;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private JUnit4TestResult readTestCase(Node testCase) {
        final JUnit4TestResult listener = new JUnit4TestResult();
        final String testClassName = testCase.getAttributes().getNamedItem("classname").getNodeValue();
        final String testName = testCase.getAttributes().getNamedItem("name").getNodeValue();
        final Description testDescription = Description.createTestDescription(testClassName, testName, testClassName + "#" + testName);
        if (testCase.getFirstChild() != null) {
            if (testCase.getFirstChild().getNextSibling() != null) {
                final Node nextSibling = testCase.getFirstChild().getNextSibling();
                if ("skipped".equals(nextSibling.getNodeName())) {
                    try {
                        listener.testIgnored(testDescription);
                        return listener;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    if ("failure".equals(nextSibling.getNodeName())) {
                        final String stacktrace = nextSibling.getFirstChild().getNodeValue();
                        final String fullQualifiedNameOfException = stacktrace.split(LINE_SEPARATOR)[0];
                        listener.getFailingTests().add(new Failure(testName, testClassName, fullQualifiedNameOfException, "", stacktrace));

                    }

                }
            }
        }
        try {
            listener.testFinished(testDescription);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return listener;
    }

    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

}
