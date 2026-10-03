package humvsai;

import java.util.*;
import java.io.*;

public class DijakPaths {

    static int rows , cols;

    static int userFlipCount, aiFlipCount;
    static String userName, userId, levelName;

    static int[][] userBoard, aiBoard, originalBoard;

    static boolean easyFirstMoveDone = false;

    static final String FILE_NAME = "winners.txt";
   //used for flipping the adjacent cells
    static final int[][] DIRS = {
            {0, 0}, {-1, 0}, {1, 0}, {0, -1}, {0, 1}
    };
 // ===== SCORE SYSTEM =====
    static int userScore = 0;
    static int aiScore = 0;           
    static int userUsedMask = 0;

    static Random rand = new Random();
    
    static class MoveResult {
        int bestMove;
        int score;

        MoveResult(int m,int s){
            bestMove = m;
            score = s;
        }
    }
 

    // ===== DP TABLES =====
    static Map<Integer,Integer> dpCost = new HashMap<>();
    static Map<Integer,Integer> dpMove = new HashMap<>();
    static Set<Integer> visiting = new HashSet<>();
    static List<Integer> btPath = new ArrayList<>();
    static Set<Integer> btVisited = new HashSet<>();    
    static  int GOAL ;
 // MAIN METHOD
    // Preamble :- This is the starting point of the program.
    // It takes user details, board size and initializes the goal state.
    // Then it starts the game execution.
    // Time Complexity :- O(n × 2^n) (depends on DP/BFS calls inside game)
    // Space Complexity :- O(2^n) (for storing board states in maps)

    public static void main(String[] args) throws IOException {

        Scanner sc = new Scanner(System.in);

        System.out.print("Enter Name: ");
        userName = sc.nextLine();

        System.out.print("Enter ID: ");
        userId = sc.nextLine();
        
        System.out.print("Enter rows: "); 
        rows = sc.nextInt(); 
        
        System.out.print("Enter cols: "); 
        cols = sc.nextInt();
        
        if(rows == 1 && cols == 1){
            System.out.println("Not possible");
            System.exit(0);
        }

        
        if(rows * cols > 20){
            System.out.println("Board too large for bitmask AI");
            System.exit(0);
        }
        
        GOAL = (1<<(rows*cols)) - 1;
        
        selectLevel(sc);

        while (true) {
            System.out.println("\n===== MAIN MENU =====");
            System.out.println("1 -> Start Game");
            System.out.println("2 -> Change Level");
            System.out.println("3 -> Exit");
            System.out.print("Choice: ");

            int ch = sc.nextInt();

            if (ch == 1) {
            	playGame(sc);
            }
            else if (ch == 2) {
            	selectLevel(sc);
            }
            else if (ch == 3) {
                System.out.println("Exiting Game...");
                sc.close();
                System.exit(0);
            }
            else {
            	System.out.println("Enter correct option!");
            }
        }
    }
    // GAME LOOP
    // Preamble :- Controls the entire game flow.
    // It displays boards, handles user input,
    // performs AI move and checks winning condition.
    // Internally it may call DP, BFS or Dijkstra.
    // Time Complexity :- O(n × 2^n) (due to state space search)
    // Space Complexity :- O(2^n)

    static void playGame(Scanner sc) throws IOException {

        startNewGame();

        while (true) {

            printCounts();
            System.out.printf("\n%-16s%s\n","USER","AI");
            printSideBySideBoard(userBoard, aiBoard);

            if (isSolved(userBoard) || isSolved(aiBoard)) {

                if (isSolved(userBoard) && !isSolved(aiBoard)) {
                    System.out.println("\nHUMAN WON!");
                    saveWinner();
                } else if (isSolved(aiBoard)) {
                    System.out.println("\nAI WON!");
                } else {
                    System.out.println("\nDRAW!");
                }

                System.out.println("\nOPTIMAL PATH:");
                printPath(computeOptimalPathBFS(originalBoard));
                showPreviousWinners();
                return;
            }
            System.out.println("\nOptions:");
            System.out.println("1 -> User Flip");
            System.out.println("2 -> Reveal Best Move");
            System.out.println("3 -> Restart");
            System.out.println("4 -> Menu");
            System.out.println("5 -> Undo Last Move");
            int ch = sc.nextInt();

            if (ch == 1) {
            	pushUndoState();
            	while (true) {
            	    System.out.print("Row : ");
            	    int r = sc.nextInt();
            	    System.out.print("Col : ");
            	    int c = sc.nextInt();

            	    if (r < 0 || r >= rows  || c < 0 || c >= cols) {
            	        System.out.println(" Path doesn't exist! Try again.");
            	    } else {
            	        flip(userBoard, r, c);
            	        userFlipCount++;
            	        aiMove();
            	        break; 
            	    }
            	}
            }
            else if (ch == 2) {
                int best=getOptimalMoveDijkstra(userBoard);
                System.out.println("Best Move: "+(best/cols)+","+(best%cols));
            }
            else if (ch == 3) {
            	startNewGame();
            }  else if (ch == 4) {
                return;
            } else if (ch == 5) {
                undoLastMove();
            }
            else {
                System.out.println(" Enter the right option!");
            }
        }
    }

