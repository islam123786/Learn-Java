# 31 · The `Collection` Interface & Its Methods
> Full notes: [31-collection-interface-methods.md](../31-collection-interface-methods.md) · [▶ Video](https://youtu.be/cazzzwdLAkQ)

## Where `Collection` sits
```
Iterable → Collection → List / Set / Queue → concrete classes
```
- **`Collection<E> extends Iterable<E>`**. (The video says it the other way round, which is a slip.)
- `Collection` is the **parent of every data structure** in the framework (except `Map`). It defines what **any** collection can do, whatever its internal structure.
- **Polymorphism:** `Collection<Integer> c = new ArrayList<>();` works with **any** concrete class (`LinkedList`, `HashSet`, `ArrayDeque`, …), and `c.` gives you every `Collection` method.

## `Collection` vs `Collections`
| `Collection` | `Collections` (with an extra **s**) |
| :--- | :--- |
| an **interface**: the parent of List/Set/Queue | a **utility class** of static helpers (`sort`, `reverse`, `max`, …) (#36) |

- The same pattern elsewhere: `Object` (the parent class) vs `Objects` (the utility class).

---

## What's the point of the `Collection` interface?
- **It holds the operations that are common to *all* containers, declared once.** Ask yourself what you can do with *any* box of things: add, remove, check whether something's in it, count, empty it, walk through it. Those are exactly its methods.
- **Methods specific to one structure** live lower down: index access in **`List`**, front/back operations in **`Queue`**, sorted navigation in **`TreeSet`**.
- **Write code once, for every collection.** A method that takes a `Collection<E>` works with lists, sets and queues alike:

```java
import java.util.*;

public class CollectionPurpose {
    // works for ANY collection, because it only uses Collection methods
    static void describe(Collection<Integer> c) {
        c.add(3);                                     // try adding a duplicate
        System.out.println(c.getClass().getSimpleName() + " " + c
                + " size=" + c.size() + " contains(2)=" + c.contains(2));
    }

    public static void main(String[] args) {
        describe(new ArrayList<>(List.of(1, 2, 3)));    // ArrayList [1, 2, 3, 3] size=4 contains(2)=true
        describe(new HashSet<>(List.of(1, 2, 3)));      // HashSet [1, 2, 3] size=3 contains(2)=true   ← no duplicate
        describe(new ArrayDeque<>(List.of(1, 2, 3)));   // ArrayDeque [1, 2, 3, 3] size=4 contains(2)=true
    }
}
```
- The **same calls** behave correctly for each structure. A list accepts the duplicate `3`, but a set refuses it. Only the **cost** differs (for example, `contains` is O(n) on a list and O(1) on a hash set).

## The methods at a glance
| Group | Methods |
| :--- | :--- |
| **Add / remove** | `add(e)`, `remove(o)` |
| **Query** | `size()`, `isEmpty()`, `contains(o)` |
| **Traverse** | **`iterator()`** |
| **Bulk** | `addAll(c)`, `removeAll(c)`, `retainAll(c)`, `containsAll(c)`, `clear()` |
| **Convert** | `toArray()`, `toArray(T[])` |
| **Default (Java 8+)** | `removeIf(predicate)`, `stream()`, `parallelStream()`, `spliterator()` |
| **From `Object`** | `equals()`, `hashCode()`, `toString()` |

---

## `iterator()`: the most important one
- `Collection` extends `Iterable`, so **every collection has `iterator()`**. That's how you loop over any collection, and it's what the **for-each loop** uses under the hood (#30).
```java
Iterator<Integer> it = c.iterator();
while (it.hasNext()) System.out.println(it.next());

for (int x : c) System.out.println(x);     // the same thing
```

## `add` and `remove`
- **`boolean add(E e)`** returns **`true`** if the collection **changed**.
  - On a `HashSet`, adding something that's already there returns **`false`** (no duplicates).
- **`boolean remove(Object o)`** returns `true` if it found and removed the element, and **`false`** if it wasn't there.
  - On a list with duplicates, it removes only the **first** match: `[1, 2, 3, 3].remove(3)` gives `[1, 2, 3]`.
- `remove` and `contains` take **`Object`** (not `E`), because they search using **`equals()`**, which every object has.

## Query methods (brief)
- **`size()`** is the number of elements. **`isEmpty()`** is better than `size() == 0` (it can be cheaper for some structures).
- **`contains(o)`** uses `equals()`. It's **O(n)** for lists and **O(1)** on average for `HashSet`.

## Bulk operations (brief)
```java
import java.util.*;

public class BulkOps {
    public static void main(String[] args) {
        Collection<Integer> c = new ArrayList<>(List.of(1, 2, 3));

        c.addAll(List.of(4, 5));                          // add every element of another collection
        System.out.println(c);                            // [1, 2, 3, 4, 5]
        System.out.println(c.containsAll(List.of(1, 2))); // true

        c.removeAll(List.of(4, 5));                       // remove everything that's in the argument
        System.out.println(c);                            // [1, 2, 3]

        c.retainAll(List.of(1, 2, 9));                    // keep ONLY what's in the argument (intersection)
        System.out.println(c);                            // [1, 2]

        c.removeIf(x -> x % 2 == 0);                      // remove what matches a condition (default method)
        System.out.println(c);                            // [1]

        c.clear();                                        // empty it
        System.out.println(c + " " + c.isEmpty());        // [] true
    }
}
```
- **`System.out.println(c)`** prints the contents directly, because collections override `toString()`.
- `addAll(Collection<? extends E>)` also accepts a collection of **subtypes** (for example, adding `Dog`s to a `Collection<Animal>`).

## `toArray` (brief)
- `Object[] toArray()` gives back an **`Object[]`**, so you'd need casts.
- `Integer[] arr = c.toArray(new Integer[0]);` gives an array of the **right type**. The empty array just tells it which type to use. Since Java 11, you can also write `c.toArray(Integer[]::new)`.

## `equals` and `hashCode` (brief)
- `List` and `Set` compare **contents**, not references. Two lists are equal if they have the same elements **in the same order**. Two sets are equal if they have the same elements.
- **If you override `equals` in your own class, override `hashCode` too.** Otherwise `contains` and `remove` won't work properly in hash-based collections.

## Default methods (brief)
- `removeIf`, `stream`, `parallelStream` and `spliterator` are **`default`** methods (Java 8+), so older implementations didn't have to add them.

**Remember:** `Collection` is the **common contract for every container**: `add`, `remove`, `contains`, `size`, `isEmpty`, `clear`, the bulk `*All` methods, and **`iterator()`** (which is what makes for-each work). Write your code against `Collection` or `List` / `Set`, and the concrete class can change without breaking anything. Don't confuse it with **`Collections`**, the utility class.
