package eu.stamp_project.testrunner.listener.junit5;

import eu.stamp_project.testrunner.listener.impl.TestListenerImpl;
import eu.stamp_project.testrunner.runner.Failure;
import org.junit.platform.engine.TestExecutionResult;
import org.junit.platform.engine.reporting.ReportEntry;
import org.junit.platform.engine.support.descriptor.MethodSource;
import org.junit.platform.launcher.TestExecutionListener;
import org.junit.platform.launcher.TestIdentifier;
import org.junit.platform.launcher.TestPlan;

import java.util.function.Function;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 13/11/18
 */
public class JUnit5TestListener extends TestListenerImpl implements TestExecutionListener {

    private static final long serialVersionUID = -7818892670028055637L;

    protected transient final Function<TestIdentifier, String> toString = testIdentifier ->
            ((MethodSource) testIdentifier.getSource().get()).getMethodName();

    protected transient final Function<TestIdentifier, String> toClassName = testIdentifier ->
            ((MethodSource) testIdentifier.getSource().get()).getClassName();


    @Override
    public void testPlanExecutionStarted(TestPlan testPlan) {
        // There is something to do?
    }

    @Override
    public void testPlanExecutionFinished(TestPlan testPlan) {
        // There is something to do?
    }

    @Override
    public void dynamicTestRegistered(TestIdentifier testIdentifier) {
        // There is something to do?
    }

    @Override
    public void executionSkipped(TestIdentifier testIdentifier, String reason) {
        if (testIdentifier.isTest()) {
            this.getIgnoredTests().add(this.toString.apply(testIdentifier));
        }
    }

    @Override
    public void executionStarted(TestIdentifier testIdentifier) {
        if (testIdentifier.isTest()) {
            this.getRunningTests().add(this.toString.apply(testIdentifier));
        }
    }

    @Override
    public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
        if (testIdentifier.isTest()) {
            switch (testExecutionResult.getStatus()) {
                case FAILED:
                    this.getFailingTests().add(
                            new Failure(
                                    this.toString.apply(testIdentifier),
                                    this.toClassName.apply(testIdentifier),
                                    testExecutionResult.getThrowable().get()
                            )
                    );
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void reportingEntryPublished(TestIdentifier testIdentifier, ReportEntry entry) {
        // There is something to do?
    }

}
