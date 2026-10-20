# Write Your First JAVA Program | JVM, JDK & JRE Explained | JSE vs JEE vs JME | JAVA Full Course #2
> Source: https://youtu.be/pdS8_smlsXA · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 41:33. The video is in Hindi/Hinglish and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within a minute or two.

## TL;DR
- **JVM ⊂ JRE ⊂ JDK.** The JVM runs bytecode. The JRE is the JVM plus the class libraries a program needs. The JDK is the JRE plus development tools (the `javac` compiler, a debugger, javadoc, and more). Developers install the JDK.
- A **compiler** translates the whole program at once. An **interpreter** translates and runs it line by line. Java uses both: `javac` compiles source to bytecode, then the JVM runs that bytecode with an **interpreter plus a JIT (Just-In-Time) compiler** that turns frequently used ("hot") code into native machine code.
- The JVM's three jobs: run bytecode as machine code, provide security (the sandbox), and manage memory through **garbage collection**.
- **Java SE** is core Java. **Java EE** (now **Jakarta EE**) adds enterprise and web APIs. **Java ME** was a lightweight edition for old mobile phones and is now obsolete, replaced by Android.
- First program: write `Demo.java`, run `javac Demo.java` to get `Demo.class`, then run `java Demo`.

## Key Concepts

**Compiler vs interpreter.** Both translate code into another form, usually machine code. A compiler converts the **whole program at once** and runs the result afterwards. An interpreter converts **one line (instruction) at a time** and runs it immediately before moving on. *Why it matters:* it explains why Java is described as both compiled and interpreted.

**Compiled vs interpreted languages.** C/C++ are compiled straight to machine code. Python is usually called interpreted. Java is **both**: it's compiled to bytecode, and the bytecode is then interpreted or JIT-compiled.

**JIT (Just-In-Time) compiler.** Part of the JVM. While the program runs, it detects code that executes often and compiles it to optimized native code, so the hot paths stop being interpreted. *Why it matters:* it's the main reason Java's speed is close to C++ today.

**JVM (Java Virtual Machine).** A virtual computer, i.e. an environment, inside which bytecode runs. It translates bytecode to machine code (interpreter + JIT), enforces security, and does garbage collection.

**JRE (Java Runtime Environment).** The JVM plus the **class libraries**: the built-in code that printing, file I/O, collections and so on depend on. *Why it matters:* the JVM alone can't run a real program, because nearly every program calls library code.

**JDK (Java Development Kit).** The JRE plus the tools you need to *develop*: `javac`, a debugger (`jdb`), `javadoc`, and more. *Why it matters:* it's what you install to write and run Java.

**Java editions.**
- **SE (Standard Edition)**: core Java, i.e. the language and the standard libraries.
- **EE (Enterprise Edition)**, renamed **Jakarta EE**: APIs for web and enterprise applications, such as servlets and transactions.
- **ME (Micro Edition)**: a lightweight edition for old feature phones and embedded devices. It's obsolete now.

## Detailed Notes

### Recap: source → bytecode → machine code [00:00]
- You write `Hello.java`. A **compiler** turns it into **bytecode**. The bytecode is platform independent, and every platform runs it through its own **JVM**, which produces that platform's machine code.
- The rest of the video answers three questions: how does the JVM run bytecode, where does the compiler live, and what are the JRE and JDK?

### The hierarchy: JVM ⊂ JRE ⊂ JDK [05:00]
Draw them as concentric circles, with the JVM innermost, the JRE around it, and the JDK outermost.
```
┌──────────────── JDK (Java Development Kit) ────────────────┐
│  javac (compiler), jdb (debugger), javadoc, jar, ...       │
│  ┌──────────── JRE (Java Runtime Environment) ──────────┐  │
│  │  Class libraries (java.lang, java.util, java.io ...)  │  │
│  │  ┌─────────── JVM (Java Virtual Machine) ─────────┐  │  │
│  │  │  Interpreter + JIT compiler, GC, security      │  │  │
│  │  └────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────┘  │
└────────────────────────────────────────────────────────────┘
```

### Compiler vs interpreter [08:19]
- Both take code and translate it into another form. A compiler isn't required to output machine code. `javac` outputs bytecode, for example.
- **Compiler**: reads the whole source, generates all the machine code, and only then does the CPU run it and produce output.
- **Interpreter**: converts one line, hands it to the CPU, gets the output, then converts the next line, and so on.
- Neither is strictly better. Each has trade-offs.

### Is Java compiled or interpreted? Both. [13:43]
- Step 1 is **compiled**: `javac` turns source into bytecode.
- Step 2 is **interpreted**: originally the JVM used an interpreter to convert bytecode to machine code line by line, running each line as it went.

