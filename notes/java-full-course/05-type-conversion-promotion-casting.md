# Java Type Conversions & Type Promotions Explained | Casting in Java | Java Full Course #5
> Source: https://youtu.be/QpWha2cLS1c · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 36:43. The video is in Hindi/Hinglish and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within a minute or two.

## TL;DR
- **Implicit (widening) conversion** happens automatically when the destination type is *wider* than the source: `byte → short → int → long → float → double`, and `char → int`. No data is lost in the range sense.
- **Explicit (narrowing) conversion** needs a **cast**, `(type) value`, because data can be lost. `(byte) 300` keeps only the low 8 bits, which gives **44**.
- **Floating → integer** casts drop the fractional part (the video calls this "truncating conversion"): `(int) 15.678f` is `15`.
- **`boolean` can't be converted** to or from any other type, not even with a cast.
- **Type promotion in expressions:** `byte`, `short` and `char` operands are promoted to `int`. If any operand is `long` the result is `long`, if any is `float` it's `float`, and if any is `double` it's `double`. That's why `byte b = 50; b = b * 2;` doesn't compile and you need `b = (byte) (b * 2);`.

## Key Concepts

**Type conversion.** Changing a value from one primitive type to another. It's implicit (done by the compiler) or explicit (requested by you with a cast).

**Widening conversion (implicit).** The rule is that the destination type must be wider than the source. Every `byte` value (−128 to 127) fits in an `int` (32 bits), so `int i = b;` just works. *Why it matters:* you can pass smaller types wherever larger ones are expected.

**Narrowing conversion (explicit).** Going to a smaller type, e.g. `int → byte`, `long → int` or `double → float`. The compiler refuses without a cast because the value might not fit. With the cast, the extra high-order bits are **discarded**. *Why it matters:* the result can be surprising (300 → 44).

**Cast operator.** `(targetType) expression`. You're telling the compiler that you know data may be lost and you want the conversion anyway.

**Truncating conversion.** The video's name for floating-point → integer narrowing: the fractional part is dropped (rounding toward zero). This happens even between `float` and `int`, which are both 32 bits, because an `int` can't hold fractions.

**Automatic type promotion.** In arithmetic, Java first promotes the operands to a common type of at least `int`, so intermediate results like `50 * 40 = 2000` don't overflow a `byte`. *Why it matters:* it explains "cannot convert from int to byte" errors on lines where you never wrote `int`.

## Detailed Notes

### Intro [00:00]
- The instructor asks viewers to like, comment, and share their progress on LinkedIn/Twitter. Sharing gives you a learning record and builds a community around it.

### Recap of primitive types [03:18]
- Integers (`byte`, `short`, `int`, `long`), real numbers (`float`, `double`), `char` and `boolean`. The question for this video is how to convert between them, e.g. `byte → short` or `short → long`.

### Two kinds of conversion [04:24]
- **Implicit**: Java does it automatically.
- **Explicit**: you have to tell Java to do it.
- Example: `byte b = 24; int i; i = b;` compiles, and `System.out.println(i)` prints `24`.

### Rule for implicit conversion: the destination must be wider [05:30]
- A `byte` is 8 bits (−128 to 127) and an `int` is 32 bits with a much larger range. Anything a `byte` can hold, an `int` can hold.
- This is called **widening conversion**. The same applies to `short → int`, `int → long`, and so on.

### Explicit / narrowing conversion [09:11]
- `int i = 300; byte b; b = i;` gives the compile error *"Type mismatch: cannot convert from int to byte"*. 32 bits might not fit in 8, and Java won't silently **truncate** data.
- The fix is a cast: `b = (byte) i;`
- **What gets printed?** 300 in binary is `1 0010 1100` (9 significant bits inside a 32-bit `int`). A `byte` keeps only the **lowest 8 bits**, `0010 1100` = 32 + 8 + 4 = **44**.
- **Shortcut** [12:51]: the byte range has 2⁸ = 256 values, and 300 % 256 = 44. (`%` is the modulo/remainder operator.)
- Other narrowing examples: `long → int`, `double → float`.

