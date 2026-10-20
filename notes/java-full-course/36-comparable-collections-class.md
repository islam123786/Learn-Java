# Comparable Interface in Java | Collections class | Comparing Objects | Java Full Course #36
> Source: https://youtu.be/uUo7AV02vJM · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 44:34. The video is in Hindi (these notes are in English). The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Java knows how to order primitives, wrappers and `String`s, but not your own classes. With a `Student(name, marks)`, should it compare by marks, by name, or both? Without that knowledge `Collections.sort(list)` doesn't compile, and `TreeSet`/`TreeMap` can't place elements.
- Implementing **`Comparable<T>`** gives a class its **natural ordering** through one method, `int compareTo(T other)`: **negative** → `this` comes first, **positive** → `this` comes after, **zero** → "equal".
- `this.marks - other.marks` sorts ascending; `other.marks - this.marks` sorts descending. Tie-breakers are chained: compare marks, and if equal, compare names with `String.compareTo`.
- **Danger of returning 0:** `TreeSet`/`TreeMap` use `compareTo`, not `equals`, so two different students with equal marks count as duplicates and one is silently dropped. Keep `compareTo` **consistent with `equals`** (`a.compareTo(b) == 0` ⇔ `a.equals(b)`).
- A class can have only **one** natural ordering. For other orderings (by name today, by marks tomorrow) use a **`Comparator`** (next video). **`Collections`** (with an *s*) is a utility class of static helpers: `sort`, `max`, `min`, `reverse`, `shuffle`, `swap`, `fill`, `binarySearch`, `frequency`, `unmodifiableList`, `emptyList`, …

## Key Concepts

**Natural ordering.** The "obvious" order of a type: numbers ascending, strings lexicographic (dictionary) order. Primitives and built-in classes already have it. *Why it matters:* sorting, `TreeSet`, `TreeMap`, `PriorityQueue`, `Collections.max/min` and `binarySearch` all rely on it.

**`Comparable<T>`.** A contract: "I know how to compare myself with another object of my own type." It has exactly one abstract method, `int compareTo(T o)`, so it's also a **functional interface** (relevant for lambdas later).

**The sign of `compareTo`.** Only the sign matters, not the size. `s1.compareTo(s2) < 0` → s1 before s2; `> 0` → s1 after s2; `== 0` → equal in ordering. Java itself doesn't know about "ascending" or "descending"; that comes from how you write the expression.

**Why `int`, not `boolean` or an enum.** There are three outcomes (less, equal, greater). A `boolean` only has two. An enum would work but is an object, so it's slower than a primitive `int`.

**Consistency with `equals`.** Sorted collections decide uniqueness with `compareTo`; hash collections use `equals`/`hashCode`. If they disagree, the same objects behave differently in a `TreeSet` and a `HashSet`.

**`Collection` vs `Collections`.** `Collection` is the root interface of lists, sets and queues. `Collections` is a final utility class with only static methods that operate on collections.

## Detailed Notes

### How does Java know what's "smaller"? [00:00]
- For primitives (`int`, `long`, …) and for `String`, smaller and larger are obvious: numbers have a natural order, strings have **lexicographic order**.
- Primitive values are stored directly, so the JVM compares them natively.

### The problem with custom classes [02:00]
- `class Student { int marks; String name; }`, with `s1 = new Student(95, "Aditya")` and `s2 = new Student(85, "Rohit")`.
- Which is smaller? Java can't tell whether you mean marks, name, or a combination.
- Storing objects is easy; **comparing** them is the problem.

### Where it breaks [03:46]
- `Collections.sort(list)` on a `List<Student>`: Java doesn't know how to sort, so it's an error.
- `TreeSet`/`TreeMap` can't store `Student`s either. They're BSTs, so every insert must compare with existing nodes to decide left or right.

### The `Comparable` interface [05:06]
- Java's solution. A class that implements it promises: "I know how to compare myself with another object of the same type."
- Its shape: `interface Comparable<T> { int compareTo(T o); }`. The interface doesn't compare anything itself; the implementing class supplies the logic.
- It has only one abstract method, which makes it a **functional interface** [06:52]. That matters when lambdas arrive.

