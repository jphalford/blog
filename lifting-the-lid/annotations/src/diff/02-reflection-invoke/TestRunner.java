package org.example.app;

public class TestRunner {
  public static void main(String[] args) throws Exception {
    IntCalculatorTest intCalculatorTest = new IntCalculatorTest();

    System.out.println("IntCalculatorTest");

    for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
      String testMethodName = declaredMethod.getName();
      if (testMethodName.startsWith("test")) {
        // We've found a test method
        try {
          declaredMethod.invoke(testInstance);
          System.out.println(String.format("PASSED - IntCalculatorTest#%s", testMethodName));
        } catch (InvocationTargetException e) {
          if (e.getTargetException() instanceof RuntimeException) {
            System.out.println(String.format("FAILED - IntCalculatorTest#%s", testMethodName));
          } else {
            throw e;
          }
        }
      }
    }
  }
}