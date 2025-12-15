import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;
import java.util.Random;

class CPUPlayer {

    private final Random rng = new Random();
    private int numExploredNodes;
    private final char cpuTeam;
    private static final int INF = 1_000_000_000;
    private static final int WIN_SCORE = 100;
    private static final int LOSE_SCORE = -100;
    private static final int DRAW_SCORE = 0;
    private Move ttBestMove = null; // restera null tant que tu n'as pas de TT

    // profondeur limite : change la valeur si tu veux chercher plus loin
    private static final int MAX_DEPTH = 5;

    private void orderMoves(ArrayList<Move> ms, Board b, char side) {
        ms.sort((a, c) -> Boolean.compare(b.isCapture(c), b.isCapture(a)));
    }

    public CPUPlayer(char cpu) {
        cpuTeam = cpu;
    }

    public int getNumOfExploredNodes() {
        return numExploredNodes;
    }

    private char otherTeam(char t) {
        return (t == 'R') ? 'N' : 'R';
    }

    private static final long TIME_LIMIT_MS = 3000;
    private long endTime;
    private Move bestSoFar;

    public Move think(Board b, char me) {
        endTime = System.currentTimeMillis() + TIME_LIMIT_MS;
        Move best = null;
        int bestVal = -INF;

        for (int D = 1; D <= MAX_DEPTH; D++) {
            if (System.currentTimeMillis() > endTime)
                break;

            ArrayList<Move> root = b.getAvailableMoves(me);
            orderMoves(root, b, me);

            int localBestVal = -INF;
            ArrayList<Move> localBestMoves = new ArrayList<>();

            for (Move m : root) {
                if (System.currentTimeMillis() > endTime)
                    break;

                b.play(m);
                int val = alphaBeta(b, otherTeam(me), -INF, INF, D - 1);
                b.remove(m);

                if (val > localBestVal) {
                    localBestVal = val;
                    localBestMoves.clear();
                    localBestMoves.add(m);
                } else if (val == localBestVal) {
                    localBestMoves.add(m);
                }
            }

            if (!localBestMoves.isEmpty()) {
                // pick a random best move at this depth
                Move localBest = localBestMoves.get(rng.nextInt(localBestMoves.size()));
                best = localBest;
                bestVal = localBestVal;
            } else {
                break;
            }
        }

        if (best == null) {
            ArrayList<Move> ms = b.getAvailableMoves(me);
            best = ms.isEmpty() ? null : ms.get(0);
        }
        return best;
    }

    private int alphaBeta(Board board, char side, int alpha, int beta, int depthLeft) {
        if (System.currentTimeMillis() > endTime)
            return 0;
        numExploredNodes++;

        if (depthLeft == 0) {
            return board.evaluate(board, cpuTeam);
        }

        int eval = board.evaluate(board, cpuTeam);
        if (Math.abs(eval) >= 999_999)
            return eval;

        ArrayList<Move> moves = board.getAvailableMoves(side);
        if (moves.isEmpty())
            return board.evaluate(board, cpuTeam);

        orderMoves(moves, board, side);

        if (side == cpuTeam) { // MAX
            int value = -INF;
            for (Move m : moves) {
                if (System.currentTimeMillis() > endTime)
                    break;
                board.play(m);
                value = Math.max(value, alphaBeta(board, otherTeam(side), alpha, beta, depthLeft - 1));
                board.remove(m);
                alpha = Math.max(alpha, value);
                if (alpha >= beta)
                    break;
            }
            return value;
        } else { // MIN
            int value = INF;
            for (Move m : moves) {
                if (System.currentTimeMillis() > endTime)
                    break;
                board.play(m);
                value = Math.min(value, alphaBeta(board, otherTeam(side), alpha, beta, depthLeft - 1));
                board.remove(m);
                beta = Math.min(beta, value);
                if (alpha >= beta)
                    break;
            }
            return value;
        }
    }

    private int minimax(Board board, int depth, int alpha, int beta, char cpu) {
        numExploredNodes++;

        // Terminal node or depth limit
        int eval = board.evaluate(board, cpuTeam);
        if (depth == 0 || eval == WIN_SCORE || eval == LOSE_SCORE) {
            return eval;
        }

        if (cpuTeam == cpu) {
            int maxEval = Integer.MIN_VALUE;

            for (Move m : board.getAvailableMoves(cpuTeam)) {
                board.play(m);

                int evalChild = minimax(board, depth - 1, alpha, beta, otherTeam(cpu));
                maxEval = Math.max(maxEval, evalChild);

                board.remove(m);

                alpha = Math.max(alpha, evalChild);
                if (beta <= alpha)
                    break; // PRUNE
            }

            return maxEval;

        } else { // Minimizing player
            int minEval = Integer.MAX_VALUE;

            for (Move m : board.getAvailableMoves(otherTeam(cpuTeam))) {
                board.play(m);

                int evalChild = minimax(board, depth - 1, alpha, beta, cpu);
                minEval = Math.min(minEval, evalChild);

                board.remove(m);

                beta = Math.min(beta, evalChild);
                if (beta <= alpha)
                    break; // PRUNE
            }

            return minEval;
        }
    }

    public ArrayList<Move> getNextMoveAB(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> win = new ArrayList<>();
        ArrayList<Move> tie = new ArrayList<>();
        ArrayList<Move> lose = new ArrayList<>();

        for (Move move : board.getAvailableMoves(cpuTeam)) {
            board.play(move);
            int score = alphaBeta(board, otherTeam(cpuTeam), Integer.MIN_VALUE, Integer.MAX_VALUE, 5);
            board.remove(move);
            if (score == WIN_SCORE)
                win.add(move);
            else if (score == DRAW_SCORE)
                tie.add(move);
            else
                lose.add(move);
        }
        if (win.isEmpty() && tie.isEmpty()) {
            return lose;
        }
        return !win.isEmpty() ? win : tie;
    }

    public ArrayList<Move> getNextMoveMinMax(Board board) {
        numExploredNodes = 0;
        ArrayList<Move> win = new ArrayList<>();
        ArrayList<Move> tie = new ArrayList<>();

        for (Move move : board.getAvailableMoves(cpuTeam)) {
            board.play(move);
            int score = minimax(board, MAX_DEPTH, INF, -INF, cpuTeam);
            board.remove(move);

            if (score == WIN_SCORE)
                win.add(move);
            else if (score == DRAW_SCORE)
                tie.add(move);
        }
        return !win.isEmpty() ? win : tie; // si win !empty, envoit win sinon envoit tie
    }

    public char getTeam() {
        return cpuTeam;
    }
}
