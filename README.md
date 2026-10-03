# 🧩 PuzzleForge — Interactive Pathfinding & Bitmask Grid Puzzle

![Java](https://img.shields.io/badge/Java-17+-orange.svg)
![GUI](https://img.shields.io/badge/GUI-Java%20Swing-blue.svg)
![Algorithms](https://img.shields.io/badge/Algorithms-BFS%20%7C%20Dijkstra-green.svg)
![State Space](https://img.shields.io/badge/State%20Space-Bitmasks-purple.svg)

`PuzzleForge` is a Java Swing interactive grid puzzle application featuring a human-vs-AI competitive gameplay mode. The game utilizes integer bit manipulation to encode grid state configurations compacting memory usage, paired with graph traversal algorithms (Breadth-First Search and Dijkstra's Algorithm) to calculate shortest paths and optimal AI moves in real time.

---

## ❓ Standard 9-Point Project Analysis

### 1. What is this?
A desktop Java application that presents an interactive pathfinding grid puzzle. Players compete against an AI agent to solve grid traversal challenges under time constraints and move limitations.

### 2. Why did I build it?
To apply core Data Structures & Algorithms concepts learned in my Computer Science coursework to a practical interactive application, specifically exploring state-space compression with bitmasks and dynamic shortest-path search in state graphs.

### 3. What technologies did I use?
- **Language:** Java (JDK 17+)
- **UI Framework:** Java Swing (`JFrame`, `JButton`, `JOptionPane`, `Timer`)
- **Core Algorithms:** Breadth-First Search (BFS), Dijkstra Shortest Path Search, Backtracking State Restoration
- **State Representation:** Integer Bitmasks (`1 << (rows * cols) - 1`)

### 4. What does it do?
- **Grid Initialization:** Renders a customizable $N \times M$ interactive grid board (up to 20 cells max).
- **Human vs AI Gameplay:** Human player inputs board movements while AI agent calculates path moves concurrently.
- **Real-Time Solver:** Computes valid path sequences using BFS for unweighted distance or Dijkstra's Algorithm for path weights.
- **Timer & Score Tracker:** Enforces a 120-second game countdown loop and tracks tile states.

### 5. How does it work?
```
+----------------+      Bitmask Encoding       +----------------+
|  Board Grid    |  ------------------------>  | Integer State  |
|  (User/AI UI)  |                             | (e.g. 0b11101) |
+----------------+                             +----------------+
        |                                              |
        v                                              v
+----------------+      Shortest Path Search   +----------------+
| Human Action   | <-------------------------> | BFS / Dijkstra |
| Input Handler  |                             | AI Move Solver |
+----------------+                             +----------------+
```
1. **Bitmask Encoding:** Each grid cell state (occupied/unoccupied) is mapped to bit positions inside an integer `bitmask`. A goal state of a $3 \times 3$ grid is encoded as `(1 << 9) - 1 = 511`.
2. **Graph Search:** The solver treats each bitmask state as a node in a state graph.
3. **BFS Execution:** Unweighted moves explore child bitmask states level-by-level to find the minimum step sequence to `GOAL`.
4. **Dijkstra Search:** Weighted paths track cost maps (`Map<Integer, Integer> dist`) to extract optimal path trajectories.

### 6. How can someone run it?

#### Prerequisites
- Java Development Kit (JDK) 11 or higher.

#### Build & Run Commands
```bash
# Clone the repository
git clone https://github.com/nikhilreddybikkam/PuzzleForge.git
cd PuzzleForge

# Compile Java source files
javac -d bin src/humvsai/*.java

# Run the Swing UI application
java -cp bin humvsai.RealtimeUI
```

### 7. What did I learn?
- How to represent high-dimensional grid states efficiently using bitwise operations (`<<`, `&`, `|`, `^`).
- How to decouple game state logic (`DijakPaths.java`) from the Swing visual view (`RealtimeUI.java`).
- Managing Swing event dispatch threads (EDT) and `javax.swing.Timer` execution loops.

### 8. What are the limitations?
- Board sizes are capped at 20 cells ($rows \times cols \le 20$) because state space complexity grows exponentially ($O(2^N)$), causing memory overhead if scaled higher without heuristic pruning.

### 9. What could be improved?
- Implementing A* Search Algorithm with Manhattan distance heuristics to accelerate state search on larger grids.
- Adding a visual solver step-by-step playback mode for educational visualization.

---

## 🏗️ Project Architecture & File Tree

```
PuzzleForge/
├── .github/
│   └── workflows/
│       └── ci.yml
├── docs/
│   └── architecture-diagram.md
├── src/
│   └── humvsai/
│       ├── DijakPaths.java       # Core game state & BFS/Dijkstra solver engine
│       └── RealtimeUI.java       # Swing GUI layout, dialogs, timer & controls
├── .gitignore
├── LICENSE
└── README.md
```

---

## ⏱️ Algorithm Complexity Analysis

- **Time Complexity:** $O(V + E)$ where $V \le 2^{N}$ states (for an $N$-cell board). On a $3 \times 3$ grid ($N=9$), $V \le 512$ states, allowing real-time path computation in $< 5\text{ms}$.
- **Space Complexity:** $O(2^N)$ space for storing visited bitmask state distances in hash maps.
