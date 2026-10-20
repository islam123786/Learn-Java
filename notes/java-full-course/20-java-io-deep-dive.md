# Java I/O Deep Dive | InputStreams, System.in, Scanner vs BufferedReader | Java Full Course #20
> Source: https://youtu.be/yfMWLZGEPwo · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 71:13. The video is in Hindi and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- `System.out.println("Hello")` breaks down into three parts. `System` is a class in **`java.lang`**, which is auto-imported. `out` is a `public static final PrintStream` field of that class. `println` is an instance method of **`PrintStream`**. `System.err` is a second `PrintStream`, meant for error messages.
- Java I/O is **stream-based**. A stream is a flow of data: an **input** stream flows *into* the program, and an **output** stream flows *out of* it. The abstract classes `InputStream` (`read`) and `OutputStream` (`write`) work on **bytes**. Concrete subclasses decide *where* the data comes from or goes to (file, memory, console, network).
- `System.in` is an `InputStream` connected to the keyboard by default. `System.in.read()` returns **one byte at a time** as an `int` (for example 65 for `'A'`), so reading a whole line means looping and casting, and every read may be an OS call.
- The `Reader` family works on **characters**. **`InputStreamReader`** is the bridge from bytes to chars. **`BufferedReader`** adds a buffer inside the program (fewer OS calls) and `readLine()`. Its drawback is that it reads only `String`s, so numbers need `Integer.parseInt`.
- **`Scanner`** (Java 1.5, in `java.util`) wraps all of this. It offers `next()`, `nextLine()`, `nextInt()`, `nextDouble()`, `nextBoolean()`, … and **tokenizes** the input on whitespace. It's convenient but **slower** than `BufferedReader` (it uses regex, tokenizing and parsing), so competitive programmers prefer `BufferedReader`.

## Key Concepts

**Types of I/O.** Data can come from or go to the console (keyboard and screen), files, the network (API calls, client and server), or memory buffers. This video covers console I/O. File I/O and network I/O come later.

**`System`, `out`, `err`, `in`.** `System` is a `final` class in `java.lang`. It has three `public static final` fields: `out` and `err` (both `PrintStream`) and `in` (an `InputStream`). They are static, so you never need `new System()`.

**`PrintStream`.** The class behind `System.out` and `System.err`. It provides `print` (no newline), `println` (adds a newline), and `printf` (formatted output). It is ultimately a subclass of `OutputStream`.

**Stream.** A flow of data, like water through a pipe. In Java's byte streams the data travels as **bytes**.

**`InputStream` / `OutputStream`.** Abstract byte-stream base classes. They declare `read()` and `write()` without saying where the data comes from. Subclasses include `FileInputStream`, `ByteArrayInputStream`, `BufferedInputStream`, `DataInputStream`, and on the output side `FileOutputStream`, `ByteArrayOutputStream`, `BufferedOutputStream`, `PrintStream`.

**`Reader`.** The abstract base class for **character** streams. Subclasses include `InputStreamReader` (bytes → chars), `BufferedReader` (buffering plus `readLine()`), and `FileReader`.

**Input buffer.** When you type and press Enter, the OS stores the bytes, followed by `\n`, in a buffer. `read()` takes one byte from it per call. `BufferedReader` pulls a big chunk into its own buffer in Java memory.

**`Scanner`.** A utility class (`java.util.Scanner`) that wraps an `InputStream`, `File` or `String`, splits the input into **tokens** (whitespace is the default delimiter), and converts them to the type you ask for.

## Detailed Notes

### Kinds of input/output [01:16]
- **Console**: keyboard in, screen out (`System.out.println`).
- **File**: read from or write to files (a separate video).
- **Network**: over the internet, e.g. a front end calling a back end's API (covered with Spring Boot).
- **Memory**: reading from in-memory buffers.

### What `System.out.println` really is [02:32]
- Work it out from the syntax:
  - `println(...)` has parentheses and takes an argument, so it's a **method**.
  - A method is called through an object, so `out` must be a **reference variable**. Its lowercase name fits that.
  - `System` starts with a capital letter, so it's a **class**. `System.out` is accessed through the class name, so `out` must be a **static** field.
- Looking inside the JDK in the IDE [08:14] (Cmd/Ctrl-click on `System`) shows `public static final PrintStream out = null;`. So the unknown type is **`PrintStream`**, and its `println(String)` is what gets called.
  - `public` makes it accessible everywhere, `static` lets you use the class name, and `final` stops you from reassigning it.
  - *Note:* the JVM fills in the real stream during startup, and you can still swap it out with `System.setOut(...)`.
