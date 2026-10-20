# Java Collection Framework Explained | Data Structures + Complete Hierarchy | Java Full Course #29
> Source: https://youtu.be/mW2OO6d6_BE · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 75:58. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Every data structure is a **trade-off**. None is "best". You pick the one that makes *your* most frequent operation (insert, search, lookup by key, …) cheap.
- You could build every classic data structure yourself from arrays, classes and references (dynamic array, linked list, hash set, stack, queue, hash map, BST). Java already ships tested, optimized versions: `ArrayList`, `LinkedList`, `HashSet`, `ArrayDeque`, `HashMap`, `TreeSet`/`TreeMap`, and more.
- Hashing (`hashCode()` → `% capacity` → bucket index) is how sets and maps get **O(1)** insert and lookup. **Chaining** handles collisions.
- Java puts these classes in one **interface hierarchy**: `Iterable` → `Collection` → `List` / `Set` / `Queue`. Because of that you code against the interface (`List<Integer> l = new ArrayList<>()`) and polymorphism gives you the same method names everywhere.
- `Map` is **not** a `Collection`. It holds key-value pairs, so it has its own hierarchy (`HashMap`, `LinkedHashMap`, `TreeMap`).

## Key Concepts

**Data structure.** A way to store data so that the queries you care about are efficient. It's a language-independent idea; languages only provide implementations. *Why it matters:* picking the wrong one can turn an O(1) operation into O(n).

**Time complexity, O(1) vs O(n).** O(1) means constant time, the same cost no matter how much data you have. O(n) means the cost grows with the number of elements. *Why it matters:* it's the language you use to compare the trade-offs.

**Dynamic array (`ArrayList`).** A fixed-size array hidden inside a class. When it fills up, the class allocates a bigger array (the video uses double the size), copies the old elements over, and carries on. *Why it matters:* plain arrays have a fixed size. A dynamic array gives you growth plus O(1) random access by index.

**Linked list (`LinkedList`).** A chain of nodes. Each node holds `data` and a `next` reference to another node. The nodes don't need to be next to each other in memory. *Why it matters:* inserting means re-linking references instead of shifting elements, and the list never "fills up".

**Set (`HashSet`, `LinkedHashSet`, `TreeSet`).** A container with no duplicates and fast `contains`. A hash set stores each element at index `hash % capacity`. *Why it matters:* you get uniqueness and membership checks in O(1) on average.

**Hashing and chaining.** `hashCode()` turns any object (for example a `String` email) into an `int`. The modulo maps that `int` to a bucket. When two elements land in the same bucket (a *collision*), the bucket holds a small linked list (*chaining*). When the chains get long, the table is resized. *Why it matters:* this is how `HashSet` and `HashMap` work internally.

**Stack (LIFO).** Last In, First Out. You push and pop at the same end, like a stack of plates. *Why it matters:* it fits undo, backtracking and call stacks, and any case where you need things back in reverse order.

**Queue (FIFO).** First In, First Out. You add at one end and remove from the other, like a line of people. *Why it matters:* it fits task scheduling, BFS and buffering.

**Map (`HashMap`, `LinkedHashMap`, `TreeMap`).** Key-value pairs with unique keys and O(1) lookup by key (for hash maps). Internally it works like a hash set of `Node(key, value)`, hashed on the **key**. *Why it matters:* it's the standard tool for "look this up by name/id".

**Tree / Binary Search Tree (BST).** Hierarchical data. In a BST each node has at most two children: smaller values go left, larger values go right. *Why it matters:* it models hierarchies such as files and folders, and keeps data sorted. `TreeSet` and `TreeMap` use a *self-balancing* BST.

**Collection Framework hierarchy.** All the "containers of values" share parent interfaces, so methods like `add` are declared once (in `Collection`) and every implementation provides its own version. *Why it matters:* there are fewer APIs to memorize, and you can swap `ArrayList` for `LinkedList` without touching the calling code.

## Detailed Notes

### Why data structures exist [00:00]
- Code is about **data**. We store it so that we can **query** it efficiently.
- Example: an unsorted array `3, 7, 10, 1, 4, 12`.
  - Appending is **O(1)**: just put the new element at the end.
  - "Find the largest" is **O(n)**: you have to scan every element.

### Sorted array: the first trade-off [03:18]
- Keep the array sorted (ascending). Then the largest element is always `arr[arr.length - 1]`, which is **O(1)**.
- Inserting (e.g. `6`) now costs **O(n)**: find the position, then shift every larger element one step to the right.
- Lesson: **every data structure is a trade-off.** Optimize for your most frequent query.
- Java gives you classes for nearly every data structure. You could also build them yourself from user-defined types (classes and objects).

