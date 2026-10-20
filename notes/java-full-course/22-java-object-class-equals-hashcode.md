# Most Important Class in Java | Object Class (equals, hashCode, toString) | Java Full Course #22
> Source: https://youtu.be/541Zpv6sOKo · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 67:31. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `java.lang.Object` is the root of every class hierarchy. Every class extends it, either directly (the compiler adds `extends Object` for you) or indirectly through its parents. That includes JDK classes, your own classes, arrays and wrapper classes. It does **not** include primitives.
- Object exists so that (1) behavior every object needs lives in one place and (2) an `Object` reference can point to *anything*, which is polymorphism at its widest.
- The core methods are `toString()`, `equals()`, `hashCode()` and `getClass()`. On top of those there's `clone()`, the deprecated `finalize()`, and `wait()`/`notify()`/`notifyAll()` for threads.
- The default `equals()` is just `==` (it compares references). If you override it to compare values, you **must** also override `hashCode()`: **equal objects must have equal hash codes**, while equal hash codes don't have to mean equal objects.
- `clone()` only works if the class implements the **marker interface** `Cloneable`. The default clone is a **shallow** copy.

## Key Concepts

**Object as the root class.** Every class inherits from `Object`. If `Student extends Human` and `Human` extends nothing, then `Human` implicitly extends `Object`, so `Student` gets Object's methods indirectly. There's still only one parent per class, so this isn't multiple inheritance. *Why it matters:* every object you'll ever have supports `toString`, `equals`, `hashCode` and the rest.

**Object reference as a universal pointer.** `Object obj = new Student();` is legal because a parent reference can point to a child object. Through `obj` you can only call Object's methods, unless you cast. *Why it matters:* it's why methods like `equals(Object o)` and collections can accept any type.

**`toString()`.** Returns a String representation of the object. The default is `ClassName@hexHashCode`. Override it to control what printing an object shows. `System.out.println(obj)` calls it for you (via `String.valueOf`). *Why it matters:* readable logs and debugging output.

**`equals(Object o)`.** Decides whether two objects are "equal". The default implementation is `return this == obj;`, which is reference comparison. Override it to compare field values. *Why it matters:* collections such as `List.contains`, `HashSet` and `HashMap` all rely on it.

**`hashCode()`.** Returns an `int` summarizing the object. Hash-based collections use it to choose a bucket. *Why it matters:* the equals/hashCode contract is what makes `HashSet` and `HashMap` work with your classes.

**`getClass()` vs `instanceof`.** `getClass()` returns the **runtime class** as a `Class<?>` object (`getName()` gives the name). It's `final`, so you can't override it. `x instanceof T` is true if `x` is a `T` **or any subclass of `T`**. *Why it matters:* you pick between them when writing `equals`, and when you need to check types at runtime.

**`clone()` and `Cloneable`.** `protected Object clone() throws CloneNotSupportedException` makes a field-by-field copy, but only if the class implements `Cloneable`. Otherwise it throws. *Why it matters:* not every object should be copyable (think DB connections or threads), so Java makes you opt in.

**Marker interface.** An interface with no methods, such as `Cloneable`. Implementing it just *marks* the class so that the JVM or a library can check `instanceof` and unlock some behavior. *Why it matters:* it's a recurring Java design pattern (`Serializable` is another one).

## Detailed Notes

### Where Object lives, and why it's the parent of everything [00:44]
- `Object` is in `java.lang`, the same package as `System`. That package is auto-imported, so you never import `Object`.
- **Every class in Java inherits from Object**, whether you wrote it or the JDK did. Writing `class Student {}` is treated as `class Student extends Object {}`.
- If `Student extends Human`, then `Human` (or something higher up, e.g. `Animal`) extends `Object`. Somewhere at the top of every hierarchy there is a class whose parent is Object, so the inheritance is direct or indirect but always there.

