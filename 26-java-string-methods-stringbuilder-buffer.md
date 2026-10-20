# Java Strings Part 2 | All String Methods + StringBuilder vs StringBuffer | Java Full Course #26
> Source: https://youtu.be/JcAD9a22Rks · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 75:54. The transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `String` has many constructors: empty, from a String, from a `char[]` or `byte[]` (whole or partial, given as offset + count), and from a `StringBuilder`/`StringBuffer`. Input arrays are **copied**, so later changes to them don't affect the String.
- Learn String methods **by group, not by heart**: length/emptiness, character access, comparison, searching, extraction/transformation, conversion, and advanced (`intern`, `format`). Your IDE lists the rest.
- Every String "modification" creates a new object. **`StringBuilder`** and **`StringBuffer`** are **mutable** alternatives backed by a growable `byte[]` buffer (default capacity 16, growing to `old*2 + 2`).
- They share the same API (`append`, `insert`, `delete`, `replace`, `reverse`, `setCharAt`, `capacity`, `ensureCapacity`, `trimToSize`, …). **StringBuffer is synchronized (thread-safe) but slower. StringBuilder is not synchronized but faster**, and it's what you'll use about 90% of the time.
- Interview trap: **`StringBuilder`/`StringBuffer` don't override `equals()`**, so `sb1.equals(sb2)` compares references. Compare `sb1.toString().equals(sb2.toString())`, or use `sb1.compareTo(sb2) == 0` (Java 11+).

## Key Concepts

**String constructors.** `new String()`, `new String("x")`, `new String(char[])`, `new String(char[], offset, count)`, `new String(byte[])`, `new String(byte[], offset, count)`, `new String(StringBuilder)` and `new String(StringBuffer)`. All create **heap** objects. *Why it matters:* you'll convert to and from arrays and builders all the time.

**Method groups.** Length/emptiness (`length`, `isEmpty`, `isBlank`). Access (`charAt`, `toCharArray`). Comparison (`equals`, `equalsIgnoreCase`, `compareTo`). Searching (`contains`, `indexOf`, `lastIndexOf`, `startsWith`, `endsWith`). Extraction/transformation (`substring`, `toUpperCase`/`toLowerCase`, `trim`/`strip`, `repeat`, `replace`/`replaceAll`, `split`/`String.join`). Conversion (`String.valueOf`, `getBytes`). Advanced (`intern`, `String.format`). *Why it matters:* knowing a method exists is what counts. The syntax is one IDE autocomplete away.

**Lexicographic comparison (`compareTo`).** Dictionary order. It returns negative, zero or positive. *Why it matters:* sorting, `Comparable`, and `TreeMap` keys.

**Half-open ranges `[start, end)`.** `substring`, `delete` and `replace` include `start` and exclude `end`. *Why it matters:* it's the most common off-by-one source.

**`intern()`.** Returns the String Pool's copy of a string, adding it to the pool if needed. *Why it matters:* you can deduplicate heap strings, and it's a common interview question.

**`String.format`.** A template with placeholders (`%s`, `%d`, `%.2f`, …) instead of long `+` chains. *Why it matters:* readable output.

**StringBuilder internals.** It holds a `byte[] value` (with a hidden coder, as in String), a `count` (characters used) and a capacity (`value.length`). When `count` would exceed the capacity, it grows to `old*2 + 2` and copies. *Why it matters:* you understand amortized O(1) appends and when to presize.

**Thread safety.** StringBuffer's methods are `synchronized`, so only one thread at a time can modify it. That prevents corrupted state when several threads share one buffer, at the cost of locking overhead. *Why it matters:* you pick the right class (usually StringBuilder, with no sharing between threads).

## Detailed Notes

### Recap: literal vs `new` [▶ ~00:00](https://youtu.be/JcAD9a22Rks?t=0)
- `String s1 = "Hello";` goes to the **String Pool**. `String s2 = new String("Hello");` goes to the **regular heap** (and the literal argument is also in the pool). See #25.

