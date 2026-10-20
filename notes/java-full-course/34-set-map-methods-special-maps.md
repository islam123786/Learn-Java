# Set & Map Methods Explained | EnumMaps, IdentityHashMaps & More | Java Full Course #34
> Source: https://youtu.be/b1Uj_2MPBwA · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 71:19. The video is in Hindi (these notes are in English). The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within a minute or two.

## TL;DR
- `Set` adds **no new methods** of its own. `HashSet` and `LinkedHashSet` only have what `Collection`/`Iterable` already declare (`add`, `remove`, `contains`, `size`, …), plus four constructors: empty, initial capacity, capacity + load factor, and "copy from another collection".
- `TreeSet` gets a rich API from **`SortedSet`** (`first`, `last`, `headSet`, `tailSet`, `subSet`) and **`NavigableSet`** (`lower`, `floor`, `higher`, `ceiling`, `pollFirst`, `pollLast`, `descendingSet`, `descendingIterator`, and inclusive/exclusive versions of the range views). To call them, declare the variable as `TreeSet`/`NavigableSet`, not `Set`.
- `Map` is not a `Collection`, so it declares all its methods itself, with familiar names: `put`, `get`, `getOrDefault`, `putIfAbsent`, `containsKey`, `containsValue`, `remove`, `replace`, `putAll`, `clear`, `keySet`, `values`, `entrySet`. `put` returns the **old value** (or `null`).
- `TreeMap` mirrors `TreeSet`, with **key** and **entry** versions of each method: `firstKey`/`firstEntry`, `lowerKey`/`lowerEntry`, `headMap`, `tailMap`, `subMap`, `descendingMap`, …
- Other maps: **`Hashtable`** (legacy, synchronized; avoid), **`Properties`** (string config, extends `Hashtable`), **`WeakHashMap`** (keys can be garbage-collected; rare), **`IdentityHashMap`** (compares keys with `==`), **`EnumMap`** (array indexed by enum ordinal; fast, ordered, no `null` keys), **`ConcurrentHashMap`** (the modern thread-safe map).

## Key Concepts

**Set inherits everything.** All the operations a set needs are already declared in `Collection`. This is inheritance and polymorphism at work: learn `Collection` once and you know `HashSet`/`LinkedHashSet`.

**`SortedSet` and `NavigableSet`.** Two interfaces between `Set` and `TreeSet`. `SortedSet` gives endpoints and range views; `NavigableSet` adds "nearest neighbour" lookups, polling, descending views, and range views with explicit inclusive/exclusive flags. *Why it matters:* these are what make `TreeSet` worth its O(log n) cost.

**Reference type limits what you can call.** `Set<Integer> s = new TreeSet<>()` compiles, but `s.first()` doesn't, because the compiler only looks at the declared type `Set`. Use `TreeSet`, `NavigableSet` or `SortedSet` as the variable type.

**Map method contracts.** `put` returns the previous value, `putIfAbsent` never overwrites, `replace` never inserts, `getOrDefault` avoids `null` checks, `remove(k, v)` and `replace(k, old, new)` only act when the current value matches.

**`Map.Entry`.** A nested interface of `Map` representing one key-value pair, with `getKey()`, `getValue()` (and `setValue`). `entrySet()` returns a `Set<Map.Entry<K, V>>`, which is the usual way to loop over a map.

**Special-purpose maps.** Most code uses `HashMap`, sometimes `LinkedHashMap` or `TreeMap`. The others solve narrow problems: identity semantics, enum keys, config files, caches, concurrency.

## Detailed Notes

### Set hierarchy [00:00]
- `Iterable` → `Collection` → `Set`. Under `Set`: `HashSet` (with `LinkedHashSet` extending it) and `TreeSet`.
- `TreeSet` doesn't implement `Set` directly. Between them sit two interfaces: **`Set` → `SortedSet` → `NavigableSet` → `TreeSet`**. `TreeSet` implements the methods of all of them.

