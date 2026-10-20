# Lock-Free Concurrency in Java - 2 | CAS Retry, Compare-and-Swap & ABA Problem | Java Full Course #55
> Source: https://youtu.be/2nBJPpERul4 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 31:29. The video is in Hindi; these notes were written from YouTube's English-translated track, so a few terms were mistranscribed (e.g. "cache" for CAS, "retie" for retry) and have been corrected here. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `volatile` only gives **visibility**. Atomic classes give visibility **and** atomic read-modify-write operations. Atomic variables already behave like `volatile`, so you don't add the keyword.
- `compareAndSet(expected, new)` lets only one thread win. When *every* thread's update has to land (a like counter), wrap it in a **CAS retry loop**: read, compute, `compareAndSet`, and if that fails, read again and retry.
- `AtomicInteger.incrementAndGet()` (and friends) already do that retry logic internally, so the hand-written loop becomes a single line.
- `AtomicReferenceArray` gives you thread-safe per-index `get`/`set`/`compareAndSet`, for example a whole row of bookable seats.
- **ABA problem:** CAS only checks "is the value the same as when I read it?", not "did anything happen in between?". A → B → A fools it. The fix is **versioning**, which Java provides as `AtomicStampedReference`.

## Key Concepts

**`volatile` vs atomic variables.** `volatile` makes every read see the latest write. It does nothing for compound actions like `count++`. Atomic classes (`AtomicInteger`, `AtomicLong`, `AtomicBoolean`, `AtomicReference`, ...) add operations that happen all at once or not at all, and their fields are volatile internally. *Why it matters:* a `volatile int` counter still loses updates under contention.

**`compareAndSet(expected, newValue)`.** This sets the new value only if the current value still equals `expected`, and returns `true` or `false`. *Why it matters:* it is the building block of every lock-free algorithm in `java.util.concurrent`.

**CAS retry loop.** `while (true) { read; compute; if (compareAndSet(read, computed)) return; }`. A thread that loses the race doesn't block. It re-reads the fresh value and tries again. *Why it matters:* nobody's update is lost and no lock is taken.

**Compare-and-swap (CAS).** The video calls the *technique* "compare and swap" and the *method* "compare and set". Strictly speaking, CAS is a single CPU instruction (e.g. `LOCK CMPXCHG` on x86), and `compareAndSet` is Java's wrapper around it. The retry loop is a pattern you build on top of it.

**Locking vs lock-free.** With a lock, one thread enters the critical section and the others **wait** (blocked). With CAS, all threads **try** at the same time, one succeeds, and the rest **retry**. *Why it matters:* lock-free code avoids blocking and context switches, but it only works for small, single-variable updates. Anything bigger still needs locks.

**`AtomicReferenceArray<E>`.** An array whose slots each support atomic `get`, `set` and `compareAndSet(index, expected, new)`. *Why it matters:* a plain array isn't thread-safe per element, and wrapping the whole array in one `AtomicReference` doesn't help with per-slot updates.

**ABA problem.** Thread T1 reads A. T2 changes A → B → A. T1's CAS sees A and succeeds, even though the state changed underneath it. *Why it matters:* it's harmless for a counter, but it can corrupt pointer-based structures like lock-free stacks, or any case where "A again" doesn't mean "same situation".

**Versioning / stamps.** Store a `(value, version)` pair and bump the version on every write. A1 → B2 → A3 no longer matches T1's A1, so the CAS fails. Java's `AtomicStampedReference` does exactly this.

## Detailed Notes

### Recap and agenda [00:00]
- So far: atomic variables (`AtomicInteger`, `AtomicLong`, `AtomicBoolean`), `AtomicReference`, and `compareAndSet`, used for the seat-booking example.
- Today: volatile vs atomic, the CAS retry loop, `AtomicReferenceArray`, how `incrementAndGet` works, and the ABA problem.

### `volatile` vs atomic [00:50]
- `volatile` = **visibility**. In the video's model, each CPU core has its own cache, and `volatile` forces reads and writes to go to main memory (RAM).
  - *Correction:* real CPUs keep caches coherent in hardware. What `volatile` actually guarantees is a *happens-before* ordering: a write to a volatile field is visible to any later read of it, and the compiler/CPU can't reorder around it. The "always reads RAM" picture is a useful simplification.
- Atomic variables = operations that are **never partially executed**. They are volatile internally, so you don't need to write `volatile` yourself.
- `compareAndSet(expected, new)` returns `true` if the current value equalled `expected` (and was replaced), and `false` otherwise.

