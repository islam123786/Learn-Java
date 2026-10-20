# Constructors in Java | Constructor Chaining, Overloading & this | Java Full Course #13
> Source: https://youtu.be/RADpqk79IzQ · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 58:02. The video is in Hindi, and its transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- **Instance variables** (fields) get **default values** (`0`, `0.0`, `false`, `null`). **Local variables** don't, and using one before assigning it is a compile error.
- A **constructor** initializes an object while it's being created. It has the **same name as the class** and **no return type (not even `void`)**, it runs as part of `new ClassName(...)`, and it can be **overloaded**.
- If you write **no** constructor, Java adds an empty no-arg **default constructor**. As soon as you write **any** constructor, that default is gone, so `new Student()` stops compiling unless you add a no-arg constructor yourself.
- `this` refers to the **current object**. Use `this.name = name` when a parameter has the same name as a field, and `this(...)` to call another constructor of the same class (**constructor chaining**). `this(...)` must be the **first statement**.
- You can't call a constructor like a normal method. It runs only through `new` or via `this(...)` (and `super(...)`, covered later). `new` can fail at runtime if the heap is full.

## Key Concepts

**Instance variable vs local variable.** Fields declared in a class are instance variables. They live inside each object on the heap and get default values. Variables declared in a method are local. They live in the method's stack frame, have no default value, and must be assigned before use. *Why it matters:* it explains both the `null 0 0 null` output and the "may not have been initialized" error.

**Instance method.** A method declared in the class (non-static) and called on an object. *Why it matters:* it's the vocabulary used for the rest of the course.

**Default values.** `int`/`long`/`short`/`byte` → `0`, `float`/`double` → `0.0`, `boolean` → `false`, `char` → `'\u0000'`, any reference type (e.g. `String`) → `null`. `null` means "refers to no object". *Why it matters:* forgetting to set a field gives you a silent default instead of an error.

**Constructor.** A special method-like block that initializes a new object. Its rules: same name as the class, no return type, invoked during object creation, used to initialize, can be overloaded. *Why it matters:* it makes it impossible to forget a required field.

**Default constructor.** The no-arg, empty constructor the compiler adds **only** when a class declares no constructors. *Why it matters:* it explains why `new Student()` works with no constructor written, and why it breaks once you add one.

**Parameterized constructor.** A constructor that takes arguments: `new Student("Aditya", 28, 1001, "IIT Guwahati")`. *Why it matters:* it lets the caller supply values at creation time and makes them mandatory.

**Constructor overloading.** Several constructors with different parameter lists. *Why it matters:* callers get several valid ways to build an object.

**`this` keyword.** A reference to the current object. `this.field` picks the field over a same-named parameter, and `this(...)` calls a sibling constructor. *Why it matters:* it lets you reuse the natural parameter names and avoid duplicated code.

**Constructor chaining.** Constructors delegating to one another with `this(...)`, so the real assignment logic is written once. *Why it matters:* there's a single place to change when you add a field or a default.

## Detailed Notes

### Printing an object you never filled in [00:00]
- `Student s1 = new Student();` followed by printing `s1.name`, `s1.age`, `s1.rollNumber`, `s1.college` gives no error. It prints `null 0 0 null`. These are the **default values**.

### Instance variables, instance methods, local variables [03:20]
- Fields in a class (data/characteristics) are **instance variables**. Methods in a class (behaviors) are **instance methods**, or just methods.
- `int x = 4;` inside `main` (or any method) is a **local variable** [04:01]. Its scope ends with the method, and it lives on the **stack**.
- Instance variables live inside the object on the **heap**.

### Default values [05:20]
- The defaults are listed above (integers `0`, floating-point `0.0`, `boolean` `false`, references `null`).
- `String` is a non-primitive. A `String` field is a reference variable, so its default is `null` ("nothing"), not `""`.

### Local variables have no default [07:40]
- `int x; System.out.println(x);` gives the compile error *"The local variable x may not have been initialized"*.
- Why the difference? The video's reasoning is that Java skips defaults for short-lived locals as an optimization and forces you to assign them.
- *More precisely:* the compiler's **definite assignment** rule forces locals to be assigned before they're read. This catches bugs at compile time. Heap objects are zero-filled when allocated, so fields always start with a well-defined default.

