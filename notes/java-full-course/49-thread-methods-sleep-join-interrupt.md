# Thread Methods | sleep, join, yield, interrupt, isAlive, priority & more | Java Full Course #49
> Source: https://youtu.be/ZPxJby0GeOQ · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 39:04. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They are rougher than usual (within a couple of minutes).

## TL;DR
- **`Thread.sleep(ms)`** puts the *current* thread into TIMED_WAITING for that long and **keeps any locks it holds**. It throws the checked `InterruptedException`.
- **`t.join()`** makes the *calling* thread wait (WAITING) until `t` finishes. **`t.join(ms)`** waits at most `ms` (TIMED_WAITING). This is the basic tool for making output order deterministic.
- **`Thread.yield()`** is only a *hint* that the current thread is willing to give up the CPU. The scheduler may ignore it, so it isn't used in production.
- **`t.interrupt()`** doesn't stop a thread. It sets an **interrupt flag** that the thread must check (`isInterrupted()`) to stop gracefully. A sleeping/waiting/joining thread that gets interrupted wakes up with `InterruptedException`. `Thread.interrupted()` reads **and clears** the flag.
- Also covered: `isAlive()`, `currentThread()`, `getName/setName`, priorities 1–10 (just a hint), and **daemon threads**, which the JVM kills as soon as all user threads finish (GC runs on daemon threads).

## Key Concepts

**`sleep(ms)`.** A static method that pauses the current thread. It goes RUNNABLE → TIMED_WAITING → RUNNABLE. *Why it matters:* you'll use it constantly in demos, and it's important to know that it doesn't release locks.

**`join()` / `join(ms)`.** The calling thread waits for another thread to terminate, either with no limit or up to a timeout. *Why it matters:* it gives order to non-deterministic code ("finish T1 first, then continue").

**`yield()`.** A static hint: "I'm willing to give my CPU time to another runnable thread of the same priority." The thread stays RUNNABLE. *Why it matters:* it shows that Java can only *suggest* scheduling decisions to the OS.

**Interrupt flag.** Every thread has a boolean "interrupted" status, `false` by default. `interrupt()` sets it, and the thread decides what to do. *Why it matters:* this is Java's cooperative cancellation mechanism, and executors use it to stop thread pools.

**`isInterrupted()` vs `Thread.interrupted()`.** The first returns the flag of the thread it is called on. The second is static, returns the *current* thread's flag, and **resets it to false**. *Why it matters:* this is an easy interview question and a common source of bugs.

**`isAlive()`.** `true` between `start()` and termination. `false` before start and after the thread ends. *Why it matters:* it is a quick way to check whether a thread is still running.

**Priority.** An int from `MIN_PRIORITY` (1) to `MAX_PRIORITY` (10), default `NORM_PRIORITY` (5). It is only a hint to the OS scheduler. *Why it matters:* code must never depend on it.

**Daemon thread.** A background thread that doesn't keep the JVM alive. Mark it with `setDaemon(true)` **before** `start()`. *Why it matters:* it suits housekeeping work (like garbage collection) that should end when the program ends.

## Detailed Notes

### `Thread.sleep()` [01:40]
- *(The first ~1.5 minutes are a course announcement.)*
- `Thread.sleep(2000)` inside `main` prints "main thread starts", pauses for 2 s, then prints "main thread ends". The thread that sleeps is always the **current** one, because `sleep` is static.
- It must be wrapped in `try/catch (InterruptedException e)` because `InterruptedException` is **checked** (otherwise you get an "unhandled exception" compile error).
- State path: RUNNABLE → **TIMED_WAITING** → RUNNABLE, and then it runs when the CPU picks it again. As covered in #48, "running" is part of RUNNABLE in Java.
- **Sleeping doesn't release locks.** If the thread is inside a critical section, every other thread waiting for that lock keeps waiting for the whole sleep.

### `join()` [03:50]
- Without coordination: `main` starts T1 (which prints "Thread-0 starts") and prints "main thread ends". The order is non-deterministic, but usually `main` finishes first because the new thread isn't scheduled immediately. With a 2 s sleep inside T1, "main thread ends" almost always comes before "Thread-0 starts".
- **`t1.join()` called in `main`** means "main, go to WAITING until T1 terminates". Everything after that line runs only after T1 is done. Output: main starts → (2 s) → Thread-0 starts → main ends.
  - State path: main RUNNABLE → WAITING. T1 RUNNABLE → TERMINATED, then main WAITING → RUNNABLE → TERMINATED.