### When one winner isn't enough: the retry loop [03:01]
- Seat booking: only one of T1/T2 *should* win, so a single `compareAndSet` is enough and the loser is told "already booked".
- Like counter: if T2 and T3 both like a post, the count must go 2 → 3 → 4. **Both** updates have to land, just one after the other.
- A lock would work, but the goal here is a lock-free solution.

### Building `LikeCounter` with `AtomicReference<Integer>` [04:25]
- The naive version is `totalCount.set(totalCount.get() + 1)`.
  - `get()` and `set()` are each atomic, but **the pair isn't**. Two threads can both read 0, both compute 1, and both write 1, so one like is lost.
- The retry loop [06:27]:
  1. `current = totalCount.get()` (this is the *expected* value)
  2. `next = current + 1`
  3. `if (totalCount.compareAndSet(current, next)) return;`
  4. Otherwise someone else updated the counter in between. Loop back and re-read.
- Trace: T1 reads 0 and computes 1, then gets switched out. T2 reads 0, computes 1, and its CAS(0 → 1) succeeds. T1 resumes and tries CAS(0 → 1), but the value is now 1, so it **fails**. T1 re-reads 1, computes 2, and its CAS(1 → 2) succeeds.
- The video prints "Conflict detected, retrying" on failure [12:28] so you can see the retries. It also moves the `current`/`next` declarations out of the loop to "reuse the variables".
  - *Correction:* that gives no real benefit. Local variables are just stack slots or registers, and declaring them inside the loop is cleaner.

### Running it [15:20]
- 10 threads × 1 like each gives a total of **10** every run. The number of conflicts varies (4 one run, 2 the next).
- Then 10 threads × 10 likes each gives **100** every time, however many retries happen [15:52].
- The demo waits with `Thread.sleep(3000)` in `main`. *Tip:* use `join()` on each thread instead, because a sleep is a guess.

### `AtomicReferenceArray` [17:01]
- A plain `int[]` shared between threads can have two threads overwrite the same index.
- `AtomicReferenceArray<String> arr = new AtomicReferenceArray<>(5);`
  - `arr.set(0, "Aditya")`, `arr.get(0)`
  - `arr.compareAndSet(2, "Rohan", "Sohan")`: if index 2 holds `"Rohan"`, replace it with `"Sohan"`; otherwise return `false`.
- Exercise [19:00]: extend seat booking to a row of seats. Each thread CASes `null → name` on a seat. If it loses, it moves on to the next seat.
- There are also `AtomicIntegerArray` and `AtomicLongArray` for primitive arrays.

### `AtomicInteger` does all of this for you [20:03]
- `AtomicInteger`, `AtomicLong` and `AtomicBoolean` are all built on the same CAS primitive.
- The six-line loop collapses to `totalCount.incrementAndGet()` [20:33], and it still prints 100.
- The video says `incrementAndGet` contains the full CAS + retry loop internally.
  - *Precision:* the JDK's Java-level fallback for it is indeed a CAS loop (`getAndAddInt` in `Unsafe`), but the JIT usually replaces it with one atomic fetch-and-add instruction (`LOCK XADD` on x86), so in practice there may be no retry at all. For arbitrary updates, use `updateAndGet(fn)` / `accumulateAndGet(x, fn)`, which really are CAS loops.

### Compare-and-swap as a technique [23:21]
- The usual pattern is read → modify → write. The gap between the read and the write is where race conditions come from.
- CAS: write **only if** the value is still what I originally read. Otherwise do nothing (and retry). Each attempt is atomic. Either the swap happens or it doesn't.
- The video sums it up as *compare-and-swap = `compareAndSet` + retry logic*.

### Locking vs CAS [25:07]
| | Locks (`synchronized`, `ReentrantLock`) | CAS (atomics) |
|---|---|---|
| Contention | One thread inside, others **blocked/waiting** | All threads try, one wins, others **retry** |
| Cost | Blocking, context switches | Wasted CPU on retries under heavy contention |
| Scope | Any critical section, many variables | One variable / one reference at a time |
| Safety | Simplest to get right | Tricky beyond simple cases |

