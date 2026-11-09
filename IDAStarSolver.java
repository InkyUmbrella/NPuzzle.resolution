package stud.g01.solver;

import core.problem.Problem;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.algorithm.searcher.AbstractSearcher;
import core.solver.queue.Frontier;
import core.solver.queue.Node;

import java.util.ArrayDeque;
import java.util.Deque;

public class IDAStarSolver extends AbstractSearcher {
    private final Predictor predictor;

    public IDAStarSolver(Frontier frontier, Predictor predictor) {
        super(frontier);
        this.predictor = predictor;
    }

    @Override
    public Deque<Node> search(Problem problem) {
        super.nodesExpanded = 0;
        super.nodesGenerated = 0;

        Node start = problem.root(predictor);
        int threshold = start.evaluation();
        int round = 0;

        while (true) {
            round++;
            // 如果你需要看日志，可以取消下面这行的注释
            // System.out.println("[IDA*] 第 " + round + " 轮，阈值=" + threshold);

            ResultWrap res = dfs(problem, start, threshold, null);
            if (res.found) {
                return res.path;
            }
            if (res.nextThreshold == Integer.MAX_VALUE) {
                return null; // 无解
            }
            threshold = res.nextThreshold; // 直接跳到下一轮的最小超限值
        }
    }

    private ResultWrap dfs(Problem problem, Node curr, int threshold, Node parent) {
        int g = curr.getPathCost();
        int h = curr.getHeuristic();
        int f = g + h;

        if (f > threshold) {
            return new ResultWrap(false, null, f);
        }

        if (problem.goal(curr.getState())) {
            return new ResultWrap(true, generatePath(curr), threshold);
        }

        super.nodesExpanded++;
        int minNextTh = Integer.MAX_VALUE;

        for (Node child : problem.childNodes(curr, predictor)) {
            super.nodesGenerated++;

            // 【核心优化】防止走回头路：如果子节点的状态和祖父节点一样，直接跳过
            if (parent != null && child.getState().equals(parent.getState())) {
                continue;
            }

            ResultWrap subResult = dfs(problem, child, threshold, curr);
            if (subResult.found) {
                return subResult;
            }
            minNextTh = Math.min(minNextTh, subResult.nextThreshold);
        }
        return new ResultWrap(false, null, minNextTh);
    }

    @Override
    protected Deque<Node> generatePath(Node goalNode) {
        Deque<Node> path = new ArrayDeque<>();
        Node curr = goalNode;
        while (curr != null) {
            path.addFirst(curr);
            curr = curr.getParent();
        }
        return path;
    }

    private static class ResultWrap {
        boolean found;
        Deque<Node> path;
        int nextThreshold;
        ResultWrap(boolean found, Deque<Node> path, int nextThreshold) {
            this.found = found;
            this.path = path;
            this.nextThreshold = nextThreshold;
        }
    }

    public int getExpandedCount() {
        return super.nodesExpanded();
    }
}
