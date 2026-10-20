# Java Operators | Arithmetic + Unary + Logical + Bitwise + Assignment Operators | Java Full Course #6
> Source: https://youtu.be/Tdq4fmDCJH8 · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 128:10. The video is in Hindi and has no English transcript, so these notes were written from the Hindi auto-transcript. It had no timing data either, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- Java's operators come in families: **arithmetic** (`+ - * / %`, compound `+= -=` …, `++ --`), **relational** (`== != < > <= >=`, always give a `boolean`), **bitwise** (`& | ^ ~ << >> >>>`), **logical** (`&& || !`), **assignment** (`=`), and the **ternary** `?:` (saved for the conditionals video).
- `=` **assigns** (right side into left side); `==` **compares**. Assignment is right-associative, so `a = b = c = 10` works.
- **Postfix** `j++` hands out the old value and then increments; **prefix** `++j` increments first and hands out the new value.
- Shifts never run on `byte`/`short`: the operand is first **promoted to `int`** (or stays `long`). Casting the result back to `byte` just keeps the low 8 bits, which explains the "weird" negative and zero results. The shift distance is taken **mod 32** for `int` (mod 64 for `long`), so `1 << 32 == 1`.
- `&&` and `||` **short-circuit** (they skip the right side when the left side already decides the result). `&` and `|` also work on booleans and **always evaluate both sides**, which is a popular interview question.

## Key Concepts

**Operator and operand.** An operator is a symbol that performs an operation; operands are the values it works on. *Unary* operators take one operand (`-x`, `!b`, `~x`, `i++`), *binary* take two (`a + b`), and the *ternary* `?:` takes three. *Why it matters:* the operand count tells you how to read an expression.

**Expression.** Any combination of variables, literals and operators that produces a value, e.g. `a < b` (a `boolean`) or `a * b + 2` (an `int`). *Why it matters:* the result type decides where you can use it (an `if` needs a `boolean`).

**Assignment vs equality.** `x = y` copies y's value into x. `x == y` asks "are they equal?" and gives `true`/`false`. *Why it matters:* in maths `=` is a comparison; in Java it is an action.

**Compound assignment.** `h += 2` means `h = h + 2`; the same pattern exists for `-= *= /= %= &= |= ^= <<= >>= >>>=`. *Why it matters:* it's shorter and is what real code uses. It also includes a hidden cast (see Pitfalls).

**Increment / decrement.** `i++`/`++i` add 1, `i--`/`--i` subtract 1. Prefix vs postfix only matters when the expression's value is *used* (e.g. `k = j++`). *Why it matters:* loops and counters use them constantly.

**Relational operators.** `== != < > <= >=` compare two values and always return a `boolean`. *Why it matters:* they are the building blocks of conditions in `if`, `while` and `for`.

**Bitwise operators.** They work on the individual bits of integer values: `&` (both 1), `|` (at least one 1), `^` (XOR: 1 when the bits differ, i.e. an odd number of 1s), `~` (flip every bit). *Why it matters:* flags, masks, fast parity checks and many DSA tricks.

**Truth table.** A table listing an operator's result for every input combination (00, 01, 10, 11). *Why it matters:* it's the quickest way to remember what `& | ^ ~` do.

