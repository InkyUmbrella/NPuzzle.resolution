package stud.g01.solver;

import core.problem.Problem;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.algorithm.searcher.AbstractSearcher;
import core.solver.queue.Frontier;
import core.solver.queue.Node;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Deque;
import java.util.List;

/**
 * IDA* 迭代加深A*算法实现
 * 成员C实现
 */
public class IdAStar extends AbstractSearcher {
    protected final Predictor predictor;

    public IdAStar(Frontier frontier) {
        this(frontier, (state, goal) -> 0);
    }

    public IdAStar(Frontier frontier, Predictor predictor) {
        super(frontier);
        this.predictor = predictor;
    }

    @Override
    public Deque<Node> search(Problem problem) {
        nodesExpanded = 0;
        nodesGenerated = 0;
        Node startNode = problem.root(predictor);
        int limit = startNode.getHeuristic();

        while (true) {
            ResultWrap wrap = dfs(startNode, problem, limit);
            if (wrap.found) {
                exportPathToFile(wrap.path, "idastar_path.txt");
                return wrap.path;
            }
            if (wrap.nextLimit == Integer.MAX_VALUE) {
                return null;
            }
            limit = wrap.nextLimit;
        }
    }

    private ResultWrap dfs(Node node, Problem problem, int limit) {
        int f = node.getPathCost() + node.getHeuristic();
        if (f > limit) {
            return new ResultWrap(false, null, f);
        }
        if (problem.goal(node.getState())) {
            return new ResultWrap(true, generatePath(node), limit);
        }
        nodesExpanded++;
        List<Node> children = problem.childNodes(node, predictor);
        int minNext = Integer.MAX_VALUE;
        for (Node child : children) {
            nodesGenerated++;
            ResultWrap sub = dfs(child, problem, limit);
            if (sub.found) {
                return sub;
            }
            if (sub.nextLimit < minNext) {
                minNext = sub.nextLimit;
            }
        }
        return new ResultWrap(false, null, minNext);
    }

    /**
     * 导出路径文本，给可视化任务8
     */
    protected void exportPathToFile(Deque<Node> path, String filePath) {
        if (path == null || path.isEmpty()) return;
        try (FileWriter writer = new FileWriter(filePath)) {
            for (Node node : path) {
                writer.write(node.getState().toString());
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("IDA*路径文件写入失败：" + e.getMessage());
        }
    }

    private static class ResultWrap {
        boolean found;
        Deque<Node> path;
        int nextLimit;
        ResultWrap(boolean f, Deque<Node> p, int nl) {
            found = f;
            path = p;
            nextLimit = nl;
        }
    }
}