- **No import needed** [10:46]: `System` lives in **`java.lang`**, which the compiler imports into every file automatically.
- **`PrintStream` methods** [12:40]:
  - `println`: print, then a newline.
  - `print`: no newline, so the next output continues on the same line.
  - `printf`: formatted output (the series used it earlier to print many decimal places).
- **Why make `out` static?** [13:56] Otherwise you'd have to write `System s = new System(); s.out.println(...)` every time. *(In practice you can't create a `System` at all, because its constructor is private.)*

### `System.err` [15:51]
- `System.err` is another `public static final PrintStream`, so `System.err.println("Bye")` also prints.
- Use `out` for normal output and `err` for errors. Example: `if (age < 0) System.err.println("Invalid age");`.
- Benefits:
  - It separates concerns in your code.
  - The OS treats it as a **separate stream** (stderr), so tools can colour it red, log it separately, or redirect it with `2>`.
- In real applications you'd use a logging library, but in plain Java this is useful.

### Why console input in Java looks complicated [19:01]
- Java was designed for **servers** (input arrives over the network as JSON or XML) and **GUIs** (forms), not for console programs. Early Java offered only low-level primitives.
- Modern Java has the friendlier `Scanner`, but it is built on those primitives. Understanding the primitives is how you understand `Scanner`.

### `System.in` and streams [20:55]
- Searching `System` for `in` turns up `public static final InputStream in`. Its type is **`InputStream`**, not `PrintStream`. It has a **`read()`** method.
- **Stream = flow of data** [22:49], like water in a pipe.
  - An **input stream** carries data **into** the program (taking input).
  - An **output stream** carries data **out of** the program (showing output).
- **`InputStream` and `OutputStream` are abstract classes** [24:43].
  - They only *declare* `read` and `write`, because they don't care whether the source is a file, the console, the network or memory.
  - Concrete subclasses implement them for each source. `PrintStream` is a subclass of `OutputStream` that knows how to write to the console.
  - Internally, `println` eventually calls `write`.
- **Java I/O is stream-based** [27:53]. Whatever the source, the data arrives as a **stream of bytes**.
- **Hierarchy** [29:48], simplified:

```
InputStream (abstract, read)          OutputStream (abstract, write)
├── FileInputStream                   ├── FileOutputStream
├── ByteArrayInputStream              ├── ByteArrayOutputStream
├── BufferedInputStream*              ├── BufferedOutputStream*
└── DataInputStream*                  └── PrintStream*
```
*In the JDK these marked classes actually extend `FilterInputStream`/`FilterOutputStream`, which sit in between.*

- `System.in` is declared as `InputStream`, but its actual object is a concrete subclass (in practice a `BufferedInputStream`). **By default it's attached to the keyboard** [33:36], and you can redirect it.

### Reading with `System.in.read()` [35:30]
- Typing `A` makes the keyboard deliver its **ASCII** code **65** (as binary) into the input stream, and `read()` reads it.
- `System.in.read()` declares `throws IOException`. Until exception handling is covered, add `throws IOException` to `main` (and `import java.io.IOException;`).
- `read()` returns an **`int`** [36:46], so `char c = System.in.read();` gives "Type mismatch: cannot convert from int to char". Typing `A` prints 65. Cast it to get the letter: `(char) System.in.read()`.
- **One byte per call** [38:40]. Typing `Aditya` prints only `A`. The rest (`d i t y a \n`) stays in the **input buffer**. Pressing Enter adds `\n`.
  - Escape sequences [39:56]: `\n` is a newline and `\t` is a tab.
- To read a whole line [41:12], loop on `read()` until `\n`, casting each value to `char` and appending it to a string.
  - That's clumsy compared with C++'s `cin` or C's `scanf`.
- Why one byte when a Java `char` is 2 bytes? [42:29] A Java `char` is 2 bytes because it holds **Unicode** (UTF-16). Plain ASCII characters typed on a keyboard fit in **one byte**.
  - *Caveat:* non-ASCII characters such as `é` are several bytes in UTF-8, so a byte-at-a-time cast breaks them. That's one more reason for `Reader`.

