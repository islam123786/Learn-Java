# Java Locks | ReentrantLock, ReadWriteLock, StampedLock, Semaphore & Condition | Java Full Course #53
> Source: https://youtu.be/JW6-TCU0iS4 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 80:04. The video is in Hindi (Hinglish); these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `synchronized` is simple, but it gives you **no control**: you can't "try" to get the lock and do something else, you can't wait with a **timeout**, and there's no **fairness** (a thread that arrived first may starve). The `java.util.concurrent.locks.Lock` interface fixes all three.
- **`ReentrantLock`** is the everyday replacement for `synchronized`. Always write `lock.lock(); try { ... } finally { lock.unlock(); }`. Its extras are `tryLock()`, `tryLock(timeout, unit)`, fairness via `new ReentrantLock(true)`, and inspection methods (`getHoldCount`, `isHeldByCurrentThread`, ...).
- **`ReentrantReadWriteLock`** splits the lock in two: a **shared read lock** (many readers at once) and an **exclusive write lock** (one writer, no readers). It supports **downgrading** write → read, but not upgrading read → write.
- **`StampedLock`** is a newer read/write lock that hands out `long` **stamps**. Its special feature is **`tryOptimisticRead()`**: read without locking, then `validate(stamp)`; if a write happened in between, fall back to a real read lock. It is **not reentrant**.
- **`Semaphore`** isn't a lock. It's a **permit counter** that lets up to N threads in at once (`acquire()` / `release()`), with no ownership. **`Condition`** (`await` / `signal` / `signalAll`) is the `Lock` version of `wait` / `notify` / `notifyAll`, and one lock can have **several** wait queues.

## Key Concepts

**`Lock` interface.** Lives in `java.util.concurrent.locks`. The core contract is `lock()` and `unlock()`; it also declares `tryLock()`, `tryLock(long, TimeUnit)`, `lockInterruptibly()` and `newCondition()`. *Why it matters:* you lock and unlock by hand, which gives you control `synchronized` can't, at the cost of having to remember to unlock.

**Limitations of `synchronized`.** (1) No control: a thread that can't get the monitor just blocks; there's no "if I can't get it, do something else". (2) No timeout. (3) No fairness: any waiting thread may be picked next, which can cause **starvation**. *Why it matters:* these are the reasons the `Lock` classes exist.

**Starvation.** A thread waits a very long time (or forever) because other threads keep getting the resource first. *Why it matters:* unfair locks make it possible; fair locks reduce it.

**Reentrancy.** The thread that already holds a lock can acquire it again without deadlocking itself. The lock keeps an **owner thread** and a **hold count**; you must call `unlock()` as many times as you called `lock()`. `synchronized` is reentrant too. *Why it matters:* a locked method can call another method that takes the same lock.

**Fair vs unfair lock.** A fair lock (`new ReentrantLock(true)`) hands the lock to the longest-waiting thread, roughly FIFO. The default (unfair) lets a newly arriving thread "barge" in ahead of waiting ones. *Why it matters:* fair means less starvation; unfair means more throughput.

**Shared vs exclusive lock.** Reading doesn't change data (non-destructive), so many readers can share it. Writing changes data (destructive), so a writer needs it alone. *Why it matters:* for read-heavy data (caches, configs, shared documents) a read/write lock lets reads run in parallel.

**Pessimistic vs optimistic locking.** Pessimistic: lock first because you assume a conflict will happen (`synchronized`, `ReentrantLock`, read/write locks). Optimistic: don't lock, do the work, then check whether anyone interfered and retry or fall back if they did (`StampedLock.tryOptimisticRead`, CAS, atomic variables). *Why it matters:* optimistic is faster when conflicts are rare.

**Stamp.** A `long` returned by `StampedLock` methods. You pass it back to unlock or validate. Think of it as a parking-lot token. *Why it matters:* `validate(stamp)` is how an optimistic read finds out whether a write happened.

**Semaphore / permits.** A counter of permits. `acquire()` takes one (blocking if none are left), `release()` gives one back. `new Semaphore(1)` is a *binary* semaphore; `new Semaphore(n)` is a *counting* semaphore. *Why it matters:* it caps how many threads do something at the same time (rate limiting, connection pools, limiting parallel tasks).

**`Condition`.** A named wait queue tied to a `Lock`, created with `lock.newCondition()`. `await()` releases the lock and waits; `signal()` wakes one waiter **from that condition**; `signalAll()` wakes all of them. *Why it matters:* producers and consumers can wait in separate queues, so a signal never wakes the wrong kind of thread.

## Detailed Notes

