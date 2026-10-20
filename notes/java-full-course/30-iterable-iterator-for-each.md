# Iterable Interface in Java | Why Iterator Exists? | For-Each Loop | Java Full Course #30
> Source: https://youtu.be/LVwpxEZ6uMQ · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 76:01. The video is in Hindi; these notes were written from its Hindi transcript. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- An index-based loop (`for i … list.get(i)`) only works for lists, and on a `LinkedList` it costs **O(n²)** overall. Writing custom loops per data structure (pop a stack, poll a queue) ties your code to that structure.
- **`Iterable<T>`** is the root interface for "anything whose elements can be visited one by one". Its key method `iterator()` returns an **`Iterator<T>`**, which has `hasNext()`, `next()` and the optional `remove()`.
- Each collection supplies its own iterator class, usually a **private inner class** that can read the collection's private fields (array plus cursor, or node pointer). Your loop code stays the same whether the collection is an `ArrayList`, `LinkedList`, `HashSet`, `ArrayDeque` or `TreeSet`.
- The **for-each loop** is syntactic sugar. For any `Iterable`, the compiler turns `for (T x : c)` into the `iterator()`/`hasNext()`/`next()` loop. It works only on arrays and `Iterable`s.
- Changing a collection structurally while iterating (e.g. `list.remove(...)` inside the loop) throws **`ConcurrentModificationException`** (fail-fast). Use `iterator.remove()` instead. The iterator is a **separate object** because that keeps concerns apart and allows several independent cursors (nested loops) over the same collection.

## Key Concepts

**`Iterable<T>`.** An interface with one abstract method, `Iterator<T> iterator()`, plus the default methods `forEach` and `spliterator`. `Collection` extends it, so every `List`, `Set` and `Queue` is iterable. *Why it matters:* implementing it is what makes your class usable in a for-each loop.

**`Iterator<T>`.** A cursor over the elements: `boolean hasNext()`, `T next()` (returns the current element and advances), `default void remove()` (removes the element last returned by `next()`), and `default forEachRemaining(...)`. *Why it matters:* it's one uniform way to traverse any collection without knowing how it's built inside.

**Abstraction of traversal.** The caller only asks "is there a next element?" and "give me the next one". *How* (array index, node pointer, hash bucket, BST in-order) is hidden inside each collection's iterator. *Why it matters:* you can swap `ArrayList` for `HashSet` without touching the loop.

**Inner-class iterator.** The JDK's `ArrayList` returns `new Itr()`, a private inner class holding a `cursor`. `LinkedList` returns a `ListItr` holding a node reference. Because they are inner classes they can read the outer collection's private array, `size` or `head`. *Why it matters:* this is the standard way to implement `Iterable` for your own classes.

**Enhanced for (for-each) loop.** `for (String s : names)` compiles to an iterator loop for an `Iterable`, and to an index loop for an array. *Why it matters:* it's shorter and clearer, but it hides the iterator, so you can't call `remove()` from inside it.

**Fail-fast and `ConcurrentModificationException`.** Collections keep a modification counter (`modCount`). The iterator records it when created and checks it on each `next()`. If the collection was changed structurally by something other than the iterator, it throws CME at once instead of carrying on with undefined behavior. *Why it matters:* it's the most common bug in "remove while looping" code.

**Separation of concerns.** The collection's job is *storing* data. Walking over it is the iterator's job. *Why it matters:* each `iterator()` call gives a fresh cursor, so nested loops and multiple simultaneous traversals just work.

## Detailed Notes

### Where `Iterable` sits [00:00]
- Hierarchy recap: `Object`, then `Iterable`, then `Collection`, then `List` / `Queue` / `Set`, then the concrete classes. `Map` sits outside `Collection`.

### Problem: iterate over *any* collection [00:56]
- The collection might be an `ArrayList`, `LinkedList`, stack, queue, set or map. The goal is to traverse it and fetch the elements one by one.
- First attempt, the index loop [02:47]:
  ```
  for (int i = 0; i < list.size(); i++) System.out.println(list.get(i));
  ```
  This works for an `ArrayList` (2, 3, 5, 9, 7, 8 printed in order).

### Why the index loop isn't general [04:26]
- Structures split into two groups:
  - those that **keep insertion order and positions**: lists;
  - those that **don't**: `Set` and `Map` (placed by hash, used for O(1) lookup), and `Stack` / `Queue` (they follow LIFO or FIFO, and you only access the top or front).
