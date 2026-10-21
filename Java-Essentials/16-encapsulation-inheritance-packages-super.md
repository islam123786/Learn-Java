# 16 · Encapsulation & Inheritance · Packages & `super`
> Full notes: [16-encapsulation-inheritance-packages-super.md](../16-encapsulation-inheritance-packages-super.md) · [▶ Video](https://youtu.be/jfLpzL1VW7Q)

### The four pillars
- OOP rests on four pillars: **encapsulation**, **inheritance**, **abstraction** and **polymorphism**. This video covers the first two.

### Encapsulation, part 1: bundle data and behavior
- A capsule packs several medicines inside one wrapper. In the same way, an object's **data** (an animal's legs, its type) and **behavior** (eat, walk, run) belong together in one class.
- Fields plus methods in a class is already "50%" of encapsulation.

### Encapsulation, part 2: no unrestricted access
- **Definition:** (1) data and behavior are bundled in an object, and (2) the data **must not have unrestricted access**.
- **Bank account:** with a plain `double balance;` field, anyone can write `ba.balance = 100000;`, which is clearly wrong.
- In real life you can't edit your balance. You **deposit** or **withdraw**, and the balance changes as a result. Code should work the same way.

### Access modifiers
- They decide who can access a class, field, method or constructor. There are four levels: `public`, *default*, `protected` and `private`.
- With `private double balance;`, the line `ba.balance` gives a compile error from outside the class: *"The field BankAccount.balance is not visible."*

### `private`
- Accessible **only inside the same class**. `BankAccount`'s own methods (e.g. `withdraw`) can use `balance`, and nothing outside can.
- It works on methods too. A `private void withdraw()` can't be called as `ba.withdraw()` from outside.

### Default (package-private)
- Accessible from any class **in the same package**. A **package** is like a folder that groups related classes.
- You get it by writing **no modifier**. Everything written without a modifier so far has been package-private.
- There's **no `default` keyword** for access. `default double balance;` is a syntax error. (`default` is a keyword, but it's used for `switch` labels and interface default methods.)

### `protected` and `public`
- **`protected`:** the same package **plus subclasses**, even subclasses in other packages.
- **`public`:** accessible from everywhere.
- From strictest to most open: `private` > default > `protected` > `public`.
- In real code, **`private` and `public` are by far the most used**.

| Modifier | Same class | Same package | Subclass (other package) | Everywhere |
|---|---|---|---|---|
| `private` | ✅ | ❌ | ❌ | ❌ |
| *(none)* | ✅ | ✅ | ❌ | ❌ |
| `protected` | ✅ | ✅ | ✅ | ❌ |
| `public` | ✅ | ✅ | ✅ | ✅ |

### Which modifiers can a class have?
- A **top-level** class can only be `public` or package-private (no modifier). It **can't** be `private` or `protected`, because there's nothing outside it except the package.
- **Nested** classes *can* be `private` or `protected` (covered later).

### Implementing encapsulation (gist)
- Make the field `private` and expose `public` methods: `deposit()`, `withdraw()` and `getBalance()`.
- Those methods hold the **validation** (e.g. `amount <= balance`), so the field can only change through code that enforces the rules.
- `getBalance()` gives **read-only** access. There's deliberately **no `setBalance()`**.
- **Getters and setters** are the most common way to do this.

### Encapsulation in code (gist)
- With a public field, `ba.balance = <huge number>` just works. With `private`, you get the "not visible" error. After deposit 500 and withdraw 300, `getBalance()` returns `200.0`.
- **Why not just make the field public if it has a getter and a setter?** Because the setter can **validate**: the college must exist, the age can't be 200, and the roll number must be in range.
- A getter returns the field's type (`String getName()`), not `void`.

### Packages in practice
- The folders `school/` and `college/` can each have their own `Student.java`. The same class name is fine in different packages.
- The **first line** of each file declares its package: `package college;`

### `import`
- C/C++ use `#include`. Java uses **`import packageName.ClassName;`**, e.g. `import college.Student;`
- **Default access across packages:** if `print()` has no modifier, calling `s1.print()` from a class in another package fails with *"not visible"*. A class in the same package can call it without any import. Making `print()` `public` fixes it.
- **Name collisions:** importing both `college.Student` and `school.Student` is an error (*"collides with another import"*). Use the **fully qualified name** for one of them: `school.Student s2 = new school.Student();`
- **Wildcard:** `import college.*;` imports every class in `college`, but **not** its sub-packages.

