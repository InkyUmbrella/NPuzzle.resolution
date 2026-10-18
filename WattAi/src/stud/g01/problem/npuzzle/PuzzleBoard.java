package stud.g01.problem.npuzzle;

import core.problem.Action;
import core.problem.State;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class PuzzleBoard extends State {

    // 棋盘边长
    private final int size;

    // 一维数组保存棋盘，0表示空格
    private final int[] tiles;

    // 空格在一维数组中的位置
    private final int blankIndex;

    private static final List<PuzzleMove> ALL_ACTIONS = List.of(
            new PuzzleMove(PuzzleMove.Direction.UP),
            new PuzzleMove(PuzzleMove.Direction.DOWN),
            new PuzzleMove(PuzzleMove.Direction.LEFT),
            new PuzzleMove(PuzzleMove.Direction.RIGHT)
    );

    public PuzzleBoard(int size, int[] tiles) {
        if (size <= 0) {
            throw new IllegalArgumentException("棋盘边长必须大于 0");
        }

        if (tiles == null || tiles.length != size * size) {
            throw new IllegalArgumentException("棋盘数据长度错误");
        }

        this.size = size;

        this.tiles = tiles.clone();

        this.blankIndex = validateAndFindBlank(this.tiles);
    }


    private static int validateAndFindBlank(int[] tiles) {
        boolean[] used = new boolean[tiles.length];
        int blankIndex = -1;

        for (int i = 0; i < tiles.length; i++) {
            int tile = tiles[i];

            if (tile < 0 || tile >= tiles.length) {
                throw new IllegalArgumentException("棋盘数字超出范围");
            }

            if (used[tile]) {
                throw new IllegalArgumentException("棋盘中存在重复数字");
            }

            used[tile] = true;

            if (tile == 0) {
                blankIndex = i;
            }
        }

        if (blankIndex == -1) {
            throw new IllegalArgumentException("棋盘中没有空格");
        }

        return blankIndex;
    }

    public int getSize() {
        return size;
    }

    public int getBlankIndex() {
        return blankIndex;
    }

    public int getTile(int index) {
        if (index < 0 || index >= tiles.length) {
            throw new IndexOutOfBoundsException("棋盘下标越界");
        }

        return tiles[index];
    }

    public int[] getTilesCopy() {
        return tiles.clone();
    }

    @Override
    public void draw() {
        for (int row = 0; row < size; row++) {
            for (int col = 0; col < size; col++) {
                int value = tiles[row * size + col];

                if (value == 0) {
                    System.out.print("  ");
                } else {
                    System.out.printf("%2d", value);
                }

                System.out.print(" ");
            }

            System.out.println();
        }
    }
    @Override
    public String toString() {
        return Arrays.toString(tiles);
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PuzzleBoard other)) {
            return false;
        }

        return size == other.size
                && Arrays.equals(tiles, other.tiles);
    }

    @Override
    public int hashCode() {
        return 31 * size + Arrays.hashCode(tiles);
    }

    public boolean canMove(PuzzleMove move) {
        Objects.requireNonNull(move, "动作不能为 null");

        int row = blankIndex / size;
        int col = blankIndex % size;

        return switch (move.getDirection()) {
            case UP -> row > 0;
            case DOWN -> row < size - 1;
            case LEFT -> col > 0;
            case RIGHT -> col < size - 1;
        };
    }

    private int destinationIndex(PuzzleMove.Direction direction) {
        return switch (direction) {
            case UP -> blankIndex - size;
            case DOWN -> blankIndex + size;
            case LEFT -> blankIndex - 1;
            case RIGHT -> blankIndex + 1;
        };
    }

    @Override
    public State next(Action action) {
        if (!(action instanceof PuzzleMove move)) {
            throw new IllegalArgumentException("动作类型必须是 PuzzleMove");
        }

        if (!canMove(move)) {
            throw new IllegalArgumentException("当前棋盘不能执行动作：" + move);
        }

        int[] nextTiles = tiles.clone();

        int destinationIndex = destinationIndex(move.getDirection());

        // 交换空格和目标位置的数字
        nextTiles[blankIndex] = nextTiles[destinationIndex];
        nextTiles[destinationIndex] = 0;

        return new PuzzleBoard(size, nextTiles);
    }

    @Override
    public Iterable<? extends Action> actions() {
        return ALL_ACTIONS;
    }
}
