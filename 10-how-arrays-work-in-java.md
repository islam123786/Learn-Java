# How Arrays work in Java | Heap, Contiguous Storage, Random Access | Java Full Course #10
> Source: https://youtu.be/Qg73uiG9b6A · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 54:03. The video is in Hindi and has no English transcript, so these notes were written from the Hindi auto-transcript. It had no timing data either, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- **Random access**: `arr[i]` jumps straight to element `i` without walking through the earlier ones. This works because array elements are **contiguous** and **all the same size**.
- Primitive local variables hold their **value directly** (on the stack). An array is non-primitive: `new int[5]` creates the array on the **heap**, and the variable `arr` is a **reference** that holds the array's address (on the stack).
- Address formula: **`address(arr[i]) = baseAddress + i × sizeOf(elementType)`**. The base address comes from the reference. Before using the formula, the JVM **bounds-checks** the index and throws `ArrayIndexOutOfBoundsException` if it's outside `0 … length-1`.
- A **2D array** is an array of **references** to row arrays, so `arr[i][j]` applies the formula **twice**: once to find row `i`'s reference, then again inside that row. A `String[]` is likewise an array of references to `String` objects.
- `boolean` has no officially fixed size (HotSpot/OpenJDK use 1 byte in arrays). Contiguous storage also makes arrays **cache-friendly**: nearby elements are loaded together.

## Key Concepts

**Random access.** Reading or writing any element in constant time, O(1), by index. *Why it matters:* it's the defining strength of arrays compared with structures like linked lists.

**Primitive vs non-primitive storage.** `int x = 4;` creates a 4-byte container that *directly holds* 4. Non-primitives (arrays, `String`, objects) live on the **heap**, and the variable only holds a **reference** to them. *Why it matters:* it explains why `arr` "points to" an array instead of containing it.

**Stack and heap (preview).** Local variables, including primitive values and reference variables, live in a method's stack frame. Everything created with `new` lives on the heap. *Why it matters:* memory management, garbage collection and reference semantics all build on this split (covered in depth later).

**Reference variable.** A variable whose value is the address of an object on the heap. Draw it as an arrow. *Why it matters:* the reference gives the JVM the **base address** of the array.

**Base address.** The address where the array's first element starts. *Why it matters:* it's the starting point of the address formula.

**Element size.** `byte` 1, `short` 2, `char` 2, `int` 4, `float` 4, `long` 8, `double` 8 bytes, `boolean` JVM-dependent (usually 1 in arrays), and a reference typically 4 (see the correction below). *Why it matters:* it's the stride the formula multiplies by.

**Bounds check.** Before computing the address, Java checks `index < 0 || index >= length`. *Why it matters:* it stops your program from reading or overwriting unrelated memory. C/C++ don't do this.

**Array of arrays (2D).** The outer array stores references and each reference points to a separate inner array. *Why it matters:* it explains double indexing, jagged arrays, and why rows aren't necessarily next to each other.

**Cache locality.** The CPU loads memory in chunks, so after reading `arr[3]`, `arr[4]` is probably already in cache. *Why it matters:* sequential array loops are very fast in practice.

## Detailed Notes

### Recap: what random access means [▶ ~00:00](https://youtu.be/Qg73uiG9b6A?t=0)
- `int[] arr = new int[5];` gives 5 contiguous slots, indexes 0–4.
- `System.out.println(arr[2]);` goes **directly** to slot 2 (say it holds 6), with no loop over 0 and 1. That's random access.
- The question for this video: *how* does the JVM know where slot 2 is?

