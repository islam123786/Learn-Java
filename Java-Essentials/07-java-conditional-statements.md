# 07 · Conditional Statements · if, if-else, if-else-if, switch
> Full notes: [07-java-conditional-statements.md](../07-java-conditional-statements.md) · [▶ Video](https://youtu.be/cdNhXVb5j7o)

## Flow of control
- Statements normally run one after another. Three kinds of statements change that flow: **selection** (`if`, `switch`), **iteration** (loops) and **jumps** (`break`, `continue`).

## `if` and `if-else`
- `if (cond) { … }` runs the block only when `cond` is `true`. The condition must be a `boolean`.
- `if (cond) { A } else { B }` runs exactly one of A or B.
- Compound conditions are fine: `if (i > 5 && i < 10)`.
- Odd or even: `i % 2 == 0` means even.
- **Braces:** they're optional for a single statement, but always use them anyway.

## Nested `if`
- An `if` inside another `if`. Each `else` belongs to the **nearest** `if` before it.
- Keep it to about 2 levels. Often `&&` replaces nesting: `if (i > 5 && i < 10)`.

## If-else-if ladder
- Conditions are checked top to bottom. The **first true one runs, and the rest are skipped**.
- Example with age = 50: `> 80`, `> 60`, `> 40`, `> 20`, `else`. Only "becoming old" prints.
- With separate `if`s instead, both `> 40` and `> 20` would print. The order of conditions matters.

## `switch`
```java
switch (expression) {
    case 1:  statements; break;
    case 2:  statements; break;
    default: statements; break;   // like the final else
}
```
- **Fall-through:** without `break`, execution keeps running into the next cases **without checking their labels**, until it reaches a `break` or the end. Use it when you want several cases to share code.

---

## Why have both `switch` and if-else-if?

Any classic `switch` can be rewritten as an if-else-if ladder.
- **Why `switch` exists:** in some cases it's **more efficient** (see the jump tables below) and clearer.
- **Why we don't always use it:** `switch` is **limited**.

| | `switch` | if-else-if |
| :--- | :--- | :--- |
| **Allowed types** | `byte`, `short`, `char`, `int` (and their wrappers), **enum**, **`String`** (Java 7+) | anything that gives a `boolean` |
| **Not allowed** | `long`, `float`, `double`, `boolean`. `switch (i > 4)` fails with *"Cannot switch on a value of type boolean"* | — |
| **What it tests** | **equality** only | `<`, `>`, ranges, method calls, `&&`/`\|\|` combinations |
| **Case values** | must be **compile-time constants** (literals or `final`), not variables or ranges | any expression |
| **Duplicates** | duplicate `case 1:` is a compile error (*"Duplicate case label"*) | allowed (the repeated condition just never runs) |
| **Speed** | jump table: O(1) or O(log n) | checks one condition at a time: O(n) |

- Since **Java 14**, `switch` also supports arrow labels (`case ->`), switch *expressions* and `yield`. The classic syntax is still valid.

## Why `switch` can be faster: jump tables

**If-else-if ladder:** tests each condition one after another.
- `i = 2` takes 2 checks and `i = 3` takes 3. In the worst case that's **O(n)**.

**`switch`:** the compiler builds a **jump table**, which works like an array indexed by the case value.
```
case value:   1        2        3        (other)
index:       [0]      [1]      [2]
              ↓        ↓        ↓          ↓
           code 1   code 2   code 3    default
```
- The value is used as an index to jump **straight** to the right code: **O(1)**.

**It's not always a table.** If the case values are **sparse** (e.g. 1, 1000, 100000), an indexed table would need about 100,000 mostly empty slots. So `javac` picks one of two bytecode instructions:

| Instruction | Used when | How it finds the case | Cost |
| :--- | :--- | :--- | :--- |
| **`tableswitch`** | case values are **dense** (close together) | direct index | **O(1)** |
| **`lookupswitch`** | case values are **sparse** | only the real keys are stored, **sorted**, and searched with binary search | about **O(log n)** |

- Both are faster than a linear ladder.
- **In practice:** the JIT also optimizes if-else chains, so for a few cases the difference is negligible. Pick `switch` mainly because it's clearer for "one value, many constant cases".

---

## Nested switch
- A `switch` can sit inside a `case` of another switch. As with `if`, avoid deep nesting.

**Remember:** use `if` for conditions and ranges, and `switch` for one value checked against many constants. `switch` compiles to a jump table (`tableswitch` O(1), `lookupswitch` O(log n)). Don't forget `break`.
