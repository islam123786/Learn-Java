# 08 · Loops & Jump Statements · for, while, do-while, break, continue
> Full notes: [08-java-loops-and-jump-statements.md](../08-java-loops-and-jump-statements.md) · [▶ Video](https://youtu.be/d6hNDJrj1bw)

## What loops are
- A loop runs a block again and again until a condition becomes false. Java has three: `while`, `do-while` and `for`.

## `while`
- `while (i <= 10) { println(i); i++; }` checks the condition **first**.
- Forget `i++` and you get an **infinite loop**. Always use braces.
- **Countdown:** start at 10, use `i >= 1` and `i--`.

## Increment inside the condition
- `i = 1; while (i++ <= 10)` prints **2 to 11**, because the old value is compared and the new value is printed.
- **Fix:** `i = 0; while (i++ < 10)` prints 1 to 10.

## `do-while`
- `do { … } while (cond);` runs the body **first**, then checks. Don't forget the semicolon at the end.
- It always runs **at least once**. With `i = 11`, `while (i <= 10)` prints nothing, but `do-while` prints 11.
- **Use case:** a menu that's shown at least once and repeats until the user picks Exit.

## `for`
- `for (int i = 1; i <= 10; i++) { … }`
- **Order:** the init runs once → check the condition → run the body → run the update → check again → …
- **Reverse:** `for (int i = 10; i >= 1; i--)`

## Infinite loops
- All three parts of a `for` are optional, but both semicolons are required: `for (;;)` loops forever.
- An infinite loop doesn't use up memory. It just runs and uses CPU until you stop it.

## Comma-separated `for`
- `for (int i = 1, j = 1; i <= 10; i++, j += 2)` has several variables in the init and the update.
- The **condition can't use commas**. Combine conditions with `&&` or `||` instead.

## Any boolean works as a condition
- `for (; running; )` keeps going while a flag is true. Something inside the loop sets `running = false` to stop it.

## Why loop counters are `int`
- `byte` and `short` get promoted to `int` anyway, and the JVM stores local variables in 32-bit slots. A smaller type saves nothing.

## Nested loops
- The inner loop runs **completely** for every outer iteration. An outer loop of n and an inner loop of m run the body **n × m** times (O(n²)).

## Pattern: right-angled triangle
- The outer loop is the **rows**. The inner loop prints `j <= i` stars on row `i`.
- Use `print("* ")` inside the inner loop and `println()` after it to end each row.

## `break`
- **Exits the loop** immediately. `for (i = 1..10) { println(i); if (i > 5) break; }` prints 1 to 6.
- **Prime check:** try `i = 2 … p-1`. If `p % i == 0`, `break`. After the loop, `i == p` means the number is prime. Declare `i` before the loop so it's still visible after it.
- **Optimization:** you only need to check up to √p (`i * i <= p`).

## `continue`
- **Skips the rest of the body** and goes to the next iteration. In a `for` loop, the update `i++` still runs.
- `break` leaves the loop. `continue` skips to the next round.

## `break` / `continue` in nested loops
- A plain `break` or `continue` only affects the **innermost** loop it's in. The outer loop keeps going.

---

## Labels

A **label** is a name followed by a colon, placed just before a loop: `outer: for (…)`. You can then write:

| Statement | What it does |
| :--- | :--- |
| `break;` | exits only the innermost loop |
| `break outer;` | exits the loop named `outer`, and every loop inside it |
| `continue;` | goes to the next iteration of the innermost loop |
| `continue outer;` | goes straight to the **next iteration of `outer`**, skipping the rest of the inner loop |

### Example 1: plain `break` vs labelled `break`
```java
public class BreakLabel {
    public static void main(String[] args) {
        // Plain break: only the inner loop stops. Every row still starts.
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (j == 2) break;
                System.out.print(i + "" + j + " ");
            }
        }
        System.out.println();                    // 11 21 31

        // Labelled break: the first time j == 2, BOTH loops stop.
        outer:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (j == 2) break outer;
                System.out.print(i + "" + j + " ");
            }
        }
        System.out.println();                    // 11
    }
}
```

### Example 2: search a 2D grid and stop at the first match
The most common real use. Without a label you'd need an extra `found` flag and a second `break`.
```java
public class GridSearch {
    public static void main(String[] args) {
        int[][] grid = {
            {1, 2, 3},
            {4, 5, 6},
            {7, 8, 9}
        };
        int target = 5;
        int row = -1, col = -1;

        search:
        for (int i = 0; i < grid.length; i++) {
            for (int j = 0; j < grid[i].length; j++) {
                if (grid[i][j] == target) {
                    row = i;
                    col = j;
                    break search;                // done: leave both loops
                }
            }
        }
        System.out.println("Found at row " + row + ", col " + col);   // Found at row 1, col 1
    }
}
```

### Example 3: labelled `continue`
`continue outer` abandons the current row and moves on to the next one.
```java
public class ContinueLabel {
    public static void main(String[] args) {
        // Print each row, but stop a row as soon as a negative number appears in it.
        int[][] data = {
            {1, 2, 3},
            {4, -1, 6},
            {7, 8, 9}
        };
        rows:
        for (int i = 0; i < data.length; i++) {
            for (int j = 0; j < data[i].length; j++) {
                if (data[i][j] < 0) {
                    System.out.println("(row " + i + " skipped at negative)");
                    continue rows;               // go to the next i
                }
                System.out.print(data[i][j] + " ");
            }
            System.out.println();                // only runs if the row finished
        }
        // 1 2 3
        // 4 (row 1 skipped at negative)
        // 7 8 9
    }
}
```

### Example 4: labelled break in the triangle pattern
```java
public class TriangleLabel {
    public static void main(String[] args) {
        outer:
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("* ");
                if (i == 3 && j == 3) break outer;   // stop the whole pattern after row 3
            }
            System.out.println();
        }
        System.out.println();
        System.out.println("done");
        // *
        // * *
        // * * *
        // done
    }
}
```

## Plain (labelled) code blocks

Any `{ … }` is a **block**, even without an `if` or `for` in front of it. Blocks can be nested and labelled. `break label;` jumps to the **end** of that block.

```java
public class LabeledBlocks {
    public static void main(String[] args) {
        first: {
            second: {
                third: {
                    System.out.println("Hello");
                    if (true) break first;               // jump past the end of 'first'
                    System.out.println("not printed");
                }
                System.out.println("not printed");
            }
            System.out.println("not printed");
        }
        System.out.println("after first");
        // Hello
        // after first

        // A more practical use: skip the rest of a block when validation fails.
        int age = -5;
        validate: {
            if (age < 0) {
                System.out.println("Invalid age");
                break validate;                          // skip the rest of this block
            }
            System.out.println("Age is " + age);
        }
        System.out.println("continuing");
        // Invalid age
        // continuing
    }
}
```

**Rules**
- `break label;` works on **any** labelled block or loop.
- `continue label;` works only when the label is on a **loop**. A block has no "next iteration".
- The label must be on a block that **contains** the `break`/`continue`. You can't jump sideways or forward to some other label. This isn't `goto`.
- You'll rarely need labelled blocks. Labelled loops (Examples 1–4) are the useful part.

**Remember:** `while` checks first, `do-while` runs at least once, and `for` is init; condition; update. `break` exits and `continue` skips, both on the innermost loop by default. Add a **label** to target an outer loop.
