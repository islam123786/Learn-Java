# AutoBoxing, Abstract classes & POJOs | Why only one public class in java file | Java Full Course #18
> Source: https://youtu.be/crVwHNW-fX8 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 72:01. The video is in Hindi and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A `.java` file may contain **at most one `public` top-level class**, and that class must be **named after the file**. The video explains this as a way to help the JVM find `main`. The more accurate reason is that it lets the compiler and tools find a public class's source from its name (see the correction below).
- Every primitive has a **wrapper class** (`int` → `Integer`, `char` → `Character`, …). Wrappers exist because many APIs, especially the Collection Framework, work only with objects. Primitives stay because of their legacy and because they are much **faster and lighter**.
- **Autoboxing** (`Integer y = x;`) is compiled to `Integer.valueOf(x)`. **Unboxing** (`int y = obj;`) is compiled to `obj.intValue()`. Both happen in assignments, method calls and arithmetic. Unboxing `null` throws a `NullPointerException`.
- On wrappers, `==` compares **references**, not values. Use `.equals()` or compare the `intValue()`s. `Integer.valueOf` **caches -128 to 127**, which is why `==` "works" for 100 and fails for 200.
- An abstract class **can** have constructors, static members, private methods (non-abstract), final methods (non-abstract), and even zero abstract methods. It **cannot** be `final`.
- A **POJO** (Plain Old Java Object) is a simple class with fields, constructors and getters/setters and no framework dependencies. An **anemic** model has only data. A **rich domain** model also has business logic.

## Key Concepts

**One public class per file.** The compiler error is "The public type Sample must be defined in its own file". Any number of *non-public* classes can sit in the same file. *Why it matters:* it's a common interview question, and it explains how your files must be organized.

**Wrapper classes.** `Byte`, `Short`, `Integer`, `Long`, `Float`, `Double`, `Character`, `Boolean`. Each is an object holding one primitive value, plus utility methods. *Why it matters:* `ArrayList<int>` is illegal and `ArrayList<Integer>` is required.

**Autoboxing / unboxing.** The compiler converts between primitive and wrapper automatically. Boxing is primitive → object ("put 10 in a box"). Unboxing is object → primitive. *Why it matters:* it's convenient, but it hides object creation, `null` risks and `==` surprises.

**`Integer.valueOf` and the Integer cache.** `valueOf` is a `static` factory that returns a **cached** `Integer` for values in -128 to 127, and a new object otherwise. The `new Integer(…)` constructor is deprecated (since Java 9, marked for removal later). *Why it matters:* it explains the `==` puzzle and why you should never call the constructor.

**`==` vs `equals`.** For primitives, `==` compares the values stored in the variables. For objects, the variable *is* a reference, so `==` compares addresses. `equals` compares contents. *Why it matters:* comparing wrappers or Strings with `==` is a classic bug.

**Abstract class rules.** It can't be instantiated and it's meant to be extended. Each rule in the interview Q&A follows from that one idea.

**POJO, anemic model, rich domain model.** A POJO is a plain class with no framework base classes or annotations required. It is also called a *model* class (for example, it maps to a database table in Spring). Anemic means only fields, constructors and getters/setters. Rich means the object also holds behavior such as `markAttendance()`.

## Detailed Notes

### Why only one public class per file, named like the file [00:41]
- Putting `public class Sample` inside `Demo.java` fails with "The public type Sample must be defined in its own file". Removing `public` makes the error go away, so many non-public classes are fine. Renaming the class to `Sample` makes the IDE rename the file to match.
- The video's reasoning [02:44]:
  - The JVM starts the program at `main`. `main` is **`static`** so the JVM can call `Demo.main(...)` without creating an object, and **`public`** so code outside the package (the JVM) can reach it.
  - If several classes in one file could be public, the JVM wouldn't know which class's `main` to call. If the public class is named after the file, the JVM can load `Demo` straight from `Demo.java`.
  - For an application with many files [06:51]: only the file you actually run (`javac Demo.java`, then `java Demo`) has to contain `main`. Everything else is imported from there.
