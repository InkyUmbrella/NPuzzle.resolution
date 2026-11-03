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

public class AStarSolver extends AbstractSearcher {
    protected final Predictor predictor;

    public AStarSolver(Frontier frontier, Predictor predictor) {
        super(frontier);
        this.predictor = predictor;
    }

    @Override
    public Deque<Node> search(Problem problem) {
        frontier.clear();
        explored.clear();
        nodesExpanded = 0;
        nodesGenerated = 0;

        Node start = problem.root(predictor);
        frontier.offer(start);
        nodesGenerated++;

        while (!frontier.isEmpty()) {
            Node curr = frontier.poll();
            if (problem.goal(curr.getState())) {
                Deque<Node> path = generatePath(curr);
                exportPathToFile(path, "astar_path.txt");
                return path;
            }
            if (explored.contains(curr.getState())) {
                continue;
            }
            explored.add(curr.getState());
            nodesExpanded++;
            List<Node> children = problem.childNodes(curr, predictor);
            for (Node child : children) {
                if (!explored.contains(child.getState())) {
                    frontier.offer(child);
                    nodesGenerated++;
                }
            }
        }
        return null;
    }

    // 导出路径文本，供给任务8可视化模块读取
    private void exportPathToFile(Deque<Node> path, String fileName) {
        if (path == null || path.isEmpty()) return;
        try (FileWriter writer = new FileWriter(fileName)) {
            for (Node node : path) {
                writer.write(node.getState().toString());
                writer.write("\n");
            }
        } catch (IOException e) {
            System.err.println("A* 路径文件写入失败：" + e.getMessage());
        }
    }
}
