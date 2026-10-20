# Java Exception Handling from Scratch | Exceptions vs Errors | Java Full Course #43
> Source: https://youtu.be/mJg9iQZJEpw · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 55:45. The video is in Hindi (Hinglish); these notes are in English, written from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Java splits every runtime problem into two kinds. An **exception** is a recoverable, handleable abnormal condition (divide by zero, missing file, bad input). An **error** is a serious JVM-level problem you are not expected to recover from (`OutOfMemoryError`, `StackOverflowError`).
- Both are **objects**. When something illegal happens, the JVM creates one (e.g. `new ArithmeticException("/ by zero")`) and **throws** it.
- A thrown exception stops the normal flow at once. The JVM then walks **down the call stack** (current method, its caller, that method's caller, ...) looking for a matching `catch`. If nobody catches it, the **default exception handler** prints the stack trace and the thread dies.
- `try` holds the risky code, `catch (SomeException e)` holds the handling code, and `finally` holds cleanup code that runs whether or not an exception happened.
- A `catch` only handles the exception type it names (or its subtypes). Catching the wrong type is the same as not handling it at all.

## Key Concepts

**Exception.** An abnormal condition during execution that the programmer can recover from or handle, e.g. `ArithmeticException` for `5 / 0`. *Why it matters:* you can decide what happens next (ask the user to retry, show a friendly message) instead of letting the program crash.

**Error.** A serious problem, usually from the JVM itself, that your business logic isn't designed to handle. The two famous ones are `OutOfMemoryError` (the heap is full, so no new objects can be created) and `StackOverflowError` (the call stack is full, typically from unbounded recursion). *Why it matters:* this is why the topic is called *exception* handling, not *error* handling.

**Exceptions are objects.** Java is object-oriented, so a problem is represented by an instance of a class such as `ArithmeticException`. It carries a message (`getMessage()`) and a stack trace. *Why it matters:* you can store, inspect, log and pass around the object `e` you catch.

**Throwing.** Creating the exception object and abruptly stopping the current flow. The rest of the statements after that point in the block don't run. *Why it matters:* code after the failing line is skipped unless you handle the exception.

**Call stack and propagation.** Every method call pushes a frame (`main` → `methodA` → `methodB`). When an exception is thrown, the JVM checks the current method for a handler. If there isn't one, it pops that frame and checks the caller, and so on up to `main`. *Why it matters:* you can choose the level at which to handle the exception.

**Stack trace.** The report printed for an uncaught exception: thread name, exception class, message, then one `at Class.method(File.java:line)` line per frame, innermost first. *Why it matters:* it's your main debugging tool. It tells you exactly which line failed and how execution got there.

**Default exception handling.** What the JVM does when no one catches the exception: it terminates the thread and prints the stack trace. *Why it matters:* this means the JVM, not you, is deciding what happens.

**`try` / `catch` / `finally`.** `try` wraps risky code. `catch` wraps the handling ("preventive") code for a given exception type. `finally` wraps cleanup code (closing files, DB connections, logging) that always runs. *Why it matters:* it's Java's built-in way to keep control of the flow when something fails.

## Detailed Notes

### Why things go wrong [00:00]
- Programs don't always run as planned: the user types bad input (their roll number in the age field), a file is missing, a denominator turns out to be zero.
- Neither the programmer nor the user is perfect, so code has to be ready for these cases.

### Java's two categories: Error and Exception [01:38]
- **Exception**: a recoverable or handleable abnormal condition that occurs during program execution.
- In Java an exception is not just an idea. It's an **object** that the JVM creates and throws when the flow of control hits a problem.
- Classic example: `int num = a / b;` with `b == 0`. The JVM sees an illegal operation and throws an `ArithmeticException`.
- Recovery examples: file missing → show "file not found"; wrong input → ask the user to retry; zero denominator → tell the user zero isn't allowed.
- **Error** [04:00]: a serious problem you can't recover from, and that your code isn't built to handle.
  - `OutOfMemoryError`: the **heap** is full, so no new objects can be created.
  - `StackOverflowError`: the **stack** is full.
  - Errors are objects too. So the JVM throws either an exception object or an error object, depending on the problem.

### Simulating both [05:30]
- `int num = 5 / 0;` → `java.lang.ArithmeticException: / by zero`.
- An error is simulated by having `main` call itself recursively forever. The program runs for a while and then dies because the stack fills up.
- *Correction:* the video says the IDE "stops it automatically". It's actually the JVM that throws `StackOverflowError` once the thread's stack is exhausted. The IDE just shows the result.

### Why handle exceptions? [06:55]
- Demo: print `Step 1`, compute `a / b` with `b = 0`, print `Step 2`. Only `Step 1` appears, followed by the exception. `Step 2` never runs.
- So an unhandled exception **disrupts the whole flow** and terminates the program at that line.

### Preventing vs handling [08:30]
- One option is an `if`/`else`: `if (b == 0) print("b cannot be zero") else print(a / b)`. This is a **preventive measure**: the illegal operation never happens, so no exception object is created.
- Exception handling doesn't mean removing every possible problem. It means **deciding what your code should do** when the problem occurs.
- If you don't handle it, the **JVM decides everything**: it stops the flow and crashes the program. You have lost control.
- Handling it yourself gives you:
  - control over the flow,
  - easier debugging, since you know how each exception is dealt with,
  - a better **user experience** [11:30]. Example: a restaurant app fetching menu items from a database. If the connection fails and the `SQLException` isn't handled, the user may see a raw exception. If it's handled, you can show an empty list or "can't load items right now, try again later".

### What the JVM does when an exception happens [13:07]
- Walk-through of `int a = 5; int b = 0; System.out.println(a / b);`. Creating `a` and `b` is fine. At `a / b` the JVM spots an illegal operation.
- Internally it does the equivalent of `throw new ArithmeticException("/ by zero");`. Built-in exceptions come with a predefined message.
- "Throw" means: stop the normal flow (nothing after this line in the block runs), then search for code that handles this exception.
- *Correction:* the video loosely says throwing means "returning" the exception to you. It's not a normal return. The method completes *abruptly*, and the exception propagates to whatever handler is found.
- If no handler is found, the exception is printed on the console. This is **default exception handling** [16:24].

### The call stack [16:50]
- `main` calls `methodA`, which calls `methodB`. Each call is **pushed** onto the stack. When `methodB` finishes it's **popped**, and control returns to `methodA`, then `main`, then the stack is empty.
- When `methodB` throws, the JVM asks: did `methodB` handle it? No → pop, ask `methodA`. No → pop, ask `main`. No → print the **stack trace** and terminate.
- The stack trace records the whole path: the failing line, the method it was in, who called that method, and so on.

### Reading a stack trace [20:30]
```
Exception in thread "main" java.lang.ArithmeticException: / by zero
    at Demo2.methodB(Demo2.java:13)
    at Demo2.methodA(Demo2.java:10)
    at Demo2.main(Demo2.java:6)
```
- `in thread "main"`: Java always runs your code in at least one thread, called `main`. With multiple threads (covered later) this tells you which one failed.
- `java.lang.ArithmeticException`: the exception class. `java.lang` is imported automatically, like `String` and `System`.
- `/ by zero`: the message.
- Each `at` line is `ClassName.methodName(FileName.java:lineNumber)`, **innermost call first**. Here: the failure is at line 13 in `methodB`, which was called from line 10 in `methodA`, which was called from line 6 in `main`.
- The video says "divide by zero" when talking about the message. The actual text the JDK prints is `/ by zero`.

### `try` / `catch` syntax [26:40]
- Goal: when an exception occurs, **don't crash**. Handle it and continue the flow.
- Put the **risky code** in `try`. In the demo the values are hard-coded, but imagine `a` and `b` come from the user, so you can't know in advance whether `b` is zero.
- Put the handling (**preventive**) code in `catch (ExceptionType e)`. Think of `catch` as a method that receives the exception object as its parameter. `ArithmeticException` extends `RuntimeException`; the full hierarchy comes in the next video.
- Demo [29:50]: `Step 1` → try `a / b` → catch prints "Division by zero is not allowed" → `Step 2`. The program no longer crashes.
- Flow: the JVM enters `try`, runs the statements, hits the illegal one, creates the exception, breaks the flow, finds a `catch` in the current method that matches `ArithmeticException`, runs it, and then carries on normally after the `try`/`catch`.

### Handling in a chain of methods [34:28]
- The same `main → methodA → methodB` chain. You can put the `try`/`catch`:
  - **inside `methodB`** around `a / b`,
  - **inside `methodA`** around the call `methodB(a, b)`, or
  - **inside `main`** around the call `methodA(5, 0)`.
- All three print the same message. The difference is *where the normal flow resumes*.
- Puzzle [38:00]: `main` prints `Step 1`, calls `methodA` inside `try` (with the `catch` in `main`), and `methodB` prints `Step 2` after the division. Output is only `Step 1` then the catch message. `methodB` and `methodA` are popped while the JVM searches for a handler, so the rest of `methodB` never runs.
- If the `try`/`catch` is moved into `methodB` itself, nothing is popped. After the catch, `methodB` carries on and `Step 2` prints.

### Using the exception object [42:50]
- `e` is a reference to the exception object on the heap. Useful methods:
  - `e.getMessage()` → `"/ by zero"`.
  - `e.printStackTrace()` → prints the full stack trace (to `System.err`) without crashing. Call it directly, not inside `println`.
  - Others listed by the IDE: `getStackTrace()`, `fillInStackTrace()`, `getCause()`, `getSuppressed()`, `initCause()`, plus the `Object` methods.
- In real applications you'd send the stack trace to a **log file** so you can find production failures later (class, method, line).

### `try`/`catch` vs `if`/`else` [45:00]
- It's a difference of intent. `if`/`else` is **preventive**: you check first and never perform the illegal operation, so no exception object exists.
- `try`/`catch` is **try first**: attempt the operation, and if it fails, an exception is thrown and caught.

### `finally` [46:40]
- `finally { ... }` takes no parameter and **always runs**, whether or not an exception happened.
- No exception (`b = 2`): `Step 1`, `2` (integer division drops `.5`), the finally message, `Step 2`. The `catch` is skipped.
- Exception (`b = 0`): `Step 1`, catch message, finally message, `Step 2`. Any statement in `try` **after** the failing line (`"hey"` in the demo) is skipped.
- Why it exists [49:50]: `try` = risky code, `catch` = handling, `finally` = **cleanup**. If you open a resource (file, DB connection) in `try` and an exception fires before your `close()` line, the resource stays open. That leaks memory and can block other threads. Put the closing (and things like logging) in `finally`.

### Catching the wrong type [51:50]
- If `try` throws `ArithmeticException` but the only `catch` is for `NullPointerException`, nothing matches. The program behaves exactly as if it weren't handled: `Step 1`, stack trace, and `Step 2` never prints.
- You can write **multiple `catch` blocks** [53:00], one per exception type. The JVM uses the first one that matches.

### `try` + `finally` with no `catch` [54:10]
- `catch` is optional too. `try { ... } finally { ... }` compiles.
- If an exception occurs, nothing handles it, but `finally` still runs **before** the exception leaves the method. Then the default handler prints the stack trace.
- There are a few rare cases where `finally` doesn't run (e.g. `System.exit()`); they come later. Next video: the exception class hierarchy.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Exception vs error, both are objects
```java
public class ExceptionVsError {
    static int depth = 0;

    static void recurse() {
        depth++;
        recurse(); // no base case -> stack fills up
    }

    public static void main(String[] args) {
        try {
            int num = 5 / 0;
            System.out.println(num);
        } catch (ArithmeticException e) {
            System.out.println("Exception object: " + e.getClass().getName());
            System.out.println("Message: " + e.getMessage());
        }

        try {
            recurse();
        } catch (StackOverflowError err) { // catching an Error only to show it is an object - don't do this in real code
            System.out.println("Error object: " + err.getClass().getName());
            System.out.println("Depth reached > 1000? " + (depth > 1000));
        }
    }
}
// Output:
// Exception object: java.lang.ArithmeticException
// Message: / by zero
// Error object: java.lang.StackOverflowError
// Depth reached > 1000? true
```

### Unhandled exception stops the flow
```java
// throws
public class UnhandledFlow {
    public static void main(String[] args) {
        int a = 5, b = 0;
        System.out.println("Step 1");
        System.out.println(a / b);     // ArithmeticException thrown here
        System.out.println("Step 2");  // never runs
    }
}
// Output:
// Step 1
// Exception in thread "main" java.lang.ArithmeticException: / by zero
//     at UnhandledFlow.main(...)
```

### Where you catch decides where the flow resumes
```java
public class CatchLevelDemo {
    public static void main(String[] args) {
        System.out.println("-- caught in main --");
        try {
            methodA(5, 0);
            System.out.println("after methodA"); // skipped
        } catch (ArithmeticException e) {
            System.out.println("main caught: " + e.getMessage());
        }

        System.out.println("-- caught in methodB --");
        safeB(5, 0);

        System.out.println("-- the call stack recorded in the exception --");
        try {
            methodA(5, 0);
        } catch (ArithmeticException e) {
            for (StackTraceElement frame : e.getStackTrace()) {
                if (frame.getClassName().equals("CatchLevelDemo")) { // skip JDK launcher frames
                    System.out.println("at " + frame.getMethodName());
                }
            }
        }
    }

    static void methodA(int a, int b) { methodB(a, b); }

    static void methodB(int a, int b) {
        System.out.println(a / b);
        System.out.println("Step 2 (B)"); // never reached when b == 0
    }

    static void safeB(int a, int b) {
        try {
            System.out.println(a / b);
        } catch (ArithmeticException e) {
            System.out.println("safeB caught: " + e.getMessage());
        }
        System.out.println("Step 2 (B)"); // runs: nothing was popped
    }
}
// Output:
// -- caught in main --
// main caught: / by zero
// -- caught in methodB --
// safeB caught: / by zero
// Step 2 (B)
// -- the call stack recorded in the exception --
// at methodB
// at methodA
// at main
```

### `try` / `catch` / `finally` in both paths
```java
public class TryCatchFinallyFlow {
    static void divide(int a, int b) {
        System.out.println("Step 1");
        try {
            System.out.println(a / b);
            System.out.println("hey (rest of try)");
        } catch (ArithmeticException e) {
            System.out.println("Division by zero is not allowed");
        } finally {
            System.out.println("finally always runs");
        }
        System.out.println("Step 2");
    }

    public static void main(String[] args) {
        divide(5, 2);
        System.out.println("---");
        divide(5, 0);
    }
}
// Output:
// Step 1
// 2
// hey (rest of try)
// finally always runs
// Step 2
// ---
// Step 1
// Division by zero is not allowed
// finally always runs
// Step 2
```

### Multiple catch blocks, and a non-matching catch
```java
public class MultiCatchDemo {
    static void run(String s, int b) {
        try {
            System.out.println(s.length() / b);
        } catch (NullPointerException e) {
            System.out.println("No nulls allowed");
        } catch (ArithmeticException e) {
            System.out.println("Arithmetic problem: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        run("hello", 5);
        run(null, 5);
        run("hello", 0);

        try {
            try {
                System.out.println(5 / 0);
            } catch (NullPointerException e) { // wrong type: does not match
                System.out.println("never printed");
            }
        } catch (ArithmeticException e) {
            System.out.println("Inner catch didn't match, outer caught: " + e.getMessage());
        }
    }
}
// Output:
// 1
// No nulls allowed
// Arithmetic problem: / by zero
// Inner catch didn't match, outer caught: / by zero
```

### `try` + `finally` without `catch`
```java
// throws
public class TryFinallyOnly {
    public static void main(String[] args) {
        System.out.println("Step 1");
        try {
            System.out.println(5 / 0);
        } finally {
            System.out.println("finally runs before the exception leaves main");
        }
        System.out.println("Step 2"); // never runs
    }
}
// Output:
// Step 1
// finally runs before the exception leaves main
// Exception in thread "main" java.lang.ArithmeticException: / by zero
```

## Common Pitfalls / Tips
- **The message is `/ by zero`**, not "divide by zero". Only *integer* division throws. `5.0 / 0` gives `Infinity` with no exception.
- **Code after the failing line in `try` never runs.** Don't put "must happen" work (closing resources) at the end of `try`. Use `finally`, or try-with-resources (later in the series).
- **A `catch` for the wrong type does nothing.** The exception propagates as if there were no handler.
- **Don't catch `Error`s** (`StackOverflowError`, `OutOfMemoryError`) in normal code. The example above only does it to show they are objects.
- **Don't swallow exceptions.** An empty `catch {}` hides bugs. At least log `e` (or `e.printStackTrace()` while learning).
- **`printStackTrace()` writes to `System.err`**, so in a console it can interleave with `System.out` output in a surprising order.
- **Read stack traces top-down:** the first `at` line is where it failed. The lines below are how execution got there. Look for the first line that belongs to *your* code.
- **Prefer prevention when it's cheap and obvious** (validate `b != 0` up front). Use `try`/`catch` when you can't check in advance (files, network, databases).

## Review Questions
1. What's the difference between an exception and an error in Java? Give two examples of each.
2. What exactly happens, step by step, when the JVM executes `System.out.println(5 / 0);` with no handler anywhere?
3. In `main → methodA → methodB`, `methodB` throws and only `main` has a matching `catch`. Which statements are skipped?
4. How do you read the line `at Demo2.methodB(Demo2.java:13)`?
5. How is `try`/`catch` different in intent from an `if (b == 0)` check?
6. When does `finally` run, and what should go in it?
7. What happens if the `catch` block names `NullPointerException` but the `try` throws `ArithmeticException`?
8. Does `try { ... } finally { ... }` without a `catch` compile? What's printed if the `try` throws?

<details>
<summary>Answers</summary>

1. An exception is a recoverable, handleable condition: `ArithmeticException`, `NullPointerException`, a missing file, an SQL failure. An error is a serious problem the program isn't expected to recover from: `OutOfMemoryError` (heap full) and `StackOverflowError` (stack full).
2. The JVM detects an illegal integer division and creates `new ArithmeticException("/ by zero")`. It abandons the rest of the statement and block, then looks for a handler in the current method, then each caller down the call stack. Finding none, the default handler prints `Exception in thread "main" ...` plus the stack trace, and the thread terminates.
3. The rest of `methodB` after the division, the rest of `methodA` after the call, and the rest of the `try` block in `main`. Execution resumes in `main`'s `catch` and then continues after the `try`/`catch`.
4. Class `Demo2`, method `methodB`, source file `Demo2.java`, line 13. Since it's the top `at` line, that's where the exception was thrown.
5. `if`/`else` prevents the problem: the illegal operation is never attempted, and no exception object is created. `try`/`catch` attempts the operation and handles the exception if it is thrown.
6. Always: after `try` completes normally, after a `catch` runs, and even when the exception isn't caught (it runs before the exception propagates). Use it for cleanup, such as closing files and DB connections or logging.
7. The catch doesn't match, so the exception isn't handled. It propagates (and crashes the program if nothing else catches it), exactly as if there were no `try`/`catch`.
8. Yes. The `finally` block's output appears, then the exception propagates and the default handler prints the stack trace. Code after the `try`/`finally` doesn't run.
</details>

## Further Study
- The `Throwable` → `Exception` / `Error` hierarchy; checked vs unchecked exceptions (next video)
- `throw` vs `throws`; custom exception classes
- Multi-catch (`catch (A | B e)`) and catch-order rules
- try-with-resources and `AutoCloseable`
- When `finally` doesn't run (`System.exit`, JVM crash) and why you shouldn't `return` from `finally`
- Logging frameworks (SLF4J, Logback) instead of `printStackTrace()`
- Stack vs heap memory in depth (the memory-management videos, #45–#46)

---

[← #42](42-parallel-streams-primitive-optionals.md) · [Index](README.md) · [#44 →](44-exception-hierarchy-throw-throws-custom.md)

---

[← #42](42-parallel-streams-primitive-optionals.md) · [Index](README.md) · [#44 →](44-exception-hierarchy-throw-throws-custom.md)