- Only `List` has `get(i)`. Sets, maps, stacks and queues don't, so the index loop can't be used for them.
- Even for `LinkedList` [07:02]: nodes aren't in contiguous memory, so there's no random access. Each `get(i)` walks from the start, which makes the whole loop **O(n²)** [~09:30]. `get` exists (it comes from `List`), but it's very slow here.
- Second attempt: use each structure's own API (repeatedly `pop()` a stack, `poll()` a queue until empty) [11:37]. The problem is that this **tightly couples** the loop to the underlying structure. If someone changes `new LinkedList<>()` to `new HashSet<>()`, the loop breaks.

### The solution: `Iterable` [12:09]
- Definition: *the root interface representing any object whose elements can be traversed one by one*. If a collection wants to be traversable, it implements `Iterable`.
- Thought experiment [13:12]: picture an object X that walks over an array-backed list. To traverse anything it needs two operations:
  - `hasNext()`: is there another element? (returns `boolean`)
  - `next()`: return the element and move forward.
- The caller doesn't need to know how X moves. That's **abstraction**.
- `Iterable` has the important method `iterator()` [16:18], which returns an **`Iterator`**. `Iterator` is a *separate* interface, and it's the one that declares `hasNext()` and `next()`.

### Demo: one loop, any collection [17:08]
- Code:
  ```
  Iterator<Integer> it = list.iterator();
  while (it.hasNext()) System.out.println(it.next());
  ```
  This prints `10 20 30 40 50`. `next()` both returns the element **and** advances the cursor. When the cursor reaches `size`, `hasNext()` is false.
