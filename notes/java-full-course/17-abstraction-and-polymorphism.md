# Abstraction & Polymorphism in Depth | Abstract Class vs Interfaces | Java Full Course #17
> Source: https://youtu.be/-L9BcU6Xk2c · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 88:01. The video is in Hindi and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- **Abstraction** means focusing on *what* something does and ignoring *how* it does it. People understand cars, ATMs and phones through their behaviors, not their internals. OOP models that human view of reality, not reality itself.
- Java gives you two levels of it. **Low-level abstraction** (hide implementation details) comes for free with ordinary classes: you call `c.start()` without reading its body. **High-level abstraction** (separate the *what* from the *how*) is done with **abstract classes** and **interfaces**, plus a parent-type reference such as `Car c = new ElectricCar()`.
- An **abstract class** can mix implemented methods with `abstract` (declared-only) methods. It can't be instantiated, and concrete subclasses must implement every abstract method. It models a **family of similar objects**.
- An **interface** (classic Java) declares only behavior. It's a **contract** or **capability** (`Flyable`, `Runnable`, `Comparable`), it isn't a blueprint of an object, and it's how Java supports multiple inheritance of type.
- **Polymorphism** ("many forms") comes in two kinds. **Compile-time** polymorphism is method **overloading**. **Run-time** polymorphism is method **overriding** with dynamic dispatch. `static`, `private` and `final` methods are not overridden, and **fields are never polymorphic**.

## Key Concepts

**Abstraction.** "The process of focusing on what something does while ignoring how it does it." *Why it matters:* nobody could use a car if they first had to learn combustion engines, physics and the maths underneath. Software is the same: callers should only need the operations.

**Low-level vs high-level abstraction.** Low-level means hiding implementation details: the user of a `Car` class just calls `start()`. High-level means separating the *what* from the *how*, so that one call (`c.drive()`) can run different implementations depending on the actual object. *Why it matters:* with only concrete classes, each reference is tied to one implementation (tight coupling).

**Concrete class.** An ordinary class whose methods all have bodies, so you can create objects of it with `new`. *Why it matters:* it's the opposite of an abstract class.

**Abstract method / abstract class.** An `abstract` method has a signature and no body. Any class that contains one must itself be declared `abstract`. An abstract class can't be instantiated (`new Car()` fails with "Cannot instantiate the type Car"). *Why it matters:* the parent can define the shared behavior (`start()`) and force each child to supply the specific behavior (`accelerate()`, `brake()`).

**Interface.** A type declared with `interface` whose methods are implicitly `public abstract` (in classic Java). Classes `implements` it rather than `extends` it. *Why it matters:* it describes a role or capability that unrelated classes can share (a bird and an airplane are both `Flyable`).

**Contract.** An interface works like a contract. "If you want to call yourself a `Car` (or `Flyable`), you must provide these methods." The compiler enforces it.

