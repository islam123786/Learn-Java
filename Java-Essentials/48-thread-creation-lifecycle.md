# 48 · Thread Creation & Lifecycle
> Full notes: [48-thread-creation-lifecycle.md](../48-thread-creation-lifecycle.md) · [▶ Video](https://youtu.be/cVRdeQFP5IM)

## Two ways to create a thread
1. **Extend the `Thread` class**
2. **Implement the `Runnable` interface**

Either way, you put the thread's work in **`run()`** and start the thread with **`start()`**.

```
       Runnable  (interface, has run())
        ▲      ▲
        │      │
  Thread       MyRunnable   ← way 2: you define a TASK
  (class)
    ▲
    │
  MyThread                  ← way 1: you define a THREAD
```

### Way 1: extend `Thread`
```java
public class ExtendThreadDemo {
    public static void main(String[] args) throws InterruptedException {
        MyThread t1 = new MyThread();   // NEW: just an object on the heap
        t1.start();                     // asks the OS for a real thread, which then calls run()
        t1.join();                      // main waits for t1 to finish
        System.out.println("main done");
        // Thread is running on Thread-0
        // main done
    }
}

class MyThread extends Thread {
    @Override
    public void run() {                 // the work this thread does
        System.out.println("Thread is running on " + Thread.currentThread().getName());
    }
}
```

**What `start()` does behind the scenes**
1. The JVM asks the OS to create a new thread.
2. The OS creates it in the **same process**, next to `main`. (`t1` is only the reference variable, not the thread's name.)
3. The new thread gets its **own stack and program counter**.
4. When the CPU gives it time, the new thread runs `run()`.

### Way 2: implement `Runnable`
A `Runnable` is a **task**, not a thread, so you hand it to a `Thread` to run it.
```java
public class RunnableDemo {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = new MyRunnable();               // a task, not a thread

        Thread t1 = new Thread(task, "worker-1");       // Thread(Runnable, name)
        Thread t2 = new Thread(task, "worker-2");       // the SAME task reused by a second thread

        // Runnable is a functional interface, so a lambda works too (no class needed)
        Thread t3 = new Thread(() ->
                System.out.println("lambda task on " + Thread.currentThread().getName()), "worker-3");

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

### Extending `Thread` vs implementing `Runnable`
| | `extends Thread` | `implements Runnable` |
| :--- | :--- | :--- |
| What you define | a **thread** (is-a relationship) | a **task**, run by a separate `Thread` object |
| Inheritance | uses up your one `extends` | you can still extend another class |
| Reuse | one object = one thread | one task can be given to many threads |
| Lambdas | no | yes: `new Thread(() -> …)` |
| Separation of concerns | the *what* and the *how* are mixed together | your class says **what** to do, and `Thread` handles **how** to run it |

**Prefer `Runnable`.** Note that `Thread` itself implements `Runnable`.

---

## Thread name and id
- `Thread.currentThread()` is **static** and returns the thread that's running this code. `getName()` is an instance method, so `Thread.getName()` doesn't compile.
- In `main`, the name is `main` and the id is `1`. Threads you create get default names `Thread-0`, `Thread-1`, …, which you can change with `setName()`.
- `getId()` is **deprecated** (Java 19). Use `threadId()` instead. Ids go up in order, but the JVM's own internal threads take the low numbers, so your first thread shows something like 20 or more.
- A thread that's created but never started never runs its code.

## `start()` vs `run()`
| Call | What happens | `currentThread().getName()` |
| :--- | :--- | :--- |
| `t1.start()` | a new OS thread runs `run()` | `Thread-0` |
| `t1.run()` | a normal method call on the **current** thread (no new thread) | `main` |

- **Starting the same thread twice** throws **`IllegalThreadStateException`**. A thread can be started only once.

```java
public class StartVsRun {
    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() ->
                System.out.println("Current thread is " + Thread.currentThread().getName()));
        t1.run();     // Current thread is main
        t1.start();   // Current thread is Thread-0
        t1.join();
        try {
            t1.start();
        } catch (IllegalThreadStateException e) {
            System.out.println("Cannot start twice");   // Cannot start twice
        }
    }
}
```

## Non-determinism
- If T1 prints even numbers and T2 prints odd numbers, the output **interleaves differently on every run**. Within each thread, though, the order is kept.
- What decides the order:
  - the OS **scheduling algorithm** (round robin, time slicing, priority)
  - **system load** (other apps' threads)
  - **hardware** (number of cores)
  - **thread creation time**
- That's why multithreading bugs may show up only once in many runs, and why they're hard to debug.

---

## Thread lifecycle

```
               start()                                  run() finishes
  ┌───────┐ ───────────► ┌─────────────────────────────┐ ─────────────► ┌────────────┐
  │  NEW  │              │          RUNNABLE           │                │ TERMINATED │
  └───────┘              │      (ready ⇄ running)      │                └────────────┘
                         └─────────────────────────────┘
                            │ ▲        │ ▲        │ ▲
                            ▼ │        ▼ │        ▼ │
                      ┌─────────┐ ┌─────────┐ ┌───────────────┐
                      │ BLOCKED │ │ WAITING │ │ TIMED_WAITING │
                      └─────────┘ └─────────┘ └───────────────┘

  RUNNABLE → BLOCKED        : tries to enter a synchronized block whose lock is held
  BLOCKED → RUNNABLE        : the lock is released
  RUNNABLE → WAITING        : wait()  or  join()
  WAITING → RUNNABLE        : notify() / notifyAll()  or  the joined thread ends
  RUNNABLE → TIMED_WAITING  : sleep(ms) / join(ms) / wait(ms)
  TIMED_WAITING → RUNNABLE  : the time is up
