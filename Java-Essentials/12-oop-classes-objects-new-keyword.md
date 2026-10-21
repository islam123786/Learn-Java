# 12 · OOP · Classes, Objects & the `new` Keyword
> Full notes: [12-oop-classes-objects-new-keyword.md](../12-oop-classes-objects-new-keyword.md) · [▶ Video](https://youtu.be/tmXD7Kzun54)

## Why OOP?
- **Without OOP**, one student is 4 loose variables (`name`, `age`, `rollNumber`, `college`), and the next student needs `name2`, `age2`, …
  - Nothing ties those variables to one student.
  - Every function call has to pass all four separately.
  - No single thing owns the data, so anyone can change any of it.
- **OOP** is a way of thinking about code (a **paradigm**): **mimic real-world things as objects**.
- **Object:** a single thing that bundles its **characteristics** (data → **variables**) and **behaviors** (actions → **methods**).
- **Model only what you need.** A real student has endless attributes. A college portal keeps only the few it actually uses.

---

## User-defined types
- **Primitive types** are built in: `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`.
- **Non-primitive types** hold several values: arrays, `String`, and **classes you write yourself**.
- A class you write, such as `Student`, is a **user-defined data type**. You use it just like `int`:
  ```java
  int x;          // built-in type
  Student s1;     // your own type
  ```

## `class`: the blueprint
```java
class Student {
    String name;              // characteristics → fields
    int age;
    int rollNumber;
    String college;

    void markAttendance() {   // behavior → method
        System.out.println("Attendance marked by " + name);
    }
}
```
- "Student" is an **abstract idea**, and Arun and Varun are **concrete** students. The class is the idea, and objects are the concrete ones.
- **A class is the blueprint of an object.** Writing it creates **no object** and stores **no data**. It only describes what every student looks like.
- **Write it once, then create as many objects as you need.**

## `new` keyword: creating the object
```java
Student s1;                 // declaration: a variable of type Student, but NO object yet
s1 = new Student();         // definition: new creates the object
Student s2 = new Student(); // both in one line
```
- `new` **allocates memory on the heap** for one `Student` (space for its 4 fields).
- This happens **at runtime**, which is why it's called **dynamic memory allocation**. No object exists until `new` runs.
- Each `new` gives a **separate, independent** object with its own fields.

## Reference variable
```
   STACK (main)               HEAP
  ┌──────────┐          ┌──────────────────────┐
  │ s1 : ────┼────────► │ name = "Aditya"      │   ← the object (it has NO name)
  └──────────┘          │ age = 28             │
                        │ rollNumber = 1001    │
                        │ college = "IIT G"    │
                        └──────────────────────┘
```
- **`s1` is not the object.** It's a **reference variable** that holds a reference (in effect, the address) of the object.
- **The object itself has no name.** Only the reference has one. This is a classic interview question.
- `s1` lives on the **stack** (in `main`'s frame), and the object lives on the **heap**.

## Dot operator (`.`)
- **`ref.member`** follows the reference to the object, then reads or writes a field or calls a method on it.
```java
public class Demo {
    public static void main(String[] args) {
        Student s1 = new Student();
        s1.name = "Aditya";                   // write a field
        s1.age = 28;
        System.out.println(s1.name);          // read a field  → Aditya
        s1.markAttendance();                  // call a method → Attendance marked by Aditya

        Student s2 = new Student();           // a second, independent object
        s2.name = "Rohit";
        s2.markAttendance();                  // Attendance marked by Rohit
    }
}

class Student {
    String name;
    int age;

    void markAttendance() {
        System.out.println("Attendance marked by " + name);
    }
}
```

---

## Other points (brief)
- **Naming conventions:** variables and methods use **camelCase** (`firstName`, `markAttendance`), and classes use **PascalCase** (`Student`).
- **Methods go inside the class**, because the behavior belongs to the object. Each object stores only its **fields**. The method code is stored **once per class**.
- **Helper methods:** a digital student can have `print()` that reads its own fields, even though a real student can't "print itself".
- **Non-physical things are objects too**, e.g. a `BankAccount` or a `Location`.
- **Even `main` lives inside a class** (`public class Demo`). You need at least one class with `main` to run anything. Java is "almost" fully object-oriented, because primitives and `static` members aren't objects.
- **Stack vs heap:** local variables live in the method's stack frame, which is freed when the method returns. Objects live on the heap and are freed by the garbage collector.

**Remember:** a **class** is the blueprint, and it's a **user-defined type**. **`new`** creates the object on the **heap** at runtime. The variable (`s1`) is only a **reference** to the object, not the object itself. The **dot operator** reaches the object's fields and methods.