### Primitives vs non-primitives [▶ ~02:17](https://youtu.be/Qg73uiG9b6A?t=137)
- Primitives: `byte`, `short`, `int`, `long`, `float`, `double`, `boolean`, `char`. Non-primitives: arrays, `String`, objects.
- `int x = 4;` [▶ ~03:25](https://youtu.be/Qg73uiG9b6A?t=205) creates a container named `x` that directly holds 4. Printing `x` reads that container. Primitives "go to the **stack** memory".
- *Nuance:* that's true for local variables. A primitive **field** inside an object lives on the heap with that object.

### Arrays live on the heap; the variable is a reference [▶ ~05:42](https://youtu.be/Qg73uiG9b6A?t=342)
- `int[] arr = new int[5];`
  - `new int[5]` creates a 5-slot array in the **heap**.
  - `arr` is created on the **stack** as a **reference variable** that *points to* the heap array, like an arrow [▶ ~07:59](https://youtu.be/Qg73uiG9b6A?t=479).
- Rule of thumb: anything made with `new` goes on the heap; plain local variables go on the stack.
- So `x` *holds* 4, but `arr` *does not hold* the array; it holds a reference to it [▶ ~09:08](https://youtu.be/Qg73uiG9b6A?t=548).

### Reading a value by address [▶ ~09:08](https://youtu.be/Qg73uiG9b6A?t=548)
- An `int` takes 4 bytes. If `x` starts at address 100, it occupies 100–103 (the video says "100 to 104"). To print `x`, the JVM goes to 100 and reads 4 bytes, because it knows both the start and the type's size.

### Array elements and their addresses [▶ ~12:33](https://youtu.be/Qg73uiG9b6A?t=753)
- Fill the array: `arr[0] = 10; arr[1] = 20; arr[2] = 30; arr[3] = 40; arr[4] = 50;`.
- Each slot is 4 bytes, so with a base of 100 the slots start at **100, 104, 108, 112, 116**. Element 2 is read from 108 (4 bytes).
- They're contiguous: element 1 is right after element 0, never somewhere far away.

### The random-access formula [▶ ~14:50](https://youtu.be/Qg73uiG9b6A?t=890)
- `arr[3]`: 100 + 4 × 3 = **112**, then read 4 bytes, which gives 40.
- `arr[4]`: 100 + 4 × 4 = **116**, which gives 50.
- General formula [▶ ~16:00](https://youtu.be/Qg73uiG9b6A?t=960): **`address of arr[i] = base address + (size of data type × i)`**.
- The JVM computes this directly, which is why access is O(1).

### Where does the base address come from? [▶ ~17:08](https://youtu.be/Qg73uiG9b6A?t=1028)
- From the reference. `arr` "points to" the array, meaning it **stores the address of the first element** (the base address, e.g. 100).
- *Simplification:* a real JVM array object starts with a small **header** (object metadata plus the `length` field), so the first element sits a fixed offset after the object's address. The formula becomes `objectAddress + headerSize + i × elementSize`, which is still O(1). Java also never exposes raw addresses to your code; this is a mental model.

### Other element types; the curious case of `boolean` [▶ ~19:25](https://youtu.be/Qg73uiG9b6A?t=1165)
- `long[]` and `double[]` use a stride of 8, `float[]` 4, `char[]` 2; the formula is otherwise the same.
- `boolean` [▶ ~20:33](https://youtu.be/Qg73uiG9b6A?t=1233): the earlier data-types video never gave its size, because **the Java specification doesn't fix one**. Each JVM decides.
  - In principle 1 **bit** would be enough (0 = false, 1 = true).
  - But CPUs fetch memory in **bytes**, not bits, so HotSpot (Oracle) and OpenJDK use **1 byte** per `boolean` (for `boolean[]` elements and fields).
  - Java leaves it to each JVM so it can choose what suits its platform.
- `boolean[] arr = new boolean[5];` [▶ ~25:07](https://youtu.be/Qg73uiG9b6A?t=1507) with base 100 has elements at 100, 101, 102, 103, 104. `arr[2]` is at 100 + 1 × 2 = 102.
- *Nuance:* a `boolean` **local variable** occupies a full 32-bit slot in the JVM (it's treated as an `int`); the 1-byte figure applies to array elements and fields.

### Bounds checking [▶ ~27:24](https://youtu.be/Qg73uiG9b6A?t=1644)
- `arr[100]` on a 5-element array: blindly applying the formula would give 100 + 4 × 100 = 500 (the video's arithmetic says 204), an address that might belong to something else entirely.
- So before the formula, Java effectively does: if `index < 0 || index >= arr.length`, throw `ArrayIndexOutOfBoundsException`.
- *Corrections:* the video writes the condition as `index > length`, but `index == length` is also out of range, so it must be `>=`. It also says the "compiler" fails here; the check happens at **runtime** in the JVM.

### 2D arrays in memory [▶ ~30:50](https://youtu.be/Qg73uiG9b6A?t=1850)
- `int[][] arr = new int[3][4];` is pictured as a 3 × 4 matrix, but Java has no real 2D array. It's an **array of arrays** [▶ ~33:07](https://youtu.be/Qg73uiG9b6A?t=1987): an outer array of size 3 whose every element is itself an array of size 4.
- Memory layout:
  - Stack: `arr`, which references the outer array on the heap.
  - Heap: the outer array, holding 3 **references** (`arr[0]`, `arr[1]`, `arr[2]`).
  - Heap: three separate `int[4]` arrays, one per reference, each at its own location.
- Example matrix: row 0 = `4 3 5 7`, row 1 = `1 10 6 3`, row 2 = `0 5 9 3`.

### Finding `arr[1][2]` with the formula twice [▶ ~36:33](https://youtu.be/Qg73uiG9b6A?t=2193)
- A reference takes 4 bytes (see the correction below). If the outer array is at 100, its slots are at 100, 104, 108.
- **Step 1, row:** 100 + 4 × 1 = 104. Read 4 bytes there to get the reference to row 1, e.g. address 200.
- **Step 2, column:** row 1's elements are `int`s at 200, 204, 208, 212. Then 200 + 4 × 2 = 208, which gives **6**.
- The outer array's slots literally contain addresses [▶ ~42:15](https://youtu.be/Qg73uiG9b6A?t=2535). Picture them holding 200, 300, 400. Those numbers are references, not `int` values.
- So `arr[i][j]` is found by applying the formula once to get row `i`'s reference and again inside that row to get column `j`.
- The same idea extends to 3D, 4D, … [▶ ~44:32](https://youtu.be/Qg73uiG9b6A?t=2672): references to arrays of references to arrays, and so on.
- For `boolean[][]`, each row stores 1-byte elements; for `double[][]`, 8-byte elements. The outer array always stores references.
- *Correction/nuance:* "a reference is 4 bytes" holds on 64-bit HotSpot with **compressed oops**, the default for heaps under about 32 GB. On 32-bit JVMs it's also 4, but without compressed oops on 64-bit it's 8. Also, the row arrays are independent objects, so row 1 isn't guaranteed to sit right after row 0.

### `String` arrays [▶ ~45:41](https://youtu.be/Qg73uiG9b6A?t=2741)
- `String[] names = new String[3];` with "Aditya", "Abhay", "Rohit". Strings have varying lengths, so how can the formula work?
- Because `String` is non-primitive, the array stores **references** (fixed size), and each points to a `String` object elsewhere on the heap (e.g. at 100, 200, 300).
- `names[1]` with array base 400: 400 + 4 × 1 = 404. Read the reference there, follow it, and you get "Abhay".
- Strings vs `char[]` [▶ ~49:06](https://youtu.be/Qg73uiG9b6A?t=2946): you can think of a string as a character array (`char[] name = {'A','d','i','t','y','a'}`), and internally it used to be one.
- *Correction:* the video says strings were `char[]` "up to JDK 9". In fact they were `char[]` **up to JDK 8**. **Compact Strings** (JDK 9+) store a `byte[]` plus an encoding flag (Latin-1 or UTF-16). Details come in the strings deep-dive.

### Why random access is great: caching [▶ ~51:24](https://youtu.be/Qg73uiG9b6A?t=3084)
- The CPU fetches memory in multi-byte chunks. In the video's example, the CPU fetches 8 bytes at a time: reading `arr[3]` (4 bytes) also brings in `arr[4]`.
- If you then read `arr[4]`, it's already close to the CPU, so there's no trip back to RAM.
- *Correction:* the video says the data is kept in **registers**. It's actually the CPU **caches** (L1/L2/L3), which load whole **cache lines** (typically 64 bytes). The principle (spatial locality) is right, and it's why looping over an array in order is so fast.

## Code Examples

Java doesn't let you see real addresses, so these programs show the observable consequences of the memory model. Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### The address formula as a simulation
```java
public class AddressFormula {
    // Mental model only: base + i * elementSize (a real JVM also adds a fixed header offset)
    static long addressOf(long base, int index, int elementSize, int length) {
        if (index < 0 || index >= length) {
            throw new ArrayIndexOutOfBoundsException("Index " + index + " out of bounds for length " + length);
        }
        return base + (long) index * elementSize;
    }

    public static void main(String[] args) {
        int[] arr = {10, 20, 30, 40, 50};
        for (int i = 0; i < arr.length; i++) {
            System.out.println("arr[" + i + "] = " + arr[i] + " at " + addressOf(100, i, Integer.BYTES, arr.length));
        }
        // arr[0] = 10 at 100
        // arr[1] = 20 at 104
        // arr[2] = 30 at 108
        // arr[3] = 40 at 112
        // arr[4] = 50 at 116

        System.out.println(addressOf(100, 2, 1, 5));        // 102 (boolean[] with 1-byte elements)
        System.out.println(addressOf(100, 3, Long.BYTES, 5)); // 124 (long[] / double[] use 8 bytes)

        try {
            addressOf(100, 100, Integer.BYTES, arr.length);
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());               // Index 100 out of bounds for length 5
        }
    }
}
```

### Primitives hold values, arrays are references
```java
public class ReferenceSemantics {
    public static void main(String[] args) {
        int x = 4;
        int y = x;          // copies the value
        y = 99;
        System.out.println(x);        // 4 (x is unaffected)

        int[] arr = new int[5];
        int[] alias = arr;  // copies the REFERENCE, not the array
        alias[2] = 6;
        System.out.println(arr[2]);           // 6 (same array on the heap)
        System.out.println(arr == alias);     // true (same address)

        int[] other = new int[5];
        other[2] = 6;
        System.out.println(arr == other);     // false (different arrays, even with equal contents)
        System.out.println(java.util.Arrays.equals(arr, other)); // true (compare contents)

        System.out.println(Integer.BYTES + " " + Long.BYTES + " " + Character.BYTES + " " + Double.BYTES);
        // 4 8 2 8
    }
}
```

### 2D arrays are arrays of references to rows
```java
public class ArrayOfArrays {
    public static void main(String[] args) {
        int[][] arr = {
            {4, 3, 5, 7},
            {1, 10, 6, 3},
            {0, 5, 9, 3}
        };
        System.out.println(arr[1][2]);       // 6

        int[] row1 = arr[1];                 // step 1: get row 1's reference
        System.out.println(row1[2]);         // 6 (step 2: index inside that row)
        System.out.println(row1 == arr[1]);  // true (same row object)

        // Rows are independent objects: swapping two references swaps whole rows
        int[] temp = arr[0];
        arr[0] = arr[2];
        arr[2] = temp;
        System.out.println(arr[0][0] + " " + arr[2][0]); // 0 4

        // A row can be replaced by an array of a different length (jagged)
        arr[1] = new int[]{8, 8};
        System.out.println(arr[1].length + " " + arr[0].length); // 2 4

        int[][] fresh = new int[3][];        // outer array exists, rows don't yet
        System.out.println(fresh[0]);        // null
    }
}
```

### `String[]` stores references
```java
public class StringArrayRefs {
    public static void main(String[] args) {
        String[] names = new String[3];
        System.out.println(names[0]);          // null (no String object yet)

        names[0] = "Aditya";
        names[1] = "Abhay";
        names[2] = "Rohit";
        System.out.println(names[1]);          // Abhay

        // Elements can have different lengths because the slots hold references, not text
        System.out.println(names[0].length() + " " + names[1].length()); // 6 5

        char[] chars = {'A', 'd', 'i', 't', 'y', 'a'};
        String fromChars = new String(chars);
        System.out.println(fromChars);                 // Aditya
        System.out.println(fromChars.equals(names[0])); // true
        System.out.println(names[0].charAt(2));         // i
    }
}
```

## Common Pitfalls / Tips
- **`==` on arrays compares references**, not contents. Use `Arrays.equals` (1D) or `Arrays.deepEquals` (2D).
- **Assigning an array doesn't copy it.** `b = a` makes both names refer to the same array. Use `a.clone()` or `Arrays.copyOf(a, a.length)` for a real copy. For 2D, `clone()` copies only the outer array of references (a shallow copy).
- **Bounds are `0 … length-1`.** The check is `index >= length`, not `index > length`.
- **New object arrays are full of `null`.** `new String[3]` and `new int[3][]` contain no objects yet, and calling a method on `null` throws `NullPointerException`.
- **Don't count on `boolean` taking one bit.** A `boolean[]` usually costs 1 byte per element. For large sets of flags, `java.util.BitSet` packs them into bits.
- **Reference size varies** (4 or 8 bytes depending on the JVM and the compressed-oops setting). Memory estimates for object arrays should take that into account.
- **Loop in memory order.** For 2D arrays, iterate row by row (outer = row, inner = column) to benefit from cache locality.

## Review Questions
1. What does "random access" mean, and which two properties of arrays make it possible?
2. Where do `int x = 4;` and `int[] arr = new int[5];` live in memory, and what does each variable actually hold?
3. Write the address formula for `arr[i]`, and compute the address of `arr[3]` for an `int[]` at base 100.
4. Where does the JVM get the base address from?
5. Why doesn't the Java spec fix the size of `boolean`, and what size do HotSpot/OpenJDK use in arrays?
6. Why doesn't Java just apply the formula for `arr[100]` on a 5-element array?
7. Walk through how `arr[1][2]` is located in `int[][] arr = new int[3][4]`.
8. How can a `String[]` give O(1) access when the strings have different lengths?

<details>
<summary>Answers</summary>

1. Access to any element in O(1) directly by index. It works because the elements are stored contiguously and every element has the same size, so the position can be computed.
2. `x` is a local primitive on the stack and directly holds 4. The array object is on the heap; `arr` is a reference variable on the stack holding the array's address.
3. `address = base + i × elementSize`. For `arr[3]`: 100 + 3 × 4 = 112.
4. From the reference variable: `arr` stores the address where the array begins (in practice the object address, plus a fixed header offset to the first element).
5. Logically 1 bit is enough, but CPUs fetch bytes, so the spec lets each JVM choose an efficient size for its platform. HotSpot/OpenJDK use 1 byte per `boolean` array element.
6. The computed address would be outside the array and might belong to other data. Java first checks `index < 0 || index >= length` and throws `ArrayIndexOutOfBoundsException` instead.
7. The outer array holds 3 references (4 bytes each with compressed oops). Base + 4 × 1 gives row 1's reference slot. Follow that reference to the row's `int[4]`, then compute rowBase + 4 × 2 and read the `int` there.
8. The array stores fixed-size references, not the characters. The formula finds the reference in O(1), and following it leads to the `String` object.
</details>

## Further Study
- JVM memory areas in depth: stack frames, heap, garbage collection (later in the series)
- Object layout: headers, compressed oops, alignment (try the JOL tool)
- Reference semantics: pass-by-value of references in method calls
- CPU caches, cache lines and spatial locality; row-major vs column-major traversal
- `String` internals: compact strings, the string pool, immutability
- `Arrays.copyOf`, `clone()`, shallow vs deep copy of 2D arrays
- Why linked lists don't have random access (compare with #29, Collections)

---

[← #09](09-introduction-to-arrays.md) · [Index](ReadMe.md) · [#11 →](11-java-functions-recursion-overloading.md)
