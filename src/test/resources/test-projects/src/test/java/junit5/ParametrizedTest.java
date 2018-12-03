package junit5;

import example.Example;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * created by Benjamin DANGLOT
 * benjamin.danglot@inria.fr
 * on 30/11/18
 */
public class ParametrizedTest {

    @ParameterizedTest
    @CsvSource({
            "abcd, 1, b",
            "abcd, 5, d",
            "abcd, -1, a",
            "abcd, 0, a",
    })

    public void test(String string, int index, String expected) {
        assertEquals(expected.charAt(0), new Example().charAt(string, index));
    }

}
