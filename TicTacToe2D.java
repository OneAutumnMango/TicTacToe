


enum State {
    Empty, X, O;

    public String toString() {
        if (this == Empty)
            return "_";
        return super.toString();
    }

    public State next() {
        if (this == X)
            return O;
        if (this == O)
            return X;
        return Empty; // Empty has no next state
    }

    public int getValue() {
        switch (this) {
            case X:
                return 1;
            case O:
                return -1;
            default:
                return 0;
        }
    }

    public int getPlayerIndex() {
        switch (this) {
            case X:
                return 0;
            case O:
                return 1;
            default:
                return -1;
        }
    }
}

public class TicTacToe2D {
    static final int DEFAULT_SIZE = 4;
    static final String DEFAULT_CONFIG = "h,h"; // default human vs human

    private Board board;

    private io[] players = new io[2];

    public TicTacToe2D() {
        this(DEFAULT_CONFIG, DEFAULT_SIZE);
    }

    public TicTacToe2D(Integer size) {
        this(DEFAULT_CONFIG, size);
    }

    public TicTacToe2D(String config) {
        this(config, DEFAULT_SIZE); 
    }
    
    /**
     * @param config '%s,%s' -> options: h, b 
     * @param size valid for 0 <= size < 31
     */
    public TicTacToe2D(String config, Integer size) {
        if (size < 0 || size > 30)
            throw new IllegalArgumentException("Illegal size: " + size);
        this.board = new Board(size);

        loadConfig(config);
 
    }



    private void loadConfig(String config) { 
        int optionCount = 2; // current allowed config options (p1, p2)

        String[] cfg = config.split(",");
        if (cfg.length > optionCount)
            throw new IllegalArgumentException("Too many arguments in config: '" + config + "'");

        System.out.println("config read: " + config);

        State playerState = State.X;
        for (int i = 0; i < cfg.length; i++) {
            switch (cfg[i]) {
                case "h" -> players[i] = new player();
                case "b" -> players[i] = new bot(playerState);
            }
            playerState.next();
        }
    }

   

    private void declareVictor(State s) {
        board.displayBoard();
        System.out.printf("%s is victorious\n", s);
    }

    private void declareDraw() {
        board.displayBoard();
        System.out.println("Draw!");
    }

    public void updateBot(io io) {
        if (io instanceof bot) {
            System.out.println("Updating bot...");
            bot bot = (bot)io;
            bot.update(new Board(board));
        }
    }

    public void play() {
        int moveCount = 0;
        State player = State.X;

        while (moveCount < board.size * board.size) { // while there are empty squares
            board.displayBoard();
            System.out.printf("Current player: '%s'\n", player);

            io p = players[player.getPlayerIndex()];
            if (p instanceof bot) {
                updateBot(p);
            }
            
            String move = getMove(p);

            move(move, player);
            if (board.victor != State.Empty) break;

            player = player.next();
            moveCount++;
        }
        if (board.victor == null) {
            declareDraw();
        }
        else {
            declareVictor(player); 
        }

    }

    private String getMove(io io) {
        String move;
        do {
            move = io.getMove();
        } while (!board.isValidMove(move));

        return move;
    }

    private void move(String move, State s) { // move = [a-d][1-4], eg 'a1' 'c2' 'd1'
        int[] coords = board.moveToCoords(move);
        board.play(coords[0], coords[1], s);
    }


    public static void main(String[] args) {
        TicTacToe2D ttt = new TicTacToe2D("h,b",3);
        ttt.play();
    }
}