### Dynamic array → `ArrayList` [07:24]
- Limits of a plain array: the size is fixed (`new int[10]` holds indexes 0–9). Writing `arr[10]` throws `ArrayIndexOutOfBoundsException`. Over-allocating (`new int[1000]`) wastes memory.
- The idea: keep a private internal array with an initial capacity and expose methods:
  - `add(el)`: put the element in the next free slot. If the array is full, create an array **twice the size**, copy the old values, then add.
  - `get(index)`: return the element at that index. The internal array stays `private`, which is encapsulation.
  - `insert(index, el)`: shift the elements from `index` onward one step right, then place the element (grow first if full). This is different from *replace*.
  - `delete`, `replace`, … in the same way.
- The class can be made generic (`T`) instead of `int`.
- Java's version is **`ArrayList`** [16:40].

### Linked list → `LinkedList` [17:00]
- Problem: you insert a lot, and every array insert shifts O(n) elements.
- Arrays use **contiguous memory** (for `int`s at addresses 1000, 1004, 1008, …). A linked list drops that requirement.
- A **node** holds `data` plus a reference to another `Node`. A class can hold a reference to its own type.
- Memory walk-through:
  - `Node n1 = new Node(); n1.data = 4;` puts the object on the heap and the `n1` reference on the stack. `n1.ref` starts out as `null`.
  - `Node n2 = new Node(); n2.data = 5;`
  - `n1.ref = n2;` copies n2's address into n1's reference, which links the two nodes.
- Advantages:
  - It never gets full. Adding means making a new node and linking it, with no doubling and copying.
  - Inserting in the middle (e.g. `8` at position 1) only re-links `4 → 8 → 5`. Nothing shifts.
- Structure of your own `LinkedList` class:
  - Make `Node` a **`private static` nested class**. It only makes sense inside the list, and users shouldn't touch it.
  - Name the reference `next` by convention, and keep a `head` reference that starts as `null`.
  - Expose `add`, `insert`, `remove`, and so on. That's abstraction.
- Java's version is **`LinkedList`**. You use it without knowing how it works inside, but you should understand that from DSA.

### Set → `HashSet` / `LinkedHashSet` [32:15]
- Requirements: (1) no duplicates, (2) `contains(x)` in **O(1)**.
- With a plain array, both are O(n). You'd have to scan for duplicates and scan for membership.
- **Version 1: direct indexing** (values in a known range, e.g. 0–100):
  - Use an array of size **101** (not 100), so that index 100 exists.
  - `add(el)`: `arr[el] = el`. Duplicates just overwrite themselves, so there's never more than one copy.
  - `contains(el)`: `arr[el] == el`. Both operations are O(1).
  - This breaks down when the range is huge (up to a crore, i.e. 10⁷, or more). The array would be enormous and mostly empty.
- **Version 2: modulo** [39:26]:
  - Use a fixed-size array (e.g. 10) and store each element at `el % arr.length`. For example, 1 → 1, 10 → 0, 6 → 6, 12 → 2.
  - `contains(el)` checks `arr[el % arr.length] == el`. Still O(1).
- **Chaining** [42:35]: `100 % 10 == 0` collides with `10`. That isn't a duplicate, so you can't overwrite it. Each slot becomes a linked list instead (10 → 100 → 1000). If the chains get long, **double the array** and re-distribute every element through the modulo again (rehashing).
- **Non-numbers** [44:29], e.g. unique email IDs (strings): call `Object.hashCode()` to get an `int`, then take it modulo the capacity.
- Java's versions are **`HashSet`** and **`LinkedHashSet`**. The difference between them is covered in a later video.

### Stack and Queue → `Stack`, `Queue`, `ArrayDeque` [45:40]
- **Stack (LIFO)**: push 2, 3, 4, 5 and you pop 5, 4, 3, 2. Entry and exit happen on the same side, and nothing is removed from the middle. Think of a stack of plates.
- Building one on an array: expose **only** `add` (push) and `remove()` (pop, which takes no argument). Leave out insert and replace.
  - The naive version keeps the top at index 0 and shifts everything on each push and pop. That's O(n).
  - The better version keeps a **top counter**, so push and pop work at the end of the used part. That's O(1).
  - A linked list works as well.
