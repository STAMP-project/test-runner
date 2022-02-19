

package example;


import junit.framework.TestCase;

public class TestSuiteExample extends TestCase {

    
    public void test3() {
        example.Example ex = new example.Example();
        java.lang.String s = "abcd";
        assertEquals('d', ex.charAt(s, ((s.length()) - 1)));
    }

    
    public void test4() {
        example.Example ex = new example.Example();
        java.lang.String s = "abcd";
        assertEquals('d', ex.charAt(s, 12));
    }

    
    public void test7() {
        example.Example ex = new example.Example();
        assertEquals('c', ex.charAt("abcd", 2));
    }

    
    public void test8() {
        example.Example ex = new example.Example();
        assertEquals('b', ex.charAt("abcd", 1));
    }

    
    public void test9() {
        example.Example ex = new example.Example();
        assertEquals('f', ex.charAt("abcdefghijklm", 5));
    }

    
    public void test2() {
        example.Example ex = new example.Example();
        assertEquals('d', ex.charAt("abcd", 3));
    }

    
    public void copyOftest2() {
        example.Example ex = new example.Example();
        assertEquals('d', ex.charAt("abcd", 3));
    }
}