    static void selectLevel(Scanner sc) {
        while (true) {
            System.out.println("\nSelect Level:");
            System.out.println("1 -> Easy");
            System.out.println("2 -> Medium");
            System.out.println("3 -> Hard");
            System.out.print("Choice: ");

            int ch = sc.nextInt();

            if (ch == 1) { levelName = "Easy"; break; }
            else if (ch == 2) { levelName = "Medium"; break; }
            else if (ch == 3) {
            	levelName = "Hard";
            	break;
            }
            else {
            	System.out.println(" Please select a correct option!");
            }
        }
    }
    // START NEW GAME
    // Preamble :- Initializes user board, AI board and original board
    // with a randomly generated configuration.
    // It also resets counters and clears DP memory tables.
    // Time Complexity :- O(n) (board generation + copying)
    // Space Complexity :- O(n) (stores three boards of size n)

    static void startNewGame(){
    	 int[][] src = new int[rows][cols];
    	    for(int i=0;i<rows;i++)
    	        Arrays.fill(src[i],1);
    	    int moves;

    	    if(levelName.equals("Easy"))
    	        moves = rows * cols / 2;

    	    else if(levelName.equals("Medium"))
    	        moves = rows * cols;

    	    else
    	        moves = rows * cols * 2;

    	    for(int k=0;k<moves;k++){
    	        int r = rand.nextInt(rows);
    	        int c = rand.nextInt(cols);
    	        flip(src,r,c);
    	    }

        userBoard=copy(src);
        aiBoard=copy(src);
        originalBoard=copy(src);

        userFlipCount=0;
        aiFlipCount=0;
        easyFirstMoveDone = false;
        undoStack.clear();
        userScore = 0;
        aiScore = 0;
        userUsedMask = 0;
        dpCost.clear();
        dpMove.clear();
        visiting.clear();
    }

    // ================= AI =================
    // AI MOVE
    // Preamble :- AI encodes the board into bitmask
    // and computes the optimal move using Dynamic Programming.
    // Then it performs the flip operation.
    // Time Complexity :- O(n × 2^n)
    // Space Complexity :- O(2^n)


    static void aiMove(){

    	if (levelName.equals("Easy")) {
    		if(!easyFirstMoveDone){
                flip(aiBoard, 0, 0);
                aiFlipCount++;
                easyFirstMoveDone = true;
                System.out.println("AI Move (Easy-First): 0,0");
                return;
            }

            List<Integer> path = computeOptimalPathBFS(aiBoard);

            if(path != null && !path.isEmpty()){
                int move = path.get(0);
                flip(aiBoard, move/cols, move%cols);
                aiFlipCount++;
                System.out.println("AI Move (Easy-BFS): " + (move/cols) + "," + (move%cols));
            }
            return;
        }

        if(levelName.equals("Medium")){
            MoveResult res = mergeDivideMove(aiBoard,0,rows-1,0,cols-1);
            if(res.bestMove!=-1){
                flip(aiBoard,res.bestMove/cols,res.bestMove%cols);
                aiFlipCount++;
            }
            return;
        }

        // HARD DP
        int mask=encode(aiBoard);
        int move=-1;
        try {
            move = getBestMoveDP(mask); 
        } catch (Exception ignored) {
            move = -1;
        }
        if(move < 0){
            int n = rows * cols;
            int cap;
            if(n <= 9) cap = 16;
            else if(n <= 16) cap = 12;
            else cap = 10;

            List<Integer> path = solveByBacktracking(aiBoard, cap);
            if(path != null && !path.isEmpty()){
                move = path.get(0);
                System.out.println("Hard fallback used Backtracking (IDDFS), depthCap=" + cap);
            } else {
                move = 0; 
            }
        }
        flip(aiBoard,move/cols,move%cols);
        aiFlipCount++;
        System.out.println("AI Move (DP): "+move/cols+","+move%cols);
    }
 // ===== UNDO SUPPORT =====
    static Deque<GameState> undoStack = new ArrayDeque<>();

