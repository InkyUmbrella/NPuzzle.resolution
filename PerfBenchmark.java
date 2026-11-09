package dmodule.test;

import core.problem.Problem;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.queue.EvaluationType;
import core.solver.queue.Frontier;
import core.solver.queue.Node;
import stud.g01.queue.PqFrontier;
import stud.g01.solver.IDAStarSolver;
import stud.g01.solver.AStarSolver;
import stud.g01.solver.SearchResult;
import stud.g01.problem.npuzzle.NPuzzleProblem;
import stud.g01.problem.npuzzle.PuzzleBoard;
import dmodule.pattern.DisjointPDBPredictor;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Deque;

public class PerfBenchmark {
    private final PuzzleInstanceGenerator generator;
    private final PuzzleBoard goal;

    public PerfBenchmark() {
        generator = new PuzzleInstanceGenerator();
        goal = generator.getGoal();
    }

    private Problem buildProblem(PuzzleBoard init, PuzzleBoard goal) {
        return new NPuzzleProblem(init, goal);
    }

    private SearchResult runOne(Problem problem, Predictor predictor, boolean useIDA) {
        long start = System.currentTimeMillis();
        Deque<Node> path;
        int expandCnt;
        if(useIDA){
            Frontier frontier = new PqFrontier(EvaluationType.FULL);
            IDAStarSolver solver = new IDAStarSolver(frontier, predictor);
            path = solver.search(problem);
            expandCnt = solver.getExpandedCount();
        }else{
            Frontier frontier = new PqFrontier(EvaluationType.FULL);
            AStarSolver solver = new AStarSolver(frontier, predictor);
            path = solver.search(problem);
            expandCnt = solver.getExpandedCount();
        }
        long timeCost = System.currentTimeMillis() - start;
        return new SearchResult(path, expandCnt, timeCost);
    }

    // 原有方法保留（跑 Hard 样例的 IDA*）
    public void runBenchmark(int testCount, String csvPath) {
        try (FileWriter writer = new FileWriter(csvPath)) {
            writer.write("id,isSuccess,expandedNodes,timeMs\n");
            Predictor predictor = new DisjointPDBPredictor(goal, false);
            for (int i = 0; i < testCount; i++) {
                PuzzleBoard startState = generator.getHardExamInstance();
                Problem problem = buildProblem(startState, goal);
                SearchResult res = runOne(problem, predictor, true); // 使用 IDA*
                String line = String.format("%d,%b,%d,%d\n",
                        i, res.isFound(), res.getExpandedCount(), res.getTimeMs());
                writer.write(line);
                System.out.println("Hard 测试样例 " + i + " 完成: found=" + res.isFound()
                        + ", expanded=" + res.getExpandedCount() + ", time=" + res.getTimeMs() + "ms");
            }
            writer.flush();
            System.out.println("Hard 样例全部跑完，结果输出到：" + csvPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // 新增方法：随机样例，可选择用 IDA* 还是 A*
    public void runBenchmarkWithRandomAndType(int testCount, String csvPath, boolean useIDA) {
        try (FileWriter writer = new FileWriter(csvPath)) {
            writer.write("id,isSuccess,expandedNodes,timeMs\n");
            Predictor predictor = new DisjointPDBPredictor(goal, false);
            for (int i = 0; i < testCount; i++) {
                PuzzleBoard startState = generator.generateSolvable(1).get(0);
                Problem problem = buildProblem(startState, goal);
                SearchResult res = runOne(problem, predictor, useIDA);

                String line = String.format("%d,%b,%d,%d\n",
                        i, res.isFound(), res.getExpandedCount(), res.getTimeMs());
                writer.write(line);
                System.out.println("随机样例 " + i + " 完成: found=" + res.isFound()
                        + ", expanded=" + res.getExpandedCount() + ", time=" + res.getTimeMs() + "ms");
            }
            writer.flush();
            System.out.println("随机样例全部跑完，结果输出到：" + csvPath);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        PerfBenchmark benchmark = new PerfBenchmark();

        // 跑 5 个随机样例，使用 A* (传 false)
        System.out.println("========== 开始测试随机 15-Puzzle 性能 (A*) ==========");
        benchmark.runBenchmarkWithRandomAndType(5, "random_result.csv", false);

        // 跑 Hard 样例，使用 IDA* (传 true)
        System.out.println("\n========== 开始测试第三阶段最难关卡 (IDA*) ==========");
        benchmark.runBenchmark(1, "hard_result.csv");
    }
}
