# 39 · Java Streams: Why, Architecture & Creation
> Full notes: [39-intro-java-streams-creation-architecture.md](../39-intro-java-streams-creation-architecture.md) · [▶ Video](https://youtu.be/-jHyDEXHr_U)

A **stream** is a tool for processing a sequence of data through a chain of operations. It replaces loop-and-if code with a **declarative** pipeline: source → intermediate operations → terminal operation.

## The problem: imperative data processing
- Task: from a list of employees, take the **names** of those earning **more than 50,000**, **sorted**.
- The classic way spells out **how**: create a result list, loop, check, add, then sort.
```java
List<String> result = new ArrayList<>();
for (Employee e : employees) {
    if (e.getSalary() > 50000) {
        result.add(e.getName());
    }
}
Collections.sort(result);
```
- Nothing is wrong with it, but it's **imperative**: the real requirement ("filter, take name, sort") is buried in mechanics (loop, temp list, `if`, `add`). Chain a few more steps and it gets hard to read.

## Why we need streams
- Functional programming wants code that just **lists the operations**, like SQL: `SELECT name FROM employees WHERE salary > 50000 ORDER BY name`. You say *what*, not *how*.
- Java 8 added **Streams** (built to work with lambdas) for exactly this. The same task:
```java
List<String> result = employees.stream()
        .filter(e -> e.getSalary() > 50000)   // WHERE
        .map(Employee::getName)               // SELECT name
        .sorted()                             // ORDER BY
        .toList();
```

| Before streams | With streams |
| :--- | :--- |
| Temp list, loop, `if`, `add`, `sort` | One expression that reads like the requirement |
| Says **how** | Says **what** |
| Each extra step = more nested code | Each extra step = one more line `.op(...)` |
| Easy to break (wrong index, forgotten `add`) | Each step is a small lambda, easy to test and change |

- **`stream()` is a method of the `Collection` interface** (along with `parallelStream()`). So **every `List`, `Set` and `Queue`** (`ArrayList`, `LinkedList`, `HashSet`, `TreeSet`, `ArrayDeque`, `PriorityQueue`, …) can be streamed.
- `Map` is not a `Collection`, so stream its views: `map.keySet().stream()`, `map.values().stream()`, `map.entrySet().stream()`.
- Convention: one operation per line. It's still a single expression.

## Streams process data, they don't store it
- A **collection** (`ArrayList`) **stores** elements in heap memory.
- A **stream** stores **nothing**. `list.stream()` does **not** copy the list. It's a **wrapper over the source** that pulls elements through the operations, one by one.
- Internally a **`Spliterator`** ("splittable iterator") walks the source and feeds elements into the pipeline. You never write the loop.
- A stream **doesn't modify its source**: `filter` and `map` produce new results; the original list is unchanged.

| Collection | Stream |
| :--- | :--- |
| Stores data | Processes data |
| You iterate it yourself (external iteration) | It iterates internally |
| Can be traversed many times | Can be consumed **only once** |
| Eager: elements exist up front | Lazy: work happens only on the terminal operation |

## Pipeline architecture
```
  SOURCE            INTERMEDIATE OPERATIONS               TERMINAL OPERATION
list.stream()  →  .filter(x -> x > 10)  →  .map(x -> x * 2)  →  .toList()
  (where data       (transform / filter; each             (triggers the run,
   comes from)       returns a new Stream)                  returns a result)
```
- Like water through an RO purifier: the **source** is the tap, the **intermediate operations** are the filters, the **terminal operation** is the glass that collects what's left.
- A pipeline has **one source**, **zero or more** intermediate operations, and **exactly one** terminal operation.

### Sources
| Source | Example | Gives |
| :--- | :--- | :--- |
| **Collection** | `list.stream()`, `set.stream()`, `queue.parallelStream()` | `Stream<T>` |
| **Array** | `Arrays.stream(arr)` | `Stream<T>` or `IntStream`… for primitive arrays |
| **Values** | `Stream.of(1, 2, 5)` | `Stream<T>` |
| **Empty** | `Stream.empty()` | an empty `Stream<T>` |
| **Infinite** | `Stream.iterate(1, x -> x + 1)`, `Stream.generate(Math::random)` | an endless `Stream<T>` |
| **Primitive** | `IntStream.of(1, 2)`, `IntStream.range(0, 5)`, `"abc".chars()` | `IntStream`, `LongStream`, `DoubleStream` |

### Intermediate operations
- **Transformations** like `filter(Predicate)`, `map(Function)`, `sorted()`, `distinct()`, `limit(n)`, `skip(n)`, `peek(Consumer)`.
- **Every intermediate operation returns a `Stream`**. That's what makes `.filter(...).map(...)` chaining possible.
- They are **lazy**: calling them only **records** a step. No element is touched yet.

### Terminal operations
- `forEach(Consumer)`, `toList()`, `collect(...)`, `count()`, `findFirst()`, `anyMatch(...)`, `reduce(...)`, `min`/`max`.
- They **trigger** the pipeline and **don't return a `Stream`**: they return a value, a collection, or nothing (`forEach`).
- **No terminal operation → nothing runs**, not even the filter.

### Value processing: vertical, lazy, short-circuiting
- **Vertical processing:** each element goes through **all** stages before the next element starts. Streams never do "filter everything, then map everything" (horizontal).
```
list = [5, 12, 7, 20]     filter(x > 10) → map(x * 2) → toList()

5   → filter ✗  dropped
12  → filter ✓ → map → 24 → added
7   → filter ✗  dropped
20  → filter ✓ → map → 40 → added          result: [24, 40]
```
- **Lazy evaluation:** the pipeline is just a description until the terminal operation pulls elements through it.
- **Short-circuiting:** because elements flow one at a time, some operations can **stop early**, like `break` in a loop. With `findFirst()` instead of `toList()`: 5 is dropped, 12 → 24 is returned, and **7 and 20 are never processed**. Others: `anyMatch`, `allMatch`, `noneMatch`, `findAny`, `limit`.
- Laziness is also what makes **infinite streams** possible.
- *Exception:* stateful operations like `sorted()` must see every element first, so they act as a barrier.

## A consumed stream is dead
- A stream can run **only once**. After its terminal operation it's consumed; any further operation throws **`IllegalStateException: stream has already been operated upon or closed`**.
- Fix: create a **new stream from the source** (`list.stream()` again), or keep a `Supplier<Stream<T>>`.
```java
Stream<Integer> s = list.stream();
s.forEach(System.out::println);
s.count();                      // IllegalStateException

Supplier<Stream<Integer>> fresh = () -> list.stream();
fresh.get().forEach(System.out::println);
fresh.get().count();            // fine, a new stream each time
```

## Code demo: the whole pipeline
```java
import java.util.*;
import java.util.stream.*;

class Employee {
    private String name;
    private int salary;

    Employee(String name, int salary) { this.name = name; this.salary = salary; }
    String getName() { return name; }
    int getSalary() { return salary; }
}

public class StreamPipelineDemo {
    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee("Rohit", 60000), new Employee("Aditya", 75000),
                new Employee("Sonu", 40000), new Employee("Aman", 52000));

        // before streams
        List<String> result = new ArrayList<>();
        for (Employee e : employees)
            if (e.getSalary() > 50000) result.add(e.getName());
        Collections.sort(result);
        System.out.println(result);                    // [Aditya, Aman, Rohit]

        // with streams
        List<String> names = employees.stream()
                .filter(e -> e.getSalary() > 50000)    // Predicate
                .map(Employee::getName)                // Function
                .sorted()
                .toList();                             // terminal
        System.out.println(names);                     // [Aditya, Aman, Rohit]

        // building a pipeline step by step (each step returns a new Stream)
        List<Integer> list = new ArrayList<>(List.of(5, 12, 7, 14));
        Stream<Integer> s = list.stream();             // source
        s = s.filter(x -> x > 10);                     // intermediate
        s = s.map(x -> x * 2);                         // intermediate
        s.forEach(System.out::println);                // terminal: 24, 28
        System.out.println(list);                      // [5, 12, 7, 14]  source unchanged

        // laziness: no terminal operation, so nothing is printed
        list.stream().filter(x -> { System.out.println("checking " + x); return x > 10; });

        // vertical processing + short-circuiting
        Optional<Integer> first = List.of(5, 12, 7, 20).stream()
                .filter(x -> { System.out.println("filter " + x); return x > 10; })
                .map(x -> { System.out.println("map " + x); return x * 2; })
                .findFirst();
        // filter 5
        // filter 12
        // map 12        (7 and 20 are never touched)
        System.out.println(first.get());               // 24

        // a consumed stream is dead
        Stream<Integer> once = list.stream();
        once.count();
        try {
            once.count();
        } catch (IllegalStateException e) {
            System.out.println("Dead: " + e.getMessage());
            // Dead: stream has already been operated upon or closed
        }
    }
}
```

## Creating streams: all the sources
```java
import java.util.*;
import java.util.stream.*;

public class StreamSourcesDemo {
    public static void main(String[] args) {
        // 1. From any Collection: List, Set, Queue
        List<Integer> list = List.of(5, 12, 7);
        list.stream().forEach(x -> System.out.print(x + " "));          // 5 12 7
        System.out.println();

        Set<String> set = new TreeSet<>(Set.of("b", "a", "c"));
        System.out.println(set.stream().map(String::toUpperCase).toList()); // [A, B, C]

        Queue<Integer> queue = new ArrayDeque<>(List.of(3, 1, 2));
        System.out.println(queue.stream().filter(x -> x > 1).count());  // 2

        Map<String, Integer> marks = new TreeMap<>(Map.of("Rohit", 80, "Sonu", 35));
        marks.entrySet().stream()                                       // Map via its views
                .filter(e -> e.getValue() >= 40)
                .forEach(e -> System.out.println(e.getKey()));          // Rohit

        System.out.println(list.parallelStream().map(x -> x * 2).toList()); // [10, 24, 14]

        // 2. From an array (arrays have no .stream(), use Arrays)
        String[] fruits = {"apple", "banana", "cherry"};
        Stream<String> fruitStream = Arrays.stream(fruits);
        System.out.println(fruitStream.filter(f -> f.contains("an")).toList()); // [banana]

        // 3. From values
        Stream<Integer> nums = Stream.of(1, 2, 5, 7, 9);
        System.out.println(nums.filter(x -> x > 4).toList());          // [5, 7, 9]

        // 4. Empty stream: "no data" without returning null
        Stream<String> none = Stream.empty();
        System.out.println(none.count());                               // 0
        System.out.println(findNames(false).toList());                  // []

        // 5. Infinite streams (always cut with limit or a short-circuit)
        System.out.println(Stream.iterate(1, x -> x + 1).limit(5).toList());   // [1, 2, 3, 4, 5]
        System.out.println(Stream.generate(() -> "hi").limit(3).toList());     // [hi, hi, hi]

        // 6. Primitive streams
        System.out.println(IntStream.of(1, 2, 7).sum());                // 10
        System.out.println(IntStream.range(0, 5).boxed().toList());     // [0, 1, 2, 3, 4]
        int[] arr = {4, 8, 6};
        System.out.println(Arrays.stream(arr).max().getAsInt());        // 8  (int[] gives IntStream)
        System.out.println(LongStream.rangeClosed(1, 5).reduce(1, (a, b) -> a * b)); // 120
        System.out.println(DoubleStream.of(1.5, 2.5).average().getAsDouble());      // 2.0
    }

    static Stream<String> findNames(boolean found) {
        return found ? Stream.of("Rohit", "Aman") : Stream.empty();
    }
}
```

## Infinite streams: `iterate` and `generate`
**Why?** Same reason infinite loops exist: sometimes you **don't know in advance how much data** you need (keep producing IDs, read requests until a condition, the first N primes). An infinite stream describes the *sequence*; `limit`, `takeWhile` or a short-circuit like `findFirst` decides *when to stop*.

**What problem it solves:** you separate **"how to produce the next value"** from **"how many values I need"**, with no counter variables or `while (true) … break`.

**It only works because streams are lazy.** Values are produced one at a time, only when the terminal operation asks for them. **Always add a stop**, or the program runs forever.

| `Stream.iterate(seed, next)` | `Stream.generate(supplier)` |
| :--- | :--- |
| Takes a starting value + a `UnaryOperator` | Takes a `Supplier` |
| Each value is **computed from the previous one** | Each value is **independent** (no previous value) |
| Ordered sequences: counters, powers, Fibonacci | Random numbers, constants, fresh objects, IDs from a source |
| `Stream.iterate(1, x -> x * 2)` → 1, 2, 4, 8, … | `Stream.generate(Math::random)` → random doubles |

- Java 9+ adds a **bounded `iterate`**, like a `for` loop: `Stream.iterate(seed, hasNext, next)`.

```java
import java.util.*;
import java.util.stream.*;

public class InfiniteStreamDemo {
    public static void main(String[] args) {
        // iterate: next value depends on the previous one
        Stream.iterate(1, x -> x + 1).limit(5).forEach(x -> System.out.print(x + " ")); // 1 2 3 4 5
        System.out.println();

        System.out.println(Stream.iterate(1, x -> x * 2).limit(6).toList());  // [1, 2, 4, 8, 16, 32]

        System.out.println(Stream.iterate(new int[]{0, 1}, f -> new int[]{f[1], f[0] + f[1]})
                .limit(8).map(f -> f[0]).toList());                          // [0, 1, 1, 2, 3, 5, 8, 13]

        System.out.println(Stream.iterate(10, x -> x > 0, x -> x - 3).toList()); // [10, 7, 4, 1]  (Java 9+)

        // first number above 100 divisible by 7: we don't know how many to try
        System.out.println(Stream.iterate(101, x -> x + 1)
                .filter(x -> x % 7 == 0).findFirst().get());                // 105

        // generate: every value is independent
        System.out.println(Stream.generate(() -> "Hello").limit(3).toList());   // [Hello, Hello, Hello]

        Stream.generate(Math::random).limit(3)
                .forEach(d -> System.out.println(d < 1.0));                 // true (printed 3 times)

        Random rnd = new Random(42);
        List<Integer> dice = Stream.generate(() -> rnd.nextInt(6) + 1).limit(5).toList();
        System.out.println(dice.size());                                    // 5

        // takeWhile (Java 9+): stop by condition instead of a count
        System.out.println(Stream.iterate(1, x -> x * 3)
                .takeWhile(x -> x < 100).toList());                         // [1, 3, 9, 27, 81]
    }
}
```

## Primitive streams
**Why?** `Stream<Integer>` holds **objects**. In `list.stream().filter(x -> x > 10).map(x -> x * 2)`, every comparison unboxes `Integer → int` and every result is boxed back to `Integer`. For large data that's extra objects, GC and time.

**The fix:** **`IntStream`, `LongStream`, `DoubleStream`** work on raw `int`/`long`/`double`, with **no boxing**. As with primitive functional interfaces, there's nothing for `char`/`short`/`byte`/`float`.

**Bonus:** they add **numeric operations** that `Stream<T>` doesn't have: `sum()`, `average()`, `min()`, `max()`, `summaryStatistics()`, `range()`, `rangeClosed()`.
- `average()` returns `OptionalDouble`, `min()`/`max()` return `OptionalInt` (the stream may be empty).
- Their lambdas use primitive interfaces: `IntPredicate`, `IntUnaryOperator`, …

### How to create one
| Way | Example |
| :--- | :--- |
| From values | `IntStream.of(1, 2, 7)`, `DoubleStream.of(1.5, 2.5)` |
| A range | `IntStream.range(1, 5)` → 1..4, `IntStream.rangeClosed(1, 5)` → 1..5 |
| From a primitive array | `Arrays.stream(new int[]{4, 8, 6})` |
| From an object stream | `list.stream().mapToInt(x -> x)` |
| From a String | `"hello".chars()` → `IntStream` of char codes |
| Infinite | `IntStream.iterate(0, x -> x + 2)`, `IntStream.generate(() -> 1)` |
| Empty | `IntStream.empty()` |

```java
import java.util.*;
import java.util.stream.*;

public class PrimitiveStreamDemo {
    public static void main(String[] args) {
        IntStream s = IntStream.of(1, 2, 7);
        System.out.println(s.sum());                                 // 10

        IntStream.range(1, 5).forEach(i -> System.out.print(i + " "));   // 1 2 3 4   (replaces a for loop)
        System.out.println();
        System.out.println(IntStream.rangeClosed(1, 5).sum());       // 15

        int[] marks = {72, 45, 90, 38};
        System.out.println(Arrays.stream(marks).max().getAsInt());   // 90
        System.out.println(Arrays.stream(marks).average().getAsDouble()); // 61.25
        System.out.println(Arrays.stream(marks).filter(m -> m >= 40).count()); // 3

        IntSummaryStatistics stats = Arrays.stream(marks).summaryStatistics();
        System.out.println(stats.getMin() + " " + stats.getMax() + " " + stats.getAverage()); // 38 90 61.25

        // from objects: total salary without boxing
        List<Integer> salaries = List.of(60000, 75000, 40000);
        int total = salaries.stream().mapToInt(Integer::intValue).sum();
        System.out.println(total);                                   // 175000

        // from a String
        System.out.println("hello".chars().filter(c -> c == 'l').count()); // 2

        System.out.println(LongStream.rangeClosed(1, 20).reduce(1, (a, b) -> a * b)); // 2432902008176640000  (20!)
        System.out.println(DoubleStream.of(1.5, 2.5, 3.0).sum());    // 7.0

        System.out.println(IntStream.iterate(0, x -> x + 2).limit(5).boxed().toList()); // [0, 2, 4, 6, 8]

        System.out.println(IntStream.empty().max().isPresent());     // false  (OptionalInt)
    }
}
```

## Hierarchy
```
                    BaseStream<T, S>
         ┌──────────┬──────┴──────┬──────────────┐
     Stream<T>   IntStream    LongStream    DoubleStream
```
- All four live in `java.util.stream`. The primitive streams extend **`BaseStream` directly**; an `IntStream` is **not** a `Stream<Integer>`.

## Converting between stream types
| From → To | Method |
| :--- | :--- |
| `Stream<T>` → primitive | `mapToInt`, `mapToLong`, `mapToDouble` |
| primitive → `Stream` | `boxed()`, or `mapToObj(...)` to build objects |
| `IntStream` → `LongStream` / `DoubleStream` | `asLongStream()`, `asDoubleStream()`, or `mapToLong` / `mapToDouble` |
| `LongStream` → `DoubleStream` | `asDoubleStream()` |

```java
import java.util.*;
import java.util.stream.*;

public class StreamConversionDemo {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 3, 4);

        // object -> primitive
        IntStream ints = list.stream().mapToInt(x -> x);
        System.out.println(ints.sum());                                        // 10

        List<String> words = List.of("java", "streams");
        System.out.println(words.stream().mapToInt(String::length).max().getAsInt()); // 7
        System.out.println(list.stream().mapToDouble(x -> x / 2.0).sum());     // 5.0

        // primitive -> object
        Stream<Integer> boxed = IntStream.range(1, 4).boxed();
        System.out.println(boxed.toList());                                    // [1, 2, 3]
        System.out.println(IntStream.rangeClosed(1, 3).mapToObj(i -> "#" + i).toList()); // [#1, #2, #3]

        // primitive -> primitive
        LongStream squares = IntStream.rangeClosed(1, 3).mapToLong(x -> (long) x * x);
        System.out.println(squares.boxed().toList());                          // [1, 4, 9]
        System.out.println(IntStream.of(1, 2).asDoubleStream().boxed().toList()); // [1.0, 2.0]
        System.out.println(IntStream.of(5, 6).asLongStream().sum());           // 11
    }
}
```

**Remember:** a stream **processes** data from a source without storing or changing it, and every `Collection` has `stream()`. Pipeline = **source → lazy intermediate operations (return `Stream`) → one terminal operation (triggers everything)**. Elements flow vertically, so short-circuiting and infinite streams (`iterate` = from the previous value, `generate` = independent) work. A stream runs once. Use `IntStream`/`LongStream`/`DoubleStream` to avoid boxing, and convert with `mapToInt`/`boxed`/`mapToObj`/`asLongStream`.
