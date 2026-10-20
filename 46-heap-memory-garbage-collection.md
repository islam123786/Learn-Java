# Java Heap Memory Deep Dive | Garbage Collection Algorithms Explained | Java Full Course #46
> Source: https://youtu.be/b2HSqC0RGsM · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 62:52. The video is in Hindi (Hinglish); these notes are in English, written from the Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- The heap is **generational**. The **Young Generation** (Eden plus two survivor spaces, S0 and S1) holds new objects. The **Old Generation** holds long-lived ones. The string pool is also on the heap.
- Every new object starts in **Eden**. Each **minor GC** (young generation) copies surviving objects into a survivor space and increments their **age**. They bounce S0 ↔ S1 until they reach the **tenuring threshold** (15 by default), then they're **promoted** to the old generation. Survivor overflow, dynamic age adjustment and very large objects can lead to earlier promotion.
- Most objects die young, so minor GCs are frequent and cheap. **Major GCs** (old generation) are rarer and more expensive, because old objects tend to have many references to scan.
- Three classic algorithms: **mark-and-sweep** (simple, but leaves **fragmentation**), **mark-compact** (no fragmentation, but moving objects costs time), and **copying** (used for the young generation: copy the live objects out and wipe the source space).
- GC involves **stop-the-world (STW)** pauses. Parallel GC uses several threads to make pauses shorter. `System.gc()` is only a request. Java also has **strong / soft / weak / phantom** references, and **heap dumps** (viewed in VisualVM) help you find leaks.

## Key Concepts

**Young Generation (Eden, S0, S1).** Where every new object is allocated, in Eden. Allocation there is very fast, and most objects become unreachable while they're still there. *Why it matters:* it's cheap to collect often, because most of it is garbage.

**Survivor spaces and age.** After each minor GC, live objects are copied into the empty survivor space (S0 or S1, which swap roles) and their age goes up by 1. *Why it matters:* the age counter is how the JVM decides that an object is "long-lived".

**Tenuring threshold / promotion.** When an object's age reaches the threshold (default `MaxTenuringThreshold = 15`), it moves to the Old Generation. *Why it matters:* objects that survive many collections stop being copied around.

**Old Generation and major GC.** Holds long-lived objects. It's collected less often, but each collection is costlier: more objects, a deeper reference graph. *Why it matters:* frequent major GCs usually mean long pauses or a memory leak.

**Mark-and-sweep.** *Mark* every object reachable from the roots, then *sweep* (free) everything unmarked. *Why it matters:* it's the basic idea behind every tracing collector. Its weakness is fragmented free space.

**Mark-compact.** Mark, then slide the live objects together so the free space is one contiguous block. *Why it matters:* there's no fragmentation, but moving objects and updating references takes time.

**Copying collector.** Copy the live objects from one space into an empty one, then treat the whole source space as free. *Why it matters:* its cost is proportional to the *live* objects only, which is perfect for the young generation.

**Stop-the-world (STW).** A pause during which application threads are halted so the GC sees a consistent object graph. *Why it matters:* GC pauses are latency spikes. Creating less garbage means fewer pauses.

**Reference strengths.** **Strong** (normal) references are never cleared while reachable. **Soft** references are cleared only when memory is needed (useful for caches). **Weak** references are cleared at the next GC once no strong reference remains (`WeakHashMap`). **Phantom** references: `get()` always returns `null`; they're used for cleanup after collection. *Why it matters:* they let you hold an object without preventing its collection.

**Heap dump.** A snapshot of every object on the heap and how they reference each other, analysed with tools such as VisualVM. *Why it matters:* it's the standard way to find what is leaking.

## Detailed Notes

### How the heap is divided [00:00]
- Objects such as a `Student`, arrays, and strings all live on the heap. String **literals** go in the **string pool**, a special part of the heap.
- The main heap has two parts [01:00]: the **Young Generation** and the **Old Generation**.
- The Young Generation is split into three [01:41]: **Eden** plus two **survivor spaces**, **S0** and **S1**.
- The string pool is drawn separately here because this video is about the main heap, where `new` objects go.

