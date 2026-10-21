# 14 · Objects Deep Dive · Size, Call by Value, Shallow vs Deep Copy
> Full notes: [14-objects-size-call-by-value-copy.md](../14-objects-size-call-by-value-copy.md) · [▶ Video](https://youtu.be/7yd5qO2TPr0)

## Size of a reference
- **4 bytes** on a 64-bit JVM with compressed references (the default), and 8 bytes on some setups. Think **4 bytes**.

## Object size = header + field data + padding
Adding up the fields of `Student { String name; int age; int rollNumber; String college; }` gives 4 + 4 + 4 + 4 = **16 bytes**, but the real size is larger. There are three parts:

| Part | Size | What it holds |
| :--- | :--- | :--- |
| **Object header** | **12 bytes** | the object's **metadata** |
| ↳ mark word | 8 | locking/synchronization state and GC information |
| ↳ class pointer | 4 (or 8) | points to the object's **class metadata** (that's how the JVM knows it's a `Student`) |
| **Field data** | sum of the fields | 16 for `Student` |
| **Padding** | 0–7 | fills the size up to a **multiple of 8 bytes** (CPUs move data in 8-byte chunks) |

```
Student:  header 12 + fields 16 = 28  →  padding 4  →  32 bytes
Person { byte age; }:  header 12 + field 1 = 13  →  padding 3  →  16 bytes
```
- The video says the `Person` total is 18 bytes, but that's an addition slip: 12 + 1 + 3 = **16**.
- These are the object's **own (shallow) sizes**. The `String` objects that `name` and `college` point to are **separate** heap objects. To measure real sizes, use the OpenJDK **JOL** tool.

---

## Java is always call by value

**The rule: Java always passes a *copy of the value*.**
- For a **primitive**, the value is the number itself.
- For an **object**, the value is the **reference** (the address). The object itself is never copied.

### Case 1: a primitive (the change does NOT stick)
```java
static void add10(int x, int y) {   // NEW variables that get copies of 4 and 5
    x = x + 10;
    y = y + 10;
}

int x = 4, y = 5;
add10(x, y);
System.out.println(x + " " + y);    // 4 5   ← unchanged!
```
```
   main's frame            add10's frame
  ┌─────────────┐        ┌──────────────┐
  │ x = 4       │  copy  │ x = 4 → 14   │
  │ y = 5       │ ─────► │ y = 5 → 15   │   ← only the copies change
  └─────────────┘        └──────────────┘
                          (this frame disappears when add10 returns)
```
- `add10` changes **its own copies**. When it returns, its stack frame (and those copies) is gone. `main`'s `x` and `y` were never touched.

### Case 2: an object (the change DOES stick)
```java
static void add10(Random r) {       // r gets a COPY of r1's reference
    r.x = r.x + 10;
    r.y = r.y + 10;
}

Random r1 = new Random(4, 5);
add10(r1);
System.out.println(r1.x + " " + r1.y);   // 14 15   ← changed!
```
```
   STACK                                  HEAP
  main:   r1 = 1001 ─────────┐
                             ├──────►  Random @1001 { x = 4 → 14, y = 5 → 15 }
  add10:  r  = 1001 ─────────┘         (ONE object, two references to it)
          (a copy of the address)
```
- `r` is a **new variable** holding a **copy of the address** (1001), so it points at **the same object**.
- `r.x = r.x + 10` follows the address and changes the **shared object**.
- When `add10` returns, `r` disappears, but the object has already been changed, so `r1` sees `14 15`.

### What call by reference would mean (C++, not Java)
- With true call by reference, the parameter is an **alias for the caller's variable itself**, so `x = x + 10` would change `main`'s `x`. C++ supports this (`int&`).
- **Java has no call by reference.**

### ❌ The myth: "primitives by value, objects by reference"
- This is a **common interview mistake**. Java is **only call by value**. With objects, the value being copied happens to be a **reference**, which gives a *call-by-reference-like effect* (changes to the shared object are visible), but **the reference itself is a copy**.
- **The test that proves it:** reassign the parameter inside the method. With true call by reference, `r1` would change. In Java, it doesn't:

```java
public class CallByValueProof {
    static void add10(Pair r) {     // modify THROUGH the copied reference
        r.x += 10;
        r.y += 10;
    }

    static void reassign(Pair r) {  // point the COPY somewhere else
        r = new Pair(0, 0);
        r.x = 99;
    }

    public static void main(String[] args) {
        int a = 4;
        addTen(a);
        System.out.println(a);                    // 4      primitive: the copy changed, not a

        Pair r1 = new Pair(4, 5);
        add10(r1);
        System.out.println(r1.x + " " + r1.y);    // 14 15  the shared object was changed

        reassign(r1);
        System.out.println(r1.x + " " + r1.y);    // 14 15  r1 still points to the SAME object
    }                                             //        → proof that it's NOT call by reference

    static void addTen(int n) { n += 10; }
}

class Pair {
    int x, y;
    Pair(int x, int y) { this.x = x; this.y = y; }
}
```

| What the method does | Primitive argument | Object argument |
| :--- | :--- | :--- |
| changes the parameter (`x = …`, `r = new …`) | ❌ caller not affected | ❌ caller not affected |
| changes the object's **fields** (`r.x = …`) | — | ✅ caller sees it (same object) |

---

## Objects as parameters, return types and constructor arguments
- A method can **take** an object (`add10(Random r)`) and **return** one (`Random doubled(Random r) { return new Random(r.x * 2, r.y * 2); }`).
- A constructor can take an object **of its own class**. This is a **copy constructor**:
```java
Random(Random r) {          // copy constructor
    this.x = r.x;
    this.y = r.y;
}

Random r2 = new Random(r1); // a NEW object with r1's values
```

## Copy constructor in memory
```
  r1 ───►  Random @1001 { x = 4, y = 5 }
  r2 ───►  Random @2002 { x = 4, y = 5 }   ← a second, independent object (new was called)
```
- `new` creates a **second** object. The constructor's parameter `r` is a copy of `r1`'s reference, and it's used to **read** 4 and 5 into the new object.
- `r1` and `r2` point to **different** objects with equal values, so changing `r2.x` to 14 **doesn't** affect `r1`.

## `r3 = r1`: just another reference
```
  r1 ───┐
        ├──►  Random @1001 { x = 4, y = 5 }    ← no new, so no new object
  r3 ───┘
```
- `Random r3 = r1;` has **no `new`**, so **no object is created**. `r3` gets the **same address** as `r1`.
- `r3.x = 14` shows up as `r1.x == 14`, and the other way round.
- **Rule of thumb:** **every `new` creates an independent object.** Plain assignment only **shares** one.

---

## Deep copy vs shallow copy

### The video's naming
| Code | The video calls it | What happens |
| :--- | :--- | :--- |
| `r2 = new Random(r1)` | **deep copy** | a new object, with the values copied |
| `r3 = r1` | **shallow copy** | the same object, with a second name |

- Sometimes you **want** two names for one object (shared state). Other times you want **independent** copies.

### The standard meaning (what interviews expect)
| Term | What it is |
| :--- | :--- |
| **Reference assignment (aliasing)** | `r3 = r1`. **No copy at all**, just a second reference to the same object. |
| **Shallow copy** | a **new** object whose fields are copied **as they are**. **Reference fields still point to the same nested objects.** |
| **Deep copy** | a **new** object, and the **nested objects are copied too** (recursively). Nothing is shared. |

- For a class with **only primitive fields** (like `Random`), shallow and deep copies behave the same.
- **The difference only shows up when a field is itself an object or an array:**

```
SHALLOW COPY                                   DEEP COPY
original ──► Marksheet { marks ─┐ }            original ──► Marksheet { marks ──► [90, 80] }
                                ├─► [90, 80]
shallow  ──► Marksheet { marks ─┘ }            deep     ──► Marksheet { marks ──► [90, 80] }  (its own array)
             (two objects, ONE shared array)                (two objects, two arrays)
```

```java
import java.util.Arrays;

public class ShallowVsDeep {
    public static void main(String[] args) {
        Marksheet original = new Marksheet("Aditya", new int[]{90, 80});
        Marksheet alias    = original;                 // reference assignment: no copy
        Marksheet shallow  = original.shallowCopy();
        Marksheet deep     = original.deepCopy();

        original.marks[0] = 10;                        // change the nested array through the original

        System.out.println(Arrays.toString(alias.marks));    // [10, 80]  same object
        System.out.println(Arrays.toString(shallow.marks));  // [10, 80]  new object, but SHARES the array
        System.out.println(Arrays.toString(deep.marks));     // [90, 80]  has its OWN array

        System.out.println(alias == original);               // true   (same object)
        System.out.println(shallow == original);             // false  (a new object)
        System.out.println(shallow.marks == original.marks); // true   (shared nested array)
        System.out.println(deep.marks == original.marks);    // false  (copied nested array)
    }
}

class Marksheet {
    String name;
    int[] marks;

    Marksheet(String name, int[] marks) { this.name = name; this.marks = marks; }

    Marksheet shallowCopy() {
        return new Marksheet(name, marks);           // copies the REFERENCE to the array
    }

    Marksheet deepCopy() {
        return new Marksheet(name, marks.clone());   // copies the ARRAY itself
    }
}
```
- `String` fields can safely be shared even in a "deep" copy, because strings are **immutable** (#25).

**Remember:** an object's size is **header (12) + fields + padding**, rounded up to a **multiple of 8**. **Java is always call by value.** For objects, the *reference* is copied, so changes to **fields** are seen by the caller, but **reassigning** the parameter isn't. `r3 = r1` is just another name for the same object, and every **`new`** makes an independent one. A **shallow** copy shares nested objects, and a **deep** copy copies them too.
