package org.example.test;

import java.lang.reflect.InvocationTargetException;

public class TestRunner {
    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException, ClassNotFoundException, NoSuchMethodException, InstantiationException {
        String className = "org.example.app.IntCalculatorFirstAnnotationTest";
        new TestEngine().runTestClass(className);
    }
}
