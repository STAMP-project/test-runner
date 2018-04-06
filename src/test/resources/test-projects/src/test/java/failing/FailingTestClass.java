package failing;

import org.junit.Ignore;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

/**
 * Created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 06/04/18
 */
public class FailingTestClass {

    @Test
    public void testPassing() throws Exception {
        assertTrue(true);
    }

    @Test
    public void testAssumptionFailing() throws Exception {
        assumeTrue(false);
    }

    @Test
    public void testFailing() throws Exception {
        assertTrue(false);
    }

    @Ignore
    @Test
    public void testIgnored() throws Exception {
    }
}
