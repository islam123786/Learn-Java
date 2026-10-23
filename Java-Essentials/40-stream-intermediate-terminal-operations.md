# 40 · Stream Operations: Intermediate, Terminal & Collectors
> Full notes: [40-stream-intermediate-terminal-operations.md](../40-stream-intermediate-terminal-operations.md) · [▶ Video](https://youtu.be/xqfOH5EA8xo)

A pipeline is **source → intermediate operations → terminal operation**. Intermediate operations return a `Stream` and are lazy. The terminal operation triggers the run and produces the result. `collect` with the `Collectors` class can build lists, sets, maps, groups and strings.

You don't need to memorize every method: type `.` after a stream and the IDE lists them. Know roughly what each one does.

---

## Intermediate operations
Each one **returns a `Stream`**, so they chain. Nothing runs until a terminal operation is called.

| Operation | Takes | Does | Stateless / stateful |
| :--- | :--- | :--- | :--- |
| `filter(p)` | `Predicate<T>` | keeps elements where `p` is true | stateless |
| `map(f)` | `Function<T,R>` | transforms each element T → R | stateless |
| `flatMap(f)` | `Function<T, Stream<R>>` | maps each element to a stream, then **flattens** them into one | stateless |
| `sorted()` / `sorted(cmp)` | nothing / `Comparator<T>` | natural order / custom order | **stateful** |
| `distinct()` | nothing | removes duplicates (`equals`/`hashCode`) | **stateful** |
| `limit(n)` | `long` | keeps at most the first `n` (short-circuits) | stateful (counts) |
| `skip(n)` | `long` | drops the first `n` | stateful (counts) |
| `peek(c)` | `Consumer<T>` | looks at each element, passes it on unchanged (debugging) | stateless |
| `mapToInt/Long/Double` | `ToIntFunction<T>`… | converts to a primitive stream | stateless |

- **Stateless:** each element is handled on its own, without knowing about the others.
- **Stateful:** the operation must remember earlier elements (`distinct`) or see **all** of them (`sorted`) before passing anything on. It acts as a **barrier** in the vertical flow.

### `filter` and `map` (recap)
```java
List<Integer> list = List.of(1, 4, 11, 13, 34);
list.stream().filter(x -> x > 10).forEach(System.out::println);           // 11 13 34
list.stream().filter(x -> x > 10).filter(x -> x % 2 == 0)
        .forEach(System.out::println);                                    // 34
list.stream().filter(x -> x > 10).map(x -> x * 2).forEach(System.out::println); // 22 26 68
```

### `flatMap`: flattening nested structures
- With `List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));` each stream element is a **whole list**, not a number.
- `nested.stream().map(x -> x * 2)` → **doesn't compile**: you can't multiply a `List`.
- `nested.stream().map(x -> x.stream())` → compiles, but gives a **stream of streams**. Printing shows `java.util.stream.ReferencePipeline$Head@…` twice. `map` keeps the nesting (one-to-one).
- **`flatMap(x -> x.stream())`** maps each list to a stream and **merges them into one level**: 1, 2, 3, 4 (one-to-many). Then `.map(x -> x * 2)` gives 2, 4, 6, 8.
```
map:      [[1,2],[3,4]]  →  [Stream(1,2), Stream(3,4)]     2 elements
flatMap:  [[1,2],[3,4]]  →  [1, 2, 3, 4]                   4 elements
```
- Common uses: list of lists, splitting sentences into words, each order's list of items.
```java
List<String> sentences = List.of("java is fun", "streams are lazy");
sentences.stream().flatMap(s -> Arrays.stream(s.split(" "))).toList();
// [java, is, fun, streams, are, lazy]
```

### `sorted`: stateful
- `sorted()` uses natural order (`Comparable`). `sorted(Comparator)` uses a custom order.
- With `[11, 34, 1, 13, 4]` → `filter(x > 10) → map(x * 2) → sorted()`: 22 passes filter and map, then **waits** at `sorted`. So do 68 and 26. Only after **all** elements arrive does `sorted` sort them and send 22, 26, 68 to `forEach`.
- Descending: `sorted(Comparator.reverseOrder())` or `sorted((a, b) -> b - a)`. Prefer `reverseOrder()`, since `b - a` can overflow for very large or very negative ints.
- Objects: `sorted(Comparator.comparing(Employee::getSalary))`.

### `distinct`: stateful
- Keeps only **unique** elements. Internally it remembers what it has seen using hashing (like a `HashSet`), so it relies on **`equals`/`hashCode`**.
- Stateful: it must remember all earlier elements. It keeps the **first occurrence** and the encounter order.
- One line replaces the manual version: if-conditions + a sort + a `Set` for uniqueness + a loop to print.

### `limit` and `skip`
- **`limit(n)`** sets a **maximum size**: keep the first `n`, then stop. It **short-circuits**, which is what makes infinite streams usable: `Stream.iterate(1, x -> x + 1).limit(10)` → 1..10.
- **`skip(n)`** **drops** the first `n`: `iterate(1, x -> x + 1).limit(10).skip(5)` → 6..10.
- Together they give **pagination**: page `p` of size `s` = `skip(p * s).limit(s)`.
- Order matters: `limit(10).skip(5)` → 6..10, but `skip(5).limit(10)` → 6..15.

### `peek`: looking inside the pipeline
- A debugger treats the whole pipeline as **one statement**, so it's hard to see what's between the stages. `peek(Consumer)` shows each element at that point and **passes it on unchanged**.
- `… map(x -> x * 2).peek(System.out::println).sorted().forEach(System.out::println)` prints **22, 68, 26** (from `peek`, before sorting), then **22, 26, 68** (from `forEach`, after sorting).
- Use it **for debugging only**, not to change data.
- Lazy like everything else: without a terminal operation, `peek` prints nothing.

### All intermediate operations in one program
```java
import java.util.*;
import java.util.stream.*;

public class IntermediateOpsDemo {
    public static void main(String[] args) {
        // filter + map
        List<Integer> list = List.of(11, 34, 1, 13, 4);
        System.out.println(list.stream().filter(x -> x > 10).map(x -> x * 2).toList()); // [22, 68, 26]

        // flatMap
        List<List<Integer>> nested = List.of(List.of(1, 2), List.of(3, 4));
        // nested.stream().map(x -> x * 2);                    // compile error: x is a List
        nested.stream().map(x -> x.stream()).forEach(System.out::println);
        // java.util.stream.ReferencePipeline$Head@...   (twice: a stream of streams)
        System.out.println(nested.stream().flatMap(x -> x.stream()).toList());              // [1, 2, 3, 4]
        System.out.println(nested.stream().flatMap(List::stream).map(x -> x * 2).toList()); // [2, 4, 6, 8]

        List<String> sentences = List.of("java is fun", "streams are lazy");
        System.out.println(sentences.stream()
                .flatMap(s -> Arrays.stream(s.split(" ")))
                .toList());                               // [java, is, fun, streams, are, lazy]

        // sorted: natural and descending
        System.out.println(list.stream().filter(x -> x > 10).map(x -> x * 2)
                .sorted().toList());                       // [22, 26, 68]
        System.out.println(list.stream().sorted(Comparator.reverseOrder()).toList()); // [34, 13, 11, 4, 1]
        System.out.println(Stream.of("ccc", "a", "bb")
                .sorted(Comparator.comparing(String::length)).toList()); // [a, bb, ccc]

        // distinct (keeps first occurrence)
        List<Integer> dup = List.of(11, 34, 1, 13, 4, 13, 34);
        System.out.println(dup.stream().filter(x -> x > 10).map(x -> x * 2)
                .sorted(Comparator.reverseOrder()).distinct().toList()); // [68, 26, 22]
        System.out.println(Stream.of("b", "a", "b", "c", "a").distinct().toList()); // [b, a, c]

        // limit and skip
        System.out.println(Stream.iterate(1, x -> x + 1).limit(10).toList());         // [1, 2, ..., 10]
        System.out.println(Stream.iterate(1, x -> x + 1).limit(10).skip(5).toList()); // [6, 7, 8, 9, 10]
        System.out.println(Stream.iterate(1, x -> x + 1).skip(5).limit(10).toList()); // [6, 7, ..., 15]
        System.out.println(dup.stream().filter(x -> x > 10).map(x -> x * 2)
                .sorted(Comparator.reverseOrder()).distinct().skip(1).toList()); // [26, 22]

        int page = 1, size = 3;                            // pagination: page 1 = items 4..6
        System.out.println(IntStream.rangeClosed(1, 10).boxed()
                .skip(page * size).limit(size).toList());  // [4, 5, 6]

        // peek: see the elements between stages
        list.stream()
                .filter(x -> x > 10)
                .map(x -> x * 2)
                .peek(x -> System.out.println("peek " + x))
                .sorted()
                .forEach(x -> System.out.println("sorted " + x));
        // peek 22
        // peek 68
        // peek 26
        // sorted 22
        // sorted 26
        // sorted 68

        // everything together, with peek showing the vertical flow and the sorted barrier
        List<List<Integer>> batches = List.of(List.of(5, 3, 8), List.of(3, 9, 1), List.of(8, 7));
        List<Integer> result = batches.stream()
                .flatMap(List::stream)                         // 5 3 8 3 9 1 8 7
                .peek(x -> System.out.print("in:" + x + " "))
                .filter(x -> x > 2)                            // drop 1
                .distinct()                                    // drop repeated 3 and 8
                .sorted(Comparator.reverseOrder())             // waits for all elements
                .skip(1)                                       // drop the largest
                .limit(3)                                      // keep the next 3
                .map(x -> x * 10)
                .toList();
        System.out.println();                                  // in:5 in:3 in:8 in:3 in:9 in:1 in:8 in:7
        System.out.println(result);                            // [80, 70, 50]
    }
}
```

---

## Terminal operations: the four groups
The terminal operation is the **trigger** (streams are lazy). It **doesn't return a `Stream`**, and after it the stream is consumed.

| Group | Methods | Returns |
| :--- | :--- | :--- |
| **1. Collecting** | `toList()`, `collect(Collector)`, `toArray()` | a `List`, `Set`, `Map`, `String`, array… |
| **2. Reducing** | `reduce`, `count`, `min`/`max(Comparator)`; on primitive streams `sum`, `min`, `max`, `average`, `summaryStatistics` | one value (`Optional`, `long`, `int`…) |
| **3. Searching / matching** | `findFirst`, `findAny`, `anyMatch`, `allMatch`, `noneMatch` | `Optional<T>` or `boolean`, **short-circuiting** |
| **4. Iterating** | `forEach`, `forEachOrdered` | `void` (side effects) |

- `filter` is **not** a terminal operation. It's intermediate and returns a `Stream`. It's often used right before `count`, `findFirst` or the `…Match` methods.

### Group 4: Iterating, `forEach` and `forEachOrdered`
- Both take a `Consumer<T>` and return `void`.
- `forEach` gives **no order guarantee** on parallel streams (threads process elements at the same time). `forEachOrdered` keeps the **encounter order** even in parallel.
- On a sequential stream they print the same thing.
```java
List<Integer> nums = List.of(1, 13, 11, 9);
nums.stream().map(x -> x + 1).forEach(System.out::println);                 // 2 14 12 10
nums.stream().map(x -> x + 1).forEachOrdered(System.out::println);          // 2 14 12 10
nums.parallelStream().map(x -> x + 1).forEach(System.out::println);         // any order, e.g. 12 10 2 14
nums.parallelStream().map(x -> x + 1).forEachOrdered(System.out::println);  // 2 14 12 10 always
```

### Group 1: Collecting, `toList` and `collect`
- **`toList()`** (Java 16+) returns an **unmodifiable** `List<T>`. `add` compiles but throws **`UnsupportedOperationException`** at runtime.
- **`collect(Collectors.toList())`** returns a list you **can** modify in practice (an `ArrayList`). The Javadoc doesn't guarantee it, so use **`Collectors.toCollection(ArrayList::new)`** when you need mutability for sure.
- **`collect(Collector)`**: `Collector` is the **interface** (a strategy for gathering elements), `Collectors` is the **utility class** that creates them, in `java.util.stream`. Compare `Collection` vs `Collections`.
- `toArray()` gives `Object[]`; `toArray(String[]::new)` gives a typed array.
- Collectors in depth are covered [below](#collectors-in-depth).

### Group 2: Reducing, `reduce` and `count`
- **`reduce`** combines all elements into **one value** using a `BinaryOperator<T>` (a `BiFunction<T,T,T>`), e.g. `(a, b) -> a + b`.
```
[1, 13, 11, 9]   reduce((a, b) -> a + b)
a=1,  b=13 → 14
a=14, b=11 → 25
a=25, b=9  → 34
```
| Form | Returns | Empty stream |
| :--- | :--- | :--- |
| `reduce(op)` | `Optional<T>` (read with `get()`) | `Optional.empty`, never `null` |
| `reduce(identity, op)` | `T` | returns `identity` |

- The **identity** is the starting value and must be **neutral**: `0` for `+`, `1` for `*`, `""` for string concatenation.
  - Sum with identity 1 gives 35 instead of 34 (wrong). On a parallel stream the identity may be applied once per chunk, so a non-neutral one breaks completely.
  - Product with identity 0 is always 0, so use 1: `reduce(1, (a, b) -> a * b)` → 1287.
- Built-in shortcuts: `reduce(Integer::sum)`, `reduce(Integer::max)`.
- **`count()`** returns the number of elements as a **`long`**. `int c = stream.count();` doesn't compile.
- **`min(cmp)` / `max(cmp)`** on `Stream<T>` take a `Comparator` and return `Optional<T>`.

### Group 3: Searching and matching
- **`findFirst()`** returns `Optional<T>` with the **first** element in encounter order. It short-circuits: once found, nothing else is processed.
- **`findAny()`** returns `Optional<T>` with **any** element. On a sequential stream it's usually the first one. On a **parallel** stream it returns whatever a thread finds first, which is faster because it doesn't have to respect order.
- **`anyMatch(p)`**: true if **at least one** element matches. Stops at the first match.
- **`allMatch(p)`**: true only if **every** element matches. Stops at the first non-match.
- **`noneMatch(p)`**: true only if **no** element matches. Stops at the first match.
- The `…Match` methods take a `Predicate` and return `boolean`.
- Empty stream: `anyMatch` → false, `allMatch` → **true**, `noneMatch` → true (nothing contradicts them).

### Terminal operations in one program
```java
import java.util.*;
import java.util.stream.*;

public class TerminalOpsDemo {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 13, 11, 9);

        // 4. iterating
        nums.stream().map(x -> x + 1).forEach(x -> System.out.print(x + " "));        // 2 14 12 10
        System.out.println();
        nums.parallelStream().map(x -> x + 1).forEachOrdered(x -> System.out.print(x + " ")); // 2 14 12 10
        System.out.println();

        // 1. collecting
        List<Integer> fixed = nums.stream().map(x -> x + 1).toList();
        try {
            fixed.add(4);
        } catch (UnsupportedOperationException e) {
            System.out.println("toList() is unmodifiable");        // toList() is unmodifiable
        }
        List<Integer> open = nums.stream().map(x -> x + 1).collect(Collectors.toList());
        open.add(4);
        System.out.println(open);                                   // [2, 14, 12, 10, 4]
        ArrayList<Integer> sure = nums.stream().collect(Collectors.toCollection(ArrayList::new));
        sure.add(0);
        System.out.println(sure);                                   // [1, 13, 11, 9, 0]
        String[] arr = Stream.of("a", "b").toArray(String[]::new);
        System.out.println(arr.length);                             // 2

        // 2. reducing
        Optional<Integer> sum = nums.stream().reduce((a, b) -> a + b);
        System.out.println(sum.get());                              // 34
        System.out.println(nums.stream().reduce(0, (a, b) -> a + b));   // 34
        System.out.println(nums.stream().reduce(1, (a, b) -> a + b));   // 35  wrong identity for a sum
        System.out.println(nums.stream().reduce(1, (a, b) -> a * b));   // 1287
        System.out.println(nums.stream().reduce(0, (a, b) -> a * b));   // 0   wrong identity for a product
        System.out.println(nums.stream().reduce(Integer::max).get());   // 13
        System.out.println(Stream.of("a", "b", "c").reduce("", (a, b) -> a + b)); // abc
        System.out.println(Stream.<Integer>empty().reduce((a, b) -> a + b));      // Optional.empty
        System.out.println(Stream.<Integer>empty().reduce(0, (a, b) -> a + b));   // 0

        long count = nums.stream().filter(x -> x > 10).count();     // long, not int
        System.out.println(count);                                  // 2
        System.out.println(nums.stream().max(Comparator.naturalOrder()).get());   // 13
        System.out.println(Stream.of("kiwi", "fig", "banana")
                .min(Comparator.comparing(String::length)).get());  // fig

        // 3. searching / matching
        System.out.println(nums.stream().filter(x -> x > 10).findFirst().get()); // 13
        System.out.println(nums.stream().filter(x -> x > 10).findAny().get());   // 13 (sequential)
        System.out.println(nums.parallelStream().filter(x -> x > 10).findAny().get()); // 13 or 11
        System.out.println(nums.stream().filter(x -> x > 100).findFirst());     // Optional.empty

        List<Integer> big = nums.stream().filter(x -> x > 10).toList();          // [13, 11]
        System.out.println(big.stream().anyMatch(x -> x % 2 == 0));   // false  both odd
        System.out.println(big.stream().allMatch(x -> x % 2 == 0));   // false
        System.out.println(big.stream().noneMatch(x -> x % 2 == 0));  // true   no even number
        System.out.println(List.of(12, 14).stream().allMatch(x -> x % 2 == 0)); // true
        System.out.println(List.of(12, 13).stream().anyMatch(x -> x % 2 == 0)); // true
        System.out.println(Stream.<Integer>empty().allMatch(x -> x > 0));       // true  (vacuously)

        // short-circuit proof: anyMatch stops at the first even number
        Stream.of(1, 4, 6, 8)
                .peek(x -> System.out.print("check " + x + " "))
                .anyMatch(x -> x % 2 == 0);                         // check 1 check 4
        System.out.println();
    }
}
```

---

## `sum`, `min`, `max`, `average`: primitive streams only
- `Stream<Integer>` has **no** `sum()` or `average()`: `nums.stream().sum()` → compile error *"The method sum() is undefined for the type Stream<Integer>"*. A generic `Stream<T>` can't know that `T` is a number.
- Convert first with **`mapToInt(x -> x)`** (takes a `ToIntFunction`, returns an `IntStream`), or `mapToLong` / `mapToDouble`, or create a primitive stream directly (`IntStream.of`, `Arrays.stream(int[])`).

| Method (on `IntStream`) | Returns | Read with | Empty stream |
| :--- | :--- | :--- | :--- |
| `sum()` | `int` | directly | `0` |
| `count()` | `long` | directly | `0` |
| `max()` / `min()` | `OptionalInt` | `getAsInt()` | `OptionalInt.empty` |
| `average()` | `OptionalDouble` (always double) | `getAsDouble()` | `OptionalDouble.empty` |
| `summaryStatistics()` | `IntSummaryStatistics` | `getSum()`, `getMin()`, `getMax()`, `getAverage()`, `getCount()` | count 0 |

- `LongStream` and `DoubleStream` have the same methods, returning `long`/`OptionalLong` and `double`/`OptionalDouble`.
- `max()`/`min()` take **no** argument here. `Stream<T>.max` needs a `Comparator`.
- `sum`, `max`, `min`, `average` and `count` are just special forms of **reduce**.

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

public class PrimitiveTerminalDemo {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 13, 11, 9);
        // nums.stream().sum();                             // compile error: not on Stream<Integer>

        System.out.println(nums.stream().filter(x -> x > 10).mapToInt(x -> x).sum());        // 24
        System.out.println(nums.stream().filter(x -> x > 10).mapToInt(x -> x).max().getAsInt()); // 13
        System.out.println(nums.stream().filter(x -> x > 10).mapToInt(x -> x).min().getAsInt()); // 11
        System.out.println(nums.stream().filter(x -> x > 10).mapToInt(x -> x).average().getAsDouble()); // 12.0
        System.out.println(nums.stream().mapToInt(Integer::intValue).count());               // 4

        IntSummaryStatistics st = nums.stream().mapToInt(x -> x).summaryStatistics();
        System.out.println(st);  // IntSummaryStatistics{count=4, sum=34, min=1, average=8.500000, max=13}

        // empty streams: sum is 0, the others are empty optionals
        System.out.println(IntStream.empty().sum());                     // 0
        System.out.println(IntStream.empty().max());                     // OptionalInt.empty
        System.out.println(IntStream.empty().average().orElse(0));       // 0.0

        // real use: salary statistics
        List<Employee> emps = List.of(new Employee("Rohit", 60000),
                new Employee("Aditya", 75000), new Employee("Sonu", 40000));
        System.out.println(emps.stream().mapToInt(Employee::getSalary).sum());               // 175000
        System.out.println(emps.stream().mapToInt(Employee::getSalary).average().getAsDouble()); // 58333.333333333336
        System.out.println(emps.stream().mapToInt(Employee::getSalary).max().getAsInt());    // 75000

        // long and double versions
        System.out.println(LongStream.rangeClosed(1, 5).sum());                              // 15
        System.out.println(DoubleStream.of(2.5, 3.5).average().getAsDouble());               // 3.0
        System.out.println(Stream.of("a", "bb", "ccc").mapToDouble(String::length).sum());   // 6.0
    }
}
```

---

## Collectors in depth
`collect(Collector)` is the most flexible terminal operation. A `Collector` is a **strategy** that says how to gather elements; `Collectors` provides the ready-made ones.

| Collector | Result type | Example |
| :--- | :--- | :--- |
| `toList()` | `List<T>` | `[2, 14, 12, 10]` |
| `toSet()` | `Set<T>` (no order guarantee, no duplicates) | `[2, 10, 12, 14]` |
| `toCollection(Supplier)` | any collection you choose | `toCollection(TreeSet::new)` |
| `toMap(keyFn, valueFn)` | `Map<K,V>` | `{2=AA, 3=BBB, 4=CCCC}` |
| `toMap(keyFn, valueFn, merge)` | `Map<K,V>`, handles duplicate keys | `(a, b) -> a + "," + b` |
| `groupingBy(classifier)` | `Map<K, List<T>>` | `{2=[AA, DD], 3=[BBB, EEE]}` |
| `groupingBy(classifier, downstream)` | `Map<K, D>` | values counted, mapped, summed… |
| `partitioningBy(predicate)` | `Map<Boolean, List<T>>` | `{false=[…], true=[…]}` |
| `joining(delim)` | `String` | `"AA-BBB-CCCC"` |
| `counting()`, `summingInt`, `averagingInt`, `mapping` | used as **downstream** collectors | |

### `toList`, `toSet`, `toMap`
- `toMap(keyMapper, valueMapper)`: both are `Function`s, one makes the **key**, the other the **value**.
- **Duplicate keys:** the 2-argument `toMap` throws **`IllegalStateException: Duplicate key`**. Pass a third **merge function** (`(old, neu) -> …`) or use `groupingBy`.
- `toMap(k, v, merge, TreeMap::new)` picks the map type (e.g. sorted keys).

### `groupingBy`
- Splits elements into groups by a **key** computed from each element. The values are **lists**, so the type is **`Map<K, List<T>>`**, not `Map<K, T>`.
- `Stream.of("AA", "BBB", "CCCC", "DD", "EEE").collect(groupingBy(String::length))` → `{2=[AA, DD], 3=[BBB, EEE], 4=[CCCC]}`.
- Real use: `employees.stream().collect(groupingBy(Employee::getDepartment))` → `HR=[…], Tech=[…]`.
- **Overloads:**
  - `groupingBy(classifier)`: values are a `List`.
  - `groupingBy(classifier, downstream)`: a **downstream collector** decides what goes into each group:
    - `mapping(fn, toList())`: transform elements first, e.g. names only.
    - `counting()`: how many per group (`Long`).
    - `summingInt(fn)` / `averagingInt(fn)`: total / average per group.
    - `toSet()`: unique values per group.
    - `maxBy(cmp)`: top element per group (`Optional`).
    - `joining(", ")`: a string per group.
  - `groupingBy(classifier, mapFactory, downstream)`: chooses the map, e.g. `TreeMap::new` for sorted keys.
- `mapping(mapper, downstream)` takes a `Function` and **another collector**. That inner collector is why it's called "downstream".
- The default map is a `HashMap`, so key order isn't guaranteed.

### `partitioningBy`
- A special case of grouping with **exactly two groups**: `true` and `false`. It takes a **`Predicate`**.
- Type: **`Map<Boolean, List<T>>`**. **Both keys are always present**, even if a group is empty (`true=[]`). `groupingBy` would omit the missing key.
- `List.of(1, 13, 11, 9)` with `partitioningBy(x -> x % 2 == 0)` → `{false=[1, 13, 11, 9], true=[]}`. With 14 instead of 13 → `{false=[1, 11, 9], true=[14]}`.
- Also has a downstream overload: `partitioningBy(pred, counting())`.

| | `groupingBy` | `partitioningBy` |
| :--- | :--- | :--- |
| Takes | `Function` (any key) | `Predicate` (true/false) |
| Number of groups | as many as distinct keys | always 2 |
| Map type | `Map<K, List<T>>` | `Map<Boolean, List<T>>` |
| Empty group | key absent | key present with `[]` |

### `joining`
- Concatenates **strings** into one `String`. Elements must be `CharSequence`, so `map(String::valueOf)` numbers first.
- `joining()`, `joining("-")`, or `joining(", ", "[", "]")` with a prefix and suffix.

```java
import java.util.*;
import java.util.stream.*;

