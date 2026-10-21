# 04 · How Java Stores Negative & Floating Point Numbers
> Full notes: [04-negative-floating-point-storage.md](../04-negative-floating-point-storage.md) · [▶ Video](https://youtu.be/iV8hy9DdKFM)

## Negative numbers: two's complement

**Formula:** −N = invert every bit of N (one's complement), then add 1

The **leftmost bit (MSB) is the sign**: 0 means positive, 1 means negative.

**Example:** `byte b = -42;`
```
+42 in binary        00101010
Invert every bit     11010101   (one's complement)
Add 1                11010110   (two's complement)  ← stored
```

**Reading it back:** the MSB is 1, so the number is negative. Apply two's complement again: `11010110` → `00101001` → `00101010` = 42, so the value is **−42**.

## Floating-point numbers: IEEE 754

**Layout**
```
float  (32 bits):  | Sign 1 | Exponent 8  | Mantissa 23 |   bias = 127
double (64 bits):  | Sign 1 | Exponent 11 | Mantissa 52 |   bias = 1023
```

**Formula:** value = (−1)^Sign × (1 + Mantissa) × 2^(Exponent − Bias)

**Steps to store a number**
1. Convert it to binary. For the integer part, keep dividing by 2. For the fraction, keep multiplying by 2 and take the integer digit each time.
2. Normalize to `1.xxxx × 2^e`.
3. Stored exponent = e + 127.
4. Store the sign, then the exponent, then `xxxx` as the mantissa. The leading `1.` isn't stored.

**Example 1: 8.125f (stored exactly)**
```
8.125      = 1000.001₂
Normalize  = 1.000001 × 2³
Exponent   = 3 + 127 = 130 = 10000010
Stored     = 0 | 10000010 | 00000100000000000000000
Read back  = (−1)⁰ × (1 + 2⁻⁶) × 2^(130 − 127) = 1.015625 × 8 = 8.125 ✓
```

**Example 2: 0.7f (stored approximately)**
```
0.7        = 0.1011001100110…₂   (the 0110 pattern repeats forever)
Normalize  = 1.0110011001100… × 2⁻¹
Exponent   = −1 + 127 = 126 = 01111110
Stored     = 0 | 01111110 | 01100110011001100110011   (cut off at 23 bits)
Read back  ≈ 0.699999988… ✗
```

**Remember:** negative integers use two's complement (invert, then add 1). Floats use sign + biased exponent + mantissa. Fractions that repeat forever in binary (like 0.7) can't be stored exactly. Use `BigDecimal` when you need exact decimals.
