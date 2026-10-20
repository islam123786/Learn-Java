# Static & Final in Java | Static Methods, Blocks & String[] args | Java Full Course #15
> Source: https://youtu.be/Lyg4ZcrjUBw · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 45:28. The video is in Hindi, and its transcript had no timing data, so the timestamps below are rough estimates based on the relative position of each part in the transcript text. They may be off by a couple of minutes.

## TL;DR
- **`static`** moves a member from the object level to the **class level**. A `static` field (a **class variable**) exists **once** and is shared by every object. Change it through any object and every object sees the change. Access it as `ClassName.field`.
- **`static` methods** belong to the class and run without an object. So they can't use `this`, and they can't touch instance fields or instance methods directly (there's no object to use).
- A **`static { }` block** initializes static fields. It runs **once, when the class is loaded**, before any object of the class is created.
- **`final`** on a variable makes it a **constant**: it can be assigned exactly once. Constants are named in `UPPER_SNAKE_CASE`, and **`static final`** is the usual combination (one shared, unchangeable value, e.g. `PI`).
- `main` is `static` so the JVM can call it without creating an object. `String[] args` holds the **command-line arguments** (`java Demo5 Aditya Tandon` gives `args = {"Aditya", "Tandon"}`).

## Key Concepts

**Static (class) variable.** A field marked `static`. It exists once per class, not once per object. *Why it matters:* it saves memory and keeps shared data (such as the college every student attends) consistent.

**Instance variable.** A normal field. Every object has its own copy. *Why it matters:* this is per-object state such as name and roll number.

**Static method.** A method marked `static`, called as `ClassName.method()`. There is no current object. *Why it matters:* it suits utility logic and entry points (`main`, `Math.max`).

**Rules for static methods.** They can't use `this`, and they can't directly access instance fields or call instance methods. They *can* use static members, and they can use instance members **through an object reference** they have. *Why it matters:* it explains the compile error "Cannot make a static reference to the non-static method…".

**Static block.** `static { ... }` runs once when the class is initialized. *Why it matters:* it lets you initialize static fields with logic, the way a constructor initializes instance fields.

**`final` variable.** It can be assigned only once. A field can be assigned at its declaration or in every constructor (or, for `static final`, in a static block). A local can be assigned at its declaration or later, but only once. *Why it matters:* it gives you real constants and guards against accidental reassignment.

**`final` elsewhere.** A `final` method can't be overridden, a `final` class can't be extended, and a `final` parameter can't be reassigned. *Why it matters:* these come up again with inheritance and polymorphism.

**Why `main` is static.** The JVM needs to start the program without an object. *Why it matters:* it's a classic interview question, and it's why helper methods in the `main` class have been `static` so far.

**`String[] args`.** Command-line arguments passed after the class name. *Why it matters:* configuration and file names are passed in this way (Spring Boot uses it too).

## Detailed Notes

### Motivation: the same college in every object [00:45]
- An app for IIT Guwahati: every `Student` object has `name`, `age`, `rollNumber`, `college`, and `college` is identical in all of them.
- Storing the same 4-byte reference in thousands of objects is waste. Better to store it **once** in a common place and have every object share it.

### `static` fields = class variables [05:00]
- `static String college;` means the field isn't part of any object. It belongs to the **class**.
- Heap objects now contain only `name`, `age`, `rollNumber`, and every object shares the single `college`.
- Naming: per-object fields are **instance variables**, and a `static` field is a **class variable**.
- *Correction:* the video says static variables "are not stored in the heap" and leaves where they go for a later video. In HotSpot since Java 8, static fields are stored **in the heap**, together with the class's `java.lang.Class` object. Class *metadata* (method code etc.) lives in **Metaspace**. What matters is that there is **one copy per class**, not one per object.

### Accessing static fields [06:30]
- `s1.college = "IIT Guwahati";` works (every object can reach the shared field), but the compiler or IDE warns: *"The static field Student.college should be accessed in a static way."*
- Preferred: `Student.college = "IIT Guwahati";`, which is `ClassName.staticVariable`.
- `s3.college = "IIT Kharagpur";` changes it for **everyone**, so `s1.college` also prints IIT Kharagpur.

### `static` methods [09:30]
- `static void markAttendance()` belongs to the class, and all objects share one method. You can call it as `s1.markAttendance()`, `s2.markAttendance()` or (preferred) `Student.markAttendance()`.
- Side note from the video: methods, static or not, are **never** stored inside objects. Objects hold only their instance fields, and method code is stored once per class.

### Rules for static methods [12:00]
1. **A static method can call only static methods** (directly). If `Student.markAttendance()` called a non-static `print()`, *whose* `print()` would run? You can call a static method with no object at all.
2. **A static method can access only static variables** (directly). Instance fields don't exist until an object is created, and even then there could be many.
3. **A static method has no `this`.** `this` is the current object, and there isn't one.
- *Precision:* rules 1 and 2 are about *implicit* access. A static method can still use instance members **through an explicit reference**, e.g. `static void show(Student s) { s.print(); }` or creating `new Student()` inside it. That's exactly what `main` does all the time.

### Code demo [16:30]
- `static String college;` in `Student`. Assigning `this.college = college` in the constructor triggers the "should be accessed in a static way" warning, and `college = college` has no effect. So `college` is dropped from the constructor.
- `Student.college = "IIT Guwahati";` in `main`, then printing both students shows IIT Guwahati for both. Writing `Student.college` instead of `s1.college` removes the warning.

### Static blocks [19:30]
- `static { college = "IIT Guwahati"; grade = 8; }` initializes static fields. It's to static fields what a constructor is to instance fields.
- You can also initialize at the declaration: `static String college = "IIT Guwahati";`.
- **Runs once, when the class is loaded**, before any object is created.
  - *Precision:* the class is initialized on its **first active use** (first `new`, first static method call, or first access to a static field), not necessarily when the program starts.

### What can't be static [23:00]
- **Method parameters and local variables can't be `static`.** They're locals in the stack frame and have nothing to do with objects or the class. `void print(static String name)` is a compile error.
- **A top-level class can't be `static`.** It belongs to a package, and there's nothing for it to be "static" relative to. Only a **nested** class can be `static` (covered later).

### `final` variables: constants [25:30]
- `class Random { final double PI = 3.14; }`. Nobody can later write `r1.PI = 3.16`. That's a compile error: *"The final field Random.PI cannot be assigned"*.
- `final` makes a variable a **constant**.

### Naming constants [28:00]
- Constants are written in all capitals, with words separated by underscores: `PI`, `PI_VALUE`, `MAX_RETRY_COUNT`.

### `final` everywhere [29:00]
- Variable → constant. Parameter → can't be reassigned inside the method (`void print(final String name)`). Method → can't be overridden (covered with polymorphism). Class → can't be subclassed (covered with inheritance).

### `final` in code: when can you assign? [30:00]
- At the declaration: `final double PI = 3.14;`.
- **Blank final field** assigned in the constructor: `final double PI;` plus `Random() { this.PI = 3.14; }`. That's allowed, because the object's fields are created and the constructor runs exactly once per object.
- Local: `final int x = 4;`, or `final int x;` followed by `x = 4;`. Both are allowed. A second assignment `x = 5;` is an error: *"The final local variable x may already have been assigned."*
- *Correction:* the video first says you can't split the declaration and the assignment of a `final`, and then shows that it works. The real rule is **definite assignment**: a `final` must be assigned **exactly once** before use. A blank `final` field must be assigned in every constructor, and a `static final` at its declaration or in a static block.

### Why `main` is `static` [34:00]
- If `main` weren't static, the JVM would have to create an object first (`Demo4 d = new Demo4(); d.main(...)`). Making it static lets the JVM simply call `Demo4.main(args)`.
- That's also why the helper methods we wrote in the `main` class had to be `static`. Calling a non-static `print()` from `main` gives *"Cannot make a static reference to the non-static method print()"*.
- *Version note:* Java 21 offered "instance main methods" as a **preview**, and Java 25 finalized them (JEP 512, compact source files). There a launcher can run a non-static `void main()` by creating the object itself. The classic `public static void main(String[] args)` is still the standard form and works everywhere.

### `static final` together [37:00]
- `static final double PI;` can't be assigned in the constructor (it's not per-object). The errors are *"The blank final field PI may not have been initialized"* and *"should be accessed in a static way"*.
- Assign it at the declaration or in a `static { PI = 3.14; }` block.
- It's common for real constants: one shared copy that can never change.