### The ABA problem [26:15]
- T1 reads A, then gets context-switched out.
- T2 reads A, writes B, then writes A again, and finishes.
- T1 comes back, sees A, concludes "nothing changed", and its CAS succeeds. It has ignored the history.
- When it doesn't matter [28:58]: for a like counter, 1 → 2 → 1 really is the same state, so T1 incrementing 1 to 2 is correct.
- When it does matter: when "A" stands for something whose history matters, like a node pointer in a lock-free stack that was popped, freed or reused, and pushed back.

### Fix: versioning [29:44]
- Store a value plus a version that increments on every write: A1 → B2 → A3.
- T1 expects (A, 1), but memory holds (A, 3), so the CAS fails. T1 re-reads and retries.
- Java's built-in version is `AtomicStampedReference<V>` (value + `int` stamp). `AtomicMarkableReference<V>` is a simpler cousin that holds value + `boolean` mark.

## Code Examples

Each block is a complete program. Save it under the class's name (e.g. `CasLikeCounter.java`) and run it with `java CasLikeCounter.java`.

### Hand-written CAS retry loop (the video's `LikeCounter`)
```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class CasLikeCounter {
    private final AtomicReference<Integer> totalCount = new AtomicReference<>(0);
    private final AtomicInteger retries = new AtomicInteger(); // just to observe conflicts

    public void like() {
        while (true) {
            Integer current = totalCount.get();       // 1. read (expected value)
            Integer next = current + 1;               // 2. compute
            if (totalCount.compareAndSet(current, next)) {
                return;                               // 3. success, done
            }
            retries.incrementAndGet();                // 4. someone beat us: retry
        }
    }

    public int getTotalLikes() { return totalCount.get(); }

    public static void main(String[] args) throws InterruptedException {
        CasLikeCounter counter = new CasLikeCounter();
        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 10; t++) {
            Thread th = new Thread(() -> {
                for (int i = 0; i < 10; i++) counter.like(); // 10 likes per thread
            });
            threads.add(th);
            th.start();
        }
        for (Thread th : threads) th.join(); // wait properly instead of sleep

        System.out.println("Total likes: " + counter.getTotalLikes()); // Total likes: 100
        System.out.println("Retries: " + counter.retries.get());       // varies per run, e.g. Retries: 3
    }
}
```

### Lost updates vs `incrementAndGet` vs `updateAndGet`
```java
import java.util.concurrent.atomic.AtomicInteger;

public class CasCounterComparison {
    static volatile int unsafeCount = 0; // volatile = visibility only, NOT atomic ++
    static final AtomicInteger atomicCount = new AtomicInteger();
    static final AtomicInteger doubledThenCapped = new AtomicInteger(1);

    public static void main(String[] args) throws InterruptedException {
        Runnable work = () -> {
            for (int i = 0; i < 100_000; i++) {
                unsafeCount++;                  // read-modify-write: can lose updates
                atomicCount.incrementAndGet();  // atomic, never loses updates
            }
        };
        Thread a = new Thread(work), b = new Thread(work);
        a.start(); b.start();
        a.join(); b.join();

        System.out.println("volatile int: " + unsafeCount);       // usually < 200000, varies
        System.out.println("AtomicInteger: " + atomicCount.get()); // AtomicInteger: 200000

        // Arbitrary update functions are CAS retry loops under the hood
        Thread c = new Thread(() -> { for (int i = 0; i < 20; i++) doubledThenCapped.updateAndGet(x -> Math.min(x * 2, 1000)); });
        Thread d = new Thread(() -> { for (int i = 0; i < 20; i++) doubledThenCapped.updateAndGet(x -> Math.min(x * 2, 1000)); });
        c.start(); d.start();
        c.join(); d.join();
        System.out.println("updateAndGet result: " + doubledThenCapped.get()); // updateAndGet result: 1000

        System.out.println(atomicCount.accumulateAndGet(5, Integer::sum)); // 200005
        System.out.println(atomicCount.getAndUpdate(x -> x - 5));          // 200005 (old value)
        System.out.println(atomicCount.get());                             // 200000
    }
}
```

