# 11 · Functions · Overloading · Recursion
> Full notes: [11-java-functions-recursion-overloading.md](../11-java-functions-recursion-overloading.md) · [▶ Video](https://youtu.be/IhtjMWXwNCc)

## Why functions?
- Adding two numbers is `int sum = i + j; println(sum);`. Then you need another pair, and another, and you end up **writing the same code each time**. That's **no code reusability**.
- **The fix:** put the repeated work into one **block of code** (a **function**), and **call** it whenever you need it. Each call does the work and gives you back the result.
- **Write once, call many times.** If the logic changes, you fix it in **one** place.

## Function basics (brief)
```java
static int sum(int a, int b) {     // return type, name, parameters
    int result = a + b;
    return result;                 // hands the output back
}

int total = sum(4, 5);             // call it: 4 and 5 are the ARGUMENTS → 9
```
- **Parts:** `static` (always for now, explained after OOP), the **return type** (`int`, or `void` if nothing is returned), the **name**, the **parameters** (placeholders like `int a, int b`) and **`return`**.
- **Parameters vs arguments:** parameters are in the definition (`a`, `b`). Arguments are the values you pass when you call it (`4`, `5`).
- Functions are written **inside the class, but outside `main`**.
- **Four kinds:**

| Kind | Example |
| :--- | :--- |
| no input, no output | `static void greet() { println("Hello"); }` |
| input, no output | `static void sayHello(String name)` |
| no input, output | `static int getNumber() { return 10; }` |
| input and output | `static int multiply(int a, int b) { return a * b; }` |

- You're allowed to **ignore a return value**: `getNumber();` compiles, and the 10 is simply lost.
- **`main` is a function too.** It's the **entry point**: the JVM runs only `main`, and other functions run only when they're called from it (directly or indirectly).

---

## Function (method) overloading
**The same name, with different parameters.** Instead of `sum2()` or `sum3()`, reuse the name `sum`, and the compiler picks the right version from the arguments you pass.

The compiler tells overloads apart by their **parameter list**:

| Rule | Example |
| :--- | :--- |
| **different number** of parameters | `sum(int a, int b)` vs `sum(int a, int b, int c)` |
| **different types** of parameters | `sum(int a, int b)` vs `sum(double a, double b)` |
| **different order** of parameter types | `greet(String name, int age)` vs `greet(int age, String name)` |
| ❌ **return type only** | `void fun()` vs `int fun()` → *"Duplicate method fun()"* |

```java
public class OverloadingDemo {
    static int sum(int a, int b)           { return a + b; }
    static int sum(int a, int b, int c)    { return a + b + c; }          // different number
    static double sum(double a, double b)  { return a + b; }              // different types

    static void greet(String name, int age) { System.out.println(name + " is " + age); }
    static void greet(int age, String name) { System.out.println(age + " years: " + name); } // different order

    public static void main(String[] args) {
        System.out.println(sum(2, 3));        // 5
        System.out.println(sum(2, 3, 4));     // 9
        System.out.println(sum(2.5, 1.5));    // 4.0
        greet("Aditya", 28);                  // Aditya is 28
        greet(28, "Rohit");                   // 28 years: Rohit
    }
}
```
- **Why the return type alone doesn't count:** a call can ignore its return value (`fun();`), so the compiler couldn't tell which version you meant. Fix it by renaming one or changing its parameters.
- An `int` function returning `a + b` of two `double`s needs a cast: `return (int) (a + b);`. Otherwise you get *"Type mismatch: cannot convert from double to int"*.

## Chaining functions
- **One function calls another**, which calls another. `main` → `a` → `b`. When `b` finishes, control goes **back to `a`, right after the call**. Then `a` finishes and returns to `main`.
- If each function prints something **after** its call, the output comes out in **reverse order** of the calls:

```java
public class ChainingDemo {
    public static void main(String[] args) {
        fun1();
        System.out.println("Bye");          // 4th
    }
    static void fun1() {
        fun2();
        System.out.println("Hi");           // 3rd
    }
    static void fun2() {
        fun3();
        System.out.println("Hello");        // 2nd
    }
    static void fun3() {
        System.out.println("How are you?"); // 1st
    }
    // How are you?
    // Hello
    // Hi
    // Bye
}
```
```
Call stack (grows downward, then unwinds):
main → fun1 → fun2 → fun3        ← going down
                     fun3 prints "How are you?", returns
              fun2 prints "Hello", returns
       fun1 prints "Hi", returns
main prints "Bye"                ← coming back up
```
- This "go down, then come back up" is the **call stack** (stack memory, #45).

## Scope of a variable
**Scope** is where a variable can be used.

| Scope | Declared | Lives | Visible in |
| :--- | :--- | :--- | :--- |
| **Local** | inside any `{ }` block (a method, `if` or loop) | until that block ends | only that block |
| **Parameter** | in the method's `( )` | while the method runs | that method |
| **Class-level** (static field) | in the class body, outside every method: `static String name = "Aditya";` | as long as the class | every static method in the class |

```java
public class ScopeDemo {
    static String name = "Aditya";             // class-level: every static method can see it

    public static void main(String[] args) {
        int x = 5, y = 10;                     // local to main
        fun(x, y);                             // pass them to share them
        if (x > 0) {
            int j = 1;                         // local to the if block
            System.out.println(j);             // 1
        }
        // System.out.println(j);              // ❌ j cannot be resolved (the block has ended)
        System.out.println(name);              // Aditya
    }

    static void fun(int x, int y) {            // NEW variables that receive copies of main's values
        // main's x and y are NOT visible here unless they're passed in
        System.out.println(x + y + " " + name);   // 15 Aditya
    }
}
```
- Variables in `main` are **not visible** in another function (*"x cannot be resolved to a variable"*). Pass them as arguments instead. The parameters are **different variables** that receive copies of the values.
- Java has no true global variables. "Global" here means a **static field** of the class. Without `static`, you get *"Cannot make a static reference to the non-static field"*.

---

## Recursion
**A function that calls itself.** It's the extreme case of chaining, where the function calls itself instead of a different one.

Every recursive function needs two parts:
1. **A base case:** a condition where it **stops** calling itself (the "ground floor").
2. **A recursive call:** the same function on a **smaller input**.

- Without a base case, `void a() { a(); }` keeps calling itself. Each call uses up a stack frame, so Java soon crashes with a **`StackOverflowError`**.

### Example 1: print 1 to n
```java
static void printNum(int n) {
    if (n == 0) return;            // base case
    printNum(n - 1);               // recursive call (going down)
    System.out.println(n);         // runs on the way back UP
}
```
**Dry run of `printNum(5)`:**
```
printNum(5) → printNum(4) → printNum(3) → printNum(2) → printNum(1) → printNum(0)  ← base case, returns
                                                          prints 1 ◄──┘
                                            prints 2 ◄────┘
                              prints 3 ◄────┘
                prints 4 ◄────┘
  prints 5 ◄────┘
Output: 1 2 3 4 5
```
- **On the way down,** each call is paused at its recursive call. **On the way back up,** each one carries on and prints.
- **Move the `println` above the recursive call** and you get **5 4 3 2 1**, because it then prints on the way down.
- **Base case analogy:** it's like the ground floor. You can walk down the stairs to it but no lower, and then you climb back up.

### Example 2: Fibonacci
- **Series:** 1, 1, 2, 3, 5, 8, 13, …, where each term is the **sum of the previous two**.
- **Recursive formula:** **fib(n) = fib(n − 1) + fib(n − 2)**, with **base cases** fib(0) = 1 and fib(1) = 1 (the video's numbering, so fib(5) = 8).

```java
static int fib(int n) {
    if (n == 0 || n == 1) return 1;   // base cases
    int x = fib(n - 1);
    int y = fib(n - 2);
    return x + y;
}
```
**Two recursive calls per level make a recursion tree:**
```
                    fib(4)=5
                 /           \
           fib(3)=3         fib(2)=2
           /      \          /     \
      fib(2)=2  fib(1)=1  fib(1)=1 fib(0)=1
       /    \
  fib(1)=1 fib(0)=1        ← the leaves are base cases. Values add up on the way back.
```
- The naive version **recomputes the same subproblems** (fib(2) is calculated twice above), so it takes **exponential time, about O(2ⁿ)**. Memoization or a loop makes it **O(n)**.
- The more common numbering is F(0) = 0, F(1) = 1.

### Run both examples
```java
public class RecursionDemo {
    static void printNum(int n) {
        if (n == 0) return;
        printNum(n - 1);
        System.out.print(n + " ");
    }

    static void printReverse(int n) {
        if (n == 0) return;
        System.out.print(n + " ");      // print BEFORE the call → on the way down
        printReverse(n - 1);
    }

    static int fib(int n) {
        if (n == 0 || n == 1) return 1;
        return fib(n - 1) + fib(n - 2);
    }

    public static void main(String[] args) {
        printNum(5);      System.out.println();   // 1 2 3 4 5
        printReverse(5);  System.out.println();   // 5 4 3 2 1
        for (int i = 0; i <= 6; i++) System.out.print(fib(i) + " ");
        System.out.println();                     // 1 1 2 3 5 8 13
    }
}
```

### Tips
- **Why use recursion when a loop can print 1..n?** Recursion is best for problems that are **naturally recursive** and awkward to write with loops, such as trees and divide-and-conquer. These come up a lot in DSA.
- **Trust the recursive call.** Don't trace every level in your head. Assume `f(n − 1)` already works, and just decide (1) the base case and (2) how to use its result.
- **Practice:** start easy (print 1..n and n..1), then move on to harder DSA problems.

**Remember:** functions give **reuse**. **Overloading** means the same name with a different number, type or order of parameters (the return type alone doesn't count). Calls form a **stack**: they go down, then come back up. A variable lives only inside its `{ }` block. **Recursion** = a **base case** + a **call on a smaller input**. Without a base case, you get a `StackOverflowError`.
