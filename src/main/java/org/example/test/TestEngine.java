package org.example.test;


import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class TestEngine {
    public void runTestClass(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<?> testClass = Class.forName(className);
        Object testInstance = testClass.getDeclaredConstructor().newInstance();

        final String testClassName = testInstance.getClass().getSimpleName();
        System.out.println("RUNNING - " + testClassName);

        for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Test.class)) {
                runTestMethod(testInstance, testClassName, declaredMethod);
            }
        }
    }

    private void runTestMethod(Object testInstance, String testClassName, Method declaredMethod) throws IllegalAccessException, InvocationTargetException {
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
