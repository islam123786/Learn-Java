# Java Functions Deep Dive | Recursion | Method Overloading | Java Full Course #11
> Source: https://youtu.be/IhtjMWXwNCc · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 84:50. The video is in Hindi and has no English transcript, so these notes were written from the Hindi auto-transcript. It had no timing data either, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A **function** (called a *method* once we reach OOP) is a named, reusable **block of code**. Write it once and **call** it wherever you need it, instead of repeating the same lines.
- Anatomy: `static int sum(int a, int b) { return a + b; }` has the **return type** (`int`, or `void` if nothing is returned), the **name**, the **parameters** (`int a, int b`) and the body with `return`. In a call such as `sum(4, 5)`, the values you pass are the **arguments**.
- `main` is itself a function (`static void main(String[] args)`). The JVM calls it as the program's **entry point**, and a function you never call from it never runs. Calls can **chain** (main → a → b), and each one returns to its **caller**.
- **Overloading**: several functions with the **same name** but different parameter **count, types or order**. A different **return type alone isn't enough** ("Duplicate method").
- **Scope**: a variable lives only inside the `{}` block that declares it. Class-level `static` variables are visible to every method in the class. **Recursion** is a function calling itself: it needs a **base case** to stop and a **recursive call** on a smaller input (e.g. print 1..n, Fibonacci).

## Key Concepts

**Function / method.** A block of code with a name that performs one task and can be called many times. *Why it matters:* it removes duplication and makes code readable and maintainable. Java calls these *methods* because they always live inside a class (explained in the OOP part).

**Calling a function.** Writing its name with arguments, `sum(i, j)`. Control jumps into the function, runs it, and returns to the caller with the result. *Why it matters:* it's how the flow of control moves between functions.

**Return type and `return`.** The data type of the value the function hands back, e.g. `int`. `void` means it returns nothing; a bare `return;` is then optional. *Why it matters:* the caller can store, print or ignore the returned value.

**Parameters vs arguments.** Parameters are the placeholder variables in the definition (`int a, int b`). Arguments are the actual values passed in the call (`4, 5` or `i, j`). *Why it matters:* the arguments are copied into the parameters.

**Function categories (by input/output).** No input & no output, input & no output, no input & output, and input & output. *Why it matters:* it's a simple way to think about what a function needs and produces, like a maths function f(x) that takes an input and gives an output.

**`main` as entry point.** The JVM looks for `public static void main(String[] args)` and runs it line by line. *Why it matters:* everything else runs only if it's reachable from `main`.

**Function chaining (call stack).** A function can call another, which calls another, and so on. They finish in reverse order. *Why it matters:* it's the foundation for understanding recursion and the stack memory.

**Method overloading.** Same name, different parameter lists. The compiler picks the version whose parameters match the arguments. *Why it matters:* related operations (`sum` of 2 ints, 3 ints, 2 doubles) can share one intuitive name.

**Scope (local vs class-level).** Local variables exist only in their block (method, `if`, loop). A `static` field declared in the class body is visible to all static methods of the class (the video calls it "global"). *Why it matters:* it explains "x cannot be resolved to a variable".

**Recursion.** A function that calls itself with a smaller or simpler input. It needs a **base case** that stops the chain. *Why it matters:* many problems (trees, divide and conquer, backtracking) are naturally recursive.

## Detailed Notes

### Why functions? [00:00]
- Add two numbers: `int i = 4, j = 5; int sum = i + j; println(sum);`. Later you need to add two more numbers (`k = 9`, `l = 10`), and later still another pair, writing the same code each time.
- That's **no code reusability**. The fix [02:08]: put the repeated work into one **block of code** (a function) and **call** it whenever needed. Each call does the work and gives you the result.

### Writing your first function [03:12]
- Functions are written **inside the class but outside `main`**.
- Building `sum` step by step [04:16]:
  - Name: `sum`.
  - Parameters in parentheses: `(int a, int b)`, the inputs it works on.
  - Body: `int result = a + b; return result;`. `return` hands the output back.
  - **Return type** [06:25]: the data type the function returns. `result` is an `int`, so the function is `int sum(int a, int b)`.
  - `static` [07:29]: for now, always write it. The reason comes after OOP. Result: `static int sum(int a, int b) { … }`.
