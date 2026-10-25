package stud.g01.problem.npuzzle.heuristic;

import core.problem.State;
import core.solver.algorithm.heuristic.Predictor;
import stud.g01.problem.npuzzle.PuzzleBoard;

/**
 * 汉明距离（不在位将牌数）启发函数：统计非空格且不在目标位置上的将牌个数。
 *
 * <p>每次移动最多让一个将牌归位，因此该启发函数不会高估实际剩余代价（admissible），
 * 但比曼哈顿距离更弱，扩展的结点更多。目标状态下返回 0。</p>
 */
public final class MisplacedHeuristic implements Predictor {

    @Override
    public int heuristics(State state, State goal) {
        PuzzleBoard board = GoalLayout.asBoard(state, "被估值状态");
        PuzzleBoard target = GoalLayout.asBoard(goal, "目标状态");

        GoalLayout.requireSameSize(board, target);

        int misplaced = 0;

        for (int index = 0; index < board.getSize() * board.getSize(); index++) {
            int tile = board.getTile(index);

            // 空格不参与比较
            if (tile == 0) {
                continue;
            }

            if (tile != target.getTile(index)) {
                misplaced++;
            }
        }

        return misplaced;
    }

    @Override
    public String toString() {
        return "不在位将牌数";
    }
}