    static class GameState {
        int userMask, aiMask;
        int userCount, aiCount;
        boolean easyDone;
        int savedUserScore;
        int savedAiScore;
        int savedUserUsedMask;

        GameState(int uMask, int aMask, int uC, int aC, boolean eDone,int uScore, int aScore, int usedMask){
            userMask = uMask;
            aiMask = aMask;
            userCount = uC;
            aiCount = aC;
            easyDone = eDone;
            savedUserScore = uScore;
            savedAiScore = aScore;
            savedUserUsedMask = usedMask;

        }
    }

    // ================= DP =================
 // DYNAMIC PROGRAMMING (Memoization)
    // Preamble :- Each board configuration is treated as a state
    // represented using an integer bitmask.
    // For every state, we try all possible flips
    // and store the minimum moves required to reach GOAL.
    // If a state is already computed, result is reused.
    // Time Complexity :- O(n × 2^n)
    // Space Complexity :- O(2^n)


    static int solveDP(int mask){

        if(mask==GOAL) return 0;

        if(dpCost.containsKey(mask))
            return dpCost.get(mask);

        if(visiting.contains(mask))
            return Integer.MAX_VALUE/2;

        visiting.add(mask);

        int best=Integer.MAX_VALUE/2;
        int bestMove=0;

        for(int i=0;i<rows*cols;i++){
            int next=flipMask(mask,i);
            int cost=1+solveDP(next);
            if(cost<best){
                best=cost;
                bestMove=i;
            }
        }

        visiting.remove(mask);
        dpCost.put(mask,best);
        dpMove.put(mask,bestMove);
        return best;
    }
    // GET BEST MOVE USING DP
    // Preamble :- Calls solveDP() and returns
    // the optimal move stored in dpMove map.
    // Time Complexity :- O(n × 2^n)
    // Space Complexity :- O(2^n)

    static int getBestMoveDP(int mask){
        solveDP(mask);
        return dpMove.getOrDefault(mask, 0);
    }
 // LOWER BOUND HEURISTIC
 // Preamble :- Estimates the minimum number of flips needed
 // to reach the GOAL state from the current bitmask.
 // Counts the number of zero bits (unsolved cells) and divides by 5,
 // since one flip can affect at most 5 cells (center + 4 neighbors).
 // Used as a pruning condition in IDDFS backtracking.
 // Time Complexity :- O(1) (uses built-in bitCount)
 // Space Complexity :- O(1)
    static int lowerBound(int mask){
        int zeros = (rows*cols) - Integer.bitCount(mask);
        return (zeros + 4) / 5;
    }
 // SOLVE BY BACKTRACKING (IDDFS)
 // Preamble :- Iterative Deepening Depth-First Search (IDDFS).
 // Tries to find a solution path within increasing depth limits
 // from 0 up to maxDepthCap. Used as a fallback when DP
 // fails due to memory or recursion limits on large boards.
 // Time Complexity :- O(n^d) where d = solution depth, n = rows × cols
 // Space Complexity :- O(d) (recursive call stack depth)
    static List<Integer> solveByBacktracking(int[][] board, int maxDepthCap){
        int start = encode(board);

        for(int limit = 0; limit <= maxDepthCap; limit++){
            btPath.clear();
            btVisited.clear();
            if(dfsBacktrack(start, limit)){
                return new ArrayList<>(btPath);
            }
        }
        return Collections.emptyList();
    }
 // DFS BACKTRACKING (Single Depth Pass)
 // Preamble :- Performs a single depth-limited DFS pass.
 // At each state, all possible flips are tried recursively.
 // The lowerBound pruning skips branches that cannot reach GOAL
 // within the remaining depth limit. Visited states within
 // the current path are tracked to avoid cycles.
 // Time Complexity :- O(n^limit) worst case, pruned by lowerBound
 // Space Complexity :- O(limit) (recursion stack + btPath list)
    static boolean dfsBacktrack(int mask, int limit){
        if(mask == GOAL) return true;
        if(limit == 0) return false;

        if(lowerBound(mask) > limit) return false;

        if(btVisited.contains(mask)) return false;
        btVisited.add(mask);
        for(int i = 0; i < rows*cols; i++){
            int next = flipMask(mask, i);
            btPath.add(i);

            if(dfsBacktrack(next, limit-1)) return true;

            btPath.remove(btPath.size()-1);
        }

        btVisited.remove(mask);
        return false;
    }
 // MERGE DIVIDE METHOD
 // Preamble :- This method follows Divide and Conquer approach.
 // The board is divided into four smaller quadrants recursively
 // similar to how Merge Sort divides an array.
 // For a single cell, it simulates flip and evaluates
 // remaining zero count using heuristic.
 // Among four parts, the move with minimum zeros is selected.
 // Time Complexity :- O(n log n)
 // Space Complexity :- O(log n) (recursive stack space)

