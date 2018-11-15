

package junit5;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestSuiteExample2 {

    @Test
    public void test3() {
        example.Example ex = new example.Example();
        String s = "abcd";
        assertEquals('d', ex.charAt(s, ((s.length()) - 1)));
    }

    @Test
    public void test4() {
        example.Example ex = new example.Example();
        String s = "abcd";
        assertEquals('d', ex.charAt(s, 12));
    }

    @Test
    public void test7() {
        example.Example ex = new example.Example();
        assertEquals('c', ex.charAt("abcd", 2));
    }

    @Test
    public void test8() {
        example.Example ex = new example.Example();
        assertEquals('b', ex.charAt("abcd", 1));
    }

    @Test
    public void test9() {
        example.Example ex = new example.Example();
        assertEquals('f', ex.charAt("abcdefghijklm", 5));
    }

    @Test
    public void test2() {
        example.Example ex = new example.Example();
        assertEquals('d', ex.charAt("abcd", 3));
    }
}

