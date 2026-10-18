# NPuzzle.resolution

基于 SearchFramework 的 Java 搜索算法课程项目，用于实现 NPuzzle（数字华容道）的状态表示、启发函数和求解算法。仓库保留已实现的地图寻路示例，便于参考和验证开发环境。

## 当前状态

- 全部 31 个 Java 源文件使用 UTF-8，能够编译。
- PathFinding 示例可以运行，使用网格距离和欧氏距离启发函数。
- NPuzzle 和 IDA* 仍有待实现的方法。运行 NPuzzle 时，`PuzzleFeeder.getProblems()` 返回 `null`，目前会触发空指针异常。

相对原始课程框架的文件变更及作用见 [框架修改说明](docs/DEVELOPMENT_PREPARATION/SOURCE_CHANGES.md)。参与开发前请阅读 [开发规范](docs/DEVELOPMENT_PREPARATION/DEVELOPMENT.md)。

## 环境

- JDK 21，命令行开发需要 `java` 和 `javac` 可用。
- IntelliJ IDEA，可直接打开本仓库根目录。
- 当前采用普通 Java 项目结构，无 Maven/Gradle 和外部 JAR 依赖。

## 在 IDEA 中运行

1. 克隆仓库后，打开包含本 README、`NPuzzle.resolution.iml` 和 `WattAi` 的目录。
2. 在 `File > Project Structure > Project` 中选择本机 JDK 21；模块 SDK 选择继承项目 SDK。共享配置中的 SDK 名称为 `21`，若本机名称不同，请重新选择。
3. 确认 `WattAi/src` 被识别为 Sources Root，项目编码为 UTF-8。
4. 在右上角运行配置列表选择 `SearchTester - PathFinding`，运行以验证环境。
5. 完成 NPuzzle 相关代码后，选择 `SearchTester - NPuzzle` 运行实验。

程序入口是 `core.runner.SearchTester`。输入数据文件和没有 `main` 方法的类不能作为“当前文件”直接运行。

| 运行配置 | 传给 main 的参数 |
| --- | --- |
| SearchTester - PathFinding | `resources/pathfinding.txt PATHFINDING 1 stud.runner.WalkerFeeder` |
| SearchTester - NPuzzle | `resources/problems.txt NPUZZLE 1 stud.g01.runner.PuzzleFeeder` |

四个参数依次为输入文件、问题类型、实验阶段和 Feeder 类。两个共享配置的工作目录均为 `WattAi`；NPuzzle 阶段 `1` 使用错位数和曼哈顿距离启发函数。

## 在命令行中运行

从仓库根目录执行以下 PowerShell 命令：

```powershell
$sources = @(Get-ChildItem ./WattAi/src -Recurse -Filter '*.java' | ForEach-Object FullName)
javac -encoding UTF-8 -d ./out/cli @sources
if ($LASTEXITCODE -ne 0) { throw 'Compilation failed' }
Push-Location ./WattAi
try {
    java -cp ../out/cli core.runner.SearchTester resources/pathfinding.txt PATHFINDING 1 stud.runner.WalkerFeeder
    if ($LASTEXITCODE -ne 0) { throw 'PathFinding failed' }
} finally {
    Pop-Location
}
```

完成 NPuzzle 代码后，在 `WattAi` 目录中运行：

```powershell
java -cp ../out/cli core.runner.SearchTester resources/problems.txt NPUZZLE 1 stud.g01.runner.PuzzleFeeder
```

## 目录与分工

```text
WattAi/
    src/algs4/util/             三方计时和随机数工具
    src/core/                   问题抽象、搜索框架和程序入口
    src/stud/problem/           已实现的地图寻路问题
    src/stud/runner/            地图寻路 Feeder
    src/stud/queue/             框架提供的队列
    src/stud/g01/problem/       NPuzzle 问题和棋盘，待实现
    src/stud/g01/runner/        PuzzleFeeder，待实现
    src/stud/g01/queue/         小组优先队列，待实现
    src/stud/g01/solver/        IDA*，待实现
    resources/                  NPuzzle 和寻路输入数据
.run/                           IDEA 共享运行配置
.github/workflows/              Java 21 编译和寻路示例检查
benchmarks/                     A*、IDA* 测试用例和参考结果
docs/
     COURSE_RESOURCES/          课程资源
     DEVELOPMENT_PREPARATION/   开发规范、成员分工、框架修改说明
     DEVELOPMENT_PROGRESSION/   开发进程声明
     THIRD_PARTY_NOTICES.md     第三方声明
LICENSES/                       第三方许可证原文
LICENSE                         MIT 许可证
```

[benchmarks/](benchmarks/) 包含 A*、IDA* 的测试用例、使用曼哈顿距离的测试结果，以及测试机信息。

五位成员的职责、交付物和协作安排见 [小组分工方案](docs/DEVELOPMENT_PREPARATION/TEAM_ASSIGNMENT.md)，包括 A 负责项目汇报、E 负责汇报 PPT。先约定方法签名和状态相等规则，再通过各自分支提交 Pull Request；合并前至少确保编译和 PathFinding 检查通过。

源码统一使用 UTF-8（无 BOM），文本文件通过 Git 统一为 LF 换行。共享 `.run`、必要的 IDEA 项目配置和模块文件；个人工作区、嵌套旧项目配置及编译输出由 `.gitignore` 排除。

## 来源与许可

本项目基于课程提供的 SearchFramework 框架。测试用例、参考结果及测试机信息收录于 `benchmarks/`；其他课程 PDF、Word 资料未包含在本代码仓库中。

本仓库维护者有权授权的原创代码、修改和文档采用 [MIT 许可证](LICENSE)，版权署名为 `2026 InkyUmbrella`。

`algs4.util` 的三个工具类继续适用 GPL-3.0-or-later，MIT 许可证不替代其原有条款；分发包含这些工具类的组合程序时仍需遵守适用的 GPL 要求。课程框架原有代码未附整体许可证，其授权需以原权利人的许可为准。详细信息见 [第三方声明](docs/THIRD_PARTY_NOTICES.md)。