- **Queue (FIFO)** [51:52]: add 2, 3, 4 and you get back 2, 3, 4. You add at the rear and remove from the front, like a line of people.
  - Array version: elements enter at one end and leave from the other. With further optimization this becomes a **circular queue** (beyond the scope of the video).
- Java's versions: the `Queue` interface, `ArrayDeque`, and the `Stack` class (details in later videos).

### Map → `HashMap` / `LinkedHashMap` [55:42]
- A key-value store, e.g. name → roll number (`Aditya → 1001`, `Rohit → 102`, `Rohan → 103`).
- Requirements: O(1) `get(key)`, and **no duplicate keys**. Adding `Aditya → 105` again must not create a second Aditya.
- Building one:
  - Make a `Node` class with `key` (String) and `value` (int). Some people call it `Pair`.
  - `add(name, roll)`: create the node, compute `hash(key) % capacity`, and store the node at that index.
  - `get(key)`: hash the key the same way. Equal strings have equal hash codes, so you land on the same index. Return `node.value`.
- It's the same mechanism as the set, except that a set stores single values and a map stores key-value nodes hashed **by key**.
- Java's versions are **`HashMap`** and **`LinkedHashMap`**.

### Trees / BST → `TreeSet` / `TreeMap` [62:53]
- Trees hold **hierarchical** data. Example: a folder contains files and sub-folders, which contain more files.
- **BST rule**: values smaller than the node go in its left subtree and larger values go in the right subtree, at every level. Root 10, left 5, right 16. Anything placed left of 16 must still be greater than 10.
- Binary means at most 2 children per node (a ternary tree allows 3).
- `TreeNode` holds `data`, `left` and `right`. You link nodes with `n1.left = n3; n1.right = n2;`. Operations would be `add`, `remove` and `contains`.
- Java's versions are **`TreeSet`** and **`TreeMap`**, backed by a **self-balancing** BST (a red-black tree, covered in a later video).

### Why a hierarchy? The Collection Framework [68:57]
- If every class were independent, each would have its own method names and there would be far more to memorize.
- They're all **containers**. Only the internal storage strategy differs. So Java builds an **inheritance/interface hierarchy** and relies on **polymorphism**:
  - `ArrayList` and `LinkedList` both implement the **`List`** interface, which declares `add`, `remove`, `set`, and so on.
  - Each class implements `add` its own way. `ArrayList` writes to the next free index. `LinkedList` creates a node and links it.
  - Your code says `List<Integer> l = new ArrayList<>();` (or `new LinkedList<>()`) and calls `l.add(4)` without caring which one it is.
- `List`, `Set` and `Queue` all extend the **`Collection`** interface. Methods every container supports, like `add`, are declared there once.

### The complete hierarchy [73:39]
```
Object
└── Iterable
    └── Collection
        ├── List   → ArrayList, LinkedList, Vector → Stack
        ├── Queue  → ArrayDeque (via Deque), PriorityQueue, LinkedList (via Deque)
        └── Set    → HashSet → LinkedHashSet, TreeSet (via SortedSet/NavigableSet)

Map (separate hierarchy!) → HashMap → LinkedHashMap, TreeMap
```
*(Simplified, but it matches the JDK. The video only shows the top levels, and the in-between interfaces such as `Deque` and `NavigableSet` are filled in here.)*
- **`Map` is outside `Collection`** [75:05] because it deals with key-value pairs, while `Collection` deals with single values. People still study the two together.
- Coming up in the series: `Iterable`, the `Collection` methods, and each of `List`, `Queue`, `Set` and `Map` in depth.

## Code Examples

These are simplified, from-scratch versions of what the video sketches. Each block is a complete program: save it under the class's name (e.g. `DynamicArray.java`) and run it with `java DynamicArray.java`. In real code, use the JDK classes shown at the end.

