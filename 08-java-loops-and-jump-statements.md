# Java Loops & Jump Statements | for, while, do-while, break, continue Explained | Java Full Course #8
> Source: https://youtu.be/d6hNDJrj1bw · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 85:06. The video is in Hindi and has no English transcript, so these notes were written from the Hindi auto-transcript. It had no timing data either, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A **loop** (iteration statement) repeats a block while a `boolean` condition stays true. Java has three: `while`, `do-while` and `for` (plus the for-each loop, covered with arrays).
- `while` checks the condition **before** each pass, so it may run zero times. `do-while` runs the body **first** and checks afterwards, so it always runs at least once. That makes it the natural fit for menus.
- `for (init; condition; update)` packs the three loop parts into one line. All three are optional (`for (;;)` is an infinite loop). Init and update may contain several comma-separated parts, but the condition must be one boolean expression (combine with `&&`/`||`).
- Nested loops multiply: an outer loop of n and an inner loop of m run the body n × m times (O(n²) when both are n). That's the basis of pattern-printing problems.
- **Jump statements:** `break` exits the innermost loop (or switch); `continue` skips to the next iteration. With **labels** (`outer:`), `break outer` / `continue outer` act on an outer loop.

## Key Concepts

**Iteration.** One pass through a loop body. A loop is a block that the flow of control keeps re-entering until its condition becomes false. *Why it matters:* it replaces copy-pasting the same statement many times.

**Loop condition.** A `boolean` expression (`i <= 10`, `flag == true`, `i <= 10 && j <= 5`). *Why it matters:* the loop ends only when the condition becomes false, so something inside the loop must eventually make it false.

**Infinite loop.** A loop whose condition never becomes false, e.g. a forgotten `i++`, an empty body, or `for (;;)`. *Why it matters:* the program hangs. Sometimes you want one on purpose (servers, game loops) and exit it with `break`.

**`while` vs `do-while`.** Entry-controlled (check first) vs exit-controlled (run first). *Why it matters:* if the body must run at least once (show a menu, read input), `do-while` expresses that directly.

**`for` loop.** `for (init; condition; update) { body }`. Init runs once; then condition → body → update repeats. *Why it matters:* it's the most common loop for counting, and it keeps the counter's scope inside the loop.

**Nested loops.** A loop inside another loop. The inner loop completes all its iterations for **each** outer iteration. *Why it matters:* you need them for 2D work (grids, tables, patterns), and they explain O(n²) running times.

**`break` / `continue`.** `break` leaves the loop immediately. `continue` abandons the current iteration and goes to the next one (for a `for` loop, it runs the update first). *Why it matters:* they let you stop early (e.g. as soon as a divisor is found) or skip unwanted items.

**Labels.** An identifier followed by a colon placed before a statement (`outer: for (…)`). `break label` exits that labelled statement; `continue label` jumps to the next iteration of that labelled loop. *Why it matters:* it's the clean way to exit nested loops in one step.

**Variable scope.** A variable declared inside a block (including in a `for` header) only exists inside that block. *Why it matters:* to use the counter after the loop you must declare it before the loop.

## Detailed Notes

### What loops are [▶ ~01:03](https://youtu.be/d6hNDJrj1bw?t=63)
- Selection statements split the flow into several paths; a loop instead makes the flow **go back up** and re-run a block a set number of times (e.g. n = 3 iterations), then continue below it.
- Java's three loops [▶ ~02:07](https://youtu.be/d6hNDJrj1bw?t=127): `while`, `do-while`, `for`.

### Motivation: printing 1 to 10 [▶ ~03:11](https://youtu.be/d6hNDJrj1bw?t=191)
- Without loops you would write `System.out.println(1); System.out.println(2); …` or `println(i); i++;` again and again. That doesn't scale.

