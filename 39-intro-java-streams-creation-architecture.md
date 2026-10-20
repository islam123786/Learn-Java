# Introduction to Java Streams | Creation & Architecture | Java Full Course #39
> Source: https://youtu.be/-jHyDEXHr_U · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 63:20. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A **stream** is a tool for processing a sequence of data through a chain of operations. It turns imperative loop-and-if code into **declarative**, SQL-like pipelines: `employees.stream().filter(...).map(...).sorted().toList()`.
- Every pipeline has three parts: a **source** (collection, array, `Stream.of`, infinite generator), zero or more **intermediate operations** (`filter`, `map`, `sorted`, `distinct`, `limit`, …), which all **return a `Stream`**, and exactly one **terminal operation** (`forEach`, `toList`, `collect`, `count`, `findFirst`, …), which returns a result or nothing.
- Streams **don't store data**. They wrap the source and pull elements through it with a `Spliterator`. They're **lazy**: nothing runs until a terminal operation is called. Elements flow **vertically**, one element through the whole pipeline at a time, which makes **short-circuiting** possible (`findFirst` stops early).
- A stream can be consumed **only once**. After the terminal operation it's "dead".
- `Stream.iterate(seed, next)` and `Stream.generate(supplier)` create **infinite streams**, which you cut off with `limit`. `IntStream`, `LongStream` and `DoubleStream` avoid boxing and add numeric methods. Convert with `mapToInt`, `boxed`, `mapToLong`, `mapToDouble`.

## Key Concepts

**Imperative vs declarative.** Imperative code describes the mechanics (for loop, if, `add`, `Collections.sort`). Declarative code lists the operations you want and lets the library decide how to run them, the way an SQL query does (`SELECT * FROM table`). *Why it matters:* the stream version reads like the requirement itself.

**Stream.** A view over a data source that you process through a chain of operations, like water flowing through a pipe with filters. `java.util.stream.Stream<T>` is an **interface**. *Why it matters:* it's the main API for functional-style data processing in Java 8+.

**Source.** Where elements come from: `collection.stream()` / `parallelStream()` (declared on `Collection`), `Arrays.stream(arr)`, `Stream.of(...)`, `Stream.empty()`, `Stream.iterate`, `Stream.generate`. *Why it matters:* you can stream almost anything.

**Intermediate operation.** A transformation that returns a new `Stream`, so you can keep chaining. `filter` takes a `Predicate` and `map` takes a `Function`. *Why it matters:* they build the pipeline step by step but do no work on their own.

**Terminal operation.** The final trigger that consumes the stream and produces a result (a `List`, a count, an element) or a side effect (`forEach`). It does **not** return a `Stream`. *Why it matters:* no terminal operation means nothing runs.

**Lazy evaluation and short-circuiting.** Intermediate operations are only recorded. When the terminal operation runs, elements are pulled through one at a time, and operations like `findFirst`, `anyMatch` or `limit` can stop as soon as they have their answer, like a `break` in a loop. *Why it matters:* it saves work and makes infinite streams usable.

**Vertical vs horizontal processing.** Vertical: element 1 goes through filter → map → collect, then element 2, and so on. Horizontal would be "filter everything, then map everything". Streams process vertically. *Why it matters:* it explains the output order of `peek` and logging, and why short-circuiting works.

**Primitive streams.** `IntStream`, `LongStream` and `DoubleStream` work on raw `int`/`long`/`double` (no boxing) and add `sum`, `average`, `max`, `min`, `range`, and so on. *Why it matters:* they're faster and more convenient for numeric work.

## Detailed Notes

