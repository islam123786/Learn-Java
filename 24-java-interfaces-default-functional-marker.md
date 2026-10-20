# Java Interfaces Deep Dive | Default Methods, Functional, Marker Interfaces | Java Full Course #24
> Source: https://youtu.be/YLLFHuStkW8 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 59:55. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- An interface describes **what** something can do, not **how**. It's a **blueprint of behavior** (a *contract*), not a blueprint of an object. Implementing classes must provide every abstract method, or be declared `abstract` themselves.
- Interface methods are implicitly `public abstract`. Interface variables are implicitly `public static final` (constants). An implementation can't reduce visibility, so it must write `public`.
- Interfaces give Java **multiple inheritance of type**: `class C implements A, B`. An interface can `extend` other interfaces.
- **Java 8** added `default` and `static` methods, so existing interfaces like `List` could gain methods without breaking every implementation. **Java 9** added `private` methods.
- Diamond rules: if two interfaces provide the same default, the class **must override** (and can call `B.super.fun()`). If a superclass and an interface clash, **the class wins**.
- A **functional interface** has exactly one abstract method, which enables lambdas. A **marker interface** has none (`Cloneable`, `Serializable`, `RandomAccess`).

## Key Concepts

**Interface as a contract.** `interface Car { void drive(); }` says "anything that is a Car can drive". *Why it matters:* code depends on capabilities, not concrete classes.

**Implementation rules.** `class Thar implements Car` must override `drive()` as `public` (an override can widen access but never narrow it). If it doesn't, `Thar` must be `abstract`, and a concrete subclass finishes the job. *Why it matters:* the compiler enforces the contract.

**Dynamic dispatch through interfaces.** `Payment p = new CreditCard(); p.pay();` calls CreditCard's version. Swap in `new DebitCard()` and nothing else changes. *Why it matters:* it's the basis of dependency injection (Spring) and clean architecture.

**Interface constants.** Any field in an interface is implicitly `public static final`, because interfaces have no objects. *Why it matters:* you get handy constant holders (`MathConstants.PI_VALUE`), though see the tips for caveats.

**Multiple inheritance via interfaces.** A class can implement many interfaces but extend only one class. *Why it matters:* you can combine capabilities without the diamond problem of classes.

**Default, static and private methods.** `default` gives an inherited body that implementations may override. `static` belongs to the interface and is called as `Vehicle.brake()`. `private` (Java 9) is a helper used only by other interface methods. *Why it matters:* APIs can evolve without breaking existing code.

**Diamond resolution rules.** (1) Class beats interface. (2) If two interfaces conflict, the implementing class must override, optionally delegating with `X.super.m()`. *Why it matters:* interviewers love these.

**Interface vs abstract class.** The **intent** differs: an interface models a *can-do* capability (Runnable, Payable), while an abstract class models an *is-a* family with shared state. In practice, interfaces have no instance fields, no constructors, public members by default, and allow multiple inheritance. Abstract classes have fields, constructors and any access modifier, but only single inheritance. *Why it matters:* choosing the right tool.

**Functional and marker interfaces.** Functional means one abstract method, and is the target type for lambdas (`Comparator`, `Runnable`, `Predicate`). Marker means no methods, only a tag checked at runtime (`Cloneable`, `Serializable`). *Why it matters:* functional programming in Java, and opt-in JVM features.

## Detailed Notes

