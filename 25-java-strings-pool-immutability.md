# Java Strings | String Pool, Immutability, Internals | Java Full Course #25
> Source: https://youtu.be/N0b8lRXtK_Y · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 68:59. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- A String is a **sequence of characters**: an abstraction over a character array. It's wrapped in the `java.lang.String` class so that you get comparison, concatenation, substring and so on without writing the algorithms yourself.
- `String` is **immutable**. Methods like `concat` return a *new* String and never change the original. That's what makes Strings safe for passwords, URLs and hash keys.
- **String literals go to the String Pool** (a special area inside the heap), where identical literals are **shared**. `new String("x")` always creates a **new heap object**.
- Golden rule: **compile-time constants go to the pool, runtime-created strings go to the regular heap.** So `"Ja" + "va" == "Java"` is `true`, but `s1 + "va" == "Java"` is `false` (when `s1` is a normal variable).
- Internals (Java 9+): a `byte[] value`, a `byte coder` (0 = LATIN1, 1 = UTF16) and a cached `int hash`. **Compact strings** store Latin-1 text in one byte per character instead of two.
- The downside of immutability: building a string in a loop creates many throwaway objects. That's what **StringBuilder and StringBuffer** are for (next video).

## Key Concepts

**Character vs String.** A `char` is a primitive holding one human-readable symbol (stored as its Unicode number, e.g. `'a'` = 97). A String is an ordered sequence of them. *Why it matters:* it explains why Strings have indexes, lengths and per-character operations.

**Why a String class.** You *could* use a `char[]` everywhere, but then you'd write comparison, concatenation, substring and search yourself. Strings are used for almost everything (names, passwords, emails, URLs, keys), so Java ships a rich class. *Why it matters:* String is the most-used non-primitive type.

**Immutability.** Once a String object is created, its contents never change. Any "modification" creates a new object and, if you reassign, moves the reference. *Why it matters:* it allows pooling and hash caching, makes Strings thread-safe, and stops security-sensitive values from being changed behind your back.

**String Pool.** A table of shared String objects for literals (the "intern pool"). Writing `"hello"` twice gives one object. *Why it matters:* it saves a lot of memory, and it's the reason `==` sometimes "works" on Strings.

**Literal vs `new`.** `String s = "hello"` uses the pool. `new String("hello")` creates a separate heap object (and the literal argument is in the pool too). *Why it matters:* it decides what `==` returns. This is the classic interview topic.

**Compile-time constant.** An expression the compiler can fully evaluate, such as literals, literal concatenations and `final` variables initialized with constants. These are folded into one pooled literal. *Why it matters:* it explains every "is it `true` or `false`?" puzzle.

**Compact strings (`byte[]` + `coder`).** Since Java 9 the characters are stored in a `byte[]`. If every char fits in Latin-1 (0–255), it's 1 byte per char (`coder = 0`). Otherwise the whole string is UTF-16 at 2 bytes per char (`coder = 1`). *Why it matters:* it roughly halves memory for typical English and ASCII text.

**Cached hash.** String stores its computed `hashCode()` in a `hash` field the first time it's called. That's only safe because the string can never change. *Why it matters:* it makes String a fast `HashMap`/`HashSet` key.

## Detailed Notes

### Characters → Strings [00:00]
- A **character** is any symbol a human can read: letters (`A`, `a`), digits (`0`–`9`) and special symbols (`.`, `,`, …), in any language. Java has the primitive `char` for one character.
- **A String is a sequence of characters.** A sequence is stored in an array, so a String is basically an abstraction over a **character array**. "Aditya" is `['A','d','i','t','y','a']`.
- In memory each character is stored as its **Unicode value** [02:20], e.g. `a` = 97.

### Why a separate String type? [02:45]
- With a raw `char[]` you'd have to write your own algorithms for comparison, concatenation, substring and more.
- `String` is a **non-primitive** class (just as `Integer` wraps `int`) that bundles fields, constructors and lots of methods.
- Strings are everywhere (names, passwords, emails, URLs, DB connection strings), so having all this built in is essential.
- It lives in **`java.lang`** [05:39] (auto-imported, like `System` and `Object`).

### How is `String s = "Hello"` even legal? [06:37]
- Objects normally need `new`. With `Integer i = 4`, Java secretly calls `Integer.valueOf(4)` (autoboxing). For Strings, the literal syntax is a special shortcut: Java creates or reuses the object for you (see the String Pool below).