### Dynamic array (what `ArrayList` does)
```java
import java.util.Arrays;

public class DynamicArray {
    private int[] arr;
    private int size; // number of elements actually stored

    public DynamicArray(int initialCapacity) {
        arr = new int[initialCapacity];
    }

    public void add(int el) {
        ensureCapacity();
        arr[size++] = el;
    }

    public void insert(int index, int el) {
        if (index < 0 || index > size) throw new IndexOutOfBoundsException("Index: " + index);
        ensureCapacity();
        for (int i = size; i > index; i--) { // shift right
            arr[i] = arr[i - 1];
        }
        arr[index] = el;
        size++;
    }

    public int get(int index) {
        if (index < 0 || index >= size) throw new IndexOutOfBoundsException("Index: " + index);
        return arr[index];
    }

    public int size() { return size; }

    private void ensureCapacity() {
        if (size == arr.length) {
            arr = Arrays.copyOf(arr, arr.length * 2); // new array, double size, copy old values
        }
    }

    @Override
    public String toString() {
        return Arrays.toString(Arrays.copyOf(arr, size));
    }

    public static void main(String[] args) {
        DynamicArray da = new DynamicArray(5);
        for (int x : new int[]{4, 10, 13, 15, 17, 21}) da.add(x); // 6th add triggers growth
        da.insert(1, 25);
        System.out.println(da);          // [4, 25, 10, 13, 15, 17, 21]
        System.out.println(da.get(2));   // 10
    }
}
```

### Linked list with a `private static` nested `Node`
```java
public class MyLinkedList {
    private static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    private Node head;

    public void add(int el) {
        Node node = new Node(el);
        if (head == null) { head = node; return; }
        Node cur = head;
        while (cur.next != null) cur = cur.next;
        cur.next = node;
    }

    public void insert(int position, int el) {
        Node node = new Node(el);
        if (position == 0) { node.next = head; head = node; return; }
        Node prev = head;
        for (int i = 0; i < position - 1; i++) {
            if (prev == null) throw new IndexOutOfBoundsException("Position: " + position);
            prev = prev.next;
        }
        if (prev == null) throw new IndexOutOfBoundsException("Position: " + position);
        node.next = prev.next; // relink only — no shifting
        prev.next = node;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        for (Node cur = head; cur != null; cur = cur.next) {
            sb.append(cur.data).append(cur.next != null ? " -> " : "");
        }
        return sb.append("]").toString();
    }

    public static void main(String[] args) {
        MyLinkedList list = new MyLinkedList();
        list.add(4); list.add(5); list.add(6); list.add(7);
        list.insert(1, 8);
        System.out.println(list); // [4 -> 8 -> 5 -> 6 -> 7]
    }
}
```

### Hash set with modulo + chaining
```java
import java.util.LinkedList;

public class MyHashSet<T> {
    private LinkedList<T>[] buckets;
    private int size;

    @SuppressWarnings("unchecked")
    public MyHashSet(int capacity) {
        buckets = new LinkedList[capacity];
    }

    private int indexFor(Object el, int capacity) {
        // hashCode() can be negative, so use floorMod instead of %
        return Math.floorMod(el.hashCode(), capacity);
    }

    public boolean add(T el) {
        if (contains(el)) return false;            // no duplicates
        if (size >= buckets.length * 0.75) resize(); // keep chains short
        int i = indexFor(el, buckets.length);
        if (buckets[i] == null) buckets[i] = new LinkedList<>();
        buckets[i].add(el);                         // chaining on collision
        size++;
        return true;
    }

    public boolean contains(T el) {
        LinkedList<T> chain = buckets[indexFor(el, buckets.length)];
        return chain != null && chain.contains(el);
    }

    @SuppressWarnings("unchecked")
    private void resize() {
        LinkedList<T>[] old = buckets;
        buckets = new LinkedList[old.length * 2];
        for (LinkedList<T> chain : old) {
            if (chain == null) continue;
            for (T el : chain) {                    // rehash every element
                int i = indexFor(el, buckets.length);
                if (buckets[i] == null) buckets[i] = new LinkedList<>();
                buckets[i].add(el);
            }
        }
    }

    public static void main(String[] args) {
        MyHashSet<Integer> set = new MyHashSet<>(10);
        for (int x : new int[]{1, 10, 6, 12, 100, 1000, 10}) set.add(x); // 10, 100, 1000 collide
        System.out.println(set.contains(100)); // true
        System.out.println(set.contains(7));   // false
        System.out.println(set.size);          // 6 (second 10 rejected)

        MyHashSet<String> emails = new MyHashSet<>(10);
        emails.add("a@x.com");
        System.out.println(emails.add("a@x.com")); // false — duplicate
    }
}
```

### Array-backed stack (with a `top` counter)
```java
public class MyStack {
    private int[] arr;
    private int top = 0; // next free slot = number of elements

    public MyStack(int capacity) { arr = new int[capacity]; }

    public void push(int el) {
        if (top == arr.length) throw new IllegalStateException("Stack full");
        arr[top++] = el;
    }

    public int pop() {
        if (top == 0) throw new IllegalStateException("Stack empty");
        return arr[--top];
    }

    public static void main(String[] args) {
        MyStack s = new MyStack(5);
        s.push(2); s.push(3); s.push(4); s.push(5);
        System.out.println(s.pop() + " " + s.pop() + " " + s.pop() + " " + s.pop()); // 5 4 3 2
    }
}
```

