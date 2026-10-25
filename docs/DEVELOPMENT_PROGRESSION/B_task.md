# B 模块开发记录与接口约定

## 已完成内容

B 负责搜索容器与基础启发函数，代码位于：

```text
WattAi/src/stud/g01/queue/PqFrontier.java
WattAi/src/stud/g01/queue/ExploredSet.java
WattAi/src/stud/g01/problem/npuzzle/heuristic/GoalLayout.java
WattAi/src/stud/g01/problem/npuzzle/heuristic/ManhattanHeuristic.java
WattAi/src/stud/g01/problem/npuzzle/heuristic/MisplacedHeuristic.java
WattAi/src/stud/g01/problem/npuzzle/heuristic/BlankDistanceHeuristic.java
WattAi/src/stud/g01/runner/PuzzleFeeder.java        （getFrontier/getPredictor）
WattAi/tests/BModuleCheck.java                      （验证程序）
```

当前实现包含：

- 基于二叉小顶堆的 Frontier（Open 表），支持按 `EvaluationType` 配置出队顺序；
- 同状态结点的去重索引、更优结点替换、较差结点丢弃；
- Closed 表（Explored）封装；
- 曼哈顿距离、不在位将牌数（汉明距离）、空位距离三个启发函数；
- `PuzzleFeeder` 的 Frontier 与 Predictor 分派；
- 不依赖测试框架的验证程序，覆盖容器行为、启发函数计算规则与 A* 端到端正确性。

## Frontier（Open 表）

### 构造方式

```java
PqFrontier frontier = new PqFrontier(EvaluationType.FULL);
PqFrontier frontier = new PqFrontier(Node.evaluator(EvaluationType.FULL));
```

内部使用 `PriorityQueue<Node>` 存放结点，另用 `HashMap<State, Node>` 记录“状态 -> 队列中的结点”。

### `EvaluationType` 与出队顺序

| 评估策略 | 排序规则 | 对应算法 |
| --- | --- | --- |
| `FULL` | f = g + h 升序，f 相同时取 g 较小的（较浅）结点 | A* |
| `PATH_COST` | g 升序 | Dijkstra |
| `HEURISTIC` | h 升序 | Greedy Best-First |

比较器直接复用框架 `Node.evaluator(type)`，与 `ListFrontier` 的参考实现保持一致，没有重新定义一套排序规则。

### 接口行为

| 方法 | 约定 |
| --- | --- |
| `poll()` | 取出并删除评估值最小的结点；队列为空返回 `null`；同时从去重索引中删除该状态 |
| `offer(Node)` | 状态不在队列中时入队并返回 `true`；已存在时保留更优结点：新结点更优则替换并返回 `true`，否则丢弃新结点并返回 `false` |
| `contains(Node)` | 按状态判断，与结点的 g、h 无关，走索引，O(1) |
| `clear()` | 同时清空堆与去重索引，保证下一轮搜索不残留上一轮状态 |
| `size()` / `isEmpty()` | 直接转发到堆 |

“更优”指 `evaluator.compare(existing, node) > 0`，即新结点的评估值更小。评估值相等时保留原结点，避免同一状态反复替换。

### 实现说明与复杂度

- 去重索引以 `State` 为键。框架 `Node` 重写了 `equals`（按状态比较）但没有重写 `hashCode`，不能直接用作 `HashMap` 的键，所以索引的键使用 `PuzzleBoard`（A 已正确实现 `equals` 与 `hashCode`）。
- 替换结点时调用 `PriorityQueue.remove(oldNode)`，它依赖 `Node.equals` 找到旧结点，需要 O(n) 扫描堆内数组。M 次入队的最坏代价因此是 O(M·n)；在 8-puzzle、15-puzzle 的规模下可以接受，若后续瓶颈出现在这里，可改为惰性删除（入堆时记录代次，出堆时丢弃过期项）。
- 堆的出队与插入依赖比较器，`PqFrontier` 不修改 `Node`，因此不影响 C 的搜索流程。

## Explored（Closed 表）

`ExploredSet` 是对 `HashSet<State>` 的薄封装，提供 `add`、`contains`、`remove`、`clear`、`size`、`isEmpty`。