    static MoveResult mergeDivideMove(int[][] board,int r1,int r2,int c1,int c2){
    	if(r1>r2 || c1>c2) {
            return new MoveResult(-1,Integer.MAX_VALUE);
    	}
	
    	if(r1==r2 && c1==c2){
    			if(board[r1][c1]==0){
    				int[][] t=copy(board);
    				flip(t,r1,c1);
    				return new MoveResult(r1*cols+c1,heuristicZeroCount(t));
    			}
    			return new MoveResult(-1,Integer.MAX_VALUE);
    		}

    		int midR=(r1+r2)/2;
    		int midC=(c1+c2)/2;

    		MoveResult a=mergeDivideMove(board,r1,midR,c1,midC);
    		MoveResult b=mergeDivideMove(board,r1,midR,midC+1,c2);
    		MoveResult c=mergeDivideMove(board,midR+1,r2,c1,midC);
    		MoveResult d=mergeDivideMove(board,midR+1,r2,midC+1,c2);

    		MoveResult best=a;
    		if(b.score<best.score) best=b;
    		if(c.score<best.score) best=c;
    		if(d.score<best.score) best=d;
    		return best;
    }
    // HEURISTIC ZERO COUNT
    // Preamble :- This function counts number of zero cells
    // present in the board after a simulated flip.
    // It is used as evaluation function in greedy approach.
    // Time Complexity :- O(n) (checks all cells)
    // Space Complexity :- O(1)
    static int heuristicZeroCount(int[][] b){
        int c=0;
        for(int[] r:b) { 
        	for(int v:r) {
        		if(v==0) {
        			c++;
        		}
        		}
        	}
        return c;
    }
    // FLIP OPERATION
    // Preamble :- Flips the selected cell and its
    // top, bottom, left and right neighbors using XOR.
    // Each flip affects at most 5 cells.
    // Time Complexity :- O(1)
    // Space Complexity :- O(1)
    static void flip(int[][] b,int r,int c){
        for(int[] d:DIRS){
            int nr=r+d[0],nc=c+d[1];
            if(nr>=0&&nr<rows&&nc>=0&&nc<cols)
                b[nr][nc]^=1;
        }
    }
    // CHECK SOLVED
    // Preamble :- Checks whether all cells in the board are 1.
    // Returns true if solved, otherwise false.
    // Time Complexity :- O(n) (checks all cells)
    // Space Complexity :- O(1)
    static boolean isSolved(int[][] b){
        for(int[] r:b) for(int v:r) if(v==0) return false;
        return true;
    }
    // ENCODE BOARD
    // Preamble :- Converts 2D board into integer bitmask.
    // Each cell corresponds to one bit.
    // Used for DP, BFS and Dijkstra.
    // Time Complexity :- O(n)
    // Space Complexity :- O(1)
    static int encode(int[][] b){
        int m=0;
        for(int i=0;i<rows;i++)
            for(int j=0;j<cols;j++)
                if(b[i][j]==1) m|=1<<(i*cols+j);
        return m;
    }
    // FLIP MASK
    // Preamble :- Performs flip operation on encoded bitmask.
    // Toggles bits corresponding to selected cell and neighbors.
    // Used in DP, BFS and Dijkstra transitions.
    // Time Complexity :- O(1)
    // Space Complexity :- O(1)
    static int flipMask(int mask,int pos){
        int r=pos/cols,c=pos%cols;
        for(int[] d:DIRS){
            int nr=r+d[0],nc=c+d[1];
            if(nr>=0&&nr<rows&&nc>=0&&nc<cols)
                mask^=1<<(nr*cols+nc);
        }
        return mask;
    }
 // COPY BOARD
 // Preamble :- Creates and returns a deep copy of the given 2D board.
 // Used to avoid modifying the original board during simulations.
 // Time Complexity :- O(n) where n = rows × cols
 // Space Complexity :- O(n) (new board of same size is allocated)
    static int[][] copy(int[][] s){
        int[][] d=new int[rows][cols];
        for(int i=0;i<rows;i++)
            System.arraycopy(s[i],0,d[i],0,cols);
        return d;
    }
 // DECODE BOARD
 // Preamble :- Converts an integer bitmask back into a 2D board.
 // Each bit position corresponds to a cell in the board.
 // This is the reverse operation of encode().
 // Used during undo to restore previous board states.
 // Time Complexity :- O(n) where n = rows × cols
 // Space Complexity :- O(1) (modifies board in-place)
    static void decode(int mask, int[][] b){
        for(int i=0;i<rows;i++){
            for(int j=0;j<cols;j++){
                int bit = 1 << (i*cols + j);
                b[i][j] = ((mask & bit) != 0) ? 1 : 0;
            }
        }
    }

