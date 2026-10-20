# Java Queue Interface Deep Dive | PriorityQueue & Heap Explained | Java Full Course #35
> Source: https://youtu.be/zj0-JIc5oPw · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 77:24. The video is in Hindi (these notes are in English). The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within a minute or two.

## TL;DR
- A **queue** is FIFO (enter at the rear, leave from the front); a **stack** is LIFO (enter and leave at the same end). Both can be built on an array or a linked list, and both need **O(1)** insert and remove. An array queue needs a **circular** buffer (indexes wrap with `% n`) plus resizing.
- Hierarchy: `Collection` → **`Queue`** → **`Deque`** (double-ended queue) → **`ArrayDeque`** (array-based) and **`LinkedList`** (which also implements `List`). `Queue` → **`PriorityQueue`**.
- Java has no modern stack class: a stack is just a deque used from one end. Use **`ArrayDeque`** with `push`/`pop`/`peek` for stacks and `offer`/`poll`/`peek` for queues. The legacy `Stack` class is slow; avoid it. The docs recommend `ArrayDeque` over `LinkedList` (contiguous memory is cache-friendly). `ArrayDeque` rejects `null`.
- Each operation comes in two flavors: **throws** (`add`, `remove`, `element`) vs **returns a special value** (`offer`, `poll`, `peek` → `false`/`null`). `Deque` doubles them with `First`/`Last` versions.
- **`PriorityQueue`** removes the **highest-priority** element, not the oldest. By default that's the smallest (natural ordering, a **min-heap**); pass a comparator for a max-heap. Internally it's a **binary heap stored in an array** (children of `i` at `2i+1`, `2i+2`; parent at `(i-1)/2`). `offer` sifts up and `poll` sifts down, both **O(log n)**. The array is not sorted.

## Key Concepts

**FIFO vs LIFO.** Queue: First In, First Out, like a line of people. Stack: Last In, First Out, like a stack of plates. *Why it matters:* BFS, scheduling and buffering use queues; undo, backtracking and expression parsing use stacks.

**Circular array queue.** Keep `front` and `rear` indexes into a fixed array. When `rear` reaches the end, wrap to index 0 with `(rear + 1) % n` to reuse freed slots. When `rear`'s next slot is `front`, the array is full, so either report "full" or resize. *Why it matters:* without wrapping, slots freed at the front would never be reused.

**Linked-list queue/stack.** Nodes are created on demand, so it never fills up and needs no wrap-around. A stack on a singly linked list pushes and pops at the **head**.

**Deque.** A double-ended queue: insert and remove at both ends. One structure gives you a queue (add at one end, remove at the other), a deque, or a stack (one end only).

**Behavioral contract.** Nothing stops you from calling `offerLast` on a "stack" or `remove(Object)` in the middle of a "queue". Keeping FIFO/LIFO is your job as the developer.

**Priority queue.** Elements come out by priority, not insertion order. *Why it matters:* job scheduling, Dijkstra's algorithm, "top K" problems, merging sorted streams.

**Binary heap.** A **complete binary tree** (filled level by level, left to right) where every parent is ≤ its children (min-heap) or ≥ them (max-heap). Because the tree is complete, it fits in an array with no gaps and no node objects.

**Up-heapify / down-heapify.** Insert at the end and swap up while smaller than the parent (sift up). Remove the root by moving the last element to the root and swapping down with the smaller child (sift down). Both walk one root-to-leaf path: O(log n).

## Detailed Notes

### Two ways to implement a queue or stack [00:00]
- Both can be built on an **array** or a **linked list**.
- Requirement either way: insertion and deletion in **constant time**.

### Array queue: `front` and `rear` [01:39]
- FIFO: elements enter at the **rear** and leave from the **front**. Insert 4, 5, 6, 7 and 4 comes out first.
- Start with `front = rear = -1`.
- `offer(x)` (also called **enqueue**): `arr[++rear] = x`. On the very first insert, also set `front = 0`.
- `poll()` (also called **dequeue**): `int x = arr[front]; front++; return x;`. The old value can stay in the array; it'll be overwritten later.

### Circular queue [05:23]
- After a few polls the front slots are free, but `rear` has reached the end. Reuse them with `rear = (rear + 1) % n`: from index 5 in a size-6 array, `6 % 6 = 0` wraps to the start.
- The queue is **full** when the slot after `rear` is `front`.

