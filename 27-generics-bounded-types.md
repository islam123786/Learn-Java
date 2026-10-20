# Java Generics Deep Dive | Bounded Types using extends | Java Full Course #27
> Source: https://youtu.be/Y7X_ReDF3x8 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 65:07. The video is in Hindi; these notes were written from its Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A **type** is a set of rules: it says which values a variable can hold and which operations and methods are allowed on it. Java checks these rules, and you want it to catch mistakes at **compile time**, not at runtime.
- **Upcasting** (child to parent, e.g. `Object o = "hi"`) is automatic and safe. **Downcasting** (parent to child, e.g. `(String) o`) needs an explicit cast and can fail at runtime with a `ClassCastException`.
- Using `Object` as a "holds anything" type works, but you **lose the type information**. Every read then needs a manual downcast, and a wrong cast only shows up at runtime.
- **Generics** (`class Box<T>`) let you write one general-purpose class or method while keeping full type information. The compiler knows `Box<Integer>` holds an `Integer`, so there are no casts and mistakes become compile errors.
- **Bounded types** (`<T extends Number>`) limit which types are allowed. In exchange you get to call the bound's methods (e.g. `doubleValue()`) on `T`. You can combine one class bound with several interface bounds: `<T extends Animal & Swimmable>`.

## Key Concepts

**Type safety.** Every type defines what you can do with its values: arithmetic on `int`, `String` methods on a `String`, `Student` methods on a `Student`. Java is a statically typed language and refuses operations that make no sense (`int x = "hello";`). *Why it matters:* it's what generics are designed to preserve.

**Upcasting.** Treating a specific object as its more general parent type: `Animal a = new Dog();` or `Object obj = "hello";`. No cast keyword is needed because a child always *is a* parent. Through the parent reference you can only call the parent's methods (no `a.bark()`). *Why it matters:* this is what lets `Object` hold anything, and it's also why you lose access to the specific methods.

**Downcasting.** Turning a general reference back into a specific one: `String s = (String) obj;`. The compiler can't prove it's correct, so you have to write the cast explicitly. The JVM checks it at runtime and throws `ClassCastException` if the object is actually something else (for example an `Integer`). *Why it matters:* every downcast is a possible runtime crash.

**Compile-time vs runtime errors.** Compile-time errors show up while you write the code: the IDE underlines them, they never reach production, and they need no debugging. Runtime errors appear only when that line runs, possibly in production. *Why it matters:* good design moves errors to compile time, and that is exactly what generics do.

**Generic class.** `class Box<T> { private T value; ... }`. `T` is a **type parameter**, a placeholder name (any identifier works). You fill it in when you use the class: `Box<Integer> b = new Box<>(10);`. `Integer` here is the **type argument**. The terms mirror method parameters and arguments. *Why it matters:* you write the class once, it works for every type, and nothing is cast.

**Generic method.** `static <T> T identity(T x)`. The `<T>` before the return type declares the method's own type parameter. The compiler usually **infers** `T` from the arguments (`identity("hi")` gives `T = String`). *Why it matters:* you get reusable utility methods that are still type-safe.

**Raw type.** Using a generic class without a type argument (`Box b = new Box(10);`). It compiles with an "unchecked / raw type" warning and behaves like the old `Object` version. *Why it matters:* it exists only for backward compatibility, so avoid it.

**Bounded type parameter (upper bound).** `<T extends Number>` means `T` must be `Number` or one of its subtypes (`Integer`, `Double`, `Float`, `Long`, …). Inside the class you may call any `Number` method on a `T`. *Why it matters:* an unbounded `T` only lets you call `Object` methods (`toString`, `hashCode`, …).

**Multiple bounds.** `<T extends ClassName & Interface1 & Interface2>`. At most one class, and it must come first, followed by any number of interfaces. *Why it matters:* you can require, say, "an `Animal` that is also `Swimmable`".

## Detailed Notes

