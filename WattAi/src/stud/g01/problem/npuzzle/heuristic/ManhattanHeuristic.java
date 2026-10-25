package stud.g01.problem.npuzzle.heuristic;

import core.problem.State;
import core.solver.algorithm.heuristic.Predictor;
import stud.g01.problem.npuzzle.PuzzleBoard;

/**
 * 曼哈顿距离启发函数：所有将牌当前位置到目标位置的曼哈顿距离之和，空格不计入。
 *
 * <p>每次移动只把一个将牌移入相邻格子，一步最多让单个将牌的曼哈顿距离减少 1，
 * 因此该启发函数不会高估实际剩余代价（admissible），可以保证 A* 求得最优解。</p>
 *
 * <p>目标状态下返回 0；将牌位置按目标布局换算，支持任意合法目标棋盘，
 * 不要求目标是“按行递增、空格在末尾”的标准布局。</p>
 */
public final class ManhattanHeuristic implements Predictor {

    @Override
    public int heuristics(State state, State goal) {
        PuzzleBoard board = GoalLayout.asBoard(state, "被估值状态");
        PuzzleBoard target = GoalLayout.asBoard(goal, "目标状态");

        GoalLayout.requireSameSize(board, target);

        int size = board.getSize();
        int[] goalIndex = GoalLayout.indexByTile(target);
        int distance = 0;

        for (int index = 0; index < size * size; index++) {
            int tile = board.getTile(index);

            // 空格不计入距离
            if (tile == 0) {
                continue;
            }

            distance += GoalLayout.distance(index, goalIndex[tile], size);
        }

        return distance;
    }

    @Override
    public String toString() {
        return "曼哈顿距离";
    }
}
