# 相对原始框架的修改说明

本文记录运行问题排查、源码编码统一和 GitHub 协作准备过程中修改或新增的文件及作用。原始课程框架没有 Git 提交历史，说明依据本次操作记录和编码转换前备份整理。

本文及第三方声明现统一存放于 `docs/`；下文表格中的文件名保留首次上传时的记录，源码路径均相对仓库根目录。

当前 Git 仓库根目录为内层 `NPuzzle.resolution`，实际源码位于 `WattAi/src`。算法实现仍由小组继续完成。

## 1. Java 源码中的编译修复

### `WattAi/src/core/runner/EngineFeeder.java`

原代码引用：

```java
import stud.solver.IdAStar;
```

修改为实际存在的包：

```java
import stud.g01.solver.IdAStar;
```

作用：修复 `stud.solver` 包不存在以及无法解析 `IdAStar` 的编译错误。这个文件同时由 GBK 转为 UTF-8，中文注释和原有方法内容保留。

### `WattAi/src/stud/g01/solver/IdAStar.java`

原代码仅有 `IdAStar(Frontier frontier)` 构造函数，但 `EngineFeeder` 调用的是 `new IdAStar(frontier, predictor)`。

修改内容：

- 引入 `core.solver.algorithm.heuristic.Predictor`。
- 增加 `protected final Predictor predictor` 字段，保存传入的启发函数。
- 增加 `IdAStar(Frontier frontier, Predictor predictor)` 构造函数，与框架调用匹配。
- 保留单参数构造函数，委托给双参数构造函数，使用返回 `0` 的默认启发函数。

作用：修复构造函数参数不匹配，保留原有单参数调用方式，并为后续 IDA* 实现提供启发函数字段。

`search(Problem problem)` 仍返回 `null`，本次未实现 IDA* 算法。

## 2. 源码编码统一

全部 31 个 Java 文件现在采用 UTF-8（无 BOM）。编码转换只改变文件字节编码，不改变解码后的文字、中文注释、字符串、算法逻辑或原有换行。

`EngineFeeder.java` 在前述修复时已单独由 GBK 转为 UTF-8。之后统一编码时，以下 16 个文件由 GBK 转为 UTF-8：

| 文件（路径相对 `WattAi/src`） | 内容作用 |
| --- | --- |
| `core/problem/Problem.java` | 问题抽象 |
| `core/problem/ProblemType.java` | 问题类型枚举 |
| `core/problem/State.java` | 状态抽象 |
| `core/runner/SearchTester.java` | 程序入口和实验测试流程 |
| `core/solver/algorithm/heuristic/HeuristicType.java` | 启发函数类型枚举 |
| `core/solver/algorithm/heuristic/Predictor.java` | 启发函数接口 |
| `core/solver/algorithm/searcher/BestFirstSearcher.java` | 框架提供的搜索算法 |
| `core/solver/queue/EvaluationType.java` | 结点评估类型枚举 |
| `core/solver/queue/Frontier.java` | 搜索边界队列接口 |
| `stud/problem/pathfinding/Direction.java` | 寻路方向 |
| `stud/problem/pathfinding/GridType.java` | 地图格子类型 |
| `stud/problem/pathfinding/Move.java` | 寻路移动动作 |
| `stud/problem/pathfinding/PathFinding.java` | 寻路问题实现 |
| `stud/queue/ListFrontier.java` | 列表队列实现 |
| `stud/queue/QueueFrontier.java` | 队列实现 |
| `stud/runner/WalkerFeeder.java` | 寻路测试所需对象的创建 |

除 `EngineFeeder.java` 和 `IdAStar.java` 的编译修复外，其他 Java 文件没有新增功能或修改算法逻辑。原本已经使用 UTF-8 的文件在统一编码过程中未重写。

转换后逐文件验证了文本内容一致性，全部源码通过 `javac -encoding UTF-8` 编译。上传准备另通过 `.gitattributes` 规范 Git 中的文本换行为 LF；这与编码转换是两项独立操作。

## 3. IDEA 项目和运行配置

