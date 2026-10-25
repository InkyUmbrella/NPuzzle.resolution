package stud.g01.queue;

import core.problem.State;
import core.solver.queue.EvaluationType;
import core.solver.queue.Frontier;
import core.solver.queue.Node;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.PriorityQueue;

/**
 * 基于 {@link PriorityQueue} 的 Frontier（Open 表）实现。
 *
 * <p>出队顺序由 {@link Node} 提供的评估器决定：{@link EvaluationType#FULL} 按 f = g + h
 * 升序排列，f 相同时取 g 较小的（较浅的）结点；{@link EvaluationType#PATH_COST} 按 g 升序，
 * 即 Dijkstra；{@link EvaluationType#HEURISTIC} 按 h 升序，即 Greedy Best-First。</p>
 *
 * <p>同一状态只允许在队列中保留一个结点：新结点入队时，若队列中已经有该状态的结点，
 * 则保留两者中评估值更小的一个，另一个被丢弃。删除旧结点时使用
 * {@link PriorityQueue#remove(Object)}，它依赖 {@link Node#equals(Object)}（按状态比较），
 * 因此每次删除需要 O(n) 扫描堆内数组；在 NPuzzle 的规模下这个开销可以接受。</p>
 *
 * <p>去重索引以 {@link State} 为键。{@link Node} 重写了 equals 但没有重写 hashCode，
 * 不能直接放进 HashSet/HashMap，所以这里不使用 Node 作为键。</p>
 */
public class PqFrontier implements Frontier {

    // 结点优先级比较器，决定 poll 的顺序
    private final Comparator<Node> evaluator;

    // 二叉小顶堆
    private final PriorityQueue<Node> heap;

    // 状态 -> 队列中该状态的结点，用于 O(1) 判断状态是否已在 Frontier 中
    private final Map<State, Node> frontierIndex;

    /**
     * @param type 结点评估策略，决定出队顺序
     */
    public PqFrontier(EvaluationType type) {
        this(Node.evaluator(Objects.requireNonNull(type, "评估类型不能为 null")));
    }

    /**
     * @param evaluator 结点比较器，一般使用 {@link Node#evaluator(EvaluationType)}
     */
    public PqFrontier(Comparator<Node> evaluator) {
        this.evaluator = Objects.requireNonNull(evaluator, "结点比较器不能为 null");
        this.heap = new PriorityQueue<>(evaluator);
        this.frontierIndex = new HashMap<>();
    }

    /**
     * 取出并删除评估值最小的结点。
     *
     * @return 队首结点；队列为空时返回 null
     */
    @Override
    public Node poll() {
        Node node = heap.poll();

        if (node != null) {
            frontierIndex.remove(node.getState());
        }

        return node;
    }

    /**
     * 清空队列和去重索引。每次开始新的搜索前必须调用，否则上一轮的状态会残留。
     */
    @Override
    public void clear() {
        heap.clear();
        frontierIndex.clear();
    }

    @Override
    public int size() {
        return heap.size();
    }

    @Override
    public boolean isEmpty() {
        return heap.isEmpty();
    }

    /**
     * 判断某个状态是否已经在 Frontier 中（按状态比较，与结点的 g、h 无关）。
     *
     * @param node 待查询的结点
     * @return 队列中已有相同状态的结点时返回 true
     */
    @Override
    public boolean contains(Node node) {
        Objects.requireNonNull(node, "结点不能为 null");

        return frontierIndex.containsKey(node.getState());
    }

    /**
     * 插入结点。队列中已有相同状态的结点时，保留评估值更小的那个。
     *
     * @param node 要插入的结点
     * @return true：结点入队（或替换了较差的旧结点）；false：结点被丢弃
     */
    @Override
    public boolean offer(Node node) {
        Objects.requireNonNull(node, "结点不能为 null");

        State state = node.getState();
        Node existing = frontierIndex.get(state);

        if (existing == null) {
            heap.offer(node);
            frontierIndex.put(state, node);

            return true;
        }

        // 旧结点不比新结点差，丢弃新结点
        if (evaluator.compare(existing, node) <= 0) {
            return false;
        }

        heap.remove(existing);
        heap.offer(node);
        frontierIndex.put(state, node);

        return true;
    }
}
