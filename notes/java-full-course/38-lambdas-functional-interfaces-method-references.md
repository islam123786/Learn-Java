# Lambdas & Functional Interface Deep Dive | Method References & Composition | Java Full Course #38
> Source: https://youtu.be/IsC0Ms2iFi8 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 115:08. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A lambda can be passed **or stored** anywhere a functional interface is expected: `Calculator c = (a, b) -> a + b;`. Writing your own functional interface is rarely needed, because `java.util.function` already covers almost every shape.
- The four core interfaces: **`Function<T,R>`** (`apply`, transform T → R), **`Consumer<T>`** (`accept`, T → nothing), **`Supplier<T>`** (`get`, nothing → T), **`Predicate<T>`** (`test`, T → boolean).
- **Primitive specializations** (`IntFunction`, `ToIntFunction`, `IntConsumer`, `ObjIntConsumer`, `IntSupplier`, `IntPredicate`, `IntUnaryOperator`, `IntBinaryOperator`, plus the `Long`/`Double` versions) skip the autoboxing/unboxing cost. They exist only for `int`, `long` and `double`.
- **Method references** (`System.out::println`, `Math::abs`, `String::isEmpty`, `ArrayList::new`) are shorthand for a lambda that only passes its arguments straight to an existing method.
- **Composition** builds pipelines out of small functions: `f.andThen(g)` = g(f(x)), `f.compose(g)` = f(g(x)); `Predicate` has `and`/`or`/`negate`; `Consumer` has `andThen`. Two-argument versions are `BiFunction`, `BiConsumer`, `BiPredicate`. This is the **declarative** style that Streams rely on.

## Key Concepts

