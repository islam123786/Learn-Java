# Inter Thread Communication in Java | wait(), notify(), notifyAll() Deep Dive | Java Full Course #52
> Source: https://youtu.be/EZS19NLnsvc · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 63:17. The video is in Hindi (Hinglish); these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `synchronized` gives **mutual exclusion** (only one thread in the critical section at a time). It does **not** control **order**, so it can't stop a consumer from running before the producer. That needs **inter-thread communication**.
- The classic example is the **producer-consumer problem**: one shared box. The consumer must not read an empty box, and the producer must not overwrite an unread value.
- Busy waiting (`while (!ready) {}`) works only with `volatile`, wastes CPU, and **deadlocks** if you also put it inside `synchronized`.
- The right tool is `wait()` / `notify()` / `notifyAll()` from `Object`. `wait()` **releases the monitor lock** and parks the thread in the object's **wait set**. `notify()` moves one random waiter out; `notifyAll()` moves all of them out. A woken thread must **re-acquire the lock** (it goes through BLOCKED) and resumes **right after** its `wait()` call.
- Always call them while holding the object's lock (otherwise `IllegalMonitorStateException`), always wait inside a **`while` loop** (a *guarded block*, which protects against spurious wake-ups), and prefer **`notifyAll()`** when there are several producers/consumers.

## Key Concepts

**Inter-thread communication.** Threads coordinating *when* they act on shared state: "wait until there's data", "tell the others the data is ready". *Why it matters:* locking alone prevents corruption but not out-of-order work.

**Producer-consumer problem.** A producer puts items into a shared buffer (here, a single `Box` holding one `Integer`); a consumer takes them out. The two failure modes are consuming when empty (reads `null`) and producing when full (the previous value is lost). *Why it matters:* it's the shape of printers and print queues, message queues, request buffers, logging pipelines, and much more.

**Busy waiting.** A thread spins in a loop re-checking a condition. It's "busy only at waiting": the CPU keeps scheduling it even though it does nothing useful. *Why it matters:* it burns CPU and needs `volatile` for visibility, and combined with locks it can deadlock.

**Monitor lock (intrinsic lock).** Every Java object has one. `synchronized` acquires it. Conceptually the monitor tracks the **owner thread**, an **entry set** of threads BLOCKED trying to get the lock, and a **wait set** of threads that called `wait()`. *Why it matters:* `wait`/`notify` work on the same object whose lock you hold.

