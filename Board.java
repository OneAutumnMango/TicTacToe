import java.util.HashMap;

public class Board {
    static final int DEFAULT_SIZE = 4;

    private State[][] board;
    public int size;

    private HashMap<String, Integer> map = new HashMap<>();
    private HashMap<String, Boolean> isActiveMap = new HashMap<>(); // used to speed up position eval -> check if you have to check a row/col/diag

    // used for the one move backup
    private State[][] backupBoard;
    private HashMap<String, Integer> backupMap = new HashMap<>();
    private HashMap<String, Boolean> backupIsActiveMap = new HashMap<>();
    private State backupVictor = State.Empty;

    public State victor = State.Empty;
    
    public Board() {
        this(DEFAULT_SIZE);
    }

    public Board(int size) {
        if (size < 0 || size > 30)
            throw new IllegalArgumentException("Illegal size: " + size);
        this.size = size;
        this.board = new State[size][size];
        this.backupBoard = new State[size][size];

        populateBoard();
        generateHashMaps();
    }

    // copy constructor
    Board(Board boardToCopy) {
        this.size = boardToCopy.size;
        this.board = new State[size][size];
        this.backupBoard = new State[size][size];

        for (int i = 0; i < this.size; i++) {
            this.board[i] = boardToCopy.board[i].clone();
            this.backupBoard[i] = boardToCopy.backupBoard[i].clone();
        }
        this.map = new HashMap<>(boardToCopy.map);
        this.isActiveMap = new HashMap<>(boardToCopy.isActiveMap);
        this.backupMap = new HashMap<>(boardToCopy.backupMap);
        this.backupIsActiveMap = new HashMap<>(boardToCopy.backupIsActiveMap);

        this.victor = boardToCopy.victor;
        this.backupVictor = boardToCopy.backupVictor;
    }

