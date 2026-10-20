# Java Lambdas & Functional Interfaces | Comparator Interface | Java Full Course #37
> Source: https://youtu.be/0V-w2BywAM0 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 49:38. These notes were made from the video's English transcript track. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- In classic Java everything revolves around **objects**. Behavior (a method) can't exist or be passed on its own; you must wrap it in a class, create an object, and pass the object. For tiny pieces of logic this is very verbose.
- **`Comparator<T>`** shows the problem. Unlike `Comparable` (one natural ordering, inside the class), a comparator keeps comparison logic **outside** the class, so you can have many: by name, by roll number, by marks. `Collections.sort(list, comparator)` calls its `compare(o1, o2)` whenever it needs to order two elements.
- The road to less code: separate comparator **classes** → **anonymous classes** → **lambda expressions**. `(s1, s2) -> s1.marks - s2.marks` is the whole comparator.
- A lambda only works where a **functional interface** is expected: an interface with **exactly one abstract method** (any number of `default`/`static` methods are fine). `Comparable`, `Comparator` and `Runnable` are examples. Because there's only one abstract method, Java knows which method the lambda implements.
- Java infers the parameter types and the target interface from context (**target typing**). Syntax forms: `(a, b) -> a + b`, `x -> x * x`, `() -> System.out.println("hello")`, and block bodies `{ ...; return ...; }`. Mark your own interfaces with **`@FunctionalInterface`** for a compile-time check.

## Key Concepts

**Behavior as a second-class citizen.** In pure OOP you can pass variables (primitives or object references) into a method, but not a function. To pass "how to do something" you wrap it in an object. *Why it matters:* this is the motivation for lambdas, the start of functional programming in Java.

**`Comparable` vs `Comparator`.** `Comparable<T>` lives inside the class (`compareTo(T other)`) and defines its single natural ordering. `Comparator<T>` is a separate object (`compare(T o1, T o2)`), so one class can be sorted in as many ways as you have comparators, and the class itself stays lightweight. *Why it matters:* different parts of an application (teacher, admin, …) need different orderings of the same data.

**Anonymous class.** A class without a name, declared and instantiated at the point of use (`new Comparator<Student>() { ... }`). It removes the named classes but still wraps the logic in a class body.

**Functional interface.** An interface with exactly one abstract method (SAM, "single abstract method"). It may have any number of `default` and `static` methods. Methods that are public methods of `Object` (like `equals` in `Comparator`) don't count.

**Lambda expression.** `(parameters) -> expression` (read the arrow as "maps to"). It's an implementation of a functional interface's single method, written without the class, method name or types.

