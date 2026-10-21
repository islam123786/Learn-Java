# 06 · Java Operators
> Full notes: [06-java-operators.md](../06-java-operators.md) · [▶ Video](https://youtu.be/Tdq4fmDCJH8)

## Arithmetic operators
- `+`, `-`, `*`, `/`, `%`. Modulus (`%`) gives the **remainder**.
- With `a = 5, b = 10`: `a + b = 15`, `a - b = -5`, `a * b = 50`, `b / a = 2`, `b % a = 0`.

## Compound assignment
- `h = h + 2` can be written `h += 2`. The same goes for `-=`, `*=`, `/=`, `%=`.
- These aren't faster. They're shorter, and they include a hidden cast: `byte b = 5; b += 2;` compiles, but `b = b + 2;` doesn't.

## Increment / decrement, prefix vs postfix
- `i++` means `i = i + 1`, and `i--` means `i = i - 1`.
- On its own line, `++j` and `j++` do the same thing. The difference shows when the value is used:

| Code | Means | `j = 9` → result |
| :--- | :--- | :--- |
| `k = j++;` (postfix) | `k = j; j = j + 1;` | `k = 9`, `j = 10` |
| `k = ++j;` (prefix) | `j = j + 1; k = j;` | `k = 10`, `j = 10` |

## Relational operators
- `==`, `!=`, `<`, `>`, `<=`, `>=` always give back a **`boolean`**.
- `a == b` compares the two values. `a = b` copies `b` into `a`.

---

## Bitwise operators

| Operator | Name | Works on |
| :--- | :--- | :--- |
| `&` | AND | two operands |
| `\|` | OR | two operands |
| `^` | XOR | two operands |
| `~` | NOT | one operand (unary) |
| `<<` | Left shift | value, shift distance |
| `>>` | Right shift (keeps the sign) | value, shift distance |
| `>>>` | Unsigned right shift (fills with 0) | value, shift distance |

- Each one that takes two operands has a compound form: `&=`, `|=`, `^=`, `<<=`, `>>=`, `>>>=`.
- They work on the **bits** of integers and give back an **integer**, not a `boolean`.

### Truth table
| a | b | a & b | a \| b | a ^ b | ~a |
|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 0 | 1 |
| 0 | 1 | 0 | 1 | 1 | 1 |
| 1 | 0 | 0 | 1 | 1 | 0 |
| 1 | 1 | 1 | 1 | 0 | 0 |

- **AND:** 1 only if both bits are 1.
- **OR:** 1 if at least one bit is 1.
- **XOR:** 1 if the bits are **different**.
- **NOT:** flips every bit.

### Worked example: `a = 2`, `b = 3`
```
a      = 0000 0010   (2)
b      = 0000 0011   (3)
a & b  = 0000 0010   → 2
a | b  = 0000 0011   → 3
a ^ b  = 0000 0001   → 1
~a     = 1111 1101   → -3   (all 32 bits flip, so the sign bit becomes 1)
```
- **Rule:** `~x == -x - 1`

### Left shift `<<`
- Every bit moves left. The leftmost bit falls off, and a **0** comes in on the right.
- Each shift **doubles** the number, so `x << n` = `x × 2ⁿ` (until bits overflow).
```
8        = 0000 1000
8 << 1   = 0001 0000   → 16
8 << 2   = 0010 0000   → 32
10 << 1  = 1010 → 10100 → 20
```
- In the 8-bit picture, shifting 64 once more puts the 1 in the **sign bit**: `1000 0000` = **-128**. One more shift gives `0000 0000` = 0, and it stays 0.

### Shifts promote to `int`
- Shifts work **only on `int` and `long`**. A `byte` or `short` is promoted to `int` first, so you have to cast the result back:
  - `byte h = 1; h = h << 1;` → compile error: *"cannot convert from int to byte"*
  - `h = (byte) (h << 1);` → `2`
- **Walk-through with `byte b = 1`:**
  1. `b = (byte) (b << 7);` As an `int`, `1 << 7` = **+128** (only bit 7 is set). The cast keeps the low 8 bits, `1000 0000`, which as a `byte` is **-128**.
  2. `b << 1` again: -128 is promoted to `int` with **sign extension** (24 leading 1s, so the value stays -128). Shifting gives `0xFFFFFF00` = **-256**.
  3. `(byte)` keeps the low 8 bits, `0000 0000`, which is **0**.
  - The bits weren't lost during the shift. They were cut off when the result was stored back into a `byte`. `int i = b << 1;` shows -256.

### Shifting an `int`: the sign bit and the mod-32 rule
- `1 << 30` = 1073741824 (bit 30 set, still positive).
- `1 << 31` sets the sign bit: **-2147483648** = `Integer.MIN_VALUE`.
- Java uses only the low 5 bits of the shift distance, i.e. **`n % 32`**:
  - `1 << 32` = `1 << 0` = **1**
  - `1 << 33` = `1 << 1` = **2**
- For `long`, the distance is taken **mod 64**. The video says 32, which is wrong for `long`.

### Right shift `>>` (keeps the sign)
- Every bit moves right. The rightmost bit falls off, and the new leftmost bit **copies the old sign bit**, so negative numbers stay negative.
- Each shift **halves** the number (rounding down), so `x >> n` = `x ÷ 2ⁿ`.
```
8 >> 1    → 4   → 2   → 1
-128 >> 1 → -64 → -32 → … → -1
```
- Because bits fall off on the right, `>>` on a `byte` (cast back) gives the result you'd expect.
- The mod-32 rule applies here too.

### Unsigned right shift `>>>` (fills with 0)
- Always shifts in a **0**, whatever the sign. The video calls `>>` "smart" and `>>>` "dumb".
- On an `int`, a negative number becomes a large positive one: `-128 >>> 1` → `0xFFFFFF80 >>> 1` = `0x7FFFFFC0` = **2147483584**.
- Use it for values where negatives make no sense, such as pixel data or hash codes treated as unsigned.
- **Byte trap:** the video shows `byte -128 >>> 1` as +64. Because of promotion, Java actually shifts the 32-bit `int`, and `(byte)` keeps `0xC0`, which is **-64**. `>>>` only behaves as unsigned at full `int`/`long` width.

### Bitwise summary
| Expression | Result |
| :--- | :--- |
| `2 & 3` | `2` |
| `2 \| 3` | `3` |
| `2 ^ 3` | `1` |
| `~2` | `-3` |
| `1 << 1` | `2` |
| `1 << 30` | `1073741824` |
| `1 << 31` | `-2147483648` |
| `1 << 32` | `1` |
| `1 << 33` | `2` |
| `-128 >> 1` | `-64` |
| `-128 >>> 1` (int) | `2147483584` |

---

## Logical operators `&&` `||` `!`
- They work on **boolean expressions**, not on bits: `boolean d = a < b && a < c;`
- They follow the same truth table as `&`/`|`, using true/false instead of 1/0.

## Short-circuit evaluation
- `A && B`: if A is false, B is **never evaluated**.
- `A || B`: if A is true, B is skipped.
- Use it for guards like null checks: `s != null && s.length() > 0`.
- `&` and `|` on booleans give the same result, but **both sides are always evaluated**. This matters when the right side has a side effect like `i++`, and it's a classic interview question.

## Assignment and chaining
- `a = b = c = 10;` evaluates **right to left**. `b` and `c` must already be declared: `int a, b, c;`

## Ternary operator (preview)
- `cond ? x : y` is the only operator with three operands. It's covered in the next video.

## Operator precedence
Highest first:

postfix `x++` → unary `++x ! ~` and casts → `* / %` → `+ -` → `<< >> >>>` → `< > <= >=` → `== !=` → `&` → `^` → `|` → `&&` → `||` → `?:` → `= += …`

- Operators at the same level go **left to right**, e.g. `8 / 2 * 4 == 16`. Assignment and the ternary go **right to left**.
- In practice, use **parentheses** instead of memorizing the table.

**Remember:** bitwise operators work on bits and give back integers. `<<` doubles and `>>` halves. Shifts promote to `int` and use the distance mod 32. `&&`/`||` short-circuit, but `&`/`|` don't.