- `join` also throws `InterruptedException`. The demo adds `throws InterruptedException` to `main` for convenience, which the video notes is a bad habit in real code.
- **Overload `join(ms)`**: wait *at most* that long. With `t1.join(1000)` and a 2 s sleep inside T1: main starts → (1 s) → main ends → (1 s) → Thread-0 starts. A timed join puts the caller in **TIMED_WAITING**, not WAITING.

### `Thread.yield()` [09:45]
- Static. The current thread says: "if another runnable thread with the **same priority** wants the CPU, I'm willing to give it my time."
- The thread **doesn't** go to WAITING, TIMED_WAITING or BLOCKED. It stays **RUNNABLE**.
- It is only a **request/suggestion to the OS**, which can ignore it completely. Java runs inside the JVM, and the OS and CPU decide what actually runs.
- Demo: T1 and T2 each print 1..10. Without `yield`, the output is mixed in some random order. With `Thread.yield()` after each print in T1, in one run T1 printed 1, then T2 printed 1–9, then T1 printed 2, and so on. T2 got most of the CPU. There is no guarantee this happens.
- It is not used in production. It can be handy when testing scheduling or context-switching behavior.

### `interrupt()` [15:40]
- `t1.interrupt()` is an **instance** method. It *sends a signal* to T1 that it should stop what it is doing. It does **not** force T1 to stop.
- Internally every thread has an **interrupt flag**, `false` by default. `interrupt()` sets it to `true`, and that's all.
- Demo 1: T1 runs `while (true) println("running")`. Calling `t1.interrupt()` changes nothing, and it keeps printing forever (the video stops it by hand).
- Demo 2: the loop condition becomes `while (!Thread.currentThread().isInterrupted())`.
  - Interrupting right after `start()` usually means nothing prints at all.
  - With `main` sleeping 2 s before calling `t1.interrupt()`, "running" prints for about 2 s and then the program ends on its own. This is **handling interruption gracefully**.
- Uses: run a thread **until an external condition** happens, **cancel a long-running background task**, and **stopping thread pools** (executors interrupt their workers on `shutdownNow()`).

### `isInterrupted()` vs `interrupted()` [22:15]
- `isInterrupted()` returns the flag's value and **doesn't change it**.
- `interrupted()` returns the flag's value **and resets it to `false`**.
- *(Precision: `Thread.interrupted()` is **static** and always checks the **current** thread. `isInterrupted()` is an instance method you can call on any thread.)*
- Why `sleep`/`join`/`wait` throw `InterruptedException`: if a thread is sleeping, joining or waiting (TIMED_WAITING or WAITING) and someone calls `interrupt()` on it, it wakes up immediately with `InterruptedException`. The compiler can't know whether that will happen, so the exception is checked.
  - *(Precision: the video phrases this as "you cannot call interrupt on a sleeping thread". You can, and that is exactly how you wake it up early. Also note that when the exception is thrown, the flag is **cleared**, so code that catches it usually calls `Thread.currentThread().interrupt()` to restore the flag.)*

### `isAlive()` [24:40]
- A thread is alive from `start()` until it terminates. It is "dead" both **before it starts** and **after it ends**, like a life between birth and death.
- Demo: T1 sleeps 2 s. `isAlive()` before `start()` is `false`. Right after `start()` it is `true`, even while T1 sleeps. After `main` sleeps 3 s it is `false`, because T1 has terminated.

### `currentThread()`, `getName()`, `setName()` [27:20]
- `Thread.currentThread()` returns a reference to the running thread, and you can call any method on it (`getName()`, `getPriority()`, and the deprecated `getId()`).
- The default name is `Thread-0`. `t1.setName("worker1")` (before starting) makes it print `worker1`.
- Good names matter for logging. In thread pools, threads are usually called **workers**.

### Thread priority [29:40]
- `t1.getPriority()` gives **5** by default. In the `Thread` source: `MIN_PRIORITY = 1`, `NORM_PRIORITY = 5`, `MAX_PRIORITY = 10`.
- `t1.setPriority(10)` tells the OS "this thread is more important, prefer it".
- It is **only an indication**. Depending on the OS, it may be respected, partially respected, or ignored completely. T1 with priority 10 usually printed before T2 with priority 5, but not always. It is rarely used.

