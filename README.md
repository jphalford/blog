# Lifting the Lid

> "Just because you don't understand something doesn't mean that it's nonsense"
>
> Lemony Snicket 

A modern Java application is usually a tangle of frameworks whose annotations make even the cleanest code look like the definition of
nonsense to the casual observer. When run, a few lines of code can be transformed into an application that takes requests,  
deserialises their contents and updates databases. This wouldn't be a problem, but when the application vanishes without so much as an 
`@Goodbye` the programmer needs to work out what went wrong...


> "What I cannot create, I do not understand"
>
> Richard Feynman 

Using minimal libraries and abstractions, Lifting the Lid implements basic versions of common frameworks and shows you how
they leverage core features of the Java Language. With this understanding, you'll have the context required to spend
less time interpreting error messages and fixing your coding errors. Ultimately, you will be able to surf the stacktrace 
in the debugger and orient yourself within these frameworks.  


# Lifting the Lid: Unit Testing Frameworks (Annotations and Reflection)

## A Custom Solution

Once upon a time, there was a programmer who decided today was a good day to write a Java calculator application (it was 
overcast with a westerly breeze). Keen to expand their craft, they decided to practice Test Driven Development. 

The programmer was untrusting of others and thought Frameworks were for other people. They decided
that for their tests, a test would be represented by a method in a test class and would be considered to have 
passed if no exceptions are thrown when invoking the test method.  
 
Half an hour later, and the programmer had some basic tests for sum and minus: 
 
`IntCalculatorTest`
```java
package org.example.app;

public class IntCalculatorInitialTest {
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
```

Alongside a test runner which would call each test method and print out pass or fail depending on whether the
test method thew an exception:

`TestRunner`
```java
package org.example.app;

public class TestRunner {
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
}
```

`Output`
```
RUNNING - IntCalculatorFirstAnnotationTest
PASSED - IntCalculatorFirstAnnotationTest#testSum
FAILED - IntCalculatorFirstAnnotationTest#testMinus
```

However, the programmer was frustrated. The calculator was going well, the test results could be seen in the console, 
but the test runner was a mess. Running a test and reporting the result required more lines of code than to specify the
test and there was plenty of repetition; something had to change.

### Reflection

Luckily, the programmer had recently been browsing the [Java Reflection Tutorial](https://docs.oracle.com/javase/tutorial/reflect/index.html)
and understood that the Reflection API allows the caller to examine properties of classes, their methods, fields etc
and even make changes to the original declarations of those classes and invoke their methods.

In particular:
- [Class Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html) - A `Class` instance is 
created by the Java Virtual Machine (JVM) for each class that is loaded by the application; it can be accessed statically via `MyClass.class` or from
an instance using the `getClass()` method provided by the top-level `Object` class. This is often used in application code simply to retrieve the class name for use by a logging framework.
However, the `Class` class exposes a wealth of information about the declaration of the class provided by the programmer. 
- [Method Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html) A `Method` instance
provides information on the declaration of a method such as it's return type, parameters and annotations and allows a caller
to invoke the method.



After perusing through the [Class Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html) the programmer formed a plan:
1. [`Class#getDeclaredMethods()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html#getDeclaredMethods()) would be used
to obtain the methods declared by `IntCalculatorTest` as `Method` instances.
1. [`Method#invoke()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html#invoke(java.lang.Object,java.lang.Object...)) 
would then be used to invoke the test method.
1. [`Method#getName()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html#getName()) would be used to print the test name
in the test results

In a jiffy, the test runner was transformed:
```java
public class TestRunner {

    public static void main(String[] args) throws InvocationTargetException {
        IntCalculatorFirstAnnotationTest testInstance = new IntCalculatorFirstAnnotationTest();

        System.out.println("RUNNING - IntCalculatorFirstAnnotationTest");

        for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
            String testMethodName = declaredMethod.getName();
            if (testMethodName.startsWith("test")) {
                // We've found a test method
                try {
                    declaredMethod.invoke(testInstance);
                    System.out.println(String.format("PASSED - IntCalculatorFirstAnnotationTest#%s", testMethodName));
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof RuntimeException) {
                        System.out.println(String.format("FAILED - IntCalculatorFirstAnnotationTest#%s", testMethodName));
                    } else {
                        throw e;
                    }
                }
            }
        }
    }
}
```

> TODO - A brief explainer on InvocationTargetException
>


The programmer was a lot happier, now they could add further test methods, and as long as they started
with the word "test" they would automatically be picked up and run by the test runner. However, there was a problem.
This wasn't a very flexible scheme, and the programmer wasn't sure they liked the repetition of "test" in the test method
names. Instead, it would be much better if there was a way to label the methods as tests. 


### Annotations to the Rescue

This sounded familiar, and sure enough after flicking through the Java Tutorials the programmer found the 
[Annotations](https://docs.oracle.com/javase/tutorial/java/annotations/index.html) section. Annotations are 
used to provide metadata about the code and can be queried at runtime by an application. So the programmer decided 
to create a `@Test` annotation to mark the test methods. Then, they could update their code to check for mehtods
with this annotation rather than starting with the word "test".

Annotations are specified using the `@interface` keyword and can themselves have annotations applied to 
provide further information. The programmer decided that for now, two annotations were needed
1. [`@Retention`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/annotation/Retention.html) would
  be used to specify that the `@Test` annotation should be available at runtime so that the test runner can find the methods.
1. [`@Target`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/annotation/Target.html) would be used to
restrict the `@Test` annotation to methods, because the only thing the programmer trusted less than frameworks
was their future self. This restriction would prevent them from accidentally adding the annotation to a type, constructor, field 
or [any other places an annotation can be used](https://docs.oracle.com/javase/specs/jls/se11/html/jls-9.html#jls-9.6.4.1).

With that settled, the programmer added the test annotation and set about using it in their tests:

`Test`
```java
package org.example.test;

import ...;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Test {
}
```
 
`IntCalculatorTest`
```java
package org.example.app;

import org.example.test.Test;

public class IntCalculatorFirstAnnotationTest {

    @Test
    void testSum() {
        IntCalculator intCalculator = new IntCalculator();
        assertEquals(2, intCalculator.sum(1, 1));
    }

    @Test
    void testMinus() {
        IntCalculator intCalculator = new IntCalculator();
        assertEquals(0, intCalculator.minus(1, 1));
    }

    public void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new RuntimeException(String.format("%d != %d", 0, actual));
        }
    }
}
```

Now, it was time to update the test runner. After going back to the [Method Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html) 
the programmer decided that [`isAnnotationPresent()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/AccessibleObject.html#isAnnotationPresent(java.lang.Class)) 
could be used to replace the check for a test method:

