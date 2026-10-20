# Exception Hierarchy Deep Dive | throw vs throws | Custom Exceptions | Java Full Course #44
> Source: https://youtu.be/xTxscoajikw · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 65:23. The video is in Hindi (Hinglish); these notes are in English, written from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `try`/`catch` blocks can be **nested**, inside a `try` or inside a `catch`. An inner exception that the inner `catch` doesn't match goes to the outer `catch`. It works, but it gets hard to read, so avoid it where you can.
- Hierarchy: **`Throwable`** → `Error` and `Exception`. `Exception` → **`RuntimeException`** (unchecked: `NullPointerException`, `ArithmeticException`, `IndexOutOfBoundsException`, `IllegalArgumentException`, `IllegalStateException`, `ClassCastException`, ...) and the **checked** ones (`IOException`, `SQLException`, `ClassNotFoundException`, ...). The compiler forces you to handle or declare checked exceptions.
- A `catch` for a parent type also catches its children. With several `catch` blocks the JVM checks them **top to bottom**, so put the **most specific first**. A parent before its child won't compile ("unreachable catch block"). Java 7+ also lets you write sibling types together: `catch (A | B e)`.
- **`throw`** raises an exception object yourself (`throw new IllegalArgumentException("...")`). **`throws`** goes in a method signature and declares that the method may throw a type, which passes the duty of handling it to the caller.
- **Custom exceptions** (`class InvalidAgeException extends Exception`) give business-specific names and can carry extra data. **try-with-resources** (`try (FileReader fr = ...) { }`) closes resources automatically.

## Key Concepts

**Nested try/catch.** A `try`/`catch` inside another `try` (or inside a `catch`). The JVM looks for a matching handler from the innermost block outward, then up the call stack. *Why it matters:* you can predict exactly which `catch` runs. In practice, keep nesting to a minimum because it makes the flow hard to follow.

**`Throwable`.** The root **class** (not an interface) of everything that can be thrown. `Error` and `Exception` both extend it. It's where the message and stack trace live, and it defines `getMessage()`, `printStackTrace()` and `toString()`. *Why it matters:* only `Throwable` subclasses can be used with `throw` and `catch`.

**Unchecked exceptions (`RuntimeException` and subclasses).** The compiler doesn't force you to handle them. They usually point to bugs or bad input found at run time. *Why it matters:* your code compiles without a `try`, but an unhandled one still crashes the thread.

**Checked exceptions (other `Exception` subclasses).** The compiler requires you either to catch them or to declare them with `throws`. They model failures outside your program's control: missing files, I/O failures, database errors. *Why it matters:* "unhandled exception of type FileNotFoundException" is a compile error, not a runtime one.

**Catch ordering.** `catch` blocks are tried top to bottom, and the first one whose type matches wins. A parent type placed before its child makes the child's block unreachable, which is a compile error. *Why it matters:* always go from specific to general.

**Multi-catch (`catch (A | B e)`).** One handler for several **unrelated** exception types (Java 7+). *Why it matters:* it avoids duplicate handlers. Types related by inheritance aren't allowed in the same multi-catch.

**`throw` vs `throws`.** `throw` is a **statement** that raises one exception object now. `throws` is a **clause** in the method signature that lists the types the method may throw and leaves handling to the caller. *Why it matters:* this is how a method tells its caller "you passed me bad input" or "this may fail; deal with it".

**Custom exception.** Your own class extending `Exception` (checked) or `RuntimeException` (unchecked), usually with a constructor that passes the message to `super(message)`. *Why it matters:* `InsufficientBalanceException` says much more than `IllegalArgumentException`, and you can catch it on its own.

**try-with-resources.** `try (Resource r = ...) { ... }` closes `r` automatically when the block ends, whether or not an exception happened. The resource must implement `AutoCloseable`. *Why it matters:* you can't forget to close files and connections in `finally`.

## Detailed Notes