    private void populateBoard() {
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                board[i][j] = State.Empty;
            }
        }
    }

    private void generateHashMaps() {
        // rows and columns
        for (int i = 0; i < size; i++) {
            map.put("row"+i, 0);
            map.put("col"+i, 0);

            isActiveMap.put("row"+i, true);
            isActiveMap.put("col"+i, true);
        }

        // diagonals
        map.put("diag1", 0);
        map.put("diag2", 0);

        isActiveMap.put("diag1", true);
        isActiveMap.put("diag2", true);
    }

    private void generateOneMoveBackup() { // saves the state of the board 1 move ago
        for (int i = 0; i < size; i++) {
            backupBoard[i] = board[i].clone();
        }
        backupMap = new HashMap<>(map);
        backupIsActiveMap = new HashMap<>(isActiveMap);
        backupVictor = victor;
    }

    private void loadOneMoveBackup() { // loads the state of the board 1 move ago
        for (int i = 0; i < size; i++) {
            board[i] = backupBoard[i].clone();
        }
        map = backupMap;
        isActiveMap = backupIsActiveMap;
        victor = backupVictor;
    }

    private void updateHashmapValue(String key, int increment) {
        // skip if already inactive
        if (!isActiveMap.get(key)) return;

        if (map.get(key) * increment < 0) {  // if product < 0, then the signs are opposite -> can be inactivated
            isActiveMap.replace(key, false);
        }
        else {
            map.merge(key, increment, Integer::sum);
        }
    }

    private boolean isActiveKey(String key) {
        return isActiveMap.get(key);
    }

    private void updateHashmap(int i, int j, State s) {
        String row = "row"+i;
        String col = "col"+j;

        int increment = s.getValue();

        // increment each value by the given value of s
        updateHashmapValue(row, increment);
        updateHashmapValue(col, increment);

        // check horizontal and vertical
        if (map.get(row) == increment * size || map.get(col) == increment * size)
            declareVictor(s);


        // check diagonals
        if (i == j) { // diag1
            updateHashmapValue("diag1", increment);

            if (map.get("diag1") == increment * size)
                declareVictor(s);
        } 
        if (i + j == size - 1) { // diag2
            updateHashmapValue("diag2", increment);

            if (map.get("diag2") == increment * size)
                declareVictor(s);
        }
    }

    public void play(int i, int j, State s) {
        if (!isEmpty(i, j)) return; // ignore if already played

        generateOneMoveBackup();

        this.set(i, j, s);
        updateHashmap(i, j, s);
    }

    /**
     * Regenerates maps - use {@code unplay()} to unplay the last move
     * @param i x-coordinate (column) of move to unplay
     * @param j y-coordinate (row) of move to unplay
     */
    public void unplay(int i, int j) {
        if (isEmpty(i, j)) return; // ignore if not already played
        
        this.set(i, j, State.Empty);
        regenerateHashmaps();
    }

    /**
     * Unplays the last move
     */
    public void unplay() {
        loadOneMoveBackup();
    }

    public boolean isEmpty(int i, int j) {
        return this.get(i, j) == State.Empty;
    }

    public boolean isValidMove(String move) {
        int[] coords = moveToCoords(move);
        int i = coords[0], j = coords[1];
        if (i < 0 || j < 0 || i > size - 1 || j > size - 1)
            return false;
        return isEmpty(i, j);
    }

    public State get(int i, int j) {
        return board[i][j];
    }

    private void set(int i, int j, State s) {
        board[i][j] = s;
    }


    public int evaluatePosition() {
        int value = 0;

        int groupingWeight = 3;  // bonus per one in group
        for (String key: map.keySet()) {
            if (isActiveKey(key)) {
                int group = map.get(key);
                // if (Math.abs(group) == size) value += 100*Integer.signum(group);  // sign of group * 1k -> heavily prioritise wins (used to return instead of incrememnt value idk)
                if (Math.abs(group) == size) return (Integer.signum(group) > 0) ? Integer.MAX_VALUE : Integer.MIN_VALUE;

                // if (group > 1)
                value += group * groupingWeight;    
            }
        }


        // place nodes in well opportunistic squares (with lower weightings as it later becomes overshadowed)
        int[] positionalWeights = {1, 4, 10}; // -> 2, 3, 4 winning lines a position holds
        int noOfLines;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                noOfLines = 0;
                if (i == j) noOfLines++;
                if (i + j == size - 1) noOfLines++;
                value += positionalWeights[noOfLines] * get(i, j).getValue();
            }
        }
        return value;
    }

    // get rid of errorrs 
    private void declareVictor(State s) {
        victor = s;
    }

    public boolean isWin() {
        return victor!=null && victor != State.Empty;
    }

    /**
     * @param ch between 0 - 9 and a - z (lowercase)
     * @return Conversion of char to int: '1' -> 1, 'b' -> 2
     */
    private int charToInt(char ch) {
        return (ch < 'a') ? (ch - '0') : (ch - 'a' + 1);
    }

    public int[] moveToCoords(String move) {
        int i = charToInt(move.charAt(1)) - 1;
        int j = charToInt(move.charAt(0)) - 1;

        return new int[]{i, j};
    }


    public void setBoard(State[][] newBoard) {
        size = newBoard.length;
        this.board = new State[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) { 
                board[i][j] = newBoard[i][j];
            }
        }
        regenerateHashmaps();
    }

    public void regenerateHashmaps() {
        generateHashMaps(); // reset hashmaps
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) { 
                if(!isEmpty(i, j))
                    updateHashmap(i, j, board[i][j]);
            }
        }
    }

    public int noOfMovesLeft() {
        int noOfMovesLeft = 0;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                noOfMovesLeft += (isEmpty(i, j)) ? 1 : 0; // +1 move position is empty
            }
        }
        return noOfMovesLeft;
    }

    public void displayBoard() {
        System.out.printf("\n   ");
        for (int i = 0; i < size; i++) {
            System.out.printf("%c ", 'a' + i);
        }
        System.out.println();

        for (int i = 0; i < size; i++) {
            // System.out.printf("%3d ", i + 1);
            System.out.printf("%2c ", '1' + i);
            for (int j = 0; j < size; j++) {
                System.out.printf("%s ", board[i][j]);
            }
            System.out.println();
        }
    }



    public static void main(String[] args) {
        Board b = new Board();

        char[][] tmp =  { {'x','_','o','x'},
                          {'x','_','_','o'},
                          {'o','x','_','o'},
                          {'x','o','_','_'} };

        char[][] tmp3 =  { {'_','_','x'},
                           {'o','x','_'},
                           {'o','x','o'} };
        b.setBoard(bot.boardBuilder(tmp3, 3));

        b.displayBoard();
        System.out.println("eval = "+b.evaluatePosition());

        b.play(0, 1, State.X);
        System.out.println(b.isWin() ? "w" : "nw");
        b.displayBoard();
        System.out.println("eval = "+b.evaluatePosition());


    }    
}