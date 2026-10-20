# Set & Map Interface in Java | Internal Working of HashMap, HashSet & TreeMap | Java Full Course #33
> Source: https://youtu.be/kzZ1TlGTWmY · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 97:27. The video is in Hindi (these notes are in English). The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within a minute or two.

## TL;DR
- A **`Set`** promises two things: no duplicates, and an (average) **O(1)** `contains`. A **`Map`** promises the same for **keys** of key-value pairs: unique keys and O(1) `get`/`containsKey`. The price is that you lose **positional (index) access** and, for the hash versions, insertion order.
- Internally a hash table is an **array of buckets**. Index = `hashCode()` reduced to the range `0..n-1` (the video uses `% n`). Collisions are handled by **chaining**: each bucket holds a linked list of nodes. A node stores `hash`, `key`, `value`, `next`.
- Java has no separate "set engine": **`HashSet` wraps a `HashMap`**. Every element becomes a key, and the value is one shared dummy object called `PRESENT`. `set.add(x)` is really `map.put(x, PRESENT) == null`.
- When looking for a key in a bucket, Java first compares the cached **hash** (cheap `int` check) and only then calls **`equals()`**. Equal hashes do not mean equal objects.
- The table grows (**rehashing**, double the capacity) when size passes **load factor 0.75 × capacity** (default capacity 16, so after 12 entries). Since Java 8, a very long bucket is converted to a **red-black tree** (treeification). **`LinkedHashSet`/`LinkedHashMap`** add a doubly linked list to remember insertion order. **`TreeSet`/`TreeMap`** don't hash at all: they are red-black trees with O(log n) operations, sorted order and range queries.

## Key Concepts

**Set contract.** No duplicates, and membership checks in constant time on average. *Why it matters:* "unique visitors" or "list of emails" problems become one-liners and stay fast at any size.

**Map contract.** Stores key-value pairs. Keys are unique, values may repeat (two students can both be named Aditya, but roll numbers are unique). `put`, `get`, `containsKey` are O(1) on average. *Why it matters:* it's the standard "look this up by id" structure.

**Constant time is an average.** Hash operations are O(1) *on average*. With many collisions a bucket can become long and an operation can degrade (to O(n) with chains, O(log n) after treeification).

**Array of buckets + hashing.** A plain "value = index" array would need to be as big as the largest key. Instead, a fixed-size array is used and the key's `hashCode()` is reduced to a bucket index. Any object can be hashed because `hashCode()` is defined on `Object`. *Why it matters:* this is what turns lookups into one array access plus a short scan.

**Collision and chaining.** Different keys can land in the same bucket (e.g. 4 and 8 with `% 4`). Each bucket keeps a linked list of nodes. Linked list, not array, because removing from the middle of a list is just re-linking, with no shifting.

**`HashSet` is a `HashMap`.** `HashSet` holds a private `HashMap<E, Object>` and a `private static final Object PRESENT = new Object()`. All set operations forward to the map.

**Node structure.** In `HashMap`, a static nested class `Node<K,V>` holds `hash`, `key`, `value`, and `next` (the next node in the same bucket). The hash is cached so it isn't recomputed on every comparison.

**`equals()`/`hashCode()` contract.** If `a.equals(b)` then `a.hashCode() == b.hashCode()`. The reverse is not guaranteed. *Why it matters:* a hash match only narrows the search; `equals()` makes the final decision.

**Load factor and rehashing.** Load factor = number of elements ÷ capacity. When it goes past 0.75 the table doubles and every entry is redistributed. *Why it matters:* it keeps chains short so operations stay O(1).

**Treeification (Java 8+).** If one bucket's chain grows past a threshold (8), it is turned into a red-black tree, so searching that bucket becomes O(log n) instead of O(n).

**Linked hash structures.** `LinkedHashSet`/`LinkedHashMap` keep a second, doubly linked list through all entries (`before`/`after` pointers) in insertion order. Iteration follows that list.

**Tree structures.** `TreeSet`/`TreeMap` store entries in a self-balancing BST (red-black tree), ordered by `compareTo` (or a `Comparator`). Operations are O(log n), but you get sorted iteration, min/max, floor/ceiling and range queries.

