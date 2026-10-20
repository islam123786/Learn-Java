# Encapsulation & Inheritance in Java | Java Packages & Super Keyword | Java Full Course #16
> Source: https://youtu.be/jfLpzL1VW7Q · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 98:02. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- **Encapsulation** (pillar 1) means two things: (1) bundle data and behavior together in a class, and (2) **don't give unrestricted access to the data**. Make fields `private` and expose `public` methods (`deposit`/`withdraw`, getters/setters) where you can **validate**.
- **Access modifiers**, from strictest to most open: `private` (same class) → *default / package-private* (same package, written as no modifier) → `protected` (same package + subclasses anywhere) → `public` (everywhere). A top-level class can only be `public` or package-private.
- A **package** is a folder-like namespace for related classes. `package college;` at the top of the file, `import college.Student;` or `import college.*;` to use it, or a fully qualified name (`college.Student`) when two classes share a simple name.
- **Inheritance** (pillar 2) models an **is-a** relationship with `extends` (`Car extends Vehicle`). The child reuses the parent's non-private members and adds its own. Java supports single, multilevel and hierarchical inheritance, but **not multiple inheritance of classes** (because of the diamond problem). Interfaces solve that.
- **`super`** refers to the parent part of the object: `super.x` (the parent's field), `super.print()` (the parent's method), and `super(...)` (the parent's constructor, which must be the first statement and is inserted automatically as `super()` if you don't write it).

## Key Concepts

**Encapsulation.** Data and the behavior that operates on it live together, and outside code can reach the data only through controlled methods. *Why it matters:* nobody can set `balance = 100000` directly. Every change goes through code that can check it.

**Access modifier.** A keyword (or the absence of one) that controls who can see a class, field, method or constructor. *Why it matters:* it's the tool that enforces encapsulation.

**Getters and setters.** `getX()` returns a private field and `setX(v)` assigns it, usually after validation. Leave out the setter when the field shouldn't be changed directly (e.g. `balance`). *Why it matters:* you keep control (validation, read-only fields) without exposing the field.

**Package.** A group of related classes and interfaces, matching a directory. *Why it matters:* it organizes code, prevents name clashes (`school.Student` vs `college.Student`) and defines the scope of package-private access.

**`import`.** Lets you refer to a class from another package by its simple name. *Why it matters:* it avoids writing `java.util.ArrayList` everywhere. It's a compile-time convenience only.

**Inheritance (is-a).** `class Child extends Parent`: the child gets the parent's accessible fields and methods. Parent = **superclass**, child = **subclass**. *Why it matters:* it gives code reuse, and it's the basis of polymorphism.

**Method overriding.** A subclass declares a method with the same signature as the parent's. Calls on the child object then use the child's version. *Why it matters:* it's how children specialize behavior (next video: polymorphism).

**Types of inheritance.** Single (A → B), multilevel (A → B → C), hierarchical (A → B and A → C), and multiple (A and B → C, **not allowed for classes in Java**). *Why it matters:* it's a common interview question.

**Diamond problem.** If D inherited from both B and C, and both override A's `show()`, which `show()` does `d.show()` run? *Why it matters:* it's why Java forbids multiple class inheritance.

**`super` keyword.** A reference to the parent-class part of the current object. *Why it matters:* it lets a child reach hidden fields, call overridden methods and reuse the parent's constructor.

## Detailed Notes

### The four pillars [00:00]
- OOP rests on four pillars: **encapsulation**, **inheritance**, **abstraction**, **polymorphism**. This video covers the first two.

### Encapsulation, part 1: bundle data and behavior [00:45]
- A capsule packs different medicines inside one wrapper. In the same way, an object's **data** (an animal's legs, type) and **behavior** (eat, walk, run) are intrinsic to it and belong in one class.
- We've already been doing this (fields + methods in a class). That's "50%" of encapsulation.

### Encapsulation, part 2: no unrestricted access [04:22]
- Definition: (1) data and behavior are encapsulated in an object, and (2) data **must not have unrestricted access**.
- **Bank account** example [05:32]: `class BankAccount { double balance; String bankName; String ifsc; }`. Anyone can write `ba.balance = 100000;`, which is clearly wrong.
- In real life you can't edit your balance. You **deposit** or **withdraw**, and the balance changes as a side effect. Mimic that.

