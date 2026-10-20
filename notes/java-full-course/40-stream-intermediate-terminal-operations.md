# Java Stream Methods Deep Dive | All Intermediate & Terminal Operations | Java Full Course #40
> Source: https://youtu.be/xqfOH5EA8xo · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 60:12. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- **Intermediate operations** return a `Stream`: `filter` (Predicate), `map` (Function), `flatMap` (flattens nested structures), `sorted` / `sorted(Comparator)`, `distinct`, `limit`, `skip`, `peek` (for debugging), plus `mapToInt`/`mapToDouble`/`mapToLong`.
- `filter` and `map` are **stateless**: each element is handled on its own. `sorted` and `distinct` are **stateful**: they need to see all (or previous) elements, so they act as a barrier in the vertical flow.
- **Terminal operations** fall into four groups. **Collecting**: `toList()` (unmodifiable), `collect(...)`. **Reducing**: `reduce`, `count`, plus `sum`/`max`/`min`/`average` on primitive streams. **Searching/matching**: `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch` (short-circuiting). **Iterating**: `forEach`, `forEachOrdered`.
- `collect` takes a **`Collector`** (interface). The **`Collectors`** utility class creates them: `toList`, `toSet`, `toMap`, `groupingBy` (optionally with a downstream such as `mapping(..., toList())`), `partitioningBy` (true/false groups), `joining`.
- Several terminal operations return **`Optional`** / `OptionalInt` / `OptionalDouble` (`reduce` without identity, `findFirst`, `max`, `average`) so that an empty result never means `null`.

## Key Concepts

**`flatMap`.** `map` turns each element into exactly one value, so mapping a `List<List<Integer>>` with `x -> x.stream()` gives a `Stream<Stream<Integer>>`, and printing that shows `java.util.stream.ReferencePipeline$Head@…`. `flatMap` takes a function that returns a *stream* and **merges** all those streams into one flat stream. *Why it matters:* it's the standard way to handle nested collections (lists of lists, orders → items, …).

