# 05 · Type Conversion, Promotion & Casting
> Full notes: [05-type-conversion-promotion-casting.md](../05-type-conversion-promotion-casting.md) · [▶ Video](https://youtu.be/QpWha2cLS1c)

### Recap of primitive types
- Integers (`byte`, `short`, `int`, `long`), real numbers (`float`, `double`), `char` and `boolean`. The question is how to convert between them.

### Two kinds of conversion
- **Implicit:** Java does it automatically, e.g. `byte b = 24; int i = b;` prints `24`.
- **Explicit:** you have to tell Java with a cast.

### Implicit (widening) conversion
- Allowed when the destination type is **wider**: `byte → short → int → long`.
- Anything a `byte` (8 bits) can hold also fits in an `int` (32 bits).

### Explicit (narrowing) conversion
- `int i = 300; byte b = i;` gives a compile error: *"cannot convert from int to byte"*. Java won't silently cut off data.
- The fix is a cast: `b = (byte) i;`
- The result is **44**, because a `byte` keeps only the **lowest 8 bits**: 300 = `1 0010 1100` → `0010 1100` = 44.
- **Shortcut:** 300 % 256 = 44.
- Other narrowing examples: `long → int`, `double → float`.

### `char → int` is widening too
- `char c = 'a'; int i = c;` prints **97**, the Unicode value. `char` is 16 bits and `int` is 32 bits.

### Floating point → integer (truncation)
- `float f = 16.25f; int i = f;` doesn't compile, because an `int` can't hold the decimal part.
- `int i = (int) f;` gives `16`. The fraction is dropped, not rounded.

### Code demos
| Code | Output |
| :--- | :--- |
| `byte b = 24; int i = b;` | `24` |
| `char c = 'a'; int i = c;` | `97` |
| `int i = 24; byte b = (byte) i;` | `24` (it fits) |
| `int i = 300; byte b = (byte) i;` | `44` |
| `float f = 15.678f; int i = (int) f;` | `15` |

### `boolean` can't be converted
- `int i = (int) bool;` gives *"Cannot cast from boolean to int"*. `true`/`false` aren't numbers in Java.

### A surprising error: `b = b * 2`
- `byte b = 50; b = b * 2;` gives *"cannot convert from int to byte"*, even though there's no `int` in the code. The cause is **type promotion**.

### Automatic type promotion
- `byte a = 40, b = 50, c = 100; int i = a * b / c;` works. `a * b` = 2000 doesn't fit in a byte, so Java **promotes the bytes to `int` first**. The result is 200.
- That's also why `b * 2` is an `int` and needs a cast to go back into a `byte`.

### Promotion rules
1. `byte`, `short` and `char` → promoted to **`int`**.
2. Any operand is `long` → the whole expression is **`long`**.
3. Any operand is `float` → **`float`**.
4. Any operand is `double` → **`double`**.

In short, an expression takes the type of its largest operand, and is never smaller than `int`.

### Fixing `b * 2`
- `b = (byte) b * 2;` still fails, because the cast applies only to `b`, which is promoted again.
- `b = (byte) (b * 2);` works and prints `100`.

### Mixed-type expression
- `f * b` → `float`. `i / c` → `int`. `d * s` → `double`.
- `float + int` → `float`. `float − double` → `double`. So the final result is a **`double`**.

**Remember:** widening is automatic and narrowing needs a cast. Arithmetic promotes everything to at least `int`, so cast the whole expression: `(byte) (b * 2)`.
