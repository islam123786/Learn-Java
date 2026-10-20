# Immutable Classes in Java | How to Create Immutable Objects | Java Full Course #21
> Source: https://youtu.be/vgQVz5OdFws · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 35:04. The video is in Hindi and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They're rougher than usual, within about 1–2 minutes.

## TL;DR
- An **immutable object** can't change after it's created: neither its state (fields) nor its behavior (methods, which could otherwise be changed by overriding in a subclass).
- The basic rules:
  1. Make the **class `final`**, so nobody can subclass it and override its behavior.
  2. Make every field **`private final`**.
  3. Provide **no setters**, and set everything once in the **constructor**.
  4. Getters are fine.
- These rules are enough when the fields are primitives or immutable types like `String`. They are **not enough when a field refers to a mutable object** (e.g. `College`). A getter that returns that reference lets callers change the "immutable" object's internals (`s1.getCollege().name = "IITB"`).
- The fix: either make the referenced class immutable as well, or make **defensive copies**, i.e. copy the mutable object **in the constructor** and **in the getter**, so the internal object is never shared. Copying into brand-new objects is the idea behind a **deep copy**. Sharing the reference is what goes wrong with a **shallow copy**.
- Why bother? Immutable objects are inherently **thread-safe** (no race conditions on their state), are easy to reason about, and are a favourite interview topic.

## Key Concepts

**Immutable object.** Once constructed, none of its observable state can change, and its behavior can't be altered. *Why it matters:* values that never change can be shared freely between methods and threads.

**Mutable object.** It has setters, or accessible fields, or can be subclassed so its methods get overridden. The usual `Student` with getters and setters is mutable.

**`final` class.** It can't be extended ("The type CSStudent cannot subclass the final class Student"). That stops a subclass from overriding methods to change behavior.

**`private final` fields.** `private` blocks direct access (`s1.name = ...`). `final` means the field is assigned exactly once, in the constructor.

**Defensive copy.** Making a private copy of a mutable object when it comes *into* your class (constructor parameter) and when it goes *out* (getter return value). Outside code then never holds a reference to your internal state.

**Shallow vs deep copy.** A shallow copy duplicates the outer object but **shares** the inner referenced objects, so changes made through one show up in the other. A deep copy duplicates the referenced objects too, so the copies are fully independent.

## Detailed Notes

### What makes an object immutable [00:40]
- An object has instance variables, such as `int i = 4, j = 5`, and methods, such as `fun()`. It's immutable if **neither** can ever change after it's created.
- This matters a lot in Java, especially once you get to threads.

### A typical mutable class [02:10]
- `Student` has `age` and `name`, a parameterized constructor, and getters and setters.
- With `Student s1 = new Student(28, "Aditya")`, a call like `s1.setName("Rohit")` changes the **state**.
- A `class ComputerScienceStudent extends Student` can override `markAttendance()`, which changes the **behavior**.