- In the JDK source [18:34]: `ArrayList` extends `AbstractList`, then `AbstractCollection`, which implements `Collection`, which extends `Iterable<E>`. `Iterable` declares `Iterator<E> iterator()`, and `Iterator<E>` declares `hasNext()` and `E next()`.
- Swap the implementation and the loop stays unchanged:
  - `LinkedList` gives `10 20 30 40 50`.
  - Declare it as `Collection<Integer> c` and assign it a `new HashSet<>()` [23:05]. That gives `50 20 40 10 30`, because a set stores by hash, not insertion order. The loop still works.
  - `ArrayDeque` [24:06] gives `10 20 30 40 50` (FIFO order). `TreeSet` gives sorted order (it's backed by a BST).

### How it works inside [24:55]
- `Iterable` has `iterator()`, which returns an `Iterator`. `Iterator` has `hasNext()` and `next()`.
- `ArrayList` implements `Iterable` indirectly, so it **must** implement `iterator()`. Since `Iterator` is an interface you can't instantiate it, so some class has to implement it. Conceptually: `class ArrayListIterator implements Iterator`, and `iterator()` does `return new ArrayListIterator();`.
- `list.iterator()` hands you that object, stored in an `Iterator` reference `it`, and you call `it.hasNext()` / `it.next()` on it.
- *(In the real JDK the class is the private inner class `ArrayList.Itr`. `LinkedList` uses `ListItr`, and `HashMap`'s key, value and entry iterators extend `HashIterator`.)*

### Conceptual `ArrayList` iterator [31:07]
- Outer class: a private array `arr`, a private `size`, and `iterator()` overridden to return `new ArrayListIterator()`.
- The iterator keeps a cursor `pos = 0`.
  - `hasNext()`: `return pos < size;`
  - `next()`: `return arr[pos++];` (postfix: return the current element, then advance).
- Problem: the iterator needs `arr` and `size`, which are private to the list. The solution is **nested/inner classes** [36:09]. Put the iterator class *inside* the list class and it can read those private members directly, with no passing of objects around.
- Trace: `pos` goes 0 to 5 while printing 1 to 5. At `pos == 5`, `hasNext()` is false and the loop ends.

### Conceptual `LinkedList` iterator [40:18]
- There's no array, only nodes: `static class Node { int data; Node next; }` and a `head` reference.
- `iterator()` returns `new LinkedListIterator()`, an inner class that keeps `Node current = head`.
  - `hasNext()`: `return current != null;`
  - `next()`: `int data = current.data; current = current.next; return data;`
- The caller writes the same `while (it.hasNext()) … it.next()` loop.

### Sets, maps, trees, queues [45:56]
- Hash-based `Set`/`Map`: the iterator probably walks **bucket by bucket**, and within a bucket element by element through the chain.
- `TreeSet`/`TreeMap` (a BST): the iterator gives an **in-order traversal**, so the elements come out sorted.
- Array-backed queues and stacks: the iterator walks the underlying array.
- *(Caveat: the legacy `Stack` class iterates bottom-to-top, in insertion order, **not** in pop order. `ArrayDeque` used as a stack via `push` iterates top-first.)*

### Build your own iterable: `NameContainer` [47:11]
- `class NameContainer implements Iterable<String>` holds `private String[] names` and `private int size`.
- Without `iterator()` you get the error "must implement the inherited abstract method". You override `public Iterator<String> iterator()` to return `new NameContainerIterator()`.
- `private class NameContainerIterator implements Iterator<String>` keeps `private int pos = 0`, with `hasNext() { return pos < size; }` and `next() { return names[pos++]; }`.
- Used in `main` with `Aditya, Rohit, Rohan, Monu`, it prints all four names.
- **Anonymous class** [53:42]: the iterator class is only used in one place, so you can write `return new Iterator<String>() { ... };` directly inside `iterator()` and delete the named inner class. The output is the same.

### The for-each loop is sugar [54:43]
- The iterator loop is verbose, so Java added the **enhanced for / for-each loop**: `for (String name : container) System.out.println(name);`
- The compiler rewrites it into the `iterator()` / `hasNext()` / `next()` loop.
- Proof [56:22]: remove `implements Iterable` from `NameContainer` and the for-each loop fails with **"Can only iterate over an array or an instance of java.lang.Iterable"**.

### Other methods of `Iterable` and `Iterator` [57:08]
- `Iterable`: `iterator()`, plus `forEach(Consumer)` (covered later with lambdas and functional interfaces) and `spliterator()` (covered later with streams).
- `Iterator` [57:43]: `hasNext()`, `next()`, **`remove()`**, and `forEachRemaining(Consumer)`.
  - `it.remove()` removes the element **last returned by `next()`**.
  - `remove()` is a `default` method, so you only *have* to implement `hasNext()` and `next()`. *(The default implementation throws `UnsupportedOperationException`.)*
- **Why no `add()` on `Iterator`?** [58:31] For a list, "insert after the cursor" makes sense. For a set, the new element lands wherever its hash says, possibly *behind* the cursor, so the iterator would never visit it. A general `add` would be ill-defined. **`ListIterator`** (lists only) does have `add`, covered in the `List` video.

### `ConcurrentModificationException` [60:18]
- Loop over `[1, 2, 3, 4, 5]` with an iterator. When `value == 3`, call `list.remove(...)`. The output is `1 2 3`, then **`ConcurrentModificationException`**.
- Why: the list was modified *behind the iterator's back*. The iterator can no longer tell what it has or hasn't visited. With a set the removed element could be anywhere.
- This is **fail-fast** behavior [63:32]: throw immediately rather than guess.
- *(Notes: (1) In the video's code `value` is an `int`, so `list.remove(value)` calls `remove(int index)` and removes the element at **index** 3, not the value 3. Use `list.remove(Integer.valueOf(3))` to remove by value. (2) The fix is `it.remove()`, or `list.removeIf(x -> x == 3)`. (3) Fail-fast is best-effort. Removing the second-to-last element happens to end the loop without an exception, so don't rely on CME to find bugs.)*

### Why a separate `Iterator`? Why not let `ArrayList` implement `Iterator`? [63:42]
- Reason 1, **separation of concerns** [65:16]: the collection's responsibility is storing data, and walking over it belongs to a separate object. This is the **Single Responsibility Principle** from SOLID / low-level design.
- Reason 2, the more important one, **state** [67:09]. Suppose `MyArrayList` itself has `pos`, `hasNext()` and `next()`:
  - After one full loop, `pos == size`, so a second loop prints nothing. You'd need a `reset()` method [70:04].
  - **Nested loops** break [70:28]. The goal is all pairs (`10 10, 10 20, 10 30, 20 10, …`), but the inner loop moves the *same* `pos` that the outer loop uses. You get `10 20, 10 30` and then nothing. A `reset()` would also reset the outer loop. A fixed number of extra cursor fields doesn't scale to 3 or 4 nested loops.
- With `Iterable`, the cursor lives in the iterator object, and **each `iterator()` call returns a new, independent iterator**. Outer and inner loops each get their own.

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java ClassName.java`.

### The same iterator loop over different collections
```java
import java.util.*;

public class IteratorAnyCollectionDemo {
    static void printAll(Collection<Integer> c) {
        Iterator<Integer> it = c.iterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) sb.append(it.next()).append(' ');
        System.out.println(c.getClass().getSimpleName() + ": " + sb.toString().trim());
    }

    public static void main(String[] args) {
        List<Integer> data = List.of(10, 20, 30, 40, 50);
        printAll(new ArrayList<>(data));   // ArrayList: 10 20 30 40 50
        printAll(new LinkedList<>(data));  // LinkedList: 10 20 30 40 50
        printAll(new HashSet<>(data));     // HashSet: 50 20 40 10 30   (hash order)
        printAll(new ArrayDeque<>(data));  // ArrayDeque: 10 20 30 40 50
        printAll(new TreeSet<>(List.of(30, 10, 50, 20, 40))); // TreeSet: 10 20 30 40 50 (sorted)
    }
}
```

### Your own `Iterable`: inner-class iterator plus for-each
```java
import java.util.Iterator;
import java.util.NoSuchElementException;

public class NameContainer implements Iterable<String> {
    private final String[] names;
    private final int size;

    public NameContainer(String[] names) {
        this.names = names;
        this.size = names.length;
    }

    @Override
    public Iterator<String> iterator() {
        return new NameContainerIterator();   // a fresh, independent cursor each call
    }

    private class NameContainerIterator implements Iterator<String> {
        private int pos = 0;                   // inner class can read names/size

        @Override public boolean hasNext() { return pos < size; }

        @Override public String next() {
            if (!hasNext()) throw new NoSuchElementException();
            return names[pos++];
        }
    }

    public static void main(String[] args) {
        NameContainer c = new NameContainer(new String[]{"Aditya", "Rohit", "Rohan", "Monu"});

        Iterator<String> it = c.iterator();    // explicit iterator loop
        while (it.hasNext()) System.out.print(it.next() + " ");
        System.out.println();                  // Aditya Rohit Rohan Monu

        for (String name : c) System.out.print(name + " "); // for-each: same thing, sugar
        System.out.println();                  // Aditya Rohit Rohan Monu
    }
}
```

### Linked-list iterator with an anonymous class
```java
import java.util.Iterator;

public class TinyLinkedIterable implements Iterable<Integer> {
    private static class Node {
        int data; Node next;
        Node(int data) { this.data = data; }
    }
    private Node head, tail;

    void add(int x) {
        Node n = new Node(x);
        if (head == null) head = tail = n; else { tail.next = n; tail = n; }
    }

    @Override
    public Iterator<Integer> iterator() {
        return new Iterator<>() {            // anonymous class, used only here
            private Node current = head;
            public boolean hasNext() { return current != null; }
            public Integer next() {
                int data = current.data;
                current = current.next;
                return data;
            }
        };
    }

    public static void main(String[] args) {
        TinyLinkedIterable list = new TinyLinkedIterable();
        list.add(2); list.add(3); list.add(4); list.add(5);
        for (int x : list) System.out.print(x + " ");
        System.out.println();                // 2 3 4 5
    }
}
```

### Independent iterators make nested loops work
```java
import java.util.*;

public class NestedIteratorsDemo {
    public static void main(String[] args) {
        List<Integer> l = List.of(10, 20, 30);
        int pairs = 0;
        for (Iterator<Integer> outer = l.iterator(); outer.hasNext(); ) {
            int a = outer.next();
            for (Iterator<Integer> inner = l.iterator(); inner.hasNext(); ) { // new cursor
                int b = inner.next();
                if (a == 10) System.out.println(a + " " + b);
                pairs++;
            }
        }
        // 10 10
        // 10 20
        // 10 30
        System.out.println("pairs = " + pairs); // pairs = 9
    }
}
```

### `ConcurrentModificationException` and the correct fixes
```java
import java.util.*;

public class CmeDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        try {
            for (Integer v : list) {
                if (v == 3) list.remove(Integer.valueOf(3)); // modifies behind the iterator's back
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("CME caught");               // CME caught
        }

        List<Integer> a = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = a.iterator();
        while (it.hasNext()) {
            if (it.next() == 3) it.remove();                // safe: removes last returned element
        }
        System.out.println(a);                              // [1, 2, 4, 5]

        List<Integer> b = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        b.removeIf(x -> x % 2 == 0);                        // also safe
        System.out.println(b);                              // [1, 3, 5]

        List<Integer> c = new ArrayList<>(List.of(10, 20, 30, 40));
        c.remove(1);                                        // remove(int index), not value 1!
        System.out.println(c);                              // [10, 30, 40]
    }
}
```

## Common Pitfalls / Tips
- **Don't loop a `LinkedList` with `get(i)`.** Each call walks the chain, so the loop is O(n²). Use for-each or an iterator, which is O(n).
- **Don't modify a collection inside a for-each loop.** Use `iterator.remove()`, `removeIf`, or collect the changes and apply them after the loop.
- **Call `next()` once per `hasNext()` check.** Calling `next()` twice per iteration skips elements and can throw `NoSuchElementException`.
- **`it.remove()` needs a preceding `next()`.** Calling it twice in a row, or before `next()`, throws `IllegalStateException`.
- **`List<Integer>.remove(3)` removes by index.** Use `remove(Integer.valueOf(3))` to remove the value.
- **Iteration order depends on the implementation:** insertion order for lists and `LinkedHashSet`, sorted for `TreeSet`, effectively arbitrary for `HashSet`/`HashMap`. Don't rely on `HashSet` order.
- **The legacy `Stack` iterates bottom-to-top.** If you need pop order, use `ArrayDeque` with `push`/`pop`.
- **For-each needs an array or an `Iterable`.** A class that merely *has* `hasNext()`/`next()` methods doesn't qualify. It must implement `Iterable<T>`.
- **Return a new iterator from every `iterator()` call.** Returning a shared one brings back the nested-loop bug.
- **`Map` is not `Iterable`.** Iterate over `map.keySet()`, `map.values()` or `map.entrySet()`.

## Review Questions
1. Why can't a `for (int i …; get(i))` loop be used to traverse every collection, and why is it slow on a `LinkedList`?
2. What's the difference between `Iterable` and `Iterator`, and which methods does each declare?
3. Why is a collection's iterator usually written as an inner class of that collection?
4. Write `hasNext()` and `next()` for an array-backed iterator and for a linked-list iterator.
5. What does the compiler turn a for-each loop into? What error do you get if the target isn't `Iterable`?
6. What causes a `ConcurrentModificationException`, and what's the correct way to remove elements while iterating?
7. Why does `Iterator` have `remove()` but not `add()`?
8. Give two reasons why `ArrayList` doesn't implement `Iterator` directly.

<details>
<summary>Answers</summary>

1. Only lists have positions and `get(i)`. Sets, maps, stacks and queues don't support index access. On a `LinkedList`, every `get(i)` walks from the start (or the end) because there's no random access, so traversing n elements costs O(n²).
2. `Iterable<T>` is something that can *give you* an iterator: `iterator()` (plus the defaults `forEach` and `spliterator`). `Iterator<T>` is the cursor itself: `hasNext()`, `next()`, and the defaults `remove()` and `forEachRemaining()`.
3. An inner class can read the outer collection's private fields (the array, `size`, `head`) directly, so it can walk the real storage without exposing it. It's also hidden from users as an implementation detail.
4. Array: `hasNext() { return pos < size; }`, `next() { return arr[pos++]; }`. Linked list: `hasNext() { return current != null; }`, `next() { T d = current.data; current = current.next; return d; }`.
5. For an `Iterable` it becomes `Iterator<T> it = c.iterator(); while (it.hasNext()) { T x = it.next(); ... }`. Arrays get an index loop instead. A non-`Iterable` target gives "Can only iterate over an array or an instance of java.lang.Iterable".
6. A structural change to the collection made by anything other than the iterator itself while iterating (e.g. `list.remove(x)` inside a for-each). The iterator detects the changed modification count on the next `next()` and fails fast. Use `it.remove()` on an explicit iterator, or `collection.removeIf(...)`.
7. Removing "the element I just returned" is well defined for every collection. Adding isn't: in a hash set or tree set the new element's position is decided by its hash or order and might land behind the cursor, so it would never be visited. `ListIterator` adds `add()` for lists, where a position is meaningful.
8. (1) Separation of concerns / single responsibility: the collection stores data and the iterator traverses it. (2) The traversal state (the cursor) would live in the collection, so you couldn't run two traversals at once. Nested loops would interfere, and you'd need manual resets. A separate iterator per `iterator()` call solves both.
</details>

## Further Study
- `ListIterator`: bidirectional traversal plus `add`/`set` (in the List interface video)
- `Iterable.forEach` and `Iterator.forEachRemaining` with lambdas (functional interfaces)
- `Spliterator` and how streams split collections for parallel processing
- Fail-fast vs fail-safe iterators: `CopyOnWriteArrayList`, `ConcurrentHashMap`
- The JDK sources of `ArrayList.Itr`, `LinkedList.ListItr` and `HashMap.HashIterator`
- The Iterator design pattern (GoF) and the Single Responsibility Principle

---

[← #29](29-java-collection-framework-explained.md) · [Index](README.md) · [#31 →](31-collection-interface-methods.md)

---

[← #29](29-java-collection-framework-explained.md) · [Index](README.md) · [#31 →](31-collection-interface-methods.md)
