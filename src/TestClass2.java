import java.io.*;
import java.util.*;

import static java.lang.Math.*;

/**
 * Created by amare on 09.01.2016.
 */
public class TestClass2 {

    static long time;

    public static void main(String[] args) throws Exception{
        time = System.currentTimeMillis();
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("input.txt")));
        int[][] array = new int[10][10];
        AI.Amazon[] all_amazons = new AI.Amazon[8];

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
                    all_amazons[our_index] = new AI.Amazon(i, j, our_index);
                    our_index++;
                }
                else if (array[i][j] == 3 - PlayerID) {
                    array[i][j] = 1;
                    all_amazons[opp_index] = new AI.Amazon(i, j, opp_index);
                    opp_index++;
                }
                else if (array[i][j] == -1) {
                    array[i][j] = 2;
                }
            }
        }



        AI.GameBoard Board = new AI.GameBoard(array, all_amazons);
        AI Brain = new AI(Board);
        AI.MoveAndBoard BestMove = Brain.getNextMove(Board);

        /*BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(System.out));
        wr.write(String.format("%d %d\n%d %d\n%d %d", all_amazons[BestMove.newMove.amazonID].row, all_amazons[BestMove.newMove.amazonID].column,
                BestMove.newMove.row, BestMove.newMove.col,
                BestMove.newMove.arrow_row, BestMove.newMove.arrow_col));*/
        System.out.println(String.format("%d %d", all_amazons[BestMove.newMove.amazon_id].row,
                all_amazons[BestMove.newMove.amazon_id].column));
        System.out.println(String.format("%d %d", BestMove.newMove.row,
                BestMove.newMove.col));
        System.out.println(String.format("%d %d", BestMove.newMove.arrow_row,
                BestMove.newMove.arrow_col));
    }
    public static class AI {

        GameBoard board = null;

        public AI(GameBoard b)
        {
            this.board = b;
        }

        public MoveAndBoard getNextMove(GameBoard b) {
            this.board = b;
            //TestClass2.AI.Edge nextMove = GreedyBestSearch(2);
            MoveAndBoard nextMove = minimaxSearch(1000, 4, 3);
            return nextMove;
        }

        // currently not using
        private MoveAndBoard GreedyBestSearch(int advance) {
            //we need to check to see if any of our amazons are almost blocked in (having a freedom of 1 or 2). If so then we need to move them immediately and not worry about anything else
            Amazon current = checkForDireAmazon();

            ArrayList<MoveAndBoard> searchResults = new ArrayList<MoveAndBoard>();
            // if we found an amazon that needs to be moved, only look at its possible moves.
            if (current != null)
            {
                //get only moves from the amazon
                searchResults.addAll(board.getPossibleMoves(current.id));
                if(searchResults.size() == 0)
                {
                    for(int i=0; i<4; i++)
                    {
                        searchResults.addAll(board.getPossibleMoves(i));
                    }
                }
            }
            else
            {
                searchResults = new ArrayList<MoveAndBoard>();
                for(int i=0; i<4; i++)
                {
                    searchResults.addAll(board.getPossibleMoves(i));
                }
            }

            MoveAndBoard newState;
            if (advance == 0)
                newState = defaultHeuristic(searchResults);
            else if (advance == 1)
                newState = advancedHeuristic(searchResults);
            else
                newState = superHeuristic(searchResults);

            return newState;
        }


        /**
         *
         * @param timer - Amount of seconds to restrict the search to.
         * @param depth - The maximum depth to expand within the amount of time.
         * @return Recommended next move.
         */
        private MoveAndBoard minimaxSearch(int timer, int depth, int advance) {
            SearchNode initialNode = new SearchNode(new MoveAndBoard(board, null));
            long endtime = System.currentTimeMillis()+timer;
            return minimax(initialNode, true, endtime, depth, advance).state;
        }

        private SearchNode minimax(SearchNode node, boolean max, long endtime, int depth, int advance) {
            if((System.currentTimeMillis() - time > 500) || (depth<=0)) return new SearchNode(node.state, new WebEvaluation(node.state.newBoard).Evaluate());
            double alpha;
            ArrayList<SearchNode> children;
            if (advance == 1) {
                children = getAdvancedChildren(node, max);
                alpha = max?Double.MIN_VALUE:Double.MAX_VALUE;
            } else if (advance == 0){
                children = getChildren(node, max);
                alpha = max?Integer.MIN_VALUE:Integer.MAX_VALUE;
            } else{
                children = getSuperChildren(node,max);
                alpha = max?Double.MIN_VALUE:Double.MAX_VALUE;
            }
            SearchNode nextBestNode = null;
            for(int i=0; i<children.size(); i++) {
                if (System.currentTimeMillis() - time > 500)
                    break;
                nextBestNode = minimax(children.get(i), !max, endtime, depth-1, advance);
                alpha = max?Math.max(alpha, nextBestNode.heuristic):Math.min(alpha, nextBestNode.heuristic);
                nextBestNode.heuristic = alpha;
            }
            return nextBestNode;
        }

        private ArrayList<SearchNode> getSuperChildren(SearchNode node, boolean ourTurn) {
            ArrayList<SearchNode> children = new ArrayList<SearchNode>();
            for(int i=0; i<4; i++) {
                int j = ourTurn?i:i+4;
                ArrayList<MoveAndBoard> amazonMoves = node.state.newBoard.getPossibleMoves(j);
                for(int k=0; k<amazonMoves.size(); k++) {
                    children.add(new SearchNode(amazonMoves.get(k), new WebEvaluation(amazonMoves.get(k).newBoard).Evaluate()));
                }
            }
            return children;
        }

        private ArrayList<SearchNode> getChildren(SearchNode node, boolean ourTurn) {
            ArrayList<SearchNode> children = new ArrayList<SearchNode>();
            for(int i=0; i<4; i++) {
                int j = ourTurn?i:i+4;
                ArrayList<MoveAndBoard> amazonMoves = node.state.newBoard.getPossibleMoves(j);
                for(int k=0; k<amazonMoves.size(); k++) {
                    children.add(new SearchNode(amazonMoves.get(k), evaluateHeuristic(amazonMoves.get(k).newBoard)));
                }
            }
            return children;
        }

        private ArrayList<SearchNode> getAdvancedChildren(SearchNode node, boolean ourTurn) {
            ArrayList<SearchNode> children = new ArrayList<SearchNode>();
            for(int i=0; i<4; i++) {
                int j = ourTurn?i:i+4;
                ArrayList<MoveAndBoard> amazonMoves = node.state.newBoard.getPossibleMoves(j);
                for(int k=0; k<amazonMoves.size(); k++) {
                    children.add(new SearchNode(amazonMoves.get(k), new Evaluation(amazonMoves.get(k).newBoard).Evaluate()));
                }
            }
            return children;
        }

        private Amazon checkForDireAmazon() {
            Amazon dire = null;
            for(int j = 0; j < 4; j++){
                if(board.getOurFreedom(board.Amazons[j]) <= 2){
                    dire = board.Amazons[j];
                }
            }
            return dire;
        }

        private MoveAndBoard defaultHeuristic(ArrayList<MoveAndBoard> moves) {
            ArrayList<Integer> evaluations = new ArrayList<Integer>(moves.size());
            for(int i=0; i<moves.size(); i++) {
                evaluations.add(i, new Integer(evaluateHeuristic(moves.get(i).newBoard)));
            }

            Integer max = Collections.max(evaluations);
            return moves.get(evaluations.indexOf(max));
        }

        private MoveAndBoard advancedHeuristic(ArrayList<MoveAndBoard> moves) {
            ArrayList<Double> evaluations = new ArrayList<Double>(moves.size());
            for(int i=0; i<moves.size(); i++) {
                evaluations.add(i, new Double(new Evaluation(moves.get(i).newBoard).Evaluate()));
            }

            Double max = Collections.max(evaluations);
            return moves.get(evaluations.indexOf(max));
        }

        private MoveAndBoard superHeuristic(ArrayList<MoveAndBoard> moves) {
            ArrayList<Double> evaluations = new ArrayList<Double>(moves.size());
            for(int i=0; i<moves.size(); i++) {
                evaluations.add(i, new Double(new WebEvaluation(moves.get(i).newBoard).Evaluate()));
            }

            Double max = Collections.max(evaluations);
            return moves.get(evaluations.indexOf(max));
        }

        private MoveAndBoard randomHeuristic(ArrayList<MoveAndBoard> moves){
            ArrayList<Integer> evaluations = new ArrayList<Integer>(moves.size());
            for(int i=0; i<moves.size(); i++) {
                evaluations.add(i, new Integer(evaluateRandomHeuristic(moves.get(i).newBoard)));
            }

            Integer max = Collections.max(evaluations);
            return moves.get(evaluations.indexOf(max));
        }

        private int evaluateRandomHeuristic(GameBoard board){
            Random rand = new Random();
            return rand.nextInt(10000);
        }

        private int evaluateHeuristic(GameBoard board) {
            int sum = getSpaceConfiguration(board);
            sum += getOurFreedom(board);
            return sum;
        }

        /**
         * Returns the score of the board as determined by freedom. For a single amazon their freedom is (8 - the number of arrows/queens beside them). This calculates the freedom of each of our amazons.
         * @param board
         * @return Returns the overall freedom of our amazons. The bigger the better.
         */
        private int getOurFreedom(GameBoard board){
            return board.getOurFreedoms();
        }

        /**
         * This will return how many of the spaces are ours minus the number of spaces that are theirs.
         * @param board
         * @return Number of spaces that are ours - spaces theirs. Larger the better
         */
        private int getSpaceConfiguration(GameBoard board){
            //Set containing all the board squares movable to by our TestClass2.AI.AmazonUnit players.
            HashSet<Integer> A = new HashSet<Integer>();
            A = board.ourSpaces();

            //Set containing all the board squares movable to by the opponents players.
            HashSet<Integer> B = new HashSet<Integer>();
            B = board.theirSpaces();

            HashSet<Integer> C = new HashSet<Integer>(A);
            A.removeAll(B);
            B.removeAll(C);

            return A.size()-B.size();
        }

        public static class Amazon {
            public int row;
            public int column;
            public int id;

            public Amazon(int row, int column, int id) {
                this.row = row;
                this.column = column;
                this.id = id;
            }

            public void updateCoords(int x, int y) {
                this.row=x;
                this.column=y;
            }

        }

        public static class GameBoard {
            //int array to store the state of the board.
            //value at (x,y) cooresponds to:
            //-1 : occupied by our player
            //0  : Empty
            //1  : occupied by other player
            //2  : occupied by an arrow
            //The first array stores the row #, second array is the column #
            //The board is indexed starting at (0,0) at the TOP LEFT CORNER.
            //Incrementing the row, moves a player down.
            //Incrementing the column, moves a player right.
            public int[][] board;

            public int[][][] BFSQueenDistances = new int[8][10][10], BFSKingDistances = new int[8][10][10];

            public Amazon[] Amazons;	//An array of all the amazons on the board.
            //Indexes 0-3 store the positions of our amazon players.
            //Indexes 4-7 store the positions of the opponents players.
            public GameBoard(boolean firstPlayer) {
                board = new int[10][10];
                if(firstPlayer) {
                    board[6][0] = -1;
                    board[9][3] = -1;
                    board[9][6] = -1;
                    board[6][9] = -1;
                    board[3][0] = 1;
                    board[0][3] = 1;
                    board[0][6] = 1;
                    board[3][9] = 1;

                    Amazons = new Amazon[8];
                    Amazons[0] = new Amazon(6, 0, 0);
                    Amazons[1] = new Amazon(9, 3, 1);
                    Amazons[2] = new Amazon(9, 6, 2);
                    Amazons[3] = new Amazon(6, 9, 3);
                    Amazons[4] = new Amazon(3, 0, 4);
                    Amazons[5] = new Amazon(0, 3, 5);
                    Amazons[6] = new Amazon(0, 6, 6);
                    Amazons[7] = new Amazon(3, 9, 7);

                } else {
                    board[6][0] = 1;
                    board[9][3] = 1;
                    board[9][6] = 1;
                    board[6][9] = 1;
                    board[3][0] = -1;
                    board[0][3] = -1;
                    board[0][6] = -1;
                    board[3][9] = -1;

                    Amazons = new Amazon[8];
                    Amazons[0] = new Amazon(3, 0, 0);
                    Amazons[1] = new Amazon(0, 3, 1);
                    Amazons[2] = new Amazon(0, 6, 2);
                    Amazons[3] = new Amazon(3, 9, 3);
                    Amazons[4] = new Amazon(6, 0, 4);
                    Amazons[5] = new Amazon(9, 3, 5);
                    Amazons[6] = new Amazon(9, 6, 6);
                    Amazons[7] = new Amazon(6, 9, 7);
                }

            }

            public GameBoard(int[][] initialBoard, Amazon[] amazons) {
                this.board = initialBoard;
                this.Amazons = amazons;
            }

            public int getAmazonId(int x, int y)
            {
                int id = 0;
                for(int i = 0; i < Amazons.length; i++){
                    if((Amazons[i].row == x) && (Amazons[i].column == y)){
                        id = Amazons[i].id;
                        break;
                    }
                }

                return id;
            }

            public void moveOpponent(int id, int fromX, int fromY, int toX, int toY, boolean whitePlayer)
            {
                board[fromX][fromY] = 0;
                board[toX][toY] = 1;
                Amazons[id].updateCoords(toX, toY);
            }

            public void moveAmazon(int amazon, int row, int column) {
                board[Amazons[amazon].row][Amazons[amazon].column]=0;
                board[row][column] = (amazon<4)?-1:1;
                Amazons[amazon].updateCoords(row, column);
            }

            public void fireArrow(int x, int y) {
                board[x][y]=2;
            }

            public String toString() {
                String toReturn = "";
                for(int i=0; i<10; i++) {
                    toReturn += "[";
                    for(int j=0; j<10; j++) {
                        toReturn+=board[i][j];
                        if(j!=9) toReturn+="\t";
                    }
                    toReturn += "]\n";
                }
                return toReturn;
            }

            public GameBoard copyOf() {
                int[][] newBoard = new int[10][10];
                Amazon[] newAmazons = new Amazon[8];


                for(int i=0; i<10; i++) {
                    for(int j=0; j<10; j++) {
                        newBoard[i][j] = this.board[i][j];
                    }
                }
                for(int i=0; i<8; i++) {
                    newAmazons[i] = new Amazon(this.Amazons[i].row, this.Amazons[i].column, this.Amazons[i].id);
                }

                return new GameBoard(newBoard, newAmazons);
            }

            public HashSet<Integer> ourSpaces() {
                HashSet<Integer> spaces = new HashSet<Integer>();
                for(int i=0; i<4; i++) {
                    spaces.addAll(moveableSpaces(Amazons[i]));
                }

                return spaces;
            }

            public HashSet<Integer> theirSpaces() {
                HashSet<Integer> spaces = new HashSet<Integer>();
                for(int i=4; i<8; i++) {
                    spaces.addAll(moveableSpaces(Amazons[i]));
                }

                return spaces;
            }

            public ArrayList<MoveAndBoard> getPossibleMoves(int amazonId) {
                ArrayList<MoveAndBoard> successors = new ArrayList<MoveAndBoard>();
                //Moves Right
                for(int i=this.Amazons[amazonId].column+1; i < 10; i++) {
                    if(this.board[this.Amazons[amazonId].row][i]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, this.Amazons[amazonId].row, i);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int j=0; j<arrows.size(); j++) {
                            successor.fireArrow(arrows.get(j)[0], arrows.get(j)[1]);
                            Move newMove = new Move(amazonId, this.Amazons[amazonId].row, i, arrows.get(j)[0], arrows.get(j)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, this.Amazons[amazonId].row, i);
                        }
                    } else break;
                }

                //Moves Down
                for(int i=this.Amazons[amazonId].row+1; i < 10; i++) {
                    if(this.board[i][this.Amazons[amazonId].column]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, i, this.Amazons[amazonId].column);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int j=0; j<arrows.size(); j++) {
                            successor.fireArrow(arrows.get(j)[0], arrows.get(j)[1]);
                            Move newMove = new Move(amazonId, i, this.Amazons[amazonId].column, arrows.get(j)[0], arrows.get(j)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, i, this.Amazons[amazonId].column);
                        }
                    } else break;
                }

                //Moves Left
                for(int i=this.Amazons[amazonId].column-1; i >= 0; i--) {
                    if(this.board[this.Amazons[amazonId].row][i]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, this.Amazons[amazonId].row, i);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int j=0; j<arrows.size(); j++) {
                            successor.fireArrow(arrows.get(j)[0], arrows.get(j)[1]);
                            Move newMove = new Move(amazonId, this.Amazons[amazonId].row, i, arrows.get(j)[0], arrows.get(j)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, this.Amazons[amazonId].row, i);
                        }
                    } else break;
                }

                //Moves Up
                for(int i=this.Amazons[amazonId].row-1; i >= 0; i--) {
                    if(this.board[i][this.Amazons[amazonId].column]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, i, this.Amazons[amazonId].column);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int j=0; j<arrows.size(); j++) {
                            successor.fireArrow(arrows.get(j)[0], arrows.get(j)[1]);
                            Move newMove = new Move(amazonId, i, this.Amazons[amazonId].column, arrows.get(j)[0], arrows.get(j)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, i, this.Amazons[amazonId].column);
                        }
                    } else break;
                }

                //Moves Down-Right
                for(int i=this.Amazons[amazonId].column+1, j=this.Amazons[amazonId].row+1; i < 10 && j < 10; i++, j++) {
                    if(this.board[j][i]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int k=0; k<arrows.size(); k++) {
                            successor.fireArrow(arrows.get(k)[0], arrows.get(k)[1]);
                            Move newMove = new Move(amazonId, j, i, arrows.get(k)[0], arrows.get(k)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }

                //Moves Down-Left
                for(int i=this.Amazons[amazonId].column-1, j=this.Amazons[amazonId].row+1; i >= 0 && j < 10; i--, j++) {
                    if(this.board[j][i]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int k=0; k<arrows.size(); k++) {
                            successor.fireArrow(arrows.get(k)[0], arrows.get(k)[1]);
                            Move newMove = new Move(amazonId, j, i, arrows.get(k)[0], arrows.get(k)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }

                //Moves Up-Left
                for(int i=this.Amazons[amazonId].column-1, j=this.Amazons[amazonId].row-1; i >= 0 && j >= 0; i--, j--) {
                    if(this.board[j][i]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int k=0; k<arrows.size(); k++) {
                            successor.fireArrow(arrows.get(k)[0], arrows.get(k)[1]);
                            Move newMove = new Move(amazonId, j, i, arrows.get(k)[0], arrows.get(k)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }

                //Moves Up-Right
                for(int i=this.Amazons[amazonId].column+1, j=this.Amazons[amazonId].row-1; i < 10 && j >= 0; i++, j--) {
                    if(this.board[j][i]==0) {	//If the spot is empty
                        GameBoard successor = this.copyOf();
                        successor.moveAmazon(amazonId, j, i);
                        ArrayList<int[]> arrows = successor.getPossibleArrows(amazonId);
                        for(int k=0; k<arrows.size(); k++) {
                            successor.fireArrow(arrows.get(k)[0], arrows.get(k)[1]);
                            Move newMove = new Move(amazonId, j, i, arrows.get(k)[0], arrows.get(k)[1]);
                            successors.add(new MoveAndBoard(successor, newMove));
                            successor = this.copyOf();
                            successor.moveAmazon(amazonId, j, i);
                        }
                    } else break;
                }

                return successors;
            }

            private ArrayList<int[]> getPossibleArrows(int amazonId) {
                ArrayList<int[]> arrows = new ArrayList<int[]>();

                //Arrows Right
                for(int i=this.Amazons[amazonId].column+1; i < 10; i++) {
                    if(this.board[this.Amazons[amazonId].row][i]==0) {
                        arrows.add(new int[] {this.Amazons[amazonId].row, i});
                    } else break;
                }

                //Arrows Down
                for(int i=this.Amazons[amazonId].row+1; i < 10; i++) {
                    if(this.board[i][this.Amazons[amazonId].column]==0) {
                        arrows.add(new int[] {i, this.Amazons[amazonId].column});
                    } else break;
                }

                //Arrows Left
                for(int i=this.Amazons[amazonId].column-1; i >= 0; i--) {
                    if(this.board[this.Amazons[amazonId].row][i]==0) {
                        arrows.add(new int[] {this.Amazons[amazonId].row, i});
                    } else break;
                }

                //Arrows Up
                for(int i=this.Amazons[amazonId].row-1; i >= 0; i--) {
                    if(this.board[i][this.Amazons[amazonId].column]==0) {
                        arrows.add(new int[] {i, this.Amazons[amazonId].column});
                    } else break;
                }

                //Arrows Down-Right
                for(int i=this.Amazons[amazonId].column+1, j=this.Amazons[amazonId].row+1; i < 10 && j < 10; i++, j++) {
                    if(this.board[j][i]==0) {
                        arrows.add(new int[] {j, i});
                    } else break;
                }

                //Arrows Down-Left
                for(int i=this.Amazons[amazonId].column-1, j=this.Amazons[amazonId].row+1; i >= 0 && j < 10; i--, j++) {
                    if(this.board[j][i]==0) {
                        arrows.add(new int[] {j, i});
                    } else break;
                }

                //Arrows Up-Left
                for(int i=this.Amazons[amazonId].column-1, j=this.Amazons[amazonId].row-1; i >= 0 && j >= 0; i--, j--) {
                    if(this.board[j][i]==0) {
                        arrows.add(new int[] {j, i});
                    } else break;
                }

                //Arrows Up-Right
                for(int i=this.Amazons[amazonId].column+1, j=this.Amazons[amazonId].row-1; i < 10 && j >= 0; i++, j--) {
                    if(this.board[j][i]==0) {
                        arrows.add(new int[] {j, i});
                    } else break;
                }

                return arrows;
            }

            private HashSet<Integer> moveableSpaces(Amazon A) {
                HashSet<Integer> spaces = new HashSet<Integer>();

                int column = A.column;
                int row = A.row;

                //Moves Right
                for(int i=column+1; i < 10; i++) {
                    if(board[row][i]==0) {	//If the spot is empty
                        spaces.add(row*10+i);
                    } else break;
                }

                //Moves Down
                for(int i=row+1; i < 10; i++) {
                    if(board[i][column]==0) {	//If the spot is empty
                        spaces.add(i*10+column);
                    } else break;
                }

                //Moves Left
                for(int i=column-1; i >= 0; i--) {
                    if(board[row][i]==0) {	//If the spot is empty
                        spaces.add(row*10+i);
                    } else break;
                }

                //Moves Up
                for(int i=row-1; i >= 0; i--) {
                    if(board[i][column]==0) {	//If the spot is empty
                        spaces.add(i*10+column);
                    } else break;
                }

                //Moves Down-Right
                for(int i=column+1, j=row+1; i < 10 && j < 10; i++, j++) {
                    if(board[j][i]==0) {	//If the spot is empty
                        spaces.add(j*10+i);
                    } else break;
                }

                //Moves Down-Left
                for(int i=column-1, j=row+1; i >= 0 && j < 10; i--, j++) {
                    if(board[j][i]==0) {	//If the spot is empty
                        spaces.add(j*10+i);
                    } else break;
                }

                //Moves Up-Left
                for(int i=column-1, j=row-1; i >= 0 && j >= 0; i--, j--) {
                    if(board[j][i]==0) {	//If the spot is empty
                        spaces.add(j*10+i);
                    } else break;
                }

                //Moves Up-Right
                for(int i=column+1, j=row-1; i < 10 && j >= 0; i++, j--) {
                    if(board[j][i]==0) {	//If the spot is empty
                        spaces.add(j*10+i);
                    } else break;
                }

                return spaces;
            }

            public int getOurFreedoms()
            {
                // get the freedom only of our amazons
                int sum = 0;
                for(int i = 0; i < 4; i++)
                {
                    sum += getOurFreedom(Amazons[i]);
                }
                return sum;
            }

            /**
             * Count the number of empty spaces beside each amazon
             * @param A
             * @return
             */
            public int getOurFreedom(Amazon A){
                int sum = 0;
                try{
                    if(board[A.row][A.column+1] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                try{
                    if(board[A.row][A.column-1] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                try{
                    if(board[A.row-1][A.column-1] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                try{
                    if(board[A.row+1][A.column-1] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                try{
                    if(board[A.row-1][A.column+1] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                try{
                    if(board[A.row+1][A.column+1] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                try{
                    if(board[A.row-1][A.column] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                try{
                    if(board[A.row+1][A.column] == 0){
                        sum++;
                    }
                }
                catch(Exception ex){

                }
                return sum;
            }

        }

        public static class Move {
            int amazon_id;
            int row;
            int col;
            int arrow_row;
            int arrow_col;

            public Move(int amazon, int row, int col, int arrow_row, int arrow_col) {
                this.amazon_id = amazon;
                this.row = row;
                this.col = col;
                this.arrow_row = arrow_row;
                this.arrow_col = arrow_col;
            }

            /**Return an int array representing the amazon's move.
             //int[0] = amazon ID being moved;
             //int[1] = row to move amazon to;
             //int[2] = column to move amazon to;
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

            /**Return an int array representing where to shoot an arrow
             //int[0] = row of arrow
             //int[1] = column of arrow
             *
             * @return int[2]
             */
            public int[] getArrowMove() {
                int[] arrowShot = new int[2];
                arrowShot[0] = arrow_row;
                arrowShot[1] = arrow_col;

                return arrowShot;
            }
        }

        public static class MoveAndBoard {
            GameBoard newBoard;
            Move newMove;

            public MoveAndBoard(GameBoard board, Move move) {
                this.newBoard = board;
                this.newMove = move;
            }

            public String toString() {
                String toReturn = "";
                toReturn += "TestClass2.AI.Move TestClass2.AI.AmazonUnit ID: " + newMove.amazon_id + " to ROW: " + newMove.row + "\tCOLUMN: " + newMove.col + "\n";
                toReturn += "Shot Arrow to ROW: " + newMove.arrow_row + "\tCOLUMN: " + newMove.arrow_col + "\n";
                toReturn += "New Board:\n" + newBoard;
                return toReturn;
            }
        }

        public static class SearchNode {
            MoveAndBoard state;
            double heuristic;

            public SearchNode(MoveAndBoard state) {
                this.state = state;
                this.heuristic = 0;
            }

            public SearchNode(MoveAndBoard state, double heuristic) {
                this.state = state;
                this.heuristic = heuristic;
            }

            public MoveAndBoard getState() {
                return this.state;
            }

            public double heuristic() {
                return heuristic;
            }
        }

        /**
         * Created by amare on 12.01.2016.
         */
        public static class Evaluation {

            private GameBoard board;

            public Evaluation(GameBoard b) {
                this.board = b;
            }

            private void BFSQueen(Amazon amazon) {
                boolean used[][] = new boolean[10][10];
                int[][] dist = new int[10][10];
                for (int i=0;i<10;i++) {
                    for (int j = 0; j < 10; j++) {
                        used[i][j] = false;
                        dist[i][j] = Integer.MAX_VALUE;
                    }
                }
                dist[amazon.row][amazon.column] = 0;
                used[amazon.row][amazon.column] = true;
                LinkedList<Pos> q = new LinkedList<Pos>();
                q.add(new Pos(amazon.row,amazon.column));
                while (!q.isEmpty()) {
                    Pos current = q.getFirst();
                    q.removeFirst();
                    int dx = current.x + 1;
                    int dy = current.y;
                    while (dx < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx++;
                    }
                    dx = current.x - 1;
                    while (dx >=0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx--;
                    }
                    dx = current.x;
                    dy = current.y + 1;
                    while (dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dy++;
                    }
                    dy = current.y - 1;
                    while (dy >= 0 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dy--;
                    }
                    dx = current.x + 1;
                    dy = current.y + 1;
                    while (dx < 10 && dy < 10 && board.board[dx][dy] == 0) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx++;
                        dy++;
                    }
                    dx = current.x - 1;
                    dy = current.y - 1;
                    while (dx >=0 && dy >= 0 && board.board[dx][dy] == 0) {
                        if (used[dx][dy] == false) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx--;
                        dy--;
                    }
                    dx = current.x + 1;
                    dy = current.y - 1;
                    while (dx < 10 && dy >= 0 && board.board[dx][dy] == 0) {
                        if (used[dx][dy] == false) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                        dx++;
                        dy--;
                    }
                    dx = current.x - 1;
                    dy = current.y + 1;
                    while (dx >= 0 && dy < 10 && board.board[dx][dy] == 0) {
                        if (used[dx][dy] == false) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
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
                for (int i=0;i<10;i++) {
                    for (int j = 0; j < 10; j++) {
                        used[i][j] = false;
                        dist[i][j] = Integer.MAX_VALUE;
                    }
                }
                dist[amazon.row][amazon.column] = 0;
                used[amazon.row][amazon.column] = true;
                LinkedList<Pos> q = new LinkedList<Pos>();
                q.add(new Pos(amazon.row,amazon.column));
                while (!q.isEmpty()) {
                    Pos current = q.getFirst();
                    q.removeFirst();
                    int dx = current.x + 1;
                    int dy = current.y;
                    if ((dx < 10) && (board.board[dx][dy] == 0)) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x - 1;
                    if ((dx >= 0) && (board.board[dx][dy] == 0)) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x;
                    dy = current.y + 1;
                    if ((dy < 10) && (board.board[dx][dy] == 0)) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx, dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dy = current.y - 1;
                    if ((dy >= 0) && (board.board[dx][dy] == 0)) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x + 1;
                    dy = current.y + 1;
                    if ((dx < 10) && (dy < 10) && (board.board[dx][dy] == 0)) {
                        if (!used[dx][dy]) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x - 1;
                    dy = current.y - 1;
                    if ((dx >= 0) && (dy >= 0) && (board.board[dx][dy] == 0)) {
                        if (used[dx][dy] == false) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x + 1;
                    dy = current.y - 1;
                    if ((dx < 10) && (dy >= 0) && (board.board[dx][dy] == 0)) {
                        if (used[dx][dy] == false) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                    dx = current.x - 1;
                    dy = current.y + 1;
                    if ((dx >= 0) && (dy < 10) && (board.board[dx][dy] == 0)) {
                        if (used[dx][dy] == false) {
                            used[dx][dy] = true;
                            q.add(new Pos(dx,dy));
                            dist[dx][dy] = min(dist[current.x][current.y] + 1, dist[dx][dy]);
                        }
                    }
                }
                board.BFSKingDistances[amazon.id] = dist;
            }

            public double Evaluate() {
                for (int i=0;i<8;i++) {
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
                                Player2Distance = min(Player2Distance, board.BFSQueenDistances[p][i][j]);
                            }
                            if ((board.BFSQueenDistances[amazon.id][i][j] == 1) && (Player2Distance != Integer.MAX_VALUE)) {
                                mobility += pow(2, -1 * board.BFSKingDistances[amazon.id][i][j]) * SquaresAround(i, j);
                            }
                        }
                    }
                } else {
                    for (int i = 0; i < 10; i++) {
                        for (int j = 0; j < 10; j++) {
                            int Player1Distance = Integer.MAX_VALUE;
                            for (int p = 0; p < 4; p++) {
                                Player1Distance = min(Player1Distance, board.BFSQueenDistances[p][i][j]);
                            }
                            if ((board.BFSQueenDistances[amazon.id][i][j] == 1) && (Player1Distance != Integer.MAX_VALUE)) {
                                mobility += pow(2, -1 * board.BFSKingDistances[amazon.id][i][j]) * SquaresAround(i, j);
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
                        int Player1Distance = Integer.MAX_VALUE;
                        int Player2Distance = Integer.MAX_VALUE;
                        for (int p = 0; p < 4; p++) {
                            Player1Distance = min(Player1Distance, board.BFSQueenDistances[p][i][j]);
                        }
                        for (int p = 4; p < 8; p++) {
                            Player2Distance = min(Player2Distance, board.BFSQueenDistances[p][i][j]);
                        }
                        if ((Player1Distance != Integer.MAX_VALUE) && (Player2Distance != Integer.MAX_VALUE)) {
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
                            Distance += GetKingMovesForSquare(i,j);
                        }
                    }
                }
                return Distance;
            }

            private int GetKingMovesForSquare(int x, int y) {
                int Player1Distance = Integer.MAX_VALUE;
                int Player2Distance = Integer.MAX_VALUE;
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
                }
                else
                    return 0;
            }

            private int QueenDistance() {
                int Distance = 0;
                for (int i = 0; i < 10; i++) {
                    for (int j = 0; j < 10; j++) {
                        if (board.board[i][j] == 0) {
                            Distance += GetQueenMovesForSquare(i,j);
                        }
                    }
                }
                return Distance;
            }

            private int GetQueenMovesForSquare(int x, int y) {
                int Player1Distance = Integer.MAX_VALUE;
                int Player2Distance = Integer.MAX_VALUE;
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
                }
                else
                    return 0;
            }

            private static class Pos {

                private int x,y;

                public Pos(int x,int y) {
                    this.x = x;
                    this.y = y;
                }
            }

        }

        /**
         * Created by amare on 14.01.2016.
         */
        public static class WebEvaluation {

            private int black_misc = 0, white_misc = 0;
            public State s = new State();

            public WebEvaluation(GameBoard b)
            {
                int p = 0, l = 0;
                for (int i = 0; i < 10; i++) {

                    for (int j = 0; j < 10; j++) {
                        if (b.board[i][j] == -1) {
                            s.white_q_x[p] = i;
                            s.white_q_y[p++] = j;
                            s.white_bd = XOR(s.white_bd, i, j);
                        } else if (b.board[i][j] == 1) {
                            s.black_q_x[l] = i;
                            s.black_q_y[l++] = j;
                            s.black_bd = XOR(s.black_bd, i, j);
                        } else if (b.board[i][j] == -1) {
                            s.blocks_bd = XOR(s.blocks_bd, i, j);
                        }
                    }
                }
            }

            private long[] XOR(long[] bd, int col, int row) {
                if (row < 5) {
                    bd[0] ^= (long) 1 << ((row * 10) + col);
                } else {
                    bd[1] ^= (long) 1 << (((row-5) * 10) + col);
                }
                return bd;
            }

            public double Evaluate() {
                int p1 = 0, p2 = 0;
                double own1L0 = 0, own2L0= 0;
                double own1L1 = 0, own2L1= 0;
                long board_u, board_l;
                long web_board_u=0, web_board_l=0;
                long white_web1_u=0, white_web1_l=0, black_web1_u=0, black_web1_l=0;
                long res1_l=0, res1_u=0, res2_l=0, res2_u=0;
                long dirs_l=0, dirs_u=0;
                long low_quad = Long.decode("0x07c1f07c1f"), high_quad = Long.decode("0xf83e0f83e0");
                int i;
                int[] black_q_pos = new int[4];
                int[] white_q_pos = new int[4];
                white_misc = black_misc = 0;
                for (i=0; i<4; i++)
                {
                    black_q_pos[i] = s.black_q_y[i] * 10 + s.black_q_x[i];
                    white_q_pos[i] = s.white_q_y[i] * 10 + s.white_q_x[i];
                }
                board_u = s.white_bd[1] | s.black_bd[1] | s.blocks_bd[1];
                board_l = s.white_bd[0] | s.black_bd[0] | s.blocks_bd[0];
                for (i = 0; i < 4; i++) {

                    if ((p1 += gen_web_board_count(white_web1_l, white_web1_u, board_l, board_u, white_q_pos[i])) == 0)
                    white_misc -= 15;  //white queen is trapped.

                    if ((p2 += gen_web_board_count(black_web1_l, black_web1_u, board_l, board_u, black_q_pos[i])) == 0)
                    black_misc -= 15;  //black queen is trapped.
                }


                for (i=0; i<4; i++)
                {
                    //check white queens
                    long[] temp = gen_dirs_board(dirs_l, dirs_u, white_q_pos[i]);
                    dirs_l = temp[0];
                    dirs_u = temp[1];
                    res1_l = (white_web1_l & dirs_l);
                    res1_u = (white_web1_u & dirs_u);

                    if (count_bits(res1_l, res1_u) < 3)
                    {
                        res2_l = res1_l & black_web1_l;
                        res2_u = res1_u & black_web1_u;
                        if (!(((res1_l ^ res2_l)!=0) || ((res1_u ^ res2_u)!=0)))
                            white_misc -= 10;
                    }

                    //check black queens
                    temp = gen_dirs_board(dirs_l, dirs_u, black_q_pos[i]);
                    dirs_l = temp[0];
                    dirs_u = temp[1];
                    res1_l = black_web1_l & dirs_l;
                    res1_u = black_web1_u & dirs_u;
                    if (count_bits(res1_l, res1_u) < 3)
                    {
                        res2_l = res1_l & white_web1_l;
                        res2_u = res1_u & white_web1_u;
                        if (!(((res1_l ^ res2_l)!=0) || ((res1_u ^ res2_u)!=0)))
                            black_misc -= 10;
                    }
                }

                for (i=0; i<2; i++)
                {
                    if (((s.white_bd[i]) & (low_quad)) == 0)
                        white_misc -= 5;
                    else if (((s.black_bd[i]) & (low_quad)) == 0)
                        white_misc += 5;  //give bonus points for owning a quadrant

                    if (((s.white_bd[i]) & (high_quad)) == 0)
                        white_misc -= 5;
                    else if (((s.black_bd[i]) & (high_quad)) == 0)
                        white_misc += 5;  //give bonus points for owning a quadrant

                    if (((s.black_bd[i]) & (low_quad)) == 0)
                        black_misc -= 5;
                    else if (((s.white_bd[i]) & (low_quad)) == 0)
                        black_misc += 5;

                    if (((s.black_bd[i]) & (high_quad)) == 0)
                        black_misc -= 5;
                    else if (((s.white_bd[i]) & (high_quad)) == 0)
                        black_misc += 5;
                }

                for (i=0; i<100; i++)
                {
                    if (i > 49)
                    {
                        if (((board_u) & (((long) 0x1 << (i % 50))))!=0)
                            continue;
                    }
                    else
                    {
                        if (((board_l) & (((long) 0x1 << (i % 50))))!=0)
                            continue;
                    }

                    web_board_l = web_board_u = 0;
                    long[] temp = gen_web_board(web_board_l, web_board_u, board_l, board_u, i);
                    web_board_l = temp[0];
                    web_board_u = temp[1];


                    if (s.turn == 1) //player 1 moves next
                    {
                        if ((web_board_l & s.white_bd[0])!=0 || (web_board_u & s.white_bd[1])!=0)
                        {
                            //printf("Square %d is owned by white - level 0\n", i);
                            ++own1L0;
                            if ((web_board_l & s.black_bd[0])!=0 || (web_board_u & s.black_bd[1])!=0)
                            own1L0 -= 0.5;
                        }
                        else if ((web_board_l & s.black_bd[0])!=0 || (web_board_u & s.black_bd[1])!=0)
                        {
                            //printf("Square %d is owned by black - level 0\n", i);
                            ++own2L0;
                        }
                        else if ((web_board_l & white_web1_l)!=0 || (web_board_u & white_web1_u)!=0)
                    {
                        //printf("Square %d is owned by white - level 1\n", i);
                        ++own1L1;
                        if ((web_board_l & black_web1_l)!=0 || (web_board_u & black_web1_u)!=0)
                            own1L1 -= 0.5;
                    }
                    else if ((web_board_l & black_web1_l)!=0 || (web_board_u & black_web1_u)!=0)
                    {
                        //printf("Square %d is owned by black - level 1\n", i);
                        ++own2L1;
                    }
                    }
                    else
                    {
                        if ((web_board_l & s.black_bd[0])!=0 || (web_board_u & s.black_bd[1])!=0)
                        {
                            //printf("Square %d is owned by black - level 0\n", i);
                            ++own2L0;
                            if ((web_board_l & s.white_bd[0])!=0 || (web_board_u & s.white_bd[1])!=0)
                            own2L0 -= 0.5;
                        }
                        else if ((web_board_l & s.white_bd[0])!=0 || (web_board_u & s.white_bd[1])!=0)
                        {
                            ++own1L0;
                            //printf("Square %d is owned by white - level 0\n", i);
                        }
                        else if ((web_board_l & black_web1_l)!=0 || (web_board_u & black_web1_u)!=0)
                    {
                        //printf("Square %d is owned by black - level 1\n", i);
                        ++own2L1;
                        if ((web_board_l & white_web1_l)!=0 || (web_board_u & white_web1_u)!=0)
                            own2L1 -= 0.5;
                    }
                    else if ((web_board_l & white_web1_l)!=0 || (web_board_u & white_web1_u)!=0)
                    {
                        //printf("Square %d is owned by white - level 1\n", i);
                        ++own1L1;
                    }
                    }
                }
                return (((own1L0 + own1L1) * 1.5 + p1 + white_misc) - (p2 + (own2L0 + own2L1) * 1.5 + black_misc));
            }

            private long[] gen_web_board(long web_l, long web_u, long board_l, long board_u, int pos) {
                short row, col, fdiag, bdiag;
                short web_row, web_col, web_fdiag, web_bdiag;
                int diag;

                //row web
                if (pos > 49)
                    row = GET_ROW(board_u, GET_COL_POS(pos));
                else
                    row = GET_ROW(board_l, GET_COL_POS(pos));

                web_row = gen_web_stream_plus(row, GET_ROW_POS(pos), 10);
                //printf("Row for pos %d:", pos);

                if (pos > 49)
                    web_u = PUT_ROW(web_u, (short) (pos/10), web_row);
                else
                    web_l = PUT_ROW(web_l, (short) (pos/10), web_row);


                        //col web
                col = GET_COL(board_l, board_u, (short) (pos%10));
                web_col = gen_web_stream_plus(col, GET_COL_POS(pos), 10);


                web_l = PUT_HALF_COL(web_l, (short) (pos%10),web_col);
                web_u = PUT_HALF_COL(web_u, (short) (pos%10),web_col);


                        //fdiag web
                        diag = GET_FDIAG(pos);
                fdiag = (short) get_forward_diag(board_l, board_u, diag);
                web_fdiag = gen_web_stream_plus(fdiag, GET_FDIAG_POS(pos), GET_FDIAG_LEN(diag));



                long[] temp = put_forward_diag(web_l, web_u, web_fdiag, diag);
                web_l = temp[0];
                web_u = temp[1];


                        //bdiag web
                        diag = GET_BDIAG(pos);
                bdiag = (short) get_back_diag(board_l, board_u, diag);
                web_bdiag = gen_web_stream_plus(bdiag, GET_BDIAG_POS(pos), GET_BDIAG_LEN(diag));



                temp = put_back_diag(web_l, web_u, web_bdiag, diag);
                web_l = temp[0];
                web_u = temp[1];

                long[] ans = new long[2];
                ans[0] = web_l;
                ans[1] = web_u;
                return ans;
            }

            private int count_bits(long board_l, long board_u) {
                int count = 0;
                int i;

                for (i=0; i< 64; i++)
                {
                    if ((board_l & 0x1)!=0)
                        ++count;
                    board_l >>= 1;

                    if ((board_u & 0x1)!=0)
                        ++count;
                    board_u >>= 1;
                }
                return count;
            }

            private long[] gen_dirs_board(long board_l, long board_u, int pos) {
                int row = GET_COL_POS(pos);
                int row_adj = row % 5;
                int pos_adj = pos % 50;
                long board_ptr;

                long final_board_l = board_l, final_board_u = board_u;

            /* Generate top row */
                if (row < 9){ //position is not against top border, generate this row
                    if (pos > 39) {
                        //use upper board
                        board_ptr = final_board_u;
                        if (pos == 40) {
                            final_board_u = (board_ptr |= 0x3);
                        } else {
                            final_board_u = (board_ptr |= (((long) 0x7 << ((pos_adj + 9) % 50)) & ((long) 0x3ff << (((row_adj + 1) % 5) * 10))));
                        }
                    } else {
                        board_ptr = final_board_l;
                        if (pos == 40) {
                            final_board_l = (board_ptr |= 0x3);
                        } else {
                            final_board_l = (board_ptr |= (((long) 0x7 << ((pos_adj + 9) % 50)) & ((long) 0x3ff << (((row_adj + 1) % 5) * 10))));
                        }
                    }
                    //The 2nd half of this expressions is a row bitmask that takes care of
                    //positions next to the left or right borders, ensuring that the bits
                    //placed on the board stay in the row
                }

            /* Generate middle row */
                if (pos < 50) {
                    board_ptr = final_board_l;  //otherwise board_ptr is still pointing to board_u}
                    if (pos_adj == 0) {
                        final_board_l = (board_ptr |= (long) (0x2)); //in bottom left corner of board half, can't shift neg
                    } else {
                        final_board_l = (board_ptr |= (((long) 0x5 << (pos_adj - 1)) & ((long) 0x3ff << (row_adj * 10))));
                    }
                } else {
                    board_ptr = final_board_u;  //otherwise board_ptr is still pointing to board_u
                    if (pos_adj == 0) {
                        final_board_u = (board_ptr |= (long) (0x2)); //in bottom left corner of board half, can't shift neg
                    } else {
                        final_board_u = (board_ptr |= (((long) 0x5 << (pos_adj - 1)) & ((long) 0x3ff << (row_adj * 10))));
                    }
                }




            /* Generate bottom row */
                if (row > 0){ //position is not against bottom border, generate this row
                    if (pos < 60) {
                        board_ptr = final_board_l;  //otherwise board_ptr is still pointing to board_u}
                        if (pos_adj == 10)
                            final_board_l = (board_ptr |= (long) 0x3); //in bottom left corner of board half, can't shift neg
                        else
                            final_board_l = (board_ptr |= (((long) 0x7 << ((pos - 11) % 50)) & ((long) 0x3ff << (((row - 1) % 5) * 10))));
                    } else {
                        board_ptr = final_board_u;  //otherwise board_ptr is still pointing to board_u
                        if (pos_adj == 10)
                            final_board_u = (board_ptr |= (long) 0x3); //in bottom left corner of board half, can't shift neg
                        else
                            final_board_u = (board_ptr |= (((long) 0x7 << ((pos - 11) % 50)) & ((long) 0x3ff << (((row - 1) % 5) * 10))));
                    }


                    }
                long[] temp = new long[2];
                temp[0] = final_board_l;
                temp[1] = final_board_u;
                return temp;
            }

            private int GET_COL_POS(int pos) {
                return (int)(pos / 10);
            }

            private int GET_ROW_POS(int pos) {
                return pos % 10;
            }

            private long PUT_ROW(long board, short row, short stream) {
                return board |= ((long) stream << ((row % 5) * 10));
            }
            private int gen_web_board_count(long web_l, long web_u, long board_l, long board_u, int pos) {
                short row, col, fdiag, bdiag;
                short web_row, web_col, web_fdiag, web_bdiag;
                int diag;
                int row_count, col_count, fdiag_count, bdiag_count;

                if (pos > 49)
                    row = GET_ROW(board_u, GET_COL_POS(pos));
                else
                    row = GET_ROW(board_l, GET_COL_POS(pos));

                web_row = gen_web_stream(row, GET_ROW_POS(pos), 10);
                row_count = count_contig_bits(web_row, 10);

                if (pos > 49)
                    web_u = PUT_ROW(web_u, (short) (pos/10),web_row);
                else
                    web_l = PUT_ROW(web_l, (short) (pos/10),web_row);
                col = GET_COL(board_l, board_u, (short) (pos%10));

                web_col = gen_web_stream(col, GET_COL_POS(pos), 10);
                col_count = count_contig_bits(web_col, 10);

                web_l = PUT_HALF_COL(web_l, (short) (pos%10),web_col);
                web_u = PUT_HALF_COL(web_u, (short) (pos%10),web_col>>5);

                diag = GET_FDIAG(pos);
                fdiag = (short) get_forward_diag(board_l, board_u, diag);
                web_fdiag = gen_web_stream(fdiag, GET_FDIAG_POS(pos), GET_FDIAG_LEN(diag));
                fdiag_count = count_contig_bits(web_fdiag, GET_FDIAG_LEN(diag));

                long[] temp = put_forward_diag(web_l, web_u, web_fdiag, diag);
                web_l = temp[0];
                web_u = temp[1];

                diag = GET_BDIAG(pos);
                bdiag = (short) get_back_diag(board_l, board_u, diag);
                web_bdiag = gen_web_stream(bdiag, GET_BDIAG_POS(pos), GET_BDIAG_LEN(diag));
                bdiag_count = count_contig_bits(web_bdiag, GET_BDIAG_LEN(diag));

                temp = put_back_diag(web_l, web_u, web_bdiag, diag);
                web_l = temp[0];
                web_u = temp[1];

                return (row_count + col_count + fdiag_count + bdiag_count - 4);
            }

            private long[] put_back_diag(long board_l, long board_u, short stream, int diag) {
                int len = GET_BDIAG_LEN(diag);
                int i;
                int pos=diag;
                short mask = 0x1;

                long final_board_l = board_l, final_board_u = board_u;
                long board;

                for (i=0; i<len; i++)
                {
                    //This needs some funky footwork to get bits in the early 50's
                    if (pos + i > 49) {
                        if (pos > 49) {
                            board = final_board_u;
                            final_board_u = (board |= ((long) (stream & mask) << (pos % 50)));
                        } else {
                            board = final_board_l;
                            final_board_l = (board |= ((long) (stream & mask) >> (50 - pos)));
                        }
                    } else {
                        board = final_board_l;
                        final_board_l = (board |= ((long) (stream & mask ) << pos));
                    }

                    pos +=8;
                    mask <<= 1;
                }
                long[] temp = new long[2];
                temp[0] = final_board_l;
                temp[1] = final_board_u;
                return temp;
            }

            private int GET_BDIAG_POS(int b) {
                return (b/10 < (10 - b%10)) ? b/10 : 9 - b%10;
            }

            private int get_back_diag(long board_l, long board_u, int diag) {
                int len = GET_BDIAG_LEN(diag);
                int i;
                short res = 0;
                int pos=diag;
                short mask = 0x1;

                for (i=0; i<=len; i++)
                {
                    //This needs some funky footwork to get bits in the early 50's
                    if (pos + i > 49)
                    {
                        if (pos > 49)
                        {
                            res |= (board_u >> (pos % 50)) & mask;
                        }
                        else
                        {
                            res |= (board_u << (50 - pos)) & mask;
                        }
                    }
                    else
                        res |= (board_l >> pos) & mask;

                    pos +=8;
                    mask <<= 1;
                }

                return res;
            }

            private int GET_BDIAG_LEN(int bdiag) {
                return (bdiag < 10) ? bdiag + 1 : (10 - bdiag/10);
            }

            private int GET_BDIAG(int b) {
                return (b/10 < (10 - b%10)) ? b - ((b/10) * 9) : b - ((9 - (b%10)) * 9);
            }

            private long[] put_forward_diag(long board_l, long board_u, short stream, int diag) {
                int len = GET_FDIAG_LEN(diag);
                int i;
                long board;
                int pos=diag;
                short mask = 0x1;

                long final_board_l = board_l, final_board_u = board_u;

                for (i=0; i<len; i++)
                {
                    if (pos > 49) {
                        board = final_board_u;
                        final_board_u = (board |= ((long) (stream & mask) << (pos % 50)));
                    } else {
                        board = final_board_l;
                        final_board_l = (board |= ((long) (stream & mask) << (pos % 50)));
                    }

                    pos +=10;
                    mask <<= 1;
                }
                long[] ans = new long[2];
                ans[0] = final_board_l;
                ans[1] = final_board_u;
                return ans;
            }

            private int GET_FDIAG_POS(int f) {
                return ((f%10 > f/10) ? f/10 : f%10);
            }

            private int get_forward_diag(long board_l, long board_u, int diag) {
                int len = GET_FDIAG_LEN(diag);
                int i;
                short res = 0;
                long board;
                int pos=diag;
                short mask = 0x1;

                for (i=0; i<len; i++)
                {
                    if (pos > 49)
                        board = board_u;
                    else
                    board = board_l;

                    res |= (board >> (pos % 50)) & mask;
                    pos +=10;
                    mask <<= 1;
                }

                return res;
            }

            private int GET_FDIAG_LEN(int fdiag) {
                return (fdiag < 10) ? (10 - fdiag) : (10 - fdiag/10);
            }

            private int GET_FDIAG(int f) {
                return (f%10 > f/10) ? f - ((f/10) * 11) : f - ((f%10) * 11);
            }

            private long PUT_HALF_COL(long board, short col, long stream) {
               return board |= (((stream & 0x1) << col) |
                ((stream & 0x2) << (col + 9)) |
                ((stream & 0x4) << (col + 18)) |
                ((stream & 0x8) << (col + 27)) |
                ((stream & 0x10) << (col + 36)));
            }

            private short gen_web_stream_plus(short stream, int pos, int len) {
                short web = 0;
                int i;

                web |= 0x1 << pos;

                for (i=pos-1; i>=0; i--)
                {
                    if ((stream & (0x1 << i))!=0)
                    {
                        web |= 0x1 << i;
                        break;
                    }
                    else
                        web |= 0x1 << i;
                }

                for (i=pos+1; i<len; i++)
                {
                    if ((stream & (0x1 << i))!=0)
                    {
                        web |= 0x1 << i;
                        break;
                    }
                    else
                        web |= 0x1 << i;
                }

                return web;
            }

            private short GET_COL(long board_l, long board_u, short col) {
                return (short) (GET_HALF_COL(board_l, col) | (GET_HALF_COL(board_u, col) << 5));
            }

            private long GET_HALF_COL(long board, short col) {
                return (((board >> col) &0x1) | ((board >> (col + 9)) &0x2) | ((board >> (col + 18)) &0x4) | ((board >> (col + 27)) &0x8) | ((board >> (col + 36)) &0x10));
            }

            private int count_contig_bits(short stream, int len) {
                int i;
                int count = 0;

                for (i=0; i<len; i++)
                {
                    if ((stream & (0x1 << i))!=0)
                        ++count;
                    else if (count != 0)
                        return count;
                }

                return count;
            }

            private short gen_web_stream(short stream, int pos, int len) {
                short web = 0;
                int i;

                web |= 0x1 << pos;

                for (i=pos-1; i>=0; i--)
                {
                    if ((stream & (0x1 << i)) != 0)
                        break;
                    else
                        web |= 0x1 << i;
                }

                for (i=pos+1; i<len; i++)
                {
                    if ((stream & (0x1 << i)) != 0)
                        break;
                    else
                        web |= 0x1 << i;
                }

                return web;
            }

            private short GET_ROW(long board, int row) {
                return (short) ((board >> (row%5) * 10) & 0x3ff);
            }

            private static class State {

                public long[] white_bd = new long[2];
                public long[] black_bd = new long[2];
                public long[] blocks_bd = new long[2];

                int[] white_q_x = new int[4];
                int[] white_q_y = new int[4];

                int[] black_q_x = new int[4];
                int[] black_q_y = new int[4];

                char turn;
                short value;
                char depth;
                char winner;

                public State() {
                    winner = 0;
                    turn = 1;
                    blocks_bd[0] = blocks_bd[1] = 0;
                    white_bd[0] = white_bd[1] = 0;
                    black_bd[0] = black_bd[1] = 0;
                    for (int i = 0; i < 4; i++) {

                        white_q_x[i] = 0;
                        white_q_y[i] = 0;
                        black_q_x[i] = 0;
                        black_q_y[i] = 0;
                    }
                    depth = 0;
                    value = 0;
                }
            }
        }
    }
}
