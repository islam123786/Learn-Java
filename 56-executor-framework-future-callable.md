# Executor Framework Deep Dive | ThreadPool, Future & Callable | Java Full Course #56
> Source: https://youtu.be/VPtaTUSaBOM · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 79:14. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Creating a new `Thread` for every task is wasteful. Each thread costs memory (its own stack), a system call to create, and CPU time lost to context switching when there are far more threads than cores.
- The **Executor framework** (`java.util.concurrent`) splits *what to do* (the task) from *how to run it* (threads). You hand it a task. A **thread pool** of reusable **worker threads** plus a **task queue** does the rest.
- `execute(Runnable)` is fire-and-forget. `submit(Callable)` returns a **`Future`**: a placeholder for a result that arrives later. `future.get()` **blocks** until it's ready, and rethrows task failures as `ExecutionException`.
- `ThreadPoolExecutor` is configured with **core pool size, max pool size, keep-alive time + unit, and a work queue**. New tasks go to core threads first, then the queue, then extra threads up to max. When all of those are full, the **rejection policy** runs.
- The `Executors` factory methods give ready-made pools: `newFixedThreadPool`, `newCachedThreadPool`, `newSingleThreadExecutor`, `newScheduledThreadPool`. Always `shutdown()` an executor when you're done.

## Key Concepts

**Manual thread creation and its costs.** `new Thread(r).start()` asks the OS for a thread every time: stack memory (typically 512 KB–1 MB by default), creation time (a system call), and scheduling overhead. *Why it matters:* thousands of one-shot threads waste memory and leave cores busy context-switching instead of working.

**Executor framework.** You give it the task, and it decides which thread runs it and when. It may reuse an existing thread or create one. *Why it matters:* the framework, not every programmer, takes responsibility for thread lifecycle and limits.

**Thread pool.** A set of pre-created **worker threads** that are *not* terminated after a task. When a worker finishes, it goes back to the pool (or takes the next task from the queue). *Why it matters:* thread reuse caps memory and context switching, and it's the backbone of servers (e.g. Spring Boot's request-handling threads).

**Work queue.** A `BlockingQueue<Runnable>` in front of the workers. Tasks wait there (FIFO) when every worker is busy. *Why it matters:* it smooths out bursts without creating more threads.

**`Executor` / `ExecutorService`.** `Executor` has one method, `void execute(Runnable)`. `ExecutorService` extends it with lifecycle and result methods: `submit`, `invokeAll`, `invokeAny`, `shutdown`, `shutdownNow`, `awaitTermination`. *Why it matters:* you program against `ExecutorService` and don't care which pool is behind it.

**`Runnable` vs `Callable<V>`.** `Runnable.run()` returns nothing and can't throw checked exceptions. `Callable.call()` returns a `V` and may throw `Exception`. Both are functional interfaces, so lambdas work. *Why it matters:* use `Callable` when the task produces a result.

**`Future<V>`.** The object you get back from `submit`. The result doesn't exist yet when `submit` returns, so you can't assign it to a plain `Integer`. `get()` blocks the calling thread until the result (or the failure) is available. *Why it matters:* it's how async work hands results back to the caller.

**`ThreadPoolExecutor` configuration.** Core pool size (threads kept alive), maximum pool size (upper limit), keep-alive time for the extra threads, and the work queue (bounded `ArrayBlockingQueue` or effectively unbounded `LinkedBlockingQueue`). *Why it matters:* these four settings decide your maximum concurrency and what happens under overload.

**Rejection policy.** What happens when workers are at max and the queue is full: `AbortPolicy` (default, throws `RejectedExecutionException`), `DiscardPolicy`, `DiscardOldestPolicy`, `CallerRunsPolicy`. *Why it matters:* it's your overload behavior. A wrong choice silently loses tasks.

## Detailed Notes

### Why manual threads are a bad approach [00:00]
- Until now the course created threads by hand: `Thread t1 = new Thread(() -> ...); t1.start();`. That sends a request to the OS to create a real thread.
- **Memory allocation** [01:22]: each Java thread gets its own stack, program counter, etc. The video quotes 512 KB–1 MB per thread, so 1000 threads ≈ 0.5–1 GB.
- **OS scheduling** [02:35]: 1000 threads on 4 cores means each core juggles hundreds of threads. Most of the time goes into context switching instead of real work. The video calls this "thrashing". *(The term usually refers to paging; "context-switch overhead" is the more precise name, but the idea is the same.)*
- **Thread creation time** [03:30]: every creation is a system call that sets up stack space and the like. And we used each thread only **once**: one task, then it dies.
- **Restaurant analogy** [04:54]: hiring a chef for one dish, then firing them, then hiring a new chef for the next dish. That's absurd. You keep a team of chefs.