### `String[] args`: command-line arguments [39:30]
- Old-school run: `javac Demo5.java` then `java Demo5`.
- A program that prints `args.length` and each `args[i]`:
  - `java Demo5` → `Number of arguments are 0`
  - `java Demo5 Aditya Tandon Rohit Negi` → 4 arguments, at indexes 0..3.
- Used to pass configuration such as input/output file names (`java Demo5 input.txt output.txt`). Frameworks like Spring Boot read these arguments too.
- With the single-file launcher you can also pass them: `java Demo5.java Aditya Tandon`.

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java FileName.java`.

### Static field shared by all objects
```java
public class StaticFieldDemo {
    public static void main(String[] args) {
        Collegian.college = "IIT Guwahati";          // ClassName.staticField (preferred)
        Collegian s1 = new Collegian("Aditya", 28, 1001);
        Collegian s2 = new Collegian("Rohit", 28, 102);
        s1.print(); // Aditya, 28, 1001, IIT Guwahati
        s2.print(); // Rohit, 28, 102, IIT Guwahati

        Collegian.college = "IIT Kharagpur";         // one change, visible to everyone
        s1.print(); // Aditya, 28, 1001, IIT Kharagpur
        s2.print(); // Rohit, 28, 102, IIT Kharagpur

        System.out.println(Collegian.count);        // 2 (a static counter of created objects)
    }
}

