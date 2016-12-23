package entity;

/**
 * Created by amareelez on 23.12.16.
 */
public class Move {
    private final int amazon_id;
    private final int row;
    private final int col;
    private final int arrow_row;
    private final int arrow_col;

    public Move(int amazon, int row, int col, int arrow_row, int arrow_col) {
        this.amazon_id = amazon;
        this.row = row;
        this.col = col;
        this.arrow_row = arrow_row;
        this.arrow_col = arrow_col;
    }

    /**
     * Return an int array representing the amazon's move.
     * //int[0] = amazon ID being moved;
     * //int[1] = row to move amazon to;
     * //int[2] = column to move amazon to;
     *
     * @return int[3]
     */
    public int[] getAmazonMove() {
        int[] thisMove = new int[3];
        thisMove[0] = amazon_id;
        thisMove[1] = row;
        thisMove[2] = col;

        return thisMove;
    }

    /**
     * Return an int array representing where to shoot an arrow
     * //int[0] = row of arrow
     * //int[1] = column of arrow
     *
     * @return int[2]
     */
    public int[] getArrowMove() {
        int[] arrowShot = new int[2];
        arrowShot[0] = arrow_row;
        arrowShot[1] = arrow_col;

        return arrowShot;
    }

    public int getAmazon_id() {
        return amazon_id;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public int getArrow_row() {
        return arrow_row;
    }

    public int getArrow_col() {
        return arrow_col;
    }

    public static class MoveAndBoard {
        private final GameBoard newBoard;
        private final Move newMove;

        public MoveAndBoard(GameBoard board, Move move) {
            this.newBoard = board;
            this.newMove = move;
        }

        public String toString() {
            String toReturn = "";
            toReturn +=
                "TestClass2.AI.Move TestClass2.AI.AmazonUnit ID: " + newMove.amazon_id + " to ROW: "
                    + newMove.row + "\tCOLUMN: " + newMove.col + "\n";
            toReturn +=
                "Shot Arrow to ROW: " + newMove.arrow_row + "\tCOLUMN: " + newMove.arrow_col + "\n";
            toReturn += "New Board:\n" + newBoard;
            return toReturn;
        }

        public GameBoard getNewBoard() {
            return newBoard;
        }

        public Move getNewMove() {
            return newMove;
        }
    }
}
