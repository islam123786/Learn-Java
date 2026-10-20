# How Java Stores Negative & Floating Point Numbers | Java Full Course #4
> Source: https://youtu.be/iV8hy9DdKFM · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 57:54. The video is in Hindi/Hinglish and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `println` rounds floats for display. `System.out.printf("%.20f", 0.7f)` shows the truth: `0.69999998807907...`. A `float` (or `double`) often **can't store a decimal value exactly**.
- **Negative integers use two's complement.** Write the positive value in binary, invert every bit (one's complement), then add 1. The **MSB (leftmost bit) is the sign**: 0 means non-negative, 1 means negative. Example: −42 as a `byte` is `11010110`.
- Two's complement gives a **single zero**. Applying it to "−0" overflows back to `00000000`, which is why it's preferred over one's complement.
- **`float` follows IEEE 754 single precision:** 1 sign bit, 8 exponent bits (stored with a **bias of 127**), and 23 mantissa bits. The value is (−1)^sign × 1.mantissa × 2^(exponent − 127). **`double`** uses 1 + 11 + 52 bits with a bias of 1023.
- 8.125 = `1000.001`₂ is stored exactly. 0.7 = `0.1011001100110…`₂ repeats forever, so it gets cut to 23 bits and you read back an approximation. For exact decimals use **`BigDecimal`**.

## Key Concepts

**MSB / LSB.** The Most Significant Bit is the leftmost bit, which has the largest place value. The Least Significant Bit is the rightmost. Changing the MSB changes the number the most, just as changing the thousands digit of 2321 matters more than changing the ones digit. *Why it matters:* in signed integers the MSB acts as the sign bit.

**One's complement.** Flip every bit (0→1, 1→0). This was an early way to represent negatives. *Why it matters:* it's the first step of two's complement, but on its own it has two zeros (+0 and −0).

**Two's complement.** One's complement plus 1. Java uses it for `byte`, `short`, `int` and `long`. *Why it matters:* there's a single zero, the range is asymmetric (−128 to 127), and ordinary binary addition works for signed numbers.

**Decimal fraction → binary.** Repeatedly multiply the fractional part by 2. The integer part of each result (0 or 1) is the next bit, read **top to bottom**. Stop when the fraction reaches 0, or notice that the bits repeat. *Why it matters:* it shows why 0.125 is exact and 0.7 isn't.

**Normalized form.** Write the binary number as `1.xxxx × 2^e` by moving the binary point. Only `xxxx` (the **mantissa**, or fraction) and `e` (the **exponent**) need to be stored. The leading `1.` is implicit.

**Bias.** A constant added to the exponent so it can be stored as an unsigned field: 2^(k−1) − 1 for a k-bit exponent. That's 127 for `float` and 1023 for `double`. *Why it matters:* negative exponents (like 0.7's 2⁻¹) can be stored without a sign bit of their own.

**IEEE 754.** The standard for floating-point formats that Java's `float` and `double` follow.

**`BigDecimal`.** A class (non-primitive type) that represents decimal numbers exactly. It's covered later in the series.

## Detailed Notes

### Java is hiding something [01:04]
- `float f = 8.125f;` prints `8.125`, and `float f = 0.7f;` [01:55] prints `0.7`. Everything looks fine.
- Switch to `System.out.printf("%.20f", f)` [02:56], which means "show 20 decimal places". `8.125f` shows `8.12500000000000000000`, but `0.7f` shows `0.6999999880…` [04:30].
- `println` was **rounding for display**. The value in memory is very close to 0.7, but not exactly 0.7. The rest of the video explains why.

### Positive integers recap [05:53]
- `byte b = 42;` uses 8 bits with place values 128, 64, 32, 16, 8, 4, 2, 1. 42 = 32 + 8 + 2, so it's `00101010`.
- **Division method** [07:25]: divide by 2 repeatedly, write down each remainder, and read the remainders **bottom-up**. 42 → 21 r0 → 10 r1 → 5 r0 → 2 r1 → 1 r0 → 0 r1 gives `101010`. Pad with leading zeros to get 8 bits.
- Storing means converting to binary and keeping the bits in the memory cell called `b`. Printing means converting the bits back to decimal.

### Negative integers: two's complement [10:06]
For `byte b = -42;`:
1. Ignore the sign and write +42 in binary: `00101010`.
2. **One's complement** [10:41], flip every bit: `11010101`.
3. **Two's complement** [11:19], add 1. Binary addition works like decimal with carries: 1 + 1 = 0 carry 1. The result is `11010110`.
4. Store `11010110` in `b`.

