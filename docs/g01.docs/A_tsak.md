# A 模块开发文档

## 负责范围

A 负责 N-Puzzle 的基础状态模块和可解性判断：

- 棋盘状态保存、复制和文本打印；
- 空格位置查找和合法移动；
- `equals()`、`hashCode()` 状态比较；
- 逆序数计算和 N-Puzzle 可解性判断；
- 为 B、C、D 提供统一的状态接口；
- 负责项目 pre 的讲述准备，汇报内容由 E 整理到 PPT。

本模块不负责 Frontier、启发函数、A*、IDA*、PDB、输入 Feeder 和可视化。

## 状态表示约定

`PuzzleBoard` 位于：

```text
WattAi/src/stud/g01/problem/npuzzle/PuzzleBoard.java
```

棋盘使用一维 `int[]` 保存，棋盘边长由 `size` 保存，空格使用数字 `0`：

```text
1 2 3
4 5 6  ->  [1, 2, 3, 4, 5, 6, 7, 0, 8]
7   8
```

二维坐标和一维下标的换算为：

```java
int row = index / size;
int col = index % size;
int index = row * size + col;
```

棋盘构造时会检查：

- 边长大于 `0`；
- 数组长度等于 `size * size`；
- 数字范围为 `0` 到 `size * size - 1`；
- 每个数字只出现一次；
- 棋盘中恰好存在一个空格 `0`。

构造方法和 `getTilesCopy()` 都使用数组复制，禁止外部代码直接修改棋盘内部状态。后继状态也必须复制父棋盘后再交换数字。

## 动作约定

`PuzzleMove` 位于：

```text
WattAi/src/stud/g01/problem/npuzzle/PuzzleMove.java
```

四个方向表示空格的移动方向：

| 动作 | 含义 | 一维下标变化 |
| --- | --- | --- |
| `UP` | 空格向上 | `-size` |
| `DOWN` | 空格向下 | `+size` |
| `LEFT` | 空格向左 | `-1` |
| `RIGHT` | 空格向右 | `+1` |

每次移动代价为 `1`。`PuzzleBoard.actions()` 返回四个候选动作，`canMove()` 和 `NPuzzleProblem.applicable()` 负责过滤越界动作。

## 状态相等与不可变性

两个棋盘在边长和数字排列都相同的情况下相等。`equals()` 和 `hashCode()` 必须使用同一组状态数据，以便 B 的 Open/Closed 容器正确去重。

状态创建后不应修改：

```java
PuzzleBoard next = (PuzzleBoard) board.next(move);
```

`next` 是新对象，`board` 必须保持不变。不要把外部传入的数组直接赋值给 `tiles`，也不要返回内部数组本身。

## 可解性判断

`NPuzzleProblem.solvable()` 先排除非 `PuzzleBoard` 状态和边长不一致的情况，然后分别计算初始状态和目标状态的逆序数。

逆序数统计时忽略空格 `0`。对于任意 `i < j`，若：

```text
tiles[i] > tiles[j]
```

则计数加一。

棋盘宽度为奇数时，初始状态和目标状态的逆序数奇偶性必须相同。棋盘宽度为偶数时，比较：

```text
逆序数 + 空格从底部数起的行号
```

两种状态的奇偶性。空格所在底行的行号为 `1`。

## 交付前检查

A 模块交付前至少验证：

1. 输入数组被复制，修改原数组不影响棋盘；
2. 空格在角落、边缘和中间时，合法动作数量正确；
3. 移动后父棋盘保持不变；
4. 相同棋盘的 `equals()` 为 `true` 且哈希值相同；
5. 重复数字、越界数字、缺少空格会被拒绝；
6. 3×3 和 4×4 的可解、不可解样例判断正确；
7. 全部 Java 源码能够按 UTF-8 编译。

编译命令：

```powershell
$sources = @(Get-ChildItem ./WattAi/src -Recurse -Filter '*.java' | ForEach-Object FullName)
javac -encoding UTF-8 -d ./out/a-check @sources
```

完成验证后，把状态表示、动作方向、可解性公式和测试结果同步给 B、C、D。A 的 pre 重点说明棋盘如何表示、一步移动如何生成新状态，以及为什么某些状态不可解。