```java
public class TestRunner {

    public static void main(String[] args) throws InvocationTargetException {
        IntCalculatorFirstAnnotationTest testInstance = new IntCalculatorFirstAnnotationTest();

        System.out.println("RUNNING - IntCalculatorFirstAnnotationTest");

        for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
            String testMethodName = declaredMethod.getName();
            if (declaredMethod.isAnnotationPresent(Test.class)) {
                try {
                    declaredMethod.invoke(testInstance);
                    System.out.println(String.format("PASSED - IntCalculatorFirstAnnotationTest#%s", testMethodName));
                } catch (InvocationTargetException e) {
                    if (e.getTargetException() instanceof RuntimeException) {
                        System.out.println(String.format("FAILED - IntCalculatorFirstAnnotationTest#%s", testMethodName));
                    } else {
                        throw e;
                    }
                }
            }
        }
    }
}
```

The programmer stood back and admired their work; they were definitely getting somewhere. The test runner was
almost independent of the test class being run. The only sticking points were the instantiation of `IntCalculatorFirstAnnotationTest` and
the name of the test class in the RUNNING/PASSED/FAILED test logs.  

Next, since the programmer had used [`Class#getSimpleName()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html#getSimpleName()) before, they decided to tidy up the test result logging.

```java
public class TestRunner {

    public static void main(String[] args) throws InvocationTargetException {
        IntCalculatorFirstAnnotationTest testInstance = new IntCalculatorFirstAnnotationTest();

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
```

Following this refactoring, the programmer could extract the test into two methods to prove that the code they had written
was independent of `IntCalculatorFirstAnnotationTest`:

```java
public class TestRunner {

    public static void main(String[] args) throws InvocationTargetException {
        IntCalculatorFirstAnnotationTest testInstance = new IntCalculatorFirstAnnotationTest();

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
```


It was approaching afternoon tea and with the smell of scones heavy in the air the programmer decided to have
one last look at their tests... 

### Removing repetition

The programmer couldn't quite put their finger on it but there was something wrong with their tests, they just 
didn't look "modern".

```java
    @Test
    public void testSum() {
        //...
    }
```

In a flash, the programmer saw the problem; access modifiers are so 2006. Surely the gauche `public` modifier could
be removed. Since the programmer wanted to modify the accessibility of a method they had a look though the [Method Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html)
and discovered a promising method; [Method#setAccessible(boolean)](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html#setAccessible(boolean)):
  
> Set the accessible flag for this reflected object to the indicated boolean value. A value of true indicates that the reflected object should suppress checks for Java language access control when it is used.


Sure enough, once the `runTestMethod` was updated to call `declaredMethod.setAccessible(true)`, the public keyword could be
removed from the tests:

```java
public class TestRunner {

    public static void main(String[] args) throws InvocationTargetException {
        IntCalculatorFirstAnnotationTest testInstance = new IntCalculatorFirstAnnotationTest();

        runTest(testInstance);
    }

    private void runTest(Object testInstance) throws InvocationTargetException {
       // ...
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
```

`IntCalculatorTest`
```java
package org.example.app;

import org.example.test.Test;

public class IntCalculatorFirstAnnotationTest {

    @Test
    void testSum() {
        IntCalculator intCalculator = new IntCalculator();
        assertEquals(2, intCalculator.sum(1, 1));
    }

    @Test
    void testMinus() {
        IntCalculator intCalculator = new IntCalculator();
        assertEquals(0, intCalculator.minus(1, 1));
    }

    public void assertEquals(int expected, int actual) {
        if (expected != actual) {
            throw new RuntimeException(String.format("%d != %d", 0, actual));
        }
    }
}
```



## Epilogue

As the days wore on, the programmer became obsessed with writing a fully featured test framework:
- defining `@Before` and `@After` annotations and using the same techniques as above to 
annotate, discover and invoke the methods before and after each test.
- abstracting the test results to support configurable reporting levels and formats.
- expanding the number of `assertEquals` methods to cover further types.  
- discovering all the test classes and running them automatically (this is actually surprisingly 
difficult in Java and will probably be the subject of a future Lifting the Lid)

With these features complete, they confidently proclaimed that this was indeed a superior test library.


## JUnit 5

Now that we have covered the basics of writing a test framework let's take a short look at a common java framework: JUnit 5

- stack trace of a running test, walk up show similar areas



# Other ideas
- Dependency Injection
- Lombok
- The Classpath (use java cmd directly, maybe construct classpath as per maven/gradle for tests)

# Q's
- JLS links or Java tutorial links?
- How to make changes in code more obvious?
- Annotation type member example for class name?
- Generic Test Runner section?
    - I think leave to The Classpath - add a teaser
    - use forName + getDeclaredConstructor to initialise the test class
    - demonstrate that no compile time reference so can be moved out of the source set