### `Set` has no special methods [02:44]
- Unlike `List` (which adds `get(index)`, `set`, `add(index, e)`, …), `Set` adds nothing new. `add`, `remove`, `contains`, `clear`, `isEmpty`, `size` all come from `Collection`.
- So `HashSet`/`LinkedHashSet` have no methods you haven't already seen. Only `TreeSet` brings new ones.

### `HashSet` / `LinkedHashSet` constructors [04:33]
1. `new HashSet<>()`: empty, default capacity 16 and load factor 0.75 (from the backing `HashMap`).
2. `new HashSet<>(100)`: initial capacity.
3. `new HashSet<>(100, 0.8f)`: capacity **and** load factor (a `float`). With 0.8, rehashing waits until the table is 80% full.
4. `new HashSet<>(List.of(1, 2, …, 9))`: copy from another collection.
- All four exist for `LinkedHashSet` too, since it extends `HashSet`.

### `TreeSet` overview [07:45]
- `TreeSet` is a self-balancing BST (backed by a `TreeMap`). Its extra methods are exactly the operations a BST is good at. Once you know them, `TreeMap`'s methods are the same with "Key"/"Entry" in the name.
- You don't need to memorize the list; know what's possible and let the IDE fill in names.

### `TreeSet` constructors and demo [09:20]
- Constructors shown: empty `new TreeSet<>()`, and from a collection `new TreeSet<>(List.of(2, 5, 7, 9))`. There's no capacity or load factor because there's no bucket array.
- *Addition:* `TreeSet` also has `TreeSet(Comparator)` for custom ordering and `TreeSet(SortedSet)`. Comparators come in videos #36 and #37.
- Demo data: `80, 23, 10, 90, 50`.

