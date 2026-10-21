# 02 · First Java Program · JVM, JDK & JRE
> Full notes: [02-first-java-program-jvm-jdk-jre.md](../02-first-java-program-jvm-jdk-jre.md) · [▶ Video](https://youtu.be/pdS8_smlsXA)

## From source code to machine code

```
 .java file   ── compiler ──►   .class file   ── interpreter ──►   machine code
(source code)   (entire file)    (bytecode)                          (0s & 1s)
```

- The `.class` file (bytecode) **won't run directly** on the system. It needs a **JVM**.
- This is why Java is **platform independent**: a `.class` file made on any system (Windows or Mac) can be used on any other system.
- The `.class` file is platform independent, but the **JVM** (which turns it into machine code) is **platform dependent**.

## JDK vs JRE vs JVM vs JIT

```
┌───────────────────────────────────────────┐
│ JDK = JRE + Dev Tools                     │
│  ┌─────────────────────────────────────┐  │
│  │ JRE = JVM + Library Classes         │  │
│  │  ┌───────────────────────────────┐  │  │
│  │  │ JVM (Java Virtual Machine)    │  │  │
│  │  │  ┌─────────────────────────┐  │  │  │
│  │  │  │ JIT Compiler            │  │  │  │
│  │  │  │ (Just In Time)          │  │  │  │
│  │  │  └─────────────────────────┘  │  │  │
│  │  └───────────────────────────────┘  │  │
│  └─────────────────────────────────────┘  │
└───────────────────────────────────────────┘
```

**JDK** gives you what you need to **develop and run** a Java program. It's a package that includes:
1. Dev tools, to develop your program
2. JRE, to run the program
3. Compiler (`javac`)
4. Archiver (`jar`)
5. Doc generator (`javadoc`)
6. Interpreter/loader (`java`)

**JRE** gives you what you need to **only run** a program. It includes:
1. JVM
2. UI toolkit
3. Deployment technologies
4. Integration libraries
5. Base libraries

```
Java source code ──► JDK ──► Bytecode ──► JVM (runs inside the JRE)
```

## What happens at runtime

After we get the `.class` file:
1. The **class loader** loads all the classes needed to run the program.
2. The JVM sends the code to the **bytecode verifier**, which checks the code format.

```
      Compile time                      Runtime
┌──────────────┐               ┌─────────────────────┐
│  .java file  │               │    Class Loader     │
└──────┬───────┘               └──────────┬──────────┘
       │ javac compiler                   ▼
       ▼                       ┌─────────────────────┐
┌──────────────┐               │ Bytecode Verifier   │
│ .class file  │  (bytecode)   └──────────┬──────────┘
└──────────────┘                          ▼
                               ┌─────────────────────┐
                               │    Interpreter      │
                               └──────────┬──────────┘
                                          ▼
                               ┌─────────────────────┐
                               │      Runtime        │
                               └──────────┬──────────┘
                                          ▼
                               ┌─────────────────────┐
                               │      Hardware       │
                               └─────────────────────┘
```

### How the class loader works
- **Loading:** reads the `.class` file and generates binary data. An object for the class is created in the heap.
- **Linking:** the JVM verifies the `.class` file, allocates memory for class (static) variables, and replaces symbolic references with direct ones.
- **Initialization:** all static variables get their assigned values.

The JVM contains **stack** and **heap** memory.

### How the JVM executes code
**Interpreter**
1. Runs the code line by line.
2. If a method is called many times, it's interpreted again every time.

**JIT (Just In Time) compiler**
1. For methods that are called repeatedly, JIT produces machine code directly, so they don't need to be interpreted again.
2. This makes the program run faster.

The **garbage collector** also runs inside the JVM and frees memory that's no longer used.

## Java editions
- **SE (Standard Edition)**: core Java, i.e. the language and the standard libraries.
- **EE (Enterprise Edition)**, now called **Jakarta EE**: APIs for web and enterprise apps, such as servlets and transactions.
- **ME (Micro Edition)**: a lightweight edition for old feature phones and embedded devices. It's obsolete now.

**Remember:** JDK ⊃ JRE ⊃ JVM ⊃ JIT. `javac` compiles once to bytecode, then the JVM loads it, verifies it, and runs it (interpreter + JIT).
