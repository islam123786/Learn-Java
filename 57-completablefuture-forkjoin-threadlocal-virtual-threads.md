# CompletableFuture, Fork-Join Pool, ThreadLocal & Virtual Threads | Java Full Course #57
> Source: https://youtu.be/FGN225TiXaE · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 82:18. The video is in Hindi. These notes were written from YouTube's English (translated) track. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `Future` has three `submit` overloads (`Callable`, `Runnable`, `Runnable` + fixed result) and five useful methods: `get()`, `get(timeout, unit)`, `isDone()`, `cancel(mayInterruptIfRunning)` and `isCancelled()`. Its main weakness is that `get()` **blocks**, and you can't **chain** follow-up work onto it.
- `CompletableFuture` fixes both. `supplyAsync` (returns a value) or `runAsync` (returns nothing) starts the work, and `thenApply` (transform), `thenAccept` (consume), `thenRun` (just run) and `thenCombine` (merge two futures) chain more steps on without blocking. By default it runs on the `ForkJoinPool` common pool, and you can pass your own `Executor` instead.
- `ForkJoinPool` is built for **divide and conquer**. Every worker has its own deque, called a *work-stealing queue*. A task splits itself (`fork`), and idle workers **steal** subtasks from busy ones. You write tasks by extending `RecursiveTask<V>` (returns a value) or `RecursiveAction` (returns nothing) and overriding `compute()`.
- `ThreadLocal<T>` gives each thread its own value behind one shared variable (`set`/`get`), so there are no races and no locks. In thread pools, always call `remove()`.
- **Virtual threads** (Java 21) are cheap threads managed by the JVM. Many of them are multiplexed onto a few OS "carrier" threads. When a virtual thread blocks, it is unmounted and its carrier runs another one. Create them with `Thread.startVirtualThread(...)` or `Executors.newVirtualThreadPerTaskExecutor()`. They are a great fit for I/O-heavy work such as web requests and DB calls.

## Key Concepts

**`Future<V>`.** A handle to the result of an asynchronous task submitted to an executor. You don't know *when* the result will be ready, so you can't put it in an ordinary variable. *Why it matters:* it's the basic building block. Everything else in this video improves on it. *(The video calls it a class. It's actually an interface. `FutureTask` and `CompletableFuture` are implementations.)*

**Blocking `get()`.** `get()` stops the calling thread until the result arrives. If you call it right after `submit`, you've turned async code back into sync code. *Why it matters:* this is the main limitation that `CompletableFuture` solves.

**`CompletableFuture<T>`.** A `Future` you can attach callbacks to. Each `then…` method returns a new `CompletableFuture`, so steps form a non-blocking pipeline. *Why it matters:* it's the style used in real production code, such as Spring Boot `@Async` services and parallel API calls.

**Functional types behind the chain.** `supplyAsync` takes a `Supplier<T>`, `runAsync` a `Runnable`, `thenApply` a `Function<T,R>`, `thenAccept` a `Consumer<T>`, `thenRun` a `Runnable`, and `thenCombine` a `BiFunction<T,U,R>`. *Why it matters:* the type tells you what the next stage gets. For example, `thenAccept` yields a `CompletableFuture<Void>`.

**`ForkJoinPool` and work stealing.** This is a pool where each worker thread owns a double-ended queue. A worker splits a big task, keeps working on one half, and pushes the other half onto its own deque. Idle workers take tasks from the shared submission queue or steal from other workers' deques. *Why it matters:* it keeps every core busy on recursive, splittable work. The same pool type also backs parallel streams, the `CompletableFuture` default, and virtual-thread carriers.

**`RecursiveTask<V>` / `RecursiveAction`.** Abstract `ForkJoinTask` subclasses. You override `protected V compute()` (or `void compute()` for `RecursiveAction`) with a base case plus a split → `fork()` → `compute()` → `join()` step. *Why it matters:* a `ForkJoinPool` can only split work that you write in this form.

**`ThreadLocal<T>`.** One variable with a separate value per thread. *Why it matters:* you get per-thread state, such as the current user, a request ID or a non-thread-safe formatter, without passing it through every method and without locking.

**Platform thread vs virtual thread.** A platform thread is a thin wrapper around an OS thread (1:1). It's expensive, with roughly 512 KB to 1 MB of stack, so you can only have a few thousand. A virtual thread is a JVM object scheduled onto a small set of carrier threads (M:N). You can have millions. *Why it matters:* you can go back to simple "one thread per request" blocking code and still scale.

## Detailed Notes

