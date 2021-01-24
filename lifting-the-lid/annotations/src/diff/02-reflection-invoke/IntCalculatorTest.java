package org.example.app;

public class IntCalculatorTest {

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
