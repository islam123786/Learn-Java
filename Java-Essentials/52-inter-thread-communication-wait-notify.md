# 52 · Inter-Thread Communication · wait(), notify(), notifyAll()
> Full notes: [52-inter-thread-communication-wait-notify.md](../52-inter-thread-communication-wait-notify.md) · [▶ Video](https://youtu.be/EZS19NLnsvc)

## In one look
- `synchronized` gives **mutual exclusion**: only one thread is inside at a time. It does **not** control **order**, so it can't stop a consumer from running before the producer. That's what **inter-thread communication** is for.
- **`wait()`** releases the lock and sleeps until notified. **`notify()`** wakes **one** waiting thread, and **`notifyAll()`** wakes **all** of them.
- The 3 rules:
  1. Call them only **while holding the object's lock**.
  2. Always wait inside a **`while`** loop.
  3. Prefer **`notifyAll()`**.

| Approach | Correct? | Problem |
| :--- | :--- | :--- |
| No coordination | ❌ | the consumer reads `null`, and the producer overwrites unread values |
| `synchronized` only | ❌ | one at a time, but still in **any order** |
| Busy waiting + `volatile` | ⚠️ works | **wastes CPU**, and the print order can be wrong |
| Busy waiting + `synchronized` | ❌ | **deadlock**: the spinner holds the lock forever |
| `synchronized` + `while`/`wait()` + `notify()` | ✅ | correct for 1 producer + 1 consumer |
| `synchronized` + `while`/`wait()` + `notifyAll()` | ✅ | correct for **many** producers and consumers |

---

## Why `synchronized` isn't enough
- **Recap:** threads can interfere through a shared resource, so we lock the critical section with `synchronized` (#51).
- But some problems are about **coordination** ("wait until the data is ready", "tell the others it's ready"), not just about keeping threads out.
- For those, Java has three methods on **`Object`**: `wait()`, `notify()` and `notifyAll()`.

## The producer-consumer problem
- A **producer** puts a value into a shared **box**. A **consumer** takes it out, leaving the box empty again. Each one runs on its own thread (T1 and T2).
```
 Producer (T1) ──put──►  ┌─────┐  ──take──► Consumer (T2)
                         │ box │
                         └─────┘
```
- Multithreaded code is **non-deterministic**, so two bad cases are possible:
  1. **The consumer runs first** and "consumes" **`null`**.
  2. **The producer runs twice in a row** and overwrites `5` with `10` before 5 was consumed, so **data is lost**.
- **The rules we need:** don't consume when the box is **empty**, and don't produce when it's **full**.
- **Real world:** a document creator (producer) and a printer (consumer), message queues, request buffers, logging pipelines.

---

## Code: the broken version

The `Box` has:
- **`Integer item`**: a wrapper type, so it can be `null` (empty)
- **`boolean flag`**: `true` means an item is present

`produce()` sets the item and the flag. `consume()` reads the item and then clears it. Nothing makes the threads take turns.

```java
public class BrokenBox {
    private Integer item;          // Integer, so it can be null (= empty)
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
        BrokenBox box = new BrokenBox();
        Thread producer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) { sleep(100); box.produce(i); }   // slower producer
        });
        Thread consumer = new Thread(() -> {
            for (int i = 1; i <= 5; i++) { sleep(70); box.consume(); }     // faster consumer
        });
        producer.start(); consumer.start();
        producer.join(); consumer.join();
    }

    static void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }
}
```
**Sample output (it varies between runs):**
```
Consumed null      ← consumer ran before anything was produced
Produced 1
Consumed 1
Consumed null      ← the consumer is faster, so it found the box empty again
Produced 2
Consumed 2
Consumed null
Produced 3
...
```
**What's going on:**
- With **one** call each, it's a 50/50 race: sometimes it works, and sometimes the consumer reads `null`.
- With a **loop** and **different speeds** (producer 100 ms, consumer 70 ms), the consumer keeps reaching the box before the next item is there, so it reads **`null` many times**.
- If the producer were faster, you'd see the other bug: **two "Produced" lines in a row**, which means a value was overwritten before anyone consumed it.
- **The root cause:** neither thread checks the box's state before acting, and nothing makes one wait for the other.

## Why adding `synchronized` alone doesn't fix it
Making both methods `synchronized` only guarantees that **one of them runs at a time**. It doesn't decide **which one goes first**.

```java
public class SyncOnlyBox {
    private Integer item;

    synchronized void produce(int value) { item = value; System.out.println("Produced " + value); }
    synchronized void consume()          { System.out.println("Consumed " + item); item = null; }

    public static void main(String[] args) throws InterruptedException {
        SyncOnlyBox box = new SyncOnlyBox();
        Thread consumer = new Thread(box::consume);
        Thread producer = new Thread(() -> box.produce(5));
        consumer.start();                // the consumer happens to get the lock first...
        Thread.sleep(50);
        producer.start();
        consumer.join(); producer.join();
        // Consumed null   ← still wrong: synchronized stopped overlap, not wrong order
        // Produced 5
    }
}
```
- The problem isn't locking. It's **communication**: "don't consume if nothing's there, and don't produce if something's already there".

---

## The naive fix: busy waiting
**The idea:** before acting, loop and keep checking until the box is in the right state.
- **Producer:** `while (flag) { /* do nothing */ }`, which means wait while the box is full.
- **Consumer:** `while (!flag) { }`, which means wait while the box is empty.

```java
public class BusyWaitBox {
    private volatile Integer item;            // volatile: the spinning thread must see updates
    private volatile boolean flag = false;
    private int consumedSum = 0;              // only written by the consumer thread

    void produce(int value) {
        while (flag) { /* spin: box still full */ }
        item = value;
        flag = true;
        System.out.println("Produced " + value);   // may print AFTER "Consumed" (see below)
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
        BusyWaitBox box = new BusyWaitBox();
        Thread p = new Thread(() -> { for (int i = 1; i <= 5; i++) box.produce(i); });
        Thread c = new Thread(() -> { for (int i = 1; i <= 5; i++) box.consume(); });
        p.start(); c.start();
        p.join(); c.join();
        System.out.println("Sum consumed = " + box.consumedSum);   // Sum consumed = 15 (data is correct)
    }
}
```
- **`volatile` is required.** Without it, a spinning thread may keep reading a **stale copy** of `flag` and never see the change (the **visibility problem**, #50).
- **The cost is CPU.** A spinning thread keeps getting CPU time while doing nothing useful. If the producer were a slow external server that takes 2 s, the consumer would spin for 2 s asking "is it there yet?". That's **busy waiting**: busy only at waiting.

### Busy waiting in practice: two more problems
**1. The print order can be wrong.** The *data* is right (the sum is 15), but sometimes you see:
```
Produced 1
Consumed 1
Consumed 2      ← printed before "Produced 2"
Produced 2
```
The producer set `flag = true`, got **context-switched out before its `println`**, and the consumer ran in between.

**2. "Fixing" that with `synchronized` causes a deadlock.** The usual fix for a race is to make both methods `synchronized`, but here that **hangs forever**:
```java
public class SyncSpinDeadlock {
    private boolean flag = false;

    synchronized void produce() {
        while (flag) { }
        flag = true;
        System.out.println("Produced");          // never reached
    }

    synchronized void consume() {
        while (!flag) { }                        // spins forever while HOLDING the lock
        flag = false;
        System.out.println("Consumed");          // never reached
    }

    public static void main(String[] args) throws InterruptedException {
        SyncSpinDeadlock box = new SyncSpinDeadlock();
        Thread consumer = new Thread(box::consume);
        Thread producer = new Thread(box::produce);
        consumer.setDaemon(true);                // daemons, so this demo can still exit
        producer.setDaemon(true);

        consumer.start();
        Thread.sleep(100);                       // let the consumer grab the lock first
        producer.start();
        producer.join(300);                      // give up after 300 ms

        System.out.println("consumer: " + consumer.getState());   // consumer: RUNNABLE (spinning)
        System.out.println("producer: " + producer.getState());   // producer: BLOCKED (can't get the lock)
    }
}
```
- The consumer enters first (the box is empty) and **holds the box's lock** while it spins.
- The producer needs **that same lock** to set the flag, so it stays **BLOCKED** forever.
- The condition can never change, and **nothing prints**. Whenever the consumer happens to go first, the program is stuck.
- **Lesson:** never spin-wait while holding a lock. The waiting thread has to **let go of the lock**, and that's exactly what `wait()` does.

---

## Waiting properly: shared resource, condition, who waits
- Java's thread lifecycle has a **WAITING** state. It's different from **TIMED_WAITING**, which you get from `Thread.sleep(ms)`. `wait()` puts a thread into WAITING, and `notify()`/`notifyAll()` take it out.
- **Every coordination problem has three pieces:**

| Piece | In producer-consumer |
| :--- | :--- |
| **Shared resource** | the box (`item`) |
| **Condition** | does the box have an item? (`flag`) |
| **Who waits** | the **consumer** waits while it's **empty**, and the **producer** waits while it's **full** |

## What `wait()` does
1. **Releases the monitor lock** the thread holds on that object.
2. Moves the thread to the **WAITING** state (into the object's **wait set**).
3. Keeps it there **until another thread wakes it** (with `notify`/`notifyAll`), not for a fixed time like `sleep`. Interrupts and rare spurious wake-ups can also end it.

- **Releasing the lock is the crucial step.** Without it, the waiting consumer would still hold the lock, the producer could never get in to call `notify()`, and you'd have the busy-wait deadlock again.

**The flow:**
```
Consumer: lock → box empty → wait()  (releases the lock, goes to WAITING)
Producer:        lock → fill box, flag = true → notify() → unlock
Consumer:  woken → re-gets the lock → re-checks the while (now false) → consume → notify() → unlock
```
- The consumer calls `notify()` too, in case a **producer** is waiting for the box to empty.

## Rule: only inside `synchronized`
- `wait()`, `notify()` and `notifyAll()` must be called by a thread that **owns the object's monitor**. Otherwise you get **`IllegalMonitorStateException`**.
- **Why:**
  - `wait()` has to **release** a lock, and it can't release one you don't hold.
  - `notify()` works on that object's **wait set**, which belongs to the lock you must hold.
- `wait()` throws the checked **`InterruptedException`**, so declare it or catch it.

```java
// throws
public class IllegalMonitor {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();
        lock.wait();     // java.lang.IllegalMonitorStateException: current thread is not owner
    }
}
```
- **Call them on the object you locked:** `synchronized (lock) { lock.wait(); }`. Calling `this.wait()` while holding a **different** object's lock also throws.

---

## Internals: the monitor, its owner and the wait set
Each object's monitor (intrinsic lock) keeps track of three things:
```
                   ┌────────────────────── monitor of `box` ──────────────────────┐
                   │                                                              │
  BLOCKED threads  │   ENTRY SET            OWNER              WAIT SET           │  WAITING threads
  (want the lock)  │   [ P1, ... ]  ──►  [ one thread ]  ──►  [ C1, C2, ... ]     │  (called wait())
                   │        ▲            holds the lock   wait()   │              │
                   │        └──────────── notify() / notifyAll() ───┘              │
                   └──────────────────────────────────────────────────────────────┘
```
- **Owner:** the thread that holds the lock right now.
- **Entry set:** threads that are **BLOCKED** trying to get the lock (seen in #51).
- **Wait set:** threads that called **`wait()`**. They're **WAITING**.

**Example:** consumer T2 takes the box's lock (owner = T2), finds the box empty, and calls `wait()`. T2 moves to the **wait set**, and the owner becomes **empty**. Producer T1, which was BLOCKED in the entry set, can now get the lock.

## What `notify()` does
1. Picks **one random** thread from the wait set. Which one depends on the JVM/OS, so it's **non-deterministic**.
2. Moves it to **BLOCKED**, **not** straight to RUNNABLE. It gave up the lock when it waited, so it has to **compete for the lock again**.
3. Once it gets the lock, it becomes RUNNABLE and continues from **the line after `wait()`**, not from the top of the method. Because `wait()` sits in a `while` loop, it **re-checks the condition** first.

```
WAITING ──notify()──► BLOCKED ──gets the lock──► RUNNABLE ──► continues right after wait()
```
- `notify()` also has to be called inside `synchronized`.
- **The notifier keeps the lock** until it leaves its synchronized block, so the woken thread can only continue after that.

---

## Code: the working version

**The changes from the broken or busy-wait version:**
- Replace "do nothing" with **`wait()`** in both loops.
- Add **`notify()`** after producing and after consuming.
- Handle **`InterruptedException`**.

```java
public class WaitNotifyBox {
    private Integer item;
    private boolean flag = false;            // no volatile needed: every access is inside synchronized

    synchronized void produce(int value) throws InterruptedException {
        while (flag) {                       // guarded block: box full → wait
            wait();                          // releases this object's lock, goes to WAITING
        }
        item = value;                        // only reached when the box is empty
        flag = true;
        System.out.println("Produced " + value);
        notify();                            // wake the consumer if it's waiting
    }

    synchronized int consume() throws InterruptedException {
        while (!flag) {                      // box empty → wait
            wait();
        }
        int v = item;                        // only reached when the box is full
        item = null;
        flag = false;
        System.out.println("Consumed " + v);
        notify();                            // wake the producer if it's waiting
        return v;
    }

    public static void main(String[] args) throws InterruptedException {
        WaitNotifyBox box = new WaitNotifyBox();
        Thread producer = new Thread(() -> {
            try {
                for (int i = 1; i <= 4; i++) { Thread.sleep(100); box.produce(i); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        Thread consumer = new Thread(() -> {
            try {
                for (int i = 1; i <= 4; i++) { Thread.sleep(70); box.consume(); }
            } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start(); consumer.start();
        producer.join(); consumer.join();
        // Always strictly alternating, on every run:
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
**Why this works:**
- **The consumer is faster (70 ms), but it never reads `null`.** When it finds the box empty it calls `wait()`, which **releases the lock** and uses **no CPU**, until the producer's `notify()` wakes it.
- **The producer can't overwrite.** If the box is still full, it waits until the consumer's `notify()`.
- **There's no deadlock**, because a waiting thread gives up the lock, so the other side can always get in and change the condition.
- **The print order is always correct**, because the `println` happens inside the `synchronized` method, together with the state change.
- The output stays strictly alternating even when both sleeps are set to the same value (100 ms).
- The video keeps `volatile` on the fields. That's harmless but **not needed** here, because every access is inside `synchronized`, and the lock already guarantees visibility.

**The same pattern with an explicit lock object** (useful when you don't want to lock `this`):
```java
public class LockObjectBox {
    private final Object lock = new Object();
    private Integer item;

    void produce(int v) throws InterruptedException {
        synchronized (lock) {
            while (item != null) lock.wait();     // wait/notify on the SAME object you locked
            item = v;
            lock.notifyAll();
        }
    }

    int consume() throws InterruptedException {
        synchronized (lock) {
            while (item == null) lock.wait();
            int v = item;
            item = null;
            lock.notifyAll();
            return v;
        }
    }

    public static void main(String[] args) throws InterruptedException {
        LockObjectBox box = new LockObjectBox();
        Thread p = new Thread(() -> {
            try { for (int i = 1; i <= 3; i++) box.produce(i * 10); }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        p.start();
        for (int i = 0; i < 3; i++) System.out.println("got " + box.consume());   // main is the consumer
        p.join();
        // got 10
        // got 20
        // got 30
    }
}
```

---

## `notifyAll()`, and why `notify()` can be dangerous
- **`notifyAll()`:** **every** thread in the wait set moves to BLOCKED. They all compete for the lock, and only one gets it at a time. It has a bit more overhead than `notify()`, but it's **safer**, so it's generally preferred.

### The lost wake-up with `notify()`: 2 producers, 2 consumers, 1 box

| Step | What happens | Box | Wait set |
| :--- | :--- | :--- | :--- |
| 1 | C1 enters, finds the box empty, waits. C2 enters, finds it empty, waits. | empty | C1, C2 |
| 2 | P1 produces 10, and `notify()` wakes **C1**. P1 finishes. | 10 | C2 |
| 3 | **P2 gets the lock before C1**, sees the box full, and waits. | 10 | C2, P2 |
| 4 | C1 gets the lock, consumes 10, and `notify()` randomly wakes **C2** (the **wrong kind** of thread). | empty | P2 |
| 5 | C2 sees the box empty and waits again. | empty | **C2, P2** |
| 6 | **Nobody is left to notify anyone.** Everything hangs. | empty | C2, P2 (forever) |

- **With `notifyAll()`**, step 4 wakes **both C2 and P2**. If C2 runs first it just goes back to waiting, and P2 still gets its turn and produces. **Someone can always make progress.**
- **Rule of thumb:** `notify()` is fine only when there's **exactly one thread of each kind**, or when all the waiters are interchangeable. **Otherwise, use `notifyAll()`.**

```java
public class NotifyAllBox {
    private Integer item;
    private int consumedSum = 0;

    synchronized void produce(int value) throws InterruptedException {
        while (item != null) wait();
        item = value;
        notifyAll();      // wake everyone. The wrong kind just re-checks and waits again.
    }

    synchronized void consume() throws InterruptedException {
        while (item == null) wait();
        consumedSum += item;
        item = null;
        notifyAll();
    }

    public static void main(String[] args) throws InterruptedException {
        NotifyAllBox box = new NotifyAllBox();
        Thread[] threads = new Thread[4];
        for (int p = 0; p < 2; p++) {                      // 2 producers: 1..50 and 51..100
            int start = p * 50 + 1;
            threads[p] = new Thread(() -> {
                try { for (int v = start; v < start + 50; v++) box.produce(v); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        for (int c = 2; c < 4; c++) {                      // 2 consumers, 50 items each
            threads[c] = new Thread(() -> {
                try { for (int i = 0; i < 50; i++) box.consume(); }
                catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        for (Thread t : threads) t.start();
        for (Thread t : threads) t.join();
        System.out.println("Sum consumed = " + box.consumedSum);   // Sum consumed = 5050 (1 + 2 + … + 100)
    }
}
```
With `notify()` instead of `notifyAll()`, this program can **occasionally hang**, exactly as in the table above.

---

## Interview question: why are these methods in `Object` and not `Thread`?
- A thread waits on and notifies an **object's monitor**.
- The **lock and its wait set belong to the object**, so the methods that put threads into that wait set and take them out live on `Object` too.
- Any object can be used for coordination: `box.wait()`, `lock.notifyAll()`.

## `sleep()` vs `wait()`

| | `Thread.sleep(ms)` | `obj.wait()` |
| :--- | :--- | :--- |
| Defined in | `Thread` (static) | `Object` |
| State | **TIMED_WAITING** | **WAITING** (`wait(ms)` gives TIMED_WAITING) |
| Wakes up when | the time is up | `notify()`/`notifyAll()`, an interrupt, or a timeout with `wait(ms)` |
| Locks | **keeps every lock** it holds | **releases `obj`'s lock** |
| Where you can call it | anywhere | only while **holding `obj`'s lock** |
| Used for | pausing | **coordinating** with other threads |

```java
public class SleepVsWait {
    public static void main(String[] args) throws InterruptedException {
        Object lock = new Object();

        // wait() RELEASES the lock
        Thread waiter = new Thread(() -> {
            synchronized (lock) {
                try { lock.wait(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
        });
        waiter.start();
        Thread.sleep(100);
        System.out.println("waiter: " + waiter.getState());                 // waiter: WAITING
        synchronized (lock) {                                               // only possible because wait() released it
            System.out.println("main got the lock while waiter waits");
            lock.notify();
        }
        waiter.join();

        // sleep() KEEPS the lock
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
        System.out.println("sleeper: " + sleeper.getState());               // sleeper: TIMED_WAITING
        System.out.println("wantsLock: " + wantsLock.getState());           // wantsLock: BLOCKED (sleep kept the lock)
        sleeper.join(); wantsLock.join();
    }
}
```

---

## Spurious wake-ups and guarded blocks
- A waiting thread can occasionally wake up **without any `notify()`**. This is a **spurious wake-up**. It's rare, and it's caused by OS/CPU-level behavior that Java doesn't fully control.
- That's why we always write **`while (!condition) wait();`**, **never `if`**. After waking, the `while` **re-checks the condition** and goes back to waiting if it still isn't met. With `if`, the thread would carry on and, for example, **consume `null`**.
- `while` + `wait()` is called a **guarded block**.
- The same `while` also protects against the **`notifyAll()` case**: several threads wake up, and an earlier one has **already taken the item**.

### Demo: `if` vs `while`
Two consumers wait on an empty box. One item arrives, and `notifyAll()` wakes both. With `if`, the second consumer doesn't re-check and reads `null`.
```java
public class WhileVsIf {
    private Integer item;
    private boolean useWhile;
    private int nullReads = 0;

    synchronized void consume() throws InterruptedException {
        if (useWhile) {
            while (item == null) wait();      // ✅ re-checks after every wake-up
        } else {
            if (item == null) wait();         // ❌ BUG: no re-check after waking
        }
        if (item == null) nullReads++;        // count bad reads
        item = null;
    }

    synchronized void produce(int v) { item = v; notifyAll(); }
    synchronized boolean isEmpty()   { return item == null; }

    static int run(boolean useWhile) throws InterruptedException {
        WhileVsIf box = new WhileVsIf();
        box.useWhile = useWhile;
        Runnable task = () -> {
            try { box.consume(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        };
        Thread c1 = new Thread(task), c2 = new Thread(task);
        c1.start(); c2.start();
        while (c1.getState() != Thread.State.WAITING || c2.getState() != Thread.State.WAITING) {
            Thread.sleep(10);                 // wait until both consumers are in the wait set
        }
        box.produce(42);                      // ONE item, but BOTH consumers are woken
        if (useWhile) {
            while (!box.isEmpty()) Thread.sleep(10);
            box.produce(43);                  // a second item, so the other waiting consumer can finish
        }
        c1.join(); c2.join();
        return box.nullReads;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println("if    -> null reads: " + run(false));   // if    -> null reads: 1
        System.out.println("while -> null reads: " + run(true));    // while -> null reads: 0
    }
}
```

---

## Real-world view
- **A document creator (producer) and a printer (consumer)** is the textbook example. Anything that produces values on one side and consumes them on the other is a producer-consumer problem: print queues, message queues, request buffers, logging pipelines.
- **In real code, use `java.util.concurrent`.** A **`BlockingQueue`** solves producer-consumer for you, with the waiting and notifying built in:
```java
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class BlockingQueueDemo {
    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> box = new ArrayBlockingQueue<>(1);   // capacity 1, just like our box

        Thread producer = new Thread(() -> {
            try { for (int i = 1; i <= 3; i++) { box.put(i); System.out.println("Produced " + i); } }
            catch (InterruptedException e) { Thread.currentThread().interrupt(); }
        });
        producer.start();
        int sum = 0;
        for (int i = 0; i < 3; i++) sum += box.take();   // take() waits while the queue is empty
        producer.join();
        System.out.println("sum = " + sum);              // sum = 6
    }
}
```
- `put()` waits while the queue is **full**, and `take()` waits while it's **empty**.
- `Lock` + `Condition` (#53) gives **separate wait sets** for "not full" and "not empty".

---

## Pitfalls and tips
- **`synchronized` doesn't control order.** It stops two threads from being inside at once, but it doesn't decide who goes first. Use a condition plus `wait`/`notify` (or a higher-level tool) for ordering.
- **Never spin while holding a lock.** That deadlocks as soon as the other side needs the same lock to change the condition.
- **Always write `while (!condition) wait();`, never `if`.** It covers spurious wake-ups and "someone else got the item first".
- **Call `wait`/`notify`/`notifyAll` on the object you've locked**: `synchronized (lock) { lock.wait(); }`
- **Prefer `notifyAll()`** unless there's exactly one kind of waiter and any waiter can handle the event.
- **A woken thread doesn't run immediately.** It goes WAITING → BLOCKED → RUNNABLE, needs the lock again, and continues **after** `wait()`. The notifier keeps the lock until it leaves its synchronized block.
- **Don't swallow `InterruptedException`.** Either pass it on or restore the flag with `Thread.currentThread().interrupt()`.
- **You don't need `volatile` inside `synchronized`.** The lock already gives visibility. `volatile` matters only for the lock-free busy-wait version.

## Quick self-test
1. **Why doesn't `synchronized` alone fix producer-consumer?** It allows only one thread at a time, but in **any order**. The consumer can still go first and read `null`.
2. **Why does the busy-wait version need `volatile`?** Otherwise the spinning thread may keep reading a stale `flag` forever.
3. **Why does busy waiting inside `synchronized` deadlock?** The spinner holds the lock, and the other thread needs that lock to change the condition, so it stays BLOCKED forever.
4. **What 3 things does `wait()` do?** It releases the lock, moves the thread to WAITING, and stays there until notified. **Releasing the lock** is what prevents deadlock.
5. **After `notify()`, where does the woken thread go, and where does it resume?** First to **BLOCKED** (it needs the lock again), then it continues from the line **after `wait()`**.
6. **Why do you get `IllegalMonitorStateException` outside `synchronized`?** `wait()` must release, and `notify()` must manage, a monitor the thread owns.
7. **How can `notify()` hang with 2 producers and 2 consumers?** It can wake the wrong kind of thread, leaving C2 and P2 both waiting with nobody to wake them. `notifyAll()` wakes everyone, so someone can always proceed.
8. **Why are these methods in `Object`, and how is `sleep` different from `wait`?** The lock and wait set belong to the object. `sleep` keeps locks and wakes on time. `wait` releases the lock and wakes on notify.
9. **Why use `while` instead of `if`?** Spurious wake-ups, and other woken threads taking the item first. The thread must re-check before acting.

**Remember:** `synchronized` stops overlap, and **`wait`/`notify`** handle **order**. The pattern is `synchronized` → **`while (!condition) wait();`** → change the state → **`notifyAll();`**. `wait()` **releases** the lock, but `sleep()` **keeps** it. In production, use **`BlockingQueue`**.