    // ===== NEW (UNDO) =====
 // PUSH UNDO STATE
 // Preamble :- Captures and saves the current game state
 // (both boards, flip counts, scores, flags) onto the undo stack
 // before the user makes a move. This enables rollback via undo.
 // Time Complexity :- O(n) (due to encode() which scans full board)
 // Space Complexity :- O(1) per call; O(k) total where k = undo history depth
    static void pushUndoState(){
        undoStack.push(new GameState(
                encode(userBoard),
                encode(aiBoard),
                userFlipCount,
                aiFlipCount,
                easyFirstMoveDone,
                userScore,
                aiScore,
                userUsedMask
        ));
    }

    // ===== NEW (UNDO) =====
 // UNDO LAST MOVE
 // Preamble :- Pops the most recent saved game state from the undo stack
 // and restores both boards, flip counts, scores and flags to that state.
 // If the stack is empty, notifies the user that there is nothing to undo.
 // Time Complexity :- O(n) (due to decode() which fills the full board)
 // Space Complexity :- O(1) (restores from already-stored state)
    static void undoLastMove(){
        if(undoStack.isEmpty()){
            System.out.println("Nothing to undo!");
            return;
        }
        GameState st = undoStack.pop();

        decode(st.userMask, userBoard);
        decode(st.aiMask, aiBoard);

        userFlipCount = st.userCount;
        aiFlipCount = st.aiCount;
        userScore = st.savedUserScore;
        aiScore = st.savedAiScore;
        userUsedMask = st.savedUserUsedMask;
        easyFirstMoveDone = st.easyDone;

        System.out.println("Undo successful ");
    }
    // BREADTH FIRST SEARCH (BFS)
    // Preamble :- Computes shortest sequence of moves
    // from initial board to GOAL state.
    // Each board configuration is treated as a node
    // and each flip is an edge.
    // Time Complexity :- O(n × 2^n)
    // Space Complexity :- O(2^n)
    
