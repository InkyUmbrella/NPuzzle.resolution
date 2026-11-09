package dmodule.pattern;

import java.util.HashSet;
import java.util.Set;

public class Pattern {
    public final Set<Integer> tiles;

    public Pattern(Set<Integer> tiles) {
        this.tiles = new HashSet<>(tiles);
    }

    public boolean containsTile(int tile) {
        return tiles.contains(tile);
    }

    public static Pattern[] buildDisjointPatterns() {
        // 4-4-4-3 划分（这是能让你 0.2 秒建表且成功跑出随机样例的核心）
        Pattern p1 = new Pattern(Set.of(1, 2, 5, 6));
        Pattern p2 = new Pattern(Set.of(3, 4, 7, 8));
        Pattern p3 = new Pattern(Set.of(9, 10, 13, 14));
        Pattern p4 = new Pattern(Set.of(11, 12, 15));
        return new Pattern[]{p1, p2, p3, p4};
    }
}
