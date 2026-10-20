# Java Variables & Data Types Explained | Identifiers, Literals, Keywords | JAVA Full Course #3
> Source: https://youtu.be/NtmULLvsABc · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 66:42. The video is in Hindi/Hinglish and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A **variable** is a named container in memory that holds a value. The name is the **identifier**, and the value written in code (e.g. `10`) is a **literal**. Syntax: `dataType identifier = value;`
- Java is **statically typed**: every variable's type has to be declared before compilation.
- There are 8 **primitive** types in four groups. Integers: `byte` (8 bit), `short` (16), `int` (32), `long` (64). Real numbers: `float` (32, single precision), `double` (64, double precision). Characters: `char` (16-bit Unicode). Booleans: `boolean` (`true`/`false` only).
- With *n* bits you get 2ⁿ combinations. Java's integer types are signed, so a `byte` covers −128 to 127 rather than 0 to 255.
- Integer literals can be written in decimal, binary (`0b`), octal (leading `0`) or hex (`0x`), and underscores (`1_000_000`) make them readable. Decimal literals default to `double`, so a `float` needs an `f` suffix. **Keywords** are reserved words you can't use as identifiers.

## Key Concepts

**Variable.** A container (a memory location) that holds a value, such as `firstNumber` holding `2`. *Why it matters:* any program that takes input or computes something has to store data.

**Identifier.** The name you use to refer to a variable (later also to methods, classes and so on). Use descriptive names like `firstNumber`, not `x`. *Why it matters:* readable code.

**Literal.** The actual value written in source code: `10`, `5.23`, `'a'`, `false`. *Why it matters:* literals have types too (`10` is an `int`, `5.23` is a `double`).

**Data type.** Tells Java what kind of value a variable holds and how much memory to reserve. Primitive types are covered now. Non-primitive (reference) types come with OOP.

**Statically typed.** Types are fixed and checked at compile time. You must write `int x`, not just `x`. *Why it matters:* the compiler catches type mistakes before the program runs.

**Binary and bit width.** Computers store everything as bits. A type's width in bits determines its range: n bits give 2ⁿ unique values. Unsigned, the maximum is 2ⁿ − 1. Signed (Java's integer types), the range is −2ⁿ⁻¹ to 2ⁿ⁻¹ − 1.

**ASCII vs Unicode.** ASCII maps about 128 characters (English letters, digits, symbols) to numbers 0 to 127. Java's `char` is **16 bits** and stores **Unicode**, a standard that gives a number to characters from every writing system (Hindi क, Chinese, Greek, ...). *Why it matters:* Java was designed to be used worldwide.

**Declaration vs definition.** `int x;` declares the variable, so the compiler reserves a 32-bit slot named `x`. `x = 4;` gives it a value (the video calls this the definition; the usual Java term is assignment or initialization). `int x = 4;` does both at once.

**Keywords.** Words reserved by the language (`class`, `public`, `static`, `void`, `int`, ...). They can't be used as identifiers.

## Detailed Notes

