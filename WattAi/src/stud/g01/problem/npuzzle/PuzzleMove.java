package stud.g01.problem.npuzzle;

import core.problem.Action;

import java.util.Objects;

public final class PuzzleMove extends Action {
    public enum Direction {
        UP,
        DOWN,
        LEFT,
        RIGHT
    }

    private final Direction direction;

    public PuzzleMove(Direction direction) {
        this.direction = Objects.requireNonNull(
                direction,
                "移动方向不能为 null"
        );
    }

    public Direction getDirection() {
        return direction;
    }

    @Override
    public void draw() {
        System.out.println(direction);
    }

    @Override
    public int stepCost() {
        return 1;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof PuzzleMove other)) {
            return false;
        }

        return direction == other.direction;
    }

    @Override
    public int hashCode() {
        return direction.hashCode();
    }

    @Override
    public String toString() {
        return direction.name();
    }
}