### Simple map (hash on the key)
```java
public class MyHashMap {
    private static class Node {
        final String key;
        int value;
        Node next; // chain for collisions
        Node(String key, int value) { this.key = key; this.value = value; }
    }

    private final Node[] table = new Node[5];

    private int indexFor(String key) {
        return Math.floorMod(key.hashCode(), table.length);
    }

    /** Returns false if the key already exists (as in the video: no duplicate keys). */
    public boolean add(String key, int value) {
        int i = indexFor(key);
        for (Node n = table[i]; n != null; n = n.next) {
            if (n.key.equals(key)) return false;
        }
        Node node = new Node(key, value);
        node.next = table[i];
        table[i] = node;
        return true;
    }

    public Integer get(String key) {
        for (Node n = table[indexFor(key)]; n != null; n = n.next) {
            if (n.key.equals(key)) return n.value;
        }
        return null;
    }

    public static void main(String[] args) {
        MyHashMap m = new MyHashMap();
        m.add("Aditya", 1001);
        m.add("Rohit", 102);
        m.add("Rohan", 103);
        System.out.println(m.add("Aditya", 105)); // false — duplicate key
        System.out.println(m.get("Aditya"));      // 1001
    }
}
```

### BST node and insert
```java
public class MyBST {
    private static class TreeNode {
        int data;
        TreeNode left, right;
        TreeNode(int data) { this.data = data; }
    }

    private TreeNode root;

    public void add(int el) { root = add(root, el); }

    private TreeNode add(TreeNode node, int el) {
        if (node == null) return new TreeNode(el);
        if (el < node.data) node.left = add(node.left, el);
        else if (el > node.data) node.right = add(node.right, el); // equal → ignore (no duplicates)
        return node;
    }

    public boolean contains(int el) {
        TreeNode cur = root;
        while (cur != null) {
            if (el == cur.data) return true;
            cur = el < cur.data ? cur.left : cur.right;
        }
        return false;
    }

    public static void main(String[] args) {
        MyBST bst = new MyBST();
        for (int x : new int[]{10, 5, 16, 12, 6}) bst.add(x);
        System.out.println(bst.contains(12)); // true
        System.out.println(bst.contains(7));  // false
    }
}
```

### The real thing: JDK classes + programming to the interface
```java
import java.util.*;

public class CollectionsTour {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(); // swap for new LinkedList<>() — same API
        list.add(4);
        list.add(0, 8);
        System.out.println(list);                    // [8, 4]

        Set<String> emails = new HashSet<>();
        emails.add("a@x.com");
        System.out.println(emails.add("a@x.com"));   // false
        System.out.println(emails.contains("a@x.com")); // true

        Deque<Integer> stack = new ArrayDeque<>();   // preferred over legacy Stack
        stack.push(2); stack.push(3); stack.push(4);
        System.out.println(stack.pop());             // 4 (LIFO)

        Queue<Integer> queue = new ArrayDeque<>();
        queue.offer(2); queue.offer(3); queue.offer(4);
        System.out.println(queue.poll());            // 2 (FIFO)

        Map<String, Integer> roll = new HashMap<>();
        roll.put("Aditya", 1001);
        roll.putIfAbsent("Aditya", 105);             // ignored — key exists
        System.out.println(roll.get("Aditya"));      // 1001

        Set<Integer> sorted = new TreeSet<>(List.of(10, 5, 16, 12, 6));
        System.out.println(sorted);                  // [5, 6, 10, 12, 16]

        Collection<Integer> anyCollection = sorted;  // every List/Set/Queue is a Collection
        System.out.println(anyCollection.size());    // 5
    }
}
```

