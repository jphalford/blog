package org.example.app;

public class TestRunner {
  public static void main(String[] args) throws InvocationTargetException {
    Class<?> testClass = Class.forName("org.example.app.IntCalculatorTest");
    Object testInstance = testClass.getDeclaredConstructor().newInstance();

    runTest(testInstance);
  }

  private void runTest(Object testInstance) throws InvocationTargetException {
    final String testClassName = testInstance.getClass().getSimpleName();
    System.out.println("RUNNING - " + testClassName);

    for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
      if (declaredMethod.isAnnotationPresent(Test.class)) {
        runTestMethod(testInstance, testClassName, declaredMethod);
      }
    }
  }

  private void runTestMethod(Object testInstance, String testClassName, Method declaredMethod) throws InvocationTargetException {
    String testMethodName = declaredMethod.getName();
    try {
      if (!declaredMethod.canAccess(testInstance)) {
        declaredMethod.setAccessible(true);
      }
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