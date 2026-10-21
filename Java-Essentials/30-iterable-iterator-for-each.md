# 30 · Iterable, Iterator & the for-each Loop
> Full notes: [30-iterable-iterator-for-each.md](../30-iterable-iterator-for-each.md) · [▶ Video](https://youtu.be/LVwpxEZ6uMQ)

## Where `Iterable` sits
```
Object → Iterable → Collection → List / Queue / Set → concrete classes        (Map is outside Collection)
```
**`Iterable` is the root.** Every collection can be iterated over.

## Problem: iterate over *any* collection
**First attempt, the index loop:**
```java
for (int i = 0; i < list.size(); i++) System.out.println(list.get(i));
```
This works for an `ArrayList`, but **it isn't general**:

| Structure | Has `get(i)`? | Problem with the index loop |
| :--- | :--- | :--- |
| `ArrayList` | ✅ | works, O(1) per `get` |
| `LinkedList` | ✅ but slow | no random access: each `get(i)` **walks from the head**, so the whole loop is **O(n²)** |
| `Set`, `Map` | ❌ | stored by **hash**, so there are no positions |
| `Stack`, `Queue` | ❌ | LIFO/FIFO: you only reach the top or front |

**Second attempt:** use each structure's own API (`pop()` a stack, `poll()` a queue until it's empty). That **tightly couples** the loop to one structure. Change `new LinkedList<>()` to `new HashSet<>()` and the loop breaks.

## The solution: `Iterable` + `Iterator`
- **`Iterable`:** the root interface for *anything whose elements can be visited one by one*. Its key method is **`iterator()`**, which returns an **`Iterator`**.
- **`Iterator`:** a **separate** interface. It's the object that does the walking:
  - **`hasNext()`:** is there another element?
  - **`next()`:** return the element **and move forward**.
- The caller doesn't know *how* it moves (an array index, following nodes, going bucket by bucket). That's **abstraction**.

```java
public interface Iterable<T> { Iterator<T> iterator(); }   // "I can GIVE you an iterator"
public interface Iterator<E> { boolean hasNext(); E next(); }  // "I can WALK"
```

---

## Demo: one loop, any collection
**The same loop works on every collection.** Only the `new …` changes:
```java
import java.util.*;

public class OneLoopAnyCollection {
    // ONE method that works for ANY collection: it only knows about Iterator
    static void printAll(String label, Collection<Integer> c) {
        Iterator<Integer> it = c.iterator();         // ask the collection for its iterator
        System.out.print(label + ": ");
        while (it.hasNext()) {                       // is there another element?
            System.out.print(it.next() + " ");       // return it AND move forward
        }
        System.out.println();
    }

    public static void main(String[] args) {
        List<Integer> data = List.of(10, 20, 30, 40, 50);

        printAll("ArrayList ", new ArrayList<>(data));    // 10 20 30 40 50   (insertion order)
        printAll("LinkedList", new LinkedList<>(data));   // 10 20 30 40 50   (insertion order)
        printAll("ArrayDeque", new ArrayDeque<>(data));   // 10 20 30 40 50   (FIFO order)
        printAll("TreeSet   ", new TreeSet<>(List.of(30, 10, 50, 20, 40)));  // 10 20 30 40 50 (sorted: BST in-order)
        printAll("HashSet   ", new HashSet<>(data));      // e.g. 50 20 40 10 30 (hash order, NOT insertion order)
    }
}
```
**What's going on:**
- `printAll` has **no idea** which class it's been given. It only uses `iterator()`, `hasNext()` and `next()`, so **changing the implementation never breaks the loop**.
- **`next()` does two things:** it returns the current element **and** moves the cursor forward. When the cursor reaches the end, `hasNext()` returns `false` and the loop stops.
- **The order depends on the structure, not on the loop:**
  - lists → insertion order
  - `ArrayDeque` → FIFO order
  - `TreeSet` → **sorted**
  - `HashSet` → **hash order**
- **In the JDK:** `ArrayList` → `AbstractList` → `AbstractCollection` → implements `Collection` → extends **`Iterable<E>`**. That's why every collection has `iterator()`.

## How it works inside
**The chain:** `list.iterator()` → returns an object of some class that **implements `Iterator`** → you call `hasNext()`/`next()` on it.

- `ArrayList` is (indirectly) `Iterable`, so it **must** implement `iterator()`.
- `Iterator` is an interface, so it **can't be instantiated**. Some class has to implement it, and `iterator()` returns **a new object** of that class.
- That class is usually an **inner class** of the collection, so it can read the collection's **private** array or nodes directly.

