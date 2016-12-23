import entity.Amazon;
import entity.GameBoard;
import entity.Move;
import evaluation.FunctionEvaluation;
import evaluation.WebEvaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Random;

/**
 * Created by amareelez on 23.12.16.
 */
@SuppressWarnings("JavaDoc") class AI {

    private GameBoard board = null;

    public AI(GameBoard b) {
        this.board = b;
    }

    public Move.MoveAndBoard getNextMove(GameBoard b) {
        this.board = b;
        //TestClass2.AI.Edge nextMove = GreedyBestSearch(2);
        return minimaxSearch(1000, 4, 3);
    }

    // currently not using
    private Move.MoveAndBoard GreedyBestSearch(int advance) {
        //we need to check to see if any of our amazons are almost blocked in (having a freedom of 1 or 2). If so then we need to move them immediately and not worry about anything else
        Amazon current = checkForDireAmazon();

        ArrayList<Move.MoveAndBoard> searchResults = new ArrayList<>();
        // if we found an amazon that needs to be moved, only look at its possible moves.
        if (current != null) {
            //get only moves from the amazon
            searchResults.addAll(board.getPossibleMoves(current.id));
            if (searchResults.size() == 0) {
                for (int i = 0; i < 4; i++) {
                    searchResults.addAll(board.getPossibleMoves(i));
                }
            }
        } else {
            searchResults = new ArrayList<>();
            for (int i = 0; i < 4; i++) {
                searchResults.addAll(board.getPossibleMoves(i));
            }
        }

        Move.MoveAndBoard newState;
        if (advance == 0)
            newState = defaultHeuristic(searchResults);
        else if (advance == 1)
            newState = advancedHeuristic(searchResults);
        else
            newState = superHeuristic(searchResults);

        return newState;
    }


    /**
     * @param timer - Amount of seconds to restrict the search to.
     * @param depth - The maximum depth to expand within the amount of time.
     * @return Recommended next move.
     */
    private Move.MoveAndBoard minimaxSearch(int timer, int depth, int advance) {
        SearchNode initialNode = new SearchNode(new Move.MoveAndBoard(board, null));
        long endtime = System.currentTimeMillis() + timer;
        return minimax(initialNode, true, endtime, depth, advance).state;
    }

    private SearchNode minimax(SearchNode node, boolean max, long endtime, int depth, int advance) {
        if ((System.currentTimeMillis() - TestClass2.time > 500) || (depth <= 0))
            return new SearchNode(node.state,
                new WebEvaluation(node.state.getNewBoard()).Evaluate());
        double alpha;
        ArrayList<SearchNode> children;
        if (advance == 1) {
            children = getAdvancedChildren(node, max);
            alpha = max ? Double.MIN_VALUE : Double.MAX_VALUE;
        } else if (advance == 0) {
            children = getChildren(node, max);
            alpha = max ? Integer.MIN_VALUE : Integer.MAX_VALUE;
        } else {
            children = getSuperChildren(node, max);
            alpha = max ? Double.MIN_VALUE : Double.MAX_VALUE;
        }
        SearchNode nextBestNode = null;
        for (SearchNode aChildren : children) {
            if (System.currentTimeMillis() - TestClass2.time > 500)
                break;
            nextBestNode = minimax(aChildren, !max, endtime, depth - 1, advance);
            alpha = max ?
                Math.max(alpha, nextBestNode.heuristic) :
                Math.min(alpha, nextBestNode.heuristic);
            nextBestNode.heuristic = alpha;
        }
        return nextBestNode;
    }

