package org.example.app;

public class TestRunner {
  public static void main(String[] args) {
    IntCalculatorTest intCalculatorTest = new IntCalculatorTest();

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
}