# Object Oriented Programming  in Java | Classes, Objects, new Keyword | Java Full Course #12
> Source: https://youtu.be/tmXD7Kzun54 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 65:10. The video is in Hindi, and its transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Storing one student as four loose variables (`name`, `age`, `rollNumber`, `college`) doesn't scale. Nothing ties them together, every function needs all four passed in, each new student needs four more variables, and nobody "owns" the data.
- **OOP** is a programming paradigm: to solve real-world problems, model the real world in code as **objects** that bundle **data (characteristics)** and **behavior (what it can do)**.
- You only model the characteristics that matter for *your* problem. A college portal cares about a person's student side, not their role as an employee or a son/daughter.
- A **class** is the blueprint (an abstract idea such as "Student"). An **object** is a concrete instance created with `new`. `Student s1 = new Student();` puts an unnamed object on the **heap** and stores a reference to it in `s1` on the **stack**.
- Use the **dot operator** to reach fields and methods (`s1.name = "Aditya"`, `s1.markAttendance()`). Methods inside the class can use the object's own fields without parameters.

## Key Concepts

**The problem with loose variables.** Four independent variables per student, renamed `name2`, `age2`, … for the next one. Nothing in the code says they describe one entity. *Why it matters:* this is the pain OOP is designed to remove.

**Object-oriented programming (OOP).** A paradigm, meaning a way of thinking about and structuring code. You mimic real-world entities as objects. Other paradigms exist (e.g. functional programming). *Why it matters:* Java is built around it, so everything from here on uses it.

**Object.** A single entity (living or not, physical or not) that groups its **characteristics** (data → variables) and **behaviors** (actions → methods). *Why it matters:* it's the unit you design, pass around and reason about.

**Abstraction of the real world (choosing what to model).** A real student has endless attributes. You keep only the ones your application needs. *Why it matters:* good classes stay small and focused on the problem.

**Class.** A blueprint written with the `class` keyword. It describes what every object of that kind looks like but stores no data itself. *Why it matters:* you write the blueprint once and create as many objects as you need.

**User-defined (non-primitive) type.** A class is a new data type you created, like `int` but made by you. Arrays, `String` and your own classes are non-primitives. *Why it matters:* you declare variables of your class type just like `int x`.

**`new` keyword.** Allocates memory for an object on the heap **at runtime** (dynamic memory allocation). *Why it matters:* no object exists until `new` runs.

**Reference variable.** A variable like `s1` that holds a reference to (in effect, the address of) an object. It is **not** the object. The object itself has no name. *Why it matters:* this is a classic interview question, and it explains aliasing later on.

**Dot operator.** `ref.member` follows the reference to the object and accesses one of its fields or methods. *Why it matters:* it's how you read, write and call anything on an object.

## Detailed Notes

### Life before OOP [00:00]
- To describe Aditya you write `String name = "Aditya"; int age = 28; int rollNumber = 1001; String college = "IIT Guwahati";`.
- For Rohit you copy all four and rename them `name2`, `age2`, `rollNumber2`, `college2`. Reading the code, you can't tell which variable belongs to whom.
- A `print(String name, int age, int rollNumber, String college)` function needs all four arguments, and if you forget one the student isn't printed properly.

### The four problems [06:28]
1. Many independent variables are needed to represent **one** student.
2. Passing a student to a function means passing every variable separately.
3. Every new student needs a whole new set of variables.
4. **No authority over the data.** No single entity owns the four values, so anyone can change any of them independently.

### What OOP says [09:11]
- OOP is a separate **paradigm**, a way of writing and thinking about code. Java is an OOP language. Functional languages are a different style.
- Why do we write code? To solve **real-world problems** (even a calculator solves one). OOP's idea: to solve real-world problems well, **mimic the real world** in your program.

### Real-world objects become programming objects [12:01]
- In the real world a student is an **object**, a complete entity with characteristics: name, age, roll number, college.
- So in code, the student should be **one block** that holds all four values as its intrinsic properties, instead of four scattered variables.
- In short: start thinking in terms of **objects**, not loose variables and functions.