### Access modifiers [09:55]
- They decide who can access a variable, method, constructor or class. Java has four levels: `public`, *default*, `protected`, `private`.
- Putting `private` on `balance` makes `ba.balance` a compile error from outside the class: *"The field BankAccount.balance is not visible."*

### `private` [13:19]
- Accessible **only inside the same class**. Methods of `BankAccount` (e.g. `withdraw`) can use `balance`, and nothing outside can.
- Works on fields and methods: a `private void withdraw()` couldn't be called as `ba.withdraw()` from outside.

### Default (package-private) [15:21]
- Accessible from any class **in the same package**.
- **Package** [17:11]: like a folder that groups similar classes and interfaces.
- Writing **no modifier** gives default access. Every field and class written without a modifier so far has been package-private.
- *Correction:* the video calls `default` one of the four access-modifier "keywords". There's no `default` keyword for access. You get package-private access by writing *nothing* (`double balance;`). Writing `default double balance;` is a syntax error. (`default` is a keyword, but for `switch` labels and interface default methods.)

### `protected` and `public` [18:32]
- `protected`: same package **plus subclasses**, even subclasses in other packages.
- `public`: accessible from everywhere.
- Strictness: `private` > default > `protected` > `public`.
- In real code, **`private` and `public` are by far the most used** [20:26]. Package-private and `protected` are less common (the video says default is "hardly used").

| Modifier | Same class | Same package | Subclass (other package) | Everywhere |
|---|---|---|---|---|
| `private` | yes | no | no | no |
| *(none)* | yes | yes | no | no |
| `protected` | yes | yes | yes | no |
| `public` | yes | yes | yes | yes |

### Which modifiers can a class have? [22:01]
- A **top-level** class can be `public` or package-private (no modifier). It **can't** be `private` ("private relative to what? There's only the package outside it") or `protected`.
- Nested classes *can* be `private` or `protected` (covered later).
- The `main` class has been `public`, and helper classes like `class Student` (no modifier) have been package-private.

### Implementing encapsulation [24:56]
- `private double balance;` plus `public void deposit(double amount)`, `public void withdraw(double amount)` and `public double getBalance()`.
- Outside code calls `ba.deposit(500)` and `ba.withdraw(300)` but never touches `balance` directly.
- In real code these methods hold **validations** (is the note genuine? is there enough balance?). That's the whole point: the field is reachable only through code that enforces the rules.
- `getBalance()` is **read-only** access. There's deliberately **no `setBalance()`**.
- **Getters and setters** [32:58] are the most common encapsulation tool. Methods like `deposit`/`withdraw` are domain ("functional") methods.

