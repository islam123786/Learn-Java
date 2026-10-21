# 03 · Variables & Data Types · Identifiers, Literals, Keywords
> Full notes: [03-variables-data-types-literals-keywords.md](../03-variables-data-types-literals-keywords.md) · [▶ Video](https://youtu.be/NtmULLvsABc)

## Data types

```
                         Data Types
                  ┌──────────┴──────────┐
                  ▼                     ▼
        Primitive Data Types    Non-Primitive Data Types
                  │             [Derived Data Types]
       int ───────┼─────── byte
      long ───────┼─────── float
    double ───────┼─────── char
     short ───────┼─────── boolean
```

| Type | Size | Note |
| :--- | :--- | :--- |
| `byte` | 1 byte | range −2⁸/2 to 2⁸/2 − 1 (−128 to 127) |
| `short` | 2 bytes | |
| `int` | 4 bytes | |
| `float` | 4 bytes | decimal number |
| `long` | 8 bytes | |
| `double` | 8 bytes | decimal number |
| `char` | 2 bytes | supports Unicode as well |
| `boolean` | depends on the JVM | |

- Primitive values go to **stack** memory.
- Non-primitive values are stored in the **heap**.

> More precisely, primitive *local variables* live on the stack. A primitive that is a field of an object is stored with that object in the heap. For non-primitives, the object is in the heap, and a local variable on the stack holds a reference to it.

## ASCII vs Unicode
- `char` holds a single character, written in **single quotes**: `char alphabet = 'a';`
- Characters are stored as **numbers**. Each character maps to an integer code. The data type decides whether you see the number (`int`) or the character (`char`).
- **ASCII** was the older standard used by C/C++. It covers English letters, digits and symbols (codes 0 to 127).
- ASCII can't represent Hindi, Tamil, Chinese and so on. Java adopted **Unicode** and made `char` **16 bits**, so every character in every script gets a code.

## Declaration vs definition
- `int x;` is the **declaration**: it reserves a 32-bit container called `x`.
- `x = 4;` gives it a value.
- `int x = 5;` does both at once.
- Values often come from the user, so you declare the variable first and assign it later.

## Reserved words (keywords)
- Keywords are **reserved words** such as `class`, `public`, `static`, `void`, `int`, `boolean`. You can't use them as variable names.
- `goto` and `const` are reserved but **never used**. They were kept because C/C++ have them.
- The video says 68 keywords, but the count depends on how you count. Recent Java versions have about 50 reserved keywords, plus contextual ones like `var`, `record` and `yield`. `true`, `false` and `null` are literals, not keywords, but you still can't use them as names.

**Remember:** 8 primitive types, each with a fixed size (except `boolean`). `char` is 2 bytes because Java uses Unicode.