### Why constructors? [11:25]
- Setting `s1.name`, `s1.age`, … one line at a time is tedious, and it's easy to forget a field (say, a `grades` field someone else added), which would then silently keep its default.
- A **constructor** puts values into the object *while* it's being constructed.

### Rules of a constructor [13:05]
1. Same name as the class.
2. No return type, **not even `void`**.
3. Called during object creation.
4. Used to initialize an object.
5. Can be overloaded.
- Syntax: `Student() { name = "Aditya"; age = 28; rollNumber = 1001; college = "IIT Guwahati"; }`. Now every `new Student()` starts with those values.

### `new Student()` is a constructor call [17:03]
- "Called automatically" is really "called by your own syntax": the `Student()` after `new` *is* the call.
- So in `Student s1 = new Student();`: `Student` is the type, `s1` is the reference variable, `new` allocates heap memory, and `Student()` runs the constructor to fill the fields. Then the reference is stored in `s1` on the stack.

### The default constructor [19:46]
- When you didn't write a constructor, `new Student()` still worked. Java requires every class to have a constructor, so the compiler inserts an empty `Student() {}`. The fields then keep their default values.
- *Terminology:* strictly, "default constructor" means only the compiler-generated one. A no-arg constructor you write yourself (even an empty one) is called a **no-arg constructor**. The video uses "default constructor" for both.

### Parameterized constructor [23:19]
- Hard-coding "Aditya" in the constructor isn't useful. You want the caller to decide, and to be *forced* to supply the values.
- `Student(String n, int a, int rn, String c) { name = n; age = a; rollNumber = rn; college = c; }`, called as `new Student("Aditya", 28, 1001, "IIT Guwahati")`.
- (The whiteboard has `int c` for the college. That's a slip. It's a `String`.)
- Now `new Student()` fails with *"The constructor Student() is undefined"*, because **the compiler adds the default constructor only when you've written none**.