**Functional interface.** An interface with exactly one abstract method (default and static methods don't count). `@FunctionalInterface` is optional, but it makes the compiler reject a second abstract method. *Why it matters:* any lambda or method reference is converted to an instance of one.

**Lambda as a value.** A lambda is just an object implementing the interface's single method. You can pass it to a method parameter or assign it to a variable of the interface type, then call the method (`c.calculate(5, 4)`). *Why it matters:* behavior becomes data you can store, pass and combine.

**`java.util.function`.** The JDK package of ready-made functional interfaces. Note that `import java.util.*;` does **not** import it; sub-packages need their own import (`import java.util.function.*;`). *Why it matters:* you'll use these types everywhere in Streams.

**The four core interfaces.**

| Interface | Method | Shape | Typical use |
|---|---|---|---|
| `Function<T,R>` | `R apply(T t)` | T → R | transform / map |
| `Consumer<T>` | `void accept(T t)` | T → void | print, log, save |
| `Supplier<T>` | `T get()` | () → T | generate, lazily create |
| `Predicate<T>` | `boolean test(T t)` | T → boolean | filter, check a condition |

**Primitive functional interfaces.** Generics only accept objects, so `Function<Integer,Integer>` unboxes the argument, computes, and boxes the result again on every call. In a hot loop that runs millions of times this adds up. The primitive versions take or return `int`/`long`/`double` directly. *Why it matters:* the same idea comes back as `IntStream`, `OptionalInt`, and so on.

**Method reference (`::`).** `ClassOrObject::methodName` with no parentheses: you *refer* to the method instead of calling it, and Java calls it later with the arguments it gets. It only works when the lambda passes its parameters through unchanged (`x -> Math.abs(x)` works, `x -> x + 5` doesn't). *Why it matters:* it's shorter, and often easier to read.

**Function composition.** `andThen` and `compose` are default methods that return a *new* `Function`, so you can keep chaining and call `apply` once at the end. *Why it matters:* a complex transformation becomes a readable list of small named steps.

**Imperative vs declarative.** Imperative code says *how* (loops, indexes, if/else). Declarative code says *what* (`trim.andThen(toUpper)`, `list.forEach(System.out::println)`). *Why it matters:* functional programming in Java, and especially Streams, is about the declarative style.

## Detailed Notes

### Recap and writing your own functional interface [00:00]
- From the previous video: lambdas work only with functional interfaces, and `@FunctionalInterface` gives a compile-time check.
- You'll almost never need your own functional interface: the JDK's cover roughly 95–99% of cases. One reason to write one is to wrap an existing interface with a more meaningful name.
- Demo: `interface Calculator { int calculate(int a, int b); }`.
  - The traditional way: `class Addition implements Calculator` overrides `calculate` to return `a + b`, then `Calculator c = new Addition(); c.calculate(5, 4)` → 9.
  - A helper `print(int a, int b, Calculator c)` prints `c.calculate(a, b)`. This is the same pattern as `Collections.sort(list, comparator)`.
  - Delete the `Addition` class and pass `(a, b) -> a + b` instead. Now you can also pass `(a, b) -> a * b` (→ 20) without writing a new class.

### Storing a lambda in a variable [07:34]
- `Calculator c = (a, b) -> a + b;` compiles. Passing a lambda to a parameter was already doing this, because the parameter is a variable of type `Calculator`.
- This matters a lot for what follows: the core interfaces are almost always used as `Function<…> f = …;` and so on.

### The four core interfaces in `java.util.function` [08:39]
- **`Function<T,R>`** [08:39]: takes an input and returns an output, like the mathematical f(x). Single method `R apply(T t)`.
  - `Function<Integer,Integer> square = x -> x * x; square.apply(5)` → 25.
- **`Consumer<T>`** [14:04]: takes an input and returns nothing. It "consumes" the value. Single method `void accept(T t)`. *(The video first calls this method `apply`, then corrects it to `accept` in the code.)*
  - `Consumer<Integer> print = x -> System.out.println(x); print.accept(7)` prints 7. Typical uses: printing, writing to a log or file.
- **`Supplier<T>`** [18:24]: takes nothing and returns a value. Single method `T get()`.
  - `Supplier<Double> rnd = () -> Math.random(); rnd.get()` returns a random double. It has to be `Supplier<Double>`, because `Math.random()` returns `double`.
- **`Predicate<T>`** [21:39]: takes an input and returns `true`/`false`. Single method `boolean test(T t)`.
  - `Predicate<Integer> isEven = x -> x % 2 == 0; isEven.test(8)` → true, `isEven.test(7)` → false.
- Summary [24:54]: Function = **transform**, Consumer = **consume**, Supplier = **supply**, Predicate = **test**. The shapes T→R, T→void, void→T and T→boolean cover almost every scenario.

### `Iterable.forEach` takes a `Consumer` [25:59]
- `import java.util.*` doesn't bring in `java.util.function`; import it separately.
- `List<Integer> list = List.of(1, 2, …, 0);` can be printed with an index loop, an enhanced for loop, or **`list.forEach(x -> System.out.println(x))`**.
- `forEach` is a **default method on `Iterable`** with the signature `forEach(Consumer<? super T> action)`. Internally it runs an enhanced for loop over `this` and calls `action.accept(t)` for each element.
- You could also pass an object of a class that implements `Consumer<Integer>`. The lambda is just shorter.

### Primitive functional interfaces [30:19]
- The problem [31:24]: in `Function<Integer,Integer> square = x -> x * x`, Java **unboxes** `x` to `int`, multiplies, then **autoboxes** the result back to `Integer`. Run that millions of times in a loop and it gets slow.
- The fix: primitive variants for **`int`, `long` and `double` only**. These are the types used in loops and arithmetic. `byte`/`short` get promoted to `int`, `float` gets replaced by `double`, and `char` is rarely used in hot loops [34:39].
- **Function family** [35:44]:
  - `IntFunction<R>`, `LongFunction<R>`, `DoubleFunction<R>`: primitive in, object out (`R apply(int value)`).
  - `ToIntFunction<T>`, `ToLongFunction<T>`, `ToDoubleFunction<T>` [37:54]: object in, primitive out (`int applyAsInt(T value)`).
- **Consumer family** [38:59]:
  - `IntConsumer`, `LongConsumer`, `DoubleConsumer`: primitive → void.
  - `ObjIntConsumer<T>`, `ObjLongConsumer<T>`, `ObjDoubleConsumer<T>` [40:04]: (object, primitive) → void. *(The video says `ObjLongConsumer` takes "T and int". It takes `T` and `long`.)*
- **Supplier family** [41:08]: `IntSupplier`, `LongSupplier`, `DoubleSupplier` (`getAsInt()` etc.). There's only one variant, since a supplier takes no input and a method can't return two values.
- **Predicate family** [42:13]: `IntPredicate`, `LongPredicate`, `DoublePredicate`: primitive → boolean.
- **Operator family** [43:18]: primitive in *and* primitive out.
  - `IntUnaryOperator`, `LongUnaryOperator`, `DoubleUnaryOperator`: one input, e.g. int → int (`applyAsInt`).
  - `IntBinaryOperator`, `LongBinaryOperator`, `DoubleBinaryOperator`: two inputs, e.g. (int, int) → int.
- You don't need to memorize these. Remember the four core interfaces and their methods; your IDE will autocomplete the rest. Prefer the primitive versions in performance-sensitive code.
- *(Also worth knowing, though the video doesn't name them: the generic `UnaryOperator<T>` (T → T) and `BinaryOperator<T>` ((T, T) → T) are the object versions of the operators.)*

### Method references [45:28]
- If a lambda only forwards its parameter to a method that already exists, you can replace it with a method reference.
  - `x -> System.out.println(x)` is a `Consumer` lambda. `System.out` is a static `PrintStream` field of `System`, and `println` is a method on it.
  - `list.forEach(System.out::println)` means "use this method when needed". There are no parentheses because you aren't calling `println` here. `forEach` calls `accept` on the resulting `Consumer`, which calls `println(element)` for each element.
- `::` is the **method reference operator**. (It looks like C++'s scope resolution operator, but in Java it means this.)
- **When you can't use one** [57:23]: `x -> x + 5`. No existing method does "add 5", so it has to stay a lambda. The same goes for `x -> Math.abs(x * x)`: the argument isn't passed unchanged.

### Kinds of method references [58:28]
- **Static method**: `x -> Math.abs(x)` → `Math::abs` (`ClassName::staticMethod`).
- **Instance method of a particular object** [59:33]: `x -> System.out.println(x)` → `System.out::println`. `out` is an object, so this refers to a method through an instance.
- **Instance method of an arbitrary object of a type** [60:38]: `str -> str.isEmpty()` → `String::isEmpty`. The first lambda parameter becomes the object the method is called on. `Predicate<String> p = String::isEmpty; p.test("hello")` → false, `p.test("")` → true.
- **Constructor reference** [61:43]: `() -> new ArrayList<Integer>()` → `ArrayList::new`. `Supplier<List<Integer>> s = ArrayList::new; List<Integer> list = s.get();`
- Readability first [63:53]: `System.out::println` reads well, but something like `ArrayList::new` can be less clear to some readers. If a method reference makes the code harder to follow, write the lambda.

### Function composition: `andThen` [64:58]
- Idea: `result = (x + 2) * 3` can be split into f(x) = x + 2 and g(x) = x * 3, so result = g(f(x)).
- In code [68:13], with `Function<Integer,Integer> add2 = x -> x + 2` and `multiply3 = x -> x * 3`:
  - Step by step: `int a = add2.apply(2); int b = multiply3.apply(a);` → 12.
  - Nested: `multiply3.apply(add2.apply(2))` → 12, which is correct but hard to read.
  - **`add2.andThen(multiply3).apply(2)`** → 12. It reads left to right: add 2, *and then* multiply by 3.
  - You can keep chaining: `add2.andThen(multiply3).andThen(divide2).apply(2)` → 6.
- How it works inside [75:48]: `andThen` takes a `Function` and **returns a `Function`**, which is why you can chain it again before calling `apply`. The JDK source is essentially `Objects.requireNonNull(after); return (T t) -> after.apply(apply(t));`.
- **Type rule** [80:08]: for `f.andThen(g)`, the output type of `f` must be the input type of `g`. With `f: Function<T,R>` and `g: Function<R,V>`, the result is `Function<T,V>`. Example: `f: Integer → String` and `g: String → String`. Then `f.andThen(g)` works, but `g.andThen(f)` doesn't compile, because `f` can't accept a `String`.

### `compose` (the reverse order) [85:32]
- `f.compose(g)` = f(g(x)): it runs the **argument first**, then `f` (right to left).
  - With `f = x -> x + 2` and `g = x -> x * 2`: `f.andThen(g)` = (x + 2) * 2, but `f.compose(g)` = (x * 2) + 2.
- Types: with `g: Function<V,T>` and `f: Function<T,R>`, `f.compose(g)` is a `Function<V,R>`.
- Both build a "method pipeline". Most people prefer `andThen` because left to right is more intuitive.

### Method references inside compositions [89:52]
- `Function<String,String> toUpper = String::toUpperCase; Function<String,Integer> length = String::length;` then `toUpper.andThen(length)` is a `Function<String,Integer>`.
- A real use [92:02]: clean up user input with `trim.andThen(toUpper).apply("   hello   ")` → `"HELLO"`. You can keep appending steps (reverse, substring, …). The code describes *what* happens, step by step.

### Predicate chaining: `and`, `or`, `negate` [94:12]
- Instead of writing `if (x > 100 && x % 2 == 0)` everywhere, build named predicates and combine them.
- **`and`** [96:22]: `isGreater.and(isEven).test(102)` → true. Internally it's `t -> test(t) && other.test(t)`, returned as a new `Predicate`, so you can keep chaining.
- **`or`** [99:37]: `isGreater.or(isEven).test(56)` → true (56 is not > 100, but it is even).
- **`negate`** [100:42]: `Predicate<Integer> isOdd = isEven.negate(); isOdd.test(55)` → true. It takes no argument and works like `!`. Negating twice gives the original back.
- Real-life example [101:47]: a `Student(marks, age)`. With `passed = s -> s.marks >= 40` and `isAdult = s -> s.age >= 18`, define `isEligible = passed.and(isAdult)`. Then `isEligible.test(new Student(50, 19))` → true, and with age 17 → false. No if/else needed.

### Consumer chaining: `andThen` [105:02]
- A consumer returns nothing, so its `andThen` just runs **both consumers on the same input**, one after the other. Use it to print to the console, write a log line, save to a file, and so on for one value.
- `Consumer<String> printName = System.out::println; Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());`
  `printName.andThen(printUpper).accept("Aditya")` prints `Aditya`, then `ADITYA`.
- `printUpper` has to stay a lambda: no existing method prints a string in upper case.

### Two-argument ("Bi") interfaces [110:27]
- **`BiFunction<T,U,R>`**: `R apply(T t, U u)`.
- **`BiConsumer<T,U>`**: `void accept(T t, U u)`. `Map.forEach` uses it.
- **`BiPredicate<T,U>`**: `boolean test(T t, U u)`.
- There's **no `BiSupplier`**. A supplier takes no input, so there's nothing to make "bi", and a method can only return one value.

### Imperative vs declarative, and what's next [111:32]
- OOP-style loops are **imperative**: `for (int i = 0; i < n; i++) …` spells out *how* to do the work.
- Functional code is **declarative**: `doThis.andThen(doThat)`, `list.forEach(System.out::println)` state *what* to do and hide the how.
- Lambdas are central to modern Java. The next lectures on **Streams** use them heavily.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Your own functional interface, implemented by a class and by lambdas
```java
public class CalculatorLambdaDemo {
    static void print(int a, int b, Calculator c) {
        System.out.println(c.calculate(a, b));
    }

    public static void main(String[] args) {
        print(5, 4, new Addition());          // 9
        print(5, 4, (a, b) -> a * b);         // 20  — no new class needed
        Calculator sub = (a, b) -> a - b;     // a lambda stored in a variable
        print(5, 4, sub);                     // 1
        System.out.println(sub.calculate(10, 3)); // 7
    }
}

@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

class Addition implements Calculator { // the "traditional" way
    @Override
    public int calculate(int a, int b) { return a + b; }
}
```

### The four core interfaces + `forEach`
```java
import java.util.List;
import java.util.function.*;

public class CoreInterfacesDemo {
    public static void main(String[] args) {
        Function<Integer, Integer> square = x -> x * x;
        System.out.println(square.apply(5));        // 25

        Consumer<Integer> print = x -> System.out.println("Got " + x);
        print.accept(7);                            // Got 7

        Supplier<String> greeting = () -> "Hello";
        System.out.println(greeting.get());         // Hello

        Predicate<Integer> isEven = x -> x % 2 == 0;
        System.out.println(isEven.test(8));         // true
        System.out.println(isEven.test(7));         // false

        List<Integer> list = List.of(1, 2, 3);
        list.forEach(x -> System.out.print(x + " ")); // 1 2 3
        System.out.println();
    }
}
```

### Primitive specializations (no boxing)
```java
import java.util.function.*;

public class PrimitiveInterfacesDemo {
    public static void main(String[] args) {
        IntFunction<String> stars = n -> "*".repeat(n);       // int -> object
        System.out.println(stars.apply(3));                   // ***

        ToIntFunction<String> len = s -> s.length();          // object -> int
        System.out.println(len.applyAsInt("Java"));           // 4

        IntConsumer show = n -> System.out.println("n=" + n); // int -> void
        show.accept(42);                                      // n=42

        ObjIntConsumer<String> repeat = (s, n) -> System.out.println(s.repeat(n));
        repeat.accept("ab", 3);                               // ababab

        IntSupplier answer = () -> 42;                        // () -> int
        System.out.println(answer.getAsInt());                // 42

        IntPredicate positive = n -> n > 0;                   // int -> boolean
        System.out.println(positive.test(-1));                // false

        IntUnaryOperator twice = n -> n * 2;                  // int -> int
        IntBinaryOperator add = (a, b) -> a + b;              // (int, int) -> int
        System.out.println(twice.applyAsInt(21));             // 42
        System.out.println(add.applyAsInt(2, 3));             // 5

        long sum = 0;
        LongUnaryOperator inc = v -> v + 1;
        for (int i = 0; i < 1_000_000; i++) sum = inc.applyAsLong(sum); // no boxing in the loop
        System.out.println(sum);                              // 1000000
    }
}
```

### All four kinds of method references
```java
import java.util.*;
import java.util.function.*;

public class MethodRefKindsDemo {
    public static void main(String[] args) {
        // 1. static method: x -> Math.abs(x)
        Function<Integer, Integer> abs = Math::abs;
        System.out.println(abs.apply(-9));          // 9

        // 2. instance method of a particular object: x -> System.out.println(x)
        Consumer<String> printer = System.out::println;
        printer.accept("hi");                       // hi

        // 3. instance method of an arbitrary object: s -> s.isEmpty()
        Predicate<String> empty = String::isEmpty;
        System.out.println(empty.test("hello"));    // false
        System.out.println(empty.test(""));         // true

        // 4. constructor: () -> new ArrayList<>()
        Supplier<List<Integer>> listMaker = ArrayList::new;
        List<Integer> list = listMaker.get();
        list.add(1);
        list.add(2);
        list.forEach(System.out::println);          // 1
                                                    // 2
    }
}
```

### Function composition: `andThen` vs `compose`
```java
import java.util.function.Function;

public class FunctionComposeDemo {
    public static void main(String[] args) {
        Function<Integer, Integer> add2 = x -> x + 2;
        Function<Integer, Integer> multiply3 = x -> x * 3;
        Function<Integer, Integer> divide2 = x -> x / 2;

        System.out.println(multiply3.apply(add2.apply(2)));                    // 12 (nested, hard to read)
        System.out.println(add2.andThen(multiply3).apply(2));                  // 12  (2+2)*3
        System.out.println(add2.andThen(multiply3).andThen(divide2).apply(2)); // 6
        System.out.println(add2.compose(multiply3).apply(2));                  // 8   (2*3)+2

        // types: Function<String,String> andThen Function<String,Integer> = Function<String,Integer>
        Function<String, String> trim = String::trim;
        Function<String, String> toUpper = String::toUpperCase;
        Function<String, Integer> length = String::length;
        System.out.println(trim.andThen(toUpper).apply("   hello   "));       // HELLO
        System.out.println(trim.andThen(length).apply("   hello   "));        // 5
    }
}
```

### Predicate chaining with a real-life example
```java
import java.util.function.Predicate;

public class PredicateChainDemo {
    public static void main(String[] args) {
        Predicate<Integer> isGreater = x -> x > 100;
        Predicate<Integer> isEven = x -> x % 2 == 0;
        Predicate<Integer> isOdd = isEven.negate();

        System.out.println(isGreater.and(isEven).test(102)); // true
        System.out.println(isGreater.or(isEven).test(56));   // true
        System.out.println(isOdd.test(55));                  // true
        System.out.println(isOdd.negate().test(55));         // false (double negate = isEven)

        Predicate<Pupil> passed = s -> s.marks >= 40;
        Predicate<Pupil> isAdult = s -> s.age >= 18;
        Predicate<Pupil> isEligible = passed.and(isAdult);
        System.out.println(isEligible.test(new Pupil(50, 19))); // true
        System.out.println(isEligible.test(new Pupil(50, 17))); // false
    }
}

class Pupil {
    final int marks, age;
    Pupil(int marks, int age) { this.marks = marks; this.age = age; }
}
```

### Consumer chaining and the Bi- interfaces
```java
import java.util.*;
import java.util.function.*;

public class ConsumerAndBiDemo {
    public static void main(String[] args) {
        Consumer<String> printName = System.out::println;
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());
        printName.andThen(printUpper).accept("Aditya"); // Aditya
                                                        // ADITYA

        BiFunction<Integer, Integer, Integer> power = (b, e) -> (int) Math.pow(b, e);
        System.out.println(power.apply(2, 10));         // 1024

        BiPredicate<String, Integer> longerThan = (s, n) -> s.length() > n;
        System.out.println(longerThan.test("lambda", 3)); // true

        Map<String, Integer> roll = new TreeMap<>(Map.of("Rohit", 102, "Aditya", 101));
        BiConsumer<String, Integer> show = (k, v) -> System.out.println(k + " -> " + v);
        roll.forEach(show);                             // Aditya -> 101
                                                        // Rohit -> 102
    }
}
```

## Common Pitfalls / Tips
- **`import java.util.*` doesn't import `java.util.function`.** Wildcard imports don't include sub-packages.
- **Remember the method names:** `apply` (Function), `accept` (Consumer), `get` (Supplier), `test` (Predicate). The primitive versions use `applyAsInt`, `getAsInt`, and so on.
- **Match the generic type to what the lambda returns.** `Supplier<Integer> s = () -> Math.random();` doesn't compile, because `Math.random()` returns `double`.
- **A method reference is a reference, not a call.** Write `System.out::println`, not `System.out::println()`, and only use it when the arguments pass through unchanged.
- **Don't force method references.** If `Foo::bar` is less clear than `x -> x.bar()`, keep the lambda.
- **`andThen` vs `compose` order:** `f.andThen(g)` = g(f(x)) and `f.compose(g)` = f(g(x)). The output type of the first step must match the input type of the next.
- **`andThen`/`and`/`or` return new functions.** Nothing runs until you call `apply`/`test`/`accept` on the result.
- **Boxing cost is real in hot loops.** Use `IntUnaryOperator` and friends (and later `IntStream`) for numeric work.
- **Lambdas can only capture effectively final local variables.** (Not covered in this video, but it's the next thing you'll run into.)

## Review Questions
1. What makes an interface "functional", and what does `@FunctionalInterface` add?
2. Name the four core interfaces in `java.util.function`, their single method, and their input → output shape.
3. Why do `IntFunction`, `ToIntFunction`, `IntUnaryOperator`, etc. exist, and why only for `int`, `long` and `double`?
4. What is the difference between `IntFunction<R>` and `ToIntFunction<T>`? Between `IntConsumer` and `ObjIntConsumer<T>`?
5. When can a lambda be replaced by a method reference? Give an example of one that can't.
6. Name the four kinds of method references, with an example of each.
7. What do `add2.andThen(multiply3).apply(2)` and `add2.compose(multiply3).apply(2)` return, and why can `andThen` be chained?
8. How would you check "student passed **and** is adult" without an `if`? Why is there no `BiSupplier`?

<details>
<summary>Answers</summary>

1. It has exactly one abstract method (default and static methods are allowed). `@FunctionalInterface` is optional; it makes the compiler report an error if someone adds a second abstract method.
2. `Function<T,R>`: `apply`, T → R. `Consumer<T>`: `accept`, T → void. `Supplier<T>`: `get`, () → T. `Predicate<T>`: `test`, T → boolean.
3. Generic interfaces only work with objects, so every call unboxes and reboxes the values. In loops that's measurable overhead. Java specializes only `int`, `long` and `double` because they're the ones used for computation: smaller integer types get promoted to `int`, `double` replaces `float`, and `char` rarely appears in numeric loops.
4. `IntFunction<R>` takes an `int` and returns an object `R`. `ToIntFunction<T>` takes an object `T` and returns an `int`. `IntConsumer` consumes one `int`. `ObjIntConsumer<T>` consumes an object and an `int` together.
5. When the lambda only passes its parameters unchanged to an existing method, e.g. `x -> System.out.println(x)` → `System.out::println`. `x -> x + 5` can't be replaced, because no existing method does that.
6. Static: `Math::abs`. Instance method of a particular object: `System.out::println`. Instance method of an arbitrary object of a type: `String::isEmpty`. Constructor: `ArrayList::new`.
7. 12, because (2 + 2) * 3, and 8, because (2 * 3) + 2. `andThen` returns a new `Function`, not a value, so you can call `andThen` on it again and call `apply` only at the end.
8. `Predicate<Student> eligible = passed.and(isAdult); eligible.test(s);`. A supplier takes no input at all, so a two-input version makes no sense, and a method can only return one value.
</details>

## Further Study
- Lambda capture rules: effectively final variables, and `this` inside lambdas vs anonymous classes
- `UnaryOperator<T>`, `BinaryOperator<T>`, `Function.identity()`, `Predicate.not(...)` (Java 11)
- `Comparator` composition: `Comparator.comparing(...).thenComparing(...).reversed()`
- How lambdas are compiled (`invokedynamic`, `LambdaMetafactory`)
- Checked exceptions inside lambdas
- Java Streams (the next video), which are built on these interfaces

---

[← #37](37-lambdas-functional-interfaces-comparator.md) · [Index](README.md) · [#39 →](39-intro-java-streams-creation-architecture.md)

---

[← #37](37-lambdas-functional-interfaces-comparator.md) · [Index](README.md) · [#39 →](39-intro-java-streams-creation-architecture.md)
