# 17 · Abstraction & Polymorphism · Abstract Class vs Interface
> Full notes: [17-abstraction-and-polymorphism.md](../17-abstraction-and-polymorphism.md) · [▶ Video](https://youtu.be/-L9BcU6Xk2c)

## What abstraction is
- **Definition:** focus on **what** something does, and ignore **how** it does it.
- **A car** is understood through its data (color, type, price) and its behavior (drive, accelerate, brake), not through its roughly 30,000 parts.
- **Layers:** without abstraction, every driver would have to be an engineer, every engineer a physicist, and every physicist a mathematician. Abstraction lets each layer use the one below without fully understanding it.
- **An ATM** is understood as deposit / withdraw / check balance. In code, those behaviors become **methods** of a class.

## OOP simulates perception, not reality
- "OOP simulates the real world" is only half true. It simulates **how humans think about** the real world.
- A `Teacher` class has `teach()`, `takeAttendance()`, a name and an age. It doesn't model cells and organs.
- **Two rules come out of this:**
  1. **Represent only what's necessary.**
  2. **Users of a model don't need to know how it's built, only how to use it.** The car maker gives you a steering wheel, pedals and a gearbox, and hides the rest.

## Low-level vs high-level abstraction
Java does abstraction at two levels:

| Level | Meaning | How |
| :--- | :--- | :--- |
| **Low-level** | hide the implementation details | a plain class: `Car c = new Car(); c.start();` works without ever opening the class |
| **High-level** | separate the **what** from the **how** | a common parent (an abstract class or interface) with many implementations |

## High-level abstraction: decoupling the what from the how
- **The problem with concrete classes:** `Car hondaCity = new Car()` ties the reference to **one** implementation. The *what* and the *how* are **tightly coupled**.
- **Add `FuelCar` and `ElectricCar`.** Both `drive()`, but they work completely differently inside (combustion vs electric, gears vs none). As separate classes, you'd need `FuelCar fc`, `ElectricCar ec`, and later `DieselCar`, `WaterCar`, … each with its own reference type.
- **The fix:** a common parent `Car`, with `FuelCar extends Car` and `ElectricCar extends Car`. **A parent reference can point to any child:**
  ```java
  Car c = new ElectricCar();   // or new FuelCar()
  c.drive();                   // runs the implementation of whichever object it is
  ```
- Shared behavior (`applyHandBrake()`) is written **once** in the parent and inherited. Each child provides its **own** `drive()`.

---

## Abstract classes
**The problem:** if the parent `Car` writes a generic `accelerate()` ("Car is accelerating at 10 km/h"), it's meaningless. *Which* car? Every subclass overrides it anyway.

**The fix:** **declare** the method without **defining** it, the same way `int x;` is a declaration and `x = 4;` is a definition.
```java
abstract void accelerate();     // no body
```

**Rules**
1. Without the `abstract` keyword, the compiler complains that the method needs a body.
2. **If a class has even one abstract method, the class itself must be `abstract`.**
3. **An abstract class can't be instantiated** (`new Car()` is an error). Otherwise, `new Car().accelerate()` would call a method with no body.
4. An abstract class **can still have fully defined methods** (such as a common `start()`), and children inherit them.
5. Every **concrete** subclass **must implement all the inherited abstract methods**. Otherwise you get *"FuelCar must implement the inherited abstract method …"*.
6. A subclass that doesn't want to implement them can itself be declared **`abstract`**, so the hierarchy can go deeper.
7. **`@Override`** is optional but good practice. It tells the compiler you mean to override, so typos get caught.

```java
public class AbstractCarDemo {
    public static void main(String[] args) {
        Car car = new ElectricCar();
        car.start();          // Car started                    ← inherited from the parent
        car.accelerate();     // Electric car is accelerating   ← the child's version
        car.brake();          // Electric car is stopping

        car = new FuelCar();  // switch the object; the code that uses it doesn't change
        car.start();          // Car started
        car.accelerate();     // Fuel car is accelerating
        car.brake();          // Fuel car is stopping
    }
}

abstract class Car {
    void start() { System.out.println("Car started"); }   // concrete: shared by all cars
    abstract void accelerate();                            // abstract: each car decides
    abstract void brake();
}

class ElectricCar extends Car {
    @Override void accelerate() { System.out.println("Electric car is accelerating"); }
    @Override void brake()      { System.out.println("Electric car is stopping"); }
}

class FuelCar extends Car {
    @Override void accelerate() { System.out.println("Fuel car is accelerating"); }
    @Override void brake()      { System.out.println("Fuel car is stopping"); }
}
```

---

## Interfaces
- The syntax looks like an abstract class, but the **idea is different**. **A class is a blueprint of an object. An interface has nothing to do with objects: it's a contract.**
- Interface methods are **abstract by default** (in classic Java), so you don't write `abstract`.
- A class **`implements`** an interface, but **`extends`** a class.
- In classic Java, the implementing class must define **all** the methods. Java 8+ also allows `default` methods (#24).
- `new Car()` on an interface gives the same *"Cannot instantiate"* error. `Car car = new FuelCar();` works as before.
- **The error *"Cannot reduce the visibility of the inherited method"*:** interface methods are implicitly **`public`**, so the implementations must be **`public`** too. An override can **widen** visibility, but never **narrow** it.

```java
public class InterfaceDemo {
    public static void main(String[] args) {
        Flyable[] things = { new Bird(), new Airplane() };
        for (Flyable f : things) f.fly();
        // Bird flies by flapping its wings
        // Airplane flies with engines and aerodynamics
    }
}

interface Flyable {                 // a capability, i.e. a contract
    void fly();                     // implicitly public and abstract
}

class Bird implements Flyable {
    public void fly() { System.out.println("Bird flies by flapping its wings"); }   // must be public
}

class Airplane implements Flyable {
    public void fly() { System.out.println("Airplane flies with engines and aerodynamics"); }
}
```

## Why Java has interfaces (C++ doesn't)
1. **Pure "what" (pure abstraction).** A classic interface defines **no behavior at all**, only what can be done. An abstract class gives a **partial what + how**. (Java 8+ `default` methods blur this line.)
2. **Intent: a contract.** An interface is like a loan agreement with a bank. It describes **responsibilities, capabilities or roles**: "if you want to call yourself X, you must implement these methods."
   - **Naming convention:** use capability names like `Comparable`, `Serializable`, `Runnable`, `Flyable`, `Walkable`. `Car` is a weak interface name. C# uses an `I` prefix (`ICar`), and some Java teams copy that, but it's a convention, not a rule.
   - **Abstract classes represent families** of similar objects: `Car` is the parent of `ElectricCar` and `FuelCar`, implements what they share, and leaves the rest to them.
3. **Multiple inheritance.** Java doesn't support it through classes, but it does through interfaces: **a class can implement many interfaces** (#24).

| | Abstract class | Interface |
| :--- | :--- | :--- |
| Represents | a **family** (is-a) | a **contract / capability** (can-do) |
| Methods | abstract **and** concrete | abstract (plus `default`/`static` since Java 8) |
| Abstraction | partial (what + some how) | pure (what only, in classic Java) |
| Keyword | `extends` (only one) | `implements` (many) |
| Method visibility | any | implicitly `public` |
| Can be instantiated? | no | no |

**Choosing between them is a design decision.** Use an abstract class for an "is-a" family, and an interface for a contract or capability.

## Abstraction vs encapsulation
| | Abstraction | Encapsulation |
| :--- | :--- | :--- |
| Idea | hide **complexity**, show only what's needed | bundle data + methods, and **protect** the data |
| How | abstract classes, interfaces | `private` fields + `public` methods |
| Car example | you *can* open the bonnet and study the engine, you just don't *have to* | you can't directly set the speedometer reading |

- **The video's one-liner:** *abstraction is about hiding, and encapsulation is about security.*
- **More precisely:** `private` is a design boundary checked by the compiler, not real security (reflection can get around it). Abstraction hides **complexity**. Encapsulation hides **state** and protects its rules.

---

## Polymorphism
*Poly* = many, *morph* = forms.

### Type 1: same object, same command, one parameter changes → overloading
- A human told to "run" runs at 2 km/h, but at 5 km/h if a dog is chasing them.
- In code: `run()` and `run(boolean isDogBehind)`. This is **method overloading**.
- It's called **compile-time (static) polymorphism**, because the **compiler** picks the method from the arguments at the call site.

### Type 2: different objects, same command → overriding
- A dog, a duck and a human all "run", each in its own way.
- In code: `abstract class Animal { abstract void run(); }` with `Dog`, `Duck` and `Human` each overriding `run()`. This is **method overriding**.
- It's called **run-time (dynamic) polymorphism**, because which class gets created, and so which `run()` runs, is only known **while the program runs**:
  ```java
  Animal a;
  if (type.equals("duck")) a = new Duck(); else a = new Dog();
  a.run();      // decided at runtime
  ```

```java
public class PolymorphismDemo {
    public static void main(String[] args) {
        // Compile-time: overloading
        Human h = new Human();
        h.run();          // Human runs at 2 km/h
        h.run(true);      // Human runs at 5 km/h (dog behind!)

        // Run-time: overriding
        String[] types = {"dog", "duck", "human"};
        for (String type : types) {
            Animal a = switch (type) {
                case "dog"  -> new Dog();
                case "duck" -> new Duck();
                default     -> new Human();
            };
            a.run();      // the JVM picks the method from the actual object
        }
        // Dog runs on four legs
        // Duck waddles
        // Human runs at 2 km/h
    }
}

abstract class Animal { abstract void run(); }

class Dog extends Animal  { void run() { System.out.println("Dog runs on four legs"); } }
class Duck extends Animal { void run() { System.out.println("Duck waddles"); } }

class Human extends Animal {
    void run() { System.out.println("Human runs at 2 km/h"); }
    void run(boolean isDogBehind) {                                 // overload
        System.out.println(isDogBehind ? "Human runs at 5 km/h (dog behind!)" : "Human runs at 2 km/h");
    }
}
```

| Kind | Also called | Mechanism | Decided by |
|---|---|---|---|
| Compile-time | static polymorphism | **overloading** | the **compiler**, from the argument types |
| Run-time | dynamic polymorphism | **overriding** | the **JVM**, from the object's actual class |

---

## What doesn't take part in overriding
| Member | Can it be overridden? | What happens |
| :--- | :--- | :--- |
| **`static` method** | ❌ (it's **hidden**) | it belongs to the class, so `A a = new B(); a.fun();` runs **A's** version. `@Override` on it doesn't compile. |
| **`private` method** | ❌ | the child can't see it, so a method with the same name in the child is a completely **separate** method |
| **`final` method** | ❌ | compile error: *"Cannot override the final method from A"*. Use it when every subclass must keep the parent's behavior. |
| **Field** | ❌ (fields aren't polymorphic) | `A a = new B(); a.x` uses the **reference type**, so you get A's `x` |
| **`final` class** | ❌ can't be extended | compile error: *"The type B cannot subclass the final class A"* |

```java
public class NotPolymorphicDemo {
    public static void main(String[] args) {
        A a = new B();
        a.fun();                       // Hello  (static: picked by the reference type A)
        System.out.println(a.x);       // 10     (field: picked by the reference type A)
        System.out.println(a.getX());  // 20     (method: picked by the object, B)
    }
}

class A {
    int x = 10;
    static void fun() { System.out.println("Hello"); }
    int getX() { return x; }
}

class B extends A {
    int x = 20;                        // hides A.x, doesn't override it
    static void fun() { System.out.println("Bye"); }   // hides A.fun()
    @Override int getX() { return x; } // a real override
}
```
- **To get B's field through an `A` reference,** wrap it in an overridable method (`getX()`).

**Remember:** abstraction separates the **what** from the **how**. Use an **abstract class** for a family (partial implementation, one parent) and an **interface** for a contract (pure what, many allowed, `public` methods). Polymorphism is **overloading** (compile-time) plus **overriding** (run-time). `static`, `private` and `final` methods, and fields, are **not** overridden.
