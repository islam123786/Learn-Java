# Java Collection Interface Deep Dive | Methods & Internals | Java Full Course #31
> Source: https://youtu.be/cazzzwdLAkQ · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 29:14. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `Collection<E>` is the parent interface of every `List`, `Set` and `Queue`, and it extends `Iterable<E>`. It declares the operations that make sense for **any** container.
- Program to it: `Collection<Integer> c = new ArrayList<>();` (or `LinkedList`, `HashSet`, `ArrayDeque`, …). The same calls (`add`, `remove`, `contains`, `size`, …) work on all of them. Only the speed changes, e.g. `contains` is O(n) on a list and O(1) on a hash set.
- You don't memorize the methods. Group them logically: **query** (`size`, `isEmpty`, `contains`), **modify** (`add`, `remove`, `clear`), **bulk** (`addAll`, `containsAll`, `removeAll`, `retainAll`), **convert/traverse** (`toArray`, `iterator`), and the **Java 8 defaults** (`removeIf`, `stream`, `parallelStream`, `spliterator`).
- `add`/`remove` return `boolean`, meaning "did the collection change?". `add` returns `false` for a duplicate in a `Set`.
- Methods that only compare elements (`contains`, `remove`, `containsAll`, `removeAll`, `retainAll`) accept `Object` / `Collection<?>`, because comparison uses `equals()`, which every object has. `addAll` takes `Collection<? extends E>` because it puts elements in (PECS).

## Key Concepts

**`Collection` (interface) vs `Collections` (utility class).** `java.util.Collection` is the root interface of the collection hierarchy. `java.util.Collections` is a class of static helpers (`sort`, `reverse`, `max`, `unmodifiableList`, …). It's the same naming pattern as `Object` vs `Objects` and `Array`/`Arrays`. *Why it matters:* the two are easy to confuse in imports and in interviews.

**Programming to the `Collection` interface.** A `Collection<E>` reference can hold any concrete collection, and only the common operations are visible through it. *Why it matters:* polymorphism lets a method accept "any container" and stay independent of the data structure.

**Same API, different cost.** Every implementation provides `contains`/`remove` its own way: a linear scan with `equals` for `ArrayList`/`LinkedList`, a hash lookup for `HashSet`, a tree search for `TreeSet`. *Why it matters:* you choose the implementation for performance without changing the calling code.

**Boolean return = "changed?".** `add`, `remove`, `addAll`, `removeAll` and `retainAll` return `true` only if the collection was modified. *Why it matters:* `set.add(x)` doubles as an "is this new?" check.

**`equals`-based operations take `Object`.** `contains(Object o)`, `remove(Object o)` and the `...All(Collection<?>)` methods only need `equals()`, defined in `Object`. *Why it matters:* it explains the signatures. It also means your element classes must override `equals` (and `hashCode`) for these methods to behave.

**`toArray`.** `Object[] toArray()` returns an `Object[]`, which loses the element type. `<T> T[] toArray(T[] a)` returns a correctly typed array, and passing `new Integer[0]` just tells it the type. *Why it matters:* it's how you get a real `Integer[]` or `String[]` out of a collection.

**Default methods (Java 8+).** `removeIf(Predicate)`, `stream()`, `parallelStream()` and `spliterator()` were added as `default` methods so that existing implementations didn't break. *Why it matters:* this is how Java evolves interfaces without breaking old code.

## Detailed Notes

