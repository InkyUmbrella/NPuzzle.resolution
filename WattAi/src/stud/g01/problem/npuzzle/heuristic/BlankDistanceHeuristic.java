package stud.g01.problem.npuzzle.heuristic;

import core.problem.State;
import core.solver.algorithm.heuristic.Predictor;
import stud.g01.problem.npuzzle.PuzzleBoard;

/**
 * 空位距离启发函数：空格当前位置到目标空格位置的曼哈顿距离。
 *
 * <p>该值只反映空格还要走多少步，通常远小于实际剩余代价，单独使用时搜索代价接近盲目搜索，
 * 主要用于观察空格移动对搜索规模的影响，也可以和曼哈顿距离一起作为对照。</p>
 *
 * <p>注意：{@code core.solver.algorithm.heuristic.HeuristicType} 目前没有对应枚举，
 * 因此本类先作为独立实现提供，不接入 {@code PuzzleFeeder}；
 * 需要接入时由 C、D 与本模块一起确认是否扩展枚举（core 下的接口改动需先约定影响范围）。</p>
 */
public final class BlankDistanceHeuristic implements Predictor {

    @Override
    public int heuristics(State state, State goal) {
        PuzzleBoard board = GoalLayout.asBoard(state, "被估值状态");
        PuzzleBoard target = GoalLayout.asBoard(goal, "目标状态");

        GoalLayout.requireSameSize(board, target);

        return GoalLayout.distance(
                board.getBlankIndex(),
                target.getBlankIndex(),
                board.getSize()
        );
    }

    @Override
    public String toString() {
        return "空位距离";
    }
}