### `Reader`, `BufferedReader`, `InputStreamReader` [43:45]
- Java added the abstract **`Reader`** class (in `java.io`), which reads a **stream of characters** directly, so no casting is needed.
- Its subclasses relevant here are **`BufferedReader`** and **`InputStreamReader`** (console), plus **`FileReader`** (files, later).
- **Why buffering** [45:01]:
  - Without it, every character means a trip: keyboard → **OS buffer** → program. With thousands of characters, that's thousands of OS calls, which is slow.
  - `BufferedReader` keeps its **own buffer in program memory**. It pulls a whole chunk from the OS buffer in one go and then hands characters to the program from its own memory, the same way reading a variable doesn't need the OS. Performance improves a lot.
- **The compatibility problem** [48:49]:
  - `System.in` (an `InputStream`) is still the only thing actually connected to the keyboard, and it produces **bytes**.
  - `BufferedReader` works on **characters**, so the two don't fit together directly.
- **`InputStreamReader` is the bridge**. It converts a byte stream into a character stream, decoding bytes into chars using a charset.
  - *Correction:* at one point the video says it converts characters into bytes. It's the other way round: bytes → chars.
- **Code** [52:37]:
  - `import java.io.*;` (the `*` imports every class in the package) and `throws IOException`.
  - `InputStreamReader isr = new InputStreamReader(System.in);`. Its constructor takes an `InputStream`.
  - `BufferedReader br = new BufferedReader(isr);`. Its constructor takes any **`Reader`**.
  - `String name = br.readLine();` then prints "Aditya Tandon": the whole line, spaces included.
  - *Correction:* the video says `InputStreamReader` is a subtype of `BufferedReader`. Both are separate subclasses of `Reader`, which is why an `InputStreamReader` can be passed where a `Reader` is expected.
- **Full flow** [55:09]: keyboard "Aditya" → OS buffer as bytes (65 100 105 116 121 97 …) → `System.in` receives the bytes → `InputStreamReader` turns them into characters → `BufferedReader` buffers them in Java memory → `readLine()` builds the `String` → `println`.
- **Usual one-liner** [57:04]: `BufferedReader br = new BufferedReader(new InputStreamReader(System.in));`.

### Limitations of `BufferedReader` [58:20]
1. **It reads only strings.** For an integer you need two steps: `String s = br.readLine(); int i = Integer.parseInt(s);`.
2. **It's verbose.** You need an `InputStreamReader` around `System.in`, a `BufferedReader` around that, `readLine`, and then parsing.

### `Scanner` [60:14]
- Introduced in **Java 1.5**. It simplifies the code and provides typed methods, so no manual parsing is needed.
- It lives in **`java.util`** [60:52], not `java.io`, and it sits outside both the `InputStream` and `Reader` hierarchies. It **wraps** them. Internally it reads through a `Readable` source, such as an `InputStreamReader` around `System.in`.
- **Code** [61:30]:
  - `import java.util.Scanner;`. No `throws IOException` is needed, because `Scanner` doesn't throw it.
  - `Scanner sc = new Scanner(System.in);`. The constructor still takes an `InputStream`: everything ultimately goes through `System.in`.
  - `String name = sc.nextLine();`.
- **Other sources** [64:40]:
  - `new Scanner(System.in)` for the keyboard.
  - `new Scanner(new File("temp.txt"))` for a file. That constructor throws `FileNotFoundException`.
  - `new Scanner("10 20 30")` to read from a string.
- **Tokenization** [66:34]: "Hello I am Aditya" becomes 4 tokens, using whitespace as the **delimiter**. Which method you call decides how much gets consumed.
- **Methods** [67:12]:
  - `next()` reads **one token (word)**, so input "Aditya Tandon" gives "Aditya".
  - `nextLine()` reads **the rest of the line**.
  - `nextInt()`, `nextDouble()`, `nextBoolean()`, `nextLong()`, `nextFloat()`, `nextShort()`, `nextByte()`, `nextBigDecimal()`, … parse the token directly. Typing `28` gives 28, `34.56` gives 34.56, `false` gives false.

### `Scanner` vs `BufferedReader` [69:06]
- **`Scanner` is slower**. It tokenizes, uses **regular expressions** internally, and converts types.
- **`BufferedReader` is faster**. It just hands you the characters.
- Use `Scanner` for everyday convenience. Use `BufferedReader` when performance matters, for example in **competitive programming**, where milliseconds count.

## Code Examples

Each block is a complete program. Save it under the class's name and run it with `java ClassName.java`. To keep them runnable without typing, most examples read from an in-memory source. Swap in `System.in` to read from the keyboard.