### `char → int` is widening too [14:19]
- `char c = 'a';` is stored as its Unicode value, **97**. The `char` type tells Java to display it as a character.
- `char` is 16 bits and `int` is 32 bits, so `int i = c;` is implicit and prints `97`.

### Truncating conversion: floating point → integer [15:47]
- `float f = 16.25f; int i = f;` doesn't compile. The two types have the same width (32 bits), but an `int` can't hold the decimal part.
- With a cast, `i = (int) f;` stores `16`. The fraction is dropped.

### Code demo: implicit conversions [17:15]
- `byte b = 24; int i = b;` prints `24`.
- `char c = 'a'; int i = c;` prints `97`.
- Earlier blocks get commented out because the same variable name can't be declared twice in one scope.

### Code demo: explicit conversions [19:28]
- `int i = 24; byte b = i;` gives *"Type mismatch: cannot convert from int to byte"*. `byte b = (byte) i;` prints `24`, since it fits.
- Change it to `int i = 300;` and it prints `44`. Printing `300 % 256` also shows `44`.

### Code demo: truncation [21:40]
- `float f = 15.678f; int i = (int) f;` prints `15`. Without the cast the error is *"cannot convert from float to int"*.

### `boolean` can't be converted [22:46]
- `boolean bool = false; int i = bool;` is a type mismatch, and `int i = (int) bool;` gives *"Cannot cast from boolean to int"*. `false` isn't a number in Java, so there's no conversion at all.

### A surprising error: `b = b * 2` [24:14]
- `byte b = 50; b = b * 2;` gives *"cannot convert from int to byte"*, even though no `int` appears anywhere. The cause is **type promotion**.

### Automatic type promotion [24:58]
- Example: `byte a = 40, b = 50, c = 100; int i = a * b / c;`
- `a * b` = 2000, which is already outside the byte range. So Java **promotes the bytes to `int` before computing**: `2000 / 100 = 200` is an `int`, and it fits in `i`.
- The same rule explains `b * 2`. `b` is promoted to `int`, so the product `100` is an `int`, and assigning an `int` to a `byte` is narrowing. It needs a cast.

### Promotion rules [29:22]
1. `byte`, `short` and `char` are promoted to **`int`** in any expression.
2. If one operand is **`long`**, the whole expression becomes `long`.
3. If one operand is **`float`**, the whole expression becomes `float`.
4. If one operand is **`double`**, the whole expression becomes `double`.
- In short, the expression takes the type of its "largest" operand, and never anything smaller than `int`.

### Fixing `b * 2` [31:13]
- `b = (byte) b * 2;` *still* fails. The cast applies only to `b`, which is then promoted to `int` again for the multiplication.
- Wrap the whole expression in parentheses: `b = (byte) (b * 2);`. That prints `100`.

### Mixed-type expression demo [32:40]
- Variables of every type: `byte`, `char`, `short`, `int`, `float`, `double`.
- `f * b` is a `float`. `i / c` is an `int` (the `char` is promoted). `d * s` is a `double`.
- `float + int` gives a `float`, and `float − double` gives a `double`, so the final `result` is a `double`. (The instructor notes his code accidentally used `+` instead of `−` for the last operation.)

## Code Examples

### Widening (implicit)
```java
public class WideningDemo {
    public static void main(String[] args) {
        byte b = 24;
        int i = b;                 // byte -> int, automatic
        System.out.println(i);

        char c = 'a';
        int code = c;              // char -> int, automatic
        System.out.println(code);

        long l = i;                // int -> long
        double d = l;              // long -> double
        System.out.println(l + " " + d);
    }
}
// Output:
// 24
// 97
// 24 24.0
```