class Employee {
    private String name;
    private String department;
    private int salary;

    Employee(String name, String department, int salary) {
        this.name = name; this.department = department; this.salary = salary;
    }
    String getName() { return name; }
    String getDepartment() { return department; }
    int getSalary() { return salary; }

    @Override
    public String toString() { return name; }
}

public class CollectorsDemo {
    public static void main(String[] args) {
        List<Integer> nums = List.of(1, 13, 11, 9);
        List<String> words = List.of("AA", "BBB", "CCCC", "DD", "EEE");

        // toList / toSet / toCollection
        System.out.println(nums.stream().map(x -> x + 1).collect(Collectors.toList()));  // [2, 14, 12, 10]
        Set<Integer> set = Stream.of(3, 1, 3, 2).collect(Collectors.toSet());
        System.out.println(set.size());                                                   // 3
        TreeSet<Integer> sorted = Stream.of(3, 1, 3, 2).collect(Collectors.toCollection(TreeSet::new));
        System.out.println(sorted);                                                       // [1, 2, 3]

        // toMap
        Map<Integer, String> byLen = Stream.of("AA", "BBB", "CCCC")
                .collect(Collectors.toMap(x -> x.length(), x -> x));
        System.out.println(byLen);                                   // {2=AA, 3=BBB, 4=CCCC}
        try {
            words.stream().collect(Collectors.toMap(String::length, x -> x));
        } catch (IllegalStateException e) {
            System.out.println(e.getMessage());   // Duplicate key 2 (attempted merging values AA and DD)
        }
        Map<Integer, String> merged = words.stream()
                .collect(Collectors.toMap(String::length, x -> x, (a, b) -> a + "," + b, TreeMap::new));
        System.out.println(merged);                                  // {2=AA,DD, 3=BBB,EEE, 4=CCCC}

        // groupingBy
        Map<Integer, List<String>> groups = words.stream()
                .collect(Collectors.groupingBy(x -> x.length()));
        System.out.println(groups);                                  // {2=[AA, DD], 3=[BBB, EEE], 4=[CCCC]}

        Map<Integer, List<String>> lower = words.stream()
                .collect(Collectors.groupingBy(x -> x.length(),
                        Collectors.mapping(x -> x.toLowerCase(), Collectors.toList())));
        System.out.println(lower);                                   // {2=[aa, dd], 3=[bbb, eee], 4=[cccc]}

        Map<Integer, Long> counts = words.stream()
                .collect(Collectors.groupingBy(String::length, Collectors.counting()));
        System.out.println(counts);                                  // {2=2, 3=2, 4=1}

        List<Employee> emps = List.of(
                new Employee("Rohit", "Tech", 60000), new Employee("Aditya", "Tech", 75000),
                new Employee("Sonu", "HR", 40000), new Employee("Aman", "HR", 45000),
                new Employee("Neha", "Sales", 50000));

        Map<String, List<Employee>> byDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new, Collectors.toList()));
        System.out.println(byDept);              // {HR=[Sonu, Aman], Sales=[Neha], Tech=[Rohit, Aditya]}

        Map<String, List<String>> namesByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new,
                        Collectors.mapping(Employee::getName, Collectors.toList())));
        System.out.println(namesByDept);         // {HR=[Sonu, Aman], Sales=[Neha], Tech=[Rohit, Aditya]}

        Map<String, Integer> totalByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new,
                        Collectors.summingInt(Employee::getSalary)));
        System.out.println(totalByDept);         // {HR=85000, Sales=50000, Tech=135000}

        Map<String, Double> avgByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new,
                        Collectors.averagingInt(Employee::getSalary)));
        System.out.println(avgByDept);           // {HR=42500.0, Sales=50000.0, Tech=67500.0}

        Map<String, Optional<Employee>> topByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new,
                        Collectors.maxBy(Comparator.comparing(Employee::getSalary))));
        System.out.println(topByDept);           // {HR=Optional[Aman], Sales=Optional[Neha], Tech=Optional[Aditya]}

        Map<String, String> joinedByDept = emps.stream()
                .collect(Collectors.groupingBy(Employee::getDepartment, TreeMap::new,
                        Collectors.mapping(Employee::getName, Collectors.joining(" & "))));
        System.out.println(joinedByDept);        // {HR=Sonu & Aman, Sales=Neha, Tech=Rohit & Aditya}

        // partitioningBy
        Map<Boolean, List<Integer>> evenOdd = nums.stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0));
        System.out.println(evenOdd);                                 // {false=[1, 13, 11, 9], true=[]}
        System.out.println(List.of(1, 14, 11, 9).stream()
                .collect(Collectors.partitioningBy(x -> x % 2 == 0))); // {false=[1, 11, 9], true=[14]}

        Map<Boolean, List<String>> rich = emps.stream()
                .collect(Collectors.partitioningBy(e -> e.getSalary() >= 50000,
                        Collectors.mapping(Employee::getName, Collectors.toList())));
        System.out.println(rich);                // {false=[Sonu, Aman], true=[Rohit, Aditya, Neha]}
        System.out.println(rich.get(true));      // [Rohit, Aditya, Neha]

        System.out.println(nums.stream()
                .collect(Collectors.partitioningBy(x -> x > 10, Collectors.counting()))); // {false=2, true=2}

        // groupingBy vs partitioningBy on the same data: groupingBy drops the empty group
        System.out.println(nums.stream().collect(Collectors.groupingBy(x -> x % 2 == 0)));     // {false=[1, 13, 11, 9]}
        System.out.println(nums.stream().collect(Collectors.partitioningBy(x -> x % 2 == 0))); // {false=[1, 13, 11, 9], true=[]}

        // joining
        System.out.println(words.stream().collect(Collectors.joining("-")));             // AA-BBB-CCCC-DD-EEE
        System.out.println(words.stream().collect(Collectors.joining()));                // AABBBCCCCDDEEE
        System.out.println(nums.stream().map(String::valueOf)
                .collect(Collectors.joining(", ", "[", "]")));                           // [1, 13, 11, 9]
    }
}
```

**Remember:** intermediate operations (`filter`, `map`, `flatMap`, `sorted`, `distinct`, `limit`, `skip`, `peek`) return a `Stream` and are lazy; `sorted` and `distinct` are stateful. Terminal operations come in four groups: **collect** (`toList` is unmodifiable, `collect(Collectors…)`), **reduce** (`reduce` with a neutral identity, `count` returns `long`), **search/match** (`findFirst`, `findAny`, `anyMatch`/`allMatch`/`noneMatch`, all short-circuiting) and **iterate** (`forEach`, `forEachOrdered`). `sum`/`max`/`min`/`average` exist only on primitive streams (`mapToInt` first). `groupingBy` → `Map<K, List<T>>` with optional downstream collectors; `partitioningBy` → always `true` and `false` keys.