### `AtomicReferenceArray`: lock-free seat booking for a row of seats
```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReferenceArray;

public class CasSeatRow {
    private final AtomicReferenceArray<String> seats;

    public CasSeatRow(int n) { seats = new AtomicReferenceArray<>(n); } // all slots start null

    /** Try seats from 0 upward; return the seat booked, or -1 if the row is full. */
    public int bookAny(String person) {
        for (int i = 0; i < seats.length(); i++) {
            if (seats.compareAndSet(i, null, person)) return i; // only one thread wins seat i
        }
        return -1;
    }

    public static void main(String[] args) throws InterruptedException {
        CasSeatRow row = new CasSeatRow(3);
        String[] people = {"Aditya", "Rohit", "Rohan", "Sohan", "Mohan"};
        List<Thread> threads = new ArrayList<>();
        for (String p : people) {
            Thread t = new Thread(() -> {
                int seat = row.bookAny(p);
                System.out.println(p + (seat >= 0 ? " got seat " + seat : " found the row full"));
            });
            threads.add(t);
            t.start();
        }
        for (Thread t : threads) t.join();
        // order may vary, e.g.:
        // Aditya got seat 0
        // Rohan got seat 1
        // Rohit got seat 2
        // Sohan found the row full
        // Mohan found the row full

        int booked = 0;
        for (int i = 0; i < row.seats.length(); i++) if (row.seats.get(i) != null) booked++;
        System.out.println("Booked seats: " + booked); // Booked seats: 3

        // Per-index CAS, as in the video
        AtomicReferenceArray<String> arr = new AtomicReferenceArray<>(5);
        arr.set(0, "Aditya");
        arr.set(2, "Rohan");
        System.out.println(arr.compareAndSet(2, "Rohan", "Sohan")); // true
        System.out.println(arr.compareAndSet(2, "Rohan", "Mohan")); // false (it's Sohan now)
        System.out.println(arr);                                     // [Aditya, null, Sohan, null, null]
    }
}
```

### The ABA problem and the `AtomicStampedReference` fix
```java
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.atomic.AtomicStampedReference;

public class CasAbaDemo {
    public static void main(String[] args) throws InterruptedException {
        // --- Plain AtomicReference: fooled by A -> B -> A ---
        AtomicReference<String> ref = new AtomicReference<>("A");
        String seenByT1 = ref.get();                    // T1 reads A, then gets "switched out"

        Thread t2 = new Thread(() -> {                  // T2 runs completely
            ref.compareAndSet("A", "B");
            ref.compareAndSet("B", "A");
        });
        t2.start();
        t2.join();

        boolean plain = ref.compareAndSet(seenByT1, "X"); // T1 resumes
        System.out.println("AtomicReference CAS succeeded: " + plain); // AtomicReference CAS succeeded: true

        // --- AtomicStampedReference: value + version ---
        AtomicStampedReference<String> stamped = new AtomicStampedReference<>("A", 1);
        int[] holder = new int[1];
        String valueSeen = stamped.get(holder);          // T1 reads (A, 1)
        int versionSeen = holder[0];

        Thread t2b = new Thread(() -> {
            int v = stamped.getStamp();
            stamped.compareAndSet("A", "B", v, v + 1);     // (B, 2)
            stamped.compareAndSet("B", "A", v + 1, v + 2); // (A, 3)
        });
        t2b.start();
        t2b.join();

        boolean versioned = stamped.compareAndSet(valueSeen, "X", versionSeen, versionSeen + 1);
        System.out.println("Stamped CAS succeeded: " + versioned);  // Stamped CAS succeeded: false
        System.out.println("Now: " + stamped.getReference() + " v" + stamped.getStamp()); // Now: A v3

        // T1 re-reads and retries with the fresh version
        String v2 = stamped.get(holder);
        System.out.println(stamped.compareAndSet(v2, "X", holder[0], holder[0] + 1)); // true
        System.out.println("Now: " + stamped.getReference() + " v" + stamped.getStamp()); // Now: X v4
    }
}
```

### Pitfall: `AtomicReference` compares with `==`, not `equals`
```java
import java.util.concurrent.atomic.AtomicReference;

public class CasIdentityPitfall {
    public static void main(String[] args) {
        AtomicReference<Integer> ref = new AtomicReference<>(1000);

        // A freshly boxed 1000 is a different object from the stored one (outside the -128..127 cache)
        Integer expected = Integer.valueOf(1000);
        System.out.println(ref.compareAndSet(expected, 1001)); // false

        // Passing the exact object you read works, which is why the retry loop is fine
        Integer current = ref.get();
        System.out.println(ref.compareAndSet(current, 1001));  // true
        System.out.println(ref.get());                          // 1001
    }
}
```

