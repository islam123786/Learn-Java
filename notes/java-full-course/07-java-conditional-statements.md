# Java Conditional Statements | If, If-Else, If-Else-If Ladder, Switch Explained | Full Java Course #7
> Source: https://youtu.be/cdNhXVb5j7o · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 81:28. The video is in Hindi and has no English transcript, so these notes were written from the Hindi auto-transcript. It had no timing data either, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Code normally runs **top to bottom, line by line**. Java has three kinds of statements that change this flow: **selection** (`if`, `switch`), **iteration** (loops) and **jumps** (`break`, `continue`, …). This video covers selection.
- `if (condition)` runs a block only when the condition is `true`. The condition must be a `boolean`. `else` gives exactly two paths. Nested `if`s are allowed but hurt readability, so combine conditions with `&&` instead.
- An **if-else-if ladder** checks conditions from top to bottom and runs **only the first** one that is true. A chain of separate `if`s can run several of them.
- `switch` compares one value against constant `case` labels. Without `break`, execution **falls through** into the next case. Switch works on `byte`, `short`, `char`, `int` (plus their wrappers), enums, and `String` since Java 7. It can't test ranges or `boolean`s, and duplicate case labels don't compile.
- Why `switch` can be faster: the compiler turns dense case values into a **`tableswitch`** (an O(1) jump by index) and sparse values into a **`lookupswitch`** (sorted keys, searched in about O(log n)). An if-else ladder tests conditions one by one, which is O(n).

## Key Concepts

**Flow of control.** The order in which statements execute. By default it's sequential. *Why it matters:* every program that makes decisions or repeats work has to change this order.

**Selection statement.** A statement that chooses which code runs based on a condition (`if`, `if-else`, ladder, `switch`). *Why it matters:* it's how a program reacts to different inputs, like choosing the long scenic road home when you have time and the short busy one when you don't.

**Condition / boolean expression.** The expression inside `if (...)`. It must evaluate to `true` or `false`, e.g. `i == 5`, `i > 5 && i < 10`. *Why it matters:* Java won't accept an `int` as a condition the way C does.

**Nested if.** An `if` inside another `if`. An `else` always pairs with the **nearest preceding unmatched `if`**. *Why it matters:* deep nesting is hard to read. The video recommends at most two levels and combining conditions with `&&` where possible.

**If-else-if ladder.** `if … else if … else if … else`. Conditions are tested in order, the first true one runs, and the rest are skipped. The final `else` is the catch-all. *Why it matters:* it gives you multi-way decisions with exactly one outcome.

**`switch` statement.** Evaluates one expression and jumps to the matching `case` label, or to `default` if none match. `break` exits the switch. *Why it matters:* it's cleaner and potentially faster than a long ladder of equality checks.

**Fall-through.** Without `break`, execution continues into the following case bodies, including `default`, without checking their labels. *Why it matters:* it's a common bug, but you can also use it deliberately to share code between cases.

**Jump table (`tableswitch` / `lookupswitch`).** How `javac` compiles a switch. Dense case values become an index-based table; sparse values become a sorted key list. *Why it matters:* it explains the "switch is more optimized" claim and its limits.

## Detailed Notes

### Flow of control [00:00]
- After `javac` compiles to bytecode and the JVM interprets/JIT-compiles it, statements run one after another: statement 1, 2, 3, …
- Some keywords create an *alternate* flow. Java's three categories are **selection** (`if`, `switch`), **iteration** (loops, next video) and **jumps** [02:14].

### The `if` statement [03:21]
- Analogy: on the way home the road splits. The long road is pleasant, the short one has traffic and potholes. Which one you take depends on a parameter (how much time you have). Selection statements represent that choice in code.
- Syntax [06:42]: `if (expression) { … }`. The block runs only if the expression is `true`; otherwise it is skipped and execution continues after it.
- The expression must evaluate to a `boolean`, e.g. `b == true` for a `boolean b`, or `i == 5`.

### `if-else` [09:00]
- `if (cond) { A } else { B }`: exactly one of A or B runs.
- Demo [11:11]: with only an `if` followed by an unconditional print, `i = 5` printed both "i is 5" and "i is not 5". Moving the second print into an `else` fixes it.
- Any comparison works, e.g. `i > 5` / else `i <= 5`, and compound conditions such as `i > 5 && i < 10` [15:39]. The whole expression just has to end up as a `boolean`.
- Exercise [15:39]: odd or even with `i % 2 == 0`. Even numbers leave remainder 0, odd numbers remainder 1.

