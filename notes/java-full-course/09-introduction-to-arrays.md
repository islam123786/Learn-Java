# Introduction to Arrays | Declaration, Initialization, Traversal | String Intro | Java Full Course #9
> Source: https://youtu.be/ajNs-dylWIM · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 107:00. The video is in Hindi and has no English transcript, so these notes were written from the Hindi auto-transcript. It had no timing data either, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- An **array** is a fixed-size collection of values of **one data type**, stored in **contiguous** memory and accessed by a **zero-based index**: `int[] roll = new int[3];` then `roll[0] = 1001;`.
- `new` allocates the array on the **heap**. `arr.length` gives the size, so loops should use `i < arr.length` instead of a hard-coded number. Going outside `0 … length-1` throws `ArrayIndexOutOfBoundsException`.
- A **2D array** is an **array of arrays**. You picture it as a rows × columns matrix, but in memory it's an outer array whose slots point to separate inner arrays. So use `m.length` for rows and `m[r].length` for each row's columns.
- Only the first size is mandatory (`new int[3][]`), so the rows can have **different lengths** (jagged arrays). The same idea extends to 3D and beyond.
- Short initializer syntax: `int[] a = {4, 5, 6};` and `int[][] m = {{1, 2}, {3, 4}};`. **`String`** is a non-primitive type for text (double quotes), and `+` **concatenates** strings.

## Key Concepts

**Array.** A named block of equally sized slots, one per element, all of the same type. *Why it matters:* it replaces dozens of separate variables (`roll1`, `roll2`, …) with one variable plus an index, and works naturally with loops.

**Contiguous memory.** The slots sit next to each other in memory. For `int`s, each slot is 4 bytes (32 bits). *Why it matters:* the element's type tells the JVM how big each slot is, and contiguity is what makes jumping straight to `arr[i]` possible (next video).

**Declaration vs definition.** `int[] roll;` declares a variable that can refer to an int array. `roll = new int[3];` creates (allocates) the array. You can combine them: `int[] roll = new int[3];`. *Why it matters:* it mirrors `int x;` vs `x = 4;` for ordinary variables.

**The `new` keyword.** Allocates memory on the heap for the array (and later for objects). *Why it matters:* arrays are non-primitive (reference) types. The variable holds a reference to the array.

**Index.** The position of an element, starting from **0**. The last index is `length - 1`. *Why it matters:* most array bugs are off-by-one errors.

**`length`.** `arr.length` is the number of slots. It's a **field**, not a method, so there are no parentheses. *Why it matters:* loops written with `arr.length` keep working when the size changes.

**`ArrayIndexOutOfBoundsException`.** Thrown at **runtime** when you access an index `< 0` or `>= length`. *Why it matters:* an exception is Java's way of saying "something went wrong"; exceptions get their own video later.

**Multidimensional / 2D array.** `int[][] marks = new int[3][3];` is an array whose elements are themselves `int[]` arrays. *Why it matters:* it models tables, e.g. students × subjects.

**Conceptual vs logical (physical) view.** Humans *picture* a 2D array as a matrix with rows and columns. The computer actually stores **one outer array of references, each pointing to an inner 1D array**. *Why it matters:* it explains `marks[r].length`, and why rows can differ in length.

**Jagged array.** A 2D array whose rows have different lengths, created as `new int[3][]` followed by `marks[0] = new int[1]; marks[1] = new int[2]; …`. *Why it matters:* real data is often uneven (students taking different numbers of subjects).

**`String`.** A non-primitive type holding text in double quotes (`"Aditya"`). `+` between strings joins them (concatenation). *Why it matters:* you've been using strings since `System.out.println("Hello")`.

## Detailed Notes

### Why arrays? [00:00]
- Storing 4 roll numbers as `int roll1 = 1, roll2 = 2, …` needs one variable each. With 100 students that's 100 variables, which isn't workable.
- The idea [02:23]: instead of many separate allocations, reserve one **big contiguous chunk** of memory, split into sections (index 0, 1, 2, …), and store each student's roll number in its own section.

### Declaring an array [03:35]
- `int[] rollNumbers;` declares it: the data type, `[]` to mark it as an array, then the name.
- Why the type is needed [04:47]: an `int` variable gets a 32-bit container. For an array of 3 ints the compiler/JVM must know it needs **3 × 32-bit** slots.
- Definition: arrays are collections of values of one data type.

