# Introduction to Multithreading in Java | Process vs Thread  | Java Full Course #47
> Source: https://youtu.be/fyAW0W526RM · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 51:37. The video is in Hindi; these notes are written in English from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A **program** is just instructions sitting on disk (`Demo.java`, `Demo.class`). When the OS loads it into RAM and runs it, it becomes a **process** with its own memory, CPU time and resources. Processes don't share memory with each other.
- A **thread** is the smallest sequence of instructions the CPU can schedule on its own (a "lightweight process"). Every Java process has at least one thread, `main`, and you can create more.
- Inside the JVM, all threads of a process **share the heap and method area**, but each thread gets its **own stack and program counter**.
- With one core, threads run **concurrently**: the OS keeps **context switching** between them, so they all make progress but only one runs at any instant. With several cores, threads can run in **true parallel**. Real machines do both.
- **Multitasking** (OS term) means many processes running at once. **Multithreading** (programming term) means many threads inside one process. Either way, the CPU only ever runs threads.

## Key Concepts

**Program.** A set of instructions stored on disk. It does nothing until it is executed. *Why it matters:* the difference between a program and a process is where the rest of the vocabulary starts.

**Process.** A program that is currently executing. The OS copies it from disk into RAM and gives it a slice of memory, CPU time and resources (files, network). *Why it matters:* processes are isolated. Chrome can't read VS Code's memory.

**Thread.** The smallest unit of execution the CPU schedules independently. A process is split into one or more threads. *Why it matters:* threads let one program do several things at once, like an IDE that autocompletes while it checks for errors.

**Main thread.** The thread the JVM creates to run `main()`. It exists even if you never create a thread yourself. *Why it matters:* every Java program you have written so far was single-threaded on `main`.