框架的 `AbstractSearcher` 目前把 Explored 写死为 `HashSet<State>`；`core/` 中的类标注了“同学们不可改写”，所以本次没有改动框架字段。`ExploredSet` 供 IDA* 的路径去重和自行实现的搜索流程使用；如果之后要替换框架里的 `HashSet`，需要先与 C 约定影响范围，并在 PR 中说明。

## 基础启发函数

三个实现都遵循 `core.solver.algorithm.heuristic.Predictor`：

```java
int heuristics(State state, State goal)
```

公共约定：

- 参数必须是 `PuzzleBoard`，尺寸不一致时抛出 `IllegalArgumentException`；
- 目标状态返回 `0`；
- 目标位置由传入的 `goal` 决定，不假设“按行递增、空格在末尾”的固定布局；
- 先从目标棋盘建立“将牌编号 -> 目标下标”的查表（`GoalLayout.indexByTile`），单次估值 O(n²) 遍历棋盘、O(1) 查表。

| 类 | 计算规则 | 是否 admissible |
| --- | --- | --- |
| `ManhattanHeuristic` | 所有非空将牌的当前位置到目标位置的曼哈顿距离之和 | 是 |
| `MisplacedHeuristic` | 非空格且不在目标位置上的将牌个数 | 是 |
| `BlankDistanceHeuristic` | 空格当前位置到目标空格位置的曼哈顿距离 | 是（但很弱） |

一次移动只改变一个将牌的位置，最多让上述距离各减少 1，因此都不会高估实际剩余代价，可以保证 A* 的最优性。

`BlankDistanceHeuristic` 目前**没有接入** `PuzzleFeeder`：`core.solver.algorithm.heuristic.HeuristicType` 是 core 下的枚举，没有对应项，扩展枚举属于跨模块改动，先与 C、D 约定后再改。

## PuzzleFeeder 接线

| 调用 | 返回 |
| --- | --- |
| `getFrontier(EvaluationType.FULL)` | `new PqFrontier(FULL)`，A* |
| `getFrontier(EvaluationType.PATH_COST)` | `new PqFrontier(PATH_COST)`，Dijkstra |
| `getPredictor(HeuristicType.MISPLACED)` | `new MisplacedHeuristic()` |
| `getPredictor(HeuristicType.MANHATTAN)` | `new ManhattanHeuristic()` |
| `getPredictor(HeuristicType.DISJOINT_PATTERN)` | 抛出 `UnsupportedOperationException`，等 D 接入 |
| 其他启发函数类型 | 抛出 `IllegalArgumentException` |

`getProblems()` 仍返回 `null`，由 C 在 runner 模块按 README 的输入格式实现；在 C 完成之前，`SearchTester - NPuzzle` 会因空指针无法运行，B 模块的验证改用 `WattAi/tests/BModuleCheck.java`。

## 与其他模块的接口交接

### 给 C：A* 与 IDA*

```java
Frontier frontier = feeder.getFrontier(EvaluationType.FULL);   // PqFrontier
Predictor predictor = feeder.getPredictor(HeuristicType.MANHATTAN);
AbstractSearcher searcher = new BestFirstSearcher(frontier, predictor);
```

- 每次 `search()` 开始时框架会调用 `frontier.clear()`，`PqFrontier` 会同时清空去重索引，重复使用同一个队列实例是安全的。
- 队列只保证“同一状态保留一个更优结点”；`Explored` 的判定仍由 `AbstractSearcher.expanded()` 完成。
- 若 IDA* 需要路径去重，可直接使用 `stud.g01.queue.ExploredSet`（`add`/`remove` 配合递归回溯）。

### 给 D：PDB 接入

PDB 只需要实现 `Predictor`：`heuristics(State, State)` 返回模式数据库查表值。接入时有两种方式：

1. 与 B、C 约定后扩展 `HeuristicType`，在 `PuzzleFeeder.getPredictor` 中增加分支；
2. 不改枚举，由实验脚本直接 `new BestFirstSearcher(new PqFrontier(EvaluationType.FULL), new PdbHeuristic(data))` 注入。

建议采用方式 1，保证 `SearchTester` 阶段 3（`DISJOINT_PATTERN`）可以直接切换基础启发函数与 PDB 做对比。

## 验证结果

编译与运行（在仓库根目录，PowerShell）：

