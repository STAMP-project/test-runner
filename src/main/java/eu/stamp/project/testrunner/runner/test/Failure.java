package eu.stamp.project.testrunner.runner.test;

import java.io.Serializable;

/**
 * This class contains the result of failing test method.
 * <p>
 * This class contains only String, in order to be able to support result inside another project,
 * without having all correct classes loaded.
 * </p>
 */
public class Failure implements Serializable {
    public final String testCaseName;
    public final String fullQualifiedNameOfException;
    public final String messageOfFailure;

    public Failure(String testCaseName, String fullQualifiedNameOfException, String messageOfFailure) {
        this.testCaseName = testCaseName;
        this.fullQualifiedNameOfException = fullQualifiedNameOfException;
        this.messageOfFailure = messageOfFailure;
    }

    @Override
    public String toString() {
        return "Failure{" +
                "testCaseName='" + testCaseName + '\'' +
                ", fullQualifiedNameOfException='" + fullQualifiedNameOfException + '\'' +
                ", messageOfFailure='" + messageOfFailure + '\'' +
                '}';
    }
}