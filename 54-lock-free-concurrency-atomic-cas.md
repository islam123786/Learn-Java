# Lock-Free Concurrency in Java | AtomicVariables & CAS Explained | Java Full Course #54
> Source: https://youtu.be/ujF2gNsCfBE · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 49:06. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `count++` is **not atomic**. It is three steps (read, add 1, write back), and two threads can interleave between them. That is the race condition behind the classic "expected 20000, got 13 thousand-something" counter.
- Locks (`synchronized`, `ReentrantLock`, …) fix it but cost something: threads block, context switches happen, code slows down. For simple shared values, Java offers **lock-free** alternatives in `java.util.concurrent.atomic`.
- `AtomicInteger`, `AtomicLong`, `AtomicBoolean` and `AtomicReference<V>` give you single operations that are atomic and visible to all threads: `incrementAndGet`, `getAndAdd`, `compareAndSet`, … No `volatile` needed.
- **Compare-And-Set (CAS)** is the core primitive. `compareAndSet(expected, newValue)` sets the new value **only if** the current value is still `expected`, and tells you whether it worked, all as one indivisible step. It turns a racy "check-then-act" into a safe one.
- Atomicity covers **one** call. Two atomic calls in a row (`if (x.get() > 4) x.incrementAndGet();`) are not atomic together.
- CAS stays correct even when threads run truly in parallel on different cores, because the CPU itself provides it as a single atomic instruction (`LOCK CMPXCHG` on x86).

## Key Concepts

**Atomic operation.** An operation that runs "all or nothing". No other thread can see it half done, and it can't be split by a context switch in a way that matters. *Why it matters:* race conditions come from non-atomic read-modify-write sequences. Make the sequence atomic and the race is gone.

**Non-atomic `count++`.** It compiles to read → increment → write. If T1 and T2 both read `0`, both write `1`, one increment is lost. *Why it matters:* this is the textbook lost-update bug.

**Lock-free concurrency.** Getting correct results under concurrency without making threads wait on a lock. It works for specific, narrow cases (a counter, a flag, swapping one reference), not for arbitrary blocks of code. *Why it matters:* it avoids the overhead of locking (blocking, context switches) where the problem is small enough.

**`AtomicInteger` / `AtomicLong`.** Wrapper objects around an `int`/`long` with atomic methods: `get`, `set`, `incrementAndGet` (like `++x`), `getAndIncrement` (like `x++`), the `decrement` pair, `addAndGet(n)` / `getAndAdd(n)`, and `compareAndSet`. `AtomicLong` is the same with a bigger range. *Why it matters:* thread-safe counters and unique-ID generators with no locks.

**`AtomicBoolean`.** A shared flag with atomic reads, writes and `compareAndSet`. *Why it matters:* "has this been done yet?" or "is the buffer full?" flags shared between threads, without `volatile` or `synchronized`.

**`AtomicReference<V>`.** The same idea for an object reference: you can atomically swap which object it points to. *Why it matters:* lets you update shared objects (a seat owner, a config object) without locks.

**Compare-And-Set (CAS).** `compareAndSet(expected, update)`: "if the current value is `expected`, replace it with `update` and return `true`; otherwise change nothing and return `false`." The whole thing is one atomic step. *Why it matters:* it is the building block for all lock-free algorithms, including the atomic classes themselves.

**Check-then-act race.** The pattern `if (condition on shared state) { change shared state }`. Two threads can both pass the check before either acts. *Why it matters:* it causes double bookings, double initialization, counters going over a limit. CAS fuses the check and the act.

## Detailed Notes

### Recap: why `count++` races [▶ ~00:00](https://youtu.be/ujF2gNsCfBE?t=0)
- Continuing the multithreading series. The previous lecture covered locks; this one is about concurrency *without* locks.
- Example: `class Counter { int count = 0; void increment() { count++; } }`. Two threads each call `increment()` many times. The final count should be the sum, but it usually comes out lower.
- `count++` is really three steps:
  1. read `count` from memory into a temporary,
  2. add 1 to the temporary,
  3. store the temporary back.