class Collegian {
    String name;          // instance variables: one per object
    int age;
    int rollNumber;
    static String college; // class variable: one per class
    static int count;

    Collegian(String name, int age, int rollNumber) {
        this.name = name;
        this.age = age;
        this.rollNumber = rollNumber;
        count++;           // instance code may use static fields
    }

    void print() {
        System.out.println(name + ", " + age + ", " + rollNumber + ", " + college);
    }
}
```

### Static block runs once, before any object
```java
public class StaticBlockDemo {
    public static void main(String[] args) {
        System.out.println("main starts");    // main starts
        Campus c1 = new Campus();             // static block runs
                                              // constructor runs
        Campus c2 = new Campus();             // constructor runs
        System.out.println(Campus.college + " " + Campus.grade); // IIT Guwahati 8
    }
}

class Campus {
    static String college;
    static int grade;

    static {
        college = "IIT Guwahati";
        grade = 8;
        System.out.println("static block runs");
    }

    Campus() {
        System.out.println("constructor runs");
    }
}
```

### Static method rules
```java
public class StaticMethodRules {
    int instanceCount = 5;
    static int staticCount = 10;

    void instanceHello() {
        System.out.println("instance hello, count = " + instanceCount);
    }

    static void staticHello() {
        System.out.println("static hello, count = " + staticCount); // static -> static: OK
        // instanceHello();           // error: non-static method from static context
        // System.out.println(this);  // error: no 'this' in a static method
        StaticMethodRules obj = new StaticMethodRules();
        obj.instanceHello();          // OK through an explicit object reference
    }

    public static void main(String[] args) {
        staticHello();
        // static hello, count = 10
        // instance hello, count = 5
        StaticMethodRules.staticHello(); // ClassName.method() works too
        // static hello, count = 10
        // instance hello, count = 5
    }
}
```

### Calling a non-static method from `main`
```java
// does not compile
public class NonStaticFromMain {
    void print() { System.out.println("hi"); }

    public static void main(String[] args) {
        print(); // error: non-static method print() cannot be referenced from a static context
    }
}
```

### `final` variables and constants
```java
public class FinalDemo {
    static final double PI;           // static final, assigned in a static block
    static {
        PI = 3.14;
    }

    public static void main(String[] args) {
        Circle c = new Circle(2);
        System.out.println(c.area());   // 12.56
        System.out.println(Circle.MAX_RADIUS); // 100

        final int x;                    // blank final local
        x = 4;                          // first (and only) assignment is fine
        // x = 5;                       // error: variable x might already have been assigned
        System.out.println(x);          // 4
        System.out.println(PI);         // 3.14
    }
}

class Circle {
    static final int MAX_RADIUS = 100; // UPPER_SNAKE_CASE for constants
    final double radius;               // blank final field...

    Circle(double radius) {
        this.radius = radius;          // ...assigned exactly once, in the constructor
    }

    double area() {
        return FinalDemo.PI * radius * radius;
    }
}
```

### Reassigning a `final` field
```java
// does not compile
public class FinalReassign {
    public static void main(String[] args) {
        Constants r1 = new Constants();
        r1.PI = 3.16; // error: cannot assign a value to final variable PI
    }
}