## Common Pitfalls / Tips
- **There's no "best" data structure.** Ask which operation happens most often and pick the structure that makes that one cheap.
- **Off-by-one in the direct-index set.** To store 0–100 you need length **101**.
- **The `arr[el] == el` check breaks for 0.** An empty `int[]` slot is also `0`, so `contains(0)` returns true even when 0 was never added. Use a `boolean[]` or a separate "occupied" flag.
- **`hashCode()` can be negative.** `-7 % 10 == -7`, which is an invalid index. Use `Math.floorMod(h, n)`; the JDK also mixes the hash bits.
- **Equal objects must have equal hash codes.** If you put your own classes in a `HashSet`/`HashMap`, override **both** `equals()` and `hashCode()`, or duplicates will slip through.
- **Insert ≠ replace.** Insert shifts and keeps the old element. Replace overwrites it. In Java these are `list.add(index, el)` and `list.set(index, el)`.
- **`HashMap.put` overwrites by default.** The video's "reject a duplicate key" behavior is `putIfAbsent`. `put` replaces the old value for an existing key.
- **`LinkedList` isn't automatically faster for inserts.** Re-linking is O(1), but *reaching* position i is O(n). In practice `ArrayList` is usually faster overall because of cache-friendly contiguous memory.
- **Avoid the legacy `Stack` class** (it extends the synchronized `Vector`). Use `Deque<E> stack = new ArrayDeque<>()` instead.
- **Declare variables by interface type** (`List`, `Set`, `Map`) so you can change the implementation later without touching the calling code.
- **`Map` is not a `Collection`.** You can't pass a `Map` where a `Collection` is expected. Use `map.keySet()`, `map.values()` or `map.entrySet()`.

## Review Questions
1. Why is "find the max" O(n) in an unsorted array but O(1) in a sorted one, and what do you pay for that speed-up?
2. What does a dynamic array do internally when you call `add` on a full array?
3. Why is inserting into the middle of a linked list cheaper than into an array? Why is `Node` usually a `private static` nested class?
4. How can a hash set answer `contains` in O(1)? What is chaining, and when do you resize?
5. How would you store strings (e.g. email IDs) in a hash-based set?
6. What is the difference between a stack and a queue? Give a real-world analogy for each.
7. How does a hash map differ from a hash set internally? Which part gets hashed?
8. Why does Java put `ArrayList`, `LinkedList`, `HashSet`, etc. under common interfaces, and why is `Map` outside `Collection`?

<details>
<summary>Answers</summary>

1. Unsorted, you have to scan every element. Sorted, the max is always at `arr[length - 1]`. The cost is that insertion becomes O(n), because you have to find the spot and shift the larger elements right.
2. It allocates a new array (the video uses double the capacity), copies the old elements into it, and then adds the new element. To the user the array never looks full.
3. You only re-link two references (`prev.next = node; node.next = old`), with no shifting, and the list never fills up. `Node` is an internal detail that only makes sense inside the list. Making it `private static` hides it from users and keeps it independent of any outer instance.
4. It stores each element at index `hash % capacity`, so `add` and `contains` go straight to one slot. Chaining keeps a linked list in each slot so that colliding values (e.g. 10, 100 and 1000 with capacity 10) can coexist. When the chains get long, you double the array and rehash every element.
5. Call `hashCode()` on the string to get an `int`, take it modulo the capacity (with `floorMod` to handle negative values), and store the string in that bucket. Use `equals()` to compare within the chain.
6. A stack is LIFO: push and pop at the same end, like a stack of plates. A queue is FIFO: add at the rear and remove from the front, like a line of people.
7. A set stores single values. A map stores `Node(key, value)` entries and hashes **only the key**, which is also what has to be unique. `get(key)` hashes the key to find the node and returns its value.
8. So that operations like `add` and `remove` have the same names everywhere (less to memorize, reuse through inheritance), and so you can program to the interface and let polymorphism choose the implementation. `Map` works with key-value pairs, while `Collection` works with single values, so it has its own hierarchy.
</details>

## Further Study
- `Iterable`, `Iterator`, and the methods declared in `Collection`
- `ArrayList` internals: growth factor (1.5× in the JDK, not 2×), amortized O(1) `add`
- `ArrayList` vs `LinkedList` performance in practice
- The `equals()`/`hashCode()` contract; `HashMap` internals (load factor 0.75, treeification of long chains in Java 8+)
- `HashSet` vs `LinkedHashSet` vs `TreeSet` (ordering guarantees)
- `HashMap` vs `LinkedHashMap` vs `TreeMap`; `SortedMap`/`NavigableMap`
- Red-black trees (self-balancing BSTs behind `TreeMap`/`TreeSet`)
- `Deque`, `ArrayDeque`, `PriorityQueue`, circular queues
- Why `Vector` and `Stack` are considered legacy
- Java Streams over collections (coming later in the series)

---

[← #28](28-generics-wildcards.md) · [Index](../../ReadMe.md) · [#30 →](30-iterable-iterator-for-each.md)
