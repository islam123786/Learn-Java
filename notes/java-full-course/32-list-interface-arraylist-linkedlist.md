# Java List Interface Deep Dive | ArrayList, LinkedList, Vector, Stack | Java Full Course #32
> Source: https://youtu.be/OfNtIOGDqzY · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 44:37. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `List<E>` is an **ordered** collection: it keeps insertion order and gives **position (index) access**. Its implementations are `ArrayList`, `LinkedList`, `Vector`, and `Stack` (which extends `Vector`).
- Since **Java 21** the hierarchy is `Iterable`, `Collection`, **`SequencedCollection`**, `List`. `SequencedCollection` adds `getFirst/getLast`, `addFirst/addLast`, `removeFirst/removeLast` (and `reversed()`).
- The `List`-specific methods are all about positions: `get(i)`, `set(i, e)`, `add(i, e)`, `addAll(i, c)`, `remove(int i)`, `indexOf`, `lastIndexOf`, `listIterator()` (bidirectional, with `add`/`set`), plus the factories `List.of(...)` and `List.copyOf(c)`, which return **unmodifiable** lists.
- **`ArrayList`** is a dynamic array that grows by **1.5×**. `get`/`set` are O(1). Inserting or removing in the middle is O(n) because of shifting. It's cache-friendly and the default choice. Extra methods: `ensureCapacity`, `trimToSize`. Know the difference between **capacity and size**.
- **`LinkedList`** is a **doubly linked list** with `first`/`last` pointers. Index access walks from the nearer end, so it's still O(n). It doubles as a `Queue`/`Deque`. It's rarely the better choice. **`Vector`/`Stack`** are legacy, synchronized classes; use `ArrayList` and `ArrayDeque` instead.

## Key Concepts

**`List` contract.** Ordered, index-based (0…size-1), duplicates allowed. *Why it matters:* any list (array-backed or node-based) can be used through the same `List<E>` reference, with only the performance differing.

**`SequencedCollection` (Java 21).** A new interface between `Collection` and `List` (also the parent of `Deque` and of `SequencedSet`/`LinkedHashSet`) for collections with a defined first and last element. *Why it matters:* `list.getFirst()` replaces `list.get(0)`, and `list.getLast()` replaces `list.get(list.size() - 1)`.

**Positional methods.** `get(int)`, `set(int, E)` (replaces and returns the old value), `add(int, E)` (inserts and shifts), `remove(int)` (removes by index and returns the element), `indexOf`/`lastIndexOf(Object)` (search with `equals`, -1 if absent). *Why it matters:* these are what make a list a list. Watch out for the `add` and `remove` overloads inherited from `Collection`.

**`ListIterator`.** A richer iterator for lists: `hasPrevious()`/`previous()`, `nextIndex()`/`previousIndex()`, and `add`, `set` and `remove` *during* iteration with no `ConcurrentModificationException`. `listIterator(n)` starts with the cursor before index n. *Why it matters:* it's the safe way to change a list while walking it, and to walk it backward.

**Unmodifiable lists.** `List.of(...)` and `List.copyOf(collection)` create lists that throw `UnsupportedOperationException` on `add`, `remove` or `set`. *Why it matters:* they're safe constants and defensive copies, but a common source of runtime exceptions.

**`ArrayList` internals.** A backing `Object[]`, a `size` field, and growth by `newCapacity = old + old/2` (1.5×) with a copy of the old elements. **Capacity** is the length of the backing array. **Size** is the number of elements stored. *Why it matters:* it explains the amortized O(1) append, the O(n) middle insert, and when to use `ensureCapacity`/`trimToSize`.

**`LinkedList` internals.** Doubly linked `Node { item, prev, next }` with `first`/`last` references. `get(i)` starts from whichever end is closer (`i < size/2` starts from the front). It also implements `Deque`. *Why it matters:* O(1) at the ends, O(n) anywhere by index.

**Legacy `Vector`/`Stack`.** These predate the Collection Framework (Java 1.0). Every method is `synchronized`, which adds overhead. They were retrofitted to implement `List` in Java 1.2. *Why it matters:* you'll see them in old code and interviews, but modern code uses `ArrayList`, `ArrayDeque`, or the concurrent collections.

