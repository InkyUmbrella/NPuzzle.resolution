package stud.g01.solver;

import core.solver.queue.Node;
import java.util.Deque;

public class SearchResult {
    private final Deque<Node> path;
    private final int expandedCount;
    private final long timeMs;

    public SearchResult(Deque<Node> path, int expandedCount, long timeMs) {
        this.path = path;
        this.expandedCount = expandedCount;
        this.timeMs = timeMs;
    }

    public Deque<Node> getPath() {
        return path;
    }

    public int getExpandedCount() {
        return expandedCount;
    }

    public long getTimeMs() {
        return timeMs;
    }

    public boolean isFound() {
        return path != null && !path.isEmpty();
    }
}

