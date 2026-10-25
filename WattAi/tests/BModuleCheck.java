import core.problem.Action;
import core.problem.Problem;
import core.problem.State;
import core.solver.algorithm.heuristic.Predictor;
import core.solver.algorithm.searcher.AbstractSearcher;
import core.solver.algorithm.searcher.BestFirstSearcher;
import core.solver.queue.EvaluationType;
import core.solver.queue.Node;
import stud.g01.problem.npuzzle.NPuzzleProblem;
import stud.g01.problem.npuzzle.PuzzleBoard;
import stud.g01.problem.npuzzle.heuristic.BlankDistanceHeuristic;
import stud.g01.problem.npuzzle.heuristic.ManhattanHeuristic;
import stud.g01.problem.npuzzle.heuristic.MisplacedHeuristic;
import stud.g01.queue.ExploredSet;
import stud.g01.queue.PqFrontier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * B 模块（搜索容器 + 基础启发函数）的验证程序，不依赖测试框架。
 *
 * <p>验证内容：</p>
 * <ul>
 *   <li>曼哈顿距离、不在位将牌数、空位距离的计算规则：目标状态为 0、空格不计入、手算样例正确；</li>
 *   <li>Frontier 的去重、更优结点替换、出队顺序、清空行为；</li>
 *   <li>Explored 集合的增删查；</li>
 *   <li>用 {@code PqFrontier} + 基础启发函数跑通 A*，解路径逐步合法，且与 Dijkstra 的最优步数一致；</li>
 *   <li>无解状态不返回解路径。</li>
 * </ul>
 *
 * <p>运行方式（在仓库根目录）：</p>
 * <pre>
 * javac -encoding UTF-8 -d ./out/cli (Get-ChildItem ./WattAi/src -Recurse -Filter '*.java').FullName `
 *     ./WattAi/tests/BModuleCheck.java
 * java -cp ./out/cli BModuleCheck ./WattAi/resources/problems.txt
 * </pre>
 *
 * <p>第二个参数传 all 时额外运行 4x4 用例（默认只跑 3x3，避免长时间实验）。</p>
 */
public final class BModuleCheck {

    private static final int[] GOAL_3X3 = {1, 2, 3, 4, 5, 6, 7, 8, 0};
    private static final int[] GOAL_4X4 = {
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 0
    };

    private static int checks = 0;
    private static final List<String> failures = new ArrayList<>();

    private BModuleCheck() {
    }

    public static void main(String[] args) throws IOException {
        Locale.setDefault(Locale.CHINA);

        String problemFile = args.length > 0 ? args[0] : "WattAi/resources/problems.txt";
        boolean includeLarge = args.length > 1 && "all".equalsIgnoreCase(args[1]);

        checkHeuristicsOnHandComputedBoards();
        checkHeuristicsOnRandomBoards();
        checkFrontier();
        checkExploredSet();
        checkSolver(problemFile, includeLarge);
        checkUnsolvable();

        System.out.println();
        System.out.println("检查项：" + checks + "，失败：" + failures.size());

        if (!failures.isEmpty()) {
            for (String failure : failures) {
                System.out.println("  FAIL " + failure);
            }
            System.exit(1);
        }

        System.out.println("结果：全部通过");
    }

    // ---------------------------------------------------------------- 启发函数

    private static void checkHeuristicsOnHandComputedBoards() {
        System.out.println("== 启发函数：手算样例 ==");

        ManhattanHeuristic manhattan = new ManhattanHeuristic();
        MisplacedHeuristic misplaced = new MisplacedHeuristic();
        BlankDistanceHeuristic blank = new BlankDistanceHeuristic();

        PuzzleBoard goal3 = new PuzzleBoard(3, GOAL_3X3);
        PuzzleBoard goal4 = new PuzzleBoard(4, GOAL_4X4);

        // 目标状态：三个启发函数都应为 0
        checkEquals("曼哈顿：目标状态为 0", 0, manhattan.heuristics(goal3, goal3));
        checkEquals("不在位将牌：目标状态为 0", 0, misplaced.heuristics(goal3, goal3));
        checkEquals("空位距离：目标状态为 0", 0, blank.heuristics(goal3, goal3));

        // 只把 8 和空格对调：8 走一格，空格也走一格
        PuzzleBoard oneSwap = new PuzzleBoard(3, new int[]{1, 2, 3, 4, 5, 6, 7, 0, 8});
        checkEquals("曼哈顿：8 与空格对调", 1, manhattan.heuristics(oneSwap, goal3));
        checkEquals("不在位将牌：8 与空格对调", 1, misplaced.heuristics(oneSwap, goal3));
        checkEquals("空位距离：8 与空格对调", 1, blank.heuristics(oneSwap, goal3));

        // 8 在左上角、1 在空格左侧：8 到目标位置 7 走 3 格，1 到目标位置 0 走 3 格
        PuzzleBoard corner = new PuzzleBoard(3, new int[]{8, 2, 3, 4, 5, 6, 7, 1, 0});
        checkEquals("曼哈顿：8 与 1 错位", 6, manhattan.heuristics(corner, goal3));
        checkEquals("不在位将牌：8 与 1 错位", 2, misplaced.heuristics(corner, goal3));
        checkEquals("空位距离：空格已在目标位置", 0, blank.heuristics(corner, goal3));

        // 空格在最前，将牌整体左移一格：3 和 6 要跨两列，其余各错一格
        PuzzleBoard blankFirst = new PuzzleBoard(3, new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8});
        checkEquals("曼哈顿：空格在最前", 12, manhattan.heuristics(blankFirst, goal3));
        checkEquals("不在位将牌：空格在最前", 8, misplaced.heuristics(blankFirst, goal3));
        checkEquals("空位距离：空格在最前", 4, blank.heuristics(blankFirst, goal3));

        // 4x4：15 与空格对调
        int[] tiles4x4 = GOAL_4X4.clone();
        tiles4x4[15] = 15;
        tiles4x4[14] = 0;
        PuzzleBoard swap4x4 = new PuzzleBoard(4, tiles4x4);
        checkEquals("曼哈顿：4x4 一格格子", 1, manhattan.heuristics(swap4x4, goal4));
        checkEquals("不在位将牌：4x4 一格格子", 1, misplaced.heuristics(swap4x4, goal4));
        checkEquals("空位距离：4x4 一格格子", 1, blank.heuristics(swap4x4, goal4));

        // 尺寸不一致时必须报错，避免把 3x3 状态当 4x4 估值
        boolean thrown = false;
        try {
            manhattan.heuristics(oneSwap, goal4);
        } catch (IllegalArgumentException expected) {
            thrown = true;
        }
        check("曼哈顿：尺寸不一致时报错", thrown);
    }

    private static void checkHeuristicsOnRandomBoards() {
        System.out.println("== 启发函数：随机棋盘的不变量 ==");

        ManhattanHeuristic manhattan = new ManhattanHeuristic();
        MisplacedHeuristic misplaced = new MisplacedHeuristic();
        BlankDistanceHeuristic blank = new BlankDistanceHeuristic();
        PuzzleBoard goal3 = new PuzzleBoard(3, GOAL_3X3);

        Random random = new Random(2026);
        boolean nonNegative = true;
        boolean manhattanNotSmaller = true;
        boolean blankNotLarger = true;

        for (int round = 0; round < 200; round++) {
            int[] tiles = shuffled(GOAL_3X3, random);
            PuzzleBoard board = new PuzzleBoard(3, tiles);

            int h1 = manhattan.heuristics(board, goal3);
            int h2 = misplaced.heuristics(board, goal3);
            int h3 = blank.heuristics(board, goal3);

            nonNegative &= h1 >= 0 && h2 >= 0 && h3 >= 0;
            // 每张错位将牌的曼哈顿距离至少为 1
            manhattanNotSmaller &= h1 >= h2;
            // 空格最多跨两行两列
            blankNotLarger &= h3 <= 4;
        }

        check("随机棋盘：启发值非负", nonNegative);
        check("随机棋盘：曼哈顿 >= 不在位将牌数", manhattanNotSmaller);
        check("随机棋盘：空格距离 <= 4", blankNotLarger);
    }

    private static int[] shuffled(int[] source, Random random) {
        int[] tiles = source.clone();

        for (int i = tiles.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            int swap = tiles[i];
            tiles[i] = tiles[j];
            tiles[j] = swap;
        }

        return tiles;
    }

    // ---------------------------------------------------------------- 容器

    private static void checkFrontier() {
        System.out.println("== Frontier：去重、替换、顺序、清空 ==");

        PuzzleBoard a = new PuzzleBoard(3, new int[]{1, 2, 3, 4, 5, 6, 7, 0, 8});
        PuzzleBoard b = new PuzzleBoard(3, new int[]{1, 2, 3, 4, 5, 6, 0, 7, 8});
        PuzzleBoard c = new PuzzleBoard(3, new int[]{1, 2, 3, 4, 0, 6, 7, 5, 8});

        PqFrontier frontier = new PqFrontier(EvaluationType.FULL);
        check("Frontier：初始为空", frontier.isEmpty() && frontier.size() == 0);
        check("Frontier：空队列 poll 返回 null", frontier.poll() == null);

        // 同一状态的两个结点，f 分别为 10 和 5
        Node worse = new Node(a, null, null, 5, 5);
        Node better = new Node(a, null, null, 2, 3);

        check("Frontier：首次入队成功", frontier.offer(worse));
        checkEquals("Frontier：入队后 size 为 1", 1, frontier.size());
        check("Frontier：contains 按状态判断", frontier.contains(better));

        check("Frontier：更优结点替换成功", frontier.offer(better));
        checkEquals("Frontier：替换后仍只有一个结点", 1, frontier.size());

        Node polled = frontier.poll();
        checkEquals("Frontier：出队的是更优结点", 2, polled.getPathCost());
        check("Frontier：出队后队列为空且不再包含该状态",
                frontier.isEmpty() && !frontier.contains(worse));

        // 较差结点被丢弃
        PqFrontier discard = new PqFrontier(EvaluationType.FULL);
        Node good = new Node(a, null, null, 4, 2);
        Node bad = new Node(a, null, null, 6, 4);
        discard.offer(good);
        check("Frontier：较差结点被丢弃", !discard.offer(bad));
        checkEquals("Frontier：丢弃后仍只有一个结点", 1, discard.size());
        checkEquals("Frontier：保留原有结点", 4, discard.poll().getPathCost());

        // 出队顺序按 f 升序
        PqFrontier order = new PqFrontier(EvaluationType.FULL);
        order.offer(new Node(a, null, null, 10, 20));   // f = 30
        order.offer(new Node(b, null, null, 5, 5));     // f = 10
        order.offer(new Node(c, null, null, 10, 10));   // f = 20

        boolean ascending = order.poll().getHeuristic() == 5
                && order.poll().getHeuristic() == 10
                && order.poll().getHeuristic() == 20;
        check("Frontier：按 f 升序出队", ascending);

        // f 相同时取 g 较小的（较浅的）结点
        PqFrontier tie = new PqFrontier(EvaluationType.FULL);
        tie.offer(new Node(a, null, null, 4, 6));   // f = 10
        tie.offer(new Node(b, null, null, 2, 8));   // f = 10
        checkEquals("Frontier：f 相同时先出浅层结点", 2, tie.poll().getPathCost());

        // PATH_COST 只按 g 排序
        PqFrontier dijkstra = new PqFrontier(EvaluationType.PATH_COST);
        dijkstra.offer(new Node(a, null, null, 5, 0));
        dijkstra.offer(new Node(b, null, null, 2, 99));
        checkEquals("Frontier：PATH_COST 按 g 出队", 2, dijkstra.poll().getPathCost());

        // clear 必须把去重索引一并清空
        PqFrontier cleared = new PqFrontier(EvaluationType.FULL);
        cleared.offer(new Node(a, null, null, 1, 1));
        cleared.clear();
        check("Frontier：clear 后为空且不包含旧状态",
                cleared.isEmpty() && cleared.size() == 0 && !cleared.contains(worse));
        check("Frontier：clear 后可以重新插入同一状态",
                cleared.offer(new Node(a, null, null, 1, 1)) && cleared.size() == 1);
    }

    private static void checkExploredSet() {
        System.out.println("== Explored：状态去重 ==");

        ExploredSet explored = new ExploredSet();
        PuzzleBoard a = new PuzzleBoard(3, new int[]{1, 2, 3, 4, 5, 6, 7, 0, 8});
        PuzzleBoard same = new PuzzleBoard(3, new int[]{1, 2, 3, 4, 5, 6, 7, 0, 8});
        PuzzleBoard b = new PuzzleBoard(3, new int[]{1, 2, 3, 4, 5, 6, 0, 7, 8});

        check("Explored：首次加入返回 true", explored.add(a));
        check("Explored：相同状态重复加入返回 false", !explored.add(same));
        check("Explored：contains 按状态判断", explored.contains(same) && !explored.contains(b));
        checkEquals("Explored：size 为 1", 1, explored.size());
        check("Explored：移除后不再包含", explored.remove(a) && explored.isEmpty());
        explored.add(a);
        explored.clear();
        check("Explored：clear 后为空", explored.isEmpty() && explored.size() == 0);
    }

    // ---------------------------------------------------------------- 求解流程

    private static void checkSolver(String problemFile, boolean includeLarge) throws IOException {
        System.out.println("== A* 端到端：解路径合法且最优 ==");

        List<String> lines = Files.readAllLines(Path.of(problemFile), StandardCharsets.UTF_8);
        int caseIndex = 0;

        for (String line : lines) {
            String trimmed = line.trim();

            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                continue;
            }

            caseIndex++;
            int[] values = parseInts(trimmed);
            int size = values[0];
            int cells = size * size;

            if (values.length < 1 + 2 * cells) {
                failures.add("用例" + caseIndex + "：输入数据长度不足");
                continue;
            }

            if (size != 3 && !includeLarge) {
                System.out.printf("  [%d] %dx%d 用例已跳过（加参数 all 运行）%n", caseIndex, size, size);
                continue;
            }

            int[] initialTiles = new int[cells];
            int[] goalTiles = new int[cells];
            System.arraycopy(values, 1, initialTiles, 0, cells);
            System.arraycopy(values, 1 + cells, goalTiles, 0, cells);

            solveOneProblem(caseIndex, size, initialTiles, goalTiles);
        }

        check("端到端：至少运行了一个用例", caseIndex > 0);
    }

    private static void solveOneProblem(int caseIndex, int size, int[] initialTiles, int[] goalTiles) {
        PuzzleBoard initial = new PuzzleBoard(size, initialTiles);
        PuzzleBoard goal = new PuzzleBoard(size, goalTiles);
        NPuzzleProblem problem = new NPuzzleProblem(initial, goal, size);

        System.out.printf("%n  [%d] %dx%d 初始状态 %s%n", caseIndex, size, size, initial);

        if (!problem.solvable()) {
            System.out.println("      不可解，跳过");
            return;
        }

        Integer optimal = null;

        if (size == 3) {
            // Dijkstra 作为最优步数的基准
            RunResult dijkstra = run(problem, initial, goal,
                    new PqFrontier(EvaluationType.PATH_COST), (state, target) -> 0, "Dijkstra");
            optimal = dijkstra.moves();

            RunResult manhattan = run(problem, initial, goal,
                    new PqFrontier(EvaluationType.FULL), new ManhattanHeuristic(), "曼哈顿");
            RunResult misplaced = run(problem, initial, goal,
                    new PqFrontier(EvaluationType.FULL), new MisplacedHeuristic(), "不在位将牌数");

            checkEquals("用例" + caseIndex + "：曼哈顿 A* 与 Dijkstra 步数一致",
                    optimal, manhattan.moves());
            checkEquals("用例" + caseIndex + "：不在位将牌 A* 与 Dijkstra 步数一致",
                    optimal, misplaced.moves());

            // admissible：起点估计值不能超过实际最优步数
            check("用例" + caseIndex + "：曼哈顿不高估",
                    new ManhattanHeuristic().heuristics(initial, goal) <= optimal);
            check("用例" + caseIndex + "：不在位将牌不高估",
                    new MisplacedHeuristic().heuristics(initial, goal) <= optimal);

            // 曼哈顿更紧，扩展结点不应多于不在位将牌数
            check("用例" + caseIndex + "：曼哈顿扩展结点不多于不在位将牌数",
                    manhattan.expanded() <= misplaced.expanded());
        } else {
            run(problem, initial, goal,
                    new PqFrontier(EvaluationType.FULL), new ManhattanHeuristic(), "曼哈顿");
        }
    }

    private static RunResult run(Problem problem, PuzzleBoard initial, PuzzleBoard goal,
                                 PqFrontier frontier, Predictor predictor, String label) {
        AbstractSearcher searcher = new BestFirstSearcher(frontier, predictor);

        long start = System.nanoTime();
        Deque<Node> path = searcher.search(problem);
        long elapsedNanos = System.nanoTime() - start;

        double elapsedMillis = elapsedNanos / 1_000_000.0;

        if (path == null) {
            System.out.printf("      %-12s 无解%n", label);
            failures.add(label + "：未找到解");
            return new RunResult(-1, searcher.nodesGenerated(), searcher.nodesExpanded(), elapsedMillis);
        }

        boolean legal = pathLegal(problem, initial, goal, path);

        System.out.printf("      %-12s 步数 %2d  生成 %6d  扩展 %6d  用时 %8.2f ms%n",
                label, path.size(), searcher.nodesGenerated(), searcher.nodesExpanded(), elapsedMillis);

        check(label + "：解路径逐步合法并到达目标", legal);

        return new RunResult(path.size(), searcher.nodesGenerated(), searcher.nodesExpanded(), elapsedMillis);
    }

    /**
     * 从初始状态逐步重放解路径：每一步动作必须合法，状态必须与结点记录一致，最后到达目标状态。
     */
    private static boolean pathLegal(Problem problem, PuzzleBoard initial, PuzzleBoard goal, Deque<Node> path) {
        State state = initial;
        int steps = 0;

        for (Node node : path) {
            Action action = node.getAction();

            if (action == null || !problem.applicable(state, action)) {
                return false;
            }

            state = state.next(action);
            steps++;

            if (!state.equals(node.getState())) {
                return false;
            }
        }

        return state.equals(goal) && steps == path.size();
    }

    private static void checkUnsolvable() {
        System.out.println();
        System.out.println("== 无解状态：不返回解路径 ==");

        PuzzleBoard goal = new PuzzleBoard(3, GOAL_3X3);
        // 交换目标状态中的 2 和 1，得到 3x3 不可解状态
        PuzzleBoard unsolvable = new PuzzleBoard(3, new int[]{2, 1, 3, 4, 5, 6, 7, 8, 0});
        NPuzzleProblem problem = new NPuzzleProblem(unsolvable, goal, 3);

        check("无解状态：solvable 返回 false", !problem.solvable());

        AbstractSearcher searcher = new BestFirstSearcher(
                new PqFrontier(EvaluationType.FULL), new ManhattanHeuristic());
        check("无解状态：A* 返回 null", searcher.search(problem) == null);
    }

    // ---------------------------------------------------------------- 工具

    private static int[] parseInts(String line) {
        String[] parts = line.split("\\s+");
        int[] values = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            values[i] = Integer.parseInt(parts[i]);
        }

        return values;
    }

    private static void check(String name, boolean condition) {
        checks++;

        if (!condition) {
            failures.add(name);
        }
    }

    private static void checkEquals(String name, int expected, int actual) {
        checks++;

        if (expected != actual) {
            failures.add(name + "：期望 " + expected + "，实际 " + actual);
        }
    }

    private record RunResult(int moves, int generated, int expanded, double millis) {
    }
}
