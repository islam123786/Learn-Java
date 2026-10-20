# Java Course Playlist Breakdown

**Channel Link:** [Coder Army Java Playlist](https://www.youtube.com/watch?v=LBqE4YOvhyc&list=PLQEaRBV9gAFsR15tNo2QLF9d2qc-c018p)

Study notes for all 57 videos (about 62 h 45 min). There's one notes file per video (the numbered `.md` files in this repo), each with a TL;DR, key concepts, detailed notes, runnable Java examples, pitfalls and review questions.

---

### Module Breakdown

| Videos | Topic |
| :--- | :--- |
| **01 – 24** | [Core Java](#core-java) |
| **25 – 26** | [Strings](#strings) |
| **27 – 28** | [Generics](#generics) |
| **29 – 36** | [Collection Framework](#collection-framework) |
| **37 – 42** | [Java 8 Features](#java-8-features) |
| **43 – 44** | [Exception in Java](#exception-in-java) |
| **45 – 46** | [Memory Management](#memory-management) |
| **47 – 57** | [Java MultiThreading](#java-multithreading) |

---

### How to use these notes
- Each section has a **▶ jump link** that opens the video at roughly that moment. The timestamps are estimates: the transcripts had no timing data, so each one was worked out from where that part falls in the transcript. They're usually within 1–2 minutes.
- Most videos are in Hindi/Hinglish. The notes were written in English from the Hindi transcript, and each file's header says which transcript was used.
- Every `java` block is a single-file program. Copy it to `ClassName.java` and run `java ClassName.java` (JDK 21+). All of them were compiled and run. Blocks whose first line is `// does not compile` or `// throws` are meant to fail, to show an error.
- Where a video simplifies something or gets it slightly wrong, the notes add a short correction.

## Core Java

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 01 | [JAVA Full Course · Introduction to JAVA](01-introduction-to-java.md) | 59:06 | [▶](https://youtu.be/LBqE4YOvhyc) |
| 02 | [Write Your First JAVA Program · JVM, JDK & JRE Explained · JSE vs JEE vs JME](02-first-java-program-jvm-jdk-jre.md) | 41:33 | [▶](https://youtu.be/pdS8_smlsXA) |
| 03 | [Java Variables & Data Types Explained · Identifiers, Literals, Keywords](03-variables-data-types-literals-keywords.md) | 66:42 | [▶](https://youtu.be/NtmULLvsABc) |
| 04 | [How Java Stores Negative & Floating Point Numbers](04-negative-floating-point-storage.md) | 57:54 | [▶](https://youtu.be/iV8hy9DdKFM) |
| 05 | [Java Type Conversions & Type Promotions Explained · Casting in Java](05-type-conversion-promotion-casting.md) | 36:43 | [▶](https://youtu.be/QpWha2cLS1c) |
| 06 | [Java Operators · Arithmetic + Unary + Logical + Bitwise + Assignment Operators](06-java-operators.md) | 128:10 | [▶](https://youtu.be/Tdq4fmDCJH8) |
| 07 | [Java Conditional Statements · If, If-Else, If-Else-If Ladder, Switch Explained](07-java-conditional-statements.md) | 81:28 | [▶](https://youtu.be/cdNhXVb5j7o) |
| 08 | [Java Loops & Jump Statements · for, while, do-while, break, continue Explained](08-java-loops-and-jump-statements.md) | 85:06 | [▶](https://youtu.be/d6hNDJrj1bw) |
| 09 | [Introduction to Arrays · Declaration, Initialization, Traversal · String Intro](09-introduction-to-arrays.md) | 107:00 | [▶](https://youtu.be/ajNs-dylWIM) |
| 10 | [How Arrays work in Java · Heap, Contiguous Storage, Random Access](10-how-arrays-work-in-java.md) | 54:03 | [▶](https://youtu.be/Qg73uiG9b6A) |
| 11 | [Java Functions Deep Dive · Recursion · Method Overloading](11-java-functions-recursion-overloading.md) | 84:50 | [▶](https://youtu.be/IhtjMWXwNCc) |
| 12 | [Object Oriented Programming in Java · Classes, Objects, new Keyword](12-oop-classes-objects-new-keyword.md) | 65:10 | [▶](https://youtu.be/tmXD7Kzun54) |
| 13 | [Constructors in Java · Constructor Chaining, Overloading & this](13-constructors-chaining-overloading-this.md) | 58:02 | [▶](https://youtu.be/RADpqk79IzQ) |
| 14 | [Objects Deep Dive · Size, Call by Value vs Reference · Shallow vs Deep Copy](14-objects-size-call-by-value-copy.md) | 44:20 | [▶](https://youtu.be/7yd5qO2TPr0) |
| 15 | [Static & Final in Java · Static Methods, Blocks & String[] args](15-static-final-blocks-string-args.md) | 45:28 | [▶](https://youtu.be/Lyg4ZcrjUBw) |
| 16 | [Encapsulation & Inheritance in Java · Java Packages & Super Keyword](16-encapsulation-inheritance-packages-super.md) | 98:02 | [▶](https://youtu.be/jfLpzL1VW7Q) |
| 17 | [Abstraction & Polymorphism in Depth · Abstract Class vs Interfaces](17-abstraction-and-polymorphism.md) | 88:01 | [▶](https://youtu.be/-L9BcU6Xk2c) |
| 18 | [AutoBoxing, Abstract classes & POJOs · Why only one public class in java file](18-autoboxing-abstract-classes-pojos.md) | 72:01 | [▶](https://youtu.be/crVwHNW-fX8) |
| 19 | [Java Nested Classes · Static Nested, Inner, Local & Anonymous Classes](19-java-nested-classes.md) | 80:57 | [▶](https://youtu.be/0LaBK28_470) |
| 20 | [Java I/O Deep Dive · InputStreams, System.in, Scanner vs BufferedReader](20-java-io-deep-dive.md) | 71:13 | [▶](https://youtu.be/yfMWLZGEPwo) |
| 21 | [Immutable Classes in Java · How to Create Immutable Objects](21-immutable-classes-in-java.md) | 35:04 | [▶](https://youtu.be/vgQVz5OdFws) |
| 22 | [Most Important Class in Java · Object Class (equals, hashCode, toString)](22-java-object-class-equals-hashcode.md) | 67:31 | [▶](https://youtu.be/541Zpv6sOKo) |
| 23 | [Java Enums · Why Not Use int/String for Constants?](23-java-enums-why-not-constants.md) | 72:09 | [▶](https://youtu.be/oXhy5dT-0Qs) |
| 24 | [Java Interfaces Deep Dive · Default Methods, Functional, Marker Interfaces](24-java-interfaces-default-functional-marker.md) | 59:55 | [▶](https://youtu.be/YLLFHuStkW8) |

## Strings

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 25 | [Java Strings · String Pool, Immutability, Internals](25-java-strings-pool-immutability.md) | 68:59 | [▶](https://youtu.be/N0b8lRXtK_Y) |
| 26 | [Java Strings Part 2 · All String Methods + StringBuilder vs StringBuffer](26-java-string-methods-stringbuilder-buffer.md) | 75:54 | [▶](https://youtu.be/JcAD9a22Rks) |

## Generics

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 27 | [Java Generics Deep Dive · Bounded Types using extends](27-generics-bounded-types.md) | 65:07 | [▶](https://youtu.be/Y7X_ReDF3x8) |
| 28 | [Wildcards in Generics · ?, ? extends, ? super](28-generics-wildcards.md) | 82:23 | [▶](https://youtu.be/Opb2q2WSyes) |

## Collection Framework

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 29 | [Java Collection Framework Explained · Data Structures + Complete Hierarchy](29-java-collection-framework-explained.md) | 75:58 | [▶](https://youtu.be/mW2OO6d6_BE) |
| 30 | [Iterable Interface in Java · Why Iterator Exists? · For-Each Loop](30-iterable-iterator-for-each.md) | 76:01 | [▶](https://youtu.be/LVwpxEZ6uMQ) |
| 31 | [Java Collection Interface Deep Dive · Methods & Internals](31-collection-interface-methods.md) | 29:14 | [▶](https://youtu.be/cazzzwdLAkQ) |
| 32 | [Java List Interface Deep Dive · ArrayList, LinkedList, Vector, Stack](32-list-interface-arraylist-linkedlist.md) | 44:37 | [▶](https://youtu.be/OfNtIOGDqzY) |
| 33 | [Set & Map Interface in Java · Internal Working of HashMap, HashSet & TreeMap](33-set-map-hashmap-internals.md) | 97:27 | [▶](https://youtu.be/kzZ1TlGTWmY) |
| 34 | [Set & Map Methods Explained · EnumMaps, IdentityHashMaps & More](34-set-map-methods-special-maps.md) | 71:19 | [▶](https://youtu.be/b1Uj_2MPBwA) |
| 35 | [Java Queue Interface Deep Dive · PriorityQueue & Heap Explained](35-queue-priorityqueue-heap.md) | 77:24 | [▶](https://youtu.be/zj0-JIc5oPw) |
| 36 | [Comparable Interface in Java · Collections class · Comparing Objects](36-comparable-collections-class.md) | 44:34 | [▶](https://youtu.be/uUo7AV02vJM) |

## Java 8 Features

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 37 | [Java Lambdas & Functional Interfaces · Comparator Interface](37-lambdas-functional-interfaces-comparator.md) | 49:38 | [▶](https://youtu.be/0V-w2BywAM0) |
| 38 | [Lambdas & Functional Interface Deep Dive · Method References & Composition](38-lambdas-functional-interfaces-method-references.md) | 115:08 | [▶](https://youtu.be/IsC0Ms2iFi8) |
| 39 | [Introduction to Java Streams · Creation & Architecture](39-intro-java-streams-creation-architecture.md) | 63:20 | [▶](https://youtu.be/-jHyDEXHr_U) |
| 40 | [Java Stream Methods Deep Dive · All Intermediate & Terminal Operations](40-stream-intermediate-terminal-operations.md) | 60:12 | [▶](https://youtu.be/xqfOH5EA8xo) |
| 41 | [Java Optional Explained from Scratch · All Methods + Streams Integration](41-java-optional-explained.md) | 76:33 | [▶](https://youtu.be/Q75kCaNa35Y) |
| 42 | [Parallel Streams in Java Explained · Primitive Optional Classes](42-parallel-streams-primitive-optionals.md) | 42:35 | [▶](https://youtu.be/zrtVQ1ev_0w) |

## Exception in Java

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 43 | [Java Exception Handling from Scratch · Exceptions vs Errors](43-exception-handling-exceptions-vs-errors.md) | 55:45 | [▶](https://youtu.be/mJg9iQZJEpw) |
| 44 | [Exception Hierarchy Deep Dive · throw vs throws · Custom Exceptions](44-exception-hierarchy-throw-throws-custom.md) | 65:23 | [▶](https://youtu.be/xTxscoajikw) |

## Memory Management

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 45 | [Java Memory Management Explained in Depth · Stack, Heap, Method Area & PC](45-java-memory-stack-heap-method-area.md) | 64:10 | [▶](https://youtu.be/kjETbH63Pco) |
| 46 | [Java Heap Memory Deep Dive · Garbage Collection Algorithms Explained](46-heap-memory-garbage-collection.md) | 62:52 | [▶](https://youtu.be/b2HSqC0RGsM) |

## Java MultiThreading

| # | Topic | Length | Video |
| :--- | :--- | :--- | :--- |
| 47 | [Introduction to Multithreading in Java · Process vs Thread](47-multithreading-process-vs-thread.md) | 51:37 | [▶](https://youtu.be/fyAW0W526RM) |
| 48 | [Java Thread Creation & Lifecycle Explained from Scratch](48-thread-creation-lifecycle.md) | 60:59 | [▶](https://youtu.be/cVRdeQFP5IM) |
| 49 | [Thread Methods · sleep, join, yield, interrupt, isAlive, priority & more](49-thread-methods-sleep-join-interrupt.md) | 39:04 | [▶](https://youtu.be/ZPxJby0GeOQ) |
| 50 | [Problems in Multithreading · Race Condition, Visibility, Ordering Explained](50-race-condition-visibility-ordering.md) | 55:53 | [▶](https://youtu.be/lrrdN_c0HQ4) |
| 51 | [Monitor Locks in Java · Synchronized Keyword, Static Sync & Custom Locks](51-monitor-locks-synchronized.md) | 47:57 | [▶](https://youtu.be/k9sURAu0xT8) |
| 52 | [Inter Thread Communication in Java · wait(), notify(), notifyAll() Deep Dive](52-inter-thread-communication-wait-notify.md) | 63:17 | [▶](https://youtu.be/EZS19NLnsvc) |
| 53 | [Java Locks · ReentrantLock, ReadWriteLock, StampedLock, Semaphore & Condition](53-java-locks-reentrant-readwrite-stamped-semaphore.md) | 80:04 | [▶](https://youtu.be/JW6-TCU0iS4) |
| 54 | [Lock-Free Concurrency in Java · AtomicVariables & CAS Explained](54-lock-free-concurrency-atomic-cas.md) | 49:06 | [▶](https://youtu.be/ujF2gNsCfBE) |
| 55 | [Lock-Free Concurrency in Java - 2 · CAS Retry, Compare-and-Swap & ABA Problem](55-cas-retry-loop-aba-problem.md) | 31:29 | [▶](https://youtu.be/2nBJPpERul4) |
| 56 | [Executor Framework Deep Dive · ThreadPool, Future & Callable](56-executor-framework-future-callable.md) | 79:14 | [▶](https://youtu.be/VPtaTUSaBOM) |
| 57 | [CompletableFuture, Fork-Join Pool, ThreadLocal & Virtual Threads](57-completablefuture-forkjoin-threadlocal-virtual-threads.md) | 82:18 | [▶](https://youtu.be/FGN225TiXaE) |