### Encapsulation in code [33:47]
- With a public field, `ba.balance = <huge>` simply works. With `private` you get the "not visible" error. After deposit 500 and withdraw 300, `getBalance()` returns `200.0`.
- **Student with getters/setters** [37:01]: all fields `private`, a constructor for the initial values, then `getName()`/`setName()`, `getCollege()`/`setCollege()`, …
  - (The video first types `public void getName()` and fixes it to `String`: a getter returns the field's type.)
  - "If there's a getter and a setter anyway, why not just make it public?" Because the setter can **validate**: the college must exist (e.g. check a list or an API), the name must have first and last name, the age can't be 200, the roll number has to be in range.
- The withdraw validation should be `amount <= balance` (the video says "≥" and then corrects itself).

### Packages in practice [43:31]
- Folder `video16/` with subfolders `school/` and `college/`, each with a `Student.java`. The same class name is fine in different packages.
- The IDE adds `package college;` (or `package school;`) as the **first line**. Every class in that folder belongs to that package.
- `Student.print()` prints "College student" or "School student".

### `import` [47:14]
- C/C++ use `#include`. Java uses `import packageName.ClassName;`, e.g. `import college.Student;`.
- `s1.print()` from `Demo2` fails with *"The method print() from the type Student is not visible"*, because `print()` has default access and `Demo2` is in a different package.
  - A `Teacher` class in the `college` package (same file or a separate `Teacher.java`) **can** call it without any import.
  - Making `print()` `public` fixes `Demo2`. Creating `new Student()` from `Demo2` already worked because the class is `public` (and has an implicit public constructor).
- Importing **both** `college.Student` and `school.Student` [52:53] gives the error *"The import school.Student collides with another import statement."* Use the **fully qualified name** for one (or both): `school.Student s2 = new school.Student();`.
- Wildcard [54:30]: `import college.*;` imports every class in `college`.
  - *Precision:* a wildcard doesn't import sub-packages. If two wildcards bring in the same simple name, using that name is ambiguous, so you still need the fully qualified name.

### Built-in packages [56:27]
- `java.lang` (`String`, `System`, `Math`, …) is imported **automatically**. `java.util` holds the Collections Framework (`ArrayList`, …). `java.io` handles input and output.
- `import java.util.ArrayList;` or `import java.util.*;`. Together these packages form the Java class library.

### How imports work [58:01]
- The video's explanation: the compiler reads the imports and loads `ArrayList.class` (bytecode) into memory so there's no ambiguity.
- *Correction:* `import` is purely a **compile-time name-resolution shortcut**. It doesn't load or copy any code, and the bytecode is the same as if you had written fully qualified names. At runtime the JVM's class loader loads `ArrayList.class` lazily, the first time the class is actually used.
- Production packages are hierarchical, reverse-domain names [60:56] such as `com.company.project.module`. More on this in the Spring module.

### Inheritance: the is-a relationship [61:29]
- *Car is a vehicle*, *bike is a vehicle*, *engineering student is a student*. Every child is a parent, but not every parent is that child.
- Every vehicle can `start()`. A car starts *because* it's a vehicle, so write `start()` once in `Vehicle`.

### `extends` [66:04]
- `class Car extends Vehicle { void setGear() {...} }`. `setGear` is car-specific (a bicycle has none), and `start()` is inherited.
- `Car c = new Car(); c.start(); c.setGear();` both work.
- If `Car` declares its own `start()`, then `c.start()` runs the car's version.
  - *Terminology:* the video calls this "shadowing" the parent's `start()`. For methods the correct term is **overriding**. *Hiding/shadowing* applies to fields (and static methods).

### Benefits [68:51]
- **Code reusability** (no duplicated `start()` in every vehicle) and **support for polymorphism** (next video).

### Inheritance in code [69:46]
- `Student { name; age; markAttendance() }` and `EngineeringStudent extends Student { attendLab() }`. An `EngineeringStudent` can call both methods.
- A plain `Student` **can't** call `attendLab()`: *"The method attendLab() is undefined for the type Student."* A parent knows nothing about its children's additions.
- Parent = **superclass**, child = **subclass** [72:43].
- Access and inheritance:
  - Default, `protected` and `public` parent methods are usable in the child (same package here).
  - A `private markAttendance()` is invisible to the child.
  - The video says the student "can't call it either". Precisely, it can't be called *from outside* `Student` (e.g. from `main` in another class). Code *inside* `Student` can still call it.

### Types of inheritance [74:48]
- **Single:** `Student` → `EngineeringStudent`.
- **Multilevel** [75:43]: `Student` → `EngineeringStudent` → `CSEEngineeringStudent`. The grandchild can call `markAttendance()`, `attendLab()` and its own `attendCSELab()`, meaning all non-private members up the chain.
- **Hierarchical** [78:03]: `EngineeringStudent` and `MedicalStudent` both extend `Student`. Each defines its own `attendLab()` ("Lab attended for Medical/Engineering student").
- **Multiple** [81:11]: `class C extends A, B` is a **compile error** in Java. Java supports it only through **interfaces** (a class can implement many interfaces), covered later.

### The diamond problem [82:32]
- A has `show()`. B and C both extend A and override `show()`. If D could extend both B and C and didn't override `show()`, `d.show()` would be ambiguous.
- *Correction:* the video says the compiler "won't be able to tell at runtime". The ambiguity is a **compile-time** design problem, and Java avoids it by forbidding multiple class inheritance. (With interface default methods, Java makes you resolve such clashes explicitly in the class.)

### `super` [85:11]
- `this` refers to the current object. **`super` refers to its parent-class part.**
- **Use 1, parent fields:** if both `A` and `B extends A` declare `int x` (4 and 5), then inside `B`, `x` is 5 and `super.x` is 4.
- **Use 2, parent methods:** `super.greet()` calls the parent's version, which is useful inside an override.
- **Use 3 (most important), parent constructor** [87:41]: `super(...)` inside a child constructor runs the parent's constructor first.
  - If you don't write it, Java inserts `super()` automatically.

### `super` in code [89:05]
- `Student { name, age, rollNumber; print() }` and `EngineeringStudent extends Student { college; print() { super.print(); /* then college */ } }`. Calling `es1.print()` prints all four values.
- (`super.super.rollNumber` appears in the video by mistake. You can't chain `super.super`. With no name clash, plain `rollNumber` or `super.rollNumber` works.)
- When the child declares a field with the same name (`x`), the child's own field wins, and `super.x` reaches the parent's.
- Constructors [93:56]: `Student(String name, int age, int rollNumber)`, plus an explicit no-arg constructor because the parameterized one removes the default.
  - `EngineeringStudent(String college)` alone leaves the inherited fields at `null 0 0`.
  - Better: `EngineeringStudent(String name, int age, int rollNumber, String college) { super(name, age, rollNumber); this.college = college; }`. The parent initializes its own fields, so the output is `Aditya, 28, 1001, IIT G`.
- Why the no-arg parent constructor mattered: if a child constructor doesn't call `super(...)` explicitly, the compiler inserts `super()`, and that **fails to compile** when the parent has no no-arg constructor.

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java FileName.java`.

### Encapsulated bank account (private field, public behavior, validation)
```java
public class EncapsulationDemo {
    public static void main(String[] args) {
        SavingsAccount ba = new SavingsAccount();
        // ba.balance = 100000;      // error: balance has private access
        ba.deposit(500);
        ba.withdraw(300);
        System.out.println(ba.getBalance()); // 200.0

        System.out.println(ba.withdraw(1000)); // false (validation: insufficient balance)
        System.out.println(ba.deposit(-50));   // false (validation: amount must be positive)
        System.out.println(ba.getBalance());   // 200.0
    }
}

class SavingsAccount {
    private double balance; // no setter on purpose

    public boolean deposit(double amount) {
        if (amount <= 0) return false;
        balance += amount;
        return true;
    }

    public boolean withdraw(double amount) {
        if (amount <= 0 || amount > balance) return false; // amount must be <= balance
        balance -= amount;
        return true;
    }

    public double getBalance() { // read-only access
        return balance;
    }
}
```

### Getters and setters with validation
```java
public class GettersSettersDemo {
    public static void main(String[] args) {
        Enrollee s = new Enrollee("Aditya Tandon", 28, 42, "IIT Guwahati");
        System.out.println(s.getName() + ", " + s.getCollege()); // Aditya Tandon, IIT Guwahati

        try {
            s.setAge(200);
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Invalid age: 200
        }
        try {
            s.setCollege("ABC College");
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage()); // Unknown college: ABC College
        }
        s.setCollege("IIT Bombay");
        System.out.println(s.getCollege()); // IIT Bombay
    }
}

class Enrollee {
    private static final java.util.Set<String> KNOWN_COLLEGES =
            java.util.Set.of("IIT Guwahati", "IIT Bombay", "IIT Kharagpur"); // stand-in for an API call

    private String name;
    private int age;
    private int rollNumber;
    private String college;

    Enrollee(String name, int age, int rollNumber, String college) {
        setName(name); // reuse the validation in the constructor
        setAge(age);
        this.rollNumber = rollNumber;
        setCollege(college);
    }

    public String getName() { return name; }
    public void setName(String name) {
        if (name == null || name.trim().split("\\s+").length < 2)
            throw new IllegalArgumentException("Need first and last name: " + name);
        this.name = name;
    }

    public int getAge() { return age; }
    public void setAge(int age) {
        if (age < 0 || age > 120) throw new IllegalArgumentException("Invalid age: " + age);
        this.age = age;
    }

    public int getRollNumber() { return rollNumber; }

    public String getCollege() { return college; }
    public void setCollege(String college) {
        if (!KNOWN_COLLEGES.contains(college))
            throw new IllegalArgumentException("Unknown college: " + college);
        this.college = college;
    }
}
```

### Packages and imports (needs a folder layout, so it can't run as one file)
```java
// does not compile as a single file: each part below is a separate file in its own folder
// video16/college/Student.java
package college;

public class Student {
    void print() { System.out.println("College student"); }      // package-private
    public void show() { System.out.println("College student"); } // public
}

// video16/college/Teacher.java   (same package: no import needed, package-private is OK)
package college;

public class Teacher {
    void teach() { new Student().print(); }
}

// video16/school/Student.java
package school;

public class Student {
    public void show() { System.out.println("School student"); }
}

// video16/Demo2.java
import college.Student;              // simple name Student now means college.Student
// import school.Student;            // error: collides with another import statement

public class Demo2 {
    public static void main(String[] args) {
        Student s1 = new Student();
        // s1.print();               // error: print() is not public in college.Student
        s1.show();                   // College student
        school.Student s2 = new school.Student(); // fully qualified name avoids the clash
        s2.show();                   // School student
    }
}
// compile & run from video16/:  javac Demo2.java college/*.java school/*.java && java Demo2
```

### Inheritance: single, multilevel, hierarchical
```java
public class InheritanceTypes {
    public static void main(String[] args) {
        Undergrad base = new Undergrad();
        base.markAttendance();            // Attendance marked
        // base.attendLab();              // error: undefined for the type Undergrad

        EngineeringUndergrad es = new EngineeringUndergrad(); // single: Undergrad -> EngineeringUndergrad
        es.markAttendance();              // Attendance marked
        es.attendLab();                   // Lab attended for engineering student

        CseUndergrad cse = new CseUndergrad(); // multilevel: Undergrad -> Engineering -> CSE
        cse.markAttendance();             // Attendance marked
        cse.attendLab();                  // Lab attended for engineering student
        cse.attendCseLab();               // CSE lab attended

        MedicalUndergrad ms = new MedicalUndergrad(); // hierarchical: Medical also extends Undergrad
        ms.markAttendance();              // Attendance marked
        ms.attendLab();                   // Lab attended for medical student
    }
}

class Undergrad {
    String name;
    int age;
    public void markAttendance() { System.out.println("Attendance marked"); }
}

class EngineeringUndergrad extends Undergrad {
    void attendLab() { System.out.println("Lab attended for engineering student"); }
}

class CseUndergrad extends EngineeringUndergrad {
    void attendCseLab() { System.out.println("CSE lab attended"); }
}

class MedicalUndergrad extends Undergrad {
    void attendLab() { System.out.println("Lab attended for medical student"); }
}
```

### Overriding vs inheriting a method
```java
public class OverrideDemo {
    public static void main(String[] args) {
        new Motorbike().start(); // Vehicle starting   (inherited)
        new Sedan().start();     // Sedan starting     (overridden)
        new Sedan().setGear(2);  // Gear set to 2      (child-only method)
    }
}

class Vehicle {
    void start() { System.out.println("Vehicle starting"); }
}

class Motorbike extends Vehicle { }

class Sedan extends Vehicle {
    @Override
    void start() { System.out.println("Sedan starting"); }
    void setGear(int g) { System.out.println("Gear set to " + g); }
}
```

### Multiple inheritance of classes is not allowed
```java
// does not compile
public class NoMultipleInheritance {
    public static void main(String[] args) { }
}

class ParentA { void show() { System.out.println("A"); } }
class ParentB { void show() { System.out.println("B"); } }
class Child extends ParentA, ParentB { } // error: '{' expected (only one superclass allowed)
```

### `super`: fields, methods and constructors
```java
public class SuperDemo {
    public static void main(String[] args) {
        Engineer es = new Engineer("Aditya", 28, 1001, "IIT G");
        es.print();      // Aditya, 28, 1001
                         // IIT G
        es.showX();      // own x = 5, parent x = 4
    }
}

class Trainee {
    String name;
    int age;
    int rollNumber;
    int x = 4;

    Trainee() { }        // explicit no-arg constructor (the parameterized one removed the default)

    Trainee(String name, int age, int rollNumber) {
        this.name = name;
        this.age = age;
        this.rollNumber = rollNumber;
    }

    void print() {
        System.out.println(name + ", " + age + ", " + rollNumber);
    }
}

class Engineer extends Trainee {
    String college;
    int x = 5;           // hides the parent's x

    Engineer(String name, int age, int rollNumber, String college) {
        super(name, age, rollNumber); // parent constructor first (must be the first statement)
        this.college = college;
    }

    @Override
    void print() {
        super.print();                // parent's version: name, age, roll number
        System.out.println(college);  // then the child's extra field
    }

    void showX() {
        System.out.println("own x = " + x + ", parent x = " + super.x);
    }
}
```

### Implicit `super()` needs a no-arg parent constructor
```java
// does not compile
public class ImplicitSuperFails {
    public static void main(String[] args) { }
}

class Parent16 {
    Parent16(String name) { }
}

class Child16 extends Parent16 {
    Child16() { } // error: compiler inserts super(), but Parent16() does not exist
}
```

## Common Pitfalls / Tips
- **Default access has no keyword.** Leaving the modifier off means package-private. Don't write `default`.
- **A getter + setter that just copies isn't encapsulation by itself.** The value comes from what the setter can *refuse*, and from leaving out setters for fields that must not be set directly.
- **Return types for getters:** `public String getName()`, not `void`.
- **One `public` top-level class per file**, named after the file. The `package` statement must come first (before imports).
- **Same simple name in two packages:** import one, and use the fully qualified name for the other.
- **`import` doesn't load code.** It's a compile-time shortcut. `java.lang` is always imported.
- **A parent reference can't see child-only methods.** `Student s = …; s.attendLab()` fails at compile time.
- **Private members aren't accessible in subclasses.** Use `protected` or public getters if children need them.
- **Overriding ≠ hiding.** Methods are overridden (dynamic dispatch). Fields with the same name are hidden (`super.x` reaches the parent's). Add `@Override` so the compiler checks your signature.
- **`super(...)`/`this(...)` must be the first statement** (before Java 25), and you can't have both in one constructor.
- **If the parent has only parameterized constructors**, every child constructor must call `super(args)` explicitly.
- **No multiple class inheritance.** Use interfaces (`class C implements A, B`) instead.

## Review Questions
1. What two things does encapsulation require? How does the bank account example satisfy both?
2. Rank the four access levels and say who can access each one.
3. Why can't a top-level class be `private` or `protected`?
4. If a setter can still change the field, why not just make the field public?
5. How do you use two classes named `Student` from different packages in one file?
6. What does an `import` actually do at compile time and at runtime?
7. Name the four types of inheritance. Which one does Java not support for classes, and why?
8. List three uses of `super`. What happens if a child constructor doesn't call `super(...)`?

<details>
<summary>Answers</summary>

1. (1) Data and behavior are bundled in one class. (2) The data has no unrestricted access. `balance` is `private` in `BankAccount`, and it changes only through `public deposit`/`withdraw` (which can validate). `getBalance()` gives read-only access and there's no setter.
2. `private` (same class only) → package-private/no modifier (same package) → `protected` (same package + subclasses in any package) → `public` (everyone).
3. `private` would mean "visible only inside the enclosing class", and a top-level class has no enclosing class, only a package. `protected` is about subclass access to members, which doesn't apply to a top-level class itself. Only `public` and package-private are allowed.
4. Because the setter is code: it can validate (a real college, a sensible age, first + last name), log or reject a change. You can also give some fields only a getter. A public field allows any value, from anywhere.
5. Import one (`import college.Student;`) and refer to the other by its fully qualified name (`school.Student s2 = new school.Student();`), or use fully qualified names for both. Importing both simple names is a collision error.
6. At compile time it only lets you use the simple name instead of the fully qualified one. It doesn't copy or load anything. At runtime the class loader loads the class lazily, when it's first used.
7. Single, multilevel, hierarchical and multiple. Multiple inheritance of classes is not supported, because of the diamond problem: if two parents provide the same method, the child's call would be ambiguous. Interfaces provide multiple inheritance of type instead.
8. `super.field` (access a parent field, e.g. one hidden by a same-named child field), `super.method()` (call the parent's version of an overridden method), and `super(args)` (call the parent's constructor as the first statement). If you don't write it, the compiler inserts `super()`, which fails to compile if the parent has no no-arg constructor.
</details>

## Further Study
- Polymorphism: method overriding, dynamic dispatch, upcasting (next video)
- Abstraction: abstract classes and interfaces; default methods and the diamond problem
- The `Object` class as every class's root; `toString`, `equals`, `hashCode`
- `protected` access across packages, in detail
- `final` methods and classes in inheritance
- Composition vs inheritance ("favor composition")
- Java Platform Module System (`module-info.java`) vs packages
- Records and immutable classes as an alternative to getters/setters

---

[← #15](15-static-final-blocks-string-args.md) · [Index](../../ReadMe.md) · [#17 →](17-abstraction-and-polymorphism.md)
