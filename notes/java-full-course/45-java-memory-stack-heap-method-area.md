# Java Memory Management Explained in Depth | Stack, Heap, Method Area & PC | Java Full Course #45
> Source: https://youtu.be/kjETbH63Pco · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 64:10. The video is in Hindi (Hinglish); these notes are in English, written from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- The JVM does three things with memory: **allocate** it, let you **use/update** it, and **deallocate** it. In C++ you free heap memory yourself (`delete`/`free`). In Java the **garbage collector (GC)** frees objects nobody references any more.
- You still need to understand memory. Lingering references cause **memory leaks** (yes, in Java too), creating too many objects makes the GC run more often and **slows** the program down, and you can still hit `OutOfMemoryError` (heap) or `StackOverflowError` (stack).
- Runtime memory is split by job into **Heap** (objects, arrays, instance fields, the string pool) and the **Method Area** (class metadata, method code, static variables), both **shared by all threads**, plus a **Stack** (one frame per method call: locals, parameters, return address) and a **Program Counter** (the current instruction), both **per thread**. A native method stack also exists, for C/C++ code.
- Stack frames are pushed and popped in LIFO order and clean themselves up, which is fast and needs no GC. Heap objects have unpredictable lifetimes, so the GC manages them. That makes the heap slower, but it's flexible and can be sized with `-Xms`/`-Xmx`.
- An object becomes **eligible for GC** when nothing reaches it: you set the reference to `null`, the method holding the only reference returns, and so on. Eligible doesn't mean collected right away. References between heap objects form a **graph**.

## Key Concepts

**Allocate / use / deallocate.** `int x = 4` and `new Student()` allocate. `x = x + 1` reads, updates and writes back. Freeing an object you no longer need is deallocation. *Why it matters:* this is the whole lifecycle that memory management covers.

**Manual vs automatic memory management.** In C++ you allocate with `new` and must free with `delete`/`free`. Forgetting to (a leak) or freeing too early (a dangling pointer) are classic bugs. Java gives deallocation to the JVM's **garbage collector**. *Why it matters:* you focus on logic, but the GC can only free objects you stop referencing.

**Memory leak in Java.** An object you no longer *need* but still *reference*, e.g. an ever-growing `List` that lives as long as `main`. The GC can't touch it. *Why it matters:* the heap fills up until `OutOfMemoryError`.

**Heap.** One large shared area where every object made with `new` lives, along with arrays, instance variables and the **string constant pool**. This is the only area the GC works on. *Why it matters:* it's where most memory problems show up.

**Method Area.** Per-class "blueprint" data: the class name, parent class, interfaces, field and method information, method bytecode, and **static variables**. Shared by all threads. *Why it matters:* methods (static *and* instance) are stored once per class, never inside each object.

**Stack (execution memory).** One stack per thread, made of **stack frames**, one per active method call. A frame holds local variables, parameters, references, and the **return address**. LIFO push and pop. *Why it matters:* it's very fast and cleans itself up, but its size is limited, so deep or infinite recursion causes `StackOverflowError`.

**Program Counter (PC register).** One per thread. It holds the address of the instruction currently being executed, so a thread can resume where it left off. *Why it matters:* it's part of what makes each thread independent.

**Thread-shared vs thread-specific.** Heap and Method Area are shared: objects accessed by several threads may need locking. Stack and PC belong to one thread: local variables are never shared, so they don't need locks. *Why it matters:* this is the basis of thread safety (multithreading comes later in the course).

**GC eligibility / reachability.** An object can be collected once no chain of references from a *root* (stack variables, static fields, ...) reaches it. *Why it matters:* setting references to `null` or letting them go out of scope is how you "free" memory in Java.

## Detailed Notes

### Why a program needs memory [00:00]
- A program declares variables, creates objects (the object goes on the heap, its reference variable on the stack), and defines classes whose metadata and methods also have to live somewhere.
- The JVM uses memory in three ways [01:00]:
  1. **Allocate**: `int x = 4;` creates `x` and stores 4. `Student s1 = new Student();` puts the object on the heap and the reference `s1` on the stack.
  2. **Use/update**: `x = x + 1;` fetches 4, adds 1, stores 5.
  3. **Deallocate/free** [02:50]: release an object you don't need so the space can be reused.

