# Monitor Locks in Java | Synchronized Keyword, Static Sync & Custom Locks | Java Full Course #51
> Source: https://youtu.be/k9sURAu0xT8 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 47:57. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Putting **`synchronized`** on `increment()` fixes the `count++` race from #50. Two threads × 10,000 increments now always give **20,000**. Only one thread at a time can be inside a synchronized section. The others go to **BLOCKED** until the lock is released, even if the lock holder is switched out or sleeps.
- `synchronized` gives **atomicity, visibility and ordering** for the code inside it. It doesn't make the rest of the program synchronized.
- The lock belongs to an **object**, not to a method. Every Java object has one built-in **monitor (intrinsic) lock**. Synchronized instance methods lock `this`, so **all** of them on the same object exclude each other. Different objects have different locks and don't block each other.
- A **synchronized block** `synchronized (obj) { ... }` locks only the critical part, and lets you choose the lock object. Separate **custom lock objects** (`private final Object lock1 = new Object()`) let independent operations (deposit vs withdraw) run in parallel. `synchronized (new Object())` locks nothing.
- **Static synchronized** methods lock the **`Class` object** (`Counter.class`). That is a different lock from any instance's lock, so a static and an instance synchronized method can run at the same time.

## Key Concepts

**`synchronized` method.** Only one thread at a time may execute it *for a given lock*. Instance methods use `this` as the lock and static methods use `ClassName.class`. *Why it matters:* it is the simplest way to make a critical section atomic.

**Monitor lock (intrinsic lock / object lock).** A lock that comes with every Java object and is managed by the JVM. A thread entering `synchronized` acquires it and leaving releases it, even when leaving with an exception. *Why it matters:* knowing *which object* is locked tells you exactly which threads block each other.

**BLOCKED state.** A thread that tries to acquire a monitor someone else holds waits in BLOCKED and goes back to RUNNABLE when the lock is freed. *Why it matters:* it is the lifecycle state from #48, now in practice.

**Synchronized block.** `synchronized (lockObj) { ... }` protects only part of a method, with an explicit lock object. *Why it matters:* less code under the lock means less waiting and better performance.

**Custom lock objects.** Plain `Object` instances used only as locks. *Why it matters:* they let you separate unrelated critical sections and keep the lock private so outside code can't interfere.

**Static synchronization (class-level lock).** `static synchronized` (or `synchronized (X.class)`) uses the single lock of the `Class` object. *Why it matters:* it protects static, shared-by-all data. It is independent of instance locks.

**Cost of synchronization.** Acquiring and releasing locks, and making threads wait, add overhead and reduce parallelism. *Why it matters:* lock only what you need. Later videos cover alternatives (atomics/CAS, optimistic locking, explicit locks).

## Detailed Notes

### Recap: the critical section [00:38]
- Critical section: code that only one thread at a time should enter, for example `Counter.increment()` doing `count++`. The shared resource is `count`.
- `count++` is non-atomic (read, increment, write), so several threads inside it at once cause a race condition.

### Adding `synchronized` [02:04]
- The demo from #50: one `Counter c1`, T1 and T2 each calling `c1.increment()` 10,000 times, `join` both, print. The result is less than 20,000 (e.g. 12,016).
- Writing `synchronized void increment()` [03:12] makes it print **20,000 every run**. It can never be lower, and it can't be higher either.

### How it works step by step [05:06]
1. T1 enters the synchronized method and **acquires the lock**.
2. T2 tries to acquire the same lock and goes to the **BLOCKED** state [05:21].
3. T1 performs the task (read, increment, write). Even if T1 is **context switched out** halfway (for example after reading), T2 still can't enter, because T1 still owns the lock. It's like T1 locking a house and stepping out: nobody else gets in.
4. When T1 comes back it finishes, and as soon as T1 **exits** it releases the lock. T2 moves BLOCKED → RUNNABLE, then runs and acquires the lock itself, so a T3 would have to wait for it.
- The name comes from this: it turns non-deterministic, asynchronous access into an orderly, *in sync*, one-at-a-time sequence.

### Demo: `show()` with sleep [08:51]
- `class Test { void show() { print(name + " inside show"); sleep 2 s; print(name + " show finished"); } }`, with two threads calling `test.show()` on the same object.
- Without `synchronized`, both print "inside show" first, then both print "finished".
- With `synchronized`, Thread-0 does inside then finished, and only then does Thread-1 do inside then finished. Sleeping inside a synchronized method **keeps the lock**.

### Why we need `synchronized` [11:57]
1. To **protect shared data**.
2. To **make any operation atomic**. The whole block runs as one unit from other threads' point of view.
3. To **ensure visibility**. Changes made inside are seen by the next thread that takes the same lock (fresh reads from main memory).
4. To **prevent reordering** around the critical section.
- Common confusion [13:26]: only the **synchronized region** is exclusive. Code outside it still runs concurrently or in parallel in all threads.