### Why other locks if we have `synchronized`? [00:00]
- Java has a `Lock` **interface** in the concurrency package (`java.util.concurrent.locks`). As a contract it says: implementations must provide a way to **lock** and a way to **unlock**, plus some optional methods.
- Before looking at it, the video lists what `synchronized` can't do.

### Limitations of `synchronized` [01:30]
- **No control over the lock.** If thread T1 tries to enter a `synchronized` method and the lock is taken, T1 just goes into the blocked state. You can't say "if you get the lock, do the work, else do something else".
- **No timeout.** You can't say "try for 2 seconds, then give up".
- **No fairness** [03:09]. Say T2 arrives at second 1, T3 at second 2, T4 at second 3, and T1 at second 4. With `synchronized`, nothing guarantees T2 goes first. T1 might go first, then T3, then T4, and T2 last. If new threads (T5, ...) keep arriving, T2 can wait for a very long time. This is **starvation**.
- You also don't decide where the lock is released; it's released automatically at the end of the block.

### The `Lock` interface: `lock()` and `unlock()` [06:30]
- Call `lock()` to get permission to enter the critical section and `unlock()` when you leave. While one thread holds it, the others that call `lock()` wait.
- The video's rule of thumb for states: a thread that can't get a lock is **blocked**; a thread that called `wait()` is **waiting**. In everyday speech people say "waiting for the lock" for both.
  - *Correction:* that's exactly right for `synchronized` monitors. A thread stuck in `ReentrantLock.lock()`, however, shows up as `WAITING` in `Thread.getState()` (it is parked with `LockSupport.park`), not `BLOCKED`. `BLOCKED` is only used for monitor locks.
- The four mechanisms covered [08:03]: **ReentrantLock** (the most used after `synchronized`), **ReadWriteLock**, **StampedLock**, and **Semaphore** (not strictly a lock, but used the same way).

### `ReentrantLock`: syntax and the `try`/`finally` rule [09:00]
- `Lock lock = new ReentrantLock();` then `lock.lock();` ... critical section ... `lock.unlock();`.
- The danger of manual locking [10:24]: if you **forget** `unlock()`, or the critical section **throws**, the lock is never released and every other thread waits forever.
- The Java docs' recommended form: call `lock()`, put the critical section in `try`, and call `unlock()` in `finally`. `finally` always runs (except in extreme cases like the JVM dying). A `catch` is optional; `try`/`finally` alone is fine.

### Demo: three threads, one lock [12:17]
- A `Resource` class has `f1()`, which prints "entered", sleeps 2 seconds, and prints "exited". The `ReentrantLock` (imported from `java.util.concurrent.locks`) guards it with `lock` / `try` / `finally unlock`.
- Three threads call `r1.f1()`. Output: Thread-0 entered/exited, then Thread-1, then Thread-2. Only one at a time is inside.
- **What is locked?** [15:30] With `synchronized`, each object (here `r1`) has its own built-in monitor lock, and threads compete for that. With `ReentrantLock`, you lock a **separate lock object**. The `Resource` object itself isn't locked at all.

### Why bother? `tryLock()` [17:54]
- The headline feature: `if (lock.tryLock()) { try { ... } finally { lock.unlock(); } } else { /* do something else */ }`.
- `tryLock()` tries once and returns `true` (you now hold the lock) or `false` immediately. The thread never goes into the blocked state.
- "With great power comes great responsibility": you must call `unlock()` yourself, always in `finally`.

### Why is it called *reentrant*? [19:33]
- The **same thread can acquire the same lock multiple times**. Lock it three times and you must unlock it three times.
- Why it's useful: `methodA()` takes the lock and, inside `try`, calls `methodB()`, which also takes the lock. The same thread enters again (hold count 2), `methodB` unlocks once, then `methodA`'s `finally` unlocks again. A non-reentrant lock would **deadlock** here (StampedLock, covered later, is like that).
- **`synchronized` is also reentrant.** Two `synchronized` methods of the same class lock the same object, and one can call the other. So reentrancy isn't what makes `ReentrantLock` special; the extra control methods are.