### Narrowing (explicit cast) and the 300 → 44 example
```java
public class NarrowingDemo {
    public static void main(String[] args) {
        int small = 24;
        byte b1 = (byte) small;    // fits: unchanged
        System.out.println(b1);

        int i = 300;
        byte b2 = (byte) i;        // keeps only the low 8 bits
        System.out.println(b2);
        System.out.println(Integer.toBinaryString(300));   // 100101100
        System.out.println(300 % 256);                     // same answer for this case

        System.out.println((byte) 200);   // low 8 bits 11001000 -> MSB 1 -> negative
        System.out.println(200 % 256);    // modulo shortcut does NOT give the signed value

        long big = 3_000_000_000L;
        System.out.println((int) big);    // wraps around

        double precise = 3.141592653589793;
        float approx = (float) precise;   // double -> float loses precision
        System.out.println(approx);
    }
}
// Output:
// 24
// 44
// 100101100
// 44
// -56
// 200
// -1294967296
// 3.1415927
```

### Truncating conversion (floating → integer)
```java
public class TruncationDemo {
    public static void main(String[] args) {
        float f = 15.678f;
        int i = (int) f;           // fraction dropped, not rounded
        System.out.println(i);

        System.out.println((int) 16.25f);
        System.out.println((int) -15.678);      // toward zero, not down
        System.out.println(Math.round(15.678)); // use Math.round if you want rounding
        System.out.println((int) 1e20);         // too big: clamps to Integer.MAX_VALUE
    }
}
// Output:
// 15
// 16
// -15
// 16
// 2147483647
```

### Type promotion in expressions
```java
public class PromotionDemo {
    public static void main(String[] args) {
        byte a = 40, b = 50, c = 100;
        int r = a * b / c;          // a*b = 2000 computed as int, no overflow
        System.out.println(r);

        byte x = 50;
        x = (byte) (x * 2);         // parentheses: cast the whole expression
        System.out.println(x);

        byte y = 50;
        y *= 2;                     // compound assignment casts implicitly
        System.out.println(y);

        // Mixed expression: result type is the "largest" operand type
        byte bt = 42;
        char ch = 'a';
        short sh = 1024;
        int in = 50000;
        float fl = 5.67f;
        double db = .1234;          // same as 0.1234
        System.out.println("f*b = " + (fl * bt));   // float
        System.out.println("i/c = " + (in / ch));   // int (char promoted)
        System.out.println("d*s = " + (db * sh));   // double
        double result = (fl * bt) + (in / ch) - (db * sh); // float + int -> float; float - double -> double
        System.out.println("result = " + result);

        System.out.println(5 / 2);    // int / int -> int
        System.out.println(5 / 2.0);  // int / double -> double
    }
}
// Output:
// 20
// 100
// 100
// f*b = 238.14
// i/c = 515
// d*s = 126.3616
// result = 626.7784146484375
// 2
// 2.5
```

### What doesn't compile
```java
// does not compile
public class ConversionErrors {
    public static void main(String[] args) {
        int i = 300;
        byte b = i;                // error: possible lossy conversion from int to byte
        float f = 16.25f;
        int n = f;                 // error: possible lossy conversion from float to int
        boolean flag = false;
        int k = (int) flag;        // error: incompatible types: boolean cannot be converted to int
        byte x = 50;
        x = x * 2;                 // error: x * 2 is an int
        x = (byte) x * 2;          // still an error: cast binds tighter than *
    }
}
```