### String constructors [▶ ~01:40](https://youtu.be/JcAD9a22Rks?t=100)
- **Empty** [▶ ~02:16](https://youtu.be/JcAD9a22Rks?t=136): `new String()` and `new String("")` both give an empty string, which prints nothing (not `null`, not a space).
- **From a String**: `new String("Hello")`. A heap object, plus the pooled literal.
- **From another String variable**: `new String(s3)` creates another heap copy, while `s3` stays the pooled one.
- **From a `char[]`** [▶ ~05:13](https://youtu.be/JcAD9a22Rks?t=313): `new String(arr)` gives `"Aditya"`. The String **copies** the array, so `arr[0] = 'B'` afterwards doesn't change it. That's immutability at work.
- **From part of a `char[]`**: `new String(arr, 0, 6)` takes the first 6 chars of `"Aditya Tandon"`, giving `"Aditya"`. The IDE shows the parameters as **`(value, offset, count)`** [▶ ~09:03](https://youtu.be/JcAD9a22Rks?t=543). The one-argument version simply uses `offset = 0, count = value.length`.
- **From a `byte[]`** [▶ ~06:41](https://youtu.be/JcAD9a22Rks?t=401): `new String(new byte[]{97, 98, 99})` gives `"abc"`. You can also give an offset and count: `(arr2, 0, 2)` gives `"ab"`.
- **From `StringBuilder`/`StringBuffer`** [▶ ~12:05](https://youtu.be/JcAD9a22Rks?t=725): `new String(sb)` gives `"Hello"`.

> **Correction:** the video describes the third argument as an "ending index, exclusive", written `[0, 6)`. The parameter is actually a **count** (how many chars to take, starting at `offset`). The two only agree when `offset` is 0. `new String(arr, 7, 6)` takes 6 chars starting at index 7 ("Tandon"), not indexes 7 to 6.

### Don't memorize, group [▶ ~15:32](https://youtu.be/JcAD9a22Rks?t=932)
- The String class has dozens of methods, many of them overloaded. Learn **what operations exist**. Type `s.` in the IDE for the rest.

> **Correction:** while motivating this, the video mentions a String `reverse` method. **`String` has no `reverse()`.** Reversing is done with `new StringBuilder(s).reverse().toString()`.

### Length and emptiness [▶ ~15:34](https://youtu.be/JcAD9a22Rks?t=934)
- `s.length()` is a **method** for Strings, while arrays use the **field** `arr.length` (no parentheses).
- `s.isEmpty()` is true only when `length() == 0`.
- `s.isBlank()` (**Java 11**) [▶ ~18:08](https://youtu.be/JcAD9a22Rks?t=1088) is true when the string is empty **or contains only whitespace**.
- For `"     "` (5 spaces): `length()` is 5, `isEmpty()` is false, `isBlank()` is true.

### Character access [▶ ~19:13](https://youtu.be/JcAD9a22Rks?t=1153)
- `s.charAt(2)` on `"Aditya"` gives `'i'` (0-based index).
- `s.toCharArray()` [▶ ~20:22](https://youtu.be/JcAD9a22Rks?t=1222) returns a **new** `char[]` copy: `['A','d','i','t','y','a']`.

### Comparison [▶ ~20:57](https://youtu.be/JcAD9a22Rks?t=1257)
- `s1 == s2` compares references. Two `new String("Aditya")` objects give **false**.
- `s1.equals(s2)` compares content and gives **true**. String overrides `Object.equals` (see #22).
- `s1.equalsIgnoreCase(s2)` [▶ ~22:42](https://youtu.be/JcAD9a22Rks?t=1362) is true for `"Aditya"` vs `"ADITYA"`.
- `s1.compareTo(s2)` [▶ ~23:26](https://youtu.be/JcAD9a22Rks?t=1406) is a **lexicographic** (dictionary-order) comparison. It's negative if s1 comes first, 0 if the strings are equal, positive if s1 comes after.
  - `"ABC".compareTo("ABD")` is −1. `"ABC".compareTo("ABE")` is −2. Equal strings give 0.

> **Correction:** the video suggests `compareTo` adds up the characters' ASCII values and subtracts the totals. It actually finds the **first index where the strings differ** and returns `s1.charAt(k) - s2.charAt(k)` (C − D = −1, C − E = −2). If one string is a prefix of the other, it returns the **length difference** (`"ab".compareTo("abcd")` is −2). There's also `compareToIgnoreCase`.

### Searching [▶ ~25:57](https://youtu.be/JcAD9a22Rks?t=1557)
- `s.contains("ity")` is true. Its parameter type is **`CharSequence`** [▶ ~26:50](https://youtu.be/JcAD9a22Rks?t=1610), an interface that `String`, `StringBuilder` and `StringBuffer` all implement.
- `s.indexOf('i')` is 2. `indexOf` has many overloads (char given as `int`, String, with a start index…). `s.indexOf("ity")` is also **2**, the index where the substring *starts*. Both return −1 if the target isn't found.
- `s.lastIndexOf("ity")` [▶ ~28:55](https://youtu.be/JcAD9a22Rks?t=1735) on `"Adityaity"` gives 6.
- `s.startsWith("Ad")` is true. `s.endsWith("ya")` [▶ ~29:32](https://youtu.be/JcAD9a22Rks?t=1772) checks the suffix.

### Extraction and transformation [▶ ~29:56](https://youtu.be/JcAD9a22Rks?t=1796)
- `s.substring(1, 4)` on `"Aditya"` gives `"dit"`, using the range **[1, 4)**. `s.substring(1)` gives `"ditya"` (to the end).
- `toUpperCase()` / `toLowerCase()` [▶ ~31:58](https://youtu.be/JcAD9a22Rks?t=1918).
- `trim()` [▶ ~32:16](https://youtu.be/JcAD9a22Rks?t=1936) removes **leading and trailing** whitespace and never touches the inside: `"  Aditya  Tandon  "` becomes `"Aditya  Tandon"`.
- `strip()` [▶ ~33:15](https://youtu.be/JcAD9a22Rks?t=1995) (**Java 11**) does the same but is **Unicode-aware**. It also removes Unicode whitespace such as the em space (`\u2003`), which `trim()` (only chars ≤ `' '`) leaves alone. `stripLeading()` and `stripTrailing()` also exist.
- `repeat(3)` [▶ ~33:38](https://youtu.be/JcAD9a22Rks?t=2018) (**Java 11**) gives `"AdityaAdityaAditya"`.
- `replace('i', 'o')` gives `"Adotya"`. `replace("it", "ABC")` gives `"AdABCya"`. Both overloads replace **every** occurrence.
- `replaceAll("Ad", "Ab")` [▶ ~35:13](https://youtu.be/JcAD9a22Rks?t=2113).
- `split(",")` [▶ ~35:51](https://youtu.be/JcAD9a22Rks?t=2151) turns `"Aditya,Rohit,Rohan"` into `["Aditya", "Rohit", "Rohan"]`, which you then loop over with for-each. It's useful for parsing comma- or hyphen-separated data from the web.
- `String.join("-", "a", "b", "c")` [▶ ~38:00](https://youtu.be/JcAD9a22Rks?t=2280) gives `"a-b-c"`. It's **static**. A typical use is building composite keys.

> **Correction:** the video presents `replaceAll` as "like `replace`, but it replaces all occurrences". **`replace` already replaces all occurrences.** The real difference is that **`replaceAll` (and `replaceFirst`) treat the first argument as a regular expression**. `"a.b.c".replace(".", "-")` gives `"a-b-c"`, but `"a.b.c".replaceAll(".", "-")` gives `"-----"`, because `.` means "any character". Likewise, **`split` takes a regex**, so splitting on a dot needs `split("\\.")`.

### Conversion [▶ ~39:07](https://youtu.be/JcAD9a22Rks?t=2347)
- There's no `new String(int)` constructor. Use the static **`String.valueOf(10)`**, which gives `"10"`. It's overloaded for every primitive, `char[]` and `Object`. Compare `Integer.valueOf`, which boxes an `int`.
- `s.getBytes()` [▶ ~40:06](https://youtu.be/JcAD9a22Rks?t=2406) returns a `byte[]` of the string's encoded bytes.

> **Correction:** the on-screen output for `"Aditya".getBytes()` is transcribed as "65, 105, 116, 127". The actual bytes are **`65, 100, 105, 116, 121, 97`**. Also, `getBytes()` encodes with a charset (the platform default, which is **UTF-8 since Java 18**), so it doesn't expose String's internal storage. For non-ASCII text, call `getBytes(StandardCharsets.UTF_8)` explicitly.

### Advanced: `intern()` [▶ ~41:04](https://youtu.be/JcAD9a22Rks?t=2464)
- `String s5 = new String("Hello");` puts an object in the regular heap. (`"Hello"` is also in the pool, but unreferenced.)
- `String s6 = s5;` gives `s5 == s6` → **true** (same heap object).
- `s5.intern()` looks for `"Hello"` in the pool, adding it if it's missing, and **returns the pooled reference**. After `s6 = s5.intern();`, `s5 == s6` is **false** (heap vs pool).
- Use: deduplicating many identical runtime-created strings so they share one pooled object.

> **Correction:** the video phrases it as "`s5.intern()` and then s6 moves to the pool". `intern()` **doesn't change any variable**. It returns a reference, and you must assign it (`s6 = s5.intern();`). Calling `s5.intern();` on its own line leaves both `s5` and `s6` where they were.

### Advanced: `String.format` [▶ ~44:45](https://youtu.be/JcAD9a22Rks?t=2685)
- The `+` version, `"Hello " + name + ", your age is " + age`, works but is hard to read.
- `String.format("Hello %s, your age is %s", name, age)` (static) fills the placeholders **in order**. `%s` works for anything. Specific ones are `%d` (integer), `%.2f` (2-decimal float), `%n` (newline). Java 15+ also has `"...".formatted(args)`.

### The immutability problem, again [▶ ~47:53](https://youtu.be/JcAD9a22Rks?t=2873)
- Each String operation (e.g. `toUpperCase`) produces a new object while the old one waits for the GC. Many operations mean many objects.
- The fix is two **mutable** classes that modify the same object: **`StringBuilder`** and **`StringBuffer`**.
  - StringBuilder: **not thread-safe**. StringBuffer: **thread-safe**. Otherwise the methods and constructors are the same.
  - Both extend **`AbstractStringBuilder`** [▶ ~49:43](https://youtu.be/JcAD9a22Rks?t=2983) and live in `java.lang`.

### StringBuilder internals [▶ ~50:52](https://youtu.be/JcAD9a22Rks?t=3052)
- Fields (conceptually): a `byte[] value` (like String's, plus an internal, non-exposed coder) and an **`int count`**.
- `new StringBuilder("Java")` allocates a buffer **bigger than needed**, so that `sb.append(" is cool")` can write into the free space without creating a new object.
- **count** is the number of positions used (4 for "Java"). **capacity** is the total size of the internal array, empty slots included.
- **Initial capacity is 16** [▶ ~53:42](https://youtu.be/JcAD9a22Rks?t=3222).
- Growth [▶ ~55:05](https://youtu.be/JcAD9a22Rks?t=3305): when the buffer is full, new capacity = **old × 2 + 2**. 16 → 34 → 70 → …. The old contents are copied into the new array. It's a **dynamic array** handled for you.

### StringBuilder constructors [▶ ~56:47](https://youtu.be/JcAD9a22Rks?t=3407)
- `new StringBuilder()` has capacity **16**.
- `new StringBuilder(50)` has capacity **50**, an explicit initial size.
- `new StringBuilder("Java")` has capacity **16 + length = 20** [▶ ~58:11](https://youtu.be/JcAD9a22Rks?t=3491), and the first 4 slots hold "Java".
- (The video doesn't show it, but there's also `new StringBuilder(CharSequence)`.)

### StringBuilder methods [▶ ~59:18](https://youtu.be/JcAD9a22Rks?t=3558)
- `append("Aditya")`, then `append(" Tandon")`, gives `"Aditya Tandon"`. `append` is overloaded for every type and returns `this`, so calls can be **chained**.
- `insert(2, "O")` [▶ ~60:05](https://youtu.be/JcAD9a22Rks?t=3605) on `"Aditya"` gives `"AdOitya"`. It shifts the rest right and doesn't overwrite.
- `delete(0, 2)` removes the range [0, 2), so `"AdOitya Tandon"` becomes `"Oitya Tandon"`. It modifies the **same object**.
- `deleteCharAt(i)` [▶ ~62:02](https://youtu.be/JcAD9a22Rks?t=3722) removes a single character.
- `replace(1, 3, "xy")` [▶ ~62:17](https://youtu.be/JcAD9a22Rks?t=3737) replaces the range [1, 3). On `"Aditya Tandon"` that gives **`"Axytya Tandon"`** ("di" replaced).
- `reverse()` [▶ ~63:31](https://youtu.be/JcAD9a22Rks?t=3811) reverses in place.
- `charAt(i)` reads a character. `setCharAt(i, 'R')` [▶ ~63:59](https://youtu.be/JcAD9a22Rks?t=3839) overwrites one.
- `length()` vs `capacity()` [▶ ~64:25](https://youtu.be/JcAD9a22Rks?t=3865): for `"Aditya"` in a default builder, length is **6** and capacity is **16**. Appending "Tandon" (12 chars) keeps it at 16. Five more chars make 17, which exceeds 16, so the capacity becomes **34**.
- `ensureCapacity(100)` [▶ ~66:13](https://youtu.be/JcAD9a22Rks?t=3973) guarantees *at least* 100. If the builder is at 34, it becomes **100** (the larger of 100 and 34×2+2 = 70). The alternative is to presize with `new StringBuilder(100)`.
- `trimToSize()` [▶ ~67:22](https://youtu.be/JcAD9a22Rks?t=4042) shrinks the capacity down to the current length (34 → **17**) when you're done appending.

### What StringBuilder doesn't have [▶ ~69:08](https://youtu.be/JcAD9a22Rks?t=4148)
- StringBuilder is **not a superset of String**. Comparison and transformation helpers such as `equalsIgnoreCase`, `toUpperCase`, `split` and `trim` are missing. Build first, then convert to a String.
- Converting back [▶ ~69:51](https://youtu.be/JcAD9a22Rks?t=4191): `String s = sb.toString();`. `println(sb)` works because println calls `toString()` for you.

### Interview trap: `equals` [▶ ~70:51](https://youtu.be/JcAD9a22Rks?t=4251)
- `new StringBuilder("Aditya").equals(new StringBuilder("Aditya"))` is **false**. StringBuilder/StringBuffer **don't override `equals`**, so `Object.equals` (reference comparison) is used.
- To compare contents: `sb1.toString().equals(sb2.toString())`, `sb1.compareTo(sb2) == 0` (Java 11+), or `"Aditya".contentEquals(sb1)`.

### StringBuffer and thread safety [▶ ~71:45](https://youtu.be/JcAD9a22Rks?t=4305)
- Use StringBuffer when you need **mutability plus thread safety**. It has the same methods and constructors as StringBuilder, but all of them are **`synchronized`**.
- Race condition [▶ ~73:24](https://youtu.be/JcAD9a22Rks?t=4404): two threads appending to one shared builder at the same time can interfere, because they both enter the "critical section" together. Synchronization lets one thread in at a time.
- The cost is locking overhead, so **StringBuilder is faster**.
- In practice [▶ ~74:29](https://youtu.be/JcAD9a22Rks?t=4469) StringBuilder is used about 90% of the time. Real code usually keeps builders local to one thread, or protects shared state with its own locks around the critical section. And for values that don't change, a plain immutable `String` is already thread-safe.

> **Correction:** the video illustrates the race with thread 2 appending "B" before thread 1 appends "A" ("HelloBA" instead of "HelloAB"). **Synchronization doesn't fix ordering.** StringBuffer can still produce either order. What it prevents is **corruption**: lost characters, wrong length, or `ArrayIndexOutOfBoundsException` when two unsynchronized appends resize the buffer at the same moment. If order matters, you need extra coordination, such as joining threads or holding one lock around the whole sequence.

## Code Examples

Each block is a complete program. Save it under the class name (e.g. `StringConstructorsDemo.java`) and run it with `java StringConstructorsDemo.java`.

### String constructors
```java
public class StringConstructorsDemo {
    public static void main(String[] args) {
        String s1 = new String();
        System.out.println("[" + s1 + "] " + s1.isEmpty());   // [] true

        String s2 = new String("Hello");
        System.out.println(s2);                               // Hello

        char[] arr = {'A', 'd', 'i', 't', 'y', 'a', ' ', 'T', 'a', 'n', 'd', 'o', 'n'};
        String s5 = new String(arr);
        arr[0] = 'B';                                         // String made a copy
        System.out.println(s5);                               // Aditya Tandon

        String s6 = new String(arr, 7, 6);                    // (array, offset, COUNT)
        System.out.println(s6);                               // Tandon

        byte[] bytes = {97, 98, 99};
        System.out.println(new String(bytes));                // abc
        System.out.println(new String(bytes, 0, 2));          // ab

        StringBuilder sb = new StringBuilder("Hello");
        StringBuffer sbf = new StringBuffer("World");
        System.out.println(new String(sb) + " " + new String(sbf)); // Hello World
    }
}
```

### Length, access, comparison, searching
```java
public class StringQueryMethodsDemo {
    public static void main(String[] args) {
        String spaces = "     ";
        System.out.println(spaces.length() + " " + spaces.isEmpty() + " " + spaces.isBlank()); // 5 false true

        String s1 = new String("Aditya");
        String s2 = new String("Aditya");
        System.out.println(s1.charAt(2));                      // i
        System.out.println(s1.toCharArray().length);           // 6

        System.out.println(s1 == s2);                          // false
        System.out.println(s1.equals(s2));                     // true
        System.out.println(s1.equalsIgnoreCase("ADITYA"));     // true

        System.out.println("ABC".compareTo("ABD"));            // -1
        System.out.println("ABC".compareTo("ABE"));            // -2
        System.out.println("ABC".compareTo("ABC"));            // 0
        System.out.println("ab".compareTo("abcd"));            // -2 (prefix: length difference)
        System.out.println("b".compareTo("a"));                // 1

        System.out.println(s1.contains("ity"));                // true
        System.out.println(s1.indexOf('i'));                   // 2
        System.out.println(s1.indexOf("ity"));                 // 2
        System.out.println(s1.indexOf("xyz"));                 // -1
        System.out.println("Adityaity".lastIndexOf("ity"));    // 6
        System.out.println(s1.startsWith("Ad") + " " + s1.endsWith("ya")); // true true
    }
}
```

### Extraction, transformation, conversion, intern, format
```java
import java.util.Arrays;

public class StringTransformMethodsDemo {
    public static void main(String[] args) {
        String s = "Aditya";
        System.out.println(s.substring(1, 4));        // dit   ([1, 4))
        System.out.println(s.substring(1));           // ditya
        System.out.println(s.toUpperCase());          // ADITYA
        System.out.println("[" + "  Aditya  Tandon  ".trim() + "]");  // [Aditya  Tandon]
        String em = "\u2003Aditya\u2003";              // surrounded by Unicode em spaces
        System.out.println(em.trim().length() + " " + em.strip().length()); // 8 6 (only strip removes them)
        System.out.println(s.repeat(3));              // AdityaAdityaAditya

        System.out.println(s.replace('i', 'o'));      // Adotya
        System.out.println(s.replace("it", "ABC"));   // AdABCya
        System.out.println("a.b.c".replace(".", "-"));    // a-b-c   (literal)
        System.out.println("a.b.c".replaceAll(".", "-")); // -----   (regex: . = any char)
        System.out.println("a.b.c".replaceAll("\\.", "-")); // a-b-c

        String names = "Aditya,Rohit,Rohan";
        String[] arr = names.split(",");
        for (String n : arr) System.out.println(n);   // Aditya / Rohit / Rohan (one per line)
        System.out.println(Arrays.toString("a.b.c".split("\\."))); // [a, b, c]
        System.out.println(String.join("-", "a", "b", "c")); // a-b-c

        String ten = String.valueOf(10);
        System.out.println(ten + 5);                  // 105 (string concatenation)
        System.out.println(Arrays.toString(s.getBytes())); // [65, 100, 105, 116, 121, 97]

        String s5 = new String("Hello");
        String s6 = s5;
        System.out.println(s5 == s6);                 // true
        s6 = s5.intern();                             // must ASSIGN the result
        System.out.println(s5 == s6);                 // false (heap vs pool)
        System.out.println(s6 == "Hello");            // true

        String name = "Aditya";
        int age = 28;
        System.out.println("Hello " + name + ", your age is " + age);           // Hello Aditya, your age is 28
        System.out.println(String.format("Hello %s, your age is %d", name, age)); // Hello Aditya, your age is 28
        System.out.println(String.format("Pi is about %.2f", Math.PI));         // Pi is about 3.14
    }
}
```

### StringBuilder methods, capacity growth and the `equals` trap
```java
public class StringBuilderDemo {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder();
        System.out.println(sb.capacity());                 // 16
        sb.append("Aditya").append(" Tandon");             // chaining: append returns this
        System.out.println(sb);                            // Aditya Tandon

        sb.insert(2, "O");
        System.out.println(sb);                            // AdOitya Tandon
        sb.delete(0, 2);                                   // [0, 2)
        System.out.println(sb);                            // Oitya Tandon
        sb.deleteCharAt(0);
        System.out.println(sb);                            // itya Tandon

        StringBuilder r = new StringBuilder("Aditya Tandon");
        r.replace(1, 3, "xy");                             // replaces "di"
        System.out.println(r);                             // Axytya Tandon
        r.setCharAt(0, 'R');
        System.out.println(r.charAt(0) + " " + r.reverse()); // R nodnaT aytyxR

        StringBuilder c = new StringBuilder();
        c.append("Aditya");
        System.out.println(c.length() + " " + c.capacity()); // 6 16
        c.append("Tandon");
        System.out.println(c.length() + " " + c.capacity()); // 12 16
        c.append("12345");                                   // 17 > 16 -> grow to 16*2+2
        System.out.println(c.length() + " " + c.capacity()); // 17 34
        c.ensureCapacity(100);                               // max(100, 34*2+2=70)
        System.out.println(c.capacity());                    // 100
        c.trimToSize();
        System.out.println(c.capacity());                    // 17

        System.out.println(new StringBuilder("Java").capacity()); // 20 (16 + 4)
        System.out.println(new StringBuilder(50).capacity());     // 50

        StringBuilder sb1 = new StringBuilder("Aditya");
        StringBuilder sb2 = new StringBuilder("Aditya");
        System.out.println(sb1.equals(sb2));                          // false (not overridden!)
        System.out.println(sb1.toString().equals(sb2.toString()));    // true
        System.out.println(sb1.compareTo(sb2) == 0);                  // true (Java 11+)
        System.out.println("Aditya".contentEquals(sb1));              // true

        String back = sb1.toString();                                 // back to an immutable String
        System.out.println(back.toUpperCase());                       // ADITYA
    }
}
```

### StringBuffer shared between threads
```java
public class StringBufferThreadsDemo {
    public static void main(String[] args) throws InterruptedException {
        StringBuffer buffer = new StringBuffer();     // synchronized methods

        Runnable appendA = () -> { for (int i = 0; i < 10_000; i++) buffer.append('A'); };
        Runnable appendB = () -> { for (int i = 0; i < 10_000; i++) buffer.append('B'); };

        Thread t1 = new Thread(appendA);
        Thread t2 = new Thread(appendB);
        t1.start(); t2.start();
        t1.join(); t2.join();

        // No characters lost; the A/B interleaving still varies per run (not printed).
        System.out.println(buffer.length());          // 20000
        // With a StringBuilder here, the length is often < 20000 or an exception is thrown.
    }
}
```

## Common Pitfalls / Tips
- **Assign the results of String methods.** `s.trim();` alone does nothing, since Strings are immutable.
- **`replaceAll`, `replaceFirst`, `split` and `matches` take regexes.** Escape special characters (`"\\."`, `"\\|"`) or use `Pattern.quote(".")`. Use plain `replace` for literal text.
- **`split` drops trailing empty strings.** `"a,b,,".split(",")` has length 2. Use `split(",", -1)` to keep them.
- **Half-open ranges.** `substring(1, 4)`, `delete(0, 2)` and `replace(1, 3, …)` exclude the end index. `new String(char[], offset, count)` and `new String(byte[], offset, count)` take a **count**, not an end index.
- **`isEmpty()` vs `isBlank()`.** User input like `"   "` is "not empty" but is blank. `strip()` (Unicode-aware) is generally preferred over `trim()`.
- **`compareTo` is case-sensitive.** `'Z'` (90) sorts before `'a'` (97). Use `compareToIgnoreCase` or `String.CASE_INSENSITIVE_ORDER`.
- **Never use `==` for content.** Not for Strings, and not for StringBuilders. `sb1.equals(sb2)` is reference equality too.
- **Presize builders when you know the size** (`new StringBuilder(expectedLength)`) to avoid repeated grow-and-copy.
- **Prefer StringBuilder.** Use StringBuffer only when one builder really is shared across threads, which is rare. It's often better to give each thread its own builder.
- **Specify charsets explicitly.** Use `getBytes(StandardCharsets.UTF_8)` and `new String(bytes, StandardCharsets.UTF_8)` instead of relying on the platform default.
- **For many joins, use `StringJoiner` or `Collectors.joining`.** `String.join` also accepts any `Iterable` (e.g. a `List<String>`).

## Review Questions
1. List the String constructors covered. Why doesn't changing the `char[]` after `new String(arr)` affect the String?
2. What's the difference between `isEmpty()` and `isBlank()` for `"     "`?
3. What do `"ABC".compareTo("ABD")` and `"ab".compareTo("abcd")` return, and how is the value computed?
4. What do `"Aditya".substring(1, 4)`, `"Aditya".indexOf("ity")` and `"Aditya".replace('i', 'o')` return?
5. What's the real difference between `replace` and `replaceAll`? What does `"a.b.c".split(".")` return, and why?
6. After `String s5 = new String("Hello"); String s6 = s5.intern();`, what is `s5 == s6`, and what is `s6 == "Hello"`?
7. How does StringBuilder grow? What are the capacities for `new StringBuilder()`, `new StringBuilder("Java")`, and after appending a 17th character to a default builder?
8. Why is `sb1.equals(sb2)` false for two builders containing "Aditya"? When would you pick StringBuffer over StringBuilder?

<details>
<summary>Answers</summary>

1. Empty, from a String, from a `char[]` (whole, or `offset` + `count`), from a `byte[]` (whole, or `offset` + `count`), and from a StringBuilder/StringBuffer. The String copies the array into its own internal storage, which is necessary for immutability.
2. `isEmpty()` is false (length is 5). `isBlank()` is true (only whitespace). `isBlank` was added in Java 11.
3. −1 and −2. `compareTo` finds the first differing index and returns the difference of those chars ('C' − 'D' = −1). If one string is a prefix of the other, it returns the length difference (2 − 4 = −2).
4. `"dit"` (range [1, 4)), `2` (where the substring starts), and `"Adotya"`.
5. Both replace every occurrence, but `replaceAll` interprets its first argument as a **regex**. `"a.b.c".split(".")` returns an **empty array**: `.` matches every character, so every piece is empty and trailing empties are dropped. Use `split("\\.")`.
6. `s5 == s6` is **false** (`s5` is the heap object, `s6` is the pooled one). `s6 == "Hello"` is **true** (both are the pool instance).
7. When `count` would exceed the capacity, the new capacity is `old * 2 + 2` (or the required size if that's larger) and the contents are copied. Default: 16. `"Java"`: 16 + 4 = 20. After 17 characters in a default builder: 34.
8. StringBuilder and StringBuffer don't override `equals`, so `Object.equals` compares references, and these are two different objects. Use StringBuffer only when one mutable buffer must be shared and modified by multiple threads. Its synchronized methods prevent corruption, but cost performance.
</details>

## Further Study
- `java.util.regex` (`Pattern`, `Matcher`) for `split`/`replaceAll`/`matches`
- `StringJoiner`, `Collectors.joining`, text blocks and `String.formatted`
- `Formatter` syntax (`%d`, `%5.2f`, `%-10s`, `%n`)
- `CharSequence` interface: `String`, `StringBuilder`, `StringBuffer`
- `Comparable`/`Comparator` and `String.CASE_INSENSITIVE_ORDER`
- Multithreading basics: `synchronized`, race conditions, locks (upcoming lectures)
- Charsets: `StandardCharsets`, UTF-8 vs UTF-16, JEP 400 (UTF-8 by default)
- `String.chars()` / `codePoints()` streams

---

[← #25](25-java-strings-pool-immutability.md) · [Index](ReadMe.md) · [#27 →](27-generics-bounded-types.md)
