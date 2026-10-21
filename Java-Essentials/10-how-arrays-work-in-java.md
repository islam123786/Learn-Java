# 10 · How Arrays Work in Java · Heap, Contiguous Storage, Random Access
> Full notes: [10-how-arrays-work-in-java.md](../10-how-arrays-work-in-java.md) · [▶ Video](https://youtu.be/Qg73uiG9b6A)

### Random access
- `arr[2]` goes **straight** to slot 2, without walking through slots 0 and 1. The question is how the JVM knows where slot 2 is.

### Primitives vs non-primitives
- `int x = 4;` is a container on the **stack** that holds 4 directly.
- (A primitive *field* inside an object lives on the heap, together with that object.)

### Arrays live on the heap, and the variable is a reference
- `int[] arr = new int[5];`
  - `new int[5]` creates the array on the **heap**.
  - `arr` sits on the **stack** and holds a **reference** (an address) to that array.
- **Rule of thumb:** anything made with `new` goes on the heap.

### Reading a value by address
- An `int` takes 4 bytes. If `x` starts at address 100, it uses bytes 100–103. The JVM goes to 100 and reads 4 bytes.

### Array elements and their addresses
- With base address 100, the elements of `{10, 20, 30, 40, 50}` sit at **100, 104, 108, 112, 116**, one right after another.

### The random-access formula
> **address of arr[i] = base address + (size of data type × i)**

- `arr[3]` = 100 + 4 × 3 = **112** → 40. Since it's one calculation, access is **O(1)**.

### Where the base address comes from
- `arr` stores the address of the first element.
- In reality there's also a small object header (which holds things like `length`), so the formula is `object address + header + i × size`. It's still O(1), and Java never shows you real addresses.

### Other element types, and `boolean`
- Element size: `long`/`double` = 8 bytes, `int`/`float` = 4, `char` = 2.
- **`boolean`:** the Java spec doesn't fix its size. In theory 1 bit is enough, but CPUs read memory in bytes, so HotSpot/OpenJDK use **1 byte** for array elements and fields. `boolean[]` at base 100: `arr[2]` = 100 + 1 × 2 = 102.

### Bounds checking
- Before using the formula, the JVM checks: `if (index < 0 || index >= arr.length)` → throw `ArrayIndexOutOfBoundsException`.
- This happens at **runtime**, not compile time. Without it, `arr[100]` would read memory that belongs to something else.

### 2D arrays in memory
- Java has no true 2D array. `new int[3][4]` is an **outer array of 3 references**, each pointing to its own separate `int[4]` row.

---

## Finding `arr[1][2]`: the formula used twice

```java
int[][] arr = {
    {4, 3, 5, 7},     // row 0
    {1, 10, 6, 3},    // row 1
    {0, 5, 9, 3}      // row 2
};
```

**Memory picture** (example addresses; a reference takes 4 bytes):
```
STACK              HEAP
┌─────┐      outer array (holds references)
│ arr │ ──►  ┌───────┬───────┬───────┐
└─────┘      │  200  │  300  │  400  │
             └───┬───┴───┬───┴───┬───┘
            addr 100   104     108
                 │       │       │
                 ▼       ▼       ▼
     row 0 @200: [ 4 | 3 | 5 | 7 ]
     row 1 @300: [ 1 | 10 | 6 | 3 ]    ← addresses 300, 304, 308, 312
     row 2 @400: [ 0 | 5 | 9 | 3 ]
```

**Step 1: find the row** (in the outer array, element size = 4 bytes, the size of a reference)
```
address of arr[1] = 100 + 4 × 1 = 104
read 4 bytes at 104 → 300   (this is a reference, not an int)
```

**Step 2: find the column** (inside row 1, element size = 4 bytes, the size of an `int`)
```
address of arr[1][2] = 300 + 4 × 2 = 308
read 4 bytes at 308 → 6  ✓
```

**Key points**
- The outer array's slots hold **addresses** (200, 300, 400), not the numbers in the matrix.
- So `arr[i][j]` is **two O(1) lookups**: one to get row `i`'s reference, then one inside that row to get column `j`.
- The element size changes per level. The outer array always stores references, and the rows store the actual type (`int` = 4, `double` = 8, `boolean` = 1).
- The rows are **separate objects**. Row 1 isn't guaranteed to sit right after row 0, which is also why rows can have different lengths (jagged arrays).
- 3D works the same way: one more lookup per dimension.
- A reference is **4 bytes** on 64-bit HotSpot with compressed oops (the default for heaps under about 32 GB), and 8 bytes without them.

**See it in code**
```java
public class FindInto2D {
    public static void main(String[] args) {
        int[][] arr = {
            {4, 3, 5, 7},
            {1, 10, 6, 3},
            {0, 5, 9, 3}
        };
        System.out.println(arr[1][2]);           // 6

        int[] row1 = arr[1];                     // step 1: get row 1's reference
        System.out.println(row1[2]);             // 6 (step 2: index inside that row)
        System.out.println(row1 == arr[1]);      // true (same row object, not a copy)

        row1[2] = 99;                            // changing through the reference...
        System.out.println(arr[1][2]);           // 99 (...changes the matrix)

        // Rows are independent objects: swapping references swaps whole rows
        int[] temp = arr[0];
        arr[0] = arr[2];
        arr[2] = temp;
        System.out.println(arr[0][0] + " " + arr[2][0]);   // 0 4

        // A row can be replaced with an array of a different length (jagged)
        arr[1] = new int[]{8, 8};
        System.out.println(arr[1].length + " " + arr[0].length);   // 2 4

        int[][] fresh = new int[3][];            // outer array exists, rows don't yet
        System.out.println(fresh[0]);            // null
    }
}
```

---

## `String` arrays

**The problem:** `"Aditya"` has 6 characters and `"Abhay"` has 5. If elements have different sizes, how can `base + size × i` work?

**The answer:** `String` is non-primitive, so a `String[]` doesn't store the text. It stores **references**, which all have the **same fixed size** (4 bytes). Each reference points to a `String` object somewhere else on the heap.

```java
String[] names = new String[3];
names[0] = "Aditya";
names[1] = "Abhay";
names[2] = "Rohit";
```

```
STACK               HEAP
┌───────┐     names array (holds references)
│ names │ ──► ┌───────┬───────┬───────┐
└───────┘     │  100  │  200  │  300  │
              └───┬───┴───┬───┴───┬───┘
             addr 400   404     408
                  │       │       │
                  ▼       ▼       ▼
           @100 "Aditya" @200 "Abhay" @300 "Rohit"
```

**Finding `names[1]`**
```
address of names[1] = 400 + 4 × 1 = 404
read 4 bytes at 404 → 200   (reference)
follow it to 200       → "Abhay"  ✓
```

**Key points**
- The array's slots are all the same size, so the formula still works, even though the strings have different lengths.
- `new String[3]` creates the array with every slot set to **`null`**. No `String` objects exist until you assign them.
- This works for **every** non-primitive array (`String[]`, `Student[]`, `int[][]`): the slots hold references, and the objects live elsewhere.

**Strings vs `char[]`**
- You can think of a string as a character array, `{'A','d','i','t','y','a'}`, and internally it used to be one.
- **Up to JDK 8**, a `String` stored a `char[]`. **From JDK 9 (Compact Strings)**, it stores a `byte[]` plus an encoding flag (Latin-1 or UTF-16), which saves memory for English text. (The video says "up to JDK 9", but it was JDK 8.)

**See it in code**
```java
public class StringArrayRefs {
    public static void main(String[] args) {
        String[] names = new String[3];
        System.out.println(names[0]);                    // null (no String object yet)

        names[0] = "Aditya";
        names[1] = "Abhay";
        names[2] = "Rohit";
        System.out.println(names[1]);                    // Abhay

        // Different lengths are fine: the slots hold references, not the text
        System.out.println(names[0].length() + " " + names[1].length());   // 6 5

        // Copying the array copies references, not the strings
        String[] copy = names.clone();
        System.out.println(copy[1] == names[1]);         // true (same String object)

        // A String is built from characters
        char[] chars = {'A', 'd', 'i', 't', 'y', 'a'};
        String fromChars = new String(chars);
        System.out.println(fromChars);                   // Aditya
        System.out.println(fromChars.equals(names[0]));  // true (same text)
        System.out.println(names[0].charAt(2));          // i
        System.out.println(names[0].toCharArray().length);   // 6
    }
}
```

---

### Why random access is fast: caching
- The CPU loads memory in chunks (**cache lines**, usually 64 bytes) into its **L1/L2/L3 caches**. Reading `arr[3]` also brings `arr[4]`, `arr[5]` and the next few elements into the cache.
- So looping over an array in order is very fast (**spatial locality**). The video says registers, but it's really the CPU caches.

**Remember:** `address = base + size × i`, so array access is O(1). A 2D array uses the formula twice, first for the row reference and then for the value. `String[]` (and every object array) stores fixed-size references, which is why elements of different lengths still work.
