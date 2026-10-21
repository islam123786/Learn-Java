# 51 · Monitor Locks & `synchronized`
> Full notes: [51-monitor-locks-synchronized.md](../51-monitor-locks-synchronized.md) · [▶ Video](https://youtu.be/k9sURAu0xT8)

## In one look
- **`synchronized`** lets **only one thread at a time** into a critical section. The other threads wait in **BLOCKED** until the lock is free.
- It gives **atomicity, visibility and ordering** for the code **inside** it. Code outside still runs in parallel.
- **The lock belongs to an object, not to a method.** Every Java object has one built-in **monitor (intrinsic) lock**.

| You write | Lock used | Blocks… |
| :--- | :--- | :--- |
| `synchronized void m()` | `this` (the object) | every other `synchronized` instance method/block on **the same object** |
| `synchronized (this) { … }` | `this` | same as above |
| `synchronized (lock1) { … }` | `lock1` | only code that also locks `lock1` |
| `static synchronized void m()` | `ClassName.class` | every other code that locks the class |
| `synchronized (ClassName.class) { … }` | `ClassName.class` | same as above |
| `synchronized (new Object()) { … }` | a **new** lock every time | **nothing** (no protection) |

---

## Recap: the critical section
- **Critical section:** code that only one thread at a time should run, e.g. `Counter.increment()` doing `count++`.
- **Shared resource:** `count`.
- `count++` is non-atomic (read → increment → write), so several threads inside it at once cause a **race condition** (#50).

## Adding `synchronized`
- **The demo from #50:** one `Counter c1`, with T1 and T2 each calling `c1.increment()` 10,000 times, then `join` both and print. The result is **less than 20,000** (e.g. 12,016).
- **Fix:** write `synchronized void increment()`. Now it prints **20,000 on every run**. It can never be lower, and it can't be higher either.

## How it works, step by step
1. T1 enters the synchronized method and **acquires the lock**.
2. T2 tries to get the same lock and goes into **BLOCKED**.
3. T1 does the work (read → increment → write). **Even if T1 is context-switched out halfway** (say, right after the read), T2 still can't get in, because T1 still **owns the lock**. It's like T1 locking the house and stepping out for a moment: nobody else gets in.
4. When T1 comes back and **exits**, the lock is released. T2 moves **BLOCKED → RUNNABLE**, runs, and acquires the lock itself. A T3 would then have to wait for T2.

```
T1: ──[acquire]──read──(switched out)──inc──write──[release]──────────────────────
T2: ─────────[try → BLOCKED ................................]──[acquire]──read──inc──write──[release]
```

- **Why it's called "synchronized":** it turns unpredictable, overlapping access into an orderly, **one-at-a-time** sequence, so the threads are *in sync*.

## Demo: `show()` with sleep
```java
class Test {
    synchronized void show() {
        System.out.println(Thread.currentThread().getName() + " inside show");
        try { Thread.sleep(2000); } catch (InterruptedException e) { }
        System.out.println(Thread.currentThread().getName() + " show finished");
    }
}
```
Two threads call `test.show()` on the **same object**.

| Without `synchronized` | With `synchronized` |
| :--- | :--- |
| Thread-0 inside show | Thread-0 inside show |
| Thread-1 inside show | Thread-0 show finished |
| Thread-0 show finished | Thread-1 inside show |
| Thread-1 show finished | Thread-1 show finished |

- **`sleep()` inside a synchronized method keeps the lock.** The other thread waits for the full 2 s.

## Why we need `synchronized`
1. **Protect shared data:** only one thread can modify it at a time.
2. **Make any operation atomic:** to other threads, the whole block runs as one unit.
3. **Visibility:** changes made inside are seen by the **next thread that takes the same lock**, because it reads fresh values from main memory.
4. **Prevent reordering** around the critical section.

⚠️ **Common confusion:** only the **synchronized region** is exclusive. Code outside it still runs concurrently or in parallel in all threads.

---

## Monitor locks (object locks)
- The mechanism behind `synchronized` is called a **monitor lock**, also known as an **object lock** or **intrinsic lock**.
- **A lock is like a permission token.**
  - A thread asks for it before entering the critical section.
  - It gets the token only if nobody else holds it. Otherwise it goes to **BLOCKED**.
  - When the holder leaves, a blocked thread becomes **RUNNABLE**, but it still has to **acquire the lock itself** before entering.
- Java also has more advanced, **explicit locks** (#53).

### `synchronized` locks an object, not a method
- **Every Java object has its own built-in lock**, managed by the JVM, which decides when it's acquired and released.
- `synchronized void increment1()`, `increment2()` and `increment3()` in the same class **all use the lock of `this`**. While T1 is in **any** of them on object `c1`, T2 can't enter **any** of them on `c1`.

## Demo: two synchronized methods, one object
```java
public class OneObjectOneLock {
    public static void main(String[] args) throws InterruptedException {
        Shared test = new Shared();
        Thread t1 = new Thread(test::m1);       // T1 → m1()
        Thread t2 = new Thread(test::m2);       // T2 → m2() on the SAME object
        t1.start(); t2.start();
        t1.join(); t2.join();
        // Never interleaved: one method finishes completely before the other starts.
        // m1 entered
        // m1 exit
        // m2 entered
        // m2 exit      (or m2 goes first)
    }
}

class Shared {
    synchronized void m1() { System.out.println("m1 entered"); pause(); System.out.println("m1 exit"); }
    synchronized void m2() { System.out.println("m2 entered"); pause(); System.out.println("m2 exit"); }

    static void pause() {
        try { Thread.sleep(200); } catch (InterruptedException e) { }   // sleep keeps the lock
    }
}
```
- **One object = one lock.** `m1` and `m2` are different methods, but they share the **same lock**, so whoever holds it blocks every synchronized method of that object.
- **Two objects = two locks.** With `test1` and `test2`, T1 can run `test1.m1()` while T2 runs `test2.m1()` **at the same time**, because they acquire different locks.

```java
public class TwoObjectsTwoLocks {
    public static void main(String[] args) throws InterruptedException {
        Printer p1 = new Printer(), p2 = new Printer();       // two objects → two locks
        long start = System.currentTimeMillis();
        Thread t1 = new Thread(p1::work);
        Thread t2 = new Thread(p2::work);
        t1.start(); t2.start();
        t1.join(); t2.join();
        long ms = System.currentTimeMillis() - start;
        System.out.println("ran in parallel: " + (ms < 380));  // ran in parallel: true
        // both 200 ms tasks overlap, so the total is about 200 ms, not 400 ms
    }
}

class Printer {
    synchronized void work() {
        try { Thread.sleep(200); } catch (InterruptedException e) { }
    }
}
```

---

## Synchronized blocks
- Often only a **small part** of a method is critical. Just as Java has a `static` block, it has a **synchronized block**:
  ```java
  synchronized (this) { count++; }    // the parentheses take the object whose lock you want
  ```
- Moving only `count++` into `synchronized (this)` (with the method itself **not** synchronized) still gives **20,000**.
- **Why bother:** every lock acquire and release has **overhead**, and while one thread holds the lock, the others wait. **The smaller the locked region, the better.**

```java
public class SyncCounterDemo {
    public static void main(String[] args) throws InterruptedException {
        SyncCounter c1 = new SyncCounter();
        Runnable task = () -> {
            for (int i = 1; i <= 10_000; i++) { c1.incrementMethod(); c1.incrementBlock(); }
        };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("method-synced: " + c1.a);   // method-synced: 20000
        System.out.println("block-synced:  " + c1.b);   // block-synced:  20000
    }
}

class SyncCounter {
    int a = 0, b = 0;

    synchronized void incrementMethod() { a++; }      // locks 'this' for the WHOLE method

    void incrementBlock() {
        // ... non-critical code can run here without the lock ...
        synchronized (this) {                          // lock ONLY the critical part
            b++;
        }
    }
}
```

## Custom lock objects
**The problem:** a `Bank` with `synchronized deposit()` and `synchronized withdraw()` (each sleeping 2 s). With one `Bank b1`, **a deposit blocks a withdraw**, because both lock `this`, even though they're unrelated.

**The fix:** every object has a lock, so create **dedicated lock objects**:
```java
public class CustomLocksDemo {
    public static void main(String[] args) throws InterruptedException {
        Bank b1 = new Bank();
        Thread t1 = new Thread(b1::deposit, "T1");
        Thread t2 = new Thread(b1::withdraw, "T2");
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("deposits=" + b1.deposits + " withdrawals=" + b1.withdrawals);
        // The two operations overlap (both "start" lines come before the "end" lines):
        // T1 deposit start
        // T2 withdraw start
        // T1 deposit end
        // T2 withdraw end      (the two "end" lines may swap)
        // deposits=1 withdrawals=1
    }
}

class Bank {
    private final Object depositLock = new Object();    // custom lock 1
    private final Object withdrawLock = new Object();   // custom lock 2
    int deposits = 0, withdrawals = 0;                  // independent data, one lock each

    void deposit() {
        synchronized (depositLock) {
            System.out.println(Thread.currentThread().getName() + " deposit start");
            sleep();
            deposits++;
            System.out.println(Thread.currentThread().getName() + " deposit end");
        }
    }

    void withdraw() {
        synchronized (withdrawLock) {
            System.out.println(Thread.currentThread().getName() + " withdraw start");
            sleep();
            withdrawals++;
            System.out.println(Thread.currentThread().getName() + " withdraw end");
        }
    }

    private static void sleep() {
        try { Thread.sleep(200); } catch (InterruptedException e) { }
    }
}
```
- **Result:** a deposit and a withdraw **can run in parallel**, but two deposits (or two withdraws) still **can't**, so each operation is still protected against itself.
- ⚠️ **Caution:** this is only correct if deposit and withdraw **don't touch the same data**. If both update **one `balance`**, they **must share one lock**, or the race comes back. Use separate locks only for truly independent state.
- Make lock fields **`private final`**, so outside code can't use them and they can't be reassigned.

## Interview question: `synchronized (new Object())`
- With `synchronized (this)` or `synchronized (lock1)`, one thread at a time gets in, as expected.
- With `synchronized (new Object()) { … }`, every entry creates a **new object with a new lock**. Every thread gets its **own** lock, so they **all enter together**. It's the same as **no synchronization at all**.

```java
public class NewObjectLockBug {
    static int count = 0;

    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 0; i < 100_000; i++) {
                synchronized (new Object()) {   // a brand-new lock every time = no mutual exclusion
                    count++;
                }
            }
        };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("count = " + count);   // usually less than 200000, as if there were no lock
    }
}
```

---

## Static synchronization (the class-level lock)
- `static` members belong to the **class**. Take `static int count` with `static synchronized void increment()`, called as `Counter.increment()` from two threads, where each sleeps 2 s and then prints the count. It prints **1**, then after a gap **2**. So it's exclusive.
- **No object is involved, so what's locked?** The **class**, or more precisely its **`Class` object** (`Counter.class`). These two are **equivalent**:
  ```java
  static synchronized void increment() { … }

  static void increment() {
      synchronized (Counter.class) { … }
  }
  ```
- **`this` isn't allowed** in a static context.
- **One class has one class-level lock**, and **each object has its own object-level lock**.

### Class lock vs object lock: different locks
- In `Test`, make `m1()` **static** with `synchronized (Test.class)` and keep `m2()` with `synchronized (this)`. T1 calls `Test.m1()` and T2 calls `test.m2()`.
- **Both enter at the same time.** One locks the **class** and the other locks the **instance**, and those are different locks.
- Calling the static method through an instance (`test.m1()`) only gives a **warning**. It still locks `Test.class`.

```java
public class StaticSyncDemo {
    public static void main(String[] args) throws InterruptedException {
        // 1) static synchronized: exclusive across all callers (lock = StaticCounter.class)
        Runnable task = () -> { for (int i = 0; i < 10_000; i++) StaticCounter.increment(); };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("static count = " + StaticCounter.count);   // static count = 20000

        // 2) the class lock and the object lock are different, so they overlap
        StaticCounter obj = new StaticCounter();
        long start = System.currentTimeMillis();
        Thread a = new Thread(StaticCounter::classLocked);   // locks StaticCounter.class
        Thread b = new Thread(obj::objectLocked);            // locks obj
        a.start(); b.start();
        a.join(); b.join();
        System.out.println("overlapped: " + (System.currentTimeMillis() - start < 380)); // overlapped: true
    }
}

class StaticCounter {
    static int count = 0;

    static synchronized void increment() { count++; }   // same as synchronized (StaticCounter.class) { … }

    static void classLocked() {
        synchronized (StaticCounter.class) { pause(); }
    }

    synchronized void objectLocked() { pause(); }       // lock = this

    static void pause() {
        try { Thread.sleep(200); } catch (InterruptedException e) { }
    }
}
```

---

## How locks work internally (conceptual)
Picture each object's lock as a small record:
```
lock {
    ownerThread  = null
    isLocked     = false
    waitingQueue = []
}
```
Acquiring the lock, in pseudocode:
```
if (!obj.lock.isLocked) {
    obj.lock.isLocked = true;
    obj.lock.owner    = currentThread;
} else {
    obj.lock.waitingQueue.add(currentThread);   // → BLOCKED
}
```
- **Isn't that check-then-act?** Two threads could both see `isLocked == false`. The JVM makes acquiring the lock **atomic** using **CAS (compare-and-swap)**: a **single CPU instruction** that sets a value **only if it still has the expected value**. (The transcript says "cash", but it means CAS.) CAS is covered in #54.
- **In HotSpot (the standard JVM):** the lock state lives in the object header's **mark word** and is taken with CAS. When there's contention, the lock is **inflated** into a full, OS-backed monitor.
- **Monitors are reentrant.** A thread that **already holds** a lock can enter another `synchronized` section on the **same object** without blocking itself:

```java
public class ReentrancyDemo {
    public static void main(String[] args) {
        new ReentrancyDemo().outer();
    }

    synchronized void outer() {
        System.out.println("in outer, holds lock: " + Thread.holdsLock(this)); // in outer, holds lock: true
        inner();   // same lock again: no self-deadlock, because monitors are reentrant
    }

    synchronized void inner() {
        System.out.println("in inner too");   // in inner too
    }
}
```

## Cost and alternatives
- Overusing `synchronized` adds **overhead** and makes programs **slower**: threads queue up, and parallelism is lost.
- In production, people often prefer better tools: **explicit locks** (#53) and **optimistic locking** (#54), which doesn't actually lock and is faster when there's little contention.

---

## Pitfalls and tips
- **Locking different objects by accident.** Each `new Counter()` has its own lock, so synchronizing on `this` **won't protect a `static` field** shared by all instances. Use a static lock or `static synchronized` for static data.
- **`synchronized (new Object())`**, or a lock field that gets **reassigned**, gives no protection. Make lock fields `private final`.
- **Mixing a class lock and an object lock for the same data.** A `static synchronized` method and a `synchronized` instance method **don't exclude each other**. Protect each piece of data with **exactly one** lock.
- **Splitting locks for shared data.** Separate deposit and withdraw locks are only safe if they don't touch the same balance.
- **Slow work while holding a lock** (sleep, I/O, network calls) makes every other thread wait. `sleep()` **keeps** the lock. Keep synchronized regions small.
- **Prefer a private lock object** over `synchronized (this)` in library code, so outside code can't lock your object and stall you.
- **Don't synchronize on strings or boxed values** (`"lock"`, `Integer`). Interning and caching mean unrelated code may share the same object.
- **Exceptions release the lock automatically.** This is an advantage over manual `lock()`/`unlock()`, where you have to use `finally`.
- **Reads need the lock too**, not only writes. Otherwise you lose the visibility guarantee.

## Quick self-test
1. **Why does `synchronized increment()` always give 20,000?** Only one thread at a time holds `c1`'s lock, so each read-increment-write runs as one unit and no increment is lost.
2. **T1 is switched out inside a synchronized method. Can T2 enter?** No. T1 still owns the lock, and T2 stays **BLOCKED** until T1 exits.
3. **Can `obj.m1()` and `obj.m2()` (both synchronized) run at the same time?** Not on the same object, because both use `this`. On **two different objects**, yes.
4. **What are the 4 guarantees of `synchronized`?** Protecting shared data, atomicity, visibility (for the next thread to take the same lock) and no harmful reordering.
5. **Block vs method?** A block locks only the critical lines, which means less waiting, and lets you choose the lock object.
6. **Why use separate `lock1`/`lock2`, and when is it dangerous?** So independent sections don't block each other. It's dangerous if both touch the same data.
7. **What does `synchronized (new Object())` do?** It creates a new lock every time, so it gives no exclusion at all.
8. **What does `static synchronized` lock, and does it block instance-synchronized methods?** It locks `ClassName.class`. It doesn't block them, because they use a different lock.

**Remember:** a lock belongs to an **object**. One object has one lock, so all of its synchronized methods block each other. `static synchronized` locks `ClassName.class`, a **separate** lock. Use **blocks** with **`private final` lock objects** to lock as little as possible, and never write `synchronized (new Object())`.
