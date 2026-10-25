package stud.g01.problem.npuzzle.heuristic;

import core.problem.State;
import stud.g01.problem.npuzzle.PuzzleBoard;

/**
 * 启发函数共用的目标布局计算。
 *
 * <p>三个基础启发函数都按目标棋盘建立“将牌编号 -> 目标位置”的查表，
 * 避免每次估值都对棋盘做 O(n^2) 的反向查找。</p>
 */
final class GoalLayout {

    private GoalLayout() {
    }

    /**
     * 把框架中的通用状态转换为棋盘。
     *
     * @param state 待转换的状态
     * @param role  出现在异常信息中的角色名，例如“当前状态”
     * @return 同一对象的棋盘视图
     */
    static PuzzleBoard asBoard(State state, String role) {
        if (!(state instanceof PuzzleBoard board)) {
            throw new IllegalArgumentException(role + "必须是 PuzzleBoard");
        }

        return board;
    }

    /**
     * 两个棋盘必须同尺寸，否则位置换算没有意义。
     */
    static void requireSameSize(PuzzleBoard board, PuzzleBoard goal) {
        if (board.getSize() != goal.getSize()) {
            throw new IllegalArgumentException(
                    "棋盘尺寸不一致：当前状态 " + board.getSize()
                            + "，目标状态 " + goal.getSize()
            );
        }
    }

    /**
     * 建立将牌编号到目标位置的一维下标表。
     *
     * <p>PuzzleBoard 保证棋盘数据是 0 到 size * size - 1 的排列，
     * 因此表中每一项都会被赋值，不需要额外的缺失检查。</p>
     *
     * @param goal 目标棋盘
     * @return 数组下标为将牌编号，元素为该将牌在目标棋盘中的一维下标
     */
    static int[] indexByTile(PuzzleBoard goal) {
        int[] indexByTile = new int[goal.getSize() * goal.getSize()];

        for (int index = 0; index < indexByTile.length; index++) {
            indexByTile[goal.getTile(index)] = index;
        }

        return indexByTile;
    }

    /**
     * 两个一维下标之间在棋盘上的曼哈顿距离。
     */
    static int distance(int fromIndex, int toIndex, int size) {
        return Math.abs(fromIndex / size - toIndex / size)
                + Math.abs(fromIndex % size - toIndex % size);
    }
}