- A thread can be switched out between any of these steps. If T1 and T2 both read `0`, both compute `1` and both store `1`, then two increments produced a count of 1. This is the **race condition**, and its root cause is that the operation is **non-atomic**.

### Locking vs lock-free [▶ ~03:00](https://youtu.be/ujF2gNsCfBE?t=180)
- One fix: treat `increment()` as a critical section and lock it, with `synchronized` (the monitor lock) or one of the explicit locks from the last video (`ReentrantLock`, `StampedLock`, …).
- Locking always works, but it has overhead and makes the code slower.
- Java also offers **lock-free concurrency**: you get the correct result without locking. It only works for specific use cases, and the first tool for it is **atomic variables**.

### What "atomic" means [▶ ~04:12](https://youtu.be/ujF2gNsCfBE?t=252)
- An atomic operation finishes as one unit: either the thread performs all of it or none of it. You can't observe it half done.
- Example from the video: `int x = 4;` is a single step. A thread can't be switched out after creating `x` but before storing 4. *(Precisely: in Java, reads and writes of `int`, references and other primitives except non-volatile `long`/`double` are atomic. Compound actions like `x++` or `x += 4` are not.)*
- Java's atomic classes: `AtomicInteger` (most common), `AtomicLong`, `AtomicBoolean`, `AtomicReference`.

