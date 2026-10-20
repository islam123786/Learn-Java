# Objects Deep Dive | Size, Call by Value vs Reference | Shallow vs Deep Copy | Java Full Course #14
> Source: https://youtu.be/7yd5qO2TPr0 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 44:20. The video is in Hindi, and its transcript had no timing data, so the timestamps below are rough estimates based on the relative position of each part in the transcript text. They may be off by a couple of minutes.

## TL;DR
- A **reference** usually takes **4 bytes** (64-bit JVM with compressed references, the default) or 8 bytes without compression.
- An **object's size** = **header** (8-byte mark word + 4/8-byte class pointer) + **field data** + **padding**, rounded up to a **multiple of 8 bytes**. A `Student` with 2 `int`s and 2 `String` references is 12 + 16 = 28 → **32 bytes**.
- **Java is always call by value.** Primitives: the value is copied, so the method can't change the caller's variable. Objects: the *reference* is copied, so both copies point at the **same object**, and changes made through it are visible to the caller. That feels like call by reference, but it isn't.
- Creating a **new object** and copying the values into it (e.g. a copy constructor) gives two independent objects. The video calls this a **deep copy**.
- `r3 = r1` copies only the reference, so both names point at one object. The video calls this a **shallow copy**. (See the terminology note below for the standard definitions.)

## Key Concepts

**Reference size.** A reference variable holds a pointer to a heap object. On a 64-bit JVM with *compressed oops* (enabled by default for heaps under ~32 GB) it's 4 bytes, otherwise 8. *Why it matters:* every reference field inside an object costs this much.

**Object header.** Metadata the JVM keeps for every object. The **mark word** (8 bytes on 64-bit) holds lock state, identity hash code and GC age bits. The **class pointer** (4 bytes compressed, or 8) tells the JVM which class the object belongs to. *Why it matters:* even an object with no fields costs at least 12 → 16 bytes.

**Field data.** The actual instance fields: primitives by their size (`byte` 1, `short`/`char` 2, `int`/`float` 4, `long`/`double` 8, `boolean` usually 1) and references at 4/8 bytes. *Why it matters:* choosing `byte` vs `int` or adding a field changes the footprint.

**Padding (alignment).** HotSpot aligns objects to 8 bytes, so the total is rounded up to the next multiple of 8. *Why it matters:* it makes memory access efficient, and it explains "wasted" bytes.

**Call by value.** The method receives a **copy** of the argument's value. *Why it matters:* this is the only passing mode Java has.

**Call by reference.** The parameter is an alias of the caller's variable itself (C++ `int&`). *Why it matters:* Java does **not** have it. A common interview trap.

**Aliasing.** Two references holding the same address refer to one object. *Why it matters:* a change made through one name is visible through every other name.

**Copy constructor.** A constructor that takes an object of the same class and copies its fields into the new object: `Random(Random other)`. *Why it matters:* it creates an independent copy.

**Shallow vs deep copy.** In the video's terms, *deep* means a new object with the values copied, and *shallow* means just another reference to the same object. *Why it matters:* it decides whether a change to the "copy" affects the original.

## Detailed Notes

### Recap: where things live [00:00]
- `Student s1 = new Student(...)`: `new` allocates the object (with fields `name`, `age`, `rollNumber`, `college`) on the **heap**, and the reference `s1` is on the **stack**.
- We know `int` is 4 bytes and `double` is 8. What do a reference and an object cost?

### Size of a reference [00:50]
- 4 bytes on a 64-bit JVM using compression (the default), and 8 bytes on some JVM configurations. It varies between JVMs, but think **4 bytes** by default.

### Naive object size, and why it's wrong [02:30]
- Fields: `String name` (reference, 4) + `int age` (4) + `int rollNumber` (4) + `String college` (reference, 4) = **16 bytes**. The real size is larger.
- Three contributors: **header**, **exact field data**, **padding**.

### Object header [04:10]
- Also called the object header. It stores the object's **metadata** (data about data).
- **Mark word: 8 bytes.** Locking information, synchronization state and GC information (covered later, with multithreading and garbage collection).
- **Class pointer: 4 bytes** (or 8). The video loosely compares it to a reference. More precisely, it points to the object's *class metadata*, which is how the JVM knows the object is a `Student`.
- Header total: **12 bytes** (with compressed class pointers).