```powershell
$sources = @(Get-ChildItem ./WattAi/src -Recurse -Filter '*.java' | ForEach-Object FullName)
$sources += (Resolve-Path ./WattAi/tests/BModuleCheck.java).Path
javac -encoding UTF-8 -d ./out/cli @sources
java -cp ./out/cli BModuleCheck ./WattAi/resources/problems.txt
```

加第二个参数 `all` 会额外运行 4×4 用例：

```powershell
java -cp ./out/cli BModuleCheck ./WattAi/resources/problems.txt all
```

`BModuleCheck` 共 69 项检查（加 `all` 运行 4×4 用例后为 70 项），覆盖：

| 分组 | 检查内容 |
| --- | --- |
| 启发函数（手算） | 目标状态为 0；8 与空格对调的三种距离；8、1 错位时的曼哈顿与错位数；空格在最前时曼哈顿为 12、错位数 8、空位距离 4；4×4 一格错位；尺寸不一致报错 |
| 启发函数（随机） | 200 个随机 3×3 棋盘：启发值非负、曼哈顿 ≥ 错位数、空位距离 ≤ 4 |
| Frontier | 空队列 poll 返回 null；按状态 contains；更优结点替换后 size 仍为 1；较差结点被丢弃；按 f 升序出队；f 相同时先出浅层结点；`PATH_COST` 按 g 出队；`clear()` 连索引一起清空 |
| Explored | 相同状态重复加入返回 false、移除、清空 |
| A* 端到端 | 解路径逐步重放合法、末状态等于目标、步数与 Dijkstra 基准一致、启发函数不高估、曼哈顿扩展结点不多于错位数 |
| 无解状态 | 交换目标状态中 1 和 2 的 3×3 棋盘，`solvable()` 为 false，A* 返回 `null` |

`WattAi/resources/problems.txt` 的 3×3 用例结果（JDK 21.0.5，本机）：

| 用例 | 算法 / 启发函数 | 步数 | 生成结点 | 扩展结点 | 用时 ms |
| --- | --- | --- | --- | --- | --- |
| 1 | Dijkstra | 21 | 172232 | 63122 | 397 |
| 1 | 曼哈顿 | 21 | 2489 | 931 | 10 |
| 1 | 不在位将牌数 | 21 | 15677 | 5794 | 41 |
| 2 | Dijkstra | 31 | 483838 | 181439 | 939 |
| 2 | 曼哈顿 | 31 | 55925 | 21197 | 119 |
| 2 | 不在位将牌数 | 31 | 385076 | 143848 | 914 |
| 3 | Dijkstra | 31 | 483836 | 181438 | 982 |
| 3 | 曼哈顿 | 31 | 55925 | 21197 | 134 |
| 3 | 不在位将牌数 | 31 | 385076 | 143848 | 699 |
| 4 | 曼哈顿（4×4） | 52 | 1010304 | 339410 | 1896 |

三个 3×3 用例上曼哈顿距离 A* 与 Dijkstra 的步数完全一致，说明搜索流程与容器没有破坏最优性；8-puzzle 的 Dijkstra 扩展数接近整个可达状态空间（9!/2 = 181440），也印证了解是最优的。

四个用例的**扩展结点数与 `benchmarks/astar-dell.txt` 的参考结果完全一致**（931、21197、21197、339410），步数也一致（21、31、31、52），说明容器与启发函数的行为与参考实现吻合。生成结点数按当前框架 `BestFirstSearcher` 的口径统计（每个子结点都计数，含被 Explored 判定为重复的结点），与参考文件里“共生成”的数值不可直接比较，对比时只比较步数与扩展结点数。

`benchmarks/` 中保留了参考结果，正式实验数据按开发规范放入 `benchmarks/runs/<实验名称>/`，由 D 统一汇总。

## 已知限制

- 替换结点使用 `PriorityQueue.remove`，为 O(n) 扫描；当前规模下不构成瓶颈，已记录在 `PqFrontier` 的类注释中。
- `BlankDistanceHeuristic` 未接入 `HeuristicType`，需要在跨模块约定后决定是否扩展枚举。
- `PuzzleFeeder.getProblems()` 与 `NPuzzleProblem.showSolution()` 仍待 C 实现，`SearchTester - NPuzzle` 暂时不能直接运行。
