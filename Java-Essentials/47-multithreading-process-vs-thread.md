# 47 · Introduction to Multithreading · Process vs Thread
> Full notes: [47-multithreading-process-vs-thread.md](../47-multithreading-process-vs-thread.md) · [▶ Video](https://youtu.be/fyAW0W526RM)

## Key concepts

**Program.** A set of instructions stored on disk (`Demo.java`, `Demo.class`, or an installed app like Chrome). It does nothing until it runs.

**Process.** A program that is currently running. The **OS loads it from disk into RAM** (the CPU only works with RAM, never directly with the disk) and gives it:
- its own slice of **memory**
- **CPU time**
- **resources** such as files and network

Processes are **isolated**, so Chrome can't read VS Code's memory.

**Thread.** The smallest unit of work the CPU can schedule on its own, also called a "lightweight process". A process is split into one or more threads. The **CPU runs threads, not processes**, so "running P1" really means running P1's threads.

**Main thread.** The thread the JVM creates to run `main()`. It always exists, so every program so far has been single-threaded on `main`.

**Shared vs per-thread memory.**
- **Shared** by all threads in the process: the **heap** and the **method area**. This is how threads communicate, and also where bugs come from (race conditions, #50).
- **Private** to each thread: its own **stack** and **program counter (PC)**.

**Context switching.** The OS pauses a running thread, saves where it was, and resumes another one.
- What gets saved: the PC (the next instruction), the CPU registers and the stack state.
- This is how one core runs many threads.
- It's also why the order of execution is **unpredictable**.

**Concurrency vs parallelism.**
- **Concurrency:** several threads take turns on **one core**. They all make progress over time, but only one runs at any instant.
- **Parallelism:** threads run at the **same instant** on **different cores**.
- Only parallelism makes CPU-heavy work finish faster. Concurrency mainly keeps a program **responsive**.

**Multitasking vs multithreading** (a common interview question)

| | Multitasking | Multithreading |
|---|---|---|
| Level | Operating system concept | Programming concept |
| Units | Many **processes** (Chrome, VS Code, Spotify) | Many **threads** inside one process |
| Memory | Isolated per process | Heap and method area shared, stack and PC private |
| How it runs | Threads of all processes scheduled on all cores | Threads of one process, run concurrently and/or in parallel |

Both come down to threads being scheduled onto cores.

---

## Extra points from the detailed notes

### Why multithreading? It isn't mainly about speed
- Most people say "to make code faster", but the video says that's the smallest benefit.
- The bigger one is doing **several jobs at once**. For example, an IDE autocompletes while it checks for errors, and each job gets its own thread.

### The JVM inside a process
- The **OS creates the process**, and then the **JVM** runs inside it and manages everything within it: loading classes, running bytecode, memory (heap, stack, method area, PC) and garbage collection.
- The JVM splits the process's memory into its own areas. The heap is the largest.
- **OS view vs JVM view of memory:**

| OS (any language) | JVM |
| :--- | :--- |
| Code and data segments | Method area |
| Heap | Heap |
| Stack | Stack |
| Registers | Program counter |

- There's also a native method stack for C/C++ code.
- **Why Java has its own layout:** it's platform independent, so it defines its own memory areas rather than following each OS's. A C++ program has no JVM, so the OS manages its memory directly.

### What happens when you run `java Demo`
1. The JVM loads into the process.
2. The **class loader** loads the class that has `main`.
3. The JVM creates the **main thread**.
4. The JVM gives the main thread its **own stack and PC**. These can't exist without a thread.
5. `main()` starts: a **stack frame** for it is pushed onto the main thread's stack and holds its local variables.

```
Process
└── JVM
    ├── Heap          (shared by all threads)   → objects
    ├── Method area   (shared by all threads)   → class metadata
    ├── main thread → own Stack + own PC        → methods running, current instruction
    └── Thread-1    → own Stack + own PC
```

### Context switching keeps progress
- A loop that's paused at iteration 50 resumes at **51**, not 0, because each thread's state is saved when it's paused.
- That makes switching fast enough to *look* simultaneous. That's how an old single-core PC could still seem to autocomplete and check errors at the same time.

### Parallel vs concurrent timing
- If main and T1 each need **3 s** of CPU time:
  - **2 cores (parallel):** both finish in about **3 s**.
  - **1 core (concurrent):** about **6 s** in total.
- This is where the claim that "multithreading makes code faster" comes from.

### Real machines do both
- There are far more threads (from many processes) than cores. For example, 5 threads on 2 cores.
- The cores run threads **in parallel**, and each core also **context switches** between its waiting threads. A thread can run on core 1 now and core 2 later.
- The CPU doesn't care which process a thread belongs to.
- The **OS scheduler** decides which thread runs where and when, not you. You only control which threads are ready to run.

**Remember:** a program on disk becomes a process in RAM, and a process is made of threads. Threads share the heap and method area but each has its own stack and PC. One core gives concurrency through context switching. Many cores give true parallelism.