### Rules for an immutable class [05:15]
Thinking it through from first principles:
1. **Mark the class `final`**, so no one can inherit it and override its behavior. *(The video says `final` stops a class from being "overridden". The precise meaning is that it can't be **subclassed**.)*
2. **Mark the instance variables `private` and `final`**: no direct access, and assigned only once.
3. **No setters.** Initialize everything through the **constructor**. A `final` field can be assigned only once, and the constructor is where that happens.
4. **Getters are fine.** They only read.

### Demo: a simple immutable `Student` [07:40]
- `final class Student` with `private final int age`, `private final String name`, a constructor, `getAge()` and `getName()`.
- `new Student(28, "Aditya")`, and then printing `getName()` and `getAge()` shows "Aditya 28".
- There is no way to change anything: no setter, no direct field access. `class CSStudent extends Student` fails with "cannot subclass the final class Student".
- Adding `private final String college` with a getter keeps the class immutable [11:15], because **`String` is itself immutable** (why that is comes in the Strings video).

### Where the rules break: a mutable field [12:15]
- A college has more than a name (an address, grades, …), so it becomes its own class: `class College { String name; String address; }`. Its fields are package-private, so it's **mutable**.
- `Student` now has `private final College college` and a `getCollege()` that returns `this.college`.
- `College c = new College("IITG", "Assam"); Student s1 = new Student(28, "Aditya", c);`. Printing `s1.getCollege().name` gives "IITG".
- Then `s1.getCollege().name = "IITB";` and printing again gives **"IITB"**. The student's internal state changed even though there's no setter. The class is **not purely immutable**.

### Why it happened: references in memory [17:30]
- `c` (on the stack) points to a `College` object on the heap. `s1` points to a `Student` object, and its `college` field holds **the same address** as `c`.
- `s1.getCollege()` returns that address, held in a temporary reference. `.name = "IITB"` follows the address and changes the one shared `College` object.
- The video calls this a **shallow copy**: you "copied" the college, but only the reference, not the object.
- *Note:* there's actually a second leak. The **caller still has `c`**, so `c.name = "IITB"` would also change the student's college.

### Two ways to fix it [22:00]
1. **Make `College` immutable too**: a `final` class, `private final` fields, no setters. A class whose fields are all primitives or immutable types (like `String`) is then immutable as a whole.
   - *(The video says primitives are safe "because they live on the stack". Primitive fields actually live inside the object on the heap. They're safe because they're **copied by value**: there's no reference to leak.)*
2. **If you can't change `College`** (it comes from a library, say), make **defensive copies** in two places:
   - **Constructor**: `this.college = new College(college.name, college.address);`. Don't keep the caller's object.
   - **Getter**: `return new College(college.name, college.address);`. Never hand out your own object.

### Demo: defensive copy [23:50]
- With both copies in place, `s1.getCollege().name = "IITB"` changes only a throwaway copy. Printing the student's college **still shows "IITG"** both times. The class is now properly immutable.

### What happens in memory with defensive copies [27:20]
- `new College("IITG", "Assam")` creates object #1, referenced by `c`.
- `new Student(..., c)` runs the constructor, which builds **object #2**, a new `College` with the same values. The student's `college` field points to #2, not #1.
- Each `getCollege()` call creates **another** object (#3, #4, …) and returns it. Whatever the caller changes, #2 is never touched.
- Rule of thumb: **never share the reference to an internal mutable object**, either on the way in or on the way out. Giving out a reference gives out access to the real thing.
- Creating fresh objects instead of sharing references is the idea behind a **deep copy**. *(Strictly, a full deep copy also copies any mutable objects nested inside `College`. Here `College` holds only `String`s, so one level of copying is enough.)*

### Why immutable objects matter [33:20]
- With **threads**, several threads may run the same code at once, which can cause **race conditions** on shared state.
- Immutable objects promise that their state can never change, so there's nothing to race on. (More on this in the threads videos.)

## Code Examples

Each block is a complete program. Save it under the class's name and run it with `java ClassName.java`.

### Simple immutable class (primitives + String)
```java
public class ImmutableBasics {
    public static void main(String[] args) {
        Student s1 = new Student(28, "Aditya", "IIT Guwahati");
        System.out.println(s1.getName() + " " + s1.getAge() + " " + s1.getCollege());
        // Aditya 28 IIT Guwahati
        // s1.name = "Rohit";      // error: name has private access
        // s1.setName("Rohit");    // error: no such method
    }
}

final class Student {                       // 1. final class: no subclass can change behavior
    private final int age;                  // 2. private final fields
    private final String name;
    private final String college;           //    String is immutable, so this is safe

    Student(int age, String name, String college) {   // 3. set once in the constructor
        this.age = age;
        this.name = name;
        this.college = college;
    }

    public int getAge()        { return age; }        // 4. getters only, no setters
    public String getName()    { return name; }
    public String getCollege() { return college; }
}
```

### The leak: a mutable field returned by reference
```java
public class LeakDemo {
    public static void main(String[] args) {
        College c = new College("IITG", "Assam");
        LeakyStudent s1 = new LeakyStudent(28, "Aditya", c);
        System.out.println(s1.getCollege().name);   // IITG

        s1.getCollege().name = "IITB";              // no setter needed...
        System.out.println(s1.getCollege().name);   // IITB   <- internal state changed!

        c.name = "IITD";                            // the caller's original reference leaks too
        System.out.println(s1.getCollege().name);   // IITD
    }
}

class College {                     // mutable
    String name;
    String address;
    College(String name, String address) { this.name = name; this.address = address; }
}

final class LeakyStudent {
    private final int age;
    private final String name;
    private final College college;

    LeakyStudent(int age, String name, College college) {
        this.age = age;
        this.name = name;
        this.college = college;      // stores the caller's reference
    }
    public College getCollege() { return college; }   // hands out the internal reference
}
```

### The fix: defensive copies in constructor and getter
```java
public class DefensiveCopyDemo {
    public static void main(String[] args) {
        College c = new College("IITG", "Assam");
        SafeStudent s1 = new SafeStudent(28, "Aditya", c);
        System.out.println(s1.getCollege().name);   // IITG

        s1.getCollege().name = "IITB";              // modifies a throwaway copy
        c.name = "IITD";                            // modifies the caller's object only
        System.out.println(s1.getCollege().name);   // IITG
        System.out.println(s1.getCollege() == s1.getCollege()); // false (new object each call)
    }
}

class College {                     // still mutable (imagine it's from a library)
    String name;
    String address;
    College(String name, String address) { this.name = name; this.address = address; }
}

final class SafeStudent {
    private final int age;
    private final String name;
    private final College college;

    SafeStudent(int age, String name, College college) {
        this.age = age;
        this.name = name;
        this.college = new College(college.name, college.address);  // copy on the way in
    }
    public int getAge() { return age; }
    public String getName() { return name; }
    public College getCollege() {
        return new College(college.name, college.address);          // copy on the way out
    }
}
```

### Collections need defensive copies too (and records)
```java
import java.util.ArrayList;
import java.util.List;

public class ImmutableCollectionsDemo {
    public static void main(String[] args) {
        List<String> names = new ArrayList<>(List.of("Aditya", "Rohit"));
        Course course = new Course("Java", names);
        names.add("Rohan");                           // caller's list changes...
        System.out.println(course.getStudents());     // [Aditya, Rohit]  ...course doesn't

        try {
            course.getStudents().add("Hacker");
        } catch (UnsupportedOperationException e) {
            System.out.println("cannot modify");      // cannot modify
        }

        Point p = new Point(3, 4);
        System.out.println(p + " " + p.x());          // Point[x=3, y=4] 3
    }
}

final class Course {
    private final String title;
    private final List<String> students;

    Course(String title, List<String> students) {
        this.title = title;
        this.students = List.copyOf(students);   // unmodifiable copy on the way in
    }
    public List<String> getStudents() { return students; } // already unmodifiable: safe to return
    public String getTitle() { return title; }
}

record Point(int x, int y) {}                     // records: final class, private final fields, no setters
```

### Subclassing a final class doesn't compile
```java
// does not compile

public class FinalClassError {
    public static void main(String[] args) {
        Student s = new Student("Aditya");
        s.name = "Rohit";               // error: name has private access / is final
    }
}

final class Student {
    private final String name;
    Student(String name) { this.name = name; }
    public String getName() { return name; }
}

class CSStudent extends Student {      // error: cannot inherit from final Student
    CSStudent() { super("x"); }
}
```

## Common Pitfalls / Tips
- **`final` on a reference field doesn't make the object immutable.** `private final College college` stops you from reassigning `college`, but `college.name` can still change. `final` is about the variable, not the object it points to.
- **Copy on both sides.** Copying only in the getter still leaves the caller's original reference (passed to the constructor) pointing at your internal state, and the reverse is just as bad.
- **Collections and arrays are mutable.** For `List`, `Map` and arrays, copy them in (`List.copyOf`, `array.clone()`) and either return unmodifiable views or copies.
- **`Collections.unmodifiableList(list)` is only a *view*.** If you keep the original list somewhere, changes to it show through. `List.copyOf` makes a real copy.
- **Make copies before validating** (copy first, then check the copy). Otherwise another thread could change the argument between the check and the copy (a TOCTOU bug).
- **Prefer records (Java 16+)** for simple immutable data carriers. Defensive copying still applies to mutable components.
- **Immutable "modification" means returning a new object**, e.g. `withName(String n)` returns a new `Student`. That's how `String`, `LocalDate` and `BigDecimal` work.
- **An alternative to `final class`** is a private constructor plus a static factory method. That also prevents subclassing.

## Review Questions
1. What does it mean for an object to be immutable, in terms of both state and behavior?
2. List the four basic rules for writing an immutable class, and say what each one prevents.
3. Why is a class with `private final String name` still immutable, but one with `private final College college` possibly not?
4. Walk through what happens in memory when `s1.getCollege().name = "IITB"` changes the student's college.
5. What is a defensive copy, and where exactly must you make one?
6. How do shallow and deep copies differ, and which one does defensive copying give you?
7. Besides changing `College`'s fields through the getter, what other way can the original (leaky) `Student` be mutated from outside?
8. Why are immutable objects useful in multithreaded programs?

<details>
<summary>Answers</summary>

1. After construction, none of its fields can change (state), and its methods can't be altered (behavior). Behavior could otherwise change through a subclass that overrides them.
2. A `final` class prevents subclassing and overriding of behavior. `private` fields prevent direct outside access. `final` fields allow only one assignment. No setters means there's no method that changes state after construction. Getters, which only read, are allowed.
3. `String` is immutable, so even though callers get a reference to it, they can't change it. `College` is mutable, and if the getter returns the internal reference, callers can change its fields.
4. The student's `college` field and the caller's `c` hold the same heap address. `getCollege()` returns that address in a temporary reference, and `.name = "IITB"` writes through it into the single shared `College` object, so the student "sees" IITB.
5. A private copy of a mutable object, made so that no outside code shares your internal object. Make one in the constructor (copy the incoming argument) and in the getter (return a new copy, not your field).
6. A shallow copy duplicates the top-level object but shares the referenced objects. A deep copy also duplicates the referenced objects, so nothing is shared. Defensive copying aims for deep-copy independence. For nested mutable objects, you must copy each level.
7. Through the original reference passed to the constructor: the caller still holds `c`, so `c.name = "IITB"` changes the student's college too. That's why the constructor must copy as well.
8. Their state can't change after construction, so threads can share them without locks and without race conditions on that state.
</details>

## Further Study
- Why `String` is immutable (string pool, security, hashing): the upcoming Strings deep-dive
- `final` keyword in depth: variables vs references vs methods vs classes
- Shallow vs deep copy: copy constructors, `clone()` and its pitfalls
- `List.copyOf`, `Map.copyOf`, `Collections.unmodifiableList` (copy vs view)
- Java records and compact canonical constructors (validation plus defensive copies)
- Immutable value types in the JDK: `LocalDate`, `BigDecimal`, `Integer`
- Thread safety, race conditions, and the Java Memory Model (`final` field semantics)
- Builder pattern for constructing immutable objects with many fields
- *Effective Java*, Item 17: "Minimize mutability"

---

[← #20](20-java-io-deep-dive.md) · [Index](ReadMe.md) · [#22 →](22-java-object-class-equals-hashcode.md)