### Where `Collection` sits [00:00]
- `Iterable` is at the top, then `Collection`, then `List`, `Set` and `Queue`.
- *(Correction: the video says "Iterable extends Collection". It's the reverse: `Collection<E> extends Iterable<E>`.)*
- `Collection` is the parent of every data structure in the framework. It tells you what **any** collection can do, whatever its internal structure.
- `Collection<Integer> c = new ...` can be followed by `ArrayList`, `LinkedList`, `HashSet`, `ArrayDeque` or any other concrete class. Then `c.` shows every method of the `Collection` interface. That's inheritance plus polymorphism.

### `Collection` vs `Collections` [02:36]
- `Collection` is an **interface**. `Collections` (with an extra "s") is a **utility class**.
- The same pattern exists elsewhere: `Object` (the parent class) vs `Objects` (the utility class). The utility class gets its own video later.

### Grouping the methods logically [03:20]
- Methods specific to one structure belong to `List`, `Set` or `Queue`. Only operations common to *all* collections live in `Collection`.
- Ask yourself, from first principles, what you can do with *any* container. The answer is the method list. In practice you type `c.` in the IDE and choose.

### Query methods: `size`, `isEmpty`, `contains` [05:38]
- Setup: `import java.util.*; Collection<Integer> c = new ArrayList<>(); c.add(1); c.add(2); c.add(3);`
- `c.size()` [06:31] gives `3`.
- `c.isEmpty()` [07:07] gives `false`. Why not just `size() == 0`? `isEmpty` can be cheaper for some structures, so prefer it.
  - *(Detail: for most JDK collections both are O(1). For a few, such as `ConcurrentLinkedQueue`, `size()` is O(n) while `isEmpty()` is O(1).)*
- `boolean contains(Object o)` [07:43]:
  - The parameter is `Object` because the check is done with **`equals()`**, which every object inherits from `Object`.
  - `ArrayList`/`LinkedList` compare element by element: **O(n)**. `HashSet` hashes straight to the bucket: **O(1)** on average.
  - `c.contains(2)` gives `true` for both `ArrayList` and `HashSet`. Only the cost differs.

### `iterator()` [10:31]
- `Collection` extends `Iterable`, so it has `iterator()`, which returns an `Iterator` (covered in #30).

### `toArray` [10:49]
- `Object[] toArray()` copies any collection (a list, set, stack, queue, …) into an array, which gives you contiguous memory and random access.
  - The catch: you get `Object[]`, so you can only call `Object` methods unless you cast. `for (Object o : obj) System.out.println(o);` prints `1 2 3`.
- `<T> T[] toArray(T[] a)` [12:22] returns an array of the type you ask for:
  - `Integer[] arr = c.toArray(new Integer[0]);`. The zero-length array only *signals the type*. Loop with `for (Integer i : arr)` to get `1 2 3`.
  - *(Details: if the passed array is big enough, it's filled and returned. Otherwise a new array of the same type is created. Since Java 11 you can also write `c.toArray(Integer[]::new)`.)*

### `add` [15:02]
- `boolean add(E e)` takes an `E` (generic), so a `Collection<Integer>` accepts only `Integer`s.
- It returns `true` if the collection changed. `c.add(4)` on a `LinkedList` gives `true`.
- It returns `false` when nothing changes. With a **`HashSet`** [16:18], `c.add(3)` when 3 is already present gives `false`, since duplicates aren't allowed.

### `remove` [16:57]
- `boolean remove(Object o)` again takes `Object`, because finding the element uses `equals`.
- On a `HashSet` it's constant time. `c.remove(2)` gives `true`, and `c.remove(4)` (absent) gives `false`.
- For a list with duplicates [19:08], `[1, 2, 3, 3]` with `c.remove(3)` removes the **first** occurrence and leaves `[1, 2, 3]`.

### Bulk operations [19:44]
- `boolean addAll(Collection<? extends E> c)` adds all the elements of another collection.
  - Why `? extends E`: if the collection holds `Animal`s, you can also add a collection of `Dog`s or `Cat`s (subtypes). That's the producer side of PECS.
  - `c.addAll(List.of(5, 6, 7, 8, 9));`. `List.of` builds a small list, explained in the `List` video.
- **Printing a collection directly** [21:16]: `System.out.println(c)` calls `c.toString()`. Collections override `toString()` and print `[1, 2, 3, 5, 6, 7, 8, 9]`, so you don't need a loop.
- `boolean containsAll(Collection<?> c)` [21:49]. It takes any collection (`?`) because it only compares with `equals`. `c.containsAll(List.of(1, 2, 3))` gives `true`.
- `boolean removeAll(Collection<?> c)` [23:03] removes every element that's also in the argument.
- `boolean retainAll(Collection<?> c)` [23:42] is the opposite: it keeps **only** the elements that are in the argument, which is a set **intersection**.
  - On `[1, 2, 3]`, `removeAll(List.of(1, 2))` gives `[3]` and `retainAll(List.of(1, 2))` gives `[1, 2]`.
- `void clear()` [25:00] empties the collection and prints as `[]`.

### Recap, and methods from `Object` [25:26]
- Groups: `add`/`remove`; `addAll`/`removeAll`/`retainAll`; `contains`/`containsAll`; the two `toArray`s; `iterator`; `size`/`isEmpty`; `clear`.
- From `Object`: `equals` and `hashCode`. Collections compare **contents**, not references.
  - The contract: if `a.equals(b)` then `a.hashCode() == b.hashCode()`. The reverse doesn't have to hold.
  - If you write your own `Student` class and override `equals`, also override `hashCode`.
  - *(Correction: `Collection` itself doesn't define value equality. `List` and `Set` do, in their own contracts. Two lists are equal if they hold equal elements in the same order. Two sets are equal if they hold the same elements. A `List` never equals a `Set`, even with the same elements, and `ArrayDeque` doesn't override `equals` at all.)*

### Modern default methods [27:19]
- `removeIf`, `spliterator`, `stream` and `parallelStream` [27:44] are **`default`** methods, so code written against the old interface keeps compiling without implementing them.
- `removeIf` takes a `Predicate`, a functional interface (covered with lambdas). Streams and spliterators are covered with streams.
- Most of what you can do to a container is declared right here. `List`/`Set`/`Queue` only add structure-specific methods: index access for lists, front/back operations for queues, ordered navigation for `TreeSet` (a BST).

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java ClassName.java`.

### Core methods through a `Collection` reference
```java
import java.util.*;

public class CollectionMethodsDemo {
    public static void main(String[] args) {
        Collection<Integer> c = new ArrayList<>();     // swap for LinkedList / HashSet / ArrayDeque
        c.add(1); c.add(2); c.add(3);

        System.out.println(c.size());         // 3
        System.out.println(c.isEmpty());      // false
        System.out.println(c.contains(2));    // true   (O(n) here, O(1) with HashSet)
        System.out.println(c.add(4));         // true
        System.out.println(c.remove(4));      // true
        System.out.println(c.remove(99));     // false  (not present)
        System.out.println(c);                // [1, 2, 3]  (toString is overridden)

        Collection<Integer> set = new HashSet<>(List.of(1, 2, 3));
        System.out.println(set.add(3));       // false  (duplicate: set unchanged)

        Collection<Integer> dupes = new ArrayList<>(List.of(1, 2, 3, 3));
        dupes.remove(3);                      // Collection.remove(Object): first occurrence
        System.out.println(dupes);            // [1, 2, 3]

        c.clear();
        System.out.println(c + " " + c.isEmpty()); // [] true
    }
}
```

### `toArray`: untyped vs typed
```java
import java.util.*;

public class ToArrayDemo {
    public static void main(String[] args) {
        Collection<Integer> c = new LinkedList<>(List.of(1, 2, 3));

        Object[] objs = c.toArray();                 // Object[]: element type lost
        System.out.println(objs.length + " " + objs.getClass().getSimpleName()); // 3 Object[]

        Integer[] arr = c.toArray(new Integer[0]);   // zero-length array only signals the type
        int sum = 0;
        for (Integer i : arr) sum += i;              // no casts needed
        System.out.println(sum + " " + arr.getClass().getSimpleName()); // 6 Integer[]

        String[] names = new ArrayList<>(List.of("a", "b")).toArray(String[]::new); // Java 11+
        System.out.println(Arrays.toString(names));  // [a, b]
    }
}
```

### Bulk operations: `addAll`, `containsAll`, `removeAll`, `retainAll`
```java
import java.util.*;

public class BulkOpsDemo {
    public static void main(String[] args) {
        Collection<Integer> c = new ArrayList<>(List.of(1, 2, 3));
        c.addAll(List.of(5, 6, 7, 8, 9));
        System.out.println(c);                              // [1, 2, 3, 5, 6, 7, 8, 9]
        System.out.println(c.containsAll(List.of(1, 2, 3))); // true
        System.out.println(c.containsAll(List.of(1, 4)));    // false

        Collection<Integer> r = new ArrayList<>(List.of(1, 2, 3));
        r.removeAll(List.of(1, 2));
        System.out.println(r);                              // [3]

        Collection<Integer> k = new ArrayList<>(List.of(1, 2, 3));
        System.out.println(k.retainAll(List.of(1, 2, 10))); // true (3 was removed)
        System.out.println(k);                              // [1, 2]  (intersection)

        Collection<Integer> evens = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        evens.removeIf(x -> x % 2 != 0);                    // Java 8 default method
        System.out.println(evens);                          // [2, 4, 6]

        Collection<Number> nums = new ArrayList<>();
        nums.addAll(List.of(1, 2));                         // addAll(Collection<? extends E>)
        nums.addAll(List.of(2.5));
        System.out.println(nums);                           // [1, 2, 2.5]
    }
}
```

### `equals` / `hashCode` matter for `contains` and `remove`
```java
import java.util.*;

public class CollectionEqualityDemo {
    static class Student {                     // no equals/hashCode: identity comparison
        final String name;
        Student(String name) { this.name = name; }
    }

    record StudentRec(String name) { }         // records generate equals/hashCode

    public static void main(String[] args) {
        Collection<Student> a = new ArrayList<>(List.of(new Student("Aditya")));
        System.out.println(a.contains(new Student("Aditya")));     // false

        Collection<StudentRec> b = new HashSet<>(List.of(new StudentRec("Aditya")));
        System.out.println(b.contains(new StudentRec("Aditya")));  // true

        System.out.println(List.of(1, 2).equals(new ArrayList<>(List.of(1, 2)))); // true (List contract)
        System.out.println(List.of(1, 2).equals(Set.of(1, 2)));                   // false (List vs Set)
    }
}
```

## Common Pitfalls / Tips
- **Don't mix up `Collection` and `Collections`.** One is the interface you implement or declare. The other holds static helpers like `Collections.sort(list)`.
- **`Collection` doesn't include `Map`.** A `Map` can't be assigned to a `Collection` variable. Use `map.keySet()`, `values()` or `entrySet()`.
- **`Collection` has no `get(index)`.** Index-based methods belong to `List`. With a `Collection` reference, use the iterator or for-each.
- **`List<Integer>.remove(3)` vs `Collection<Integer>.remove(3)`.** Through a `List` reference, `remove(3)` means *index* 3. Through a `Collection` reference it means *the value* 3 (autoboxed). Write `remove(Integer.valueOf(3))` to be explicit.
- **`contains`/`remove` rely on `equals` (and on `hashCode` for hash-based sets).** Without overrides, two "equal" objects are treated as different.
- **Compare with `isEmpty()`**, not `size() == 0`. It reads better and is never slower.
- **`List.of(...)`/`Set.of(...)` are immutable.** Calling `add`, `remove` or `clear` on them throws `UnsupportedOperationException`. Wrap them, e.g. `new ArrayList<>(List.of(...))`, to modify.
- **Use `toArray(new T[0])` or `toArray(T[]::new)`** to get a typed array. Casting `Object[]` to `Integer[]` throws `ClassCastException`.
- **`retainAll` is intersection, `removeAll` is difference.** Both return whether the collection changed.

## Review Questions
1. Where does `Collection` sit in the hierarchy, and what's the difference between `Collection` and `Collections`?
2. Why is it useful to declare a variable as `Collection<Integer>` instead of `ArrayList<Integer>`?
3. Why do `contains` and `remove` accept `Object` rather than `E`?
4. What does the `boolean` returned by `add` mean? Give a case where it's `false`.
5. What's the difference between the two `toArray` methods, and why pass `new Integer[0]`?
6. Why does `addAll` take `Collection<? extends E>` while `removeAll` takes `Collection<?>`?
7. Starting from `[1, 2, 3]`, what is the result of `removeAll(List.of(1, 2))`, and of `retainAll(List.of(1, 2))`?
8. Why were `removeIf`, `stream` and `parallelStream` added as `default` methods?

<details>
<summary>Answers</summary>

1. `Collection<E>` extends `Iterable<E>` and is the parent of `List`, `Set` and `Queue`. `Collection` is that root interface. `Collections` is a utility class of static helper methods (`sort`, `max`, `unmodifiableList`, …).
2. Polymorphism. The code (and any method that takes the variable) works with any implementation, and you can switch `ArrayList` to `HashSet` or `LinkedList` without changing it. Only the common operations are visible, which keeps the code general.
3. They only need to compare the argument with elements using `equals()`, which every `Object` has. No element is inserted, so type safety isn't at risk. (It also kept compatibility with pre-generics code.)
4. It means "the collection changed as a result". It's `false`, for example, when adding a duplicate to a `Set` such as a `HashSet`.
5. `toArray()` returns `Object[]`, so you'd need casts to use the elements. `toArray(T[] a)` returns a `T[]` of the runtime type of the array you pass. A zero-length array just tells it the type, and it allocates a correctly sized one (or fills yours if it's big enough).
6. `addAll` inserts elements, so they must be `E` or a subtype (the producer side, `extends`). `removeAll` only compares with `equals`, so a collection of any type is acceptable.
7. `removeAll` gives `[3]`. `retainAll` gives `[1, 2]` (the intersection).
8. Adding abstract methods to an existing interface would break every class that already implemented it. `default` methods come with a body, so old implementations keep compiling and still get the new behavior.
</details>

## Further Study
- The `java.util.Collections` utility class: `sort`, `binarySearch`, `unmodifiable*`, `synchronized*`, `frequency`
- `equals`/`hashCode` contracts of `List` vs `Set`
- `removeIf` with lambdas and `Predicate`; `stream()` and `parallelStream()`
- `Spliterator` characteristics (`SIZED`, `ORDERED`, …)
- `AbstractCollection`: how the JDK implements most `Collection` methods on top of `iterator()` and `size()`
- Immutable collections: `List.of`, `Set.of`, `List.copyOf`, `Collections.unmodifiableList`

---

[← #30](30-iterable-iterator-for-each.md) · [Index](../../ReadMe.md) · [#32 →](32-list-interface-arraylist-linkedlist.md)