### Demo: the compile error [07:18]
- `Student` with `name`, `marks`, a constructor, and nothing else.
- `List<Student> list = new ArrayList<>()`, add Aditya 95, Rohit 85, Rohan 56.
- `Collections.sort(list)` → compile error: *"The method sort(List&lt;T&gt;) in the type Collections is not applicable for the arguments (List&lt;Student&gt;)"*.
  - *Why exactly:* the signature is `<T extends Comparable<? super T>> void sort(List<T> list)`. `Student` doesn't satisfy the bound, so the call doesn't type-check.
- By contrast, a `List<Integer>` with 10, 5, 45 sorts fine to `[5, 10, 45]` [10:24], because `Integer` already implements `Comparable<Integer>`.

### Implementing `compareTo` [11:31]
- `class Student implements Comparable<Student>` and override `public int compareTo(Student other) { return this.marks - other.marks; }`. The error disappears.
- Meaning of the result [12:24]: negative → `this` before `other`; positive → `other` before `this`.
- Output [13:30] (printed with a for-each loop, since `toString()` isn't overridden): Rohan 56, Rohit 85, Aditya 95 (ascending by marks).
- **Descending** [14:24]: flip it, `return other.marks - this.marks;` → Aditya, Rohit, Rohan.
- *Tip:* prefer `Integer.compare(this.marks, other.marks)` to subtraction. Subtraction can **overflow** for large values of opposite signs and flip the sign. It's safe for small marks, but it's a bad habit.

### What Java does with the result [14:50]
- During sorting Java effectively calls `s1.compareTo(s2)`; inside it, `this` is `s1` and `other` is `s2`.
- **Case 1, `this.marks - other.marks`** [15:57]:
  - Aditya 10, Rohit 20: 10 - 20 = -10, negative → Aditya first. Ascending.
  - Swap the marks (Aditya 20, Rohit 10): 20 - 10 = +10, positive → Rohit first. Still ascending.
- **Case 2, `other.marks - this.marks`** [18:49]: the same two scenarios now always give descending order.
- **Conclusion** [20:09]: Java only looks at the sign. Negative → s1 before s2, positive → s1 after s2, zero → treated as equal (order between them doesn't matter for sorting).

### `TreeSet`/`TreeMap` call `compareTo` too [21:29]
- Inserting 7 into a tree whose root is 10: Java calls `compareTo` on the element's type (`Integer`'s) and goes left.
- For `Student`s, your `compareTo` decides left/right. All built-in classes (`String`, wrappers, …) implement `Comparable` directly or indirectly.

### Only one natural ordering [23:15]
- Why not `compareByMarks()`, `compareByName()`, …? Because `sort`, `TreeSet` etc. need **one** agreed method to call. So each class has exactly one natural ordering: its `compareTo`.

### Why `compareTo` returns `int` [24:35]
- Three outcomes are needed (smaller, equal, larger). `boolean` has two, which is why `equals` (only "same or not") returns `boolean`.
- An enum (`SMALLER`, `EQUAL`, `LARGER`) would work, but enums are objects, and a primitive `int` (often a simple subtraction) is faster.

### Tie-breaking: marks, then name [26:48]
- Add Monu with 85, the same marks as Rohit. With marks only, the order between them is whatever the list had (Rohit was inserted first, so Rohit stays first).
  - *Note:* this works because `Collections.sort` (`List.sort`) is **stable**: equal elements keep their relative order. That's guaranteed by the docs, not luck.
- Wanted: equal marks → order by name lexicographically. "Monu" < "Rohit" because 'M' < 'R'.
- A more complex `compareTo`: if marks differ, `return this.marks - other.marks;`, else `return this.name.compareTo(other.name);`. This reuses `String`'s own `compareTo`.
- Output: Rohan 56, Monu 85, Rohit 85, Aditya 95.

### The danger of returning zero [29:27]
- Zero means "**equal**", not just "similar". Some structures act on that.
- `TreeSet`/`TreeMap` use `compareTo` (not `equals`) and don't allow duplicates. If two **different** students compare as 0, the second is **not inserted**. Silent data loss.
- Why don't tree collections use `equals`? It only says yes or no. A BST needs to know smaller or larger to go left or right.
- `HashSet`/`HashMap` are different: the bucket comes from `hashCode()` and duplicates are detected with `equals()`, so `compareTo` isn't involved.

### The rule: consistent with `equals` [33:00]
- If `a.compareTo(b) == 0`, then `a.equals(b)` should be `true` (and vice versa). Your class is responsible for this.
- You *can* return 0; just be sure the two objects really are the same as far as your business logic is concerned.
- *Note:* the Javadoc calls this "consistent with equals" and *strongly recommends* it, but doesn't require it. `BigDecimal` is a famous exception: `new BigDecimal("1.0")` and `new BigDecimal("1.00")` are `compareTo`-equal but not `equals`.

### Demo: data loss in a `TreeSet` [34:59]
- `Student("Aditya", 95)` and `Student("Rohit", 95)`, comparing by marks only. Add both to a `TreeSet<Student>` → `size()` is **1**.
- Switch to the marks-then-name `compareTo` → size **2**. Two students who are both "Aditya, 95" still give 1, which is expected. Override `equals` (and `hashCode`) along the same lines.

### When to use `Comparable` (and when not) [36:59]
- Use it when your class has an **obvious** natural ordering. Marks then name felt natural here; someone else might prefer name then marks. Choose one.
- You can't write two `Comparable` implementations for one class. If the ordering isn't obvious, or you need different orderings in different places, use a **`Comparator`** (an external comparison strategy; covered after lambdas).

### The `Collections` utility class [38:19]
- `Collection` (interface, root of the hierarchy) ≠ `Collections` (utility class).
- A utility class is a bag of **static** methods, so there are no objects to create: `Collections.sort(list)`.
- Common methods [39:52]:
  - `sort(list)`: sort a list by natural ordering (or `sort(list, comparator)`).
  - `max(coll)` / `min(coll)`: largest/smallest element.
  - `fill(list, 0)` [40:18]: replace every element with the value.
  - `reverse(list)`, `shuffle(list)` (random order, like shuffling cards).
  - `swap(list, i, j)`: swap two positions. *Correction:* the video calls the arguments "two elements"; they are **indexes**.
  - `binarySearch(sortedList, key)`: O(log n) search, but the list **must be sorted** first. *Addition:* it returns the index, or a negative value `-(insertionPoint) - 1` when the key is absent.
  - `frequency(coll, x)`: how many times `x` appears.
- `unmodifiableList(list)` [42:04] (also `unmodifiableSet`, `unmodifiableMap`): a read-only list; modification attempts throw `UnsupportedOperationException`.
  - *Precise version:* it's a read-only **view**. If someone still holds the original list and changes it, the change shows through the view. For a true snapshot use `List.copyOf(list)`.
- `emptyList()` [42:58] (also `emptySet`, `emptyMap`): an immutable empty list. Use case: a method that finds no results should return an empty list instead of `null`, so callers never have to null-check. This comes up again with Spring Boot.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`. The first block intentionally doesn't compile.

### Without `Comparable`, `Collections.sort` doesn't compile
```java
// does not compile
import java.util.*;

public class SortWithoutComparable {
    static class Student {
        String name; int marks;
        Student(String name, int marks) { this.name = name; this.marks = marks; }
    }

    public static void main(String[] args) {
        List<Student> list = new ArrayList<>();
        list.add(new Student("Aditya", 95));
        Collections.sort(list); // error: no suitable method found for sort(List<Student>)
    }
}
```

### `Comparable` with a tie-breaker (marks, then name)
```java
import java.util.*;

public class ComparableStudents {
    static class Student implements Comparable<Student> {
        final String name;
        final int marks;
        Student(String name, int marks) { this.name = name; this.marks = marks; }

        @Override
        public int compareTo(Student other) {
            if (this.marks != other.marks) {
                return Integer.compare(this.marks, other.marks); // safer than this.marks - other.marks
            }
            return this.name.compareTo(other.name);              // tie → lexicographic name
        }

        @Override public String toString() { return name + ", " + marks; }
    }

    public static void main(String[] args) {
        List<Student> list = new ArrayList<>();
        list.add(new Student("Aditya", 95));
        list.add(new Student("Rohit", 85));
        list.add(new Student("Rohan", 56));
        list.add(new Student("Monu", 85));

        Collections.sort(list);
        for (Student s : list) System.out.println(s);
        // Rohan, 56
        // Monu, 85
        // Rohit, 85
        // Aditya, 95

        list.sort(Collections.reverseOrder());   // descending natural order
        System.out.println(list.get(0));         // Aditya, 95

        Student a = new Student("A", 10), b = new Student("B", 20);
        System.out.println(a.compareTo(b) < 0);  // true  (a comes first)
        System.out.println(b.compareTo(a) > 0);  // true  (b comes after)

        List<Integer> nums = new ArrayList<>(List.of(10, 5, 45));
        Collections.sort(nums);                  // Integer is already Comparable
        System.out.println(nums);                // [5, 10, 45]
    }
}
```

### The danger of returning 0: silent data loss in a `TreeSet`
```java
import java.util.*;

public class ZeroReturnDanger {
    // compareTo looks only at marks
    record ByMarks(String name, int marks) implements Comparable<ByMarks> {
        public int compareTo(ByMarks o) { return Integer.compare(marks, o.marks); }
    }

    // compareTo consistent with equals (records compare all fields in equals)
    record ByMarksThenName(String name, int marks) implements Comparable<ByMarksThenName> {
        public int compareTo(ByMarksThenName o) {
            int c = Integer.compare(marks, o.marks);
            return c != 0 ? c : name.compareTo(o.name);
        }
    }

    public static void main(String[] args) {
        Set<ByMarks> tree = new TreeSet<>();
        tree.add(new ByMarks("Aditya", 95));
        tree.add(new ByMarks("Rohit", 95));   // compareTo == 0 → treated as a duplicate
        System.out.println(tree.size());      // 1
        System.out.println(tree);             // [ByMarks[name=Aditya, marks=95]]

        Set<ByMarks> hash = new HashSet<>();  // uses equals/hashCode instead
        hash.add(new ByMarks("Aditya", 95));
        hash.add(new ByMarks("Rohit", 95));
        System.out.println(hash.size());      // 2  (same objects, different answer!)

        Set<ByMarksThenName> fixed = new TreeSet<>();
        fixed.add(new ByMarksThenName("Aditya", 95));
        fixed.add(new ByMarksThenName("Rohit", 95));
        fixed.add(new ByMarksThenName("Aditya", 95)); // truly equal → one copy
        System.out.println(fixed.size());     // 2
    }
}
```

### A tour of the `Collections` utility class
```java
import java.util.*;

public class CollectionsUtilityTour {
    public static void main(String[] args) {
        List<Integer> list = new ArrayList<>(List.of(4, 1, 5, 1, 3));
        System.out.println(Collections.max(list) + " " + Collections.min(list)); // 5 1
        System.out.println(Collections.frequency(list, 1));  // 2

        Collections.sort(list);
        System.out.println(list);                            // [1, 1, 3, 4, 5]
        System.out.println(Collections.binarySearch(list, 4)); // 3   (index; list must be sorted)
        System.out.println(Collections.binarySearch(list, 2)); // -3  (absent: -(insertionPoint) - 1)

        Collections.reverse(list);
        System.out.println(list);                            // [5, 4, 3, 1, 1]
        Collections.swap(list, 0, 4);                        // swap by INDEX
        System.out.println(list);                            // [1, 4, 3, 1, 5]

        List<Integer> deck = new ArrayList<>(List.of(1, 2, 3, 4, 5));
        Collections.shuffle(deck, new Random(42));           // seeded so the output is repeatable
        System.out.println(deck);                            // [2, 3, 4, 5, 1]  (with this seed on current JDKs)

        Collections.fill(deck, 0);
        System.out.println(deck);                            // [0, 0, 0, 0, 0]

        List<String> original = new ArrayList<>(List.of("a", "b"));
        List<String> readOnly = Collections.unmodifiableList(original);
        try {
            readOnly.add("c");
        } catch (UnsupportedOperationException e) {
            System.out.println("read-only");                 // read-only
        }
        original.add("c");                                   // it's a VIEW: changes show through
        System.out.println(readOnly);                        // [a, b, c]
        List<String> snapshot = List.copyOf(original);       // a real immutable copy
        original.add("d");
        System.out.println(snapshot);                        // [a, b, c]

        System.out.println(findStudents("nobody").size());   // 0  (no null to check)
    }

    static List<String> findStudents(String query) {
        if (query.equals("nobody")) return Collections.emptyList(); // better than returning null
        return List.of(query);
    }
}
```

## Common Pitfalls / Tips
- **Implement `Comparable<Student>`, not raw `Comparable`.** With the type argument, `compareTo` takes a `Student` and you don't need casts.
- **Avoid `a - b` in comparisons.** Use `Integer.compare(a, b)` (also `Long.compare`, `Double.compare`). Subtraction can overflow, and with `double` fields a cast to `int` truncates the difference.
- **Return 0 only for truly equal objects** if the type will go into a `TreeSet`/`TreeMap`, or you'll lose data silently. Keep `compareTo`, `equals` and `hashCode` aligned.
- **Only the sign of `compareTo` is meaningful.** Don't test `compareTo(...) == -1`; test `< 0`.
- **`Collections.binarySearch` on an unsorted list gives garbage.** Sort it first (with the same ordering).
- **`Collections.unmodifiableList` is a view, not a copy.** Use `List.copyOf` for a true immutable snapshot.
- **Return empty collections, not `null`.** `Collections.emptyList()` / `List.of()` make callers simpler and avoid `NullPointerException`s.
- **`Collections.sort` is stable.** Equal elements keep their original relative order, which you can rely on when sorting in multiple passes.

## Review Questions
1. Why can Java sort a `List<Integer>` but not a `List<Student>` out of the box?
2. What does `Comparable<T>` declare, and what do negative, positive and zero results of `compareTo` mean?
3. `compareTo` returns `this.marks - other.marks`. Does the list sort ascending or descending? What change reverses it?
4. Why does `compareTo` return an `int` instead of a `boolean` or an enum?
5. Write the logic for "sort by marks; if marks are equal, by name".
6. Two different students with the same marks are added to a `TreeSet`, but `size()` is 1. Why, and how do you fix it?
7. Why can a class have only one natural ordering, and what do you use when you need several orderings?
8. What's the difference between `Collection` and `Collections`? Name five methods of `Collections`.

<details>
<summary>Answers</summary>

1. `Integer` implements `Comparable<Integer>`, so it has a natural ordering. `Student` doesn't, so Java doesn't know whether to compare by marks, by name or both, and `Collections.sort`'s type bound (`T extends Comparable<? super T>`) rejects it at compile time.
2. One method, `int compareTo(T o)`. Negative: `this` comes before `o`. Positive: `this` comes after `o`. Zero: they're equal for ordering purposes.
3. Ascending. If `this` has fewer marks, the result is negative, so `this` comes first. Use `other.marks - this.marks` (or better `Integer.compare(other.marks, this.marks)`) for descending.
4. There are three possible outcomes (less, equal, greater). A `boolean` only has two. An enum could represent three but is an object and slower than a primitive `int`, which can often be computed with a single comparison or subtraction.
5. `if (this.marks != other.marks) return Integer.compare(this.marks, other.marks); return this.name.compareTo(other.name);`
6. `TreeSet` decides duplicates with `compareTo`, not `equals`. Comparing by marks only returns 0 for the two students, so the second is treated as a duplicate and not inserted. Add a tie-breaker (e.g. the name) so that `compareTo` returns 0 only for truly equal objects, consistent with `equals`.
7. Sorting methods and sorted collections need a single, agreed method to call, and that is `compareTo`. For alternative or context-specific orderings, pass a `Comparator` to `sort`, `TreeSet`, `PriorityQueue`, etc.
8. `Collection` is the root interface of lists, sets and queues. `Collections` is a utility class of static helper methods. Examples: `sort`, `max`, `min`, `reverse`, `shuffle`, `swap`, `fill`, `binarySearch`, `frequency`, `unmodifiableList`, `emptyList`.
</details>

## Further Study
- `Comparator` and lambda expressions (next video): `Comparator.comparing(...).thenComparing(...)`, `reversed()`
- The `Comparable` contract: antisymmetry, transitivity, consistency with `equals`
- `Arrays.sort` vs `Collections.sort`; TimSort and sort stability
- `List.of`, `List.copyOf`, `Collections.unmodifiable*` and immutability
- `Collections.synchronizedList`, `nCopies`, `addAll`, `disjoint`, `rotate`
- `BigDecimal.compareTo` vs `equals` as a real-world inconsistency

---

[← #35](35-queue-priorityqueue-heap.md) · [Index](../../ReadMe.md) · [#37 →](37-lambdas-functional-interfaces-comparator.md)