### `System.out`, `System.err`, print vs println vs printf
```java
public class OutputDemo {
    public static void main(String[] args) {
        System.out.print("Aditya ");
        System.out.println("Tandon");               // Aditya Tandon
        System.out.printf("%.3f%n", Math.PI);       // 3.142
        System.out.printf("%-6s|%4d%n", "age", 28); // age   |  28

        int age = -3;
        if (age < 0) {
            System.err.println("Invalid age");      // goes to stderr (shown separately; try 2>/dev/null)
        } else {
            System.out.println("Valid age");
        }

        java.io.PrintStream out = System.out;       // System.out is just a PrintStream reference
        out.println(out.getClass().getSimpleName()); // PrintStream
    }
}
```

### `InputStream.read()`: one byte at a time
```java
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReadBytesDemo {
    public static void main(String[] args) throws IOException {
        // Stand-in for System.in: what the OS buffer would hold after typing "Aditya" + Enter
        InputStream in = new ByteArrayInputStream("Aditya\n".getBytes());

        int first = in.read();
        System.out.println(first);          // 65  (ASCII of 'A', returned as int)
        System.out.println((char) first);   // A

        // Read the rest of the line byte by byte
        StringBuilder sb = new StringBuilder().append((char) first);
        int c;
        while ((c = in.read()) != '\n' && c != -1) {   // -1 means end of stream
            sb.append((char) c);
        }
        System.out.println(sb);             // Aditya
    }
}
```

### `BufferedReader` + `InputStreamReader`
```java
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class BufferedReaderDemo {
    public static void main(String[] args) throws IOException {
        // In a real program: InputStream source = System.in;
        InputStream source = new ByteArrayInputStream("Aditya Tandon\n25\n".getBytes());

        BufferedReader br = new BufferedReader(new InputStreamReader(source)); // bytes -> chars -> buffered

        String name = br.readLine();               // whole line, spaces included
        System.out.println(name);                  // Aditya Tandon

        int age = Integer.parseInt(br.readLine()); // BufferedReader gives only Strings: parse manually
        System.out.println(age + 1);               // 26
    }
}
```

### `Scanner`: typed methods and tokenization
```java
import java.util.Locale;
import java.util.Scanner;

public class ScannerDemo {
    public static void main(String[] args) {
        // In a real program: new Scanner(System.in)
        Scanner sc = new Scanner("Aditya Tandon\n28 34.56 false\nHello I am Aditya\n");
        sc.useLocale(Locale.US);           // nextDouble() is locale-sensitive ("34,56" in some locales)

        String first = sc.next();          // one token
        String rest = sc.nextLine();       // rest of the current line
        System.out.println(first);         // Aditya
        System.out.println(rest.trim());   // Tandon

        int age = sc.nextInt();
        double d = sc.nextDouble();
        boolean b = sc.nextBoolean();
        System.out.println(age + " " + d + " " + b); // 28 34.56 false

        sc.nextLine();                      // consume the leftover '\n' after nextBoolean()
        int tokens = 0;
        Scanner line = new Scanner(sc.nextLine());
        while (line.hasNext()) { line.next(); tokens++; }
        System.out.println(tokens);         // 4
    }
}
```

### Reading real keyboard input (pipe it in to test)
```java
// Run interactively:   java EchoInput.java   (then type a name, Enter, a number, Enter)
// Or non-interactively: printf 'Aditya\n41\n' | java EchoInput.java
import java.util.Scanner;

public class EchoInput {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        String name = sc.hasNextLine() ? sc.nextLine() : "nobody";
        int n = sc.hasNextInt() ? sc.nextInt() : 0;
        System.out.println("Hello " + name + ", next number is " + (n + 1));
        // with the piped input above: Hello Aditya, next number is 42
    }
}
```

### Fast input pattern (competitive programming)
```java
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.StringTokenizer;

public class FastInput {
    public static void main(String[] args) throws IOException {
        // In a contest: new BufferedReader(new InputStreamReader(System.in))
        BufferedReader br = new BufferedReader(new StringReader("5\n3 1 4 1 5\n"));
        int n = Integer.parseInt(br.readLine().trim());
        StringTokenizer st = new StringTokenizer(br.readLine());
        long sum = 0;
        for (int i = 0; i < n; i++) sum += Integer.parseInt(st.nextToken());
        System.out.println(sum);   // 14
    }
}
```