### Optional braces [17:53]
- If a branch has **one** statement, the `{ }` can be omitted.
- Good practice: always use braces. Statements tend to get added later, and leaving them out invites bugs.

### Nested `if` [19:00]
- An `if` inside another `if`'s block, e.g. `if (i > 5) { if (i < 10) { … } else { … } } else { … }`.
- Each `else` belongs to the nearest preceding `if`. Every `if` may or may not have its own `else`.
- Trace with `i = 8` [22:22]: `i > 5` is true, so go in. `i < 10` is true, so go in. `i == 7` is false and that inner `if` has no else, so nothing happens. Leaving, the outer `else` blocks are skipped because their `if`s were true.
- Java has no limit on nesting depth, but keep it to about two levels for readability.
- Better [24:36]: `if (i > 5 && i < 10)` replaces two nested levels.

### If-else-if ladder [25:43]
- For more than two choices: `if (i == 5) … else if (i == 6) … else if (i == 7) … else …`.
- Conditions are tested top to bottom. As soon as one is true, its block runs and the **rest of the ladder is skipped**, even if later conditions would also be true.
- Example [31:18], age 50: `> 80` "very old", `> 60` "old", `> 40` "becoming old", `> 20` "young", `else` "child". It prints only "You are becoming old".
- With **independent `if`s** instead of `else if` [35:47], age 50 matches both `> 40` and `> 20`, so two lines print. That's why the ladder (and the order of its conditions) matters.

### `switch`: syntax [36:54]
```
switch (expression) {
    case 1:  statements; break;
    case 2:  statements; break;
    case 3:  statements; break;
    default: statements; break;
}
```
- `case 1:` means roughly "if expression == 1". `default` plays the role of the final `else`.
- Demo [44:44]: `i = 3` prints "i is 3"; `i = 5` falls to `default`, "i is greater than 3".

### `break` and fall-through [48:05]
- With every `break` removed and `i = 1`, the output is all four lines: "i is 1", "i is 2", "i is 3", "i is greater than 3".
- After a matching case, execution simply runs on into the next case bodies **without comparing their labels**, until a `break` or the end of the switch.
- Why Java allows it [50:19]: sometimes you deliberately want one case to run its own code plus the code of the cases below it. `break` gives you that control.

### Why have both `switch` and if-else-if? [51:26]
- Any classic switch can be rewritten as an if-else-if ladder. So why does switch exist? Because in some cases it is **more efficient**.
- Then why not always use switch? Because its use cases are **limited** [52:33]:
  - The switch expression must be `byte`, `short`, `char` or `int` (or their wrapper classes), an **enum**, or (since Java 7) a **`String`** [55:55]. `long`, `float`, `double` and `boolean` are not allowed: `switch (i > 4)` fails with "Cannot switch on a value of type boolean".
  - **No duplicate case labels** [54:48]: a second `case 1:` is a compile error ("Duplicate case label"). An if-else ladder can repeat a condition; the second one just never runs.
  - Case labels must be **compile-time constants** (literals or `final` constants), not variables or ranges.
- Since **Java 14**, switch has been greatly enhanced (arrow `case ->` labels, switch *expressions*, `yield`). The series will cover it after core Java. The classic syntax is still fully valid.

### Switch vs if-else-if: equality only [58:09]
- `switch` only tests **equality** against constants. `if` can test anything: `<`, `>`, ranges, method calls, combinations.

### Why switch can be faster: jump tables [61:30]
- If-else ladder with `i = 2`: test `i == 1` (false), then `i == 2` (true), so two evaluations. For `i = 3` it's three. In the worst case that's **O(n)** comparisons.
- Switch [63:45]: the compiler builds a **jump table**, which works like an array indexed by the case value. Case 1's code is at index 0, case 2's at index 1, and so on, with anything out of range going to `default`. The value is used to jump straight to the right code: **O(1)**.
- **Not always a table** [68:13]: if the case values are **sparse** (e.g. 1, 1000, 100000), an indexed table would need about 100,000 mostly empty slots. So there are two forms [71:34]:
  - **`tableswitch`** for **dense** values: direct index, O(1).
  - **`lookupswitch`** for **sparse** values: only the existing keys, stored **sorted**, and the matching key is found by binary search, about **O(log n)**.
- Both beat a linear ladder.
- *Correction/nuance:* `javac` chooses between the two bytecode instructions; the JVM specification only requires `lookupswitch` keys to be sorted (binary search is the typical implementation). In practice the JIT compiler also optimizes if-else chains, so for a handful of cases the speed difference is negligible. Choose `switch` mainly because it is clearer for "one value, many constant cases".