### Field data and padding [08:20]
- Field data for `Student` is 16 bytes. Header + data = 28 bytes.
- CPUs move data in 8-byte chunks, so the JVM keeps every object a **multiple of 8 bytes**. The next multiple after 28 is 32, so there are **4 bytes of padding**.
- **`Student` object = 12 + 16 + 4 = 32 bytes.**

### Practice: a `Person` with a `byte age` [11:00]
- A `byte` is enough for an age (range −128..127. The video says "−18", a slip).
- Header 12 + field 1 = 13. The next multiple of 8 is 16, so 3 bytes of padding.
- *Correction:* the video adds 12 + 1 + 3 and says **18 bytes**. The correct total is **16 bytes** (12 + 1 + 3 = 16).
- *Note:* these figures are the object's own ("shallow") size. The `String` objects that `name` and `college` point to are separate heap objects with their own headers and character arrays. To measure real sizes, use the OpenJDK **JOL** (Java Object Layout) tool.

### Call by value: the surprising primitive example [13:20]
- `int x = 4, y = 5;` and `static void add10(int x, int y) { x = x + 10; y = y + 10; }`.
- It prints `4 5`, calls `add10(x, y)`, and prints again: still **`4 5`**.

### Same idea with an object [15:00]
- `class Random { int x, y; Random(int x, int y) {...} }`, `Random r1 = new Random(4, 5)`, and `add10(Random r) { r.x += 10; r.y += 10; }`.
- Print, call `add10(r1)`, print again: **`4 5` → `14 15`**. This time the change stuck. Why?

### Walking through the primitive case [16:40]
- `main`'s frame has `x = 4`, `y = 5`. Calling `add10(x, y)` creates **new** parameter variables `x`, `y` in `add10`'s frame and **copies** 4 and 5 into them.
- `add10` changes its own copies to 14 and 15. When it returns, its frame (and those copies) disappears.
- Back in `main`, the original `x`, `y` are untouched, so it prints `4 5`. This is **call by value**.

### What call by reference would be (C++ only) [20:30]
- If the parameters were aliases of `main`'s `x` and `y`, then `x = x + 10` would change `main`'s variables. That's **call by reference**, and C/C++ support it (C++ through `int&`).
- **Java has no call by reference.** Everything is call by value.

### Walking through the object case [23:00]
- Heap: one `Random` object `{x=4, y=5}`. Stack: `r1` holds its address (say 1001).
- Calling `add10(r1)` creates a **new** parameter variable `r` and copies `r1`'s *value* into it. That value is the address 1001, so `r` points at **the same object**.
- `r.x = r.x + 10` follows the address and changes the shared object to `{14, 15}`.
- When the method returns, `r` disappears (it was a local). The object's fields have already changed, so `r1.x`, `r1.y` print `14 15`.

### "Java is call by value for primitives and call by reference for objects": wrong [29:30]
- A common fresher mistake in interviews. Java is **only** call by value. For objects, the value being copied is a reference, so you get a *call-by-reference-like effect* (shared mutations), but the reference itself is a copy.
- A quick test that proves it: reassigning the parameter (`r = new Random(0, 0)`) inside the method does **not** change what `r1` points to. True call by reference would.

### Non-primitives as parameters, return types and constructor arguments [31:30]
- A method can take an object (`add10(Random r)`) and also **return** one (`Random add10(Random r) { …; return r; }`).
- A constructor can take an object of its own class, which is a **copy constructor**:
  `Random(Random r) { this.x = r.x; this.y = r.y; }`, used as `Random r2 = new Random(r1);`.

### Copy constructor in memory [33:30]
- `new` allocates a **second** object. The constructor's parameter `r` is a copy of `r1`'s reference, and it's used to read 4 and 5 into the new object.
- `r1` and `r2` now point to **different** objects with equal values. Changing `r2.x` to 14 doesn't affect `r1`.

### `r3 = r1`: just another reference [37:30]
- `Random r3 = r1;` has no `new`, so no object is created. `r3` gets the same address as `r1`.
- `r3.x = 14` is visible as `r1.x == 14`, and vice versa.
- Rule of thumb: **every `new` creates an independent object**, and plain assignment only shares one.