### Daemon threads [33:10]
- **Background-running threads are called daemon threads.** Java has two kinds: **user threads** (the default, everything created so far) and **daemon threads**.
- A user thread keeps the program running. A T1 with `while (true) println("running")` keeps going even after `main` returns.
- **`t1.setDaemon(true)` before `start()`** means the JVM doesn't wait for this thread. When `main` (the last user thread) finishes, the daemon is stopped immediately.
  - Starting the daemon and returning immediately usually prints nothing at all.
  - If `main` sleeps 2 s first, "running" prints for about 2 s, then the program exits.
- Real-world example: **garbage collection** runs on daemon threads, because it only needs to run as long as the main program does.
- *(Precision: the JVM exits when **all user threads** have finished, not specifically `main`. If `main` ends but another user thread is still running, daemons keep running too.)*

## Code Examples

### `sleep` and `join` / `join(ms)`
```java
public class SleepJoinDemo {
    public static void main(String[] args) throws InterruptedException {
        System.out.println("main starts");
        Thread t1 = new Thread(() -> {
            try { Thread.sleep(300); } catch (InterruptedException e) { }
            System.out.println("Thread-0 finished its work");
        });
        t1.start();
        t1.join(100);   // wait at most 100 ms: t1 is still sleeping, so main moves on
        System.out.println("main stopped waiting, t1 alive? " + t1.isAlive());
        t1.join();      // now wait for t1 to finish completely
        System.out.println("main ends");
        // main starts
        // main stopped waiting, t1 alive? true
        // Thread-0 finished its work
        // main ends
    }
}
```

### `yield()`: just a hint
```java
public class YieldDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) {
                System.out.println("T1 says " + i);
                Thread.yield(); // "I'm willing to give up the CPU" — the scheduler may ignore it
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 5; i++) System.out.println("T2 says " + i);
        });
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println("done"); // done
        // Interleaving varies between runs, e.g.:
        // T1 says 1
        // T2 says 1
        // T2 says 2
        // T1 says 2 ...
    }
}
```

### Graceful stop with `interrupt()` and `isInterrupted()`
```java
public class InterruptDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread worker = new Thread(() -> {
            long loops = 0;
            while (!Thread.currentThread().isInterrupted()) { // check the flag
                loops++;                                       // "long-running task"
            }
            System.out.println("worker saw the interrupt, stopping (did work: " + (loops > 0) + ")");
        });
        worker.start();
        Thread.sleep(200);    // let it run for a while
        worker.interrupt();   // only sets the flag; the worker decides to stop
        worker.join();
        System.out.println("worker alive? " + worker.isAlive());
        // worker saw the interrupt, stopping (did work: true)
        // worker alive? false
    }
}
```

### Interrupting a sleeping thread, and `interrupted()` clearing the flag
```java
public class InterruptSleepingDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread sleeper = new Thread(() -> {
            try {
                Thread.sleep(10_000); // would sleep 10 s...
                System.out.println("woke up normally");
            } catch (InterruptedException e) {
                System.out.println("woken early by interrupt");
                // the flag is cleared when the exception is thrown:
                System.out.println("flag after catch: " + Thread.currentThread().isInterrupted());
            }
        });
        sleeper.start();
        Thread.sleep(100);
        sleeper.interrupt();  // ...but is interrupted after ~100 ms
        sleeper.join();

        Thread.currentThread().interrupt();                  // set main's own flag
        System.out.println(Thread.currentThread().isInterrupted()); // true  (does not clear)
        System.out.println(Thread.interrupted());                   // true  (reads AND clears)
        System.out.println(Thread.interrupted());                   // false (already cleared)
        // woken early by interrupt
        // flag after catch: false
        // true
        // true
        // false
    }
}
```

### Names, priority, `isAlive()`
```java
public class NamePriorityAlive {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> System.out.println("I am " + Thread.currentThread().getName()));
        System.out.println("default priority: " + t1.getPriority()); // default priority: 5
        t1.setName("worker1");
        t1.setPriority(Thread.MAX_PRIORITY);                          // only a hint to the OS
        System.out.println("alive before start: " + t1.isAlive());    // alive before start: false
        t1.start();
        t1.join();                                                    // I am worker1
        System.out.println("priority now: " + t1.getPriority());     // priority now: 10
        System.out.println("alive after end: " + t1.isAlive());       // alive after end: false
        System.out.println(Thread.MIN_PRIORITY + " " + Thread.NORM_PRIORITY + " " + Thread.MAX_PRIORITY); // 1 5 10
    }
}
```

