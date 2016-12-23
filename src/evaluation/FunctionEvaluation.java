package evaluation;

import entity.Amazon;
import entity.GameBoard;

import java.util.LinkedList;

import static java.lang.Math.*;


/**
 * Created by amare on 12.01.2016.
 */
public class FunctionEvaluation {

    private final GameBoard board;

    public FunctionEvaluation(GameBoard b) {
        this.board = b;
    }

    private void BFSQueen(Amazon amazon) {
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
        LinkedList<FunctionEvaluation.Pos> q = new LinkedList<>();
        q.add(new FunctionEvaluation.Pos(amazon.row, amazon.column));
        while (!q.isEmpty()) {
            FunctionEvaluation.Pos current = q.getFirst();
            q.removeFirst();
            int dx = current.x + 1;
            int dy = current.y;
            while (dx < 10 && board.board[dx][dy] == 0) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
                dx++;
            }
            dx = current.x - 1;
            while (dx >= 0 && board.board[dx][dy] == 0) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
                dx--;
            }
            dx = current.x;
            dy = current.y + 1;
            while (dy < 10 && board.board[dx][dy] == 0) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
                dy++;
            }
            dy = current.y - 1;
            while (dy >= 0 && board.board[dx][dy] == 0) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
                dy--;
            }
            dx = current.x + 1;
            dy = current.y + 1;
            while (dx < 10 && dy < 10 && board.board[dx][dy] == 0) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
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
                    q.add(new FunctionEvaluation.Pos(dx, dy));
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
                    q.add(new FunctionEvaluation.Pos(dx, dy));
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
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
                dx--;
                dy++;
            }
        }
        board.BFSQueenDistances[amazon.id] = dist;
    }

    private void BFSKing(Amazon amazon) {
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
        LinkedList<FunctionEvaluation.Pos> q = new LinkedList<>();
        q.add(new FunctionEvaluation.Pos(amazon.row, amazon.column));
        while (!q.isEmpty()) {
            FunctionEvaluation.Pos current = q.getFirst();
            q.removeFirst();
            int dx = current.x + 1;
            int dy = current.y;
            if ((dx < 10) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
            dx = current.x - 1;
            if ((dx >= 0) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
            dx = current.x;
            dy = current.y + 1;
            if ((dy < 10) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
            dy = current.y - 1;
            if ((dy >= 0) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
            dx = current.x + 1;
            dy = current.y + 1;
            if ((dx < 10) && (dy < 10) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
            dx = current.x - 1;
            dy = current.y - 1;
            if ((dx >= 0) && (dy >= 0) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
            dx = current.x + 1;
            dy = current.y - 1;
            if ((dx < 10) && (dy >= 0) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
            dx = current.x - 1;
            dy = current.y + 1;
            if ((dx >= 0) && (dy < 10) && (board.board[dx][dy] == 0)) {
                if (!used[dx][dy]) {
                    used[dx][dy] = true;
                    q.add(new FunctionEvaluation.Pos(dx, dy));
                    dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                }
            }
        }
        board.BFSKingDistances[amazon.id] = dist;
    }

    public double Evaluate() {
        for (int i = 0; i < 8; i++) {
            BFSQueen(board.Amazons[i]);
            BFSKing(board.Amazons[i]);
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
            mobility += Penalty(board.Amazons[i]);
        }
        for (int i = 0; i < 4; i++) {
            mobility -= Penalty(board.Amazons[i]);
        }
        return mobility;
    }

    private double Penalty(Amazon amazon) {
        return 30 / (5.0 + MobilityOfAmazon(amazon));
    }

    private double MobilityOfAmazon(Amazon amazon) {
        double mobility = 0;
        if (amazon.id < 4) {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    int Player2Distance = Integer.MAX_VALUE;
                    for (int p = 4; p < 8; p++) {
                        Player2Distance =
                            Math.min(Player2Distance, board.BFSQueenDistances[p][i][j]);
                    }
                    if ((board.BFSQueenDistances[amazon.id][i][j] == 1) && (Player2Distance
                        != Integer.MAX_VALUE)) {
                        mobility +=
                            pow(2, -1 * board.BFSKingDistances[amazon.id][i][j]) * SquaresAround(i,
                                j);
                    }
                }
            }
        } else {
            for (int i = 0; i < 10; i++) {
                for (int j = 0; j < 10; j++) {
                    int Player1Distance = Integer.MAX_VALUE;
                    for (int p = 0; p < 4; p++) {
                        Player1Distance =
                            Math.min(Player1Distance, board.BFSQueenDistances[p][i][j]);
                    }
                    if ((board.BFSQueenDistances[amazon.id][i][j] == 1) && (Player1Distance
                        != Integer.MAX_VALUE)) {
                        mobility +=
                            pow(2, -1 * board.BFSKingDistances[amazon.id][i][j]) * SquaresAround(i,
                                j);
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
                    if (((i != 0) || (j != 0)) && (board.board[x + i][y + j] == 0)) {
                        squares++;
                    }
                } catch (Exception ignored) {

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
                int Player1Distance = Integer.MAX_VALUE;
                int Player2Distance = Integer.MAX_VALUE;
                for (int p = 0; p < 4; p++) {
                    Player1Distance = Math.min(Player1Distance, board.BFSQueenDistances[p][i][j]);
                }
                for (int p = 4; p < 8; p++) {
                    Player2Distance = Math.min(Player2Distance, board.BFSQueenDistances[p][i][j]);
                }
                if ((Player1Distance != Integer.MAX_VALUE) && (Player2Distance
                    != Integer.MAX_VALUE)) {
                    advancement += pow(2, -1 * abs(Player1Distance - Player2Distance));
                }
            }
        }
        return advancement;
    }

    private int KingDistance() {
        int Distance = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board.board[i][j] == 0) {
                    Distance += GetKingMovesForSquare(i, j);
                }
            }
        }
        return Distance;
    }

    private int GetKingMovesForSquare(int x, int y) {
        int Player1Distance = Integer.MAX_VALUE;
        int Player2Distance = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            Player1Distance = Math.min(Player1Distance, board.BFSKingDistances[i][x][y]);
        }
        for (int i = 4; i < 8; i++) {
            Player2Distance = Math.min(Player2Distance, board.BFSKingDistances[i][x][y]);
        }
        if (Player1Distance < Player2Distance) {
            return 1;
        } else if (Player2Distance < Player1Distance) {
            return -1;
        } else
            return 0;
    }

    private int QueenDistance() {
        int Distance = 0;
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                if (board.board[i][j] == 0) {
                    Distance += GetQueenMovesForSquare(i, j);
                }
            }
        }
        return Distance;
    }

    private int GetQueenMovesForSquare(int x, int y) {
        int Player1Distance = Integer.MAX_VALUE;
        int Player2Distance = Integer.MAX_VALUE;
        for (int i = 0; i < 4; i++) {
            Player1Distance = Math.min(Player1Distance, board.BFSQueenDistances[i][x][y]);
        }
        for (int i = 4; i < 8; i++) {
            Player2Distance = Math.min(Player2Distance, board.BFSQueenDistances[i][x][y]);
        }
        if (Player1Distance < Player2Distance) {
            return 1;
        } else if (Player2Distance < Player1Distance) {
            return -1;
        } else
            return 0;
    }

    private static class Pos {

        private final int x;
        private final int y;

        Pos(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

}
