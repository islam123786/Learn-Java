# Wildcards in Generics | ?, ? extends, ? super  | Java Full Course #28
> Source: https://youtu.be/Opb2q2WSyes · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 82:23. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- **Generics are invariant.** `Dog` is an `Animal`, but `List<Dog>` is **not** a `List<Animal>`. If it were, you could add a plain `Animal` to a list of dogs and break type safety.
- **Arrays are covariant.** `Animal[] a = new Dog[10];` compiles, but storing a non-`Dog` throws **`ArrayStoreException` at runtime**. Generics chose compile-time safety instead.
- **Wildcards** restore flexibility:
  - `List<?>` accepts any list. You can only read elements as `Object` and you can't add anything (except `null`).
  - `List<? extends Animal>` (upper bound, **covariant**) accepts `List<Animal>`, `List<Dog>`, `List<Cat>`. You can **read** as `Animal` but **not add**.
  - `List<? super Animal>` (lower bound, **contravariant**) accepts `List<Animal>`, `List<Object>`. You can **add** `Animal`s and their subtypes, but you can only read as `Object`.
- **PECS**: *Producer Extends, Consumer Super*. Use `extends` when the collection gives you data and `super` when it receives data.
- **Type erasure**: the JVM knows nothing about generics. The compiler checks the types, replaces `T` with `Object` (or with its bound), inserts casts, and generates bridge methods. That's why `instanceof List<String>` and `List<int>` aren't allowed, and why you can't overload on `List<String>` vs `List<Integer>`.

## Key Concepts

**Invariance.** Even if `A` is a subtype of `B`, `G<A>` is unrelated to `G<B>`. *Why it matters:* it's the reason a method taking `List<Animal>` rejects your `List<Dog>`.

**Covariance (arrays).** `Dog[]` *is* treated as a subtype of `Animal[]`. It's convenient but unsafe: every array store is checked at runtime and a bad store throws `ArrayStoreException`. *Why it matters:* it shows exactly the bug that generics prevent at compile time.

**Unbounded wildcard `?`.** "A list of some specific type I don't know." *Why it matters:* it's useful for methods that only need `Object`-level behavior (printing, `size()`, `clear()`), but it's too loose for anything else.

**Upper-bounded wildcard `? extends T`.** "A list of `T` or some subtype of `T`." Reading is safe because every element is at least a `T`. Writing is unsafe because the real list might be a `List<Cat>` when you're trying to add a `Dog`. *Why it matters:* it's what you want for a read-only parameter such as `printAll(List<? extends Animal>)`.

**Lower-bounded wildcard `? super T`.** "A list of `T` or some supertype of `T`." Writing a `T` (or a subtype) is safe, because any such list can hold a `T`. Reading only gives `Object`. *Why it matters:* it's what you want for a write-only parameter such as `addDogs(List<? super Dog>)`.

**PECS.** Producer Extends, Consumer Super. A collection that *produces* values for you to read uses `extends`. A collection that *consumes* values you write uses `super`. `Collections.copy(List<? super T> dest, List<? extends T> src)` is the textbook example. *Why it matters:* it's a quick rule for choosing the right wildcard.

**Type parameter `T` vs wildcard `?`.** `T` *declares* a named type that you can refer to again (on classes, methods, fields). `?` only appears in *uses* of a generic type (parameter types, variable types) and means "some unknown type". *Why it matters:* `class Box<?>` and `<?> void f()` are syntax errors.

**Type erasure.** At compile time the compiler checks generic types, then removes them. The bytecode contains raw types, with `Object` or the bound in place of `T`, plus automatic casts. *Why it matters:* it explains many generics restrictions and kept pre-Java-5 code working.

**Bridge method.** A synthetic method the compiler adds so that overriding still works after erasure (e.g. `Object get()` that calls your `String get()`). *Why it matters:* it explains extra methods you see in stack traces and reflection.

## Detailed Notes