### Manual memory management and garbage collection [03:45]
- In C++ you free heap objects yourself (`delete ptr` / `free(ptr)`). Leaving that to developers is error-prone. They forget, they free too early, or large objects (say 1 MB each) pile up until the heap is full.
- Java's answer [05:13]: the **garbage collector**, part of the JVM, decides which heap objects can be freed. You never free objects yourself.

### Why learn memory management anyway? [05:50]
- Memory is finite, and writing memory-friendly code is still your job.
- **The GC can only free unreferenced objects.** If any variable still points to a big (say 4 MB) object, the GC must keep it, because you could still call `s.name`.
- **Memory leaks happen in Java** [07:30]. Example: a `List<Student>` declared in `main` that you keep adding to and never clear. It might grow to 100–200 MB, and since `main` lives as long as the program, it's never released. Eventually you get `OutOfMemoryError`.
- **Performance** [08:42]: the GC is itself a program that needs CPU. The more garbage you create, the more often it runs, and the more your program slows down. (The "every 2 seconds" figure in the video is purely hypothetical. The real frequency depends on which part of the heap is being collected and on allocation pressure.)
- **Errors** [10:27]: `OutOfMemoryError` when the heap is full, `StackOverflowError` when a thread's stack is full.

### Why split memory into areas? [10:50]
- The JVM may be juggling thousands or millions of objects and many threads. Some data is **long-lived**, some very **temporary**.
- If it were all in one area, the GC would have to scan everything often just to clear short-lived data. Long-lived data doesn't need frequent checks. Splitting memory by responsibility lets each area be managed in the way that suits it.

### The four runtime areas [12:11]
| Area | Stores | Shared? | Managed by |
|---|---|---|---|
| **Heap** | objects, arrays, instance fields, string pool | all threads | GC |
| **Method Area** | class metadata, method code, static variables | all threads | class loading/unloading |
| **Stack** | one frame per call: locals, params, refs, return address | per thread | automatic push/pop |
| **PC register** | address of the current instruction | per thread | JVM |
| *(Native method stack)* | frames for native (C/C++) methods | per thread | JVM |

- *Correction:* around [13:56] the video says "local variables or instance variables are stored on the stack". Only **local** variables (and parameters) live on the stack. **Instance** variables live inside the object on the heap, as the video itself explains a few minutes later.
- Shared areas need thread-safety (locks) for critical objects. Per-thread areas never have race conditions.

### Heap overview [14:20]
- One big region. Every `new` puts an object here. The **GC works only on the heap**, never on the stack, PC or Method Area.

### Method Area [15:00]
- Stores each class's **blueprint/metadata**. For `class Student`: its name, its parent (`Object` by default), its interfaces, its methods `m1`/`m2` with their parameters, local variable information, return types and bytecode.
- Why it's needed: when `main` does `new Student()`, the JVM must know what a `Student` looks like. The class is **loaded into the Method Area first**.
- **Static variables** belong to the class, not to any object, so they're stored with the class data.
- **All methods** (instance and static) are stored here once per class. Objects only contain their instance variables (`age`, `rollNo`), never methods.
- *HotSpot details, beyond the video:* since Java 8 the Method Area is implemented as **Metaspace** in native memory. Static field *values* are actually stored with the class's `java.lang.Class` object on the heap. JIT-compiled machine code lives in a separate **code cache**. The video's model, which lumps these together, is the JVM-spec view and works fine for reasoning.

### Stack overview [19:09]
- LIFO, like the stack data structure. Also called **execution memory**.
- When `main` starts, a `main` frame is pushed, holding its locals (`a = 5`, `b = 7`). When `main` calls `m1`, an `m1` frame is pushed. When `m1` calls `m2`, an `m2` frame is pushed. When `m2` finishes it's popped and control returns to `m1`.

### Program Counter [21:00]
- Holds the current instruction. The JVM interprets bytecode (and JIT-compiles hot code), instruction by instruction. The PC records where it is so that a paused thread can resume at the right point.
- *Note:* the video uses line numbers as a simplification. The real PC holds a **bytecode** address, and it's undefined while a native method is running.