## Detailed Notes

### The `List` hierarchy [01:36]
- `Object`, `Iterable`, `Collection`, then three branches: `Set`, `Queue`, `List`. This video covers **`List`**.
- The interfaces need concrete classes that implement their methods:
  - `ArrayList` and `LinkedList` (both familiar),
  - `Vector`,
  - `Stack`, which doesn't implement `List` directly but **extends `Vector`**.

### What makes something a list [01:55]
- It **preserves insertion order** [02:12] and you can **access elements by position/index**: in `[2, 3, 4, 5]`, `get(2)` is 4.
- `ArrayList`: random access, so it jumps straight to the index.
- `LinkedList` [03:05]: nodes have no indices, but it's still a list. Positions are *simulated* by walking from the head (position 0, 1, 2, …).
  - *(Correction: the video calls a single `get` on a linked list O(n²). One `get(i)` is O(n). Calling `get(i)` for every i in a loop is what makes it O(n²).)*
- Program to the interface: `List<Integer> l = new ArrayList<>();` (or `LinkedList`). The same methods work through `l`.

### Java 21: `SequencedCollection` [05:14]
- Before Java 21: `Iterable`, `Collection`, `List` (plus some intermediate abstract classes).
- Java 21 inserted **`SequencedCollection`** [05:41] between `Collection` and `List`. It extends `Collection`, and `List` now extends it.
- Its methods [06:37] are `getFirst()`, `getLast()`, `addFirst(e)`, `addLast(e)`, `removeFirst()`, `removeLast()`. *(Also `reversed()`, which returns a reverse-ordered view.)* `ArrayList`, `LinkedList` and the other concrete classes implement them.

### `List`-specific methods [07:26]
- `E get(int index)` returns the element at that position. It's the most-used list method.
- `E set(int index, E element)` [08:33] **replaces** the element. On `[2, 3, 4, 5]`, `set(1, 6)` gives `[2, 6, 4, 5]`. `ArrayList` jumps there directly, while `LinkedList` walks from the head and then overwrites.
- `void add(int index, E element)` [10:00] **inserts**. On `[2, 3, 4, 5]`, `add(1, 6)` gives `[2, 6, 3, 4, 5]`, and the size goes from 4 to 5.
  - `ArrayList` shifts 3, 4, 5 one place to the right and writes 6 into the gap.
  - `LinkedList` walks to position 1, creates a node, and re-links `2 → 6 → 3` with no shifting.
  - It's different from `Collection.add(e)`, which appends at the end. The two are overloads.
