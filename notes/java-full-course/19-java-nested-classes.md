# Java Nested Classes | Static Nested, Inner, Local & Anonymous Classes | Java Full Course #19
> Source: https://youtu.be/0LaBK28_470 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 80:57. The video is in Hindi and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A **nested class** is a class declared inside another class (or block). There are four kinds: **static nested**, **inner** (non-static), **local** (inside a block or method) and **anonymous** (no name, declared and instantiated in one expression).
- Why nest at all? For **logical grouping** (a class that only makes sense inside another), for **encapsulation** (a `private` nested helper class is invisible to the outside world), and for **direct access to the outer class's private members** without getters or setters.
- **Static nested**: `new Outer.Inner()`, no outer object needed. It can reach only the outer class's **static** members directly. It's the most widely used kind (helpers, Builder pattern, DTOs).
- **Inner**: it needs an outer instance (`outer.new Inner()`) and holds a hidden reference to it, so it can access **everything** in the outer class. `Outer.this.x` reaches a shadowed outer field. Static members inside inner classes were illegal **before Java 16**.
- **Local** classes live inside a block and can use local variables only if they are **effectively final**, because the values are *copied* into the object. **Anonymous** classes are one-off subclasses or implementations with no constructor. Lambdas have largely replaced them.

## Key Concepts

**Outer / inner terminology.** The enclosing class is the *outer* class, and the one declared inside it is *nested*. Nesting can go deeper (C inside B inside A), but two levels is the norm.

**Logical grouping.** A class that is meaningful only in the context of another class (an `InterestCalculator` used only by `BankAccount`, a `Node` used only by a linked list) belongs inside it. *Why it matters:* it keeps related code together and reduces clutter at package level.

**Static nested class.** `static class Inner` inside `Outer`. Its full name is `Outer.Inner`. It isn't tied to any outer object, so it behaves exactly like a top-level class that happens to be namespaced inside `Outer`, with privileged access to `Outer`'s private static members.

**Inner (non-static nested) class.** It belongs to an **instance** of the outer class. Each inner object carries a hidden reference to the outer object that created it (`Outer.this`).

**Local class.** A class declared inside a method, constructor, `if`/loop/`switch` block, or static block. It's visible only inside that block.

**Effectively final.** A local variable that is never reassigned after initialization, even without the `final` keyword. Local and anonymous classes (and lambdas) may capture only such variables.

**Anonymous class.** `new Person() { ... }` declares an unnamed subclass of `Person` (or implementation of an interface) and creates one instance of it on the spot. It can't have a constructor because it has no name.

## Detailed Notes

### What and why [00:00]
- In Java you can nest classes, just as you nest loops or `switch` statements.
- The four types [01:15], classified by *how* the inner class is nested: static nested, inner, local, anonymous.
- Why nest [01:53]:
  1. **Logical grouping.** Some classes make sense only inside another class.
  2. **Better access to the outer class.** A nested class is inside the outer class, so it can use the outer class's `private` members without getters or setters. A separate top-level class would need `getX()`/`setX()`.

### Static nested class [05:02]
- Syntax: `class Outer { static class Inner { ... } }`.
- **Creating an object** [06:55]. Static members belong to the class, not to objects (as with `static int x`, accessed as `Outer.x`). A static nested class likewise needs **no outer object**.
  - The JVM finds it through the outer class, so its name is `Outer.Inner` [08:48].
  - `Outer.Inner inner = new Outer.Inner(); inner.fun();` prints "Hello".
- **It can access only the outer class's static members** [10:42].
  - Accessing a static `x` works. Accessing an instance field `y` gives the error "Cannot make a static reference to the non-static field y".
  - Reason: an `Inner` object can exist without any `Outer` object. Which object's `y` would it read?
  - **Workaround** [13:50]: pass an outer reference in, either as a method parameter (`fun(Outer outer) { outer.y }`) or through a constructor that stores it in a field.
- **Private members are fine** [16:21]: `private static int x` is accessible, because the nested class is inside `Outer`.
- **It's a full-fledged class** [16:59]. It can `extend` a class, `implement` interfaces, and have fields, methods, constructors, and **its own static members**. Only its name (`Outer.Inner`) and its lack of access to the outer's instance members are different.
- **Access modifiers** [19:30]. Members of the nested class can be `private`/`protected`/`public`, and so can **the nested class itself**.
- **Example: a private helper class** [20:08].
  - `BankAccount.computeInterest(principal)` delegates to `private static class InterestCalculator` with `static double calculateYearly(p, rate)`, and later `calculateMonthly`, `calculateQuarterly`, and so on.
  - The calculator is completely hidden from outside code. Only `BankAccount`'s methods can use it. This is the main benefit.