- *Correction:* the JVM doesn't decide this by looking at public classes. You always tell it which class to run (`java Demo`), and that class doesn't even have to be public. Only `main` must be `public static`. The rule really exists for the **compiler and build tools**: when other code refers to the public class `com.x.Demo`, `javac` can find its source at `com/x/Demo.java` without scanning every file. The JLS lets compilers enforce it, and `javac` does. *(Since Java 11, `java Demo.java` runs a single source file directly, using the first class declared in it.)*

### Primitives vs wrapper classes [08:55]
- Data types are either primitives (`int`, `long`, `short`, `byte`, `float`, `double`, `char`, `boolean`) or non-primitives (objects and user-defined types).
- Java gives every primitive a **wrapper class**. Most have the same name capitalized; the exceptions are `int → Integer` and `char → Character`.
- Memory picture:
  - `int x = 10;` is a container on the stack that holds 10.
  - `Integer x = new Integer(10);` is a reference on the stack pointing to a heap object that holds the value 10.
- Conceptually, `Integer` is a class with a private `int value` field, constructors, and methods.

### Why wrappers exist, and why primitives still exist [13:43]
- **Wrappers:** much of Java (the Collection Framework's `ArrayList`, `LinkedList`, …) works only with objects. As an OO language, Java also wants an object form of every primitive, with useful methods.
- **Primitives** remain for two reasons [14:24]:
  1. **Legacy.** They kept the language familiar to C/C++ programmers.
  2. **Speed.** A primitive is just a value. An object needs a heap allocation, a header and padding, plus a reference. Use primitives unless you need objects.

### Autoboxing [16:28]
- `int x = 10; Integer y = x;` works without any cast. That conversion is **autoboxing**.
- What the compiler actually emits:
  - In old Java this was conceptually `new Integer(x)`.
  - In modern Java it is `Integer.valueOf(x)` [18:31], which is faster because of caching.
- Because it's called as `Integer.valueOf`, it must be a **`static`** method: `public static Integer valueOf(int x)`.

### Unboxing [21:16]
- `Integer x = 10; int y = x;` is compiled to `int y = x.intValue();`.
- `intValue()` is an **instance** method because it's called on the object, and it acts as a getter for the private `value` field.

### Demo, and where boxing/unboxing happen [24:01]
- `new Integer(20)` shows the deprecation warning ("The constructor Integer(int) has been deprecated since version 9 and marked for removal"). Prefer `Integer.valueOf(20)`, or simply `Integer a = 20;`.
- `System.out.println(y)` for an `Integer y` prints the value. *(The video calls this unboxing. Strictly, `println(Object)` calls `toString()`, so no unboxing happens there.)*
- Contexts where conversions happen automatically [30:11]:
  1. **Assignments**: `Integer y = x;` / `int b = a;`
  2. **Method calls**: passing an `Integer` to `printInteger(int x)` unboxes it. Passing an `int` to a parameter of type `Integer` boxes it.
  3. **Arithmetic**: `Integer a = 10, b = 20; int sum = a + b;` unboxes both operands (`a.intValue() + b.intValue()`) and prints 30.

### Unboxing `null` → NullPointerException [33:37]
- `Integer x = null;` is legal because objects can be null. Primitives can't hold null.
- `int y = x;` compiles, but at run time it becomes `x.intValue()` on `null`, which throws a **`NullPointerException`** ("Cannot invoke Integer.intValue() because x is null").

### `==` on wrappers [35:41]
- `int x = 10000, y = 10000; x == y` is `true`.
- `Integer a = 200, b = 200; a == b` is **`false`** [37:44].
  - Each line becomes `Integer.valueOf(200)`, which creates two separate heap objects at different addresses (say 1001 and 2001).
  - `==` compares what's stored in the variables. For references that's the addresses, which differ.
- `==` doesn't "change behavior" between primitives and objects. It always compares the variables' contents. A primitive variable contains the value. A reference variable contains an address.
- To compare values:
  - `a.intValue() == b.intValue()` → `true`
  - `a.equals(b)` → `true` [43:14]. `equals` is actually declared in `Object` (the parent of every class), and `Integer` overrides it.

### A conceptual `Integer` class [45:17]
- `public final class Integer`. It's `final` so nobody can subclass it, and `public` so it's usable everywhere.
- It has a `private final int value`, a (deprecated) constructor, `public int intValue()`, `public static Integer valueOf(int)` (with caching), and `equals` (inherited from `Object` and overridden).
- The real class also extends `Number`, implements `Comparable<Integer>`, and holds an internal cache array. You'll be able to read JDK sources like this by the end of the series.

### The Integer cache [48:43]
- `Integer x = 100, y = 100; x == y` is **`true`**.
- `valueOf` keeps pre-built `Integer` objects for **-128 to 127** (the `byte` range, where the most commonly used numbers fall). For a value in that range it returns the cached reference instead of creating a new object, so both variables point to the same object.
- That's why you use `valueOf` (or autoboxing) and avoid the deprecated constructor.
- *Correction:* the video says the result "depends on the compiler". In fact, the Java Language Specification **guarantees** caching for -128 to 127 when autoboxing. Larger values *may* also be cached (`-XX:AutoBoxCacheMax`), so never rely on `==` either way.

### Abstract classes, interview Q&A [53:32]
Recap: an abstract class can't be instantiated directly, can contain abstract and normal methods, and is meant to be extended. With `abstract class Animal { abstract void makeSound(); void sleep() {...} }` and `class Dog extends Animal`:

| Question | Answer | Reason |
|---|---|---|
| Constructors? [56:58] | **Yes** | They run when a *subclass* object is created (`Dog(String name) { super(name); }`). |
| Can it be `final`? [59:01] | **No** | `final` means it can't be inherited, but abstract means it must be. The compiler says "The class Animal can be either abstract or final, not both". |
| Static methods/fields/blocks? [60:23] | **Yes** | They belong to the class, not to objects. A `static abstract` method is illegal, though. |
| Private methods? [61:05] | **Yes, if non-abstract** | `private abstract` is illegal because the subclass couldn't see the method to implement it. |
| Final methods? [62:27] | **Yes, if non-abstract** | `final` forbids overriding and `abstract` requires it, so the two contradict each other. |
| Zero abstract methods? [63:49] | **Yes** | Useful when you want to stop direct instantiation but give every method a default implementation. |

- *Note:* abstract methods may also be package-private (no modifier). Only `private` is forbidden.

### POJO classes [65:12]
- **POJO** stands for **Plain Old Java Object**. It's a simple class that doesn't depend on any external framework (such as Spring). It has fields, constructors and getters/setters.
- Purpose: to **model** a real-world entity (a `Student` with name, age, roll number, college). They're also called **model classes**. In Spring, such a class often maps to a SQL table, with one field per column.
- The definition is loose. Some people add a Builder (from the Builder design pattern) or a little business logic.
- **Anemic model vs rich domain model** [70:00]. This is only an architectural distinction; both are POJOs.
  - **Anemic**: only fields, constructors, getters and setters.
  - **Rich domain**: also has business logic, such as `markAttendance()`.
- *Note:* in Domain-Driven Design (Martin Fowler), the anemic domain model is often called an anti-pattern, because the logic ends up in separate "service" classes. Which to use is a design choice.

## Code Examples

Each block is a complete program. Save it under the class's name and run it with `java ClassName.java`.

### One public class per file (non-public helpers are fine)
```java
public class OnePublicClass {      // must match the file name OnePublicClass.java
    public static void main(String[] args) {
        System.out.println(Helper.greet());   // Hello from Helper
        System.out.println(Sample.twice(21)); // 42
    }
}

class Helper {                     // not public: allowed alongside the public class
    static String greet() { return "Hello from Helper"; }
}

class Sample {                     // also not public
    static int twice(int x) { return 2 * x; }
}
```

### Autoboxing and unboxing in assignments, method calls and arithmetic
```java
public class BoxingDemo {
    static void printInteger(int x) { System.out.println("int param: " + x); }
    static void printBoxed(Integer x) { System.out.println("Integer param: " + x); }

    public static void main(String[] args) {
        int x = 10;
        Integer y = x;                 // autoboxing  -> Integer.valueOf(x)
        int z = y;                     // unboxing    -> y.intValue()
        System.out.println(y + " " + z);   // 10 10

        Integer fifty = 50;
        printInteger(fifty);           // int param: 50      (unboxed)
        printBoxed(60);                // Integer param: 60  (boxed)

        Integer a = 10, b = 20;
        int sum = a + b;               // a.intValue() + b.intValue()
        System.out.println(sum);       // 30
    }
}
```

### `==` vs `equals` and the Integer cache
```java
public class IntegerCacheDemo {
    public static void main(String[] args) {
        int p = 10000, q = 10000;
        System.out.println(p == q);                         // true  (values)

        Integer a = 200, b = 200;
        System.out.println(a == b);                         // false (different objects)
        System.out.println(a.equals(b));                    // true
        System.out.println(a.intValue() == b.intValue());   // true

        Integer c = 100, d = 100;
        System.out.println(c == d);                         // true  (both from cache -128..127)

        Integer e = Integer.valueOf(127), f = Integer.valueOf(127);
        System.out.println(e == f);                         // true
    }
}
```

### Unboxing null throws
```java
// throws
public class UnboxNull {
    public static void main(String[] args) {
        Integer x = null;   // fine: objects can be null
        int y = x;          // compiles, but x.intValue() on null -> NullPointerException
        System.out.println(y);
    }
}
```

### What an abstract class may contain
```java
public class AbstractRulesDemo {
    public static void main(String[] args) {
        Animal a = new Dog("Bruno");
        a.intro();          // [Bruno] says:
                            // Barking
        a.sleep();          // Bruno is sleeping
        Animal.describe();  // Kingdom: Animalia

        NoAbstractMethods n = new NoAbstractMethods() {}; // must subclass (anonymous) to instantiate
        n.hello();          // default hello
    }
}

abstract class Animal {
    protected String name;
    static String kingdom = "Animalia";           // static field: OK

    Animal(String name) { this.name = name; }      // constructor: OK

    abstract void makeSound();                     // abstract (not private/final/static)

    final void sleep() { System.out.println(name + " is sleeping"); } // final, non-abstract: OK
    private String tag() { return "[" + name + "]"; }                 // private, non-abstract: OK
    static void describe() { System.out.println("Kingdom: " + kingdom); } // static: OK

    void intro() { System.out.println(tag() + " says:"); makeSound(); }
}

abstract class NoAbstractMethods {               // abstract with zero abstract methods: OK
    void hello() { System.out.println("default hello"); }
}

class Dog extends Animal {
    Dog(String name) { super(name); }             // calls the abstract class constructor
    @Override void makeSound() { System.out.println("Barking"); }
}
```

### Illegal abstract-class combinations
```java
// does not compile

public class AbstractIllegal {
    public static void main(String[] args) { }
}

final abstract class Bad1 {}                 // abstract and final, not both

abstract class Bad2 {
    private abstract void eat();             // private abstract: subclass can't see it
    final abstract void run();               // final abstract: contradiction
    static abstract void walk();             // static abstract: illegal
}
```

### POJO: anemic vs rich domain model
```java
public class PojoDemo {
    public static void main(String[] args) {
        StudentAnemic s1 = new StudentAnemic("Aditya", 1001);
        s1.setName("Rohit");
        System.out.println(s1.getName() + " " + s1.getRollNo()); // Rohit 1001

        StudentRich s2 = new StudentRich("Aditya");
        s2.markAttendance();
        s2.markAttendance();
        System.out.println(s2.getName() + " " + s2.getAttendance()); // Aditya 2
    }
}

class StudentAnemic {                          // anemic: data only
    private String name;
    private int rollNo;

    StudentAnemic(String name, int rollNo) { this.name = name; this.rollNo = rollNo; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public int getRollNo() { return rollNo; }
    public void setRollNo(int rollNo) { this.rollNo = rollNo; }
}

class StudentRich {                            // rich: data + business logic
    private final String name;
    private int attendance;

    StudentRich(String name) { this.name = name; }
    public void markAttendance() { attendance++; }
    public int getAttendance() { return attendance; }
    public String getName() { return name; }
}
```

## Common Pitfalls / Tips
- **Never compare wrappers (or Strings) with `==`.** Use `equals()`, or `Objects.equals(a, b)` if either side may be null. The cache makes `==` *seem* to work for small numbers, which hides the bug until values grow.
- **Watch for `null` unboxing.** `map.get(key)` returns `null` for a missing key, so `int v = map.get(key);` can throw. Check first or use `getOrDefault`.
- **Don't use `new Integer(...)`.** It's deprecated and always creates a new object. Use autoboxing or `Integer.valueOf`.
- **Boxing in hot loops costs time.** `Long sum = 0L; for (...) sum += i;` creates millions of objects. Use a primitive `long`.
- **Mixed `==`**: `Integer a = 200; int b = 200; a == b` is `true`, because `a` gets unboxed. The rules are subtle, so stick to `equals` for objects.
- **Abstract class checklist:** constructors yes, static yes, private/final yes as long as they're not abstract, zero abstract methods yes, `final class` no. Each answer follows from "can't instantiate, must be extended".
- **The file-name rule** applies only to `public` top-level classes. Nested classes and non-public top-level classes are unaffected.

## Review Questions
1. What error do you get when a `.java` file has two public top-level classes, and how do you fix it without creating a new file?
2. Why do wrapper classes exist, and why didn't Java drop primitives?
3. What does the compiler generate for `Integer y = x;` and for `int z = y;`?
4. Name three contexts where autoboxing or unboxing happens automatically.
5. Why does `Integer a = 200, b = 200; a == b` print `false` while the same code with 100 prints `true`?
6. What happens at run time with `Integer x = null; int y = x;`?
7. Can an abstract class have a constructor? Can it be `final`? Explain both.
8. What's the difference between an anemic model and a rich domain model?

<details>
<summary>Answers</summary>

1. "The public type X must be defined in its own file." Remove `public` from the extra class, since non-public top-level classes can share the file, or make it a nested class.
2. Many APIs (the Collection Framework, generics) work only with objects, and wrappers also provide utility methods. Primitives stayed for familiarity with C/C++ and above all for performance: no heap allocation, no object header, no reference.
3. `Integer y = Integer.valueOf(x);` (old Java conceptually used `new Integer(x)`), and `int z = y.intValue();`.
4. Assignments, method arguments and parameters (passing an `Integer` to an `int` parameter or the reverse), and arithmetic expressions (`a + b` on `Integer`s).
5. `==` compares references. `Integer.valueOf` returns cached objects for -128 to 127, so both 100s are the same object. 200 is outside the cache, so two separate objects are created and their addresses differ.
6. It compiles, but `x.intValue()` is called on `null` and throws a `NullPointerException`.
7. Yes, it can have a constructor: it runs (via `super(...)`) when a subclass object is created and initializes the inherited fields. No, it can't be `final`: `final` forbids subclassing, while an abstract class only makes sense if it's subclassed.
8. Both are POJOs. An anemic model has only fields, constructors and getters/setters. A rich domain model also contains business behavior (for example `markAttendance()`).
</details>

## Further Study
- `Object` class methods: `equals`, `hashCode`, `toString` (and the contract between `equals` and `hashCode`)
- `Number` class; `Integer.parseInt`, `Integer.toBinaryString`, `Character.isDigit` and other wrapper utilities
- Integer cache internals (`IntegerCache`, `-XX:AutoBoxCacheMax`) and the caches for `Byte`, `Short`, `Long`, `Character`, `Boolean`
- Performance cost of boxing; primitive streams (`IntStream`)
- Java records (Java 16+) as concise immutable data carriers
- JavaBeans conventions; DTOs vs entities; Lombok
- Builder design pattern; anemic domain model vs rich domain model (DDD)
- Java 11 single-file source launcher (`java File.java`)

---

[← #17](17-abstraction-and-polymorphism.md) · [Index](README.md) · [#19 →](19-java-nested-classes.md)

---

[← #17](17-abstraction-and-polymorphism.md) · [Index](README.md) · [#19 →](19-java-nested-classes.md)