### Recap: Future and Callable [▶ ~00:00](https://youtu.be/FGN225TiXaE?t=0)
- This is the last lecture of the series. It builds on #56 (Executor framework).
- `executor.submit(task)` hands the task to a pool. It may run now or wait in the queue, so the result goes into a `Future`.
- `Callable` is like `Runnable` but returns a value. *(The video calls `Runnable` "consumer-type". Strictly, a `Runnable` takes nothing and returns nothing, while a `Consumer` takes an argument.)*

### The three `submit` overloads [▶ ~01:53](https://youtu.be/FGN225TiXaE?t=113)
1. `submit(Callable<T>)` → `Future<T>`, e.g. `() -> 10` gives a `Future<Integer>`.
2. `submit(Runnable)` → `Future<?>`. `get()` returns `null`. You still keep the `Future` so that you can call `isDone()` or `cancel()` on it.
3. `submit(Runnable, T result)` → `Future<T>`. `get()` returns the result you supplied (e.g. `"success"`) once the runnable finishes. It's an easy way to signal "done".

### Future's methods [▶ ~06:00](https://youtu.be/FGN225TiXaE?t=360)
- **`get()`** blocks the current thread until the result is ready. It throws the checked exceptions `InterruptedException` and `ExecutionException` (the latter wraps whatever the task threw).
- **Anti-pattern:** calling `submit(...)` and then `get()` on the very next line. The main thread just waits, so you might as well have called a plain method. Better: submit, do other useful work, and call `get()` only when you actually need the value.
- **`get(2, TimeUnit.SECONDS)`** waits at most that long, then throws `TimeoutException`.
- **`isDone()`** is non-blocking and returns true or false. You can check it and only call `get()` once it's true.
- **`cancel(boolean mayInterruptIfRunning)`** [▶ ~12:30](https://youtu.be/FGN225TiXaE?t=750):
  - Task not started yet (still in the queue): it is cancelled and will never run.
  - Task already running with `true`: the worker thread gets **interrupted**. If the task is in `sleep`/`wait`/blocking I/O it gets an `InterruptedException`. A busy loop only stops if it checks `Thread.interrupted()`.
  - Task already running with `false`: the task runs to completion.
  - *Correction:* the video says a task that can't be interrupted "will throw an InterruptedException". It's more accurate to say that interruption is only a request. After a successful `cancel`, `isCancelled()` and `isDone()` both return `true` right away, and **`get()` throws `CancellationException`**, even if the task's code is technically still running.
- **`isCancelled()`** returns whether the task was cancelled.

### Limitations of Future [▶ ~15:45](https://youtu.be/FGN225TiXaE?t=945)
1. **Blocking**: the only way to get the result is `get()`, which blocks.
2. **No chaining**: you can't say "when this finishes, transform it, then combine it with that one". `Future` is *passive*: you can only poll it or wait on it.
- `CompletableFuture` is the "advanced Future" that fixes both, and it's used far more in production code.

### CompletableFuture: supplyAsync / runAsync [▶ ~18:41](https://youtu.be/FGN225TiXaE?t=1121)
- You don't need to create an executor. `CompletableFuture.supplyAsync(() -> 10)` submits the work to the **`ForkJoinPool` common pool**.
- `supplyAsync(Supplier)` is for work that returns a value. `runAsync(Runnable)` returns a `CompletableFuture<Void>`. *(The video says supplyAsync takes a Callable. It actually takes a `Supplier`, which can't throw checked exceptions.)*
- Demo [▶ ~21:56](https://youtu.be/FGN225TiXaE?t=1316): `f1.get()` still works and prints 10, but that defeats the purpose.

### Chaining: thenApply, thenAccept, thenRun [▶ ~24:10](https://youtu.be/FGN225TiXaE?t=1450)
- **`thenApply(Function)`**: runs after the previous stage and transforms its result. `supplyAsync(() -> 10).thenApply(r -> r * 2).thenApply(r -> r * 3)` gives **60**.
- **`thenAccept(Consumer)`** [▶ ~26:27](https://youtu.be/FGN225TiXaE?t=1587): consumes the result (e.g. prints it) and returns nothing. The result type becomes `CompletableFuture<Void>`, and assigning it to `CompletableFuture<Integer>` is a compile error ("cannot convert from `CompletableFuture<Void>`…").
- **`thenRun(Runnable)`** [▶ ~28:56](https://youtu.be/FGN225TiXaE?t=1736): ignores the result and just runs something, e.g. prints `"Done"`.
- The point: these stages are **non-blocking**. The main thread carries on while the pipeline runs.
  - *Caveat:* common-pool threads are daemon threads. If `main` ends before the pipeline finishes, the JVM exits and the `thenAccept` output never appears. It worked in the demo because the task was instant. In small programs, finish with `.join()`. In servers, the app stays alive anyway.

### thenCombine [▶ ~29:30](https://youtu.be/FGN225TiXaE?t=1770)
- `f1.thenCombine(f2, (a, b) -> a + b)` waits for **both** futures and merges their results with a `BiFunction`. 10 + 20 gives 30, and you can chain `.thenAccept(System.out::println)` onto it.

### Custom executor [▶ ~32:05](https://youtu.be/FGN225TiXaE?t=1925)
- Every `…Async` method has an overload that takes an `Executor`: `supplyAsync(task, myExecutor)`. Use it when you want a dedicated pool (e.g. one created with `Executors.newFixedThreadPool`) instead of the shared common pool. This matters for blocking I/O, which should not tie up common-pool threads.

### ThreadPoolExecutor recap → ForkJoinPool idea [▶ ~33:08](https://youtu.be/FGN225TiXaE?t=1988)
- A normal pool has N workers and one shared queue. Extra tasks wait in the queue, and new threads are created up to the maximum pool size.
- **Fork/Join principle** [▶ ~35:28](https://youtu.be/FGN225TiXaE?t=2128): split a big task into subtasks, split those again recursively until a **base case**, solve the small pieces in parallel, then **join** the partial results. This is divide and conquer, as in DSA. *Fork* = split, *join* = combine.

### Inside ForkJoinPool: work-stealing queues [▶ ~37:10](https://youtu.be/FGN225TiXaE?t=2230)
- A ForkJoinPool has worker threads plus a **submission queue** (shared, like the normal blocking queue).
- Each worker **also has its own deque**, the **work-stealing queue**.
- **Scenario 1:** Task 1 arrives and T1 takes it. T1 splits it into ST1 and ST2, keeps working on ST1, and pushes ST2 onto its own deque. T2 is idle, the submission queue is empty and its own deque is empty, so it **steals** ST2 from T1's deque. Both finish, and the results are joined into Task 1's result.
- **Scenario 2** [▶ ~41:27](https://youtu.be/FGN225TiXaE?t=2487): Task 1 can't be split, so T1 runs it whole. Task 2 is split, so T2 runs ST1 and parks ST2 in its own deque. Task 3 waits in the submission queue. When T1 becomes free, it takes **Task 3 from the submission queue first**. Only when that is done and nothing else is waiting does it steal ST2 from T2.
- The rule from the video [▶ ~44:52](https://youtu.be/FGN225TiXaE?t=2692): shared submission-queue work comes first, and stealing happens when there's nothing else to do. *(This is a simplification. Real workers drain their **own** deque first, LIFO from the top, and then scan other queues, both submission queues and other workers' deques, stealing FIFO from the bottom. Stealing the oldest, usually biggest, piece is what makes it efficient.)*

### RecursiveTask and RecursiveAction [▶ ~45:24](https://youtu.be/FGN225TiXaE?t=2724)
- A ForkJoinPool can only split tasks written as `RecursiveTask<V>` (returns a value) or `RecursiveAction` (returns nothing). Both are abstract classes. You extend one and override `compute()`.
- Example problem: the sum of a huge array. A loop is O(n) on one thread. Two threads each summing half, then adding `s1 + s2`, roughly halves the time, and you can keep splitting recursively. This works because the problem is **independent** (the order of the additions doesn't matter).

### Coding the SumTask [▶ ~48:49](https://youtu.be/FGN225TiXaE?t=2929)
- `class SumTask extends RecursiveTask<Integer>` with fields `arr`, `start` and `end`, set through the constructor.
- `protected Integer compute()`:
  1. **Base case:** if the range is small (the video uses `end - start <= 2`), sum it with a plain loop.
  2. **Divide:** `mid = (start + end) / 2`, then `left = new SumTask(arr, start, mid)` and `right = new SumTask(arr, mid + 1, end)`.
  3. **Fork / compute / join:** `left.fork()` pushes the left half onto this worker's deque so another thread can steal it. `right.compute()` does the right half on the current thread. `left.join()` waits for the left half's result. Return `leftSum + rightSum`.
- Compared with single-threaded divide and conquer (`left.compute() + right.compute()`), the only change is that the left half is offered to other threads.
- `main`: `ForkJoinPool pool = new ForkJoinPool(); int result = pool.invoke(new SumTask(arr, 0, arr.length - 1)); pool.shutdown();`
- The bug in the demo [▶ ~62:12](https://youtu.be/FGN225TiXaE?t=3732): passing `arr.length` as `end` caused `ArrayIndexOutOfBoundsException`, because the code treats `end` as **inclusive**. The fix is `arr.length - 1`. For 1..8 the output is **36**.
- `Executors.newWorkStealingPool()` [▶ ~62:45](https://youtu.be/FGN225TiXaE?t=3765) also returns a `ForkJoinPool`, but the video recommends using the `ForkJoinPool` class directly.

### ThreadLocal [▶ ~62:56](https://youtu.be/FGN225TiXaE?t=3776)
- Heap memory is shared. If T1, T2 and T3 all write `resource.name`, they race and overwrite each other.
- `ThreadLocal<String> name = new ThreadLocal<>();` keeps **one value per thread**. Inside T1, `name.set("Aditya")`. Inside T2, `name.set("Rohit")`. Then `name.get()` anywhere in T1's code returns "Aditya" and anywhere in T2's returns "Rohit". There's no race and no lock.
- **Why not just a local variable?** [▶ ~66:33](https://youtu.be/FGN225TiXaE?t=3993) Locals live only as long as one method call, and you'd have to pass them as parameters everywhere. A `ThreadLocal` field lives in a long-lived object and is reachable from any method running on that thread.

### Virtual threads: the problem with platform threads [▶ ~67:07](https://youtu.be/FGN225TiXaE?t=4027)
- `new Thread(...)` followed by `start()` asks the OS for a real thread and links the JVM thread to it **one-to-one** [▶ ~69:14](https://youtu.be/FGN225TiXaE?t=4154).
- Problems:
  - Each thread is expensive (roughly 512 KB to 1 MB of stack, plus creation cost), and having many of them causes heavy context switching.
  - When a thread blocks on I/O, its OS thread sits idle and can't be used for anything else.
  - Thread-per-request web servers therefore don't scale. Thread pools (the Executor framework) cap the thread count, but that also caps concurrency at the pool size.

### Virtual threads: how they work [▶ ~71:41](https://youtu.be/FGN225TiXaE?t=4301)
- The JVM manages **virtual threads**. You can create a huge number, and they're just lightweight objects.
- When a virtual thread actually needs to run, the JVM **mounts** it on an OS-backed **carrier thread**.
- When it blocks (I/O, `sleep`, waiting on a lock), the JVM **unmounts** it and puts a runnable virtual thread on that carrier. OS threads stay busy, and when the blocked virtual thread is ready again it gets mounted on whichever carrier is free.
- The mapping is "one OS thread to many virtual threads" (M:N). You can have hundreds of thousands of virtual threads with almost no memory cost.

### Virtual threads: code [▶ ~76:03](https://youtu.be/FGN225TiXaE?t=4563)
- `Thread t1 = Thread.startVirtualThread(runnable); t1.join();`
  - Printing `Thread.currentThread()` shows something like `VirtualThread[#21]/runnable@ForkJoinPool-1-worker-1`. The carrier threads come from a ForkJoinPool.
  - `getName()` printed an empty string. *Correction:* that's not because "we don't use getName() on virtual threads". Virtual threads simply have **no name by default**. Use `Thread.ofVirtual().name("x").start(...)` to give them one.
  - Without `join()`, nothing printed. *Correction:* the video says it "behaves like a daemon thread but isn't one". In fact virtual threads **are always daemon threads** (`isDaemon()` returns `true`), so the JVM doesn't wait for them.
- The usual way [▶ ~79:10](https://youtu.be/FGN225TiXaE?t=4750): `Executors.newVirtualThreadPerTaskExecutor()`. Submit tasks and each one runs in its own new virtual thread. *(It's not really a "pool": virtual threads are never reused, because they're cheap enough to throw away.)* The video uses `Thread.sleep(3000)` to wait for output. Closing the executor (try-with-resources), or `shutdown()` + `awaitTermination()`, is the proper way.
- Use cases [▶ ~81:03](https://youtu.be/FGN225TiXaE?t=4863): web servers (one virtual thread per request), API calls and DB calls, which is any work that spends most of its time **waiting**.

## Code Examples

Each block is a complete program. Save it under the class name (e.g. `CfFutureDemo.java`) and run it with `java CfFutureDemo.java` (JDK 21+).

### Future: the three submit overloads, timeout, isDone, cancel
```java
import java.util.concurrent.*;

public class CfFutureDemo {
    public static void main(String[] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(2);
        try {
            // 1) Callable -> Future<Integer>
            Future<Integer> f1 = executor.submit(() -> 10);
            // 2) Runnable -> Future<?> (get() returns null)
            Future<?> f2 = executor.submit(() -> System.out.println("hello"));
            // 3) Runnable + predefined result
            Future<String> f3 = executor.submit(() -> {}, "success");

            System.out.println(f2.get()); // prints "hello" (from the task) then "null"
            System.out.println(f1.get()); // 10
            System.out.println(f3.get()); // success

            // get with a timeout
            Future<Integer> slow = executor.submit(() -> { Thread.sleep(500); return 1; });
            try {
                slow.get(100, TimeUnit.MILLISECONDS);
            } catch (TimeoutException e) {
                System.out.println("TimeoutException: not ready in 100 ms");
            }

            // cancel a running task (true = interrupt it)
            System.out.println("cancel: " + slow.cancel(true));    // cancel: true
            System.out.println("isCancelled: " + slow.isCancelled()); // isCancelled: true
            System.out.println("isDone: " + slow.isDone());         // isDone: true
            try {
                slow.get();
            } catch (CancellationException e) {
                System.out.println("get() -> CancellationException");
            }

            // isDone(): poll without blocking, do other work meanwhile
            Future<Integer> work = executor.submit(() -> { Thread.sleep(100); return 42; });
            while (!work.isDone()) {
                Thread.sleep(20); // main thread "does other work" here
            }
            System.out.println("result: " + work.get()); // result: 42
        } finally {
            executor.shutdown();
        }
    }
}
// Output:
// hello
// null
// 10
// success
// TimeoutException: not ready in 100 ms
// cancel: true
// isCancelled: true
// isDone: true
// get() -> CancellationException
// result: 42
```

### CompletableFuture chaining
```java
import java.util.concurrent.*;

public class CfChainingDemo {
    public static void main(String[] args) {
        // thenApply: transform (Function)
        CompletableFuture<Integer> f1 = CompletableFuture
                .supplyAsync(() -> 10)
                .thenApply(r -> r * 2)
                .thenApply(r -> r * 3);
        System.out.println(f1.join()); // 60

        // thenAccept: consume (Consumer) -> CompletableFuture<Void>
        CompletableFuture<Void> f2 = CompletableFuture
                .supplyAsync(() -> 10)
                .thenAccept(r -> System.out.println("accepted " + r));
        f2.join(); // only so main doesn't exit early; prints: accepted 10

        // thenRun: just run something (Runnable)
        CompletableFuture.supplyAsync(() -> 10)
                .thenRun(() -> System.out.println("Done"))
                .join(); // Done

        // thenCombine: merge two independent futures (BiFunction)
        CompletableFuture<Integer> a = CompletableFuture.supplyAsync(() -> 10);
        CompletableFuture<Integer> b = CompletableFuture.supplyAsync(() -> 20);
        a.thenCombine(b, (x, y) -> x + y)
         .thenAccept(sum -> System.out.println("sum = " + sum))
         .join(); // sum = 30

        // runAsync: no return value
        CompletableFuture.runAsync(() -> System.out.println("runAsync ran")).join(); // runAsync ran

        // default pool vs a custom executor
        String defaultThread = CompletableFuture
                .supplyAsync(() -> Thread.currentThread().getName()).join();
        System.out.println(defaultThread.startsWith("ForkJoinPool.commonPool")); // true

        ExecutorService pool = Executors.newFixedThreadPool(2);
        try {
            String customThread = CompletableFuture
                    .supplyAsync(() -> Thread.currentThread().getName(), pool).join();
            System.out.println(customThread.startsWith("pool-")); // true
        } finally {
            pool.shutdown();
        }
    }
}
// Output:
// 60
// accepted 10
// Done
// sum = 30
// runAsync ran
// true
// true
```

### ForkJoinPool: RecursiveTask (sum) and RecursiveAction (in-place doubling)
```java
import java.util.Arrays;
import java.util.concurrent.*;
import java.util.stream.LongStream;

public class CfForkJoinDemo {
    public static void main(String[] args) {
        // The video's example: 8 elements, tiny threshold, end index inclusive
        long[] small = {1, 2, 3, 4, 5, 6, 7, 8};
        ForkJoinPool pool = new ForkJoinPool();
        try {
            long result = pool.invoke(new CfSumTask(small, 0, small.length - 1, 2));
            System.out.println(result); // 36
        } finally {
            pool.shutdown();
        }

        // A realistic size and threshold, using the shared common pool
        long[] big = LongStream.rangeClosed(1, 1_000_000).toArray();
        long bigSum = ForkJoinPool.commonPool().invoke(new CfSumTask(big, 0, big.length - 1, 10_000));
        System.out.println(bigSum); // 500000500000

        // RecursiveAction: no return value, modifies the array in place
        long[] data = {1, 2, 3, 4, 5};
        ForkJoinPool.commonPool().invoke(new CfDoubleAction(data, 0, data.length - 1));
        System.out.println(Arrays.toString(data)); // [2, 4, 6, 8, 10]
    }
}

class CfSumTask extends RecursiveTask<Long> {
    private final long[] arr;
    private final int start, end;   // end is inclusive
    private final int threshold;

    CfSumTask(long[] arr, int start, int end, int threshold) {
        this.arr = arr; this.start = start; this.end = end; this.threshold = threshold;
    }

    @Override
    protected Long compute() {
        // 1) Base case: small enough -> plain loop
        if (end - start <= threshold) {
            long sum = 0;
            for (int i = start; i <= end; i++) sum += arr[i];
            return sum;
        }
        // 2) Divide
        int mid = (start + end) / 2;
        CfSumTask left = new CfSumTask(arr, start, mid, threshold);
        CfSumTask right = new CfSumTask(arr, mid + 1, end, threshold);
        // 3) Fork left (may be stolen), compute right here, then join left
        left.fork();
        long rightSum = right.compute();
        long leftSum = left.join();
        return leftSum + rightSum;
    }
}

class CfDoubleAction extends RecursiveAction {
    private final long[] arr;
    private final int start, end; // inclusive

    CfDoubleAction(long[] arr, int start, int end) {
        this.arr = arr; this.start = start; this.end = end;
    }

    @Override
    protected void compute() {
        if (end - start <= 1) {
            for (int i = start; i <= end; i++) arr[i] *= 2;
            return;
        }
        int mid = (start + end) / 2;
        invokeAll(new CfDoubleAction(arr, start, mid), new CfDoubleAction(arr, mid + 1, end));
    }
}
// Output:
// 36
// 500000500000
// [2, 4, 6, 8, 10]
```

### ThreadLocal: per-thread values, and why `remove()` matters in pools
```java
import java.util.concurrent.*;

public class CfThreadLocalDemo {
    static final ThreadLocal<String> USER = new ThreadLocal<>();

    public static void main(String[] args) throws Exception {
        CfResource resource = new CfResource();

        Thread t1 = new Thread(() -> { resource.name.set("Aditya"); greet(resource); }, "T1");
        Thread t2 = new Thread(() -> { resource.name.set("Rohit");  greet(resource); }, "T2");
        t1.start(); t2.start();
        t1.join();  t2.join();
        // order may vary:
        // T1 -> Aditya
        // T2 -> Rohit

        System.out.println("main sees: " + resource.name.get()); // main sees: null

        // withInitial: every thread starts with its own default value
        ThreadLocal<Integer> counter = ThreadLocal.withInitial(() -> 0);
        counter.set(counter.get() + 1);
        System.out.println("main counter: " + counter.get()); // main counter: 1

        // Pitfall: pool threads are reused, so values leak between tasks
        try (ExecutorService single = Executors.newSingleThreadExecutor()) {
            single.submit(() -> USER.set("alice")).get();          // task 1 forgets to clean up
            System.out.println("leaked: " + single.submit(() -> USER.get()).get()); // leaked: alice

            single.submit(() -> {
                try {
                    USER.set("bob");
                    return USER.get();
                } finally {
                    USER.remove();                                // always clean up
                }
            }).get();
            System.out.println("after remove: " + single.submit(() -> USER.get()).get()); // after remove: null
        }
    }

    static void greet(CfResource r) {
        // the same field, a different value depending on the calling thread
        System.out.println(Thread.currentThread().getName() + " -> " + r.name.get());
    }
}

class CfResource {
    final ThreadLocal<String> name = new ThreadLocal<>();
}
```

### Virtual threads
```java
import java.time.Duration;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class CfVirtualThreadDemo {
    public static void main(String[] args) throws Exception {
        // 1) Start a single virtual thread
        Thread t1 = Thread.startVirtualThread(
                () -> System.out.println(Thread.currentThread() + " says hello"));
        t1.join(); // virtual threads are daemon threads: without join, main may exit first
        // e.g. VirtualThread[#21]/runnable@ForkJoinPool-1-worker-1 says hello  (ids vary)

        System.out.println("isVirtual: " + t1.isVirtual());      // isVirtual: true
        System.out.println("isDaemon: " + t1.isDaemon());        // isDaemon: true
        System.out.println("name: [" + t1.getName() + "]");      // name: []  (unnamed by default)

        Thread named = Thread.ofVirtual().name("vt-worker").start(
                () -> System.out.println("I am " + Thread.currentThread().getName()));
        named.join(); // I am vt-worker

        // 2) One virtual thread per task; close() waits for all tasks
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 1; i <= 5; i++) {
                int id = i;
                executor.submit(() ->
                        System.out.println("Task " + id + " executed by " + Thread.currentThread()));
            }
        }
        // order may vary:
        // Task 2 executed by VirtualThread[#25]/runnable@ForkJoinPool-1-worker-2
        // Task 1 executed by VirtualThread[#24]/runnable@ForkJoinPool-1-worker-1 ...

        // 3) 10,000 blocking tasks: cheap, because a sleeping virtual thread frees its carrier
        AtomicInteger done = new AtomicInteger();
        long start = System.nanoTime();
        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            for (int i = 0; i < 10_000; i++) {
                executor.submit(() -> {
                    Thread.sleep(Duration.ofMillis(200)); // simulates waiting on I/O
                    done.incrementAndGet();
                    return null;
                });
            }
        }
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("completed: " + done.get());     // completed: 10000
        System.out.println("well under 10,000 x 200 ms: " + (ms < 10_000)); // well under 10,000 x 200 ms: true
    }
}
```

## Common Pitfalls / Tips
- **Don't call `get()` right after `submit()`.** That makes the code synchronous again. Do other work first, or use `CompletableFuture` callbacks.
- **`cancel(true)` only *requests* interruption.** Tasks must respond to it: blocking calls throw `InterruptedException`, and loops should check `Thread.currentThread().isInterrupted()`. After cancelling, `get()` throws `CancellationException`.
- **`CompletableFuture` and daemon threads.** Common-pool threads don't keep the JVM alive. In a `main` method, finish the pipeline with `join()` or the callback output may never appear.
- **`join()` vs `get()`.** On a `CompletableFuture`, `join()` throws the unchecked `CompletionException`, while `get()` throws checked exceptions. `join()` reads better inside lambdas.
- **Handle failures in the chain.** If any stage throws, the later `thenApply`/`thenAccept` stages are skipped. Add `.exceptionally(ex -> fallback)` or `.handle((v, ex) -> ...)`.
- **Don't run blocking I/O on the common pool.** It's shared with parallel streams and sized to the CPU count. Pass your own executor (or a virtual-thread executor) to `supplyAsync`.
- **Fork/Join order matters.** Use `left.fork(); right.compute(); left.join();`. Calling `left.join()` before computing the right half (or forking both and joining both) wastes the current thread.
- **Pick a sensible threshold.** Splitting down to 2 elements, as in the demo, creates millions of tiny tasks for big arrays. Real thresholds are usually thousands of elements, or "stop when the chunk is small enough".
- **Inclusive vs exclusive `end`.** Decide once. The demo's `ArrayIndexOutOfBoundsException` came from passing `arr.length` to code that treats `end` as inclusive.
- **Always `remove()` ThreadLocals in pooled threads.** Otherwise the next task on that thread sees stale data (a security bug in web apps) and you leak memory.
- **Virtual threads are for waiting, not for computing.** CPU-bound work gains nothing, because the number of carriers is still about the number of cores. Use ForkJoin or parallel streams for that.
- **Don't pool virtual threads.** Create one per task. To limit access to a scarce resource (e.g. 10 DB connections), use a `Semaphore`.
- **Pinning (JDK 21–23).** A virtual thread that blocks inside a `synchronized` block stays pinned to its carrier. Prefer `ReentrantLock` around blocking calls. JDK 24 removed most of this limitation.
- **Use `ThreadLocal` sparingly with virtual threads.** With millions of threads you get millions of copies. Look at `ScopedValue` (preview, later finalized) as the modern alternative.

## Review Questions
1. What are the three `submit` overloads, and what does `get()` return for each?
2. Why is `Future f = executor.submit(task); f.get();` on consecutive lines considered pointless?
3. Describe what `cancel(true)` and `cancel(false)` do in each task state. What happens if you call `get()` afterwards?
4. What are the two main limitations of `Future`, and how does `CompletableFuture` address them?
5. What's the difference between `thenApply`, `thenAccept`, `thenRun` and `thenCombine`? What is the result type of `supplyAsync(() -> 10).thenAccept(System.out::println)`?
6. Which executor does `CompletableFuture.supplyAsync` use by default, and how do you change it?
7. Explain work stealing. What are the submission queue and the per-worker queues for?
8. When do you extend `RecursiveTask` vs `RecursiveAction`? Write the shape of a typical `compute()`.
9. How is a `ThreadLocal` field different from a local variable and from a normal shared field? Why must you call `remove()` in a thread pool?
10. Why do virtual threads scale better than platform threads for I/O-heavy servers? What happens when a virtual thread blocks?

<details>
<summary>Answers</summary>

1. `submit(Callable<T>)` returns a `Future<T>` whose `get()` gives the callable's return value. `submit(Runnable)` returns a `Future<?>` whose `get()` gives `null`. `submit(Runnable, T result)` returns a `Future<T>` whose `get()` gives the result you passed in, once the runnable finishes.
2. `get()` blocks the caller until the task finishes, so the main thread just sits waiting. That's the same as calling a normal method synchronously, plus extra overhead. You get the benefit only if the caller does other work before calling `get()`.
3. If the task hasn't started yet, either call cancels it and it never runs. If it's running, `true` interrupts the worker thread (the task stops if it responds to interruption), while `false` lets it run to completion. Once `cancel` returns `true`, `isCancelled()` and `isDone()` are both true and `get()` throws `CancellationException`.
4. (1) The only way to get the result, `get()`, blocks. (2) You can't chain or combine results. `CompletableFuture` lets you attach callbacks (`thenApply`, `thenAccept`, `thenCombine`, …) that run when the value arrives, without blocking the caller.
5. `thenApply` transforms the value (`Function`, new result type). `thenAccept` consumes it (`Consumer`, gives `Void`). `thenRun` ignores it and runs a `Runnable` (`Void`). `thenCombine` merges this future with another one through a `BiFunction`. The example's type is `CompletableFuture<Void>`.
6. `ForkJoinPool.commonPool()`. To change it, pass an `Executor` as the second argument, e.g. `supplyAsync(task, myExecutor)`.
7. Every worker has its own deque. When a task forks, the subtask goes onto the current worker's deque. Idle workers take new work from the shared submission queue and, when they find nothing there, steal queued subtasks from other workers' deques. This keeps every thread busy with split-up work.
8. Extend `RecursiveTask<V>` when `compute()` should return a value (e.g. a sum), and `RecursiveAction` when it returns nothing (e.g. sorting or modifying an array in place). The shape: `if (small) return solveDirectly(); split into left/right; left.fork(); r = right.compute(); l = left.join(); return combine(l, r);`
9. A local variable lives only for one method call and has to be passed around explicitly. A normal field is shared by all threads (race conditions). A `ThreadLocal` field is one long-lived variable that holds a separate value per thread and is reachable from any method on that thread. Pool threads are reused, so a value left behind by one task is seen by the next task (wrong data, memory leaks). `remove()` in a `finally` block prevents that.
10. Platform threads map 1:1 to OS threads. They are expensive (large stacks, creation cost, context switching), and a blocked one wastes its OS thread. Virtual threads are cheap JVM objects multiplexed onto a few carrier threads. When one blocks on I/O, a lock or `sleep`, it is unmounted and the carrier runs another runnable virtual thread. When the blocked one becomes ready, it is mounted again on a free carrier. You can have huge numbers of concurrent waiting tasks.
</details>

## Further Study
- `CompletableFuture` error handling: `exceptionally`, `handle`, `whenComplete`; and `thenCompose` (flat-map) vs `thenApply`
- `CompletableFuture.allOf` / `anyOf`, `orTimeout`, `completeOnTimeout`
- `…Async` variants (`thenApplyAsync`) and which thread runs each stage
- `ForkJoinPool.commonPool()` sizing (`java.util.concurrent.ForkJoinPool.common.parallelism`) and parallel streams
- Work-stealing deque internals (LIFO for the owner, FIFO for thieves); `ForkJoinTask.invokeAll`
- `InheritableThreadLocal`, `ScopedValue` and structured concurrency (`StructuredTaskScope`)
- Virtual thread pinning, `jdk.tracePinnedThreads`, and JEP 491 (no pinning on `synchronized` in JDK 24)
- `Thread.ofVirtual()` / `Thread.ofPlatform()` builders and `ThreadFactory`
- Reactive programming (Project Reactor, RxJava) compared with virtual threads

---

[← #56](56-executor-framework-future-callable.md) · [Index](ReadMe.md) · (end) →
