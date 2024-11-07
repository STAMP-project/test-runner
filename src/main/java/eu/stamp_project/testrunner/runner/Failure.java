package eu.stamp_project.testrunner.runner;

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

    private final static long serialVersionUID = 4319480863941757524L;

    public final String testCaseName;
    public final String testClassName;
    public final String fullQualifiedNameOfException;
    public final String messageOfFailure;
    public final String stackTrace;
    public SerializableThrowable throwable; // Throwable is not present if Failure is read from surefire report

    public Failure(String testCaseName, String testClassName, Throwable exception) {
        this.testCaseName = testCaseName;
        this.fullQualifiedNameOfException = exception.getClass().getName();
        this.messageOfFailure = exception.getMessage();
        this.testClassName = testClassName;
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        exception.printStackTrace(pw);
        this.stackTrace = sw.toString(); // stack trace as a string
        this.throwable = new SerializableThrowable(exception);
    }

    public Failure(String testCaseName, String testClassName, String fullQualifiedNameOfException, String messageOfFailure, String stackTrace) {
        this.testCaseName = testCaseName;
        this.fullQualifiedNameOfException = fullQualifiedNameOfException;
        this.messageOfFailure = messageOfFailure;
        this.testClassName = testClassName;
        this.stackTrace = stackTrace;
    }

    @Override
    public String toString() {
        return this.testCaseName + "(" + this.testClassName + "): " + this.messageOfFailure;
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
        return messageOfFailure != null ? messageOfFailure.equals(failure.messageOfFailure) : failure.messageOfFailure == null;
    }

    @Override
    public int hashCode() {
        int result = testCaseName != null ? testCaseName.hashCode() : 0;
        result = 31 * result + (fullQualifiedNameOfException != null ? fullQualifiedNameOfException.hashCode() : 0);
        result = 31 * result + (messageOfFailure != null ? messageOfFailure.hashCode() : 0);
        return result;
    }


    public static class SerializableThrowable implements Serializable {

        private static final long serialVersionUID = 2988580623727952827L;

        public final String className;
        public final String message;
        public final StackTraceElement[] stackTrace;

        public SerializableThrowable(Throwable throwable) {
            this.className = throwable.getClass().getName();
            this.message = throwable.getMessage();
            this.stackTrace = throwable.getStackTrace();
        }
    }

}