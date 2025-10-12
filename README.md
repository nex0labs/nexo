# 🚀 Nexo (Alpha)

**Nexo** is an **alpha-stage high-performance search server** built with [Tantivy](https://github.com/quickwit-oss/tantivy) and **Java**, designed to **index and query large datasets in sub-seconds**.

> ⚠️ **Alpha Notice:**
> Nexo is currently in **active development**. Expect breaking changes, incomplete features, and evolving APIs. Feedback and contributions are highly encouraged!

---

## 📚 Table of Contents

* [⚡ Features](#-features)
* [🧩 Architecture Overview](#-architecture-overview)
* [🚀 Quick Start](#-quick-start)
* [🧠 Example Use Cases](#-example-use-cases)
* [🛠️ Tech Stack](#️-tech-stack)
* [📈 Performance](#-performance)
* [🤝 Contributing](#-contributing)
* [📄 License](#-license)
* [🌟 Acknowledgments](#-acknowledgments)

---

## ⚡ Features (in progress)

* 🔍 **Ultra-fast full-text search** — powered by Tantivy’s Rust engine
* 🧩 **Java API layer** — for easy integration and extension
* ⚙️ **RESTful HTTP endpoints** — simple and language-agnostic
* 🧠 **Custom analyzers & ranking** *(planned)*
* 🗃️ **Persistent and concurrent indexing** *(in progress)*
* 📊 **Efficient query execution** *(coming soon)*

---

## 🧩 Architecture Overview

Nexo combines **Rust performance** with **Java's ecosystem** through an FFI (Foreign Function Interface) bridge, serving requests via **Netty's high-performance HTTP server**.

```
         ┌──────────────────────────────┐
         │           Client             │
         └─────────────┬────────────────┘
                       │ HTTP/REST
         ┌─────────────┴────────────────┐
         │        Netty Server          │
         │      (HTTP Handler)          │
         └─────────────┬────────────────┘
                       │
         ┌─────────────┴────────────────┐
         │          Nexo API            │
         │       (Java Service)         │
         └─────────────┬────────────────┘
                       │ JNI / FFI
         ┌─────────────┴────────────────┐
         │       Tantivy Engine         │
         │      (Rust Search Core)      │
         └──────────────────────────────┘
```

---

## 🚀 Quick Start

### 1. Clone the repository

```bash
git clone https://github.com/nex0labs/nexo.git
cd nexo
```

### 2. Build the project

```bash
mvn clean package
```

### 3. Run the server

```bash
java -jar target/nexo-0.1.0-SNAPSHOT.jar
```

### 4. Try it out

```bash
# Check server health
curl http://localhost:9090/

# Create a collection
curl -X POST http://localhost:9090/collections/ \
     -H "Content-Type: application/json" \
     -d '{"name": "my_collection", "schema": {}}'
```

---

## 🧠 Example Use Cases

* 🔎 Document, log, or dataset search
* 🛍️ Product or metadata indexing for e-commerce
* 📊 Fast analytics and exploration tools
* 🧩 Embedded local search for Java applications

---

## 🛠️ Tech Stack

| Component          | Technology                                                |
| ------------------ | --------------------------------------------------------- |
| Core Search Engine | [Tantivy (Rust)](https://github.com/quickwit-oss/tantivy) |
| HTTP Server        | [Netty](https://netty.io/)                                |
| API Layer          | Java 21+                                                  |
| Build Tool         | Maven                                                     |
| Integration        | REST / JSON API                                           |

---

## 📈 Performance

Nexo’s architecture targets **sub-second search latency** even on large indexes, leveraging Tantivy’s optimized data structures.
⚠️ Performance tuning and benchmark reports will be available in later releases.

---

## 🤝 Contributing

Nexo is in its **alpha stage**, and we’d love your feedback!

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/new-feature`)
3. Commit your changes
4. Push to your fork and open a Pull Request

---

## 📄 License

**Nexo** is licensed under the **Apache License 2.0**.
See the [LICENSE](LICENSE) file for full details.

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0
```

---

## 🌟 Acknowledgments

* [Tantivy](https://github.com/quickwit-oss/tantivy) — powering Nexo’s search core
* The open-source community for ideas, contributions, and inspiration