| 文件 | 修改或保留内容 | 作用 |
| --- | --- | --- |
| `.idea/encodings.xml` | 将项目编码统一设为 UTF-8，移除逐文件 GBK 设置 | 避免 IDEA 继续按旧编码读取源码 |
| `.run/SearchTester - PathFinding.run.xml` | 新增共享 Application 配置，主类为 `core.runner.SearchTester`，工作目录为 `$PROJECT_DIR$/WattAi` | 让协作者能够直接运行已实现的寻路示例 |
| `.run/SearchTester - NPuzzle.run.xml` | 新增共享 Application 配置，使用同一个入口和工作目录，传入 NPuzzle 参数 | 让协作者能够启动 NPuzzle 实验入口 |
| `.idea/misc.xml` | 保留项目 JDK 21 设置 | 共享 Java 语言级别；协作者仍需选择本机 JDK |
| `.idea/modules.xml` | 保留当前 `NPuzzle.resolution` 模块登记 | 让 IDEA 找到共享模块文件 |
| `NPuzzle.resolution.iml` | 保留 `WattAi/src` 作为 Sources Root 的配置 | 让 IDEA 正确识别 Java 包和入口 |

两个运行配置的参数如下：

```text
PathFinding: resources/pathfinding.txt PATHFINDING 1 stud.runner.WalkerFeeder
NPuzzle:     resources/problems.txt NPUZZLE 1 stud.g01.runner.PuzzleFeeder
```

实验指导书第 9 页的 NPuzzle 参数与此一致。指导书的寻路参数写作 `stud.g01.runner.WalkerFeeder`，当前源码实际类名为 `stud.runner.WalkerFeeder`，因此运行配置采用实际包名。

四个参数依次表示输入文件、问题类型、实验阶段和 Feeder 类。显式设置工作目录，是为了让 `resources/...` 相对路径正确指向输入数据。

本地还同步更新了外层资料目录及 `WattAi` 旧项目的编码设置，并在外层资料项目中补齐运行配置、移除失效的源码路径。它们不属于本 Git 仓库。个人 `.idea/workspace.xml` 中保存的默认运行目标也不上传。

## 4. GitHub 协作准备新增文件

| 文件 | 作用 |
| --- | --- |
| `README.md` | 说明项目状态、JDK 21 环境、IDEA 和命令行运行方式、目录结构及分工建议 |
| `.gitignore` | 排除编译输出、个人编辑器配置、本地凭据文件和 `WattAi` 内旧的嵌套 IDEA 配置；保留必要的共享项目文件 |
| `.gitattributes` | 自动识别文本文件，为 Java、XML、Markdown 等文本指定 LF 换行 |
| `.editorconfig` | 为后续编辑约定 UTF-8、LF 和文件末尾换行 |
| `.github/workflows/java.yml` | 在 push 和 Pull Request 时，使用 JDK 21 编译源码并运行 PathFinding 示例 |
| `THIRD_PARTY_NOTICES.md` | 记录三个 `algs4.util` 工具类的来源、版权及 GPL-3.0-or-later 声明 |
| `LICENSES/GPL-3.0-or-later.txt` | 提供第三方工具类声明所引用的 GNU GPL v3 原文，不代表为整个课程项目另行指定许可证 |
| `SOURCE_CHANGES.md` | 本修改说明，便于协作者区分框架调整与后续实验实现 |

原有工具类的版权注释继续保留。许可证副本取自 SPDX license-list-data 的 `GPL-3.0-or-later.txt`。

后续新增根目录 [LICENSE](../LICENSE)，采用 MIT 许可，署名为 `2026 InkyUmbrella`，适用范围为维护者有权授权的原创代码、修改和文档。第三方代码及原课程框架的授权范围见 [第三方声明](THIRD_PARTY_NOTICES.md)。

首次上传的 Git 仓库仅包含内层代码项目，课程 PDF、Word 资料、外层测试结果及本地编码备份未纳入上传内容。后续将 A*、IDA* 测试用例、参考结果和测试机信息迁入 `benchmarks/`。

## 5. 验证结果与待完成工作

- 全部 31 个 Java 文件可按 UTF-8 编译。
- 仅使用 Git 暂存内容导出的干净副本，也能编译并运行 PathFinding，退出代码为 `0`。
- 编译产物和个人 IDEA 配置已确认被 Git 忽略。
- GitHub Actions 工作流的 YAML 已解析检查；首次推送后的实际执行结果以 GitHub Actions 页面为准。

NPuzzle 尚未完成：`PuzzleFeeder` 的对象创建方法、`PuzzleBoard`、`NPuzzleProblem`、小组优先队列及 IDA* 仍需实现。当前运行 NPuzzle 会因为 `PuzzleFeeder.getProblems()` 返回 `null` 而在 `SearchTester` 中触发空指针异常。这是待完成的实验代码，不是运行配置或编码问题。
