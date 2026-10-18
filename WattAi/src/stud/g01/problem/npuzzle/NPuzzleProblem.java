package stud.g01.problem.npuzzle;

import core.problem.Action;
import core.problem.Problem;
import core.problem.State;
import core.solver.queue.Node;

import java.util.Deque;

public class NPuzzleProblem extends Problem {

    public NPuzzleProblem(State initialState, State goal) {
        super(initialState, goal);
    }

    public NPuzzleProblem(State initialState, State goal, int size) {
        super(initialState, goal, size);
    }

    private static int inversionCount(int[] tiles) {
        int count = 0;

        for (int i = 0; i < tiles.length; i++) {
            if (tiles[i] == 0) {
                continue;
            }

            for (int j = i + 1; j < tiles.length; j++) {
                if (tiles[j] == 0) {
                    continue;
                }

                if (tiles[i] > tiles[j]) {
                    count++;
                }
            }
        }

        return count;
    }

    private static int blankRowFromBottom(PuzzleBoard board) {
        int rowFromTop = board.getBlankIndex() / board.getSize();

        // 底部行编号从 1 开始
        return board.getSize() - rowFromTop;
    }

    @Override
    public boolean solvable() {
        if (!(initialState instanceof PuzzleBoard start)) {
            return false;
        }

        if (!(goal instanceof PuzzleBoard target)) {
            return false;
        }

        if (start.getSize() != target.getSize()) {
            return false;
        }

        int width = start.getSize();

        int startInversions = inversionCount(start.getTilesCopy());
        int targetInversions = inversionCount(target.getTilesCopy());

        // 棋盘宽度为奇数
        if (width % 2 == 1) {
            return (startInversions % 2)
                    == (targetInversions % 2);
        }

        // 棋盘宽度为偶数
        int startParity =
                (startInversions + blankRowFromBottom(start)) % 2;

        int targetParity =
                (targetInversions + blankRowFromBottom(target)) % 2;

        return startParity == targetParity;
    }
    @Override
    public int stepCost(State state, Action action) {
        if (!(action instanceof PuzzleMove move)) {
            throw new IllegalArgumentException("动作类型必须是 PuzzleMove");
        }

        return move.stepCost();
    }

    @Override
    public boolean applicable(State state, Action action) {
        if (!(state instanceof PuzzleBoard board)) {
            return false;
        }

        if (!(action instanceof PuzzleMove move)) {
            return false;
        }

        return board.canMove(move);
    }

    @Override
    public void showSolution(Deque<Node> path) {

    }
}
