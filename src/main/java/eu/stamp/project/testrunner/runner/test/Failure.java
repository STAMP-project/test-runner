package eu.stamp.project.testrunner.runner.test;

import java.io.PrintWriter;
import java.io.Serializable;
import java.io.StringWriter;

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
    public final String stackTrace;

    public Failure(String testCaseName, Throwable exception) {
        this.testCaseName = testCaseName;
        this.fullQualifiedNameOfException = exception.getClass().getName();
        this.messageOfFailure = exception.getMessage();
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        this.stackTrace = sw.toString(); // stack trace as a string
    }

    @Override
    public String toString() {
        return "Failure{" +
                "testCaseName='" + testCaseName + '\'' +
                ", fullQualifiedNameOfException='" + fullQualifiedNameOfException + '\'' +
                ", messageOfFailure='" + messageOfFailure + '\'' +
                ", stackTrace=" + stackTrace +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Failure failure = (Failure) o;

        if (testCaseName != null ? !testCaseName.equals(failure.testCaseName) : failure.testCaseName != null)
            return false;
        if (fullQualifiedNameOfException != null ? !fullQualifiedNameOfException.equals(failure.fullQualifiedNameOfException) : failure.fullQualifiedNameOfException != null)
            return false;
        if (messageOfFailure != null ? !messageOfFailure.equals(failure.messageOfFailure) : failure.messageOfFailure != null)
            return false;
        return stackTrace != null ? stackTrace.equals(failure.stackTrace) : failure.stackTrace == null;
    }

    @Override
    public int hashCode() {
        int result = testCaseName != null ? testCaseName.hashCode() : 0;
        result = 31 * result + (fullQualifiedNameOfException != null ? fullQualifiedNameOfException.hashCode() : 0);
        result = 31 * result + (messageOfFailure != null ? messageOfFailure.hashCode() : 0);
        result = 31 * result + (stackTrace != null ? stackTrace.hashCode() : 0);
        return result;
    }
}