**`wait()`.** Releases the monitor lock, moves the thread to the WAITING state (in the wait set), and stays there until another thread notifies it (or it's interrupted, or a spurious wake-up happens). *Why it matters:* the thread sleeps without using CPU, and releasing the lock lets the other side make progress.

**`notify()` / `notifyAll()`.** `notify()` picks **one arbitrary** thread from the wait set; `notifyAll()` picks **all** of them. Woken threads move to BLOCKED, compete for the lock, and the winner continues from the line after `wait()`. *Why it matters:* with several kinds of waiters, `notify()` can wake the wrong kind and leave everyone asleep.

**Guarded block.** `while (!condition) wait();`. The `while` (not `if`) makes a woken thread re-check its condition before proceeding. *Why it matters:* it handles spurious wake-ups and "another thread got there first".

## Detailed Notes

### Why `synchronized` isn't enough [▶ ~00:00](https://youtu.be/EZS19NLnsvc?t=0)
- Recap: threads can interfere through a shared resource, so we lock the critical section with `synchronized`.
- But some problems are about **coordination**, not just exclusion. For those, Java has three `Object` methods: `wait()`, `notify()`, `notifyAll()`.

### The producer-consumer problem [▶ ~01:27](https://youtu.be/EZS19NLnsvc?t=87)
- A producer puts a value into a box; a consumer takes it out, leaving the box empty again. Each runs on its own thread (T1, T2).
- Multithreaded code is **non-deterministic**, so two bad cases are possible:
  1. The consumer runs first and "consumes" `null`.
  2. The producer runs twice in a row and overwrites `5` with `10` before it was consumed, so data is lost.

### Code: the broken version [▶ ~04:17](https://youtu.be/EZS19NLnsvc?t=257)
- `Box` with an `Integer item` (wrapper type so it can hold `null`) and a `boolean flag` meaning "item present".
- `produce(int value)`: set `item`, set `flag = true`, print. `consume()`: print `item`, set it back to `null`, set `flag = false`.
- With one call each, it's a 50/50 race. With a loop of 20 iterations [▶ ~08:46](https://youtu.be/EZS19NLnsvc?t=526) and different sleeps (producer 100 ms, consumer 70 ms), the output shows the consumer reading `null` many times.

### Why adding `synchronized` alone doesn't fix it [▶ ~10:49](https://youtu.be/EZS19NLnsvc?t=649)
- Making both methods `synchronized` only guarantees that **one of them runs at a time**. It doesn't say *which one goes first*. The consumer can still enter first and read `null`.
- The problem isn't locking, it's **communication**: "don't consume if nothing is there; don't produce if something is already there".

### The naive fix: busy waiting [▶ ~13:06](https://youtu.be/EZS19NLnsvc?t=786)
- Producer: `while (flag) { /* do nothing */ }` before producing. Consumer: `while (!flag) { }` before consuming.
- The flag must be `volatile`, or a thread may keep reading a stale cached value forever (the visibility problem from the earlier video).
- The cost [▶ ~16:32](https://youtu.be/EZS19NLnsvc?t=992): a spinning thread keeps getting CPU time. If the producer is a slow external server taking 2 s, the consumer spins for 2 s asking "is it there yet?". That's **busy waiting**.

### Busy waiting in practice [▶ ~17:00](https://youtu.be/EZS19NLnsvc?t=1020)
- It produces the right sequence, but sometimes the **print order** is wrong [▶ ~18:49](https://youtu.be/EZS19NLnsvc?t=1129): "consumed 1" before "produced 1". The producer set the flag, got context-switched *before* its `println`, and the consumer ran in between.
- The usual race-condition fix is to make both methods `synchronized`, and that **deadlocks** [▶ ~20:36](https://youtu.be/EZS19NLnsvc?t=1236). If the consumer enters first (flag `false`), it holds the box's lock and spins forever. The producer can never enter to set the flag. Nothing prints. At any moment the consumer is first, it's stuck.

### Waiting properly: shared resource, condition, waiting [▶ ~22:08](https://youtu.be/EZS19NLnsvc?t=1328)
- Java's thread lifecycle has a **WAITING** state (different from TIMED_WAITING, which you get from `Thread.sleep(ms)`). `wait()` enters it; `notify()`/`notifyAll()` get a thread out.
- Every coordination problem has three pieces [▶ ~24:10](https://youtu.be/EZS19NLnsvc?t=1450):
  - **Shared resource:** the box/item.
  - **Condition:** does the box have an item?
  - **Who waits:** the consumer waits while it's empty; the producer waits while it's full.

### What `wait()` does [▶ ~25:21](https://youtu.be/EZS19NLnsvc?t=1521)
1. **Releases the monitor lock(s)** the thread holds on that object.
2. Moves the thread to the **WAITING** state.
3. Stays there **until another thread wakes it** (not after a fixed time, unlike `sleep`).
- Releasing the lock is the crucial step. Without it, the waiting consumer would still hold the lock, the producer could never enter to call `notify()`, and you'd have the deadlock again.
- Flow: the consumer sees an empty box and waits (releasing the lock). The producer enters, fills the box, sets the flag, and calls `notify()` on the way out. The consumer wakes, re-checks the loop condition (now false), consumes, and calls `notify()` in case a producer is waiting.

### Rule: only inside `synchronized` [▶ ~30:16](https://youtu.be/EZS19NLnsvc?t=1816)
- `wait()` and `notify()` must be called by a thread that **owns the object's monitor**, otherwise you get `IllegalMonitorStateException`. Reason: `wait()` has to release a lock, and it can't release one you don't hold. `notify()` operates on that object's wait set, which belongs to the lock you must hold.
- `wait()` throws the checked `InterruptedException`, so declare or catch it.

### Internals: monitor, owner, wait set [▶ ~35:00](https://youtu.be/EZS19NLnsvc?t=2100)
- Each object's intrinsic (monitor) lock records the **owner thread**, plus a queue of BLOCKED threads trying to enter (seen in the previous video) and a **waiting queue** (wait set).
- Consumer T2 takes the box's lock (owner = T2), finds it empty, calls `wait()`: T2 goes into the wait set and owner becomes empty. Producer T1, which was BLOCKED waiting for the lock, can now get it.

### What `notify()` does [▶ ~38:45](https://youtu.be/EZS19NLnsvc?t=2325)
1. Picks **one random** thread from the wait set (which one depends on the JVM/OS, so it's non-deterministic) [▶ ~42:53](https://youtu.be/EZS19NLnsvc?t=2573).
2. Moves it to the **BLOCKED** state, *not* straight to RUNNABLE. It gave up the lock when it waited, so it has to compete for the lock again.
3. Once it gets the lock it becomes RUNNABLE/RUNNING and continues from **the line after `wait()`** (not from the top of the method). Since `wait()` sits in a loop, it re-checks the condition first.
- `notify()` also has to be called inside `synchronized`. Note that the notifier keeps the lock until it leaves its synchronized block, so the woken thread only proceeds after that.

### Code: the working version [▶ ~44:11](https://youtu.be/EZS19NLnsvc?t=2651)
- Replace "do nothing" with `wait()` in both loops, add `notify()` after producing and after consuming, and handle `InterruptedException`.
- Output now strictly alternates "produced n / consumed n", even when both sleeps are set to 100 ms. No CPU is wasted while waiting.
- *(Side note: the video keeps `volatile` on the fields. It's harmless but not needed once every access happens inside `synchronized`, because the lock already guarantees visibility.)*

### `notifyAll()` and why `notify()` can be dangerous [▶ ~48:19](https://youtu.be/EZS19NLnsvc?t=2899)
- `notifyAll()`: every thread in the wait set moves to BLOCKED; they all compete; only one gets the lock at a time. It has more overhead than `notify()` but is **safer**, and is generally preferred.
- Scenario with **2 producers (P1, P2) and 2 consumers (C1, C2)** and one box [▶ ~49:26](https://youtu.be/EZS19NLnsvc?t=2966):
  1. Box empty. C1 enters and waits. C2 enters and waits. (Wait set: C1, C2.)
  2. P1 produces 10, `notify()` wakes C1, and P1 finishes.
  3. P2 gets the lock before C1, sees the box full, and waits. (Wait set: C2, P2.)
  4. C1 gets the lock, consumes, and calls `notify()`, which **randomly wakes C2** (the wrong kind of thread).
  5. C2 sees an empty box and waits again. Now **C2 and P2 both wait** and nobody is left to notify them. Everything hangs (a deadlock-like "lost wake-up").
- With `notifyAll()`, step 4 wakes both C2 and P2. If C2 runs first it just goes back to waiting, and P2 still gets to produce. Someone can always make progress.
- Rule of thumb: `notify()` is fine only when there's exactly one thread of each kind (or all waiters are interchangeable). Otherwise use `notifyAll()`.

### Interview question: why are they in `Object`, not `Thread`? [▶ ~57:50](https://youtu.be/EZS19NLnsvc?t=3470)
- A thread waits on and notifies an **object's monitor**. The lock and its wait set belong to the object, so the methods that put threads into that wait set and take them out live on `Object` too.

### `sleep()` vs `wait()` [▶ ~58:40](https://youtu.be/EZS19NLnsvc?t=3520)
- `Thread.sleep(ms)`: TIMED_WAITING for a fixed time, **keeps any locks it holds**, can be called anywhere.
- `obj.wait()`: WAITING until notified, **releases `obj`'s lock**, and must be called while holding that lock. *(There is also a timed `wait(ms)` overload, which gives TIMED_WAITING.)*

### Spurious wake-ups and guarded blocks [▶ ~59:30](https://youtu.be/EZS19NLnsvc?t=3570)
- A waiting thread can occasionally wake up **without** any `notify()`. This is a *spurious wake-up*: rare, caused by OS/CPU-level behavior that Java doesn't fully control.
- That's why we always wrote `while (flag == false) wait();` and never `if`. After waking, the loop re-checks the condition and goes back to waiting if it's still not satisfied. With `if`, the thread would fall through and, for example, consume `null`.
- `while` + `wait()` is called a **guarded block** [▶ ~62:31](https://youtu.be/EZS19NLnsvc?t=3751).
- *(The same `while` also protects against the `notifyAll()` case where several threads wake and an earlier one already took the item. The code examples below show that one, since it can be reproduced reliably.)*

### Real-world view [▶ ~62:50](https://youtu.be/EZS19NLnsvc?t=3770)
- A document creator (producer) and a printer (consumer) is the textbook example. Anything that produces values on one side and consumes them on the other is a producer-consumer problem.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`. Thread interleavings vary between runs, so outputs marked "order may vary" are samples.

### 1. The broken box (no coordination)
```java
public class ItcBrokenBox {
    private Integer item;          // Integer so it can be null
    private boolean flag = false;  // true = item present

    void produce(int value) {
        item = value;
        flag = true;
        System.out.println("Produced " + item);
    }

    void consume() {
        System.out.println("Consumed " + item);
        item = null;
        flag = false;
    }

    public static void main(String[] args) throws InterruptedException {
        ItcBrokenBox box = new ItcBrokenBox();
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                sleep(30);
                box.produce(i);
            }
        });
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                sleep(20); // faster consumer
                box.consume();
            }
        });
        producer.start();
        consumer.start();
        producer.join();
        consumer.join();
        // order may vary, e.g.:
        // Consumed null
        // Produced 1
        // Consumed 1
        // Consumed null
        // Produced 2
        // ...
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
```

### 2. Busy waiting with `volatile` (works, but wastes CPU)
```java
public class ItcBusyWait {
    private volatile Integer item;
    private volatile boolean flag = false;
    private int consumedSum = 0; // only written by the consumer thread

    void produce(int value) {
        while (flag) { /* spin: box still full */ }
        item = value;
        flag = true;
        System.out.println("Produced " + value); // may print after "Consumed" (race)
    }

    void consume() {
        while (!flag) { /* spin: box empty */ }
        int v = item;
        System.out.println("Consumed " + v);
        consumedSum += v;
        item = null;
        flag = false;
    }

    public static void main(String[] args) throws InterruptedException {
        ItcBusyWait box = new ItcBusyWait();
        Thread p = new Thread(() -> { for (int i = 1; i <= 5; i++) box.produce(i); });
        Thread c = new Thread(() -> { for (int i = 1; i <= 5; i++) box.consume(); });
        p.start(); c.start();
        p.join(); c.join();
        System.out.println("Sum consumed = " + box.consumedSum); // Sum consumed = 15
        // The Produced/Consumed lines: order may vary, e.g.
        // Produced 1
        // Consumed 1
        // Consumed 2   <- printed before "Produced 2" because of the race
        // Produced 2
        // ...
    }
}
```

### 3. Busy waiting inside `synchronized` = deadlock
The consumer is started first on purpose, so the hang is reproducible. Daemon threads plus `join` with a timeout let the program end anyway.
```java
public class ItcSyncSpinDeadlock {
    private boolean flag = false;

    synchronized void produce() {
        while (flag) { }
        flag = true;
        System.out.println("Produced"); // never reached
    }

    synchronized void consume() {
        while (!flag) { } // spins forever while HOLDING the lock
        flag = false;
        System.out.println("Consumed"); // never reached
    }

    public static void main(String[] args) throws InterruptedException {
        ItcSyncSpinDeadlock box = new ItcSyncSpinDeadlock();
        Thread consumer = new Thread(box::consume);
        Thread producer = new Thread(box::produce);
        consumer.setDaemon(true);
        producer.setDaemon(true);

        consumer.start();
        Thread.sleep(100);   // let the consumer grab the lock first
        producer.start();
        producer.join(300);  // give up after 300 ms

        System.out.println("consumer: " + consumer.getState()); // consumer: RUNNABLE (spinning)
        System.out.println("producer: " + producer.getState()); // producer: BLOCKED (can't get the lock)
    }
}
```

### 4. Correct producer-consumer with `wait()` / `notify()`
```java
public class ItcWaitNotifyBox {
    private Integer item;
    private boolean flag = false;

    synchronized void produce(int value) throws InterruptedException {
        while (flag) {       // guarded block: box full -> wait
            wait();          // releases this object's lock
        }
        item = value;
        flag = true;
        System.out.println("Produced " + value);
        notify();            // wake the consumer if it is waiting
    }

    synchronized int consume() throws InterruptedException {
        while (!flag) {      // box empty -> wait
            wait();
        }
        int v = item;
        item = null;
        flag = false;
        System.out.println("Consumed " + v);
        notify();            // wake the producer if it is waiting
        return v;
    }

    public static void main(String[] args) throws InterruptedException {
        ItcWaitNotifyBox box = new ItcWaitNotifyBox();
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 4; i++) { Thread.sleep(20); box.produce(i); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 4; i++) { Thread.sleep(10); box.consume(); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start(); consumer.start();
        producer.join(); consumer.join();
        // Always strictly alternating:
        // Produced 1
        // Consumed 1
        // Produced 2
        // Consumed 2
        // Produced 3
        // Consumed 3
        // Produced 4
        // Consumed 4
    }
}
```

### 5. Calling `wait()` without holding the lock
```java
// throws
public class ItcIllegalMonitor {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();
        lock.wait(); // java.lang.IllegalMonitorStateException: current thread is not owner
    }
}
```

### 6. Many producers and consumers with `notifyAll()`
```java
public class ItcNotifyAllBox {
    private Integer item;
    private int consumedSum = 0;

    synchronized void produce(int value) throws InterruptedException {
        while (item != null) wait();
        item = value;
        notifyAll(); // wake everyone; the wrong kind simply re-checks and waits again
    }

    synchronized void consume() throws InterruptedException {
        while (item == null) wait();
        consumedSum += item;
        item = null;
        notifyAll();
    }

    public static void main(String[] args) throws InterruptedException {
        ItcNotifyAllBox box = new ItcNotifyAllBox();
        Thread[] threads = new Thread[4];
        // 2 producers: P1 puts 1..50, P2 puts 51..100
        for (int p = 0; p < 2; p++) {
            int start = p * 50 + 1;
            threads[p] = new Thread(() -> {
                try { for (int v = start; v < start + 50; v++) box.produce(v); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        // 2 consumers, 50 items each
        for (int c = 2; c < 4; c++) {
            threads[c] = new Thread(() -> {
                try { for (int i = 0; i < 50; i++) box.consume(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        System.out.println("Sum consumed = " + box.consumedSum); // Sum consumed = 5050
    }
}
```
With `notify()` instead of `notifyAll()` this program can occasionally hang, as in the P1/P2/C1/C2 walk-through above.

### 7. Why the guard must be `while`, not `if`
Two consumers wait on an empty box. One item arrives and `notifyAll()` wakes both. With `if`, the second consumer doesn't re-check and reads `null`. With `while`, it goes back to waiting.
```java
public class ItcWhileVsIf {
    private Integer item;
    private boolean useWhile;
    private int nullReads = 0;

    synchronized void consume() throws InterruptedException {
        if (useWhile) {
            while (item == null) wait();
        } else {
            if (item == null) wait(); // BUG: no re-check after waking
        }
        if (item == null) nullReads++;
        item = null;
    }

    synchronized void produce(int v) {
        item = v;
        notifyAll();
    }

    synchronized boolean isEmpty() { return item == null; }

    static int run(boolean useWhile) throws InterruptedException {
        ItcWhileVsIf box = new ItcWhileVsIf();
        box.useWhile = useWhile;
        Runnable task = () -> {
            try { box.consume(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };
        Thread c1 = new Thread(task), c2 = new Thread(task);
        c1.start(); c2.start();
        while (c1.getState() != Thread.State.WAITING || c2.getState() != Thread.State.WAITING) {
            Thread.sleep(10); // wait until both consumers are in the wait set
        }
        box.produce(42); // one item, both consumers woken
        if (useWhile) {
            while (!box.isEmpty()) Thread.sleep(10); // first consumer took 42
            box.produce(43); // second item, so the waiting second consumer can finish
        }
        c1.join(); c2.join();
        return box.nullReads;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("if    -> null reads: " + run(false)); // if    -> null reads: 1
        System.out.println("while -> null reads: " + run(true));  // while -> null reads: 0
    }
}
```

### 8. `wait()` releases the lock, `sleep()` keeps it
```java
public class ItcSleepVsWait {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();

        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        waiter.start();
        Thread.sleep(100);
        System.out.println("waiter: " + waiter.getState()); // waiter: WAITING
        synchronized (lock) {                              // possible only because wait() released it
            System.out.println("main got the lock while waiter waits"); // main got the lock while waiter waits
            lock.notify();
        }
        waiter.join();

        Thread sleeper = new Thread(() -> {
            synchronized (lock) {
                try { Thread.sleep(400); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        Thread wantsLock = new Thread(() -> { synchronized (lock) { } });
        sleeper.start();
        Thread.sleep(100);
        wantsLock.start();
        Thread.sleep(100);
        System.out.println("sleeper: " + sleeper.getState());     // sleeper: TIMED_WAITING
        System.out.println("wantsLock: " + wantsLock.getState()); // wantsLock: BLOCKED (sleep kept the lock)
        sleeper.join();
        wantsLock.join();
    }
}
```

## Common Pitfalls / Tips
- **`synchronized` is not ordering.** It stops two threads being inside at once; it doesn't decide who goes first. Use a condition plus `wait`/`notify` (or a higher-level tool) for ordering.
- **Never spin while holding a lock.** That's a guaranteed deadlock as soon as the other side needs the same lock to change the condition.
- **Always `while (!condition) wait();`**, never `if`. It covers spurious wake-ups and "someone else got the item first".
- **Call `wait`/`notify`/`notifyAll` on the object you've locked.** `synchronized (lock) { lock.wait(); }`. Calling `this.wait()` while holding a different object's lock throws `IllegalMonitorStateException`.
- **Prefer `notifyAll()`** unless there's exactly one waiter type and any waiter can handle the event. `notify()` can wake the wrong thread and lose the wake-up.
- **A woken thread doesn't run immediately.** It goes WAITING → BLOCKED → RUNNABLE, needs the lock again, and continues after `wait()`. The notifier keeps the lock until it exits its synchronized block.
- **Don't swallow `InterruptedException`.** Either propagate it or restore the flag with `Thread.currentThread().interrupt()`.
- **`volatile` isn't needed inside `synchronized`.** Lock acquire/release already give visibility. `volatile` matters for the lock-free busy-wait variant.
- **In real code, reach for `java.util.concurrent`.** `BlockingQueue` (`ArrayBlockingQueue`, `LinkedBlockingQueue`) solves producer-consumer directly, and `Lock` + `Condition` (next video) gives separate wait sets for "not full" and "not empty".

## Review Questions
1. Why doesn't making `produce()` and `consume()` `synchronized` fix the producer-consumer problem?
2. What is busy waiting, and why does the busy-wait version need `volatile`?
3. Why does busy waiting inside `synchronized` methods deadlock?
4. List the three things `wait()` does. Which one prevents deadlock?
5. After `notify()`, which state does the woken thread go to first, and where does it resume execution?
6. Why do `wait()`/`notify()` throw `IllegalMonitorStateException` outside a synchronized block?
7. Describe a scenario with two producers and two consumers where `notify()` leaves every thread waiting. How does `notifyAll()` fix it?
8. Why are `wait`/`notify`/`notifyAll` defined in `Object` rather than `Thread`, and how does `sleep()` differ from `wait()`?
9. What is a spurious wake-up, and why does it require a `while` loop (guarded block)?

<details>
<summary>Answers</summary>

1. It only guarantees that one method runs at a time. It doesn't control order, so the consumer can still enter first and read `null`, or the producer can enter twice in a row and overwrite an unconsumed value.
2. A thread repeatedly checks a condition in an empty loop, using CPU without doing useful work. Without `volatile`, the spinning thread may keep reading a stale cached value of the flag and never see the other thread's update.
3. The thread that enters first (say, the consumer with an empty box) holds the object's lock while spinning. The producer needs the same lock to set the flag, so it stays BLOCKED forever and the condition never changes.
4. It releases the object's monitor lock, moves the thread to WAITING (the wait set), and keeps it there until it's notified or interrupted. Releasing the lock is the step that prevents deadlock, because it lets the other thread enter and change the condition.
5. It goes to BLOCKED, because it has to re-acquire the lock it released. Once it gets the lock it becomes RUNNABLE and resumes at the statement after `wait()`. Inside a `while` loop, that means it re-checks the condition.
6. `wait()` must release the monitor and `notify()` must manipulate that monitor's wait set. Both only make sense for a thread that owns the monitor, so the JVM throws if the current thread doesn't own it.
7. C1 and C2 wait on an empty box. P1 produces and wakes C1. P2 gets the lock first, finds the box full, and waits. C1 consumes and `notify()` wakes C2, which finds the box empty and waits again. Now C2 and P2 both wait and nobody will notify them. `notifyAll()` wakes both C2 and P2, so P2 can still produce.
8. The lock and its wait set belong to the object, and threads coordinate through an object's monitor, so the methods live on `Object`. `sleep()` pauses for a fixed time (TIMED_WAITING), keeps all held locks, and works anywhere. `wait()` releases that object's lock, waits until notified (WAITING), and must be called while holding the lock.
9. A thread can come out of `wait()` without being notified. With `while`, it re-checks the condition after waking and goes back to waiting if the condition still isn't met. With `if`, it would carry on and act on invalid state (e.g. consume `null`).
</details>

## Further Study
- `Lock` and `Condition` (`await`/`signal`/`signalAll`), which give separate "notFull"/"notEmpty" wait sets
- `BlockingQueue` implementations: `ArrayBlockingQueue`, `LinkedBlockingQueue`, `SynchronousQueue`
- Bounded-buffer producer-consumer (capacity > 1) and the circular buffer
- Thread states in detail: NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED
- Java Memory Model: happens-before for `synchronized`, `volatile`, and `Thread.join()`
- Interruption: `Thread.interrupt()`, `InterruptedException`, and cooperative cancellation
- Other coordination tools: `CountDownLatch`, `CyclicBarrier`, `Semaphore`, `Exchanger`

---

[← #51](51-monitor-locks-synchronized.md) · [Index](ReadMe.md) · [#53 →](53-java-locks-reentrant-readwrite-stamped-semaphore.md)