    static List<Integer> computeOptimalPathBFS(int[][] board){
        int start=encode(board);
        Queue<Integer> q=new LinkedList<>();
        Map<Integer,Integer> par=new HashMap<>();
        Map<Integer,Integer> mv=new HashMap<>();
        q.add(start);
        par.put(start,-1);
        while(!q.isEmpty()){
            int cur=q.poll();
            if(cur==GOAL) break;
            for(int i=0;i<rows*cols;i++){
                int nxt=flipMask(cur,i);
                if(!par.containsKey(nxt)){
                    par.put(nxt,cur);
                    mv.put(nxt,i);
                    q.add(nxt);
                }
            }
        }
        List<Integer> path=new ArrayList<>();
        int cur=GOAL;
        while(par.containsKey(cur)&&par.get(cur)!=-1){
            path.add(mv.get(cur));
            cur=par.get(cur);
        }
        Collections.reverse(path);
        return path;
    }
    // DIJKSTRA ALGORITHM
    // Preamble :- Finds the best next move using
    // shortest path search with priority queue.
    // Each board state is a node and each flip has cost 1.
    // Time Complexity :- O(n^2 × 2^n)
    // Space Complexity :- O(2^n)
    static int getOptimalMoveDijkstra(int[][] board) {

        int start = encode(board), goal = GOAL;
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a->a[1]));
        Map<Integer,Integer> dist = new HashMap<>();
        Map<Integer,Integer> parent = new HashMap<>();
        Map<Integer,Integer> move = new HashMap<>();

        pq.add(new int[]{start,0});
        dist.put(start,0);
        parent.put(start,-1);

        while (!pq.isEmpty()) {
            int[] cur = pq.poll();
            if (cur[0]==goal) break;

            for (int i=0;i<rows*cols;i++) {
                int next = flipMask(cur[0],i);
                int nc = dist.get(cur[0])+1;
                if (!dist.containsKey(next) || nc < dist.get(next)) {
                    dist.put(next,nc);
                    parent.put(next,cur[0]);
                    move.put(next,i);
                    pq.add(new int[]{next,nc});
                }
            }
        }

        int cur = goal;
        if(!parent.containsKey(goal))
            return 0;
        int prev = parent.get(cur);
        if (start == goal) return 0;
        if (prev == -1) return 0;
        while (parent.containsKey(prev) && parent.get(prev) != -1) {
            cur = prev;
            prev = parent.get(cur);
        }

        return move.getOrDefault(cur, 0);
    }
    // PRINT COUNTS
    // Preamble :- Displays total number of flips
    // performed by user and AI.
    // Time Complexity :- O(1)
    // Space Complexity :- O(1)
    static void printCounts(){
        System.out.println("User="+userFlipCount+" AI="+aiFlipCount);
    }
    // PRINT SIDE BY SIDE BOARD
    // Preamble :- Displays user board and AI board
    // side by side for comparison.
    // Time Complexity :- O(n)
    // Space Complexity :- O(1)
    static void printSideBySideBoard(int[][] u,int[][] a){
    	 int width = 3;   

    	    for(int i=0;i<rows;i++){

    	        for(int j=0;j<cols;j++)
    	            System.out.printf("%"+width+"d", u[i][j]);

    	        System.out.print("     ");

    	        for(int j=0;j<cols;j++)
    	            System.out.printf("%"+width+"d", a[i][j]);

    	        System.out.println();
    	    }
    }
    // PRINT PATH
    // Preamble :- Prints the optimal sequence of moves
    // required to reach goal state.
    // Time Complexity :- O(k) (k = number of moves)
    // Space Complexity :- O(1)
    static void printPath(List<Integer> path){
        for(int p:path)
            System.out.print("("+(p/cols)+","+(p%cols)+") ");
        System.out.println();
    }
    // SAVE WINNER
    // Preamble :- Appends winner details into winners.txt file.
    // Time Complexity :- O(1)
    // Space Complexity :- O(1)
    static void saveWinner() throws IOException{
        try(FileWriter fw=new FileWriter(FILE_NAME,true)){
            fw.write(userName+" | "+userId+" | "+levelName+"\n");
        }
    }
 // SHOW PREVIOUS WINNERS
 // Preamble :- Reads and prints all past winner records
 // from "winners.txt" line by line after each game ends.
 // If the file does not exist or is empty, it silently skips.
 // Time Complexity :- O(w) where w = number of winner records in file
 // Space Complexity :- O(1) (reads one line at a time)
    static void showPreviousWinners(){
        try(BufferedReader br=new BufferedReader(new FileReader(FILE_NAME))){
            String l; while((l=br.readLine())!=null)
                System.out.println(l);
        }catch(Exception e){}
    }
}
