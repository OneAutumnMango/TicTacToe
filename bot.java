public class bot implements io {
    Board board;
    private State state = State.X; // default state

    public bot() {
        this.board = new Board();
    }

    public bot(int size) {
        this.board = new Board(size);
    }

    public bot(State s) {
        this.state = s;
    }

    public String getMove() {
        // System.out.println("Position eval: " + minimax(board, lines, 7, true));

        int bestValue = Integer.MIN_VALUE;
        int value;
        String move = "";
        for (int i = 0; i < board.size; i++) {
            for (int j = 0; j < board.size; j++) {
                if (board.isEmpty(i, j)) { // if possible move
                    board.play(i, j, this.state);
                    value = minimax(new Board(board), 1, false);
                    board.unplay();

                    if (value > bestValue) {
                        move = String.valueOf((char)('a' + j)) + String.valueOf((char)('1' + i));
                        bestValue = value;

                        // System.out.println("new best move: " + move + " value: " + bestValue);
                    }
                    // System.out.println("current move: "+String.valueOf((char)('a' + j)) + String.valueOf((char)('1' + i)) + " value: " + value);
                }
            }
        }
        System.out.println("bot playing: " + move);
        return move;
    }

    public void update(Board board) {
        System.out.println("Recieved update");
        this.board = board;
    }

    private int minimax(Board board, int depth,     boolean isMaximising) {
        if (depth==0 || board.noOfMovesLeft()==0 || board.isWin()) 
            return board.evaluatePosition();

        int value = 0;
        if (isMaximising) {
            value = Integer.MIN_VALUE;

            for (int i = 0; i < this.board.size; i++) {
                for (int j = 0; j < this.board.size; j++) {
                    if (board.isEmpty(i, j)) { // if possible move
                        board.play(i, j, this.state);
                        value = Math.max(value, minimax(new Board(board), depth-1, false));
                        board.unplay();
                    }
                }
            }
            return value;
        }
        else { //minimising player
            value = Integer.MAX_VALUE;

            for (int i = 0; i < this.board.size; i++) {
                for (int j = 0; j < this.board.size; j++) {
                    if (board.isEmpty(i, j)) { // if possible move
                        board.play(i, j, this.state.next());
                        value = Math.min(value, minimax(new Board(board), depth-1, true));
                        board.unplay();
                    }
                }
            }
            return value;
        }
    }


    public static void main(String[] args) {
        bot b = new bot();
        char[][] tmp4 =  { {'x','o','o','x'},
                           {'x','x','o','o'},
                           {'o','x','_','o'},
                           {'x','o','_','_'} };
        char[][] tmp3 =  { {'_','_','x'},
                           {'o','x','_'},
                           {'o','x','o'} };
        b.board.setBoard(boardBuilder(tmp3, 3));


        b.board.displayBoard();
        System.out.println("---------initial board ^^ ---------");
        System.out.println(b.getMove());
    

        // System.out.println(b.board.evaluatePosition());

        // b.minimax(b.board, 2, true);
        // System.out.println("victor: " + b.board.victor);
        
    }

    public static State[][] boardBuilder(char[][] board, int size) {
        State[][] out = new State[size][size];
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                switch (board[i][j]) {
                    case 'x' -> out[i][j] = State.X;
                    case 'o' -> out[i][j] = State.O;
                    case '_' -> out[i][j] = State.Empty;
                }
            }
        }
        return out;
    }
}
