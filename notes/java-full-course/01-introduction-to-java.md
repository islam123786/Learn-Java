# JAVA Full Course | Introduction to JAVA | Java Full Course for Beginners in 2026
> Source: https://youtu.be/LBqE4YOvhyc · Channel: Coder Army · Notes created: 2026-10-20
>
> Length: 59:06. The video is in Hindi/Hinglish and the transcript had no timing data, so the timestamps below are estimated from where each part falls in the transcript. They should be within about a minute.

## TL;DR
- The series promises Java "in depth" (internals, first-principles reasoning) rather than just syntax, and expects you to code along and practice actively.
- Java was created to fix three weaknesses of C/C++: **portability**, **simplicity** and **security**.
- C/C++ compile straight to **machine code**, and machine code is tied to a **platform** (processor + operating system). Running on a new platform means recompiling.
- Java compiles once to platform-neutral **bytecode** (`.class` files). A platform-specific **JVM** on each machine translates that bytecode into native instructions. This is **WORA: Write Once, Run Anywhere**.
- The JVM also gave Java its security story: it runs code inside a restricted **sandbox**. Simplicity came from dropping pointers, multiple inheritance of classes, and manual memory deallocation.

## Key Concepts

**Compiler.** A program that translates source code into another form, usually machine code (0s and 1s) that the CPU can execute. *Why it matters:* the target of compilation decides whether the output is portable.

**Machine code.** Binary instructions that one specific kind of processor understands. *Why it matters:* it's the reason a C++ binary built for one machine won't run on another.

**Platform.** The combination of a **processor** (e.g. Intel x86, ARM) and an **operating system** (e.g. Windows, macOS). Example: x86 + Windows is one platform, ARM + macOS is another. *Why it matters:* native binaries are platform-specific.

**Why binaries differ across platforms.**
- *OS differences:* printing, file I/O and memory allocation all go through OS **system libraries**, and each OS exposes different routines. The compiled program contains calls into those OS-specific routines.
- *Processor differences:* each CPU family has its own **ISA (Instruction Set Architecture)**, the "grammar" of basic operations like add, load, store and jump. x86 and ARM speak different ISAs.

**Platform dependent vs platform independent.** C/C++ are platform dependent: you compile separately for every target. Java is platform independent at the **bytecode** level. *Why it matters:* one build artifact can be deployed to many servers and devices.

**Bytecode.** An intermediate, platform-neutral instruction format produced by the Java compiler (`javac`). `Hello.java` compiles to `Hello.class`. It's not machine code. *Why it matters:* it's what makes "compile once" possible.

**JVM (Java Virtual Machine).** Software that reads bytecode and executes it as native instructions for the machine it runs on. *Why it matters:* the JVM itself is **platform-specific** (there's a separate JVM build for each platform), and that's exactly what lets the bytecode be platform-independent.

**WORA.** "Write Once, Run Anywhere": compile once, then run the same bytecode on any platform that has a JVM.

**Sandbox model.** The JVM runs untrusted code in a restricted environment so it can't freely access files, the network or system resources. *Why it matters:* it was essential for applets, which ran code downloaded from the internet.

## Detailed Notes

### Two warnings about the series [00:00]
- The series is about understanding Java's internals and the "why" behind each feature, not memorizing syntax. The instructor argues that in the AI era you can always look up or generate syntax, but strong fundamentals are what pay off when you build Spring Boot or Android apps later.
- You can't learn passively. Watch one video a day, implement what was covered, and solve the practice questions.
- The course is meant to be beginner-friendly and to go from absolute basics to advanced topics.

### The world before Java: C and C++ [02:51]
- In the 1980s and 1990s C and its successor C++ dominated [03:29]. They were fast, simpler than older languages, used English-like syntax, and were **low-level** (close to the hardware, with few abstraction layers).
- Before C came assembly (still hardware-oriented, with a few English mnemonics like MOV), and before that raw machine code. Languages evolved so that programmers could write something close to English and let a compiler produce the 0s and 1s.
- So why did anyone need a new language? Three problems: portability, simplicity and security.

### Problem 1: Portability [06:03]
- You write `hello.cpp` and compile it. The compiler produces machine code for one platform, P1. Copy that binary to platform P2 and it won't run. You have to compile again for P2, and again for every new platform.
- That makes C/C++ **platform dependent**.

### What a platform is [10:43]
- Platform = processor + operating system. Example: P1 = Intel x86 + Windows, P2 = ARM + macOS.
- The same source code produces different binaries on P1 and P2. The next two sections explain why.

### Why the OS changes the binary [15:38]
- Printing to the console, reading or writing files, and allocating memory for a variable are all things your program can't do alone. It asks the OS to do them.
- When you compile, the program is linked against the OS's **system libraries**. Windows and macOS provide different routines for the same task (the video's made-up names are `writeToConsole` and `writeThere`), so the machine code that comes out is different.