- `boolean addAll(int index, Collection<? extends E> c)` [12:34] inserts a whole collection at a position. It returns `true` if the list changed.
- `E remove(int index)` [13:55] removes by **position**, while `Collection.remove(Object)` removes by **value** (using `equals`).
  - *(Correction: the video says `remove(index)` is O(1) for `ArrayList`. It's O(1) only for the **last** index. Elsewhere it shifts everything after it to the left, which is O(n), as the video itself says later. Removing by value is O(n) for both kinds of list, since you have to find the element first.)*
- `int indexOf(Object o)` and `int lastIndexOf(Object o)` [14:53] return the first and last positions of a value, or -1. They take `Object` because they compare with `equals`.
- `ListIterator<E> listIterator()` [16:08] is a list-only iterator. It can move forward **and backward** and can **insert, replace and remove** while iterating without a `ConcurrentModificationException` (unlike a plain `Iterator`, which only has `remove`).
  - `listIterator(int index)` starts the cursor at that index.
- `static List.of(...)` [17:32] creates an **immutable** list. `List<Integer> l = List.of(1, 2, 3); l.add(4);` throws.
- `static List.copyOf(Collection)` [18:22] makes an unmodifiable copy of any collection.
  - *(Precision: `List.of` takes individual elements, with overloads for 0 to 10 arguments plus varargs, not a collection. Use `List.copyOf` for a collection.)*

### Demo [18:42]
- Start with `import java.util.*; List<Integer> list = new ArrayList<>(); list.add(1); list.add(2); list.add(3);`
- `list.get(1)` [19:39] gives `2`. `list.set(1, 5)` makes `println(list)` show `[1, 5, 3]`, because `toString` is overridden.
- `list.addAll(0, List.of(9, 8, 7))` [20:41] gives `[9, 8, 7, 1, 5, 3]`. Then `list.remove(0)` gives `[8, 7, 1, 5, 3]`.
- `indexOf(2)` and `lastIndexOf(5)` work as described above.
- `ListIterator<Integer> it = list.listIterator();` [22:02] with `while (it.hasNext()) println(it.next())` walks the whole list.
- **Backward** [23:28] on `[1, 2, 3]`:
  - `listIterator(2)` with `hasPrevious()`/`previous()` gives `2, 1`. The cursor sits *before* index 2, so the previous element is 2.
  - `listIterator(3)`, i.e. `size()`, gives `3, 2, 1`.
  - `previousIndex()` [24:33] instead gives `2, 1, 0` (you still have to call `previous()` to move).
- **Unmodifiable** [24:51]: `List<Integer> list = List.of(1, 2, 3, 4, 5, 6, 7, 8);` prints fine. `list.add(9)` throws **`UnsupportedOperationException`** [26:01]. A `List.copyOf(list)` copy behaves the same way.

### `ArrayList` [26:29]
- It's a class backed by a normal array that it resizes: a **dynamic array**. When the array is full, `add` allocates a bigger one and copies.
- The from-scratch version (video #29) doubled the size. **Java grows by 1.5×** [27:32]: `newCapacity = oldCapacity + (oldCapacity >> 1)`. So 4 becomes 6, then 9, then 13, and so on.
  - *(The transcript garbles the formula as "multiplied by two". It's the old capacity plus **half** of it.)*
- Performance [28:12]:
  - `get(i)` and `set(i, e)` are **O(1)** thanks to random access.
  - `add(i, e)` and `remove(i)` in the middle are **O(n)**, because elements shift right or left.
  - `add(e)` at the end is amortized O(1).
- Why it's the most popular list (the video estimates about 90% of usage) [29:18]: random access, a **cache-friendly** contiguous layout, and a simple structure.
- Constructors [29:47]:
  - `new ArrayList<>()` creates an empty list.
  - `new ArrayList<>(10)` sets the initial **capacity**. After that it grows by 1.5×.
  - `new ArrayList<>(collection)` copies another collection, e.g. `new ArrayList<>(List.of(...))`.
  - *(Detail: the no-arg constructor starts with an empty shared array and allocates capacity 10 on the first `add`.)*
- `ArrayList` implements the methods of `Iterable`, `Collection`, `SequencedCollection` and `List`. Its only notable extras are:
  - `ensureCapacity(int min)` [31:10]. If you know about 100 elements are coming, call `l.ensureCapacity(100)` once to avoid repeated grow-and-copy steps (4, 6, 9, 13, …). *(The video says "the compiler" does this. It's the `ArrayList` itself, at runtime.)*
  - `trimToSize()` [32:17] shrinks the backing array to exactly `size`, freeing unused slots once the list won't grow any more.
- **Capacity vs size** [32:48]. Capacity is how many elements fit before a resize. Size is how many are stored now. With capacity 4, adding 1, 2, 4, 7 takes the size from 1 to 4. The next add grows the capacity to 6 while the size becomes 5.

### `LinkedList` [33:50]
- It's built from a `Node` class, a **static nested class** inside `LinkedList`. The objects live on the heap.
- A singly linked list has only `next`. Java's `LinkedList` is a **doubly linked list** [35:12]: each node has `prev` and `next` (left and right), so you can traverse in both directions. That makes reverse traversal with `ListIterator.previous()` easy.
- It keeps **two pointers**, `first` and `last` [36:50]. It never moves them while traversing. It copies them into a temporary reference (`Node temp = first;`) and moves that instead.
- **Nearest-end optimization** [38:45]: `get(index)` checks whether `index < size / 2`. If so it walks from `first`, otherwise from `last`. In a 100-element list, `get(75)` takes 25 steps from the end instead of 75 from the start.
- Cost: `get`, `set`, `add(i)` and `remove(i)` are all **O(n)**, because you have to reach the position. Once there, inserting or removing only re-links pointers, with no shifting.
- `LinkedList` is also used to implement **queues and stacks** [40:32]. It implements `Deque`, so it has front/back methods (`offer`, `poll`, `peek`, `push`, `pop`, …), covered in the Queue video.
- In production it's rarely needed (the video estimates under 10% of usage) [41:08]. Reads usually outnumber writes, `get` is slow on a linked list, and an `ArrayList` can do everything a `LinkedList` can.

### `Vector` and `Stack`: legacy [42:02]
- **Legacy classes** [42:12] from before the Collection Framework existed. Back then Java had `Vector` (a dynamic array, now replaced by `ArrayList`) and `Hashtable` (now replaced by `HashMap`). `Stack` extends `Vector` and is an array-based stack.
- Why they're avoided:
  - The API is old and outdated.
  - They're **thread-safe** [43:00] (every method is synchronized), which adds overhead even in single-threaded code.
  - Modern thread-safe alternatives exist (covered in the concurrency videos). *(For example `Collections.synchronizedList`, `CopyOnWriteArrayList`, and `ConcurrentLinkedDeque`/`LinkedBlockingDeque`.)*
- To fit them into the framework, `Vector` was made to implement `List`, and `Stack` simply extends `Vector`.
- **Stacks in modern Java** [44:13]: there's no `Stack` interface among `List`/`Queue`/`Set`. Use **`ArrayDeque`**, which works as both a queue and a stack and is faster than `Stack`. *(The transcript says "non-array deque" and "set implementation". It means `ArrayDeque` and stack implementation.)*

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java ClassName.java` (Java 21+ for the `SequencedCollection` methods).

### Positional `List` methods
```java
import java.util.*;

public class ListMethodsDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3)); // or new LinkedList<>(...)
        System.out.println(list.get(1));            // 2
        System.out.println(list.set(1, 5));         // 2 (returns the old value)
        System.out.println(list);                   // [1, 5, 3]

        list.add(1, 6);                             // insert + shift
        System.out.println(list);                   // [1, 6, 5, 3]
        list.addAll(0, List.of(9, 8, 7));
        System.out.println(list);                   // [9, 8, 7, 1, 6, 5, 3]

        System.out.println(list.remove(0));         // 9 (remove by INDEX returns the element)
        System.out.println(list.remove(Integer.valueOf(6))); // true (remove by VALUE)
        System.out.println(list);                   // [8, 7, 1, 5, 3]

        list.add(5);
        System.out.println(list.indexOf(5) + " " + list.lastIndexOf(5) + " " + list.indexOf(42)); // 3 5 -1
    }
}
```

### Java 21 `SequencedCollection` methods
```java
import java.util.*;