### Why Java needs a root class [03:20]
1. **Common behavior.** Methods that every class needs are written once in `Object` and inherited everywhere through inheritance. So `s1.toString()` works on any `Student`.
2. **Polymorphism.** A parent reference can refer to a child object, so `Object obj = new Student();` always works. When you don't know what type to store something as, `Object` always fits. Through `obj` you can call **Object's** methods (not Student's) without a cast.

### The method list [06:47]
| Group | Methods |
|---|---|
| Core | `toString()`, `equals()`, `hashCode()`, `getClass()` |
| Cloning | `clone()` |
| Garbage collection | `finalize()` (deprecated) |
| Threads | `wait()`, `notify()`, `notifyAll()` (covered with multithreading) |

The video focuses on the core methods and `clone()`.

### `toString()` [08:02]
- Signature: `public String toString()`. Purpose: return a String representation of the object.
- Default output: **class name + `@` + the hash code in hex**, e.g. `Student@1b6d3586`. The number varies between runs and machines (see the correction below).
- Demo [11:24]: a `Student` with `name` and `age`. `s1.toString()` compiles even though Student declares no such method, which proves the method is inherited from Object.
- Overriding: `@Override public String toString() { return name + ", " + age; }`. The first attempt printed inside the method, which doesn't compile because the method has to **return** a String.
- `System.out.println(s1)` gives the same output as `println(s1.toString())`, because println converts the object with `toString()` internally [14:41].
- Writing `extends Object` explicitly changes nothing. It's optional.

> **Correction:** the hex number in the default `toString()` is `Integer.toHexString(hashCode())`. For a class that doesn't override `hashCode`, that's an *identity* hash code. It stays the same for one object during one run, but it isn't a memory address, it isn't "unique for the class", and it usually changes between runs.

### `equals()` with default behavior [15:25]
- Signature: `public boolean equals(Object obj)`. The parameter is `Object` because Object can't know which subclasses will exist.
- Default implementation: `return (this == obj);`, i.e. **reference comparison**, exactly like `==`.
- Memory picture [17:59]: two `new Student("Aditya", 28)` calls create **two heap objects**. `s1` and `s2` on the stack point to different objects, so `s1.equals(s2)` is **false** even though the fields match. `s1 == s2` is also false.
- Code demo [20:47]: same result. The video opens `Object.java` to show the one-line `this == obj`.

### Overriding `equals()` properly [23:25]
- The parameter type must stay `Object`. Writing `equals(Student s)` is an **overload**, not an override, and `@Override` reports the error "must override or implement a supertype method".
- Inside, cast: `Student s = (Student) obj;` then compare `this.name` with `s.name` and `this.age` with `s.age`. Now `s1.equals(s2)` is **true**.
- The first version had problems that the video then fixes:
  - **`null` argument** [25:38]: casting `null` works, but then reading `s.name` throws `NullPointerException`. Fix: `if (obj == null) return false;`
  - **Wrong type** [27:01]: passing an `Integer` compiles fine (anything is an Object), but the cast throws `ClassCastException`. Fix: `if (getClass() != obj.getClass()) return false;`
  - **Shortcut** [28:47]: `if (this == obj) return true;`. If both references point to the same object, skip the rest.

> **Correction:** the video compares `name` with `==`. That only works here because both names are the same pooled literal. For Strings, use `name.equals(other.name)`, or `Objects.equals(name, other.name)` if `name` can be `null`. `==` on Strings compares references (see #25).

### `hashCode()` [29:54]
- Signature: `public int hashCode()`. It returns an int for an object, and the same input always gives the same number. "Aditya" always maps to the same int and "Rohit" to a different one (usually).
- Functions like this are called **hash functions**. `HashMap`, `HashSet` and `Hashtable` use them internally.
- The video says the value is "in hexadecimal". That's not quite right: it's an ordinary `int`. Hex is only how the default `toString()` *displays* it.

### The equals/hashCode contract [32:26]
- **Rule: if two objects are equal (`a.equals(b)` is true), their hash codes must be equal.** Never break this.
- **The reverse might not be true** [33:21]. Two *unequal* objects can have the same hash code. That's a *collision*. Example: s1 (Aditya, 28) and s3 (Rohit, 28) could happen to hash to the same number.
- Why the rule exists: hash collections first use the hash to pick a bucket and then use `equals` inside the bucket. If equal objects had different hashes, they'd land in different buckets and a `HashSet` would happily store "duplicates".
- With the **default** `equals`, the rule holds automatically. Objects are only equal when they're the same object, and the same object always has the same hash. **Once you override `equals`, it's your job to override `hashCode` too.**

### Overriding `hashCode()` [40:52]
- Demo: with only `equals` overridden, `s1.equals(s2)` is true but `s1.hashCode() == s2.hashCode()` is false. That violates the contract.
- The classic recipe [41:39], using primes to spread values out:
  ```
  int result = 17;
  result = 31 * result + age;
  result = 31 * result + (name == null ? 0 : name.hashCode());
  return result;
  ```
  `String` already overrides `hashCode()`, so you can delegate to `name.hashCode()`.
- The **null check** matters. `name.hashCode()` on a `null` name throws `NullPointerException`, hence the ternary.
- Shortcut [46:36]: `return Objects.hash(name, age);`. Note that `java.util.Objects` (a utility class) is different from `java.lang.Object`.

### `getClass()` [47:03]
- Signature: `public final Class<?> getClass()`. It's **final**, so you can't override it. It returns a `Class` object describing the **runtime** class. `s1.getClass().getName()` gives `"Student"`.
- `Class` objects (and the `<?>` wildcard) come back later with reflection and generics.

### `instanceof` vs `getClass()` [50:26]
- `s1 instanceof Student` is true. `s1 instanceof Object` is also true, because `instanceof` accepts the class **or any subclass**.
- Demo [52:08] with `Animal` and `Dog extends Animal`:
  - `Animal a = new Animal(); Animal d = new Dog();`
  - `a.getClass().getName()` gives `Animal`. `d.getClass().getName()` gives `Dog`, the runtime object type, not the reference type.
  - `a instanceof Animal` → true. `d instanceof Animal` → true. `a instanceof Dog` → false.
- Summary: `getClass()` tells you *exactly* which class the object was created from. `instanceof` tells you whether it *is-a* T.

### `clone()` [54:32]
- Signature: `protected Object clone() throws CloneNotSupportedException`. It's `protected`, not public.
- To use it on your class, override it (you can widen the access to `public`) and call `super.clone()`. Cast the result: `Student s3 = (Student) s1.clone();`. Callers must handle or declare `CloneNotSupportedException`. Exceptions come later; for now, add `throws` to `main`.
- Running it **without** `implements Cloneable` compiles but throws `CloneNotSupportedException` at runtime [58:43]. Adding `implements Cloneable` fixes it, and `s3` then has Aditya / 28.
- Why opt in [56:08]: **not every object should be cloneable** (DB connections, threads…). Conceptually, `Object.clone()` does "if this implements Cloneable, copy; else throw".
- **Marker interface** [61:28]: `Cloneable` is empty, so there's nothing to implement. It only marks the class.
- The default clone is a **shallow copy** [62:48]. Fields are copied as-is, so reference fields (arrays, lists, other objects) end up **shared** between the original and the clone. For a deep copy, override `clone()` and copy those fields yourself.

> **Correction:** in the video's sketch the exception is first written as "ClassNotSupportedException". The video corrects it later: the right name is **`CloneNotSupportedException`**. Also, the rule isn't that you can't *override* `clone()` without `Cloneable`. Overriding compiles fine. It's `Object.clone()` (i.e. `super.clone()`) that throws at runtime.

### `finalize()` and the thread methods [63:15]
- `finalize()` used to run before the garbage collector reclaimed an object. It's **deprecated** because it's unpredictable, unsafe and unreliable: the GC decides if and when it runs.
- `wait()`, `notify()` and `notifyAll()` [64:35] are for thread coordination and are covered in the multithreading lectures.

> **Note:** `finalize()` has been deprecated since Java 9 and "deprecated for removal" since Java 18. For cleanup, use `try`-with-resources (`AutoCloseable`) or `java.lang.ref.Cleaner`.

### Arrays and primitives [65:06]
- Arrays are **non-primitive**. `new int[5]` creates an object on the heap, and the array's class extends Object, so Object's methods work on arrays.
- `java.util.Arrays` (a utility class) isn't the same thing as an array, just as `Objects` isn't `Object`. Java often uses a trailing "s" for a helper class.
- **Primitives (`int`, `char`, `float`, …) are not objects** [66:32], so there's no Object above them. You compare them with `==` and can't call `.equals()` or `.toString()` on them. Their **wrapper classes** (`Integer`, `Character`, …) do extend Object.

## Code Examples

Each block is a complete program. Save it under the class name (e.g. `ObjectToStringDemo.java`) and run it with `java ObjectToStringDemo.java`.

### `toString()`: default vs overridden
```java
public class ObjectToStringDemo {
    static class Plain {               // does not override toString
        String name = "Aditya";
    }

    static class Student {
        String name;
        int age;
        Student(String name, int age) { this.name = name; this.age = age; }

        @Override
        public String toString() {      // must RETURN a String, not print it
            return name + ", " + age;
        }
    }

    public static void main(String[] args) {
        Plain p = new Plain();
        // Default format is ClassName@hexHashCode; the hex part varies per run,
        // so we only check its shape here.
        String def = p.toString();
        System.out.println(def.startsWith(p.getClass().getName() + "@")); // true
        System.out.println(def.equals(p.getClass().getName() + "@" + Integer.toHexString(p.hashCode()))); // true

        Student s1 = new Student("Aditya", 28);
        System.out.println(s1.toString()); // Aditya, 28
        System.out.println(s1);            // Aditya, 28  (println calls toString for you)
        System.out.println("Student: " + s1); // Student: Aditya, 28
    }
}
```

### A correct `equals()` + `hashCode()` pair
```java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class ObjectEqualsHashCodeDemo {
    static class Student {
        String name;
        int age;
        Student(String name, int age) { this.name = name; this.age = age; }

        @Override
        public boolean equals(Object obj) {          // parameter must be Object
            if (this == obj) return true;             // same reference: shortcut
            if (obj == null) return false;            // avoids NullPointerException
            if (getClass() != obj.getClass()) return false; // avoids ClassCastException
            Student s = (Student) obj;
            return age == s.age && Objects.equals(name, s.name); // null-safe String compare
        }

        @Override
        public int hashCode() {
            // Manual version from the video:
            // int result = 17;
            // result = 31 * result + age;
            // result = 31 * result + (name == null ? 0 : name.hashCode());
            // return result;
            return Objects.hash(name, age);
        }
    }

    public static void main(String[] args) {
        Student s1 = new Student("Aditya", 28);
        Student s2 = new Student("Aditya", 28);
        Student s3 = null;

        System.out.println(s1 == s2);                       // false (different objects)
        System.out.println(s1.equals(s2));                  // true  (same values)
        System.out.println(s1.hashCode() == s2.hashCode()); // true  (contract holds)
        System.out.println(s1.equals(s3));                  // false (no NPE)
        System.out.println(s1.equals(Integer.valueOf(28))); // false (no ClassCastException)

        Set<Student> set = new HashSet<>();
        set.add(s1);
        set.add(s2);
        System.out.println(set.size());                     // 1 (duplicate detected)
        System.out.println(set.contains(new Student("Aditya", 28))); // true

        Student nullName = new Student(null, 28);
        System.out.println(nullName.hashCode() == new Student(null, 28).hashCode()); // true (no NPE)
    }
}
```

### What goes wrong if you override only `equals()`
```java
import java.util.HashSet;
import java.util.Set;

public class BrokenHashCodeDemo {
    static class Point {
        final int x, y;
        Point(int x, int y) { this.x = x; this.y = y; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Point p = (Point) o;
            return x == p.x && y == p.y;
        }
        // hashCode() NOT overridden -> identity hash codes, contract broken
    }

    public static void main(String[] args) {
        Point a = new Point(1, 2);
        Point b = new Point(1, 2);
        System.out.println(a.equals(b)); // true

        Set<Point> set = new HashSet<>();
        set.add(a);
        set.add(b);
        // Equal objects land in different buckets, so the "duplicate" is stored.
        // (Identity hashes could in theory collide, but in practice this prints 2.)
        System.out.println(set.size());  // 2
    }
}
```

### `getClass()` vs `instanceof`
```java
public class GetClassInstanceofDemo {
    static class Animal {}
    static class Dog extends Animal {}

    public static void main(String[] args) {
        Animal a = new Animal();
        Animal d = new Dog();   // reference type Animal, runtime type Dog

        System.out.println(a.getClass().getSimpleName()); // Animal
        System.out.println(d.getClass().getSimpleName()); // Dog
        System.out.println(a.getClass() == d.getClass()); // false

        System.out.println(a instanceof Animal); // true
        System.out.println(d instanceof Animal); // true  (subclass counts)
        System.out.println(a instanceof Dog);    // false (parent is not a child)
        System.out.println(d instanceof Object); // true  (everything is an Object)

        Object obj = d;          // Object reference can point to anything
        if (obj instanceof Dog dog) {             // pattern matching (Java 16+)
            System.out.println("It's a " + dog.getClass().getSimpleName()); // It's a Dog
        }
    }
}
```

### `clone()`: `Cloneable`, shallow copy, deep copy
```java
import java.util.Arrays;

public class CloneDemo {
    static class Student implements Cloneable {   // remove "implements Cloneable" -> CloneNotSupportedException
        String name;
        int age;
        int[] marks;
        Student(String name, int age, int[] marks) { this.name = name; this.age = age; this.marks = marks; }

        @Override
        public Student clone() throws CloneNotSupportedException {
            return (Student) super.clone();       // default: shallow copy
        }

        public Student deepClone() throws CloneNotSupportedException {
            Student copy = (Student) super.clone();
            copy.marks = marks.clone();           // copy the referenced array too
            return copy;
        }
    }

    static class Gadget {                         // NOT Cloneable
        Object tryClone() throws CloneNotSupportedException { return super.clone(); }
    }

    public static void main(String[] args) throws CloneNotSupportedException {
        Student s1 = new Student("Aditya", 28, new int[]{90, 80});
        Student s3 = s1.clone();
        System.out.println(s3.name + " " + s3.age);   // Aditya 28
        System.out.println(s1 == s3);                 // false (a new object)
        System.out.println(s1.marks == s3.marks);     // true  (shallow: array is shared)

        s3.marks[0] = 10;
        System.out.println(Arrays.toString(s1.marks)); // [10, 80]  (original affected!)

        Student s4 = s1.deepClone();
        s4.marks[1] = 0;
        System.out.println(Arrays.toString(s1.marks)); // [10, 80]  (original untouched)
        System.out.println(Arrays.toString(s4.marks)); // [10, 0]

        try {
            new Gadget().tryClone();
        } catch (CloneNotSupportedException e) {
            System.out.println("Caught " + e.getClass().getSimpleName()); // Caught CloneNotSupportedException
        }
    }
}
```

### Arrays are objects, primitives are not
```java
import java.util.Arrays;

public class ArraysAreObjectsDemo {
    public static void main(String[] args) {
        int[] a = {1, 2, 3};
        int[] b = {1, 2, 3};

        Object o = a;                             // an array IS an Object
        System.out.println(o instanceof Object);  // true
        System.out.println(a.getClass().getSimpleName()); // int[]
        System.out.println(a.equals(b));          // false (arrays don't override equals)
        System.out.println(Arrays.equals(a, b));  // true  (utility class compares contents)

        int x = 5, y = 5;
        System.out.println(x == y);               // true  (primitives: compare with ==)
        // x.equals(y) would not compile: int has no methods
        Integer wx = x;                           // wrapper class extends Object
        System.out.println(wx.equals(y));         // true
    }
}
```

## Common Pitfalls / Tips
- **`equals(Student s)` is an overload, not an override.** It won't be called by collections. Always use `equals(Object o)` with `@Override`, which lets the compiler catch the mistake.
- **Override `equals` and `hashCode` together, using the same fields.** If you override only `equals`, `HashSet`/`HashMap` quietly store duplicates and lookups fail.
- **Guard `equals` against `null` and foreign types.** Do the `this == o` shortcut first, then `o == null`, then `getClass() != o.getClass()` (or `instanceof`), then cast.
- **`getClass()` vs `instanceof` in `equals`.** `getClass()` is strict, so a subclass is never equal to its parent, and it keeps `equals` symmetric. `instanceof` allows subclasses but can break symmetry if a subclass adds fields. Pick one on purpose.
- **Compare String fields with `equals`, not `==`.** Use `Objects.equals(a, b)` when they may be `null`.
- **Guard `hashCode` against `null` fields**, or use `Objects.hash(...)`, which handles them.
- **Don't base `hashCode` on mutable fields of objects you put in a `HashSet`.** If a field changes after insertion, the object is stranded in the wrong bucket.
- **The default `toString()` hex isn't a memory address,** and it isn't stable across runs.
- **The default `clone()` is shallow.** Arrays and collections inside are shared, so deep-copy them yourself. Many developers avoid `clone()` and use copy constructors or static factory methods instead.
- **`Object` vs `Objects`, array vs `Arrays`.** The "s" versions are utility classes in `java.util`.
- **Don't use `finalize()`.** Use try-with-resources instead.
- **Records (Java 16+)** generate `equals`, `hashCode` and `toString` from their components: `record Student(String name, int age) {}`.

## Review Questions
1. Why does `Student` have a `toString()` method even if you never wrote one? What does the default print?
2. Two `new Student("Aditya", 28)` objects: what do `==` and the default `equals()` return, and why?
3. Why must the parameter of `equals` be `Object` and not `Student`?
4. Which three checks make an `equals` implementation safe, and what does each prevent?
5. State the equals/hashCode contract. Which direction is not guaranteed, and why?
6. What happens in a `HashSet` if a class overrides `equals` but not `hashCode`?
7. For `Animal d = new Dog();`, what do `d.getClass().getName()`, `d instanceof Animal` and `new Animal() instanceof Dog` give?
8. Why does `clone()` require `Cloneable`, what kind of interface is it, and what kind of copy does the default `clone()` make?

<details>
<summary>Answers</summary>

1. Every class extends `Object` (implicitly), and `Object` defines `toString()`. The default returns `ClassName@` followed by the hash code in hex, e.g. `Student@1b6d3586`.
2. Both return `false`. `new` creates two separate heap objects, and the default `equals` is `this == obj`, which compares references, not field values.
3. `Object.equals(Object)` is the method being overridden. A `Student` parameter creates a different (overloaded) method, which collections never call, and `@Override` flags it as an error.
4. `this == obj` returns true early (an optimization). `obj == null` returns false, which prevents a `NullPointerException` when reading fields. `getClass() != obj.getClass()` returns false, which prevents a `ClassCastException` on the cast.
5. If `a.equals(b)`, then `a.hashCode() == b.hashCode()`. The reverse isn't guaranteed: different objects may collide on the same hash code, because an `int` can't uniquely represent every possible object.
6. Equal objects get different (identity) hash codes, land in different buckets, and are both stored. The set then contains "duplicates", and `contains(new Student(...))` returns false.
7. `Dog` (the runtime class), `true` (a Dog is an Animal), and `false` (an Animal is not necessarily a Dog).
8. Java makes cloning opt-in because some objects (DB connections, threads) shouldn't be copied. `Cloneable` is a **marker interface** with no methods, and `Object.clone()` throws `CloneNotSupportedException` if it's missing. The default is a **shallow** copy: reference fields are shared.
</details>

## Further Study
- *Effective Java*, items 10–13 (`equals`, `hashCode`, `toString`, `clone`)
- `java.util.Objects`: `equals`, `hash`, `requireNonNull`, `toString`
- Records (Java 16+), which auto-generate `equals`/`hashCode`/`toString`
- `HashMap` internals: buckets, collisions, treeification (#29 and later videos)
- Copy constructors vs `clone()`, deep vs shallow copies
- `Comparable`/`Comparator` (ordering vs equality)
- `Class<?>` and reflection basics
- `wait`/`notify`/`notifyAll` and the monitor concept (multithreading)
- `Cleaner` and try-with-resources as replacements for `finalize()`

---

[← #21](21-immutable-classes-in-java.md) · [Index](../../ReadMe.md) · [#23 →](23-java-enums-why-not-constants.md)