### Built-in packages
- **`java.lang`** (`String`, `System`, `Math`) is imported **automatically**.
- **`java.util`** has the collections (`ArrayList`, …).
- **`java.io`** handles input and output.

### How imports work
- `import` is only a **compile-time shortcut for names**. It doesn't load or copy any code, and the bytecode is the same as if you wrote the full names.
- At runtime, the class loader loads `ArrayList.class` **the first time it's used**.
- Real projects use reverse-domain package names: `com.company.project.module`.

### Inheritance: the is-a relationship
- *A car is a vehicle*, and *an engineering student is a student*. Every child is a parent, but not every parent is that child.
- Every vehicle can `start()`, so write it once in `Vehicle` and let `Car` inherit it.

### `extends`
- `class Car extends Vehicle { void setGear() { … } }` means `Car` inherits `start()` and adds its own `setGear()`.
- If `Car` defines its own `start()`, `c.start()` runs the car's version. This is **overriding**. (The video calls it "shadowing", but that term is for fields.)

### Benefits
- **Code reuse** (no copy of `start()` in every vehicle) and support for **polymorphism** (next video).

### Inheritance in code (gist)
- `EngineeringStudent extends Student` can call both its own `attendLab()` and the inherited `markAttendance()`.
- A plain `Student` **can't** call `attendLab()`. A parent knows nothing about what its children add.
- The parent is the **superclass**, and the child is the **subclass**.
- **`private` members aren't visible to the child.** Default, `protected` and `public` ones are (within the same package).

### Types of inheritance
```
Single           Multilevel                 Hierarchical                  Multiple (❌ with classes)
Student          Student                     Student                     A       B
   │                │                       ┌───┴───┐                     └───┬───┘
   ▼                ▼                       ▼       ▼                         ▼
EngStudent       EngStudent             EngStudent  MedStudent                C
                    │
                    ▼
                 CSEStudent
```
- **Single:** one parent, one child.
- **Multilevel:** a chain. The grandchild can use all non-private members up the chain.
- **Hierarchical:** one parent with many children, and each child can have its own `attendLab()`.
- **Multiple:** `class C extends A, B` is a **compile error**. Java gets the same effect through **interfaces** instead (covered later).

### The diamond problem
```
        A  show()
       ┌┴┐
       B  C      both override show()
       └┬┘
        D        d.show() → B's or C's? ambiguous
```
- Java avoids this ambiguity by **not allowing multiple class inheritance**. (With interface default methods, the class has to resolve the clash explicitly.)

### `super`
- `this` refers to the current object. **`super` refers to the parent-class part** of it.
1. **Parent field:** if both `A` and `B extends A` declare `x` (4 and 5), then inside `B`, `x` is 5 and `super.x` is 4.
2. **Parent method:** `super.print()` calls the parent's version, which is useful inside an override.
3. **Parent constructor (the most important use):** `super(...)` runs the parent's constructor first. It must be the first line of the child's constructor.
   - If you don't write it, Java inserts **`super()`** automatically.

### `super` in code
```java
public class SuperDemo {
    public static void main(String[] args) {
        new EngineeringStudent("Aditya", 28, 1001, "IIT G").print();   // Aditya, 28, 1001, IIT G
    }
}

class Student {
    String name; int age; int rollNumber;
    Student() { }                                        // needed if a child relies on the implicit super()
    Student(String name, int age, int rollNumber) {
        this.name = name; this.age = age; this.rollNumber = rollNumber;
    }
    void print() { System.out.print(name + ", " + age + ", " + rollNumber); }
}

class EngineeringStudent extends Student {
    String college;
    EngineeringStudent(String name, int age, int rollNumber, String college) {
        super(name, age, rollNumber);                    // the parent sets up its own fields
        this.college = college;
    }
    @Override
    void print() {
        super.print();                                   // reuse the parent's print
        System.out.println(", " + college);
    }
}
```
- With only `EngineeringStudent(String college)`, the inherited fields stay at `null 0 0`. Pass them to `super(...)` instead.
- **There's no `super.super`.** You can't skip a level.
- **Trap:** if a child constructor doesn't call `super(...)`, the compiler inserts `super()`. That **fails to compile** if the parent has no no-arg constructor.

**Remember:** encapsulation means `private` fields with `public` methods that validate. The four access levels are private < default < protected < public. Inheritance (`extends`) is an is-a relationship. Java allows one class parent and many interfaces. `super` reaches the parent's fields, methods and constructor.