### Monitor locks / object locks [13:57]
- The mechanism behind `synchronized` is called **monitor locks**, also known as object locks.
- A lock is like a **permission token** [14:21]. A thread asks for it before entering the critical section and gets it only if nobody else holds it. Otherwise it goes to BLOCKED. When the holder leaves, a blocked thread becomes RUNNABLE and then has to acquire the lock itself.
- Java also has more advanced locks (explicit locks, later videos).
- **`synchronized` locks an object, not a method** [16:09]:
  - **Every Java object has an inherent (intrinsic) lock**, maintained by the JVM, which decides when it is acquired and released.
  - `synchronized void increment1()`, `increment2()` and `increment3()` in the same class all use the lock of `this`. If T1 is in any of them on object `c1`, T2 can't enter **any** of them on `c1`.

### Demo: two synchronized methods, one object [17:43]
- `Test` has `synchronized m1()` and `synchronized m2()`, each printing entered, sleeping 2 s, and printing exit. T1 calls `test.m1()` and T2 calls `test.m2()`.
- Output: m1 entered → m1 exit → m2 entered → m2 exit (or m2 first). They are different methods, but they share the **same lock**.
- **One object = one lock** [21:08]. Whoever holds it blocks every synchronized method of that object.
- **Two objects = two locks** [21:40]. With `test1` and `test2`, T1 can run `test1.m1()` while T2 runs `test2.m1()` at the same time, because they acquire different locks.

### Synchronized blocks [22:50]
- Often only a small part of a method is critical. Like a `static` block, Java has a **synchronized block**: `synchronized (this) { count++; }`, where the parentheses take the **object whose lock** you want.
- Moving only `count++` into `synchronized (this)` (with the method itself not synchronized) still gives 20,000.
- Why bother: every lock acquire/release has **overhead**, so the smaller the locked region, the better.

### Custom lock objects [25:00]
- Problem [26:11]: a `Bank` with `synchronized deposit()` and `synchronized withdraw()` (each sleeping 2 s). With one `Bank b1`, a deposit blocks a withdraw, because both lock `this`.
- Since every object has a lock, create dedicated lock objects [28:17]:
  - `Object lock1 = new Object();` and `Object lock2 = new Object();`
  - `deposit()` uses `synchronized (lock1) { ... }` and `withdraw()` uses `synchronized (lock2) { ... }`.
- Result: a deposit and a withdraw **can run in parallel**, but two deposits (or two withdraws) still can't. Each operation is still protected against itself.
  - *(Caution: this is only correct if deposit and withdraw don't touch the **same** data. If both update one `balance`, they have to share one lock, or the race comes back. Use separate locks only for truly independent state.)*

### Interview question: `synchronized (new Object())` [30:39]
- Using `this` or `lock1` in `m1()` lets one thread in at a time, as expected.
- `synchronized (new Object()) { ... }` [32:30] creates a **new object, with a new lock, on every entry**. Every thread gets its own lock, so all of them enter together. It is the same as having no synchronization at all.

### Static synchronization [33:49]
- `static` members belong to the class. For `static synchronized void increment()` with `static int count`, called as `Counter.increment()` from two threads (each sleeping 2 s and then printing the count): it prints 1, then after a gap prints 2. It is exclusive.
- There's no object, so what is locked? The **class**, or precisely its `Class` object. These two are equivalent [37:45]:
  - `static synchronized void increment() { ... }`
  - `static void increment() { synchronized (Counter.class) { ... } }`
  - `this` is not allowed in a static context.
- **Class lock vs object lock** [39:04]: in `Test`, make `m1()` static with `synchronized (Test.class)` and keep `m2()` with `synchronized (this)`. T1 calls `Test.m1()` and T2 calls `test.m2()`. Both enter **at the same time**, because one locks the class and the other locks the instance, which are different locks. Calling the static method through an instance (`test.m1()`) gives only a warning and still locks `Test.class`.
- One class has one class-level lock, and each object has one object-level lock.

### How locks work internally (conceptual) [41:37]
- Picture each object's lock as a small record:
  ```
  lock { ownerThread = null, isLocked = false, waitingQueue = [] }
  ```
- Acquiring the lock (pseudocode): `if (!obj.lock.isLocked) { obj.lock.isLocked = true; obj.lock.owner = currentThread; } else { obj.lock.waitingQueue.add(currentThread); /* BLOCKED */ }`
- Isn't that **check-then-act** [45:00]? Two threads could both see `isLocked == false`. The JVM makes this acquisition **atomic** using **CAS (compare-and-swap)**, a single CPU instruction that sets the value only if it still has the expected one. *(The transcript says "cash/kaash". It is CAS.)* More about CAS later.
- *(Precision: in HotSpot the lock state lives in the object header's "mark word" and is acquired via CAS, inflating to a full OS-backed monitor under contention. Also note that Java monitors are **reentrant**: a thread already holding a lock can enter another `synchronized` section on the same object without blocking itself.)*

### Cost and alternatives [47:16]
- Overusing `synchronized` creates **overhead** and makes programs **slower** (threads queue up, and parallelism is lost).
- In production, people often prefer better mechanisms such as explicit locks and **optimistic locking**, which doesn't actually lock and is faster under low contention. These come in the next videos.

## Code Examples

### Fixing the counter with a synchronized method or block
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
        System.out.println("method-synced: " + c1.a); // method-synced: 20000
        System.out.println("block-synced:  " + c1.b); // block-synced:  20000
    }
}

