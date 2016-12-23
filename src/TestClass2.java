import entity.Amazon;
import entity.GameBoard;
import entity.Move;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStreamReader;

/**
 * Created by amare on 09.01.2016.
 */
public class TestClass2 {

    static long time;

    public static void main(String[] args) throws Exception {
        time = System.currentTimeMillis();
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader br =
            new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
        int[][] array = new int[10][10];
        Amazon[] all_amazons = new Amazon[8];

        for (int i = 0; i < 10; i++) {
            String line = br.readLine();
            String[] chars = line.split(" ");
            for (int j = 0; j < chars.length; j++)
                array[i][j] = Integer.parseInt(chars[j]);
        }

        String line = br.readLine();
        int PlayerID = Integer.parseInt(line);

        int our_index = 0, opp_index = 4;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (array[i][j] == PlayerID) {
                    array[i][j] = -1;
                    all_amazons[our_index] = new Amazon(i, j, our_index);
                    our_index++;
                } else if (array[i][j] == 3 - PlayerID) {
                    array[i][j] = 1;
                    all_amazons[opp_index] = new Amazon(i, j, opp_index);
                    opp_index++;
                } else if (array[i][j] == -1) {
                    array[i][j] = 2;
                }
            }
        }



        GameBoard Board = new GameBoard(array, all_amazons);
        AI Brain = new AI(Board);
        Move.MoveAndBoard BestMove = Brain.getNextMove(Board);

        /*BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(System.out));
        wr.write(String.format("%d %d\n%d %d\n%d %d", all_amazons[BestMove.newMove.amazonID].row, all_amazons[BestMove.newMove.amazonID].column,
                BestMove.newMove.row, BestMove.newMove.col,
                BestMove.newMove.arrow_row, BestMove.newMove.arrow_col));*/
        System.out.println(String
            .format("%d %d", all_amazons[BestMove.getNewMove().getAmazon_id()].row,
                all_amazons[BestMove.getNewMove().getAmazon_id()].column));
        System.out.println(
            String.format("%d %d", BestMove.getNewMove().getRow(), BestMove.getNewMove().getCol()));
        System.out.println(String.format("%d %d", BestMove.getNewMove().getArrow_row(),
            BestMove.getNewMove().getArrow_col()));
    }

}