### Daemon vs user thread
```java
public class DaemonDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread background = new Thread(() -> {
            while (true) {                       // infinite loop, but it is a daemon
                try { Thread.sleep(100); } catch (InterruptedException e) { return; }
                System.out.println("background tick");
            }
        });
        background.setDaemon(true);              // must be called before start()
        background.start();
        System.out.println("is daemon? " + background.isDaemon()); // is daemon? true
        Thread.sleep(350);                       // main (the only user thread) works a bit
        System.out.println("main ends -> JVM exits, daemon is killed");
        // is daemon? true
        // background tick
        // background tick
        // background tick
        // main ends -> JVM exits, daemon is killed
        // (the number of ticks can be 2-3 depending on timing)
    }
}
```

## Common Pitfalls / Tips
- **`sleep` is static.** `t1.sleep(1000)` compiles (with a warning) but sleeps the **current** thread, not `t1`. Always write `Thread.sleep(...)`.
- **Swallowing `InterruptedException`.** An empty `catch` loses the interrupt. In real code, either rethrow it or call `Thread.currentThread().interrupt()` to restore the flag.
- **Expecting `interrupt()` to stop a loop.** A loop that never checks `isInterrupted()` (and never sleeps or waits) will ignore interrupts forever.
- **Using `Thread.interrupted()` as a plain check.** It clears the flag, so a second check returns `false`. Use `isInterrupted()` when you only want to read it.
- **Relying on `yield()` or priorities.** Both are hints that may be ignored. Use proper coordination (`join`, locks, latches) instead.
- **`setDaemon(true)` after `start()`** throws `IllegalThreadStateException`.
- **Daemons can be killed mid-work.** Don't use daemon threads for things that must finish, like writing a file or committing a transaction. `finally` blocks in a daemon may never run.
- **`join(ms)` doesn't tell you whether the thread finished.** Check `isAlive()` afterwards.

## Review Questions
1. Which thread does `Thread.sleep(2000)` put to sleep, which state does it go to, and does it release its locks?
2. What does `t1.join()` do, and on which thread does it take effect? How is `join(1000)` different?
3. Why is `Thread.yield()` not used in production?
4. Does `t1.interrupt()` stop `t1`? What does it actually do, and how should `t1` react?
5. What is the difference between `isInterrupted()` and `Thread.interrupted()`?
6. What happens when you interrupt a thread that is sleeping?
7. When is `isAlive()` true?
8. What is a daemon thread, how do you create one, and what is a real-world example?

<details>
<summary>Answers</summary>

1. The current thread, because `sleep` is static. It goes to TIMED_WAITING for the given time and comes back to RUNNABLE. It keeps every lock it holds while sleeping.
2. The thread that *calls* `t1.join()` (e.g. `main`) waits in WAITING until `t1` terminates. `join(1000)` waits at most 1 second (TIMED_WAITING) and then continues whether or not `t1` has finished.
3. It is only a suggestion. The OS scheduler can ignore it completely, and the effect varies by platform and load, so you can't build correct behavior on it. It is only useful for experiments and tests.
4. No. It sets `t1`'s interrupt flag to true (and wakes it with `InterruptedException` if it is sleeping, waiting or joining). `t1` should check `Thread.currentThread().isInterrupted()` regularly, or handle `InterruptedException`, and then stop cleanly.
5. `isInterrupted()` (instance method) returns the flag and leaves it unchanged. `Thread.interrupted()` (static, current thread only) returns the flag and resets it to false.
6. It wakes up immediately with `InterruptedException`, and its interrupt flag is cleared. This is why `sleep`, `join` and `wait` declare that checked exception.
7. From the moment `start()` is called until the thread terminates. It is false before `start()` and after `run()` has finished.
8. A background thread that doesn't keep the JVM alive: when all user threads finish, daemons are stopped. Create one with `t.setDaemon(true)` before `t.start()`. Garbage collection runs on daemon threads.
</details>

## Further Study
- Problems in multithreading: race conditions, visibility, ordering (#50)
- `synchronized` and monitor locks (#51)
- `wait()` / `notify()` / `notifyAll()` (WAITING state and inter-thread communication)
- `ExecutorService.shutdown()` vs `shutdownNow()` (interrupt-based pool shutdown)
- Correct handling of `InterruptedException` ("don't swallow interrupts")
- `CountDownLatch`, `CompletableFuture`: higher-level alternatives to `join`
- Why `Thread.stop()`, `suspend()` and `resume()` were removed

---

[← #48](48-thread-creation-lifecycle.md) · [Index](README.md) · [#50 →](50-race-condition-visibility-ordering.md)

---

[← #48](48-thread-creation-lifecycle.md) · [Index](README.md) · [#50 →](50-race-condition-visibility-ordering.md)