- Calling [08:33]: `int result = sum(i, j);`. `i` is copied into `a` and `j` into `b`, the function returns 9, and the `=` stores it in `result`.
- Call it as often as you like with different arguments, including literals: `int c = sum(10, 9);` gives 19.

### Naming the parts [10:41]
- Return type, function name, **parameters** (the type and name pairs the function expects) and the `return` statement (returns the output).
- `sum(i, j)` is **calling a function**. The values passed (4 and 5) are **arguments** [11:45]. The definition only says "give me any two ints a and b"; those placeholders are the **parameters**.

### Functions in maths vs programming [13:54]
- f(x): input → processing → output. A Java function is the same idea: `sum` takes `i, j` and produces `result`.

### Four kinds of functions [14:58]
- No input, no output; input but no output; no input but output; input and output (like `sum`).
- In the video's terminology they're "functions" until OOP, after which they're called methods.

### Coding the four kinds [17:06]
- **No input, no output:** `static void greet() { System.out.println("Hello"); }`. `void` means nothing is returned, and a bare `return;` at the end is optional.
- **Input, no output** [19:15]: `static void sayHello(String name) { System.out.println("Hello " + name); }`. A function can take **any number** of parameters.
- **No input, output** [20:19]: `static int getNumber() { return 10; }`. Not very useful by itself, but it shows the shape.
- **Input and output** [22:27]: `static int multiply(int a, int b) { return a * b; }`. The expression is evaluated, then returned.

### Calling them from `main` [23:31]
- `greet();` makes control jump into `greet`, print "Hello", return, and continue with the next line.
- `sayHello("Rohit");` prints "Hello Rohit". Pass a variable or a literal. The IDE shows a hint like `name:` for the parameter.
- `int x = getNumber();`, or directly `System.out.println(getNumber());`.
- **Ignoring a return value** [28:52] is allowed: `getNumber();` alone compiles, and the 10 is simply lost.
- `System.out.println(multiply(2, 4));` prints 8.

### `main` is a function too [31:01]
- `public static void main(String[] args)`: `static`, return type `void`, name `main`, and one parameter, a `String` array named `args`. (`public` and `args` are explained later.) A `return;` at the end of `main` is also optional.
- The JVM finds `main` and executes it line by line [32:05]. If `main` is empty, nothing runs, even if other functions are defined.
- So `main` is the **entry point**. Other functions run only when called (directly or indirectly) from `main`.

### Function (method) overloading [35:17]
- `sum(int a, int b)` works for any pair: `sum(2, 3)` is 5, `sum(5, 6)` is 11. Adding three numbers would need `sum(3, 4) + 5`, which is clumsy.
- You could write `sum2(int a, int b, int c)`, but Java lets you reuse the name `sum` [39:34]. The compiler tells the versions apart by their **parameters**:
  - **Different number of parameters**: `sum(int, int)` vs `sum(int, int, int)`.
  - **Different types of parameters** [40:38]: `sum(double a, double b)`. Returning `a + b` from an `int` function needs a cast, `return (int)(a + b);` ("Type mismatch: cannot convert from double to int").
  - **Different order of parameters** [41:43]: `greet(String name, int age)` vs `greet(int age, String name)`. `greet("Aditya", 28)` calls the first and `greet(28, "Rohit")` calls the second.
- **Return type alone doesn't count** [44:55]: `void fun()` and `int fun()` give "Duplicate method fun()". Why: a call can ignore its return value (`fun();`), so the compiler couldn't tell which one you meant. To fix it, rename one or change its parameters.

### Chaining functions [48:08]
- `main` calls `a`, `a` calls `b`, `b` prints "Hello". When `b` finishes, control returns to `a` just after the call, then `a` finishes and returns to `main`.
- If each function prints something *after* its call, the output appears in reverse order of the calls [52:24]. With `main → fun1 → fun2 → fun3`, where fun3 prints "How are you?" and each caller prints after its call, the output is: How are you?, Hello, Hi, Bye.
- This "go down, then come back up" is the **call stack** (stack memory, covered later).