### Interfaces recap: what, not how [▶ ~00:29](https://youtu.be/YLLFHuStkW8?t=29)
- Java has two tools for abstraction: **abstract classes** and **interfaces**.
- `interface Car { void drive(); }` only declares the method. Definition: *"Define what an object can do without telling how it does that."*
- A class is a blueprint of an **object**. An interface has nothing to do with objects (you can't instantiate one). It's a **blueprint of behavior** [▶ ~03:27](https://youtu.be/YLLFHuStkW8?t=207), also called a **contract**: whoever implements it must define every method, or else be `abstract`.

### Implementing an interface [▶ ~05:24](https://youtu.be/YLLFHuStkW8?t=324)
- `class Thar implements Car { void drive() {...} }` gives the error **"Cannot reduce the visibility of the inherited method"**.
- Interface methods are implicitly `public`. Leaving out `public` in the class gives package-private (default) access, which is *more restrictive*. Java forbids narrowing access in an override, so you write `public void drive()`.
- Usage: `Car c = new Thar(); c.drive();`. You can't write `new Car()`.
- Not ready to implement [▶ ~08:05](https://youtu.be/YLLFHuStkW8?t=485)? Mark the method and class `abstract` (`abstract class Thar implements Car`). Then a concrete `class BlackThar extends Thar` overrides `drive()`.

### Dynamic dispatch with interfaces [▶ ~09:06](https://youtu.be/YLLFHuStkW8?t=546)
- `interface Payment { void pay(); }` with `CreditCard` and `DebitCard` implementations.
- `Payment p = new CreditCard(); p.pay();` prints "Paying via credit card". Change it to `new DebitCard()` and you get "Paying via debit card", with no other code changes. The choice could come from configuration or a condition at runtime.
- This *dynamic polymorphism* is the foundation of **dependency injection** in Spring Boot and of clean-architecture designs.

### Variables in interfaces [▶ ~11:47](https://youtu.be/YLLFHuStkW8?t=707)
- `interface MathConstants { double PI_VALUE = 3.14; int VALUE = 10; }`
- Every interface field is implicitly **`public static final`**:
  - `static`, because there's no interface object to hold instance state.
  - `final`, because an interface can only hold constants, not changeable state.
- Implementing classes can read `PI_VALUE` but can't assign it.
- With `MathConstants r1 = new Random();`, `r1.fun()` fails to compile unless `fun()` is declared in `MathConstants`. A reference can only call methods its own type knows about.
- Access without any object: `MathConstants.PI_VALUE` [▶ ~15:53](https://youtu.be/YLLFHuStkW8?t=953). Teams sometimes put app-wide constants in an interface for exactly this reason, since there's no need to type `public static final` for each one.

### Multiple inheritance through interfaces [▶ ~17:15](https://youtu.be/YLLFHuStkW8?t=1035)
- `class C extends A, B` is a syntax error. Java avoids the **diamond problem** of classes.
- `class C implements A, B` is fine, and you can implement as many interfaces as you like. `C` must override both `fun()` (from A) and `fun2()` (from B).

### Interface inheritance [▶ ~19:14](https://youtu.be/YLLFHuStkW8?t=1154)
- An interface can **extend** another interface (it can't *implement* one, because it doesn't provide bodies): `interface Dog extends Animal { void bark(); }`.
- `class StreetDog implements Dog` must override **both** `eat()` (from Animal) and `bark()`. The compiler error names exactly which one is missing.

### Java 8: default methods [▶ ~21:22](https://youtu.be/YLLFHuStkW8?t=1282)
- Before Java 8, interface methods were implicitly `public abstract`, and writing a body gave "Abstract methods do not specify a body".
- `default void drive() { System.out.println("Vehicle is driving"); }` [▶ ~22:55](https://youtu.be/YLLFHuStkW8?t=1375) gives the interface a body. `class Car implements Vehicle` no longer has to override it, and `new Car().drive()` uses the default.
- If `Car` overrides `drive()`, the override wins ("Car is driving"), just as with an abstract class's concrete method.

### Why default methods were added [▶ ~25:24](https://youtu.be/YLLFHuStkW8?t=1524)
- Story: Java wanted to add methods to the **`List`** interface (and others). Adding a plain abstract method (the video's made-up example is `pushBack`) would force **every class in the world** that implements `List` to change. That would break production code everywhere.
- Making the new method `default` with a body means old implementations keep compiling. They inherit the default and can override it if they want.
- In the real JDK, Java 8 added methods like `List.sort`, `Iterable.forEach` and `Collection.stream` this way.

### Static and private methods [▶ ~27:35](https://youtu.be/YLLFHuStkW8?t=1655)
- **Static (Java 8)**: `static void brake() {...}`, called as `Vehicle.brake()` (interface name + method), with no object needed. It's **not** inherited by implementing classes, so `Car.brake()` doesn't work.
- **Private (Java 9)** [▶ ~29:05](https://youtu.be/YLLFHuStkW8?t=1745): `private void accelerate() {...}` can only be called from other methods inside the interface. Example: the default `drive()` prints "Vehicle is driving" and then calls `accelerate()`, which prints "Vehicle is accelerating".

> **Correction:** the video names the static method `break`. That's a reserved keyword in Java and won't compile. The intended word is **`brake`**.

### The diamond problem with classes [▶ ~30:51](https://youtu.be/YLLFHuStkW8?t=1851)
- Class `A` defines `fun()`. `B` and `C` both extend `A` and override `fun()` differently. If `D` could extend both `B` and `C`, then `new D().fun()` would be ambiguous.
- C++ resolves this with extra rules. James Gosling's team decided it wasn't worth the complexity and **banned multiple class inheritance**.

### Why interfaces avoid it (before Java 8) [▶ ~33:52](https://youtu.be/YLLFHuStkW8?t=2032)
- If `A`, `B` and `C` are interfaces, they only **declare** `fun()`. None of them supplies a body.
- The concrete class `D implements B, C` **must** provide the only body, so there's nothing ambiguous: `new D().fun()` calls D's version.
- Code [▶ ~35:44](https://youtu.be/YLLFHuStkW8?t=2144): `interface A { void fun(); }`, `interface B extends A`, `interface C extends A`, and `class D implements B, C` with one `fun()`, which prints "default implementation".

### The diamond after Java 8: conflicting defaults [▶ ~37:18](https://youtu.be/YLLFHuStkW8?t=2238)
- Give **both** `B` and `C` a `default void fun()` (printing "B" and "C"). Now `D implements B, C` inherits two bodies.
- Rule: **D must override `fun()`**, or it won't compile. The error is "Duplicate default methods named fun with the parameters () … are inherited from the types C and B" [▶ ~42:00](https://youtu.be/YLLFHuStkW8?t=2520).
- Inside the override you can:
  - write your own logic (prints "D"),
  - delegate to one parent with `B.super.fun();` [▶ ~39:47](https://youtu.be/YLLFHuStkW8?t=2387) (prints "B"),
  - or call both: `B.super.fun(); C.super.fun();` (prints B then C).
- If `D` implemented only `B`, there'd be no conflict and no need to override.

### Resolution priority: class wins [▶ ~42:22](https://youtu.be/YLLFHuStkW8?t=2542)
- `class C extends B implements A`, where class `B` has `public void fun()` and interface `A` has `default void fun()`.
- No compile error, and `new C().fun()` prints **"Inside B class"** [▶ ~44:00](https://youtu.be/YLLFHuStkW8?t=2640). **A method from a superclass always beats an interface default.**
- To change that, `C` overrides `fun()` itself (prints "Inside C class").

> **Note:** the full rule set is: (1) a method declared in a class or superclass wins over any interface default. (2) Otherwise, the most specific interface wins (a sub-interface's default beats its parent's). (3) If it's still ambiguous, the class must override.

### Interface vs abstract class: what's still different? [▶ ~46:42](https://youtu.be/YLLFHuStkW8?t=2802)
- After Java 8/9, interfaces can have default (concrete), static and private methods plus constants, which sounds a lot like an abstract class. The differences that remain:
- **Intent** (the biggest difference):
  - An interface represents a **role or capability**. It's often named like an adjective or verb: `Runnable`, `Walkable`, `Payable` (better than `Payment`). This is a **can-do** relationship [▶ ~50:01](https://youtu.be/YLLFHuStkW8?t=3001): "A implements Runnable" means A *can run*.
  - An abstract class represents a **family of similar classes** with shared code, e.g. `Animal` above `Dog`, `Duck` and `Elephant`, which share `eat()`. This is an **is-a** relationship: a dog *is an* animal.
- **Practical differences**:

| | Interface | Abstract class |
|---|---|---|
| Instance fields | No. Fields are `public static final` | Yes, normal fields |
| Constructors | No [▶ ~51:44](https://youtu.be/YLLFHuStkW8?t=3104) | Yes |
| Multiple inheritance | Yes (`implements A, B`) | No (single `extends`) |
| Member access | `public` by default (`private` helper methods since Java 9) | Any modifier: `private`, `protected`, default, `public` |

> **Correction:** at one point the video says "what do we normally call an interface? Is-a relationship" and then immediately explains **can-do**. The intended mapping is: interface = **can-do**, abstract class or inheritance = **is-a**.

### Functional interfaces [▶ ~52:59](https://youtu.be/YLLFHuStkW8?t=3179)
- An interface with **exactly one abstract method**, e.g. `interface A { void fun(); }`.
- It matters because it unlocks **functional programming** with **lambda expressions** (Java 8). That gets its own lecture.
- JDK examples mentioned: `Comparable`, `Predicate`. Others include `Runnable`, `Comparator`, `Function`, `Supplier` and `Consumer`.

> **Note:** "one method" really means **one abstract method**. A functional interface can still have any number of `default` and `static` methods (e.g. `Comparator` has many), and redeclared `Object` methods such as `equals` don't count. Mark yours with `@FunctionalInterface` so the compiler enforces the rule.

### Marker interfaces [▶ ~54:58](https://youtu.be/YLLFHuStkW8?t=3298)
- An interface with **no methods at all**, used to *mark* a class. The video lists **`Cloneable`, `Serializable` and `RandomAccess`**.
- `Cloneable` and `Object.clone()`: a class can only use `super.clone()` successfully if it `implements Cloneable`. Without it you get `CloneNotSupportedException` at **runtime**. There's nothing to override from `Cloneable`: you're simply telling Java "I opt in to cloning". Critical resources (DB connections, threads) shouldn't be cloned, which is why it's opt-in.

> **Note:** those three aren't the *only* marker interfaces (`java.rmi.Remote` and `java.util.EventListener` are others). Since Java 5, annotations are often used instead of new marker interfaces.

### Interfaces in bytecode [▶ ~59:01](https://youtu.be/YLLFHuStkW8?t=3541)
- `javac Animal.java` produces `Animal.class`. Inside, an interface is stored much like a class, but with the **`ACC_INTERFACE`** flag set (plus `ACC_ABSTRACT`). That's how the JVM tells them apart.
- Same idea as enums (previous video): a special keyword in source, a class-like structure after compilation.

## Code Examples

Each block is a complete program. Save it under the class name (e.g. `InterfaceBasicsDemo.java`) and run it with `java InterfaceBasicsDemo.java`.

### Contract, visibility, abstract partial implementation, dynamic dispatch
```java
public class InterfaceBasicsDemo {
    interface Car { void drive(); }               // implicitly public abstract

    static class Thar implements Car {
        @Override
        public void drive() {                     // must be public (can't narrow access)
            System.out.println("Thar is driving");
        }
    }

    static abstract class Jeep implements Car {}  // doesn't implement drive -> must be abstract

    static class BlackJeep extends Jeep {
        @Override
        public void drive() { System.out.println("BlackJeep is driving"); }
    }

    interface Payment { void pay(); }
    static class CreditCard implements Payment {
        public void pay() { System.out.println("Paying via credit card"); }
    }
    static class DebitCard implements Payment {
        public void pay() { System.out.println("Paying via debit card"); }
    }

    public static void main(String[] args) {
        Car c = new Thar();
        c.drive();                                 // Thar is driving
        c = new BlackJeep();
        c.drive();                                 // BlackJeep is driving

        boolean useCredit = false;                 // could come from config/user input
        Payment p = useCredit ? new CreditCard() : new DebitCard();
        p.pay();                                   // Paying via debit card
    }
}
```

### Interface constants
```java
public class InterfaceConstantsDemo {
    interface MathConstants {
        double PI_VALUE = 3.14;   // implicitly public static final
        int VALUE = 10;
        void fun();
    }

    static class Random implements MathConstants {
        @Override
        public void fun() { System.out.println(PI_VALUE); }  // inherited constant
    }

    public static void main(String[] args) {
        MathConstants r1 = new Random();
        r1.fun();                                    // 3.14
        System.out.println(MathConstants.PI_VALUE);  // 3.14 (no object needed)
        System.out.println(MathConstants.VALUE * 2); // 20
        // MathConstants.VALUE = 5;                  // compile error: final
    }
}
```

### Multiple interfaces + interface inheritance
```java
public class InterfaceInheritanceDemo {
    interface A { void fun(); }
    interface B { void fun2(); }

    static class C implements A, B {               // implement many interfaces
        public void fun()  { System.out.println("fun from A"); }
        public void fun2() { System.out.println("fun2 from B"); }
    }

    interface Animal { void eat(); }
    interface Dog extends Animal { void bark(); }  // interface extends interface

    static class StreetDog implements Dog {        // must implement eat() AND bark()
        public void eat()  { System.out.println("Eating"); }
        public void bark() { System.out.println("Barking"); }
    }

    public static void main(String[] args) {
        C c = new C();
        c.fun();                // fun from A
        c.fun2();               // fun2 from B

        Animal a = new StreetDog();
        a.eat();                // Eating
        ((Dog) a).bark();       // Barking
        System.out.println(a instanceof Animal && a instanceof Dog); // true
    }
}
```

### Default, static and private interface methods
```java
public class DefaultStaticPrivateDemo {
    interface Vehicle {
        default void drive() {                   // Java 8: has a body
            System.out.println("Vehicle is driving");
            accelerate();                        // may call the private helper
        }

        static void brake() {                    // Java 8: called via Vehicle.brake()
            System.out.println("Vehicle is applying brake");
        }

        private void accelerate() {              // Java 9: only usable inside the interface
            System.out.println("Vehicle is accelerating");
        }
    }

    static class Car implements Vehicle {}       // no override needed

    static class SportsCar implements Vehicle {
        @Override
        public void drive() { System.out.println("SportsCar is driving"); }
    }

    public static void main(String[] args) {
        Vehicle v = new Car();
        v.drive();            // Vehicle is driving
                              // Vehicle is accelerating
        new SportsCar().drive(); // SportsCar is driving
        Vehicle.brake();      // Vehicle is applying brake
        // Car.brake();       // compile error: static interface methods aren't inherited
    }
}
```

### Diamond with two defaults: override and pick with `X.super`
```java
public class DefaultDiamondDemo {
    interface B { default void fun() { System.out.println("B"); } }
    interface C { default void fun() { System.out.println("C"); } }

    static class D implements B, C {
        @Override
        public void fun() {              // REQUIRED: B and C both provide fun()
            B.super.fun();               // explicitly call B's default
            C.super.fun();               // ...and then C's
            System.out.println("D");
        }
    }

    public static void main(String[] args) {
        new D().fun();
        // B
        // C
        // D
    }
}
```

### Same conflict without an override (does not compile)
```java
// does not compile
// error: types B and C are incompatible; class D inherits unrelated defaults for fun() from types B and C
public class DiamondNoOverride {
    interface B { default void fun() { System.out.println("B"); } }
    interface C { default void fun() { System.out.println("C"); } }

    static class D implements B, C {}   // must override fun()

    public static void main(String[] args) {
        new D().fun();
    }
}
```

### Resolution priority: the class wins
```java
public class ClassWinsDemo {
    interface A {
        default void fun() { System.out.println("Inside A interface"); }
    }

    static class B {
        public void fun() { System.out.println("Inside B class"); }
    }

    static class C extends B implements A {}           // no error, no override needed

    static class C2 extends B implements A {
        @Override
        public void fun() { System.out.println("Inside C class"); }
    }

    static class OnlyInterface implements A {}

    public static void main(String[] args) {
        new C().fun();              // Inside B class   (superclass beats interface default)
        new C2().fun();             // Inside C class   (own override beats both)
        new OnlyInterface().fun();  // Inside A interface
    }
}
```

### Functional interface + lambda, and a marker interface
```java
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class FunctionalMarkerDemo {
    @FunctionalInterface                     // compiler enforces exactly one abstract method
    interface Greeter {
        String greet(String name);
        default Greeter twice() { return n -> greet(greet(n)); } // defaults are allowed
    }

    interface Auditable {}                   // custom marker interface: no methods

    static class Order implements Auditable {}
    static class Note {}

    static void save(Object o) {
        if (o instanceof Auditable) System.out.println("audit log: " + o.getClass().getSimpleName());
        else System.out.println("no audit: " + o.getClass().getSimpleName());
    }

    public static void main(String[] args) {
        Greeter anon = new Greeter() {       // old way: anonymous class
            public String greet(String name) { return "Hi " + name; }
        };
        Greeter lambda = name -> "Hello " + name;  // lambda: only possible for functional interfaces

        System.out.println(anon.greet("Aditya"));          // Hi Aditya
        System.out.println(lambda.greet("Aditya"));        // Hello Aditya
        System.out.println(lambda.twice().greet("Aditya")); // Hello Hello Aditya

        List<String> names = new ArrayList<>(List.of("Rohit", "Aditya", "Rohan"));
        names.sort(Comparator.naturalOrder());             // Comparator is functional too
        System.out.println(names);                         // [Aditya, Rohan, Rohit]

        save(new Order());   // audit log: Order
        save(new Note());    // no audit: Note
        System.out.println(new ArrayList<String>() instanceof java.util.RandomAccess); // true
    }
}
```

## Common Pitfalls / Tips
- **Write `public` on implemented interface methods.** Leaving it out narrows access and doesn't compile.
- **Interface fields are constants.** Every field is `public static final` and must be initialized. Implementations don't get their own copy.
- **The "constant interface" pattern is considered an anti-pattern** (*Effective Java*, item 22). If a class *implements* a constants interface just to use its constants, they leak into the class's public API. Prefer a `final` class with a private constructor, or an enum, and use `static import` if you want short names.
- **Static interface methods aren't inherited.** Call them through the interface name: `Vehicle.brake()`, not `car.brake()` or `Car.brake()`.
- **Two defaults with the same signature force an override.** Use `Iface.super.method()` to reuse one of them. That syntax only works for **direct** superinterfaces.
- **Class beats interface.** A superclass method (even one you didn't expect) silently wins over a default method.
- **Default methods can't override `Object` methods** (`equals`, `hashCode`, `toString`). Writing one is a compile error.
- **Mark functional interfaces with `@FunctionalInterface`.** Adding a second abstract method then fails fast.
- **Name interfaces after capabilities** (`Payable`, `Drivable`) when that reads naturally. Use abstract classes when subclasses share **state** or constructor logic.
- **`break` is a keyword.** You can't name a method `break` (the video meant `brake`).

## Review Questions
1. Why is an interface called a "blueprint of behavior" and a "contract" rather than a blueprint of an object?
2. Why does `void drive()` in an implementing class fail with "Cannot reduce the visibility…"?
3. What modifiers does Java add implicitly to interface methods and to interface fields, and why `static final` for fields?
4. Why can a class implement many interfaces when it can only extend one class? Explain using the diamond problem before Java 8.
5. Why were `default` methods introduced in Java 8? What did `static` (Java 8) and `private` (Java 9) methods add?
6. `D implements B, C` and both have `default void fun()`. What must `D` do, and how can it reuse B's version?
7. `class C extends B implements A`, where B has `fun()` and A has `default fun()`. Which runs, and why?
8. Name three practical differences between an interface and an abstract class, plus the conceptual one. What are functional and marker interfaces?

<details>
<summary>Answers</summary>

1. It describes *what* a type can do (its methods) without saying *how*, and it has no objects of its own. Any class that implements it promises (contracts) to provide all the methods, or else to be abstract.
2. Interface methods are implicitly `public`. Without a modifier, the class method is package-private, which is more restrictive, and an override can't reduce visibility. Fix it by declaring the method `public`.
3. Methods are `public abstract`. Fields are `public static final`. Fields are `static` because there's no interface instance to hold state, and `final` because interfaces can only define constants.
4. With classes, two parents could both provide a body for `fun()`, so `new D().fun()` would be ambiguous (the diamond problem). Pre-Java-8 interfaces only declared methods, so the single concrete class always supplied the one and only body and nothing was ambiguous.
5. To add methods to existing interfaces (e.g. `List`) without breaking every existing implementation: old classes inherit the default body. Static methods let an interface hold utility methods called as `Iface.m()`. Private methods let default and static methods share helper code without exposing it.
6. `D` must override `fun()`, otherwise compilation fails. Inside, it can call `B.super.fun();` (and/or `C.super.fun();`) or write its own logic.
7. B's `fun()` ("Inside B class"). The resolution rule says a method inherited from a class always takes priority over an interface default.
8. Interfaces have no instance fields, no constructors, public members by default, and support multiple inheritance. Abstract classes have fields and constructors, allow any access modifier, and support only single inheritance. Conceptually, an interface is a *can-do* capability and an abstract class is an *is-a* family. A functional interface has exactly one abstract method (a lambda target). A marker interface has no methods and only tags a class (`Cloneable`, `Serializable`, `RandomAccess`).
</details>

## Further Study
- Lambda expressions, method references and `java.util.function` (`Function`, `Predicate`, `Supplier`, `Consumer`)
- `Comparator` vs `Comparable`
- Dependency injection and programming to interfaces (Spring Boot)
- Sealed interfaces (Java 17) and pattern matching
- Annotations vs marker interfaces
- `Serializable` and Java serialization
- *Effective Java*: item 20 (prefer interfaces to abstract classes), item 21 (design interfaces for posterity), item 22 (use interfaces only to define types)
- JVM class file format: access flags (`ACC_INTERFACE`, `ACC_ABSTRACT`), `javap -v`

---

[← #23](23-java-enums-why-not-constants.md) · [Index](ReadMe.md) · [#25 →](25-java-strings-pool-immutability.md)