### The problem: imperative data processing [▶ ~00:00](https://youtu.be/-jHyDEXHr_U?t=0)
- Typical task: from a list of `Employee` objects, keep those with salary > 50,000, take their names, and sort them.
- Traditional code [▶ ~02:11](https://youtu.be/-jHyDEXHr_U?t=131): create `List<String> result = new ArrayList<>()`, loop `for (Employee e : employees)`, `if (e.getSalary() > 50000) result.add(e.getName())`, then `Collections.sort(result)`.
- Nothing is *wrong* with it, but it's **imperative** [▶ ~03:17](https://youtu.be/-jHyDEXHr_U?t=197): it spells out *how* (loop, condition, add, sort) instead of *what* you want. With more loops chained together it gets hard to read.

### The declarative goal (SQL analogy) [▶ ~04:23](https://youtu.be/-jHyDEXHr_U?t=263)
- Functional programming wants code that just lists the operations, like an SQL query where you say `SELECT … FROM …` and don't care whether the database uses loops or indexes internally.
- That's why Java 8 brought lambdas and then **Streams**, which are designed to work with lambdas.

### The same task with streams [▶ ~06:35](https://youtu.be/-jHyDEXHr_U?t=395)
```text
List<String> result = employees.stream()
        .filter(e -> e.getSalary() > 50000)
        .map(Employee::getName)
        .sorted()
        .toList();
```
- `stream()` (and `parallelStream()`) are methods on the **`Collection` interface** (we skipped them in the Collection video). So every `List`, `Set` and `Queue` (`ArrayList`, `LinkedList`, `ArrayDeque`, …) can be streamed.
- *(The video writes the filter as `x -> x > 50000`. It has to compare the salary, `e.getSalary() > 50000`, because `e` is an `Employee`.)*
- Convention: put each operation on its own line [▶ ~09:52](https://youtu.be/-jHyDEXHr_U?t=592). It's still a single expression, just easier to read.

### Definition and the water-pipeline picture [▶ ~10:58](https://youtu.be/-jHyDEXHr_U?t=658)
- "A stream is a tool for processing a sequence of data through a chain of operations."
- The analogy [▶ ~12:04](https://youtu.be/-jHyDEXHr_U?t=724): water comes from a **source**, passes through filters F1, F2, F3 (like an RO purifier), and what's left at the end is the result.
- Example [▶ ~13:10](https://youtu.be/-jHyDEXHr_U?t=790): `numbers.stream().filter(x -> x > 10).filter(x -> x % 2 == 0).map(x -> x * x).forEach(System.out::println)` keeps the numbers > 10, then the even ones, squares them and prints them. No loop, no `if`.

### Streams process data; they don't store it [▶ ~15:21](https://youtu.be/-jHyDEXHr_U?t=921)
- An `ArrayList` is a **collection**: it actually stores elements in heap memory.
- `numbers.stream()` does **not** copy the data into a new collection. The stream is a **wrapper** over the existing source, and the operations run against that source.
- Internally, the stream uses a **`Spliterator`** [▶ ~18:39](https://youtu.be/-jHyDEXHr_U?t=1119) (a "splittable iterator", covered later) to walk the source and feed elements into the pipeline one by one. You never write the iteration yourself.

### Pipeline architecture: source → intermediate → terminal [▶ ~19:45](https://youtu.be/-jHyDEXHr_U?t=1185)
- For `list.stream().filter(x -> x > 10).map(x -> x * 2).toList()`:
  - **Source** [▶ ~21:57](https://youtu.be/-jHyDEXHr_U?t=1317): `list.stream()` attaches the pipeline to the list.
  - **Intermediate operations**: `filter` and `map`, the "filters in the pipe".
  - **Terminal operation** [▶ ~23:02](https://youtu.be/-jHyDEXHr_U?t=1382): `toList()`. It decides what happens at the end (collect, print, count, …).
- Without a terminal operation **nothing runs**, not even the filter [▶ ~23:02](https://youtu.be/-jHyDEXHr_U?t=1382).

### Sources [▶ ~24:08](https://youtu.be/-jHyDEXHr_U?t=1448)
- Any `Collection` (list, queue, set), arrays, the static `Stream.of(...)`, and infinite streams.

### Intermediate operations ("transformations") [▶ ~26:20](https://youtu.be/-jHyDEXHr_U?t=1580)
- `filter(Predicate)` keeps elements that pass the test. `map(Function)` transforms each element. `sorted()`, `distinct()`, `limit(n)`, `skip(n)`: details in the next video.
- `list.stream()` returns a `Stream<Integer>` that you can store: `Stream<Integer> s = list.stream();` [▶ ~27:26](https://youtu.be/-jHyDEXHr_U?t=1646).
- **Every intermediate operation returns a `Stream`** [▶ ~28:32](https://youtu.be/-jHyDEXHr_U?t=1712). That's what makes `.filter(...).map(...)` chaining possible: each call builds the pipeline one more step.

### Terminal operations [▶ ~29:38](https://youtu.be/-jHyDEXHr_U?t=1778)
- They trigger the pipeline and **don't return a `Stream`**. They produce a collection (`toList()`, `collect(...)`), a value (`count()` returns a number, `findFirst()`), or a side effect (`forEach(Consumer)`).
- Key line: **no terminal operation → no stream execution** [▶ ~31:49](https://youtu.be/-jHyDEXHr_U?t=1909).

### Walkthrough: vertical processing [▶ ~32:55](https://youtu.be/-jHyDEXHr_U?t=1975)
- List `[5, 12, 7, 20]`, pipeline `filter(x > 10) → map(x * 2) → toList()`:
  - 5 fails the filter and is dropped right away.
  - 12 passes, is mapped to 24, and is added to the result.
  - 7 is dropped.
  - 20 passes, is mapped to 40, and is added. Result: `[24, 40]`.
- Each element goes through **all** stages before the next one starts. That's **vertical** processing. Streams never do "all elements through stage 1, then stage 2" (horizontal) [▶ ~35:07](https://youtu.be/-jHyDEXHr_U?t=2107).
- *(Stateful operations such as `sorted()` are the exception: they have to see every element before passing any downstream, so they act as a barrier in the pipeline.)*

### Lazy evaluation and short-circuiting [▶ ~36:13](https://youtu.be/-jHyDEXHr_U?t=2173)
- Streams are **lazy by design**: the pipeline is only a description until a terminal operation is called.
- Benefit: **short-circuiting**, the same idea as `&&`/`||`. Replace `toList()` with `findFirst()`: 5 is dropped, 12 becomes 24 and is returned, and **7 and 20 are never processed**. It works like `break` in a loop.

### A consumed stream is dead [▶ ~38:24](https://youtu.be/-jHyDEXHr_U?t=2304)
- After a terminal operation the stream is consumed. Calling another terminal operation on the same `Stream` object fails (it throws `IllegalStateException`). Create a new stream from the source instead.
- You can build the pipeline step by step with a variable: `s = s.filter(...); s = s.map(...); s.forEach(...)`. That works because each step returns a new stream.

### Code demo [▶ ~39:30](https://youtu.be/-jHyDEXHr_U?t=2370)
- `List<Integer> list = new ArrayList<>(List.of(5, 12, 7, 14));`, `Stream<Integer> s = list.stream();` (needs `import java.util.stream.Stream;`).
- `Stream` is an interface in `java.util.stream` that extends `BaseStream`, with methods like `filter`, `map`, `mapToInt`, `mapToLong`, …
- `filter` takes a **Predicate**, `map` takes a **Function**, `forEach` takes a **Consumer** and returns `void`.
- Output: `24` and `28`. *(The video briefly says 48, but 14 × 2 = 28.)* The single-chain version prints the same thing.

### Creating streams: all the sources [▶ ~43:54](https://youtu.be/-jHyDEXHr_U?t=2634)
- **From a collection**: `list.stream()`. This is the most common way. There's also `list.parallelStream()` [▶ ~44:59](https://youtu.be/-jHyDEXHr_U?t=2699). Normal streams are **sequential**, while parallel streams can process elements on multiple threads (covered in #42).
- **From an array** [▶ ~46:05](https://youtu.be/-jHyDEXHr_U?t=2765): `Arrays.stream(arr)`. An array has no `stream()` method, so you use the `Arrays` helper. A `String[]` gives you a `Stream<String>`.
- **`Stream.of(1, 2, 5, 7, 9)`** [▶ ~47:11](https://youtu.be/-jHyDEXHr_U?t=2831) creates a stream directly from values.
- **`Stream.empty()`** gives an empty stream. It's useful for returning "nothing" without using `null`, like empty lists and maps.

### Infinite streams: `iterate` and `generate` [▶ ~47:11](https://youtu.be/-jHyDEXHr_U?t=2831)
- **`Stream.iterate(seed, next)`** [▶ ~48:17](https://youtu.be/-jHyDEXHr_U?t=2897): starts at `seed` and applies `next` to the **previous value** each time. `Stream.iterate(1, x -> x + 1)` gives 1, 2, 3, … forever.
- Stop it with **`limit(n)`** [▶ ~49:23](https://youtu.be/-jHyDEXHr_U?t=2963), an intermediate operation: `Stream.iterate(1, x -> x + 1).limit(10).forEach(System.out::println)` prints 1–10.
- **`Stream.generate(supplier)`** [▶ ~49:23](https://youtu.be/-jHyDEXHr_U?t=2963): each element comes from a `Supplier`, with **no dependence on the previous value**. `Stream.generate(Math::random).limit(10)` gives 10 random doubles (`Stream<Double>`).
- Why have infinite streams at all [▶ ~51:35](https://youtu.be/-jHyDEXHr_U?t=3095)? For the same reason infinite loops exist: you use them **strategically** when you don't know in advance how much data there will be (dynamic data, e.g. a backend receiving requests), and you stop based on a condition or a limit.
- Infinite streams only work **because streams are lazy** [▶ ~53:46](https://youtu.be/-jHyDEXHr_U?t=3226). If `iterate` started producing values immediately, it would never finish.

### Primitive streams [▶ ~54:52](https://youtu.be/-jHyDEXHr_U?t=3292)
- `list.stream().filter(x -> x > 10).map(x -> x * 2).toList()` on `Integer`s unboxes for every comparison and calculation, then boxes again for the list. That's slow for large data.
- So Java provides **`IntStream`, `LongStream`, `DoubleStream`**. As with the primitive functional interfaces, there are none for `char`/`short`/`byte`/`float`, because `int`, `long` and `double` cover what we actually use [▶ ~57:04](https://youtu.be/-jHyDEXHr_U?t=3424).
- `IntStream s = IntStream.of(1, 2, 7);`. Primitive streams also add **type-specific operations** like `max`, `min`, `sum`, `average`, `count`.

### Hierarchy [▶ ~58:10](https://youtu.be/-jHyDEXHr_U?t=3490)
- `BaseStream` sits at the top, with `Stream<T>`, `IntStream`, `LongStream`, `DoubleStream` below it.
- *(Correction: the video draws the three primitive streams under `Stream`. In the JDK they extend **`BaseStream` directly**, alongside `Stream<T>`. An `IntStream` is **not** a `Stream<Integer>`.)*

### Converting between stream types [▶ ~60:21](https://youtu.be/-jHyDEXHr_U?t=3621)
- Object → primitive: `Stream<Integer> s = list.stream(); IntStream is = s.mapToInt(x -> x);`
- Primitive → object: `is.boxed()` gives a `Stream<Integer>`.
- Primitive → primitive: `intStream.mapToLong(x -> x)` gives a `LongStream`, `mapToDouble(...)` a `DoubleStream`. You can transform at the same time, e.g. `mapToLong(x -> (long) x * x)`. *(There are also the shortcuts `asLongStream()`/`asDoubleStream()`.)*
- Next video: every intermediate and terminal operation in detail.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Imperative vs declarative (the employee example)
```java
import java.util.*;

public class EmployeeStreamDemo {
    record Employee(int id, String name, double salary) {}

    public static void main(String[] args) {
        List<Employee> employees = List.of(
                new Employee(1, "Rohit", 65000),
                new Employee(2, "Aditya", 45000),
                new Employee(3, "Anjali", 80000),
                new Employee(4, "Karan", 52000));

        // imperative: HOW to do it
        List<String> result = new ArrayList<>();
        for (Employee e : employees) {
            if (e.salary() > 50000) result.add(e.name());
        }
        Collections.sort(result);
        System.out.println(result);   // [Anjali, Karan, Rohit]

        // declarative: WHAT to do
        List<String> result2 = employees.stream()
                .filter(e -> e.salary() > 50000)
                .map(Employee::name)
                .sorted()
                .toList();
        System.out.println(result2);  // [Anjali, Karan, Rohit]
    }
}
```

### Source → intermediate → terminal, laziness and vertical flow
```java
import java.util.*;
import java.util.stream.*;

public class PipelineFlowDemo {
    public static void main(String[] args) {
        List<Integer> list = List.of(5, 12, 7, 20);

        Stream<Integer> pipeline = list.stream()
                .filter(x -> { System.out.println("filter " + x); return x > 10; })
                .map(x -> { System.out.println("map " + x); return x * 2; });
        System.out.println("nothing has run yet"); // lazy: no filter/map output above

        System.out.println(pipeline.toList());
        // nothing has run yet
        // filter 5
        // filter 12
        // map 12
        // filter 7
        // filter 20
        // map 20
        // [24, 40]

        // short-circuiting: 7 and 20 are never looked at
        Optional<Integer> first = list.stream()
                .filter(x -> { System.out.println("check " + x); return x > 10; })
                .map(x -> x * 2)
                .findFirst();
        System.out.println(first.get());
        // check 5
        // check 12
        // 24
    }
}
```

### A stream can be consumed only once
```java
import java.util.*;
import java.util.stream.*;

public class StreamOnceDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(5, 12, 7, 14));
        Stream<Integer> s = list.stream();
        s = s.filter(x -> x > 10);   // each step returns a new Stream
        s = s.map(x -> x * 2);
        s.forEach(System.out::println); // 24
                                        // 28
        try {
            s.forEach(System.out::println);  // second terminal op on the same stream
        } catch (IllegalStateException e) {
            System.out.println("IllegalStateException: stream already consumed");
        }
        // IllegalStateException: stream already consumed

        System.out.println(list.stream().count()); // 4 — a fresh stream works fine
    }
}
```

### All the ways to create a stream
```java
import java.util.*;
import java.util.stream.*;

public class StreamSourcesDemo {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 2, 5, 10);
        System.out.println(list.stream().map(x -> x * 10).toList()); // [10, 20, 50, 100]

        String[] arr = {"Aditya", "Rohit", "Anjali"};
        Stream<String> fromArray = Arrays.stream(arr);
        System.out.println(fromArray.filter(n -> n.startsWith("A")).toList()); // [Aditya, Anjali]

        Stream<Integer> fromValues = Stream.of(1, 2, 5, 7, 9);
        System.out.println(fromValues.count());                  // 5

        System.out.println(Stream.empty().count());              // 0

        // infinite: iterate (depends on previous value) + limit
        System.out.println(Stream.iterate(1, x -> x + 1).limit(10).toList());
        // [1, 2, 3, 4, 5, 6, 7, 8, 9, 10]

        // infinite: generate (no dependence on previous value) + limit
        Stream<Double> randoms = Stream.generate(Math::random).limit(3);
        System.out.println(randoms.allMatch(d -> d >= 0 && d < 1)); // true (values themselves vary)

        Stream.generate(() -> "hi").limit(2).forEach(System.out::println); // hi
                                                                          // hi
    }
}
```

### Primitive streams and conversions
```java
import java.util.*;
import java.util.stream.*;

public class PrimitiveStreamConvertDemo {
    public static void main(String[] args) {
        IntStream s = IntStream.of(1, 2, 7);
        System.out.println(s.sum());                              // 10

        System.out.println(IntStream.of(4, 9, 2).max().getAsInt()); // 9
        System.out.println(IntStream.of(4, 9, 2).average().getAsDouble()); // 5.0

        List<Integer> list = List.of(3, 4, 5);
        IntStream ints = list.stream().mapToInt(x -> x);         // object -> primitive
        System.out.println(ints.map(x -> x * x).sum());           // 50

        Stream<Integer> boxed = IntStream.of(1, 2, 3).boxed();    // primitive -> object
        System.out.println(boxed.toList());                       // [1, 2, 3]

        LongStream longs = IntStream.of(1, 2, 3).mapToLong(x -> (long) x * 1_000_000_000L);
        System.out.println(longs.max().getAsLong());              // 3000000000

        DoubleStream doubles = IntStream.of(1, 2).mapToDouble(x -> x / 2.0);
        System.out.println(Arrays.toString(doubles.toArray()));   // [0.5, 1.0]
    }
}
```

## Common Pitfalls / Tips
- **Forgetting the terminal operation.** `list.stream().filter(...).map(...);` compiles but does nothing.
- **Reusing a stream.** A second terminal operation throws `IllegalStateException`. Call `collection.stream()` again instead.
- **A stream isn't a collection.** It holds no data. Collect it (`toList()`, `collect(...)`) if you need to keep the results.
- **`toList()` (Java 16+) returns an unmodifiable list.** Use `collect(Collectors.toList())` or `new ArrayList<>(...)` if you need to modify it.
- **Infinite streams need a short-circuit.** `Stream.iterate(1, x -> x + 1).forEach(...)` never ends. Add `limit`, or use `takeWhile` / the 3-argument `iterate(seed, hasNext, next)` from Java 9.
- **Side effects inside `filter`/`map`** (printing, modifying outside state) make code hard to reason about. Use them only for debugging, and prefer `peek` for that.
- **`IntStream` is not a `Stream<Integer>`.** Use `boxed()` to convert. `Arrays.stream(int[])` gives an `IntStream`, while `Stream.of(int[])` gives a `Stream<int[]>` with a single element.
- **Write one operation per line** so long pipelines stay readable.

## Review Questions
1. Why is the loop + if + `Collections.sort` version called imperative, and what makes the stream version declarative?
2. What are the three parts of a stream pipeline? Give two examples of each.
3. Why can intermediate operations be chained, and what do terminal operations return instead?
4. What happens if you write a pipeline without a terminal operation? What is lazy evaluation?
5. Explain vertical processing using `[5, 12, 7, 20]` with `filter(x > 10).map(x * 2).findFirst()`. Which elements are never touched?
6. What happens when you call `forEach` twice on the same `Stream` object?
7. What is the difference between `Stream.iterate` and `Stream.generate`, and why do infinite streams only make sense because streams are lazy?
8. Why do `IntStream`/`LongStream`/`DoubleStream` exist, and how do you convert between `Stream<Integer>` and `IntStream`?

<details>
<summary>Answers</summary>

1. It describes the mechanics of the work (how to loop, when to add, how to sort). The stream version just names the operations (filter, map, sorted, toList) and leaves the iteration to the library, the way an SQL query does.
2. Source: `list.stream()`, `Arrays.stream(arr)`, `Stream.of(...)`, `Stream.iterate(...)`. Intermediate: `filter`, `map`, `sorted`, `distinct`, `limit`, `skip`. Terminal: `forEach`, `toList`, `collect`, `count`, `findFirst`.
3. Every intermediate operation returns a new `Stream`, so you can call another stream method on it. Terminal operations return a non-stream result (a list, a number, an `Optional`) or `void` (`forEach`).
4. Nothing executes: no element passes through the filters. Lazy evaluation means intermediate operations only describe the pipeline, and the work starts when (and only when) a terminal operation is called.
5. 5 fails the filter. 12 passes, becomes 24, and `findFirst` returns it immediately. 7 and 20 are never processed. Each element goes through the whole pipeline before the next one starts.
6. The second call throws `IllegalStateException`, because the stream was already consumed by the first terminal operation.
7. `iterate(seed, f)` computes each element from the previous one (seed, f(seed), f(f(seed)), …). `generate(supplier)` produces independent values (e.g. `Math::random`). Because of laziness, elements are only produced when a terminal operation asks for them, and `limit`/`findFirst` stops asking. Without laziness, `iterate` would loop forever as soon as it was called.
8. To avoid boxing and unboxing on every element, and to provide numeric operations (`sum`, `average`, `max`, `range`). Use `stream.mapToInt(x -> x)` for `Stream<Integer>` → `IntStream`, and `intStream.boxed()` for the reverse.
</details>

## Further Study
- All intermediate and terminal operations (next video): `distinct`, `sorted(Comparator)`, `peek`, `flatMap`, `reduce`, `collect`, `anyMatch`, …
- Stateless vs stateful intermediate operations
- `Spliterator` and parallel streams (#42)
- `Collectors`: `toList`, `toSet`, `toMap`, `groupingBy`, `joining`
- Java 9+ stream additions: `takeWhile`, `dropWhile`, `iterate(seed, hasNext, next)`, `Stream.ofNullable`
- `IntStream.range` / `rangeClosed`, `summaryStatistics()`

---

[← #38](38-lambdas-functional-interfaces-method-references.md) · [Index](ReadMe.md) · [#40 →](40-stream-intermediate-terminal-operations.md)
