package dmodule.pattern;

import stud.g01.problem.npuzzle.PuzzleBoard;
import java.util.Arrays;

public class PatternState {
    // 将状态直接压缩为一个 long 型数字，极大节省内存
    public final long key;

    // 从真实棋盘构造
    public PatternState(PuzzleBoard board, Pattern pattern) {
        int[] positions = new int[16];
        Arrays.fill(positions, -1);
        int blankPos = -1;
        int len = board.getSize() * board.getSize();

        for (int pos = 0; pos < len; pos++) {
            int tile = board.getTile(pos);
            if (tile == 0) {
                blankPos = pos;
            } else if (pattern.containsTile(tile)) {
                positions[tile] = pos;
            }
        }
        this.key = encode(positions, blankPos);
    }

    // 从数组构造并编码（供 PDB 生成使用）
    public static long encode(int[] positions, int blankPos) {
        long k = blankPos; // 低 4 位存空格
        for (int i = 1; i <= 15; i++) {
            if (positions[i] != -1) {
                k = (k << 4) | positions[i]; // 依次将每个方块的位置塞进去
            }
        }
        return k;
    }
}