### Defining with `new` [08:22]
- `int[] rollNumbers = new int[3];`
  - `int[]`: the element type.
  - `rollNumbers`: the identifier (the array's name).
  - `new`: a special keyword that allocates memory on the **heap**. The heap is explained later with classes and objects; for now treat it as a black box.
  - `[3]`: the size of the array.
- It's just like `int x = 4;` (one 32-bit container named `x`), except here you get three 32-bit containers together under the name `rollNumbers` [11:57].
- The slots are **contiguous**: one directly after another in memory.
- *Correction:* the video's example addresses go 1001, 1002, 1003, and it then mentions skipping 32 bits. Addresses count **bytes**, so consecutive `int` slots differ by **4** (e.g. 1000, 1004, 1008).

### Indexing, storing and reading [13:09]
- Each slot gets an index starting at **0**: `rollNumbers[0] = 1001; rollNumbers[1] = 1002; rollNumbers[2] = 1003;`.
- Read the same way [16:45]: `System.out.println(rollNumbers[2]);` prints 1003.

### Use loops, not copy-paste [17:56]
- Filling 100 slots one statement at a time defeats the purpose. Loops are heavily used with arrays.
- Fill: `int x = 1001; for (int i = 0; i < 3; i++) { rollNumbers[i] = x; x++; }` gives 1001, 1002, 1003.
- Print: `for (int i = 0; i < 3; i++) System.out.println(rollNumbers[i]);`.

### `length` instead of hard-coding [22:43]
- Writing `i < 3` hard-codes the size. In a large codebase you may not know the size, or it may change later.
- `rollNumbers.length` gives the size (3 here), so write `for (int i = 0; i < rollNumbers.length; i++)`.
- *Correction:* the video calls `length` a "special method". For arrays it's a final **field** (`arr.length`, no parentheses). `String` has a `length()` method, with parentheses.

### Out-of-bounds access [26:19]
- `rollNumbers[3]` or `rollNumbers[4]` on a 3-element array throws `ArrayIndexOutOfBoundsException` (the video says "IndexOutOfBoundsException"; the array version is a subclass of it).
- An exception is Java signalling "something went wrong while running this; fix it". It's raised at **runtime** by the JVM, not by the compiler.

### Coding it [27:31]
- Demo of all of the above: assign by index, print by index, print `length` (3), then fill and print with loops.
- Alternative syntax [31:06]: `int rollNumbers[] = new int[3];` also works. Java prefers `int[] name`; the C/C++ style `name[]` is supported for legacy reasons.

### Multidimensional arrays: 2D [32:18]
- A 1D array is an *array of ints*. A 2D array is an **array of arrays**: each slot of the outer array is itself an array.
- Example [35:53]: 3 students × 3 subjects (Hindi, English, CS).

  | | Hindi | English | CS |
  |---|---|---|---|
  | S1 | 50 | 30 | 90 |
  | S2 | 60 | 40 | 80 |
  | S3 | 70 | 50 | 70 |

  - Separate variables would need 9 of them (`s1Hindi`, `s1English`, …). A 1D array can't hold 3 marks in one slot unless that slot is itself an array.
- Declaration [40:40]: `int[][] marks = new int[3][3];`. The first size is the **number of rows** and the second is the **number of columns**.

### The conceptual (matrix) view [41:52]
- Picture a 3 × 3 matrix. Rows are horizontal (R1–R3) and columns vertical (C1–C3), each indexed from 0.
- Each cell is addressed `[row][col]`: `[0][0]`, `[0][1]`, …, `[2][2]`.
- Filling [47:51]: the values here have no pattern, so assign each one: `marks[0][0] = 50; marks[0][1] = 30; marks[0][2] = 90; marks[1][0] = 60; …`.
- Printing [52:38]: **nested loops**. The outer loop walks the rows and the inner loop walks the columns. Use `print` inside and `println()` after each row, the same technique as pattern printing.

### The logical (physical) view [56:13]
- The computer has no notion of rows and columns; tables are a human visualisation (like tables in SQL).
- `new int[3][3]` actually creates an **outer array of 3 slots**. Each slot refers to its **own inner `int[3]` array**.
- `marks[0][1] = 30` means: go to slot 0 of the outer array, follow it to that inner array, and store 30 at its index 1.
- The video says the details (references and objects) come after OOP.

### `length` in 2D [62:12]
- `marks.length` is the number of rows (the outer array's size).
- `marks[0].length`, `marks[1].length`, … are the sizes of each inner array.
- So the general loop is `for (r = 0; r < marks.length; r++) for (c = 0; c < marks[r].length; c++)`.
- Using `marks.length` for the inner loop only works when every row has the same length as the number of rows.

### Jagged arrays: rows of different lengths [69:23]
- The row count is **mandatory**, the column count is **optional**: `int[][] marks = new int[3][];`.
- Then size each row separately: `marks[0] = new int[2]; marks[1] = new int[3]; marks[2] = new int[4];`.
- Conceptually it's a matrix whose rows have 2, 3 and 4 columns. Physically it's an outer array pointing to inner arrays of sizes 2, 3 and 4. Example use: students taking different numbers of subjects.
- `int marks[][]` also works as declaration syntax.

### 3D and beyond [78:57]
- `int[][][] arr = new int[3][3][3];` is an array of arrays of arrays: `arr` → `arr[i]` → `arr[i][j]` → `int` values at `arr[i][j][k]`.
- Conceptually, a stack of 2D matrices, like squares stacked into a cube (rows, columns, **depth**).
- You can go to any number of dimensions, but in practice it's mostly 2D, occasionally 3D.

### Initializer syntax [84:56]
- If you already know the values: `int[] rollNumbers = {4, 5, 6};`. The size (3) is inferred, and no `new` is needed.
- 2D: `int[][] marks = {{4, 5, 6}, {7, 8, 9}, {23, 34, 93}};`, one inner `{…}` per row.
- Coding demos [87:20]: 3 × 3 assignment and printing, the jagged array (rows of 1, 2 and 3 elements → `23` / `24 90` / `12 78 45`) [90:55], and initializer syntax [96:54]. Accessing `marks[0][1]` on a 1-element row would throw `ArrayIndexOutOfBoundsException`.

### Strings (intro) [98:06]
- Recap of data types: **primitive** (`byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`) and **non-primitive** (arrays, `String`, objects). Strings get a deep dive after OOP.
- `String fName = "Aditya";` (capital `S`). String literals use **double quotes**, while a `char` uses single quotes (`'a'`).
- Everything inside `println("Hello")` is already a string literal.
- Concatenation [102:53]: `fName + lName` gives `"AdityaTandon"`, with no automatic space. Use `fName + " " + lName` for `"Aditya Tandon"`. `" "` (a space) and `""` (empty) are valid strings too.
- You can store the result: `String fullName = fName + " " + lName;` [104:05].

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### 1D array: declare, fill, read, `length`
```java
public class ArrayBasics {
    public static void main(String[] args) {
        int[] rollNumbers = new int[3];   // three int slots, all 0 initially
        System.out.println(rollNumbers[0]); // 0 (default value)

        int x = 1001;
        for (int i = 0; i < rollNumbers.length; i++) {
            rollNumbers[i] = x;
            x++;
        }

        for (int i = 0; i < rollNumbers.length; i++) {
            System.out.println(rollNumbers[i]);
        }
        // 1001
        // 1002
        // 1003

        System.out.println(rollNumbers.length); // 3
        System.out.println(rollNumbers[2]);     // 1003

        int legacyStyle[] = {4, 5, 6};          // C-style brackets + initializer
        System.out.println(legacyStyle.length); // 3
    }
}
```

### Out-of-bounds access
```java
// throws
public class OutOfBounds {
    public static void main(String[] args) {
        int[] arr = {10, 20, 30};
        System.out.println(arr[2]); // 30
        System.out.println(arr[3]); // throws ArrayIndexOutOfBoundsException: Index 3 out of bounds for length 3
    }
}
```

### 2D array: marks table
```java
public class MarksTable {
    public static void main(String[] args) {
        int[][] marks = new int[3][3];  // 3 students (rows) x 3 subjects (columns)
        marks[0][0] = 50; marks[0][1] = 30; marks[0][2] = 90;
        marks[1][0] = 60; marks[1][1] = 40; marks[1][2] = 80;
        marks[2][0] = 70; marks[2][1] = 50; marks[2][2] = 70;

        for (int row = 0; row < marks.length; row++) {
            for (int col = 0; col < marks[row].length; col++) {
                System.out.print(marks[row][col] + " ");
            }
            System.out.println();
        }
        // 50 30 90
        // 60 40 80
        // 70 50 70

        System.out.println(marks.length + " " + marks[0].length); // 3 3
    }
}
```

### Jagged array and initializer syntax
```java
public class JaggedArray {
    public static void main(String[] args) {
        int[][] marks = new int[3][];   // rows mandatory, columns decided per row
        marks[0] = new int[1];
        marks[1] = new int[2];
        marks[2] = new int[3];

        marks[0][0] = 23;
        marks[1][0] = 24; marks[1][1] = 90;
        marks[2][0] = 12; marks[2][1] = 78; marks[2][2] = 45;

        for (int r = 0; r < marks.length; r++) {
            for (int c = 0; c < marks[r].length; c++) {
                System.out.print(marks[r][c] + " ");
            }
            System.out.println();
        }
        // 23
        // 24 90
        // 12 78 45

        int[][] grid = {{12, 14, 56}, {34, 45, 67}, {45, 67, 78}};
        System.out.println(grid[1][2]);     // 67

        int[][] uneven = {{1}, {2, 3}, {4, 5, 6, 7}};
        System.out.println(uneven[2].length); // 4

        int[][][] cube = new int[3][3][3];  // 3D: array of arrays of arrays
        cube[2][1][0] = 9;
        System.out.println(cube[2][1][0] + " " + cube.length + " " + cube[0][0].length); // 9 3 3
    }
}
```

### Strings and concatenation
```java
public class StringIntro {
    public static void main(String[] args) {
        String firstName = "Aditya";
        String lastName = "Tandon";

        System.out.println(firstName + lastName);        // AdityaTandon
        System.out.println(firstName + " " + lastName);  // Aditya Tandon

        String fullName = firstName + " " + lastName;
        System.out.println(fullName.length());           // 13 (String uses a method with ())

        char c = 'a';                                     // char: single quotes
        String empty = "";                                // valid, empty string
        System.out.println(c + empty + "!");             // a!

        System.out.println("Sum: " + 2 + 3);              // Sum: 23 (left to right, string wins)
        System.out.println("Sum: " + (2 + 3));            // Sum: 5
    }
}
```

## Common Pitfalls / Tips
- **Indexes run from 0 to `length - 1`.** `i <= arr.length` in a loop throws on the last iteration.
- **`arr.length` vs `str.length()`.** Arrays have a field; strings have a method.
- **Arrays are fixed-size.** You can't grow `new int[3]` later; you'd have to create a bigger array and copy the elements (that's what `ArrayList` does internally).
- **Default values.** A new `int[]` is filled with `0`, `double[]` with `0.0`, `boolean[]` with `false`, and object arrays (like `String[]`) with `null`.
- **Initializer only at declaration.** `int[] a; a = {1, 2};` doesn't compile; write `a = new int[]{1, 2};`.
- **Bracket placement.** `int[] a, b;` declares two arrays, but `int a[], b;` declares one array and one plain `int`. Prefer `int[] name`.
- **2D loops.** Use `m[r].length` for the inner bound; it's correct for jagged arrays too.
- **`new int[][3]` doesn't compile.** The first dimension is required.
- **Printing an array directly** (`println(arr)`) prints something like `[I@1b6d3586`, not the contents. Use a loop or `java.util.Arrays.toString(arr)` / `Arrays.deepToString(m)`.
- **String concatenation goes left to right.** `"Sum: " + 2 + 3` is `"Sum: 23"`. Parenthesise arithmetic.

## Review Questions
1. Why is an array better than many separate variables, and what does "contiguous memory" mean?
2. Explain each part of `int[] rollNumbers = new int[3];`.
3. What are the valid indexes of an array of length 5? What happens if you access index 5?
4. Why should a loop use `arr.length` instead of a literal size? Is `length` a method?
5. What is the difference between the conceptual and the logical view of `int[][] marks = new int[3][3]`?
6. In a 2D traversal, what do `marks.length` and `marks[r].length` represent?
7. How do you create a 2D array whose rows have lengths 2, 3 and 4? Which dimension is mandatory?
8. What do `"Aditya" + "Tandon"` and `"Total: " + 2 + 3` produce?

<details>
<summary>Answers</summary>

1. One array variable plus an index replaces many variables and works with loops. Contiguous means the element slots are laid out one after another in memory, each the size of the element type (4 bytes for `int`).
2. `int[]` is the element type and marks it as an array, `rollNumbers` is the name, `new` allocates the array on the heap, and `[3]` is the size (three `int` slots, initially 0).
3. 0 to 4. Accessing index 5 throws `ArrayIndexOutOfBoundsException` at runtime.
4. So the loop stays correct if the size changes or isn't known where the loop is written. `length` is a field of arrays (no parentheses), not a method.
5. Conceptually it's a 3 × 3 matrix of rows and columns. Logically, in memory, it's an outer array of 3 references, each pointing to a separate inner `int[3]` array.
6. `marks.length` is the number of rows (the outer array's size); `marks[r].length` is the number of columns in row `r` (that inner array's size).
7. `int[][] m = new int[3][]; m[0] = new int[2]; m[1] = new int[3]; m[2] = new int[4];`. The first dimension (number of rows) is mandatory.
8. `"AdityaTandon"` (no automatic space), and `"Total: 23"`, because concatenation proceeds left to right once a `String` is involved.
</details>

## Further Study
- How arrays are laid out on the heap and why random access is O(1) (next video)
- Default values and reference types (`null`)
- The enhanced for-each loop over arrays
- `java.util.Arrays`: `toString`, `deepToString`, `fill`, `sort`, `copyOf`, `equals`
- Common array problems: max/min, reverse, linear search, sum of a matrix, transpose
- `String` in depth: immutability, the string pool, `equals` vs `==`, `StringBuilder`
- `ArrayList` as a resizable alternative to arrays

---

[← #08](08-java-loops-and-jump-statements.md) · [Index](README.md) · [#10 →](10-how-arrays-work-in-java.md)

---

[← #08](08-java-loops-and-jump-statements.md) · [Index](README.md) · [#10 →](10-how-arrays-work-in-java.md)