### Deep copy vs shallow copy [40:30]
- The video's naming: `r2 = new Random(r1)` (new object, values copied) is a **deep copy**. `r3 = r1` (same object) is a **shallow copy**.
- Sometimes you want two names for one object (shared state). Sometimes you want independent copies.
- *Terminology note:* in standard usage, `r3 = r1` is **reference assignment (aliasing)**, not a copy at all. A **shallow copy** is a *new* object whose fields are copied as-is, so any **reference fields** still point to the same nested objects. A **deep copy** also copies those nested objects recursively. For a class with only primitive fields, like `Random`, shallow and deep copies behave the same. The difference shows up when a field is itself an object or an array (see the last code example).

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java FileName.java`.

### Estimating object size with header + fields + padding
```java
public class ObjectSizeEstimate {
    static final int MARK_WORD = 8;
    static final int CLASS_POINTER = 4; // compressed class pointers (default on 64-bit)
    static final int REFERENCE = 4;     // compressed oops (default on 64-bit)

    static int align8(int bytes) {
        return (bytes + 7) / 8 * 8;     // round up to the next multiple of 8
    }

    static void show(String label, int fieldBytes) {
        int header = MARK_WORD + CLASS_POINTER;
        int raw = header + fieldBytes;
        int total = align8(raw);
        System.out.println(label + ": header " + header + " + fields " + fieldBytes
                + " + padding " + (total - raw) + " = " + total + " bytes");
    }

    public static void main(String[] args) {
        // Student { String name; int age; int rollNumber; String college; }
        show("Student", REFERENCE + 4 + 4 + REFERENCE);
        // Student: header 12 + fields 16 + padding 4 = 32 bytes

        // Person { byte age; }
        show("Person", 1);
        // Person: header 12 + fields 1 + padding 3 = 16 bytes

        // Empty object
        show("Empty", 0);
        // Empty: header 12 + fields 0 + padding 4 = 16 bytes
    }
}
```

### Call by value with primitives
```java
public class CallByValuePrimitive {
    static void add10(int x, int y) { // x, y are new copies
        x = x + 10;
        y = y + 10;
        System.out.println("inside: " + x + " " + y); // inside: 14 15
    }

    public static void main(String[] args) {
        int x = 4, y = 5;
        System.out.println(x + " " + y); // 4 5
        add10(x, y);
        System.out.println(x + " " + y); // 4 5  (main's variables are unchanged)
    }
}
```

### Passing an object: the reference is copied
```java
public class CallByValueObject {
    static void add10(Pair r) {       // r is a copy of the caller's reference
        r.x = r.x + 10;               // modifies the shared object
        r.y = r.y + 10;
    }

    static void reassign(Pair r) {
        r = new Pair(0, 0);           // only the local copy now points elsewhere
        r.x = 99;
    }

    static Pair doubled(Pair r) {     // objects can be returned too
        return new Pair(r.x * 2, r.y * 2);
    }

    public static void main(String[] args) {
        Pair r1 = new Pair(4, 5);
        System.out.println(r1.x + " " + r1.y); // 4 5
        add10(r1);
        System.out.println(r1.x + " " + r1.y); // 14 15

        reassign(r1);                          // proves it's NOT call by reference
        System.out.println(r1.x + " " + r1.y); // 14 15

        Pair d = doubled(r1);
        System.out.println(d.x + " " + d.y);   // 28 30
    }
}

class Pair {
    int x, y;
    Pair(int x, int y) { this.x = x; this.y = y; }
}
```

### Copy constructor vs reference assignment (the video's demo)
```java
public class CopyVsAlias {
    public static void main(String[] args) {
        Coord r1 = new Coord(4, 5);
        Coord r2 = new Coord(r1); // new object, values copied
        Coord r3 = r1;            // no new: same object, second name

        r2.x = 14;
        System.out.println(r1.x); // 4   (r2 is independent)

        r3.x = 14;
        r3.y = 15;
        System.out.println(r1.x + " " + r1.y); // 14 15 (r3 and r1 share one object)

        System.out.println(r1 == r3); // true
        System.out.println(r1 == r2); // false
    }
}

class Coord {
    int x, y;
    Coord(int x, int y) { this.x = x; this.y = y; }
    Coord(Coord other) {       // copy constructor
        this.x = other.x;
        this.y = other.y;
    }
}
```

### Standard meaning: shallow vs deep copy with a nested object
```java
import java.util.Arrays;