### Why generations? Objects start in Eden [03:23]
- `Student s1 = new Student();` puts the new object in **Eden**. Every object starts its life there.
- Eden properties [04:00]:
  - allocation is **very fast**,
  - **most objects die there**. Java code creates lots of temporary objects for small jobs. Once nothing references them, the GC removes them.
- The JVM doesn't know how long a fresh object will live, so it begins in the young generation and has to "earn" its way into the old generation by surviving.

### Survivor spaces and promotion [05:04]
- "Getting older" means **surviving a GC**: a GC ran and the object was still referenced.
- First survival: Eden → **S0**. Next GC: S0 → **S1**. Next: S1 → S0, and so on. The two survivor spaces swap roles each time.
- After surviving a maximum number of GCs [06:46] (the video uses **15**, which is HotSpot's default `MaxTenuringThreshold`), the object is **promoted to the Old Generation**.
- The JVM's reasoning: an object that survived 15 collections is probably long-lived, likely referenced from several places, and won't go away soon.
- So GC runs **frequently in the young generation** and **less frequently in the old generation** [08:27]. Every GC slows the program, so the JVM avoids repeatedly scanning objects that are unlikely to die.

### Walk-through: OB1–OB4 [09:00]
- `OB1`–`OB4` are created, all in **Eden**.
- A GC in the young generation is called a **minor GC** [10:09].
- **After the 1st GC**: `OB1` and `OB3` were unreachable and are removed. `OB2` and `OB4` survive and move to **S0** with **age 1**.
- **After the 2nd GC** [11:51]: both survive and move to **S1**, age 2.
- **After the 3rd GC**: back to **S0**, age 3. The S0 ↔ S1 bouncing continues.
- **After the 15th GC** [13:00]: `OB4` died somewhere along the way (say around age 13). `OB2` reached age 15 and is promoted to the **Old Generation**.

### Old Generation and major GC [13:32]
- GC in the old generation is a **major GC**.
- Old objects may be referenced by many variables and by other heap objects. Remember that the heap is a **graph**. So deciding whether one is dead needs a **deep scan**, and that's **expensive** [14:30].
- That's why the JVM runs it less often.
- Once in the old generation, there's **no age counter** any more [15:50]. The object stays until some major GC finds it unreachable. New objects keep starting in Eden.

### The threshold isn't fixed [17:00]
- Many JVMs use 15, but the JVM adapts:
  1. **Survivor space overflow**: if the survivors don't fit in the survivor space, some are promoted early, even at age 5 or 6.
  2. **Dynamic age determination** [18:00]: if many objects of the same age are piling up in the survivor space, the JVM lowers the effective threshold and promotes them together.
     - *Correction:* the video explains this as "you created many together, so you must need them". The real rule is about **space**. HotSpot lowers the threshold to the smallest age N at which objects of age ≤ N fill more than `TargetSurvivorRatio` (50% by default) of a survivor space.
  3. **Very large objects** [18:50] (e.g. `new int[10_000_000]`) can be allocated **directly in the old generation** instead of being copied through Eden and the survivor spaces.
     - *Note:* the exact rule depends on the collector. G1 puts objects bigger than half a region into special "humongous" regions. Serial and Parallel use the size/`PretenureSizeThreshold` and whether the object fits in the young generation.

### Full example: every memory area together [19:30]
```
class Student {
    String name; int age;                  // instance fields
    static String college = "IIT G";       // static field
    Student(String name, int age) { this.name = name; this.age = age; }
    void markAttendance() { System.out.println(name + " is present"); }
    static void changeCollege(String newCollege) { college = newCollege; }
}
// main: Student s1 = new Student("Aditya", 28); s1.markAttendance(); Student.changeCollege("IIT Bombay");
```
1. `javac Main.java`, then `java Main`. **The JVM starts** [21:00].
2. **Load `Main`** into the Method Area: name, parent `Object`, method `main(String[])`, access modifiers, bytecode. The PC is still empty.
3. **Create the main thread** and push the `main` frame [22:50] with slots for `args` and `s1`. How does the JVM know about `s1` before running that line? From the **method metadata** in the Method Area.
4. **Execute `main`, line 1** [25:30]: the JVM hasn't seen `Student` yet, so the class loader **loads `Student`** into the Method Area. That includes the instance field descriptions (`name`, `age`, which are only signatures here; the values will live in objects), the static `college`, and the methods `markAttendance` and `changeCollege` with their bytecode.
5. **Static initialization** [27:05]: `college` is assigned **before any object exists**. `"IIT G"` is a literal, so the string lives in the **string pool** and `college` refers to it.
   - *HotSpot note:* in modern HotSpot the static field's *slot* actually sits in the class's `java.lang.Class` object on the heap. Treating it as class-level data in the Method Area is the conceptual model.
6. **`new Student(...)`** [29:00]: the object is allocated on the heap with defaults `name = null` and `age = 0`. The literal `"Aditya"` also lands in the string pool.
7. **The constructor runs** [30:28]: a **constructor frame** is pushed holding `this` (a reference to the new object), plus the parameters `name` (referring to `"Aditya"` in the pool) and `age = 28`. Every constructor and instance method gets an implicit `this`. Static methods don't.
   - `this.name = name; this.age = age;` [32:09]: follow `this` to the object and set its **instance** fields from the **local** parameters. The object's `name` now refers to the pooled `"Aditya"`. **Objects are never stored on the stack**, only references.
8. **The constructor frame is popped** [35:00], taking its `this` with it. `s1` now refers to the object.
9. **`s1.markAttendance()`** [35:50]: the method's code is in the Method Area. A frame is pushed with `this`, and the return address is saved for the PC. It prints `Aditya is present`, then the frame is popped and the PC resumes at the next line of `main`.
10. **`Student.changeCollege("IIT Bombay")`** [38:56]: a static method, so the frame has **no `this`**, only the parameter `newCollege`, which refers to `"IIT Bombay"` in the pool. `college = newCollege` re-points the static reference [40:37]. The frame is popped.
11. Nothing references `"IIT G"` any more, and the video says the GC will remove it [42:19].
    - *Correction:* string **literals** are interned and referenced from the class's constant pool, so in practice they live as long as the class stays loaded. Strings created at run time and then interned can be collected once unreachable.
12. `main` ends: its frame is popped and `s1` disappears. The `Student` object becomes unreachable and is collected eventually. When the program exits, the Method Area is released too.

### Types of references [44:00]
- Ordered from strongest to weakest, i.e. by how strictly they keep an object alive: **Strong → Soft → Weak → Phantom** ("almost gone"). About 99% of the time you use strong references.
- **Strong** [45:42]: `Student s1 = new Student();`. While any strong reference reaches the object, the GC can't collect it.
- **Soft** [46:30]: `SoftReference<Student> s2 = new SoftReference<>(new Student());`. The GC may clear it **when memory is needed**. `s2.get()` returns the object, or `null` once cleared.
  - Use case [47:30]: **caches**. Keep frequently used data until memory gets tight. A cleared entry is a **cache miss**, and you reload from the database.
- **Weak** [48:20]: `WeakReference<Student> s3 = new WeakReference<>(new Student());`. It doesn't survive the **next GC cycle**.
  - Use case [49:20]: **`WeakHashMap`** (entries disappear when their key is no longer strongly referenced). It can also be used for caches, with more misses.
- *Correction:* the video says this "doesn't depend on whether I point to it". Soft and weak references are cleared only when the object is **only** softly or weakly reachable. If you also hold a strong reference, the object stays.
- **Phantom** [49:40]: `get()` **always returns `null`**. You never use the object through it.
  - The video says the GC uses it internally to "work out when to remove". *More precisely:* a `PhantomReference` is registered with a `ReferenceQueue`. The JVM enqueues it after the object has been found unreachable, which lets *your* code run post-mortem cleanup. `java.lang.ref.Cleaner` is built on this.

### GC algorithms [51:30]
- **Mark-and-sweep** [51:50]: objects A–E.
  - **Mark**: trace from the roots and mark what's reachable (A, C, E).
  - **Sweep**: free the unmarked (B, D).
  - The video uses it for the **old generation / major GC**.
  - Downside [53:10]: **fragmentation**. The holes left where B and D were, like gaps in an array, may be too small to use.
- **Mark-compact** [53:50]: mark the same way, then **compact**, sliding A, C, E to one end so the free space is contiguous.
  - Upside: no fragmentation.
  - Downside: **moving objects is costly**, like shifting array elements. Also an old-generation algorithm.
- **Copying** [54:50]: used for the **young generation / minor GC**. Of the objects in Eden (A, B, C, D), copy the live ones (B, D) to S0 and wipe Eden in one go. Next time, copy from S0 to S1, and so on.
  - No separate sweep phase, and young objects have shallow reference graphs, so it's **fast**.

### Stop-the-world [56:00]
- The joke in the Java community: "When GC runs, it stops the world." **STW** means application threads pause while the GC runs. That's why frequent GC makes a program slow.
- Why it pauses [56:50]: the GC is tracing a big graph of objects and references. If the application kept running, it could create objects or change references in the middle of the trace. So the JVM freezes the application, lets the GC finish, then resumes.
- Frequent GCs usually come from **leaks**: references that never go away, e.g. objects held by **static** fields, which live as long as the class.
- **Parallel GC** [59:15]: older Java used a **serial** collector (one GC thread). Parallel GC uses **multiple threads**, so the pause is shorter. It's still a pause. Parallel GC doesn't mean the GC runs alongside your code.
- *Correction / update:* modern collectors such as **G1** (the default since Java 9), **ZGC** and **Shenandoah** do most of their work **concurrently** with the application and stop the world only briefly. Some pauses still exist, but "the program can never run while GC runs" is only true for the stop-the-world collectors (Serial, Parallel).

### `System.gc()` and heap dumps [60:20]
- `System.gc()` **asks** the JVM to run GC. There's **no guarantee**: the JVM decides (and it can be disabled with `-XX:+DisableExplicitGC`). Don't rely on it in production code.
- When the heap fills up because everything is still referenced, you get `OutOfMemoryError`. To investigate [61:20], take a **heap dump**, a snapshot of every object and its references, and open it in **VisualVM** (or Eclipse MAT) to see which objects take the most space and what holds on to them. Then fix the code.
- *Tip not in the video:* `jcmd <pid> GC.heap_dump file.hprof`, or start the JVM with `-XX:+HeapDumpOnOutOfMemoryError`.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`. GC timing isn't deterministic, so the examples either simulate the algorithms or only print things that are guaranteed.

### The full example: instance vs static, `this`, and string references
```java
public class CollegeDemo {
    public static void main(String[] args) {
        System.out.println("college before any object: " + CollegeStudent.college); // static init already ran
        CollegeStudent s1 = new CollegeStudent("Aditya", 28);
        s1.markAttendance();
        CollegeStudent.changeCollege("IIT Bombay");          // static: called on the class, no 'this'
        System.out.println("college now: " + CollegeStudent.college);

        CollegeStudent s2 = new CollegeStudent("Rohit", 21);
        System.out.println("shared static seen via s2: " + s2.getCollege());
        System.out.println("literal \"Aditya\" is pooled (same object): " + (s1.name == "Aditya"));
    }
}

class CollegeStudent {
    String name;                       // instance fields: inside each object on the heap
    int age;
    static String college = "IIT G";   // one class-level copy, refers to a pooled literal

    CollegeStudent(String name, int age) { // the constructor frame holds this, name, age
        this.name = name;
        this.age = age;
    }

    void markAttendance() {            // instance method: has 'this'
        System.out.println(this.name + " is present");
    }

    static void changeCollege(String newCollege) { // static method: no 'this'
        college = newCollege;          // just re-points the static reference
    }

    String getCollege() { return college; }
}
// Output:
// college before any object: IIT G
// Aditya is present
// college now: IIT Bombay
// shared static seen via s2: IIT Bombay
// literal "Aditya" is pooled (same object): true
```

### Simulating generations: Eden → S0/S1 → Old
```java
import java.util.*;

public class GenerationsSimulation {
    static final int THRESHOLD = 3; // real HotSpot default is 15; 3 keeps the demo short

    public static void main(String[] args) {
        // object name -> number of GCs it survives before becoming unreachable
        Map<String, Integer> lifetime = new LinkedHashMap<>();
        lifetime.put("OB1", 0);
        lifetime.put("OB2", 99); // long-lived
        lifetime.put("OB3", 0);
        lifetime.put("OB4", 2);

        List<String> eden = new ArrayList<>(lifetime.keySet());
        Map<String, Integer> survivor = new LinkedHashMap<>(); // name -> age
        List<String> old = new ArrayList<>();
        String[] spaces = {"S0", "S1"};

        for (int gc = 1; gc <= 4; gc++) {
            Map<String, Integer> next = new LinkedHashMap<>();
            for (String o : eden) if (lifetime.get(o) >= gc) next.put(o, 1);   // copy live Eden objects
            for (var e : survivor.entrySet()) {
                String o = e.getKey();
                if (lifetime.get(o) < gc) continue;                            // died: not copied
                int age = e.getValue() + 1;
                if (age >= THRESHOLD) old.add(o); else next.put(o, age);      // promote or keep in young gen
            }
            eden.clear();      // Eden is wiped in one go (copying collector)
            survivor = next;
            System.out.println("after minor GC " + gc + ": " + spaces[(gc - 1) % 2] + "=" + survivor + " old=" + old);
        }
    }
}
// Output:
// after minor GC 1: S0={OB2=1, OB4=1} old=[]
// after minor GC 2: S1={OB2=2, OB4=2} old=[]
// after minor GC 3: S0={} old=[OB2]
// after minor GC 4: S1={} old=[OB2]
```

### Mark-sweep vs mark-compact vs copying (on an array "heap")
```java
import java.util.*;

public class GcAlgorithmsSimulation {
    public static void main(String[] args) {
        String[] heap = {"A", "B", "C", "D", "E"};
        Set<String> reachable = Set.of("A", "C", "E");   // result of the MARK phase

        String[] swept = heap.clone();                    // SWEEP: free unmarked slots in place
        for (int i = 0; i < swept.length; i++) if (!reachable.contains(swept[i])) swept[i] = "_";
        System.out.println("mark-sweep:   " + Arrays.toString(swept) + "  <- holes = fragmentation");

        String[] compacted = new String[heap.length];     // COMPACT: slide live objects together
        Arrays.fill(compacted, "_");
        int next = 0;
        for (String o : heap) if (reachable.contains(o)) compacted[next++] = o;
        System.out.println("mark-compact: " + Arrays.toString(compacted) + "  <- contiguous free space, but objects moved");

        List<String> eden = List.of("A", "B", "C", "D");  // COPYING: copy only live objects, wipe source
        Set<String> liveYoung = Set.of("B", "D");
        List<String> s0 = new ArrayList<>();
        for (String o : eden) if (liveYoung.contains(o)) s0.add(o);
        System.out.println("copying:      eden=[] s0=" + s0 + "  <- cost depends only on live objects");
    }
}
// Output:
// mark-sweep:   [A, _, C, _, E]  <- holes = fragmentation
// mark-compact: [A, C, E, _, _]  <- contiguous free space, but objects moved
// copying:      eden=[] s0=[B, D]  <- cost depends only on live objects
```

### Strong, soft, weak and phantom references
```java
import java.lang.ref.*;

public class ReferenceTypesDemo {
    public static void main(String[] args) throws InterruptedException {
        Object strong = new Object();                               // strong: never collected while reachable

        SoftReference<byte[]> soft = new SoftReference<>(new byte[1024]);
        System.out.println("soft.get() with plenty of memory: " + (soft.get() != null));

        Object target = new Object();
        WeakReference<Object> weak = new WeakReference<>(target);
        System.gc();
        System.out.println("weak.get() while a strong ref exists: " + (weak.get() != null));

        target = null;                                              // now only weakly reachable
        for (int i = 0; i < 50 && weak.get() != null; i++) {        // System.gc() is only a request,
            System.gc();                                            // so retry a few times
            Thread.sleep(10);
        }
        System.out.println("weak.get() after dropping the strong ref and GC: " + weak.get());

        ReferenceQueue<Object> queue = new ReferenceQueue<>();
        PhantomReference<Object> phantom = new PhantomReference<>(strong, queue);
        System.out.println("phantom.get() is always: " + phantom.get());
    }
}
// Output:
// soft.get() with plenty of memory: true
// weak.get() while a strong ref exists: true
// weak.get() after dropping the strong ref and GC: null
// phantom.get() is always: null
```

### `WeakHashMap`: entries vanish when the key is no longer strongly referenced
```java
import java.util.WeakHashMap;

public class WeakHashMapDemo {
    public static void main(String[] args) throws InterruptedException {
        WeakHashMap<Object, String> cache = new WeakHashMap<>();
        Object key = new Object();
        cache.put(key, "cached value");
        System.out.println("size while key is referenced: " + cache.size());

        key = null;                                       // drop the only strong reference
        for (int i = 0; i < 50 && !cache.isEmpty(); i++) {
            System.gc();                                  // request GC (not guaranteed, so retry)
            Thread.sleep(10);
        }
        System.out.println("size after key became unreachable: " + cache.size());
    }
}
// Output:
// size while key is referenced: 1
// size after key became unreachable: 0
```

## Common Pitfalls / Tips
- **"Minor GC = no pause" is false.** Young collections are stop-the-world too. They're just short because only the live objects are copied.
- **Lots of short-lived garbage is cheap. Long-lived garbage is expensive.** Objects that survive long enough to be promoted and *then* die cause costly old-generation collections.
- **Static collections are the classic leak.** Anything reachable from a `static` field lives as long as the class. Remove entries, or use weak or bounded structures.
- **Don't call `System.gc()` in application code.** It's a hint, it may trigger a full, expensive collection, and it can be disabled.
- **Soft/weak references don't override strong ones.** Clearing only happens when the object isn't strongly reachable.
- **Always check `ref.get()` for `null`** and keep the result in a local variable before using it. It can be cleared between two calls.
- **Phantom references are for cleanup hooks**, not for reaching the object. Prefer `java.lang.ref.Cleaner` over `finalize()` (which is deprecated).
- **Know your collector.** The default is G1 (Java 9+). Low-latency options are ZGC (`-XX:+UseZGC`) and Shenandoah. Throughput-oriented: `-XX:+UseParallelGC`. Log with `-Xlog:gc` to see minor/major collections and pause times.
- **Investigating OOMs:** add `-XX:+HeapDumpOnOutOfMemoryError` and analyse the `.hprof` file in VisualVM or Eclipse MAT. Look for the biggest retained sizes and their GC-root paths.

## Review Questions
1. Name the parts of the heap and say what lives in each.
2. Describe an object's journey from `new` to the old generation. What increments its age?
3. Why does the JVM collect the young generation often and the old generation rarely?
4. Give three reasons an object might be promoted before reaching age 15.
5. Compare mark-and-sweep, mark-compact and copying: how each works, its main drawback, and which generation it suits.
6. What is stop-the-world, and why does the GC need it? Does Parallel GC remove it?
7. In the `Student` example, what's on the stack, the heap and the Method Area during the constructor call? Why does `changeCollege` have no `this`?
8. Explain strong, soft, weak and phantom references, with one use case for soft and weak.
9. What does `System.gc()` guarantee? How would you find a memory leak in production?

<details>
<summary>Answers</summary>

1. The Young Generation: **Eden** (all new objects) plus survivor spaces **S0/S1** (objects that survived at least one minor GC, with their age). The **Old Generation** holds long-lived, promoted objects. The **string pool** holds interned strings and literals.
2. It's allocated in Eden. At each minor GC, if it's still reachable it's copied to the empty survivor space (Eden → S0 → S1 → S0 ...), and its age goes up by 1 per survived GC. When the age reaches the tenuring threshold (default 15), it's promoted to the old generation.
3. Most objects die young, so young collections reclaim a lot quickly (copying only the few survivors). Old objects rarely die and have deeper reference graphs, so scanning them is expensive and yields little. Doing it less often saves time.
4. The survivor space overflows, so the survivors don't fit. Dynamic age determination: same-or-younger-age objects fill more than the target share of a survivor space, so the threshold is lowered. The object is very large and is allocated directly in the old generation (or humongous regions in G1).
5. Mark-and-sweep marks reachable objects and frees the rest in place. It's simple, but leaves fragmentation (old generation). Mark-compact marks, then slides live objects together. There's no fragmentation, but moving objects is costly (old generation). Copying copies live objects to an empty space and wipes the source. It's fast for mostly-dead spaces, but needs spare space (young generation: Eden/survivors).
6. Application threads are paused so the GC sees a consistent object graph. Otherwise the application could create objects or change references in the middle of the trace. Parallel GC only shortens the pause by using several GC threads. Concurrent collectors (G1, ZGC, Shenandoah) do most of the work alongside the application, but still have brief pauses.
7. Stack: the `main` frame (`args`, `s1`) and the constructor frame (`this`, `name`, `age`). Heap: the `Student` object (`name` → pooled `"Aditya"`, `age = 28`) and the pooled strings. Method Area: the `Main` and `Student` class metadata, method bytecode, and the static `college`. `changeCollege` is `static`: it belongs to the class, not to an object, so there's no `this`.
8. Strong: normal references, never collected while reachable. Soft: cleared only when memory is needed, which suits memory-sensitive **caches**. Weak: cleared at the next GC once only weakly reachable, as used by **`WeakHashMap`** and canonicalizing maps. Phantom: `get()` is always `null`, and it's enqueued after the object is found unreachable for post-mortem cleanup (`Cleaner`).
9. Nothing. It's only a request, which the JVM may ignore. To find a leak, capture a heap dump (`jcmd <pid> GC.heap_dump`, or `-XX:+HeapDumpOnOutOfMemoryError`) and analyse it in VisualVM or Eclipse MAT for the largest retained objects and what references them.
</details>

## Further Study
- HotSpot collectors: Serial, Parallel, G1 (region-based), ZGC, Shenandoah, and when to choose each
- GC logging (`-Xlog:gc*`) and reading pause times; GC tuning flags (`-Xmn`, `-XX:MaxTenuringThreshold`, `-XX:SurvivorRatio`)
- GC roots, tri-color marking, write barriers and card tables (how concurrent and young collections work)
- Thread-local allocation buffers (TLABs) and bump-pointer allocation (why Eden allocation is fast)
- Escape analysis and scalar replacement
- `ReferenceQueue`, `Cleaner`, and why `finalize()` is deprecated
- Heap dump analysis with VisualVM / Eclipse MAT; Java Flight Recorder
- Metaspace and class unloading

---

[← #45](45-java-memory-stack-heap-method-area.md) · [Index](ReadMe.md) · [#47 →](47-multithreading-process-vs-thread.md)
