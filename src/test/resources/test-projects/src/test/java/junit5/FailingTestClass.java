package junit5;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

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

    /*
    @Test
    public void testAssumptionFailing() throws Exception {
        assumeTrue(false);
    }
    */

    @Test
    public void testFailing() throws Exception {
        assertTrue(false);
    }

    @Disabled
    @Test
    public void testIgnored() throws Exception {
    }
}