### What the Executor framework promises [05:46]
- So far we told Java both *what* to do (the task) and *how* to do it (create the thread, pass the `Runnable`, start it).
- The Executor framework, like the Collection framework, is a ready-made library. You only say *what* to do and submit the task. It runs the task **asynchronously** on a thread that it creates *or reuses*.
- The mechanism behind that promise is the **thread pool**.

### How a thread pool works [08:34]
- Picture a pool with 5 pre-created threads T1–T5, called **worker threads**.
- Task 1 arrives → assigned to T1, which leaves the pool to do the work. Task 2 → T2, and so on.
- When T1 finishes, it **is not terminated**. It returns to the pool and waits for the next task. Tasks 6 and 7 can then reuse T1 and T2. The pool manages the worker life cycle.
- **The queue** [13:18]: when all workers are busy, a new task (task 8) goes into a FIFO queue. A worker that becomes free checks the queue first and picks up the oldest waiting task.
- When the workers are all busy **and** the queue is full (task 12), there are two possibilities, depending on the pool's rules: create an extra worker, or **reject/discard** the task.
- There are several pool types (fixed, cached, fork-join, scheduled). They all share this general shape (some have no real queue) but follow different rules.
- A "task" is just a `Runnable` (or `Callable`) object. Flow: task → executor → thread pool → a worker runs it.

### First demo: fixed pool, 5 tasks, 2 workers [17:56]
- `ExecutorService executor = Executors.newFixedThreadPool(2);`. `ExecutorService` is an interface, and `Executors` is a utility class with factory methods.
- A loop submits 5 tasks with `executor.execute(() -> ...)`, each printing `Task i is performed by <Thread.currentThread().getName()>`.
- The lambda can't capture the loop variable `i` because it isn't effectively final. Copy it first: `int taskId = i;` (see the nested classes / lambdas video).
- Always call `executor.shutdown()` at the end. Otherwise the worker threads stay alive and the JVM doesn't exit.
- Output: only `pool-1-thread-1` and `pool-1-thread-2` appear. Two threads run all five tasks, and the other three wait in the queue.
- **Real-world use** [24:59]: web servers keep a bounded pool of request-handling threads. Creating one thread per client would let thousands of simultaneous clients overwhelm the machine. Extra requests wait in the queue, so concurrency is limited on purpose.

### The hierarchy [26:09]
```
Executor                       (interface: execute(Runnable))
└── ExecutorService            (interface: submit, invokeAll, invokeAny, shutdown, ...)
    ├── ThreadPoolExecutor     (the normal configurable pool)
    ├── ForkJoinPool           (work-stealing pool, next video)
    └── ScheduledExecutorService (interface: schedule, scheduleAtFixedRate, ...)
        └── ScheduledThreadPoolExecutor
```
*(Simplified. In the JDK, `ThreadPoolExecutor` and `ForkJoinPool` extend `AbstractExecutorService`, and `ScheduledThreadPoolExecutor` extends `ThreadPoolExecutor` while implementing `ScheduledExecutorService`.)*

### `Executor` interface [28:10]
- A single abstract method, `void execute(Runnable command)`. You hand over a task; which pool runs it is the implementation's business.

### `ExecutorService`: `submit`, `Callable` and `Future` [29:02]
- `ExecutorService` adds the methods that **manage** the pool. It still has `execute`.
- **`submit`** [29:37] also runs a task asynchronously, but it accepts a **`Callable`**, a task that *returns a value*. Its method is `call()` instead of `run()`.
  - `() -> System.out.println("hello")` fits `Runnable` (no input, no result).
  - `() -> 10 + 20` fits `Callable<Integer>` (no input, returns 30).
  - *(The video calls the Runnable shape a "consumer". `java.util.function.Consumer` takes an argument, so `Runnable` is really "no input, no output".)*
