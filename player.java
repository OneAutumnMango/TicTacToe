import java.util.Scanner;


public class player implements io {
    private State[][] board;
    private Scanner sc = new Scanner(System.in);

    public String getMove() {
        String move;
        System.out.println("Please enter a valid move (eg. a1):");
        move = sc.next();

        return move;
    }

    public void updateBoard(State[][] newBoard) {
        this.board = newBoard;
    }
}
