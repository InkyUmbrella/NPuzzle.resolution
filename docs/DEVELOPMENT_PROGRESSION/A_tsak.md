# A 模块开发记录与接口约定

## 已完成内容

A 已完成 N-Puzzle 基础状态模块和可解性判断，代码位于：

```text
WattAi/src/stud/g01/problem/npuzzle/PuzzleBoard.java
WattAi/src/stud/g01/problem/npuzzle/PuzzleMove.java
WattAi/src/stud/g01/problem/npuzzle/NPuzzleProblem.java
```

当前实现包含：

- 一维数组棋盘状态；
- `0` 空格表示；
- 棋盘数据复制与输入校验；
- 空格位置记录；
- 棋盘文本打印；
- 四个方向的移动动作；
- 后继状态生成；
- 状态相等比较和哈希计算；
- 合法移动判断；
- 逆序数计算；
- 奇数、偶数宽度棋盘的可解性判断；
- 3×3、4×4 棋盘基础行为验证。

## 棋盘状态接口

`PuzzleBoard` 继承框架中的 `State`：

```java
public final class PuzzleBoard extends State
```

构造方法：

```java
public PuzzleBoard(int size, int[] tiles)
```

参数约定：

| 参数 | 含义 |
| --- | --- |
| `size` | 棋盘边长，例如 `3` 表示 3×3 棋盘 |
| `tiles` | 按行展开的一维棋盘数据 |

棋盘数组使用 `0` 表示空格，数字范围为 `0` 到 `size * size - 1`。例如：

```java
new PuzzleBoard(
        3,
        new int[]{1, 2, 3, 4, 5, 6, 7, 0, 8}
);
```

对应棋盘：

```text
1 2 3
4 5 6
7   8
```

二维坐标与一维下标的换算：

```java
int row = index / size;
int col = index % size;
int index = row * size + col;
```

### 查询方法

| 方法 | 返回内容 |
| --- | --- |
| `getSize()` | 棋盘边长 |
| `getBlankIndex()` | 空格在一维数组中的下标 |
| `getTile(int index)` | 指定下标上的数字 |
| `getTilesCopy()` | 棋盘数据副本 |
| `toString()` | 一维数组形式的状态文本 |
| `draw()` | 按棋盘布局打印状态 |

`getTilesCopy()` 返回独立数组。调用方通过副本读取或处理棋盘数据，原状态保持稳定。

### 状态比较

两个 `PuzzleBoard` 在 `size` 和所有格子数字均相同时相等：

```java
board1.equals(board2)
```

相等棋盘具有相同的 `hashCode()`，可以直接用于 B 的 `HashSet`、`HashMap` 和 Closed 集合。

## 移动动作接口

`PuzzleMove` 继承框架中的 `Action`：

```java
public final class PuzzleMove extends Action
```

方向枚举：

```java
public enum Direction {
    UP,
    DOWN,
    LEFT,
    RIGHT
}
```

方向表示空格的移动方向：

| 方向 | 空格移动 | 一维下标变化 |
| --- | --- | --- |
| `UP` | 向上 | `-size` |
| `DOWN` | 向下 | `+size` |
| `LEFT` | 向左 | `-1` |
| `RIGHT` | 向右 | `+1` |

动作接口：

| 方法 | 约定 |
| --- | --- |
| `getDirection()` | 返回方向枚举 |
| `stepCost()` | 每次移动返回 `1` |
| `draw()` | 打印方向 |
| `toString()` | 返回 `UP`、`DOWN`、`LEFT` 或 `RIGHT` |

`PuzzleMove` 创建后方向保持不变。相同方向的动作对象通过 `equals()` 判断为相等，并具有相同哈希值。

## 状态转移接口

`PuzzleBoard.actions()` 返回四个候选动作：

```java
Iterable<? extends Action> actions()
```

候选动作顺序为：

```text
UP、DOWN、LEFT、RIGHT
```

`PuzzleBoard.canMove(PuzzleMove move)` 根据空格所在行列判断方向是否越界：

```java
boolean canMove(PuzzleMove move)
```

`PuzzleBoard.next(Action action)` 生成后继状态：

```java
State next(Action action)
```

状态转移过程为：

1. 检查动作类型；
2. 检查空格移动方向；
3. 复制当前棋盘数组；
4. 交换空格与目标位置数字；
5. 创建并返回新的 `PuzzleBoard`。

父状态和后继状态使用不同数组，搜索树中的状态可以独立保存。

## NPuzzleProblem 接口

构造方法：

```java
public NPuzzleProblem(State initialState, State goal)
public NPuzzleProblem(State initialState, State goal, int size)
```

### `applicable()`

```java
public boolean applicable(State state, Action action)
```

该方法将框架中的通用 `State` 和 `Action` 转换为 `PuzzleBoard`、`PuzzleMove`，并调用：

```java
board.canMove(move)
```

`Problem.childNodes()` 先遍历 `actions()`，再使用 `applicable()` 筛选合法动作，最后调用 `next()` 创建子状态。

### `stepCost()`

```java
public int stepCost(State state, Action action)
```

NPuzzle 每次移动代价为 `1`，实现通过 `PuzzleMove.stepCost()` 返回该值。

### `solvable()`

```java
public boolean solvable()
```

实现过程：

1. 读取初始棋盘和目标棋盘；
2. 计算两个棋盘的逆序数；
3. 忽略空格 `0`；
4. 奇数宽度比较逆序数奇偶性；
5. 偶数宽度比较“逆序数 + 空格从底部数起的行号”的奇偶性。

逆序数定义为：对于一维数组中 `i < j` 的两个非空格数字，当：

```text
tiles[i] > tiles[j]
```

计数加一。

空格从底部数起的行号从 `1` 开始。目标棋盘参与同样的计算，因此支持传入统一规则下的目标状态。

## 与其他模块的接口交接

### B：Open/Closed 与启发函数

B 使用以下状态接口进行去重：

```java
PuzzleBoard.equals(Object object)
PuzzleBoard.hashCode()
PuzzleBoard.getTilesCopy()
```

棋盘状态的相等判断只依赖边长和数字排列。

### C：A* 与 IDA*

C 通过框架的 `Problem` 接口调用：

```java
problem.solvable()
problem.childNodes(node)
problem.goal(state)
```

`childNodes()` 内部使用 `actions()`、`applicable()`、`next()` 和 `stepCost()` 完成后继结点生成。

### D：随机用例与实验

D 生成的棋盘数据按照以下格式创建 `PuzzleBoard`：

```java
new PuzzleBoard(size, tiles)
```

随机状态可通过 `NPuzzleProblem.solvable()` 进行可解性判断。实验统计使用同一棋盘状态表示和同一目标状态。

## 已验证结果

当前 A 模块已完成以下验证：

- Java 源码按 UTF-8 编译通过；
- 输入数组复制后，外部数组修改不影响棋盘；
- 后继状态生成后，父棋盘保持不变；
- 空格移动边界判断通过；
- 相同棋盘的 `equals()` 和 `hashCode()` 一致；
- 3×3 可解和不可解状态判断通过；
- 4×4 可解和不可解状态判断通过。