```

| State | How a thread gets there | How it leaves |
| :--- | :--- | :--- |
| **NEW** | `new Thread(...)`. The object exists, but the OS knows nothing about it yet. | `start()` → RUNNABLE |
| **RUNNABLE** | after `start()`. Either waiting for the CPU (ready) or running on a core. | `run()` ends, or it moves to one of the three waiting states |
| **BLOCKED** | trying to enter a **`synchronized`** block or method whose lock another thread holds | the lock is released → RUNNABLE |
| **WAITING** | `wait()`, or `join()` with no timeout | `notify()` / `notifyAll()`, or the joined thread ends → RUNNABLE |
| **TIMED_WAITING** | `sleep(ms)`, `join(ms)`, `wait(ms)` | the time is up (or it's notified) → RUNNABLE |
| **TERMINATED** | `run()` finished. No method call is needed. | never. It can't be restarted. |

**Key points**
- **There's no RUNNING state in Java.** You can only `start()` a thread, never make it run, so "ready" and "running" are both **RUNNABLE**.
- **BLOCKED and critical sections:** shared data (e.g. one `Student` object that T1 and T2 both update) is a **critical section**. A **lock** makes sure only one thread is inside it at a time. Other threads wait in BLOCKED.
- **Waiting for I/O** (e.g. reading `System.in` or a socket) shows as **RUNNABLE** in Java, not BLOCKED. BLOCKED is used only for waiting on a `synchronized` lock.
- **Locks while waiting:** `wait()` and `wait(ms)` **release** the lock. `sleep()` **keeps** every lock it holds.
- **`stop()`** is deprecated, and since Java 20 it always throws an exception. Stop a thread with `interrupt()` instead (#49).

### Seeing the states in code
`Thread.State` is an enum: `NEW, RUNNABLE, BLOCKED, WAITING, TIMED_WAITING, TERMINATED`.
```java
public class LifecycleDemo {
    public static void main(String[] args) throws InterruptedException {
        Thread mainThread = Thread.currentThread();
        Object lock = new Object();

        // NEW → RUNNABLE → TERMINATED, and main in TIMED_WAITING
        Thread t1 = new Thread(() -> {
            try { Thread.sleep(200); } catch (InterruptedException e) { }
            System.out.println("main while sleeping: " + mainThread.getState()); // TIMED_WAITING
        });
        System.out.println("t1 before start: " + t1.getState());   // NEW
        t1.start();
        System.out.println("t1 after start: " + t1.getState());    // RUNNABLE
        Thread.sleep(500);
        System.out.println("t1 after finish: " + t1.getState());   // TERMINATED

        // BLOCKED: t2 wants the lock that main is holding
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
        System.out.println("t3 in wait(): " + t3.getState());      // WAITING
        synchronized (lock) { lock.notify(); }
        t3.join();
        System.out.println("t3 at the end: " + t3.getState());     // TERMINATED
    }
}
```
- Right after `start()`, the state is almost always RUNNABLE, not TERMINATED yet, because creating the OS thread takes time.
- `main` can't print its own state while it sleeps, so another thread (`t1`) checks `mainThread.getState()` for it.

**Remember:** create threads with `extends Thread` or (better) `implements Runnable` / a lambda, and always call **`start()`**, not `run()`. The states are NEW → RUNNABLE → TERMINATED, with BLOCKED (waiting for a lock), WAITING (`wait`/`join`) and TIMED_WAITING (`sleep`/timeouts) leading out of RUNNABLE and back into it.