    private ArrayList<SearchNode> getSuperChildren(SearchNode node, boolean ourTurn) {
        ArrayList<SearchNode> children = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int j = ourTurn ? i : i + 4;
            ArrayList<Move.MoveAndBoard> amazonMoves = node.state.getNewBoard().getPossibleMoves(j);
            for (Move.MoveAndBoard amazonMove : amazonMoves) {
                children.add(new SearchNode(amazonMove,
                    new WebEvaluation(amazonMove.getNewBoard()).Evaluate()));
            }
        }
        return children;
    }

    private ArrayList<SearchNode> getChildren(SearchNode node, boolean ourTurn) {
        ArrayList<SearchNode> children = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int j = ourTurn ? i : i + 4;
            ArrayList<Move.MoveAndBoard> amazonMoves = node.state.getNewBoard().getPossibleMoves(j);
            for (Move.MoveAndBoard amazonMove : amazonMoves) {
                children
                    .add(new SearchNode(amazonMove, evaluateHeuristic(amazonMove.getNewBoard())));
            }
        }
        return children;
    }

    private ArrayList<SearchNode> getAdvancedChildren(SearchNode node, boolean ourTurn) {
        ArrayList<SearchNode> children = new ArrayList<>();
        for (int i = 0; i < 4; i++) {
            int j = ourTurn ? i : i + 4;
            ArrayList<Move.MoveAndBoard> amazonMoves = node.state.getNewBoard().getPossibleMoves(j);
            for (Move.MoveAndBoard amazonMove : amazonMoves) {
                children.add(new SearchNode(amazonMove,
                    new FunctionEvaluation(amazonMove.getNewBoard()).Evaluate()));
            }
        }
        return children;
    }

    private Amazon checkForDireAmazon() {
        Amazon dire = null;
        for (int j = 0; j < 4; j++) {
            if (board.getOurFreedom(board.Amazons[j]) <= 2) {
                dire = board.Amazons[j];
            }
        }
        return dire;
    }

    private Move.MoveAndBoard defaultHeuristic(ArrayList<Move.MoveAndBoard> moves) {
        ArrayList<Integer> evaluations = new ArrayList<>(moves.size());
        for (int i = 0; i < moves.size(); i++) {
            evaluations.add(i, evaluateHeuristic(moves.get(i).getNewBoard()));
        }

        Integer max = Collections.max(evaluations);
        return moves.get(evaluations.indexOf(max));
    }

    private Move.MoveAndBoard advancedHeuristic(ArrayList<Move.MoveAndBoard> moves) {
        ArrayList<Double> evaluations = new ArrayList<>(moves.size());
        for (int i = 0; i < moves.size(); i++) {
            evaluations.add(i, new FunctionEvaluation(moves.get(i).getNewBoard()).Evaluate());
        }

        Double max = Collections.max(evaluations);
        return moves.get(evaluations.indexOf(max));
    }

    private Move.MoveAndBoard superHeuristic(ArrayList<Move.MoveAndBoard> moves) {
        ArrayList<Double> evaluations = new ArrayList<>(moves.size());
        for (int i = 0; i < moves.size(); i++) {
            evaluations.add(i, new WebEvaluation(moves.get(i).getNewBoard()).Evaluate());
        }

        Double max = Collections.max(evaluations);
        return moves.get(evaluations.indexOf(max));
    }

    private Move.MoveAndBoard randomHeuristic(ArrayList<Move.MoveAndBoard> moves) {
        ArrayList<Integer> evaluations = new ArrayList<>(moves.size());
        for (int i = 0; i < moves.size(); i++) {
            evaluations.add(i, evaluateRandomHeuristic(moves.get(i).getNewBoard()));
        }

        Integer max = Collections.max(evaluations);
        return moves.get(evaluations.indexOf(max));
    }

    private int evaluateRandomHeuristic(GameBoard board) {
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
     *
     * @param board
     * @return Returns the overall freedom of our amazons. The bigger the better.
     */
    private int getOurFreedom(GameBoard board) {
        return board.getOurFreedoms();
    }

    /**
     * This will return how many of the spaces are ours minus the number of spaces that are theirs.
     *
     * @param board
     * @return Number of spaces that are ours - spaces theirs. Larger the better
     */
    private int getSpaceConfiguration(GameBoard board) {
        //Set containing all the board squares movable to by our TestClass2.AI.AmazonUnit players.
        HashSet<Integer> A;
        A = board.ourSpaces();

        //Set containing all the board squares movable to by the opponents players.
        HashSet<Integer> B;
        B = board.theirSpaces();

        HashSet<Integer> C = new HashSet<>(A);
        A.removeAll(B);
        B.removeAll(C);

        return A.size() - B.size();
    }


    public static class SearchNode {
        final Move.MoveAndBoard state;
        double heuristic;

        public SearchNode(Move.MoveAndBoard state) {
            this.state = state;
            this.heuristic = 0;
        }

        public SearchNode(Move.MoveAndBoard state, double heuristic) {
            this.state = state;
            this.heuristic = heuristic;
        }

        public Move.MoveAndBoard getState() {
            return this.state;
        }

        public double heuristic() {
            return heuristic;
        }
    }


}
