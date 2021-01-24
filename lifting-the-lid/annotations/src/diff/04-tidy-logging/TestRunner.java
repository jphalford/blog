package org.example.app;

public class TestRunner {
  public static void main(String[] args) {
    IntCalculatorTest intCalculatorTest = new IntCalculatorTest();

    final String testClassName = testInstance.getClass().getSimpleName();
    System.out.println("RUNNING - " + testClassName);

    for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
      String testMethodName = declaredMethod.getName();
      if (declaredMethod.isAnnotationPresent(Test.class)) {
        try {
          declaredMethod.invoke(testInstance);
          System.out.println(String.format("PASSED - %s#%s", testClassName, testMethodName));
        } catch (InvocationTargetException e) {
          if (e.getTargetException() instanceof RuntimeException) {
            System.out.println(String.format("FAILED - %s#%s", testClassName, testMethodName));
          } else {
            throw e;
          }
        }
      }
    }
  }
}