```java
import java.util.Iterator;

public class HowItWorksInside {
    public static void main(String[] args) {
        MyList list = new MyList();
        list.add(1); list.add(2); list.add(3);

        Iterator<Integer> it = list.iterator();      // 1. ask for an iterator → a NEW MyListIterator
        System.out.println(it.getClass().getSimpleName());   // MyListIterator
        while (it.hasNext()) {                       // 2. pos < size ?
            System.out.print(it.next() + " ");       // 3. return arr[pos], then pos++
        }
        // 1 2 3
    }
}

class MyList implements Iterable<Integer> {
    private int[] arr = new int[10];                 // private storage...
    private int size = 0;

    void add(int x) { arr[size++] = x; }

    @Override
    public Iterator<Integer> iterator() {
        return new MyListIterator();                 // a NEW iterator every time it's called
    }

    // inner class: it can read the outer class's PRIVATE arr and size
    private class MyListIterator implements Iterator<Integer> {
        private int pos = 0;                         // the cursor lives in the ITERATOR, not the list
        public boolean hasNext() { return pos < size; }
        public Integer next()    { return arr[pos++]; }   // postfix: return the current one, then advance
    }
}
```
**Trace for `[1, 2, 3]`:**
```
pos=0 → hasNext? 0<3 ✓ → next() returns arr[0]=1, pos=1
pos=1 → hasNext? 1<3 ✓ → next() returns arr[1]=2, pos=2
pos=2 → hasNext? 2<3 ✓ → next() returns arr[2]=3, pos=3
pos=3 → hasNext? 3<3 ✗ → loop ends
```
- **Why an inner class:** the iterator needs `arr` and `size`, which are **private** to the list. A class **nested inside** the list can read them directly, with no need to pass objects around.
- In the real JDK, these are private inner classes: `ArrayList.Itr`, `LinkedList.ListItr`, and `HashMap`'s `KeyIterator` / `ValueIterator` / `EntryIterator`.

---

## Conceptual iterators for other structures (brief)
Every structure answers the same two questions, *"is there more?"* and *"give me the next one"*, in its own way:

| Structure | Cursor | `hasNext()` | `next()` |
| :--- | :--- | :--- | :--- |
| **ArrayList** | `int pos = 0` | `pos < size` | `return arr[pos++];` |
| **LinkedList** | `Node current = head` | `current != null` | `data = current.data; current = current.next; return data;` |
| **HashSet / HashMap** | bucket index + node | any non-empty bucket left? | walk **bucket by bucket**, and inside a bucket along its chain |
| **TreeSet / TreeMap** | a stack of nodes | stack not empty | **in-order traversal**, so elements come out **sorted** |
| **ArrayDeque** (deque/queue) | index in a circular array | items left? | walk from the **head** to the tail of the circular array |

- **Maps aren't `Iterable` themselves.** Iterate over `map.keySet()`, `map.values()` or `map.entrySet()`.
- **The legacy `Stack` class** iterates **bottom to top** (insertion order), **not** in pop order. `ArrayDeque` used as a stack (`push`) iterates **top first**.
- **`ListIterator`** (lists only) can also go **backwards** (`hasPrevious()`/`previous()`) and can `add`/`set`. It's covered in #32.

---

## Build your own iterable: `NameContainer`
**Step 1:** implement `Iterable<String>`. Without `iterator()`, you get *"must implement the inherited abstract method"*.
**Step 2:** return an iterator, either from a **named inner class** or an **anonymous class**.
**Step 3:** use it with an iterator loop, **or with for-each**.

```java
import java.util.Iterator;
import java.util.NoSuchElementException;

public class NameContainerDemo {
    public static void main(String[] args) {
        NameContainer names = new NameContainer(new String[]{"Aditya", "Rohit", "Rohan", "Monu"});

        // 1) the explicit iterator loop
        Iterator<String> it = names.iterator();
        while (it.hasNext()) System.out.print(it.next() + " ");
        System.out.println();                       // Aditya Rohit Rohan Monu

        // 2) the for-each loop: the SAME thing, written shorter
        for (String name : names) System.out.print(name + " ");
        System.out.println();                       // Aditya Rohit Rohan Monu

        // 3) the anonymous-class version behaves the same way
        for (String name : names.reversed()) System.out.print(name + " ");
        System.out.println();                       // Monu Rohan Rohit Aditya
    }
}

class NameContainer implements Iterable<String> {
    private final String[] names;
    private final int size;

    NameContainer(String[] names) { this.names = names; this.size = names.length; }

    // Version A: a NAMED private inner class
    @Override
    public Iterator<String> iterator() {
        return new NameContainerIterator();
    }

    private class NameContainerIterator implements Iterator<String> {
        private int pos = 0;
        @Override public boolean hasNext() { return pos < size; }
        @Override public String next() {
            if (!hasNext()) throw new NoSuchElementException();   // good practice
            return names[pos++];
        }
    }

    // Version B: an ANONYMOUS class (used in only one place, so it doesn't need a name)
    Iterable<String> reversed() {
        return () -> new Iterator<String>() {          // Iterable has one method, so a lambda works
            private int pos = size - 1;
            @Override public boolean hasNext() { return pos >= 0; }
            @Override public String next()     { return names[pos--]; }
        };
    }
}
```
- **Named inner class vs anonymous class:** if the iterator class is used in only **one place**, write `return new Iterator<String>() { … };` directly inside `iterator()` and delete the named class. The output is the same.
- **Good practice:** `next()` should throw **`NoSuchElementException`** when there's nothing left.