class Constants {
    final double PI = 3.14;
}
```

### `String[] args`
```java
public class ArgsDemo {
    static void printArgs(String[] args) {
        System.out.println("Number of arguments are " + args.length);
        for (int i = 0; i < args.length; i++) {
            System.out.println("Argument " + i + " = " + args[i]);
        }
    }

    public static void main(String[] args) {
        printArgs(args);
        // with `java ArgsDemo.java` (no arguments):
        // Number of arguments are 0

        // simulate `java ArgsDemo.java Aditya Tandon Rohit Negi`
        printArgs(new String[]{"Aditya", "Tandon", "Rohit", "Negi"});
        // Number of arguments are 4
        // Argument 0 = Aditya
        // Argument 1 = Tandon
        // Argument 2 = Rohit
        // Argument 3 = Negi
    }
}
```

## Common Pitfalls / Tips
- **Access static members through the class name** (`Student.college`). Writing `s1.college` compiles, but it suggests the value is per-object.
- **One object changing a static field changes it for all of them.** Don't make something `static` just to avoid passing it around.
- **"Static methods can't use instance members" means *without an object*.** Pass or create an object and you can call its instance methods.
- **No `static` locals or parameters, and no `static` top-level classes.** Only nested classes can be `static`.
- **A `final` reference isn't an immutable object.** `final int[] a = {1};` stops `a = …`, but `a[0] = 9` is still allowed.
- **A blank `final` field must be assigned in every constructor path.** A `static final` must be assigned at the declaration or in a static block, never in a constructor.
- **Static blocks run once, at class initialization** (first use), in source order along with the static field initializers.
- **`main` can be overloaded**, but the JVM only calls `public static void main(String[] args)` (or the newer instance-main forms in Java 25).

## Review Questions
1. Why make `college` `static` in a single-college app, and what's the term for such a field?
2. Name two ways to access a static field. Which one is preferred, and why?
3. What are the three rules for static methods, and why does each one make sense?
4. When does a static block run, and what is it used for?
5. Can a method parameter or a top-level class be `static`? Why or why not?
6. Where can a `final` instance field be assigned? A `static final` field? A `final` local?
7. Why is `main` static, and why did helper methods in the `main` class need to be static?
8. What does `String[] args` contain for `java Demo5 Aditya Tandon Rohit Negi`?

<details>
<summary>Answers</summary>

1. Every student has the same college, so keeping one shared copy saves memory and keeps it consistent. It's a class variable (as opposed to an instance variable).
2. `ClassName.field` and `object.field`. `ClassName.field` is preferred because it makes clear the value belongs to the class, and the compiler warns about the other form.
3. A static method can directly call only static methods, can directly access only static variables, and has no `this`. A static method can run without any object, so there's no current object whose instance fields, instance methods or `this` could be used. (Through an explicit object reference, instance members are fine.)
4. Once, when the class is initialized (on first use), before any object is created. It initializes static fields, possibly with logic.
5. No. Parameters and locals live in the method's stack frame and have nothing to do with the class level. A top-level class belongs to a package, so `static` doesn't mean anything there. Only nested classes can be static.
6. A `final` instance field: at its declaration or in every constructor (or an instance initializer). A `static final` field: at its declaration or in a static block. A `final` local: at its declaration or later, exactly once before use.
7. So that the JVM can call it as `ClassName.main(args)` without creating an object. Since `main` is static, it can only call other methods of its class directly if they're static too.
8. `{"Aditya", "Tandon", "Rohit", "Negi"}`, so `args.length == 4` with indexes 0 to 3.
</details>

## Further Study
- Class loading and initialization order (static fields, static blocks, instance initializers, constructors)
- Static nested classes vs inner classes
- `final` methods and classes (inheritance, polymorphism)
- Immutability and `final` fields; why `String` is immutable
- Utility classes (`Math`, `Collections`) and private constructors
- Singleton pattern (a common use of `static`)
- JEP 512: compact source files and instance main methods (Java 25)
- Parsing command-line arguments (e.g. picocli); Spring Boot application arguments

---

[← #14](14-objects-size-call-by-value-copy.md) · [Index](ReadMe.md) · [#16 →](16-encapsulation-inheritance-packages-super.md)
