# 49 · Thread Methods · sleep, join, yield, interrupt, isAlive, priority, daemon
> Full notes: [49-thread-methods-sleep-join-interrupt.md](../49-thread-methods-sleep-join-interrupt.md) · [▶ Video](https://youtu.be/ZPxJby0GeOQ)

## Quick reference

| Method | Static or instance? | What it does | State change | Throws `InterruptedException`? |
| :--- | :--- | :--- | :--- | :--- |
| `Thread.sleep(ms)` | static (affects the **current** thread) | pauses for `ms` milliseconds, **keeping its locks** | RUNNABLE → TIMED_WAITING → RUNNABLE | yes |
| `t.join()` | instance | the **caller** waits until `t` finishes | caller → WAITING | yes |
| `t.join(ms)` | instance | the caller waits **at most** `ms` | caller → TIMED_WAITING | yes |
| `Thread.yield()` | static | a *hint*: "I'm willing to give up the CPU" | stays RUNNABLE | no |
| `t.interrupt()` | instance | sets `t`'s **interrupt flag** to `true` (a request, not a force) | wakes `t` if it's sleeping, waiting or joining | — |
| `t.isInterrupted()` | instance | reads the flag, **without changing it** | — | no |
| `Thread.interrupted()` | static (**current** thread) | reads the flag **and resets it to `false`** | — | no |
| `t.isAlive()` | instance | `true` between `start()` and the end | — | no |
| `Thread.currentThread()` | static | gives back the running thread | — | no |
| `getName()` / `setName()` | instance | read or set the thread's name | — | no |
| `getPriority()` / `setPriority(1–10)` | instance | read or set the priority (only a hint) | — | no |
| `setDaemon(true)` | instance, **before** `start()` | makes it a background thread | — | no |

---

## `Thread.sleep(ms)`
- Pauses the **current** thread. Because it's static, `t1.sleep()` would still put the *caller* to sleep, not `t1`.
- State path: RUNNABLE → **TIMED_WAITING** → RUNNABLE.
- `InterruptedException` is a **checked** exception, so you must wrap the call in `try/catch` or declare `throws`.
- **It doesn't release locks.** If the thread is inside a `synchronized` block, every other thread that needs that lock keeps waiting for the whole sleep.

```java
public class SleepDemo {
    public static void main(String[] args) {
        System.out.println("main thread starts");
        try {
            Thread.sleep(2000);                 // main pauses for 2 s
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt(); // restore the flag (see interrupt below)
        }
        System.out.println("main thread ends"); // printed about 2 s later
    }
}
```

## `join()` and `join(ms)`
- **`t1.join()`** means "**I** (the calling thread) will wait until `t1` has finished." The caller goes to **WAITING**.
- **`t1.join(ms)`** means "wait **at most** `ms`, then carry on whether `t1` has finished or not." The caller goes to **TIMED_WAITING**.
- Use it to make the order of non-deterministic code **predictable**.

```java
public class JoinDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try { Thread.sleep(2000); } catch (InterruptedException e) { }
            System.out.println("Thread-0 finished");
        });

        System.out.println("main starts");
        t1.start();
        t1.join();                     // main waits here until t1 is done
        System.out.println("main ends");
        // main starts
        // Thread-0 finished   (after about 2 s)
        // main ends

        // With t1.join(1000) instead, main would wait only 1 s:
        // main starts → main ends → (1 s later) Thread-0 finished
    }
}
```
- Writing `throws InterruptedException` on `main` is fine for demos, but handle it properly in real code.

## `Thread.yield()`
- Static. The current thread says: "if another runnable thread with the **same priority** wants the CPU, it can have my turn."
- The thread **stays RUNNABLE**. It doesn't wait or block.
- It's only a **suggestion**, and the OS scheduler may ignore it completely. In the demo, with `yield()` after each print, T2 got most of the CPU in one run, but that isn't guaranteed.
- It's **not used in production**. It can be useful when you're testing scheduling behavior.

## `interrupt()`: asking a thread to stop

**The key idea:** `interrupt()` doesn't stop a thread. It only **sets a flag**. The thread itself has to check that flag and decide to stop. This is called **cooperative cancellation**.

```
t1.interrupt()  ──►  t1's interrupt flag: false → true
                     ├─ t1 is running code?          → nothing happens unless t1 checks the flag
                     └─ t1 is in sleep/wait/join?    → it wakes up with InterruptedException
                                                       (and the flag is cleared back to false)
```

**1. A thread that never checks the flag can't be stopped**
```java
// while (true) { System.out.println("running"); }
// t1.interrupt() changes nothing. This runs forever.
```

**2. Checking the flag and stopping gracefully**
```java
public class InterruptDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            int count = 0;
            while (!Thread.currentThread().isInterrupted()) {   // check the flag
                count++;                                        // do some work
            }
            System.out.println("worker stopped cleanly");
        });
        t1.start();
        Thread.sleep(500);        // let it work for 0.5 s
        t1.interrupt();           // ask it to stop
        t1.join();
        System.out.println("main ends");
        // worker stopped cleanly
        // main ends
    }
}
```

**3. Interrupting a sleeping thread wakes it up early**
```java
public class InterruptSleeper {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try {
                Thread.sleep(10_000);                     // planning to sleep 10 s
                System.out.println("woke up normally");
            } catch (InterruptedException e) {
                System.out.println("interrupted while sleeping");
                System.out.println("flag after exception: " + Thread.currentThread().isInterrupted()); // false
                Thread.currentThread().interrupt();       // restore the flag for code further up
            }
        });
        t1.start();
        Thread.sleep(500);
        t1.interrupt();                                   // wakes t1 straight away
        t1.join();
        // interrupted while sleeping
        // flag after exception: false
    }
}
```
- **This is why `sleep`, `join` and `wait` throw the checked `InterruptedException`.** Any thread could be interrupted while it waits.
- When the exception is thrown, the **flag is cleared**. Good practice is to call `Thread.currentThread().interrupt()` in the `catch` to set it again, so the code that called you can still see that a stop was requested.

**Where it's used:** running a thread until something outside it happens, cancelling a long background task, and **shutting down thread pools** (`executor.shutdownNow()` interrupts the workers).

## `isInterrupted()` vs `Thread.interrupted()`

| | `t.isInterrupted()` | `Thread.interrupted()` |
| :--- | :--- | :--- |
| Type | instance (works on any thread) | **static** (always the **current** thread) |
| Returns | the flag's value | the flag's value |
| Changes the flag? | **no** | **yes, resets it to `false`** |

```java
public class FlagCheck {
    public static void main(String[] args) {
        Thread.currentThread().interrupt();                         // set main's flag
        System.out.println(Thread.currentThread().isInterrupted()); // true
        System.out.println(Thread.currentThread().isInterrupted()); // true  (unchanged)
        System.out.println(Thread.interrupted());                   // true  (and now cleared)
        System.out.println(Thread.interrupted());                   // false
    }
}
```

## `isAlive()`
- **Alive** means it has started and hasn't finished yet, like a life between birth and death.
- `false` **before** `start()` and `false` **after** it ends. `true` in between, even while the thread sleeps.

```java
public class AliveDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            try { Thread.sleep(1000); } catch (InterruptedException e) { }
        });
        System.out.println(t1.isAlive());   // false (not started)
        t1.start();
        System.out.println(t1.isAlive());   // true  (running or sleeping)
        t1.join();
        System.out.println(t1.isAlive());   // false (terminated)
    }
}
```

## `currentThread()`, `getName()`, `setName()`
- `Thread.currentThread()` gives back the thread that's running this code. You can then call `getName()`, `getPriority()` and so on.
- Default names are `Thread-0`, `Thread-1`, …. Use `t1.setName("worker1")` (or `new Thread(task, "worker1")`) to give a meaningful name for **logging**. In thread pools, threads are usually called **workers**.

## Thread priority
- Constants: `MIN_PRIORITY = 1`, `NORM_PRIORITY = 5` (the **default**), `MAX_PRIORITY = 10`.
- `t1.setPriority(10)` tells the OS "prefer this thread".
- It's **only a hint**. The OS may follow it, partly follow it, or ignore it completely. **Never write code that depends on priority.** It's rarely used.

## Daemon threads

Java has two kinds of threads:

| | User thread | Daemon thread |
| :--- | :--- | :--- |
| Default? | **yes** (every thread you create) | no, you have to call `setDaemon(true)` **before** `start()` |
| Keeps the JVM running? | **yes**. The program won't exit while one is still running | **no**. It's killed as soon as the last user thread ends |
| Used for | the program's real work | background housekeeping, e.g. **garbage collection** |

```java
public class DaemonDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            while (true) {                       // an endless background loop
                System.out.println("daemon running");
                try { Thread.sleep(300); } catch (InterruptedException e) { }
            }
        });
        t1.setDaemon(true);                      // must be called BEFORE start()
        t1.start();

        Thread.sleep(1000);                      // main works for 1 s
        System.out.println("main ends");         // last user thread ends → JVM exits → daemon is killed
        // daemon running   (printed about 3–4 times)
        // main ends
    }
}
```
- Without `setDaemon(true)`, this program would **never exit**, because the loop is a user thread.
- Calling `setDaemon()` after `start()` throws `IllegalThreadStateException`.
- The JVM exits when **all user threads** have finished, not specifically when `main` finishes. If another user thread is still running, daemons keep running too.
- Daemons are stopped abruptly (`finally` blocks may not run), so don't use them for work that must finish, such as saving a file.

---

**Remember:**
- **`sleep`** pauses the current thread and keeps its locks.
- **`join`** waits for another thread to finish.
- **`yield`** and **priority** are only hints.
- **`interrupt`** sets a flag that the thread must check. It also wakes a thread from sleep, join or wait with `InterruptedException`, which clears the flag.
- **`interrupted()`** clears the flag, but **`isInterrupted()`** doesn't.
- **Daemon** threads don't keep the JVM alive.