## The for-each loop is just sugar
```java
for (String name : names) {          // what you write
    System.out.println(name);
}

Iterator<String> it = names.iterator();   // what the compiler turns it into
while (it.hasNext()) {
    String name = it.next();
    System.out.println(name);
}
```
- The compiler **rewrites** for-each into the `iterator()` / `hasNext()` / `next()` loop. (For **arrays**, it becomes an index loop instead.)
- **Proof:** remove `implements Iterable` from `NameContainer`, and the for-each loop no longer compiles: *"Can only iterate over an array or an instance of java.lang.Iterable"*.
- So **any class you write** can be used in a for-each loop, as long as it implements `Iterable`.

---

## Other methods of `Iterable` and `Iterator`
| Interface | Methods |
| :--- | :--- |
| **`Iterable`** | `iterator()`, `forEach(Consumer)` (#37), `spliterator()` (#39) |
| **`Iterator`** | `hasNext()`, `next()`, **`remove()`**, `forEachRemaining(Consumer)` |

- **`it.remove()`** removes the element **last returned by `next()`**. It's a `default` method that throws `UnsupportedOperationException` unless it's overridden, so you only **have** to write `hasNext()` and `next()`.
- **Why doesn't `Iterator` have `add()`?** For a set, a new element goes wherever its hash puts it, which might be **behind** the cursor, so the iterator would never visit it. A general `add` would be ill-defined. **`ListIterator`** (lists only) does have `add`.

## `ConcurrentModificationException`
```java
import java.util.*;

public class CmeDemo {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        try {
            for (Integer value : list) {
                System.out.print(value + " ");
                if (value == 3) list.remove(Integer.valueOf(3));   // ❌ modifying the LIST during iteration
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("→ ConcurrentModificationException");
        }
        // 1 2 3 → ConcurrentModificationException

        // ✅ Fix 1: remove THROUGH the iterator
        List<Integer> a = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Iterator<Integer> it = a.iterator();
        while (it.hasNext()) if (it.next() == 3) it.remove();
        System.out.println(a);                         // [1, 2, 4, 5]

        // ✅ Fix 2: removeIf
        List<Integer> b = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        b.removeIf(x -> x == 3);
        System.out.println(b);                         // [1, 2, 4, 5]
    }
}
```
- **Why it happens:** the list was changed **behind the iterator's back**, so the iterator can no longer tell what it has and hasn't visited (in a set, the removed element could be anywhere).
- This is **fail-fast** behavior: it throws immediately rather than guessing.
- ⚠️ `list.remove(value)` with an **`int`** calls `remove(int index)`, so it removes **by position**. Use `Integer.valueOf(3)` to remove **by value**.
- ⚠️ Fail-fast is **best effort**. Removing the **second-to-last** element happens to end the loop without an exception, so don't rely on it to find bugs.

---

## Why a separate `Iterator`? Why doesn't `ArrayList` implement `Iterator` itself?
**Reason 1: separation of concerns (the Single Responsibility Principle).** The collection's job is **storing** data. **Walking** over it is a separate job, so it belongs in a separate object.

**Reason 2 (the more important one): state.** If the list kept the cursor (`pos`) itself:
- After one full loop, `pos == size`, so a **second loop prints nothing** unless you call `reset()`.
- **Nested loops break**, because the inner loop moves the **same** `pos` as the outer loop.

```java
import java.util.*;

public class WhySeparateIterator {
    public static void main(String[] args) {
        // ❌ A list that IS its own iterator: one shared cursor
        SelfIteratingList bad = new SelfIteratingList(10, 20, 30);
        System.out.print("shared cursor:      ");
        while (bad.hasNext()) {
            int a = bad.next();
            while (bad.hasNext()) System.out.print(a + "-" + bad.next() + " ");   // moves the SAME cursor
        }
        System.out.println();
        // shared cursor:      10-20 10-30           ← the outer loop never gets to 20 or 30

        // ✅ Real collections: every iterator() call gives a NEW, independent cursor
        List<Integer> good = List.of(10, 20, 30);
        System.out.print("independent cursor: ");
        for (int a : good) for (int b : good) System.out.print(a + "-" + b + " ");
        System.out.println();
        // independent cursor: 10-10 10-20 10-30 20-10 20-20 20-30 30-10 30-20 30-30
    }
}

class SelfIteratingList {
    private final int[] arr;
    private int pos = 0;                                   // the cursor is stored IN the list ❌
    SelfIteratingList(int... values) { arr = values; }
    boolean hasNext() { return pos < arr.length; }
    int next()        { return arr[pos++]; }
}
```
- **With `Iterable`**, the cursor lives in the **iterator object**, and **each `iterator()` call returns a new, independent iterator**. The outer and inner loops each get their own. Adding a fixed number of extra cursor fields to the list wouldn't scale to 3 or 4 nested loops.

**Remember:** **`Iterable`** = "I can give you an iterator" (`iterator()`). **`Iterator`** = "I can walk" (`hasNext()`, `next()`, `remove()`). One `while (it.hasNext()) it.next()` loop works for **every** collection. **for-each** is just shorter syntax for that loop, and it works on anything `Iterable`. Don't change a collection while iterating over it. Use `it.remove()` or `removeIf`. Each `iterator()` call returns a **fresh cursor**, which is why nested loops work.
