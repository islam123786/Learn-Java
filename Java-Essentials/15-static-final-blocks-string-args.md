# 15 · `static` & `final` · Static Methods, Blocks & `String[] args`
> Full notes: [15-static-final-blocks-string-args.md](../15-static-final-blocks-string-args.md) · [▶ Video](https://youtu.be/Lyg4ZcrjUBw)

## Why static? The same college in every object
**The situation:** an app for IIT Guwahati. Every `Student` object has `name`, `age`, `rollNumber` and `college`, and **`college` is identical in all of them**.

**Without `static`:** every object stores its **own copy** of the same value.
```
Student @1 { name="Aditya", age=28, roll=1001, college ──► "IIT Guwahati" }
Student @2 { name="Rohit",  age=27, roll=1002, college ──► "IIT Guwahati" }
Student @3 { name="Arun",   age=22, roll=1003, college ──► "IIT Guwahati" }
... × 10,000 students = 10,000 copies of the same reference
```
- **It wastes memory:** the same 4-byte reference is stored in thousands of objects.
- **It's hard to change:** renaming the college means updating **every** object.
- **It's error-prone:** one object could end up with a different college by mistake.

**With `static`:** store the value **once**, in the **class**, and let every object **share** it.
```
                     Student (class)
                     └── static college = "IIT Guwahati"   ← ONE copy, shared
                              ▲     ▲     ▲
Student @1 { name, age, roll }│     │     │
Student @2 { name, age, roll }┘     │     │
Student @3 { name, age, roll }──────┘─────┘   (objects no longer contain college)
```

## `static` fields = class variables
- `static String college;` means the field **isn't part of any object**. It belongs to the **class**.
- **Instance variable:** one copy **per object**. **Class (static) variable:** **one copy per class**.
- **Where it lives:** in HotSpot (Java 8+), static fields are stored **in the heap**, together with the class's `Class` object. Class metadata (method code, etc.) is in **Metaspace**. The video says "not in the heap", which isn't accurate. What matters is that there's **one copy**.

## Accessing static fields
- `s1.college = "IIT Guwahati";` works, but you get a warning: *"The static field Student.college should be accessed in a static way."*
- **Preferred:** `Student.college = "IIT Guwahati";` (`ClassName.staticVariable`).
- **Changing it through any object changes it for everyone:** after `s3.college = "IIT Kharagpur";`, `s1.college` also prints IIT Kharagpur.

## Code demo: a static field
**The first attempt** keeps `college` in the constructor:
```java
Student(String name, int age, int rollNumber, String college) {
    this.name = name; this.age = age; this.rollNumber = rollNumber;
    this.college = college;   // ⚠️ warning: "should be accessed in a static way"
    // writing  college = college;  instead would do nothing (the parameter is assigned to itself)
}
```
- A static field isn't per-object, so setting it in a per-object constructor is the wrong place. It would also **overwrite the shared value** every time a new student is created.
- **The fix:** remove `college` from the constructor and set it **once**, through the class.

```java
public class StaticFieldDemo {
    public static void main(String[] args) {
        Student.college = "IIT Guwahati";               // set ONCE, through the class name

        Student s1 = new Student("Aditya", 28, 1001);
        Student s2 = new Student("Rohit", 27, 1002);
        s1.print();     // Aditya 28 1001 IIT Guwahati
        s2.print();     // Rohit 27 1002 IIT Guwahati

        s2.college = "IIT Kharagpur";                   // ⚠️ works (with a warning), but changes it for EVERYONE
        s1.print();     // Aditya 28 1001 IIT Kharagpur  ← s1 sees the change too

        Student.college = "IIT Guwahati";               // ✅ the preferred way: ClassName.field
        System.out.println(Student.count);              // 2   ← one shared counter
    }
}

class Student {
    String name;                // instance variables: one copy per object
    int age;
    int rollNumber;
    static String college;      // class variable: ONE copy, shared by all objects
    static int count = 0;       // a common real use: counting objects

    Student(String name, int age, int rollNumber) {
        this.name = name;
        this.age = age;
        this.rollNumber = rollNumber;
        count++;                // every constructor call updates the SAME counter
    }

    void print() {              // instance methods CAN read static fields
        System.out.println(name + " " + age + " " + rollNumber + " " + college);
    }
}
```
**What's happening:**
- `Student.college = …` is written **once**, and **every** object sees it, because there's only one `college`.
- Changing it through `s2` changed it for `s1` too. That proves it's **shared**, not copied into each object.
- `count++` in the constructor shows another common use: a **shared counter** of how many objects have been created.
- **Instance methods can freely read static fields.** It's static methods that have the restrictions (below).

---

## Static methods (brief)
- `static void markAttendance()` belongs to the **class**. Call it as `Student.markAttendance()` (preferred) or through any object.
- Method code is **never stored inside objects**, static or not. It's stored once per class.

**The 3 rules for static methods:**
1. They can **only call static methods** directly.
2. They can **only use static variables** directly.
3. They have **no `this`**, because there's no current object.

**Why:** a static method can run **with no object at all**. So which object's `name` or `print()` would it use?

```java
public class StaticRules {
    int instanceField = 5;
    static int staticField = 10;

    void instanceMethod() { System.out.println("instance method"); }
    static void staticMethod() { System.out.println("static method"); }

    static void demo() {
        staticMethod();                         // ✅ static → static
        System.out.println(staticField);        // ✅ static field
        // instanceMethod();                    // ❌ "Cannot make a static reference to the non-static method"
        // System.out.println(instanceField);   // ❌ same error, for the field
        // System.out.println(this);            // ❌ no 'this' in a static context

        StaticRules obj = new StaticRules();    // ✅ but THROUGH an explicit object, it's fine
        obj.instanceMethod();
        System.out.println(obj.instanceField);
    }

    public static void main(String[] args) {
        demo();
        // static method
        // 10
        // instance method
        // 5
    }
}
```
- The rules are about **direct (implicit) access**. Through an **explicit reference** (`obj.instanceMethod()`), it's fine. That's exactly what `main` does.

## Static blocks (brief)
```java
class Student {
    static String college;
    static int grade;
    static {                       // runs ONCE, when the class is first used
        college = "IIT Guwahati";
        grade = 8;
        System.out.println("static block ran");
    }
}
```
- It's like a constructor, but for **static fields**. You can also just initialize at the declaration: `static String college = "IIT Guwahati";`
- It runs **once**, when the class is **first actively used** (the first `new`, static method call or static field access), before any object is created.

## What can't be static (brief)
- **Local variables and method parameters:** they live in the stack frame and have nothing to do with the class. `void print(static String name)` is a compile error.
- **Top-level classes:** only **nested** classes can be `static` (#19).

---

## `final` variables: constants
```java
class Random {
    final double PI = 3.14;
}
r1.PI = 3.16;      // ❌ "The final field Random.PI cannot be assigned"
```
- `final` makes a variable a **constant**: it can be assigned only once.
- **Naming:** use ALL_CAPS with underscores: `PI`, `PI_VALUE`, `MAX_RETRY_COUNT`.

## `final` everywhere (brief)
| `final` on | Meaning |
| :--- | :--- |
| variable | a constant (assigned once) |
| parameter | can't be reassigned inside the method: `void print(final String name)` |
| method | can't be overridden (#17) |
| class | can't be extended (#17) |

## `final` in code: when can you assign?
**The rule (definite assignment):** a `final` must be assigned **exactly once** before it's used. Where that one assignment can happen depends on the kind of variable:

| Kind | Where you can assign it | Example |
| :--- | :--- | :--- |
| `final` **field** | ① at the declaration, **or** ② in **every** constructor (a **blank final**) | `final double PI = 3.14;` or `final double PI;` + `Random() { PI = 3.14; }` |
| `final` **local** | at the declaration, **or** later, but only once | `final int x = 4;` or `final int x; x = 4;` |
| `static final` | ① at the declaration, **or** ② in a **`static` block**, but **never in a constructor** | `static final double PI = 3.14;` or `static { PI = 3.14; }` |

```java
public class FinalAssignDemo {
    final double PI = 3.14;              // ① final field, assigned at the declaration
    final int id;                        // ② BLANK final field: no value yet...
    static final double E;               // ③ blank static final...
    static {
        E = 2.718;                       //    ...assigned in a static block
    }

    FinalAssignDemo(int id) {
        this.id = id;                    //    ...assigned in the constructor (once per object)
        // this.id = 99;                 // ❌ "variable id might already have been assigned"
    }

    FinalAssignDemo() {
        this(0);                         // every constructor must assign id (here, by chaining)
    }

    public static void main(String[] args) {
        final int x = 4;                 // final local, assigned at the declaration
        final int y;                     // final local, declared now...
        y = 5;                           // ...assigned later (only once)
        // y = 6;                        // ❌ "The final local variable y may already have been assigned"

        FinalAssignDemo a = new FinalAssignDemo(7);
        FinalAssignDemo b = new FinalAssignDemo();
        System.out.println(a.PI + " " + a.id + " " + b.id + " " + E + " " + x + " " + y);
        // 3.14 7 0 2.718 4 5
        // a.id = 8;                     // ❌ "cannot assign a value to final variable id"
    }
}
```
**What's going on:**
- **A blank final field** (`final int id;`) is allowed, because the constructor runs **exactly once per object**, so each object gets its own value assigned once. Two objects can have **different** final values (7 and 0).
- **If any constructor forgets** to assign it, you get a compile error: *"The blank final field id may not have been initialized."*
- **A `static final` can't be assigned in a constructor.** It's not per-object, and a constructor runs many times. Assign it at the declaration or in a `static` block.
- **A `final` local** can be split into a declaration and a later assignment. The video first says you can't, then shows that you can. Only a **second** assignment is an error.

## `static final` together (brief)
- **`static final`** means **one shared copy** that **never changes**. This is the usual way to write real constants:
  ```java
  public static final double PI = 3.14159;
  public static final int MAX_STUDENTS = 60;
  ```
- If you put `static final double PI;` and try to set it in a constructor, you get *"The blank final field PI may not have been initialized"*.

---

## Why `main` is `static` (brief)
- If `main` weren't static, the JVM would need to **create an object first**. Because it's static, the JVM just calls `Demo.main(args)`.
- That's why helper methods in the `main` class had to be `static`. Calling a non-static `print()` from `main` gives *"Cannot make a static reference to the non-static method print()"*.
- **Version note:** Java 25 finalized **instance main methods** (JEP 512), which allow a plain `void main()`. The classic `public static void main(String[] args)` is still the standard form.

## `String[] args`: command-line arguments (brief)
```java
public class ArgsDemo {
    public static void main(String[] args) {
        System.out.println("Number of arguments are " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println(i + ": " + args[i]);
        }
    }
}
```
```bash
java ArgsDemo.java                         # Number of arguments are 0
java ArgsDemo.java Aditya Tandon Rohit     # Number of arguments are 3
                                           # 0: Aditya
                                           # 1: Tandon
                                           # 2: Rohit
```
- Words after the class name go into `args`, separated by spaces.
- **Used for:** configuration, such as file names (`java App input.txt output.txt`). Frameworks like Spring Boot read them too.

**Remember:** **`static`** means **one copy per class**, shared by every object. Access it as `ClassName.member`. Static methods can't directly use instance members or `this`. A **static block** runs once, when the class is first used. **`final`** means it's assigned **exactly once**: a field at the declaration or in every constructor, a local at any point (but only once), and a `static final` at the declaration or in a static block.