### How the computer knows it's negative [14:18]
- Look at the **MSB** [16:06]. If it's 1 the number is negative, and if it's 0 the number is positive.
- "But couldn't a positive number have MSB 1?" [16:41] Not within the type's range. The largest `byte` is 127 = `01111111`. Setting the MSB would mean 128 or more, which is out of range. So every valid positive has MSB 0. Inverting a positive number's bits always sets the MSB to 1, and adding 1 keeps it there, except in the zero case described below.

### Reading a negative number back [20:25]
- `println(b)` reads `11010110`. The MSB is 1, so the number is negative.
- Apply two's complement again: invert to get `00101001`, add 1 to get `00101010` = 42. Attach the minus sign: **−42**.

### Why two's complement and not one's? Zero. [21:53]
- `byte b = 0` is `00000000`.
- Suppose someone writes `-0` [22:44]. One's complement would give `11111111`, a "negative zero", which is a second representation of the same number.
- Two's complement adds 1 to `11111111`. The carry ripples through all 8 bits and produces a 9th bit: `1 00000000`. A `byte` only keeps the low 8 bits, so the extra bit is discarded and the result is `00000000` again.
- So zero has exactly one representation, and the MSB is 0 for it as expected. That's the special case mentioned earlier.

### Floating point: the 32-bit layout [26:57]
```
| S |  Exponent (8)  |            Mantissa (23)              |
 31   30         23   22                                    0
```
- 1 bit **sign** [28:39] (0 = positive, 1 = negative), 8 bits **exponent**, 23 bits **mantissa**.
- The example uses `float` because 32 bits are easier to write out than 64, but `double` works the same way.

### Storing 8.125f step by step
1. **Convert to binary.** The integer part 8 = `1000`. The fraction .125 uses **multiply by 2** [30:28]: 0.125×2 = **0**.25, 0.25×2 = **0**.5, 0.5×2 = **1**.0, then stop because the fraction is 0. Reading top-down gives `.001`. So 8.125 = `1000.001`₂.
2. **Normalize** [32:45] to `1.x × 2^e`: move the binary point 3 places left to get `1.000001 × 2³`. It's 2³ and not 10³ because this is base 2.
3. **Add the bias** [34:04]: exponent 3 + 127 = 130 = `10000010`.
4. **Place the values in memory** [35:27]:
   - sign = `0`
   - exponent = `10000010`
   - mantissa = `000001` followed by 17 zeros. The leading `1.` isn't stored because it's always there.
   - Full bit pattern: `0 10000010 00000100000000000000000` (hex `0x41020000`).

### Reading 8.125 back [37:59]
- Formula: **value = (−1)^S × (1 + mantissa) × 2^(E − bias)**
  - (−1)^0 = 1 for a positive number and (−1)^1 = −1 for a negative one, so the first factor applies the sign.
  - `1 +` puts back the implicit leading 1.
  - `E − bias` undoes the bias: 130 − 127 = 3.
- Fractional place values are 2⁻¹, 2⁻², 2⁻³, … The mantissa `000001` has a single 1 at 2⁻⁶ = 1/64 = 0.015625.
- So 1 × (1 + 0.015625) × 2³ = 1.015625 × 8 = **8.125** [42:19]. It's exact.

### Storing 0.7f: the problem case [43:52]
1. **Binary of .7** with the multiply-by-2 method: 0.7→**1**.4, 0.4→**0**.8, 0.8→**1**.6, 0.6→**1**.2, 0.2→**0**.4, 0.4→**0**.8, 0.8→**1**.6, … The fractions 0.4 → 0.8 → 0.6 → 0.2 start **repeating** [44:42]. So 0.7 = `0.1 0110 0110 0110…`₂ and it never ends.
2. **Normalize** [46:07]: `1.0110 0110 0110… × 2⁻¹`.
3. **Bias:** −1 + 127 = 126 = `01111110`.
4. **Store:** sign `0`, exponent `01111110`, and only 23 mantissa bits fit: `01100110011001100110011`. Everything after that is lost [48:12].
- Reading it back gives (1 + Σ 2⁻ᵏ over the 1-bits) × 2⁻¹, which is very close to 0.7 but not 0.7. That's the `0.69999998…` printed at the start.

### Where the bias comes from [51:51]
- **IEEE 754** [52:22] stores the exponent as an unsigned number. Adding a bias shifts negative exponents (like −1 for 0.7) into positive territory, which simplifies hardware comparisons and arithmetic.
- An 8-bit exponent field holds 0 to 255. The bias sits near the middle: **2^(8−1) − 1 = 127**.
- Why 127 and not 128? It comes down to hardware and microarchitecture details that the video skips. See the Further Study note on special exponents.