### Why the early JVM used an interpreter [15:47]
- Java already does two conversion steps (source → bytecode → machine code). That's the price of portability.
- In the 1990s hardware was slow, RAM was limited and disks were slow. Compiling twice would have made programs slow to *start*. An interpreter starts executing immediately.

### The modern JVM: interpreter + JIT [17:52]
- As hardware improved, a **JIT compiler** was added to the JVM. It works alongside the interpreter rather than replacing it.
- The hybrid model works like this: the JVM spots code that is used **frequently** and JIT-compiles it straight to native code. Less frequent code keeps running through the interpreter.
- C++ may still win some speed benchmarks because it skips the bytecode step. With modern hardware the gap is negligible for most applications, and Java buys portability, simplicity and security in exchange.

### JVM responsibilities [21:36]
1. **Convert bytecode to machine code**, using the interpreter plus the JIT compiler.
2. **Security through the sandbox model.** Bytecode downloaded from anywhere doesn't run directly on your OS. It runs inside the JVM, which watches for requests for access the code shouldn't have (deleting files, planting a virus or a trojan) and restricts them.
3. **Garbage collection**, explained in a later lecture.

### JRE = JVM + class libraries [24:06]
- With only a JVM you could run bytecode, but you couldn't produce any, and most programs still wouldn't work.
- Your code calls built-in functionality all the time: printing to the console, reading files, and so on. In C++ that comes from `#include <iostream>` (`cout`), in C from `printf`, and in Java from **class libraries** brought in with `import` statements.
- You write the business logic, and the language's libraries handle the internals.
- So you need **at least a JRE** to run a Java program. Standalone JREs used to be downloadable, and the video says that today you just install the JDK.

### JDK = JRE + development tools [27:25]
- The JDK contains everything needed to **write and run** Java: the JRE, the **compiler** (`javac`), a **debugger** (which pauses a program at chosen lines so you can inspect values step by step), **javadoc** (for generating documentation), and more.
- Any editor works: VS Code, IntelliJ, even Notepad for tiny programs. A real IDE is better for large projects.
- **Homework** [29:55]: install the latest stable JDK yourself, from Oracle's site, Homebrew or another vendor. You may need to set `PATH`/`JAVA_HOME`. Work out the steps yourself before asking ChatGPT, Claude or Google.

### JSE vs JEE vs JME [31:10]
- **JSE (Java Standard Edition)** is **core Java**: classes, methods, OOP, the whole language. The series covers this first.
- **JEE (Java Enterprise Edition)**: once Java got popular for building websites and web apps, extra libraries and APIs were added (transactions and others). It's now called **Jakarta EE**. It comes up again when the series reaches Spring Boot.
- **JME (Java Micro Edition)**: a lightweight edition used for apps on early mobile phones. It's obsolete because **Android** replaced it, and Android apps are written in Java or Kotlin (a JVM language). You don't need to study it.

### Plan for the first program [34:54]
1. Create `Demo.java` in an editor or IDE (VS Code in the video).
2. Compile it: `javac Demo.java` produces `Demo.class` (bytecode).
3. Run it: `java Demo`. The JRE's JVM loads the class, the interpreter and JIT turn it into machine code, the CPU executes it, and the output appears.

### Live demo in VS Code [36:59]
- In an empty folder called `java-first-program`, create `Demo.java`. VS Code generates some **boilerplate** (template code). Ignore `class`, `public static void main(String[] args)` and so on for now, since they're explained later.
- Add `System.out.println("Hello World");` and save.
- Open the integrated terminal and run `javac Demo.java` [38:38]. `Demo.class` appears in the folder.
- Opening `Demo.class` in VS Code shows source code again. That's because VS Code **decompiles** `.class` files (the banner mentions the *FernFlower* decompiler). A real `.class` file is unreadable binary bytecode, neither English nor machine code.
- Run `java Demo` and "Hello World" prints.
- The instructor has created a GitHub repository for the playlist with practice questions and asks you to solve them without immediately reaching for ChatGPT or Google.

## Code Examples

### The first program
Save it as `Demo.java`.
```java
public class Demo {
    public static void main(String[] args) {
        System.out.println("Hello World");
    }
}
// Output:
// Hello World
```

Two ways to run it:
```bash
# Classic: compile to bytecode, then run on the JVM
javac Demo.java      # creates Demo.class
java Demo            # note: class name, no .class extension

# Shortcut (Java 11+): compile in memory and run a single source file
java Demo.java
```