### Resizing [06:50]
- When full, either refuse (a bounded queue) or allocate a bigger array (e.g. double) and copy.
- Two ways to copy: lay elements out from `front` to `rear` starting at index 0, or keep the wrapped layout and move the front part to the end of the new array, leaving the gap in the middle. Java's choice comes later.

### Linked-list queue [08:29]
- `Node { int data; Node next; }`. The first node is both `front` and `rear`.
- `offer`: create a node, link `rear.next` to it, move `rear`.
- `poll`: `int x = front.data; front = front.next; return x;`.
- Advantages: never "full", no circular logic, no wasted nodes (removed nodes can be garbage-collected). Time complexity is the same as the array version: O(1) per insert/remove.

### Stack: LIFO [11:35]
- Push 5, 10, 3, 4, 6 and the first thing out is 6. Entry and exit are at the **same** end.

### Array stack [12:37]
- One pointer `top`, starting at `-1`.
- `push(x)`: `arr[++top] = x`. `pop()`: `int x = arr[top--]; return x;`.
- No circular logic needed, because both operations use the same end. When full, resize (double and copy).

### Linked-list stack [15:18]
- If you push by appending at the tail of a **singly** linked list, `pop` can't move `top` back (a node doesn't know its predecessor).
- Fix: push at the **head**. `Node temp = new Node(x); temp.next = top; top = temp;`. Pop: `x = top.data; top = top.next;`.
- Java's `LinkedList` is doubly linked, so either end works.

### The `Queue` hierarchy [17:35]
- `Iterable` → `Collection` → **`Queue`** (interface) → **`Deque`** (interface, "double-ended queue": insert and remove at both ends) → **`ArrayDeque`** (class, the array implementation).
- A deque can act as a single-ended queue: just add at one end and remove at the other.

### `LinkedList` is also a `Deque` [19:51]
- `LinkedList` implements both `List` and `Deque`. You can use it as a list or as a queue: it's the linked-list implementation of a queue, while `ArrayDeque` is the array implementation.

### Where is the stack? [20:53]
- The old `Stack` class (under `List`, extends `Vector`) is legacy. For stacks Java uses **`ArrayDeque`** too.
- A stack is a deque with one end "walled off". Use only the front: `offerFirst` to insert, `pollFirst` to remove. Ignore `offerLast`/`pollLast`.
- So `ArrayDeque` can be used three ways: a queue, a double-ended queue, or a stack. `LinkedList` can too.

### `push`/`pop` wrappers [24:12]
- Because people are used to stack names, `Deque` also has `push`, `pop` and `peek`. They are wrappers over the front-end methods. The names also remind you that you're using it as a stack.
- *Correction:* the video says `push` = `offerFirst` and `pop` = `pollFirst`. According to the `Deque` Javadoc, **`push` = `addFirst`** and **`pop` = `removeFirst`**, the *throwing* versions. On an empty deque `pop()` throws `NoSuchElementException`, while `pollFirst()` returns `null`. `peek()` = `peekFirst()` (returns `null` when empty).

### Inside `ArrayDeque` [25:26]
- An internal array plus two indexes (called `head` and `tail` in the JDK; front/rear in the video). Insert 5: rear moves and stores it; front points at 0. Insert 7, 9, 10, 11 the same way.
- **Growth** [27:31]: when full, allocate a bigger array and copy. The video says it grows by 50% (5 → 7.5 → 7).
  - *Precise version:* `ArrayDeque` (JDK 9+) **doubles** the capacity while it's small (under 64) and grows by **50%** after that. The default initial capacity is 16 (plus one spare slot).
- **It is circular** [28:33]: after two polls, front has moved to index 2. New elements wrap around with `(i + 1) % n` into indexes 0 and 1. When rear's next slot is front, it's full again.
- **Copy on resize** [29:59]: Java keeps the wrapped layout. The part at the start of the array (`10, 12`) stays at the start, and the front part (`9, 10, 11`) moves to the **end** of the new array, so the free space opens up between rear and front.
- Being a deque, you can also insert at the front (one slot before `front`) or remove from the rear.

### Inside `LinkedList` as a queue [31:14]
- Same as the linked-list queue above, but doubly linked, so both ends support insertion and deletion.
- A "stack" built on `ArrayDeque` or `LinkedList` is the same internal structure. Java doesn't know you're using it as a stack; you only use one end.