## Common Pitfalls / Tips
- **`get()` then `set()` isn't atomic.** Each call is, but the gap between them is a race. Use `compareAndSet` in a loop, or a built-in like `incrementAndGet`/`updateAndGet`.
- **`volatile` doesn't make `++` safe.** It only fixes visibility.
- **Prefer the built-ins over hand-written loops.** `incrementAndGet`, `addAndGet`, `updateAndGet`, `accumulateAndGet`. Write your own loop only to learn, or when the update doesn't fit a function.
- **Keep update functions side-effect free.** `updateAndGet(fn)` may call `fn` several times under contention.
- **`AtomicReference.compareAndSet` uses identity (`==`).** With boxed `Integer`/`Long` above 127, or with strings built at runtime, always pass the exact reference you read.
- **Lock-free isn't wait-free.** Under heavy contention one thread can keep losing and retrying, and the wasted CPU adds up. For hot counters, `LongAdder` spreads the updates across cells and is much faster.
- **CAS covers one variable.** If two fields must change together, either put them in one immutable object behind an `AtomicReference`, or use a lock.
- **ABA only matters when history matters.** Counters are fine. Pointer-based structures (lock-free stacks/queues) and "state machine" values need `AtomicStampedReference`.
- **Use `join()`, not `sleep()`,** to wait for worker threads before reading results.

## Review Questions
1. What does `volatile` guarantee, and why isn't it enough for a counter?
2. Why does `totalCount.set(totalCount.get() + 1)` lose updates even though `get` and `set` are both atomic?
3. Write out the steps of a CAS retry loop. What happens to a thread whose `compareAndSet` fails?
4. How is a lock-based critical section different from a CAS-based one when many threads compete?
5. What does `arr.compareAndSet(2, "Rohan", "Sohan")` do on an `AtomicReferenceArray<String>`?
6. Describe the ABA problem with a T1/T2 timeline. Why is it harmless for a like counter?
7. How does versioning fix ABA, and which JDK class implements it?
8. Why might `ref.compareAndSet(Integer.valueOf(1000), 1001)` return `false` even when the value is 1000?

<details>
<summary>Answers</summary>

1. Visibility: a write to a volatile field is seen by later reads in other threads, and reordering around it is restricted. `count++` is still three steps (read, add, write), so two threads can read the same value and one increment gets lost.
2. Between the `get()` and the `set()`, another thread can read the same old value. Both compute the same new value and both write it, so one update disappears. Atomicity of the individual calls doesn't make the combination atomic.
3. Read the current value (the expected value), compute the new value from it, then call `compareAndSet(expected, new)`. If it returns `true`, you're done. If it returns `false`, another thread changed the value in between, so you loop: re-read the fresh value, recompute, and try again. The thread never blocks.
4. With a lock, one thread enters and the others are blocked until it leaves, so there are no retries but there is blocking and context switching. With CAS, all threads attempt the update at once, exactly one succeeds per round, and the rest retry immediately.
5. It atomically checks whether index 2 currently holds `"Rohan"`. If it does, it replaces it with `"Sohan"` and returns `true`. Otherwise it leaves the slot unchanged and returns `false`.
6. T1 reads A and gets switched out. T2 changes A → B → A. T1 resumes, sees A, and its CAS succeeds, unaware of the intermediate change. For a counter, the value 1 after 1 → 2 → 1 genuinely represents the same state, so incrementing it is still correct.
7. Pair each value with a version that increments on every write (A1 → B2 → A3). The CAS checks both the value and the version, so T1's expected (A, 1) no longer matches (A, 3) and fails. Java's class for this is `AtomicStampedReference`.
8. `AtomicReference` compares references with `==`. `Integer.valueOf(1000)` creates a new object (only −128..127 are cached), which isn't the same object as the one stored, so the comparison fails.
</details>

## Further Study
- `LongAdder` / `LongAccumulator` for high-contention counters
- `AtomicIntegerArray`, `AtomicLongArray`, `AtomicMarkableReference`
- `AtomicIntegerFieldUpdater` / `AtomicReferenceFieldUpdater`
- `VarHandle` (the modern low-level API that atomics are built on)
- Lock-free data structures: Treiber stack, Michael-Scott queue (`ConcurrentLinkedQueue`)
- Lock-free vs wait-free vs obstruction-free progress guarantees
- The Java Memory Model and *happens-before*
- Hardware CAS: `CMPXCHG`, LL/SC, false sharing and `@Contended`

---

[← #54](54-lock-free-concurrency-atomic-cas.md) · [Index](README.md) · [#56 →](56-executor-framework-future-callable.md)

---

[← #54](54-lock-free-concurrency-atomic-cas.md) · [Index](README.md) · [#56 →](56-executor-framework-future-callable.md)