## Detailed Notes

### What a Set promises [00:00]
- Two guarantees: **duplicates are not allowed**, and **search is constant time** (`set.contains(x)` costs the same with 1 element or a billion).
- Compare with a list: an `ArrayList` stores elements at indexes 0, 1, 2, … and has no idea where 7 might be, so `contains(7)` asks every element in turn. That's **O(n)**.
- Real uses: counting **unique visitors** to a website (Aditya visits twice, count him once), storing **email IDs** (unique by nature). Use a set wherever you don't want to deal with duplicates.

### What a Map promises [04:56]
- A map stores a **key-value pair** instead of a single element. Keys must be unique; values can repeat.
- Example: roll number → name. `1001 → Aditya`, `102 → Rohit`, `103 → Aditya`. Two values are "Aditya", which is fine; the keys differ.
- Operations: `map.put(k, v)`, `map.containsKey(k)` (yes/no in O(1)), `map.get(k)` (the value in O(1)).
- O(1) is the **average** case. Sometimes it can take longer; that is covered later in the video.

### The trade-off: no positional access [08:22]
- Nothing in computer science is free. Sets and maps don't store elements at "the next index", otherwise they'd need O(n) searches too. Elements go to a position decided by some rule.
- Result: when you print a set, the order can differ from the order you inserted. There is **no index-based access** (`set.get(2)` doesn't exist).

### First demo [09:47]
- Three kinds of set: `HashSet`, `LinkedHashSet`, `TreeSet`. Three kinds of map: `HashMap`, `LinkedHashMap`, `TreeMap`.
- Demo: `Set<String> set = new HashSet<>()`, add "Aditya", "Rohit", "Rohan". `contains("Aditya")` → `true`, `contains("Abhay")` → `false`.
- `Map<Integer, String> map = new HashMap<>()`, `put(1001, "Aditya")`, `put(102, "Rohit")`, `put(103, "Aditya")`. `containsKey(1001)` → `true`, `get(102)` → `"Rohit"`.

### Building a set: from direct indexing to modulo [13:30]
- Internally: an array whose cells are called **buckets**.
- Idea 1: put value 5 at index 5, 3 at index 3, and so on. Lookups are trivial, but the array would have to be as large as the largest value (10,000? a billion?). Wasteful.
- Idea 2 [15:26]: array of size `n`; store `x` at `x % n`. With `n = 4`: 5 → 1, 6 → 2, 3 → 3, 4 → 0. Any number, however large, maps into 0..n-1. `contains(6)`: compute `6 % 4 = 2`, look in bucket 2.

### Collisions and chaining [17:09]
- `8 % 4 == 0` collides with 4. You can't overwrite 4; both deserve to be stored.
- Fix: make each bucket a **linked list**. Bucket 0 holds 4 → 8.
- Why a linked list and not an array per bucket? Removing from an array means shifting everything after it. A linked list just re-links one node.

### Hashing non-numbers [19:43]
- To store "Aditya" you need a number first. `Object.hashCode()` gives every object an `int`, and since `Object` is the parent of every class, you can call it on anything.
- Behind `set.add(value)`: `int h = value.hashCode(); int index = h % n;` then append to that bucket's list (new node if empty, else walk to the end and add).
- Example from the board: Aditya → bucket 0, Rohit → 1, Abhay → 4, Rohan → 0 again (chained after Aditya).

### Building a map: `Node<K, V>` [22:31]
- A map stores pairs, and no primitive type holds a pair, so you write a class: `class Node<K, V> { K key; V value; }`.
- `put(key, value)`: hash **only the key** (the key is the unique part), reduce to an index, and store the whole node in that bucket.
- `containsKey(key)` / `get(key)`: hash the key again. Equal keys give the same hash, so you land in the same bucket; find the node and return `true` or its `value`.
- Collisions are again handled with a chain of nodes (e.g. 1001 and 103 both in bucket 0).

### Surprise: Java has no separate Set implementation [26:48]
- A set promises "no duplicates + O(1) search". A map already gives exactly that for its keys. So why write the bucket logic twice?
- **`HashSet` internally creates a `HashMap`.** The element you add becomes the **key**. The value is a dummy object.
- `set.add(2)` becomes `map.put(2, PRESENT)`. `set.contains(2)` becomes `map.containsKey(2)`. `set.remove(2)` becomes `map.remove(2)`.

### The `PRESENT` dummy object [31:31]
- In `HashSet`: `private static final Object PRESENT = new Object();` (all caps because it's a constant).
- Every key in the backing map points to this same single object. It just fills the value slot; the set never reads it.
- *Correction:* the video draws `PRESENT` as a reference on the stack. It's a `static` field, so it belongs to the class (its storage is not on any thread's stack); the object itself is on the heap. One object shared by every `HashSet`.

### Java's real `Node`: `hash`, `key`, `value`, `next` [34:18]
- Java's flow is the same as ours: compute hash → find bucket → store (directly if the bucket is empty, else at the end of the chain).
- `HashMap` has a `static class Node<K,V>` with four fields:
  - `key`, `value`
  - `hash`: cached so comparisons don't call `hashCode()` again and again (an optimization)
  - `next`: the link to the next node in the **same bucket's** chain.

### `HashSet.add` → `HashMap.put` [37:05]
- `add` returns `boolean`: `true` if the element was added, `false` if it was a duplicate.
- Implementation idea: `return map.put(e, PRESENT) == null;`
- `put` returns the **previous value** for the key, or `null` if the key was new. New key → `null` → `add` returns `true`. Existing key → returns `PRESENT` → `add` returns `false`.

### Inside `put`: the cases [42:01]
- Compute `hash(key)`, then the bucket index.
- **Case 1, empty bucket:** `bucket[index] = new Node(key, value, hash, null)`.
- **Case 2, non-empty bucket** [43:06]. Two different reasons can put two keys in the same bucket:
  - They are different keys whose hashes reduce to the same index (a real collision).
  - They are the **same** key added again (equal objects have equal hash codes).
- So `put` walks the chain (`temp = head; while (temp != null) …`) checking for an existing key.
- **Duplicate key found** [46:57]: the old node's value is replaced with the new value, the key stays, no new node is created, and `put` returns the **old value**. For a set this replaces `PRESENT` with `PRESENT`.
- **Not found:** append a new node at the end of the chain (the last node's `next` now points to it) and return `null`.

### `contains` flow [49:32]
- `set.contains(k)` → `map.containsKey(k)` → hash, find bucket, walk the chain comparing keys with `equals`. Found → `true`; reached the end → `false`.
- *Correction:* on the board the video briefly says "reached the end → return true"; it's the other way round.

### Why compare both hash and `equals()` [52:06]
- The correct check is `node.hash == hash && node.key.equals(key)`. The video first wrote `||` and `==` on the board and then fixed it to `&&` and `.equals()` (`==` compares references, not content).
- Reasoning:
  - `hashCode()` already picked the bucket in O(1). Now the candidates in the chain must be compared.
  - **The contract:** `k1.equals(k2)` ⇒ same `hashCode()`. The reverse doesn't hold: "Aditya" and "Rohit" could both hash to 2023.
  - So a hash match only means "maybe equal". `equals()` confirms it.
  - Why not just `equals()`? Comparing two `int`s is very cheap; `equals()` can be slow (e.g. long strings). If the hashes differ you skip `equals()` entirely.
- *Note:* the real JDK check is `p.hash == hash && ((k = p.key) == key || (key != null && key.equals(k)))`, which also short-circuits when it's literally the same object.

### `remove` and `get` [60:15]
- `remove`: find the bucket, walk the chain, unlink the matching node; if none matches, report "not found" (for a set, `false`).
- `get(key)` exists only on `Map` (a set has no values): find the bucket, walk the chain, return the value or `null`.

### Chaining vs open addressing [61:45]
- What Java uses is **chaining** (a chain of linked nodes per bucket).
- **Open addressing** (not used by `HashMap`, but asked in interviews): if the bucket is taken, put the entry somewhere else in the array. The simplest form is **linear probing**: try the next index, then the next.
  - Aditya → 1. Rohit also → 1, taken, stored at 2. Rohan → 1, 2 taken, stored at 3. Then Aman, whose home is 2, is pushed to 4.
  - Lookups must follow the same probe sequence, and entries drift away from their home slots. It gets complicated.
- *Note:* Java does use open addressing in a few places, e.g. `IdentityHashMap` and the immutable sets/maps from `Set.of`/`Map.of`. `HashMap`/`HashSet` use chaining.

### Load factor [64:32]
- **Load factor = number of elements ÷ capacity** (capacity = number of buckets). 6 elements in 5 buckets = 1.2; 4 in 5 = 0.8.
- The threshold is **0.75**: once the table is more than 75% "full", it is considered too crowded.

### Rehashing [66:28]
- Step 1: create an array **twice** as large (5 → 10).
- Step 2: **rehash** every existing key into the new array (`% 10` instead of `% 5`). Many keys move: 5 was in bucket 0 with `% 5` but goes to bucket 5 with `% 10`.
- Result: values spread out, chains get shorter, O(1) is preserved. Without it, chains would grow without limit and operations become O(n).
- **Defaults** [68:49]: initial capacity **16**, load factor **0.75**, so the threshold is 12 entries. *Precise version:* `HashMap` resizes when the size exceeds the threshold, i.e. when the 13th entry goes in.
- *Note:* the JDK doesn't literally use `%`. Capacity is always a power of two, and the index is `(n - 1) & hash`, after mixing the high bits into the low ones (`h ^ (h >>> 16)`). Same idea, faster, and never negative.

### Treeification (Java 8+) [69:54]
- If a single bucket's chain grows past **8** nodes, Java converts that chain into a **red-black tree** (a self-balancing BST). Search in that bucket goes from O(n) to **O(log n)**. When it shrinks again, it converts back to a list.
- Why not always use trees? Tree nodes are bigger and more complex (two child pointers and more), so for short chains a list is cheaper.
- *Precise version:* treeify happens when a bin reaches 8 entries **and** the table has at least 64 buckets (`MIN_TREEIFY_CAPACITY`). With a smaller table, `HashMap` resizes instead. A tree bin turns back into a list when it drops to 6 or fewer during a resize/split.

### `LinkedHashSet` / `LinkedHashMap` [71:37]
- Problem: a `HashSet`/`HashMap` iterator walks bucket by bucket, so insertion order (Aditya, Rohit, Rohan) can come back as Aditya, Rohan, Rohit.
- Fix: keep a **doubly linked list** through all entries in insertion order, in addition to the buckets. Aditya → Rohit → Rohan → Abhay, regardless of which buckets they live in. Iteration follows this list from its head.
- Two linked lists at work: the per-bucket chain (`next`), and the insertion-order list.
- The entry class extends the normal node with two extra pointers, **`before`** and **`after`**.
- `LinkedHashSet` **extends** `HashSet` and `LinkedHashMap` **extends** `HashMap`. `LinkedHashSet` is backed by a `LinkedHashMap`.
- Extra memory and work per entry, so use it only when you actually need insertion order.

### `TreeSet` / `TreeMap` [78:03]
- `TreeSet` is backed by a `TreeMap` (same pattern as `HashSet`/`HashMap`).
- No hashing and no bucket array. Entries live in a **self-balancing BST (red-black tree)**.
- Same operations (`add`, `contains`, `remove` / `put`, `containsKey`, `get`) but **O(log n)** instead of O(1).
- Why accept slower? It unlocks things a hash structure can't do: **sorted iteration**, **smallest/largest**, **range queries** (all numbers between 20 and 75), floor/ceiling. These methods are covered in the next video.
- For strings, "order" means **lexicographic (dictionary) order**, via `compareTo`.

### BST insertion and search [82:46]
- Rule: **smaller goes left, larger goes right**, at every node.
- Insert 50, 70, 30, 20, 40, 60, 80: 50 is the root; 70 right of 50; 30 left; 20 left of 30; 40 right of 30; 60 left of 70; 80 right of 70.
- "Left" and "right" are conceptual; nodes are heap objects connected by references.
- `contains(70)`: compare with 50 (bigger → go right), find 70 → `true`. `contains(90)`: 50 → 70 → 80 → right is `null` → `false`.

### Why self-balancing [88:07]
- Insert 50, 60, 70, 80, 90 into a plain BST and every node goes right: the tree degenerates into a linked list, so search is O(n).
- In a balanced tree each comparison discards about half the remaining nodes, which is where **O(log n)** comes from.
- A red-black tree rebalances as you insert: after 50, 60, 70 it rotates so that 60 is on top with 50 left and 70 right. The BST rule still holds.
- AVL trees are another self-balancing BST; Java's `TreeMap` uses red-black.

### `TreeMap` node structure [92:12]
- `Entry<K,V>` holds `key`, `value`, `left`, `right`, **`parent`**, and a **`boolean color`** (red/black). There is no `hash` field because hashing isn't used.
- The parent pointer (rare in textbook DSA code) makes moving up and down the tree easy, which helps rebalancing and iteration.

### `null` keys and values [94:07]
- `HashMap`, `LinkedHashMap`, `HashSet`, `LinkedHashSet` allow a `null` key **once** (keys are unique) and `null` values any number of times.
- A `null` key has no `hashCode()`, so `HashMap` treats its hash as 0 and stores it in bucket 0.
- `TreeMap`/`TreeSet` compare keys with `compareTo`. Comparing `null` throws a `NullPointerException`, so `null` keys aren't allowed. *Precise version:* with natural ordering, yes. A custom `Comparator` that handles `null` (e.g. `Comparator.nullsFirst(...)`) can allow it. `null` **values** are fine in a `TreeMap`.

### The hierarchy [95:37]
```
Collection
└── Set (interface)
    ├── HashSet ──► LinkedHashSet        (backed by HashMap / LinkedHashMap)
    └── TreeSet  (via SortedSet → NavigableSet; backed by TreeMap)

Map (separate hierarchy)
├── HashMap ──► LinkedHashMap
└── TreeMap  (via SortedMap → NavigableMap)
```
- `Map` stays outside `Collection` because it deals with pairs.
- *Correction:* near the end the video says "LinkedHashMap internally uses LinkedHashSet". It's the reverse: **`LinkedHashSet` uses a `LinkedHashMap`** internally.
- Next video: the methods of `Set` and `Map` in detail.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Set and Map basics (the video's first demo)
```java
import java.util.*;

public class SetMapBasics {
    public static void main(String[] args) {
        Set<String> set = new HashSet<>();
        set.add("Aditya");
        set.add("Rohit");
        set.add("Rohan");
        System.out.println(set.contains("Aditya")); // true
        System.out.println(set.contains("Abhay"));  // false
        System.out.println(set.add("Aditya"));      // false (duplicate)
        System.out.println(set.size());             // 3

        Map<Integer, String> map = new HashMap<>();
        map.put(1001, "Aditya");
        map.put(102, "Rohit");
        map.put(103, "Aditya");                     // repeated value is fine
        System.out.println(map.containsKey(1001));  // true
        System.out.println(map.get(102));           // Rohit
        System.out.println(map.get(999));           // null
    }
}
```

### A set built on a map with a `PRESENT` dummy (how `HashSet` works)
```java
import java.util.HashMap;

public class SetOnTopOfMap<E> {
    private static final Object PRESENT = new Object(); // one shared dummy value
    private final HashMap<E, Object> map = new HashMap<>();

    public boolean add(E e)      { return map.put(e, PRESENT) == null; } // null = key was new
    public boolean contains(E e) { return map.containsKey(e); }
    public boolean remove(E e)   { return map.remove(e) == PRESENT; }
    public int size()            { return map.size(); }

    public static void main(String[] args) {
        SetOnTopOfMap<String> s = new SetOnTopOfMap<>();
        System.out.println(s.add("Aditya"));      // true
        System.out.println(s.add("Aditya"));      // false
        System.out.println(s.contains("Aditya")); // true
        System.out.println(s.remove("Rohit"));    // false
        System.out.println(s.size());             // 1
    }
}
```

### A chained hash map with `hash`/`key`/`value`/`next`, load factor and rehashing
```java
public class ChainedHashMap<K, V> {
    private static class Node<K, V> {
        final int hash;
        final K key;
        V value;
        Node<K, V> next; // next node in the same bucket

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash; this.key = key; this.value = value; this.next = next;
        }
    }

    private Node<K, V>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public ChainedHashMap(int capacity) { buckets = (Node<K, V>[]) new Node[capacity]; }

    private static int hash(Object key) { return key == null ? 0 : key.hashCode(); } // null key → bucket 0
    private int indexFor(int h, int n)  { return Math.floorMod(h, n); }

    /** Returns the previous value, or null if the key was new (same contract as HashMap.put). */
    public V put(K key, V value) {
        int h = hash(key);
        int i = indexFor(h, buckets.length);
        Node<K, V> last = null;
        for (Node<K, V> n = buckets[i]; n != null; n = n.next) {
            if (n.hash == h && java.util.Objects.equals(n.key, key)) { // cheap int check first
                V old = n.value;
                n.value = value; // duplicate key → update in place
                return old;
            }
            last = n;
        }
        Node<K, V> node = new Node<>(h, key, value, null);
        if (last == null) buckets[i] = node; else last.next = node; // append at the end of the chain
        if (++size > buckets.length * 0.75) resize();
        return null;
    }

    public V get(Object key) {
        int h = hash(key);
        for (Node<K, V> n = buckets[indexFor(h, buckets.length)]; n != null; n = n.next) {
            if (n.hash == h && java.util.Objects.equals(n.key, key)) return n.value;
        }
        return null;
    }

    public boolean containsKey(Object key) {
        int h = hash(key);
        for (Node<K, V> n = buckets[indexFor(h, buckets.length)]; n != null; n = n.next) {
            if (n.hash == h && java.util.Objects.equals(n.key, key)) return true;
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        Node<K, V>[] old = buckets;
        buckets = (Node<K, V>[]) new Node[old.length * 2]; // twice as large
        for (Node<K, V> head : old) {
            for (Node<K, V> n = head; n != null; ) {
                Node<K, V> next = n.next;
                int i = indexFor(n.hash, buckets.length); // rehash into the new table
                n.next = buckets[i];
                buckets[i] = n;
                n = next;
            }
        }
    }

    public int capacity() { return buckets.length; }
    public int size()     { return size; }

    public static void main(String[] args) {
        ChainedHashMap<Integer, String> m = new ChainedHashMap<>(4);
        System.out.println(m.put(1001, "Aditya"));   // null
        System.out.println(m.put(1001, "Aadi"));     // Aditya (old value returned)
        m.put(102, "Rohit");
        m.put(103, "Rohan");
        System.out.println(m.capacity());            // 4  (3 entries, 3 > 3.0 is false)
        m.put(104, "Abhay");                         // 4 > 3.0 → resize
        System.out.println(m.capacity());            // 8
        System.out.println(m.get(1001));             // Aadi
        System.out.println(m.containsKey(103));      // true
        System.out.println(m.get(999));              // null
        m.put(null, "nobody");
        System.out.println(m.get(null));             // nobody
        System.out.println(m.size());                // 5
    }
}
```

### Same hash, different objects: why `equals()` is still needed
```java
import java.util.*;

public class HashVsEquals {
    static class Student {            // overrides equals() but NOT hashCode()
        final int roll;
        Student(int roll) { this.roll = roll; }
        @Override public boolean equals(Object o) {
            return o instanceof Student s && s.roll == roll;
        }
    }

    record GoodStudent(int roll) {}   // records generate both equals() and hashCode()

    public static void main(String[] args) {
        // "Aa" and "BB" are a classic String hash collision
        System.out.println("Aa".hashCode() + " " + "BB".hashCode()); // 2112 2112
        System.out.println("Aa".equals("BB"));                       // false

        Set<String> s = new HashSet<>(List.of("Aa", "BB"));
        System.out.println(s.size());                                // 2 (same bucket, both kept)

        Set<Student> bad = new HashSet<>();
        bad.add(new Student(1));
        bad.add(new Student(1));
        System.out.println(bad.size()); // 2 (almost always; different identity hash codes → different buckets)

        Set<GoodStudent> good = new HashSet<>();
        good.add(new GoodStudent(1));
        good.add(new GoodStudent(1));
        System.out.println(good.size());                             // 1
    }
}
```

### Iteration order: `HashSet` vs `LinkedHashSet` vs `TreeSet`
```java
import java.util.*;

public class SetOrderDemo {
    public static void main(String[] args) {
        List<Integer> input = List.of(30, 5, 17);

        // Small Integers hash to themselves; with 16 buckets: 30→14, 5→5, 17→1.
        // So this order is deterministic here, but it is NOT insertion order and not guaranteed in general.
        System.out.println(new HashSet<>(input));        // [17, 5, 30]
        System.out.println(new LinkedHashSet<>(input));  // [30, 5, 17]  insertion order
        System.out.println(new TreeSet<>(input));        // [5, 17, 30]  sorted

        Map<String, Integer> lhm = new LinkedHashMap<>();
        lhm.put("Aditya", 1); lhm.put("Rohit", 2); lhm.put("Rohan", 3); lhm.put("Abhay", 4);
        System.out.println(lhm.keySet());                // [Aditya, Rohit, Rohan, Abhay]
    }
}
```

### `TreeSet` extras: sorted order, min/max, ranges, strings
```java
import java.util.*;

public class TreeSetPowers {
    public static void main(String[] args) {
        TreeSet<Integer> t = new TreeSet<>(List.of(50, 70, 30, 20, 40, 60, 80));
        System.out.println(t);                   // [20, 30, 40, 50, 60, 70, 80]
        System.out.println(t.first() + " " + t.last()); // 20 80
        System.out.println(t.subSet(30, 75));    // [30, 40, 50, 60, 70]  (range query, 75 exclusive)
        System.out.println(t.floor(45));         // 40  (largest ≤ 45)
        System.out.println(t.ceiling(45));       // 50  (smallest ≥ 45)
        System.out.println(t.contains(90));      // false

        TreeSet<String> names = new TreeSet<>(List.of("Rohit", "Aditya", "Rohan", "Abhay"));
        System.out.println(names);               // [Abhay, Aditya, Rohan, Rohit]  (lexicographic)
    }
}
```

### `null` keys: hash maps allow one, tree maps don't
```java
import java.util.*;

public class NullKeyDemo {
    public static void main(String[] args) {
        Map<String, String> hm = new HashMap<>();
        hm.put(null, "first");
        hm.put(null, "second");       // same (null) key → value replaced
        hm.put("a", null);
        hm.put("b", null);            // null values may repeat
        System.out.println(hm.size());      // 3
        System.out.println(hm.get(null));   // second

        Map<String, String> tm = new TreeMap<>();
        tm.put("a", null);            // null VALUE is fine
        try {
            tm.put(null, "x");        // natural ordering calls compareTo on null
        } catch (NullPointerException e) {
            System.out.println("TreeMap rejects null key"); // TreeMap rejects null key
        }
        Map<String, String> tm2 = new TreeMap<>(Comparator.nullsFirst(Comparator.<String>naturalOrder()));
        tm2.put(null, "ok");          // a null-friendly Comparator allows it
        System.out.println(tm2);      // {null=ok}
    }
}
```

## Common Pitfalls / Tips
- **O(1) is average, not guaranteed.** Bad `hashCode()` implementations (e.g. returning a constant) put everything in one bucket.
- **Override `equals()` and `hashCode()` together** for objects you put in a `HashSet` or use as `HashMap` keys. Otherwise "equal" objects end up in different buckets and duplicates slip through. Records do this for you.
- **Don't mutate a key after inserting it.** If its hash changes, the map looks in the wrong bucket and the entry becomes unreachable.
- **`put` overwrites.** It returns the old value (or `null`). Use `putIfAbsent` if you want to keep the first value. A `null` return can also mean the old value was `null`; use `containsKey` when that matters.
- **Don't rely on `HashSet`/`HashMap` iteration order.** Use `LinkedHashSet`/`LinkedHashMap` for insertion order, `TreeSet`/`TreeMap` for sorted order.
- **Size your map if you know the count.** `new HashMap<>(n)` doesn't mean "n entries without resizing" because of the 0.75 factor. On Java 19+ use `HashMap.newHashMap(n)`.
- **Tree structures need comparable keys.** Elements of a `TreeSet` must implement `Comparable` or you must pass a `Comparator`, otherwise you get a `ClassCastException` on insert.
- **`TreeMap`/`TreeSet` reject `null` keys** with natural ordering. Hash-based ones allow a single `null` key.
- **`TreeSet` uses `compareTo`, not `equals`, to detect duplicates.** If the two disagree, the set can behave differently from a `HashSet`.

## Review Questions
1. What two things does a `Set` promise, and what do you give up compared with a `List`?
2. Why does a hash table use `hashCode() % n` (or similar) instead of storing value `x` at index `x`?
3. What is a collision, and how does chaining handle it? Why a linked list rather than an array per bucket?
4. How is `HashSet` implemented internally? What is `PRESENT`, and how does `add` decide whether to return `true` or `false`?
5. Why does `HashMap` compare the cached `hash` before calling `equals()`? Why isn't the hash comparison enough on its own?
6. What is the load factor, what is the default threshold, and what happens during rehashing?
7. What is treeification and why doesn't Java use trees in every bucket from the start?
8. How do `LinkedHashMap` and `TreeMap` differ from `HashMap` internally, and when would you choose each?

<details>
<summary>Answers</summary>

1. No duplicates, and average O(1) `contains`. You lose positional (index) access, and with `HashSet` you also lose insertion order.
2. A direct index would need an array as large as the biggest possible value (millions or billions of mostly empty slots). Reducing the hash to the range `0..n-1` lets a small fixed array hold any value.
3. A collision is two different keys mapping to the same bucket. Chaining keeps a linked list of nodes in each bucket so both can be stored. A linked list lets you remove a node by re-linking, whereas an array would need shifting.
4. `HashSet` wraps a private `HashMap<E, Object>`. Each element is stored as a key, with the shared constant `PRESENT` (`new Object()`) as the value. `add(e)` returns `map.put(e, PRESENT) == null`: `put` returns `null` for a new key (so `true`) and the old value for an existing key (so `false`).
5. Comparing two `int`s is very cheap, while `equals()` can be expensive, so most non-matching nodes are skipped quickly. The hash isn't enough because the `equals`/`hashCode` contract only goes one way: equal objects have equal hashes, but equal hashes don't imply equal objects (e.g. `"Aa"` and `"BB"`).
6. Load factor = elements ÷ capacity. The default is 0.75 with capacity 16, so the table resizes once there are more than 12 entries. Rehashing allocates an array twice as large and re-distributes every entry using the new capacity, which shortens the chains.
7. In Java 8+, when a bucket's chain reaches 8 nodes (and the table has at least 64 buckets), the chain is converted into a red-black tree so lookups in that bucket are O(log n). Tree nodes are larger and more complex, so for the usual short chains a linked list is cheaper.
8. `LinkedHashMap` is a `HashMap` whose entries also form a doubly linked list (`before`/`after`) in insertion order. Choose it when you need predictable iteration order. `TreeMap` uses no hashing; it's a red-black tree ordered by `compareTo`/`Comparator`, with O(log n) operations. Choose it for sorted keys, min/max, floor/ceiling and range queries. Plain `HashMap` is the fastest default when order doesn't matter.
</details>

## Further Study
- `HashMap` source: `hash()` bit spreading, `(n - 1) & hash`, `resize()` splitting chains into "lo" and "hi" lists
- `TREEIFY_THRESHOLD`, `UNTREEIFY_THRESHOLD`, `MIN_TREEIFY_CAPACITY`
- Open addressing: linear probing, quadratic probing, double hashing (used by `IdentityHashMap`)
- Red-black trees vs AVL trees; tree rotations
- `NavigableSet`/`NavigableMap` methods: `floor`, `ceiling`, `higher`, `lower`, `subMap`, `headMap`, `tailMap` (next video)
- `LinkedHashMap` access order and building an LRU cache with `removeEldestEntry`
- `ConcurrentHashMap` for multi-threaded code
- The `equals()`/`hashCode()` contract and `Comparable` consistency with `equals`

---

[← #32](32-list-interface-arraylist-linkedlist.md) · [Index](../../ReadMe.md) · [#34 →](34-set-map-methods-special-maps.md)