### Nested switch [76:03]
- A `switch` can sit inside a `case` of another switch (e.g. switch on `j`, and inside `case 5:` switch on `k`), to any depth.
- As with `if`, avoid deep nesting.

*Note:* video #6 said the ternary operator would be covered with conditionals, but this video doesn't cover it. It's included below for completeness.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### `if`, `if-else`, odd/even, compound condition
```java
public class IfElseBasics {
    public static void main(String[] args) {
        int i = 6;

        if (i == 5) {
            System.out.println("i is 5");
        } else {
            System.out.println("i is not 5");   // i is not 5
        }

        if (i % 2 == 0) {
            System.out.println("i is even");    // i is even
        } else {
            System.out.println("i is odd");
        }

        if (i > 5 && i < 10) {                  // replaces a nested if
            System.out.println("i is between 6 and 9"); // i is between 6 and 9
        }
    }
}
```

### Nested `if` and which `else` belongs to which `if`
```java
public class NestedIf {
    public static void main(String[] args) {
        int i = 8;
        if (i > 5) {
            if (i < 10) {
                if (i == 7) {
                    System.out.println("i is 7");
                }
                System.out.println("5 < i < 10");     // 5 < i < 10
            } else {
                System.out.println("i >= 10");
            }
        } else {
            System.out.println("i <= 5");
        }

        // Dangling else: without braces, the else binds to the NEAREST if
        int x = 3;
        if (x > 5)
            if (x < 10)
                System.out.println("between");
            else
                System.out.println("x >= 10");  // belongs to if (x < 10), not if (x > 5)
        System.out.println("done");             // done (nothing else printed)
    }
}
```

### If-else-if ladder vs independent `if`s
```java
public class AgeLadder {
    public static void main(String[] args) {
        int age = 50;

        if (age > 80) {
            System.out.println("You are very old");
        } else if (age > 60) {
            System.out.println("You are old");
        } else if (age > 40) {
            System.out.println("You are becoming old"); // You are becoming old
        } else if (age > 20) {
            System.out.println("You are young");        // skipped: ladder already matched
        } else {
            System.out.println("You are a child");
        }

        System.out.println("---");                      // ---

        // Independent ifs: every one is tested
        if (age > 40) System.out.println("You are becoming old"); // You are becoming old
        if (age > 20) System.out.println("You are young");        // You are young
    }
}
```

### Classic `switch`, `default`, and fall-through
```java
public class SwitchDemo {
    static void describe(int i) {
        switch (i) {
            case 1:
                System.out.println("i is 1");
                break;
            case 2:
                System.out.println("i is 2");
                break;
            case 3:
                System.out.println("i is 3");
                break;
            default:
                System.out.println("i is greater than 3");
                break;
        }
    }

    static void fallThrough(int i) {
        switch (i) { // no breaks
            case 1: System.out.println("i is 1");
            case 2: System.out.println("i is 2");
            case 3: System.out.println("i is 3");
            default: System.out.println("i is greater than 3");
        }
    }

    public static void main(String[] args) {
        describe(3);     // i is 3
        describe(5);     // i is greater than 3
        fallThrough(1);  // i is 1 / i is 2 / i is 3 / i is greater than 3 (4 lines)
        fallThrough(3);  // i is 3 / i is greater than 3
    }
}
```

### Deliberate fall-through, `String` and `char` switch, nested switch
```java
public class SwitchVariety {
    public static void main(String[] args) {
        int month = 4;
        int days;
        switch (month) {           // grouping cases via fall-through
            case 4: case 6: case 9: case 11:
                days = 30;
                break;
            case 2:
                days = 28;
                break;
            default:
                days = 31;
        }
        System.out.println(days);  // 30

        String cmd = "stop";       // String switch: Java 7+
        switch (cmd) {
            case "start": System.out.println("Starting"); break;
            case "stop":  System.out.println("Stopping"); break; // Stopping
            default:      System.out.println("Unknown");
        }

        char grade = 'B';
        switch (grade) {
            case 'A': System.out.println("Excellent"); break;
            case 'B': System.out.println("Good"); break;          // Good
            default:  System.out.println("Keep going");
        }

        int j = 5, k = 1;
        switch (j) {               // nested switch
            case 4:
                System.out.println("j is 4");
                break;
            case 5:
                switch (k) {
                    case 1: System.out.println("j is 5 and k is 1"); break; // j is 5 and k is 1
                    case 2: System.out.println("j is 5 and k is 2"); break;
                }
                break;
            default:
                System.out.println("j is not 4 or 5");
        }
    }
}
```