**Target typing.** The compiler works out *which* interface a lambda implements, and its parameter types, from where the lambda is used (the parameter type of `sort`, the variable it's assigned to, …).

## Detailed Notes

### Java is object-centric [00:00]
- "Everything in Java revolves around objects." Methods are behavior, fields are characteristics.
- To perform any behavior: create class `X`, put method `m1` in it, create `new X()`, call `x.m1()`. Behavior always rides inside an object.
- So objects are first-class and behaviors are effectively **second-class citizens** [02:17]. That's fine most of the time.

### When you only need behavior [03:34]
- Sometimes you want to treat a behavior independently, e.g. pass a function `doSomething` straight into `m1(doSomething)`.
- Classic Java can't do this. A method argument can only be a variable: a primitive (`int`, `float`, `char`, …) or a reference (e.g. a `Student s1`).
- Other languages popularised passing functions. Java responded with **functional interfaces** and **lambdas**: a shift toward **functional programming**. OOP is still right about 90% of the time; in the other cases forcing everything into objects makes code verbose.

### Real-life analogy: the chef [05:21]
- You (the customer) have a perfect pasta recipe, which is a set of instructions, i.e. a behavior.
- The chef has a `cook(...)` method. In Java terms you can't hand over the recipe itself. You must create a `Person p1 = new Person()`, put the recipe inside the `Person` class as a method, pass `p1`, and the chef calls `p1.recipe()`.
- In real life you'd just hand over the recipe. OOP is supposed to model real life, and here it makes things complicated.

### Why behavior passing matters: sorting students [08:24]
- `Student { String name; int rollNumber; int marks; }` in a list that needs sorting. Java doesn't know the criterion: name, roll number or marks?
- **Option 1: `Comparable`** [10:11]: `implements Comparable<Student>` with `compareTo` returning `this.marks - other.marks`. This works only when one natural ordering is enough.
- **The catch** [11:27]: in a college application, a teacher sorts by marks, the admin office by name, someone else by roll number. One `compareTo` can't serve them all, and baking the logic into `Student` couples the class to one sort order.

### The `Comparator` interface [12:29]
- Keep comparison logic **outside** the class. `Student` stays lightweight with its own fields and methods; an external object knows how to compare students.
- Shape: `interface Comparator<T> { int compare(T o1, T o2); }`. It takes two objects of the same type.
- Implementations [14:00]:
  - `SortByName`: `return s1.name.compareTo(s2.name);` (reusing `String.compareTo`).
  - `SortByMarks`: `return s1.marks - s2.marks;`.
  - `SortByRollNumber`: `return s1.rollNumber - s2.rollNumber;`.
- *Note:* `Comparator` also has many `default` and `static` helper methods (`reversed`, `thenComparing`, `comparing`, `naturalOrder`, …). They don't stop it from being a functional interface.

### Demo: comparator classes [15:32]
- `List<Student> list` with Aditya, Rohit, Rohan, Sonu (different roll numbers and marks).
- `Collections.sort(list)` → compile error (no natural ordering) [18:05]. Adding `implements Comparable<Student>` fixes it but fixes the order to marks. Remove it.
- **`Collections.sort` is overloaded** [20:07]: `sort(list)` and `sort(list, comparator)`.
- Create `class SortByName implements Comparator<Student>`, `SortByRollNumber`, `SortByMarks` [21:08], each overriding `public int compare(Student s1, Student s2)`.
- You can't instantiate the interface, but you can instantiate the classes [22:40]: `Comparator<Student> c1 = new SortByName();` etc. `Collections.sort(list, c1)` sorts by name, `c2` by marks, `c3` by roll number.

### How `sort` uses the comparator [24:42]
- Internally `sort` runs some sorting algorithm (merge sort, quick sort, …; the exact one doesn't matter). Whenever it must decide whether `s1` or `s2` comes first, it calls **`comparator.compare(s1, s2)`** on whatever comparator you passed.
- *Note:* `List.sort`/`Collections.sort` use TimSort (a merge-sort hybrid), which is stable.

### The verbosity problem [25:58]
- The only logic that matters is one `return` line per comparator. To pass it you wrote a class, a method signature, an object, and a variable.
- You can't pass the `return` statement, or even the method, directly. The only way is to wrap it in a class and pass an object of it.

### Step 1: anonymous classes [28:16]
- Skip the named classes: `Collections.sort(list, new Comparator<Student>() { @Override public int compare(Student s1, Student s2) { return s1.marks - s2.marks; } });`
- The `SortBy…` classes can be deleted. It's "anonymous" because the class has no name.
- **Still verbose** [30:33]: it's still a class, and the logic is still boxed inside a method inside a class. You wanted to pass the "how"; Java made you wrap the "how" in a box (an object).

### Step 2: functional interfaces [31:50]
- **Definition:** an interface with **only one abstract method** (declared, not defined). It may have any number of `static` and `default` methods, since those already have bodies.
- `Comparable<T>` (`compareTo(T)`) and `Comparator<T>` (`compare(T, T)`) both qualify [32:51].
- On top of these special interfaces, Java built lambdas, and with them **functional programming** in Java [33:36]: most of the time objects are right, but sometimes the function is what matters and boxing it is pointless.

### The idea of a lambda [34:22]
- In `SortByMarks` only `return s1.marks - s2.marks;` matters; everything else is noise. Java says: pass me just that logic and I'll handle the rest.
- You can't write a bare `return` as an argument, so Java introduced a syntax for it.

### Lambda syntax [35:54]
- General form: **`(parameters) -> expression`**. The arrow is read "maps to" (or "gives").
- The `compare` method becomes `(s1, s2) -> s1.marks - s2.marks`: the parameters of `compare`, an arrow, and the returned expression. No `return` keyword is needed for a single expression.

### How Java understands it [37:41]
- `sort(list, ___)` expects a `Comparator`. `Comparator` is a functional interface with one method, `compare`. So the lambda must be the body of `compare`. There's no ambiguity, which is exactly why a single abstract method is required.
- Whatever follows the arrow is the return value; whatever precedes it are the method's parameters.

### Demo: the lambda version [38:57]
- `Collections.sort(list, (s1, s2) -> s1.marks - s2.marks);` compiles and sorts by marks.
- **Types are inferred** [40:14]: the list is a `List<Student>`, so the comparator must be a `Comparator<Student>`, so `s1` and `s2` are `Student`s. The result (negative/zero/positive) drives the sort exactly as before.
- *Tip:* modern Java offers ready-made comparators: `Comparator.comparingInt(s -> s.marks)`, `Comparator.comparing(s -> s.name)`, `.reversed()`, `.thenComparing(...)`. Also `list.sort(cmp)` does the same as `Collections.sort(list, cmp)`.

### The forms of a lambda [41:30]
- **Multiple parameters:** `(a, b) -> a + b`, equivalent to `int sum(int a, int b) { return a + b; }`. Types come from the expected interface.
- **Single parameter** [42:31]: `(x) -> x * x`. The parentheses are optional: `x -> x * x`.
- **No parameters** [43:02]: `() -> System.out.println("hello")`. It takes nothing and returns nothing (the method is `void`); the println is just a statement, not a return.
- **Multi-line (block) body** [43:32]: `(a, b) -> { int sum = a + b; return sum; }`. Curly braces are **mandatory**, and so is `return` if the method returns a value.
- **Keep lambdas short** [44:49]. If the logic grows into many lines and gets complex, a proper class (OOP) is clearer. Functional style shines when the logic is small.
- *Additions:* parameter types may be written explicitly, `(Student s1, Student s2) -> ...` (all or none). A lambda can use local variables from the enclosing method only if they are **effectively final**.

### Target typing [46:05]
- Java figures out from the **context** where the lambda is used: which functional interface it is (here `Comparator`), which method it implements (`compare`), and the parameter types (`Student`, from the list). This is called **target typing**, and it's why you don't have to write all that code.
- *Consequence:* the same lambda text can be different things in different contexts, e.g. `() -> {}` can be a `Runnable` or your own no-arg interface. A lambda on its own (`var f = x -> x;`) doesn't compile because there's no target type.

### Why a functional interface is required [47:21]
- If an interface had two abstract methods `f1` and `f2`, Java couldn't tell which one a lambda implements. So lambdas require exactly one abstract method.

### Your own functional interface [48:38]
- Any interface with one abstract method works: `interface Demo { void m1(); }`.
- Optional **`@FunctionalInterface`** annotation: the compiler then **errors** if anyone adds a second abstract method later. Recommended for compile-time safety.
- Next video: Java's built-in functional interfaces (`Predicate`, `Function`, `Consumer`, `Supplier`, …) and method references.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`. The sample data uses different values from the video so that each ordering gives a visibly different result. The last block intentionally doesn't compile.

### Step 0: separate comparator classes
```java
import java.util.*;

public class ComparatorClassesDemo {
    static class Student {
        String name; int rollNumber; int marks;
        Student(String name, int rollNumber, int marks) {
            this.name = name; this.rollNumber = rollNumber; this.marks = marks;
        }
        @Override public String toString() { return name + "," + rollNumber + "," + marks; }
    }

    static class SortByName implements Comparator<Student> {
        public int compare(Student s1, Student s2) { return s1.name.compareTo(s2.name); }
    }
    static class SortByRollNumber implements Comparator<Student> {
        public int compare(Student s1, Student s2) { return Integer.compare(s1.rollNumber, s2.rollNumber); }
    }
    static class SortByMarks implements Comparator<Student> {
        public int compare(Student s1, Student s2) { return Integer.compare(s1.marks, s2.marks); }
    }

    public static void main(String[] args) {
        List<Student> list = new ArrayList<>(List.of(
                new Student("Aditya", 103, 89),
                new Student("Rohit", 101, 98),
                new Student("Rohan", 104, 85),
                new Student("Sonu", 102, 93)));

        Comparator<Student> c1 = new SortByName();
        Comparator<Student> c2 = new SortByMarks();
        Comparator<Student> c3 = new SortByRollNumber();

        Collections.sort(list, c1);
        System.out.println(list); // [Aditya,103,89, Rohan,104,85, Rohit,101,98, Sonu,102,93]
        Collections.sort(list, c2);
        System.out.println(list); // [Rohan,104,85, Aditya,103,89, Sonu,102,93, Rohit,101,98]
        Collections.sort(list, c3);
        System.out.println(list); // [Rohit,101,98, Sonu,102,93, Aditya,103,89, Rohan,104,85]
    }
}
```

### Step 1 and 2: anonymous class vs lambda
```java
import java.util.*;

public class AnonymousToLambda {
    record Student(String name, int rollNumber, int marks) {}

    public static void main(String[] args) {
        List<Student> list = new ArrayList<>(List.of(
                new Student("Aditya", 103, 89),
                new Student("Rohit", 101, 98),
                new Student("Rohan", 104, 85),
                new Student("Sonu", 102, 93)));

        // Anonymous class: no named class, but still a class body
        Collections.sort(list, new Comparator<Student>() {
            @Override
            public int compare(Student s1, Student s2) {
                return s1.marks() - s2.marks();
            }
        });
        System.out.println(list.get(0).name()); // Rohan

        // Lambda: just the logic; types inferred from List<Student>
        Collections.sort(list, (s1, s2) -> s1.name().compareTo(s2.name()));
        System.out.println(list.get(0).name()); // Aditya

        // Same thing with explicit parameter types and a block body
        list.sort((Student s1, Student s2) -> {
            int diff = s2.marks() - s1.marks();   // descending by marks
            return diff;
        });
        System.out.println(list.get(0).name()); // Rohit

        // Modern helpers built on Comparator's static/default methods
        list.sort(Comparator.comparingInt(Student::rollNumber));
        System.out.println(list.get(0).name()); // Rohit
        list.sort(Comparator.comparingInt(Student::marks).reversed());
        System.out.println(list.get(0).name()); // Rohit
        list.sort(Comparator.comparing(Student::name).reversed());
        System.out.println(list.get(0).name()); // Sonu
    }
}
```

### The forms of lambda syntax, with your own functional interfaces
```java
public class LambdaSyntaxForms {
    @FunctionalInterface
    interface BinaryOp { int apply(int a, int b); }

    @FunctionalInterface
    interface UnaryOp { int apply(int x); }

    @FunctionalInterface
    interface Action {
        void run();
        default void runTwice() { run(); run(); }   // default methods are allowed
        static Action noop() { return () -> {}; }   // static methods too
    }

    static int calculate(int a, int b, BinaryOp op) { return op.apply(a, b); } // behavior as an argument

    public static void main(String[] args) {
        BinaryOp sum = (a, b) -> a + b;              // multiple parameters
        UnaryOp square = x -> x * x;                 // single parameter, no parentheses
        Action hello = () -> System.out.println("hello"); // no parameters, no return value
        BinaryOp sumVerbose = (a, b) -> {            // block body: braces + return required
            int s = a + b;
            return s;
        };

        System.out.println(sum.apply(2, 3));         // 5
        System.out.println(square.apply(4));         // 16
        hello.run();                                 // hello
        hello.runTwice();                            // hello
                                                     // hello
        System.out.println(sumVerbose.apply(10, 20)); // 30
        System.out.println(calculate(6, 7, (a, b) -> a * b)); // 42
        Action.noop().run();                         // (prints nothing)

        int base = 100;                              // effectively final → usable inside a lambda
        UnaryOp addBase = x -> x + base;
        System.out.println(addBase.apply(5));        // 105

        Runnable r = () -> System.out.println("same lambda shape, different target type");
        r.run();                                     // same lambda shape, different target type
    }
}
```

### `@FunctionalInterface` catches a second abstract method
```java
// does not compile
public class NotFunctional {
    @FunctionalInterface
    interface Demo {
        void m1();
        void m2(); // error: Demo is not a functional interface (multiple non-overriding abstract methods)
    }

    public static void main(String[] args) {
        Demo d = () -> System.out.println("which method is this?");
    }
}
```

## Common Pitfalls / Tips
- **Lambdas need a functional-interface target.** You can't assign a lambda to `Object` or `var`, or pass it where a class or a multi-method interface is expected.
- **`Comparable` vs `Comparator` names:** `compareTo(T other)` lives inside the class; `compare(T o1, T o2)` lives in a separate comparator object. Don't mix them up.
- **Avoid subtraction in comparators** (`s1.marks - s2.marks`). It can overflow; use `Integer.compare` or `Comparator.comparingInt(...)`.
- **Block bodies need braces and an explicit `return`.** `x -> { x * x }` doesn't compile; write `x -> x * x` or `x -> { return x * x; }`.
- **Captured local variables must be effectively final.** Changing them before or after the lambda gives a compile error.
- **Keep lambdas short.** If one grows past a few lines, move the logic to a named method (and use a method reference) or a proper class.
- **Use `@FunctionalInterface` on your own interfaces.** It documents intent and prevents someone from accidentally adding a second abstract method.
- **`this` inside a lambda refers to the enclosing instance**, unlike in an anonymous class, where it refers to the anonymous object.

## Review Questions
1. Why are behaviors called "second-class citizens" in classic Java? What does that force you to do?
2. When would you choose a `Comparator` over `Comparable`? What are their method names and signatures?
3. How does `Collections.sort(list, comparator)` use the comparator internally?
4. What problem do anonymous classes solve, and why are they still considered verbose?
5. What exactly is a functional interface? Does a `default` or `static` method disqualify an interface?
6. Rewrite `new Comparator<Student>() { public int compare(Student a, Student b) { return a.marks - b.marks; } }` as a lambda.
7. List the lambda syntax forms (multiple, single and no parameters, block body) and the rules for parentheses, braces and `return`.
8. What is target typing, and why must a lambda's target interface have exactly one abstract method?

<details>
<summary>Answers</summary>

1. Java is designed around objects: any behavior must live as a method inside a class and be invoked through an object. To pass behavior to a method you have to wrap it in a class, create an object and pass that object, because method arguments can only be primitives or references.
2. Use `Comparator` when there's no single obvious ordering, when you need several orderings (by name, marks, roll number) in different places, or when you can't or don't want to modify the class. `Comparable<T>` has `int compareTo(T other)` inside the class; `Comparator<T>` has `int compare(T o1, T o2)` in a separate object.
3. Its sorting algorithm repeatedly needs to decide the order of two elements; each time it calls `comparator.compare(a, b)` and uses the sign of the result: negative means `a` first, positive means `b` first, zero means equal.
4. They remove the need to declare and name a separate class for each comparator, because the class is defined inline where it's used. But you still write a whole class body with an overridden method signature around one line of logic.
5. An interface with exactly one abstract method. Any number of `default` and `static` methods are allowed, since they have bodies. Abstract methods that are public methods of `Object` (like `equals` in `Comparator`) don't count either.
6. `(a, b) -> a.marks - b.marks` (better: `(a, b) -> Integer.compare(a.marks, b.marks)` or `Comparator.comparingInt(s -> s.marks)`).
7. `(a, b) -> a + b`; `x -> x * x` (parentheses optional for exactly one untyped parameter); `() -> System.out.println("hello")` (empty parentheses required); `(a, b) -> { int s = a + b; return s; }` (braces required for multiple statements, and `return` required if a value is returned). Single-expression bodies return their value implicitly.
8. Target typing means the compiler infers what a lambda is (which interface, which method, which parameter types) from the context where it appears, e.g. the `Comparator<Student>` parameter of `sort`. It needs exactly one abstract method so that the lambda can be matched to a single method without ambiguity.
</details>

## Further Study
- Built-in functional interfaces in `java.util.function`: `Predicate`, `Function`, `Consumer`, `Supplier`, `BiFunction`, `UnaryOperator`, `BinaryOperator` (next video)
- Method references: `Student::getName`, `String::compareTo`, `System.out::println`
- `Comparator.comparing`, `comparingInt`, `thenComparing`, `reversed`, `nullsFirst`/`nullsLast`
- Effectively final variables and closures; lambdas vs anonymous classes (`this`, shadowing, `invokedynamic`)
- Streams API (`filter`, `map`, `sorted`, `collect`), where lambdas are used everywhere
- `Runnable` and `Callable` with lambdas in multithreading

---

[← #36](36-comparable-collections-class.md) · [Index](../../ReadMe.md) · [#38 →](38-lambdas-functional-interfaces-method-references.md)