class SyncCounter {
    int a = 0, b = 0;

    synchronized void incrementMethod() { a++; }   // locks 'this' for the whole method

    void incrementBlock() {
        // ... non-critical code could run here without the lock ...
        synchronized (this) {                       // lock only the critical part
            b++;
        }
    }
}
```

### One object = one lock: two different synchronized methods still exclude each other
```java
public class OneObjectOneLock {
    public static void main(String[] args) throws InterruptedException {
        Shared test = new Shared();
        Thread t1 = new Thread(test::m1);
        Thread t2 = new Thread(test::m2);
        t1.start(); t2.start();
        t1.join(); t2.join();
        // Never interleaved: one method fully finishes before the other starts.
        // Which goes first varies, e.g.:
        // m1 entered
        // m1 exit
        // m2 entered
        // m2 exit
    }
}

class Shared {
    synchronized void m1() { System.out.println("m1 entered"); pause(); System.out.println("m1 exit"); }
    synchronized void m2() { System.out.println("m2 entered"); pause(); System.out.println("m2 exit"); }

    static void pause() {
        try { Thread.sleep(200); } catch (InterruptedException e) { } // sleep keeps the lock
    }
}
```

### Two objects = two locks: no blocking between them
```java
public class TwoObjectsTwoLocks {
    public static void main(String[] args) throws InterruptedException {
        Printer p1 = new Printer(), p2 = new Printer();
        long start = System.currentTimeMillis();
        Thread t1 = new Thread(p1::work);
        Thread t2 = new Thread(p2::work);
        t1.start(); t2.start();
        t1.join(); t2.join();
        long ms = System.currentTimeMillis() - start;
        System.out.println("ran in parallel: " + (ms < 380)); // ran in parallel: true
        // (both 200 ms tasks overlap, so the total is ~200 ms rather than ~400 ms)
    }
}

class Printer {
    synchronized void work() {
        try { Thread.sleep(200); } catch (InterruptedException e) { }
    }
}
```

### Custom lock objects: deposit and withdraw in parallel
```java
public class CustomLocksDemo {
    public static void main(String[] args) throws InterruptedException {
        Bank b1 = new Bank();
        Thread t1 = new Thread(b1::deposit, "T1");
        Thread t2 = new Thread(b1::withdraw, "T2");
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("deposits=" + b1.deposits + " withdrawals=" + b1.withdrawals); // deposits=1 withdrawals=1
        // Both "start" lines usually appear before either "end" line (they overlap), e.g.:
        // T1 deposit start
        // T2 withdraw start
        // T1 deposit end
        // T2 withdraw end
        // deposits=1 withdrawals=1
    }
}

class Bank {
    private final Object depositLock = new Object();   // custom lock #1
    private final Object withdrawLock = new Object();  // custom lock #2
    int deposits = 0, withdrawals = 0;                 // independent data, one lock each

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

### `synchronized (new Object())` locks nothing
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
        System.out.println("count = " + count);
        // The result is usually less than 200000, just like with no lock at all
    }
}
```

### Static synchronization: class lock vs object lock
```java
public class StaticSyncDemo {
    public static void main(String[] args) throws InterruptedException {
        // 1) static synchronized: exclusive across all callers (lock = StaticCounter.class)
        Runnable task = () -> { for (int i = 0; i < 10_000; i++) StaticCounter.increment(); };
        Thread t1 = new Thread(task), t2 = new Thread(task);
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("static count = " + StaticCounter.count); // static count = 20000

        // 2) class lock and object lock are different locks -> they overlap
        StaticCounter obj = new StaticCounter();
        long start = System.currentTimeMillis();
        Thread a = new Thread(StaticCounter::classLocked);  // locks StaticCounter.class
        Thread b = new Thread(obj::objectLocked);           // locks obj
        a.start(); b.start();
        a.join(); b.join();
        System.out.println("overlapped: " + (System.currentTimeMillis() - start < 380)); // overlapped: true
    }
}