### Why the processor changes the binary [21:06]
- A CPU is a physical chip with billions of transistors. Each transistor is either on (1) or off (0).
- Intel and ARM lay out their chips differently, so talking to them at the hardware level ("add two numbers") requires different bit patterns.
- **ISA (Instruction Set Architecture)** [24:25] is the interface between programs and the processor. It defines how basic operations (add, load, store, jump) are encoded. Think of it as the processor's **grammar**. Different processors have different ISAs, so the same source code compiles to different machine code.
- Conclusion: OS differences plus ISA differences mean that every pre-Java language that compiled to native code was platform dependent.

### Problems 2 and 3: Simplicity and security [27:14]
- C++ was simple compared with what came before it, but it still had complex parts. Java wanted to remove or hide them.
- C++ was also less secure, and Java aimed to be more secure.
- The three features to remember from this video are **portable, simple and secure** [28:27].

### How Java solved portability: bytecode [29:48]
- **Analogy** [30:14]: you (who speak Hindi/English) visit China and then Spain. The C/C++ approach is "learn Mandarin, then learn Spanish", which means compiling separately for each platform. The Java approach is "bring a translator friend who speaks every language". You speak one language, and the translator handles the local one.
- In Java:
  - `javac` compiles `Hello.java` into **bytecode**, `Hello.class`. That's an intermediate code, not 0s and 1s.
  - The same `Hello.class` goes to P1 and to P2. On each platform the **JVM** [33:41] (the translator) reads the bytecode and turns it into that platform's machine instructions.
  - You compile once, and the bytecode runs on P1, P2, or a future P3. The only condition is that the platform has a JVM.

### Correction: there is a JVM per platform [38:15]
- The analogy suggests one translator travelling with you. In reality the **JVM itself is not portable**. There's a JVM built for x86 + Windows, another for ARM + macOS, and so on.
- The JVM has to know the platform's OS system libraries and its processor's ISA. So the JVM is platform-specific, and only the bytecode is independent.
- Java's trade-off: write a JVM for each platform once, and from then on every program is portable. A better version of the analogy is a local translator stationed in each country.
- This is **platform independence** or **WORA, Write Once, Run Anywhere** [41:07].

### Why portability mattered so much [41:29]
- Lots of new devices were appearing around 1990: embedded systems, set-top boxes, TVs (and later smart devices). Java's creators wanted one application to run on any Java-enabled device.
- Raw speed mattered less than before because hardware was getting fast. They wanted a **general-purpose** language. (The video notes that even slower languages like Python run fine on modern hardware.)
- The **internet** [43:49] was taking off. A backend may be deployed on many servers with different platforms, and without portability you'd recompile for each one.

### How Java became simpler [46:02]
Java removed three sources of complexity in C/C++:
- **Pointers**
- **Multiple inheritance** of classes
- **Manual memory deallocation**, which Java handles automatically (garbage collection, covered later)

### How Java became secure [47:07]
- Java spread very quickly. On the backend it was used through **servlets** [48:08], Java programs that act like APIs (fetching data, writing to a database).
- Java also arrived before JavaScript. **Fun fact** [48:38]: JavaScript was named after Java to ride on its popularity. The two languages are otherwise unrelated.
- On the frontend there were **applets** [50:10]: lightweight Java code sent over the internet that ran inside the user's browser (simple forms, a loan calculator, and so on).
- The risk was that a malicious applet could ask for dangerous permissions, crash your system or steal data.
- The answer was again the JVM. Bytecode doesn't run directly on the OS. It runs inside the JVM, which can refuse unnecessary permissions. This restricted "safe mode" is the **sandbox model** [53:16].
- So the JVM solves both portability and security. Simplicity comes from the language design itself.
- Applets have since been dropped from the JDK. The instructor isn't sure of the exact version and guesses JDK 10 or 11.

### Why didn't C++ just add a VM? [55:46]
- In theory you could give C++ an intermediate code plus a "CVM" and make it portable. Microsoft explored this idea with **C#** [56:30], a C-family language that runs on a virtual machine and became Java's main alternative, though it never got as popular as Java.
- C++ itself was never meant to be platform independent. Its purpose is to be fast and close to the hardware, so nobody wanted to change it that much.
- Languages that came after Java, such as C# and Python, carry the same VM/bytecode idea forward in their own ways.
- Next lecture: JVM/JRE/JDK internals and the first Java program.

## Code Examples

The video doesn't write any code yet. This small program shows the WORA idea in practice: the same `.class` file prints the platform it happens to be running on. Save it as `WhereAmI.java` and run it with `java WhereAmI.java`.