**Shift operators.** `<<` moves bits left and fills with 0 (multiplies by 2 per step). `>>` moves bits right and fills with copies of the sign bit (an "arithmetic" or "smart" shift; divides by 2 per step, rounding down). `>>>` moves bits right and always fills with 0 (a "logical" or, in the video's words, "dumb" shift). *Why it matters:* shifts are fast powers of two, and `>>>` lets you treat an `int` as unsigned.

**Type promotion in expressions.** `byte`, `short` and `char` are promoted to `int` before any arithmetic, bitwise or shift operation. *Why it matters:* `byte b = b << 1;` doesn't compile, and casting the result back to `byte` truncates to the low 8 bits.

**Logical operators and short-circuiting.** `&&` is true only if both sides are true; `||` is true if either side is. If the left side already decides the answer (`false && …`, `true || …`), Java doesn't evaluate the right side. *Why it matters:* it lets you write guards like `s != null && s.length() > 0` safely.

**Operator precedence.** Rules for which operator binds first when there are no parentheses. Parentheses override everything. *Why it matters:* you rarely need to memorise the table, but you must know that assignment comes last and `*` binds tighter than `+`.

## Detailed Notes

### What operators are [01:09]
- Same idea as maths operators: as soon as you go one step beyond declaring variables you need them.
- Plan for the video: arithmetic first, then relational, bitwise (with shifts in depth), logical, assignment, a teaser for the ternary operator, and precedence.

### Arithmetic operators [02:19]
- The basic five: `+`, `-`, `*`, `/`, `%`. Modulus gives the **remainder** of a division.
- Demo [03:29] with `a = 5, b = 10`: `a + b = 15`, `a - b = -5`, `a * b = 50`, `b / a = 2`, `b % a = 0`.

### `=` as assignment, and compound operators [06:58]
- `h = h + 2` is nonsense in maths but normal in code: evaluate the right side, then store the result in the left variable.
- Because this pattern is so common, Java (like C/C++) offers shortcuts: `h += 2`, `h -= 2`, `h *= 3`, `h /= 5`, `h %= 5`.
- The video says these are "a bit faster". *Correction:* the compiler produces the same code; the real benefits are brevity and an implicit cast (see Pitfalls).

### Increment and decrement [11:38]
- `i++` is shorthand for `i += 1`, which is shorthand for `i = i + 1`. `i--` is the opposite.
- There is no `**` or `//` operator: adding or subtracting 1 (moving to the next/previous index) is so common in loops and arrays that only those two got special operators.

### Prefix vs postfix [15:07]
- `++j` is **prefix** (operator first), `j++` is **postfix** (operator after). Standing alone on a line, both just add 1.
- The difference shows up when the value is used [17:27]:
  - `k = j++;` behaves like `k = j; j = j + 1;`. With `j = 9`: `j` becomes 10, `k` gets **9**.
  - `l = ++j;` behaves like `j = j + 1; l = j;`. With `j = 10`: `j` becomes 11, `l` gets **11**.
- The same rules apply to `--`. This matters later in loops.

### Relational operators [23:16]
- `==`, `!=`, `<`, `>`, `<=`, `>=`. Each one asks the compiler a yes/no question ("is a equal to b?") and gives back a `boolean`.
- `a == b` compares; `a = b` copies b into a (afterwards printing `a` shows 10).
- Things like `a < b` or `a * b` are called **expressions**: they compute a result that you can store, e.g. `boolean c = (a == b);`.
- Demo [29:05] (the video also shows the VS Code shortcut `psvm` + Tab for `public static void main`):
  - `a = 5, b = 10`: `a == b` is `false`, `a != b` is `true`.
  - With `a = b = 10`: `a <= b` is `true`, `a < b` is `false`, `a != b` is `false`, `a == b` is `true`. `<=` is true when **either** part holds; `<` is strict.

### Bitwise operators: the list [33:44]
- `&` (AND), `|` (OR), `^` (XOR), `~` (NOT), `<<` (left shift), `>>` (right shift), `>>>` (right shift with zero fill).
- Each binary one also has a compound form: `&=`, `|=`, `^=`, `<<=`, `>>=`, `>>>=`.
- An operation on two integers gives an **integer** result (not a `boolean`): it works on their bits, not on their decimal value.
- Example: 2 is `0000 0010`, 3 is `0000 0011` (shown with 8 bits for readability; an `int` really has 32).

### Truth tables for `& | ^ ~` [39:33]
| a | b | a & b | a \| b | a ^ b | ~a |
|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 0 | 1 |
| 0 | 1 | 0 | 1 | 1 | 1 |
| 1 | 0 | 0 | 1 | 1 | 0 |
| 1 | 1 | 1 | 1 | 0 | 0 |
- AND: 1 only if both are 1. OR: 1 if at least one is 1. XOR: 1 if there is an **odd** number of 1s (i.e. the bits differ). NOT is unary and flips the bit.
- These are the same rules as logic gates in the processor; the operators let you use them from code.

### Left shift `<<` [45:23]
- `b << 1` moves every bit one place left; the leftmost bit falls off and a 0 enters on the right (LSB).
- 8 → 16 → 32 → 64: each shift doubles the number, because every 1 moves into a position worth twice as much. 10 (`1010`) becomes 20 (`10100`). In general `x << n` equals `x * 2^n` (until bits overflow).
- `b << 2` shifts two places at once and brings in two zeros.
- With 8 bits, shifting 64 once more puts the 1 in the MSB, the sign bit, so the byte reads as **-128** (two's complement). Byte range is -128 to 127. One more shift and the 8-bit view becomes 0, and stays 0 however much you shift.

### The full story: promotion to `int` [59:20]
- Recall type promotion: `byte b = 5; b = b * 5;` doesn't compile because `b * 5` is an `int`. You need `b = (byte)(b * 5);`.
- Shift operators work **only on `int` and `long`**. A `byte` or `short` operand is promoted to `int` first, and you must cast the result back: `b = (byte)(b << 7);`.
- Walk-through with `byte b = 1`:
  - `(byte)(b << 7)`: as an `int`, `1 << 7` is +128 (bit 7 set, sign bit 31 is still 0). Casting to `byte` keeps the low 8 bits, `1000 0000`, which reads as **-128**.
  - Now `b << 1` again: promoting -128 to `int` **sign-extends** it (24 leading 1s, so the value stays -128). Shifting gives `0xFFFFFF00`, which is **-256** as an `int`. Cast back to `byte`, the low 8 bits are all 0, so you see **0**.
  - So the bits weren't "lost"; storing in a `byte` truncated them. `int i = b << 1;` shows -256.

### Shifting an `int`, `Integer.MIN_VALUE`, and the mod-32 rule [77:58]
- `1 << 30` sets bit 30 (a large positive number, 1073741824). `1 << 31` sets the sign bit: **-2147483648**, which is `Integer.MIN_VALUE`, the smallest `int`. (The video calls it "negative infinity" informally; it is just the minimum value.)
- Shifting a bit past position 31 would make no sense, so Java uses only the low 5 bits of the shift distance: effectively `n % 32` [82:37]. `i << 32` equals `i << 0` (unchanged) and `i << 33` equals `i << 1`.
- *Correction:* the video says the same mod-32 rule applies to `long`. For `long` the distance is taken mod **64**.

### Right shift `>>` [86:06]
- Moves bits right; the LSB falls off. The new MSB copies the **old sign bit**, so negative numbers stay negative.
- 8 → 4 → 2 → 1: each step halves the number. -128 → -64 → -32 → … → -1.
- Because the LSB is what falls off, using `>>` on a byte (with a cast back) still gives the result you expect, unlike `<<`. The mod-32 rule applies here too.

### Unsigned right shift `>>>` [91:55]
- Always shifts in a **0**, whatever the sign was. The video calls `>>` "smart" and `>>>` "dumb".
- On a 32-bit `int`, `-128 >>> 1` becomes a large positive number, because the sign bit is replaced by 0.
- Use case: values where negatives make no sense, e.g. treating pixel data or hash codes as unsigned.
- *Correction:* the video shows `byte -128 >>> 1` as +64 in an 8-bit picture. Because of promotion, Java actually computes this on the 32-bit `int` (`0xFFFFFF80 >>> 1 = 0x7FFFFFC0`), and casting back to `byte` keeps `0xC0`, which is **-64**. `>>>` only behaves "unsigned" at full `int`/`long` width.

### Bitwise demo [94:15]
- `a = 2, b = 3` gives `a & b = 2`, `a | b = 3`, `a ^ b = 1`, `~a = -3`.
- Why `~2` is -3: all 32 bits flip, the leading zeros become ones, so the number becomes negative. In general `~x == -x - 1`.
- Shifts [98:54]: `g = 1; g << 1` is 2; `1 << 30` is 1073741824; `1 << 31` is negative; `1 << 32` is 1 again; `1 << 33` is 2.
- `byte h = 1; h = h << 1;` gives "Type mismatch: cannot convert from int to byte". `h = (byte)(h << 1);` fixes it and gives 2.

### Logical operators `&&` `||` `!` [104:44]
- They work on **boolean expressions**, not on bits: `boolean d = a < b && a < c;`.
- Same truth table as `&`/`|`, with true/false in place of 1/0.
- `!` (logical NOT) was already seen with `!=`.

### Short-circuit evaluation [109:23]
- `A && B`: if A is false the result must be false, so B is **never evaluated**.
- `A || B`: if A is true the result must be true, so B is skipped.
- Used for guards such as null checks and in loop conditions where the second check must not run if the first one fails.

### `&` and `|` on booleans: no short-circuit [112:52]
- In Java, `&` and `|` also accept boolean operands. The result is the same as `&&`/`||`, but **both sides are always evaluated**.
- You almost never want this in practice (the video says 99% of code uses `&&`/`||`), but it's a classic interview question, and it matters when the right side has a side effect such as `i++`.
- Demo [115:12] with `a = 5, b = 10, c = 15`: `a < b && b < c` is `true`; with `a = 25` it is `false` and `b < c` is skipped. With `&`, the result is still `false`, but both comparisons run.

### Assignment operator and chaining [117:32]
- `=` stores the right-hand value into the left-hand variable. It is the last thing to happen in a statement.
- Chaining: `int a, b, c; a = b = c = 10;` evaluates right to left: 10 goes into c, then c into b, then b into a.
- *Correction:* the video writes `int a = b = c = 10;`. That doesn't compile unless b and c were declared earlier. Declare them first, as above.

### Ternary operator (preview) [121:01]
- The only operator with three operands (`cond ? x : y`). It's covered with if/else in the next video.

### Operator precedence [122:11]
- Like BODMAS in maths, Java has a precedence table (highest first, simplified): postfix `x++ x--` → unary/prefix `++x --x + - ! ~` and casts → `* / %` → `+ -` → `<< >> >>>` → `< > <= >= instanceof` → `== !=` → `&` → `^` → `|` → `&&` → `||` → `?:` → assignment `= += …`.
- Operators in the same row are evaluated **left to right**, except assignment (and the ternary), which go **right to left**.
- *Correction:* the video's BODMAS remark ("division first, then multiplication") doesn't apply to Java. `*`, `/` and `%` have equal precedence and go left to right, e.g. `8 / 2 * 4 == 16`.
- Practical advice [124:31]: nobody memorises the table. Use **parentheses**: `(b + c) * d` instead of relying on the rules.

## Code Examples

Each block is a complete program: save it under the class's name and run it with `java ClassName.java`.

### Arithmetic, compound assignment, prefix/postfix
```java
public class ArithmeticOps {
    public static void main(String[] args) {
        int a = 5, b = 10;
        System.out.println((a + b) + ", " + (a - b) + ", " + (a * b) + ", " + (b / a) + ", " + (b % a));
        // 15, -5, 50, 2, 0

        int h = a + 2;   // 7
        h += 2;          // 9
        h -= 2;          // 7
        h *= 3;          // 21
        h /= 5;          // 4 (integer division)
        h %= 5;          // 4
        System.out.println(h); // 4

        int j = 9;
        int k = j++;     // k gets old value, then j increments
        System.out.println(j + ", " + k); // 10, 9
        int l = ++j;     // j increments first, l gets new value
        System.out.println(j + ", " + l); // 11, 11

        System.out.println(7 / 2 + " " + 7 % 2 + " " + -7 / 2 + " " + -7 % 2); // 3 1 -3 -1
    }
}
```

### Relational operators and `=` vs `==`
```java
public class RelationalOps {
    public static void main(String[] args) {
        int a = 5, b = 10;
        boolean c = (a == b);
        boolean d = (a != b);
        System.out.println(c + " " + d);  // false true

        a = 10; // now a and b are equal
        System.out.println((a <= b) + " " + (a < b) + " " + (a != b) + " " + (a == b));
        // true false false true

        int x = 5, y = 10;
        x = y;                 // assignment: copy y into x
        System.out.println(x); // 10
    }
}
```

### Bitwise AND, OR, XOR, NOT
```java
public class BitwiseOps {
    public static void main(String[] args) {
        int a = 2, b = 3; // 10 and 11 in binary
        System.out.println((a & b) + ", " + (a | b) + ", " + (a ^ b) + ", " + (~a)); // 2, 3, 1, -3
        System.out.println(Integer.toBinaryString(~a)); // 11111111111111111111111111111101
        System.out.println(~a == -a - 1);               // true

        int n = 7;
        System.out.println((n & 1) == 1 ? "odd" : "even"); // odd
    }
}
```

### Shifts on `int`: doubling, MIN_VALUE, and the mod-32 rule
```java
public class ShiftOps {
    public static void main(String[] args) {
        int g = 1;
        System.out.println(g << 1);   // 2
        System.out.println(8 << 1);   // 16
        System.out.println(10 << 1);  // 20
        System.out.println(g << 30);  // 1073741824
        System.out.println(g << 31);  // -2147483648
        System.out.println((g << 31) == Integer.MIN_VALUE); // true
        System.out.println(g << 32);  // 1  (32 % 32 == 0)
        System.out.println(g << 33);  // 2  (33 % 32 == 1)
        System.out.println(1L << 32); // 4294967296 (long uses % 64)

        System.out.println(8 >> 1);      // 4
        System.out.println(-128 >> 1);   // -64  (sign bit copied)
        System.out.println(-128 >>> 1);  // 2147483584 (zero filled)
        System.out.println(-7 >> 1);     // -4  (rounds toward negative infinity)
    }
}
```

### Shifts on `byte`: promotion and truncation
```java
public class ByteShift {
    public static void main(String[] args) {
        byte b = 1;
        // b = b << 7;           // does not compile: int cannot be converted to byte
        b = (byte) (b << 7);     // int 128 -> low 8 bits 1000_0000
        System.out.println(b);   // -128

        int asInt = b << 1;      // b is sign-extended to int first
        System.out.println(asInt);              // -256
        System.out.println((byte) (b << 1));    // 0 (low 8 bits are all zero)

        byte h = 1;
        h <<= 1;                 // compound assignment casts for you
        System.out.println(h);   // 2

        byte m = -128;
        System.out.println((byte) (m >> 1));    // -64
        System.out.println((byte) (m >>> 1));   // -64, not +64: shift happened on the 32-bit int
        System.out.println((m & 0xFF) >>> 1);   // 64  (mask to get the unsigned byte value first)
    }
}
```

### Logical operators and short-circuiting
```java
public class LogicalOps {
    static int calls = 0;

    static boolean check(boolean result) {
        calls++;
        return result;
    }

    public static void main(String[] args) {
        int a = 5, b = 10, c = 15;
        System.out.println(a < b && b < c); // true
        System.out.println(a > b || b < c); // true
        System.out.println(!(a < b));       // false

        calls = 0;
        boolean r1 = check(false) && check(true);
        System.out.println(r1 + " calls=" + calls); // false calls=1 (right side skipped)

        calls = 0;
        boolean r2 = check(false) & check(true);
        System.out.println(r2 + " calls=" + calls); // false calls=2 (both evaluated)

        calls = 0;
        boolean r3 = check(true) || check(false);
        System.out.println(r3 + " calls=" + calls); // true calls=1

        String s = null;
        System.out.println(s != null && s.length() > 0); // false, no NullPointerException
    }
}
```

### Assignment chaining and precedence
```java
public class AssignPrecedence {
    public static void main(String[] args) {
        int a, b, c;
        a = b = c = 10;                  // right to left
        System.out.println(a + " " + b + " " + c); // 10 10 10

        int x = 2, y = 3, z = 4;
        System.out.println(x + y * z);   // 14
        System.out.println((x + y) * z); // 20
        System.out.println(8 / 2 * 4);   // 16 (left to right, not "division first")
        System.out.println(1 + 2 << 1);  // 6  (+ binds tighter than <<)
    }
}
```

## Common Pitfalls / Tips
- **`=` vs `==`.** `if (x = 5)` doesn't compile in Java (an `int` isn't a `boolean`), but `if (flag = true)` with a `boolean` does compile and is always true.
- **Integer division truncates.** `7 / 2` is 3, and `-7 / 2` is -3 (toward zero). `%` keeps the sign of the left operand: `-7 % 2 == -1`. Use `Math.floorMod` when you need a non-negative remainder.
- **Don't mix `++` into complex expressions.** `i = i++;` leaves `i` unchanged. Keep increments on their own line unless prefix/postfix is clearly intended.
- **Compound assignment hides a cast.** `byte b = 10; b += 5;` and `b <<= 1;` compile, but `b = b + 5;` doesn't. The hidden cast can also silently overflow (`b += 200`).
- **`byte`/`short` are promoted to `int`** before any operator. Cast back explicitly, and remember the cast only keeps the low bits.
- **The shift distance wraps.** `x << 32 == x` for `int`, and `x << 64 == x` for `long`. Use `1L << n` when you need bit positions above 31.
- **`>>` vs `>>>`.** For negative numbers `>>` stays negative and `>>>` becomes a large positive number. On `byte`/`short`, `>>>` doesn't give an unsigned result unless you mask with `& 0xFF` / `& 0xFFFF` first.
- **`~x == -x - 1`**, so `~2` is -3, not some small positive number.
- **Prefer `&&`/`||` for conditions.** Use `&`/`|` on booleans only when you deliberately need both sides evaluated.
- **Use parentheses** instead of relying on the precedence table. `a & b == c` parses as `a & (b == c)`, which won't compile for `int`s.

## Review Questions
1. What is the difference between `=` and `==`? What type does `a == b` produce?
2. Given `int j = 9; int k = j++; int l = ++j;`, what are `j`, `k` and `l` at the end?
3. Compute `2 & 3`, `2 | 3`, `2 ^ 3` and `~2`. Explain why `~2` is negative.
4. Why does `byte b = 1; b = b << 1;` fail to compile, and what does `(byte)(b << 7)` print?
5. What do `1 << 31`, `1 << 32` and `1 << 33` evaluate to for an `int`, and why?
6. How do `>>` and `>>>` differ? What is `-128 >> 1` and what is `-128 >>> 1` for an `int`?
7. What is short-circuit evaluation? Give an example where it prevents an error.
8. Do `&` and `|` work on `boolean` operands in Java? How do they differ from `&&` and `||`?

<details>
<summary>Answers</summary>

1. `=` assigns the right-hand value to the left-hand variable. `==` compares two values and produces a `boolean`.
2. `k = j++` gives `k = 9` and makes `j = 10`. `l = ++j` makes `j = 11` and then `l = 11`. Final: `j = 11, k = 9, l = 11`.
3. `2 & 3 = 2` (`10 & 11 = 10`), `2 | 3 = 3`, `2 ^ 3 = 1`, `~2 = -3`. `~` flips all 32 bits, including the leading zeros, so the sign bit becomes 1. In two's complement, `~x = -x - 1`.
4. `b << 1` promotes `b` to `int`, so the result is an `int` and can't be assigned to a `byte` without a cast. `(byte)(b << 7)` computes the `int` 128 and keeps its low 8 bits `1000 0000`, which prints -128.
5. `1 << 31` is -2147483648 (`Integer.MIN_VALUE`, sign bit set). Java takes the `int` shift distance mod 32, so `1 << 32` is `1 << 0 = 1` and `1 << 33` is `1 << 1 = 2`.
6. `>>` fills the new high bits with copies of the sign bit (arithmetic shift); `>>>` always fills with 0 (logical shift). `-128 >> 1 = -64`; `-128 >>> 1 = 2147483584`.
7. With `&&`, if the left side is false the right side isn't evaluated; with `||`, if the left side is true the right side is skipped. Example: `s != null && s.length() > 0` never calls `length()` on null.
8. Yes. They give the same boolean result as `&&`/`||` but always evaluate both operands (no short-circuit), which matters when the right side has side effects.
</details>

## Further Study
- The ternary operator `cond ? a : b` (next video, with if/else)
- Two's complement and sign extension in detail
- Integer overflow, `Math.addExact`, and `Math.floorDiv`/`floorMod`
- Bit-manipulation tricks: check/set/clear/toggle the k-th bit, `n & (n - 1)`, counting set bits (`Integer.bitCount`)
- `Integer.toBinaryString`, `Integer.toUnsignedString`, `Byte.toUnsignedInt`
- The full Java operator precedence table (JLS §15)
- Evaluation order: Java evaluates operands left to right (JLS §15.7)

---

[← #05](05-type-conversion-promotion-casting.md) · [Index](../../ReadMe.md) · [#07 →](07-java-conditional-statements.md)