### `double` [55:21]
- 64 bits: 1 sign, **11 exponent**, **52 mantissa**. Bias = 2^(11−1) − 1 = **1023** [56:35]. The procedure is otherwise identical.
- More mantissa bits make the approximation much closer, but 0.7 still isn't exact in a `double`. Neither `float` nor `double` can store every decimal exactly.

### The fix: `BigDecimal` [57:24]
- Java provides **`BigDecimal`** for exact decimal values. Put in 0.7 and you get exactly 0.7 back. It's covered when the series moves to non-primitive types.

## Code Examples

### Seeing what `println` hides
```java
public class FloatPrecisionDemo {
    public static void main(String[] args) {
        float a = 8.125f;
        float b = 0.7f;
        System.out.println(a);                 // 8.125
        System.out.println(b);                 // 0.7 (rounded for display)
        System.out.printf("%.20f%n", a);       // 8.12500000000000000000
        System.out.printf("%.20f%n", b);       // 0.69999998807907100000
        // The exact value stored in memory:
        System.out.println(new java.math.BigDecimal(b)); // 0.699999988079071044921875
        System.out.println(0.1 + 0.2);         // 0.30000000000000004 (double has the same issue)
    }
}
// Output:
// 8.125
// 0.7
// 8.12500000000000000000
// 0.69999998807907100000
// 0.699999988079071044921875
// 0.30000000000000004
```

### Two's complement by hand vs what Java stores
```java
public class TwosComplementDemo {
    static String bits8(int x) {
        // keep only the low 8 bits and left-pad to 8 characters
        return String.format("%8s", Integer.toBinaryString(x & 0xFF)).replace(' ', '0');
    }

    public static void main(String[] args) {
        int positive = 42;
        int onesComplement = ~positive;           // flip every bit
        int twosComplement = onesComplement + 1;  // add 1
        System.out.println("+42      = " + bits8(positive));
        System.out.println("1's comp = " + bits8(onesComplement));
        System.out.println("2's comp = " + bits8(twosComplement));

        byte b = -42;
        System.out.println("byte -42 = " + bits8(b) + "  (same bits)");
        System.out.println("MSB      = " + ((b >> 7) & 1) + "  (1 => negative)");

        // Reading it back: invert + 1 gives the magnitude
        int magnitude = (~b + 1) & 0xFF;
        System.out.println("decoded  = -" + magnitude);

        // Negative zero collapses to zero: ~0 + 1 overflows the 8 bits
        System.out.println("-0       = " + bits8(~0 + 1));
        System.out.println(-0 == 0);
    }
}
// Output:
// +42      = 00101010
// 1's comp = 11010101
// 2's comp = 11010110
// byte -42 = 11010110  (same bits)
// MSB      = 1  (1 => negative)
// decoded  = -42
// -0       = 00000000
// true
```

### Taking a float apart: sign, exponent, mantissa
```java
public class FloatBitsDemo {
    static void dissect(float f) {
        int bits = Float.floatToIntBits(f);
        int sign = (bits >>> 31) & 1;
        int exponent = (bits >>> 23) & 0xFF;       // 8 bits, biased
        int mantissa = bits & 0x7FFFFF;            // 23 bits, implicit leading 1
        String m = String.format("%23s", Integer.toBinaryString(mantissa)).replace(' ', '0');
        String e = String.format("%8s", Integer.toBinaryString(exponent)).replace(' ', '0');
        // value = (-1)^sign * (1 + mantissa/2^23) * 2^(exponent - 127)
        double rebuilt = Math.pow(-1, sign) * (1 + mantissa / Math.pow(2, 23)) * Math.pow(2, exponent - 127);
        System.out.println(f + " -> " + sign + " " + e + " " + m
                + " | exp-127 = " + (exponent - 127) + " | rebuilt = " + rebuilt);
    }

    public static void main(String[] args) {
        dissect(8.125f);
        dissect(0.7f);
        dissect(-8.125f);
        System.out.println(Integer.toHexString(Float.floatToIntBits(8.125f)));
    }
}
// Output:
// 8.125 -> 0 10000010 00000100000000000000000 | exp-127 = 3 | rebuilt = 8.125
// 0.7 -> 0 01111110 01100110011001100110011 | exp-127 = -1 | rebuilt = 0.699999988079071
// -8.125 -> 1 10000010 00000100000000000000000 | exp-127 = 3 | rebuilt = -8.125
// 41020000
```

### Exact decimals with `BigDecimal`
```java
import java.math.BigDecimal;

public class BigDecimalPreview {
    public static void main(String[] args) {
        BigDecimal exact = new BigDecimal("0.7");        // build from a String!
        BigDecimal fromDouble = new BigDecimal(0.7);     // inherits the double's error
        System.out.println(exact);
        System.out.println(fromDouble);
        System.out.println(new BigDecimal("0.1").add(new BigDecimal("0.2"))); // 0.3
    }
}
// Output:
// 0.7
// 0.6999999999999999555910790149937383830547332763671875
// 0.3
```