```java
public class WhereAmI {
    public static void main(String[] args) {
        System.out.println("Hello from the same bytecode!");
        // The JVM tells us which platform it is translating for:
        System.out.println("OS:   " + System.getProperty("os.name"));
        System.out.println("CPU:  " + System.getProperty("os.arch"));
        System.out.println("JVM:  " + System.getProperty("java.vm.name"));
    }
}
// Output (the last three lines vary by machine):
// Hello from the same bytecode!
// OS:   Mac OS X        (e.g. "Windows 11" or "Linux" elsewhere)
// CPU:  aarch64         (e.g. "amd64" on Intel/AMD)
// JVM:  OpenJDK 64-Bit Server VM
```

The classic two-step flow (covered properly in the next video):
```bash
javac WhereAmI.java   # source -> bytecode (WhereAmI.class), once
java WhereAmI         # the local JVM runs the bytecode on this platform
```

## Common Pitfalls / Tips
- **"Java is platform independent" applies to the bytecode, not to the JVM.** The JVM, and the JDK you download, are platform-specific. You pick the build for your OS and CPU.
- **Bytecode isn't machine code.** A `.class` file can't be executed directly by the CPU. It always needs a JVM.
- **Java isn't just "interpreted".** Modern JVMs interpret bytecode *and* JIT-compile hot code to native code, which is why Java is much faster than early versions (next video).
- **"No pointers" means no raw pointers or pointer arithmetic.** Java still has **references**, and a `NullPointerException` is exactly what happens when you use a null reference.
- **"No multiple inheritance" means of classes.** A Java class can still implement multiple **interfaces**.
- **The sandbox story is historical.** The applet/browser sandbox is gone, and the `SecurityManager` that enforced it was deprecated in Java 17 and permanently disabled in JDK 24. Java code today is secure mainly through bytecode verification, memory safety (no pointer arithmetic, bounds-checked arrays) and the JVM's managed runtime, not a sandbox.
- **Applets are dead.** Browsers dropped the Java plugin, Oracle removed the plugin and Java Web Start from JDK 11, and the Applet API itself was deprecated in Java 9 and later removed. Don't spend time learning them.
- **"Half of the internet runs on Java"** is a slogan, not a measured statistic. Java *is* very widely used on servers, in enterprise systems and on Android.

## Review Questions
1. What two components make up a "platform"? Give two examples.
2. Why does the same C++ source produce different machine code on Windows/x86 and macOS/ARM? Give both reasons.
3. What is an ISA, and how does it relate to machine code?
4. What does `javac` produce, and why is that output portable?
5. Is the JVM platform independent? Explain the trade-off Java made.
6. What does WORA stand for, and why was it so valuable in the 1990s?
7. Which three C/C++ features did Java remove to become simpler?
8. What is the sandbox model, and why was it needed for applets?

<details>
<summary>Answers</summary>

1. A processor and an operating system. Examples: Intel x86 + Windows, ARM + macOS.
2. (a) The OS: printing, file I/O and memory allocation go through OS system libraries, which differ between Windows and macOS. (b) The processor: x86 and ARM have different instruction set architectures, so the same operation is encoded differently.
3. The Instruction Set Architecture is the processor's "grammar". It defines the basic instructions (add, load, store, jump) and how they're encoded in binary. Machine code is a program written in a specific ISA.
4. Bytecode in a `.class` file. It's an intermediate format that doesn't depend on any OS or CPU. Any platform with a JVM can run it.
5. No. Each platform needs its own JVM, which understands that platform's OS libraries and ISA. Java accepted the cost of writing and maintaining a JVM per platform so that every Java program would become portable.
6. Write Once, Run Anywhere. Many new devices (set-top boxes, TVs, embedded systems) and the growing internet (backends on servers with different platforms) meant that recompiling for every target was a real burden.
7. Pointers, multiple inheritance (of classes), and manual memory deallocation.
8. The JVM runs bytecode inside a restricted environment and refuses permissions the code shouldn't have. Applets were downloaded from the internet and executed in the user's browser, so without restrictions malicious code could damage the system or steal data.
</details>

## Further Study
- How `javac` and the JVM work together: class loading, bytecode verification, interpreter + JIT (next video)
- JDK vs JRE vs JVM; Java SE / EE (Jakarta EE) / ME
- Garbage collection: what replaced manual `free`/`delete`
- References vs pointers in Java; why `NullPointerException` exists
- Interfaces as Java's alternative to multiple inheritance
- Compiled vs interpreted languages; how C#/.NET and Python also use VMs and bytecode
- History of Java: Sun Microsystems, James Gosling, Project Green/Oak (1991-1995)
- `javap -c` to look at real bytecode

---

← (start) · [Index](README.md) · [#02 →](02-first-java-program-jvm-jdk-jre.md)
