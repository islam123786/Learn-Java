# 37 · Lambdas, Functional Interfaces & `Comparator`
> Full notes: [37-lambdas-functional-interfaces-comparator.md](../37-lambdas-functional-interfaces-comparator.md) · [▶ Video](https://youtu.be/0V-w2BywAM0)

## Java is object-centric
- **Everything in Java revolves around objects.** Fields are characteristics, methods are behavior.
- To run any behavior you create a class, put the method in it, create an object and call the method on it. Behavior always rides **inside an object**.
- So objects are first-class and behaviors are **second-class citizens**. That's fine most of the time.

## When you only need behavior
- Sometimes the object is irrelevant and you only want to pass **a piece of logic** ("how to do something") to a method.
- Classic Java can't do that. A method argument can only be a **primitive** or an **object reference**, never a function.
- So you must wrap the logic in a class, create an object and pass the object. For tiny logic that's very verbose.
- Java's answer is **functional interfaces + lambdas**, the start of functional programming in Java.

---

## The example: sorting students
```java
class Student {
    String name;
    int rollNumber;
    int marks;
}
```
- To sort a `List<Student>`, Java has to know the **criterion**: name, roll number or marks?

## Option 1: `Comparable`
- The class implements `Comparable<Student>` and defines its **natural ordering** in `compareTo(Student other)`.
- `Collections.sort(list)` then uses it.

```java
import java.util.*;

class Student implements Comparable<Student> {
    String name; int rollNumber; int marks;

    Student(String name, int rollNumber, int marks) {
        this.name = name; this.rollNumber = rollNumber; this.marks = marks;
    }

    @Override
    public int compareTo(Student other) {
        return Integer.compare(this.marks, other.marks);   // by marks, ascending
    }

    @Override
    public String toString() { return name + "(" + marks + ")"; }
}

public class ComparableDemo {
    public static void main(String[] args) {
        List<Student> list = new ArrayList<>(List.of(
                new Student("Aditya", 103, 89),
                new Student("Rohit", 101, 98),
                new Student("Rohan", 104, 85),
                new Student("Sonu", 102, 93)));

        Collections.sort(list);            // uses compareTo
        System.out.println(list);          // [Rohan(85), Aditya(89), Sonu(93), Rohit(98)]
    }
}
```

### The challenges with `Comparable`
- **Only one ordering.** A class has one `compareTo`. In a college app the teacher wants students by marks, the admin office by name, someone else by roll number. One method can't serve them all.
- **The logic lives inside the class.** `Student` is now coupled to one sort order, and changing it means editing the class.
- **You need access to the class.** You can't add `Comparable` to a class you don't own (library code).

## Option 2: `Comparator`
- Keep the comparison logic **outside** the class. `Student` stays lightweight; a separate object knows how to compare two students.
```java
interface Comparator<T> {
    int compare(T o1, T o2);   // negative: o1 first, positive: o2 first, 0: equal
}
```
- **How it's implemented:** write one class per ordering that `implements Comparator<Student>` and overrides `compare`. Then pass an object of it to the overloaded **`Collections.sort(list, comparator)`**.
- **How `sort` uses it:** whenever the sorting algorithm has to decide whether `s1` or `s2` comes first, it calls `comparator.compare(s1, s2)` and looks at the sign of the result.

```java
import java.util.*;

class Student {                          // no Comparable needed
    String name; int rollNumber; int marks;

    Student(String name, int rollNumber, int marks) {
        this.name = name; this.rollNumber = rollNumber; this.marks = marks;
    }

    @Override
    public String toString() { return name + "(" + rollNumber + ", " + marks + ")"; }
}

class SortByName implements Comparator<Student> {
    public int compare(Student s1, Student s2) { return s1.name.compareTo(s2.name); }
}

class SortByRollNumber implements Comparator<Student> {
    public int compare(Student s1, Student s2) { return Integer.compare(s1.rollNumber, s2.rollNumber); }
}

class SortByMarks implements Comparator<Student> {
    public int compare(Student s1, Student s2) { return Integer.compare(s1.marks, s2.marks); }
}

public class ComparatorDemo {
    public static void main(String[] args) {
        List<Student> list = new ArrayList<>(List.of(
                new Student("Aditya", 103, 89),
                new Student("Rohit", 101, 98),
                new Student("Rohan", 104, 85),
                new Student("Sonu", 102, 93)));

        Comparator<Student> c1 = new SortByName();
        Comparator<Student> c2 = new SortByMarks();
        Comparator<Student> c3 = new SortByRollNumber();

        Collections.sort(list, c1);
        System.out.println(list);   // [Aditya(103, 89), Rohan(104, 85), Rohit(101, 98), Sonu(102, 93)]

        Collections.sort(list, c2);
        System.out.println(list);   // [Rohan(104, 85), Aditya(103, 89), Sonu(102, 93), Rohit(101, 98)]

        Collections.sort(list, c3);
        System.out.println(list);   // [Rohit(101, 98), Sonu(102, 93), Aditya(103, 89), Rohan(104, 85)]
    }
}
```

### `Comparator`: advantages and disadvantages
| Advantages | Disadvantages |
| :--- | :--- |
| **As many orderings as you want** (by name, marks, roll number), each picked where it's needed. | **One class per ordering.** Three orderings mean three extra classes. |
| The class **stays clean**: no sorting logic inside `Student`. | **Lots of boilerplate** around one line of real logic. |
| Works for classes **you can't modify**. | You still have to **create an object** just to pass a method. |

## The verbosity problem
- In each `SortBy…` class, the only line that matters is the `return`. Everything else (class, method signature, `new`, variable) is noise.
- **We only want to pass that method**, but Java forces us to wrap it in a class, create an object of it and pass the object.

---

## Anonymous class
- A class **without a name**, declared and instantiated **at the point of use**.
- It removes the named `SortBy…` classes:
```java
Collections.sort(list, new Comparator<Student>() {
    @Override
    public int compare(Student s1, Student s2) {
        return Integer.compare(s1.marks, s2.marks);
    }
});
```
- **Still verbose:** it's still a class body with a full method signature around one line of logic. The "how" is still boxed in an object.

## Functional interface
- **An interface with exactly one abstract method** (SAM: Single Abstract Method).
- It can have **any number of `default` and `static` methods**, since those already have bodies.
- `Comparator<T>` (`compare`), `Comparable<T>` (`compareTo`) and `Runnable` (`run`) are all functional interfaces.

### How it solves the `Comparator` problem
- `Collections.sort(list, ___)` expects a `Comparator`, which has **only one** abstract method: `compare`.
- So Java already knows which method you're implementing, and that the parameters are two `Student`s (from the `List<Student>`). This is called **target typing**.
- That means you only need to pass **the logic**: the parameters and the result. That's a **lambda expression**:
```
(parameters) -> expression
```

### Why a functional interface is required
- If an interface had two abstract methods `f1` and `f2`, Java couldn't tell **which one** a lambda implements.
- With exactly one abstract method there's no ambiguity, so **lambdas only work where a functional interface is expected**.

## The comparator logic as lambdas
- Each `SortBy…` class (or anonymous class) becomes one line:
```java
import java.util.*;

class Student {
    String name; int rollNumber; int marks;

    Student(String name, int rollNumber, int marks) {
        this.name = name; this.rollNumber = rollNumber; this.marks = marks;
    }

    @Override
    public String toString() { return name + "(" + rollNumber + ", " + marks + ")"; }
}

public class LambdaDemo {
    public static void main(String[] args) {
        List<Student> list = new ArrayList<>(List.of(
                new Student("Aditya", 103, 89),
                new Student("Rohit", 101, 98),
                new Student("Rohan", 104, 85),
                new Student("Sonu", 102, 93)));

        Collections.sort(list, (s1, s2) -> s1.name.compareTo(s2.name));
        System.out.println(list);   // [Aditya(103, 89), Rohan(104, 85), Rohit(101, 98), Sonu(102, 93)]

        Collections.sort(list, (s1, s2) -> Integer.compare(s1.marks, s2.marks));
        System.out.println(list);   // [Rohan(104, 85), Aditya(103, 89), Sonu(102, 93), Rohit(101, 98)]

        Comparator<Student> byRoll = (s1, s2) -> Integer.compare(s1.rollNumber, s2.rollNumber);
        Collections.sort(list, byRoll);
        System.out.println(list);   // [Rohit(101, 98), Sonu(102, 93), Aditya(103, 89), Rohan(104, 85)]
    }
}
```
- No classes, no `new`, no method signature, no types: `s1` and `s2` are inferred as `Student`.
- A single expression after the arrow is **returned automatically** (no `return` keyword).

## The forms of a lambda
| Form | Example | Rule |
| :--- | :--- | :--- |
| **Multiple parameters** | `(a, b) -> a + b` | parentheses required |
| **Single parameter** | `x -> x * x` | parentheses optional: `(x) -> x * x` |
| **No parameters** | `() -> System.out.println("hello")` | empty `()` required |
| **Block body** | `(a, b) -> { int sum = a + b; return sum; }` | `{ }` and `return` required |
| **Explicit types** | `(Student s1, Student s2) -> s1.marks - s2.marks` | all types or none |

- **Keep lambdas short.** If the logic grows into many lines, a proper class is clearer.

## Your own functional interface
```java
@FunctionalInterface
interface Calculator {
    int calculate(int a, int b);      // the one abstract method
}

public class OwnFunctionalInterface {
    public static void main(String[] args) {
        Calculator add = (a, b) -> a + b;
        Calculator multiply = (a, b) -> a * b;

        System.out.println(add.calculate(3, 4));       // 7
        System.out.println(multiply.calculate(3, 4));  // 12
    }
}
```
- Any interface with one abstract method works with lambdas.
- **`@FunctionalInterface`** is optional, but then the compiler **errors** if someone adds a second abstract method later.

**Remember:** in Java, behavior must live inside an object. `Comparable` gives only one ordering inside the class; `Comparator` gives many orderings outside it, but costs a class and an object each. An anonymous class removes the names, not the boilerplate. Because `Comparator` is a **functional interface** (one abstract method), a **lambda** `(s1, s2) -> ...` can pass just the logic.
