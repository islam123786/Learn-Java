# 09 · Introduction to Arrays · Declaration, Initialization, Traversal · String Intro
> Full notes: [09-introduction-to-arrays.md](../09-introduction-to-arrays.md) · [▶ Video](https://youtu.be/ajNs-dylWIM)

### Why arrays?
- 100 students would need 100 variables. An array is one **contiguous block** of memory split into slots (index 0, 1, 2, …), all of the **same type**.

### Declaring an array
- `int[] rollNumbers;` The type tells Java how big each slot is (32 bits for an `int`).
- `int rollNumbers[];` also works (C style), but `int[]` is the preferred way.

### Defining with `new`
- `int[] rollNumbers = new int[3];` creates 3 `int` slots next to each other on the **heap**.
- Addresses count bytes, so consecutive `int` slots are 4 apart (1000, 1004, 1008).

### Indexing, storing and reading
- Indexes start at **0**: `rollNumbers[0] = 1001;`
- Read the same way: `System.out.println(rollNumbers[2]);`

### Use loops, not copy-paste
- `for (int i = 0; i < 3; i++) rollNumbers[i] = 1001 + i;`

### `length` instead of hard-coding
- `for (int i = 0; i < rollNumbers.length; i++)`
- For arrays, `length` is a **field** with no `()`. `String` has a `length()` **method**.

### Out-of-bounds access
- `rollNumbers[3]` on a 3-element array throws **`ArrayIndexOutOfBoundsException`** at **runtime**, not at compile time.

### 2D arrays
- A 2D array is an **array of arrays**. Example: 3 students × 3 subjects.
- `int[][] marks = new int[3][3];` The first size is the **rows** and the second is the **columns**.

### The conceptual (matrix) view
- Think of it as a table where each cell is `marks[row][col]`, from `[0][0]` to `[2][2]`.
- To print it, use **nested loops**: the outer loop for rows, the inner loop for columns, and `println()` after each row.

### The logical (physical) view
- Memory has no rows or columns. `new int[3][3]` is an **outer array of 3 slots**, and each slot points to its own inner `int[3]` array.
- `marks[0][1] = 30` means: go to outer slot 0, follow it to that inner array, and store 30 at index 1.

### `length` in 2D
- `marks.length` is the number of rows. `marks[r].length` is the length of row `r`.
- `for (r = 0; r < marks.length; r++) for (c = 0; c < marks[r].length; c++)`

### Jagged arrays
- The row count is required, but the column count is optional: `int[][] marks = new int[3][];`
- Then size each row separately: `marks[0] = new int[2]; marks[1] = new int[3]; marks[2] = new int[4];`

### 3D and beyond
- `int[][][] arr = new int[3][3][3];` is a stack of 2D tables (rows, columns, depth). In practice you'll mostly use 2D.

### Initializer syntax
- `int[] nums = {4, 5, 6};` The size is worked out from the values, and no `new` is needed.
- `int[][] marks = {{4, 5, 6}, {7, 8, 9}, {23, 34, 93}};` has one `{…}` per row.

### Strings (intro)
- A **non-primitive** type: `String fName = "Aditya";` Strings use double quotes, while a `char` uses single quotes.
- `fName + lName` gives `"AdityaTandon"` (no space is added). Use `fName + " " + lName` instead.

**Remember:** indexes start at 0 and go up to `length - 1`. A 2D array is an array of arrays, so its rows can have different lengths (jagged).