- **Use cases** [24:32]:
  - helper classes for the outer class
  - the **Builder design pattern**
  - nested classes that need static methods
  - request/response **DTOs** (Spring Boot, later in the series)

### Inner class [27:03]
- An inner class is any nested class **without** `static`. It belongs to an **object** of the outer class.
- **Creating one** [28:19]:
  - `new Outer.Inner()` gives a compile error, because an outer instance is required.
  - `Outer outer = new Outer(); Outer.Inner inner = outer.new Inner();`. Read it like `outer.x`: "inside the outer object, create a new `Inner`".
  - One-liner [30:50]: `Outer.Inner inner = new Outer().new Inner();`. Use it only when you don't need a reference to the outer object.
- **Memory** [32:43]:
  - The inner object is **not** stored inside the outer object. It's a separate heap object.
  - The compiler adds a hidden field to the inner class holding a reference to the outer object. That's why the two are linked.
- **It can access everything** [35:52]: static and instance members, private or not.
  - The video says each outer object has "its own" inner object, while static nested objects are "shared". *Correction:* inner objects are created explicitly, so one outer object can have zero, one or many inner objects, and each inner object is tied to exactly one outer object. Static nested classes can also have any number of instances. "Static" only means there's no link to an outer instance; it doesn't make the instance shared.
- **Shadowing and `Outer.this`** [37:45]:
  - If `Outer` has `int x = 10` and `Inner` has `int x = 20`, then `x` inside `Inner` is **20** (the inner field shadows the outer one).
  - `Outer.this.x` is **10**.
  - Without a field of the same name, a plain `x` resolves to `Outer.this.x` automatically.
- **Static members inside inner classes** [40:17]:
  - Allowed **since Java 16** (JEP 395, which came with records). In older Java they were a compile error.
- **Why old Java forbade them** [42:48]:
  - `static` means "one per class". But an inner class is itself tied to outer *instances*.
  - Should a `static int x` in `B` (inner to `A`) be one value shared across all `A` objects, or one per `A` object? Java avoided the ambiguity by banning it. If you need static members, make the nested class static.

### Local class [46:34]
- A class declared inside **any code block**: a method, constructor, `if`, `for`/`while`, `switch`, or even a `static` block.
- Rarely used in production, but good to know.
- **Demo** [47:50]:
  - `void greet() { class Local { void sayHello() {...} } Local l = new Local(); l.sayHello(); }`.
  - The class declaration is just a definition. Nothing happens until `new Local()` runs.
  - It's useful when a group of helper methods only matters inside one method. It can usually be replaced by plain code (if/else).
- **Access** [52:52]: it follows the same rules as an inner class. It can see all of the outer class's static and instance members, including private ones.
- **The effectively-final rule** [53:30]:
  - It can read a local variable `y` of the enclosing method. But adding `y++` anywhere gives "Local variable y defined in an enclosing scope must be final or effectively final".
- **Why** [57:16]:
  - Local variables live on the stack and disappear when the method returns. The local class's object lives on the heap and may **outlive** the method, for example if the method returns it.
  - So the compiler **copies** the captured value into a hidden field of the object.
  - If `y` could change later, which value should the copy hold? To avoid that ambiguity, Java demands the variable never change.
- **Returning a local-class object** [67:20]:
  - The type name `Local` doesn't exist outside the method, so the return type must be a supertype, such as **`Object`**, the root class of all classes (covered in its own video). An interface also works.

### Anonymous class [68:36]
- "Anonymous" means a class without a name.
- **Problem**: `Person.introduce()` prints "Hi, I am a person". Exactly one place in the program needs "Hi, I am a guest".
  - Writing `class Guest extends Person` with an override, then `Person p2 = new Guest()`, works, but it's a whole class for one use.
- **Shortcut** [71:45]: `Person p2 = new Person() { @Override void introduce() { ... "Hi, I am a guest" ... } };`. Opening a `{` instead of ending with `;` starts the body of an unnamed subclass.
- **What it can contain** [74:16]:
  - Fields (`String name = "Aditya"`) and extra methods (`greet()`).
  - `p2.greet()` and `p2.name` **don't compile**. The reference type is `Person`, which has neither. Extra members can be used only *inside* the overridden methods. For example, `introduce()` can call `greet()` and print "Hello" then "Hi, I am Aditya".
  - *Nuance:* with `var p2 = new Person() {...}` (Java 10+), the inferred type is the anonymous type, so `p2.greet()` does compile.