### Demo: the racy counter [▶ ~05:56](https://youtu.be/ujF2gNsCfBE?t=356)
- `Demo.java`: a `Counter` with `int count`, two threads each calling `increment()` 10,000 times. The main thread sleeps 2 seconds, then prints `count`.
- Expected 20,000. Actual runs gave about 13,24x and about 6,800 less than expected. It changes every run, because the number of lost updates is random.
- *(Tip: use `t1.join(); t2.join();` instead of `Thread.sleep(2000)`. Join waits exactly as long as needed and also guarantees the main thread sees the threads' writes.)*

### Fix with `AtomicInteger` [▶ ~09:19](https://youtu.be/ujF2gNsCfBE?t=559)
- Replace the field with `AtomicInteger count = new AtomicInteger(0);` (package `java.util.concurrent.atomic`).
- Replace `count++` with `count.incrementAndGet()`. That is one atomic operation: increment, then return the new value (like `++count`). `getAndIncrement()` is the `count++` version, returning the old value.
- The result is **exactly 20,000 on every run** [▶ ~10:39](https://youtu.be/ujF2gNsCfBE?t=639), with no lock on the method.

### Concurrent vs parallel: how can it be safe on multiple cores? [▶ ~12:00](https://youtu.be/ujF2gNsCfBE?t=720)
- **Concurrent** (one core, threads take turns): it's easy to see why it works. The thread finishes `incrementAndGet` before being switched out.
- **Parallel** (T1 and T2 on different cores at the same instant): both could, in theory, read `0` at the same moment. Yet the answer is still 20,000 on a multi-core machine.
- The reason is that atomic classes are built on **CAS (Compare-And-Set / Compare-And-Swap)** [▶ ~13:30](https://youtu.be/ujF2gNsCfBE?t=810). The transcript renders it as "cache"; it means CAS. The deep dive comes at the end of the video.

### `AtomicInteger` methods [▶ ~14:14](https://youtu.be/ujF2gNsCfBE?t=854)
| Method | Meaning | Like |
|---|---|---|
| `get()` | read the current value | `x` |
| `set(v)` | write a value | `x = v` |
| `incrementAndGet()` | add 1, return new | `++x` |
| `getAndIncrement()` | return old, then add 1 | `x++` |
| `decrementAndGet()` | subtract 1, return new | `--x` |
| `getAndDecrement()` | return old, then subtract 1 | `x--` |
| `addAndGet(n)` | add n, return new | `x += n` (then read) |
| `getAndAdd(n)` | return old, then add n | |
| `compareAndSet(e, n)` | set to n only if current == e | (see below) |

These cover most real usage, and `AtomicLong` / `AtomicBoolean` have the matching ones.

### `AtomicLong` and `AtomicBoolean` [▶ ~16:22](https://youtu.be/ujF2gNsCfBE?t=982)
- **`AtomicLong`**: same API as `AtomicInteger` with the `long` range. Good for **unique ID generation**: IDs can be large, and many threads can call `incrementAndGet()` without ever getting the same ID.
- **`AtomicBoolean`** [▶ ~17:00](https://youtu.be/ujF2gNsCfBE?t=1020): for a boolean shared between threads. Example: the `flag` from the producer-consumer lecture ("is the shared slot full or empty?") could be an `AtomicBoolean`.
- With atomic classes you **don't need `volatile`**. They already guarantee visibility (every thread sees the latest value) as well as atomicity. *(You still want the field holding the atomic object to be `final`, so the reference itself never changes.)*

### Why not `synchronized` everywhere? [▶ ~18:38](https://youtu.be/ujF2gNsCfBE?t=1118)
- `synchronized` uses locking, which has a big overhead and slows everything down.
- In specialized cases where you don't actually need a lock, use the non-locking mechanism instead.

### Atomicity doesn't compose [▶ ~19:04](https://youtu.be/ujF2gNsCfBE?t=1144)
- Each atomic method is atomic by itself. **Two atomic calls in a row are not atomic together.**
- `if (count.get() > 4) count.incrementAndGet();` is **not** thread-safe. Two threads can both pass the `get()` check before either increments.
- Atomicity makes one statement indivisible. It doesn't remove races *between* statements. *(The fix is a CAS retry loop or `updateAndGet` / `accumulateAndGet`, which run your whole update function atomically. See Code Examples. The next video covers CAS retry loops.)*

### `AtomicReference` and the seat-booking problem [▶ ~20:19](https://youtu.be/ujF2gNsCfBE?t=1219)
- References can be atomic too. Normally `Student s1 = new Student();` puts the object on the heap and the reference `s1` on the stack. `AtomicReference<Student>` makes *changing which object the reference points to* atomic.
- **Example: booking a seat** [▶ ~21:09](https://youtu.be/ujF2gNsCfBE?t=1269) (like BookMyShow or District), simplified to a single seat:
  ```
  String seat = "EMPTY";
  boolean bookSeat(String person) {
      if (seat.equals("EMPTY")) { seat = person; return true; }
      return false;
  }
  ```
- The bug: T1 ("Rahul") and T2 ("Rohan") both check `seat.equals("EMPTY")` at the same time, both see `true`, and both enter the `if`. One write overwrites the other, so the seat ends up with only one person, but **both get `true`**. Both think they've booked. This is the **multiple / double seat booking problem** [▶ ~26:15](https://youtu.be/ujF2gNsCfBE?t=1575).
- The root cause is **check-then-act**: compare first, act afterwards, with a gap in between.
- A lock would fix it, but a lock-free fix exists, so use that.

### Fixing it with `compareAndSet` [▶ ~27:40](https://youtu.be/ujF2gNsCfBE?t=1660)
- Demo: `SeatBooking` with threads booking for "Aditya" and "Rohit". With the plain-`String` version, the final seat is either name, and the race is still there.
- The lock-free version [▶ ~31:00](https://youtu.be/ujF2gNsCfBE?t=1860):
  ```
  AtomicReference<String> seat = new AtomicReference<>("EMPTY");
  boolean bookSeat(String name) {
      String current = seat.get();
      if (!current.equals("EMPTY")) return false;   // fast path: already booked
      return seat.compareAndSet("EMPTY", name);     // atomic check + act
  }
  ```
- `compareAndSet(expectedValue, newValue)`: if the seat currently holds `expectedValue`, put `newValue` in it and return `true`. Otherwise do nothing and return `false`. The old `if` plus assignment is now **one atomic line**.

### Dry run [▶ ~35:29](https://youtu.be/ujF2gNsCfBE?t=2129)
- **Concurrent case:** both threads read `current = "EMPTY"` (each into its own stack variable) and both pass the early check. T1 reaches CAS first: the seat is `"EMPTY"`, so it becomes `"Aditya"` and T1 gets `true`. T2's CAS then sees `"Aditya"`, which isn't `"EMPTY"`, so nothing changes and T2 gets `false`. There's no visibility problem, because atomic references always show the latest value.
- **Parallel case** [▶ ~39:07](https://youtu.be/ujF2gNsCfBE?t=2347): even if both threads issue CAS at the same instant on different cores, one of them executes first and the other sees the updated value. Both can never succeed.
- Running it [▶ ~40:30](https://youtu.be/ujF2gNsCfBE?t=2430): printing each thread's return value gives `T1 says false, T2 says true` on one run and the reverse on another. **There is always exactly one `true`.** A later caller fails at the early `equals("EMPTY")` check.

### Why CAS can't run "simultaneously": the hardware view [▶ ~41:56](https://youtu.be/ujF2gNsCfBE?t=2516)
- **Lock signal** [▶ ~42:42](https://youtu.be/ujF2gNsCfBE?t=2562): when the CPU runs CAS, it uses a special processor instruction. On x86 that's `CMPXCHG` with a **`LOCK` prefix**. That isn't a thread-level lock. It tells the system bus and memory controller "I'm doing an atomic operation, everyone else wait", and the other processors are held off for that tiny moment.
- **Memory controller** [▶ ~45:21](https://youtu.be/ujF2gNsCfBE?t=2721): even if two requests arrive at the "same" time, the memory controller is a physical device built from flip-flops and logic gates. It can only let one signal through at a time, and a gate is either open or closed, never half open for each request. One request always wins, even by picoseconds.
- **Buzzer analogy** [▶ ~46:47](https://youtu.be/ujF2gNsCfBE?t=2807): two kids hit a quiz buzzer at "the same time", but one signal reaches the board first. Wire lengths differ by tiny amounts, and at nearly light speed that's enough to break the tie.
- Conclusion: Java doesn't handle parallel execution here itself. The **CPU guarantees** that atomic instructions are serialized, which is why we can trust `compareAndSet` and the classes built on it.
- *(Correction/precision: on modern multi-core x86 CPUs, `LOCK CMPXCHG` usually doesn't lock the whole bus. The core takes **exclusive ownership of the cache line** through the cache-coherence protocol (MESI), so other cores can't read or write that line until the instruction completes. Full bus locking is a legacy fallback for unusual cases, such as data spanning two cache lines. The idea is the same: the hardware serializes the atomic operation. Also, `AtomicInteger.incrementAndGet()` on x86 compiles to `LOCK XADD`, a hardware fetch-and-add, rather than a CAS loop. ARM uses load-linked/store-conditional or LSE atomics instead.)*

## Code Examples

Each block is a complete program: save it under the class name (e.g. `AfAtomicCounter.java`) and run it with `java AfAtomicCounter.java`.

### The race: plain `int` counter
```java
public class AfRaceCounter {
    public static void main(String[] args) throws InterruptedException {
        AfPlainCounter counter = new AfPlainCounter();
        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) counter.increment();
        };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join(); // better than Thread.sleep(2000)

        System.out.println("Expected: 200000");
        System.out.println("Actual:   " + counter.count);
        // Expected: 200000
        // Actual:   137452   <- varies on every run, usually below 200000 (lost updates)
    }
}

class AfPlainCounter {
    int count = 0;
    void increment() { count++; } // read, +1, write: NOT atomic
}
```

### The fix: `AtomicInteger`
```java
import java.util.concurrent.atomic.AtomicInteger;

public class AfAtomicCounter {
    private final AtomicInteger count = new AtomicInteger(0);

    void increment() { count.incrementAndGet(); } // one atomic step, no lock

    public static void main(String[] args) throws InterruptedException {
        AfAtomicCounter counter = new AfAtomicCounter();
        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) counter.increment();
        };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();

        System.out.println(counter.count.get()); // 200000 (every run)
    }
}
```

### Tour of `AtomicInteger` methods
```java
import java.util.concurrent.atomic.AtomicInteger;

public class AfAtomicMethods {
    public static void main(String[] args) {
        AtomicInteger x = new AtomicInteger(4);

        System.out.println(x.get());               // 4
        x.set(10);
        System.out.println(x.incrementAndGet());   // 11  (like ++x)
        System.out.println(x.getAndIncrement());   // 11  (like x++), x is now 12
        System.out.println(x.decrementAndGet());   // 11  (like --x)
        System.out.println(x.getAndDecrement());   // 11  (like x--), x is now 10
        System.out.println(x.addAndGet(4));        // 14
        System.out.println(x.getAndAdd(4));        // 14, x is now 18

        System.out.println(x.compareAndSet(18, 100)); // true  (was 18 -> now 100)
        System.out.println(x.compareAndSet(18, 200)); // false (is 100, not 18)
        System.out.println(x.get());                   // 100

        // Atomic read-modify-write with your own function (Java 8+)
        System.out.println(x.updateAndGet(v -> v * 2));            // 200
        System.out.println(x.accumulateAndGet(5, Math::max));      // 200
    }
}
```

### `AtomicLong` as a unique ID generator
```java
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class AfIdGenerator {
    private static final AtomicLong NEXT_ID = new AtomicLong(1000);

    static long nextId() { return NEXT_ID.incrementAndGet(); }

    public static void main(String[] args) throws InterruptedException {
        Set<Long> ids = ConcurrentHashMap.newKeySet();
        Thread[] threads = new Thread[4];
        for (int t = 0; t < threads.length; t++) {
            threads[t] = new Thread(() -> {
                for (int i = 0; i < 1000; i++) ids.add(nextId());
            });
            threads[t].start();
        }
        for (Thread t : threads) t.join();

        System.out.println("IDs generated: " + ids.size()); // IDs generated: 4000 (no duplicates)
        System.out.println("Last ID: " + NEXT_ID.get());    // Last ID: 5000
    }
}
```

### `AtomicBoolean`: run initialization exactly once
```java
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class AfAtomicBooleanFlag {
    private static final AtomicBoolean initialized = new AtomicBoolean(false);
    private static final AtomicInteger initCount = new AtomicInteger();

    static void initOnce() {
        // Only the first thread to flip false -> true does the work.
        if (initialized.compareAndSet(false, true)) {
            initCount.incrementAndGet();
            System.out.println("Initialized by " + Thread.currentThread().getName());
        }
    }

    public static void main(String[] args) throws InterruptedException {
        Thread[] threads = new Thread[5];
        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(AfAtomicBooleanFlag::initOnce, "T" + (i + 1));
            threads[i].start();
        }
        for (Thread t : threads) t.join();

        System.out.println("Init ran " + initCount.get() + " time(s)");
        // Initialized by T1   <- which thread wins may vary
        // Init ran 1 time(s)  <- always 1
    }
}
```

### Atomicity doesn't compose: check-then-act on an `AtomicInteger`
```java
import java.util.concurrent.atomic.AtomicInteger;

public class AfCheckThenAct {
    static final int LIMIT = 100;

    // BROKEN: two atomic calls are not atomic together.
    static void brokenIncrement(AtomicInteger c) {
        if (c.get() < LIMIT) {
            Thread.yield();          // widen the gap so the race shows up
            c.incrementAndGet();
        }
    }

    // CORRECT: the whole "check + update" runs atomically (CAS retry loop inside).
    static void safeIncrement(AtomicInteger c) {
        c.updateAndGet(v -> v < LIMIT ? v + 1 : v);
    }

    static int run(boolean safe) throws InterruptedException {
        AtomicInteger c = new AtomicInteger();
        Thread[] ts = new Thread[8];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new Thread(() -> {
                for (int j = 0; j < 1000; j++) {
                    if (safe) safeIncrement(c); else brokenIncrement(c);
                }
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        return c.get();
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("broken: " + run(false)); // broken: 103  <- often above 100, varies
        System.out.println("safe:   " + run(true));  // safe:   100  (always)
    }
}
```

### Double booking with a plain `String` (the bug)
```java
public class AfSeatBookingBroken {
    String seat = "EMPTY";

    boolean bookSeat(String person) {
        if (seat.equals("EMPTY")) {                      // check
            try { Thread.sleep(10); } catch (InterruptedException e) { }
            seat = person;                               // ...then act
            return true;
        }
        return false;
    }

    public static void main(String[] args) throws InterruptedException {
        AfSeatBookingBroken sb = new AfSeatBookingBroken();
        Thread t1 = new Thread(() -> System.out.println("Rahul booked? " + sb.bookSeat("Rahul")));
        Thread t2 = new Thread(() -> System.out.println("Rohan booked? " + sb.bookSeat("Rohan")));
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("Seat owner: " + sb.seat);
        // order may vary, sample:
        // Rahul booked? true
        // Rohan booked? true     <- both told "booked"!
        // Seat owner: Rohan      <- but only one actually has it
    }
}
```

### Lock-free seat booking with `AtomicReference.compareAndSet`
```java
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class AfSeatBooking {
    static final String EMPTY = "EMPTY";
    final AtomicReference<String> seat = new AtomicReference<>(EMPTY);

    boolean bookSeat(String name) {
        String current = seat.get();
        if (!current.equals(EMPTY)) return false;   // already booked: fail fast
        return seat.compareAndSet(EMPTY, name);     // atomic check + set
    }

    public static void main(String[] args) throws InterruptedException {
        AfSeatBooking sb = new AfSeatBooking();
        AtomicInteger successes = new AtomicInteger();
        String[] people = {"Aditya", "Rohit", "Rahul", "Rohan", "Priya", "Neha"};
        Thread[] ts = new Thread[people.length];
        for (int i = 0; i < people.length; i++) {
            String p = people[i];
            ts[i] = new Thread(() -> {
                boolean ok = sb.bookSeat(p);
                if (ok) successes.incrementAndGet();
                System.out.println(p + " says " + ok);
            });
            ts[i].start();
        }
        for (Thread t : ts) t.join();

        System.out.println("Successful bookings: " + successes.get());
        System.out.println("Seat booked: " + !sb.seat.get().equals(EMPTY));
        // order may vary, sample:
        // Aditya says true
        // Rohit says false
        // ... (all others false)
        // Successful bookings: 1           <- always exactly 1
        // Seat booked: true
    }
}
```

### Gotcha: `AtomicReference` CAS compares with `==`, not `equals`
```java
import java.util.concurrent.atomic.AtomicReference;

public class AfReferenceIdentity {
    public static void main(String[] args) {
        // A *different* String object that happens to contain "EMPTY"
        AtomicReference<String> seat = new AtomicReference<>(new String("EMPTY"));

        System.out.println(seat.get().equals("EMPTY"));         // true  (same content)
        System.out.println(seat.compareAndSet("EMPTY", "Aditya")); // false (different object!)
        System.out.println(seat.get());                          // EMPTY

        // Fix: compare against the exact object currently stored
        String current = seat.get();
        System.out.println(seat.compareAndSet(current, "Aditya")); // true
        System.out.println(seat.get());                           // Aditya
    }
}
```

## Common Pitfalls / Tips
- **`count++`, `x += n` and `if (...) x = ...` on shared fields are never atomic**, even on `volatile` fields. `volatile` gives visibility, not atomicity.
- **Atomicity doesn't compose.** `if (a.get() > 4) a.incrementAndGet();` still races. Use `compareAndSet`, `updateAndGet` or `accumulateAndGet` so that the check and the update happen as one step.
- **`AtomicReference.compareAndSet` uses reference identity (`==`).** It works with the `"EMPTY"` literal only because string literals are interned to the same object. If the stored value came from `new String("EMPTY")`, user input or a database, CAS against a literal fails. Use a shared constant, or CAS against the value you just read with `get()`. The same goes for boxed `Integer`s outside the -128..127 cache in `AtomicReference<Integer>`. Use `AtomicInteger` for numbers.
- **The early `equals` check in `bookSeat` is only an optimization.** The `compareAndSet` alone is what guarantees correctness.
- **No `volatile` needed on atomic values, but make the field `final`** (`private final AtomicInteger count = ...`). Reassigning the atomic object itself would bring the race back.
- **Use `join()`, not `Thread.sleep()`**, to wait for threads in demos and tests.
- **Lock-free is not always faster.** Under heavy contention, CAS retries spin and burn CPU. For hot counters updated by many threads, `LongAdder` is usually faster than `AtomicLong`. Locks are still the right tool when an update spans several variables or needs to block or wait.
- **Lock-free still involves a hardware "lock".** The `LOCK` prefix or cache-line ownership just never puts a *thread* to sleep. That is the difference that matters.

## Review Questions
1. Why does `count++` produce lost updates with two threads? Describe the interleaving.
2. What does it mean for an operation to be atomic? Give an atomic and a non-atomic example in Java.
3. What is the difference between `incrementAndGet()` and `getAndIncrement()`?
4. Why don't you need `volatile` on an `AtomicBoolean` flag?
5. Why is `if (count.get() > 4) count.incrementAndGet();` not thread-safe, even though each call is atomic? How would you fix it?
6. Explain the double seat-booking bug and how `compareAndSet` fixes it.
7. How can CAS be safe when two threads run it at the same instant on different cores?
8. Why might `seat.compareAndSet("EMPTY", name)` fail even though `seat.get().equals("EMPTY")` is `true`?

<details>
<summary>Answers</summary>

1. `count++` is read, add 1, write. T1 reads 0, T2 reads 0, T1 writes 1, T2 writes 1. Two increments happened, but the value only went up by one.
2. Atomic means all or nothing: no thread can observe it half done. `int x = 4;` (a single write) is atomic. `x++` (read-modify-write) is not.
3. `incrementAndGet()` adds 1 and returns the **new** value (like `++x`). `getAndIncrement()` returns the **old** value and then adds 1 (like `x++`). Both are atomic.
4. Atomic classes already guarantee visibility. Their reads and writes have `volatile` semantics, so every thread sees the latest value.
5. Another thread can change the value between `get()` and `incrementAndGet()`. Both threads pass the check and both increment. Fix it with one atomic update: `count.updateAndGet(v -> v > 4 ? v + 1 : v)`, or a `compareAndSet` retry loop.
6. Two threads both see the seat as `"EMPTY"`, both assign their name and both return `true`. Only one name survives, but both users are told they're booked. `seat.compareAndSet("EMPTY", name)` does the check and the assignment as one atomic step: only the first thread finds `"EMPTY"`, and the rest get `false`.
7. CAS maps to a single atomic CPU instruction (`LOCK CMPXCHG` on x86). The hardware serializes it: the core takes exclusive ownership of the cache line (or, in the video's explanation, the memory controller lets one signal through at a time), so one thread always goes first and the other sees the updated value.
8. `compareAndSet` compares references with `==`. If the stored string is a different object with the same content (e.g. `new String("EMPTY")`), the identity check fails.
</details>

## Further Study
- CAS retry loops and the ABA problem (the next video), plus `AtomicStampedReference` / `AtomicMarkableReference`
- `LongAdder` / `LongAccumulator` for high-contention counters
- `AtomicIntegerArray`, `AtomicReferenceArray`, `AtomicIntegerFieldUpdater`
- `VarHandle` (Java 9+), the modern API under the atomic classes
- The Java Memory Model: happens-before, `volatile` semantics
- Cache coherence (MESI) and how `LOCK`-prefixed instructions work
- Lock-free data structures: `ConcurrentLinkedQueue`, `ConcurrentHashMap` internals
- Lock-free vs wait-free vs obstruction-free progress guarantees

---

[← #53](53-java-locks-reentrant-readwrite-stamped-semaphore.md) · [Index](ReadMe.md) · [#55 →](55-cas-retry-loop-aba-problem.md)
