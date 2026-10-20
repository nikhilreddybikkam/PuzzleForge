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



---

## ▶️ How to Run the Project

### Prerequisites

- Java Development Kit (JDK) 11 or higher
- Visual Studio Code (VS Code) or any terminal
- Git (to clone the repository)

Verify Java installation:

```bash
java -version
javac -version
```

### 1. Clone the Repository

```bash
git clone https://github.com/nikhilreddybikkam/PuzzleForge.git
cd PuzzleForge
```

If you have already downloaded the project, open the `PuzzleForge` folder in VS Code.

### 2. Compile the Java Source Files

Open the terminal inside the project directory and run:

**Windows PowerShell:**

```powershell
mkdir bin
javac -d bin src/humvsai/*.java
```

If the `bin` directory already exists, you can reuse it.

### 3. Run the Application

```powershell
java -cp bin humvsai.RealtimeUI
```

The Java Swing GUI should open if compilation succeeds.

### 4. Troubleshooting

| Error | Solution |
|---|---|
| `javac is not recognized` | Install a JDK and configure the Java PATH. |
| `file not found` | Verify the source directory and Java filenames. |
| `Could not find or load main class` | Check the package declaration and compiled class location. |
| `Main method not found` | Ensure `RealtimeUI.java` contains a valid `public static void main(String[] args)` method. |
| `cannot find symbol` | Check class names, imports, and dependencies. |

**Note:** The commands above assume that the Java source files are located in `src/humvsai/` and that `RealtimeUI` is the main class.

---

## 🧠 Algorithm Design & Implementation Details

### 1. Bitmask-Based State Representation
Each grid configuration is represented using an integer bitmask, where individual bits encode the states of grid cells.

- **Space-efficient representation:** Stores multiple binary cell states in a single integer.
- **Fast state operations:** Uses bitwise operators to inspect and modify configurations.
- **State comparison:** Enables efficient comparison and hashing of puzzle configurations.

### 2. Graph-Based State-Space Search
The puzzle is modeled as a state-space graph:

- **Nodes:** Valid grid configurations.
- **Edges:** Legal moves between configurations.
- **Goal state:** The configuration that satisfies the puzzle's completion condition.

This representation allows standard graph-search algorithms to explore possible solutions.

### 3. Search Algorithms

| Algorithm | Purpose | Key Property |
|---|---|---|
| Breadth-First Search (BFS) | Finds shortest paths when all moves have equal cost | Complete and optimal for unweighted graphs |
| Dijkstra's Algorithm | Finds minimum-cost paths when edge weights are non-negative | Optimal for weighted graphs |

### 4. Complexity Analysis

For a puzzle with \(N\) binary cells, there can be at most \(2^N\) distinct bitmask configurations.

- **State-space size:** \(O(2^N)\)
- **BFS time complexity:** \(O(V+E)\)
- **BFS space complexity:** \(O(V)\)
- **Dijkstra time complexity:** \(O((V+E)\log V)\) with a suitable priority queue implementation

Here, \(V\) is the number of reachable states and \(E\) is the number of valid transitions between states. Actual complexity depends on the implementation and reachable state space.

### 5. Software Architecture

The application separates puzzle logic from the graphical interface:

- **`DijakPaths.java`:** Core puzzle-state representation and solver logic.
- **`RealtimeUI.java`:** Java Swing interface, user interactions, and game controls.

This separation improves code organization, maintainability, and future extensibility.

### 6. Future Enhancements

- Implement A* search with an admissible heuristic.
- Add step-by-step visualization of algorithm execution.
- Introduce solver performance comparisons across different board sizes.
- Add automated tests for state transitions and solution correctness.
- Improve scalability with search pruning and compact state representations.



## Features

- Interactive puzzle gameplay
- Move tracking and scoring
- Undo functionality
- Solver algorithms