### `ArrayDeque` vs `LinkedList` [33:18]
- The linked list looks more elegant (no wrap-around, never full), and both are O(1) per operation.
- But the Java docs recommend **`ArrayDeque`** in almost all cases. Arrays are **contiguous**, so they are **cache-friendly**: when the CPU loads one element it also brings nearby ones into cache, and queue/stack operations always touch the neighbouring slot. Linked-list nodes are scattered across the heap (address 1000, 3000, …), and each node also costs extra memory.
- **`null`** [35:47]: `ArrayDeque` **rejects** `null` elements (throws `NullPointerException`); `LinkedList` allows them. Avoid `null` elements anyway, because `poll`/`peek` use `null` to mean "empty".

### Why not the `Stack` class [36:37]
- `Stack` is legacy and slow (it extends `Vector`, whose methods are all synchronized). Use `ArrayDeque` and its `push`/`pop`/`peek`.

### Queue methods in code [37:51]
- `Queue<Integer> q = new ArrayDeque<>();`. Declaring it as `Queue` limits you to queue methods, which is what you want for a plain FIFO. (Declare `Deque` or `ArrayDeque` to reach the double-ended methods.)
- **Insert** [39:43]: `add(1)` vs `offer(2)`. If insertion fails, `add` **throws** and `offer` returns **`false`**.
- **Examine** [41:22]: `peek()` vs `element()` show the front without removing it. On an empty queue, `peek()` returns `null` and `element()` throws **`NoSuchElementException`**.
- **Remove** [42:49]: `remove()` (throws if empty) vs `poll()` (returns `null` if empty).
- The summary table [43:26]:

| | Throws exception | Returns special value |
|---|---|---|
| Insert | `add(e)` | `offer(e)` → `false` |
| Remove | `remove()` | `poll()` → `null` |
| Examine | `element()` | `peek()` → `null` |

- *Note:* for unbounded queues like `ArrayDeque`, `LinkedList` and `PriorityQueue`, `add` and `offer` both effectively always succeed. The difference matters for **capacity-restricted** queues (e.g. `ArrayBlockingQueue`), where `add` throws `IllegalStateException` when full.

### `Deque` methods [43:51]
- Every method splits into a `First` and a `Last` version:

| | Throws (first / last) | Special value (first / last) |
|---|---|---|
| Insert | `addFirst` / `addLast` | `offerFirst` / `offerLast` |
| Remove | `removeFirst` / `removeLast` | `pollFirst` / `pollLast` |
| Examine | `getFirst` / `getLast` | `peekFirst` / `peekLast` |

- All of these exist on both `ArrayDeque` and `LinkedList`.
- **As a stack** [45:18]: use only one end (`offerFirst`/`pollFirst`/`peekFirst`, or the throwing versions), or the convenience names **`push`, `pop`, `peek`** [46:20]. Prefer `push`/`pop`/`peek` because the names say "stack".

### Behavioral contracts [46:57]
- `Queue` inherits everything from `Collection`. `add` appends at the rear, which fits. But `remove(Object o)` can delete from the **middle**, which breaks FIFO.
- Likewise you can call `offerLast` on a deque you're treating as a stack.
- Java doesn't prevent this. FIFO for queues and LIFO for stacks are **behavioral contracts** that you, the developer, must respect. Java exposes all the methods for the sake of a clean inheritance/polymorphism design.

### `PriorityQueue` [49:51]
- A class that implements `Queue` directly (not via `Deque`).
- `poll()` removes the element with the **highest priority**, not the oldest. There is no FIFO, front or rear.
- Priority comes from **natural ordering** by default: the **smallest** element has the highest priority. With 5, 10, 1, 7, 3: `poll()` → 1, then 3, then 5, …
- It can be reversed so the largest comes out first (10, 7, 5, …).
- **Use case** [51:55]: scheduling and job-scheduling problems, where some jobs are more important.

### Heap data structure [52:45]
- `PriorityQueue` is implemented with a **binary heap** (unrelated to heap *memory*; it just shares the name).
- **Complete binary tree** [53:22]: each node has at most two children, and the tree is filled **level by level, left to right**. You never start a new level before the current one is full.
- **Min-heap property**: every parent is smaller than its children. Example: root 10, children 20 and 30; 20's children 40 and 50.
- *Correction:* the transcript says the tree is "implemented using a stack". It's implemented using an **array**.

