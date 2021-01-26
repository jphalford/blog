---
layout: post
title:  "Lifting the Lid: Unit Testing Frameworks"
date:   2021-01-20 20:18:13 +0000
categories: lifting-the-lid java
excerpt: >- 
  Learn how to use Annotations and Reflection to write a basic Unit Testing Framework! 

---

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


# Unit Testing Frameworks (Annotations and Reflection

If you would like to follow along, the initial project and final solution are available at [jphalford/lifting-the-lid-unit-testing-framework](https://github.com/jphalford/lifting-the-lid-unit-testing-framework).

## A Custom Solution

Once upon a time, there was a programmer who decided today was a good day to write a Java calculator application (it was 
overcast with a westerly breeze). Keen to expand their craft, they decided to practice Test Driven Development. 

The programmer was untrusting of others and thought Frameworks were for other people. They decided
that for their tests, a test would be represented by a method in a test class and would be considered to have 
passed if no exceptions are thrown when invoking the test method.  
 
Half an hour later, and the programmer had some basic tests for sum and minus: 

```diff
diff --git a/IntCalculatorInitialTest.java b/IntCalculatorInitialTest.java 
new file mode 100644
--- /dev/null                        
+++ IntCalculatorInitialTest.java       
@@ -0,0 +1,20 @@
+package org.example.app;
+
+public class IntCalculatorInitialTest {
+
+  public void testSum() {
+    IntCalculator intCalculator = new IntCalculator();
+    assertEquals(2, intCalculator.sum(1, 1));
+  }
+
+  public void testMinus() {
+    IntCalculator intCalculator = new IntCalculator();
+    assertEquals(0, intCalculator.minus(1, 1));
+  }
+
+  public void assertEquals(int expected, int actual) {
+    if (expected != actual) {
+      throw new RuntimeException(String.format("%d != %d", 0, actual));
+    }
+  }
+}
```

Alongside a test runner which would call each test method and print out pass or fail depending on whether the
test method thew an exception:

```diff
diff -uN 00-empty/TestRunner.java 01-initial/TestRunner.java
new file mode 100644
--- TestRunner.java    1970-01-01 00:00:00.000000000 +0000
+++ TestRunner.java  2021-01-24 19:02:53.483116100 +0000
@@ -0,0 +1,23 @@
+package org.example.app;
+
+public class TestRunner {
+  public static void main(String[] args) {
+    IntCalculatorInitialTest intCalculatorTest = new IntCalculatorInitialTest();
+
+    System.out.println("IntCalculatorTest");
+
+    try {
+      intCalculatorTest.testSum();
+      System.out.println("PASSED - IntCalculatorTest#testSum");
+    } catch (Exception e) {
+      System.out.println("FAILED - IntCalculatorTest#testSum");
+    }
+
+    try {
+      intCalculatorTest.testMinus();
+      System.out.println("PASSED - IntCalculatorTest#testMinus");
+    } catch (Exception e) {
+      System.out.println("FAILED - IntCalculatorTest#testMinus");
+    }
+  }
+}

```

Triggering the test runner, the programmer recieved the following output:

```shell
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
```diff
diff -uN 01-initial/TestRunner.java 02-reflection-invoke/TestRunner.java
--- TestRunner.java  2021-01-24 19:02:53.483116100 +0000
+++ TestRunner.java  2021-01-24 19:10:19.243665900 +0000
@@ -1,23 +1,26 @@
 package org.example.app;

 public class TestRunner {
-  public static void main(String[] args) {
+  public static void main(String[] args) throws Exception {
     IntCalculatorTest intCalculatorTest = new IntCalculatorTest();

     System.out.println("IntCalculatorTest");

-    try {
-      intCalculatorTest.testSum();
-      System.out.println("PASSED - IntCalculatorTest#testSum");
-    } catch (Exception e) {
-      System.out.println("FAILED - IntCalculatorTest#testSum");
-    }
-
-    try {
-      intCalculatorTest.testMinus();
-      System.out.println("PASSED - IntCalculatorTest#testMinus");
-    } catch (Exception e) {
-      System.out.println("FAILED - IntCalculatorTest#testMinus");
+    for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
+      String testMethodName = declaredMethod.getName();
+      if (testMethodName.startsWith("test")) {
+        // We've found a test method
+        try {
+          declaredMethod.invoke(testInstance);
+          System.out.println(String.format("PASSED - IntCalculatorTest#%s", testMethodName));
+        } catch (InvocationTargetException e) {
+          if (e.getTargetException() instanceof RuntimeException) {
+            System.out.println(String.format("FAILED - IntCalculatorTest#%s", testMethodName));
+          } else {
+            throw e;
+          }
+        }
+      }
     }
   }
 }
```

> n.b. In the code above, when we `invoke` a method we must handle the three checked exceptions declared by the `invoke`
> method. One of the exceptions is of particular interest; `InvocationTargetException`. 
>
> If the method that we `invoke`
> throws an exception (such as the `RuntimeException` thrown when a test fails) then the JVM will create an `InvocationTargetException` to "wrap" the thrown exception. By inspecting `getTargetException` we can retrieve the "wrapped" exception and
> determine whether it was expected (i.e. a `RuntimeException`) and if so, mark the test as failed.
 


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


```diff
diff -uN 02-reflection-invoke/Test.java 03-test-annotation/Test.java
new file mode 100644
--- Test.java      1970-01-01 00:00:00.000000000 +0000
+++ Test.java        2021-01-14 18:42:00.771000000 +0000
@@ -0,0 +1,11 @@
+package org.example.test;
+
+import java.lang.annotation.ElementType;
+import java.lang.annotation.Retention;
+import java.lang.annotation.RetentionPolicy;
+import java.lang.annotation.Target;
+
+@Retention(RetentionPolicy.RUNTIME)
+@Target(ElementType.METHOD)
+public @interface Test {
+}
diff -uN 02-reflection-invoke/IntCalculatorTest.java 03-test-annotation/IntCalculatorTest.java
--- IntCalculatorTest.java 2021-01-24 19:28:40.346808600 +0000
+++ IntCalculatorTest.java   2021-01-24 19:28:56.170386600 +0000
@@ -1,12 +1,16 @@
 package org.example.app;

+import org.example.test.Test;
+
 public class IntCalculatorTest {

+  @Test
   public void testSum() {
     IntCalculator intCalculator = new IntCalculator();
     assertEquals(2, intCalculator.sum(1, 1));
   }

+  @Test
   public void testMinus() {
     IntCalculator intCalculator = new IntCalculator();
     assertEquals(0, intCalculator.minus(1, 1));
```

Now, it was time to update the test runner. After going back to the [Method Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html) 
the programmer decided that [`isAnnotationPresent()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/AccessibleObject.html#isAnnotationPresent(java.lang.Class)) 
could be used to replace the check for a test method:

```diff
diff -uN 02-reflection-invoke/TestRunner.java 03-test-annotation/TestRunner.java
--- TestRunner.java        2021-01-24 19:29:24.901621600 +0000
+++ TestRunner.java  2021-01-24 19:40:35.800716600 +0000
@@ -8,8 +8,7 @@

     for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
       String testMethodName = declaredMethod.getName();
-      if (testMethodName.startsWith("test")) {
-        // We've found a test method
+      if (declaredMethod.isAnnotationPresent(Test.class)) {
         try {
           declaredMethod.invoke(testInstance);
           System.out.println(String.format("PASSED - IntCalculatorTest#%s", testMethodName));
```

The programmer stood back and admired their work; they were definitely getting somewhere. The test runner was
almost independent of the test class being run. The only sticking points were the instantiation of `IntCalculatorFirstAnnotationTest` and
the name of the test class in the RUNNING/PASSED/FAILED test logs.  

Next, since the programmer had used [`Class#getSimpleName()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html#getSimpleName()) before, they decided to tidy up the test result logging.

```diff
diff -uN 03-test-annotation/TestRunner.java 04-tidy-logging/TestRunner.java
--- TestRunner.java  2021-01-24 19:29:31.943647100 +0000
+++ TestRunner.java     2021-01-24 19:30:16.774400600 +0000
@@ -4,17 +4,18 @@
   public static void main(String[] args) throws Exception {
     IntCalculatorTest intCalculatorTest = new IntCalculatorTest();

-    System.out.println("IntCalculatorTest");
+    final String testClassName = testInstance.getClass().getSimpleName();
+    System.out.println("RUNNING - " + testClassName);

     for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
       String testMethodName = declaredMethod.getName();
       if (declaredMethod.isAnnotationPresent(Test.class)) {
         try {
           declaredMethod.invoke(testInstance);
-          System.out.println(String.format("PASSED - IntCalculatorTest#%s", testMethodName));
+          System.out.println(String.format("PASSED - %s#%s", testClassName, testMethodName));
         } catch (InvocationTargetException e) {
           if (e.getTargetException() instanceof RuntimeException) {
-            System.out.println(String.format("FAILED - IntCalculatorTest#%s", testMethodName));
+            System.out.println(String.format("FAILED - %s#%s", testClassName, testMethodName));
           } else {
             throw e;
           }
```

Following this refactoring, the programmer could extract the test into two methods to prove that the code they had written
was independent of `IntCalculatorFirstAnnotationTest`:

```diff
--- TestRunner.java     2021-01-24 19:30:16.774400600 +0000
+++ TestRunner.java  2021-01-24 19:40:35.810692200 +0000
@@ -4,22 +4,30 @@
   public static void main(String[] args) throws Exception {
     IntCalculatorTest intCalculatorTest = new IntCalculatorTest();

+    runTest(intCalculatorTest);
+  }
+
+  private void runTest(Object testInstance) throws Exception {
     final String testClassName = testInstance.getClass().getSimpleName();
     System.out.println("RUNNING - " + testClassName);

     for (Method declaredMethod : testInstance.getClass().getDeclaredMethods()) {
-      String testMethodName = declaredMethod.getName();
       if (declaredMethod.isAnnotationPresent(Test.class)) {
-        try {
-          declaredMethod.invoke(testInstance);
-          System.out.println(String.format("PASSED - %s#%s", testClassName, testMethodName));
-        } catch (InvocationTargetException e) {
-          if (e.getTargetException() instanceof RuntimeException) {
-            System.out.println(String.format("FAILED - %s#%s", testClassName, testMethodName));
-          } else {
-            throw e;
-          }
-        }
+        runTestMethod(testInstance, testClassName, declaredMethod);
+      }
+    }
+  }
+
+  private void runTestMethod(Object testInstance, String testClassName, Method declaredMethod) throws Exception {
+    String testMethodName = declaredMethod.getName();
+    try {
+      declaredMethod.invoke(testInstance);
+      System.out.println(String.format("PASSED - %s#%s", testClassName, testMethodName));
+    } catch (InvocationTargetException e) {
+      if (e.getTargetException() instanceof RuntimeException) {
+        System.out.println(String.format("FAILED - %s#%s", testClassName, testMethodName));
+      } else {
+        throw e;
       }
     }
   }

```

> Note that in the `runTest` and `runTestMethod` methods, there are no references to `IntCalculatorTest`.
> These methods are now independent of our application code and tests and could be moved elsewhere (such as a dedicated test libary).

Finally, the programmer decided to look at the instantiation of the test class itself via `new IntCalculatorFirstAnnotationTest()`.
If reflection could be used to invoke a method, surely it could be used to invoke a constructor. The programmer revisited the 
[Class Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html):
1. [`Class#getDeclaredConstructor()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/Class.html#getDeclaredConstructor()) would be used
to obtain the default (i.e. no arguments) constructor.  
1. [`Constructor#newInstance()`](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html#invoke(java.lang.Object,java.lang.Object...)) 
would then be used to invoke the constructor


```diff
diff -uN 05-extract-methods/TestRunner.java 06-extract-constructor/TestRunner.java
--- TestRunner.java  2021-01-24 19:40:35.810692200 +0000
+++ TestRunner.java      2021-01-26 18:07:50.481474100 +0000
@@ -2,9 +2,10 @@

 public class TestRunner {
   public static void main(String[] args) throws Exception {
-    IntCalculatorTest intCalculatorTest = new IntCalculatorTest();
+    Class<?> testClass = Class.forName("org.example.app.IntCalculatorTest");
+    Object testInstance = testClass.getDeclaredConstructor().newInstance();

-    runTest(intCalculatorTest);
+    runTest(testInstance);
   }

   private void runTest(Object testInstance) throws Exception {

``` 

> A `Constructor` is not the same as a `Method` and has its own type in Java. If our constructor had arguments, or the
> Test class had multiple constructors, the desired constructor can be retrieved by passing its argument types into
> `Class#getDeclaredConstructor()` and argument values into `Class#newInstance()`.
 


It was approaching afternoon tea and with the smell of scones heavy in the air the programmer decided to have
one last look at their tests... 

### Removing repetition

The programmer couldn't quite put their finger on it but there was something wrong with their tests, they just 
didn't look "modern".

```java     
public class IntCalculatorTest {
  @Test
  public void testSum() {
    //...
  }
}
```

In a flash, the programmer saw the problem; access modifiers are so 2006. Surely the gauche `public` modifier could
be removed. Since the programmer wanted to modify the accessibility of a method they had a look though the [Method Javadoc](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html)
and discovered a promising method; 
> [Method#setAccessible(boolean)](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/lang/reflect/Method.html#setAccessible(boolean)):  
> Set the accessible flag for this reflected object to the indicated boolean value. A value of true indicates that the reflected object should suppress checks for Java language access control when it is used.


Sure enough, once the `runTestMethod` was updated to call `declaredMethod.setAccessible(true)`, the public keyword could be
removed from the tests:

```diff
diff -uN 05-extract-methods/TestRunner.java 06-remove-repetition/TestRunner.java
--- TestRunner.java  2021-01-24 19:30:46.805758500 +0000
+++ TestRunner.java  2021-01-24 19:31:28.178498700 +0000
@@ -22,6 +22,9 @@
   private void runTestMethod(Object testInstance, String testClassName, Method declaredMethod) throws Exception {
     String testMethodName = declaredMethod.getName();
     try {
+      if (!declaredMethod.canAccess(testInstance)) {
+        declaredMethod.setAccessible(true);
+      }
       declaredMethod.invoke(testInstance);
       System.out.println(String.format("PASSED - %s#%s", testClassName, testMethodName));
     } catch (InvocationTargetException e) {
diff -uN 05-extract-methods/IntCalculatorTest.java 06-remove-repetition/IntCalculatorTest.java
--- IntCalculatorTest.java   2021-01-24 19:28:56.170000000 +0000
+++ IntCalculatorTest.java 2021-01-24 19:31:43.329204000 +0000
@@ -5,13 +5,13 @@
 public class IntCalculatorTest {

   @Test
-  public void testSum() {
+  void testSum() {
     IntCalculator intCalculator = new IntCalculator();
     assertEquals(2, intCalculator.sum(1, 1));
   }

   @Test
-  public void testMinus() {
+  void testMinus() {
     IntCalculator intCalculator = new IntCalculator();
     assertEquals(0, intCalculator.minus(1, 1));
   }
```




### Epilogue

As the days wore on, the programmer became obsessed with writing a fully featured test framework:
- defining `@Before` and `@After` annotations and using the same techniques as above to 
annotate, discover and invoke the methods before and after each test.
- abstracting the test results to support configurable reporting levels and formats.
- expanding the number of `assertEquals` methods to cover further types.  
- discovering all the test classes and running them automatically (this is actually surprisingly 
difficult in Java and will probably be the subject of a future Lifting the Lid)

With these features complete, they confidently proclaimed that this was indeed a superior test library.


## JUnit 5

Now we have covered the basics of writing a test framework let's take a short look at a common java framework: JUnit 5.

One way to explore frameworks is using breakpoints in your application code. When triggered, you will
be able to use the stacktrace to explore the framework code that is invoking your application function. 

Since the framework in question is JUnit 5, let's place a breakpoint in a test from a recent project (I would encourage 
you to try the same). In a recent [Advent of Code](https://github.com/jphalford/advent-of-code) project, I get the following (the breakpoint is [here](https://github.com/jphalford/advent-of-code/blob/main/src/test/java/com/jphalford/aoc/day10/Day10Test.java#L57))

```shell
part2Example1:58, Day10Test (com.jphalford.aoc.day10)
invoke0:-1, NativeMethodAccessorImpl (jdk.internal.reflect)
invoke:62, NativeMethodAccessorImpl (jdk.internal.reflect)
invoke:43, DelegatingMethodAccessorImpl (jdk.internal.reflect)
invoke:566, Method (java.lang.reflect)
invokeMethod:688, ReflectionUtils (org.junit.platform.commons.util)
proceed:60, MethodInvocation (org.junit.jupiter.engine.execution)
proceed:131, InvocationInterceptorChain$ValidatingInvocation (org.junit.jupiter.engine.execution)
...
invokeTestMethod:206, TestMethodTestDescriptor (org.junit.jupiter.engine.descriptor)
...
startRunnerWithArgs:71, JUnit5IdeaTestRunner (com.intellij.junit5)
startRunnerWithArgs:33, IdeaTestRunner$Repeater (com.intellij.rt.junit)
prepareStreamsAndStart:220, JUnitStarter (com.intellij.rt.junit)
main:53, JUnitStarter (com.intellij.rt.junit)
```
 
Let's look at the first few lines:
```shell
part2Example1:58, Day10Test (com.jphalford.aoc.day10)
invoke0:-1, NativeMethodAccessorImpl (jdk.internal.reflect)
invoke:62, NativeMethodAccessorImpl (jdk.internal.reflect)
invoke:43, DelegatingMethodAccessorImpl (jdk.internal.reflect)
invoke:566, Method (java.lang.reflect)
invokeMethod:688, ReflectionUtils (org.junit.platform.commons.util)
```

On the first line, we have our test method, `part2Example1` and on the bottom, a method from the JUnit framework `invokeMethod`.
From the trace of the methods in between, it looks like it's using the same `invoke` function from the Java Reflection library that we saw earlier.

Let's look closer at the `invokeMethod` method:
```java
public class ReflectionUtils {   
  //...

  /**
   * @see org.junit.platform.commons.support.ReflectionSupport#invokeMethod(Method, Object, Object...)
   */
  public static Object invokeMethod(Method method, Object target, Object... args) {
    Preconditions.notNull(method, "Method must not be null");
    Preconditions.condition((target != null || isStatic(method)),
      () -> String.format("Cannot invoke non-static method [%s] on a null target.", method.toGenericString()));
  
    try {
      return makeAccessible(method).invoke(target, args);
    }
    catch (Throwable t) {
      throw ExceptionUtils.throwAsUncheckedException(getUnderlyingCause(t));
    }
  }  
  
  //...

}
```

Once we get past the `Preconditions` checks validating the method arguments the pattern followed by
our programmer emerges; the method is made accessible, invoked and the `Exception` is handled. 
In the JUnit 5 implementation the responsibility to report the test lies elsewhere in the framework. 
However, the core principles are the same. 

Further down the stacktrace, we can find the primary method responsible for running a test:

```shell
invokeTestMethod:206, TestMethodTestDescriptor (org.junit.jupiter.engine.descriptor)
```

```java
public class TestMethodTestDescriptor {
  //...
    
  @Override
  public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
          DynamicTestExecutor dynamicTestExecutor) {
    ThrowableCollector throwableCollector = context.getThrowableCollector();

    // @formatter:off
    invokeBeforeEachCallbacks(context);
      if (throwableCollector.isEmpty()) {
        invokeBeforeEachMethods(context);
          if (throwableCollector.isEmpty()) {
            invokeBeforeTestExecutionCallbacks(context);
              if (throwableCollector.isEmpty()) {
                invokeTestMethod(context, dynamicTestExecutor);
              }
            invokeAfterTestExecutionCallbacks(context);
          }
        invokeAfterEachMethods(context);
      }
    invokeAfterEachCallbacks(context);
    // @formatter:on

    return context;
  }

  // ...
}
```

Here, you can see the methods annotated with `@Before` being invoked (`invokeBeforeEachMethods()`) prior to the test
(`invokeTestMethod()`) and finally the (`invokeAfterEachCallbacks`) invoking the methods tagged with `@After`. 
Looking further in the stacktrace would also reveal the code to process the `@BeforeAll` and `@AfterAll` annotations.


Finally, if we look at the initial main function in the debugger we can see how the test to run was specified:
```
args = {String[3]@1945}
       0 = "-ideVersion5"
       1 = "-junit5"
       2 = "com.jphalford.aoc.day10.Day10Test"
```

In the third parameter (index 2) we can see the class containing the tests to be run (`com.jphalford.aoc.day10.Day10Test`). 
This string can be used by the JUnit framework to instantiate the test class in the same manner as our programmer used above.



# Conclusion

We've seen how to write a basic unit testing library and compared that with the approaches taken in an established framework.
The language features used are not unique to unit testing frameworks and can be seen across a wide range of frameworks.
Hopefully, this will help lift the lid on the mechanics of these and inspire you to explore them further.  

If you would like to try to implement your own test harness, or would like to see the final solution in one place, you can find the project at 
[jphalford/lifting-the-lid-unit-testing-framework](https://github.com/jphalford/lifting-the-lid-unit-testing-framework).

I'm planning further articles on Dependency Injection frameworks (e.g. Spring beans), Lombok and the Classpath. If you 
would like to see articles on a particular subject, get in contact!