### `SortedSet` methods [11:37]
- First attempt: `Set<Integer> set = new TreeSet<>(); set.first();` → compile error "first() is undefined for the type Set". The object is a `TreeSet`, but the **reference type** decides which methods you can call. Declare it as `TreeSet<Integer>` (or `NavigableSet`/`SortedSet`; with `SortedSet` you can't reach the `NavigableSet` methods).
- `first()` → smallest (10). `last()` → largest (90). In a BST the leftmost node is the smallest and the rightmost the largest.
- Most `TreeSet` operations are **O(log n)**.
- `headSet(80)` [14:21] → elements **strictly less** than 80: `[10, 23, 50]`.
- `tailSet(80)` [15:43] → elements **≥ 80**: `[80, 90]` (inclusive).
- `subSet(23, 80)` [16:10] → from 23 **inclusive** to 80 **exclusive**: `[23, 50]`.
- *Correction:* the video says `NavigableSet` "repeats" `first`/`last`/`headSet`/… These are inherited from `SortedSet`, not re-declared. What `NavigableSet` adds are the boolean overloads (below).

### `NavigableSet` methods [17:32]
- `lower(80)` → largest element **strictly less** than 80: `50`.
- `floor(80)` [18:27] → largest element **≤** 80: `80`. The difference shows at the bottom: `lower(10)` → `null`, `floor(10)` → `10`.
- `higher(80)` [19:49] → smallest element **strictly greater**: `90`. `ceiling(80)` → smallest **≥**: `80`.
- `pollFirst()` / `pollLast()` [20:57] → return **and remove** the smallest/largest (10 and 90). Afterwards `first()` is 23. `first()`/`last()` only look.
- `descendingSet()` [22:33] → `[90, 80, 50, 23, 10]`. `descendingIterator()` walks the same order with `hasNext()`/`next()`.
- Tunable range views [23:28]: `headSet(80, true)` includes 80 → `[10, 23, 50, 80]`; `tailSet(80, false)` excludes it; `subSet(10, true, 80, true)` lets you choose both ends. The one-argument versions have fixed rules (`headSet` exclusive, `tailSet` inclusive, `subSet` from-inclusive/to-exclusive).
- *Note:* `headSet`, `tailSet` and `subSet` return **views** backed by the original set, not copies. Changes to one show up in the other.

### Map hierarchy and the zoo of maps [25:44]
- `Map` → `HashMap` → `LinkedHashMap`; `Map` → `SortedMap` → `NavigableMap` → `TreeMap`.
- Other maps covered at the end: `Hashtable`, `Properties`, `EnumMap`, `IdentityHashMap`, `WeakHashMap`, `ConcurrentHashMap`. Some are niche, some are legacy.
- Because `Map` is outside `Collection`, it must declare `size`, `isEmpty`, `containsKey`, etc. itself. Java kept the names close to `Collection`'s so there's little new to remember.

### `Map` methods [27:47]
- Setup: `Map<Integer, String> map = new HashMap<>()` with roll number → name: `101 → Aditya`, `102 → Rohit`, `103 → Rohan`. (The transcript sometimes says "1001"; the outputs shown match 101.)
- `size()` → 3. `isEmpty()` → false.
- **`containsKey(102)`** / **`containsValue("Aditya")`** [29:50]. `containsKey` is O(1) on average (keys are hashed). `containsValue` is **O(n)**: values aren't hashed, so it must walk every bucket and every chain.
- **`get(101)`** [31:12] → "Aditya". `get(105)` → `null` for a missing key.
- **`put` returns something** [32:07]: `put(104, "Abhay")` (new key) → `null`. `put(103, "Abhay")` (existing key) → `"Rohan"`, the **old** value, and `get(103)` is now "Abhay".
- `remove(101)`, `putAll(otherMap)` (bulk `put`), `clear()` [33:02].
- **`keySet()`** [34:51] → `Set<Integer>` of all keys (a `Set` because keys are unique).
- **`values()`** [35:46] → `Collection<String>`. Not a set, because values can repeat. That's why there's no `valueSet()`.
- **`entrySet()`** [36:54] → `Set<Map.Entry<Integer, String>>`. `Map.Entry` is an interface nested inside `Map`; each entry is one key-value pair and prints as `101=Aditya`.
- **`getOrDefault(105, "Unknown")`** [38:57] → "Unknown" instead of `null`. Used a lot in production because it avoids `null` checks and `NullPointerException`s.
- **`putIfAbsent(103, "Abhay")`** [39:51] → does nothing because 103 exists. Plain `put` always replaces; `putIfAbsent` **never replaces an existing value**.
- **`remove(key, value)`** [41:13] → removes only if the key currently maps to that value. `remove(101, "Abhay")` does nothing.
  - *Correction:* the video says the one-argument `remove(Object key)` "comes from the Object class". It doesn't; it's declared in `Map`. It just takes an `Object` parameter.
- **`replace(101, "Sonu")`** [41:54] → updates only if the key exists; unlike `put`, it **never inserts** a new key.
- **`replace(101, "Aditya", "Sonu")`** → "safe replace": only if the current value equals the given old value.
- **Looping with entries** [43:57]: `for (Map.Entry<Integer, String> e : map.entrySet())` with `e.getKey()` and `e.getValue()`.
- **`Map.of(k1, v1, k2, v2, …)`** [45:47] → an **immutable** map. `put` on it throws `UnsupportedOperationException`.
  - *Note:* `Map.of` has overloads up to 10 pairs; beyond that use `Map.ofEntries(Map.entry(k, v), …)`. It also rejects `null` keys/values and duplicate keys, and its iteration order is unspecified.

### `HashMap` / `LinkedHashMap` constructors [47:09]
- Same four as the sets: `new HashMap<>()`, `new HashMap<>(100)`, `new HashMap<>(100, 0.8f)`, and `new HashMap<>(otherMap)` (e.g. from `Map.of(...)`). All work for `LinkedHashMap` too.

### `TreeMap` methods [49:12]
- Declare as `TreeMap` (or `NavigableMap`) to reach these. They mirror `TreeSet`, but since only keys are unique, names say **Key** or **Entry**:
  - `firstKey()` → 101, `lastKey()` → 103 [51:28]; `firstEntry()` → `101=Aditya`, `lastEntry()` → `103=Rohan`.
  - `headMap(103)` → `{101=Aditya, 102=Rohit}` (exclusive) [52:37]; `tailMap(102)` → `{102=Rohit, 103=Rohan}` (inclusive); `subMap(101, 103)` → `{101=Aditya, 102=Rohit}`.
  - `lowerKey(102)` → 101 [53:59], `lowerEntry(102)` → `101=Aditya`. Likewise `floorKey/floorEntry`, `ceilingKey/ceilingEntry`, `higherKey/higherEntry`.
  - `pollFirstEntry()`, `pollLastEntry()`, `descendingMap()` → `{103=Rohan, 102=Rohit, 101=Aditya}`.
  - Boolean overloads: `headMap(k, inclusive)`, `tailMap(k, inclusive)`, `subMap(from, fromInc, to, toInc)`.
- *Correction:* the video attributes `headMap`/`tailMap`/`subMap` to `NavigableMap` and `lowerKey` etc. to `SortedMap`. It's mostly the other way: `firstKey`, `lastKey` and the one/two-argument `headMap`/`tailMap`/`subMap` come from **`SortedMap`**. The `…Entry` methods, `lower/floor/ceiling/higher…`, `poll…`, `descendingMap` and the boolean overloads come from **`NavigableMap`**.

### `Hashtable` [56:15]
- A **legacy** class from before the Collections Framework. Same job as `HashMap`, but every method is synchronized (thread-safe).
- Like `Vector` for lists, avoid it in new code: locking the whole table for every call is slow. For thread safety use **`ConcurrentHashMap`**.
- *Addition:* `Hashtable` also rejects `null` keys and values.

### `Properties` [57:51]
- Extends `Hashtable`. A specialized map for **configuration data**, which is text: `username=admin`, `password=123`.
- You'll see it again with Spring Boot configuration. Many people use it without realizing it's a `Map`.
- *Addition:* use `setProperty`/`getProperty(key, default)` rather than `put`/`get`, and `load(Reader)`/`store(...)` to read/write `.properties` files.

### `WeakHashMap` [59:13]
- Rarely used. Keys are held through **weak references**, so an entry can disappear once nothing else references its key. Sometimes used for cache-like behavior.
- Weak references come with memory management later in the course.

### `IdentityHashMap` [59:54]
- A normal `HashMap` is **equality-based**: it uses `hashCode()` and `equals()`. Two `new String("Aditya")` objects are different heap objects, but they are `equals`, have the same hash, and count as **one** key.
- `IdentityHashMap` breaks that rule on purpose: keys are equal only if they are the **same object** (`==`). Those two strings are two different keys.
- Rare uses, e.g. graph algorithms (cycle detection, topological sort) where you track specific node objects, or serialization/deep-copy code.
- *Addition:* it uses `System.identityHashCode` and open addressing (linear probing) internally, not chaining.

### `EnumMap` [65:08]
- For maps whose keys are enum constants (`enum Day { MONDAY, TUESDAY, … }`). A `HashMap<Day, Integer>` works, but `EnumMap` is better.
- Java knows an enum has a fixed, small set of constants, each with an **ordinal** (MONDAY = 0, TUESDAY = 1, …). `EnumMap` uses the ordinal as an array index, so there's **no hashing** and no collisions.
- Benefits: more memory-efficient and faster; **iteration follows enum declaration order** (MONDAY, TUESDAY, …), whatever order you inserted.
- **`null` keys are not allowed**; `null` values are.
- Create it with `new EnumMap<>(Day.class)`. The video writes `new EnumMap<>()`, which doesn't compile: there is no no-arg constructor because the map needs the enum's class.
- *Correction:* the video talks about "better distribution in buckets". There are no buckets: it's a plain array with one slot per constant.

### `ConcurrentHashMap` [69:01]
- The modern **thread-safe** map for multithreaded code (race conditions). It doesn't lock the whole map for reads or for updates to different parts, so it's much faster than `Hashtable`.
- Summary: day to day use `HashMap`; `LinkedHashMap` for insertion order; `TreeMap` for sorted keys; `ConcurrentHashMap` for multiple threads; `EnumMap`/`IdentityHashMap` for their niche; `WeakHashMap` and `Hashtable` almost never.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### `TreeSet`: `SortedSet` and `NavigableSet` methods
```java
import java.util.*;

public class TreeSetNavigation {
    public static void main(String[] args) {
        TreeSet<Integer> set = new TreeSet<>(List.of(80, 23, 10, 90, 50)); // not Set<Integer>!

        // SortedSet
        System.out.println(set);                 // [10, 23, 50, 80, 90]
        System.out.println(set.first());         // 10
        System.out.println(set.last());          // 90
        System.out.println(set.headSet(80));     // [10, 23, 50]       (< 80)
        System.out.println(set.tailSet(80));     // [80, 90]           (>= 80)
        System.out.println(set.subSet(23, 80));  // [23, 50]           [23, 80)

        // NavigableSet
        System.out.println(set.lower(80));       // 50
        System.out.println(set.floor(80));       // 80
        System.out.println(set.lower(10));       // null
        System.out.println(set.floor(10));       // 10
        System.out.println(set.higher(80));      // 90
        System.out.println(set.ceiling(80));     // 80
        System.out.println(set.headSet(80, true));            // [10, 23, 50, 80]
        System.out.println(set.subSet(10, false, 80, true));  // [23, 50, 80]
        System.out.println(set.descendingSet()); // [90, 80, 50, 23, 10]

        Iterator<Integer> it = set.descendingIterator();
        StringBuilder sb = new StringBuilder();
        while (it.hasNext()) sb.append(it.next()).append(' ');
        System.out.println(sb.toString().trim()); // 90 80 50 23 10

        System.out.println(set.pollFirst() + " " + set.pollLast()); // 10 90
        System.out.println(set.first());         // 23  (10 was removed)
        System.out.println(set);                 // [23, 50, 80]
    }
}
```

### `Map` essentials
```java
import java.util.*;

public class MapEssentials {
    public static void main(String[] args) {
        // HashMap with small Integer keys 101..104 iterates in key order in practice
        // (they hash to buckets 5, 6, 7, 8). Not guaranteed in general.
        Map<Integer, String> map = new HashMap<>();
        map.put(101, "Aditya");
        map.put(102, "Rohit");
        map.put(103, "Rohan");

        System.out.println(map.size() + " " + map.isEmpty());      // 3 false
        System.out.println(map.containsKey(102));                  // true   O(1) avg
        System.out.println(map.containsValue("Aditya"));           // true   O(n)
        System.out.println(map.get(105));                          // null
        System.out.println(map.getOrDefault(105, "Unknown"));      // Unknown

        System.out.println(map.put(104, "Abhay"));                 // null   (new key)
        System.out.println(map.put(103, "Abhay"));                 // Rohan  (old value)
        System.out.println(map.get(103));                          // Abhay

        System.out.println(map.putIfAbsent(103, "Aman"));          // Abhay  (kept, returns current)
        System.out.println(map.replace(999, "Ghost"));             // null   (replace never inserts)
        System.out.println(map.containsKey(999));                  // false
        System.out.println(map.replace(101, "Aditya", "Sonu"));    // true   (old value matched)
        System.out.println(map.remove(102, "Wrong"));              // false  (value mismatch)
        System.out.println(map.remove(104));                       // Abhay

        System.out.println(map.keySet());                          // [101, 102, 103]
        System.out.println(map.values());                          // [Sonu, Rohit, Abhay]
        System.out.println(map.entrySet());                        // [101=Sonu, 102=Rohit, 103=Abhay]

        for (Map.Entry<Integer, String> e : map.entrySet()) {
            System.out.println(e.getKey() + ", " + e.getValue());
        }
        // 101, Sonu
        // 102, Rohit
        // 103, Abhay

        Map<Integer, String> extra = new HashMap<>();
        extra.put(201, "Neha");
        map.putAll(extra);
        System.out.println(map.size());                            // 4
        map.clear();
        System.out.println(map.isEmpty());                         // true

        Map<Integer, String> fixed = Map.of(101, "Aditya", 102, "Rohit");
        try {
            fixed.put(103, "Rohan");
        } catch (UnsupportedOperationException ex) {
            System.out.println("Map.of is immutable");             // Map.of is immutable
        }
    }
}
```

### `TreeMap`: key and entry navigation
```java
import java.util.*;

public class TreeMapNavigation {
    public static void main(String[] args) {
        TreeMap<Integer, String> map = new TreeMap<>();
        map.put(103, "Rohan");
        map.put(101, "Aditya");
        map.put(102, "Rohit");

        System.out.println(map);                    // {101=Aditya, 102=Rohit, 103=Rohan}  (sorted by key)
        System.out.println(map.firstKey());         // 101
        System.out.println(map.lastKey());          // 103
        System.out.println(map.firstEntry());       // 101=Aditya
        System.out.println(map.lastEntry());        // 103=Rohan
        System.out.println(map.headMap(103));       // {101=Aditya, 102=Rohit}
        System.out.println(map.tailMap(102));       // {102=Rohit, 103=Rohan}
        System.out.println(map.subMap(101, 103));   // {101=Aditya, 102=Rohit}
        System.out.println(map.headMap(103, true)); // {101=Aditya, 102=Rohit, 103=Rohan}
        System.out.println(map.lowerKey(102));      // 101
        System.out.println(map.lowerEntry(102));    // 101=Aditya
        System.out.println(map.ceilingKey(150));    // null
        System.out.println(map.floorEntry(150));    // 103=Rohan
        System.out.println(map.descendingMap());    // {103=Rohan, 102=Rohit, 101=Aditya}
        System.out.println(map.pollFirstEntry());   // 101=Aditya
        System.out.println(map);                    // {102=Rohit, 103=Rohan}
    }
}
```

### Special maps: `IdentityHashMap`, `EnumMap`, `Properties`
```java
import java.util.*;

public class SpecialMapsDemo {
    enum Day { MONDAY, TUESDAY, WEDNESDAY }

    public static void main(String[] args) {
        String a = new String("Aditya");
        String b = new String("Aditya");          // equal content, different object

        Map<String, Integer> normal = new HashMap<>();
        normal.put(a, 1);
        normal.put(b, 2);
        System.out.println(normal.size());        // 1  (a.equals(b))

        Map<String, Integer> identity = new IdentityHashMap<>();
        identity.put(a, 1);
        identity.put(b, 2);
        System.out.println(identity.size());      // 2  (a != b)

        EnumMap<Day, Integer> days = new EnumMap<>(Day.class); // needs the enum class
        days.put(Day.WEDNESDAY, 3);
        days.put(Day.MONDAY, 1);
        days.put(Day.TUESDAY, null);              // null values are fine
        System.out.println(days);                 // {MONDAY=1, TUESDAY=null, WEDNESDAY=3}  (ordinal order)
        try {
            days.put(null, 0);
        } catch (NullPointerException e) {
            System.out.println("EnumMap rejects null keys"); // EnumMap rejects null keys
        }

        Properties config = new Properties();
        config.setProperty("username", "admin");
        config.setProperty("password", "123");
        System.out.println(config.getProperty("username"));        // admin
        System.out.println(config.getProperty("port", "8080"));    // 8080 (default)
        System.out.println(config instanceof Map);                 // true
    }
}
```

## Common Pitfalls / Tips
- **Declare `TreeSet`/`TreeMap` (or `NavigableSet`/`NavigableMap`) when you need navigation methods.** With `Set`/`Map` as the variable type, `first()`, `floor()`, `headMap()` etc. don't compile.
- **Remember the default bounds:** `headSet`/`headMap` exclude the bound, `tailSet`/`tailMap` include it, `subSet`/`subMap` include the start and exclude the end. Use the boolean overloads when you need something else.
- **`lower`/`higher` are strict, `floor`/`ceiling` are not.** All four return `null` when nothing qualifies, so unboxing the result into an `int` can throw `NullPointerException`.
- **Range views are live.** `set.headSet(80).clear()` removes those elements from the original set. Adding an element outside a view's range throws `IllegalArgumentException`.
- **`containsValue` is O(n).** If you need fast lookups by value too, keep a second map in the reverse direction.
- **`put` vs `putIfAbsent` vs `replace`:** `put` always writes; `putIfAbsent` only writes if the key is absent (or mapped to `null`); `replace` only writes if the key is present.
- **`Map.of`/`Set.of`/`List.of` are immutable** and reject `null`s. Wrap them in `new HashMap<>(...)` if you need to modify them.
- **Don't use `Hashtable` in new code.** Use `HashMap`, or `ConcurrentHashMap` when threads share the map.
- **`EnumMap` needs `Day.class`** in its constructor and rejects `null` keys.

## Review Questions
1. Why does `Set` not declare any new methods compared with `Collection`? Where do `TreeSet`'s extra methods come from?
2. `Set<Integer> s = new TreeSet<>(); s.first();` doesn't compile. Why, and how do you fix it?
3. Given the `TreeSet` `[10, 23, 50, 80, 90]`, what do `lower(80)`, `floor(80)`, `higher(80)`, `ceiling(80)`, `lower(10)` and `headSet(80)` return?
4. What is the difference between `first()` and `pollFirst()`?
5. What does `map.put(k, v)` return, and how does that differ from `putIfAbsent` and `replace`?
6. Why does `values()` return a `Collection` but `keySet()` return a `Set`? Why is `containsValue` slower than `containsKey`?
7. What is a `Map.Entry`, and how do you iterate over all key-value pairs of a map?
8. When would you use `IdentityHashMap`, `EnumMap` and `ConcurrentHashMap` instead of `HashMap`?

<details>
<summary>Answers</summary>

1. Everything a set needs (`add`, `remove`, `contains`, `size`, `clear`, `isEmpty`, iteration) is already declared in `Collection`/`Iterable`; a set only changes the *rules* (no duplicates). `TreeSet`'s extra methods come from the `SortedSet` and `NavigableSet` interfaces between `Set` and `TreeSet`.
2. The compiler only allows methods of the declared reference type, and `Set` has no `first()`. Declare the variable as `TreeSet<Integer>` (or `SortedSet`/`NavigableSet`).
3. `lower(80)` = 50, `floor(80)` = 80, `higher(80)` = 90, `ceiling(80)` = 80, `lower(10)` = `null`, `headSet(80)` = `[10, 23, 50]`.
4. `first()` only returns the smallest element. `pollFirst()` returns it **and removes** it from the set (and returns `null` on an empty set instead of throwing).
5. `put` returns the previous value for the key, or `null` if the key was new, and always stores the new value. `putIfAbsent` stores only if the key is absent, so an existing value is never replaced. `replace` stores only if the key is already present, so it never inserts a new key.
6. Keys are unique, so they naturally form a set; values can repeat, so they're returned as a plain `Collection`. Keys are hashed, so `containsKey` jumps straight to one bucket; values aren't, so `containsValue` must scan every entry.
7. `Map.Entry<K, V>` is an interface nested in `Map` representing one key-value pair, with `getKey()` and `getValue()`. Loop with `for (Map.Entry<K, V> e : map.entrySet()) { e.getKey(); e.getValue(); }`.
8. `IdentityHashMap` when keys must be compared by object identity (`==`), e.g. tracking specific node objects in graph algorithms. `EnumMap` when keys are enum constants: faster, smaller, ordered by declaration. `ConcurrentHashMap` when several threads read and write the same map.
</details>

## Further Study
- `NavigableSet`/`NavigableMap` Javadoc: full list of methods and view semantics
- `Map` default methods from Java 8: `compute`, `computeIfAbsent`, `computeIfPresent`, `merge`, `forEach`, `replaceAll`
- Java 21 `SequencedCollection`/`SequencedMap` (`firstEntry`, `reversed()` on `LinkedHashMap`)
- `Collections.unmodifiableMap` vs `Map.copyOf` vs `Map.of`
- `ConcurrentHashMap` internals (CAS, per-bin locking) and `Collections.synchronizedMap`
- Weak, soft and phantom references; `WeakHashMap` and caches
- `LinkedHashMap` access order and LRU caches
- Reading `.properties` files with `Properties.load`

---

[← #33](33-set-map-hashmap-internals.md) · [Index](README.md) · [#35 →](35-queue-priorityqueue-heap.md)

---

[← #33](33-set-map-hashmap-internals.md) · [Index](README.md) · [#35 →](35-queue-priorityqueue-heap.md)