### Important `ReentrantLock` methods [24:43]
- `lock()` / `unlock()`. Calling `unlock()` when the current thread doesn't hold the lock throws **`IllegalMonitorStateException`**, just like `wait`/`notify` outside `synchronized`.
- `tryLock()`: try once, no waiting.
- `tryLock(2, TimeUnit.SECONDS)` [26:00]: keep trying for up to 2 seconds, then return `false` and go to the `else`. *(`TimeUnit` is an **enum** with `SECONDS`, `MILLISECONDS`, `MINUTES`, ...; the video calls it a static class.)*
- `isLocked()`: is any thread holding it? For monitoring and debugging only. By the time you act on the answer it may already be wrong, because check-then-act isn't atomic.
- `isHeldByCurrentThread()`: e.g. `if (lock.isHeldByCurrentThread()) lock.unlock();`. Rarely needed.
- `getHoldCount()` [28:10]: how many times the current thread has locked it. A `ReentrantLock` tracks an **owner thread** and a **count**, much like an object's monitor does.
- `isFair()` [29:00]: `new ReentrantLock()` is **unfair** (any thread can be picked); `new ReentrantLock(true)` is **fair** (longest waiter first, like a queue), so starvation is unlikely. Even a fair lock is only *approximately* FIFO, since the OS still schedules threads.
- *Note:* `isLocked`, `isHeldByCurrentThread`, `getHoldCount` and `isFair` are methods of the **`ReentrantLock` class**, not the `Lock` interface. Declare the variable as `ReentrantLock` if you need them.

### `ReadWriteLock`: shared reads, exclusive writes [31:51]
- So far only one thread at a time could enter. But for a resource like a file, database or cache, **reading changes nothing** (it's non-destructive), while **writing** can make writers overwrite each other or leave readers with stale data (it's destructive).
- So split the lock in two:
  - **Read lock = shared lock.** Many readers can hold it together.
  - **Write lock = exclusive lock.** One writer, and no readers at the same time.
- `ReadWriteLock rw = new ReentrantReadWriteLock();` then `Lock rl = rw.readLock();` and `Lock wl = rw.writeLock();`. Both are ordinary `Lock` objects.
- Rules: many readers → allowed together; one writer → alone; writer + reader → not together. If a writer is inside, every reader and writer waits. If a reader is inside, more readers can enter but writers wait.

### The Reader-Writer problem [38:01]
- A classic: several writers (W1, W2, W3) and readers (R1, R2, R3) share a resource. Think of a Google Doc shared with some people as editors and some as viewers.
- Writers need to go one at a time; readers can read together. Shared caches and databases solve this same problem internally.
- It differs from producer-consumer: a consumer *takes* the item away, while a reader only looks at it.

### Demo: `SharedResource` with a read/write lock [39:58]
- `private int value = 0;` `read()` uses `rl.lock(); try { sleep 1s; print; return value; } finally { rl.unlock(); }` (the `finally` runs even after `return`). `write(int)` does the same with `wl`.
- Three readers and three writers (writing 5, 7, 9). In the run, the three readers entered **together** and all read `0` (they happened to run first), then the writers went **one at a time**. *(Which group goes first is up to the scheduler.)*
- **Why have a read lock at all** [47:10], if readers can share? So that **no writer gets in while readers are inside**. Without it, a writer could change the value mid-read.
- Pass `true` to the constructor (`new ReentrantReadWriteLock(true)`) for a fair version.

### Writer starvation [49:15]
- A shared lock is cheap to get, and an exclusive one needs everyone else out. If readers keep arriving, there's always at least one reader inside, the read lock is never fully released, and a waiting writer can starve. This is called **writer starvation**.
- The video suggests limiting how many readers can get in at once so the writer gets a turn.
  - *Note:* in practice the JDK already helps. Even in non-fair mode, `ReentrantReadWriteLock` makes a new reader wait if the thread at the head of the queue is a writer. Fair mode (`true`) gives strict arrival order.

### Lock downgrading [49:46]
- You **can downgrade** a write lock to a read lock, which is useful when you write and then immediately want to read what you wrote. The order matters: lock write → modify → **acquire read lock** → **then** release write lock → read → release read lock. If you released the write lock first, another writer could slip in between.
- You **cannot upgrade** read → write. Other readers may be inside, so a shared lock can't simply become exclusive. *(In `ReentrantReadWriteLock`, calling `writeLock().lock()` while holding the read lock blocks forever, a self-deadlock; `writeLock().tryLock()` just returns `false`.)*

### `StampedLock` [52:01]
- A more modern take on the read/write lock. Three main methods: `writeLock()` (exclusive), `readLock()` (shared), and **`tryOptimisticRead()`**.
- Each returns a **`long` stamp**, an id you store and pass back:
  - `long stamp = lock.readLock(); try { ... } finally { lock.unlockRead(stamp); }`
  - `long stamp = lock.writeLock(); try { ... } finally { lock.unlockWrite(stamp); }`
- The parking-lot analogy: you get a token on the way in and show it on the way out. Unlike `ReentrantLock`, there's no owner thread.
  - *Correction:* the video says the stamp lets the lock check that the *same thread* unlocks. It doesn't: a `StampedLock` has no notion of ownership, and **any** thread holding a valid stamp may unlock it. The stamp only identifies the lock mode and version.

