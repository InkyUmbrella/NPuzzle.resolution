package stud.g01.runner;

import core.problem.Problem;
import core.runner.EngineFeeder;
import core.solver.algorithm.heuristic.HeuristicType;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.queue.EvaluationType;
import core.solver.queue.Frontier;
import stud.g01.problem.npuzzle.heuristic.ManhattanHeuristic;
import stud.g01.problem.npuzzle.heuristic.MisplacedHeuristic;
import stud.g01.queue.PqFrontier;

import java.util.ArrayList;
import java.util.Objects;

/**
 * NPuzzle 的素材提供者：从输入文件读入问题，按评估类型生成 Frontier，按启发函数类型生成 Predictor。
 *
 * <p>启发函数与容器由 B 提供；输入文件的解析（{@link #getProblems}）由 C 在 runner 模块完成。</p>
 */
public class PuzzleFeeder extends EngineFeeder {

    @Override
    public ArrayList<Problem> getProblems(ArrayList<String> problemLines) {
        return null;    // 由 C 实现：输入格式见 README（size + 初始棋盘 + 目标棋盘）
    }

    /**
     * 按评估策略生成 Frontier。
     *
     * @param type {@link EvaluationType#FULL} 对应 A*，{@link EvaluationType#PATH_COST} 对应 Dijkstra
     * @return 使用该评估策略的优先队列
     */
    @Override
    public Frontier getFrontier(EvaluationType type) {
        return new PqFrontier(Objects.requireNonNull(type, "评估类型不能为 null"));
    }

    /**
     * 按启发函数类型生成 Predictor。
     *
     * <p>第一阶段只使用不在位将牌数与曼哈顿距离。
     * 不相交模式数据库（{@link HeuristicType#DISJOINT_PATTERN}）由 D 提供，尚未接入。</p>
     *
     * @param type 启发函数类型
     * @return 对应的启发函数实例
     * @throws UnsupportedOperationException PDB 尚未接入
     * @throws IllegalArgumentException      传入与该问题无关的启发函数类型
     */
    @Override
    public Predictor getPredictor(HeuristicType type) {
        Objects.requireNonNull(type, "启发函数类型不能为 null");

        return switch (type) {
            case MISPLACED -> new MisplacedHeuristic();
            case MANHATTAN -> new ManhattanHeuristic();
            case DISJOINT_PATTERN -> throw new UnsupportedOperationException(
                    "不相交模式数据库启发函数由 D 提供，尚未接入");
            default -> throw new IllegalArgumentException(
                    "NPuzzle 不支持启发函数类型：" + type);
        };
    }
}
