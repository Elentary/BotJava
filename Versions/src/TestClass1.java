import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static java.lang.Math.*;

/**
 * Created by amare on 09.01.2016.
 */
public class TestClass1 {

    private static double mark;

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
        int[][] array = new int[10][10];
        AI.AmazonUnit[] all_amazonUnits = new AI.AmazonUnit[8];

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
                    all_amazonUnits[our_index] = new AI.AmazonUnit(i, j, our_index);
                    our_index++;
                } else if (array[i][j] == 3 - PlayerID) {
                    array[i][j] = 1;
                    all_amazonUnits[opp_index] = new AI.AmazonUnit(i, j, opp_index);
                    opp_index++;
                } else if (array[i][j] == -1) {
                    array[i][j] = 2;
                }
            }
        }


        AI.GameSpace Board = new AI.GameSpace(array, all_amazonUnits);
        AI Brain = new AI(Board);
        AI.Edge BestMove = Brain.getNextMove(Board);

        /*BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(System.out));
        wr.write(String.format("%d %d\n%d %d\n%d %d", all_amazonUnits[BestMove.newMovement.num].row, all_amazonUnits[BestMove.newMovement.num].col,
                BestMove.newMovement.row, BestMove.newMovement.col,
                BestMove.newMovement.arrow_row, BestMove.newMovement.arrow_col));*/
        System.out.println(String.format("%d %d", all_amazonUnits[BestMove.newMove.amazonID].row,
                all_amazonUnits[BestMove.newMove.amazonID].column));
        System.out.println(String.format("%d %d", BestMove.newMove.row,
                BestMove.newMove.col));
        System.out.println(String.format("%d %d", BestMove.newMove.arrow_row,
                BestMove.newMove.arrow_col));
        System.out.println(mark);
    }

    public static class AI {

        GameSpace board = null;

        public AI(GameSpace b) {
            this.board = b;
        }

        public Edge getNextMove(GameSpace b) {
            this.board = b;
            return GreedySearch();
        }

        private Edge GreedySearch() {

            AmazonUnit probe = BlackAmazonCheck();

            ArrayList<Edge> result = new ArrayList<Edge>();

            if (probe != null) {

                result.addAll(board.getPossibleMoves(probe.ID));
                if (result.size() == 0) {
                    for (int i = 0; i < 4; i++) {
                        result.addAll(board.getPossibleMoves(i));
                    }
                }
            } else {
                result = new ArrayList<Edge>();
                for (int i = 0; i < 4; i++) {
                    result.addAll(board.getPossibleMoves(i));
                }
            }
            return advancedEvaluation(result);
        }

        private AmazonUnit BlackAmazonCheck() {
            AmazonUnit black = null;
            for (int j = 0; j < 4; j++) {
                if (board.getOurFreedom(board.amazonUnits[j]) <= 2) {
                    black = board.amazonUnits[j];
                }
            }
            return black;
        }

        private Edge advancedEvaluation(ArrayList<Edge> moves) {
            ArrayList<Double> evaluations = new ArrayList<Double>(moves.size());
            for (int i = 0; i < moves.size(); i++) {
                evaluations.add(i, new Double(new Evaluation(moves.get(i).newBoard).Evaluate()));
            }

            mark = Collections.max(evaluations);
            return moves.get(evaluations.indexOf(Collections.max(evaluations)));
        }

        public static class AmazonUnit {
            public int row;
            public int column;
            public int ID;

            public AmazonUnit(int row, int column, int id) {
                this.row = row;
                this.column = column;
                this.ID = id;
            }

            public void updateCoords(int x, int y) {
                this.row = x;
                this.column = y;
            }
        }

        public static class GameSpace {

            public int[][] board;

            public int[][][] BFSQueenDistances = new int[8][10][10], BFSKingDistances = new int[8][10][10];

            public AmazonUnit[] amazonUnits;

            public GameSpace(int[][] originBoard, AmazonUnit[] amazons) {
                this.board = originBoard;
                this.amazonUnits = amazons;
            }

            public void moveAmazon(int amazon, int row, int column) {
                board[amazonUnits[amazon].row][amazonUnits[amazon].column] = 0;
                board[row][column] = (amazon < 4) ? -1 : 1;
                amazonUnits[amazon].updateCoords(row, column);
            }

            public void shootArrow(int x, int y) {
                board[x][y] = 2;
            }

            public GameSpace Clone() {
                int[][] newBoard = new int[10][10];
                AmazonUnit[] newAmazonUnits = new AmazonUnit[8];

                for (int i = 0; i < 10; i++) {
                    System.arraycopy(this.board[i], 0, newBoard[i], 0, 10);
                }
                for (int i = 0; i < 8; i++) {
                    newAmazonUnits[i] = new AmazonUnit(this.amazonUnits[i].row, this.amazonUnits[i].column, this.amazonUnits[i].ID);
                }

                return new GameSpace(newBoard, newAmazonUnits);
            }

            public ArrayList<Edge> getPossibleMoves(int amazonId) {
                ArrayList<Edge> children = new ArrayList<Edge>();

                for (int i = this.amazonUnits[amazonId].column + 1; i < 10; i++) {
                    if (this.board[this.amazonUnits[amazonId].row][i] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, this.amazonUnits[amazonId].row, i);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, this.amazonUnits[amazonId].row, i, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, this.amazonUnits[amazonId].row, i);
                        }
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].row + 1; i < 10; i++) {
                    if (this.board[i][this.amazonUnits[amazonId].column] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, i, this.amazonUnits[amazonId].column);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, i, this.amazonUnits[amazonId].column, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, i, this.amazonUnits[amazonId].column);
                        }
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column - 1; i >= 0; i--) {
                    if (this.board[this.amazonUnits[amazonId].row][i] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, this.amazonUnits[amazonId].row, i);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, this.amazonUnits[amazonId].row, i, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, this.amazonUnits[amazonId].row, i);
                        }
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].row - 1; i >= 0; i--) {
                    if (this.board[i][this.amazonUnits[amazonId].column] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, i, this.amazonUnits[amazonId].column);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, i, this.amazonUnits[amazonId].column, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, i, this.amazonUnits[amazonId].column);
                        }
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column + 1, j = this.amazonUnits[amazonId].row + 1; i < 10 && j < 10; i++, j++) {
                    if (this.board[j][i] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, j, i, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column - 1, j = this.amazonUnits[amazonId].row + 1; i >= 0 && j < 10; i--, j++) {
                    if (this.board[j][i] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, j, i, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column - 1, j = this.amazonUnits[amazonId].row - 1; i >= 0 && j >= 0; i--, j--) {
                    if (this.board[j][i] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, j, i, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column + 1, j = this.amazonUnits[amazonId].row - 1; i < 10 && j >= 0; i++, j--) {
                    if (this.board[j][i] == 0) {
                        GameSpace child = this.Clone();
                        child.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = child.getPossibleArrows(amazonId);
                        for (int[] arrow : arrows) {
                            child.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(amazonId, j, i, arrow[0], arrow[1]);
                            children.add(new Edge(child, newMove));
                            child = this.Clone();
                            child.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }

                return children;
            }

            private ArrayList<int[]> getPossibleArrows(int amazonId) {
                ArrayList<int[]> arrows = new ArrayList<int[]>();


                for (int i = this.amazonUnits[amazonId].column + 1; i < 10; i++) {
                    if (this.board[this.amazonUnits[amazonId].row][i] == 0) {
                        arrows.add(new int[]{this.amazonUnits[amazonId].row, i});
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].row + 1; i < 10; i++) {
                    if (this.board[i][this.amazonUnits[amazonId].column] == 0) {
                        arrows.add(new int[]{i, this.amazonUnits[amazonId].column});
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column - 1; i >= 0; i--) {
                    if (this.board[this.amazonUnits[amazonId].row][i] == 0) {
                        arrows.add(new int[]{this.amazonUnits[amazonId].row, i});
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].row - 1; i >= 0; i--) {
                    if (this.board[i][this.amazonUnits[amazonId].column] == 0) {
                        arrows.add(new int[]{i, this.amazonUnits[amazonId].column});
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column + 1, j = this.amazonUnits[amazonId].row + 1; i < 10 && j < 10; i++, j++) {
                    if (this.board[j][i] == 0) {
                        arrows.add(new int[]{j, i});
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column - 1, j = this.amazonUnits[amazonId].row + 1; i >= 0 && j < 10; i--, j++) {
                    if (this.board[j][i] == 0) {
                        arrows.add(new int[]{j, i});
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column - 1, j = this.amazonUnits[amazonId].row - 1; i >= 0 && j >= 0; i--, j--) {
                    if (this.board[j][i] == 0) {
                        arrows.add(new int[]{j, i});
                    } else break;
                }


                for (int i = this.amazonUnits[amazonId].column + 1, j = this.amazonUnits[amazonId].row - 1; i < 10 && j >= 0; i++, j--) {
                    if (this.board[j][i] == 0) {
                        arrows.add(new int[]{j, i});
                    } else break;
                }

                return arrows;
            }

            public int getOurFreedom(AmazonUnit A) {
                int sum = 0;
                try {
                    if (board[A.row][A.column + 1] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (board[A.row][A.column - 1] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (board[A.row - 1][A.column - 1] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (board[A.row + 1][A.column - 1] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (board[A.row - 1][A.column + 1] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (board[A.row + 1][A.column + 1] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (board[A.row - 1][A.column] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (board[A.row + 1][A.column] == 0) {
                        sum++;
                    }
                } catch (Exception ex) {

                }
                return sum;
            }

        }

        public static class Move {
            int amazonID;
            int row;
            int col;
            int arrow_row;
            int arrow_col;

            public Move(int amazon, int row, int col, int arrow_row, int arrow_col) {
                this.amazonID = amazon;
                this.row = row;
                this.col = col;
                this.arrow_row = arrow_row;
                this.arrow_col = arrow_col;
            }

        }

        public static class Edge {
            GameSpace newBoard;
            Move newMove;

            public Edge(GameSpace board, Move move) {
                this.newBoard = board;
                this.newMove = move;
            }
        }

        /**
         * Created by amare on 12.01.2016.
         */
        public static class Evaluation {

            private GameSpace board;

            public Evaluation(GameSpace b) {
                this.board = b;
            }

            private void BFSQueen(AmazonUnit amazon) {
                boolean used[][] = new boolean[10][10];
                int[][] dist = new int[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        used[i][j] = false;
                        dist[i][j] = Integer.MAX_VALUE;
                    }
                }
                dist[amazon.row][amazon.column] = 0;
                used[amazon.row][amazon.column] = true;
                LinkedList<Pos> q = new LinkedList<Pos>();
                q.add(new Pos(amazon.row, amazon.column));
                while (!q.isEmpty()) {
                    Pos current = q.getFirst();
                    q.removeFirst();
                    int dx = current.x + 1;
                    int dy = current.y;
                    while (dx < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx++;
                    }
                    dx = current.x - 1;
                    while (dx >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx--;
                    }
                    dx = current.x;
                    dy = current.y + 1;
                    while (dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dy++;
                    }
                    dy = current.y - 1;
                    while (dy >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dy--;
                    }
                    dx = current.x + 1;
                    dy = current.y + 1;
                    while (dx < 10 && dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx++;
                        dy++;
                    }
                    dx = current.x - 1;
                    dy = current.y - 1;
                    while (dx >= 0 && dy >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx--;
                        dy--;
                    }
                    dx = current.x + 1;
                    dy = current.y - 1;
                    while (dx < 10 && dy >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx++;
                        dy--;
                    }
                    dx = current.x - 1;
                    dy = current.y + 1;
                    while (dx >= 0 && dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx--;
                        dy++;
                    }
                }
                board.BFSQueenDistances[amazon.ID] = dist;
            }

            private void BFSKing(AmazonUnit amazon) {
                boolean used[][] = new boolean[10][10];
                int[][] dist = new int[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        used[i][j] = false;
                        dist[i][j] = Integer.MAX_VALUE;
                    }
                }
                dist[amazon.row][amazon.column] = 0;
                used[amazon.row][amazon.column] = true;
                LinkedList<Pos> q = new LinkedList<Pos>();
                q.add(new Pos(amazon.row, amazon.column));
                while (!q.isEmpty()) {
                    Pos current = q.getFirst();
                    q.removeFirst();
                    int dx = current.x + 1;
                    int dy = current.y;
                    if (dx < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x - 1;
                    if (dx >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x;
                    dy = current.y + 1;
                    if (dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dy = current.y - 1;
                    if (dy >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x + 1;
                    dy = current.y + 1;
                    if (dx < 10 && dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x - 1;
                    dy = current.y - 1;
                    if (dx >= 0 && dy >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x + 1;
                    dy = current.y - 1;
                    if (dx < 10 && dy >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x - 1;
                    dy = current.y + 1;
                    if (dx >= 0 && dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                }
                board.BFSKingDistances[amazon.ID] = dist;
            }

            public double Evaluate() {
                for (int i = 0; i < 8; i++) {
                    BFSQueen(board.amazonUnits[i]);
                    BFSKing(board.amazonUnits[i]);
                }

                double evaluation = 0;
                double advancement = GetAdvancement();
                evaluation += sigma1(advancement) * QueenDistance();
                evaluation += sigma2(advancement) * KingDistance();
                evaluation += sigma3(advancement) * OverallMobility();
                return evaluation;
            }

            private double OverallMobility() {
                double mobility = 0;
                for (int i = 4; i < 8; i++) {
                    mobility += Penalty(board.amazonUnits[i]);
                }
                for (int i = 0; i < 4; i++) {
                    mobility -= Penalty(board.amazonUnits[i]);
                }
                return mobility;
            }

            private double Penalty(AmazonUnit amazon) {
                return 30 / (5.0 + MobilityOfAmazon(amazon));
            }

            private double MobilityOfAmazon(AmazonUnit amazon) {
                double mobility = 0;
                if (amazon.ID < 4) {
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            long Player2Distance = Long.MAX_VALUE;
                            for (int p = 4; p < 8; p++) {
                                Player2Distance = min(Player2Distance, board.BFSQueenDistances[p][i][j]);
                            }
                            if (board.BFSQueenDistances[amazon.ID][i][j] == 1 && Player2Distance != Long.MAX_VALUE) {
                                mobility += pow(2, -1 * board.BFSKingDistances[amazon.ID][i][j]) * SquaresAround(i, j);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            long Player1Distance = Long.MAX_VALUE;
                            for (int p = 0; p < 4; p++) {
                                Player1Distance = min(Player1Distance, board.BFSQueenDistances[p][i][j]);
                            }
                            if (board.BFSQueenDistances[amazon.ID][i][j] == 1 && Player1Distance != Long.MAX_VALUE) {
                                mobility += pow(2, -1 * board.BFSKingDistances[amazon.ID][i][j]) * SquaresAround(i, j);
                            }
                        }
                    }
                }
                return mobility;

            }

            private int SquaresAround(int x, int y) {
                int squares = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        try {
                            if ((i != 0 || j != 0) && board.board[x + i][y + j] == 0) {
                                squares++;
                            }
                        } catch (Exception ex) {

                        }

                    }
                }
                return squares;
            }

            private double sigma1(double advancement) {
                return 1;
            }

            private double sigma2(double advancement) {
                if (advancement > 40) {
                    return 1;
                } else {
                    return advancement / 40;
                }
            }

            private double sigma3(double advancement) {
                if (advancement > 40) {
                    return 0.8;
                } else {
                    return 0.8 * advancement / 40;
                }
            }

            private double GetAdvancement() {
                double advancement = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        long Player1Distance = Long.MAX_VALUE;
                        long Player2Distance = Long.MAX_VALUE;
                        for (int p = 0; p < 4; p++) {
                            Player1Distance = min(Player1Distance, board.BFSQueenDistances[p][i][j]);
                        }
                        for (int p = 4; p < 8; p++) {
                            Player2Distance = min(Player2Distance, board.BFSQueenDistances[p][i][j]);
                        }
                        if (Player1Distance != Long.MAX_VALUE && Player2Distance != Long.MAX_VALUE) {
                            advancement += pow(2, -1 * abs(Player1Distance - Player2Distance));
                        }
                    }
                }
                return advancement;
            }

            private long KingDistance() {
                long Distance = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (board.board[i][j] == 0) {
                            Distance += GetKingMovesForSquare(i, j);
                        }
                    }
                }
                return Distance;
            }

            private long GetKingMovesForSquare(int x, int y) {
                long Player1Distance = Long.MAX_VALUE;
                long Player2Distance = Long.MAX_VALUE;
                for (int i = 0; i < 4; i++) {
                    Player1Distance = min(Player1Distance, board.BFSKingDistances[i][x][y]);
                }
                for (int i = 4; i < 8; i++) {
                    Player2Distance = min(Player2Distance, board.BFSKingDistances[i][x][y]);
                }
                if (Player1Distance < Player2Distance) {
                    return 1;
                } else if (Player2Distance < Player1Distance) {
                    return -1;
                } else
                    return 0;
            }

            private long QueenDistance() {
                long Distance = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (board.board[i][j] == 0) {
                            Distance += GetQueenMovesForSquare(i, j);
                        }
                    }
                }
                return Distance;
            }

            private long GetQueenMovesForSquare(int x, int y) {
                long Player1Distance = Long.MAX_VALUE;
                long Player2Distance = Long.MAX_VALUE;
                for (int i = 0; i < 4; i++) {
                    Player1Distance = min(Player1Distance, board.BFSQueenDistances[i][x][y]);
                }
                for (int i = 4; i < 8; i++) {
                    Player2Distance = min(Player2Distance, board.BFSQueenDistances[i][x][y]);
                }
                if (Player1Distance < Player2Distance) {
                    return 1;
                } else if (Player2Distance < Player1Distance) {
                    return -1;
                } else
                    return 0;
            }

            private static class Pos {

                private int x, y;

                public Pos(int x, int y) {
                    this.x = x;
                    this.y = y;
                }
            }

        }
    }
}