## Common Pitfalls / Tips
- **Never compare floating-point values with `==` after arithmetic.** `0.1 + 0.2 == 0.3` is `false`. Compare with a tolerance (`Math.abs(a - b) < 1e-9`) or use `BigDecimal`.
- **Build `BigDecimal` from a `String`**, as in `new BigDecimal("0.7")` or `BigDecimal.valueOf(0.7)`. `new BigDecimal(0.7)` captures the double's binary approximation.
- **Why two's complement is really used.** Removing −0 is one benefit. The bigger reason is that **the same adder circuit works for signed and unsigned numbers** (5 + (−3) is plain binary addition with the overflow bit dropped), and the range gains one extra negative value (−128).
- **IEEE 754 rounds, it doesn't just truncate.** The 23 kept bits are rounded to the nearest representable value. For 0.7 the next bit happens to be 0, so rounding and truncation agree. For other numbers the stored value can be slightly *above* the decimal.
- **Special exponent values.** All-zeros (0) and all-ones (255) are reserved, for zero/subnormals and for Infinity/NaN. So normal `float` exponents run from −126 to +127. This is part of why the bias is 127.
- **Floating point *does* have −0.0.** Integers have a single zero, but IEEE floats have a sign bit, so `-0.0` exists: `0.0 == -0.0` is `true`, but `1/-0.0` is `-Infinity`.
- **Integer overflow wraps around.** `byte b = 127; b++;` leaves `b` at −128 (`01111111` + 1 = `10000000`), and `Integer.MAX_VALUE + 1` is `Integer.MIN_VALUE`. Java doesn't throw. Use `Math.addExact` if you want an exception.
- **`printf` with `%.20f` doesn't print every exact digit.** Java formats from the shortest decimal that identifies the value, which is why you see `…07100000` rather than all of `…071044921875`. `new BigDecimal(f)` shows the exact binary value.

## Review Questions
1. Why does `System.out.println(0.7f)` print `0.7` even though memory doesn't hold exactly 0.7?
2. Write −42 as an 8-bit two's complement number, showing each step.
3. How does Java tell whether a stored `byte` is negative? Why can't a valid positive `byte` have MSB 1?
4. What problem with zero does two's complement solve compared with one's complement?
5. Convert 8.125 to binary and give its normalized form.
6. What are the three fields of a 32-bit `float`, and how many bits does each get?
7. What is the bias for `float` and for `double`, and how is it computed?
8. Why can 8.125 be stored exactly but not 0.7? What should you use when you need exact decimals?

<details>
<summary>Answers</summary>

1. `println` prints the shortest decimal that uniquely identifies the stored `float`, which is "0.7". The actual stored value is 0.699999988079071044921875.
2. +42 = `00101010`. Inverted: `11010101`. Plus 1: `11010110`.
3. It checks the MSB: 1 means negative. The largest `byte` is 127 = `01111111`, and setting the MSB would require a value of 128 or more, which is out of range.
4. One's complement has two zeros (`00000000` and `11111111` = −0). In two's complement, −0 becomes `11111111` + 1, which overflows to `00000000`, so there's only one zero.
5. 8 = `1000` and .125 = `.001`, so 8.125 = `1000.001`₂ = `1.000001 × 2³`.
6. Sign (1 bit), exponent (8 bits, biased) and mantissa/fraction (23 bits, with an implicit leading 1).
7. `float`: 127. `double`: 1023. Formula: 2^(k−1) − 1, where k is the number of exponent bits (8 for `float`, 11 for `double`).
8. .125 = 1/8 is a finite sum of powers of 2, so its binary expansion ends (`.001`). .7 has an infinitely repeating binary expansion (`.1 0110 0110…`) that has to be cut off at 23 (or 52) bits. For exact decimal arithmetic use `BigDecimal` constructed from a `String`.
</details>

## Further Study
- IEEE 754 details: rounding modes, subnormal numbers, ±Infinity, NaN, −0.0
- `Float.floatToIntBits`, `Double.doubleToLongBits`, `Math.ulp`
- Integer overflow, `Math.addExact`, and the `>>` vs `>>>` shift operators
- `BigDecimal` and `RoundingMode`, and why money should never be a `double`
- `strictfp` (and why it became a no-op in Java 17)
- Bitwise operators (`~`, `&`, `|`, `^`) used for bit manipulation

---

[← #03](03-variables-data-types-literals-keywords.md) · [Index](ReadMe.md) · [#05 →](05-type-conversion-promotion-casting.md)
