package fr.inria.stamp.runner.test;

import java.io.Serializable;

public class Failure implements Serializable {
        public final String testCaseName;
        public final String fullQualifiedNameOfException;

        public Failure(String testCaseName, String fullQualifiedNameOfException) {
            this.testCaseName = testCaseName;
            this.fullQualifiedNameOfException = fullQualifiedNameOfException;
        }

        @Override
        public String toString() {
            return "Failure{" +
                    "testCaseName='" + testCaseName + '\'' +
                    ", fullQualifiedNameOfException='" + fullQualifiedNameOfException + '\'' +
                    '}';
        }
    }