public class ShallowVsDeep {
    public static void main(String[] args) {
        Marksheet original = new Marksheet("Aditya", new int[]{90, 80});

        Marksheet shallow = original.shallowCopy();
        Marksheet deep = original.deepCopy();

        original.marks[0] = 10; // mutate the nested array through the original

        System.out.println(Arrays.toString(shallow.marks)); // [10, 80]  shares the array
        System.out.println(Arrays.toString(deep.marks));    // [90, 80]  has its own array
        System.out.println(shallow != original);           // true (shallow copy is still a new object)
    }
}

class Marksheet {
    String name;
    int[] marks;

    Marksheet(String name, int[] marks) { this.name = name; this.marks = marks; }

    Marksheet shallowCopy() {
        return new Marksheet(name, marks);          // copies the reference to the array
    }

    Marksheet deepCopy() {
        return new Marksheet(name, marks.clone());  // copies the array itself
    }
}
```

## Common Pitfalls / Tips
- **"Java passes objects by reference" is wrong.** It passes references *by value*. You can mutate the object, but you can't make the caller's variable point to a different object.
- **A swap method can't work in Java**, whether you pass `int`s or two references. Return a result or wrap the values in an object instead.
- **Object sizes are JVM-dependent.** The 12-byte header and 4-byte references assume a 64-bit HotSpot with compressed oops and class pointers. Use JOL to measure instead of guessing.
- **Don't forget the referenced objects.** A 32-byte `Student` object doesn't include the `String`s it points to.
- **Field order doesn't reliably change the layout.** The JVM reorders fields to reduce padding.
- **`=` between references never copies an object.** If you need an independent copy, use `new` (a copy constructor or a copy method).
- **A copy constructor that copies reference fields as-is produces a shallow copy.** Clone nested mutable objects (arrays, lists, other objects) if you need full independence.

## Review Questions
1. How many bytes does a reference take, and why can it differ between JVMs?
2. Name the three parts of an object's size and what the header contains.
3. Compute the size of an object with `String name; int age; int rollNumber; String college;`. What about one with only `byte age`?
4. Why does `add10(int x, int y)` not change `main`'s variables?
5. Why does `add10(Random r)` change `r1`'s fields if Java is call by value?
6. What experiment shows that Java does not pass objects by reference?
7. After `Random r2 = new Random(r1); Random r3 = r1;`, which of `r2`/`r3` sees changes made through `r1`, and why?
8. What are the standard definitions of shallow and deep copy, and when do they differ?

<details>
<summary>Answers</summary>

1. 4 bytes with compressed references (the default on 64-bit JVMs with heaps under ~32 GB), and 8 bytes without compression. It depends on the JVM and its settings.
2. Header, field data and padding. The header holds the mark word (8 bytes: lock/sync state, hash code, GC age) and the class pointer (4 or 8 bytes) to the class metadata.
3. 12 + (4 + 4 + 4 + 4) = 28 → padded to 32 bytes. With `byte age`: 12 + 1 = 13 → padded to 16 bytes (not 18 as stated in the video).
4. The parameters are new variables holding copies of 4 and 5. The method changes its copies, which disappear when it returns.
5. The parameter `r` receives a copy of `r1`'s value, and that value is a reference to the same heap object. Changing `r.x` changes that shared object, which `r1` also points to.
6. Reassign the parameter inside the method (`r = new Random(0, 0)`). The caller's `r1` still points to the original object, which wouldn't be the case with real call by reference.
7. `r3`, because `r3 = r1` copied the reference, so both point to the same object. `r2` was created with `new`, so it's a separate object that only started with the same values.
8. Shallow copy: a new object whose fields are copied directly, so nested reference fields are shared. Deep copy: nested objects are copied too, so nothing is shared. They differ only when the object has reference fields pointing to mutable objects (arrays, lists, other objects).
</details>

## Further Study
- JOL (Java Object Layout) for measuring real object sizes
- Compressed oops, `-XX:-UseCompressedOops`, object alignment
- Stack frames, the heap, and garbage collection
- `Object.clone()`, the `Cloneable` interface, and why copy constructors are usually preferred
- Immutability (`final` fields, `String`) as an alternative to defensive copies
- Defensive copying of mutable fields in constructors and getters

---

[← #13](13-constructors-chaining-overloading-this.md) · [Index](ReadMe.md) · [#15 →](15-static-final-blocks-string-args.md)