public class SequencedListDemo {
    public static void main(String[] args) {
        List<String> l = new ArrayList<>(List.of("b", "c"));
        l.addFirst("a");
        l.addLast("d");
        System.out.println(l);                         // [a, b, c, d]
        System.out.println(l.getFirst() + l.getLast()); // ad
        System.out.println(l.reversed());              // [d, c, b, a] (a view)
        l.removeFirst();
        l.removeLast();
        System.out.println(l);                         // [b, c]
    }
}
```

### `ListIterator`: backward, indices, and modifying while iterating
```java
import java.util.*;

public class ListIteratorDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3));

        ListIterator<Integer> back = list.listIterator(list.size()); // cursor after the last element
        StringBuilder sb = new StringBuilder();
        while (back.hasPrevious()) sb.append(back.previous()).append(' ');
        System.out.println(sb.toString().trim());   // 3 2 1

        ListIterator<Integer> from2 = list.listIterator(2);
        System.out.println(from2.previous() + " " + from2.previous()); // 2 1

        ListIterator<Integer> idx = list.listIterator(3);
        StringBuilder ix = new StringBuilder();
        while (idx.hasPrevious()) { ix.append(idx.previousIndex()).append(' '); idx.previous(); }
        System.out.println(ix.toString().trim());   // 2 1 0

        ListIterator<Integer> it = list.listIterator();
        while (it.hasNext()) {
            int v = it.next();
            if (v == 2) { it.set(20); it.add(25); } // replace, then insert after: no CME
        }
        System.out.println(list);                   // [1, 20, 25, 3]
    }
}
```

### Unmodifiable lists: `List.of` and `List.copyOf`
```java
import java.util.*;

