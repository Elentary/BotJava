import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

import static java.lang.Math.*;

public class TestClass {

    public static void main(String[] args) throws Exception {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
        int[][] inp = new int[10][10];
        Solver.Amazon[] all_amazons = new Solver.Amazon[8];

        for (int i = 0; i < 10; i++) {
            String line = bufferedReader.readLine();
            String[] chars = line.split(" ");
            for (int j = 0; j < chars.length; j++)
                inp[i][j] = Integer.parseInt(chars[j]);
        }

        String line = bufferedReader.readLine();
        int PlayerID = Integer.parseInt(line);

        int our_index = 0, opp_index = 4;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (inp[i][j] == PlayerID) {
                    inp[i][j] = -1;
                    all_amazons[our_index] = new Solver.Amazon(i, j, our_index);
                    our_index++;
                } else if (inp[i][j] == 3 - PlayerID) {
                    inp[i][j] = 1;
                    all_amazons[opp_index] = new Solver.Amazon(i, j, opp_index);
                    opp_index++;
                } else if (inp[i][j] == -1) {
                    inp[i][j] = 2;
                }
            }
        }


        Solver.Field Board = new Solver.Field(inp, all_amazons);
        Solver Brain = new Solver(Board);
        Solver.MBState BestMove = Brain.solve(Board);

        System.out.print(all_amazons[BestMove.newMove.amazonNum].row);
        System.out.print(" ");
        System.out.println(all_amazons[BestMove.newMove.amazonNum].col);
        System.out.print(BestMove.newMove.row);
        System.out.print(" ");
        System.out.println(BestMove.newMove.col);
        System.out.print(BestMove.newMove.arrow_row);
        System.out.print(" ");
        System.out.println(BestMove.newMove.arrow_col);
    }

    public static class Solver {

        Field board = null;

        public Solver(Field b) {
            this.board = b;
        }

        public MBState solve(Field f) {
            this.board = f;
            return Search();
        }

        private MBState Search() {

            Amazon checkResult = CheckForImmobility();

            ArrayList<MBState> candidates = new ArrayList<MBState>();

            if (checkResult == null) {
                candidates = new ArrayList<MBState>();
                for (int i = 0; i < 4; i++) candidates.addAll(board.getMoves(i));
            } else {
                candidates.addAll(board.getMoves(checkResult.num));
                if (candidates.size() == 0) for (int i = 0; i < 4; i++) candidates.addAll(board.getMoves(i));
            }
            return MBChoosing(candidates);
        }

        private Amazon CheckForImmobility() {
            Amazon amazonInDanger = null;
            for (int i = 0; i < 4; i++) {
                if (board.amazonLiberty(board.amazons[i]) > 2) {
                    continue;
                }
                amazonInDanger = board.amazons[i];
            }
            return amazonInDanger;
        }

        private MBState MBChoosing(ArrayList<MBState> MBs) {
            ArrayList<Double> approximations = new ArrayList<Double>(MBs.size());
            for (int i = 0; i < MBs.size(); i++)
                approximations.add(i, new Function(MBs.get(i).newBoard).Evaluate());

            return MBs.get(approximations.indexOf(Collections.max(approximations)));
        }

        public static class Amazon {
            public int row;
            public int col;
            public int num;

            public Amazon(int row, int col, int id) {
                this.row = row;
                this.col = col;
                this.num = id;
            }

            public void update(int x, int y) {
                this.row = x;
                this.col = y;
            }
        }

        public static class Field {

            public int[][] field;

            public int[][][] QDistances = new int[8][10][10];
            public int[][][] KDistances = new int[8][10][10];

            public Amazon[] amazons;

            public Field(int[][] initialState, Amazon[] amazons) {
                this.field = initialState;
                this.amazons = amazons;
            }

            public void moveAmazon(int amazon, int row, int col) {
                field[amazons[amazon].row][amazons[amazon].col] = 0;
                if (amazon < 4) field[row][col] = -1;
                else field[row][col] = 1;
                amazons[amazon].update(row, col);
            }

            public void shootArrow(int x, int y) {
                field[x][y] = 2;
            }

            public Field ConstructCopy() {
                int[][] newBoard = new int[10][10];
                Amazon[] newAmazons = new Amazon[8];

                for (int i = 0; i < 10; i++) System.arraycopy(this.field[i], 0, newBoard[i], 0, 10);
                for (int i = 0; i < 8; i++)
                    newAmazons[i] = new Amazon(this.amazons[i].row, this.amazons[i].col, this.amazons[i].num);

                return new Field(newBoard, newAmazons);
            }

            public ArrayList<MBState> getMoves(int num) {
                ArrayList<MBState> heritors = new ArrayList<MBState>();

                for (int i = this.amazons[num].col + 1; i < 10; i++) {
                    if (this.field[this.amazons[num].row][i] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, this.amazons[num].row, i);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, this.amazons[num].row, i, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, this.amazons[num].row, i);
                        }
                    }
                }
                for (int i = this.amazons[num].row + 1; i < 10; i++) {
                    if (this.field[i][this.amazons[num].col] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, i, this.amazons[num].col);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, i, this.amazons[num].col, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, i, this.amazons[num].col);
                        }
                    }
                }
                for (int i = this.amazons[num].col - 1; i >= 0; i--) {
                    if (this.field[this.amazons[num].row][i] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, this.amazons[num].row, i);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, this.amazons[num].row, i, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, this.amazons[num].row, i);
                        }
                    }
                }
                for (int i = this.amazons[num].row - 1; i >= 0; i--) {
                    if (this.field[i][this.amazons[num].col] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, i, this.amazons[num].col);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, i, this.amazons[num].col, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, i, this.amazons[num].col);
                        }
                    }
                }
                for (int i = this.amazons[num].col + 1, j = this.amazons[num].row + 1; i < 10 && j < 10; i++, j++) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, j, i);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, j, i, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, j, i);
                        }
                    }
                }
                for (int i = this.amazons[num].col - 1, j = this.amazons[num].row + 1; i >= 0 && j < 10; i--, j++) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, j, i);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, j, i, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, j, i);
                        }
                    }
                }
                for (int i = this.amazons[num].col - 1, j = this.amazons[num].row - 1; i >= 0 && j >= 0; i--, j--) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, j, i);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, j, i, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, j, i);
                        }
                    }
                }
                for (int i = this.amazons[num].col + 1, j = this.amazons[num].row - 1; i < 10 && j >= 0; i++, j--) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        Field heritor = this.ConstructCopy();
                        heritor.moveAmazon(num, j, i);
                        ArrayList<int[]> arrows = heritor.getArrows(num);
                        for (int[] arrow : arrows) {
                            heritor.shootArrow(arrow[0], arrow[1]);
                            Move newMove = new Move(num, j, i, arrow[0], arrow[1]);
                            heritors.add(new MBState(heritor, newMove));
                            heritor = this.ConstructCopy();
                            heritor.moveAmazon(num, j, i);
                        }
                    }
                }
                return heritors;
            }

            private ArrayList<int[]> getArrows(int num) {
                ArrayList<int[]> arrows = new ArrayList<int[]>();


                for (int i = this.amazons[num].col + 1; i < 10; i++) {
                    if (this.field[this.amazons[num].row][i] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{this.amazons[num].row, i});
                    }
                }
                for (int i = this.amazons[num].row + 1; i < 10; i++) {
                    if (this.field[i][this.amazons[num].col] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{i, this.amazons[num].col});
                    }
                }
                for (int i = this.amazons[num].col - 1; i >= 0; i--) {
                    if (this.field[this.amazons[num].row][i] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{this.amazons[num].row, i});
                    }
                }
                for (int i = this.amazons[num].row - 1; i >= 0; i--) {
                    if (this.field[i][this.amazons[num].col] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{i, this.amazons[num].col});
                    }
                }
                for (int i = this.amazons[num].col + 1, j = this.amazons[num].row + 1; i < 10 && j < 10; i++, j++) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{j, i});
                    }
                }
                for (int i = this.amazons[num].col - 1, j = this.amazons[num].row + 1; i >= 0 && j < 10; i--, j++) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{j, i});
                    }
                }
                for (int i = this.amazons[num].col - 1, j = this.amazons[num].row - 1; i >= 0 && j >= 0; i--, j--) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{j, i});
                    }
                }
                for (int i = this.amazons[num].col + 1, j = this.amazons[num].row - 1; i < 10 && j >= 0; i++, j--) {
                    if (this.field[j][i] != 0) {
                        break;
                    } else {
                        arrows.add(new int[]{j, i});
                    }
                }
                return arrows;
            }

            public int amazonLiberty(Amazon A) {
                int result = 0;
                try {
                    if (field[A.row][A.col + 1] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (field[A.row][A.col - 1] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (field[A.row - 1][A.col - 1] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (field[A.row + 1][A.col - 1] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (field[A.row - 1][A.col + 1] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (field[A.row + 1][A.col + 1] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (field[A.row - 1][A.col] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                try {
                    if (field[A.row + 1][A.col] == 0) {
                        result++;
                    }
                } catch (Exception ex) {

                }
                return result;
            }

        }

        public static class Move {
            int amazonNum;
            int row;
            int col;
            int arrow_row;
            int arrow_col;

            public Move(int amazon, int row, int col, int arrow_row, int arrow_col) {
                this.amazonNum = amazon;
                this.row = row;
                this.col = col;
                this.arrow_row = arrow_row;
                this.arrow_col = arrow_col;
            }

        }

        public static class MBState {
            Field newBoard;
            Move newMove;

            public MBState(Field board, Move move) {
                this.newBoard = board;
                this.newMove = move;
            }
        }

        public static class Function {

            private Field field;

            public Function(Field b) {
                this.field = b;
            }

            private void Queen(Amazon amazon) {
                boolean were[][] = new boolean[10][10];
                int[][] path = new int[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        were[i][j] = false;
                        path[i][j] = Integer.MAX_VALUE;
                    }
                }
                path[amazon.row][amazon.col] = 0;
                were[amazon.row][amazon.col] = true;
                LinkedList<CellType> q = new LinkedList<CellType>();
                q.add(new CellType(amazon.row, amazon.col));
                while (!q.isEmpty()) {
                    CellType temp = q.getFirst();
                    q.removeFirst();
                    int displace_dx = temp.x + 1;
                    int displace_dy = temp.y;
                    while (displace_dx < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dx++;
                    }
                    displace_dx = temp.x - 1;
                    while (displace_dx >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dx--;
                    }
                    displace_dx = temp.x;
                    displace_dy = temp.y + 1;
                    while (displace_dy < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dy++;
                    }
                    displace_dy = temp.y - 1;
                    while (displace_dy >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dy--;
                    }
                    displace_dx = temp.x + 1;
                    displace_dy = temp.y + 1;
                    while (displace_dx < 10 && displace_dy < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dx++;
                        displace_dy++;
                    }
                    displace_dx = temp.x - 1;
                    displace_dy = temp.y - 1;
                    while (displace_dx >= 0 && displace_dy >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dx--;
                        displace_dy--;
                    }
                    displace_dx = temp.x + 1;
                    displace_dy = temp.y - 1;
                    while (displace_dx < 10 && displace_dy >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dx++;
                        displace_dy--;
                    }
                    displace_dx = temp.x - 1;
                    displace_dy = temp.y + 1;
                    while (displace_dx >= 0 && displace_dy < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                        displace_dx--;
                        displace_dy++;
                    }
                }
                field.QDistances[amazon.num] = path;
            }

            private void King(Amazon amazon) {
                boolean were[][] = new boolean[10][10];
                int[][] path = new int[10][10];
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        were[i][j] = false;
                        path[i][j] = Integer.MAX_VALUE;
                    }
                }
                path[amazon.row][amazon.col] = 0;
                were[amazon.row][amazon.col] = true;
                LinkedList<CellType> q = new LinkedList<CellType>();
                q.add(new CellType(amazon.row, amazon.col));
                while (!q.isEmpty()) {
                    CellType temp = q.getFirst();
                    q.removeFirst();
                    int displace_dx = temp.x + 1;
                    int displace_dy = temp.y;
                    if (displace_dx < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                    displace_dx = temp.x - 1;
                    if (displace_dx >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                    displace_dx = temp.x;
                    displace_dy = temp.y + 1;
                    if (displace_dy < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                    displace_dy = temp.y - 1;
                    if (displace_dy >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                    displace_dx = temp.x + 1;
                    displace_dy = temp.y + 1;
                    if (displace_dx < 10 && displace_dy < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                    displace_dx = temp.x - 1;
                    displace_dy = temp.y - 1;
                    if (displace_dx >= 0 && displace_dy >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                    displace_dx = temp.x + 1;
                    displace_dy = temp.y - 1;
                    if (displace_dx < 10 && displace_dy >= 0 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                    displace_dx = temp.x - 1;
                    displace_dy = temp.y + 1;
                    if (displace_dx >= 0 && displace_dy < 10 && field.field[displace_dx][displace_dy] == 0) {
                        if (!were[displace_dx][displace_dy]) {
                            were[displace_dx][displace_dy] = true;
                            q.add(new CellType(displace_dx, displace_dy));
                            path[displace_dx][displace_dy] = min(path[temp.x][temp.y] + 1, path[displace_dx][displace_dy]);
                        }
                    }
                }
                field.KDistances[amazon.num] = path;
            }

            public double Evaluate() {
                for (int i = 0; i < 8; i++) {
                    Queen(field.amazons[i]);
                    King(field.amazons[i]);
                }

                double turn = GetTurn();
                double evaluation = f1() * QDistance();
                evaluation += f2(turn) * KDistance();
                evaluation += f4(turn) * C1();
                evaluation += f5(turn) * C2();
                evaluation += f3(turn) * Liberty(turn);
                return evaluation;
            }

            private double Liberty(double turn) {
                double liberty = 0;
                for (int i = 4; i < 8; i++) {
                    liberty += NegativeLiberty(field.amazons[i], turn);
                }
                for (int i = 0; i < 4; i++) {
                    liberty -= NegativeLiberty(field.amazons[i], turn);
                }
                return liberty;
            }

            private double NegativeLiberty(Amazon amazon, double turn) {
                return turn / (5.0 + LibertyOfAmazon(amazon));
            }

            private double LibertyOfAmazon(Amazon amazon) {
                double liberty = 0;
                if (amazon.num < 4) {
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            long Player2Path = Long.MAX_VALUE;
                            for (int p = 4; p < 8; p++) {
                                Player2Path = min(Player2Path, field.QDistances[p][i][j]);
                            }
                            if (field.QDistances[amazon.num][i][j] != 1 || Player2Path == Long.MAX_VALUE) {
                                continue;
                            }
                            liberty += pow(2, -1 * field.KDistances[amazon.num][i][j]) * Cells(i, j);
                        }
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            long Player1Path = Long.MAX_VALUE;
                            for (int p = 0; p < 4; p++) {
                                Player1Path = min(Player1Path, field.QDistances[p][i][j]);
                            }
                            if (field.QDistances[amazon.num][i][j] != 1 || Player1Path == Long.MAX_VALUE) {
                                continue;
                            }
                            liberty += pow(2, -1 * field.KDistances[amazon.num][i][j]) * Cells(i, j);
                        }
                    }
                }
                return liberty;

            }

            private int Cells(int x, int y) {
                int cells = 0;
                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        try {
                            if ((i == 0 && j == 0) || field.field[x + i][y + j] != 0) {
                            } else {
                                cells++;
                            }
                        } catch (Exception ex) {
                        }
                    }
                }
                return cells;
            }

            private double f1() {
                return 1;
            }

            private double f2(double turn) {
                if (turn > 40) {
                    return 1;
                } else {
                    return turn / 40;
                }
            }

            private double f3(double turn) {
                if (turn > 40) {
                    return 0.8;
                } else {
                    return 0.8 * turn / 40;
                }
            }

            private double f4(double turn) {
                if (turn > 40) {
                    return 1 * turn / 40;
                } else {
                    return 1;
                }
            }

            private double f5(double turn) {
                if (turn > 40) {
                    return 0.8 * turn / 40;
                } else {
                    return 0.8;
                }
            }

            private double GetTurn() {
                double turn = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        long Player1Path = Long.MAX_VALUE;
                        long Player2Path = Long.MAX_VALUE;
                        for (int p = 0; p < 4; p++) {
                            Player1Path = min(Player1Path, field.QDistances[p][i][j]);
                        }
                        for (int p = 4; p < 8; p++) {
                            Player2Path = min(Player2Path, field.QDistances[p][i][j]);
                        }
                        if (Player1Path != Long.MAX_VALUE && Player2Path != Long.MAX_VALUE) {
                            turn += pow(2, -1 * abs(Player1Path - Player2Path));
                        }
                    }
                }
                return turn;
            }

            private long KDistance() {
                long path = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (field.field[i][j] != 0) {
                            continue;
                        }
                        path += KDForCell(i, j);
                    }
                }
                return path;
            }

            private long KDForCell(int x, int y) {
                long Player1Path = Long.MAX_VALUE;
                long Player2Path = Long.MAX_VALUE;
                for (int i = 0; i < 4; i++) {
                    Player1Path = min(Player1Path, field.KDistances[i][x][y]);
                }
                for (int i = 4; i < 8; i++) {
                    Player2Path = min(Player2Path, field.KDistances[i][x][y]);
                }
                if (Player1Path < Player2Path) {
                    return 1;
                } else if (Player2Path < Player1Path) {
                    return -1;
                } else
                    return 0;
            }

            private long QDistance() {
                long path = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (field.field[i][j] == 0) {
                            path += QDForCell(i, j);
                        }
                    }
                }
                return path;
            }

            private long QDForCell(int x, int y) {
                long Player1Path = Long.MAX_VALUE;
                long Player2Path = Long.MAX_VALUE;
                for (int i = 0; i < 4; i++) {
                    Player1Path = min(Player1Path, field.QDistances[i][x][y]);
                }
                for (int i = 4; i < 8; i++) {
                    Player2Path = min(Player2Path, field.QDistances[i][x][y]);
                }
                if (Player1Path < Player2Path) {
                    return 1;
                } else if (Player2Path < Player1Path) {
                    return -1;
                } else
                    return 0;
            }

            private double C1() {
                double ans = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (field.field[i][j] != 0) {
                            continue;
                        }
                        ans += C1PerCell(i,j);
                    }
                }
                return 2 * ans;
            }

            private double C2() {
                double ans = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (field.field[i][j] != 0) {
                            continue;
                        }
                        ans += C2PerCell(i,j);
                    }
                }
                return ans;
            }

            private double C1PerCell(int x, int y) {
                long Player1Path = Long.MAX_VALUE;
                long Player2Path = Long.MAX_VALUE;
                for (int i = 0; i < 4; i++) {
                    Player1Path = min(Player1Path, field.QDistances[i][x][y]);
                }
                for (int i = 4; i < 8; i++) {
                    Player2Path = min(Player2Path, field.QDistances[i][x][y]);
                }
                return pow(2, -Player1Path) - pow(2, -Player2Path);
            }

            private double C2PerCell(int x, int y) {
                long Player1Path = Long.MAX_VALUE;
                long Player2Path = Long.MAX_VALUE;
                for (int i = 0; i < 4; i++) {
                    Player1Path = min(Player1Path, field.KDistances[i][x][y]);
                }
                for (int i = 4; i < 8; i++) {
                    Player2Path = min(Player2Path, field.KDistances[i][x][y]);
                }
                return min(1, max(-1,(Player2Path - Player1Path)/6));
            }

            private static class CellType {

                private int x, y;

                public CellType(int x, int y) {
                    this.x = x;
                    this.y = y;
                }
            }

        }
    }
}
