# NetRoyale

![Java](https://img.shields.io/badge/Java-21-orange?style=flat-square&logo=openjdk)
![JavaFX](https://img.shields.io/badge/JavaFX-UI-blue?style=flat-square)
![Build](https://img.shields.io/badge/Build-Maven-C71A36?style=flat-square&logo=apachemaven)
![License](https://img.shields.io/badge/License-MIT-green?style=flat-square)

**NetRoyale** is a high-performance, real-time multiplayer strategy game engineered in **Java**. It features a custom TCP-based client-server architecture, synchronizing complex game states across distributed clients with minimal latency.

The project demonstrates advanced object-oriented design, custom network protocol implementation, and efficient rendering techniques using the JavaFX Canvas API.

---

## Key Features

### Core Gameplay Mechanics
* **Tactical Turn-Based Combat:** 1v1 grid-based warfare requiring strategic positioning and resource management.
* **Polymorphic Unit System:** Distinct unit classes utilizing OOP inheritance for unique stats and behaviors:
    * **Tank:** High durability, frontline defender.
    * **Warrior:** Versatile melee combatant.
    * **Archer:** Long-range sniper with low defense.
* **Dynamic Visibility (Fog of War):** Real-time visibility calculation based on unit position.
* **Pathfinding Engine:** Valid movement and attack ranges calculated instantly using **Breadth-First Search (BFS)** algorithms.

### Engineering & Architecture
* **Client-Server Architecture:** A centralized authoritative server manages game state, enforcing rules and preventing client-side state manipulation.
* **Custom Network Protocol:** Developed a lightweight JSON-based communication protocol over raw **TCP Sockets** for reliable state synchronization.
* **Interpolated Rendering:** Implemented a smooth animation system using a custom 60 FPS `AnimationTimer` loop to decouple logic updates from rendering frames.
* **Concurrency:** Multi-threaded server implementation to handle concurrent client sessions and chat streams.

---

## Tech Stack

| Category | Technology |
| :--- | :--- |
| **Language** | Java 21 (OpenJDK) |
| **GUI Framework** | JavaFX (Canvas API) |
| **Networking** | Java `java.net.Socket`, `ServerSocket` (TCP) |
| **Build Tool** | Apache Maven (Multi-module Architecture) |
| **Serialization** | Jackson (JSON) |
| **Version Control** | Git & GitHub |

---

## Project Structure

The project follows a modular Maven architecture to separate concerns:

```text
NetRoyale/
├── common/       # Shared Library (POJOs, GameState, Network Packets)
├── server/       # Backend Logic (Client handling, Game Loop, Validation)
├── client/       # Frontend (UI Rendering, Input Handling, Audio)
└── pom.xml       # Root Build Configuration