### Recap and the big question [▶ ~01:19](https://youtu.be/Opb2q2WSyes?t=79)
- A generic class `Box<T>` holds a `T value`. `T` is just a placeholder.
- The headline claim: **generics break the parent-child relationship.**
- Inheritance recap: `Animal a = new Dog();` works. Through `a` you can call `eat()` and `walk()`, but not `bark()`. For `bark()` you need a `Dog d`.

### Quick intro to `List` / `ArrayList` [▶ ~03:52](https://youtu.be/Opb2q2WSyes?t=232)
- `List` is an interface, and `ArrayList` is a class that implements it. It's like an array that grows as you `add` (details in the Collection videos).
- `List<Integer> list = new ArrayList<>(); list.add(2); list.add(3);`. The type argument fixes what the list holds.

### `List<Dog>` is not a `List<Animal>` [▶ ~06:54](https://youtu.be/Opb2q2WSyes?t=414)
- `List<Dog> dogs = new ArrayList<>(); List<Animal> animals = dogs;` is a **compile error**.
- You can say "a Dog **is an** Animal" but **not** "a list of Dogs is a list of Animals". There's no relationship at all.
- General rule: `G<A>` is not a subtype of `G<B>`, even when `A` extends `B`.

### Why: type safety would break [▶ ~09:26](https://youtu.be/Opb2q2WSyes?t=566)
- Suppose it were allowed, so `animals` points at the list of dogs.
  - Reading is fine: `animals.get(0)` returns a `Dog`, and a `Dog` is an `Animal`.
  - Writing is the problem: `animals.add(new Animal())` would put a plain `Animal` into the dog list.
  - Another developer later loops `for (Dog d : dogs) d.bark();` and crashes on the element that isn't a `Dog`.