**Shared vs per-thread memory.** Heap and method area are shared by all threads of the process. Stack and program counter (PC) are private to each thread. *Why it matters:* shared memory is how threads communicate, and also where the bugs come from (race conditions, covered in #50).

**Context switching.** The OS pauses a running thread, saves where it was (PC, registers, stack state), and resumes another thread. *Why it matters:* this is how one core runs many threads, and it is the reason the order of execution is unpredictable.

**Concurrency vs parallelism.** Concurrency: several threads make progress over the same period, taking turns on one core. Parallelism: several threads run at the same instant on different cores. *Why it matters:* only parallelism makes CPU-bound work finish faster. Concurrency mainly keeps a program responsive.

**Multitasking vs multithreading.** Multitasking is the OS running many processes (Chrome, VS Code, Spotify). Multithreading is one process running many threads. *Why it matters:* this is a common interview question.

## Detailed Notes

### Why multithreading? [00:36]
- Normally the main thread executes your code top to bottom, one instruction at a time (with `if`s and loops branching the flow).
- Multithreading adds more threads (T1, T2, T3, ...) that execute parts of the code alongside `main`.
- Most people answer "to make the code faster". The video says that is only one advantage, and the smallest one.
- Example [02:26]: an IDE such as VS Code autocompletes while it underlines compile errors. A natural design gives each job its own thread so they run side by side.

### What is a program? [03:48]
- A set of instructions, for example `Demo.java`. `javac Demo.java` produces `Demo.class` (bytecode), which is still just a file on disk.
- Running `java Demo` is what actually executes it. Installed apps (Chrome, VS Code, Spotify) are also just programs until you open them.

### What is a process? [06:19]
- **A program that is currently being executed is a process.**
- Running something means the **OS loads it from disk into RAM**. The CPU only talks to RAM (through registers and caches), never directly to the disk.
- Rough hardware path: CPU (registers) ↔ cache ↔ RAM ↔ disk/SSD.
- RAM is limited [10:29]. With 8 GB, three processes using 2 GB each consume 6 GB. Each process uses only its own share.
- A Java process's share holds the JVM memory areas: heap, stack, method area and program counter (see the memory-management video).
- What a process needs [13:21]: **RAM**, **CPU time**, and **resources** (files, network), which the OS provides.
- **Processes don't share memory** [15:04]. P1 (e.g. Chrome) can never look into P2's (e.g. VS Code's) memory.

### What is a thread? [15:44]
- Textbook definition: *the smallest sequence of instructions that can be executed by the CPU independently*, also called a *lightweight process*.
- A process is a big chunk, and it can be divided into multiple threads. Java always creates the **main thread**, and you can create T1, T2, ... in your code (how: next video). They all belong to the same process.
- The CPU works with **threads, not processes**. "Running process P1" really means running P1's threads.
- On a **single-core** CPU [18:13] only one thread runs at a time: main for a while, then T1, then T2, then main again, and so on. A core is roughly "one thing at a time". Modern CPUs have 8, 10 or more.

### Inside a Java process: the JVM [22:18]
- The OS creates the process. The **JVM** then starts inside it and manages everything within it (threads, memory). The OS's job ends at creating the process.
- JVM duties: load classes, run bytecode, manage memory (stack, heap, method area), run garbage collection.
- The JVM splits the process's memory (e.g. 2 GB) into heap (the largest part), method area, stack and PC.
- **OS view vs JVM view** [24:33]. Language-independent OS books describe process memory as *code segment, data segment, heap, stack, registers*. The JVM's view maps onto that roughly as: code/data segments → **method area**, heap → **heap**, stack → **stack**, registers → **program counter**. There is also a native method stack for native (C/C++) code.
- Why the JVM has its own layout [25:56]: Java is **platform independent**, so it doesn't follow each OS's layout. It defines its own areas and takes the memory for them from whatever RAM the platform has. A C++ program has no JVM layer, so the OS manages its memory directly.

### What happens when you run `java Demo` [28:32]
1. The JVM loads into the process.
2. The JVM's **class loader** finds and loads the main class (the one with `main`).
3. The JVM creates the **main thread**.
4. The JVM creates a **separate stack and PC** for the main thread. A stack and PC can't exist without a thread.
5. Execution starts at `main()`: a **stack frame** for `main` is pushed onto the main thread's stack and holds its local variables.
- PC holds the current instruction. Stack holds the method being executed. Heap holds objects. Method area holds class metadata.

### Multiple threads in one process [31:12]
```
Process
└── JVM
    ├── Heap          (shared by all threads)
    ├── Method area   (shared by all threads)
    ├── main thread → own Stack + own PC
    └── Thread-1    → own Stack + own PC
```

### Single core: context switching and concurrency [32:57]
- The CPU fetches each instruction from the current thread's **PC**, executes it, and keeps intermediate values in its **registers**. For `a = b + c * d`, the CPU computes `c * d` into a register first, then adds `b`.
- With main and T1 both ready, a single core can't run both. The OS **context switches** [36:35]: run main for a time slice (imagine 0.5 s), pause it, run T1, pause it, run main again, and so on.
- When a thread is paused, its progress has to be saved so it can resume where it stopped. For example, a loop paused at iteration 50 resumes at 51, not 0. The video says the progress goes into the thread's **PC** (the next instruction) and its **stack**, with intermediate values in the frame's **operand stack** [38:46].
- *(Correction/precision: at the hardware level the OS saves the CPU's registers into the thread's kernel data structure when it switches. The JVM operand stack is part of the method frame and simply stays where it is. The idea is the same: everything needed to resume is saved per thread.)*
- **Concurrent execution** [40:00]: several threads are ready, only one makes progress at any instant, and over time all of them progress (e.g. main 50 % done, T1 40 % done). Switching is fast enough that it *looks* simultaneous. That is how an old single-core PC still seemed to autocomplete and check errors at the same time.

### Multiple cores: true parallelism [41:56]
- With 2 cores, core 1 can run main and core 2 can run T1 at the same moment, with no context switching needed.
- If main and T1 each need 3 s of CPU, parallel execution finishes both in about **3 s**. Concurrent execution on one core takes about **6 s** in total. This is **true parallelism** [43:43], and it is where the "multithreading makes code faster" claim comes from.
- Concurrency alone is often enough, for example to keep a UI responsive.

### Real machines: both at once [45:41]
- Many processes (Chrome, VS Code, your Java app), each with several threads, give far more threads than cores. Example: 5 threads on 2 cores.
- Each core runs threads in **parallel** with the other core, and each core also **context switches** among the waiting (runnable) threads. A thread may run on core 1 now and core 2 later.
- The CPU doesn't care which process a thread belongs to. It might run T1 from process 1 and then T3 from process 2. Which thread goes where is decided by the **OS scheduler**, not by you. You only control which threads are ready to run.

### Multitasking vs multithreading [48:17]
| | Multitasking | Multithreading |
|---|---|---|
| Level | Operating system concept | Programming concept |
| Units | Multiple **processes** (Chrome, VS Code, Spotify) | Multiple **threads** inside one process |
| Memory | Isolated per process | Heap and method area shared, stack and PC private |
| How it runs | Threads of all processes scheduled on all cores | Threads of one process, concurrently and/or in parallel |
- Both come down to threads being scheduled onto cores, with context switching in between.

## Code Examples

The video has no code yet (thread creation is in #48). These small programs show the ideas above. Run each with `java ClassName.java`.

### Every program already has a `main` thread
```java
public class MainThreadInfo {
    public static void main(String[] args) {
        Thread current = Thread.currentThread();
        System.out.println("Running on thread: " + current.getName()); // Running on thread: main
        System.out.println("At least one core: " + (Runtime.getRuntime().availableProcessors() > 0)); // At least one core: true
        // availableProcessors() returns the real count (e.g. 8); the boolean keeps the output fixed
    }
}
```

### Shared heap vs private stack
```java
public class SharedVsPrivateMemory {
    static int[] shared = new int[2]; // the array object lives on the heap, visible to all threads

    public static void main(String[] args) throws InterruptedException {
        Thread t1 = new Thread(() -> {
            int local = 10;          // local variable: on t1's own stack
            shared[0] = local;       // write to the shared heap object
        });
        Thread t2 = new Thread(() -> {
            int local = 20;          // a different 'local', on t2's own stack
            shared[1] = local;
        });
        t1.start(); t2.start();
        t1.join(); t2.join();        // wait for both to finish
        System.out.println(shared[0] + " " + shared[1]); // 10 20
    }
}
```

### Concurrency you can see: interleaved output
```java
public class InterleavingDemo {
    public static void main(String[] args) throws InterruptedException {
        Runnable task = () -> {
            for (int i = 1; i <= 3; i++) {
                System.out.println(Thread.currentThread().getName() + " step " + i);
            }
        };
        Thread a = new Thread(task, "A");
        Thread b = new Thread(task, "B");
        a.start(); b.start();
        a.join(); b.join();
        System.out.println("both done"); // always printed last
        // The order of the A/B lines varies between runs, e.g.:
        // A step 1
        // B step 1
        // A step 2
        // A step 3
        // B step 2
        // B step 3
        // both done
    }
}
```

## Common Pitfalls / Tips
- **"Multithreading = faster" is not always true.** On one core, CPU-bound work split into threads takes about the same total time or longer, because context switches cost something. You get speed-up from parallelism on multiple cores. Concurrency mainly improves responsiveness and overlaps waiting (I/O).
- **Don't confuse process and thread.** Processes have isolated memory. Threads of the same process share heap objects, which is convenient but means shared data needs protecting (see #50 and #51).
- **You don't control scheduling.** The OS decides which thread runs, on which core, and for how long. Never write code that depends on a particular interleaving.
- **Stack and PC are per thread, heap is not.** Local variables are thread-safe by nature. Fields of shared objects are not.
- **OS memory terms vs JVM memory terms.** In an OS course you'll see code/data segment and registers. In Java you talk about method area and PC register. They describe the same ideas at different levels.

## Review Questions
1. What is the difference between a program and a process?
2. What three things does a process need from the system to run?
3. Define a thread. Which thread does every Java program have, and who creates it?
4. Which JVM memory areas are shared between threads, and which are private to each thread?
5. List the steps the JVM goes through when you run `java Demo`.
6. What is context switching, and what must be saved when a thread is paused?
7. Two threads each need 3 seconds of CPU. How long does it take on one core vs two cores, and why?
8. What is the difference between multitasking and multithreading?

<details>
<summary>Answers</summary>

1. A program is a set of instructions stored on disk (e.g. `Demo.java`, `Demo.class`). A process is that program while it is executing, after the OS has loaded it into RAM.
2. Memory (a part of RAM), CPU time, and resources such as files or network access. The OS provides all three.
3. The smallest sequence of instructions the CPU can execute and schedule independently, sometimes called a lightweight process. Every Java program has the `main` thread, created by the JVM before `main()` starts.
4. Shared: heap and method area. Private: each thread's stack and program counter.
5. The OS creates the process, the JVM loads into it, the class loader loads the main class, the JVM creates the main thread and gives it its own stack and PC, and a stack frame for `main()` is pushed and execution begins.
6. The OS pauses one thread and resumes another on the same core. The paused thread's position (its PC, CPU register values, stack state) has to be saved so it can later continue exactly where it stopped.
7. On one core the threads take turns (concurrency), so about 6 s in total. On two cores they run at the same time (parallelism), so about 3 s.
8. Multitasking is an OS concept: several processes (apps) running at once. Multithreading is a programming concept: several threads inside one process. Underneath, the CPU only runs threads in both cases.
</details>

## Further Study
- Creating threads in Java: `Thread`, `Runnable`, and the thread lifecycle (#48)
- Thread methods: `sleep`, `join`, `yield`, `interrupt`, daemon threads (#49)
- JVM memory areas in depth: heap, stack, metaspace, PC register, native method stack
- OS scheduling algorithms: round robin, time slicing, priority scheduling
- CPU caches and why they cause visibility problems (#50)
- Amdahl's law: how much speed-up parallelism can actually give
- Virtual threads (Java 21) vs platform threads

---

[← #46](46-heap-memory-garbage-collection.md) · [Index](ReadMe.md) · [#48 →](48-thread-creation-lifecycle.md)