### What a variable is [▶ ~00:33](https://youtu.be/NtmULLvsABc?t=33)
- We write programs to *do* something: print to the screen, perform calculations, build an app.
- Printing "Hello World" doesn't store anything. A calculator that adds two numbers [▶ ~01:47](https://youtu.be/NtmULLvsABc?t=107) has to keep both inputs (say 2 and 3) in memory before it can add them.
- Memory (RAM) is pictured as a set of boxes. Heap and stack come later in the series. Each value is stored in binary, so 2 is stored as `10`.
- We refer to those locations by name: `x` holds 2 and `y` holds 3, so `x + y` means 2 + 3. A variable is a **container with a name that holds a value** [▶ ~04:42](https://youtu.be/NtmULLvsABc?t=282).
- Use **descriptive names** [▶ ~05:24](https://youtu.be/NtmULLvsABc?t=324): `firstNumber` and `secondNumber` are better than `x` and `y`.
- The name is called an **identifier** [▶ ~07:08](https://youtu.be/NtmULLvsABc?t=428) because it identifies the container. Variables, identifiers, keywords and literals are concepts common to every programming language.

### Treat boilerplate as a black box [▶ ~07:56](https://youtu.be/NtmULLvsABc?t=476)
- Early on, some syntax (`class`, `public static void main(String[] args)`) won't make sense yet. Treat it as a **black box** and focus only on the code between the curly braces of `main`. Learning a language isn't linear, and these pieces get explained later.

### Declaration syntax [▶ ~10:16](https://youtu.be/NtmULLvsABc?t=616)
```
dataType identifier = value;
int      firstNumber = 10;
```
- The **data type** says what kind of value the container holds: integer, real number, boolean, and so on. Java needs it up front because Java is **statically typed** [▶ ~10:55](https://youtu.be/NtmULLvsABc?t=655).
- Every statement ends with a **semicolon** [▶ ~11:28](https://youtu.be/NtmULLvsABc?t=688), just as in C++.

### Primitive vs non-primitive [▶ ~12:03](https://youtu.be/NtmULLvsABc?t=723)
- Primitive types are covered now. Non-primitive types come with OOP.
- There are four groups of primitives (like number categories in maths): **integers** (`byte`, `short`, `int`, `long`), **real numbers** (`float`, `double`), **characters** (`char`) and **booleans** (`boolean`).

### Integer types and ranges [▶ ~13:42](https://youtu.be/NtmULLvsABc?t=822)
- All four store whole numbers. They differ in **range**, i.e. how big a number they can hold: `byte` < `short` < `int` < `long`.
- `10` fits in all four, so any of them works. A value that's too big for the type doesn't compile.

### Binary number system crash course [▶ ~16:25](https://youtu.be/NtmULLvsABc?t=985)
- Decimal is **base 10**: ten symbols (0 to 9). After 9 you reuse them by adding a digit (10, 11, ...), and the places are 1s, 10s, 100s.
- Binary is **base 2** [▶ ~18:30](https://youtu.be/NtmULLvsABc?t=1110): two symbols (0 = off/no current, 1 = on/current). The places are powers of 2: 1, 2, 4, 8, 16, ...
  - 0 → `0`, 1 → `1`, 2 → `10`, 3 → `11`, 4 → `100`, 10 → `1010`.
- Leading zeros don't change the value, so in an 8-bit slot 2 is `00000010`.

### Bit widths [▶ ~20:40](https://youtu.be/NtmULLvsABc?t=1240)

| Type | Bits | Range |
|---|---|---|
| `byte` | 8 | −128 to 127 |
| `short` | 16 | −32,768 to 32,767 |
| `int` | 32 | −2,147,483,648 to 2,147,483,647 (about ±2.1 billion) |
| `long` | 64 | about ±9.22 × 10¹⁸ (quintillions) |

- In Java the integer types are **signed** [▶ ~22:39](https://youtu.be/NtmULLvsABc?t=1359): they hold negative and positive values.
- **Counting combinations** [▶ ~23:54](https://youtu.be/NtmULLvsABc?t=1434): 2 bits give 00, 01, 10, 11, which is 4 = 2² values. In general n bits give 2ⁿ unique combinations.
- **Largest unsigned value** [▶ ~25:44](https://youtu.be/NtmULLvsABc?t=1544): 2ⁿ − 1. With 2 bits that's 3 (`11`), with 3 bits 7 (`111`), and with 8 bits 255 (`11111111` = 128+64+32+16+8+4+2+1).
- So why does a `byte` stop at 127? Because the 256 combinations are split between negatives and non-negatives: −128 to −1 and 0 to 127. To store 255 you need a `short` [▶ ~27:48](https://youtu.be/NtmULLvsABc?t=1668).
- For numbers bigger than `long` Java has a special class. The video leaves it as a teaser; it's `BigInteger`.
- How negative numbers are actually stored is the topic of the next video.

### Real numbers: `float` and `double` [▶ ~30:59](https://youtu.be/NtmULLvsABc?t=1859)
- Decimal values such as 5.23 or 10.02 are called **floating-point numbers**.

| Type | Bits | Approximate range | Precision |
|---|---|---|---|
| `float` | 32 | ±1.4 × 10⁻⁴⁵ to ±3.4 × 10³⁸ | single, about 6 to 7 significant digits |
| `double` | 64 | ±4.9 × 10⁻³²⁴ to ±1.8 × 10³⁰⁸ | double, about 15 to 16 significant digits |

- The ranges are in **scientific notation** [▶ ~32:30](https://youtu.be/NtmULLvsABc?t=1950): `4.9e-324` means 4.9 × 10⁻³²⁴.
- `float` is **single precision** and `double` is **double precision** [▶ ~32:56](https://youtu.be/NtmULLvsABc?t=1976).
- **Prefer `double`** [▶ ~33:17](https://youtu.be/NtmULLvsABc?t=1997). `float` dates from when hardware was optimized for it, and its precision is low. Modern hardware is optimized for `double`, it has twice the range and much better precision, and standard library methods (`Math.sin`, `Math.cos`, and so on) return `double`. `float` is rarely used in production code.

### Characters: `char`, ASCII and Unicode [▶ ~35:36](https://youtu.be/NtmULLvsABc?t=2136)
- `char` holds a single character. Write character literals in **single quotes** [▶ ~37:02](https://youtu.be/NtmULLvsABc?t=2222), e.g. `char alphabet = 'a';`. Without the quotes, `a` would be read as an identifier.
- **ASCII** [▶ ~35:50](https://youtu.be/NtmULLvsABc?t=2150) was the older standard used by C/C++. It covers English letters, digits and symbols, with codes 0 to 127.
- Characters are stored as **numbers**. The character is mapped to an integer code, and that integer is stored in binary. When you read the variable back, its **data type** tells Java whether to show the number or the character. Declared as `char`, the stored code is shown as a character. Declared as `int`, the same bits are shown as a number.
- ASCII can't represent Hindi, Tamil, Chinese and so on. Java wanted worldwide use, so it adopted **Unicode** [▶ ~40:56](https://youtu.be/NtmULLvsABc?t=2456) and made `char` **16 bits**. Every character in every script gets an integer code.

### Booleans [▶ ~42:05](https://youtu.be/NtmULLvsABc?t=2525)
- `boolean` has exactly two values, `true` and `false`. Example: `boolean b = false;`
- **Not like C/C++** [▶ ~43:24](https://youtu.be/NtmULLvsABc?t=2604): there, any non-zero number counts as true and 0 as false. In Java `boolean b = 0;` or `b = 1` **doesn't compile**. Only `true` and `false` are allowed.

### Variable, identifier, literal [▶ ~44:38](https://youtu.be/NtmULLvsABc?t=2678)
- In `int b = 12;`, the container is the **variable**, `b` is the **identifier**, and `12` is the **literal** (literally the value that's stored).

### Code demo: printing all types [▶ ~45:01](https://youtu.be/NtmULLvsABc?t=2701)
- **Comments** [▶ ~45:21](https://youtu.be/NtmULLvsABc?t=2721): `// single line` and `/* multi-line */`. The compiler ignores them, so they're notes for humans.
- The demo declares `byte b = 5; short s = 10; int i = 4000; long l = 10000;` and prints them. Running the file with VS Code's **Run** button prints `5, 10, 4000, 10000`.
- `float f = 10.54;` is an **error** [▶ ~49:17](https://youtu.be/NtmULLvsABc?t=2957): *"Type mismatch: cannot convert from double to float"*. A decimal literal is a `double` by default, so a `float` literal needs the suffix: `10.54f`.
- `char c = 'a';` prints `a`. `boolean bool = false;` prints `false`.

### Number systems for integer literals [▶ ~52:47](https://youtu.be/NtmULLvsABc?t=3167)

| System | Base | Digits | Prefix | 5 written as |
|---|---|---|---|---|
| Decimal | 10 | 0-9 | none | `5` |
| Binary | 2 | 0-1 | `0b` | `0b101` |
| Octal | 8 | 0-7 | `0` | `05` |
| Hexadecimal | 16 | 0-9, A-F (10-15) | `0x` | `0x5` |

- **Binary** [▶ ~54:04](https://youtu.be/NtmULLvsABc?t=3244): `0b` tells the compiler the digits that follow are binary.
- **Octal**: `05` is fine, but `08` is an error ("literal out of range") because 8 isn't an octal digit. Likewise `0b2` is invalid.
- **Hex** [▶ ~55:56](https://youtu.be/NtmULLvsABc?t=3356): `0xF` is 15 and `0xA` is 10.
- These prefixes work for all integer types. Hex floating-point literals exist but are rarely used.

### Scientific notation for doubles [▶ ~58:12](https://youtu.be/NtmULLvsABc?t=3492)
- Standard way: `23.0987`. Scientific way: `6.022e23` means 6.022 × 10²³ (Avogadro's number).

### Underscores in numeric literals [▶ ~59:47](https://youtu.be/NtmULLvsABc?t=3587)
- `long l = 12_34_56_789L;` is easier to read (Indian grouping: 12 crore 34 lakh 56 thousand 789), and the compiler ignores the underscores. They work in floats and doubles too.
- Underscores are **not allowed** next to the decimal point, next to `e`, or at the start or end of the number.

### Declaration vs definition [▶ ~61:59](https://youtu.be/NtmULLvsABc?t=3719)
- Values often come from the user, not from hard-coded literals. Then you declare first and assign later.
- `int x;` is the **declaration**: reserve a 32-bit container called `x`. `x = 4;` gives it a value. `int x = 5;` does both at once.

### Keywords [▶ ~63:41](https://youtu.be/NtmULLvsABc?t=3821)
- Keywords are **reserved words** such as `class`, `public`, `static`, `void`, `int`, `boolean`. You can't name a variable `class`, because the compiler wouldn't know which you meant.
- The video says Java has **68 keywords** [▶ ~65:21](https://youtu.be/NtmULLvsABc?t=3921) and suggests googling the list.
- `goto` and `const` [▶ ~65:38](https://youtu.be/NtmULLvsABc?t=3938) are reserved but **never used**. They were reserved because C/C++ have them and Java's designers thought they might need them.

## Code Examples

### All eight primitives
```java
public class PrimitivesDemo {
    public static void main(String[] args) {
        // Integers
        byte b = 5;
        short s = 10;
        int i = 4000;
        long l = 10000;
        System.out.println("Integer values -> " + b + ", " + s + ", " + i + ", " + l);

        // Real numbers (float needs the f suffix)
        float f = 10.54f;
        double d = 23.0987;
        System.out.println("Floating values -> " + f + ", " + d);

        // Character and boolean
        char c = 'a';
        boolean bool = false;
        System.out.println("Character value -> " + c);
        System.out.println("Boolean value -> " + bool);
    }
}
// Output:
// Integer values -> 5, 10, 4000, 10000
// Floating values -> 10.54, 23.0987
// Character value -> a
// Boolean value -> false
```

### Ranges come from bit widths
```java
public class RangesDemo {
    public static void main(String[] args) {
        System.out.println("byte:  " + Byte.MIN_VALUE + " to " + Byte.MAX_VALUE);
        System.out.println("short: " + Short.MIN_VALUE + " to " + Short.MAX_VALUE);
        System.out.println("int:   " + Integer.MIN_VALUE + " to " + Integer.MAX_VALUE);
        System.out.println("long:  " + Long.MIN_VALUE + " to " + Long.MAX_VALUE);
        System.out.println("8 bits -> combinations: " + (int) Math.pow(2, 8)
                + ", max unsigned: " + ((int) Math.pow(2, 8) - 1));
        System.out.println("char:  " + (int) Character.MIN_VALUE + " to " + (int) Character.MAX_VALUE);
    }
}
// Output:
// byte:  -128 to 127
// short: -32768 to 32767
// int:   -2147483648 to 2147483647
// long:  -9223372036854775808 to 9223372036854775807
// 8 bits -> combinations: 256, max unsigned: 255
// char:  0 to 65535
```

### Literal formats: binary, octal, hex, scientific, underscores
```java
public class LiteralsDemo {
    public static void main(String[] args) {
        byte bin = 0b101;      // binary 101 = 5
        byte oct = 05;         // octal 5 = 5
        byte hex = 0xF;        // hex F = 15
        int hexA = 0xA;        // 10
        int octal10 = 012;     // octal 12 = 8 + 2 = 10 (a classic trap)
        System.out.println(bin + " " + oct + " " + hex + " " + hexA + " " + octal10);

        double avogadro = 6.022e23;               // scientific notation
        long big = 12_34_56_789L;                 // underscores are ignored
        double pi = 3.141_592_653;
        System.out.println(avogadro);
        System.out.println(big);
        System.out.println(pi);

        System.out.println(Integer.toBinaryString(10)); // 1010
        System.out.println(Integer.toHexString(255));   // ff
    }
}
// Output:
// 5 5 15 10 10
// 6.022E23
// 123456789
// 3.141592653
// 1010
// ff
```

### `char` is a number underneath
```java
public class CharDemo {
    public static void main(String[] args) {
        char lower = 'a';
        char upper = 'A';
        char hindi = 'क';           // Unicode: any script works
        System.out.println(lower + " -> " + (int) lower);   // a -> 97
        System.out.println(upper + " -> " + (int) upper);   // A -> 65
        System.out.println(hindi + " -> " + (int) hindi);   // क -> 2325
        char fromCode = 66;          // an int literal that fits in char
        System.out.println(fromCode);                       // B
    }
}
// Output:
// a -> 97
// A -> 65
// क -> 2325
// B
```

### Declaration, then assignment
```java
public class DeclareDefineDemo {
    public static void main(String[] args) {
        int firstNumber;           // declaration: reserves an int slot
        int secondNumber;
        firstNumber = 2;           // assignment (the video calls it definition)
        secondNumber = 3;
        int sum = firstNumber + secondNumber; // declare + initialize together
        System.out.println(sum);
    }
}
// Output:
// 5
```

### Things that don't compile
```java
// does not compile
public class CompileErrors {
    public static void main(String[] args) {
        float f = 10.54;      // error: possible lossy conversion from double to float (needs 10.54f)
        byte tooBig = 200;    // error: 200 is outside -128..127
        int octal = 08;       // error: 8 is not an octal digit
        boolean flag = 0;     // error: int cannot be converted to boolean
        int class = 5;        // error: 'class' is a keyword
        long l = 1_000_;      // error: underscore at end of literal
        int x;
        System.out.println(x); // error: variable x might not have been initialized
    }
}
```

## Common Pitfalls / Tips
- **Decimal literals are `double`.** Write `10.54f` for a `float`. Likewise, an integer literal larger than `int` needs `L`: `10_000_000_000L`. Use capital `L`, since lowercase `l` looks like `1`.
- **Leading zero means octal.** `012` is 10, not 12. Never zero-pad decimal numbers in Java source.
- **Binary for 5 is `101`.** At one point the video says "1001", but that's 9. The value 5 is `0b101`.
- **`char` is unsigned.** The "every number is signed in Java" rule covers `byte`, `short`, `int` and `long` (and the floating types), but `char` ranges from 0 to 65535.
- **Exact `short` range:** 2¹⁶ = 65,536 values split as −32,768 to 32,767. That's −2¹⁵ to 2¹⁵ − 1, not "(2¹⁶ − 1)/2".
- **Local variables must be assigned before use.** Java has no garbage default value for locals: `int x; System.out.println(x);` doesn't compile. (Fields get default values, covered with classes.)
- **The keyword count depends on how you count.** Recent JLS editions list about 50 reserved keywords (including the unused `const` and `goto`, plus `_`), plus a set of contextual keywords such as `var`, `record`, `sealed` and `yield`. `true`, `false` and `null` are literals, not keywords, but you still can't use them as names. Don't memorize "68". Know that the list exists and recognize the common ones.
- **Use `float` only when you have a reason** (huge arrays, graphics APIs). Neither type is exact: never use `float`/`double` for money. Use `BigDecimal` (or integer cents).
- **Naming conventions:** `camelCase` for variables (`firstNumber`). Identifiers can contain letters, digits, `_` and `$`, can't start with a digit, and are case-sensitive.

## Review Questions
1. In `int marks = 95;`, which part is the data type, the identifier and the literal?
2. What does "Java is statically typed" mean?
3. How many distinct values can 8 bits represent? Why does `byte` stop at 127 and not 255?
4. Which integer type would you choose to store 255? And 3,000,000,000?
5. Why does `float f = 10.54;` fail, and what are two ways to fix it?
6. Why did Java choose Unicode and a 16-bit `char` over 8-bit ASCII?
7. What do `0b1010`, `012` and `0x1F` evaluate to?
8. What's the difference between `int x;` and `int x = 4;`? Can you print `x` after only `int x;`?

<details>
<summary>Answers</summary>

1. `int` is the data type, `marks` is the identifier, and `95` is the literal.
2. Every variable's type must be declared in the source and is checked at compile time. You can't create a variable without giving its type.
3. 2⁸ = 256. Java's `byte` is signed, so the 256 values are split into −128 to −1 and 0 to 127.
4. `short` (255 > 127, so a `byte` won't do). For 3,000,000,000 use `long` (it exceeds `int`'s max of about 2.147 billion), written as `3_000_000_000L`.
5. `10.54` is a `double` literal, and assigning a `double` to a `float` is a narrowing conversion. Fix it with `10.54f`, or declare `double f = 10.54;` (or cast: `(float) 10.54`).
6. ASCII only covers about 128 English-centric characters. Java was meant for worldwide use, and Unicode gives a code to characters from every script. That needs more than 8 bits.
7. `0b1010` = 10, `012` (octal) = 10, `0x1F` = 31.
8. `int x;` only declares the variable (reserves space). `int x = 4;` declares it and initializes it. A local variable that's only declared can't be read: the compiler reports "might not have been initialized".
</details>

## Further Study
- Negative numbers in binary (two's complement) and IEEE 754 floating point (next video)
- Type conversion and casting between primitives (video #5)
- Wrapper classes (`Integer`, `Double`, ...) and autoboxing
- `BigInteger` and `BigDecimal` for numbers beyond `long` or for exact decimals
- `var` (local variable type inference, Java 10+): still statically typed
- Full JLS list of keywords and contextual keywords
- Default values of fields vs local variables
- Unicode beyond 16 bits: code points, surrogate pairs and `String.codePoints()`

---

[← #02](02-first-java-program-jvm-jdk-jre.md) · [Index](ReadMe.md) · [#04 →](04-negative-floating-point-storage.md)