## Common Pitfalls / Tips
- **`System.in.read()` returns an `int`**, partly so that it can return **-1 at end of stream**. Cast it to `char` only after checking for -1.
- **The `nextInt()` + `nextLine()` trap.** `nextInt()` leaves the `\n` in the buffer, so the next `nextLine()` returns `""`. Add an extra `sc.nextLine()` after reading numbers, or read lines and parse them yourself.
- **`next()` stops at whitespace.** Use `nextLine()` to read full names or sentences.
- **Bad input throws exceptions.** `nextInt()` on "abc" throws `InputMismatchException`, and `Integer.parseInt("abc")` throws `NumberFormatException`. Check with `hasNextInt()` first.
- **Create one `Scanner` for `System.in`** and reuse it. Closing it also closes `System.in` for the rest of the program.
- **`InputStreamReader` uses the platform charset by default.** Pass `StandardCharsets.UTF_8` explicitly when it matters (UTF-8 is the default since Java 18).
- **`System.out` is buffered and `System.err` isn't.** Their output can interleave in odd orders in the console.
- **Performance**: for large inputs, use `BufferedReader` with `StringTokenizer`, or `split`. Printing in loops is also slow. Build the output in a `StringBuilder`, or use a `PrintWriter`, and flush once.

## Review Questions
1. Explain each part of `System.out.println("Hi")`: which is a class, which a field, which a method, and of what types?
2. Why don't you need to import `System`?
3. What's the difference between `System.out` and `System.err`, and why would you use `err`?
4. What is a stream? What do `InputStream` and `OutputStream` do, and why are they abstract?
5. Why does `(char) System.in.read()` print only `A` when you type `Aditya`?
6. What problem does `BufferedReader` solve, and why do you need `InputStreamReader` between it and `System.in`?
7. What are `BufferedReader`'s two limitations, and how does `Scanner` address them?
8. When would you choose `BufferedReader` over `Scanner`, and why is `Scanner` slower?

<details>
<summary>Answers</summary>

1. `System` is a class (in `java.lang`). `out` is a `public static final` field of type `PrintStream`. `println` is an instance method of `PrintStream` that prints its argument followed by a newline.
2. `java.lang` is imported automatically into every Java source file, and `System` is in it.
3. Both are `PrintStream`s. `out` is standard output and `err` is standard error. Using `err` for errors separates concerns in code, and it lets the OS and tools handle errors differently (separate redirection, red colouring, separate log files).
4. A stream is a flow of data. `InputStream` brings bytes into the program (`read`), and `OutputStream` sends bytes out (`write`). They're abstract because they define *what* can be done while subclasses decide *where* the data comes from or goes to (file, memory, console, network).
5. `read()` returns only one byte per call. The remaining bytes (`d i t y a \n`) stay in the input buffer until more `read()` calls consume them.
6. Reading one byte at a time from the OS is slow (one call per byte). `BufferedReader` pulls a large chunk into its own in-memory buffer and adds `readLine()`. It works on characters while `System.in` produces bytes, so `InputStreamReader` decodes bytes into characters between them.
7. It only returns `String`s, so numbers need `Integer.parseInt` and similar, and the setup is verbose. `Scanner` wraps the source in one constructor and provides typed methods (`nextInt`, `nextDouble`, `nextBoolean`, …) on top of whitespace tokenization.
8. Choose `BufferedReader` when speed matters: large inputs, competitive programming. `Scanner` does extra work for every token (regex-based delimiter matching, tokenizing, parsing and type conversion), while `BufferedReader` just returns raw characters and lines.
</details>

## Further Study
- File I/O: `FileInputStream`/`FileOutputStream`, `FileReader`/`FileWriter`, `java.nio.file.Files`
- Exception handling and `try-with-resources` (why `read()` throws `IOException`)
- Character encodings: ASCII vs Unicode vs UTF-8/UTF-16; `StandardCharsets`
- `PrintWriter`, `BufferedWriter` and fast output techniques
- `Console` class (`System.console()`) for password input
- Regular expressions (what `Scanner` uses for delimiters)
- The Decorator pattern (how `BufferedReader(new InputStreamReader(System.in))` composes streams)
- NIO channels and buffers; network I/O with sockets and HTTP clients

---

[← #19](19-java-nested-classes.md) · [Index](README.md) · [#21 →](21-immutable-classes-in-java.md)

---

[← #19](19-java-nested-classes.md) · [Index](README.md) · [#21 →](21-immutable-classes-in-java.md)