### What type safety means [▶ ~00:20](https://youtu.be/Y7X_ReDF3x8?t=20)
- Values have types: `10` is an `int`, `"hello"` is a `String`, `true` is a `boolean`.
- A type is a **set of rules** for what you can do with the value: arithmetic on numbers, `String` methods on strings, your own class's methods on your objects.
- Java enforces these rules. `int x = 10.2;` and `int y = s;` (where `s` is a `String`) are compile errors. Java is a **typed** language and won't run "unknown" operations.

### Casting for primitives, then upcasting [▶ ~03:29](https://youtu.be/Y7X_ReDF3x8?t=209)
- Recap of primitives: `long l = x;` (int to long) is a **widening** conversion and happens automatically. `int i = l;` (long to int) is **narrowing** and needs `(int) l`. The cast tells the compiler "I know what I'm doing."
- For objects the same idea is called up- and downcasting. Example hierarchy: `class Animal` with `walk()`, `run()`, `eat()`, and `class Dog extends Animal` that adds `bark()`.
- `Animal a = new Dog();` is legal because a parent reference can point to a child object. The `Dog` lives on the heap and the reference `a` on the stack.
  - Through `a` you can call only `Animal` methods. `a.bark()` doesn't compile. To call `bark()` you need a `Dog` reference.
- `Dog d = new Dog(); Animal a = d;` is **upcasting**: both references point to the same object, but `a` sees it as a general `Animal`.
- `String s = "hello"; Object obj = s;` is also upcasting, because `Object` is the parent of every class. Through `obj` you only get `Object` methods (`toString`, `equals`, `hashCode`, …).
- Upcasting needs **no manual cast**, just like int to long.

### Downcasting and `ClassCastException` [▶ ~10:29](https://youtu.be/Y7X_ReDF3x8?t=629)
- `Object obj = "hello"; String s = obj;` is a **compile error**. `obj` could hold anything (`Integer`, `Boolean`, `Student`, …), so the compiler won't trust it.
- `String s = (String) obj;` compiles. You've told the compiler the conversion is intentional.
- This is the dangerous part: `Object obj = 10;` (autoboxed to `Integer`) followed by `String s = (String) obj;` **compiles fine** but throws **`ClassCastException`** at runtime.
- Why doesn't the compiler catch it when it can "see" `10`? In real code the value usually comes from user input, a file or an API call, so it isn't known until the program runs. The compiler's rules are written for the general case. The JVM checks the actual class at runtime.

### Demo of up- and downcasting [▶ ~15:16](https://youtu.be/Y7X_ReDF3x8?t=916)
- Upcast: `Object obj = s; System.out.println(obj);` prints `hello`. `println` calls `toString()` for you.
- Downcast without a cast gives the compile error "Type mismatch: cannot convert from Object to String". With `(String)` it works and prints `Aditya`.
- Downcasting an `Object` that holds `10` to `String` compiles, then throws `ClassCastException` when run.

### Compile-time errors beat runtime errors [▶ ~19:36](https://youtu.be/Y7X_ReDF3x8?t=1176)
- Two kinds of errors: compile-time (`int x = "hello";`) and runtime (`ClassCastException`).
- Compile-time is **always preferred**: you catch it early, there's no production bug, it gives safer code, the IDE shows it with a red underline, and you skip the debugging.

