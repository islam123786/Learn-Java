# Parallel Streams in Java Explained | Primitive Optional Classes | Java Full Course #42
> Source: https://youtu.be/zrtVQ1ev_0w · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 42:35. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data and came back as a single block, so the timestamps below are rough estimates based on where each part falls in the transcript (expect an error of a minute or two).

## TL;DR
- A **sequential stream** pushes one element at a time through the pipeline. A **parallel stream** (`list.parallelStream()` or `stream().parallel()`) **splits the data into chunks, processes them on multiple threads, and combines the results**, using the **Fork/Join pool** internally.
- **Order is not guaranteed** with `forEach` on a parallel stream. `forEachOrdered` restores encounter order, but it waits on the threads and gives up much of the speed-up.
- Splitting is done by a **`Spliterator`** ("split + iterator"). It *traverses* elements (`tryAdvance`), *decomposes* the source (`trySplit`), and *describes* it (size, ordered, sized, …). Java decides how many splits and threads to use. `parallelStream()` is a **request**, not a guarantee.
- Parallel streams help with **huge data**, **CPU-heavy work**, **mostly stateless operations** (`filter`, `map`) and **easily splittable sources** (arrays, `ArrayList`). They hurt with **small data**, **many stateful operations** (`sorted`, `distinct`), **`LinkedList`**, and they're simply **buggy with shared mutable state** (race conditions).
- **Primitive optionals**: `OptionalInt`, `OptionalLong`, `OptionalDouble` avoid boxing. They're returned by `IntStream.max()`, `average()` and similar, and read with `getAsInt()`/`getAsLong()`/`getAsDouble()` plus the usual `orElse`, `isPresent`, …

## Key Concepts

**Sequential vs parallel stream.** Sequential: one element travels through the whole pipeline before the next, which takes about O(n) time. Parallel: several elements go through the pipeline at the same time on different threads, so with k workers each one does roughly n/k of the work. *Why it matters:* parallelism can speed up big, independent workloads with almost no code change.

**Stateless vs stateful operations.** `map` and `filter` need only the current element, so they parallelize well. `sorted` and `distinct` need all the elements (or all the earlier ones), so every thread has to wait at them. *Why it matters:* stateful steps act as "join" barriers and eat into the benefit of parallelism.

**Fork/Join pool.** A pool of worker threads. Work is **forked** (recursively split into chunks), processed, then **joined** (combined). Parallel streams use the shared `ForkJoinPool.commonPool()`. *Why it matters:* you get multithreading without managing threads yourself, but thread coordination has a cost.

**`Spliterator`.** The iterator behind every stream. Besides `tryAdvance` (process the next element), it can `trySplit` (hand off part of the source to a new spliterator) and report characteristics such as `SIZED`, `ORDERED`, `SUBSIZED`. *Why it matters:* without splitting there would be no parallel streams, and how cheaply a source splits largely decides how well it parallelizes.

**Race condition / shared mutable state.** Two threads updating the same non-thread-safe object (for example `result.add(x)` on an `ArrayList`) can overwrite each other, losing data or corrupting the list. *Why it matters:* this isn't just slower, it gives **wrong** results. Use `collect`/`toList`/`reduce` instead of side effects.

**Primitive optionals.** `OptionalInt`, `OptionalLong`, `OptionalDouble` are the 0-or-1 containers for primitives, mirroring `IntStream`/`IntFunction` etc. *Why it matters:* numeric stream terminals return them, and they avoid boxing.

## Detailed Notes

### Recap: a sequential stream [▶ ~00:00](https://youtu.be/zrtVQ1ev_0w?t=0)
- `List<Integer> list = new ArrayList<>(List.of(1, 2, …, 11)); list.stream().map(x -> x * 2).forEach(System.out::println);` prints 2, 4, …, 22 in order.
- That's a **sequential stream** [▶ ~01:00](https://youtu.be/zrtVQ1ev_0w?t=60): its spliterator takes one element at a time and passes it **vertically** through the pipeline (map, then forEach).

### Why parallelism is possible: stateless vs stateful [▶ ~03:00](https://youtu.be/zrtVQ1ev_0w?t=180)
- `map` is **stateless**: doubling 1 doesn't depend on any other element.
- Add `.sorted()` and you have a **stateful** operation. Element 1 passes `map` but has to **wait** at `sorted` until every element has arrived.
- If the operations are stateless and the elements are independent, there's no reason to wait for element 1 before starting element 2. They can go through the pipe at the same time.
- Threads (covered properly later in the multithreading series): for now, think of a thread as a lightweight program that can run independently.