### Immutability [08:03]
- `String` is an **immutable class**. Recall the recipe: make the class `final` (no subclass can change behavior), have no setters, keep fields `private final`, and set everything in the constructor.
- **Why Java made Strings immutable** [08:42]: Strings hold passwords, URLs (DB and app), and keys and hashes for `HashMap`/`HashSet`. If a String could change after being used as a key, hashes would change and hash collections would break. Security-sensitive values could also be modified.
- Demo [10:16]: `String s1 = "Hello"; s1.concat(" World"); System.out.println(s1);` prints **`Hello`**. `concat` built a new `"Hello World"` object that nothing references. To keep it, write `s1 = s1.concat(" World");`.

> **Correction:** the video lists "make all methods final" as part of the immutability recipe. Once the class itself is `final`, that's redundant, since nothing can override its methods. What really matters is: final class, private final fields, no mutators, and defensive copies of any mutable inputs.

### Two ways to create a String [11:41]
1. **Literal**: `String s = "hello";`
2. **`new` operator**: `String s = new String("hello");`

### The String Pool [13:59]
- Millions of Strings are created in typical apps, and a new object for every one would waste memory. Java **reuses** them.
- Inside the heap, Java reserves a special area: the **String Pool**.
- `String s1 = "Hello";` checks whether the pool already has `"Hello"`. If not, it creates one there, and `s1` (on the stack) points to it.
- `String s2 = "Hello";` finds the existing `"Hello"` and points `s2` at the **same object**. `s3 = "Hello"` does the same. Three references, one object.
- `String s4 = "World";` isn't in the pool yet, so a new pooled object is created.
- History [16:40]: the pool used to live in a special area called **PermGen**. In modern Java (since Java 7) it's part of the regular heap.

### `new String(...)` [16:58]
- `new String("Hello")` always creates a **new object in the regular heap**, even if the content is identical. Two `new` calls give two objects.
- The literal is reused, while `new` gives every reference its own object.

### `==` vs `equals()` for Strings [18:35]
- `==` compares **references**. `String` **overrides `equals()`** to compare **contents**.
- Code: `"Hello" == "Hello"` (two literals) is **true**, because they're the same pooled object. `new String("Aditya") == new String("Aditya")` [19:59] is **false**, because they're two heap objects. `equals` is **true** in both cases.

### The golden rule [20:59]
> **Only compile-time constants go to the String Pool. Strings created at runtime go to the regular heap.**

Interview questions on Strings mostly test this rule.

**Case 1: compile-time concatenation** [22:11]
- `String s1 = "Ja" + "va"; String s2 = "Java";` gives `s1 == s2` → **true**.
- Both operands are literals, so the compiler folds them into `"Java"` *at compile time*. It's a single pooled literal, and `s2` reuses it.

**Case 2: runtime concatenation** [23:47]
- `String s1 = "Ja"; String s2 = s1 + "va"; String s3 = "Java";` gives `s2 == s3` → **false**.
- `s1` is a variable, so the compiler can't know its value. The concatenation happens at **runtime**, and the result is a new object in the **regular heap**.
- Side effect: the literal `"va"` still gets its own pool entry, referenced by nobody, and is eventually garbage-collected. (GC of the pool is slower or rarer; that comes up in the GC video.)
- `s3 = "Java"` is a literal in the pool. Same content, different object, so `==` is false.

> **Correction:** the "variable" part matters. If the variable is a **compile-time constant**, i.e. `final String s1 = "Ja";`, then `s1 + "va"` is folded at compile time and **is** the pooled `"Java"`, so `==` gives **true**. See the code example below.

**Case 3: plain assignment** [28:21]
- `String s1 = "Java"; String s2 = s1;` gives `s1 == s2` → **true**. Both references point to the same pooled object.

> **Correction:** the video explains this as "assignment is resolved at compile time, so s2 goes to the pool". More precisely: `s2 = s1` **copies the reference** and creates no String object at all, so the result is the same whether `s1` points into the pool or the heap.

**Case 4: reassignment** [30:01]
- `String s = "Hello"; s = "World";` creates (or reuses) a pooled `"World"`. `s` now points there, and `"Hello"` stays in the pool with no reference until it's collected. Printing gives `World`. The original object was never modified, only the reference moved.