- **No constructors** [77:25]: a constructor must be named after its class, and this class has no name. *(It can still pass arguments to the superclass constructor, `new Person("x") {...}`, and use an instance initializer block `{ ... }`.)*
- **When to use it** [78:03]: short, localized, one-off behavior. If you need the behavior repeatedly, write a proper class.
- **Rules**: the same as for inner classes (full access to the outer class's members). If it's declared inside a method, it's also local, so the effectively-final rule applies.

### How often each kind is used [79:18]
1. **Static nested**: by far the most common.
2. **Inner**: less common, but you'll see it.
3. **Anonymous**: widely used in old Java and now mostly replaced by **lambdas**.
4. **Local**: rare.

## Code Examples

Each block is a complete program. Save it under the class's name and run it with `java ClassName.java`.

### Static nested class: creation, static-only access, and the workaround
```java
public class StaticNestedDemo {
    public static void main(String[] args) {
        Outer o = new Outer();
        Outer.Inner a = new Outer.Inner(o);   // no outer object needed for the class itself
        Outer.Inner b = new Outer.Inner(o);   // many instances are fine
        a.fun();                              // 4
                                              // 0
        System.out.println(Outer.Inner.count); // 2
    }
}

class Outer {
    private static int x = 4;   // private static: accessible from the nested class
    int y;                      // instance field: NOT directly accessible

    static class Inner {
        static int count = 0;   // static members allowed in a static nested class
        private final Outer outer;

        Inner(Outer outer) { this.outer = outer; count++; }

        void fun() {
            System.out.println(x);         // OK: static member of Outer
            // System.out.println(y);      // error: Cannot make a static reference to the non-static field y
            System.out.println(outer.y);   // OK via an explicit reference
        }
    }
}
```

### Private static nested helper (BankAccount / InterestCalculator)
```java
public class BankAccountDemo {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount();
        System.out.println(acc.computeInterest(1000));        // 90.0
        System.out.println(acc.computeMonthlyInterest(1200)); // 9.0
        // BankAccount.InterestCalculator c;  // error: InterestCalculator has private access
    }
}

class BankAccount {
    private static class InterestCalculator {      // invisible outside BankAccount
        static double calculateYearly(double principal, double rate) {
            return principal * rate;
        }
        static double calculateMonthly(double principal, double rate) {
            return principal * rate / 12;
        }
    }

    public double computeInterest(double principal) {
        return InterestCalculator.calculateYearly(principal, 0.09);
    }

    public double computeMonthlyInterest(double principal) {
        return InterestCalculator.calculateMonthly(principal, 0.09);
    }
}
```

### Inner class: creation syntax, shadowing, `Outer.this`
```java
public class InnerClassDemo {
    public static void main(String[] args) {
        Shop shop = new Shop();
        Shop.Counter c1 = shop.new Counter();       // needs an outer instance
        c1.show();
        // 20
        // 20
        // 10
        // Shop

        Shop.Counter c2 = new Shop().new Counter(); // one-liner when you don't need the outer ref
        System.out.println(c2.x);                   // 20
    }
}

class Shop {
    private int x = 10;
    private static String name = "Shop";

    class Counter {                       // inner (non-static)
        int x = 20;                       // shadows Shop.x
        void show() {
            System.out.println(x);            // 20
            System.out.println(this.x);       // 20
            System.out.println(Shop.this.x);  // 10
            System.out.println(name);         // Shop (static outer member also fine)
        }
    }
}
```

### Local class and the effectively-final rule
```java
public class LocalClassDemo {
    private static int x = 4;

    static Object makeGreeter(String who) {   // 'who' is effectively final
        int y = 5;                            // effectively final (never reassigned)
        class Local {                         // local class
            @Override
            public String toString() {
                return "Hello " + who + ", x=" + x + ", y=" + y;
            }
        }
        // y++;   // would break the code above: y must be final or effectively final
        return new Local();                  // outlives the method; returned as Object
    }

    public static void main(String[] args) {
        Object g = makeGreeter("Aditya");
        System.out.println(g);   // Hello Aditya, x=4, y=5  (captured copies still available)
    }
}
```

### Anonymous class: one-off override, extra members, no constructor
```java
public class AnonymousDemo {
    public static void main(String[] args) {
        Person p1 = new Person();
        p1.introduce();                      // Hi, I am a person

        Person p2 = new Person() {           // unnamed subclass of Person
            String name = "Aditya";          // extra field
            { System.out.println("init block runs instead of a constructor"); }
            void greet() { System.out.println("Hello"); }   // extra method
            @Override
            void introduce() {
                greet();                     // usable only from inside
                System.out.println("Hi, I am " + name);
            }
        };
        // init block runs instead of a constructor
        p2.introduce();
        // Hello
        // Hi, I am Aditya
        // p2.greet();                       // error: greet() is undefined for the type Person

        Runnable r = new Runnable() {        // anonymous implementation of an interface
            @Override public void run() { System.out.println("running"); }
        };
        r.run();                             // running

        Runnable lambda = () -> System.out.println("running (lambda)"); // modern replacement
        lambda.run();                        // running (lambda)
    }
}

class Person {
    void introduce() { System.out.println("Hi, I am a person"); }
}
```

### Things that don't compile
```java
// does not compile

public class NestedErrors {
    public static void main(String[] args) {
        Box.InnerPart ip = new Box.InnerPart();     // inner needs an outer instance: box.new InnerPart()
        int count = 0;
        Runnable r = new Runnable() {
            public void run() { System.out.println(count); }
        };
        count++;                                    // count is no longer effectively final
    }
}

class Box {
    int value = 1;
    static class StaticPart {
        void read() { System.out.println(value); } // static nested can't see instance field
    }
    class InnerPart {}
}
```

## Common Pitfalls / Tips
- **Prefer `static` nested classes.** If a nested class doesn't need the outer instance, make it `static`. Inner objects keep a hidden reference to the outer object, which can cause **memory leaks** (for example, a long-lived listener keeping a large outer object alive).
- **Inner-class creation syntax is `outer.new Inner()`**, not `new Outer.Inner()`.
- **`Outer.this.field`** is how you reach a shadowed outer field from an inner class.
- **"Effectively final" also applies to lambdas.** A common workaround is a one-element array or an `AtomicInteger`, but often the better fix is restructuring the code.
- **Members added to an anonymous class can't be reached** through the supertype reference. Use them inside the overridden methods, or use `var`.
- **Anonymous classes vs lambdas**: for functional interfaces (`Runnable`, `Comparator`), use a lambda. Anonymous classes are still needed when you subclass a *class*, need state, or must implement more than one method.
- **`this` inside an anonymous class** refers to the anonymous object. Inside a lambda, it refers to the enclosing instance.
- **Static members in inner classes** compile only on Java 16+. Keep that in mind if your code must build on older JDKs.

## Review Questions
1. Name the four kinds of nested classes and say how each is declared.
2. Give two reasons to nest a class.
3. How do you create an instance of a static nested class, and of an inner class?
4. Why can't a static nested class access the outer class's instance fields directly, and how can you work around it?
5. Inside an inner class with a field `x` that shadows the outer `x`, how do you print the outer one?
6. Why did Java (before 16) forbid static members in inner classes?
7. What does "effectively final" mean, and why must local and anonymous classes capture only such variables?
8. Why can't an anonymous class have a constructor, and why does `p2.greet()` fail when `p2` is declared as `Person`?

<details>
<summary>Answers</summary>

1. Static nested is `static class B` inside `A`. Inner is `class B` inside `A` without `static`. Local is a class declared inside a method or another block. Anonymous is `new Type() { ... }`, an unnamed subclass or implementation created in one expression.
2. Logical grouping (the class only makes sense inside the outer one, such as a helper or a `Node`), and encapsulation/access (it can be `private` and hidden, and it can use the outer class's private members without getters and setters).
3. Static nested: `Outer.Inner i = new Outer.Inner();`. Inner: `Outer o = new Outer(); Outer.Inner i = o.new Inner();` (or `new Outer().new Inner()`).
4. A static nested object can exist without any outer object, so there's no particular outer object whose field it could read. Pass an outer reference in, through a method parameter or a constructor field, and use `outer.y`.
5. `Outer.this.x`.
6. An inner class is tied to outer *instances*, while `static` means one per class. It was ambiguous whether a static field should be shared across all outer objects or exist per outer object, so Java disallowed it until Java 16 relaxed the rule.
7. A local variable that is never reassigned after initialization, even without `final`. The local or anonymous object can outlive the method's stack frame, so the compiler copies the value into the object. If the variable could change, the copy and the original could disagree.
8. Constructors must be named after the class, and an anonymous class has no name (use an initializer block or pass arguments to the superclass constructor instead). `p2.greet()` fails because the compiler checks members against the reference type `Person`, which doesn't declare `greet()`.
</details>

## Further Study
- Builder design pattern (built on a static nested class)
- Lambdas and functional interfaces: how they replace most anonymous classes
- Variable capture with lambdas; `this` in lambdas vs anonymous classes
- The `Object` class (`toString`, `equals`, `hashCode`), covered in an upcoming video
- JEP 395 (Java 16): static members in inner classes; local records, enums and interfaces
- Memory leaks from inner classes (e.g., listeners and handlers holding the outer instance)
- How `javac` compiles nested classes (`Outer$Inner.class`, `Outer$1.class`, synthetic `this$0` fields)

---

[← #18](18-autoboxing-abstract-classes-pojos.md) · [Index](README.md) · [#20 →](20-java-io-deep-dive.md)

---

[← #18](18-autoboxing-abstract-classes-pojos.md) · [Index](README.md) · [#20 →](20-java-io-deep-dive.md)