### `parallelStream()` in action [▶ ~06:00](https://youtu.be/zrtVQ1ev_0w?t=360)
- `list.parallelStream().map(x -> x * 2).forEach(System.out::println)` prints all the doubled values, but **out of order** (e.g. 6, 20, 14, 16, …).
- The reason: different threads process different elements at their own pace. Some wait their turn (context switching), others get ahead. The elements are effectively racing. Not every element gets its own thread: there are a few threads, each handling several elements.
- **`forEachOrdered`** [▶ ~07:15](https://youtu.be/zrtVQ1ev_0w?t=435) restores the original order, but it's **overhead**: it has to wait until results are ready in order, which removes much of the reason for using parallelism.
- With only ~11 elements you won't notice any speed difference. With something like a million elements and enough CPU work per element, the parallel version can finish faster.

### Architecture: split, assign, process, combine [▶ ~09:00](https://youtu.be/zrtVQ1ev_0w?t=540)
- The steps: **split the data into chunks → assign each chunk to a thread → process the chunks independently → combine the results**.
- Analogy: you have to double 10 numbers. Alone, that's n units of work. Give 5 to a friend and do 5 yourself, sharing the same "pipeline" concurrently, and each of you does n/2.
- Under the hood this is the **Fork/Join pool** [▶ ~12:30](https://youtu.be/zrtVQ1ev_0w?t=750): a pool of idle worker threads (your "friends"). Chunks get assigned to T1, T2, …
- A huge input (say a billion elements) is split **recursively**: halves (1–5, 6–10), then halves again (1–3, 4–5, 6–8, 9–10), spread over T1–T4 so that each does about n/4. Some chunks really run at the same moment.
- **You don't decide any of this** [▶ ~15:00](https://youtu.be/zrtVQ1ev_0w?t=900). Java's algorithms choose how many splits and threads to use. Writing `parallelStream()` only tells Java "parallelize this if you can".

### `Spliterator` vs `Iterator` [▶ ~16:30](https://youtu.be/zrtVQ1ev_0w?t=990)
- A normal `Iterator` (`hasNext`, `next`, `remove`) can only walk **one element at a time**, so a parallel stream can't split work with it.
- **`Spliterator` = split + iterator.** It has three jobs:
  1. **Traverse** the elements, like any iterator.
  2. **Decompose** the source into parts, recursively: split into two, split again if the parts are still big. That's the "fork" in fork/join. "Join" is combining the results at the end.
  3. **Describe** the source: is it ordered? Indexed (e.g. an `ArrayList`)? Sorted? What's its size? Streams use these "characteristics" to optimize. This is a big reason streams are efficient.
- Without the Spliterator there would be no parallel streams.
- You'll almost never call Spliterator methods or write your own (in the video's words, 99.9% of the time). Custom `Iterator`s are already rare.

### `tryAdvance` and `trySplit` [▶ ~20:00](https://youtu.be/zrtVQ1ev_0w?t=1200)
- **`tryAdvance(action)`**: handles traversal. If a next element exists, it feeds it to the pipeline (it plays the role of `hasNext`/`next`).
- **`trySplit()`**: *tries* to split the data in two. If it succeeds, it returns a new spliterator for one part (SI1/SI2), and the original keeps the rest.
- Note the word **"try"** [▶ ~22:00](https://youtu.be/zrtVQ1ev_0w?t=1320): splitting isn't always possible or worthwhile, and parallel processing isn't magic.

### Parallel ≠ automatically faster [▶ ~22:00](https://youtu.be/zrtVQ1ev_0w?t=1320)
- Example: `list.parallelStream().filter(x -> x > 10).map(x -> x * 2).sorted().distinct().forEach(System.out::println)`.
  - `filter` and `map` are stateless, so the chunks (1–3, 4–5, 6–8, 9–10) can pass through them in parallel. That's where time is saved.
  - `sorted` and `distinct` are **stateful**. Neither lets anything past until it has every element, so the chunks have to be **joined** back together there.
- **Fork** = divide the data. **Join** = bring it back together for operations that need everything [▶ ~25:30](https://youtu.be/zrtVQ1ev_0w?t=1530).
- With many stateful operations, the thread-management cost of the Fork/Join pool can make the parallel version **slower than sequential**. That's why most real code uses ordinary sequential streams.
- How Java decides how far to split [▶ ~27:30](https://youtu.be/zrtVQ1ev_0w?t=1650): **size of the data**, **complexity of the problem**, and **available threads** (the pool is limited). For small data it keeps parallelism low to limit overhead.

### When parallel streams actually help [▶ ~29:30](https://youtu.be/zrtVQ1ev_0w?t=1770)
- **Huge data sets** (millions of elements). Even with a few stateful steps, the stateless parts gain a lot.
- **CPU-intensive work per element.** A multi-core CPU can run several threads truly at the same time (T1–T4 on different cores).
- **Mostly or only stateless operations.**
- **Data structures that split well**: indexed, random-access structures (**arrays**, **`ArrayList`**). The spliterator can jump straight to the middle in O(1).

### When not to use them [▶ ~32:00](https://youtu.be/zrtVQ1ev_0w?t=1920)
- **`LinkedList`**: there's no random access, so finding the middle means walking from the head (or tail). Splitting works but is expensive.
- **Small data sets**: the overhead outweighs the gain [▶ ~33:00](https://youtu.be/zrtVQ1ev_0w?t=1980).
- **Lots of stateful operations**: little or no benefit.
- **Shared mutable resources, which is a real bug** [▶ ~34:00](https://youtu.be/zrtVQ1ev_0w?t=2040):
  - `List<Integer> result = new ArrayList<>(); list.parallelStream().map(...).forEach(x -> result.add(x));`
  - Sequentially this is fine. In parallel, two threads can reach `result.add` at the same moment. Both may write into the same slot before the internal size/index is updated, so one value overwrites the other and data is **lost**. That's a **race condition**.
  - `result` is **shared** (any thread can reach it) and **mutable** (it gets modified). Avoid that combination. *(Fix: let the stream build the result, `list.parallelStream().map(...).toList()` / `collect(...)`, or use a thread-safe structure.)*
- Summary [▶ ~37:00](https://youtu.be/zrtVQ1ev_0w?t=2220): parallel streams aren't "always better". With a `LinkedList`, many stateful operations and little data, they're slower because of the thread and work-pool overhead.

### Primitive optional classes [▶ ~38:00](https://youtu.be/zrtVQ1ev_0w?t=2280)
- Recap: `Optional<T>` wraps an object to avoid returning `null`, e.g. `Optional<String> name = getName(); name.get() / name.orElse(...)`.
- It can wrap `Integer`, but then every operation **unboxes** to `int` and every store **autoboxes** back. Java already added primitive streams and primitive functional interfaces to avoid this, so `Optional` gets **primitive versions** too.
- **`OptionalInt`, `OptionalDouble`, `OptionalLong`**: there are no byte/short/float/char versions, for the same reason as before (int, double and long cover real use).
- Where you meet them: `IntStream.of(1, 2, 3, 4).filter(...).max()` returns **`OptionalInt`**, and `average()` always returns **`OptionalDouble`**, even on an `IntStream`.
- Reading the value [▶ ~41:00](https://youtu.be/zrtVQ1ev_0w?t=2460): **`getAsInt()`**, **`getAsDouble()`**, **`getAsLong()`** instead of `get()`. `orElse`, `isPresent`, `ifPresent`, … work the same way.
- *(Note: primitive optionals don't have `map`, `filter` or `flatMap`. If you need those, box it, or work on the stream before the terminal operation.)*

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`. Thread scheduling isn't deterministic, so where output order or values can change, the comment says so.

### Sequential vs parallel, and getting a deterministic result
```java
import java.util.*;
import java.util.stream.*;

public class ParallelBasicsDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11));

        list.stream().map(x -> x * 2).forEach(x -> System.out.print(x + " "));
        System.out.println();
        // 2 4 6 8 10 12 14 16 18 20 22

        list.parallelStream().map(x -> x * 2).forEach(x -> System.out.print(x + " "));
        System.out.println();
        // same numbers, but the ORDER VARIES from run to run (e.g. 14 16 6 20 ...)

        list.parallelStream().map(x -> x * 2).forEachOrdered(x -> System.out.print(x + " "));
        System.out.println();
        // 2 4 6 8 10 12 14 16 18 20 22   (encounter order restored, at a cost)

        List<Integer> doubled = list.parallelStream().map(x -> x * 2).toList();
        System.out.println(doubled);
        // [2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22]  (toList/collect keep encounter order)

        System.out.println(list.parallelStream().mapToInt(x -> x).sum()); // 66
        System.out.println(list.stream().parallel().isParallel());        // true
        System.out.println(ForkJoinPoolInfo.parallelism() >= 1);         // true
    }
}

class ForkJoinPoolInfo {
    static int parallelism() {
        return java.util.concurrent.ForkJoinPool.commonPool().getParallelism(); // usually cores - 1
    }
}
```

### Stateless + stateful ops still give correct results in parallel
```java
import java.util.*;
import java.util.stream.*;

public class ParallelStatefulDemo {
    public static void main(String[] args) {
        List<Integer> list = List.of(3, 12, 7, 15, 12, 20, 9, 15, 11);

        List<Integer> seq = list.stream()
                .filter(x -> x > 10).map(x -> x * 2).sorted().distinct().toList();
        List<Integer> par = list.parallelStream()
                .filter(x -> x > 10).map(x -> x * 2).sorted().distinct().toList();

        System.out.println(seq);              // [22, 24, 30, 40]
        System.out.println(par);              // [22, 24, 30, 40]  (same result; less speed-up due to stateful ops)
        System.out.println(seq.equals(par));  // true

        // findAny may return ANY match in parallel; findFirst respects order
        System.out.println(list.parallelStream().filter(x -> x > 10).findFirst().get()); // 12
        int any = list.parallelStream().filter(x -> x > 10).findAny().get();
        System.out.println(any > 10);         // true  (the value itself varies)
    }
}
```

### Shared mutable state: the race condition and the fix
```java
import java.util.*;
import java.util.stream.*;

public class RaceConditionDemo {
    public static void main(String[] args) {
        List<Integer> nums = IntStream.rangeClosed(1, 100_000).boxed().toList();

        // BUG: unsynchronized shared counter mutated from many threads
        int[] counter = {0};
        nums.parallelStream().forEach(x -> counter[0]++);
        System.out.println(counter[0] <= 100_000); // true  (exact value VARIES; often < 100000 = lost updates)

        // BUG: ArrayList.add from many threads may lose elements or even throw
        List<Integer> unsafe = new ArrayList<>();
        try {
            nums.parallelStream().forEach(unsafe::add);
            System.out.println("unsafe size may be wrong: " + (unsafe.size() <= 100_000)); // true (size VARIES)
        } catch (RuntimeException e) {
            System.out.println("unsafe add crashed: " + e.getClass().getSimpleName()); // possible outcome
        }

        // FIX: let the stream build the result (no shared mutable state)
        List<Integer> safe = nums.parallelStream().map(x -> x).toList();
        System.out.println(safe.size());                                   // 100000
        System.out.println(nums.parallelStream().count());                 // 100000
        System.out.println(nums.parallelStream().mapToLong(x -> x).sum()); // 5000050000
    }
}
```

### Peeking at a `Spliterator`
```java
import java.util.*;

public class SpliteratorPeekDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6, 7, 8, 9, 10));
        Spliterator<Integer> right = list.spliterator();

        System.out.println(right.estimateSize());                       // 10
        System.out.println(right.hasCharacteristics(Spliterator.SIZED));   // true
        System.out.println(right.hasCharacteristics(Spliterator.ORDERED)); // true

        Spliterator<Integer> left = right.trySplit();   // decompose: left half handed off
        System.out.println(left.estimateSize() + " + " + right.estimateSize()); // 5 + 5

        left.tryAdvance(x -> System.out.println("first of left: " + x)); // first of left: 1
        StringBuilder sb = new StringBuilder();
        left.forEachRemaining(x -> sb.append(x).append(' '));
        right.forEachRemaining(x -> sb.append(x).append(' '));
        System.out.println(sb.toString().trim());       // 2 3 4 5 6 7 8 9 10
    }
}
```

### Primitive optionals: `OptionalInt`, `OptionalDouble`, `OptionalLong`
```java
import java.util.*;
import java.util.stream.*;

public class PrimitiveOptionalsDemo {
    public static void main(String[] args) {
        OptionalInt max = IntStream.of(1, 2, 3, 4).filter(x -> x > 1).max();
        System.out.println(max.getAsInt());                        // 4

        OptionalDouble avg = IntStream.of(1, 2, 3, 4).average();   // always OptionalDouble
        System.out.println(avg.getAsDouble());                     // 2.5

        OptionalLong big = LongStream.of(5_000_000_000L, 7L).min();
        System.out.println(big.getAsLong());                       // 7

        OptionalInt none = IntStream.of(1, 2, 3).filter(x -> x > 10).findFirst();
        System.out.println(none.isPresent());                      // false
        System.out.println(none.orElse(-1));                       // -1
        none.ifPresentOrElse(System.out::println, () -> System.out.println("no match")); // no match

        OptionalInt direct = OptionalInt.of(42);
        System.out.println(direct);                                // OptionalInt[42]
        System.out.println(OptionalInt.empty());                   // OptionalInt.empty
    }
}
```

## Common Pitfalls / Tips
- **Don't parallelize by default.** Measure first (ideally with a benchmark harness like JMH). For small or I/O-bound workloads, sequential is usually faster.
- **Never mutate shared state from a parallel stream** (`list.add`, counters, `HashMap.put`). Use `collect`, `toList`, `reduce`, `sum`, or concurrent collectors.
- **`forEach` order is undefined in parallel.** Use `forEachOrdered`, or better, collect the results (`toList()` keeps encounter order).
- **`findAny` can return any match in parallel.** Use `findFirst` if you need the first one.
- **`reduce` in parallel needs a true identity and an associative operation.** `reduce(1, Integer::sum)` gives different answers in parallel.
- **`LinkedList`, `Stream.iterate` and I/O-backed sources split poorly.** Prefer arrays, `ArrayList`, `IntStream.range`.
- **All parallel streams share `ForkJoinPool.commonPool()`.** A slow or blocking task in one can starve others. Don't do blocking I/O inside them.
- **Primitive optionals use `getAsInt()`/`getAsDouble()`/`getAsLong()`, not `get()`.** They have no `map`/`filter`.

## Review Questions
1. How does a parallel stream process data differently from a sequential stream?
2. Why does `list.parallelStream().map(x -> x * 2).forEach(System.out::println)` print in a different order each run, and how can you get the original order?
3. What are the three jobs of a `Spliterator`, and which two methods matter most?
4. What do "fork" and "join" mean in the Fork/Join pool, and where does joining happen in `filter → map → sorted → distinct`?
5. List three conditions under which parallel streams help, and three under which they don't.
6. Why does `LinkedList` parallelize worse than `ArrayList`?
7. What goes wrong with `list.parallelStream().forEach(x -> result.add(x))` on an `ArrayList`? How do you fix it?
8. What are `OptionalInt`, `OptionalLong` and `OptionalDouble`? Which stream methods return them, and how do you read their value?

<details>
<summary>Answers</summary>

1. A sequential stream sends one element at a time through the whole pipeline on one thread. A parallel stream splits the source into chunks (via a `Spliterator`), processes them on several threads from the common Fork/Join pool, then combines the results.
2. Different threads handle different elements and run at their own pace, so the side effects of `forEach` happen in no fixed order. Use `forEachOrdered` (slower), or collect the results with `toList()`/`collect`, which keep encounter order.
3. Traverse elements, decompose (split) the source, and describe the source (size, ordered, sorted, …). The key methods are `tryAdvance` (process the next element) and `trySplit` (try to hand half of the remaining elements to a new spliterator).
4. Fork = recursively split the data into chunks for different threads. Join = combine the partial results. `filter` and `map` run on the chunks independently. `sorted` (and then `distinct`) need all the elements, so the chunks are joined there.
5. They help with huge data sets, CPU-intensive work per element, mostly stateless operations, and easily splittable sources (arrays, `ArrayList`). They don't help with small data sets, many stateful operations, poorly splittable sources (`LinkedList`), and (as a correctness bug, not just slowness) shared mutable state.
6. Splitting needs to reach the middle of the data. An `ArrayList` does that in O(1) by index. A `LinkedList` has to walk node by node from an end, so every split is expensive.
7. Several threads call `add` at once on a non-thread-safe list. They can overwrite each other's slot before the size is updated (lost elements), or corrupt the internal array and throw. That's a race condition on shared mutable state. Fix it by letting the stream build the list (`parallelStream().map(...).toList()` or `collect(Collectors.toList())`), or use a thread-safe collection.
8. They're the 0-or-1 containers for primitives, so they avoid boxing. `IntStream.max()/min()/findFirst()` return `OptionalInt`, `average()` returns `OptionalDouble` on any primitive stream, and `LongStream.max()` returns `OptionalLong`. Read them with `getAsInt()`, `getAsDouble()` or `getAsLong()`, or safely with `orElse`/`ifPresent`.
</details>

## Further Study
- Multithreading basics: threads, context switching, race conditions, synchronization (upcoming in the series)
- `ForkJoinPool`, `RecursiveTask`, work stealing; running a parallel stream in a custom pool
- `Spliterator` characteristics in depth; writing a custom Spliterator
- Concurrent collectors: `Collectors.toConcurrentMap`, `groupingByConcurrent`
- Benchmarking with JMH; Amdahl's law
- `unordered()` to speed up parallel `distinct`/`limit`

---

[← #41](41-java-optional-explained.md) · [Index](ReadMe.md) · [#43 →](43-exception-handling-exceptions-vs-errors.md)