## Common Pitfalls / Tips
- **The modulo shortcut only works for small positive results.** `(byte) 300 = 44` matches `300 % 256`, but `(byte) 200` is **−56**, not 200. The kept 8 bits are read as two's complement, and `(byte) -300` is −44. Think in terms of "keep the low 8 bits, then read them as a signed byte".
- **Casts bind tighter than arithmetic.** `(byte) b * 2` means `((byte) b) * 2`, which is an `int`. Write `(byte) (b * 2)`.
- **Compound assignments cast for you.** `b *= 2`, `b += 1` and `b++` compile on a `byte` because they include an implicit cast. They can still overflow silently.
- **Constants get a pass.** `byte b = 10;` compiles even though `10` is an `int` literal, because the compiler knows the constant fits. `byte b = i;` (a variable) doesn't. The same goes for `final int K = 10; byte b = K;`.
- **Floating → integer rounds toward zero.** `(int) -15.678` is −15. NaN becomes 0, and huge values clamp to `MIN_VALUE`/`MAX_VALUE`. Use `Math.round`, `Math.floor` or `Math.ceil` when you mean rounding.
- **"Widening" can still lose precision.** `int → float` and `long → float/double` are implicit but can lose low-order digits: `(float) 123_456_789` is `1.23456792E8`. The range fits, the precision doesn't.
- **`char` ↔ `byte`/`short` always need casts**, in both directions. `char` is unsigned 16-bit, so neither side's range contains the other.
- **Integer division:** `5 / 2` is `2`. Make one operand floating (`5 / 2.0`, or `(double) a / b`) *before* dividing.
- **Terminology:** the JLS calls these *widening primitive conversion* and *narrowing primitive conversion*. "Truncating conversion" is the video's label for the floating → integral case of narrowing.

## Review Questions
1. What's the difference between implicit and explicit conversion? Give one example of each.
2. State the rule that decides whether a primitive conversion can be implicit.
3. Why does `(byte) 300` give 44? Show the bit-level reasoning.
4. What does `(int) 15.678f` evaluate to, and why does `int i = 15.678f;` fail even though `float` and `int` are both 32 bits?
5. Can you cast a `boolean` to an `int`? Why or why not?
6. Why does `byte b = 50; b = b * 2;` fail to compile? Give two ways to fix it.
7. List the four type-promotion rules for expressions.
8. In `(f * b) + (i / c) - (d * s)` with `f` float, `b` byte, `i` int, `c` char, `d` double and `s` short, what is the type of each part and of the result?

<details>
<summary>Answers</summary>

1. Implicit conversion is done automatically by the compiler, e.g. `int i = someByte;`. Explicit conversion needs a cast, e.g. `byte b = (byte) someInt;`.
2. The destination type must be wider than the source (`byte → short → int → long → float → double`, and `char → int`). Otherwise you need a cast.
3. 300 = `1 0010 1100`₂. A `byte` keeps only the lowest 8 bits, `0010 1100` = 32 + 8 + 4 = 44. The MSB is 0, so it's positive. (Equivalently, 300 mod 256 = 44.)
4. `15`, because the fractional part is dropped. The assignment fails because an `int` can't represent fractions, so the conversion is narrowing even though the widths are equal.
5. No. In Java `boolean` isn't a numeric type, and no conversion exists in either direction, implicit or with a cast.
6. `b * 2` is promoted to `int`, and assigning an `int` to a `byte` is narrowing. Fixes: `b = (byte) (b * 2);` or `b *= 2;`.
7. (1) `byte`, `short` and `char` are promoted to `int`. (2) If any operand is `long`, the result is `long`. (3) If any operand is `float`, the result is `float`. (4) If any operand is `double`, the result is `double`.
8. `f * b` is a `float`, `i / c` is an `int`, and `d * s` is a `double`. `float + int` gives `float`, and `float − double` gives `double`, so the result is a `double`.
</details>

## Further Study
- JLS chapter 5 (Conversions and Contexts): assignment, method invocation, casting and numeric promotion contexts
- Operators and precedence (next topics), including where casts sit in the precedence table
- Wrapper classes, autoboxing/unboxing, and `Integer.parseInt` / `String.valueOf` for String ↔ number
- Overflow-safe arithmetic: `Math.addExact`, `Math.toIntExact`
- `Math.round` / `floor` / `ceil` and `BigDecimal.setScale` for controlled rounding
- Reference type casting (upcasting/downcasting) when OOP begins

---

[← #04](04-negative-floating-point-storage.md) · [Index](../../ReadMe.md) · [#06 →](06-java-operators.md)