**Stateless vs stateful operations.** Stateless ones (`filter`, `map`, `peek`) decide each element on its own. Stateful ones (`sorted`, `distinct`, and in a way `limit`/`skip`) have to remember earlier elements, or wait for all of them, before they can emit anything. *Why it matters:* stateful operations cost memory and limit the benefit of parallel streams (#42).

**`peek`.** An intermediate operation that runs a `Consumer` on each element as it passes and returns the same stream. *Why it matters:* a normal debugger steps over a whole pipeline in one go, and `peek` lets you see what's flowing between stages. It's meant for debugging, not for production logic.

**`reduce`.** It folds all elements into one value using a `BinaryOperator` (a `BiFunction<T,T,T>`). `reduce(op)` returns an `Optional<T>`, while `reduce(identity, op)` returns `T` and starts from `identity`. *Why it matters:* sum, product, max and string concatenation are all reductions.

**Short-circuiting terminal operations.** `findFirst`, `findAny`, `anyMatch`, `allMatch` and `noneMatch` stop as soon as the answer is known. *Why it matters:* with lazy, vertical processing, they can skip most of the stream.

**`Collector` vs `Collectors`.** `Collector<T,A,R>` is the interface describing *how* to accumulate elements. `Collectors` is the utility class of ready-made collectors, just as `Collection` vs `Collections`. *Why it matters:* `collect` is the most powerful and flexible terminal operation.

**`groupingBy` / `partitioningBy`.** `groupingBy(classifier)` builds a `Map<K, List<T>>` with any number of keys. `partitioningBy(predicate)` builds a `Map<Boolean, List<T>>` with exactly two keys, `false` and `true`. *Why it matters:* this is the stream version of SQL `GROUP BY`. Employees by department is the classic example.

## Detailed Notes

### Recap [00:00]
- A stream is a pipeline: a **source** (`list.stream()`), **intermediate operations** (`filter(x -> x > 10)`, `map(x -> x * 2)`), and a **terminal** operation (`toList()`).
- You don't need to memorize every method. Type `.` after a stream in the IDE to see them. Knowing roughly what each one does is enough.

### Intermediate operations [03:08]
- Setup: `List<Integer> list = new ArrayList<>(List.of(1, 4, 11, 13, 34));`, ending with `forEach(System.out::println)` so the result is printed. Put each operation on a new line.
- **`filter(Predicate)`** [04:11]: `filter(x -> x > 10)` keeps 11, 13, 34. Chain `filter(x -> x % 2 == 0)` and only 34 is left. `filter` returns a `Stream`. `forEach` takes a `Consumer` and returns `void`.
- **`map(Function)`** [06:16]: `map(x -> x * 2)` after the first filter prints 22, 26, 68. `map` transforms T → R.

### `flatMap`: flattening nested lists [07:19]
- `List<List<Integer>> list2 = List.of(List.of(1, 2), List.of(3, 4));`
- `list2.stream().map(x -> x * 2)` **doesn't compile** ("cannot infer type arguments"), because each element `x` is a whole `List`, not a number.
- `list2.stream().map(x -> x.stream())…forEach(System.out::println)` compiles but prints two lines like `java.util.stream.ReferencePipeline$Head@…`. `map` **keeps the nested structure**, so you get a stream *of streams* [10:28].
- **`flatMap(x -> x.stream())`** [11:31] flattens everything into a single level: 1, 2, 3, 4. Add `.map(x -> x * 2)` after it and you get 2, 4, 6, 8.

### `sorted`: a stateful operation [12:33]
- With the list rearranged to `[11, 34, 1, 13, 4]`: `filter(x > 10) → map(x * 2) → sorted()` prints 22, 26, 68.
- **Stateful** [13:36]: 22 (from 11) gets through the filter and the map, then **waits** at `sorted`. So do 68 and 26. Only after every element has arrived does `sorted` sort them and pass them on to `forEach`. `filter` and `map` are **stateless**: they don't care what they've seen before.
- **Descending order** [14:39]: `sorted` is overloaded with a `Comparator` version, e.g. `sorted((a, b) -> b - a)`. *(Tip: prefer `Comparator.reverseOrder()`. `b - a` can overflow for very large or very negative ints.)*

### `distinct` [15:42]
- Keeps only unique values. Internally it tracks the elements it has already seen, using hashing (set-like behavior, based on `equals`/`hashCode`).
- It's **stateful** too: it has to remember the earlier elements.
- With duplicates added (13 and 34 twice), `… sorted(desc).distinct()` prints 68, 26, 22 only once each [16:45].
- Compare that with writing the same logic by hand: if-conditions, a sort, a `Set` for uniqueness, then a loop to print.

### `limit` and `skip` [17:48]
- `Stream.iterate(1, x -> x + 1).forEach(...)` never stops. **`limit(10)`** caps it at the first 10 elements (1..10). `limit` sets a **maximum size**.
- **`skip(5)`** [18:50] drops the first 5 elements: `iterate(1, x -> x + 1).limit(10).skip(5)` prints 6..10.
- `skip` works on any stream, e.g. `skip(1)` at the end of the distinct pipeline drops 68 and leaves 26, 22 [19:53].

### `peek`: looking inside the pipeline [19:53]
- `… map(x -> x * 2).peek(System.out::println).sorted().distinct().forEach(System.out::println)` first prints 22, 68, 26 (the order before sorting, from `peek`), then 22, 26, 68 (sorted, from `forEach`) [21:59].
- Why it exists: a debugger treats the stream pipeline as one statement and can't easily show what's in between. `peek` shows the state after a particular stage. Use it for debugging only [23:02].
- `mapToInt` / `mapToDouble` / `mapToLong` (from the last video) convert to primitive streams to avoid boxing.

### Terminal operations: the four groups [23:02]
- Streams are lazy, so the terminal operation is the **trigger**.
- **Collecting results**: `toList()`, `collect(...)`.
- **Reducing results** [24:05]: `reduce`, and the special reductions `sum`, `max`, `min`, `average`, `count`. Most of the special ones exist only on primitive streams, so convert first.
- **Searching / matching** [25:07]: `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch`. The `…Match` methods return `boolean`.
- **Iterating** [25:07]: `forEach`, and `forEachOrdered` [26:10], which keeps the encounter order even in **parallel** streams, where several threads can process elements at the same time.

### `forEach`, `forEachOrdered`, `toList` [27:13]
- With `List.of(1, 13, 11, 9)`: `map(x -> x + 1).forEach(System.out::println)` prints 2, 14, 12, 10. `forEachOrdered` prints the same thing on a sequential stream. The difference only shows with parallel streams.
- **`toList()`** [28:16] returns a `List<T>`. It's **unmodifiable**: `list2.add(4)` compiles but throws `UnsupportedOperationException` at runtime [29:19]. *(The video calls it an "unsorted" operation exception.)*

### `collect` + `Collectors` [30:22]
- `collect(Collector)` can gather results into many structures in many ways. `Collector` is the interface and `Collectors` is the utility class (compare `Collection` vs `Collections`).
- **`collect(Collectors.toList())`** [31:24] gives a list you **can** modify: `add(4)` works. *(Strictly speaking the Javadoc doesn't guarantee mutability, though in practice you get an `ArrayList`. If you need a mutable list, `Collectors.toCollection(ArrayList::new)` makes that explicit.)*
- **`Collectors.toSet()`** collects into a `Set`. `toMap` and the other collectors are covered below.

### `reduce` [32:27]
- It combines stream elements into **one value**. It takes a `BinaryOperator<T>` (which extends `BiFunction<T,T,T>`), e.g. `(a, b) -> a + b`.
- For `[1, 13, 11, 9]`: 1 + 13 = 14, 14 + 11 = 25, 25 + 9 = 34.
- `reduce(op)` returns **`Optional<Integer>`** [33:30]. Read it with `sum.get()`; `Optional` protects you from `null` when the stream is empty (it gets its own video next).
- `reduce(identity, op)` [34:33] returns a plain value: `reduce(0, (a, b) -> a + b)` → 34. The **identity** is the starting value. It's also what you get back for an empty stream.
  - With identity 1 for a sum you'd get 35. For a product use identity 1 (identity 0 makes everything 0): `reduce(1, (a, b) -> a * b)` → 1287.
  - *(Strictly, the identity must be a true neutral element, 0 for `+` and 1 for `*`. Using 1 as the "start" of a sum gives a wrong result on parallel streams, because the identity may be applied once per chunk.)*

### `count` [36:39]
- `filter(x -> x > 10).count()` → 2. `count` returns **`long`**, so assigning it to an `int` doesn't compile.

### `findFirst` and `findAny` [37:41]
- `filter(x -> x > 10).findFirst()` returns `Optional<Integer>` → 13. It **short-circuits**: once it has 13, nothing else is processed.
- `findAny` [38:44] also returns any matching element and stops. On a sequential stream it usually gives the same as `findFirst` (13). On a **parallel** stream it can return whichever match a thread finds first, which is faster because it doesn't have to respect order.

### `anyMatch`, `allMatch`, `noneMatch` [39:47]
- They take a `Predicate` and return `boolean`.
- After `filter(x > 10)` (13, 11 are left): `anyMatch(x -> x % 2 == 0)` → false, since both are odd. Change 13 to 12 and it's true.
- `allMatch` [40:50] is true only if **every** element matches: false with 13 in the list, true if 12 and 14 are the ones left.
- `noneMatch` [41:53] is true only if **no** element matches: false if any even number gets through, true with 13 and 11.

### `sum`, `max`, `average`: primitive streams only [41:53]
- `Stream<Integer>.sum()` → compile error "The method sum() is undefined for the type Stream". Convert first: **`mapToInt(x -> x)`** returns an `IntStream` and takes a `ToIntFunction` [42:56].
- `filter(x > 10).mapToInt(x -> x).sum()` → 24 (13 + 11) [43:58].
- `max()` returns **`OptionalInt`**. Read it with `getAsInt()` → 13.
- `average()` returns **`OptionalDouble`**. Read it with `getAsDouble()` → 12.0 [45:01].

### `Collector` in depth: the basic collections [45:01]
- `collect` expects a `Collector`, which is a **strategy** for gathering the elements (as a list, set or map, grouped, joined, …). The `Collectors` utility class lives in `java.util.stream` [46:04].
- **`toList`, `toSet`** [47:07]: `map(x -> x + 1).collect(Collectors.toList())` → `[2, 14, 12, 10]`. `toSet()` gives the same values in a `Set`, in no guaranteed order.
- **`toMap(keyMapper, valueMapper)`** [48:10]: both arguments are `Function`s. With `List.of("AA", "BBB", "CCCC")`, `collect(Collectors.toMap(x -> x.length(), x -> x))` → `{2=AA, 3=BBB, 4=CCCC}` [51:18].
  - *(Watch out: if two elements produce the same key, the 2-argument `toMap` throws `IllegalStateException: Duplicate key`. Pass a third merge-function argument, or use `groupingBy`.)*

### `groupingBy` [51:18]
- It groups elements by a key. With `["AA", "BBB", "CCCC", "DD", "EEE"]`, `collect(Collectors.groupingBy(x -> x.length()))` → `{2=[AA, DD], 3=[BBB, EEE], 4=[CCCC]}`.
- The values are lists, so the type is **`Map<Integer, List<String>>`**, not `Map<Integer, String>` [52:21].
- Real-world use [53:24]: `employees.stream().collect(Collectors.groupingBy(Employee::getDepartment))` gives HR → [...], Tech → [...].

### `partitioningBy` [54:27]
- A special case of grouping with **only two groups**, `true` and `false`. It takes a `Predicate`.
- `List.of(1, 13, 11, 9)` with `partitioningBy(x -> x % 2 == 0)` → `{false=[1, 13, 11, 9], true=[]}`. Change 13 to 14 and you get `{false=[1, 11, 9], true=[14]}`. The type is `Map<Boolean, List<Integer>>`.

### `groupingBy` with a downstream `mapping` collector [55:30]
- The overload `groupingBy(classifier, downstream)` controls what goes into each group.
- `groupingBy(x -> x.length(), Collectors.mapping(x -> x.toLowerCase(), Collectors.toList()))` → `{2=[aa, dd], 3=[bbb, eee], 4=[cccc]}` [57:35].
- `mapping(mapper, downstream)` takes a `Function` plus *another collector*, which is where the name "downstream" comes from.

### `joining` [58:38]
- `collect(Collectors.joining("-"))` concatenates the strings with a delimiter → `"AA-BBB-CCCC-DD-EEE"`, and returns a `String`.
- Next video: the `Optional` class and parallel streams.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Intermediate operations: filter, map, sorted, distinct, skip, peek
```java
import java.util.*;
import java.util.stream.*;

public class IntermediateOpsDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(11, 34, 1, 13, 4, 13, 34));

        list.stream().filter(x -> x > 10).filter(x -> x % 2 == 0)
                .forEach(System.out::println);                 // 34
                                                               // 34
        System.out.println(list.stream()
                .filter(x -> x > 10)
                .map(x -> x * 2)
                .sorted(Comparator.reverseOrder())             // descending (stateful)
                .distinct()                                    // unique (stateful)
                .toList());                                    // [68, 26, 22]

        System.out.println(list.stream().filter(x -> x > 10).map(x -> x * 2)
                .sorted().distinct().skip(1).toList());        // [26, 68]

        List<Integer> out = list.stream()
                .filter(x -> x > 10).map(x -> x * 2).distinct()
                .peek(x -> System.out.println("before sort: " + x))
                .sorted()
                .toList();
        // before sort: 22
        // before sort: 68
        // before sort: 26
        System.out.println(out);                               // [22, 26, 68]

        System.out.println(Stream.iterate(1, x -> x + 1).limit(10).skip(5).toList());
        // [6, 7, 8, 9, 10]
    }
}
```

### `map` vs `flatMap` on a list of lists
```java
import java.util.*;
import java.util.stream.*;

public class FlatMapDemo {
    public static void main(String[] args) {
        List<List<Integer>> list2 = List.of(List.of(1, 2), List.of(3, 4));

        // map keeps the nesting: a Stream<Stream<Integer>>
        long streamsCount = list2.stream().map(x -> x.stream()).count();
        System.out.println(streamsCount);                       // 2

        // flatMap flattens to Stream<Integer>
        List<Integer> doubled = list2.stream()
                .flatMap(x -> x.stream())
                .map(x -> x * 2)
                .toList();
        System.out.println(doubled);                            // [2, 4, 6, 8]

        List<String> sentences = List.of("java streams", "are fun");
        System.out.println(sentences.stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .toList());                                     // [java, streams, are, fun]
    }
}
```

### Terminal operations: iterate, toList, reduce, count, find, match, numeric
```java
import java.util.*;
import java.util.stream.*;

public class TerminalOpsDemo {
    public static void main(String[] args) {
        List<Integer> list = List.of(1, 13, 11, 9);

        list.stream().map(x -> x + 1).forEachOrdered(x -> System.out.print(x + " ")); // 2 14 12 10
        System.out.println();

        List<Integer> immutable = list.stream().map(x -> x + 1).toList();
        try {
            immutable.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println("toList() is unmodifiable");    // toList() is unmodifiable
        }
        List<Integer> mutable = list.stream().map(x -> x + 1).collect(Collectors.toList());
        mutable.add(4);
        System.out.println(mutable);                            // [2, 14, 12, 10, 4]

        Optional<Integer> sum = list.stream().reduce((a, b) -> a + b);
        System.out.println(sum.get());                          // 34
        System.out.println(list.stream().reduce(0, (a, b) -> a + b));  // 34
        System.out.println(list.stream().reduce(1, (a, b) -> a * b));  // 1287
        System.out.println(Stream.<Integer>empty().reduce(0, Integer::sum)); // 0

        long count = list.stream().filter(x -> x > 10).count();
        System.out.println(count);                              // 2

        System.out.println(list.stream().filter(x -> x > 10).findFirst().get()); // 13
        System.out.println(list.stream().filter(x -> x > 10).findAny().isPresent()); // true

        System.out.println(list.stream().filter(x -> x > 10).anyMatch(x -> x % 2 == 0));  // false
        System.out.println(list.stream().filter(x -> x > 10).allMatch(x -> x % 2 == 1));  // true
        System.out.println(list.stream().filter(x -> x > 10).noneMatch(x -> x % 2 == 0)); // true

        // sum/max/average live on primitive streams
        System.out.println(list.stream().filter(x -> x > 10).mapToInt(x -> x).sum());                // 24
        System.out.println(list.stream().filter(x -> x > 10).mapToInt(x -> x).max().getAsInt());     // 13
        System.out.println(list.stream().filter(x -> x > 10).mapToInt(x -> x).average().getAsDouble()); // 12.0
    }
}
```

### Collectors: toSet, toMap, groupingBy, partitioningBy, mapping, joining
```java
import java.util.*;
import java.util.stream.*;

public class CollectorsTourDemo {
    record Emp(String name, String dept) {}

    public static void main(String[] args) {
        List<String> words = List.of("AA", "BBB", "CCCC", "DD", "EEE");

        Set<Integer> lengths = words.stream().map(String::length).collect(Collectors.toCollection(TreeSet::new));
        System.out.println(lengths);                                   // [2, 3, 4]

        Map<Integer, String> byLen = List.of("AA", "BBB", "CCCC").stream()
                .collect(Collectors.toMap(x -> x.length(), x -> x));
        System.out.println(new TreeMap<>(byLen));                      // {2=AA, 3=BBB, 4=CCCC}

        Map<Integer, String> merged = words.stream()                   // duplicate keys -> merge function
                .collect(Collectors.toMap(String::length, x -> x, (a, b) -> a + "|" + b, TreeMap::new));
        System.out.println(merged);                                    // {2=AA|DD, 3=BBB|EEE, 4=CCCC}

        Map<Integer, List<String>> groups = words.stream()
                .collect(Collectors.groupingBy(x -> x.length(), TreeMap::new, Collectors.toList()));
        System.out.println(groups);                                    // {2=[AA, DD], 3=[BBB, EEE], 4=[CCCC]}

        Map<Integer, List<String>> lower = words.stream()
                .collect(Collectors.groupingBy(x -> x.length(), TreeMap::new,
                        Collectors.mapping(x -> x.toLowerCase(), Collectors.toList())));
        System.out.println(lower);                                     // {2=[aa, dd], 3=[bbb, eee], 4=[cccc]}

        Map<Boolean, List<Integer>> parts = List.of(1, 14, 11, 9).stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0));
        System.out.println(parts);                                     // {false=[1, 11, 9], true=[14]}

        System.out.println(words.stream().collect(Collectors.joining("-"))); // AA-BBB-CCCC-DD-EEE

        List<Emp> emps = List.of(new Emp("Rohit", "Tech"), new Emp("Anjali", "HR"), new Emp("Karan", "Tech"));
        Map<String, List<String>> byDept = emps.stream()
                .collect(Collectors.groupingBy(Emp::dept, TreeMap::new,
                        Collectors.mapping(Emp::name, Collectors.toList())));
        System.out.println(byDept);                                    // {HR=[Anjali], Tech=[Rohit, Karan]}
    }
}
```

## Common Pitfalls / Tips
- **`map` vs `flatMap`.** If your lambda returns a stream (or a collection you then `.stream()`), you almost certainly want `flatMap`. Printing `ReferencePipeline$Head@…` is the giveaway.
- **Stateful operations buffer data.** `sorted()`/`distinct()` on a huge or infinite stream can use a lot of memory or never finish (`Stream.iterate(...).sorted()` hangs).
- **Don't put business logic in `peek`.** In newer JDKs it may not even run (e.g. `list.stream().peek(...).count()` can skip it, because the size is already known).
- **`toList()` is unmodifiable.** Use `collect(Collectors.toCollection(ArrayList::new))` if you need to `add` later.
- **`count()` returns `long`**, and `max()`/`average()` on primitive streams return `OptionalInt`/`OptionalDouble`. Read them with `getAsInt()`/`getAsDouble()`.
- **`reduce` identity must be neutral** (0 for sum, 1 for product, `""` for concatenation), or parallel results will be wrong.
- **`Collectors.toMap` throws on duplicate keys.** Supply a merge function, or use `groupingBy`.
- **`HashMap`/`HashSet` collectors have no defined order.** Use `TreeMap::new` / `TreeSet::new` / `LinkedHashMap::new` when the order matters, e.g. for printing or tests.
- **`(a, b) -> b - a` can overflow.** Use `Comparator.reverseOrder()` or `Integer.compare(b, a)`.

## Review Questions
1. What's the difference between `map` and `flatMap`? What happens if you use `map(x -> x.stream())` on a `List<List<Integer>>`?
2. Which of `filter`, `map`, `sorted` and `distinct` are stateful, and why does it matter?
3. What do `limit(10).skip(5)` produce on `Stream.iterate(1, x -> x + 1)`?
4. What is `peek` for, and why shouldn't you rely on it in production code?
5. How is `toList()` different from `collect(Collectors.toList())`?
6. What do `reduce(op)` and `reduce(identity, op)` return? What is the identity for, and what's wrong with using 1 as the identity of a sum?
7. Why doesn't `Stream<Integer>.sum()` compile, and how do you fix it? What type does `average()` return?
8. When would you use `groupingBy` vs `partitioningBy`? What does `groupingBy(String::length, mapping(String::toLowerCase, toList()))` produce for `["AA", "BBB", "DD"]`?

<details>
<summary>Answers</summary>

1. `map` turns each element into exactly one value and keeps the nesting. `flatMap` takes a function that returns a stream and merges all the resulting streams into one. `map(x -> x.stream())` gives a `Stream<Stream<Integer>>`, so `forEach(println)` prints stream objects (`ReferencePipeline$Head@…`) instead of numbers.
2. `sorted` and `distinct` are stateful. `filter` and `map` are stateless. Stateful operations have to buffer or remember elements (sorting needs all of them, and distinct needs the ones seen so far), so they break pure element-by-element flow, use memory, and reduce the benefit of parallelism.
3. `limit(10)` keeps 1..10, then `skip(5)` drops 1..5, leaving `[6, 7, 8, 9, 10]`.
4. To observe elements between stages while debugging, since a debugger can't easily step inside a pipeline. It's a side effect inside a lazy pipeline: it may run in an unexpected order, or not at all if the terminal operation can skip it.
5. `toList()` (Java 16+) returns an unmodifiable list, and `add` throws `UnsupportedOperationException`. `Collectors.toList()` in practice returns a mutable `ArrayList`, though this isn't guaranteed. Use `toCollection(ArrayList::new)` to be explicit.
6. `reduce(op)` returns `Optional<T>`, because the stream might be empty. `reduce(identity, op)` returns `T`, starts from the identity, and returns it for an empty stream. The identity must be neutral for the operation. With 1 as the identity of a sum, the result is off by one (35 instead of 34) sequentially, and off by the number of chunks in parallel.
7. `sum()` is only defined on primitive streams. Use `mapToInt(x -> x).sum()`. `average()` returns `OptionalDouble`, read with `getAsDouble()`.
8. `groupingBy` makes any number of groups, keyed by a classifier function. `partitioningBy` always makes exactly two groups, keyed `true`/`false` by a predicate. The example gives `{2=[aa, dd], 3=[bbb]}`.
</details>

## Further Study
- `Optional` in depth (next video)
- More collectors: `counting`, `summingInt`, `averagingDouble`, `maxBy`, `collectingAndThen`, `teeing` (Java 12), `toUnmodifiableList`
- Multi-level grouping: `groupingBy(a, groupingBy(b))`
- `takeWhile` / `dropWhile` (Java 9), `mapMulti` (Java 16)
- `IntSummaryStatistics` via `summaryStatistics()`
- The three-argument `reduce(identity, accumulator, combiner)` and its role in parallel streams

---

[← #39](39-intro-java-streams-creation-architecture.md) · [Index](README.md) · [#41 →](41-java-optional-explained.md)

---

[← #39](39-intro-java-streams-creation-architecture.md) · [Index](README.md) · [#41 →](41-java-optional-explained.md)