**Case 5: `new String("Hello")` creates up to two objects** [32:23]
- `new String("Hello")` puts a new object in the regular heap, *and* the literal `"Hello"` passed to the constructor lives in the pool (if it wasn't there already).
- So a later `String s2 = "Hello";` reuses that pooled object and no new one is created. Meanwhile `s == s2` is **false** (heap vs pool).
- Code walk-through of all these cases [33:30] [37:39] [39:40]: same results as above.

### The problem with immutability [41:04]
- Code: `String s = ""; for (int i = 0; i < 5; i++) { s += i; System.out.println(s); }` prints `0`, `01`, `012`, `0123`, `01234`.
- `s += i` means `s = s + i`. That's runtime concatenation, so **every iteration creates a new heap String**: "0", "01", "012", …. `s` moves to the newest one, and the old ones become garbage waiting for the GC. The empty literal `""` stays in the pool.
- So a simple loop leaves lots of dead objects behind. The fix is to use **mutable** builders: **`StringBuilder`** and **`StringBuffer`** [48:01] (next video).

> **Note:** since Java 9, `javac` compiles `+` concatenation to an `invokedynamic` call (`StringConcatFactory`) instead of an explicit `StringBuilder`. Either way, each `s += i` still produces a new String, so the advice holds: inside loops, use one `StringBuilder`.

### Inside the String class [48:36]
Roughly (simplified):
```
public final class String {
    private final byte[] value;   // the characters (Java 9+; char[] before)
    private final byte coder;     // 0 = LATIN1, 1 = UTF16
    private int hash;             // cached hashCode, 0 until first computed
    ...
}
```

### From `char[]` to `byte[]` (compact strings) [49:25]
- Before Java 9, `value` was a `char[]`. A Java `char` is **2 bytes** [54:22], so `"Java"` used **8 bytes** of character data.
- Since Java 9, `value` is a `byte[]`. For `"Java"` it holds the code values `74, 97, 118, 97` [52:12], at **1 byte each**, so **4 bytes**. That's 50% less.
- Why that works: most strings in real programs are English letters, digits and keyboard symbols, and all of them fit in one byte. `char` is 2 bytes only because Java supports all of Unicode (Hindi, Chinese, …). For example, Hindi `क` needs 2 bytes [56:09].

> **Correction:** the video calls the 0–255 range "ASCII". **ASCII is only 0–127.** The one-byte encoding Java uses is **Latin-1 (ISO-8859-1)**, which covers 0–255 (ASCII plus Western-European letters like `é`). Also, a `char` is a UTF-16 *code unit*: characters outside the Basic Multilingual Plane (e.g. most emoji) need **two** chars (a surrogate pair), i.e. 4 bytes.

### The `coder` field [60:21]
- `coder` tells String how to read `value`:
  - **0 = LATIN1** [61:04]: every character fits in 1 byte, so read 1 byte per character.
  - **1 = UTF16**: read **2 bytes per character**.
- Example: `"Java"` has `coder = 0`, so 74 → J, 97 → a, 118 → v, 97 → a.
- Example [62:45]: `"कग"` (two Hindi characters) has `coder = 1`. `value` has 4 bytes, read 2 at a time: क, then ग.

> **Note:** the choice is made **per string**. If even one character is outside Latin-1, the *entire* string is stored as UTF-16 (2 bytes for every character, ASCII ones included). The JVM flag `-XX:-CompactStrings` turns the optimization off.

### The cached `hash` [66:09]
- `s.hashCode()` is computed on first use (the formula walks over all the characters) and saved in the `hash` field, which acts as a **cache**. Later calls, including those from `HashMap`/`HashSet`, return the stored value instantly.
- This is only safe because Strings are immutable. If the content could change, the cached hash would become stale.

> **Correction:** the video says `hashCode()` "comes from the Object class". It's *declared* in `Object`, but `String` **overrides** it with a content-based formula: `s[0]*31^(n-1) + s[1]*31^(n-2) + … + s[n-1]`. That's why equal strings always have equal hash codes.

### Summary: three optimizations [67:54]
1. **String Pool**: identical literals are shared, so far fewer objects get created.
2. **`char[]` → `byte[]` + `coder`**: about half the memory for Latin-1 text.
3. **Cached hash**: the hash is computed once and reused.
Plus the foundation that enables all three: **immutability**.

## Code Examples

Each block is a complete program. Save it under the class name (e.g. `StringImmutabilityDemo.java`) and run it with `java StringImmutabilityDemo.java`.

### Immutability: methods return new strings
```java
public class StringImmutabilityDemo {
    public static void main(String[] args) {
        String s1 = "Hello";
        s1.concat(" World");              // result discarded: s1 unchanged
        System.out.println(s1);           // Hello

        s1.toUpperCase();                 // also returns a NEW string
        System.out.println(s1);           // Hello

        String s2 = s1.concat(" World");  // keep the new object
        System.out.println(s2);           // Hello World
        System.out.println(s1 == s2);     // false (two different objects)

        s1 = s1 + "!";                    // reassignment moves the reference
        System.out.println(s1);           // Hello!
    }
}
```

### The String Pool and the golden rule, case by case
```java
public class StringPoolDemo {
    public static void main(String[] args) {
        // Literals are shared
        String s1 = "Hello";
        String s2 = "Hello";
        System.out.println(s1 == s2);            // true

        // new always creates a separate heap object
        String s3 = new String("Aditya");
        String s4 = new String("Aditya");
        System.out.println(s3 == s4);            // false
        System.out.println(s3.equals(s4));       // true (String overrides equals)

        // Case 1: compile-time constant folding
        String a = "Ja" + "va";
        String b = "Java";
        System.out.println(a == b);              // true

        // Case 2: runtime concatenation -> regular heap
        String ja = "Ja";
        String c = ja + "va";
        System.out.println(c == b);              // false
        System.out.println(c.equals(b));         // true

        // final constant variable -> folded at compile time again
        final String fja = "Ja";
        String d = fja + "va";
        System.out.println(d == b);              // true

        // Case 3: plain assignment copies the reference
        String e = b;
        System.out.println(e == b);              // true

        // Case 4: reassignment
        String s = "Hello";
        s = "World";
        System.out.println(s);                   // World

        // Case 5: new String("Hello") vs the pooled literal
        String s9 = new String("Hello");
        String s10 = "Hello";
        System.out.println(s9 == s10);           // false (heap vs pool)
        System.out.println(s9.intern() == s10);  // true  (intern returns the pooled copy)
        System.out.println(c.intern() == b);     // true
    }
}
```

### Characters are numbers: Unicode values and encodings
```java
import java.nio.charset.StandardCharsets;

public class CharsAndBytesDemo {
    public static void main(String[] args) {
        char[] name = {'J', 'a', 'v', 'a'};
        String s = new String(name);             // String built from a char array
        System.out.println(s);                   // Java

        for (char ch : s.toCharArray()) System.out.print((int) ch + " ");
        System.out.println();                    // 74 97 118 97

        // One byte per char in Latin-1, two in UTF-16 (what compact strings exploit)
        System.out.println(s.getBytes(StandardCharsets.ISO_8859_1).length); // 4
        System.out.println(s.getBytes(StandardCharsets.UTF_16BE).length);   // 8

        String hindi = "कग";           // the two Hindi letters "ka" and "ga"
        System.out.println(hindi.length());      // 2 (two chars)
        System.out.println((int) hindi.charAt(0) + " " + (int) hindi.charAt(1)); // 2325 2327 (> 255: needs UTF16)
        System.out.println(hindi.getBytes(StandardCharsets.UTF_16BE).length);    // 4

        String emoji = "😀";           // one emoji = a surrogate pair
        System.out.println(emoji.length());      // 2 (chars), even though it's 1 symbol
        System.out.println(emoji.codePointCount(0, emoji.length())); // 1
    }
}
```

### String's hashCode formula (and why caching is safe)
```java
public class StringHashDemo {
    // Same formula String uses: s[0]*31^(n-1) + ... + s[n-1]
    static int manualHash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 31 * h + s.charAt(i);
        }
        return h;
    }

    public static void main(String[] args) {
        String s = "Java";
        System.out.println(s.hashCode());                        // 2301506
        System.out.println(manualHash(s));                       // 2301506
        System.out.println(new String("Java").hashCode() == s.hashCode()); // true (content-based)
        System.out.println("Aa".hashCode() == "BB".hashCode());  // true (collision: equal hash, unequal strings)
        System.out.println("Aa".equals("BB"));                   // false
    }
}
```

### The immutability problem in loops, and the fix
```java
public class ConcatInLoopDemo {
    public static void main(String[] args) {
        String s = "";
        for (int i = 0; i < 5; i++) {
            s += i;                 // creates a NEW String every iteration
            System.out.println(s);
        }
        // 0
        // 01
        // 012
        // 0123
        // 01234

        StringBuilder sb = new StringBuilder(); // one mutable buffer
        for (int i = 0; i < 5; i++) {
            sb.append(i);
        }
        System.out.println(sb.toString());      // 01234
    }
}
```

## Common Pitfalls / Tips
- **Never compare String contents with `==`.** Use `equals` (or `equalsIgnoreCase`). `==` only *seems* to work for literals because of pooling, and fails for runtime-built strings.
- **Write `"literal".equals(var)`** (or `Objects.equals(a, b)`) to avoid a `NullPointerException` when `var` might be `null`.
- **String methods don't modify in place.** `s.trim();`, `s.toUpperCase();` and `s.concat(x);` on their own lines do nothing useful. Assign the result.
- **Avoid `new String("literal")`.** It's almost always pointless and creates an extra object. Use the literal.
- **`final` changes `==` puzzles.** `final String a = "Ja"; a + "va" == "Java"` is `true`. The same code without `final` gives `false`.
- **`intern()` returns the pooled instance, and you must use the returned value.** `s.intern();` on its own doesn't change `s`.
- **Building strings in loops: use `StringBuilder`.** `+=` in a loop is O(n²) and creates garbage.
- **`length()` counts UTF-16 chars, not visible characters.** Emoji and some scripts use surrogate pairs. Use `codePointCount` or `codePoints()` when that matters.
- **Store passwords in `char[]` (and clear them) where an API allows it.** Immutable Strings linger in memory until garbage-collected and can't be wiped.

## Review Questions
1. What is a String, conceptually, and why does Java provide a `String` class instead of making you use `char[]`?
2. What does "Strings are immutable" mean? Why did Java choose immutability?
3. What does `String s1 = "Hello"; s1.concat(" World"); System.out.println(s1);` print, and why?
4. Explain the String Pool. How many objects exist after `String a = "Hi", b = "Hi", c = "Hi";`?
5. How many objects can `new String("Hello")` create, and where?
6. State the golden rule. Predict: `"Ja" + "va" == "Java"`, `s1 + "va" == "Java"` (with `String s1 = "Ja"`), and the same with `final String s1 = "Ja"`.
7. Why did Java 9 switch String's internal storage from `char[]` to `byte[]`, and what does the `coder` field do?
8. Why can String cache its hash code, and what does the cache speed up?

<details>
<summary>Answers</summary>

1. A sequence of characters, essentially an abstraction over a character array. The class provides ready-made operations (comparison, concatenation, substring, search, case conversion, …) that you'd otherwise have to code by hand for one of the most-used data types.
2. A String object's content can never change after creation, and "modifying" methods return new objects. That protects passwords, URLs and hash keys from being changed, keeps `HashMap`/`HashSet` working, makes sharing through the pool safe, and makes Strings thread-safe.
3. `Hello`. `concat` returns a new `"Hello World"` object, the result isn't assigned, and `s1` still points to the original unchanged `"Hello"`.
4. A special area of the heap holding unique String objects for literals. A literal is looked up there first and reused if present. After the three assignments there's **one** String object, with three references to it.
5. Up to **two**: one new object in the regular heap (always), plus the pooled object for the literal `"Hello"` if it wasn't already in the pool.
6. Compile-time constants go to the pool, runtime-created strings go to the heap. `"Ja" + "va" == "Java"` is **true** (folded at compile time). With a non-final `s1`, it's **false** (runtime concatenation, new heap object). With `final String s1`, it's **true** (`s1` is a constant, so it's folded again).
7. A Java `char` is 2 bytes, but most strings only contain Latin-1 characters that fit in 1 byte. A `byte[]` halves the memory for those. `coder` records the encoding of each string: 0 = LATIN1 (read 1 byte per char), 1 = UTF16 (read 2 bytes per char).
8. Because the content can never change, the hash computed the first time stays valid forever. It's stored in the `hash` field, so repeated `hashCode()` calls, such as those made by `HashMap`/`HashSet` operations, skip recomputing it.
</details>

## Further Study
- `String.intern()` and pool sizing (`-XX:StringTableSize`)
- JEP 254: Compact Strings; JEP 280: Indify String Concatenation
- Java memory model basics: heap, stack, metaspace (vs the old PermGen)
- `StringBuilder` vs `StringBuffer` (next video)
- Unicode, UTF-8 vs UTF-16, code points and surrogate pairs
- Text blocks (`"""`, Java 15) and `String.formatted`
- How to design your own immutable class
- Garbage collection and string deduplication (`-XX:+UseStringDeduplication` in G1)

---

[← #24](24-java-interfaces-default-functional-marker.md) · [Index](ReadMe.md) · [#26 →](26-java-string-methods-stringbuilder-buffer.md)