### Scope of a variable [55:37]
- `x` and `y` declared in `main` are **not visible** in another function: "x cannot be resolved to a variable".
- Fix: pass them as arguments (`fun(x, y)` with `static void fun(int x, int y)`), or declare new variables there. Those are different variables from `main`'s.
- **Local scope**: a variable declared inside any `{}` block (method, `if`, loop) exists only inside that block and is discarded when the block ends [58:49]. A `j` declared in an `if` can't be printed after the `if`.
- **Class-level ("global") scope** [59:54]: declare it in the class body outside every method, e.g. `static String name = "Aditya";`. Every static method in the class can use it, and it lives as long as the class does.
- *Note:* Java has no true global variables; these are **static fields** of the class. Without `static` you get "Cannot make a static reference to the non-static field".

### Recursion [62:02]
- Definition: **a function that calls itself**. It's the extreme case of chaining, where the function calls itself instead of a different one.
- `void a() { a(); }` would call itself forever. Real recursion always has a **base case** [64:10], a condition under which it stops calling itself.
- *Correction:* without a base case the video says the calls loop forever. In Java each call consumes a stack frame, so the program crashes quickly with a **`StackOverflowError`**.

### Example: print 1 to n recursively [64:10]
```
static void printNum(int n) {
    if (n == 0) return;          // base case
    printNum(n - 1);             // recursive call
    System.out.println(n);       // runs on the way back up
}
```
- Dry run for `printNum(5)` [66:19]:
  - Going down: 5 isn't 0 so call `printNum(4)`, then 3, 2, 1, 0.
  - At 0 the base case returns.
  - Coming back up: the call with n = 1 continues after its recursive call and prints 1, then the n = 2 call prints 2, … up to 5, which returns to `main`. Output: 1 2 3 4 5.
- **Base case** [73:48]: without it the calls would continue to -1, -2, … It's the "ground floor": you can go down the stairs to it, but no lower, and then you climb back up.
- **Recursive call**: `printNum(n - 1)`, the same function on a smaller input.
- Why bother when a `for` loop can print 1..n [74:52]? Recursion shines on problems that are naturally recursive and awkward to write with loops, which come up a lot in DSA.
- Tip [75:56]: don't trace every level in your head. **Trust the recursive call**: assume `f(n - 1)` already works, and just decide how to use its result plus the base case.