### Walk-through: `int x = 5; Student s1 = new Student(); s1.name = "Aditya";` [23:20]
1. `javac Demo.java` produces `Demo.class` (bytecode). `java Demo` runs it.
2. **The JVM starts** [24:50].
3. **Class loading**: a class loader finds `Demo.class`, reads its bytecode, and creates a **Method Area entry** with the class name `Demo`, the parent `Object`, and the method `main(String[])` with its bytecode.
4. **The main thread is created** [27:52], with its own **stack** and **PC**. (The heap already exists.)
5. **`main` runs** [29:36]: a `main` frame is pushed with slots for `x` and `s1`, not yet assigned.
6. `int x = 5;` stores 5 in the `x` slot. The PC moves on (the video uses 1001, then 1002 as stand-in addresses).
7. `new Student()` [31:21]: load the `Student` class into the Method Area if it isn't there already, create the object on the heap, run the constructor, and store its address in `s1`, so `s1` now points into the heap.
8. `s1.name = "Aditya";` [33:05]: follow `s1` to the object and set its `name` field. Because `"Aditya"` is a **string literal**, the string itself sits in the **string pool** (part of the heap), and `name` holds a reference to it.
9. `main` ends [34:50]: the `main` frame is popped, so `x` and `s1` disappear. The `Student` object is still on the heap, but nothing points to it. The next GC that scans it finds it **unreachable** and can free it.

### Stack in depth: frames and the return address [35:00]
- **One method call = one frame**: `m1` → `m2` → `m3` means three frames on top of each other.
- A frame holds **local variables**, **method parameters** (which behave like locals), **reference variables**, and the **return address**.
- Why store a return address when LIFO already tells you which method to go back to? [37:00] LIFO tells you *which method*, but not *which instruction in it*. If `main` calls `m1()` on line 1, prints on line 2, and calls `m1()` again on line 3, then after the first call returns, execution has to resume at **line 2**. The return address records that, and the PC is set back from it.

### Example: `add(a, 20)` on the stack [40:30]
```
int a = 10;
int result = add(a, 20);   // add(x, y) { int sum = x + y; return sum; }
```
- Push `main`'s frame: `a = 10`, `result = ?`.
- Call `add`: push a new frame with `x = 10` and `y = 20` (**copies**, because Java is always pass-by-value, so `x` is independent of `a`), `sum = ?`, and the return address.
- `sum = 30`, then `return sum`. The `add` frame is **popped** and 30 goes into `result`.
- `main` ends, its frame is popped, and the stack is empty.
- Observations [45:17]:
  - The stack **grows and shrinks automatically** with calls and returns.
  - It's **auto-cleaned**. Locals vanish with their frame, so no GC is needed.
  - Execution order is LIFO.
- Why not keep locals on the heap? Method calls are extremely frequent, and stack push and pop are **O(1)** and very fast. Locals are short-lived and useless after their method ends, so putting them on the heap would only add GC work.

### `StackOverflowError` [47:01]
- An `Error` (a `Throwable`, but not an `Exception`). The stack isn't infinitely flexible: each thread's stack has a **fixed maximum size**.
- Classic cause: **recursion without a base case**. `f1()` calls `f1()` forever, frames pile up until the stack is full, and the JVM throws `StackOverflowError`.
- *Tip not in the video:* the per-thread stack size can be tuned with `-Xss` (e.g. `-Xss2m`).

### Heap in depth [48:46]
- Objects have **unpredictable lifetimes**. Some live milliseconds, some minutes, some until the program ends. They can't simply be popped like locals, so they get their own area, overseen by the **garbage collector**.
- What goes on the heap [49:50]:
  - **every object** created with `new` (e.g. `new Student()`),
  - **instance variables** (`name`, `age`), stored inside their object,
  - **arrays** (the array is on the heap, while the `arr` reference is on the stack),
  - **strings**: `new String("...")` goes on the normal heap, while **literals** go in the **string pool**, a special region of the heap.
