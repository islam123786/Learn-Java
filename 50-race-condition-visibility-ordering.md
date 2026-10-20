# Problems in Multithreading | Race Condition, Visibility, Ordering Explained | Java Full Course #50
> Source: https://youtu.be/lrrdN_c0HQ4 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 55:53. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- **Race condition**: the final result depends on the (unpredictable) order in which threads run. Example: two threads each doing `count++` 10,000 times usually end with less than 20,000.
- The root cause is **non-atomicity**. `count++` is really three steps (**read → increment → write**), and a context switch or a parallel thread can get in between them. **Check-then-act** (`if (balance >= amt) balance -= amt;`) has the same problem.
- **Visibility**: one thread updates a variable, but another thread keeps seeing the old value (a cached or hoisted copy). Fix it with **`volatile`** (or `synchronized`).
- **Ordering**: the compiler, JIT and CPU may **reorder** independent instructions. That is safe for single-threaded code, but another thread can observe "flag = true" before "x = 10". `volatile` and `synchronized` prevent it.
- **`volatile` fixes visibility and ordering, but not atomicity.** `synchronized` fixes all three. Atomic classes (`AtomicInteger`) and other locks are the alternatives (next videos).

## Key Concepts

**Race condition.** The program's final state depends on thread scheduling (who runs first, where the context switches fall). *Why it matters:* the result is wrong only some of the time, so it is hard to reproduce.

**Critical section.** The part of code that several threads execute on shared data, for example `increment()` doing `count++`. *Why it matters:* this is the region you must protect.

**Shared resource.** The data inside the critical section that threads share: `count`, a `List`, a `BankAccount` object. *Why it matters:* local variables are per thread, so shared heap data is where interference happens.

**Atomic vs non-atomic operation.** An atomic operation either happens completely or not at all. No thread can see it half-done, and no context switch can split it. A non-atomic operation is made of smaller steps. *Why it matters:* every race condition comes from a non-atomic operation on shared data.

**Check-then-act.** You test a condition and then act on it (`if (x) doY()`). Another thread can change the condition between the check and the act. *Why it matters:* this is the classic double-withdrawal bug that leaves a bank balance negative.

**Visibility problem.** Without synchronization, Java doesn't guarantee that a write by one thread is ever seen by another. *Why it matters:* it causes loops that never end and flags that "don't work".

**Reordering / ordering problem.** Instructions may run in a different order from the source code, as long as a single thread can't tell. *Why it matters:* other threads *can* tell.

**`volatile`.** A field modifier: every read sees the latest write, and reads and writes of it aren't reordered with the code around them. *Why it matters:* it is the lightweight fix for flags and status variables. It doesn't make `++` atomic.

**Thread interference → data inconsistency.** The umbrella term: threads interfering through shared data (via races, visibility or ordering) leave the data inconsistent. *Why it matters:* applications are built around their data, and corrupt data is the worst kind of bug.

## Detailed Notes