public class ImmutableListDemo {
    public static void main(String[] args) {
        List<Integer> fixed = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        System.out.println(fixed);                       // [1, 2, 3, 4, 5, 6, 7, 8]
        try {
            fixed.add(9);
        } catch (UnsupportedOperationException e) {
            System.out.println("UnsupportedOperationException"); // UnsupportedOperationException
        }

        List<Integer> source = new ArrayList<>(List.of(1, 2));
        List<Integer> copy = List.copyOf(source);        // unmodifiable snapshot
        source.add(3);
        System.out.println(source + " " + copy);         // [1, 2, 3] [1, 2]
        try {
            copy.set(0, 99);
        } catch (UnsupportedOperationException e) {
            System.out.println("copy is unmodifiable too"); // copy is unmodifiable too
        }
    }
}
```

### `ArrayList` growth: capacity vs size
```java
import java.util.*;

public class ArrayListGrowthDemo {
    // Mimics ArrayList's growth rule: new = old + old/2
    static int grow(int old) { return old + (old >> 1); }

    public static void main(String[] args) {
        int cap = 4;
        StringBuilder sb = new StringBuilder("capacities: " + cap);
        for (int i = 0; i < 4; i++) { cap = grow(cap); sb.append(" -> ").append(cap); }
        System.out.println(sb);                  // capacities: 4 -> 6 -> 9 -> 13 -> 19

        ArrayList<Integer> l = new ArrayList<>(4); // capacity 4, size 0
        System.out.println(l.size());            // 0
        l.ensureCapacity(100);                   // grow once, up front
        for (int i = 0; i < 5; i++) l.add(i);
        l.trimToSize();                          // backing array shrinks to size
        System.out.println(l.size() + " " + l);  // 5 [0, 1, 2, 3, 4]
    }
}
```

### `LinkedList` as a list *and* a deque, plus `ArrayDeque` as the modern stack
```java
import java.util.*;

