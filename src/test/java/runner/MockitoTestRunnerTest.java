package runner;

import listener.TestListener;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 02/07/17.
 */
public class MockitoTestRunnerTest {

    @Test
    public void testOnClass() throws Exception {
        TestRunner runner = new MockitoTestRunner(
                new String[]{
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT.jar",
                        "src/test/resources/MockitoDemo-1.0-SNAPSHOT-tests.jar",
                });
        TestListener results = runner.run("info.sanaulla.dal.BookDALTest");
        System.out.println(results);
    }
}
