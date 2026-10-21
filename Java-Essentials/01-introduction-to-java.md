# 01 · Introduction to Java
> Full notes: [01-introduction-to-java.md](../01-introduction-to-java.md) · [▶ Video](https://youtu.be/LBqE4YOvhyc)

Java was built to fix three problems with C/C++.

| Problem in C/C++ | How Java solves it |
| :--- | :--- |
| **Portability:** code compiles straight to machine code for one platform (CPU + OS). A new platform means compiling again. | `javac` compiles once to platform-neutral **bytecode** (`.class`). Each platform has its own **JVM** that runs it. This is **WORA: Write Once, Run Anywhere**. |
| **Simplicity:** pointers, multiple inheritance and manual memory management make it complex. | Java removed **pointers**, **multiple inheritance of classes** and **manual deallocation** (garbage collection does it instead). |
| **Security:** native code runs directly on the OS, so harmful code can damage the system or steal data. | Bytecode runs **inside the JVM**, which can block dangerous actions (the **sandbox model**). |

**Remember:** the bytecode is platform independent, but the JVM is not. There is a separate JVM for each platform.