### The problem generics solve: a `Box` for any type [▶ ~21:10](https://youtu.be/Y7X_ReDF3x8?t=1270)
- A `Box` class holds one value. It has a private field, a constructor, a getter and a setter.
- Version 1: `private int value;`. That works for integers only. `new Box("hello")` won't compile.
- One class per type (`Box2` for `String`, `Box3` for `double`, …) is clearly not a real solution.
- Version 2 [▶ ~24:19](https://youtu.be/Y7X_ReDF3x8?t=1459): `private Object value;`. Everything fits (`new Box(10)`, `new Box("hello")`, `new Box(true)`), but:
  - `b1.getValue() + 5` doesn't compile ("operator + is undefined for the argument type Object"). The compiler can't tell whether `+` means addition, concatenation or nonsense.
  - You have to downcast every time: `Integer x = (Integer) b1.getValue();`.
  - **Type information is lost.** Nothing stops you from putting the wrong type in or casting to the wrong type. The errors move to runtime as `ClassCastException`s.
- Conclusion: `Object` as a universal type is a poor way to build general-purpose classes.

### Generic `Box<T>` [▶ ~31:11](https://youtu.be/Y7X_ReDF3x8?t=1871)
- Replace `Object` with a placeholder `T` everywhere (field, constructor parameter, getter return type, setter parameter), then declare it on the class: `class Box<T>`.
  - `T` is just a name, and `X`, `Z` or `Q` would work too. By convention it's `T` for "type", `E` for element, `K`/`V` for key/value.
  - Without `<T>` on the class you get the error "T cannot be resolved to a type".
- `<T>` on the class is the **type parameter**. You supply it when creating an object.
- **Raw type** [▶ ~34:08](https://youtu.be/Y7X_ReDF3x8?t=2048): `Box b1 = new Box(10);` gives only a warning ("Box is a raw type…"). Java then treats it as if `T` were `Object`.
- **Type argument** [▶ ~35:15](https://youtu.be/Y7X_ReDF3x8?t=2115): `Box<Integer> b1 = new Box<>(10);`. The `Integer` is the argument. You can leave the right side empty (`<>`, the diamond operator) because it's inferred from the left.
- Now `b1.getValue() + 5` compiles and prints `15`. `Box<String>` gives `hello5` and `Box<Boolean>` gives `false`. There are no casts.
- Wrong casts are now **compile errors**:
  - `String s = (String) b1.getValue();` on a `Box<Integer>` gives "Cannot cast from Integer to String". `Integer` and `String` are **siblings** (both children of `Object`). Casting only works along a parent-child line.
  - `b1.getValue().substring(1)` doesn't compile, because `Integer` has no `substring`.
- Type information is **not lost**: `b1` knows it holds an `Integer`, `b2` a `String`, `b3` a `Boolean`.

### Generic class with two type parameters: `Pair<T, U>` [▶ ~41:25](https://youtu.be/Y7X_ReDF3x8?t=2485)
- Start with a `Pair` class holding two `int`s (`first`, `second`) and a constructor.
- Make it generic as `Pair<T>`: both fields are `T`, so `Pair<String>` can hold `("name", "Aditya")`.
- Make it more general with two independent types [▶ ~44:30](https://youtu.be/Y7X_ReDF3x8?t=2670): `class Pair<T, U> { T first; U second; }`, used as `Pair<Integer, String> p = new Pair<>(23, "Aditya");`.

### Generic methods and type inference [▶ ~45:18](https://youtu.be/Y7X_ReDF3x8?t=2718)
- Start with `static int getResult(int x)`. It only works for `int`.
- An `Object` version (`static Object getResult(Object x)`) needs casts at the call site and can fail at runtime, the same problem as before.
- Generic version: `static <T> T getResult(T x) { return x; }`. The general form is:
  `<T> ReturnType methodName(T param)`, with the type-parameter list **before the return type**.
- `String y = getResult("hello");` needs no cast. `String y = getResult(23);` is a compile error.
- Two type parameters: `static <T, U> void printPair(T first, U second)`.
- **Type inference** [▶ ~52:08](https://youtu.be/Y7X_ReDF3x8?t=3128): you never write the types when calling. `printPair(11, "hello")` gives `T = Integer, U = String`, and `printPair(11, 23)` makes both `Integer`. The compiler infers them from the arguments.

### Why restrict a generic? Bounds [▶ ~53:09](https://youtu.be/Y7X_ReDF3x8?t=3189)
- Unbounded `T` accepts everything. Sometimes you want "numbers only", for example to do real arithmetic (`5 + 2` should be `7`, not `"52"`) or to call number-only methods.
- All numeric wrappers (`Integer`, `Double`, `Float`, `Long`, `Short`, `Byte`, …) extend **`java.lang.Number`**. `String` and `Boolean` don't.
- `Number` declares `doubleValue()`, `intValue()`, and so on. For example `Integer x = 4; double d = x.doubleValue();` gives `4.0`.

### Upper bound with `extends` [▶ ~55:39](https://youtu.be/Y7X_ReDF3x8?t=3339)
- In `class Box<T> { T value; void printDouble() { value.doubleValue(); } }`, the call doesn't compile ("method is undefined for the type T"). The compiler only knows that `T` is *some* `Object`, so only `Object` methods (`toString`, `hashCode`, …) are allowed.
- **Upper bound** [▶ ~58:39](https://youtu.be/Y7X_ReDF3x8?t=3519): `class Box<T extends Number>`. This says `T` is `Number` or one of its subtypes, and nothing else.
  - Now `value.doubleValue()` compiles.
  - `Box<Integer>`, `Box<Double>`, `Box<Float>` and `Box<Number>` are all OK.
  - `Box<String>` is a compile error: "Bound mismatch: The type String is not a valid substitute for the bounded parameter `<T extends Number>`".
- Before the bound, "T can be anything". With the bound, "T is at least a `Number` (or a subtype)". `b1.value = 5; b1.printDouble();` prints `5.0`.
- *(The video calls the method "getDouble" at one point. The real `Number` method is `doubleValue()`.)*

### Multiple bounds: class plus interfaces [▶ ~63:31](https://youtu.be/Y7X_ReDF3x8?t=3811)
- Hierarchy: `Animal` (with `display()`), `Dog extends Animal`, and `Fish extends Animal implements Swimmable`, where `interface Swimmable { void swim(); }`.
- `class Box<T extends Animal>` accepts `Animal`, `Dog` and `Fish`.
- `class Box<T extends Animal & Swimmable>` accepts only `Fish`. `Box<Dog>` and `Box<Animal>` give "Bound mismatch", because neither implements `Swimmable`.
- Standard form [▶ ~64:17](https://youtu.be/Y7X_ReDF3x8?t=3857): `<T extends SomeClass & Interface1 & Interface2>`. You use `extends` for the interfaces too (there's no `implements` here), and **the class must come first**.
- *(Correction: the video writes the interface list with a comma, `Interface1, Interface2`. In real Java every extra bound is joined with `&`. A comma would start a second type parameter instead.)*
- Next video: **wildcards** (`?`, `? extends`, `? super`).

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java ClassName.java`.

### Upcasting, downcasting, `ClassCastException`
```java
public class CastingDemo {
    static class Animal { void eat() { System.out.println("Animal eating"); } }
    static class Dog extends Animal { void bark() { System.out.println("Woof"); } }

    public static void main(String[] args) {
        Animal a = new Dog();          // upcast: automatic
        a.eat();                       // Animal eating
        // a.bark();                   // compile error: Animal has no bark()

        Object obj = "hello";          // upcast to Object
        String s = (String) obj;       // downcast: explicit, OK at runtime
        System.out.println(s.toUpperCase()); // HELLO

        Object num = 10;               // autoboxed Integer
        try {
            String bad = (String) num; // compiles, fails at runtime
            System.out.println(bad);
        } catch (ClassCastException e) {
            System.out.println("ClassCastException caught"); // ClassCastException caught
        }
    }
}
```

### The `Object` box problem vs a generic `Box<T>`
```java
public class GenericBoxDemo {
    // Old way: holds anything, but loses type information
    static class ObjectBox {
        private Object value;
        ObjectBox(Object value) { this.value = value; }
        Object getValue() { return value; }
    }

    // Generic way: T is fixed when you create the object
    static class Box<T> {
        private T value;
        Box(T value) { this.value = value; }
        T getValue() { return value; }
        void setValue(T value) { this.value = value; }
    }

    public static void main(String[] args) {
        ObjectBox ob = new ObjectBox(10);
        // int bad = ob.getValue() + 5;          // compile error: + undefined for Object
        int x = (Integer) ob.getValue() + 5;     // manual downcast needed
        System.out.println(x);                    // 15

        Box<Integer> b1 = new Box<>(10);
        Box<String> b2 = new Box<>("hello");
        Box<Boolean> b3 = new Box<>(false);
        System.out.println(b1.getValue() + 5);   // 15
        System.out.println(b2.getValue() + 5);   // hello5
        System.out.println(b3.getValue());       // false
        System.out.println(b2.getValue().substring(1)); // ello
        // String s = (String) b1.getValue();    // compile error: cannot cast Integer to String
    }
}
```

### Two type parameters: `Pair<T, U>`
```java
public class GenericPairDemo {
    static class Pair<T, U> {
        T first;
        U second;
        Pair(T first, U second) { this.first = first; this.second = second; }
    }

    public static void main(String[] args) {
        Pair<Integer, Integer> p1 = new Pair<>(2, 3);
        Pair<String, String> p2 = new Pair<>("name", "Aditya");
        Pair<Integer, String> p3 = new Pair<>(23, "Aditya");
        System.out.println(p1.first + ", " + p1.second); // 2, 3
        System.out.println(p2.first + ", " + p2.second); // name, Aditya
        System.out.println(p3.first + ", " + p3.second); // 23, Aditya
    }
}
```

### Generic methods and type inference
```java
public class GenericMethodDemo {
    static <T> T getResult(T x) {             // <T> goes before the return type
        return x;
    }

    static <T, U> void printPair(T first, U second) {
        System.out.println(first + ", " + second);
    }

    public static void main(String[] args) {
        String s = getResult("hello");         // T inferred as String, no cast
        Integer n = getResult(23);             // T inferred as Integer
        System.out.println(s);                 // hello
        System.out.println(n + 1);             // 24
        printPair(11, "hello");                // 11, hello
        printPair(11, 23);                     // 11, 23
        System.out.println(GenericMethodDemo.<Double>getResult(2.5)); // 2.5 (explicit type argument)
        // String bad = getResult(23);         // compile error
    }
}
```

### Upper-bounded type parameter `<T extends Number>`
```java
public class BoundedBoxDemo {
    static class NumberBox<T extends Number> {
        T value;
        NumberBox(T value) { this.value = value; }
        void printDouble() {
            System.out.println(value.doubleValue()); // allowed: T is at least a Number
        }
    }

    // A bounded generic method: works for any list of numbers
    static <T extends Number> double sum(java.util.List<T> nums) {
        double total = 0;
        for (T n : nums) total += n.doubleValue();
        return total;
    }

    public static void main(String[] args) {
        new NumberBox<>(5).printDouble();      // 5.0
        new NumberBox<>(2.5f).printDouble();   // 2.5
        new NumberBox<Number>(7L).printDouble(); // 7.0
        // new NumberBox<>("hi");              // compile error: Bound mismatch
        System.out.println(sum(java.util.List.of(1, 2, 3)));   // 6.0
        System.out.println(sum(java.util.List.of(1.5, 2.5)));  // 4.0
    }
}
```

### Multiple bounds: `<T extends Animal & Swimmable>`
```java
public class MultipleBoundsDemo {
    static class Animal { void display() { System.out.println("Displaying animal"); } }
    interface Swimmable { void swim(); }
    static class Dog extends Animal { }
    static class Fish extends Animal implements Swimmable {
        public void swim() { System.out.println("Fish swimming"); }
    }

    static class SwimmerBox<T extends Animal & Swimmable> { // class first, then interfaces with &
        T value;
        SwimmerBox(T value) { this.value = value; }
        void show() {
            value.display();  // Animal method
            value.swim();     // Swimmable method
        }
    }

    public static void main(String[] args) {
        SwimmerBox<Fish> box = new SwimmerBox<>(new Fish());
        box.show();
        // Displaying animal
        // Fish swimming
        // SwimmerBox<Dog> bad;                // compile error: Dog is not Swimmable
    }
}
```

## Common Pitfalls / Tips
- **Upcasting is free, downcasting is a promise.** If the promise is wrong you get a `ClassCastException` at runtime. Use `instanceof` (or pattern matching, `if (obj instanceof String s)`) before you downcast.
- **Sibling types can't be cast to each other.** `(String) someInteger` is a compile error because `Integer` and `String` have no parent-child relationship. Through an `Object` reference it compiles, then fails at runtime.
- **Don't use raw types.** `Box b = new Box(10);` silently turns off the generic checks. Always supply the type argument, or at least the diamond `<>`.
- **The diamond `<>` only works on the right-hand side.** `Box<Integer> b = new Box<>(10);` is fine. For a generic method you rarely need explicit types, because inference does it.
- **Generic type arguments must be reference types.** You write `Box<Integer>`, not `Box<int>`. Autoboxing converts `10` to `Integer` for you (see video #28 for why).
- **Unbounded `T` only exposes `Object` methods.** If you need `compareTo`, `doubleValue`, etc., add a bound: `<T extends Comparable<T>>` or `<T extends Number>`.
- **Multiple-bound syntax:** `<T extends Class & I1 & I2>`. Use `&`, not commas, and put the class first. Only one class is allowed.
- **`extends` in a bound covers interfaces too.** `<T extends Comparable<T>>` means "T implements Comparable". There's no `implements` keyword inside `<>`.

## Review Questions
1. What does "type safety" mean, and why are compile-time errors preferred over runtime errors?
2. What is the difference between upcasting and downcasting? Which one needs an explicit cast, and why?
3. Why does `String s = (String) obj;` compile even when `obj` holds an `Integer`? What happens when it runs?
4. What problems come with using `Object` as a universal type for a `Box` class?
5. In `class Box<T>` and `new Box<Integer>(5)`, which part is the type parameter and which is the type argument?
6. How do you declare a generic method, and how does Java decide what `T` is when you call it?
7. Why doesn't `value.doubleValue()` compile in `class Box<T>`, and how does `<T extends Number>` fix it?
8. Write the bound for "a type that is an `Animal` and also implements `Swimmable` and `Comparable`".

<details>
<summary>Answers</summary>

1. Every type defines the values and operations allowed on it, and Java checks these rules. Compile-time errors are found while you write the code (IDE support, no production bugs, no debugging). Runtime errors only appear when the code runs, possibly in production.
2. Upcasting goes child to parent (`Animal a = dog;`). It's always safe, so it's automatic. Downcasting goes parent to child (`Dog d = (Dog) a;`). It might be wrong, so you must write the cast explicitly, and the JVM verifies it at runtime.
3. The compiler only knows the static type `Object`, which *could* be a `String`, so the cast is allowed. When it runs, the JVM sees the actual object is an `Integer` and throws `ClassCastException`.
4. Type information is lost. You need a manual downcast on every read, nothing stops wrong values or wrong casts, and the mistakes show up as runtime `ClassCastException`s instead of compile errors.
5. `<T>` in the class declaration is the type parameter (the placeholder). `Integer` in `Box<Integer>` is the type argument (the actual type you pass in).
6. Put the type-parameter list before the return type: `static <T> T identity(T x)`. When you call it, the compiler infers `T` from the argument types (type inference). For example `identity("a")` gives `T = String`.
7. For an unbounded `T` the compiler only knows it's some `Object`, so only `Object` methods are callable. `<T extends Number>` guarantees `T` is a `Number` or one of its subtypes, so `Number`'s methods such as `doubleValue()` become available. Non-numbers like `String` are rejected at compile time.
8. `<T extends Animal & Swimmable & Comparable<T>>`. The class comes first, then the interfaces joined with `&`.
</details>

## Further Study
- Wildcards: `?`, `? extends T`, `? super T` and the PECS rule (next video, #28)
- Type erasure: what generics compile to, and why primitives aren't allowed
- Recursive bounds such as `<T extends Comparable<? super T>>` (as used in `Collections.sort`, `Collections.max`)
- Pattern matching for `instanceof` (Java 16+) as a safer way to downcast
- Generic interfaces (`Comparable<T>`, `Iterable<T>`) and how the Collection Framework uses `E`, `K` and `V`
- Autoboxing and unboxing between primitives and wrapper classes

---

[← #26](26-java-string-methods-stringbuilder-buffer.md) · [Index](ReadMe.md) · [#28 →](28-generics-wildcards.md)
