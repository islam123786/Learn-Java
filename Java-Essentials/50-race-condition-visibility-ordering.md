# 50 · Race Condition, Visibility & Ordering
> Full notes: [50-race-condition-visibility-ordering.md](../50-race-condition-visibility-ordering.md) · [▶ Video](https://youtu.be/lrrdN_c0HQ4)

## The big picture: thread interference → data inconsistency

When threads work on **shared data**, they can **interfere** with each other and leave the data **inconsistent** (wrong balances, lost updates). This is the main problem, and there are **three causes**:

| Problem | What goes wrong | Fix |
| :--- | :--- | :--- |
| **Atomicity** (race condition) | another thread gets in **between the steps** of an operation | `synchronized`, `AtomicInteger`, locks |
| **Visibility** | one thread **doesn't see** another thread's write | `volatile` (or `synchronized`) |
| **Ordering** | instructions run in a **different order** than written, and another thread notices | `volatile` (or `synchronized`) |

---

## Shared resource and critical section

- **Shared resource:** data that several threads read and write, e.g. `count`, a `List`, a `BankAccount` object. It lives on the **heap**. (Local variables are per thread, so they're safe.)
- **Critical section:** the **piece of code** that works on the shared resource, e.g. the body of `increment()`. This is the part you must protect.

```java
class Counter {
    int count = 0;                   // ← shared resource
    void increment() { count++; }    // ← critical section
}
```

## Race condition

**Definition:** the final result depends on the **order in which threads happen to run**, and that order is unpredictable. The result is wrong only *some* of the time, which makes it hard to reproduce.

**Why `count++` is risky:** it looks like one statement, but the CPU does **3 steps**:
1. **Read** `count` from memory
2. **Add** 1
3. **Write** it back

**Two threads, starting at count = 0:**

| Step | T1 | T2 |
|---|---|---|
| 1 | read 0 | |
| 2 | | read 0 |
| 3 | 0 + 1 = 1 | |
| 4 | | 0 + 1 = 1 |
| 5 | write 1 | |
| 6 | | write 1 ← overwrites T1's write |

Two increments happened, but the result is **1**. One update was lost. This happens with a context switch on one core **and** with two cores running at the same time.

**In code:** two threads each calling `increment()` 10,000 times should give 20,000. Real runs gave **12109, 19669, 11864**, a different result each time.

## The atomicity problem

**The root cause of every race condition** is that the operation on shared data is **non-atomic**.

**Atomic:** an operation that happens **completely or not at all**. No thread can see it half-done, and a context switch can happen before it or after it, but **never in the middle**.

**Bank transfer example:** sending ₹500 from A to B is `A.balance -= 500; B.balance += 500;`. If the thread is switched out after the first line, A has lost ₹500 and B never got it. The two lines must act as **one atomic unit**.

## Atomic vs non-atomic operations

| ✅ Atomic | ❌ Non-atomic |
| :--- | :--- |
| `int x = 2;` (one write) | `x++`, `x--` (read → change → write) |
| `s1 = new Student();` (the **reference write** is atomic) | `x = x + 5`, `x += 5` |
| reading or writing an `int`, `boolean` or reference | **check-then-act**: `if (balance >= amt) balance -= amt;` |
| reading or writing a **`volatile`** `long`/`double` | reading or writing a **non-volatile** `long`/`double` (it may be split into two 32-bit halves) |

- Each single step (one read, one add, one write) is atomic. The **combination** isn't.
- `s1 = new Student()`: storing the reference is atomic, but *building the object* isn't.
- **`long` and `double`:** the Java spec allows a non-volatile 64-bit write to be done as two 32-bit halves, so another thread could see half of an old value and half of a new one. On modern 64-bit JVMs these writes are atomic in practice, but only `volatile` *guarantees* it.

### Check-then-act: the overdraft bug
Balance = ₹1000. T1 withdraws ₹500 and T2 withdraws ₹600, both using `if (balance >= amt) balance -= amt;`:
1. T1 checks `1000 >= 500`, which passes, and is **switched out** before subtracting.
2. T2 checks `1000 >= 600`, which passes, and subtracts. Balance = ₹400.
3. T1 resumes **inside the `if`**. Its check is now **stale**, but it subtracts ₹500 anyway. Balance = **−₹100**.

₹1100 was paid out of a ₹1000 account.

### ✅ Solution for atomicity: `synchronized` or atomic classes
```java
import java.util.concurrent.atomic.AtomicInteger;

public class RaceFixed {
    static int unsafeCount = 0;
    static int syncCount = 0;
    static final AtomicInteger atomicCount = new AtomicInteger();

    static synchronized void syncIncrement() { syncCount++; }   // only one thread at a time

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                unsafeCount++;                     // ❌ race condition
                syncIncrement();                   // ✅ synchronized
                atomicCount.incrementAndGet();     // ✅ atomic (uses CAS)
            }
        };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();                      // always join before reading the result
        System.out.println("unsafe:       " + unsafeCount);       // usually < 200000, e.g. 137512
        System.out.println("synchronized: " + syncCount);         // 200000
        System.out.println("atomic:       " + atomicCount.get()); // 200000
    }
}
```
- **`synchronized`** lets only **one thread at a time** into the critical section, so the 3 steps can't be split. This also fixes check-then-act if the check and the act are in the same `synchronized` block.
- **`AtomicInteger`** (and `AtomicLong`, …) does read-modify-write as **one atomic step**, using the CPU's **CAS (compare-and-swap)** instruction (#54).

---

## The visibility problem

**Definition:** one thread **updates** a variable, but another thread **keeps seeing the old value**. The new value is "invisible" to it.

**Why it happens:**
```
   Core 1 (T1)          Core 2 (T2)
  ┌────────────┐       ┌────────────┐
  │ registers  │       │ registers  │
  │ cache      │       │ cache      │  ← T2 keeps reading its old copy: flag = false
  └─────┬──────┘       └─────┬──────┘
        └──────── RAM ───────┘           flag = true  (written by T1)
```
- Each core keeps **working copies** in its registers and cache, because RAM is slow. T1 writes `flag = true`, but T2 keeps using its old `false`.
- In practice, the usual culprit is the **JIT compiler**. It sees that the loop never changes `flag`, so it reads `flag` **once** and turns `while (!flag) {}` into `while (true) {}`.
- With a single thread there's no problem, because a thread always sees its own writes.

**Example:** T2 runs `while (!flag) { }`, and T1 sets `flag = true` after 1 s. With an empty loop body, **the program never stops**.
- With a `println` inside the loop it *does* stop, because `println` happens to use a lock. That's a side effect, though. **Never rely on it.**

### ✅ Solution for visibility: `volatile`
```java
public class VisibilityDemo {
    static volatile boolean flag = false;   // remove 'volatile' and the program may hang forever

    public static void main(String[] args) throws InterruptedException {
        Thread t2 = new Thread(() -> {
            while (!flag) { }               // keeps re-reading flag from main memory
            System.out.println("Thread 2 saw flag = true");
        });
        Thread t1 = new Thread(() -> {
            try { Thread.sleep(500); } catch (InterruptedException e) { }
            flag = true;
        });
        t2.start(); t1.start();
        t1.join(); t2.join();
        System.out.println("main done");
        // Thread 2 saw flag = true
        // main done
    }
}
```
- **`volatile`:** every read gets the **latest** value, and every write is visible to all threads straight away.
- **`synchronized`** also fixes visibility (taking and releasing a lock refreshes memory), but it costs more. Use `volatile` for simple flags.
- ⚠️ **`volatile` doesn't fix race conditions.** `volatile int count; count++;` is still read → add → write, and threads can still get in between the steps.

---

## The ordering (reordering) problem

**Definition:** the **compiler, JIT or CPU** may run instructions in a **different order** than you wrote them, to go faster.

**The rule:** reordering must never change the result for a **single thread**. Dependent lines stay in order (`b = a + 1` can't move before `a` is set). But Java makes **no such promise for other threads**.

**Example:** `int x = 0; boolean flag = false;`
```
T1:  x = 10;            T2:  if (flag)
     flag = true;                System.out.println(x);
```
- **Expected:** T2 prints `10`, or nothing.
- **What can happen:** T1's two lines get **reordered** to `flag = true; x = 10;`. If T2 runs in between, it sees `flag == true` and prints **0**.
- A bug like this can show up once in many runs in production and is very hard to reproduce.

### ✅ Solution for ordering: `volatile` (or `synchronized`)
```java
public class OrderingDemo {
    static int x = 0;
    static volatile boolean ready = false;   // volatile: x = 10 can't move after this write

    public static void main(String[] args) throws InterruptedException {
        Thread writer = new Thread(() -> {
            x = 10;          // 1
            ready = true;    // 2  volatile write: everything before it is published
        });
        Thread reader = new Thread(() -> {
            while (!ready) { }                  // wait until published
            System.out.println("x = " + x);     // guaranteed to be 10
        });
        reader.start(); writer.start();
        writer.join(); reader.join();
        // x = 10
    }
}
```
- Making just the **flag** `volatile` is enough. Every write that comes **before** a volatile write is visible to any thread that **reads** the volatile variable and sees the new value.

---

## Summary: which fix solves which problem

| Problem | `volatile` | `synchronized` | `AtomicInteger` |
| :--- | :--- | :--- | :--- |
| Visibility | ✅ | ✅ | ✅ |
| Ordering | ✅ | ✅ | ✅ |
| Atomicity (race on `++`, check-then-act) | ❌ | ✅ | ✅ (for single-variable updates) |

**Remember:** shared data plus a non-atomic operation gives a **race condition**. `volatile` fixes **visibility** and **ordering**, but **not atomicity**. `synchronized` fixes all three. `AtomicInteger` fixes single-variable updates without a lock.