### A human plays many roles: model only what you need [16:15]
- A student is a human, and the same human is a son/daughter at home and an employee at the office, with different characteristics in each role (house name; employee ID, salary, …).
- Even as a student they have far more attributes (grades, subjects, marks per semester).
- You **don't** model the whole hierarchy. You model the part relevant to your problem. For a college portal (attendance, subjects) that's the student role, and only the fields you'll actually use.

### Class = blueprint [23:35]
- "Student" on its own is an **abstract idea**. Arun and Varun are concrete (absolute) students.
- In code the abstract idea is a **class**:
  ```text
  class Student {
      String name;
      int age;
      int rollNumber;
      String college;
  }
  ```
- Writing the class creates **no** object and allocates no memory for data. It only describes what a student looks like, which is why a class is called the **blueprint of an object** [24:49].

### Primitive vs non-primitive / user-defined types [25:35]
- Primitives: `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`.
- Non-primitives (arrays, `String`, objects) are containers that hold several things. Some come with Java. The ones you write yourself (like `Student`) are **user-defined data types**.

### Creating an object: declaration and `new` [27:41]
- Declaration: `Student s1;` gives a variable of type `Student`. No object exists yet.
- Definition: `s1 = new Student();`. Or both in one line: `Student s1 = new Student();`.
- Reading it right to left:
  - `new` allocates a chunk of **heap** memory big enough for a `Student` [29:47].
  - The reference is stored in `s1`, which lives on the **stack** (inside `main`'s frame).
  - The video pictures this as `s1` holding the object's starting address (e.g. 1001).
- `s1` is a **reference variable** [32:18], not the object. The heap object is **unnamed** [33:20]. Only the reference has a name.

### Compile time vs runtime: dynamic allocation [35:43]
- Some errors appear at compile time (red squiggles), and others only when the program runs.
- `new` allocates memory **at runtime**, which is why it's called **dynamic memory allocation**.
- *Correction:* the video says `int x = 4` is allocated "at compile time" (static allocation). In Java, local variables are also created at runtime, in the method's stack frame when the method is called. The compiler only works out how much frame space is needed. The real distinction is **stack** (locals, freed when the method returns) vs **heap** (objects, freed by the garbage collector).

### Assigning and reading fields with the dot operator [37:18]
- `s1.name = "Aditya"; s1.age = 28; s1.rollNumber = 1001; s1.college = "IIT Guwahati";`
- One blueprint gives you any number of objects: `Student s2 = new Student();` creates a second, independent heap block with its own four fields.
- The dot operator follows the reference to the object and then picks the field. `System.out.println(s1.name)` reads it.

### Naming conventions [42:18]
- Conventions are good practice, not compiler rules.
- Variables (primitive and reference) and methods use **camelCase**: `firstName`, `firstNameOfStudent`, `markAttendance`.
- Classes use **PascalCase** (start with a capital): `Student`, `Human`, `Chair`.

### Objects also have behavior [45:26]
- Real objects also *do* things. An animal has characteristics (type, number of legs, wild or domestic) and behaviors (make sound, eat, run).
- Characteristics → **variables**. Behaviors → **methods** written **inside** the class, because the behavior is intrinsic to the object.
- Student example: `void markAttendance()` inside `Student`, called as `s1.markAttendance();`.
- *Correction:* the video describes the method as living "inside" each object. Logically it belongs to the object, but physically each object only stores its fields. Method code is stored once per class (the next videos come back to this).

### Helper behavior: the digital student [52:00]
- Pass the whole student instead of four values: `void print(Student s)`.
- Better still, move `print()` **into** the class. It needs no parameters because it can read the object's own fields directly. "Each student can print itself" [54:59].
- A real student can't "print itself", but the **digital** student can. Classes may contain helpers that make the code easier, beyond strict mimicry.

### Non-physical objects [55:37]
- Anything too complex for one variable becomes a class, whether or not it's physical: a `BankAccount` (account number, IFSC, branch), a `Location` (latitude, longitude).

### Code walk-through [57:27]
- `class Student` is written **outside** the `public class Demo` block, with fields plus `markAttendance()` (prints "Attendance marked by " + name) and `print()`.
- Objects are created in `main`, because execution starts there and memory is only allocated when that code runs.
- Output: two "Attendance marked by …" lines, then both students' details.

### The boilerplate is a class too [61:00]
- `public class Demo` is itself a class, and `main` is a method inside it whose return type is `void` and which takes `String[] args`. `public` and `static` are explained in later videos.
- To run *anything* in Java you need at least one class containing `main`. The video calls Java "almost completely object-oriented" [63:39]. The "almost" is because primitives and `static` members aren't objects.

## Code Examples

Each block is a complete program. Save it under the public class's name and run it with `java FileName.java`.

### Loose variables vs one object
```java
public class LooseVsObject {
    // The old way: a function that needs every piece separately
    static void printLoose(String name, int age, int rollNumber, String college) {
        System.out.println(name + ", " + age + ", " + rollNumber + ", " + college);
    }

    public static void main(String[] args) {
        String name = "Aditya"; int age = 28; int rollNumber = 1001; String college = "IIT Guwahati";
        String name2 = "Rohit"; int age2 = 28; int rollNumber2 = 102; String college2 = "IIT Guwahati";
        printLoose(name, age, rollNumber, college);     // Aditya, 28, 1001, IIT Guwahati
        printLoose(name2, age2, rollNumber2, college2); // Rohit, 28, 102, IIT Guwahati

        // The OOP way: one object carries all four values
        Pupil p = new Pupil();
        p.name = "Aditya"; p.age = 28; p.rollNumber = 1001; p.college = "IIT Guwahati";
        p.print();                                      // Aditya, 28, 1001, IIT Guwahati
    }
}

class Pupil {
    String name;
    int age;
    int rollNumber;
    String college;

    void print() { // no parameters needed: it reads its own fields
        System.out.println(name + ", " + age + ", " + rollNumber + ", " + college);
    }
}
```

### Class, objects, fields and behavior (the video's demo)
```java
public class ClassesObjectsDemo {
    public static void main(String[] args) {
        CollegeStudent s1 = new CollegeStudent(); // object on the heap, reference s1 on the stack
        CollegeStudent s2 = new CollegeStudent(); // a second, independent object

        s1.name = "Aditya"; s1.age = 28; s1.rollNumber = 1001; s1.college = "IIT Guwahati";
        s2.name = "Rohit";  s2.age = 28; s2.rollNumber = 102;  s2.college = "IIT Guwahati";

        s1.markAttendance(); // Attendance marked by Aditya
        s2.markAttendance(); // Attendance marked by Rohit
        s1.print();          // Aditya, 28, 1001, IIT Guwahati
        s2.print();          // Rohit, 28, 102, IIT Guwahati

        System.out.println(s1.name); // Aditya   (reading a field with the dot operator)
    }
}

class CollegeStudent {
    // characteristics (data)
    String name;
    int age;
    int rollNumber;
    String college;

    // behaviors
    void markAttendance() {
        System.out.println("Attendance marked by " + name);
    }

    void print() {
        System.out.println(name + ", " + age + ", " + rollNumber + ", " + college);
    }
}
```

### A reference is not the object
```java
public class ReferenceNotObject {
    public static void main(String[] args) {
        Animal a;              // declaration only: no object yet
        a = new Animal();      // definition: new allocates the object at runtime
        a.type = "Herbivore";
        a.legs = 4;

        Animal b = a;          // copies the reference, not the object
        b.legs = 3;
        System.out.println(a.legs);  // 3 (a and b refer to the same unnamed object)
        System.out.println(a == b);  // true

        Animal c = new Animal();     // a different object
        System.out.println(a == c);  // false
        a.makeSound();               // Herbivore makes a sound
    }
}

class Animal {
    String type;
    int legs;

    void makeSound() {
        System.out.println(type + " makes a sound");
    }
}
```

### Non-physical object: a bank account
```java
public class NonPhysicalObject {
    public static void main(String[] args) {
        BankAccount acc = new BankAccount();
        acc.accountNumber = "1234567890";
        acc.ifsc = "SBIN0001234";
        acc.branch = "Guwahati";
        acc.describe(); // Account 1234567890 (SBIN0001234) at Guwahati
    }
}

class BankAccount {
    String accountNumber;
    String ifsc;
    String branch;

    void describe() {
        System.out.println("Account " + accountNumber + " (" + ifsc + ") at " + branch);
    }
}
```

## Common Pitfalls / Tips
- **`s1` is not the object.** It's a reference to an unnamed heap object. `Student b = a;` copies the reference, so both names see the same object.
- **Declaring isn't creating.** `Student s1;` creates no object. Using `s1.name` before assigning `s1` is a compile error for a local variable, and a `NullPointerException` if `s1` is `null`.
- **Write the helper class outside the public class.** In the single-file setup, `class Student { … }` goes after (or before) `public class Demo { … }`, not inside `main`.
- **Only one `public` top-level class per file**, and its name must match the file name.
- **Model only what you need.** Don't add every attribute a real-world entity might have. Add what the application uses.
- **Follow the conventions**: `PascalCase` for classes, `camelCase` for variables and methods.
- **Methods that use the object's own fields don't need those fields as parameters.** `s1.print()` beats `print(s1.name, s1.age, …)`.

## Review Questions
1. List the four problems with representing students as loose variables.
2. What does it mean that OOP is a "paradigm", and what is its core idea?
3. Why don't we model every characteristic a real student has?
4. What's the difference between a class and an object? Why is a class called a blueprint?
5. What exactly happens, in memory, when `Student s1 = new Student();` runs?
6. Is `s1` the object? Where do `s1` and the object live?
7. How are characteristics and behaviors represented in a class? Why can `print()` inside `Student` take no parameters?
8. Why is `new` called dynamic memory allocation?

<details>
<summary>Answers</summary>

1. (1) Many independent variables per student. (2) Every one has to be passed to a function separately. (3) Each new student needs a new set of variables. (4) No single entity owns or controls the data.
2. It's a way of thinking about and structuring code. The core idea is to mimic real-world entities as objects that bundle their data and behavior, so that real-world problems are easier to model and solve.
3. Because the program only needs what's relevant to the problem it solves. A college portal needs name, roll number and so on, not salary or a house name. Extra fields only add noise.
4. A class is an abstract description (the fields and methods every student has) and holds no data itself. An object is a concrete instance with its own field values. The class is the blueprint that objects are built from.
5. `new` allocates heap memory for a `Student` object at runtime. Its fields get their default values. A reference to that object is stored in the local variable `s1` on the stack.
6. No. `s1` is a reference variable on the stack. The object is on the heap and has no name of its own.
7. Characteristics are fields (variables) and behaviors are methods, both declared inside the class. `print()` is called on a specific object (`s1.print()`), so it can read that object's fields directly.
8. Because the allocation happens while the program runs (at runtime), not when it's compiled. (Local primitives are also created at runtime, but on the stack in the method's frame.)
</details>

## Further Study
- Constructors and the `this` keyword (next video)
- Default values of fields; `null` and `NullPointerException`
- Stack vs heap in the JVM; garbage collection
- `static` members, and why `main` is `static`
- The four pillars of OOP: encapsulation, inheritance, polymorphism, abstraction
- OOP vs functional programming

---

[← #11](11-java-functions-recursion-overloading.md) · [Index](../../ReadMe.md) · [#13 →](13-constructors-chaining-overloading-this.md)
