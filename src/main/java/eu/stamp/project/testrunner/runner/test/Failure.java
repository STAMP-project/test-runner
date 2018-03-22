package eu.stamp.project.testrunner.runner.test;

import java.io.Serializable;

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