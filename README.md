# Lifting the Lid

> "Just because you don't understand something doesn't mean that it's nonsense"
>
> Lemony Snicket 

A modern Java application is usually a tangle of frameworks whose annotations make even the cleanest code look like the definition of
nonesense to the casual observer. To exacerbate matters, when you actually run the application some kind of arcane witchcraft 
takes place and before you know it your application is taking requests, reading messages and shoving things into databases 
before it vanishes without so much as an `@Goodbye` and you need to work out what went wrong.


> "What I cannot create, I do not understand"
>
> Richard Feynman 

Using minimal libraries and abstractions, Lifting the Lid implements basic versions of these frameworks and shows you how
they leverage core features of the Java Language. With this understanding, you'll have the context required to spend
less time to interpreting error messages and fix your coding errors. Ultimately, you will be able to surf the stacktrace 
in the debugger and orient yourself within these frameworks.  


# Lifting the Lid: Unit Testing Frameworks (Annotations and Reflection)

## A Custom Solution

Once upon a time, there was a programmer who decided today was a good day to write a Java calculator application (it was 
overcast with a westerly breeze). Keen to expand their craft, this time they decided to practice Test 
Driven Development. 

The programmer was untrusting of others and thought Frameworks were things that other people used. They decided
that for their test framework, a test would be represented by a method in the test class and would be considered to have 
passed if no exception was thrown when it was invoked.  
 
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


However, the programmer was frustrated. The calculator was going well and the test results could be seen in the console, 
but the test runner was a mess. With so much repeated code and running the tests requiring more lines of code than to specify them
something had to change.

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
1. [`Class#getDeclaredMehtods`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html#getDeclaredMethods()) would be used
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

The programmer was a lot happier, now they could add further test methods, and as long as they started
with the word "test" they would automatically be picked up and run by the test runner. However, there was a problem,
this wasn't a very flexible scheme and the programmer wasn't sure they liked the repetition of "test" in the test method
names. 


### Annotations to the Rescue
- Add Test Annotation

### A Generic Test Runner 
- use forName to initialise the test class
- demonstrate that no compile time reference so can be moved out of the source set

### Removing repetition
- remove need for public on the test methods

### The logical conclusion
Talk a bit about BeforeEach/All and other common test framework features and how they could be implemented
abstrations for test and results to allow them to be displayed in different ways etc

## JUnit 5

Now that we have covered the basics of writing a test framework let's take a short look at a common java framework: JUnit 5

- stack trace of a running test, walk up show similar areas



# Other ideas
- Dependency Injection
- Lombok