class StaticCounter {
    static int count = 0;

    static synchronized void increment() { count++; }  // same as synchronized (StaticCounter.class) { ... }

    static void classLocked() {
        synchronized (StaticCounter.class) { pause(); }
    }

    synchronized void objectLocked() { pause(); }      // lock = this

    static void pause() {
        try { Thread.sleep(200); } catch (InterruptedException e) { }
    }
}
```

### Reentrancy: a thread can re-acquire a lock it already holds
```java
public class ReentrancyDemo {
    public static void main(String[] args) {
        new ReentrancyDemo().outer();
    }

    synchronized void outer() {
        System.out.println("in outer, holds lock: " + Thread.holdsLock(this)); // in outer, holds lock: true
        inner(); // same lock again: no self-deadlock, monitors are reentrant
    }

    synchronized void inner() {
        System.out.println("in inner too"); // in inner too
    }
}
```

## Common Pitfalls / Tips
- **Locking different objects by accident.** Each `new Counter()` has its own lock, so synchronizing on `this` won't protect a `static` field shared by all instances. Use a static lock or `static synchronized` for static data.
- **`synchronized (new Object())`** (or a lock field that gets reassigned) gives no protection at all. Make lock fields `private final`.
- **Mixing a class lock and an object lock for the same data.** A `static synchronized` method and a `synchronized` instance method don't exclude each other. Protect each piece of data with exactly one lock.
- **Splitting locks for data that is shared.** Separate locks for deposit/withdraw are only safe if they don't touch the same balance.
- **Doing slow work while holding a lock** (sleeping, I/O, network calls) makes every other thread wait. `sleep()` keeps the lock. Keep synchronized regions small.
- **Prefer a private lock object** over `synchronized (this)` in library code, so outside code can't lock your object and stall you.
- **Don't synchronize on strings or boxed values** (`"lock"`, `Integer`). Interning and caching mean unrelated code may share the same object.
- **Exceptions release the lock automatically**, which is one advantage over manual `lock()`/`unlock()`, where you must use `finally`.
- **Every read of shared data needs the lock too**, not only the writes, or you lose the visibility guarantee.

## Review Questions
1. Why does adding `synchronized` to `increment()` always produce 20,000?
2. If T1 is inside a synchronized method and gets context switched out, can T2 enter? What state is T2 in?
3. A class has two synchronized instance methods `m1` and `m2`. Can T1 run `obj.m1()` while T2 runs `obj.m2()`? What if the calls are on two different objects?
4. What four guarantees does `synchronized` provide?
5. What is the advantage of a synchronized block over a synchronized method?
6. Why would you create separate lock objects like `lock1` and `lock2`? When is that dangerous?
7. What happens with `synchronized (new Object()) { ... }`?
8. What does a `static synchronized` method lock on? Can it run at the same time as a `synchronized` instance method of the same class?

<details>
<summary>Answers</summary>

1. Only one thread at a time can hold the lock on `c1`, so each read-increment-write runs as a unit and no increment is lost.
2. No. T1 still owns the lock while it is switched out. T2 stays BLOCKED until T1 finishes the synchronized section and releases the lock.
3. On the same object, no: both methods lock the same `this`, so one waits for the other. On two different objects, yes: each object has its own lock.
4. Protecting shared data (mutual exclusion), atomicity of the whole section, visibility of changes to the next thread that takes the same lock, and no harmful reordering across the section.
5. It locks only the critical lines (less waiting, less overhead), and you can choose which object's lock to use.
6. So that independent critical sections (e.g. deposit vs withdraw) don't block each other while each is still protected against itself. It is dangerous if the sections touch the same data, because then two threads could modify it at once.
7. Every execution creates a new object with a new lock, so every thread "acquires" its own lock and nobody is excluded. It behaves exactly like having no synchronization.
8. The class's `Class` object (e.g. `Counter.class`). Yes. The instance method locks the object, which is a different lock, so the two can run at the same time.
</details>

## Further Study
- `wait()`, `notify()`, `notifyAll()`: they must be called while holding the monitor
- Explicit locks: `ReentrantLock`, `tryLock`, fairness, `ReadWriteLock`, `StampedLock`
- CAS (compare-and-swap), `AtomicInteger`, and optimistic locking
- Deadlock, livelock and starvation, and lock ordering to avoid deadlock
- Reentrancy and the HotSpot object header (mark word, lock inflation)
- Virtual threads and pinning on `synchronized` (Java 21; improved in Java 24)

---

[← #50](50-race-condition-visibility-ordering.md) · [Index](README.md) · [#52 →](52-inter-thread-communication-wait-notify.md)

---

[← #50](50-race-condition-visibility-ordering.md) · [Index](README.md) · [#52 →](52-inter-thread-communication-wait-notify.md)
