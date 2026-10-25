package stud.g01.queue;

import core.problem.State;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Closed 表（Explored）封装：记录已经扩展过的状态，避免重复扩展。
 *
 * <p>状态相等与哈希由具体问题的 State 实现决定，例如
 * {@code stud.g01.problem.npuzzle.PuzzleBoard} 按棋盘边长和数字排列比较。
 * 状态进入本集合后不允许再修改参与相等判断与哈希计算的数据。</p>
 *
 * <p>框架的 {@code AbstractSearcher} 目前直接使用 {@code HashSet<State>} 作为 Explored，
 * 该类不替换框架字段，供 IDA* 的路径去重和自行实现的搜索流程复用；
 * 如果需要改动框架字段，先与 C 约定影响范围再提交。</p>
 */
public final class ExploredSet {

    private final Set<State> explored = new HashSet<>();

    /**
     * 加入状态。
     *
     * @param state 被扩展的状态
     * @return true：该状态此前不在集合中；false：已扩展过
     */
    public boolean add(State state) {
        return explored.add(requireState(state));
    }

    public boolean contains(State state) {
        return explored.contains(requireState(state));
    }

    /**
     * 移除状态。IDA* 等算法需要在回溯时把状态移出当前路径集合。
     *
     * @return true：状态存在并被移除
     */
    public boolean remove(State state) {
        return explored.remove(requireState(state));
    }

    public void clear() {
        explored.clear();
    }

    public int size() {
        return explored.size();
    }

    public boolean isEmpty() {
        return explored.isEmpty();
    }

    private static State requireState(State state) {
        return Objects.requireNonNull(state, "状态不能为 null");
    }
}
