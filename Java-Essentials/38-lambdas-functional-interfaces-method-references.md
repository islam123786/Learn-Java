# 38 · Functional Interfaces Deep Dive, Method References & Composition
> Full notes: [38-lambdas-functional-interfaces-method-references.md](../38-lambdas-functional-interfaces-method-references.md) · [▶ Video](https://youtu.be/IsC0Ms2iFi8)

Lambdas can be passed **and stored** wherever a functional interface is expected. `java.util.function` ships ready-made interfaces for almost every shape, method references shorten lambdas further, and composition methods (`andThen`, `compose`, `and`, `or`, `negate`) build pipelines out of small functions.

## Recap: writing your own functional interface
- A **functional interface** has exactly **one abstract method**. `default` and `static` methods don't count.
- `@FunctionalInterface` is optional, but the compiler then **errors** if a second abstract method is added.
- A lambda is the body of that one method. Java picks the method and the parameter types from the target interface.
- **Before lambdas:** one class per behavior (`class Addition implements Calculator`). **With lambdas:** pass the logic directly, with no class and no `new`.
- You rarely need your own: `java.util.function` covers ~95% of cases. Write one when you want a **meaningful domain name** (`Calculator`, `Validator`, `DiscountRule`) or a shape the JDK doesn't have (e.g. three arguments).

```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

@FunctionalInterface
interface Greeter {
    String greet(String name);
}

@FunctionalInterface
interface TriFunction {                    // the JDK has no 3-argument Function
    int apply(int a, int b, int c);
}

class Addition implements Calculator {     // the old way: a whole class for one line
    @Override
    public int calculate(int a, int b) { return a + b; }
}

public class OwnInterfaceDemo {
    static void print(int a, int b, Calculator c) {
        System.out.println(c.calculate(a, b));
    }

    public static void main(String[] args) {
        print(5, 4, new Addition());       // 9   (class + object)
        print(5, 4, (a, b) -> a + b);      // 9   (same logic, no class)
        print(5, 4, (a, b) -> a * b);      // 20  (new behavior, still no class)
        print(5, 4, (a, b) -> Math.max(a, b)); // 5

        Greeter polite = name -> "Good morning, " + name;
        System.out.println(polite.greet("Aditya"));   // Good morning, Aditya

        TriFunction sum3 = (a, b, c) -> a + b + c;
        System.out.println(sum3.apply(1, 2, 3));      // 6
    }
}
```

**In short:** one abstract method → a lambda can implement it. The interface gives the *shape*, the lambda gives the *logic*.

## Storing a lambda in a variable
- A lambda is an **object** that implements the interface, so it can be assigned to a variable of that interface type: `Calculator add = (a, b) -> a + b;`.
- Passing a lambda to a method already did this: the parameter `Calculator c` is just a variable.
- Once stored, behavior is **data**: you can reuse it, pass it, put it in a collection, return it from a method, or swap it at runtime.
- You run it by calling the interface's method: `add.calculate(5, 4)`. The variable itself is not "called" like a function.

```java
import java.util.*;

@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);
}

public class StoreLambdaDemo {
    static Calculator pick(char op) {                 // returning a lambda
        if (op == '+') return (a, b) -> a + b;
        if (op == '-') return (a, b) -> a - b;
        return (a, b) -> a * b;
    }

    public static void main(String[] args) {
        Calculator add = (a, b) -> a + b;             // stored in a variable
        System.out.println(add.calculate(5, 4));      // 9
        System.out.println(add.calculate(10, 20));    // 30  (reused)

        Calculator op = add;                          // copied like any reference
        op = (a, b) -> a % b;                         // reassigned to new behavior
        System.out.println(op.calculate(10, 3));      // 1

        Map<String, Calculator> ops = new LinkedHashMap<>();   // lambdas in a collection
        ops.put("add", (a, b) -> a + b);
        ops.put("sub", (a, b) -> a - b);
        ops.put("pow", (a, b) -> (int) Math.pow(a, b));
        for (String name : ops.keySet())
            System.out.println(name + " = " + ops.get(name).calculate(2, 5));
        // add = 7
        // sub = -3
        // pow = 32

        System.out.println(pick('-').calculate(9, 4)); // 5
    }
}
```

## The four core interfaces (`java.util.function`)
- `import java.util.*;` does **not** import `java.util.function`. Sub-packages need their own import: `import java.util.function.*;`.

How each one looks in the JDK (simplified):
```java
@FunctionalInterface
public interface Function<T, R> {      // T -> R        transform
    R apply(T t);
}

@FunctionalInterface
public interface Consumer<T> {         // T -> nothing  consume
    void accept(T t);
}

@FunctionalInterface
public interface Supplier<T> {         // nothing -> T  supply
    T get();
}

@FunctionalInterface
public interface Predicate<T> {        // T -> boolean  test
    boolean test(T t);
}
```

| Interface | Method | Shape | Typical use |
| :--- | :--- | :--- | :--- |
| `Function<T,R>` | `R apply(T t)` | T → R | convert, map, calculate |
| `Consumer<T>` | `void accept(T t)` | T → void | print, log, save |
| `Supplier<T>` | `T get()` | () → T | generate, create lazily, default value |
| `Predicate<T>` | `boolean test(T t)` | T → boolean | filter, validate |

```java
import java.util.*;
import java.util.function.*;

public class CoreInterfacesDemo {
    public static void main(String[] args) {
        // Function: input -> output
        Function<Integer, Integer> square = x -> x * x;
        Function<String, Integer> length = s -> s.length();
        Function<Integer, String> grade = m -> m >= 40 ? "Pass" : "Fail";
        System.out.println(square.apply(5));        // 25
        System.out.println(length.apply("Java"));   // 4
        System.out.println(grade.apply(35));        // Fail

        // Consumer: input -> nothing
        Consumer<Integer> print = x -> System.out.println("Got " + x);
        Consumer<List<Integer>> addOne = l -> l.add(1);
        print.accept(7);                            // Got 7
        List<Integer> nums = new ArrayList<>();
        addOne.accept(nums);
        System.out.println(nums);                   // [1]

        // Supplier: nothing -> output
        Supplier<String> greeting = () -> "Hello";
        Supplier<Double> random = () -> Math.random();   // must be Double, not Integer
        Supplier<List<String>> newList = () -> new ArrayList<>();
        System.out.println(greeting.get());         // Hello
        System.out.println(random.get() < 1.0);     // true
        System.out.println(newList.get());          // []

        // Predicate: input -> true/false
        Predicate<Integer> isEven = x -> x % 2 == 0;
        Predicate<String> isEmpty = s -> s.isEmpty();
        System.out.println(isEven.test(8));         // true
        System.out.println(isEven.test(7));         // false
        System.out.println(isEmpty.test(""));       // true
    }
}
```

## `Iterable.forEach` takes a `Consumer`
- `forEach` is a **`default` method on `Iterable`**, so every `List`, `Set`, `Queue` has it.
- It loops over the elements and calls `action.accept(element)` on each:
```java
default void forEach(Consumer<? super T> action) {
    Objects.requireNonNull(action);
    for (T t : this) {
        action.accept(t);
    }
}
```

```java
import java.util.*;
import java.util.function.*;

class PrintConsumer implements Consumer<Integer> {   // the long way
    @Override
    public void accept(Integer x) { System.out.print(x + " "); }
}

public class ForEachDemo {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4);

        list.forEach(new PrintConsumer());                // 1 2 3 4
        System.out.println();

        list.forEach(x -> System.out.print(x * 10 + " ")); // 10 20 30 40
        System.out.println();

        Consumer<Integer> show = x -> System.out.print("[" + x + "]");
        list.forEach(show);                               // [1][2][3][4]
        System.out.println();

        Set<String> names = new TreeSet<>(Set.of("Rohit", "Aditya"));
        names.forEach(n -> System.out.println("Hi " + n));
        // Hi Aditya
        // Hi Rohit
    }
}
```

## Why primitive functional interfaces?
- **Generics only work with objects.** `Function<int, int>` is illegal, so you write `Function<Integer, Integer>`.
- Every call then pays a hidden cost:
```java
Function<Integer, Integer> square = x -> x * x;
int r = square.apply(5);
// 1. 5 is autoboxed      int     -> Integer
// 2. x is unboxed        Integer -> int, then x * x
// 3. result is autoboxed int     -> Integer
// 4. r is unboxed        Integer -> int
```
- Each box creates an `Integer` object on the heap (except cached values -128..127), which means extra **memory, garbage collection and time**. Inside a loop that runs millions of times this becomes noticeable.
- **The fix:** primitive versions that take/return `int`, `long`, `double` directly, with no boxing at all.
```java
IntUnaryOperator square = x -> x * x;
int r = square.applyAsInt(5);      // pure int math, no objects
```
- **Only `int`, `long` and `double`** get versions. These are the types used for computation: `byte`/`short`/`char` are promoted to `int` anyway, and `float` is covered by `double`.
- The method names change so the return type is clear: `applyAsInt`, `applyAsLong`, `applyAsDouble`, `getAsInt`, …
- The same idea returns later as `IntStream`, `LongStream`, `DoubleStream` and `OptionalInt`.

## The primitive families
Naming pattern: **`Int…`** = takes an `int`. **`ToInt…`** = returns an `int`. **`Obj…`** = takes an object plus a primitive. Replace `Int` with `Long` or `Double` for the other two types.

### Function family
| Interface | Shape | Method |
| :--- | :--- | :--- |
| `IntFunction<R>` / `LongFunction<R>` / `DoubleFunction<R>` | primitive → object | `R apply(int value)` |
| `ToIntFunction<T>` / `ToLongFunction<T>` / `ToDoubleFunction<T>` | object → primitive | `int applyAsInt(T value)` |
| `IntToLongFunction`, `IntToDoubleFunction`, `LongToIntFunction`, `DoubleToIntFunction`, … | primitive → other primitive | `long applyAsLong(int value)` |

```java
IntFunction<String> stars = n -> "*".repeat(n);           // int -> String
System.out.println(stars.apply(3));                        // ***

DoubleFunction<String> price = d -> "Rs " + d;             // double -> String
System.out.println(price.apply(99.5));                     // Rs 99.5

ToIntFunction<String> len = s -> s.length();               // String -> int
System.out.println(len.applyAsInt("Java"));                // 4

ToDoubleFunction<Integer> half = x -> x / 2.0;             // Integer -> double
System.out.println(half.applyAsDouble(7));                 // 3.5

IntToDoubleFunction toKm = m -> m / 1000.0;                // int -> double
System.out.println(toKm.applyAsDouble(2500));              // 2.5
```

### Consumer family
| Interface | Shape | Method |
| :--- | :--- | :--- |
| `IntConsumer` / `LongConsumer` / `DoubleConsumer` | primitive → void | `void accept(int value)` |
| `ObjIntConsumer<T>` / `ObjLongConsumer<T>` / `ObjDoubleConsumer<T>` | (object, primitive) → void | `void accept(T t, int value)` |

```java
IntConsumer show = n -> System.out.println("n = " + n);
show.accept(42);                                           // n = 42

DoubleConsumer tax = amt -> System.out.println("Tax: " + amt * 0.18);
tax.accept(100.0);                                         // Tax: 18.0

ObjIntConsumer<String> repeat = (s, n) -> System.out.println(s.repeat(n));
repeat.accept("ab", 3);                                    // ababab

ObjLongConsumer<String> log = (user, id) -> System.out.println(user + "#" + id);
log.accept("rohit", 9_000_000_000L);                       // rohit#9000000000
```

### Supplier family
| Interface | Shape | Method |
| :--- | :--- | :--- |
| `IntSupplier` / `LongSupplier` / `DoubleSupplier` | () → primitive | `int getAsInt()` |
| `BooleanSupplier` | () → boolean | `boolean getAsBoolean()` |

- Only one variant per type: a supplier takes **no input**, so there's nothing to mix, and it can only return one value.
```java
IntSupplier dice = () -> (int) (Math.random() * 6) + 1;
System.out.println(dice.getAsInt());                       // 1..6

LongSupplier now = () -> System.currentTimeMillis();
System.out.println(now.getAsLong() > 0);                   // true

DoubleSupplier pi = () -> Math.PI;
System.out.println(pi.getAsDouble());                      // 3.141592653589793

BooleanSupplier ready = () -> true;
System.out.println(ready.getAsBoolean());                  // true
```

### Predicate family
| Interface | Shape | Method |
| :--- | :--- | :--- |
| `IntPredicate` / `LongPredicate` / `DoublePredicate` | primitive → boolean | `boolean test(int value)` |

- They have `and`, `or`, `negate` too.
```java
IntPredicate isPositive = n -> n > 0;
System.out.println(isPositive.test(-1));                   // false

IntPredicate isEven = n -> n % 2 == 0;
System.out.println(isPositive.and(isEven).test(8));        // true

LongPredicate isBig = n -> n > 1_000_000_000L;
System.out.println(isBig.test(5_000_000_000L));            // true

DoublePredicate passed = pct -> pct >= 33.0;
System.out.println(passed.test(32.5));                     // false
```

### Operator family
- An **operator** is a function whose input and output are the **same type**.

| Interface | Shape | Method |
| :--- | :--- | :--- |
| `IntUnaryOperator` / `LongUnaryOperator` / `DoubleUnaryOperator` | one primitive → same primitive | `int applyAsInt(int operand)` |
| `IntBinaryOperator` / `LongBinaryOperator` / `DoubleBinaryOperator` | two primitives → same primitive | `int applyAsInt(int left, int right)` |
| `UnaryOperator<T>` (object version) | T → T | `T apply(T t)` |
| `BinaryOperator<T>` (object version) | (T, T) → T | `T apply(T t1, T t2)` |

```java
IntUnaryOperator twice = n -> n * 2;
System.out.println(twice.applyAsInt(21));                  // 42
System.out.println(twice.andThen(n -> n + 1).applyAsInt(5)); // 11

IntBinaryOperator add = (a, b) -> a + b;
IntBinaryOperator max = (a, b) -> Math.max(a, b);
System.out.println(add.applyAsInt(2, 3));                  // 5
System.out.println(max.applyAsInt(2, 3));                  // 3

DoubleUnaryOperator celsiusToF = c -> c * 9 / 5 + 32;
System.out.println(celsiusToF.applyAsDouble(100));         // 212.0

DoubleBinaryOperator avg = (a, b) -> (a + b) / 2;
System.out.println(avg.applyAsDouble(3, 4));               // 3.5

UnaryOperator<String> shout = s -> s.toUpperCase() + "!";
System.out.println(shout.apply("hi"));                     // HI!

BinaryOperator<String> join = (a, b) -> a + "-" + b;
System.out.println(join.apply("A", "B"));                  // A-B
```

### Boxed vs primitive in a loop
```java
import java.util.function.*;

public class BoxingCostDemo {
    public static void main(String[] args) {
        Function<Long, Long> boxedInc = x -> x + 1;      // boxes on every call
        LongUnaryOperator primInc = x -> x + 1;          // no boxing at all

        long t1 = System.nanoTime();
        Long a = 0L;
        for (int i = 0; i < 50_000_000; i++) a = boxedInc.apply(a);
        long t2 = System.nanoTime();
        long b = 0;
        for (int i = 0; i < 50_000_000; i++) b = primInc.applyAsLong(b);
        long t3 = System.nanoTime();

        System.out.println(a + " " + b);                 // 50000000 50000000
        System.out.println("boxed:     " + (t2 - t1) / 1_000_000 + " ms");
        System.out.println("primitive: " + (t3 - t2) / 1_000_000 + " ms");  // usually much faster
    }
}
```
- You don't need to memorize every name. Remember the **four core interfaces** and the **naming pattern**; the IDE autocompletes the rest.

## Method references
**The problem:** many lambdas do nothing except forward their argument to a method that already exists. The lambda parameter is pure noise:
```java
list.forEach(x -> System.out.println(x));     // x is received only to be passed on
Function<Integer, Integer> abs = x -> Math.abs(x);
```

**The solution:** point at the method itself with `::` and let Java pass the arguments:
```java
list.forEach(System.out::println);            // "use println for each element"
Function<Integer, Integer> abs = Math::abs;
```
- `::` is the **method reference operator**. There are **no parentheses**: you *refer to* the method, you don't call it. `forEach` calls `accept` later, which calls `println(element)`.
- The method's parameters and return type must fit the functional interface's method.
- **Only works when arguments pass through unchanged.** `x -> x + 5` and `x -> Math.abs(x * x)` must stay lambdas, since no existing method does exactly that.

### The four kinds
| Kind | Lambda | Method reference |
| :--- | :--- | :--- |
| Static method | `x -> Math.abs(x)` | `Math::abs` |
| Instance method of a **particular object** | `x -> System.out.println(x)` | `System.out::println` |
| Instance method of an **arbitrary object** of a type | `s -> s.isEmpty()` | `String::isEmpty` |
| Constructor | `() -> new ArrayList<>()` | `ArrayList::new` |

- In the "arbitrary object" kind, the **first lambda parameter becomes the object** the method is called on: `String::compareTo` means `(a, b) -> a.compareTo(b)`.

```java
import java.util.*;
import java.util.function.*;

class Student {
    String name;
    Student(String name) { this.name = name; }
    String getName() { return name; }
}

public class MethodRefDemo {
    static boolean isAdult(int age) { return age >= 18; }

    public static void main(String[] args) {
        // 1. static method
        Function<Integer, Integer> abs = Math::abs;
        Function<String, Integer> parse = Integer::parseInt;
        Predicate<Integer> adult = MethodRefDemo::isAdult;      // your own static method
        System.out.println(abs.apply(-7));                      // 7
        System.out.println(parse.apply("123") + 1);             // 124
        System.out.println(adult.test(20));                     // true

        // 2. instance method of a particular object
        Consumer<String> printer = System.out::println;
        printer.accept("hello");                                // hello
        String prefix = "Mr. ";
        Function<String, String> addPrefix = prefix::concat;    // prefix.concat(s)
        System.out.println(addPrefix.apply("Rohit"));           // Mr. Rohit

        // 3. instance method of an arbitrary object
        Predicate<String> empty = String::isEmpty;
        Function<String, String> upper = String::toUpperCase;
        Function<Student, String> name = Student::getName;
        BiFunction<String, String, Boolean> same = String::equalsIgnoreCase;
        System.out.println(empty.test(""));                     // true
        System.out.println(upper.apply("java"));                // JAVA
        System.out.println(name.apply(new Student("Aditya")));  // Aditya
        System.out.println(same.apply("JAVA", "java"));         // true

        // 4. constructor
        Supplier<List<Integer>> listMaker = ArrayList::new;
        Function<String, Student> makeStudent = Student::new;   // calls Student(String)
        List<Integer> list = listMaker.get();
        list.add(5);
        System.out.println(list);                               // [5]
        System.out.println(makeStudent.apply("Sonu").name);     // Sonu

        // with collections
        List<String> names = new ArrayList<>(List.of("rohit", "aditya", "sonu"));
        names.sort(String::compareTo);                          // (a, b) -> a.compareTo(b)
        names.forEach(System.out::println);
        // aditya
        // rohit
        // sonu
    }
}
```
- **Readability first.** `System.out::println` is clearer than the lambda. If a reference like `ArrayList::new` confuses your readers, keep the lambda.

## Function composition: `andThen` and `compose`
- Split a calculation into small functions and join them: `(x + 2) * 3` = g(f(x)) with f = `x + 2`, g = `x * 3`.
- **`f.andThen(g)`** → g(f(x)): run `f` **first**, then `g` (left to right).
- **`f.compose(g)`** → f(g(x)): run `g` **first**, then `f` (right to left).
- Both are `default` methods that **return a new `Function`**, so you can keep chaining. Nothing runs until `apply`.
- Inside the JDK, `andThen` is basically `return t -> after.apply(apply(t));`.
- **Type rule:** the output of one step must be the input of the next. `Function<T,R>.andThen(Function<R,V>)` → `Function<T,V>`.

```java
import java.util.function.*;

public class CompositionDemo {
    public static void main(String[] args) {
        Function<Integer, Integer> add2 = x -> x + 2;
        Function<Integer, Integer> multiply3 = x -> x * 3;
        Function<Integer, Integer> divide2 = x -> x / 2;

        // three ways to do (2 + 2) * 3
        int a = add2.apply(2);
        System.out.println(multiply3.apply(a));                         // 12  step by step
        System.out.println(multiply3.apply(add2.apply(2)));             // 12  nested, hard to read
        System.out.println(add2.andThen(multiply3).apply(2));           // 12  reads left to right

        System.out.println(add2.andThen(multiply3).andThen(divide2).apply(2)); // 6

        // andThen vs compose
        System.out.println(add2.andThen(multiply3).apply(5));   // 21  (5 + 2) * 3
        System.out.println(add2.compose(multiply3).apply(5));   // 17  (5 * 3) + 2

        // a stored pipeline, reused
        Function<Integer, Integer> pipeline = add2.andThen(multiply3);
        System.out.println(pipeline.apply(0));                  // 6
        System.out.println(pipeline.apply(10));                 // 36

        // types change along the chain: Integer -> String -> Integer
        Function<Integer, String> toText = x -> "Value:" + x;
        Function<String, Integer> length = s -> s.length();
        Function<Integer, Integer> textLength = toText.andThen(length);
        System.out.println(textLength.apply(12345));           // 11
        // length.andThen(toText) is fine, but toText.compose(toText) won't compile:
        // toText outputs a String, and toText needs an Integer input.

        // identity: a function that returns its input
        Function<Integer, Integer> same = Function.identity();
        System.out.println(same.andThen(add2).apply(1));        // 3
    }
}
```

## Method references inside composition
- Steps of a pipeline are often existing methods, so method references fit naturally.
- Example: cleaning user input.

```java
import java.util.function.*;

public class TextPipelineDemo {
    public static void main(String[] args) {
        Function<String, String> trim = String::trim;
        Function<String, String> toUpper = String::toUpperCase;
        Function<String, Integer> length = String::length;
        Function<String, String> reverse = s -> new StringBuilder(s).reverse().toString();

        System.out.println(trim.andThen(toUpper).apply("   hello   "));             // HELLO
        System.out.println(trim.andThen(toUpper).andThen(reverse).apply("  java ")); // AVAJ
        System.out.println(trim.andThen(length).apply("   hi   "));                 // 2

        Function<String, String> clean = trim.andThen(toUpper).andThen(s -> s.replace(" ", "_"));
        System.out.println(clean.apply("  learn java fast  "));                     // LEARN_JAVA_FAST

        Function<String, Integer> parse = Integer::parseInt;
        Function<Integer, Integer> square = x -> x * x;
        System.out.println(trim.andThen(parse).andThen(square).apply(" 12 "));      // 144
    }
}
```

## Predicate chaining: `and`, `or`, `negate`
- Instead of repeating `if (x > 100 && x % 2 == 0)` everywhere, write **small named predicates** and combine them.
- **`p.and(q)`** → `t -> p.test(t) && q.test(t)`
- **`p.or(q)`** → `t -> p.test(t) || q.test(t)`
- **`p.negate()`** → `t -> !p.test(t)` (no argument). Negating twice gives the original.
- Each returns a **new `Predicate`**, so they chain. Short-circuiting works like `&&` / `||`.
- Java 11+: `Predicate.not(String::isBlank)` negates a method reference.

```java
import java.util.*;
import java.util.function.*;

class Student {
    String name; int marks; int age;
    Student(String name, int marks, int age) { this.name = name; this.marks = marks; this.age = age; }
}

public class PredicateChainDemo {
    public static void main(String[] args) {
        Predicate<Integer> isGreater = x -> x > 100;
        Predicate<Integer> isEven = x -> x % 2 == 0;

        System.out.println(isGreater.and(isEven).test(102));   // true   both
        System.out.println(isGreater.and(isEven).test(101));   // false  101 is odd
        System.out.println(isGreater.or(isEven).test(56));     // true   even is enough
        System.out.println(isGreater.or(isEven).test(55));     // false  neither

        Predicate<Integer> isOdd = isEven.negate();
        System.out.println(isOdd.test(55));                    // true
        System.out.println(isOdd.negate().test(55));           // false  back to isEven

        // bigger chains: (> 100 AND even) OR exactly 7
        Predicate<Integer> special = isGreater.and(isEven).or(x -> x == 7);
        System.out.println(special.test(7));                   // true

        // real life: eligibility without if/else
        Predicate<Student> passed = s -> s.marks >= 40;
        Predicate<Student> isAdult = s -> s.age >= 18;
        Predicate<Student> isEligible = passed.and(isAdult);
        System.out.println(isEligible.test(new Student("Rohit", 50, 19)));  // true
        System.out.println(isEligible.test(new Student("Sonu", 50, 17)));   // false

        // method references + not()
        Predicate<String> notBlank = Predicate.not(String::isBlank);
        Predicate<String> startsWithA = s -> s.startsWith("A");
        List<String> names = new ArrayList<>(List.of("Aditya", " ", "Rohit", "Aman", ""));
        names.removeIf(notBlank.and(startsWithA).negate());   // keep non-blank names starting with A
        System.out.println(names);                            // [Aditya, Aman]
    }
}
```

## Consumer chaining: `andThen`
- A consumer returns nothing, so there's no output to pass on. **`c1.andThen(c2)`** runs **both consumers on the same input**, in order.
- Use it to do several side effects for one value: print, log, save, send.
- There's no `compose` for `Consumer`.

```java
import java.util.*;
import java.util.function.*;

public class ConsumerChainDemo {
    public static void main(String[] args) {
        Consumer<String> printName = System.out::println;
        Consumer<String> printUpper = s -> System.out.println(s.toUpperCase());  // must stay a lambda
        Consumer<String> printLength = s -> System.out.println(s.length());

        printName.andThen(printUpper).accept("Aditya");
        // Aditya
        // ADITYA

        List<String> saved = new ArrayList<>();
        Consumer<String> log = s -> System.out.println("[LOG] " + s);
        Consumer<String> save = saved::add;                     // method reference
        Consumer<String> all = log.andThen(save).andThen(printLength);

        List.of("Rohit", "Sonu").forEach(all);
        // [LOG] Rohit
        // 5
        // [LOG] Sonu
        // 4
        System.out.println(saved);                              // [Rohit, Sonu]
    }
}
```

## Two-argument ("Bi") interfaces
| Interface | Method | Shape |
| :--- | :--- | :--- |
| `BiFunction<T,U,R>` | `R apply(T t, U u)` | (T, U) → R |
| `BiConsumer<T,U>` | `void accept(T t, U u)` | (T, U) → void |
| `BiPredicate<T,U>` | `boolean test(T t, U u)` | (T, U) → boolean |
| `BinaryOperator<T>` | `T apply(T t1, T t2)` | (T, T) → T, a `BiFunction` with all types the same |

- **No `BiSupplier`:** a supplier takes no input, and a method can only return one value.
- Primitive versions: `ToIntBiFunction<T,U>`, `ToLongBiFunction`, `ToDoubleBiFunction`, and the `ObjIntConsumer` family.
- `BiFunction` has `andThen` (with a one-argument `Function`); `BiPredicate` has `and`/`or`/`negate`; `BiConsumer` has `andThen`.
- **`Map.forEach` takes a `BiConsumer<K,V>`**, and `Map.replaceAll` takes a `BiFunction<K,V,V>`.

```java
import java.util.*;
import java.util.function.*;

public class BiInterfacesDemo {
    public static void main(String[] args) {
        BiFunction<Integer, Integer, Integer> power = (a, b) -> (int) Math.pow(a, b);
        BiFunction<String, Integer, String> repeat = String::repeat;   // s.repeat(n)
        System.out.println(power.apply(2, 10));                        // 1024
        System.out.println(repeat.apply("ha", 3));                     // hahaha
        System.out.println(power.andThen(x -> x + 1).apply(2, 3));     // 9

        BiPredicate<String, Integer> longerThan = (s, n) -> s.length() > n;
        System.out.println(longerThan.test("lambda", 3));              // true
        System.out.println(longerThan.negate().test("lambda", 3));     // false

        BiConsumer<String, Integer> show = (k, v) -> System.out.println(k + " -> " + v);
        Map<String, Integer> roll = new TreeMap<>(Map.of("Rohit", 102, "Aditya", 101));
        roll.forEach(show);
        // Aditya -> 101
        // Rohit -> 102

        roll.replaceAll((k, v) -> v + 100);                            // BiFunction<K,V,V>
        System.out.println(roll);                                      // {Aditya=201, Rohit=202}

        BinaryOperator<Integer> max = BinaryOperator.maxBy(Comparator.naturalOrder());
        System.out.println(max.apply(4, 9));                           // 9

        ToIntBiFunction<String, String> totalLen = (a, b) -> a.length() + b.length();
        System.out.println(totalLen.applyAsInt("ab", "cde"));          // 5
    }
}
```

## Imperative vs declarative
| Imperative (classic OOP style) | Declarative (functional style) |
| :--- | :--- |
| Says **how**: loops, indexes, temp variables, if/else | Says **what**: named steps joined together |
| You manage the control flow | The library manages the control flow |
| Logic is mixed with mechanics | Each step is small, named, reusable and testable |

The same task in both styles: from a list of names, print the non-blank ones, trimmed and in upper case.
```java
import java.util.*;
import java.util.function.*;

public class ImperativeVsDeclarative {
    public static void main(String[] args) {
        List<String> names = List.of("  rohit ", " ", "aditya", "");

        // imperative: how
        for (int i = 0; i < names.size(); i++) {
            String s = names.get(i);
            if (!s.isBlank()) {
                String clean = s.trim().toUpperCase();
                System.out.println(clean);
            }
        }
        // ROHIT
        // ADITYA

        // declarative: what
        Predicate<String> notBlank = Predicate.not(String::isBlank);
        Function<String, String> trim = String::trim;
        Function<String, String> clean = trim.andThen(String::toUpperCase);
        Consumer<String> print = System.out::println;

        names.forEach(s -> { if (notBlank.test(s)) print.accept(clean.apply(s)); });
        // ROHIT
        // ADITYA
    }
}
```
- Functional programming in Java is about the declarative style. **Streams** (next video) make it complete: `names.stream().filter(notBlank).map(clean).forEach(print);`.

**Remember:** a lambda is an object of a functional interface, so it can be stored, passed and returned. Function = **transform** (`apply`), Consumer = **consume** (`accept`), Supplier = **supply** (`get`), Predicate = **test** (`test`). Use `Int`/`Long`/`Double` versions to avoid boxing. A method reference (`Class::method`) replaces a lambda that only forwards its arguments. Build pipelines with `andThen`/`compose`, `and`/`or`/`negate` and `Consumer.andThen`; `Bi…` versions take two arguments.