### Array representation [55:38]
- Because the tree is complete, store it level by level in an array: `[10, 20, 30, 40, 50]`. No `Node` objects with `left`/`right` pointers.
- For a node at index `i`: **left child = `2i + 1`**, **right child = `2i + 2`**. Check: 0 → 1, 2; 1 → 3, 4.
- **A heap is not a sorted array** [57:55]. `[10, 30, 20, 40, 50]` is also a valid min-heap. The heap only promises that `poll` returns the smallest, then the next smallest, and so on.
- **Min-heap vs max-heap** [58:32]: in a max-heap every parent is larger than its children. `PriorityQueue` is a min-heap by default; a comparator in the constructor turns it into a max-heap.

### Insert: up-heapify (sift up) [60:11]
- Heap `[10, 30, 20, 40, 50]`, `offer(5)`:
  1. Put 5 at the next free spot (the end of the array, index 5).
  2. Compare with its parent (20). 5 is smaller → swap.
  3. Compare with the new parent (10). Smaller → swap. 5 is now the root; stop.
- Result: `[5, 30, 10, 40, 50, 20]`.
- **Parent formula** [64:07]: parent of `i` = **`floor((i - 1) / 2)`**. It works for both children: (5 - 1) / 2 = 2 and (6 - 1) / 2 = 2.5 → 2. (The video says "ceiling" once and then corrects it to floor. Floor is right, and in Java integer division `(i - 1) / 2` already does it.)
- `offer(8)`: goes to index 6, parent index 2 (10) → swap; then parent 5 → stop. Result: `[5, 30, 8, 40, 50, 20, 10]`.

### Remove: down-heapify (sift down) [69:05]
- `poll()` must return the root (5), but you can't just delete it: its children would be orphaned.
- Steps:
  1. Swap the root with the **last** element (10), then remove the last slot and return 5.
  2. Sift the new root (10) down: compare with its children (30 and 8). 8 is smaller → swap.
  3. 10's only child is now 20 → 10 is smaller; stop.
- Result: `[8, 30, 10, 40, 50, 20]`.
- *Precise version:* the video compares with the left child, then the right. The correct rule is to swap with the **smaller** of the two children (for a min-heap); otherwise you can break the heap property.
- Summary: `offer` → up-heapify, `poll` → down-heapify.