### Nested try/catch [▶ ~00:30](https://youtu.be/xTxscoajikw?t=30)
- You can nest `if`, loops, `switch` and classes, so you can nest `try`/`catch` too. There are two cases.
- **Case 1, the inner try handles its own exception** [▶ ~03:21](https://youtu.be/xTxscoajikw?t=201). Demo: print "outer try starts", "inner try starts", then `5 / 0`. The inner `catch (ArithmeticException)` prints "...inner". Then "outer try ends" prints. The outer `catch` never runs, because from the outer `try`'s point of view nothing was thrown.
- **Case 2, the outer catch handles the inner exception** [▶ ~06:42](https://youtu.be/xTxscoajikw?t=402). Same code, but the inner `catch` handles `NullPointerException`. The `ArithmeticException` doesn't match, so the JVM moves on to the outer `catch (ArithmeticException)`. Output: "outer try starts", "inner try starts", "...outer". "inner try ends" and "outer try ends" are both skipped.
- **Nesting inside a `catch`** [▶ ~10:04](https://youtu.be/xTxscoajikw?t=604) works the same way. The inner `try` there only runs if the outer `try` threw. If nothing in the method matches, the exception propagates to the caller and eventually to the default handler.
- **Advice** [▶ ~11:44](https://youtu.be/xTxscoajikw?t=704): avoid nested `try`/`catch` in real code. Working out which `catch` handles which exception becomes hard. Extract a method instead.

### The hierarchy [▶ ~12:00](https://youtu.be/xTxscoajikw?t=720)
```
Object
└── Throwable                       (class: getMessage, printStackTrace, toString)
    ├── Error                       (don't handle: OutOfMemoryError, StackOverflowError)
    └── Exception
        ├── RuntimeException        (UNCHECKED)
        │   ├── NullPointerException
        │   ├── ArithmeticException
        │   ├── IndexOutOfBoundsException
        │   │   ├── ArrayIndexOutOfBoundsException
        │   │   └── StringIndexOutOfBoundsException
        │   ├── IllegalArgumentException
        │   │   └── NumberFormatException
        │   ├── IllegalStateException
        │   └── ClassCastException
        └── (CHECKED)
            ├── IOException
            │   ├── FileNotFoundException
            │   └── EOFException
            ├── SQLException
            └── ClassNotFoundException   (via ReflectiveOperationException)
```
- `Error` and `Exception` both extend `Throwable`. Anything that isn't a `Throwable` can't be thrown.
- You *can* throw an `Error` yourself, but you normally don't. Errors arise on their own (memory exhausted) and can't be fixed from your code. Focus on exceptions.
- The three most-used methods [▶ ~14:30](https://youtu.be/xTxscoajikw?t=870):
  - `getMessage()` returns the message, e.g. `/ by zero`.
  - `printStackTrace()` prints the full trace.
  - `toString()` returns `ClassName: message`.
  These are all defined in `Throwable`.

### Runtime (unchecked) exceptions [▶ ~16:46](https://youtu.be/xTxscoajikw?t=1006)
- **`NullPointerException`**: calling a method on, or accessing a field of, a `null` reference, e.g. `String s = null; s.length();`.
- **`ArithmeticException`**: illegal arithmetic, such as integer division by zero.
- **`IndexOutOfBoundsException`**, the parent of:
  - `ArrayIndexOutOfBoundsException`: index 4 of a 4-element array (valid indexes 0–3).
  - `StringIndexOutOfBoundsException`: index 5 of `"hello"` (valid 0–4).
  - *Correction:* the video writes `arr.get(4)` and `s.get(5)`. In Java it's `arr[4]` for an array and `s.charAt(5)` for a string. `list.get(4)` on a `List` throws a plain `IndexOutOfBoundsException`.
- **`IllegalArgumentException`** [▶ ~18:40](https://youtu.be/xTxscoajikw?t=1120): a method received an argument it doesn't accept.
  - `NumberFormatException` is a subclass: `Integer.parseInt("abc")`. `"123"` works.
  - `Thread.sleep(-1500)`: a negative duration is illegal.
- **`IllegalStateException`** [▶ ~20:20](https://youtu.be/xTxscoajikw?t=1220): the object isn't in a state where the call makes sense. Example: calling `iterator.remove()` before ever calling `next()`. There is no "previous element" to remove yet.
- **`ClassCastException`** [▶ ~21:49](https://youtu.be/xTxscoajikw?t=1309): `Object obj = "hello"; Integer i = (Integer) obj;`. The object is a `String`, not an `Integer`.
- These are called **unchecked** because the compiler doesn't check whether you handle them. The code compiles either way. If one occurs and nobody catches it, the stack trace is printed and the program stops.

### Checked exceptions [▶ ~22:50](https://youtu.be/xTxscoajikw?t=1370)
- The compiler **forces** you either to put the risky code in a `try` with a matching `catch` or to declare `throws` on the method.
- Examples:
  - **`IOException`**: an input/output operation failed (file, stream). Subclasses: `FileNotFoundException` (the file is missing) and `EOFException` (the end of the file was reached unexpectedly).
  - **`SQLException`**: something went wrong talking to a database.
  - **`ClassNotFoundException`**: the JVM couldn't find a class it was asked to load by name. Rare.
- *Correction:* the video calls these "compile-time exceptions". They still *occur* at run time. It's only the handle-or-declare **check** that happens at compile time.
- In practice you rarely memorize every built-in type. You use a handful, plus a generic handler and your own **custom exceptions** with meaningful names.

### Catching via a parent type [▶ ~25:40](https://youtu.be/xTxscoajikw?t=1540)
- `ArithmeticException` extends `RuntimeException`, which extends `Exception`, which extends `Throwable`. A parent reference can point to a child object, so `catch (RuntimeException e)`, `catch (Exception e)` and `catch (Throwable e)` all catch the `ArithmeticException`.
- **Never catch `Throwable`** [▶ ~28:31](https://youtu.be/xTxscoajikw?t=1711). It also catches `Error`s, which you shouldn't handle. `catch (Exception e)` is the widest sensible handler: it catches `ArithmeticException`, `NullPointerException`, and the rest.

### Multiple catch blocks and their order [▶ ~29:40](https://youtu.be/xTxscoajikw?t=1780)
- A `try` can have several `catch` blocks, but only **one** runs. The first exception thrown stops the `try`, so a second one can never happen in the same pass.
- The JVM checks the `catch` blocks **top to bottom**. Example: `ArithmeticException`, then `NullPointerException`, then `Exception`. A `ClassCastException` doesn't match either of the first two, so the generic `Exception` block handles it ("some generic exception").
- **Rule** [▶ ~33:00](https://youtu.be/xTxscoajikw?t=1980): specific first, generic last. Putting `catch (Exception e)` first causes the compile error *"Unreachable catch block for ArithmeticException. It is already handled by the catch block for Exception."* The same applies to `RuntimeException` placed before its children.
- Siblings such as `ArithmeticException` and `NullPointerException` can go in any order. Then come their parent `RuntimeException`, then `Exception`. The video calls this "chaining of catch blocks".
- **Multi-catch** [▶ ~34:50](https://youtu.be/xTxscoajikw?t=2090), Java 7+: `catch (ArithmeticException | NullPointerException e)`. `e` refers to whichever exception was actually thrown, so `e.getMessage()` prints `/ by zero` or the NPE's helpful message ("Cannot invoke "String.length()" because ... is null").
- You can't combine a parent and its child in a multi-catch (`ArithmeticException | Exception`). The compiler reports that the subtype "is already caught by the alternative Exception".

### `throw`: raising your own exceptions [▶ ~36:55](https://youtu.be/xTxscoajikw?t=2215)
- Example: `checkEligibility(int age)` prints "You are eligible to vote" when `age >= 18`.
- What about a negative age? Printing "Age cannot be negative" (even with `System.err`) doesn't tell the **caller** anything. The method just carries on.
- Exceptions are how code *communicates* that something went wrong, the same way Java throws `ArithmeticException` for `5 / 0` instead of just printing a line. So [▶ ~40:16](https://youtu.be/xTxscoajikw?t=2416):
  ```
  if (age < 0) throw new IllegalArgumentException("Age cannot be negative");
  ```
- Constructor chain [▶ ~41:57](https://youtu.be/xTxscoajikw?t=2517): the message passes from `IllegalArgumentException(String)` to `super(message)` in `RuntimeException`, then `Exception`, then **`Throwable`**. `Throwable` stores the detail message and fills in the stack trace.
- If the caller doesn't handle it, you get the default trace: `Exception in thread "main" java.lang.IllegalArgumentException: Age cannot be negative`. If `main` wraps the call in `try`/`catch (IllegalArgumentException e)` [▶ ~43:38](https://youtu.be/xTxscoajikw?t=2618), it prints `e.getMessage()` and carries on (`Step 2` still runs).
- **Anti-pattern** [▶ ~45:18](https://youtu.be/xTxscoajikw?t=2718): throwing an exception and catching it in the *same* method. The caller never finds out anything happened, so you might as well have printed the message. Throw it, and let the caller decide what to do.

### `throws`: declaring and passing on responsibility [▶ ~47:00](https://youtu.be/xTxscoajikw?t=2820)
- `new FileReader("abc.txt")` gives the compile error *"Unhandled exception type FileNotFoundException"*. The file lives outside the program, and Java can't guarantee it exists, so it forces you to deal with it.
- Option 1: wrap it in `try`/`catch (FileNotFoundException e)`. With no such file, it prints "File not found".
- Option 2 [▶ ~50:20](https://youtu.be/xTxscoajikw?t=3020): declare it on the method: `private static void readFile() throws FileNotFoundException`. The error disappears from `readFile` and moves to the **caller**, which now has to catch it or declare `throws` as well.
- If `main` also declares `throws FileNotFoundException` [▶ ~51:30](https://youtu.be/xTxscoajikw?t=3090), nobody handles it. Its caller is the JVM, so the stack trace is printed. That trace starts deep inside the JDK (`FileInputStream.open0`, a native method, then `FileInputStream.open`, then the `FileInputStream` and `FileReader` constructors) before it reaches `readFile` and `main`.
- Advice: `throws` on helper methods is fine. It's an honest declaration. But **someone** should handle the exception. Don't declare `throws` on `main`; catch it there.

### Custom exceptions [▶ ~53:42](https://youtu.be/xTxscoajikw?t=3222)
- Built-in names are generic. Business logic deserves specific names: `InsufficientBalanceException` (withdrawing ₹2000 from a ₹1000 balance), `PaymentFailedException`, `OrderNotFoundException` (for a Zomato/Swiggy-style app), `InvalidAgeException`.
- Recipe [▶ ~55:50](https://youtu.be/xTxscoajikw?t=3350):
  ```
  class InvalidAgeException extends Exception {
      public InvalidAgeException(String message) { super(message); }
  }
  ```
  `Exception` isn't abstract and has nothing you must override. Most production custom exceptions are just this: a meaningful name plus a message. The name works almost like a marker.
- Because it extends `Exception`, it's **checked**. `throw new InvalidAgeException(...)` inside `checkEligibility` needs `throws InvalidAgeException` on the method, and the caller must catch `InvalidAgeException`.
- **Extra data** [▶ ~59:40](https://youtu.be/xTxscoajikw?t=3580): add a `private int age` field, a constructor `(String message, int age)` that calls `super(message)` and sets `this.age`, and a `getAge()` getter. The handler can then print "Entered age was -5". Keep it simple unless you actually need the extra information.
- *Tip not in the video:* extend `RuntimeException` instead if you want an **unchecked** custom exception that callers aren't forced to handle.

### try-with-resources [▶ ~61:50](https://youtu.be/xTxscoajikw?t=3710)
- A **resource** is any object you must close after use: files, DB connections (where `SQLException` is checked), network sockets.
- The old way: open it in `try`, handle errors in `catch`, close it in `finally` (`db.close()`). People forgot, and resources leaked.
- The new way: `try (FileReader fr = new FileReader("abc.txt")) { ... } catch (...) { ... }`. Java closes `fr` automatically whether the `try` completes or throws, so there's no `finally` needed for closing.
- *Corrections:* the resource type must implement `AutoCloseable` (or `Closeable`). It's closed **before** the `catch`/`finally` blocks run. Since `FileReader.close()` itself declares `IOException`, the video's `catch (FileNotFoundException e)` alone wouldn't compile. You need `catch (IOException e)` (which also covers `FileNotFoundException`).

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Nested try/catch: who handles the inner exception?
```java
public class NestedTryDemo {
    public static void main(String[] args) {
        System.out.println("== Case 1: inner catch matches ==");
        try {
            System.out.println("outer try starts");
            try {
                System.out.println("inner try starts");
                System.out.println(5 / 0);
                System.out.println("inner try ends"); // skipped
            } catch (ArithmeticException e) {
                System.out.println("Divide by zero not allowed - inner");
            }
            System.out.println("outer try ends");
        } catch (ArithmeticException e) {
            System.out.println("Divide by zero not allowed - outer"); // not reached
        }

        System.out.println("== Case 2: inner catch does not match ==");
        try {
            System.out.println("outer try starts");
            try {
                System.out.println("inner try starts");
                System.out.println(5 / 0);
                System.out.println("inner try ends"); // skipped
            } catch (NullPointerException e) {
                System.out.println("Nulls are not allowed"); // wrong type
            }
            System.out.println("outer try ends"); // skipped: exception still unhandled
        } catch (ArithmeticException e) {
            System.out.println("Divide by zero not allowed - outer");
        }
    }
}
// Output:
// == Case 1: inner catch matches ==
// outer try starts
// inner try starts
// Divide by zero not allowed - inner
// outer try ends
// == Case 2: inner catch does not match ==
// outer try starts
// inner try starts
// Divide by zero not allowed - outer
```

### Tour of common runtime exceptions and their parents
```java
import java.util.*;

public class RuntimeExceptionsTour {
    interface Risky { void run() throws Exception; }

    static void show(String label, Risky r) {
        try {
            r.run();
        } catch (Exception e) {
            System.out.println(label + " -> " + e.getClass().getSimpleName()
                    + " (parent: " + e.getClass().getSuperclass().getSimpleName() + ")");
        }
    }

    public static void main(String[] args) {
        show("null.length()", () -> { String s = null; s.length(); });
        show("5 / 0", () -> System.out.println(5 / 0));
        show("arr[4]", () -> { int[] arr = new int[4]; arr[4] = 1; });
        show("\"hello\".charAt(5)", () -> "hello".charAt(5));
        show("parseInt(\"abc\")", () -> Integer.parseInt("abc"));
        show("sleep(-1500)", () -> Thread.sleep(-1500));
        show("remove() before next()", () -> {
            Iterator<Integer> it = new ArrayList<>(List.of(1, 2, 3)).iterator();
            it.remove();
        });
        show("(Integer) \"hello\"", () -> { Object obj = "hello"; Integer i = (Integer) obj; });
    }
}
// Output:
// null.length() -> NullPointerException (parent: RuntimeException)
// 5 / 0 -> ArithmeticException (parent: RuntimeException)
// arr[4] -> ArrayIndexOutOfBoundsException (parent: IndexOutOfBoundsException)
// "hello".charAt(5) -> StringIndexOutOfBoundsException (parent: IndexOutOfBoundsException)
// parseInt("abc") -> NumberFormatException (parent: IllegalArgumentException)
// sleep(-1500) -> IllegalArgumentException (parent: RuntimeException)
// remove() before next() -> IllegalStateException (parent: RuntimeException)
// (Integer) "hello" -> ClassCastException (parent: RuntimeException)
```

### Catch order: specific first, then generic; multi-catch
```java
public class CatchOrderDemo {
    static void risky(int choice) {
        try {
            if (choice == 1) System.out.println(5 / 0);
            if (choice == 2) { String s = null; s.length(); }
            if (choice == 3) { Object obj = "hello"; Integer i = (Integer) obj; }
            System.out.println("no exception");
        } catch (ArithmeticException e) {
            System.out.println("Divide by zero is not allowed");
        } catch (NullPointerException e) {
            System.out.println("Nulls are not allowed");
        } catch (RuntimeException e) {          // parent of both siblings above
            System.out.println("Some runtime exception: " + e.getClass().getSimpleName());
        } catch (Exception e) {                 // most generic goes last
            System.out.println("Some generic exception");
        }
    }

    static void multiCatch(boolean divide) {
        try {
            if (divide) System.out.println(5 / 0);
            else { String s = null; s.length(); }
        } catch (ArithmeticException | NullPointerException e) { // siblings only (Java 7+)
            System.out.println("multi-catch got " + e.getClass().getSimpleName());
        }
    }

    public static void main(String[] args) {
        for (int c = 0; c <= 3; c++) risky(c);
        multiCatch(true);
        multiCatch(false);
    }
}
// Output:
// no exception
// Divide by zero is not allowed
// Nulls are not allowed
// Some runtime exception: ClassCastException
// multi-catch got ArithmeticException
// multi-catch got NullPointerException
```

### Generic catch before specific: compile error
```java
// does not compile
public class UnreachableCatch {
    public static void main(String[] args) {
        try {
            System.out.println(5 / 0);
        } catch (Exception e) {
            System.out.println("generic");
        } catch (ArithmeticException e) { // error: exception ArithmeticException has already been caught
            System.out.println("specific");
        }
    }
}
```

### `throw`: let the caller handle bad input
```java
public class ThrowDemo {
    public static void main(String[] args) {
        checkEligibility(19);
        try {
            checkEligibility(-5);
        } catch (IllegalArgumentException e) {
            System.out.println("Caller handled: " + e.getMessage());
        }
        System.out.println("Step 2");
    }

    private static void checkEligibility(int age) {
        if (age < 0) {
            throw new IllegalArgumentException("Age cannot be negative");
        }
        if (age >= 18) {
            System.out.println("You are eligible to vote");
        } else {
            System.out.println("Not eligible yet");
        }
    }
}
// Output:
// You are eligible to vote
// Caller handled: Age cannot be negative
// Step 2
```

### `throws` with a checked exception
```java
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ThrowsCheckedDemo {
    public static void main(String[] args) {
        try {
            readFile("this-file-does-not-exist-123.txt");
        } catch (FileNotFoundException e) {   // main handles what readFile declared
            System.out.println("File not found (" + e.getClass().getSimpleName() + ")");
        }
        System.out.println("FileNotFoundException is an IOException? "
                + IOException.class.isAssignableFrom(FileNotFoundException.class));
    }

    // Without "throws" (or a try/catch) this does not compile:
    // "unreported exception FileNotFoundException; must be caught or declared to be thrown"
    private static void readFile(String name) throws FileNotFoundException {
        FileReader fr = new FileReader(name);
        System.out.println("opened " + name);
    }
}
// Output:
// File not found (FileNotFoundException)
// FileNotFoundException is an IOException? true
```

### Custom exceptions: checked with extra data, and unchecked
```java
public class CustomExceptionDemo {
    public static void main(String[] args) {
        try {
            checkEligibility(-5);
        } catch (InvalidAgeException e) {
            System.out.println(e.getMessage());
            System.out.println("Entered age was " + e.getAge());
        }

        BankAccount acc = new BankAccount(1000);
        try {
            acc.withdraw(2000);
        } catch (InsufficientBalanceException e) {
            System.out.println(e); // toString(): ClassName: message
        }
    }

    static void checkEligibility(int age) throws InvalidAgeException {
        if (age < 0) throw new InvalidAgeException("Age cannot be negative", age);
        System.out.println(age >= 18 ? "You are eligible to vote" : "Not eligible yet");
    }
}

// Checked: callers must catch or declare it
class InvalidAgeException extends Exception {
    private final int age;

    public InvalidAgeException(String message, int age) {
        super(message);      // message travels up to Throwable
        this.age = age;
    }

    public int getAge() { return age; }
}

// Unchecked: extends RuntimeException, no throws needed
class InsufficientBalanceException extends RuntimeException {
    public InsufficientBalanceException(String message) { super(message); }
}

class BankAccount {
    private int balance;
    BankAccount(int balance) { this.balance = balance; }

    void withdraw(int amount) {
        if (amount > balance) {
            throw new InsufficientBalanceException("Balance " + balance + " < requested " + amount);
        }
        balance -= amount;
    }
}
// Output:
// Age cannot be negative
// Entered age was -5
// InsufficientBalanceException: Balance 1000 < requested 2000
```

### try-with-resources closes automatically
```java
public class TryWithResourcesDemo {
    public static void main(String[] args) {
        try (DemoResource r = new DemoResource("db-connection")) {
            System.out.println("using " + r.name);
        }

        try (DemoResource a = new DemoResource("file"); DemoResource b = new DemoResource("socket")) {
            System.out.println("about to fail");
            System.out.println(5 / 0);
        } catch (ArithmeticException e) {
            System.out.println("caught after resources were closed: " + e.getMessage());
        }
    }
}

class DemoResource implements AutoCloseable {
    final String name;
    DemoResource(String name) { this.name = name; System.out.println("open " + name); }

    @Override
    public void close() { System.out.println("close " + name); }
}
// Output:
// open db-connection
// using db-connection
// close db-connection
// open file
// open socket
// about to fail
// close socket
// close file
// caught after resources were closed: / by zero
```

## Common Pitfalls / Tips
- **Don't catch `Throwable`** (or `Error`). `catch (Exception e)` is the widest you should normally go, and specific types are better still.
- **Order matters:** child types before parent types, or the code won't compile. Siblings can be in any order.
- **Multi-catch only for unrelated types**, and the variable is implicitly `final` (you can't reassign `e`).
- **Don't throw and catch in the same method.** It hides the problem from the caller. Throw it and let the caller decide.
- **`throws` on `main` is a cop-out.** The exception just ends up as a stack trace. Handle it at the top level.
- **Checked vs unchecked for custom exceptions:** extend `Exception` when callers can reasonably recover and *should* be forced to think about it. Extend `RuntimeException` for programming errors and invalid arguments.
- **Keep the cause when wrapping:** `throw new MyException("context", e)` (add a `(String, Throwable)` constructor that calls `super(message, cause)`) so the original stack trace isn't lost.
- **try-with-resources needs `AutoCloseable`.** Resources close in **reverse** order of opening, before `catch`/`finally` runs.
- **Avoid deep nesting of `try` blocks.** Extract methods so that each one handles one concern.

## Review Questions
1. In a nested `try`, the inner block throws `ArithmeticException` and the inner `catch` handles only `NullPointerException`. What runs next, and which statements are skipped?
2. Draw the hierarchy from `Throwable` down to `NumberFormatException` and `FileNotFoundException`.
3. What is the practical difference between checked and unchecked exceptions? Give two of each.
4. Why does putting `catch (Exception e)` before `catch (ArithmeticException e)` fail to compile?
5. When can you use `catch (A | B e)`, and when can't you?
6. Explain `throw` vs `throws` with one line of code each.
7. Why is throwing an exception and catching it in the same method usually pointless?
8. Write a minimal checked custom exception. How would you make it unchecked instead?
9. What does try-with-resources do, and what must the resource type implement?

<details>
<summary>Answers</summary>

1. The inner `catch` doesn't match, so the exception propagates to the outer `try`'s `catch` blocks. If one matches `ArithmeticException`, it runs. The rest of the inner `try` and the rest of the outer `try` after the inner block are both skipped.
2. `Throwable` → `Exception` → `RuntimeException` → `IllegalArgumentException` → `NumberFormatException`. And `Throwable` → `Exception` → `IOException` → `FileNotFoundException`. (`Error` is the other child of `Throwable`.)
3. The compiler forces you to catch or declare checked exceptions (`IOException`, `SQLException`, `FileNotFoundException`, `ClassNotFoundException`). Unchecked ones (`NullPointerException`, `ArithmeticException`, `IllegalArgumentException`, ...) need no handling to compile. Both kinds happen at run time.
4. `catch (Exception e)` already catches every `ArithmeticException`, so the later block could never run. The compiler rejects unreachable catch blocks.
5. When the types aren't related by inheritance (e.g. `ArithmeticException | NullPointerException`). You can't combine a type with its parent (`ArithmeticException | Exception`), because the subtype is already covered.
6. `throw new IllegalArgumentException("Age cannot be negative");` raises an exception now. `void readFile() throws FileNotFoundException` declares that the method may throw it, which makes the caller responsible for handling it.
7. The caller never learns that anything went wrong. The method behaves as if it had just printed a message. The point of throwing is to let the caller decide how to react.
8. `class InvalidAgeException extends Exception { InvalidAgeException(String m) { super(m); } }`. To make it unchecked, extend `RuntimeException` instead.
9. It automatically calls `close()` on every resource declared in the `try (...)` header when the block exits, normally or by exception, before any `catch`/`finally`. The type must implement `AutoCloseable` (or `Closeable`).
</details>

## Further Study
- Exception chaining: `getCause()`, `initCause()`, the `(String, Throwable)` constructors
- Suppressed exceptions in try-with-resources (`getSuppressed()`)
- Rethrowing and "precise rethrow" (Java 7+)
- Overriding rules: an overriding method can't declare broader checked exceptions
- Helpful `NullPointerException` messages (JEP 358, Java 14+)
- `finally` edge cases: `return` inside `finally`, `System.exit()`
- Designing exception hierarchies for an application (a base `AppException`)
- Java memory management: stack vs heap (next videos, #45–#46)

---

[← #43](43-exception-handling-exceptions-vs-errors.md) · [Index](ReadMe.md) · [#45 →](45-java-memory-stack-heap-method-area.md)