- Why `Integer i = executor.submit(() -> 10);` can't work [34:26]: `submit` returns immediately, but the task might run now, in a few milliseconds, or after waiting in the queue. A plain variable needs the value right away.
- So Java provides **`Future<V>`**: `Future<Integer> f1 = executor.submit(() -> 10);`. Flow: task submitted → runs on a background thread → result stored in the future.
- **`f1.get()`** blocks the *current* thread (e.g. `main`) until the result is there, then returns it.
- **Demo 2** [37:01]: `get()` must be wrapped in try/catch (it throws the checked `InterruptedException` and `ExecutionException`). It prints `10`. Adding `Thread.sleep(3000)` inside the callable makes `main` visibly wait about 3 seconds before printing `10`. A multi-line lambda needs an explicit `return`.

### `shutdown`, `shutdownNow`, `invokeAll`, `invokeAny` [40:15]
- **`shutdown()`**: stop accepting new tasks (later submissions are rejected), let the running *and queued* tasks finish, then terminate the pool.
- **`shutdownNow()`** [41:30]: stop accepting tasks and *try* to stop the running ones. It's best-effort: Java can't force-kill a thread, so a running task may still finish normally. *(Concretely, it interrupts the workers and returns the `List<Runnable>` of queued tasks that never started. A task stops only if it reacts to interruption, e.g. via `sleep` throwing `InterruptedException` or checking `isInterrupted()`.)*
- **`invokeAll(tasks)`** [42:28]: takes a collection of `Callable`s (e.g. `List.of(() -> 10, () -> 20, () -> 30)`), runs them all, and returns a `List<Future<T>>`. Loop over the futures and call `get()` on each. *(It blocks until all tasks are done.)*
- **`invokeAny(tasks)`**: mentioned but not explained. *(It returns the result of one task that completes successfully and cancels the rest.)*
- The video calls `execute` and `submit` the two methods you'll use most.

### Exceptions: `execute` vs `submit` [44:56]
- With `execute`, the exception is "lost" from the caller's point of view. **Demo 3** [45:05]: `executor.execute(() -> { int x = 10 / 0; })` inside a `try/catch` in `main`. The catch block **never runs**, because the exception happens on a worker thread, not on `main`.
- With `submit(() -> 10 / 0)`, wrapping `f1.get()` in try/catch **does** catch it. It prints "Caught submit exception".
- The video mentions `ThreadFactory` as a way to handle `execute` exceptions, without going into it.
- *(More precisely: with `execute`, the default uncaught-exception handler prints the stack trace to stderr and the worker thread dies; the pool replaces it. With `submit`, the exception is stored in the `Future` and rethrown by `get()` wrapped in an `ExecutionException` (`e.getCause()` is the `ArithmeticException`). The flip side: if you never call `get()`, a failed `submit` task is silently swallowed.)*

### `ThreadPoolExecutor` internals [48:33]
- A configurable engine: limited worker threads + a queue for extra tasks + rules for the overload case.
- The **`Executors`** utility class [49:52] gives you pre-configured pools, so you don't have to call the complicated constructor yourself. `newFixedThreadPool(n)` internally does `new ThreadPoolExecutor(...)`. You build one by hand only when you need fine control (and for interviews).
- **Constructor** [51:37]: `new ThreadPoolExecutor(int corePoolSize, int maximumPoolSize, long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue)`. *(The video says "current pool size"; the parameter is `corePoolSize`. There are also overloads that add a `ThreadFactory` and a `RejectedExecutionHandler`.)*
  - **Core pool size**: the number of workers kept alive even when idle (e.g. 2).
  - **Max pool size** [52:44]: the upper limit of workers (e.g. 5). It caps how many tasks can run at once.
  - **Work queue**: bounded or unbounded (see below).
  - **Keep-alive time + unit** [53:59]: how long the *extra* (non-core) workers T3–T5 may sit idle before they're removed, bringing the pool back down to the core size. E.g. `10, TimeUnit.SECONDS`.

