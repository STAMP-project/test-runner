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
 * on 26/11/18
 */
@RunWith(Parameterized.class)
public class ParametrizedTestSuiteExample {

    private String string;

    public ParametrizedTestSuiteExample(String string) {
        this.string = string;
    }


    @Parameterized.Parameters
    public static Collection<Object[]> strategies() {
        return Arrays.asList(
                new Object[]{
                        "abcd",
                },
                new Object[]{
                        "abcd",
                });
    }

    @Test
    public void test3() {
        example.Example ex = new example.Example();
        assertEquals('d', ex.charAt(this.string, ((this.string.length()) - 1)));
    }

    @Test
    public void test4() {
        example.Example ex = new example.Example();
        assertEquals('d', ex.charAt(this.string, 12));
    }

    @Test
    public void test7() {
        example.Example ex = new example.Example();
        assertEquals('c', ex.charAt(this.string, 2));
    }

    @Test
    public void test8() {
        example.Example ex = new example.Example();
        assertEquals('b', ex.charAt(this.string, 1));
    }

    @Test
    public void test2() {
        example.Example ex = new example.Example();
        assertEquals('d', ex.charAt(this.string, 3));
    }


}
