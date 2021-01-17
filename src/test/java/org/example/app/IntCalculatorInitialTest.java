package org.example.app;

public class IntCalculatorInitialTest {

    public static void main(String[] args) {
        IntCalculatorInitialTest intCalculatorTest = new IntCalculatorInitialTest();

        System.out.println("IntCalculatorTest");

        try {
            intCalculatorTest.testSum();
            System.out.println("PASSED - IntCalculatorTest#testSum");
        } catch (Exception e) {
            System.out.println("FAILED - IntCalculatorTest#testSum");
        }

        try {
            intCalculatorTest.testMinus();
            System.out.println("PASSED - IntCalculatorTest#testMinus");
        } catch (Exception e) {
            System.out.println("FAILED - IntCalculatorTest#testMinus");
        }
    }

    public void testSum() {
        IntCalculator intCalculator = new IntCalculator();
        assertEquals(2, intCalculator.sum(1, 1));
    }

    public void testMinus() {
        IntCalculator intCalculator = new IntCalculator();
        assertEquals(0, intCalculator.minus(1, 1));
    }

    public void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new RuntimeException(String.format("%d != %d", 0, actual));
        }
    }
}