### The submission rules [55:20]
With core = 2, max = 5 and a bounded queue (threads are created lazily, so the pool starts empty):
1. **If running workers < core size** [56:14], create a new worker for the task, even if other workers are idle.
2. **Else, if the queue has space**, put the task in the queue.
3. **Else, if workers < max size**, create an extra worker.
4. **Else, reject the task** (rejection policy).
- So the pool fills the core first, then the **queue**, and only then grows toward max. Maximum concurrency is 5, and excess load is queued or rejected instead of overwhelming the machine. In practice a worker usually frees up before the extreme case is reached.
- *(The video's walkthrough has the new extra worker T3 take the task at the head of the queue (task 3). That isn't what happens: an extra worker created in step 3 runs **the task that just arrived**, and queued tasks keep waiting. The video's own demo output shows this, with task 5 run by thread-3 while task 3 is still queued.)*

### Demo 4: building your own `ThreadPoolExecutor` [61:22]
- `new ThreadPoolExecutor(2, 5, 10, TimeUnit.SECONDS, new ArrayBlockingQueue<>(2))`, then 5 tasks that each print and sleep 2 s, then `shutdown()`.
- Output: task 1 → thread-1, task 2 → thread-2, **task 5 → thread-3**, then tasks 3 and 4 picked up by thread-1 and thread-3 when they free up. Three threads handled all five tasks.
- In practice you rarely do this. `Executors.newFixedThreadPool(2)` configures everything for you.

### Queue types [67:39]
- **`ArrayBlockingQueue`**: array-backed, **fixed capacity** given in the constructor.
- **`LinkedBlockingQueue`**: linked-list-backed and can be **unbounded** (capacity `Integer.MAX_VALUE` by default), so tasks are never rejected for lack of space.

### Ready-made pools in `Executors` [69:27]
- **`newFixedThreadPool(n)`**: fixed number of threads (core = max = n) + an **unbounded** `LinkedBlockingQueue`. Risk: a flood of tasks can pile up in the queue until you run **out of memory**. *(Because the queue never fills, a fixed pool never reaches the rejection step except after shutdown.)*
- **`newCachedThreadPool()`** [69:49]: the opposite, with "unlimited threads + no queue". Threads are **created on demand** (idle ones are reused) and die after **60 s** idle. Good for bursts of short tasks, dangerous under heavy load because the thread count can explode. *(Technically it uses a `SynchronousQueue`, a zero-capacity hand-off, rather than no queue at all.)*
- **`newSingleThreadExecutor()`** [70:54]: one worker + a queue. All tasks run **one after another**, which serializes the work. Useful for something like a logging system where every entry must be written in order. *(The video says "serializable", meaning sequential execution. This has nothing to do with the `Serializable` interface.)*
- **`newScheduledThreadPool(n)`** [72:18]: runs tasks in the future.
  - `scheduler.schedule(() -> System.out.println("hello"), 2, TimeUnit.SECONDS)` runs once, 2 s from now.
  - `scheduler.scheduleAtFixedRate(task, 0, 2, TimeUnit.SECONDS)` [73:49]: first run after an initial delay of 0, then every 2 s. *(There's also `scheduleWithFixedDelay`, which waits 2 s after each run **ends**.)*
- `ForkJoinPool` is covered in the next video.

### Rejection policies [75:43]
When max threads are busy and the queue is full:
- **`AbortPolicy`** (the default): throw `RejectedExecutionException`.
- **`DiscardPolicy`**: silently drop the new task. It's dangerous because you never find out, so data is lost.
- **`DiscardOldestPolicy`** [76:34]: drop the oldest task in the queue and enqueue the new one. The video calls this the most used in real systems.
- *(Correction/addition: `DiscardOldestPolicy` also loses data (the oldest task). The policy most often recommended when you can't lose work is **`CallerRunsPolicy`**, which the video doesn't mention: the submitting thread runs the task itself, which naturally slows producers down (back-pressure). You can also implement your own `RejectedExecutionHandler`.)*

### Homework and what's next [78:52]
- Write programs for the cached, single-thread and scheduled pools yourself. A cached pool runs all tasks in parallel (each gets a thread). A single-thread pool runs 100 tasks one at a time, never two at once. A scheduled pool can run something 3 s later.
- *(The video suggests a single-thread pool might run task 4 before task 3. It won't: `newSingleThreadExecutor` runs tasks in submission order.)*
- Next (last Java lecture): `CompletableFuture`, `ForkJoinPool`, virtual threads.

## Code Examples

Each block is a complete program: save it under the class's name (e.g. `ExFixedPoolDemo.java`) and run it with `java ExFixedPoolDemo.java`. Every program shuts its executor down, so it terminates.

### Fixed pool: 5 tasks, 2 workers
```java
import java.util.Set;
import java.util.concurrent.*;

public class ExFixedPoolDemo {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        Set<String> workers = ConcurrentHashMap.newKeySet();

        for (int i = 1; i <= 5; i++) {
            int taskId = i; // lambda needs an effectively final variable
            executor.execute(() -> {
                String name = Thread.currentThread().getName();
                workers.add(name);
                System.out.println("Task " + taskId + " is performed by " + name);
                try { Thread.sleep(100); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }

        executor.shutdown();                          // no new tasks; finish queued ones
        executor.awaitTermination(5, TimeUnit.SECONDS);
        // order may vary, e.g.:
        // Task 1 is performed by pool-1-thread-1
        // Task 2 is performed by pool-1-thread-2
        // Task 3 is performed by pool-1-thread-1
        // Task 4 is performed by pool-1-thread-2
        // Task 5 is performed by pool-1-thread-1
        System.out.println("Distinct workers: " + workers.size()); // Distinct workers: 2
    }
}
```

### `Callable` + `Future`: `get()` blocks
```java
import java.util.concurrent.*;

public class ExCallableFutureDemo {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            Callable<Integer> task = () -> {
                Thread.sleep(300);   // pretend this is slow work
                return 10;           // multi-line lambda needs an explicit return
            };
            Future<Integer> f1 = executor.submit(task);
            System.out.println("Submitted, done yet? " + f1.isDone()); // Submitted, done yet? false

            long start = System.nanoTime();
            Integer result = f1.get();                  // main blocks here ~300 ms
            long waitedMs = (System.nanoTime() - start) / 1_000_000;
            System.out.println("Result: " + result);    // Result: 10
            System.out.println("main waited >= 200 ms: " + (waitedMs >= 200)); // main waited >= 200 ms: true

            Future<Integer> f2 = executor.submit(() -> 10 + 20);
            System.out.println("Second result: " + f2.get()); // Second result: 30
        } finally {
            executor.shutdown();
        }
    }
}
```

### Exceptions: `execute` vs `submit`
```java
import java.util.concurrent.*;

public class ExExceptionDemo {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            executor.execute(() -> {
                int x = 10 / 0; // thrown on the worker thread, not on main
            });
        } catch (Exception e) {
            System.out.println("Caught execute exception"); // never printed
        }
        Thread.sleep(200);
        // stderr (from the default uncaught-exception handler), e.g.:
        // Exception in thread "pool-1-thread-1" java.lang.ArithmeticException: / by zero

        Future<Integer> f1 = executor.submit(() -> 10 / 0);
        try {
            f1.get();
        } catch (ExecutionException e) {
            System.out.println("Caught submit exception: " + e.getCause());
            // Caught submit exception: java.lang.ArithmeticException: / by zero
        }

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
    }
}
```

### `invokeAll` and `invokeAny`
```java
import java.util.List;
import java.util.concurrent.*;

public class ExInvokeAllDemo {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(3);
        try {
            List<Callable<Integer>> tasks = List.of(() -> 10, () -> 20, () -> 30);

            List<Future<Integer>> futures = executor.invokeAll(tasks); // waits for all
            int sum = 0;
            for (Future<Integer> f : futures) {
                System.out.println("Result: " + f.get()); // in task order: 10, 20, 30
                sum += f.get();
            }
            System.out.println("Sum: " + sum); // Sum: 60

            Integer any = executor.invokeAny(tasks); // result of one successful task
            System.out.println("invokeAny returned one of 10/20/30: " + List.of(10, 20, 30).contains(any));
            // invokeAny returned one of 10/20/30: true
        } finally {
            executor.shutdown();
        }
    }
}
```

### Hand-built `ThreadPoolExecutor`: core → queue → extra threads → reject
```java
import java.util.concurrent.*;

public class ExThreadPoolExecutorDemo {
    public static void main(String[] args) throws InterruptedException {
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                2,                              // corePoolSize
                5,                              // maximumPoolSize
                10, TimeUnit.SECONDS,           // keepAliveTime for extra (non-core) threads
                new ArrayBlockingQueue<>(2));   // bounded work queue, capacity 2
                                                // default policy: AbortPolicy

        for (int i = 1; i <= 8; i++) {
            int taskId = i;
            try {
                executor.execute(() -> {
                    System.out.println("Task " + taskId + " is performed by " + Thread.currentThread().getName());
                    try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                });
            } catch (RejectedExecutionException e) {
                System.out.println("Task " + taskId + " rejected");
            }
        }
        // Tasks 1-2 -> core threads, 3-4 -> queue, 5-7 -> extra threads 3..5, task 8 -> rejected
        System.out.println("Pool size: " + executor.getPoolSize());          // Pool size: 5
        System.out.println("Queued: " + executor.getQueue().size());         // Queued: 2

        executor.shutdown();
        executor.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Completed: " + executor.getCompletedTaskCount()); // Completed: 7
        // order may vary, e.g.:
        // Task 1 is performed by pool-1-thread-1
        // Task 2 is performed by pool-1-thread-2
        // Task 5 is performed by pool-1-thread-3   <- new thread takes the NEW task, not task 3
        // Task 6 is performed by pool-1-thread-4
        // Task 7 is performed by pool-1-thread-5
        // Task 8 rejected
        // Pool size: 5
        // Queued: 2
        // Task 3 is performed by pool-1-thread-1   <- queued tasks go to whichever threads free up first
        // Task 4 is performed by pool-1-thread-2
        // Completed: 7
    }
}
```

### Rejection policies side by side
```java
import java.util.*;
import java.util.concurrent.*;

public class ExRejectionPoliciesDemo {
    public static void main(String[] args) throws InterruptedException {
        run("AbortPolicy", new ThreadPoolExecutor.AbortPolicy());
        run("DiscardPolicy", new ThreadPoolExecutor.DiscardPolicy());
        run("DiscardOldestPolicy", new ThreadPoolExecutor.DiscardOldestPolicy());
        run("CallerRunsPolicy", new ThreadPoolExecutor.CallerRunsPolicy());
        // AbortPolicy: rejected [3, 4], ran [1, 2]
        // DiscardPolicy: rejected [], ran [1, 2]
        // DiscardOldestPolicy: rejected [], ran [1, 4]
        // CallerRunsPolicy: rejected [], ran [1, 2, 3, 4] (3 and 4 ran on main)
    }

    // 1 worker + queue of 1. Task 1 blocks the worker until all 4 tasks are submitted.
    static void run(String name, RejectedExecutionHandler policy) throws InterruptedException {
        ThreadPoolExecutor ex = new ThreadPoolExecutor(1, 1, 0, TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1), policy);
        CountDownLatch release = new CountDownLatch(1);
        List<Integer> ran = Collections.synchronizedList(new ArrayList<>());
        List<Integer> rejected = new ArrayList<>();
        Set<String> callerRan = Collections.synchronizedSet(new TreeSet<>());

        for (int i = 1; i <= 4; i++) {
            int id = i;
            try {
                ex.execute(() -> {
                    if (id == 1) {
                        try { release.await(); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
                    }
                    if (Thread.currentThread().getName().equals("main")) callerRan.add(String.valueOf(id));
                    ran.add(id);
                });
            } catch (RejectedExecutionException e) {
                rejected.add(id);
            }
        }
        release.countDown();
        ex.shutdown();
        ex.awaitTermination(5, TimeUnit.SECONDS);

        List<Integer> sorted = new ArrayList<>(ran);
        Collections.sort(sorted);
        String extra = callerRan.isEmpty() ? "" : " (" + String.join(" and ", callerRan) + " ran on main)";
        System.out.println(name + ": rejected " + rejected + ", ran " + sorted + extra);
    }
}
```

### Single-thread vs cached pool
```java
import java.util.*;
import java.util.concurrent.*;

public class ExSingleVsCachedDemo {
    public static void main(String[] args) throws InterruptedException {
        // Single thread: strictly one at a time, in submission order
        ExecutorService single = Executors.newSingleThreadExecutor();
        List<Integer> order = Collections.synchronizedList(new ArrayList<>());
        Set<String> singleThreads = ConcurrentHashMap.newKeySet();
        for (int i = 1; i <= 5; i++) {
            int id = i;
            single.execute(() -> {
                order.add(id);
                singleThreads.add(Thread.currentThread().getName());
            });
        }
        single.shutdown();
        single.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Single order: " + order);                // Single order: [1, 2, 3, 4, 5]
        System.out.println("Single threads used: " + singleThreads.size()); // Single threads used: 1

        // Cached: a new thread whenever no idle one exists
        ExecutorService cached = Executors.newCachedThreadPool();
        Set<String> cachedThreads = ConcurrentHashMap.newKeySet();
        for (int i = 1; i <= 5; i++) {
            cached.execute(() -> {
                cachedThreads.add(Thread.currentThread().getName());
                try { Thread.sleep(200); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            });
        }
        cached.shutdown();
        cached.awaitTermination(5, TimeUnit.SECONDS);
        System.out.println("Cached threads used: " + cachedThreads.size()); // Cached threads used: 5
    }
}
```

### Scheduled pool: `schedule` and `scheduleAtFixedRate`
```java
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ExScheduledDemo {
    public static void main(String[] args) throws Exception {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
        try {
            ScheduledFuture<String> once = scheduler.schedule(() -> "hello after 300 ms", 300, TimeUnit.MILLISECONDS);

            AtomicInteger ticks = new AtomicInteger();
            CountDownLatch threeTicks = new CountDownLatch(3);
            ScheduledFuture<?> repeating = scheduler.scheduleAtFixedRate(() -> {
                System.out.println("tick " + ticks.incrementAndGet());
                threeTicks.countDown();
            }, 0, 200, TimeUnit.MILLISECONDS); // first run now, then every 200 ms

            System.out.println(once.get());   // blocks until the one-shot task runs
            threeTicks.await();
            repeating.cancel(false);          // stop the repeating task
            // order may vary, e.g.:
            // tick 1
            // tick 2
            // hello after 300 ms
            // tick 3
        } finally {
            scheduler.shutdown();
            scheduler.awaitTermination(5, TimeUnit.SECONDS);
        }
        System.out.println("Scheduler terminated: " + scheduler.isTerminated()); // Scheduler terminated: true
    }
}
```

### `shutdown()` vs `shutdownNow()`
```java
import java.util.List;
import java.util.concurrent.*;

public class ExShutdownNowDemo {
    public static void main(String[] args) throws InterruptedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        CountDownLatch started = new CountDownLatch(1);

        executor.execute(() -> {
            started.countDown();
            try {
                Thread.sleep(5_000);                   // long task
                System.out.println("long task finished");
            } catch (InterruptedException e) {
                System.out.println("long task interrupted"); // shutdownNow interrupts it
            }
        });
        for (int i = 2; i <= 4; i++) {
            int id = i;
            executor.execute(() -> System.out.println("task " + id)); // stays queued
        }

        started.await();
        List<Runnable> neverStarted = executor.shutdownNow();
        System.out.println("Never started: " + neverStarted.size());   // Never started: 3
        System.out.println("Terminated in time: " + executor.awaitTermination(2, TimeUnit.SECONDS));
        // long task interrupted        (may print before or after "Never started")
        // Terminated in time: true

        try {
            executor.execute(() -> {});
        } catch (RejectedExecutionException e) {
            System.out.println("New task after shutdown rejected"); // New task after shutdown rejected
        }
    }
}
```

## Common Pitfalls / Tips
- **Always shut executors down** (`shutdown()` then `awaitTermination`, or try-with-resources on Java 19+). Pool threads are non-daemon, so a forgotten executor keeps the JVM alive.
- **`future.get()` blocks.** Calling it right after `submit` makes the code effectively synchronous. Submit everything first, then collect results. Use `get(timeout, unit)` to avoid waiting forever.
- **`submit` swallows exceptions if you never call `get()`.** The exception lives inside the `Future`. With `execute`, it goes to the uncaught-exception handler instead. A `try/catch` around `execute(...)` in the caller never sees it.
- **Lambdas capture only effectively final locals.** Copy the loop variable (`int taskId = i;`) before using it inside the task.
- **The pool grows past core size only when the queue is full.** With an unbounded `LinkedBlockingQueue`, `maximumPoolSize` has no effect, because the queue never fills. A common misconfiguration.
- **`newFixedThreadPool` / `newSingleThreadExecutor` use unbounded queues**, which can cause an OOM under sustained overload. **`newCachedThreadPool` has unbounded threads**, which can mean thousands of threads. For production, consider a `ThreadPoolExecutor` with explicit bounds and a rejection policy.
- **`DiscardPolicy` and `DiscardOldestPolicy` lose tasks silently.** Prefer `AbortPolicy` (fail loudly) or `CallerRunsPolicy` (back-pressure) unless losing work is acceptable.
- **`shutdownNow()` only interrupts.** Tasks that ignore interruption (busy loops without `isInterrupted()` checks) keep running.
- **`submit` also accepts a `Runnable`.** It returns a `Future<?>` whose `get()` returns `null`, which is still useful for waiting and for catching exceptions.
- **Sizing hint:** CPU-bound work → about `Runtime.getRuntime().availableProcessors()` threads. I/O-bound work → more, because threads spend time waiting (or use virtual threads, next video).

## Review Questions
1. Give three reasons why creating a new `Thread` for every task is a bad idea.
2. What does a worker thread do after it finishes a task in a thread pool, and why does that matter?
3. What is the difference between `Executor` and `ExecutorService`? Name four methods `ExecutorService` adds.
4. Why can't you write `Integer x = executor.submit(() -> 10);`? What do you use instead, and what does its `get()` do?
5. A task divides by zero. What happens if it was started with `execute` vs `submit`, and where can you catch the exception?
6. With `corePoolSize = 2`, `maximumPoolSize = 5` and `ArrayBlockingQueue(2)`, eight long-running tasks arrive at once. Which run immediately, which wait, and which are rejected?
7. What is the difference between `shutdown()` and `shutdownNow()`?
8. Compare `newFixedThreadPool`, `newCachedThreadPool` and `newSingleThreadExecutor`: threads, queue, and the main risk of each.
9. List the four built-in rejection policies and what each does.

<details>
<summary>Answers</summary>

1. Memory: each thread has its own stack (around 0.5–1 MB). Creation cost: a system call every time. Scheduling: far more threads than cores means constant context switching. On top of that, each thread is used once and thrown away.
2. It isn't terminated. It goes back to the pool, or takes the next task from the queue. Threads are reused, so their number stays bounded and creation cost is paid only once.
3. `Executor` has only `execute(Runnable)`. `ExecutorService` extends it with lifecycle and result methods: `submit`, `invokeAll`, `invokeAny`, `shutdown`, `shutdownNow`, `awaitTermination`, `isShutdown`/`isTerminated`.
4. `submit` returns immediately, before the task has run, so no `Integer` exists yet. It returns a `Future<Integer>`. `get()` blocks the calling thread until the result is ready and then returns it (or throws `ExecutionException` if the task failed).
5. `execute`: the exception is thrown on the worker thread and goes to the uncaught-exception handler (stack trace on stderr). A try/catch in the submitting thread can't catch it. `submit`: the exception is stored in the `Future`. Calling `get()` throws `ExecutionException` with the `ArithmeticException` as its cause, and you can catch that.
6. Tasks 1–2 start on the two core threads. Tasks 3–4 go into the queue (capacity 2). Tasks 5–7 each get a new extra thread (threads 3–5, reaching max). Task 8 is rejected (`RejectedExecutionException` with the default `AbortPolicy`). Tasks 3–4 run when a worker frees up.
7. `shutdown()` stops accepting new tasks but lets the running and queued tasks finish. `shutdownNow()` stops accepting tasks, interrupts running ones (best-effort), and returns the list of queued tasks that never started.
8. Fixed: n threads, unbounded queue, risk of out-of-memory from queued tasks. Cached: threads created on demand (reused when idle, removed after 60 s), no real queue (`SynchronousQueue`), risk of thread explosion. Single: one thread, unbounded queue, tasks run strictly sequentially in order, with the same queue-growth risk and no parallelism.
9. `AbortPolicy` (default): throw `RejectedExecutionException`. `DiscardPolicy`: silently drop the new task. `DiscardOldestPolicy`: drop the oldest queued task and enqueue the new one. `CallerRunsPolicy`: the submitting thread runs the task itself (back-pressure).
</details>

## Further Study
- `ThreadPoolExecutor` extras: `ThreadFactory` (naming threads, setting an `UncaughtExceptionHandler`), `prestartAllCoreThreads`, `allowCoreThreadTimeOut`
- Custom `RejectedExecutionHandler` implementations
- `scheduleAtFixedRate` vs `scheduleWithFixedDelay`; `ScheduledFuture.cancel`
- `Future.cancel`, `isCancelled`, `get(timeout, unit)` and `TimeoutException`
- `ExecutorCompletionService` (consume results in completion order)
- `CompletableFuture` (non-blocking composition instead of `get()`), next video
- `ForkJoinPool` and work stealing, next video
- Virtual threads and `Executors.newVirtualThreadPerTaskExecutor()` (Java 21)
- Sizing thread pools for CPU-bound vs I/O-bound workloads

---

[← #55](55-cas-retry-loop-aba-problem.md) · [Index](ReadMe.md) · [#57 →](57-completablefuture-forkjoin-threadlocal-virtual-threads.md)