Look inside the bytecode (a JDK tool, not a decompiler):
```bash
javap -c Demo        # disassembles Demo.class into JVM instructions
```

### Using the class libraries (what the JRE adds)
`String`, `Math` and `LocalDate` all come from the JDK's class libraries, not from your code.
```java
import java.time.LocalDate;

public class LibraryDemo {
    public static void main(String[] args) {
        System.out.println(Math.max(7, 42));                 // java.lang.Math
        System.out.println("java".toUpperCase());            // java.lang.String
        System.out.println(LocalDate.of(1995, 5, 23).getYear()); // java.time
        System.out.println("Running on Java " + Runtime.version().feature());
    }
}
// Output (last line depends on your JDK):
// 42
// JAVA
// 1995
// Running on Java 21
```

## Common Pitfalls / Tips
- **`java Demo`, not `java Demo.class`.** The launcher takes a class name. `java Demo.java` (with `.java`) is the separate single-file source mode added in Java 11.
- **The file name must match the public class.** `public class Demo` has to live in `Demo.java`, or `javac` reports an error.
- **`javac: command not found`** means the JDK's `bin` directory isn't on your `PATH` (or you only have a JRE). Check with `java -version` and `javac -version`.
- **JIT isn't a separate step you run.** It happens inside the JVM while your program executes. The JVM *profiles* the running code to find hot methods and loops. It doesn't decide by reading the bytecode in advance.
- **"Interpreter converts line by line"** is a simplification. The JVM interprets *bytecode instructions*, not source lines.
- **JRE today:** Oracle stopped shipping a separate JRE after Java 8. Since Java 11 you install the JDK, or build a trimmed runtime with `jlink`. Some vendors (e.g. Eclipse Temurin) still publish JRE builds.
- **Security/sandbox:** the applet-era sandbox and the `SecurityManager` are gone (deprecated in 17, disabled in 24). The JVM's security today comes mainly from bytecode verification and memory safety.
- **Java EE → Jakarta EE** also changed package names from `javax.*` to `jakarta.*` (Jakarta EE 9+). You'll see both in tutorials.

## Review Questions
1. Draw or describe the relationship between the JVM, JRE and JDK. Which one do you install to develop?
2. How does a compiler differ from an interpreter?
3. Why is Java called both compiled and interpreted?
4. Why did early JVMs rely on an interpreter instead of compiling bytecode to native code?
5. What does the JIT compiler do, and why does the JVM keep the interpreter as well?
6. List three responsibilities of the JVM.
7. Why can't a bare JVM run a typical "Hello World"? What does the JRE add?
8. What are Java SE, EE and ME? Which one is obsolete, and what replaced it?

<details>
<summary>Answers</summary>

1. JVM ⊂ JRE ⊂ JDK. The JRE contains the JVM plus class libraries. The JDK contains the JRE plus tools such as `javac` and the debugger. Developers install the JDK.
2. A compiler translates the whole program at once and runs it afterwards. An interpreter translates and executes one line or instruction at a time.
3. `javac` *compiles* source into bytecode, and the JVM then *interprets* that bytecode (and JIT-compiles the hot parts) into machine code.
4. Hardware in the 1990s was slow, with limited RAM and slow disks. Java already had a compile step to bytecode, and compiling a second time would have delayed startup. An interpreter starts running immediately.
5. It finds frequently executed ("hot") code at runtime and compiles it to optimized native code, so that code no longer runs line by line. The interpreter stays because compiling everything up front is slow, and rarely run code isn't worth compiling.
6. Translating and executing bytecode (interpreter + JIT), security (the sandbox / restricted environment), and garbage collection.
7. Printing, string handling and so on are implemented in the standard **class libraries**. The JRE bundles those libraries with the JVM, so the bytecode's calls into them can be resolved.
8. SE is core Java. EE (now Jakarta EE) adds enterprise and web APIs. ME was a lightweight edition for early mobile phones and embedded devices. ME is obsolete and has been replaced by Android.
</details>

## Further Study
- JVM architecture: class loader, bytecode verifier, runtime data areas (heap, stack, method area), execution engine
- HotSpot JIT tiers (C1/C2), warm-up, and GraalVM / AOT native images
- Garbage collection basics (G1, ZGC)
- `javap`, `jshell`, `jlink`, `jdb`
- `PATH` vs `JAVA_HOME`; managing several JDKs (SDKMAN!, Homebrew)
- What `public static void main(String[] args)` means (next lectures)
- Jakarta EE vs Spring Boot

---

[← #01](01-introduction-to-java.md) · [Index](../../ReadMe.md) · [#03 →](03-variables-data-types-literals-keywords.md)