- Properties [52:15]:
  - **Shared across threads**, so critical shared objects need locking.
  - **Dynamic**: it can grow far more than the stack, within limits you set:
    - `-Xms2m` sets the **initial (minimum)** heap size.
    - `-Xmx4m` sets the **maximum** heap size. Exceed it and you get `OutOfMemoryError: Java heap space`.
  - **Managed by the GC** [57:50].
  - **Slower than the stack**, because allocation and deallocation are more complex and there's GC overhead.

### `OutOfMemoryError` demo [53:40]
- `List<int[]> list = new ArrayList<>();` then `while (true) { list.add(new int[250_000]); count++; print(count); }` run with `java -Xms2m -Xmx4m Demo`.
- Each array is 250,000 × 4 bytes = 1,000,000 bytes, roughly 1 MB. *(The transcript says "250", but 250 ints is only 1,000 bytes. A 1 MB block needs 250,000.)*
- It printed blocks 1–5 and then threw `OutOfMemoryError`. Why 5 and not 4? 1,000,000 bytes is a little less than 1 MiB (1,048,576 bytes), so slightly more blocks fit than the naive count suggests. *(Also, the exact count depends on JVM version, GC, and per-object overhead. When we re-ran it on JDK 21 with `javac` + `java -Xms2m -Xmx4m` it also reached 5, but don't rely on the number. Using the single-file launcher `java Demo.java` needs a bigger heap, since the in-memory compiler itself uses heap space.)*

### Object creation step by step [58:00]
- `Student s1 = new Student("Aditya", 28);`
  1. Memory for the object is allocated on the heap. Fields get **default values** (`name = null`, `age = 0`).
  2. The **constructor** runs and assigns `name = "Aditya"` and `age = 28`.
  3. The address is stored in the reference `s1` on the stack.
- `Student s2 = s1;` [60:40] creates **no new object**. It's a second reference to the same one. Only `new` creates objects.

### When does an object become unreachable? [61:20]
1. **Reassign the reference**: `s1 = null;`. Nothing points to the object now, so it's **eligible for GC**.
2. **The method ends**: its frame is popped, so the references in it are gone.
- *Eligible* ≠ *deleted immediately*. The GC frees it whenever it next runs and decides to reclaim that region.

### The heap is a graph [62:42]
- Objects reference other objects (an `A` holds a `B`, which holds a `C`). A heap object may be reachable only through another heap object, not directly from the stack. So the heap looks like a **graph**, and the GC follows references from the roots to find out what's still alive.
- Next video: how the heap is divided (young and old generations) and the GC algorithms.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Stack frames and pass-by-value
```java
public class StackFrameDemo {
    public static void main(String[] args) {
        int a = 10;                 // lives in main's frame
        int result = add(a, 20);    // new frame for add: x = copy of a, y = 20
        System.out.println("result = " + result);

        bump(a);                    // bump gets its own copy
        System.out.println("a after bump = " + a);
    }

    static int add(int x, int y) {
        int sum = x + y;            // local in add's frame
        return sum;                 // frame popped, 30 copied back to the caller
    }

    static void bump(int x) {
        x = x + 1;                  // changes only the copy in bump's frame
        System.out.println("inside bump x = " + x);
    }
}
// Output:
// result = 30
// inside bump x = 11
// a after bump = 10
```

### Return address: execution resumes at the next instruction
```java
public class ReturnAddressDemo {
    public static void main(String[] args) {
        m1();                       // after m1 returns, continue with the next line
        System.out.println("hello from main");
        m1();
        System.out.println("main done");
    }

    static void m1() {
        System.out.println("  m1 starts");
        m2();
        System.out.println("  m1 resumes after m2");
    }

    static void m2() {
        System.out.println("    m2 runs (stack depth: main > m1 > m2)");
    }
}
// Output:
//   m1 starts
//     m2 runs (stack depth: main > m1 > m2)
//   m1 resumes after m2
// hello from main
//   m1 starts
//     m2 runs (stack depth: main > m1 > m2)
//   m1 resumes after m2
// main done
```

### Recursion without a base case: `StackOverflowError`
```java
public class StackOverflowDemo {
    static int frames = 0;

    static void f1() {
        frames++;
        f1();                       // no base case
    }

    public static void main(String[] args) {
        try {
            f1();
        } catch (StackOverflowError e) { // caught only for the demo
            // the exact depth depends on stack size (-Xss) and the JVM, so don't print it
            System.out.println("StackOverflowError after many frames: " + (frames > 1000));
        }
    }
}
// Output:
// StackOverflowError after many frames: true
```

### Heap objects, shared references, and GC eligibility
```java
public class HeapReferenceDemo {
    public static void main(String[] args) {
        HeapStudent s1 = new HeapStudent("Aditya", 28); // object on heap, s1 on stack
        HeapStudent s2 = s1;                             // no new object: two refs, one object
        s2.age = 29;
        System.out.println(s1.name + " " + s1.age);      // change visible through s1
        System.out.println("same object? " + (s1 == s2));

        System.out.println("defaults before assignment: " + new HeapStudent().name + ", " + new HeapStudent().age);

        s1 = null;   // object still reachable through s2, so NOT eligible
        System.out.println("still reachable via s2: " + s2.name);
        s2 = null;   // now unreachable: eligible for GC (collected whenever the GC decides)
        System.out.println("s1 and s2 are null; the object is eligible for GC");
    }
}

class HeapStudent {
    String name;   // instance variables live inside the object on the heap
    int age;

    HeapStudent() { }  // fields keep default values: null and 0
    HeapStudent(String name, int age) { this.name = name; this.age = age; }
}
// Output:
// Aditya 29
// same object? true
// defaults before assignment: null, 0
// still reachable via s2: Aditya
// s1 and s2 are null; the object is eligible for GC
```

### Static (class-level) vs instance (object-level) data
```java
public class StaticVsInstanceDemo {
    public static void main(String[] args) {
        Counter c1 = new Counter();
        Counter c2 = new Counter();
        Counter c3 = new Counter();
        c1.hits++;
        c1.hits++;
        c3.hits++;
        System.out.println("objects created (static, one copy per class): " + Counter.created);
        System.out.println("hits per object (instance, one copy per object): "
                + c1.hits + " " + c2.hits + " " + c3.hits);
    }
}

class Counter {
    static int created = 0;  // stored with the class, shared by all objects
    int hits = 0;            // stored in each object on the heap

    Counter() { created++; }
}
// Output:
// objects created (static, one copy per class): 3
// hits per object (instance, one copy per object): 2 0 1
```

### Filling the heap: `OutOfMemoryError` (run with a small `-Xmx`)
```java
import java.util.ArrayList;
import java.util.List;

public class HeapFillDemo {
    public static void main(String[] args) {
        long max = Runtime.getRuntime().maxMemory();
        if (max > 64L * 1024 * 1024) {   // safety: don't fill a multi-GB default heap
            System.out.println("Heap is large; run: java -Xmx16m HeapFillDemo.java");
            return;
        }
        List<int[]> list = new ArrayList<>();
        int count = 0;
        try {
            while (true) {
                list.add(new int[250_000]);  // 250,000 * 4 bytes = ~1 MB per block
                count++;
            }
        } catch (OutOfMemoryError e) {
            list.clear();                    // drop references so we have room to print
            System.out.println("OutOfMemoryError: " + e.getMessage() + " after " + count + " blocks");
        }
    }
}
// With `java -Xmx16m HeapFillDemo.java` it prints something like
// "OutOfMemoryError: Java heap space after 12 blocks" (the count varies by JVM/GC).
// Output (plain `java HeapFillDemo.java`, default heap):
// Heap is large; run: java -Xmx16m HeapFillDemo.java
```

## Common Pitfalls / Tips
- **"Java has a GC, so no leaks" is false.** Anything still referenced (static collections, caches, listeners, a `List` in `main` that keeps growing) can never be collected. Clear collections or drop references when you're done.
- **Instance variables are not on the stack.** Locals and parameters are on the stack. Fields live in the object on the heap. Static fields are class-level.
- **Methods aren't copied into objects.** Code is stored once per class in the Method Area, whether the method is static or not.
- **`s2 = s1` doesn't copy an object.** Both names point to the same heap object, so a change through one is visible through the other.
- **Setting a reference to `null` makes an object *eligible*, not collected.** Don't call `System.gc()` hoping to force it. It's only a hint.
- **Too many short-lived objects cost CPU** through extra GC runs. Reuse where it's sensible (e.g. a `StringBuilder` in loops instead of string concatenation).
- **Deep recursion → `StackOverflowError`.** Always have a base case. For very deep algorithms consider iteration, or raise `-Xss` as a last resort.
- **`-Xms` = initial heap, `-Xmx` = maximum heap.** Too small an `-Xmx` causes `OutOfMemoryError`. Very tiny values (a few MB) may not even be enough for the single-file source launcher.
- **Thread safety follows memory sharing.** Locals are thread-confined. Heap objects shared between threads need synchronization.

## Review Questions
1. What three things does the JVM do with memory, and which one does Java automate compared with C++?
2. If Java has a garbage collector, how can a Java program still leak memory? Give an example.
3. Name the four main runtime memory areas, what each stores, and which are shared between threads.
4. Where are these stored: a local `int`, an instance field, a static field, a method's bytecode, a string literal, an array?
5. What does a stack frame contain, and why is a return address needed if the stack is LIFO anyway?
6. Trace the stack for `int a = 10; int result = add(a, 20);`. Why doesn't changing `x` inside `add` affect `a`?
7. Why are local variables kept on the stack rather than the heap?
8. What do `-Xms` and `-Xmx` control? What happens when the heap limit is exceeded?
9. Give two ways an object becomes eligible for garbage collection. Is it freed immediately?

<details>
<summary>Answers</summary>

1. Allocate, use/update, and deallocate. Java automates deallocation of heap objects through the garbage collector. In C++ you call `delete`/`free` yourself.
2. The GC only frees *unreachable* objects. If you keep a reference to data you no longer need, it can't be collected. Example: a `List` in `main` (or a static field) that you keep adding to and never clear. It grows until `OutOfMemoryError`.
3. Heap (objects, arrays, instance fields, string pool), Method Area (class metadata, method code, static variables), Stack (frames: locals, parameters, return address) and Program Counter (the current instruction). Heap and Method Area are shared. Stack and PC are per thread.
4. Local `int`: the stack frame. Instance field: inside the object on the heap. Static field: class-level storage (the Method Area in the spec model). Method bytecode: the Method Area. String literal: the string pool on the heap. Array: the heap (its reference may be on the stack).
5. Local variables, parameters, reference variables, and the return address. LIFO tells you which method to return to, but not where inside it to resume. The return address gives the exact next instruction (e.g. the line after the call), and the PC is restored from it.
6. Push `main` (`a = 10`, `result = ?`). Call `add`: push a frame with copies `x = 10` and `y = 20`, `sum = ?`, and the return address. `sum = 30`, return, pop `add`, `result = 30`. Pop `main`. `x` is a copy of `a`'s value (Java is pass-by-value), so changing it doesn't affect `a`.
7. Method calls are very frequent, and stack push and pop are O(1) and fast. Locals are short-lived, and they're freed automatically when the frame is popped, so the GC never needs to deal with them.
8. `-Xms` sets the initial heap size and `-Xmx` sets the maximum. Exceeding the maximum throws `OutOfMemoryError: Java heap space`.
9. Setting every reference to it to `null` (or pointing them elsewhere), or the method holding the only reference returning (its frame is popped). It's only *eligible*. The GC reclaims it whenever it next decides to collect that region.
</details>

## Further Study
- Heap generations (young: Eden + survivor spaces; old/tenured) and minor vs major GC (next video, #46)
- GC algorithms: mark-and-sweep, mark-compact, copying; Serial, Parallel, G1, ZGC
- Metaspace (Java 8+) vs the old PermGen; the code cache
- Class loaders (bootstrap, platform, application) and the class loading phases
- The string constant pool and `String.intern()`
- `-Xss` thread stack size; escape analysis and stack allocation by the JIT
- Weak, soft and phantom references
- Tools: VisualVM, `jcmd`, `jstat`, heap dumps for finding leaks

---

[← #44](44-exception-hierarchy-throw-throws-custom.md) · [Index](README.md) · [#46 →](46-heap-memory-garbage-collection.md)

---

[← #44](44-exception-hierarchy-throw-throws-custom.md) · [Index](README.md) · [#46 →](46-heap-memory-garbage-collection.md)