### Example: Fibonacci [75:56]
- Series: 1, 1, 2, 3, 5, 8, 13, … where each term is the sum of the previous two.
- Recursive definition: **fib(n) = fib(n - 1) + fib(n - 2)** [78:05], with base cases fib(0) = 1 and fib(1) = 1 (the video's indexing, so fib(4) = 5 and fib(5) = 8).
- Code: `int x = fib(n - 1); int y = fib(n - 2); return x + y;`, after `if (n == 0 || n == 1) return 1;`.
- Two recursive calls per level form a **recursion tree** [79:09]: fib(5) → fib(4) and fib(3) → … down to fib(1)/fib(0) leaves (the base cases). Values are summed on the way back up: fib(2) = 2, fib(3) = 3, …
- *Note:* the more common convention is F(0) = 0, F(1) = 1. Also, this naive version recomputes the same subproblems, so it takes exponential time (about O(2ⁿ)). Memoization or a loop makes it O(n).
- Practice suggestion: easy recursion first (print 1..n, n..1), then harder DSA problems.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### The four kinds of functions
```java
public class FunctionKinds {
    // no input, no output
    static void greet() {
        System.out.println("Hello");
        return; // optional in a void function
    }

    // input, no output
    static void sayHello(String name) {
        System.out.println("Hello " + name);
    }

    // no input, output
    static int getNumber() {
        return 10;
    }

    // input and output
    static int multiply(int a, int b) {
        return a * b;
    }

    static int sum(int a, int b) {
        int result = a + b;
        return result;
    }

    public static void main(String[] args) {
        greet();                                  // Hello
        sayHello("Rohit");                        // Hello Rohit
        int x = getNumber();
        System.out.println(x);                    // 10
        getNumber();                              // return value ignored: legal
        System.out.println(multiply(2, 4));       // 8

        int i = 4, j = 5;
        int result = sum(i, j);                   // arguments i, j -> parameters a, b
        System.out.println(result);               // 9
        System.out.println(sum(10, 9));           // 19
    }
}
```

### Overloading by count, type and order
```java
public class Overloading {
    static int sum(int a, int b) { return a + b; }
    static int sum(int a, int b, int c) { return a + b + c; }      // different count
    static int sum(double a, double b) { return (int) (a + b); }   // different types

    static void greet(String name, int age) {                     // different order
        System.out.println("Hi " + name + ". Your age is " + age);
    }
    static void greet(int age, String name) {
        System.out.println("Hi " + name + ". Your age is " + age + " (age first)");
    }

    // static int sum(int x, int y) { return 0; }  // would not compile: same parameter types as sum(int, int)
    // static void fun() {}  +  static int fun() { return 5; }  // would not compile: return type alone is not enough

    public static void main(String[] args) {
        System.out.println(sum(2, 3));        // 5
        System.out.println(sum(5, 6));        // 11
        System.out.println(sum(3, 5, 6));     // 14
        System.out.println(sum(2.5, 3.9));    // 6 (6.4 cast to int)
        System.out.println(sum(2.5, 3));      // 5 (3 widened to double; 5.5 cast to int)
        greet("Aditya", 28);                  // Hi Aditya. Your age is 28
        greet(28, "Rohit");                   // Hi Rohit. Your age is 28 (age first)
    }
}
```

### Duplicate method: return type alone
```java
// does not compile
public class DuplicateMethod {
    static void fun() { System.out.println("Hello"); }
    static int fun() { return 5; }   // error: method fun() is already defined in class DuplicateMethod

    public static void main(String[] args) {
        fun();                       // which one? the compiler cannot decide
    }
}
```

### Chaining: calls go down, results come back up
```java
public class Chaining {
    static void fun3() {
        System.out.println("How are you?");
    }
    static void fun2() {
        fun3();
        System.out.println("Hello");
    }
    static void fun1() {
        fun2();
        System.out.println("Hi");
    }

    public static void main(String[] args) {
        fun1();
        System.out.println("Bye");
        // How are you?
        // Hello
        // Hi
        // Bye
    }
}
```

### Scope: local vs class-level (static) variables
```java
public class ScopeDemo {
    static String name = "Aditya";        // class-level: visible to all static methods

    static void fun(int x, int y) {       // x, y here are new variables (parameters)
        System.out.println(x + " " + y + " " + name);
    }

    public static void main(String[] args) {
        int x = 4, y = 5;                 // local to main
        System.out.println(x + ", " + y); // 4, 5
        fun(x, y);                        // 4 5 Aditya

        if (x == 4) {
            int j = 7;                    // local to this if-block
            System.out.println(j);        // 7
        }
        // System.out.println(j);         // would not compile: j cannot be resolved
        int j = 70;                       // a different j, fine because the block ended
        System.out.println(j);            // 70
    }
}
```

### Recursion: print 1..n, n..1, and Fibonacci
```java
public class RecursionDemo {
    static void printNum(int n) {
        if (n == 0) return;          // base case
        printNum(n - 1);             // recursive call first...
        System.out.print(n + " ");   // ...then print on the way back up
    }

    static void printReverse(int n) {
        if (n == 0) return;
        System.out.print(n + " ");   // print first, then recurse
        printReverse(n - 1);
    }

    // Video's convention: fib(0) = 1, fib(1) = 1
    static int fib(int n) {
        if (n == 0 || n == 1) return 1;
        int x = fib(n - 1);
        int y = fib(n - 2);
        return x + y;
    }

    public static void main(String[] args) {
        printNum(5);
        System.out.println();            // 1 2 3 4 5
        printReverse(5);
        System.out.println();            // 5 4 3 2 1
        System.out.println(fib(4));      // 5
        System.out.println(fib(5));      // 8
        for (int i = 0; i <= 6; i++) System.out.print(fib(i) + " ");
        System.out.println();            // 1 1 2 3 5 8 13
    }
}
```

### Missing base case
```java
// throws
public class NoBaseCase {
    static int depth = 0;

    static void a() {
        depth++;
        a();                         // no base case
    }

    public static void main(String[] args) {
        try {
            a();
        } catch (StackOverflowError e) {
            System.out.println("StackOverflowError after roughly " + (depth / 1000) + "k calls");
            // exact count depends on the JVM's stack size
        }
    }
}
```

## Common Pitfalls / Tips
- **Write methods inside the class but outside `main`.** A method can't be nested inside another method.
- **Non-`void` methods must return on every path.** Otherwise you get "missing return statement". And `return` with a value is an error in a `void` method.
- **Return type mismatch.** Returning a `double` from an `int` method needs an explicit cast (and loses the fraction).
- **Overloading uses the parameter list only.** Count, types and order count; parameter *names* and return type don't. `sum(int a, int b)` and `sum(int x, int y)` are duplicates.
- **Overload resolution can surprise you.** `sum(2, 3)` picks `sum(int, int)` even though `sum(double, double)` also fits, because exact matches beat widening. Combinations like `f(int, double)` and `f(double, int)` called as `f(1, 2)` are ambiguous and don't compile.
- **Java passes arguments by value.** Changing a parameter inside a method doesn't change the caller's variable. For arrays, the reference is copied, so element changes *are* visible to the caller.
- **Every recursion needs a base case that's actually reached.** `fib(-1)` with the code above never hits `n == 0 || n == 1` and overflows the stack.
- **Naive recursive Fibonacci is exponential.** Fine for learning; use memoization or iteration for large n.
- **"Global" variables are `static` fields.** Keep them to a minimum; prefer passing parameters.

## Review Questions
1. What problem do functions solve? Give the four parts of `static int sum(int a, int b) { return a + b; }`.
2. What is the difference between a parameter and an argument? Identify both in `int r = sum(i, j);`.
3. What does `void` mean, and is `return;` required in a `void` function?
4. Why is `main` special, and what happens to a function that is never called from it?
5. Which changes make two methods with the same name valid overloads? Why does a different return type alone fail?
6. With `main → fun1 → fun2 → fun3`, where each function prints after its call returns, in what order are the lines printed?
7. Why can't a function see a variable declared in `main`? How do you share a value between them?
8. What are the base case and the recursive call in `printNum`? Why does it print 1..5 and not 5..1? What happens without a base case?

<details>
<summary>Answers</summary>

1. They remove repeated code: write the logic once and call it many times. The parts are the return type (`int`), the name (`sum`), the parameters (`int a, int b`), and the body with a `return` statement (`static` is required for now).
2. Parameters are the placeholder variables in the definition (`a`, `b`). Arguments are the actual values passed in the call (`i`, `j`, i.e. 4 and 5), which are copied into the parameters.
3. `void` means the function returns no value. A bare `return;` is optional; the function also returns when it reaches the closing brace.
4. The JVM starts execution at `main`, the entry point. A function that is never called (directly or indirectly) from `main` never runs.
5. A different number of parameters, different parameter types, or a different order of parameter types. Return type alone fails because a call can ignore the returned value (`fun();`), so the compiler couldn't decide which method to run, and Java therefore treats them as duplicates.
6. fun3's line first, then fun2's, then fun1's, then main's: the reverse of the call order, because each function finishes only after the one it called returns.
7. Local variables exist only inside the block that declares them. Pass the value as an argument (or, sparingly, use a class-level `static` field).
8. The base case is `if (n == 0) return;` and the recursive call is `printNum(n - 1);`. The print comes after the recursive call, so it runs while the calls return, from n = 1 up to n = 5. Without a base case the calls continue into negative numbers until Java throws `StackOverflowError`.
</details>

## Further Study
- Classes and objects: why `static` is needed here, and methods vs functions
- `public`/`private` access modifiers and `String[] args` (command-line arguments)
- The call stack and stack frames (stack memory deep dive)
- Pass-by-value in Java with primitives vs references (arrays, objects)
- Varargs (`static int sum(int... nums)`) as an alternative to many overloads
- Overload resolution rules: exact match, widening, boxing, varargs
- Recursion practice: factorial, sum of digits, power, reverse a string, binary search
- Memoization and dynamic programming (efficient Fibonacci); tail recursion (not optimized by the JVM)

---

[← #10](10-how-arrays-work-in-java.md) · [Index](ReadMe.md) · [#12 →](12-oop-classes-objects-new-keyword.md)
