# Linear Algebra Engine (LAE) ☕⚡

A robust, multi-threaded Java application designed to perform complex linear algebra computations efficiently. The engine features a custom thread pool implementation, fine-grained synchronization primitives, and a task-scheduling mechanism that optimizes CPU utilization while maintaining strict fairness and preventing resource contention.

---

## 🚀 Key Features & Technical Highlights

- **Custom Thread Pool (`TiredExecutor` & `TiredThread`):** 
  - Implemented a thread pool from scratch using Java's built-in monitor primitives (`synchronized`, `wait()`, `notify()`, `notifyAll()`) without relying on higher-level concurrency utilities for core scheduling.
  - Dynamic fatigue-based priority scheduling that tracks CPU execution and idle periods to distribute workloads fairly across worker threads.
- **Fine-Grained Concurrency (`SharedMatrix` & `SharedVector`):** 
  - Utilizes `ReentrantReadWriteLock` mechanisms to allow concurrent multi-reader access while ensuring exclusive write safety on shared row/column vectors.
- **Computation Tree Architecture:** Parses structured JSON input files into nested computation nodes, breaking down matrix operations (addition, multiplication, transpose, negation) into discrete parallel tasks.
- **Comprehensive Unit Testing:** Includes robust unit tests validating matrix operations, dimension constraints, error-handling resilience, and edge cases.

---

## 🛠️ Architecture Overview

1. **Main Orchestrator (`LinearAlgebraEngine`):** Parses input expressions, manages computation trees, loads matrices into shared memory slots ($M_1$ and $M_2$), and submits vector-level tasks.
2. **Execution Layer:** Distributes tasks among tired worker threads based on real-time fatigue metrics.
3. **Shared Memory Layer:** Safe in-place updates on shared matrix vectors with proper synchronization.

---

## ⚙️ Getting Started & Build Instructions

### Prerequisites
- **Java 21** (or compatible)
- **Apache Maven**

### Compilation & Testing
Clone the repository and run the following Maven commands in the root directory:

```bash
# Compile source code
mvn compile

# Run unit tests
mvn test

# Package into an executable JAR file
mvn package
Running the Engine
Execute the packaged JAR file by providing the number of threads, input JSON path, and output JSON path:

Bash
java -jar target/lga-1.0.jar 10 ./input_files/example1.json ./output_files/example1.out.json
📂 Project Structure
Plaintext
├── src/
│   ├── main/java/       # Core LAE logic, tasks, executor, and shared memory
│   └── test/java/       # Comprehensive unit tests
├── pom.xml              # Maven project configuration and dependencies
└── README.md