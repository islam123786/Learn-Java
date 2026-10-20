# Java Thread Creation & Lifecycle Explained from Scratch | Java Full Course #48
> Source: https://youtu.be/cVRdeQFP5IM · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 60:59. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- There are two ways to define a thread's work: **extend `Thread`** and override `run()`, or **implement `Runnable`** and pass it to `new Thread(runnable)`. Prefer `Runnable` (usually as a lambda). It separates the *task* from the *thread*, it can be reused, and your class stays free to extend something else.
- **`start()` creates a new OS thread**, which then calls `run()`. Calling `run()` yourself is just a normal method call on the current thread, and no new thread is created.
- A thread can be started **only once**. A second `start()` throws `IllegalThreadStateException`.
- Thread execution is **non-deterministic**. The OS scheduler, system load, core count and thread creation timing decide the order, so output from two threads interleaves differently on every run.
- Lifecycle (`Thread.State`): **NEW → RUNNABLE → (BLOCKED / WAITING / TIMED_WAITING) → TERMINATED**. "Running" is not a separate Java state. It is part of RUNNABLE.

## Key Concepts

**`Thread` class.** Java's class representing a thread. Its `run()` method holds the code the thread executes. *Why it matters:* extending it is the most direct (but least flexible) way to make a thread.

**`Runnable` interface.** A functional interface with a single `void run()`. `Thread` itself implements `Runnable`. *Why it matters:* it describes a **task** without tying it to a thread, and because it is functional you can write it as a lambda.

**`start()` vs `run()`.** `start()` asks the JVM/OS to create a new thread (its own stack and PC) and then calls `run()` on that thread. `run()` called directly runs on whatever thread calls it. *Why it matters:* this is a classic interview question and a common beginner bug.

**Thread name and id.** `Thread.currentThread()` returns the running thread. `getName()` gives `main`, `Thread-0`, `Thread-1`, ... `getId()` is **deprecated** since Java 19 (use `threadId()`). *Why it matters:* names make multithreaded logs readable.

**Non-determinism.** You can't predict which runnable thread gets the CPU, on which core, or for how long. *Why it matters:* bugs may show up once in many runs and are hard to reproduce.

**Critical section and locks.** A piece of code that touches shared data (for example a heap object both threads modify). Only one thread should be inside it at a time, so it gets protected with a lock. *Why it matters:* this is where the BLOCKED state comes from, and it is the topic of #50 and #51.

**Thread lifecycle.** The states a thread goes through from creation to death. You can read the current one with `getState()`. *Why it matters:* it explains what `sleep`, `join`, `wait` and locks actually do to a thread.

## Detailed Notes

### Two ways to create a thread [00:40]
- In Java everything is an object, so a thread is created through a class too.
- Option 1: **extend `Thread`**. Option 2: **implement `Runnable`**.

### Way 1: extending `Thread` [01:29]
- `class MyThread extends Thread` overrides `public void run()`. Whatever you put in `run()` is what the thread does.
- In `main`: `MyThread t1 = new MyThread(); t1.start();` prints "Thread is running", then the thread terminates, then `main` ends.
- Behind the scenes of `start()` [03:33]:
  1. The JVM asks the OS to create a new thread.
  2. The OS creates it inside the same process, next to `main`. `t1` is only the *reference variable*, not the thread's name.
  3. The new thread gets its own **stack** and **program counter**.
  4. The thread executes `run()` once the CPU gives it time.

### Way 2: implementing `Runnable` [04:54]
- `class MyRunnable implements Runnable` overrides `public void run()`.
- `Thread` itself `implements Runnable`, which you can see by opening the class.
- A `Runnable` is not a thread, so you wrap it: `Thread t1 = new Thread(new MyRunnable()); t1.start();`. `Thread` has a constructor that takes a `Runnable task`.
- The difference [07:27]: extending `Thread` means you are **defining a thread** (*is-a* relationship). Implementing `Runnable` means you are **defining a task**, and a separate `Thread` object runs it.
```
Runnable (interface)
   ▲            ▲
Thread (class)  MyRunnable (your task)
   ▲
MyThread (your subclass)
```