### The `while` loop [▶ ~04:14](https://youtu.be/d6hNDJrj1bw?t=254)
- Syntax: `while (condition) { body }`. The condition must be a `boolean`, just like in `if`.
- `int i = 1; while (i <= 10) { System.out.println(i); i++; }` prints 1 to 10. When `i` becomes 11 the condition is false and control continues after the loop.
- Unlike `if`, the block runs again and again until the condition becomes false.
- Forget `i++` [▶ ~06:22](https://youtu.be/d6hNDJrj1bw?t=382) and you get 1, 1, 1, … forever: an **infinite loop**.
- `println` prints and then moves to a new line.
- Braces [▶ ~09:33](https://youtu.be/d6hNDJrj1bw?t=573): with a single body statement you can omit `{}`, but then `i++` on the next line is **outside** the loop, which gives another infinite loop. Always use braces.
- Counting down [▶ ~10:37](https://youtu.be/d6hNDJrj1bw?t=637): start at 10, use `i >= 1`, and `i--`. *Correction:* the transcript reads "i <= 1", but a countdown needs `i >= 1`.

### Increment inside the condition [▶ ~11:41](https://youtu.be/d6hNDJrj1bw?t=701)
- `while (i++ <= 10)` with `i = 1` prints **2 to 11**: the comparison uses the old value, then `i` is incremented before the body runs.
- Fix: start with `i = 0` and use `<` instead of `<=`, i.e. `while (i++ < 10)`, which prints 1 to 10.

### The `do-while` loop [▶ ~13:48](https://youtu.be/d6hNDJrj1bw?t=828)
- Syntax: `do { body } while (condition);` Note the **semicolon** at the end.
- The body runs first, then the condition is checked.
- Key difference [▶ ~16:59](https://youtu.be/d6hNDJrj1bw?t=1019): with `i = 11`, `while (i <= 10)` prints nothing, but the `do-while` prints **11** once before checking.
- Use case [▶ ~18:03](https://youtu.be/d6hNDJrj1bw?t=1083): **menu selection**. A game shows "1. Play, 2. Load saved game, 3. Exit" at least once, performs the choice, and shows the menu again until the user picks Exit.

### The `for` loop [▶ ~21:14](https://youtu.be/d6hNDJrj1bw?t=1274)
- Syntax: `for (int i = 1; i <= 10; i++) { … }`. The three parts are separated by **semicolons**.
- The counter can be declared outside (`int i; for (i = 1; …)`), but declaring it in the header is the norm when it's only needed inside the loop.
- Order of execution [▶ ~24:25](https://youtu.be/d6hNDJrj1bw?t=1465):
  1. Initialization runs **once**.
  2. The condition is evaluated.
  3. If it's true, the body runs.
  4. Control goes back to the `for` and the update (`i++`) runs.
  5. The condition is evaluated again.
  6. Steps 3–5 repeat until the condition is false.
- Reverse [▶ ~27:37](https://youtu.be/d6hNDJrj1bw?t=1657): `for (int i = 10; i >= 1; i--)` prints 10 down to 1.
- The **for-each** loop is covered later, with arrays and strings.

### Infinite loops on purpose (and by accident) [▶ ~28:40](https://youtu.be/d6hNDJrj1bw?t=1720)
- An empty body with an unchanged variable (`while (i < 10) { }`) spins forever, so the code after it never runs.
- *Correction:* the video says it runs "until RAM runs out". An empty or printing loop doesn't consume memory; it just runs (and uses CPU) until you stop the program.
- For loops [▶ ~30:48](https://youtu.be/d6hNDJrj1bw?t=1848): removing `i++`, removing the condition (`for (int i = 1; ; i++)`), or removing everything (`for (;;)`) all give infinite loops. **All three parts are optional**, but both semicolons are required.

### Comma-separated `for` [▶ ~33:59](https://youtu.be/d6hNDJrj1bw?t=2039)
- Several variables in init and update: `for (int i = 1, j = 1; i <= 10; i++, j += 2) System.out.println(i * j);` prints 1, 6, 15, 28, 45, …
- Each variable can be updated differently (`i++`, `j += 2`, `k *= 2`).
- The **condition can't be comma-separated** [▶ ~38:14](https://youtu.be/d6hNDJrj1bw?t=2294) ("Syntax error on token ','"). Combine conditions with `&&` (stop as soon as either is false) or `||` (stop only when both are false). With `i <= 10 && j <= 5` and `j += 2`, it prints just 1, 6, 15.

### Any boolean works as a condition [▶ ~40:22](https://youtu.be/d6hNDJrj1bw?t=2422)
- `for (…; b == true; …)` keeps going while a flag is true; some `if` inside the loop can set `b = false` to end it.

### Why loop counters are `int`, not `byte`/`short` [▶ ~41:25](https://youtu.be/d6hNDJrj1bw?t=2485)
- Most loops would fit in a `short`, so why always `int`? Because of type promotion: `byte`/`short` values are promoted to `int` in arithmetic and comparisons anyway. The JVM also stores locals in 32-bit slots, so a `short` counter saves nothing. Use `long` only when the range really exceeds `int`.

### Nested loops [▶ ~43:33](https://youtu.be/d6hNDJrj1bw?t=2613)
- A loop inside a loop; any loop type can be nested, to any depth.
- With `for (i = 1..5) { for (j = 1..5) { … } }` [▶ ~45:40](https://youtu.be/d6hNDJrj1bw?t=2740), for `i = 1` the inner loop runs `j = 1..5` completely, then `i = 2` runs `j = 1..5` again, and so on.
- Count [▶ ~47:48](https://youtu.be/d6hNDJrj1bw?t=2868): outer n, inner m gives **n × m** executions of the body. With n = m = 5 that's 25 (O(n²)); three nested loops give O(n³).

### Pattern printing: right-angled triangle [▶ ~49:55](https://youtu.be/d6hNDJrj1bw?t=2995)
- Outer loop = **rows** (`i = 1..n`). Inner loop = what goes on each row. Row `i` needs `i` stars, so the inner condition is `j <= i`, not `j <= 5`.
- Use `System.out.print("* ")` (no newline) inside, and `System.out.println()` after the inner loop to end the row.
- Change n from 5 to 10 or 20 and the triangle grows. Pattern problems are great loop practice.

### Jump statements: `break` [▶ ~57:21](https://youtu.be/d6hNDJrj1bw?t=3441)
- Two jump statements here: `break` and `continue`. You already saw `break` in `switch`, but both are mostly used in loops.
- `for (i = 1..10) { println(i); if (i > 5) break; }` prints **1 to 6**: 6 is printed, then `6 > 5` triggers the break.
- Why not just change the loop bound? Because the stopping point is often decided **dynamically** at runtime (e.g. a flag set by some other condition).

### Example: prime check with `break` [▶ ~61:36](https://youtu.be/d6hNDJrj1bw?t=3696)
- To test whether `p` is prime, try divisors `i = 2 … p - 1`. If `p % i == 0` for any of them, print "not prime" and `break`, since there's no need to test further.
- After the loop: if `i == p`, the loop finished without breaking, so `p` is prime.
- **Scope** [▶ ~68:03](https://youtu.be/d6hNDJrj1bw?t=4083): `i` declared in the `for` header isn't visible after the loop ("i cannot be resolved to a variable"). Declare `int i;` before the loop.
- Results: 7 gives "The number is prime", 9 gives "The number is not prime" (it breaks at `i = 3`).
- Optimization: you only need to test up to √p (`i * i <= p`). *Correction:* the transcript garbles this as "i < P".

### `continue` [▶ ~70:06](https://youtu.be/d6hNDJrj1bw?t=4206)
- Print odd numbers 1 to 10: `if (i % 2 != 0) println(i);` works, and so does `if (i % 2 == 0) continue; println(i);`.
- `continue` skips the rest of the body and goes to the next iteration. In a `for` loop the update `i++` still runs.
- Summary: **`break` leaves the loop; `continue` skips to the next iteration.**

### `break` / `continue` in nested loops [▶ ~74:21](https://youtu.be/d6hNDJrj1bw?t=4461)
- An unlabelled `break` exits only the **innermost** loop that contains it.
- In the triangle with `if (j >= 5) break;` inside the inner loop: rows 1–4 are normal, and every row from 5 on is capped at 5 stars, while the outer loop keeps going.
- `continue` in the inner loop likewise only affects the inner loop.

### Labels [▶ ~78:36](https://youtu.be/d6hNDJrj1bw?t=4716)
- Name a loop: `outer: for (…) { inner: for (…) { … } }`.
- `break outer;` exits **both** loops at once. In the triangle example, the pattern stops after the 5th row.
- `continue outer;` jumps straight to the outer loop's next iteration.

### Plain (labelled) code blocks [▶ ~81:47](https://youtu.be/d6hNDJrj1bw?t=4907)
- Any `{ … }` is a block, even without `if`/`for`, and blocks can be nested and labelled: `first: { second: { third: { println("Hello"); break first; } } }`.
- `break first;` jumps to the end of the `first` block. You rarely need this; labels are mostly used with loops.
- Note: `continue` with a label only works if the label is on a loop; `break label` works on any labelled block.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### `while`, countdown, and increment-in-condition
```java
public class WhileLoops {
    public static void main(String[] args) {
        int i = 1;
        while (i <= 5) {
            System.out.print(i + " ");
            i++;
        }
        System.out.println();          // 1 2 3 4 5

        i = 5;
        while (i >= 1) {
            System.out.print(i + " ");
            i--;
        }
        System.out.println();          // 5 4 3 2 1

        i = 1;
        while (i++ <= 5) System.out.print(i + " ");
        System.out.println();          // 2 3 4 5 6  (old value compared, new value printed)

        i = 0;
        while (i++ < 5) System.out.print(i + " ");
        System.out.println();          // 1 2 3 4 5
    }
}
```

### `do-while` runs at least once
```java
public class DoWhileDemo {
    public static void main(String[] args) {
        int i = 11;
        while (i <= 10) {
            System.out.println("while: " + i);    // never runs
        }

        do {
            System.out.println("do-while: " + i); // do-while: 11
            i++;
        } while (i <= 10);

        // Menu-style loop: choices simulate user input
        int[] choices = {1, 2, 3};
        int k = 0, choice;
        do {
            System.out.println("1. Play  2. Load  3. Exit");
            choice = choices[k++];
            if (choice == 1) System.out.println("Playing...");
            else if (choice == 2) System.out.println("Loading saved game...");
        } while (choice != 3);
        System.out.println("Bye");
        // Output: menu, Playing..., menu, Loading saved game..., menu, Bye
    }
}
```

### `for` loop variants
```java
public class ForLoops {
    public static void main(String[] args) {
        for (int i = 1; i <= 5; i++) System.out.print(i + " ");
        System.out.println();                         // 1 2 3 4 5

        for (int i = 5; i >= 1; i--) System.out.print(i + " ");
        System.out.println();                         // 5 4 3 2 1

        for (int i = 1, j = 1; i <= 5; i++, j += 2) System.out.print(i * j + " ");
        System.out.println();                         // 1 6 15 28 45

        for (int i = 1, j = 1; i <= 10 && j <= 5; i++, j += 2) System.out.print(i * j + " ");
        System.out.println();                         // 1 6 15

        int count = 0;
        for (;;) {                                    // infinite loop, exited with break
            if (++count == 3) break;
        }
        System.out.println("count = " + count);       // count = 3

        boolean running = true;
        int n = 0;
        for (; running; ) {                           // any boolean condition works
            n++;
            if (n == 4) running = false;
        }
        System.out.println("n = " + n);               // n = 4
    }
}
```

### Nested loops and the triangle pattern
```java
public class TrianglePattern {
    public static void main(String[] args) {
        int rows = 5;
        for (int i = 1; i <= rows; i++) {       // one iteration per row
            for (int j = 1; j <= i; j++) {      // i stars on row i
                System.out.print("* ");
            }
            System.out.println();               // end the row
        }
        // *
        // * *
        // * * *
        // * * * *
        // * * * * *

        int iterations = 0;
        for (int i = 1; i <= 5; i++)
            for (int j = 1; j <= 5; j++)
                iterations++;
        System.out.println(iterations);         // 25 (n * m)
    }
}
```

### `break` and `continue`: prime check and odd numbers
```java
public class BreakContinue {
    static boolean isPrime(int p) {
        if (p < 2) return false;
        int i;                                   // declared outside so it's visible after the loop
        for (i = 2; i < p; i++) {
            if (p % i == 0) break;               // found a divisor: stop early
        }
        return i == p;                           // loop finished without break
    }

    public static void main(String[] args) {
        for (int i = 1; i <= 10; i++) {
            System.out.print(i + " ");
            if (i > 5) break;
        }
        System.out.println();                    // 1 2 3 4 5 6

        System.out.println(isPrime(7));          // true
        System.out.println(isPrime(9));          // false
        System.out.println(isPrime(2));          // true

        for (int i = 1; i <= 10; i++) {
            if (i % 2 == 0) continue;            // skip even numbers
            System.out.print(i + " ");
        }
        System.out.println();                    // 1 3 5 7 9
    }
}
```

### Labelled `break` / `continue` and labelled blocks
```java
public class LabeledJumps {
    public static void main(String[] args) {
        // Unlabelled break: only the inner loop stops, rows are capped at 3 stars
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("*");
                if (j >= 3) break;
            }
            System.out.println();
        }
        // * / ** / *** / *** / ***

        // Labelled break: both loops stop at row 3
        outer:
        for (int i = 1; i <= 5; i++) {
            for (int j = 1; j <= i; j++) {
                System.out.print("#");
                if (i == 3 && j == 3) break outer;
            }
            System.out.println();
        }
        System.out.println();
        // # / ## / ###

        // Labelled continue: skip the rest of the row when j == 2
        rows:
        for (int i = 1; i <= 3; i++) {
            for (int j = 1; j <= 3; j++) {
                if (j == 2) continue rows;
                System.out.print(i + "" + j + " ");
            }
        }
        System.out.println();                    // 11 21 31

        first: {
            second: {
                System.out.println("Hello");     // Hello
                if (true) break first;
                System.out.println("never printed");
            }
            System.out.println("never printed either");
        }
        System.out.println("after first");       // after first
    }
}
```

## Common Pitfalls / Tips
- **Off-by-one errors.** `i <= 10` vs `i < 10`, and starting at 0 vs 1. Trace the first and last iteration by hand.
- **Missing update leads to an infinite loop.** Every `while`/`do-while` body must move toward making the condition false.
- **Braceless loop bodies.** Only the first statement belongs to the loop. Always use braces.
- **Stray semicolon.** `for (int i = 0; i < 5; i++);` or `while (cond);` has an empty body. With `while` this is usually an infinite loop.
- **`do-while` needs a trailing `;`** after `while (cond)`.
- **`for` header variables are scoped to the loop.** Declare the counter before the loop if you need it afterwards (as in the prime check).
- **Don't use `byte` counters.** `for (byte b = 0; b < 200; b++)` never ends, because `b` overflows from 127 to -128 before it reaches 200.
- **`continue` in a `while` loop can skip your increment.** If `i++` comes after the `continue`, it never runs on that path, which gives an infinite loop. A `for` loop's update always runs.
- **Unlabelled `break`/`continue` affect only the innermost loop.** Use a label to leave several levels at once, or move the nested loop into a method and `return`.
- **Prime checking:** test divisors only up to `i * i <= p`, and treat `p < 2` as not prime.

## Review Questions
1. What is the difference between `while` and `do-while`? Give a situation where `do-while` is the better choice.
2. List the three parts of a `for` header and the exact order in which they execute.
3. Why does `int i = 1; while (i++ <= 10) System.out.println(i);` print 2 to 11? How do you fix it?
4. Which parts of a `for` header may contain commas, and how do you combine two conditions?
5. How many times does the body of `for (i = 0; i < n; i++) for (j = 0; j < m; j++) { … }` run?
6. In the triangle pattern, why is the inner condition `j <= i` and not `j <= 5`? Why use `print` inside and `println` outside?
7. What do `break` and `continue` do? In nested loops, which loop do they affect, and how can you target the outer loop?
8. In the prime check, why must `i` be declared before the loop, and what does `i == p` after the loop tell you?

<details>
<summary>Answers</summary>

1. `while` checks the condition first and may run zero times. `do-while` runs the body once before checking, so it runs at least once. Example: showing a menu and repeating until the user chooses Exit.
2. Initialization (once), condition, body, update; then condition, body, update again, until the condition is false.
3. `i++ <= 10` compares the old value and then increments, so the body already sees the incremented value (2 on the first pass, 11 on the last). Fix: start at `i = 0` and use `i++ < 10`, or put `i++` at the end of the body.
4. Initialization and update can hold several comma-separated parts (`int i = 1, j = 1` / `i++, j += 2`). The condition must be one boolean expression, so combine with `&&` or `||`.
5. n × m times (n² if m = n).
6. Row `i` needs exactly `i` stars, so the inner loop depends on the outer counter. `print` keeps the stars on one line; `println()` after the inner loop ends the row.
7. `break` exits the loop immediately; `continue` skips the rest of the current iteration and starts the next one. Both act on the innermost enclosing loop. Label the outer loop (`outer:`) and use `break outer` / `continue outer`.
8. A variable declared in the `for` header only exists inside the loop. `i == p` means the loop ran to completion without `break`, i.e. no divisor was found, so `p` is prime.
</details>

## Further Study
- The enhanced for-each loop (`for (int x : arr)`) with arrays and collections
- Big-O notation and analysing nested loops
- More pattern-printing problems (pyramids, number patterns, diamonds)
- Efficient primality testing (√n bound, Sieve of Eratosthenes)
- Reading user input with `Scanner` to build real menu loops
- Variable scope and blocks (covered in depth later in the series)
- `return` as a jump statement, and `IntStream.range` as a stream-based alternative to counting loops

---

[← #07](07-java-conditional-statements.md) · [Index](ReadMe.md) · [#09 →](09-introduction-to-arrays.md)