### Pessimistic vs optimistic locking [55:36]
- **Pessimistic:** "multiple threads might clash, so lock before entering." This covers `synchronized`, `ReentrantLock`, `ReentrantReadWriteLock`, and `StampedLock`'s `readLock`/`writeLock`. Safe but **slower**: every lock/unlock has overhead (recording the owner, queuing, ...).
- **Optimistic:** don't lock at all. Read, then check whether anyone changed things meanwhile. If nobody did, you're done; if someone did, use a **fallback** (e.g. read again with a real read lock). Faster when writes are rare. CAS and atomic integers (upcoming videos) also work optimistically.
- The `tryOptimisticRead` steps: (1) get a stamp; (2) read the data **without locking**; (3) check whether it was modified (`validate`); if so, fall back to a pessimistic read.

### Demo: optimistic read [59:24]
- `write()` becomes `long stamp = lock.writeLock(); try { value = v; } finally { lock.unlockWrite(stamp); }`.
- `read()`: `long stamp = lock.tryOptimisticRead(); int current = value; sleep(1s); if (!lock.validate(stamp)) { stamp = lock.readLock(); try { current = value; } finally { lock.unlockRead(stamp); } } print and return current;`.
- The sleep goes **before** `validate`. Putting it after would make the check meaningless.
- In the run, the writers changed the value to 5, 7, then 9 while the readers slept. The readers' stamps were invalidated, so they fell back and all read `9`.
- `validate(stamp)` returns `true` if no write happened since the stamp was issued, `false` otherwise.
  - *Correction:* the video describes the stamp as something like a hash of the variables you read. It isn't tied to your variables at all. It's a version number of the **lock's** state, and `validate` fails if **any write lock** was taken since the stamp, even one that wrote the same value or touched different fields.
- **`StampedLock` is not reentrant** [66:47]. If a thread that holds it tries to lock it again, it deadlocks itself.

