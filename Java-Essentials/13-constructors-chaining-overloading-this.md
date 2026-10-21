# 13 · Constructors · Chaining, Overloading & `this`
> Full notes: [13-constructors-chaining-overloading-this.md](../13-constructors-chaining-overloading-this.md) · [▶ Video](https://youtu.be/RADpqk79IzQ)

## Printing an object you never filled in
```java
Student s1 = new Student();
System.out.println(s1.name + " " + s1.age + " " + s1.rollNumber + " " + s1.college);
// null 0 0 null      ← no error: these are the DEFAULT VALUES
```

## Instance variables, instance methods, local variables
| | Declared | Lives on | Scope |
| :--- | :--- | :--- | :--- |
| **Instance variable** (field) | in the class body | the **heap**, inside the object | as long as the object |
| **Instance method** | in the class body | (the code is stored once per class) | called on an object |
| **Local variable** | inside a method (`int x = 4;`) | the **stack** | until the method ends |

## Default values (fields only)
| Field type | Default |
| :--- | :--- |
| `byte`, `short`, `int`, `long` | `0` |
| `float`, `double` | `0.0` |
| `char` | `'\u0000'` |
| `boolean` | `false` |
| any reference (`String`, arrays, objects) | **`null`** |

- `String` is a non-primitive, so a `String` field is a reference whose default is **`null`** ("nothing"), not `""`.

## Local variables have no default
```java
int x;
System.out.println(x);   // ❌ compile error: "The local variable x may not have been initialized"
```
- **Why the difference?** The compiler's **definite assignment** rule makes you assign a local before reading it, which catches bugs at compile time. Heap objects are zero-filled when they're allocated, so **fields** always start with a well-defined default.

---

## Why constructors?
- Setting `s1.name`, `s1.age`, … one line at a time is **tedious**, and it's easy to **forget a field** (for example, a `grades` field a teammate added), which would then quietly keep its default.
- A **constructor** puts values into the object **while it's being built**.

## Rules of a constructor
1. It has the **same name as the class**.
2. It has **no return type, not even `void`**.
3. It's **called while the object is being created**.
4. It's used to **initialize** the object.
5. It **can be overloaded**.

```java
class Student {
    String name; int age; int rollNumber; String college;

    Student() {                      // a constructor: same name, no return type
        name = "Aditya";
        age = 28;
        rollNumber = 1001;
        college = "IIT Guwahati";
    }
}
```
Now every `new Student()` starts with those values.

## `new Student()` is a constructor call
```
Student   s1   =   new   Student();
   │       │        │       │
 type   reference  makes   CALLS the constructor,
        variable   room    which fills in the fields
                   on the heap
```
- "The constructor is called automatically" really means it's called **by your own syntax**: the `Student()` after `new` *is* the call.
- Order: `new` allocates heap memory → `Student()` runs and fills the fields → the reference is stored in `s1` (on the stack).

## The default constructor
- Earlier, `new Student()` worked even though you never wrote a constructor. Java requires **every class to have a constructor**, so the **compiler adds an empty `Student() {}`**. The fields then keep their default values.
- **Terminology:** strictly, the "default constructor" is only the one the compiler generates. A no-argument constructor you write yourself (even an empty one) is a **no-arg constructor**. The video uses "default constructor" for both.

## Parameterized constructor
- Hard-coding "Aditya" isn't useful. You want the **caller to decide**, and to be **forced** to give the values.
```java
Student(String n, int a, int rn, String c) {
    name = n; age = a; rollNumber = rn; college = c;
}

Student s1 = new Student("Aditya", 28, 1001, "IIT Guwahati");
```
- ⚠️ Now **`new Student()` fails**: *"The constructor Student() is undefined"*. **The compiler adds the default constructor only when you've written none.**

## Constructor overloading
- Fix it by **also** declaring `Student() {}`. Now both versions compile, just like method overloading:
```java
Student() { }                                          // no-arg
Student(String n, int a, int rn, String c) { … }       // parameterized

new Student();                                         // ✅
new Student("Rohit", 28, 102, "IIT Guwahati");         // ✅
```

---

## The `this` keyword
- **`this` refers to the current object**: the one being built, or the one the method was called on.
- **The problem:** if the parameters have the same names as the fields, `name = name;` assigns the parameter **to itself**. The IDE warns: *"The assignment to variable name has no effect"*. The field stays `null`.
- **The fix:** `this.name = name;` means "**this object's field** `name` = the **parameter** `name`". It's like `s1.name`, but written from inside the object.

```java
Student(String name, int age, int rollNumber, String college) {
    this.name = name;               // field = parameter
    this.age = age;
    this.rollNumber = rollNumber;
    this.college = college;
}
```
- `this.` is optional when the names are different (`name = n`), but the usual convention is to **use the same names with `this.`**, because it reads clearly.

## Constructor overloading, at scale
- Five constructors that take 0, 1 (`name`), 2 (+ `age`), 3 (+ `rollNumber`) and 4 (+ `college`) arguments. Objects `s1` to `s5` each use a different one.
- Any field that isn't passed keeps its default (`null` or `0`).
- **The problem:** every constructor **repeats the same assignments**.

## Constructor chaining with `this(...)`
**`this(...)` calls another constructor of the same class.** Let each smaller constructor call the full one and pass defaults for the missing values:
```java
Student()                                    { this("Unknown", 0, 0, "Unknown"); }
Student(String name)                         { this(name, 0, 0, "Unknown"); }
Student(String name, int age)                { this(name, age, 0, "Unknown"); }
Student(String name, int age, int roll)      { this(name, age, roll, "Unknown"); }
Student(String name, int age, int roll, String college) {   // the only one that assigns
    this.name = name; this.age = age; this.rollNumber = roll; this.college = college;
}
```
- The assignments are written **once**.
- Using `"Unknown"` instead of `null` avoids a `NullPointerException` later on.
- In an IDE, **Cmd/Ctrl + click** on `this(...)` jumps to the constructor it calls.

## Chaining step by step
**Another style:** each constructor calls the **next larger** one:
```
new Student()  →  this("Unknown")  →  this(name, 0)  →  this(name, age, 0)  →  this(name, age, roll, "Unknown")  →  4-arg assigns everything
```
With a print in each one (**after** its `this(...)` call), `new Student()` prints in **reverse**, like recursion unwinding:

```java
public class ChainingDemo {
    public static void main(String[] args) {
        Student s = new Student();
        System.out.println(s.name + " " + s.age + " " + s.rollNumber + " " + s.college);
        // fifth
        // fourth
        // third
        // second
        // first
        // Unknown 0 0 Unknown
    }
}

class Student {
    String name; int age; int rollNumber; String college;

    Student()                                        { this("Unknown");                          System.out.println("first"); }
    Student(String name)                             { this(name, 0);                            System.out.println("second"); }
    Student(String name, int age)                    { this(name, age, 0);                       System.out.println("third"); }
    Student(String name, int age, int rollNumber)    { this(name, age, rollNumber, "Unknown");   System.out.println("fourth"); }
    Student(String name, int age, int rollNumber, String college) {
        this.name = name; this.age = age; this.rollNumber = rollNumber; this.college = college;
        System.out.println("fifth");
    }
}
```
- Each constructor **jumps to the next one before** running its own print. So the 4-arg constructor finishes first, and the no-arg one finishes last.

## `this(...)` must be the first statement
```java
Student() {
    System.out.println("hi");    // ❌ compile error: the constructor call must be the first statement
    this("Unknown");
}
```
- The call to another constructor (`this(...)`, or `super(...)` for a parent class, #16) **must be the first line** of the constructor.
- **Version note:** this is true up to **Java 24**. **Java 25** finalized "flexible constructor bodies" (JEP 513), which allows statements before `this(...)`/`super(...)`, as long as they don't use the object being built.

**Remember:** fields get **default values** (`0`, `false`, `null`), but **locals don't**. A constructor has the **class's name and no return type**, and `new Student(...)` **calls** it. Java adds an empty default constructor **only if you write none**. **`this.field`** means the current object's field. **`this(...)`** chains to another constructor and must be the **first statement**.
