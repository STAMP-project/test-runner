package example;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/11/18
 */
@RunWith(Parameterized.class)
public class ParametrizedTest {

    private String string;

    private int index;

    private char expected;

    public ParametrizedTest(String string, int index, char expected) {
        this.string = string;
        this.index = index;
        this.expected = expected;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> strategies() {
        return Arrays.asList(
                new Object[]{
                        "abcd",
                        1,
                        'b'
                },
                new Object[]{
                        "abcd",
                        5,
                        'd'
                },
                new Object[]{
                        "abcd",
                        -1,
                        'a'
                },
                new Object[]{
                        "abcd",
                        0,
                        'a'
                }
        );
    }

    @Test
    public void test() {
        assertEquals(this.expected, new Example().charAt(this.string, this.index));
    }

}