### Race condition [▶ ~00:41](https://youtu.be/lrrdN_c0HQ4?t=41)
- With threads T1..Tn running in parallel or concurrently, the **final state depends on the order of execution**, which is non-deterministic.
- `count++` looks like one statement but is a **risky line** [▶ ~01:42](https://youtu.be/lrrdN_c0HQ4?t=102). The CPU does:
  1. **Read** `count` from memory (into a register).
  2. **Increment** it by 1.
  3. **Write** the new value back to memory.
- Sample code: `class Counter { int count = 0; void increment() { count++; } }`, one `Counter c1`, and several threads calling `c1.increment()`.
- Step-by-step interleaving [▶ ~05:27](https://youtu.be/lrrdN_c0HQ4?t=327), with count = 0:
  | Step | T1 | T2 |
  |---|---|---|
  | 1 | read 0 | |
  | 2 | | read 0 |
  | 3 | 0 + 1 = 1 | |
  | 4 | | 0 + 1 = 1 |
  | 5 | write 1 | |
  | 6 | | write 1 (overwrites T1's write) |
  - Two increments happened, but count = **1**. That is a race condition.
- It happens in **true parallelism** too [▶ ~09:15](https://youtu.be/lrrdN_c0HQ4?t=555). Two cores read 0 at the same moment and both write 1.

### Race condition in code [▶ ~10:19](https://youtu.be/lrrdN_c0HQ4?t=619)
- One `increment()` per thread almost always prints 2, so the bug hides.
- With each thread calling `increment()` **10,000 times** [▶ ~13:46](https://youtu.be/lrrdN_c0HQ4?t=826), expected 20,000, the runs printed **12109, 19669, 11864**. Every run is different and usually less than 20,000.
- Remember to `join()` both threads before printing, or `main` may print before they have even started.

### Critical section and shared resource [▶ ~15:17](https://youtu.be/lrrdN_c0HQ4?t=917)
- **Critical section**: the risky region several threads share, here the body of `increment()`.
- **Shared resource** [▶ ~16:22](https://youtu.be/lrrdN_c0HQ4?t=982): the data inside it that threads share (`count`). It can be anything: a `List<Integer>`, a `BankAccount` object, and so on.

### The atomicity problem [▶ ~17:12](https://youtu.be/lrrdN_c0HQ4?t=1032)
- The root cause of the `count++` race: it is **non-atomic** (read, increment, write).
- **Atomic** (definition): *any operation that either happens completely or not at all*. There is no halfway point, and a thread can be switched out before it or after it, but never in the middle.
- Each of the three sub-steps on its own (read a value, add 1, write a value) is atomic. You can't "half read" `x`.
- Banking example [▶ ~21:07](https://youtu.be/lrrdN_c0HQ4?t=1267): transferring ₹500 from account A to account B means `A.balance -= 500; B.balance += 500;`. If the thread is switched out (or blocks or waits for a long time) after the debit, A has lost money and B never receives it. Operations like this must be made atomic.
- Ways to fix it [▶ ~23:26](https://youtu.be/lrrdN_c0HQ4?t=1406): **`synchronized`** blocks/methods and **atomic classes** (`AtomicInteger`, also `AtomicLong`, etc.). Internally the atomic classes use **CAS (compare-and-swap)**, which is covered later. *(The transcript says "CAS theorem". It is the compare-and-swap CPU instruction, not a theorem.)*

### Atomic vs non-atomic operations [▶ ~23:51](https://youtu.be/lrrdN_c0HQ4?t=1431)
- Always atomic:
  - A simple assignment of a literal, e.g. `int x = 2;`: one write.
  - A reference assignment, e.g. `Student s1 = new Student();`: storing the reference is one write.
  - *(Precision: the **write of the reference** is atomic, but object construction is not, which is why safe publication matters. And `x = y` reads `y` and then writes `x`, two separate atomic steps.)*
- Non-atomic:
  - `x++`, `x--`, `x + y` stored back, and compound assignments like `x = x + 5` (fetch, add, store).
  - **Check-then-act** [▶ ~25:12](https://youtu.be/lrrdN_c0HQ4?t=1512): `if (balance >= amount) balance -= amount;`
- Check-then-act walkthrough, with balance ₹1000. T1 withdraws ₹500 and T2 withdraws ₹600:
  1. T1 checks `1000 >= 500`, which passes, and is switched out right before subtracting.
  2. T2 checks `1000 >= 600`, which passes, and subtracts. Balance = ₹400.
  3. T1 resumes *inside* the `if`. Its check is now stale, and it subtracts ₹500. Balance = **−₹100**.
  - ₹1100 was paid out of a ₹1000 account, which is like a UPI app letting you overspend.
- Edge case [▶ ~29:04](https://youtu.be/lrrdN_c0HQ4?t=1744): `double d = 112.234;` may be written as **two 32-bit halves** by some JVMs, so another thread could see half of an old value and half of a new one.
  - *(Precision: JLS §17.7 allows this for non-`volatile` **`long` and `double`** fields only. On today's 64-bit JVMs these writes are atomic in practice, but the spec only guarantees it if you declare the field `volatile`.)*

### The visibility problem [▶ ~30:16](https://youtu.be/lrrdN_c0HQ4?t=1816)
- Definition: *one thread updates a variable, but another thread doesn't see the updated value*. The new value is "invisible" to it.
- Hardware picture [▶ ~31:49](https://youtu.be/lrrdN_c0HQ4?t=1909): each core has **registers** and its own **cache** (L1/L2...) in front of the shared **RAM**. RAM is slow, cache is faster and registers are faster still, so cores keep working copies.
  - `flag = false` lives in RAM. T1 (core 1) and T2 (core 2) each copy it into their own cache.
  - T1 sets `flag = true`, and it reaches RAM.
  - T2 keeps reading **its cached `false`** and never sees `true`.
  - With a single thread there is no problem, because it always sees its own writes.
- *(Precision: modern CPUs keep their caches coherent in hardware, so the usual real culprit is the **JIT compiler**. It sees that the loop body never writes `flag`, so it reads `flag` once and keeps it in a register, effectively turning the loop into `while (true)`. The video's cache model is a good mental picture, and the Java fix is the same either way. Formally, the Java Memory Model just says that without a *happens-before* relationship there is no guarantee another thread ever sees the write.)*

### Visibility in code [▶ ~35:06](https://youtu.be/lrrdN_c0HQ4?t=2106)
- `static boolean flag = false;`. T2 runs `while (!flag) { }`. T1 sleeps 1 s and then sets `flag = true`.
  - The field has to be `static` (a class field), because a lambda can only capture local variables that are effectively final.
- With `System.out.println("Thread 2 running...")` inside the loop, the program **did stop** after 1 s [▶ ~39:41](https://youtu.be/lrrdN_c0HQ4?t=2381). The video explains that `println` is internally `synchronized`, and lock acquire/release forces fresh reads from main memory.
  - *(Precision: this is a side effect, not a guarantee. The lock inside `println` is on the `PrintStream`, not on anything related to `flag`. Never rely on a print statement to fix visibility.)*
- With an **empty loop body** [▶ ~40:17](https://youtu.be/lrrdN_c0HQ4?t=2417), the program **never stops**. T2 never sees the update.
- The fix [▶ ~42:43](https://youtu.be/lrrdN_c0HQ4?t=2563) is **`static volatile boolean flag = false;`**. Reads and writes of a `volatile` variable always go to main memory (no stale copies), so T2 sees `true` and prints "Thread 2 finished".
- **`volatile` doesn't fix race conditions** [▶ ~44:07](https://youtu.be/lrrdN_c0HQ4?t=2647). `volatile int count; count++;` is still read, increment, write. It fixes *where* you read from, not threads interleaving in the middle of `++`.
- `synchronized` also fixes visibility, because locking and unlocking flush and refresh memory. But it costs more (it involves locking), so it is usually kept for atomicity problems, and `volatile` is used for plain visibility.

### The ordering (reordering) problem [▶ ~46:13](https://youtu.be/lrrdN_c0HQ4?t=2773)
- Ordering means *the sequence in which instructions are executed*.
- `int a = 1; int b = 2;` may run as "b first, then a". The **compiler/JIT** and the **CPU** reorder instructions for efficiency.
- The rule [▶ ~48:20](https://youtu.be/lrrdN_c0HQ4?t=2900): reordering must never change the result for a **single-threaded** program. Dependent statements stay in order: `int b = a + 1` can't move before `a` is assigned. Java makes no such promise for multithreaded code, because it can't predict how threads will interleave.
- Example [▶ ~49:28](https://youtu.be/lrrdN_c0HQ4?t=2968), with `int x = 0; boolean flag = false;`:
  - T1: `x = 10; flag = true;`
  - T2: `if (flag) System.out.println(x);`
  - You would expect `10` (or nothing if T2 runs first). If T1's two writes are reordered (`flag = true` first), and T2 runs right between them, T2 sees `flag == true` and prints **0**.
  - A bug like this might show up once in many runs in production and is very hard to recreate.
- Fixes: `synchronized`, or declaring the variables **`volatile`**. Volatile accesses aren't reordered around in a way that breaks this. In practice, making just `flag` volatile is enough: the write to `x` *before* the volatile write is visible to any thread that reads `flag == true`.
- Summary table:
  | Problem | `volatile` | `synchronized` | `AtomicInteger` |
  |---|---|---|---|
  | Visibility | Yes | Yes | Yes |
  | Ordering | Yes | Yes | Yes |
  | Atomicity / race on `++` | No | Yes | Yes |

### Thread interference [▶ ~53:43](https://youtu.be/lrrdN_c0HQ4?t=3223)
- Everything above is **thread interference**: threads disturbing each other's work through shared data. The causes are non-atomic operations, shared resources, race conditions, visibility and ordering.
- The result is **data inconsistency**, the most dangerous outcome, because every app (for example banking balances and user info) is built around its data.
- Solutions to come: `synchronized` blocks, `volatile`, atomic integers, and other (explicit) lock mechanisms.

## Code Examples

### Race condition on `count++`
```java
public class RaceConditionDemo {
    public static void main(String[] args) throws InterruptedException {
        Counter c1 = new Counter();
        Runnable task = () -> {
            for (int i = 1; i <= 100_000; i++) c1.increment();
        };
        Thread t1 = new Thread(task);
        Thread t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();           // wait before reading the result
        System.out.println("expected 200000, got " + c1.count);
        // The result varies between runs and is usually less than 200000, e.g.:
        // expected 200000, got 131877
    }
}

class Counter {
    int count = 0;
    void increment() { count++; }       // read -> increment -> write: NOT atomic
}
```

### `volatile` does not fix the race
```java
public class VolatileNotAtomic {
    static volatile int count = 0;      // visible to all threads... but ++ is still 3 steps

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> { for (int i = 0; i < 100_000; i++) count++; };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("volatile count = " + count);
        // The result is usually less than 200000, e.g.: volatile count = 152331
    }
}
```

### The fixes preview: `synchronized` and `AtomicInteger`
```java
import java.util.concurrent.atomic.AtomicInteger;

public class RaceFixed {
    private static int syncCount = 0;
    private static final AtomicInteger atomicCount = new AtomicInteger();

    private static synchronized void syncIncrement() { syncCount++; } // one thread at a time

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                syncIncrement();
                atomicCount.incrementAndGet();   // atomic read-modify-write (CAS)
            }
        };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("synchronized: " + syncCount);       // synchronized: 200000
        System.out.println("atomic:       " + atomicCount.get()); // atomic:       200000
    }
}
```

### Check-then-act: overdrawing a bank account
```java
public class CheckThenActDemo {
    public static void main(String[] args) throws InterruptedException {
        UnsafeAccount acc = new UnsafeAccount(1000);
        Thread t1 = new Thread(() -> acc.withdraw(500));
        Thread t2 = new Thread(() -> acc.withdraw(600));
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("final balance: " + acc.balance);
        // The tiny sleep between check and act makes the bad interleaving very likely:
        // final balance: -100
        // (in rare runs one withdrawal is correctly refused and the balance is 500 or 400)
    }
}

class UnsafeAccount {
    int balance;
    UnsafeAccount(int balance) { this.balance = balance; }

    void withdraw(int amount) {
        if (balance >= amount) {                                  // CHECK
            try { Thread.sleep(50); } catch (InterruptedException e) { } // a context switch "here"
            balance -= amount;                                    // ACT (on a stale check)
        }
    }
}
```

### Visibility: the loop that never ends, fixed with `volatile`
```java
public class VisibilityDemo {
    static volatile boolean flag = false;   // remove 'volatile' and the program may hang forever

    public static void main(String[] args) throws InterruptedException {
        Thread t2 = new Thread(() -> {
            while (!flag) {
                // do nothing (no println here: it would hide the bug)
            }
            System.out.println("Thread 2 finished");
        });
        Thread t1 = new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { }
            flag = true;
            System.out.println("Thread 1 set flag = true");
        });
        t2.start(); t1.start();
        t1.join(); t2.join();
        System.out.println("main done");
        // Thread 1 set flag = true
        // Thread 2 finished
        // main done
        // (the first two lines may swap)
    }
}
```

### Ordering: publishing data safely with a volatile flag
```java
public class OrderingDemo {
    static int x = 0;
    static volatile boolean ready = false; // volatile: the write to x cannot move after this write

    public static void main(String[] args) throws InterruptedException {
        Thread writer = new Thread(() -> {
            x = 10;        // 1
            ready = true;  // 2 (volatile write publishes everything before it)
        });
        Thread reader = new Thread(() -> {
            while (!ready) { Thread.onSpinWait(); } // wait until published
            System.out.println("x = " + x);         // guaranteed to see 10
        });
        reader.start(); writer.start();
        writer.join(); reader.join();
        // x = 10
    }
}
```

## Common Pitfalls / Tips
- **"It printed 2, so it's fine."** Races hide at small scale. Test with many iterations and many threads, and even then a passing test proves nothing.
- **Forgetting `join()` before reading results.** Otherwise you are reading while the threads are still running (or haven't started).
- **Thinking `volatile` makes `++` safe.** It doesn't. Use `synchronized`, `AtomicInteger`/`LongAdder`, or a lock for read-modify-write.
- **Debugging with `println`.** Printing involves a lock and slows threads down, which can make visibility and race bugs disappear. That is a "Heisenbug".
- **Check-then-act on shared state** (`if (!map.containsKey(k)) map.put(k, v)`, `if (balance >= amt) ...`) must be one atomic unit: lock it, or use atomic methods like `putIfAbsent` / `compareAndSet`.
- **Use `volatile` for simple flags** (stop signals, "initialized" markers) written by one thread and read by others.
- **Prefer local variables and immutable objects.** Data that isn't shared can't race.

## Review Questions
1. What is a race condition? Use `count++` with two threads to explain how one increment can get lost.
2. Why can't you get rid of the race with `count++` just by running on a multi-core CPU?
3. Define an atomic operation. Give two atomic and two non-atomic examples.
4. What is check-then-act? Walk through how a ₹1000 balance can end at −₹100.
5. What is the visibility problem, and why does adding `println` inside the loop sometimes "fix" it?
6. What does `volatile` guarantee, and what does it *not* guarantee?
7. Why does Java allow instruction reordering, and how can it break a two-thread program?
8. What is thread interference, and what does it lead to?

<details>
<summary>Answers</summary>

1. The result depends on the order threads happen to run in. `count++` is read, increment, write. T1 reads 0, T2 reads 0, both compute 1, both write 1. Two increments were done but count went up by only one.
2. With true parallelism, two cores can read the old value at exactly the same moment, so the problem is the non-atomic read-modify-write itself, not just context switching. Parallelism can actually make it worse.
3. An operation that either completes entirely or doesn't happen at all, with nothing observable in between. Atomic: `int x = 2;`, assigning a reference `s1 = obj`, a single read. Non-atomic: `x++`, `x = x + 5`, check-then-act, and (per spec) writes to non-volatile `long`/`double`.
4. You check a condition and then act on it, but the condition can change in between. T1 checks 1000 ≥ 500 and is paused. T2 checks 1000 ≥ 600 and withdraws, leaving 400. T1 resumes and withdraws 500 based on its stale check, leaving −100.
5. A thread may keep using a stale copy of a variable (cached, or kept in a register by the JIT) and never see another thread's write. `println` takes a lock and works as a side-effect memory barrier, which also stops the JIT from hoisting the read, so the loop happens to see the update. That is not a guaranteed fix.
6. It guarantees that every read sees the latest write (visibility) and that accesses aren't reordered across it (ordering, happens-before). It does not make compound actions like `count++` or check-then-act atomic.
7. Compilers, the JIT and CPUs reorder independent instructions for speed, and this is safe for single-threaded semantics. Another thread can see the reordered effects: with `x = 10; flag = true;` reordered, a reader may see `flag == true` while `x` is still 0.
8. Threads disturbing each other through shared data (races, non-atomic operations, visibility, ordering). It leads to inconsistent or corrupt data.
</details>

## Further Study
- `synchronized` methods/blocks and monitor locks (#51)
- `AtomicInteger`, `AtomicLong`, `LongAdder`, and how CAS (compare-and-swap) works
- The Java Memory Model and *happens-before* rules (JLS chapter 17)
- Explicit locks: `ReentrantLock`, `ReadWriteLock`, `StampedLock`
- Safe publication and immutable objects
- Concurrent collections: `ConcurrentHashMap`, `CopyOnWriteArrayList`
- CPU cache coherence (MESI) and false sharing

---

[← #49](49-thread-methods-sleep-join-interrupt.md) · [Index](ReadMe.md) · [#51 →](51-monitor-locks-synchronized.md)