### `Semaphore` [67:00]
- Use it when you want **several** threads in the critical section at once, all doing the same kind of work, with a fixed upper limit (3, 4, 5, ...).
- It keeps a count of **permits**: `Semaphore s = new Semaphore(3);`. `acquire()` takes a permit (3 → 2 → 1 → 0); when the count is 0, the next thread blocks until someone calls `release()`, which gives a permit back.
- **Binary** semaphore: `new Semaphore(1)`, which behaves like a lock. **Counting** semaphore: `new Semaphore(n)`.
- **Lock vs Semaphore** [70:11]: locks are **ownership-based**. The thread that acquired the lock is its owner and must release it (with a shared read lock, several threads own it, but they're still owners). A semaphore has **no ownership**: it's only a counter, and any thread can call `release()`.
- Fairness: `new Semaphore(3, true)`.
- Use cases [72:45]: an **API rate limiter** (cap concurrent requests; it comes back in the Spring Boot part of the course) and limiting the number of tasks that run in parallel (e.g. at most 5).

### `Condition`: `await` / `signal` / `signalAll` [73:38]
- The `Lock` counterparts of `wait()` / `notify()` / `notifyAll()` from the previous video. With a `ReentrantLock` you can't use `wait`/`notify` on it meaningfully; you use a `Condition`.
- A condition is a **waiting area** tied to a lock: `Condition c1 = lock.newCondition();`.
  - `c1.await()`: release the lock and wait in `c1`'s queue.
  - `c1.signal()`: wake **one** thread waiting on `c1`.
  - `c1.signalAll()`: wake **all** threads waiting on `c1`.
  - Calling them without holding the lock throws `IllegalMonitorStateException`.
- The big difference: an object's monitor has **one** wait queue, but one lock can have **many** conditions. In producer-consumer with many producers and consumers, `notify()` could wake the wrong kind of thread (a producer when a consumer was needed), which is why we used `notifyAll()`. With separate `notFull` and `notEmpty` conditions, `notEmpty.signal()` only ever wakes a consumer.
- Homework from the video: re-solve the multi-producer/multi-consumer problem with `ReentrantLock` + `Condition`, and see whether you still need `signalAll()`.
- **Spurious wakeups** [79:14] can happen with `await()` just as with `wait()`: a thread can wake up without being signalled. So always check the condition in a **`while`** loop, not an `if`.

## Code Examples

Each block is a complete program. Save it under the class's name (e.g. `LkReentrantBasic.java`) and run it with `java LkReentrantBasic.java`.

### `ReentrantLock` with `try`/`finally`
```java
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class LkReentrantBasic {
    public static void main(String[] args) throws InterruptedException {
        LkResource r1 = new LkResource();
        Thread[] ts = new Thread[3];
        for (int i = 0; i < ts.length; i++) {
            ts[i] = new Thread(r1::f1, "T" + (i + 1));
            ts[i].start();
        }
        for (Thread t : ts) t.join();
        System.out.println("done");
        // order may vary, but entered/exited pairs never interleave. Sample:
        // T1 entered
        // T1 exited
        // T3 entered
        // T3 exited
        // T2 entered
        // T2 exited
        // done
    }
}

class LkResource {
    private final Lock lock = new ReentrantLock();

    void f1() {
        lock.lock();              // acquire before the critical section
        try {
            System.out.println(Thread.currentThread().getName() + " entered");
            Thread.sleep(100);    // pretend to work
            System.out.println(Thread.currentThread().getName() + " exited");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            lock.unlock();        // always released, even on exceptions
        }
    }
}
```

### Reentrancy, hold count, and unlocking a lock you don't hold
```java
import java.util.concurrent.locks.ReentrantLock;

public class LkReentrancyDemo {
    // Declared as ReentrantLock (not Lock) so getHoldCount() etc. are available
    private static final ReentrantLock lock = new ReentrantLock();

    static void methodA() {
        lock.lock();
        try {
            System.out.println("in A, hold count = " + lock.getHoldCount());
            methodB();            // same thread locks again: no deadlock
        } finally {
            lock.unlock();
        }
    }

    static void methodB() {
        lock.lock();
        try {
            System.out.println("in B, hold count = " + lock.getHoldCount());
            System.out.println("held by me? " + lock.isHeldByCurrentThread());
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) {
        methodA();
        System.out.println("after A, hold count = " + lock.getHoldCount());
        System.out.println("isLocked = " + lock.isLocked());
        try {
            lock.unlock();        // we don't hold it any more
        } catch (IllegalMonitorStateException e) {
            System.out.println("unlock without lock -> IllegalMonitorStateException");
        }
        // in A, hold count = 1
        // in B, hold count = 2
        // held by me? true
        // after A, hold count = 0
        // isLocked = false
        // unlock without lock -> IllegalMonitorStateException
    }
}
```

### `tryLock()`, `tryLock(timeout)` and fairness
```java
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class LkTryLockDemo {
    public static void main(String[] args) throws InterruptedException {
        ReentrantLock lock = new ReentrantLock();

        lock.lock(); // main holds the lock
        Thread t1 = new Thread(() -> {
            if (lock.tryLock()) {
                try { System.out.println("t1 got it"); } finally { lock.unlock(); }
            } else {
                System.out.println("t1: lock busy, doing something else");
            }
            try {
                if (lock.tryLock(200, TimeUnit.MILLISECONDS)) {
                    try { System.out.println("t1 got it after waiting"); } finally { lock.unlock(); }
                } else {
                    System.out.println("t1: gave up after 200 ms");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
        t1.start();
        t1.join();
        lock.unlock(); // main releases

        Thread t2 = new Thread(() -> {
            if (lock.tryLock()) {
                try { System.out.println("t2 got it"); } finally { lock.unlock(); }
            }
        });
        t2.start();
        t2.join();

        System.out.println("default fair? " + lock.isFair());
        System.out.println("new ReentrantLock(true) fair? " + new ReentrantLock(true).isFair());
        // t1: lock busy, doing something else
        // t1: gave up after 200 ms
        // t2 got it
        // default fair? false
        // new ReentrantLock(true) fair? true
    }
}
```

### Reader-Writer with `ReentrantReadWriteLock`
```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LkReadWriteDemo {
    public static void main(String[] args) throws InterruptedException {
        LkSharedResource sr = new LkSharedResource();

        // Phase 1: three readers at once -> they share the read lock
        List<Thread> readers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) readers.add(new Thread(sr::read, "R" + i));
        long start = System.nanoTime();
        readers.forEach(Thread::start);
        for (Thread t : readers) t.join();
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("3 readers x 300 ms finished in under 800 ms? " + (ms < 800));

        // Phase 2: three writers -> exclusive, one at a time
        List<Thread> writers = new ArrayList<>();
        for (int i = 1; i <= 3; i++) writers.add(new Thread(sr::increment, "W" + i));
        writers.forEach(Thread::start);
        for (Thread t : writers) t.join();
        System.out.println("final value = " + sr.peek());
        System.out.println("max writers inside at once = " + sr.maxWriters());
        // order may vary. Sample:
        // R2 reads 0
        // R1 reads 0
        // R3 reads 0
        // 3 readers x 300 ms finished in under 800 ms? true
        // W1 changes value to 1
        // W3 changes value to 2
        // W2 changes value to 3
        // final value = 3
        // max writers inside at once = 1
    }
}

class LkSharedResource {
    private int value = 0;
    private final ReadWriteLock rw = new ReentrantReadWriteLock();
    private final Lock rl = rw.readLock();   // shared
    private final Lock wl = rw.writeLock();  // exclusive
    private final AtomicInteger writersInside = new AtomicInteger();
    private final AtomicInteger maxWriters = new AtomicInteger();

    int read() {
        rl.lock();
        try {
            Thread.sleep(300);
            System.out.println(Thread.currentThread().getName() + " reads " + value);
            return value;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return -1;
        } finally {
            rl.unlock(); // runs even after return
        }
    }

    void increment() {
        wl.lock();
        try {
            maxWriters.accumulateAndGet(writersInside.incrementAndGet(), Math::max);
            Thread.sleep(50);
            value++;
            System.out.println(Thread.currentThread().getName() + " changes value to " + value);
            writersInside.decrementAndGet();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            wl.unlock();
        }
    }

    int peek() { rl.lock(); try { return value; } finally { rl.unlock(); } }
    int maxWriters() { return maxWriters.get(); }
}
```

### Lock downgrading (write → read is OK, read → write is not)
```java
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LkDowngradeDemo {
    public static void main(String[] args) {
        ReentrantReadWriteLock rw = new ReentrantReadWriteLock();
        int[] value = {0};

        rw.writeLock().lock();
        try {
            value[0] = 100;               // modify under the exclusive lock
            rw.readLock().lock();         // 1) take the read lock first...
        } finally {
            rw.writeLock().unlock();      // 2) ...then drop the write lock
        }
        try {
            System.out.println("read after write: " + value[0]);
            System.out.println("write locked now? " + rw.isWriteLocked());
            System.out.println("my read holds: " + rw.getReadHoldCount());
            // Upgrade attempt: lock() here would block forever; tryLock() just fails
            System.out.println("upgrade read->write possible? " + rw.writeLock().tryLock());
        } finally {
            rw.readLock().unlock();
        }
        // read after write: 100
        // write locked now? false
        // my read holds: 1
        // upgrade read->write possible? false
    }
}
```

### `StampedLock`: optimistic read with fallback
```java
import java.util.concurrent.locks.StampedLock;

public class LkStampedDemo {
    private static final StampedLock lock = new StampedLock();
    private static int value = 0;

    static void write(int v) {
        long stamp = lock.writeLock();
        try {
            value = v;
        } finally {
            lock.unlockWrite(stamp);
        }
    }

    static int optimisticRead(Runnable somethingInBetween) {
        long stamp = lock.tryOptimisticRead(); // no lock taken
        int current = value;
        somethingInBetween.run();              // a writer may sneak in here
        if (!lock.validate(stamp)) {           // was a write lock taken since the stamp?
            System.out.println("  stamp invalid -> fallback to pessimistic read");
            stamp = lock.readLock();
            try {
                current = value;
            } finally {
                lock.unlockRead(stamp);
            }
        } else {
            System.out.println("  stamp valid -> no lock was needed");
        }
        return current;
    }

    public static void main(String[] args) {
        write(5);
        System.out.println("read 1:");
        System.out.println("  value = " + optimisticRead(() -> { }));

        System.out.println("read 2:");
        int v = optimisticRead(() -> {
            Thread writer = new Thread(() -> write(9));
            writer.start();
            try { writer.join(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        System.out.println("  value = " + v);
        // read 1:
        //   stamp valid -> no lock was needed
        //   value = 5
        // read 2:
        //   stamp invalid -> fallback to pessimistic read
        //   value = 9
    }
}
```

### `Semaphore`: at most 2 threads at a time
```java
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicInteger;

public class LkSemaphoreDemo {
    public static void main(String[] args) throws InterruptedException {
        Semaphore permits = new Semaphore(2);   // counting semaphore with 2 permits
        AtomicInteger inside = new AtomicInteger();
        AtomicInteger maxInside = new AtomicInteger();

        List<Thread> tasks = new ArrayList<>();
        for (int i = 1; i <= 6; i++) {
            tasks.add(new Thread(() -> {
                try {
                    permits.acquire();          // take a permit (blocks if none left)
                    try {
                        maxInside.accumulateAndGet(inside.incrementAndGet(), Math::max);
                        System.out.println(Thread.currentThread().getName() + " working");
                        Thread.sleep(100);
                        inside.decrementAndGet();
                    } finally {
                        permits.release();      // give it back
                    }
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }, "task" + i));
        }
        tasks.forEach(Thread::start);
        for (Thread t : tasks) t.join();

        System.out.println("never more than 2 inside: " + (maxInside.get() <= 2));
        System.out.println("available permits: " + permits.availablePermits());

        permits.release(); // no ownership: release without acquire is allowed...
        System.out.println("after extra release: " + permits.availablePermits()); // ...and adds a permit!
        // order may vary. Sample:
        // task1 working
        // task2 working
        // task4 working
        // task3 working
        // task5 working
        // task6 working
        // never more than 2 inside: true
        // available permits: 2
        // after extra release: 3
    }
}
```

### `Condition`: producer-consumer with two wait queues
```java
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LkConditionDemo {
    public static void main(String[] args) throws InterruptedException {
        LkBoundedBuffer buffer = new LkBoundedBuffer(2);
        AtomicInteger sum = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();

        for (int p = 1; p <= 2; p++) {               // 2 producers, 5 items each
            threads.add(new Thread(() -> {
                try {
                    for (int i = 1; i <= 5; i++) buffer.put(i);
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "P" + p));
        }
        for (int c = 1; c <= 2; c++) {               // 2 consumers, 5 items each
            threads.add(new Thread(() -> {
                try {
                    for (int i = 1; i <= 5; i++) sum.addAndGet(buffer.take());
                } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }, "C" + c));
        }
        threads.forEach(Thread::start);
        for (Thread t : threads) t.join();

        System.out.println("sum of consumed items = " + sum.get());
        // order of produce/consume lines may vary. Sample (first lines):
        // P1 produced 1
        // P1 produced 2
        // C1 consumed 1
        // P2 produced 1
        // ...
        // sum of consumed items = 30
    }
}

class LkBoundedBuffer {
    private final Queue<Integer> items = new ArrayDeque<>();
    private final int capacity;
    private final ReentrantLock lock = new ReentrantLock();
    private final Condition notFull = lock.newCondition();   // producers wait here
    private final Condition notEmpty = lock.newCondition();  // consumers wait here

    LkBoundedBuffer(int capacity) { this.capacity = capacity; }

    void put(int x) throws InterruptedException {
        lock.lock();
        try {
            while (items.size() == capacity) notFull.await(); // while, not if (spurious wakeups)
            items.add(x);
            System.out.println(Thread.currentThread().getName() + " produced " + x);
            notEmpty.signal();  // wakes a consumer, never a producer
        } finally {
            lock.unlock();
        }
    }

    int take() throws InterruptedException {
        lock.lock();
        try {
            while (items.isEmpty()) notEmpty.await();
            int x = items.remove();
            System.out.println(Thread.currentThread().getName() + " consumed " + x);
            notFull.signal();   // wakes a producer, never a consumer
            return x;
        } finally {
            lock.unlock();
        }
    }
}
```

## Common Pitfalls / Tips
- **Always unlock in `finally`.** Put `lock()` right *before* the `try` (not inside it). If `lock()` were inside and failed, `finally` would call `unlock()` on a lock you never got and throw `IllegalMonitorStateException`.
- **Lock as many times as you unlock.** With reentrant locks, every `lock()` needs a matching `unlock()`, or the lock stays held after the method returns.
- **Only unlock when `tryLock()` returned `true`.** The `unlock()` belongs inside the `if (lock.tryLock())` branch.
- **Don't build logic on `isLocked()`.** The answer can be stale by the time you use it. Use `tryLock()` instead.
- **`tryLock()` with no arguments ignores fairness.** It grabs a free lock even when other threads are queued on a fair lock. Use `tryLock(0, TimeUnit.SECONDS)` if you want fairness respected.
- **Fair isn't free.** Fair locks have noticeably lower throughput. Use them only when starvation is a real problem.
- **Read/write locks pay off only for read-heavy workloads** with reads that take some time. For tiny critical sections, a plain `ReentrantLock` is often faster.
- **Never try to upgrade read → write** in `ReentrantReadWriteLock`. `writeLock().lock()` while holding the read lock deadlocks. (`StampedLock` offers `tryConvertToWriteLock(stamp)` for this.)
- **Downgrade in the right order:** acquire the read lock *before* releasing the write lock.
- **`StampedLock` is not reentrant** and doesn't support `Condition`. Don't call a method that takes the same `StampedLock` while you hold it.
- **Optimistic reads:** copy the fields into locals first, call `validate` afterwards, and only use the locals if it returned `true`. Don't act on the data (e.g. follow a reference) before validating.
- **A `Semaphore` doesn't check who releases.** An extra `release()` permanently raises the permit count above your limit. Release in `finally`, and only after a successful `acquire()`.
- **Call `await`/`signal` only while holding the lock** (otherwise `IllegalMonitorStateException`), and check the condition in a `while` loop to guard against spurious wakeups.
- **Don't mix the two APIs.** With a `Lock`, use `Condition.await/signal`, not `Object.wait/notify`.

## Review Questions
1. Name three things `synchronized` can't do that the `Lock` interface can.
2. Why should `unlock()` always go in a `finally` block? What happens if the critical section throws and you didn't use `finally`?
3. What does "reentrant" mean? Is `synchronized` reentrant? What's the hold count after `methodA` (which locks) calls `methodB` (which locks the same lock)?
4. What's the difference between `tryLock()` and `tryLock(2, TimeUnit.SECONDS)`?
5. What is a fair lock, how do you create one, and what problem does it reduce?
6. In a `ReentrantReadWriteLock`, which combinations of threads may hold the lock together? Why does the read lock exist if readers can share anyway?
7. What is writer starvation? What is lock downgrading, and why can't you upgrade?
8. Explain the three steps of `StampedLock.tryOptimisticRead()`. When does `validate(stamp)` return `false`?
9. How is a `Semaphore` different from a lock? Give one real use case.
10. What advantage does `Condition` have over `wait`/`notify` in a multi-producer, multi-consumer setup?

<details>
<summary>Answers</summary>

1. Try to acquire and do something else if the lock is busy (`tryLock()`), give up after a timeout (`tryLock(time, unit)`), and guarantee fairness (`new ReentrantLock(true)`). You also decide exactly where to lock and unlock, can wait interruptibly (`lockInterruptibly()`), and can have several conditions.
2. Because `finally` runs even if the critical section throws or returns. Without it, an exception skips `unlock()`, the lock stays held, and every other thread trying to `lock()` waits forever.
3. The thread holding the lock can acquire it again without blocking itself. The lock tracks the owner and a count, and you must unlock the same number of times. Yes, `synchronized` is reentrant too. Inside `methodB` the hold count is 2; after both unlock, it's 0.
4. `tryLock()` tries once and returns `true`/`false` immediately. `tryLock(2, SECONDS)` keeps trying for up to 2 seconds (it can be interrupted) and returns `false` only if the lock still wasn't free by then.
5. A fair lock gives the lock to the longest-waiting thread (roughly FIFO). You create it with `new ReentrantLock(true)` (or `new ReentrantReadWriteLock(true)`, `new Semaphore(n, true)`). It reduces starvation, at the cost of throughput.
6. Many readers can hold it together; one writer holds it alone, with no readers and no other writers. The read lock exists so that a writer can't enter while readers are inside: holding a shared lock blocks the exclusive one.
7. Writer starvation: readers keep arriving, so there's always someone holding the shared lock, and the writer never gets the exclusive lock. Downgrading means turning a write lock into a read lock: acquire the read lock while still holding the write lock, then release the write lock. Upgrading is impossible because other readers may be inside; a shared lock can't become exclusive without them leaving, and two readers both trying to upgrade would deadlock.
8. (1) `long stamp = lock.tryOptimisticRead()` takes a stamp without locking; (2) read the fields into locals; (3) `lock.validate(stamp)`: if it's `true`, use the locals; if `false`, fall back to `readLock()`, read again, and `unlockRead(stamp)`. `validate` returns `false` if a write lock was acquired after the stamp was issued (or the stamp was 0 because a writer held the lock at the time).
9. A lock has an owner (the thread that locked it must unlock it), and an exclusive lock allows one thread. A semaphore is only a counter of permits: up to N threads can be inside, and any thread may call `release()`. Use cases: an API rate limiter, a connection pool, capping the number of parallel tasks.
10. You can create a separate `Condition` per kind of waiter (e.g. `notFull` for producers, `notEmpty` for consumers). `notEmpty.signal()` wakes exactly one consumer and never a producer. With the single wait set of `wait`/`notify`, `notify()` might wake the wrong kind of thread, so you're forced to use `notifyAll()`.
</details>

## Further Study
- `lockInterruptibly()` and how interruption works with locks
- `ReentrantLock` internals: `AbstractQueuedSynchronizer` (AQS) and `LockSupport.park/unpark`
- `StampedLock.tryConvertToWriteLock`, `tryConvertToReadLock`, and the read/write views (`asReadLock()`)
- Other synchronizers: `CountDownLatch`, `CyclicBarrier`, `Phaser`, `Exchanger`
- `BlockingQueue` (`ArrayBlockingQueue`, `LinkedBlockingQueue`): producer-consumer without writing locks yourself
- CAS (compare-and-swap) and atomic variables (`AtomicInteger`, `AtomicReference`): the next videos in the series
- Deadlock detection and avoidance (lock ordering, `tryLock` with timeout)
- Virtual threads and pinning: why `ReentrantLock` was preferred over `synchronized` before JDK 24

---

[← #52](52-inter-thread-communication-wait-notify.md) · [Index](../../ReadMe.md) · [#54 →](54-lock-free-concurrency-atomic-cas.md)