### Constructor overloading [27:22]
- Fix it by also declaring `Student() {}`. Now both `new Student()` and `new Student("Rohit", 28, 102, "IIT Guwahati")` compile, just like method overloading.
- Code demo [28:23]: a hand-written no-arg constructor with hard-coded values, then an empty one (equivalent to the compiler's), then the parameterized one printing Rohit's details.

### The `this` keyword [32:13]
- `this` refers to the **current object**, the one being constructed or the one the method was called on.
- With parameters named like the fields (`String name, int age, …`), plain `name = name;` assigns the parameter to itself. The IDE warns *"The assignment to variable name has no effect"*.
- `this.name = name;` means "the field `name` of this object = the parameter `name`". It works just like `s1.name`, but from inside the object.
- `this.` is optional when the names differ (`name = n`), but the convention is to use the same names with `this.`, because it reads clearly.

### Constructor overloading, at scale [36:14]
- New file `Demo3` [36:48]: five constructors taking 0, 1 (`name`), 2 (`name, age`), 3 (`+ rollNumber`) and 4 (`+ college`) arguments.
- Objects `s1` to `s5` each use a different one. Any field not passed keeps its default (`null`/`0`).

### Constructor chaining with `this(...)` [42:51]
- Every constructor repeats assignments. Instead, let each one **call the full constructor** with `this(...)` and pass defaults for the missing pieces:
  - `Student(String name) { this(name, 0, 0, "Unknown"); }`, and so on.
- The no-arg one becomes `this("Unknown", 0, 0, "Unknown")`. Using `"Unknown"` instead of `null` avoids a later `NullPointerException`.
- In an IDE, Cmd/Ctrl + click on `this(...)` jumps to the constructor it resolves to.

### Chaining step by step [48:06]
- Alternative style: each constructor calls the **next larger** one. 0-arg → `this("Unknown")` → `this(name, 0)` → `this(name, age, 0)` → `this(name, age, rollNumber, "Unknown")` → the 4-arg one assigns everything.
- Print statements show the flow: `new Student()` prints **fifth, fourth, third, second, first**. Each constructor jumps to the next before running its own print, much like recursion unwinding.

### `this(...)` must be the first statement [51:13]
- Putting the print above `this(...)` is a compile error. The call to another constructor must be the first line of the constructor body.
- *Version note:* this holds up to Java 24 (and on the Java 21 used to test these notes). Java 25 finalized "flexible constructor bodies" (JEP 513), which allows statements before `this(...)`/`super(...)` as long as they don't touch the object being built.

### Interview questions [55:30]
- **Can you call a constructor manually, like a method?** No. A constructor runs only when an object is created with `new`, or from another constructor through `this(...)` (or `super(...)` for a parent class).
- **Can `new` fail?** Yes [57:10]. Allocation happens at runtime, and if the heap has no room the JVM throws an error.
  - *Correction:* the video calls this a "runtime exception". It's actually `java.lang.OutOfMemoryError`, an `Error` and not a `RuntimeException`, and normally you don't catch it.

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java FileName.java`.

### Default values of fields
```java
public class DefaultValuesDemo {
    public static void main(String[] args) {
        Defaults d = new Defaults(); // compiler-generated default constructor
        System.out.println(d.i + " " + d.l + " " + d.dbl + " " + d.b + " " + d.s); // 0 0 0.0 false null
        System.out.println((int) d.c); // 0  (char default is '\u0000')
    }
}

class Defaults {
    int i;
    long l;
    double dbl;
    boolean b;
    String s;
    char c;
}
```

### Local variables have no default
```java
// does not compile
public class LocalNoDefault {
    public static void main(String[] args) {
        int x;
        System.out.println(x); // error: variable x might not have been initialized
    }
}
```

### Writing a constructor removes the default one
```java
// does not compile
public class NoDefaultAnymore {
    public static void main(String[] args) {
        Learner ok = new Learner("Aditya");
        Learner bad = new Learner(); // error: constructor Learner() is undefined
    }
}

class Learner {
    String name;
    Learner(String name) { this.name = name; }
}
```

### No-arg, parameterized and overloaded constructors with `this`
```java
public class ConstructorsDemo {
    public static void main(String[] args) {
        Scholar s1 = new Scholar();                                // no-arg constructor
        Scholar s2 = new Scholar("Rohit", 28, 102, "IIT Guwahati"); // parameterized
        s1.print(); // Aditya, 28, 1001, IIT Guwahati
        s2.print(); // Rohit, 28, 102, IIT Guwahati
    }
}

class Scholar {
    String name;
    int age;
    int rollNumber;
    String college;

    Scholar() { // our own no-arg constructor with hard-coded values
        name = "Aditya";
        age = 28;
        rollNumber = 1001;
        college = "IIT Guwahati";
    }

    Scholar(String name, int age, int rollNumber, String college) {
        this.name = name;          // field = parameter
        this.age = age;
        this.rollNumber = rollNumber;
        this.college = college;
    }

    void print() {
        System.out.println(name + ", " + age + ", " + rollNumber + ", " + college);
    }
}
```

### Forgetting `this` silently does nothing
```java
public class ForgotThis {
    public static void main(String[] args) {
        Item it = new Item("Pen");
        System.out.println(it.name); // null (the assignment had no effect)
    }
}

class Item {
    String name;
    Item(String name) {
        name = name; // assigns the parameter to itself; the field stays null
    }
}
```

### Constructor chaining, step by step (prints the call order)
```java
public class ChainingDemo {
    public static void main(String[] args) {
        Pupil13 s1 = new Pupil13();
        // I am in fifth constructor
        // I am in fourth constructor
        // I am in third constructor
        // I am in second constructor
        // I am in first constructor
        System.out.println(s1.name + " " + s1.age + " " + s1.rollNumber + " " + s1.college);
        // Unknown 0 0 Unknown

        Pupil13 s3 = new Pupil13("Rohit", 28);
        // I am in fifth constructor
        // I am in fourth constructor
        // I am in third constructor
        System.out.println(s3.rollNumber + " " + s3.college); // 0 Unknown
    }
}

class Pupil13 {
    String name;
    int age;
    int rollNumber;
    String college;

    Pupil13() {
        this("Unknown");                       // must be the first statement
        System.out.println("I am in first constructor");
    }

    Pupil13(String name) {
        this(name, 0);
        System.out.println("I am in second constructor");
    }

    Pupil13(String name, int age) {
        this(name, age, 0);
        System.out.println("I am in third constructor");
    }

    Pupil13(String name, int age, int rollNumber) {
        this(name, age, rollNumber, "Unknown");
        System.out.println("I am in fourth constructor");
    }

    Pupil13(String name, int age, int rollNumber, String college) {
        this.name = name;
        this.age = age;
        this.rollNumber = rollNumber;
        this.college = college;
        System.out.println("I am in fifth constructor");
    }
}
```

### `this(...)` must come first
```java
// does not compile
public class ThisNotFirst {
    public static void main(String[] args) { }
}

class Box {
    int w, h;
    Box() {
        System.out.println("before"); // error (before Java 25): call to this must be first statement
        this(1, 1);
    }
    Box(int w, int h) { this.w = w; this.h = h; }
}
```

## Common Pitfalls / Tips
- **Don't give a constructor a return type.** `void Student() {}` compiles, but it's an ordinary *method* called `Student`, not a constructor, so `new Student(...)` won't use it.
- **Adding a constructor removes the free no-arg one.** If other code (or a framework) needs `new X()`, declare `X() {}` explicitly.
- **`name = name;` is a no-op.** Use `this.name = name;`. Treat the IDE's "has no effect" warning as a bug.
- **`this(...)`/`super(...)` go on the first line** (until Java 25), and only one of them per constructor.
- **Chain toward one "full" constructor** so the field assignments live in a single place.
- **Prefer meaningful defaults to `null`** (`"Unknown"`, empty collections) to avoid `NullPointerException`s later.
- **Local variables must be assigned before use.** Fields get defaults, locals don't.
- **You can't call a constructor directly** (`s1.Student()` is not a thing). To "reset" an object, write a normal method or create a new object.

## Review Questions
1. What gets printed for an unassigned `int`, `double`, `boolean` and `String` field? What happens if you print an unassigned local `int`?
2. List the five rules of a constructor.
3. In `Student s1 = new Student();`, what does each part do?
4. When does Java add a default constructor, and when does it stop adding one?
5. Why does `name = name;` inside a constructor not work, and how does `this` fix it?
6. What is constructor chaining, and what rule applies to `this(...)`?
7. In the step-by-step chaining example, why is "fifth" printed first?
8. Can you call a constructor manually? Can `new` fail?

<details>
<summary>Answers</summary>

1. `0`, `0.0`, `false`, `null`. An unassigned local `int` is a compile error ("might not have been initialized"), because locals have no default value.
2. Same name as the class. No return type (not even `void`). Invoked during object creation. Used to initialize the object. Can be overloaded.
3. `Student` is the declared type, `s1` is the reference variable (on the stack), `new` allocates heap memory at runtime, and `Student()` calls the constructor that initializes the fields. The resulting reference is assigned to `s1`.
4. Only when the class declares no constructors at all. Once you write any constructor (no-arg or parameterized), the compiler doesn't add one.
5. Both sides refer to the parameter, which shadows the field, so the field is never set. `this.name` explicitly means the current object's field: `this.name = name;`.
6. One constructor calling another of the same class with `this(...)`, so that the initialization logic lives in one place. The `this(...)` call must be the first statement (before Java 25), and a constructor can contain only one such call.
7. Each constructor's first statement is `this(...)`, so control jumps all the way to the 4-arg constructor before any print runs. Then the prints execute as each call returns: fifth, fourth, third, second, first.
8. No. A constructor runs only via `new` or from another constructor through `this(...)`/`super(...)`. And yes, allocation can fail at runtime with `OutOfMemoryError` if the heap is exhausted.
</details>

## Further Study
- `super(...)` and constructor chaining across inheritance
- Instance initializer blocks and static blocks, and the order of initialization
- Copy constructors (next video)
- `NullPointerException` and `Optional`
- Builder pattern (alternative to many overloaded constructors)
- JEP 513: flexible constructor bodies (Java 25)
- Java records: compact canonical constructors

---

[← #12](12-oop-classes-objects-new-keyword.md) · [Index](../../ReadMe.md) · [#14 →](14-objects-size-call-by-value-copy.md)
