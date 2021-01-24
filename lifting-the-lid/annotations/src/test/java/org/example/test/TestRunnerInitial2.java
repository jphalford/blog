package org.example.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestRunnerInitial2 {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        Class<?> testClass = Class.forName("org.example.app.IntCalculatorFirstAnnotationTest");
        Object testInstance = testClass.getDeclaredConstructor().newInstance();

        final String testClassName = testInstance.getClass().getSimpleName();
        System.out.println("RUNNING - " + testClassName);

        for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Test.class)) {
                // We've found a test method
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
    }
}
