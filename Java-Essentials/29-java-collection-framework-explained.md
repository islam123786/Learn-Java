# 29 · Java Collection Framework Explained
> Full notes: [29-java-collection-framework-explained.md](../29-java-collection-framework-explained.md) · [▶ Video](https://youtu.be/mW2OO6d6_BE)

## Step 1: Why data structures exist
- Code is all about **data**. We store it so we can **query it efficiently**.
- **Every data structure is a trade-off.** Some operations get fast while others get slow. Pick the one that makes your **most frequent operation** fast.

## Step 2: Unsorted array
Example: `[3, 7, 10, 1, 4, 12]`

| Operation | Time | Why |
| :--- | :--- | :--- |
| **Append** an item | **O(1)** | just put it at the end |
| **Find the largest** | **O(n)** | you have to scan every element |

- ✅ **Pros:** appending is fast, it's simple, and reading by index is O(1).
- ❌ **Cons:** finding the largest or searching is O(n), and the **size is fixed**.

## Step 3: Sorted array
Example: `[1, 3, 4, 7, 10, 12]`

| Operation | Time | Why |
| :--- | :--- | :--- |
| **Find the largest** | **O(1)** | it's always `arr[arr.length - 1]` |
| **Insert** (e.g. 6) | **O(n)** | find its place, then **shift** every larger element right |

- **How it helps:** queries like largest/smallest (and binary search) become fast.
- **The cost:** inserting gets slow. That's the trade-off.

## Step 4: Dynamic array → `ArrayList`
**The problem with plain arrays:** the size is **fixed**. `new int[10]` has indexes 0–9, and `arr[10]` throws `ArrayIndexOutOfBoundsException`. Allocating `new int[1000]` "just in case" **wastes memory**.

**The idea:** wrap a **private** internal array in a class and expose methods:
- **`add(el)`:** put it in the next free slot. If the array is **full**, create a new array **twice the size**, copy everything over, then add.
- **`get(index)`:** return the element at that index. The array itself stays `private` (encapsulation).
- **`insert(index, el)`:** shift the elements from `index` onward one step right, then place it.
- **`remove`, `set`, …** work the same way.

```
capacity 4: [3, 7, 1, 4]   full!   add(9)  →  capacity 8: [3, 7, 1, 4, 9, _, _, _]
```
- In Java, this is **`ArrayList`**.

## Step 5: Linked list → `LinkedList`
**The problem:** inserting into an array shifts O(n) elements, and arrays need **contiguous memory**.

**The idea:** store each element in its own **node**, which holds the `data` plus a **reference to the next node**.
```
head → [4 | ●] → [8 | ●] → [5 | null]        inserting 8 between 4 and 5 just changes two links
```

| ✅ Advantages | ❌ Disadvantages |
| :--- | :--- |
| **never gets full**: just make a new node and link it, with no doubling or copying | **no random access**: `get(i)` has to walk from the head, which is **O(n)** |
| **insert or delete in the middle** only re-links nodes, so **nothing shifts** | **extra memory** for each node's reference |
| doesn't need contiguous memory | worse **cache** performance (the nodes are spread out) |

- **How it's built:** `Node` is a **`private static` nested class**, there's a `head` reference, and the class exposes `add`, `insert` and `remove`.
- In Java, this is **`LinkedList`**.

## Step 6: Set → `HashSet`, `LinkedHashSet`
**Requirements:** (1) **no duplicates**, and (2) **`contains(x)` in O(1)**. A plain array makes both O(n).

**How it's built, step by step:**
1. **Direct indexing** (for values 0–100): use an array of size 101, store `arr[el] = el`, and check `arr[el] == el`. That's O(1), and duplicates simply overwrite themselves. But a huge range would need a huge, mostly empty array.
2. **Modulo:** use a small array and store each element at **`el % capacity`** (e.g. 12 → slot 2). Still O(1).
3. **Collisions → chaining:** `10` and `100` both map to slot 0. They aren't duplicates, so each slot becomes a **linked list**: `10 → 100`. If the chains get long, **double the array** and spread everything out again (**rehashing**).
4. **Non-numbers:** call **`hashCode()`** to turn the object (e.g. an email string) into an `int`, then take it `% capacity`.

| Class | Implementation | Order |
| :--- | :--- | :--- |
| **`HashSet`** | a hash table (array + chains) | **no** order |
| **`LinkedHashSet`** | a hash table **plus a linked list** through the entries | keeps **insertion order** |

## Step 7: Stack and Queue
| | **Stack (LIFO)** | **Queue (FIFO)** |
| :--- | :--- | :--- |
| Rule | **last in, first out** | **first in, first out** |
| Picture | a stack of plates | a line of people |
| In 2, 3, 4, 5 → out | 5, 4, 3, 2 | 2, 3, 4, 5 |
| Operations | `push` / `pop` / `peek`, at the **same end** | `offer` at the rear, `poll` at the **front** |
| Built on an array | a **top counter**, so push and pop are O(1) | two ends (→ a **circular queue**) |

- **What it provides:** **restricted access**, with no inserting in the middle and no replacing. That makes the behavior predictable and the operations O(1).

**Which classes provide them:**

| Structure | Java classes |
| :--- | :--- |
| Stack | **`Stack`** (legacy, extends `Vector`), **`ArrayDeque`** ✅ preferred (`push`/`pop`), `LinkedList` |
| Queue | **`ArrayDeque`** ✅, **`LinkedList`**, **`PriorityQueue`** (removes the **smallest** first, not FIFO) |

## Step 8: Map → `HashMap`, `LinkedHashMap`
- A **key → value** store, e.g. `"Aditya" → 1001`, `"Rohit" → 102`.
- **Requirements:** **`get(key)` in O(1)**, and **no duplicate keys** (putting `"Aditya" → 105` **replaces** the old value).
- **How it's built:** the **same hashing as a set**, but each slot stores a **key-value node**, placed at `hash(key) % capacity`. Equal keys give equal hash codes, so `get` lands on the same slot.

| Class | Implementation | Order |
| :--- | :--- | :--- |
| **`HashMap`** | a hash table of key-value nodes | **no** order |
| **`LinkedHashMap`** | a hash table **plus a linked list** (it extends `HashMap`) | keeps **insertion order** |

## Step 9: Trees → `TreeSet`, `TreeMap`
- Trees hold **hierarchical** data, like folders containing files and sub-folders.
- **BST (binary search tree) rule:** **smaller values go left and larger values go right**, at every level.
```
        10
       /  \
      5    16        ← everything under 16 is still > 10
          /
        12
```
- Each node has `data`, `left` and `right`. Search, insert and delete are **O(log n)** when the tree is balanced.
- **What it provides:** elements are kept **sorted**.

| Class | Implementation |
| :--- | :--- |
| **`TreeSet`** | a **self-balancing BST (red-black tree)**. Unique elements, **sorted**. |
| **`TreeMap`** | a red-black tree ordered **by key**. Keys are **sorted**. |

---

## Step 10: Why a hierarchy?
- If every class were independent, each would have **its own method names**, and there'd be far more to memorize.
- They're all **containers**. Only the internal storage differs. So Java uses **interfaces + polymorphism**:
  - `ArrayList` and `LinkedList` both implement **`List`** (`add`, `get`, `remove`, `set`), and each implements `add` **its own way**.
  - Your code depends on the **interface**, not the class:
```java
List<Integer> list = new ArrayList<>();   // swap in new LinkedList<>() and nothing else changes
list.add(4);
```
- Methods that **every** container has (`add`, `remove`, `size`, `contains`) are declared **once**, in **`Collection`**.

---

## ⭐ The complete hierarchy (the main takeaway)

```
Iterable (I)
└── Collection (I)
    ├── List (I)
    │   ├── ArrayList            (class)
    │   ├── LinkedList           (class)   ← also implements Deque
    │   └── Vector               (class, legacy)
    │       └── Stack            (class, legacy)   extends Vector
    │
    ├── Queue (I)
    │   ├── PriorityQueue        (class)
    │   └── Deque (I)                      extends Queue
    │       ├── ArrayDeque       (class)
    │       └── LinkedList       (class)   ← the same LinkedList as above
    │
    └── Set (I)
        ├── HashSet              (class)
        │   └── LinkedHashSet    (class)   extends HashSet
        └── SortedSet (I)                  extends Set
            └── NavigableSet (I)           extends SortedSet
                └── TreeSet      (class)

Map (I)                                    ← a SEPARATE hierarchy, NOT under Collection
├── HashMap                      (class)
│   └── LinkedHashMap            (class)   extends HashMap
├── Hashtable                    (class, legacy)
└── SortedMap (I)                          extends Map
    └── NavigableMap (I)                   extends SortedMap
        └── TreeMap              (class)

(I) = interface.  interface → interface: extends.  class → interface: implements.  class → class: extends.
```

### Who extends or implements what
| Concrete class | Extends (class) | Implements (interface) | Underlying structure |
| :--- | :--- | :--- | :--- |
| **`ArrayList`** | — | `List` | dynamic array |
| **`LinkedList`** | — | **`List` and `Deque`** (so it's also a `Queue`) | doubly linked list |
| **`Vector`** | — | `List` | dynamic array (synchronized, legacy) |
| **`Stack`** | **`Vector`** | (via `Vector`) `List` | dynamic array (legacy) |
| **`ArrayDeque`** | — | `Deque` → `Queue` | resizable circular array |
| **`PriorityQueue`** | — | `Queue` | binary heap |
| **`HashSet`** | — | `Set` | hash table |
| **`LinkedHashSet`** | **`HashSet`** | `Set` | hash table + linked list |
| **`TreeSet`** | — | `NavigableSet` → `SortedSet` → `Set` | red-black tree |
| **`HashMap`** | — | `Map` | hash table |
| **`LinkedHashMap`** | **`HashMap`** | `Map` | hash table + linked list |
| **`TreeMap`** | — | `NavigableMap` → `SortedMap` → `Map` | red-black tree |
| **`Hashtable`** | — | `Map` | hash table (synchronized, legacy) |

**Key points to remember**
- **Interfaces extend interfaces** (`List extends Collection`). **Classes implement interfaces** (`ArrayList implements List`). **Classes extend classes** (`Stack extends Vector`, `LinkedHashSet extends HashSet`, `LinkedHashMap extends HashMap`).
- **`LinkedList` is both a `List` and a `Deque`/`Queue`.**
- **`Map` is NOT a `Collection`.** It stores **key-value pairs**, while `Collection` stores **single values**. They're still studied together.
- `Stack`, `Vector` and `Hashtable` are **legacy**. Prefer `ArrayDeque` and `HashMap`.
- In the JDK, the concrete classes actually extend helper classes like `AbstractList`, `AbstractSet` and `AbstractMap`. These are left out above for clarity.
- **Java 21** added `SequencedCollection` / `SequencedSet` / `SequencedMap` between some of these levels (with methods like `getFirst()` and `reversed()`). The structure above stays the same.

## Summary: which one to pick
| You need | Use | Key cost |
| :--- | :--- | :--- |
| fast access by index | `ArrayList` | inserting in the middle is O(n) |
| lots of inserting/removing at the ends or in the middle (with an iterator) | `LinkedList` | `get(i)` is O(n) |
| unique items, fast `contains` | `HashSet` (`LinkedHashSet` to keep insertion order) | no sorting |
| unique items, **sorted** | `TreeSet` | O(log n) |
| stack / queue | `ArrayDeque` | — |
| smallest (or highest-priority) first | `PriorityQueue` | O(log n) add/remove |
| key → value | `HashMap` (`LinkedHashMap` to keep order, `TreeMap` for sorted keys) | — |

**Remember:** each data structure trades one operation's speed for another's. Arrays are good for appending and reading by index. Sorted arrays are good for finding the largest or smallest. Linked lists are good for inserting. Hash tables give O(1) `contains`/`get`, and trees keep everything sorted. **`List`, `Queue` and `Set` extend `Collection`, but `Map` is separate.**