### Complexity [72:11]
- Both walk at most the height of a complete binary tree, so `offer` and `poll` are **O(log n)**. `peek` is O(1) (it's `arr[0]`).
- *Addition:* `remove(Object)` and `contains` are O(n), because the heap isn't searchable.

### `PriorityQueue` in code [73:38]
- `PriorityQueue<Integer> pq = new PriorityQueue<>();` offer 10, 30, 20, 40, 50 → polling gives 10, 20, 30, 40, 50.
- **Max-heap** [75:05]: `new PriorityQueue<>((a, b) -> b - a)` → 50, 40, 30, 20, 10. The lambda is a **comparator**; lambdas are explained in video #37.
  - *Tip:* prefer `Comparator.reverseOrder()` (or `Collections.reverseOrder()`). `b - a` can overflow for large values of opposite sign (e.g. `Integer.MIN_VALUE`) and give the wrong order.
- **Methods** [76:19]: the usual queue pairs, `add`/`offer`, `remove`/`poll`, `element`/`peek`. The second of each pair is the safe one.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Circular array queue with resizing
```java
import java.util.NoSuchElementException;

public class CircularArrayQueue {
    private int[] arr;
    private int front = 0; // index of the oldest element
    private int size = 0;

    public CircularArrayQueue(int capacity) { arr = new int[capacity]; }

    public void offer(int x) {
        if (size == arr.length) grow();
        int rear = (front + size) % arr.length; // wrap around
        arr[rear] = x;
        size++;
    }

    public int poll() {
        if (size == 0) throw new NoSuchElementException("queue empty");
        int x = arr[front];
        front = (front + 1) % arr.length;
        size--;
        return x;
    }

    private void grow() {
        int[] bigger = new int[arr.length * 2];
        for (int i = 0; i < size; i++) bigger[i] = arr[(front + i) % arr.length]; // unwrap in FIFO order
        arr = bigger;
        front = 0;
    }

    public int capacity() { return arr.length; }

    public static void main(String[] args) {
        CircularArrayQueue q = new CircularArrayQueue(4);
        q.offer(4); q.offer(5); q.offer(6); q.offer(7);
        System.out.println(q.poll() + " " + q.poll()); // 4 5
        q.offer(9); q.offer(10);                      // wrap into freed slots 0 and 1
        System.out.println(q.capacity());             // 4
        q.offer(11);                                  // full → grow
        System.out.println(q.capacity());             // 8
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(q.poll()).append(' ');
        System.out.println(sb.toString().trim());     // 6 7 9 10 11
    }
}
```

### Linked-list queue and linked-list stack (push at the head)
```java
public class LinkedQueueAndStack {
    private static class Node {
        int data;
        Node next;
        Node(int data) { this.data = data; }
    }

    // ----- queue: insert at rear, remove at front -----
    static class LinkedQueue {
        private Node front, rear;
        void offer(int x) {
            Node n = new Node(x);
            if (rear == null) { front = rear = n; return; }
            rear.next = n;
            rear = n;
        }
        Integer poll() {
            if (front == null) return null;
            int x = front.data;
            front = front.next;
            if (front == null) rear = null;
            return x;
        }
    }

    // ----- stack: push and pop at the head of a singly linked list -----
    static class LinkedStack {
        private Node top;
        void push(int x) {
            Node temp = new Node(x);
            temp.next = top;
            top = temp;
        }
        Integer pop() {
            if (top == null) return null;
            int x = top.data;
            top = top.next;
            return x;
        }
    }

    public static void main(String[] args) {
        LinkedQueue q = new LinkedQueue();
        for (int x : new int[]{2, 7, 9, 3, 5}) q.offer(x);
        System.out.println(q.poll() + " " + q.poll() + " " + q.poll()); // 2 7 9

        LinkedStack s = new LinkedStack();
        for (int x : new int[]{2, 3, 4}) s.push(x);
        System.out.println(s.pop() + " " + s.pop() + " " + s.pop());    // 4 3 2
        System.out.println(s.pop());                                     // null
    }
}
```

### `Queue` methods: throwing vs special-value versions
```java
import java.util.*;

public class QueueMethodsDemo {
    public static void main(String[] args) {
        Queue<Integer> q = new ArrayDeque<>();
        q.add(1);
        q.offer(2);
        q.offer(3);
        System.out.println(q);           // [1, 2, 3]
        System.out.println(q.peek());    // 1  (front, not removed)
        System.out.println(q.element()); // 1
        System.out.println(q.poll());    // 1
        System.out.println(q.remove());  // 2
        System.out.println(q);           // [3]

        Queue<Integer> empty = new ArrayDeque<>();
        System.out.println(empty.peek()); // null
        System.out.println(empty.poll()); // null
        try {
            empty.element();
        } catch (NoSuchElementException e) {
            System.out.println("element() threw NoSuchElementException"); // element() threw NoSuchElementException
        }
        try {
            q.offer(null);
        } catch (NullPointerException e) {
            System.out.println("ArrayDeque rejects null"); // ArrayDeque rejects null
        }

        Queue<Integer> linked = new LinkedList<>();
        linked.offer(null);                // LinkedList allows null (avoid it anyway)
        System.out.println(linked.size()); // 1
    }
}
```

### `ArrayDeque` as a deque and as a stack
```java
import java.util.*;

public class DequeAsStack {
    public static void main(String[] args) {
        Deque<Integer> dq = new ArrayDeque<>();
        dq.offerLast(2);
        dq.offerLast(3);
        dq.offerFirst(1);
        System.out.println(dq);                  // [1, 2, 3]
        System.out.println(dq.peekFirst() + " " + dq.peekLast()); // 1 3
        System.out.println(dq.pollLast());       // 3
        System.out.println(dq);                  // [1, 2]

        Deque<Integer> stack = new ArrayDeque<>(); // preferred over java.util.Stack
        stack.push(5); stack.push(10); stack.push(3);
        System.out.println(stack);               // [3, 10, 5]  (top is first)
        System.out.println(stack.peek());        // 3
        System.out.println(stack.pop());         // 3
        System.out.println(stack.pop());         // 10
        System.out.println(stack.pop());         // 5
        System.out.println(stack.pollFirst());   // null (empty, safe)
        try {
            stack.pop();                         // pop == removeFirst → throws when empty
        } catch (NoSuchElementException e) {
            System.out.println("pop() on empty stack throws"); // pop() on empty stack throws
        }
    }
}
```

### `PriorityQueue`: min-heap, max-heap, and the internal array
```java
import java.util.*;

public class PriorityQueueDemo {
    public static void main(String[] args) {
        PriorityQueue<Integer> pq = new PriorityQueue<>();      // min-heap
        for (int x : new int[]{10, 30, 20, 40, 50}) pq.offer(x);
        pq.offer(5);
        pq.offer(8);
        System.out.println(pq);          // [5, 30, 8, 40, 50, 20, 10]  (heap array, NOT sorted)
        System.out.println(pq.peek());   // 5
        System.out.println(pq.poll());   // 5
        System.out.println(pq);          // [8, 30, 10, 40, 50, 20]

        StringBuilder sb = new StringBuilder();
        while (!pq.isEmpty()) sb.append(pq.poll()).append(' ');
        System.out.println(sb.toString().trim()); // 8 10 20 30 40 50

        PriorityQueue<Integer> maxPq = new PriorityQueue<>(Comparator.reverseOrder()); // max-heap
        maxPq.addAll(List.of(10, 30, 20, 40, 50));
        sb.setLength(0);
        while (!maxPq.isEmpty()) sb.append(maxPq.poll()).append(' ');
        System.out.println(sb.toString().trim()); // 50 40 30 20 10

        // Why not (a, b) -> b - a ? It overflows for extreme values:
        Comparator<Integer> risky = (a, b) -> b - a;
        System.out.println(risky.compare(Integer.MIN_VALUE, 1) < 0); // true  (wrongly says MIN_VALUE comes first)
    }
}
```

### A min-heap from scratch (up-heapify and down-heapify)
```java
import java.util.Arrays;

public class MinHeapFromScratch {
    private int[] a = new int[4];
    private int size;

    public void offer(int x) {
        if (size == a.length) a = Arrays.copyOf(a, a.length * 2);
        a[size] = x;                          // 1. insert at the end
        int i = size++;
        while (i > 0) {                       // 2. up-heapify
            int parent = (i - 1) / 2;
            if (a[i] >= a[parent]) break;
            swap(i, parent);
            i = parent;
        }
    }

    public int poll() {
        if (size == 0) throw new IllegalStateException("empty");
        int top = a[0];
        a[0] = a[--size];                     // 1. move last element to the root
        int i = 0;
        while (true) {                        // 2. down-heapify
            int left = 2 * i + 1, right = 2 * i + 2, smallest = i;
            if (left < size && a[left] < a[smallest]) smallest = left;
            if (right < size && a[right] < a[smallest]) smallest = right; // pick the smaller child
            if (smallest == i) break;
            swap(i, smallest);
            i = smallest;
        }
        return top;
    }

    private void swap(int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }

    @Override public String toString() { return Arrays.toString(Arrays.copyOf(a, size)); }

    public static void main(String[] args) {
        MinHeapFromScratch h = new MinHeapFromScratch();
        for (int x : new int[]{10, 30, 20, 40, 50}) h.offer(x);
        System.out.println(h);        // [10, 30, 20, 40, 50]
        h.offer(5);
        System.out.println(h);        // [5, 30, 10, 40, 50, 20]
        h.offer(8);
        System.out.println(h);        // [5, 30, 8, 40, 50, 20, 10]
        System.out.println(h.poll()); // 5
        System.out.println(h);        // [8, 30, 10, 40, 50, 20]
    }
}
```

## Common Pitfalls / Tips
- **Use `ArrayDeque` for both stacks and queues.** Avoid `java.util.Stack` (synchronized, extends `Vector`, and it even iterates bottom-to-top). Use `LinkedList` only if you need `null` elements or `List` operations as well.
- **Know which methods throw.** `add`/`remove`/`element` (and `push`/`pop`/`getFirst`) throw; `offer`/`poll`/`peek` return `false`/`null`. Don't unbox a possibly-`null` `poll()` result into an `int`.
- **Don't store `null` in queues.** `ArrayDeque` and `PriorityQueue` reject it, and even where allowed it collides with "empty" from `poll`/`peek`.
- **Don't break FIFO/LIFO** with `remove(Object)`, `offerLast` on a stack, etc. The compiler won't stop you.
- **Printing or iterating a `PriorityQueue` doesn't give sorted order.** `toString()` and `for-each` show the heap array. Only repeated `poll()` comes out in priority order.
- **Prefer `Comparator.reverseOrder()` over `(a, b) -> b - a`.** Subtraction can overflow. Use `Integer.compare(b, a)` if you write it by hand.
- **Elements of a `PriorityQueue` must be comparable** (implement `Comparable`) or you must supply a `Comparator`, otherwise you get a `ClassCastException`.
- **Mutating an element that's already inside a `PriorityQueue` doesn't re-heapify it.** Remove it, change it, and re-insert it.
- **`PriorityQueue.remove(Object)` and `contains` are O(n).**

## Review Questions
1. Why does an array-based queue need a circular buffer, but an array-based stack doesn't?
2. How do you implement a stack on a singly linked list so that both push and pop are O(1)?
3. What are the roles of `Queue`, `Deque`, `ArrayDeque` and `LinkedList`? How can one class serve as a queue, a deque and a stack?
4. Why do the Java docs recommend `ArrayDeque` over `LinkedList` and over `Stack`?
5. What's the difference between `offer` and `add`, `poll` and `remove`, `peek` and `element`?
6. What does `PriorityQueue.poll()` return, and how do you turn the default into a max-heap?
7. How is a binary heap stored in an array? Give the child and parent index formulas.
8. Walk through `offer(5)` on the min-heap `[10, 30, 20, 40, 50]`, and explain why `offer` and `poll` are O(log n).

<details>
<summary>Answers</summary>

1. A queue removes from the front and inserts at the rear, so both indexes keep moving forward. Without wrapping (`% n`), the slots freed at the front would never be reused. A stack pushes and pops at the same end, so the used part is always `0..top`.
2. Push and pop at the **head**: `push` creates a node whose `next` is the current `top`, then sets `top` to it; `pop` returns `top.data` and sets `top = top.next`. Appending at the tail would make `pop` O(n), because a node can't step back to its predecessor.
3. `Queue` is the FIFO interface. `Deque` extends it with insert/remove at both ends. `ArrayDeque` (array-based) and `LinkedList` (doubly linked; also a `List`) implement `Deque`. Insert at one end and remove at the other for a queue, use both ends for a deque, or use one end only (`push`/`pop`/`peek`) for a stack.
4. `ArrayDeque` keeps elements in contiguous memory, which is cache-friendly and avoids a node object per element, so it's faster than `LinkedList` in practice. `Stack` is a legacy class built on synchronized `Vector`, so it pays locking costs for nothing.
5. They do the same job, but `add`, `remove` and `element` throw an exception on failure (full or empty), while `offer` returns `false` and `poll`/`peek` return `null`.
6. It returns and removes the highest-priority element: by default the smallest by natural ordering (min-heap). Pass a comparator, e.g. `new PriorityQueue<>(Comparator.reverseOrder())`, for a max-heap.
7. Level by level, left to right, in one array (possible because the tree is complete). For index `i`: left child `2i + 1`, right child `2i + 2`, parent `(i - 1) / 2` (floor).
8. Put 5 at index 5. Its parent is index 2 (20); 5 < 20 → swap. New parent is index 0 (10); 5 < 10 → swap. 5 is now the root: `[5, 30, 10, 40, 50, 20]`. Each step moves one level up (and `poll` moves one level down), and a complete binary tree with n nodes has about log₂ n levels, so both are O(log n).
</details>

## Further Study
- `BlockingQueue` implementations (`ArrayBlockingQueue`, `LinkedBlockingQueue`) and the producer-consumer pattern
- `ConcurrentLinkedQueue`, `ConcurrentLinkedDeque`, `PriorityBlockingQueue`
- Heap sort and building a heap in O(n) (`PriorityQueue(Collection)` uses heapify)
- Classic heap problems: top-K elements, K-way merge, running median (two heaps), Dijkstra's shortest path
- BFS with a queue; monotonic stack and monotonic deque problems
- `Comparator` and lambdas (videos #36–#37), `Comparator.comparing(...)`
- CPU caches and locality of reference (why arrays beat linked lists)

---

[← #34](34-set-map-methods-special-maps.md) · [Index](../../ReadMe.md) · [#36 →](36-comparable-collections-class.md)