**Polymorphism.** One name, many forms. Compile-time polymorphism: the same object responds differently to the same command when the arguments differ (overloading, resolved by the compiler). Run-time polymorphism: different objects respond differently to the same command (overriding, resolved by the object's actual class while the program runs).

**Abstraction vs encapsulation.** In the video's framing, encapsulation is about bundling data with methods **and protecting data** through access modifiers. Abstraction is about **hiding complexity** so people can use something without understanding it.

## Detailed Notes

### What abstraction is [01:23]
- Definition: focus on what something does, ignore how it does it.
- The whole world runs on abstraction. A **car** is understood through its data (color, type such as SUV/hatchback/sedan, price) and its behaviors (drive, accelerate, brake), not through its roughly 30,000 parts.
- The chain: without abstraction every driver would have to be an engineer, every engineer a physicist, and every physicist a mathematician. Abstraction lets each layer use the layer below without fully understanding it.
- An **ATM** is understood as deposit / withdraw / check balance. In code those behaviors become **methods** of a class.

### OOP simulates perception, not reality [06:17]
- "OOP simulates the real world" is a half-truth. It simulates **how humans think about** the real world.
- A `Teacher` class has `teach()` and `takeAttendance()` and some data (name, age, gender). It does not model cells and organs.
- Two rules come out of this:
  1. **Represent only what's necessary.**
  2. **The users of a model don't need to know how it's implemented, only how to use it.** The car maker gives you a steering wheel, pedals and a gear box (interfaces) and hides the rest.

### Low-level vs high-level abstraction in Java [12:35]
- Java implements abstraction at two levels:
  - **Low-level**: hide the implementation details.
  - **High-level**: separate the *what* from the *how*.
- **Low-level** [13:17]: write a `Car` class with `start()`, `accelerate()`, `brake()`. Another developer's `main` (in a different file) does `Car c = new Car(); c.start();` without ever opening the class. Plain classes, the bundling part of encapsulation, already give you this.

### High-level abstraction: decoupling what from how [17:29]
- **Concrete classes** tie a reference to one implementation. With `Car hondaCity = new Car()`, `hondaCity.accelerate()` always runs that one `accelerate()`. The *what* and the *how* are **tightly coupled**.
- Add a `FuelCar` and an `ElectricCar` [20:59]. Both `drive()`, but internally they are completely different (combustion vs electric, gears vs none). As separate classes you need `ElectricCar ec = ...`, `FuelCar fc = ...`, then later `DieselCar`, `WaterCar`, and so on, each with its own reference type.
- The fix [24:28]: a common parent `Car` with `FuelCar extends Car` and `ElectricCar extends Car`. A parent reference can point to any child, so `Car c = new ElectricCar()` or `new FuelCar()` works.
- Shared behavior such as `applyHandBrake()` lives once in the parent and is inherited. Each child provides its own `drive()`. Now `c.drive()` picks the implementation based on the object, so the what and the how are separated.

### Abstract classes [28:40]
- If the parent `Car` writes a generic `accelerate()` ("Car is accelerating at 10 km/h"), the code is meaningless. *Which* car? Each concrete subclass overrides it anyway.
- So **declare** the method without **defining** it, the way `int x;` is a declaration and `x = 4;` is a definition: `abstract void accelerate();`
- The rules [36:22]:
  - Without the `abstract` keyword, the compiler complains that the method requires a body.
  - **If a class has even one abstract method, the class itself must be `abstract`.**
  - **An abstract class can't be instantiated.** Otherwise `new Car().accelerate()` would call a method with no body.
  - An abstract class may still have **fully defined methods**, such as a common `start()`, which children inherit.
  - Every concrete subclass **must implement all inherited abstract methods**. Otherwise the error is "FuelCar must implement the inherited abstract method …".
  - A subclass that doesn't want to implement them can itself be declared `abstract`, and the hierarchy can go deeper.
- In the demo [39:52], `Car car = new ElectricCar()` prints "Car started / Electric car is accelerating / Electric car is stopping". Changing it to `new FuelCar()` switches the last two lines, while `start()` still comes from the parent.
- `@Override` [42:40] is optional but good practice. It tells the compiler you mean to override, so typos get caught.

### Interfaces [45:27]
- The syntax looks like an abstract class, but the idea is different. **A class is a blueprint of an object. An interface has nothing to do with objects.** It's a **contract**.
- `interface Car { void start(); void accelerate(); void brake(); }`. No `abstract` keyword is needed because interface methods are abstract by default (in classic Java).
- Classes `implements` an interface (they `extends` a class): `class FuelCar implements Car`.
- In classic Java the implementing class must define **all** the methods, including `start()`. Java 8+ allows `default` methods (covered in a later video).
- `new Car()` on an interface gives the same "Cannot instantiate" error. `Car car = new FuelCar()` works exactly as before.
- **The "Cannot reduce the visibility of the inherited method" error** [61:33]: interface methods are implicitly `public`, so the implementations must be declared `public` too. An override can widen visibility but never narrow it.

### Why Java has interfaces (C++ doesn't) [50:21]
1. **Pure "what" (pure abstraction).** Classic interfaces define no behavior, only what can be done. Abstract classes give a **partial what + how**. *(Java 8+ default methods blur this line.)*
2. **Intent** [51:45]: interfaces are **contracts**, like a loan agreement with a bank. They describe **responsibilities / capabilities / roles**. If something wants to call itself a `Car`, it must implement these methods.
   - Naming convention [55:15]: capability names such as `Comparable`, `Serializable`, `Runnable`, `Flyable`, `Walkable`. `Bird` and `Airplane` both implement `Flyable`, and each defines `fly()` its own way (flapping wings vs engines and aerodynamics). `Car` is a weak interface name. C# uses an `I` prefix (`ICar`), and some Java teams copy that. It's a convention, not a rule.
   - **Abstract classes represent families of similar objects** [58:03]: `Car` is the parent of `ElectricCar` and `FuelCar`, implements what they share and leaves the rest to them.
   - Choosing between them is a design decision. Use an abstract class for an "is-a family", and an interface for a contract or capability.
3. **Multiple inheritance** [59:27]: not supported through classes, but supported through interfaces. A class can implement many interfaces. Details come in the interfaces deep-dive.

### Abstraction vs encapsulation [63:39]
- **Encapsulation**: (1) bundle data and methods into a class, and (2) **data security**: sensitive data is protected with access modifiers.
- **Abstraction**: the world is complex, so hide the implementation. That's **hiding, not security**. You can open the car's bonnet and study the engine if you want; you just don't have to.
- The video's one-liner: *abstraction is about hiding, encapsulation is about security.*
- *Nuance:* `private` is a design boundary checked by the compiler, not real security (reflection can get around it). A better summary is that abstraction hides **complexity** and encapsulation hides **state** and protects invariants.

### Polymorphism [65:45]
- *Poly* = many, *morph* = forms.
- **Type 1: same object, same command, one parameter changes** [66:26]. A human told to "run" runs at 2 km/h, and at 5 km/h if a dog is chasing them.
  - In code: `run()` and `run(boolean isDogBehind)`. That's **method overloading**.
  - It's called **compile-time polymorphism** [69:56] because the compiler picks the overload from the arguments at the call site.
- **Type 2: different objects, same command, same situation** [70:38]. A dog, a duck and a human all "run", each in its own way.
  - In code: `abstract class Animal { abstract void run(); }` with `Dog`, `Duck`, `Human` overriding `run()`. That's **method overriding**.
  - It's called **run-time (dynamic) polymorphism** [74:08]. With `Animal a; if (type.equals("duck")) a = new Duck(); else ...`, which class gets instantiated, and therefore which `run()` executes, is only known while the program runs.
- Summary table:

| Kind | Also called | Mechanism | Decided |
|---|---|---|---|
| Compile-time | static polymorphism | overloading | by the compiler, from the argument types |
| Run-time | dynamic polymorphism | overriding | by the JVM, from the object's actual class |

### What doesn't take part in overriding [76:56]
- **`static` methods aren't overridden.** They belong to the class, not to objects. With `A a = new B(); a.fun();` where both declare `static void fun()`, you get **A's** version ("Hello", not "Bye"), because the call binds to the reference type. *(The precise term is method **hiding**. Putting `@Override` on a static method doesn't compile.)*
- **`private` methods can't be overridden** [80:26]. The child can't even see them, so a same-named method in the child is a completely separate method.
- **`final` methods can't be overridden** [81:50]. The error is "Cannot override the final method from A". Use `final` when every subclass must keep the parent's behavior.
- **Fields aren't polymorphic** [83:14]. With `int x = 10` in A and `int x = 20` in B, `A a = new B(); a.x` gives **10**, because fields resolve by the reference type. To get B's value you need a B reference, or turn the field into an overridable method (`int getX()`). Then `a.getX()` gives 20.
- **`final` class** [86:44]: it can't be subclassed. The error is "The type B cannot subclass the final class A".
- Closing thought: once abstraction, encapsulation and inheritance make sense, polymorphism is simply overloading plus overriding.

## Code Examples

Each block is a complete program. Save it under the class's name and run it with `java ClassName.java`.

### Abstract class with a shared concrete method
```java
public class AbstractCarDemo {
    public static void main(String[] args) {
        // Car c = new Car();             // compile error: Cannot instantiate the type Car
        Car car = new ElectricCar();
        car.start();       // Car started
        car.accelerate();  // Electric car is accelerating
        car.brake();       // Electric car is stopping (regenerative)

        car = new FuelCar();               // same reference type, different "how"
        car.start();       // Car started
        car.accelerate();  // Fuel car is accelerating
        car.brake();       // Fuel car is stopping
    }
}

abstract class Car {
    void start() {                       // shared, fully defined
        System.out.println("Car started");
    }
    abstract void accelerate();          // declared only: children must define
    abstract void brake();
}

class FuelCar extends Car {
    @Override void accelerate() { System.out.println("Fuel car is accelerating"); }
    @Override void brake()      { System.out.println("Fuel car is stopping"); }
}

class ElectricCar extends Car {
    @Override void accelerate() { System.out.println("Electric car is accelerating"); }
    @Override void brake()      { System.out.println("Electric car is stopping (regenerative)"); }
}
```

### Interface as a contract / capability
```java
public class FlyableDemo {
    public static void main(String[] args) {
        Flyable[] things = { new Bird(), new Airplane() };
        for (Flyable f : things) {
            f.fly();
        }
        // Bird flaps its wings
        // Airplane uses engines and aerodynamics
    }
}

interface Flyable {
    void fly();                 // implicitly public abstract
}

class Bird implements Flyable {
    @Override
    public void fly() {         // must be public: can't reduce visibility
        System.out.println("Bird flaps its wings");
    }
}

class Airplane implements Flyable {
    @Override
    public void fly() {
        System.out.println("Airplane uses engines and aerodynamics");
    }
}
```

### Compile-time (overloading) vs run-time (overriding) polymorphism
```java
public class PolymorphismDemo {
    static Animal create(String type) {                    // decided at run time
        if (type.equals("duck")) return new Duck();
        else if (type.equals("human")) return new Human();
        else return new Dog();
    }

    public static void main(String[] args) {
        Human h = new Human();
        h.run();          // Human runs at 2 km/h   (overload chosen at compile time)
        h.run(true);      // Human runs at 5 km/h

        for (String t : new String[]{"dog", "duck", "human"}) {
            Animal a = create(t);
            a.run();      // overriding: the object's class decides
        }
        // Dog runs on four legs
        // Duck waddles
        // Human runs at 2 km/h
    }
}

abstract class Animal {
    abstract void run();
}

class Dog extends Animal   { @Override void run() { System.out.println("Dog runs on four legs"); } }

class Duck extends Animal  { @Override void run() { System.out.println("Duck waddles"); } }

class Human extends Animal {
    @Override void run() { System.out.println("Human runs at 2 km/h"); }
    void run(boolean isDogBehind) {                        // overload
        System.out.println(isDogBehind ? "Human runs at 5 km/h" : "Human runs at 2 km/h");
    }
}
```

### static methods are hidden, fields are not polymorphic
```java
public class OverrideRulesDemo {
    public static void main(String[] args) {
        A a = new B();
        a.fun();                       // Hello   (static: bound to reference type A)
        a.show();                      // B.show  (instance method: dynamic dispatch)
        System.out.println(a.x);       // 10      (field: reference type A)
        System.out.println(a.getX());  // 20      (method: overridden)
        System.out.println(((B) a).x); // 20
        a.fun3();                      // final in A
    }
}

class A {
    int x = 10;
    static void fun() { System.out.println("Hello"); }
    void show()       { System.out.println("A.show"); }
    int getX()        { return 10; }
    final void fun3() { System.out.println("final in A"); }
}

class B extends A {
    int x = 20;                                   // hides A.x, does not override it
    static void fun() { System.out.println("Bye"); } // hides A.fun
    @Override void show() { System.out.println("B.show"); }
    @Override int getX()  { return 20; }
    // void fun3() {}  // compile error: Cannot override the final method from A
}
```

### Things that don't compile
```java
// does not compile

public class BrokenAbstraction {
    public static void main(String[] args) {
        Shape s = new Shape();          // error: Cannot instantiate the type Shape
    }
}

abstract class Shape { abstract double area(); }

final class Point {}

class Square extends Shape {}           // error: must implement inherited abstract method area()

class Point3D extends Point {}          // error: cannot subclass the final class Point

interface Greeter { void greet(); }

class LoudGreeter implements Greeter {
    void greet() {}                     // error: cannot reduce the visibility (must be public)
}
```

## Common Pitfalls / Tips
- **One abstract method makes the whole class abstract.** You can't have a "normal" class with a bodiless method.
- **Abstract classes can still have constructors, fields and concrete methods.** The constructor runs when a subclass object is created. "Can't instantiate" only means you can't `new` the abstract class directly.
- **Interface methods are `public`.** Implementations must be `public`, or you get "Cannot reduce the visibility of the inherited method".
- **Modern interfaces aren't "pure what" any more.** Java 8 added `default` and `static` methods, and Java 9 added `private` ones. Interfaces still can't hold instance state, though: their fields are always `public static final` constants.
- **`extends` for classes, `implements` for interfaces.** A class can extend **one** class but implement **many** interfaces. An interface `extends` other interfaces.
- **Don't expect static methods or fields to be polymorphic.** Both resolve by the **reference type**. Call static methods via the class name (`A.fun()`) to avoid confusion.
- **Use `@Override`.** It turns accidental overloads or typos (`brak()`) into compile errors.
- **Choosing**: abstract class for an "is-a" family that shares code and state; interface for a capability that unrelated classes can have ("can-fly", "can-be-compared").
- **Overloading is chosen by the compile-time types of the arguments**, not by their run-time types. Only the receiver object (`a.` in `a.run()`) is dispatched dynamically.

## Review Questions
1. Define abstraction in one sentence and give a non-programming example.
2. What's the difference between low-level and high-level abstraction, and which Java features implement each?
3. Why must a class containing an abstract method be declared abstract, and why can't it be instantiated?
4. Name two differences in *intent* between an abstract class and an interface.
5. Why does `class Bird implements Flyable { void fly() {} }` fail to compile?
6. How does the video distinguish abstraction from encapsulation?
7. Which kind of polymorphism is overloading, and which is overriding? Why are they called compile-time and run-time?
8. Given `A a = new B();` where both classes declare `static void fun()` and `int x`, what do `a.fun()` and `a.x` use, and why?

<details>
<summary>Answers</summary>

1. Focusing on what something does while ignoring how it does it. Example: you drive a car with the steering wheel and pedals without knowing how the engine works.
2. Low-level hides implementation details, and ordinary classes (encapsulation) give you that. High-level separates the "what" from the "how" so that one call can have many implementations. Abstract classes, interfaces and parent-type references (inheritance plus overriding) implement it.
3. An abstract method has no body. If you could create an object of the class, someone could call that method and nothing would run. Java prevents this by requiring the class to be abstract, and forbids `new` on abstract classes.
4. An abstract class models a family of similar objects and can share implementation and state. An interface is a contract or capability (roles and responsibilities) that unrelated classes can sign up to. Interfaces also allow multiple inheritance of type, while classes allow only single inheritance.
5. Interface methods are implicitly `public`. The implementation uses package-private access, which reduces visibility, and that isn't allowed. It must be `public void fly()`.
6. Encapsulation bundles data and methods and protects (secures) data with access modifiers. Abstraction hides complexity and implementation details so things are easier to use. It's about hiding, not security.
7. Overloading is compile-time (static) polymorphism: the compiler picks the method from the argument list. Overriding is run-time (dynamic) polymorphism: the JVM picks the method from the actual class of the object, which may only be known while the program runs.
8. Both use **A's** versions. Static methods belong to the class and bind to the reference type (they are hidden, not overridden), and fields also resolve by the reference type. Only instance methods are dispatched dynamically.
</details>

## Further Study
- Interfaces in depth: `default`, `static` and `private` interface methods, and the diamond problem
- Functional interfaces and lambdas (`Runnable`, `Comparator`)
- `Comparable` vs `Comparator`, `Serializable` (marker interfaces)
- Method hiding vs overriding; covariant return types; overloading resolution rules
- Sealed classes and interfaces (Java 17)
- SOLID principles, especially Dependency Inversion ("program to an interface")
- Template Method and Strategy design patterns (abstract classes vs interfaces in practice)

---

[← #16](16-encapsulation-inheritance-packages-super.md) · [Index](README.md) · [#18 →](18-autoboxing-abstract-classes-pojos.md)

---

[← #16](16-encapsulation-inheritance-packages-super.md) · [Index](README.md) · [#18 →](18-autoboxing-abstract-classes-pojos.md)