- So Java forbids it. This property is **invariance**: generics are invariant [▶ ~13:12](https://youtu.be/Opb2q2WSyes?t=792).

### Arrays are covariant and fail at runtime [▶ ~13:54](https://youtu.be/Opb2q2WSyes?t=834)
- `Dog[] dogs = new Dog[10]; Animal[] animals = dogs;` **compiles**, because arrays don't use generics.
- `animals[2] = new Animal();` also compiles, but every slot of that array is really a `Dog` slot, and a `Dog` slot can't hold a plain `Animal` (`Dog d = new Animal()` is illegal). The JVM catches it **at runtime**.
- Demo [▶ ~17:39](https://youtu.be/Opb2q2WSyes?t=1059): classes `Animal` (with `eat()`, `walk()`) and `Dog` (with `bark()`).
  - `List<Animal> animals = dogs;` gives "Type mismatch: cannot convert from `List<Dog>` to `List<Animal>`".
  - With arrays: fill `animals[0..3]` with dogs and then do `animals[4] = new Animal()`. The result is **`ArrayStoreException`** [▶ ~22:05](https://youtu.be/Opb2q2WSyes?t=1325). (Null slots are skipped in the loop to avoid an NPE.)
  - *(Correction: the exception is thrown at the store, `animals[4] = ...`, not later in the loop.)*
- Summary: arrays are **covariant but unsafe**. Generics are **invariant but safe**.

### The cost of invariance [▶ ~24:49](https://youtu.be/Opb2q2WSyes?t=1489)
- `static void printAnimals(List<Animal> a)` can't be called with a `List<Dog>`, even though every dog is an animal. With a single `Animal` parameter, a `Dog` argument would work. Generics are what block it.
- The fix is **wildcards** [▶ ~26:15](https://youtu.be/Opb2q2WSyes?t=1575).

### Unbounded wildcard `?` [▶ ~26:15](https://youtu.be/Opb2q2WSyes?t=1575)
- `List<?> list = new ArrayList<Dog>();` (or `<Animal>`, `<String>`, …). "A list of *some* specific type, not known yet."
- `static void fun(List<?> list)` accepts any list.
- **You can't add** [▶ ~28:35](https://youtu.be/Opb2q2WSyes?t=1715): `list.add("hello")` and `list.add(new Dog())` don't compile. The compiler doesn't know whether the list is a `List<String>`, a `List<Dog>`, or something else. The call could come from another file or an API, so it can't assume.
- **Reading is possible, but only as `Object`**: `Object obj = list.get(0);`.
- Demo [▶ ~31:10](https://youtu.be/Opb2q2WSyes?t=1870): `fun(List<Animal>)` rejects a `List<Dog>`. With `fun(List<?> values)` both work. Loop with `for (Object obj : values)` and print `obj.getClass().getName()` to get `Animal Animal` or `Dog Dog`.
  - `values.add(10)` and `values.add(new Dog())` give "The method add(capture#1-of ?) … is not applicable".
- `?` is very flexible but very limited: only `Object` methods, and no adding.
- *(Small addition: you **can** add `null` to a `List<?>`, and you can still call `clear()`, `remove(Object)`, `size()`.)*

### Upper-bounded wildcard `? extends Animal`: covariance [▶ ~38:42](https://youtu.be/Opb2q2WSyes?t=2322)
- `static void fun(List<? extends Animal> values)` accepts a `List<Animal>`, `List<Dog>` or `List<Cat>`. A `List<Integer>` is a compile error.
- **Reading as `Animal` is safe**: `Animal a = values.get(0);` and `for (Animal a : values) a.eat();`. The compiler knows each element is *at least* an `Animal`.
  - If `Dog` overrides `eat()` you get "Dog eating", otherwise "Animal eating", because normal polymorphism still applies.
- Compare with `List<?>` plus a manual `(Animal) obj` cast: that compiles, but passing a `List<Integer>` gives a runtime `ClassCastException`. With `? extends Animal` the wrong list is rejected at compile time.
- **Adding is still not allowed** [▶ ~46:49](https://youtu.be/Opb2q2WSyes?t=2809): `values.add(new Animal())` and `values.add(new Dog())` are compile errors.
  - Why: the caller might have passed a `List<Cat>`. Adding a `Dog` would mix siblings, and adding an `Animal` to a `List<Dog>` would put a parent into a child list. The compiler can't rule either out.
- So `extends` means **read OK, write not OK**, which is **covariance** [▶ ~50:31](https://youtu.be/Opb2q2WSyes?t=3031).

### Lower-bounded wildcard `? super Animal`: contravariance [▶ ~50:51](https://youtu.be/Opb2q2WSyes?t=3051)
- `List<? super Animal>` means "a list of `Animal` or any **supertype** of `Animal`", which here is `List<Animal>` or `List<Object>`. `List<Dog>` and `List<Cat>` are not allowed.
  - `extends` draws a line and allows everything **below** it. `super` allows everything **above** it.
- **Adding is safe**: `l.add(new Animal())`, `l.add(new Dog())`, `l.add(new Labrador())` [▶ ~54:43](https://youtu.be/Opb2q2WSyes?t=3283). Whether the real list is `List<Animal>` or `List<Object>`, it can hold any `Animal` or `Animal` subtype.
- **Reading only as `Object`**: `Animal a = l.get(0);` doesn't compile, because the list might be a `List<Object>`. `Object obj = l.get(0);` works, and you can then cast `(Animal) obj` yourself.
- Demo [▶ ~57:20](https://youtu.be/Opb2q2WSyes?t=3440): `values.add(new Animal())` works, and so do adding `Labrador`, `Dog` and `Cat`. `for (Animal a : values)` gives "Type mismatch: cannot convert from capture#… super Animal to Animal". Reading as `Object` and casting prints "Animal eating" three times.

### Summary and PECS [▶ ~61:52](https://youtu.be/Opb2q2WSyes?t=3712)
| Declaration | Accepts | Read as | Add | Name |
|---|---|---|---|---|
| `List<T>` | exactly `List<T>` | `T` | `T` | invariant |
| `List<?>` | any list | `Object` | nothing (only `null`) | unbounded |
| `List<? extends T>` | `List<T>` or a list of a subtype | `T` | nothing (only `null`) | covariant |
| `List<? super T>` | `List<T>` or a list of a supertype | `Object` | `T` and subtypes | contravariant |
- **PECS** [▶ ~62:02](https://youtu.be/Opb2q2WSyes?t=3722): **P**roducer **E**xtends, **C**onsumer **S**uper. If a structure *produces* data you read, use `extends`. If it *consumes* data you write, use `super`.

### `T` vs `?`: when to use which [▶ ~62:53](https://youtu.be/Opb2q2WSyes?t=3773)
- `class Box<?>` or `Box<?> b = new Box<?>()` gives "Syntax error … identifier expected". The same happens for `public static <?> void fun(? a, ? b)`.
- `T` **declares** a type (classes, generic methods). `?` only shows up where you **use** a generic type, typically to relate one container to another (`List<? extends Animal>` and similar).
- *(Small refinement: `Box<?> b = new Box<String>("x");` as a variable type is legal. What's illegal is `?` in a declaration or in `new Box<?>()`.)*

### Type erasure: does the JVM know about generics? [▶ ~65:59](https://youtu.be/Opb2q2WSyes?t=3959)
- Pipeline: `Demo.java`, then the compiler (compile time), then `Demo.class` bytecode, then the JVM runs it (runtime).
- **The JVM doesn't know generics** [▶ ~66:14](https://youtu.be/Opb2q2WSyes?t=3974). They're a **compile-time safety layer only**.
- The erasure rules [▶ ~68:51](https://youtu.be/Opb2q2WSyes?t=4131):
  1. **Unbounded, so replace with `Object`.** `class Box<T> { T value; }` becomes `class Box { Object value; }`, and `Pair<T, U>` becomes `Object first; Object second;`.
  2. **Bounded, so replace with the bound.** `class Box<T extends Number> { T value; }` becomes `Number value;`.
  3. **Insert casts automatically** [▶ ~70:41](https://youtu.be/Opb2q2WSyes?t=4241). `Integer x = b1.value;` becomes `Integer x = (Integer) b1.value;` in the bytecode. That cast **can't fail** because the compiler already guaranteed that only `Integer`s go into a `Box<Integer>`.
- Consequences:
  - **No `instanceof` with type arguments** [▶ ~74:05](https://youtu.be/Opb2q2WSyes?t=4445). `l instanceof List<String>` is an error, since at runtime it's just `List`. *(Note: use `l instanceof List<?>`. Since Java 16 the compiler does accept `instanceof List<String>` when the static type already proves it, e.g. when `l` is declared as `Collection<String>`.)*
  - **No overloading on type arguments** [▶ ~74:51](https://youtu.be/Opb2q2WSyes?t=4491). `print(List<String>)` and `print(List<Integer>)` both erase to `print(List)`, which gives a "name clash: … have the same erasure" error.
  - **Bridge methods** [▶ ~75:39](https://youtu.be/Opb2q2WSyes?t=4539). `class Parent<T> { T get() { return null; } }` and `class Child extends Parent<String> { String get() { return "hello"; } }`. After erasure, `Parent.get()` returns `Object`, so `Child`'s `String get()` no longer overrides it at the bytecode level. The compiler adds a synthetic `Object get() { return this.get(); }` (calling the `String` version) so that polymorphism keeps working. *(Correction: the video says the JVM writes this method. It's the **compiler** that generates it.)*

### Why erasure exists [▶ ~78:53](https://youtu.be/Opb2q2WSyes?t=4733)
- Generics arrived in **Java 5**. Before that, everyone wrote raw `List l = new ArrayList();`.
- If the JVM had been changed to require generic type information, all existing `.class` files and libraries would have broken. Erasure keeps the bytecode format compatible, so old and new code run on the same JVM.

### Why primitives aren't allowed: `Box<int>` [▶ ~80:31](https://youtu.be/Opb2q2WSyes?t=4831)
- After erasure, `T` becomes `Object`. `Integer` is an `Object`, but `int` is a primitive, not a class at all, so it can't be replaced with `Object`.
- Hence `List<Integer>`, never `List<int>`. Autoboxing hides the conversion.

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java ClassName.java`.

### Invariance: what doesn't compile
```java
// does not compile: demonstrates that List<Dog> is not a List<Animal>
import java.util.*;

public class InvarianceDemo {
    static class Animal { }
    static class Dog extends Animal { }

    static void printAnimals(List<Animal> a) { }

    public static void main(String[] args) {
        List<Dog> dogs = new ArrayList<>();
        List<Animal> animals = dogs;   // error: incompatible types
        printAnimals(dogs);            // error: List<Dog> cannot be converted to List<Animal>
    }
}
```

### Arrays are covariant, so a bad store fails at runtime
```java
public class ArrayCovarianceDemo {
    static class Animal { void eat() { System.out.println("Animal eating"); } }
    static class Dog extends Animal { void eat() { System.out.println("Dog eating"); } }

    public static void main(String[] args) {
        Dog[] dogs = new Dog[3];
        Animal[] animals = dogs;          // compiles: arrays are covariant
        animals[0] = new Dog();           // fine
        animals[0].eat();                 // Dog eating
        try {
            animals[1] = new Animal();    // compiles, but the real array is Dog[]
        } catch (ArrayStoreException e) {
            System.out.println("ArrayStoreException: " + e.getMessage());
            // ArrayStoreException: ArrayCovarianceDemo$Animal
        }
    }
}
```

### Unbounded wildcard `List<?>`
```java
import java.util.*;

public class UnboundedWildcardDemo {
    static class Animal { }
    static class Dog extends Animal { }

    static void printClasses(List<?> values) {
        for (Object obj : values) {               // can only read as Object
            System.out.println(obj.getClass().getSimpleName());
        }
        // values.add(new Dog());                 // compile error
        // values.add("hello");                   // compile error
    }

    public static void main(String[] args) {
        List<Dog> dogs = List.of(new Dog(), new Dog());
        List<String> words = List.of("hi");
        printClasses(dogs);    // Dog
                               // Dog
        printClasses(words);   // String
    }
}
```

### Upper bound `? extends Animal`: read, no write
```java
import java.util.*;

public class UpperBoundWildcardDemo {
    static class Animal { void eat() { System.out.println("Animal eating"); } }
    static class Dog extends Animal { @Override void eat() { System.out.println("Dog eating"); } }
    static class Cat extends Animal { @Override void eat() { System.out.println("Cat eating"); } }

    static void feedAll(List<? extends Animal> values) {
        for (Animal a : values) a.eat();          // reading as Animal is safe
        // values.add(new Dog());                 // compile error: list might be List<Cat>
    }

    public static void main(String[] args) {
        feedAll(List.of(new Dog(), new Dog()));   // Dog eating
                                                  // Dog eating
        feedAll(List.of(new Cat()));              // Cat eating
        feedAll(List.of(new Animal()));           // Animal eating
        // feedAll(List.of(1, 2));                // compile error: Integer is not an Animal
    }
}
```

### Lower bound `? super Animal`: write, read only as `Object`
```java
import java.util.*;

public class LowerBoundWildcardDemo {
    static class Animal { void eat() { System.out.println(getClass().getSimpleName() + " eating"); } }
    static class Dog extends Animal { }
    static class Labrador extends Dog { }
    static class Cat extends Animal { }

    static void addSome(List<? super Animal> values) {
        values.add(new Animal());                 // writing is safe
        values.add(new Labrador());
        values.add(new Cat());
        // Animal a = values.get(0);              // compile error: might be List<Object>
        Object first = values.get(0);             // reading only as Object
        System.out.println("first: " + first.getClass().getSimpleName());
    }

    public static void main(String[] args) {
        List<Animal> animals = new ArrayList<>();
        addSome(animals);                         // first: Animal
        for (Animal a : animals) a.eat();         // Animal eating
                                                  // Labrador eating
                                                  // Cat eating
        List<Object> objects = new ArrayList<>();
        objects.add("not an animal");
        addSome(objects);                         // first: String
        System.out.println(objects.size());       // 4
        // addSome(new ArrayList<Dog>());         // compile error: Dog is below Animal
    }
}
```

### PECS in practice: a generic copy method
```java
import java.util.*;

public class PecsCopyDemo {
    // src PRODUCES Ts (extends), dest CONSUMES Ts (super)
    static <T> void copy(List<? extends T> src, List<? super T> dest) {
        for (T item : src) dest.add(item);
    }

    static double sum(Collection<? extends Number> nums) { // producer: read-only
        double total = 0;
        for (Number n : nums) total += n.doubleValue();
        return total;
    }

    public static void main(String[] args) {
        List<Integer> ints = List.of(1, 2, 3);
        List<Number> numbers = new ArrayList<>(List.of(0.5));
        copy(ints, numbers);                     // Integer producer -> Number consumer
        System.out.println(numbers);             // [0.5, 1, 2, 3]
        System.out.println(sum(numbers));        // 6.5
        List<Object> sink = new ArrayList<>();
        copy(ints, sink);
        System.out.println(sink);                // [1, 2, 3]
    }
}
```

### Type erasure: same runtime class, overloading clash
```java
import java.util.*;

public class TypeErasureDemo {
    static class Box<T> { T value; }
    static class NumBox<T extends Number> { T value; }

    // static void print(List<String> l) {}
    // static void print(List<Integer> l) {}   // compile error: name clash, same erasure

    public static void main(String[] args) throws Exception {
        List<String> a = new ArrayList<>();
        List<Integer> b = new ArrayList<>();
        System.out.println(a.getClass() == b.getClass()); // true (both just ArrayList)

        // Field types after erasure: Object for unbounded, the bound for bounded
        System.out.println(Box.class.getDeclaredField("value").getType().getSimpleName());    // Object
        System.out.println(NumBox.class.getDeclaredField("value").getType().getSimpleName()); // Number

        Object o = a;
        System.out.println(o instanceof List<?>);  // true  (List<String> here would not compile)
    }
}
```

### Bridge method generated by the compiler
```java
import java.lang.reflect.Method;

public class BridgeMethodDemo {
    static class Parent<T> { T get() { return null; } }
    static class Child extends Parent<String> {
        @Override String get() { return "hello"; }
    }

    public static void main(String[] args) {
        Parent<String> p = new Child();
        System.out.println(p.get());               // hello (polymorphism still works)

        for (Method m : Child.class.getDeclaredMethods()) {
            if (m.getName().equals("get") && m.isBridge()) {
                System.out.println("bridge: " + m.getReturnType().getSimpleName() + " get()");
                // bridge: Object get()
            }
        }
    }
}
```

## Common Pitfalls / Tips
- **"Every Dog is an Animal" doesn't carry over to containers.** A `List<Animal>` parameter rejects a `List<Dog>`. Use `List<? extends Animal>` if you only read from it.
- **You can't add to a `? extends` list**, not even something that obviously fits. If you need to add, either use `? super` or make the method generic (`<T> void f(List<T> l, T item)`).
- **Reading from `? super` gives `Object`.** Don't fight it with casts. If you need both reading and writing of a specific type, use a plain type parameter `T`.
- **Arrays and generics behave differently.** `Animal[] a = new Dog[1]` compiles, while the generic equivalent doesn't. Arrays check at runtime (`ArrayStoreException`), generics at compile time.
- **You can't create generic arrays:** `new T[10]` and `new List<String>[10]` don't compile, because of erasure. Use `List<List<String>>` or `ArrayList` instead.
- **Overloads that differ only by type argument clash.** `f(List<String>)` and `f(List<Integer>)` have the same erasure. Give them different names.
- **`instanceof` and casts can't check type arguments at runtime.** `(List<String>) obj` only gives an "unchecked" warning, and the problem shows up later as a `ClassCastException` somewhere else.
- **Primitives can't be type arguments.** You write `List<Integer>` and `Map<String, Long>`. Watch out for `list.remove(int)` vs `list.remove(Object)` with `List<Integer>`.
- **Remember PECS when designing APIs.** `addAll(Collection<? extends E>)` and `Collections.copy(List<? super T>, List<? extends T>)` follow it.

## Review Questions
1. Why is `List<Animal> animals = new ArrayList<Dog>();` a compile error when `Animal a = new Dog();` is fine?
2. What does "arrays are covariant" mean, and what happens if you store the wrong type into a covariant array?
3. What can and can't you do with a parameter of type `List<?>`?
4. With `List<? extends Animal> l`, why is `l.get(0)` assignable to `Animal` but `l.add(new Dog())` rejected?
5. With `List<? super Animal> l`, which lists can be passed in, and why is `l.add(new Dog())` allowed?
6. State the PECS rule and apply it to the signature of a `copy(src, dest)` method.
7. What are the three type-erasure rules, and why can the inserted cast never fail?
8. Give two restrictions that follow from erasure, and explain why `List<int>` isn't allowed.

<details>
<summary>Answers</summary>

1. Generics are invariant. If `List<Dog>` were a `List<Animal>`, you could call `animals.add(new Animal())` and put a non-dog into a dog list, which breaks type safety. A single `Dog` assigned to an `Animal` reference can't be "polluted" this way.
2. `Dog[]` is treated as a subtype of `Animal[]`, so `Animal[] a = new Dog[10];` compiles. Storing a plain `Animal` (or a `Cat`) into it compiles too, but the JVM checks every array store and throws `ArrayStoreException` at runtime.
3. You can accept any list, read elements as `Object`, and call type-independent methods (`size`, `clear`, `remove(Object)`, `contains`). You can't add elements (except `null`) or read them as any specific type.
4. Whatever list is passed (`List<Animal>`, `List<Dog>`, `List<Cat>`), every element is at least an `Animal`, so reading as `Animal` is safe. But the compiler doesn't know which subtype the list really holds. It might be a `List<Cat>`, and adding a `Dog` there would be wrong, so all adds are rejected.
5. `List<Animal>` and `List<Object>` (lists of `Animal` or a supertype). Any of those can safely hold an `Animal` or any subtype (`Dog`, `Labrador`, `Cat`), so adding is allowed. Reading gives `Object`, since the list could be a `List<Object>`.
6. Producer Extends, Consumer Super. The source produces values you read, so it's `List<? extends T>`. The destination consumes values you write, so it's `List<? super T>`. That gives `static <T> void copy(List<? extends T> src, List<? super T> dest)`.
7. (1) An unbounded `T` becomes `Object`. (2) A bounded `T` becomes its bound (e.g. `Number`). (3) Casts are inserted where generic values are used as specific types. The cast can't fail because the compiler already guaranteed that only correctly typed values were put in, for example only `Integer`s into a `Box<Integer>`.
8. Any two of: no `instanceof List<String>` (use `List<?>`); no overloading on type arguments (`f(List<String>)` and `f(List<Integer>)` clash); no `new T()` or `new T[]`; the compiler has to generate bridge methods. `List<int>` isn't allowed because erasure replaces `T` with `Object` (or the bound), and a primitive `int` isn't an `Object`. Only reference types fit.
</details>

## Further Study
- `Collections.copy`, `Collections.max(Collection<? extends T>)`, and other JDK signatures that use PECS
- Recursive bounds: `<T extends Comparable<? super T>>`
- Wildcard capture and helper methods (the "capture#1-of ?" compiler messages)
- Reifiable vs non-reifiable types; heap pollution; `@SafeVarargs`
- Why generic arrays can't be created, and the `(T[]) new Object[n]` workaround used inside `ArrayList`
- Reflection with generics: `ParameterizedType`, `getGenericSuperclass()` (the "super type token" trick)
- Project Valhalla, a proposal that would allow primitive-like value types in generics

---

[← #27](27-generics-bounded-types.md) · [Index](ReadMe.md) · [#29 →](29-java-collection-framework-explained.md)