### Why prefer `Runnable` [11:04]
- **Separation of concerns.** Your class says *what* to do. Java's `Thread` handles *how* to run it.
- **Reusability.** One `Runnable` instance can be given to several threads (`new Thread(r1)`, `new Thread(r1)`, ...).
- **Inheritance freedom.** A class can extend only one class but implement many interfaces. Extending `Thread` uses up your single `extends`.
- **Lambdas.** `Runnable` is a functional interface, so you can skip the class: `new Thread(() -> System.out.println("Thread is running"))`.
- Either way, you end up asking the OS for a new thread with its own stack and PC.

### Thread name and id [15:08]
- `Thread.currentThread()` is a **static** method that returns the `Thread` object currently running. `getName()` is an **instance** method, so `Thread.getName()` doesn't compile ("cannot make a static reference to a non-static method").
- In `main`: name `main`, id `1`.
- `getId()` is **deprecated** [17:54], and the compiler prints a note about a deprecated API. The video's reasoning is that the id gives a false sense of meaning (it doesn't reflect run order).
  - *(Precision: `getId()` was deprecated in Java 19 because it isn't `final` and can be overridden. The replacement is `threadId()`. The ids aren't random: they are handed out in increasing order, and the JVM's own internal threads take some of the numbers first. That is why your first thread shows an id like 20 or more instead of 2.)*
- A thread that is created but never started never runs its code.
- With two started threads, the four print lines (name and id of each) come out in a **different order on each run**.
- Names are what you'll use for logging. Default names (`Thread-0`, `Thread-1`) say nothing, so you can set your own with `setName` (next video).

### `start()` vs `run()` [24:00]
- `t1.start()` prints `Current thread is Thread-0`.
- `t1.run()` prints `Current thread is main`. No OS thread was created, so the lambda just ran as a normal method call on `main`.
- **Can you start the same thread twice?** [27:24] No. The second `start()` throws **`IllegalThreadStateException`**. Once a thread has left the NEW state it can never be started again.

### Non-determinism [28:11]
- Demo: T1 prints even numbers 2..100 and T2 prints odd numbers 1..99. Both are started, T1 first.
- A beginner expects all the evens and then all the odds. The real output is mixed (for example 2 4 6 8, then 1 3 5 ... 13, then 10, ...), and a different run can even start with T2.
- Why [33:07]: the CPU/OS **scheduling algorithm** (round robin, time slicing, priority-based, ...) decides which thread runs, where and for how long. Other factors:
  - **System load**: other apps (Chrome, OBS, ...) also have threads waiting.
  - **Hardware**: how many cores there are.
  - **Thread creation time**: when the OS actually creates each thread. T1 may get going before T2 even exists yet.
- As a result, multithreaded bugs may appear once in many runs, which makes them hard to debug. Good practices (later videos) keep errors to a minimum.

### Thread lifecycle [36:58]
- **NEW**: `Thread t1 = new Thread(...)`. The object exists on the heap, but the OS knows nothing about it yet.
- **RUNNABLE** [38:28]: after `t1.start()`. The OS thread exists and is in line for the CPU.
- **"Running"** [39:28]: the thread currently on a core, executing `run()`. On a single core only one thread at a time is in this condition.
- **TERMINATED** [40:14]: `run()` finished. No method call is needed to get here. A terminated thread can't be restarted.
- **BLOCKED** [42:00]: shared data (e.g. a `Student s1` object on the heap that T1 sets to "Aditya" and T2 sets to "Rohit") makes a **critical section**. You protect it with a **lock** so that only one thread is inside at a time. If T1 holds the lock and T2 tries to enter, T2 goes to BLOCKED [45:48]. When T1 releases the lock, T2 goes back to RUNNABLE.
  - Locks don't stop both threads from *ever* entering. They stop both from being inside *at the same time*.
  - The video also puts **I/O waiting** (e.g. waiting for user input) under blocked [47:21]. *(Precision: in Java's `Thread.State`, a thread blocked in a read from a socket or `System.in` actually reports **RUNNABLE**. `BLOCKED` is reserved for waiting to enter a `synchronized` block or method.)*
- RUNNABLE and running are **one state** in Java [48:07]. You can only `start()` a thread, never make it run, so Java doesn't separate "ready" from "running". Arrows into BLOCKED/WAITING come from (and go back to) RUNNABLE as a whole.
- **WAITING** [49:08]: entered via `wait()` (and also `join()` with no timeout). Left via `notify()` / `notifyAll()`. Used for communication between threads (later video).
- **TIMED_WAITING** [49:42]: entered via `sleep(ms)`, `join(ms)` or `wait(ms)`. Goes back to RUNNABLE when the time is up.
- **`stop()`** [51:01] used to move a thread from any state straight to TERMINATED. It is deprecated and dangerous (it can leave shared data half-updated).
  - *(Update: since Java 20, `Thread.stop()` always throws `UnsupportedOperationException`. Stop threads cooperatively with `interrupt()`, covered in #49.)*
```
          start()                       run() ends
  NEW ──────────────► RUNNABLE ─────────────────► TERMINATED
                      (ready ⇄ running)
               ▲  │          ▲  │            ▲  │
   lock freed  │  │ lock     │  │ wait()/    │  │ sleep(ms)/join(ms)/
               │  ▼ busy     │  ▼ join()     │  ▼ wait(ms)
              BLOCKED       WAITING      TIMED_WAITING
                          (notify()/      (time over)
                           thread ends)
```

### Seeing the states in code [52:04]
- `Thread.State` is an **enum** with `NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED`. There is no RUNNING value.
- `t1.getState()` before `start()` gives `NEW`. Right after `start()` it gives `RUNNABLE`. It is very likely not `TERMINATED` yet, because creating the OS thread takes longer than the next `println` does, even when you repeat the print several times.
- To observe `TERMINATED`, make `main` `Thread.sleep(2000)` (this needs `try/catch` for the checked `InterruptedException`), then check again.
- To observe `TIMED_WAITING` for `main` [59:37], save `Thread mainThread = Thread.currentThread()` before starting T1, and have **T1 print `mainThread.getState()`** while `main` is asleep. `main` itself can't print anything while it sleeps.
- Lock behavior [60:28]. The video says BLOCKED and WAITING threads release their monitor locks while TIMED_WAITING threads don't. *(Correction: what matters is the method, not the state. `wait()` and `wait(ms)` **release** the monitor of the object you wait on, even though `wait(ms)` puts the thread in TIMED_WAITING. `sleep()` **keeps** every lock it holds. A BLOCKED thread holds nothing for that monitor: it is trying to get the lock.)*

## Code Examples

### Way 1: extend `Thread`
```java
public class ExtendThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        MyThread t1 = new MyThread();
        t1.start();      // new OS thread, which calls run()
        t1.join();       // wait so the output is fixed
        System.out.println("main done");
        // Thread is running
        // main done
    }
}

class MyThread extends Thread {
    @Override
    public void run() {
        System.out.println("Thread is running");
    }
}
```

### Way 2: implement `Runnable` (class and lambda), one task shared by many threads
```java
public class RunnableDemo {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = new MyRunnable();          // a task, not a thread
        Thread t1 = new Thread(task, "worker-1");  // reuse the same task...
        Thread t2 = new Thread(task, "worker-2");  // ...in a second thread
        Thread t3 = new Thread(() -> System.out.println("lambda task on " + Thread.currentThread().getName()), "worker-3");

        t1.start(); t1.join();
        t2.start(); t2.join();
        t3.start(); t3.join();
        // task running on worker-1
        // task running on worker-2
        // lambda task on worker-3
    }
}

class MyRunnable implements Runnable {
    @Override
    public void run() {
        System.out.println("task running on " + Thread.currentThread().getName());
    }
}
```

### `start()` vs `run()`, and starting twice
```java
public class StartVsRun {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() ->
                System.out.println("Current thread is " + Thread.currentThread().getName()));

        t1.run();    // plain method call: Current thread is main
        t1.start();  // real new thread:   Current thread is Thread-0
        t1.join();

        try {
            t1.start();  // second start
        } catch (IllegalThreadStateException e) {
            System.out.println("Cannot start twice: " + e.getClass().getSimpleName());
        }
        // Current thread is main
        // Current thread is Thread-0
        // Cannot start twice: IllegalThreadStateException
    }
}
```

### Non-deterministic order: evens and odds
```java
public class EvenOddRace {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            for (int i = 1; i <= 20; i++) if (i % 2 == 0) System.out.print("T1:" + i + " ");
        });
        Thread t2 = new Thread(() -> {
            for (int i = 1; i <= 20; i++) if (i % 2 != 0) System.out.print("T2:" + i + " ");
        });
        t1.start(); t2.start();
        t1.join(); t2.join();
        System.out.println();
        System.out.println("done"); // done
        // The order of T1/T2 tokens varies between runs, e.g.:
        // T1:2 T1:4 T2:1 T2:3 T2:5 T1:6 T1:8 T2:7 ...
        // Within one thread the numbers stay in order (T1: 2,4,6...; T2: 1,3,5...).
    }
}
```

### Watching the lifecycle with `getState()`
```java
public class LifecycleDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread mainThread = Thread.currentThread();
        Object lock = new Object();

        Thread t1 = new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) { }
            // main is sleeping right now, so its state is TIMED_WAITING
            System.out.println("main while sleeping: " + mainThread.getState());
        });
        System.out.println("t1 before start: " + t1.getState()); // NEW
        t1.start();
        System.out.println("t1 after start: " + t1.getState());  // RUNNABLE (or TIMED_WAITING if it is already in sleep())
        Thread.sleep(500);
        System.out.println("t1 after finish: " + t1.getState()); // TERMINATED

        // BLOCKED: t2 tries to enter a synchronized block whose lock main holds
        Thread t2 = new Thread(() -> { synchronized (lock) { } });
        synchronized (lock) {
            t2.start();
            Thread.sleep(200);
            System.out.println("t2 waiting for lock: " + t2.getState()); // BLOCKED
        }
        t2.join();

        // WAITING: t3 calls wait() until main calls notify()
        Thread t3 = new Thread(() -> {
            synchronized (lock) {
                try { lock.wait(); } catch (InterruptedException e) { }
            }
        });
        t3.start();
        Thread.sleep(200);
        System.out.println("t3 in wait(): " + t3.getState()); // WAITING
        synchronized (lock) { lock.notify(); }
        t3.join();
        System.out.println("t3 at the end: " + t3.getState()); // TERMINATED
        // Sample output:
        // t1 before start: NEW
        // t1 after start: RUNNABLE
        // main while sleeping: TIMED_WAITING
        // t1 after finish: TERMINATED
        // t2 waiting for lock: BLOCKED
        // t3 in wait(): WAITING
        // t3 at the end: TERMINATED
    }
}
```

### Thread name and id (`threadId()` replaces deprecated `getId()`)
```java
public class ThreadNameId {
    public static void main(String[] args) throws InterruptedException {
        System.out.println(Thread.currentThread().getName() + " id=" + Thread.currentThread().threadId()); // main id=1
        Thread t1 = new Thread(() ->
                System.out.println("name=" + Thread.currentThread().getName()
                        + " idPositive=" + (Thread.currentThread().threadId() > 1)));
        t1.start();
        t1.join();
        // name=Thread-0 idPositive=true   (the actual id is something like 20+; JVM internal threads use the lower numbers)
    }
}
```

## Common Pitfalls / Tips
- **Calling `run()` instead of `start()`.** It compiles and "works", but everything runs on the calling thread, with no concurrency at all.
- **Restarting a thread.** `start()` twice throws `IllegalThreadStateException`. To repeat work, create a new `Thread` (or submit the `Runnable` to an executor).
- **Extending `Thread` by habit.** Prefer `Runnable` or a lambda. Extend `Thread` only when you really need to customise thread behavior.
- **Assuming start order equals run order.** Starting T1 before T2 guarantees nothing. Use `join()` (or other coordination) when order matters.
- **Checking `getState()` and trusting it.** The state can change the moment after you read it, so use it for debugging, not for control flow.
- **`Thread.sleep` and `join` throw the checked `InterruptedException`.** Either catch it or declare `throws InterruptedException`. Declaring it on `main` is fine in demos but not as a habit in real code.
- **Don't use `getId()` or `stop()`.** Use `threadId()`, and `interrupt()` for cancellation.
- **Name your threads** (`new Thread(task, "worker-1")` or `setName`) so logs show who did what.

## Review Questions
1. What are the two ways to create a thread in Java, and which one is preferred? Why?
2. What happens internally when you call `t1.start()`?
3. What does `t1.run()` print for `Thread.currentThread().getName()`, and why?
4. What happens if you call `start()` twice on the same thread?
5. Name four factors that make thread execution order non-deterministic.
6. List the six values of `Thread.State`. Why is there no RUNNING state?
7. Which actions move a thread into BLOCKED, WAITING and TIMED_WAITING, and how does it get back to RUNNABLE from each?
8. Does `Thread.sleep()` release locks? Does `wait()`?

<details>
<summary>Answers</summary>

1. Extend `Thread` and override `run()`, or implement `Runnable` and pass it to a `Thread` constructor. `Runnable` (usually as a lambda) is preferred: it separates the task from the thread, one task can be reused by many threads, the class can still extend another class, and it works with lambdas because it is a functional interface.
2. The JVM asks the OS to create a new thread in the same process. The new thread gets its own stack and program counter, moves to RUNNABLE, and when scheduled it executes the object's `run()` method.
3. `main`. Calling `run()` directly is an ordinary method call. No new thread is created, so the code runs on the thread that called it.
4. The second call throws `IllegalThreadStateException`. A thread can be started only once.
5. Any four of: the OS/CPU scheduling algorithm, system load (other processes' threads), the hardware (number of cores), and the time at which the OS actually creates each thread.
6. NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED. Java can't make a thread run (only `start()` it), and the OS decides when a ready thread gets a core, so "ready" and "running" are combined into RUNNABLE.
7. BLOCKED: trying to enter a `synchronized` section whose lock another thread holds. Returns to RUNNABLE when the lock is released. WAITING: `wait()` or `join()` without a timeout. Returns on `notify()`/`notifyAll()` or when the joined thread ends. TIMED_WAITING: `sleep(ms)`, `join(ms)`, `wait(ms)`. Returns when the time runs out (or earlier on notify, interrupt, or the joined thread ending).
8. `sleep()` keeps every lock the thread holds. `wait()` (with or without a timeout) releases the monitor of the object it is called on and gets it back before returning.
</details>

## Further Study
- Thread methods: `sleep`, `join`, `yield`, `interrupt`, priorities, daemon threads (#49)
- `wait()` / `notify()` / `notifyAll()` and inter-thread communication
- Why `Thread.stop()`, `suspend()` and `resume()` were removed
- `ExecutorService` and thread pools as an alternative to creating threads by hand
- `Callable` and `Future` (tasks that return a value)
- Virtual threads (`Thread.ofVirtual()`, Java 21)

---

[← #47](47-multithreading-process-vs-thread.md) · [Index](../../ReadMe.md) · [#49 →](49-thread-methods-sleep-join-interrupt.md)