public class LinkedListDequeDemo {
    public static void main(String[] args) {
        LinkedList<Integer> ll = new LinkedList<>(List.of(2, 3, 4));
        ll.addFirst(1);                          // O(1) at either end
        ll.addLast(5);
        System.out.println(ll);                  // [1, 2, 3, 4, 5]
        System.out.println(ll.get(3));           // 4 (walks from the nearer end)
        System.out.println(ll.poll() + " " + ll.pollLast()); // 1 5 (queue/deque methods)
        System.out.println(ll);                  // [2, 3, 4]

        Deque<Integer> stack = new ArrayDeque<>(); // preferred over legacy Stack
        stack.push(10); stack.push(20); stack.push(30);
        System.out.println(stack.pop() + " " + stack.peek()); // 30 20

        Stack<Integer> legacy = new Stack<>();   // extends Vector, so it is also a List
        legacy.push(1); legacy.push(2);
        System.out.println(legacy + " " + (legacy instanceof List)); // [1, 2] true
    }
}
```

## Common Pitfalls / Tips
- **`list.remove(1)` on a `List<Integer>` removes index 1**, not the value 1. Use `remove(Integer.valueOf(1))` to remove by value.
- **`set` replaces, `add(i, e)` inserts.** `set` keeps the size and returns the old element. `add(i, e)` grows the size by one and shifts.
- **`ArrayList.remove(i)` and `add(i, e)` are O(n)**, except at the end. Avoid repeated inserts at the front of a large `ArrayList`. Use `ArrayDeque` if you need a queue.
- **Don't index-loop a `LinkedList`.** `for (i…) ll.get(i)` is O(n²). Use for-each or a `ListIterator`.
- **`List.of` / `List.copyOf` / `Arrays.asList` are not regular `ArrayList`s.** The first two are fully unmodifiable and reject `null` elements. `Arrays.asList` is fixed-size: `set` works, `add`/`remove` throw.
- **Pre-size big lists** with `new ArrayList<>(expected)` or `ensureCapacity`. `trimToSize` helps only for long-lived lists that won't grow again.
- **Capacity isn't size.** `new ArrayList<>(10).get(0)` throws `IndexOutOfBoundsException`, because the size is still 0.
- **Modify a list during iteration only through its `ListIterator`** (`add`, `set`, `remove`), or with `removeIf`/`replaceAll`.
- **Avoid `Vector` and `Stack` in new code.** Use `ArrayList` and `ArrayDeque`, and the concurrent collections or `Collections.synchronizedList` when you need thread safety.
- **`getFirst()`/`getLast()` need Java 21.** On older versions use `get(0)` and `get(size() - 1)`, or `Deque`'s `peekFirst`/`peekLast`.

## Review Questions
1. What two properties define a `List`, and how can a `LinkedList` offer index access without real indices?
2. What did Java 21 change in the `List` hierarchy, and which methods does `SequencedCollection` add?
3. Explain the difference between `set(1, 6)` and `add(1, 6)` on `[2, 3, 4, 5]`, and between `remove(int)` and `remove(Object)`.
4. What can a `ListIterator` do that an `Iterator` can't? What does `listIterator(list.size())` let you do?
5. How does `ArrayList` grow, and what's the difference between capacity and size? When would you use `ensureCapacity` and `trimToSize`?
6. Give the time complexity of `get`, `set`, `add(i, e)` and `remove(i)` for `ArrayList` and for `LinkedList`.
7. Why is Java's `LinkedList` doubly linked, and what optimization does `get(index)` use?
8. Why are `Vector` and `Stack` considered legacy, and what should you use instead?

<details>
<summary>Answers</summary>

1. It preserves insertion order and gives positional (index) access. A `LinkedList` simulates indices by counting nodes from the head (or the tail) until it reaches position i. That's correct, but O(n) per access.
2. `SequencedCollection` was inserted between `Collection` and `List`. It adds `getFirst`, `getLast`, `addFirst`, `addLast`, `removeFirst`, `removeLast`, plus `reversed()`.
3. `set(1, 6)` replaces 3 to give `[2, 6, 4, 5]` (the size stays 4). `add(1, 6)` inserts to give `[2, 6, 3, 4, 5]` (the size becomes 5). `remove(int)` removes the element at that index and returns it. `remove(Object)` removes the first element `equal` to the argument and returns a `boolean`.
4. It can move backward (`hasPrevious`/`previous`), report indices (`nextIndex`/`previousIndex`), and `add`/`set`/`remove` during iteration without a `ConcurrentModificationException`. `listIterator(list.size())` puts the cursor after the last element, so you can walk the list in reverse with `previous()`.
5. When full, it allocates `old + old/2` (1.5×) and copies the old elements across. Capacity is the backing array's length. Size is the number of elements stored. Use `ensureCapacity(n)` before adding many elements to avoid repeated resizes. Use `trimToSize()` once the list is final, to release unused slots.
6. `ArrayList`: `get`/`set` O(1); `add(i, e)`/`remove(i)` O(n) because of shifting (O(1) amortized at the end). `LinkedList`: all four are O(n) to reach the position (the re-linking itself is O(1)), and operations at either end are O(1).
7. With `prev` and `next` links it can traverse in both directions (which `ListIterator.previous()` needs) and do O(1) operations at both ends via the `first`/`last` pointers. `get(index)` starts from `first` if `index < size/2`, otherwise from `last`.
8. They predate the Collection Framework, have dated APIs, and synchronize every method, which is slower even in single-threaded code. Use `ArrayList` instead of `Vector`, `ArrayDeque` instead of `Stack`, and concurrent collections (`CopyOnWriteArrayList`, `Collections.synchronizedList`, concurrent deques) when you need thread safety.
</details>

## Further Study
- `Queue`/`Deque` interfaces and `ArrayDeque` internals (circular array), from the upcoming Queue video
- `ArrayList` vs `LinkedList` benchmarks: CPU caches and why linked lists usually lose
- `List.subList`, `replaceAll`, `sort(Comparator)` and `Collections` helpers for lists
- `Arrays.asList` vs `List.of` vs `new ArrayList<>(...)`: mutability and `null` handling
- `RandomAccess` marker interface and how algorithms use it
- Thread-safe lists: `Collections.synchronizedList`, `CopyOnWriteArrayList`
- JEP 431: Sequenced Collections (Java 21)

---

[← #31](31-collection-interface-methods.md) · [Index](../../ReadMe.md) · [#33 →](33-set-map-hashmap-internals.md)