### Modern switch (Java 14+) and the ternary operator
```java
public class ModernSwitch {
    public static void main(String[] args) {
        int i = 2;

        // Arrow labels: no fall-through, no break needed
        switch (i) {
            case 1 -> System.out.println("one");
            case 2, 3 -> System.out.println("two or three"); // two or three
            default -> System.out.println("other");
        }

        // Switch expression: produces a value
        String size = switch (i) {
            case 1 -> "small";
            case 2 -> "medium";
            default -> {
                String s = "large";
                yield s;
            }
        };
        System.out.println(size); // medium

        // Ternary operator: condition ? valueIfTrue : valueIfFalse
        int n = 7;
        String parity = (n % 2 == 0) ? "even" : "odd";
        System.out.println(parity); // odd
    }
}
```

## Common Pitfalls / Tips
- **Forgotten `break`** is the classic switch bug: execution falls into the next case. If fall-through is intentional, add a `// fall through` comment. Arrow-style `case ->` never falls through.
- **Order matters in a ladder.** Put the most specific or largest thresholds first (`> 80` before `> 60`). If `> 20` came first, everyone over 20 would be "young".
- **`if (x = 5)`** doesn't compile for `int`s, but `if (flag = false)` with a `boolean` compiles and assigns. Write `if (!flag)` instead of `== false`.
- **Stray semicolon.** `if (cond); { … }` makes the block run **always**, because the `;` is an empty statement that serves as the if body.
- **Dangling `else`.** Without braces, an `else` binds to the nearest `if`. Always use braces.
- **Switch limits.** No `long`/`float`/`double`/`boolean`, no ranges, labels must be constants, no duplicates. A `switch` on a `null` `String` or wrapper throws `NullPointerException`.
- **Compare `String`s with `equals`**, not `==`, in `if` conditions. `switch` on strings uses `equals` internally.
- **Prefer readability over micro-optimisation.** Use `switch` when testing one value against many constants, and `if` for ranges and complex conditions.

## Review Questions
1. What are Java's three categories of control-flow statements, and which one does this video cover?
2. What type must the expression inside `if (...)` have? Is `if (5)` legal in Java?
3. For `age = 50`, what is printed by an if-else-if ladder with `> 40` and `> 20` branches, and what is printed by two independent `if`s? Why do they differ?
4. In a nested `if` without braces, which `if` does an `else` belong to?
5. What happens in a `switch` when a matching case has no `break`?
6. Which types can a `switch` expression have? Name three things a classic `switch` can't do that an `if` can.
7. Explain `tableswitch` and `lookupswitch`. When does the compiler choose each, and what is the cost of each lookup?
8. Why does Java keep the if-else-if ladder if `switch` can be faster?

<details>
<summary>Answers</summary>

1. Selection (`if`, `switch`), iteration (loops) and jumps (`break`, `continue`, `return`). This video covers selection.
2. It must be a `boolean`. `if (5)` doesn't compile in Java (unlike C/C++).
3. The ladder prints only "You are becoming old": the first true branch runs and the rest are skipped. Independent `if`s print both "You are becoming old" and "You are young", because each `if` is tested separately.
4. The nearest preceding `if` that doesn't already have an `else`.
5. Execution falls through into the following case bodies (and `default`) without checking their labels, until it hits a `break` or the end of the switch.
6. `byte`, `short`, `char`, `int` (and `Byte`, `Short`, `Character`, `Integer`), enums, and `String` (Java 7+). A classic switch can't test ranges or relational conditions (`i > 4`), can't switch on `boolean`/`long`/`double`, and can't use non-constant or duplicate case labels.
7. `tableswitch` is used for dense case values: the value (minus the lowest label) indexes a table directly, O(1). `lookupswitch` is used for sparse values: only the existing keys are stored, sorted, and the match is found by search (typically binary search, about O(log n)). This avoids a huge, mostly empty table.
8. Because `switch` only handles equality against constants of certain types. Ranges, compound boolean conditions, `double`s, method calls and so on need `if`.
</details>

## Further Study
- Loops and jump statements (`for`, `while`, `do-while`, `break`, `continue`): next video
- Java 14+ switch expressions, `yield`, and Java 21 pattern matching for `switch`
- The ternary operator and when it improves (or hurts) readability
- Enums and why they pair well with `switch`
- `tableswitch`/`lookupswitch` in the JVM spec; inspect bytecode with `javap -c`
- Binary search and Big-O notation (O(1), O(log n), O(n))
- Guard clauses / early returns as an alternative to deep nesting

---

[← #06](06-java-operators.md) · [Index](../../ReadMe.md) · [#08 →](08-java-loops-and-jump-statements.md)
